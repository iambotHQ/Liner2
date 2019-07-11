package g419.liner2.daemon.utils;

import g419.corpus.HasLogger;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.corpus.structure.Annotation;
import g419.liner2.core.Liner2;
import g419.liner2.daemon.grpc.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class GrpcWorker implements HasLogger {
    private final Server server;

    final int port;
    final Liner2 liner2;
    final String hostname;

    public GrpcWorker(final String hostname, final int port, final String modelPath) throws Exception {
        this.port = port;
        this.hostname = hostname;
        liner2 = new Liner2(modelPath);
        server = ServerBuilder.forPort(port)
                .addService(new LinerServerImpl(liner2))
                .build();
    }

    public void run() {
        try {
            getLogger().info("Listing to gRPC on port {}", port);
            server.start();
            server.awaitTermination();
        } catch (final IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    static class LinerServerImpl extends LinerGrpc.LinerImplBase implements HasLogger {
        final Liner2 linerService;

        public LinerServerImpl(final Liner2 linerService) {
            this.linerService = linerService;
        }

        private AnnotationType mapAnnotationType(String entType) throws Exception {
            switch(entType) {
                case "date":
                    return AnnotationType.DATE;
                case "geogname":
                    return AnnotationType.GEOG_NAME;
                case "orgname":
                    return AnnotationType.ORG_NAME;
                case "persname":
                    return AnnotationType.PERS_NAME;
                case "persname_addname":
                    return AnnotationType.PERS_NAME_ADD_NAME;
                case "persname_forename":
                    return AnnotationType.PERS_NAME_FORENAME;
                case "persname_surname":
                    return AnnotationType.PERS_NAME_SURNAME;
                case "placename":
                    return AnnotationType.PLACE_NAME;
                case "placename_bloc":
                    return AnnotationType.PLACE_NAME_BLOC;
                case "placename_country":
                    return AnnotationType.PLACE_NAME_COUNTRY;
                case "placename_district":
                    return AnnotationType.PLACE_NAME_DISTRICT;
                case "placename_region":
                    return AnnotationType.PLACE_NAME_REGION;
                case "placename_settlement":
                    return AnnotationType.PLACE_NAME_SETTLEMENT;
                case "time":
                    return AnnotationType.TIME;
                default:
                    throw new Exception("Unknown entity type `" + entType + "`");
            }
        }

        @Override
        public void tagNamedEntities(TagRequest request,
                                     io.grpc.stub.StreamObserver<TagResponse> responseObserver) {
            TagResponse.Builder reply = TagResponse.newBuilder();

            try {
                AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
                    "grpc input",
                    IOUtils.toInputStream(request.getText(), "UTF-8"),
                    "plain:wcrft"
                );
                Document text = reader.nextDocument();
                reader.close();

                linerService.chunkInPlace(text);
                getLogger().debug("Num sentences: " + text.getSentences().size());
                int annChanelOffset = 0;
                for(int i = 0, s = text.getSentences().size(); i < s; i++) {
                    Sentence sentence = text.getSentences().get(i);
                    LinkedHashSet<Annotation> chunks = sentence.getChunks();
                    final List<Token> tokens = sentence.getTokens();

                    getLogger().debug("Num tokens: " + tokens.size());
                    int biggestAnnIdx = 0;
                    for(int j = 0; j < tokens.size(); j++) {
                        Token token = tokens.get(j);
                        List<Annotation> chunksInToken = sentence.getChunksAt(j);
                        getLogger().debug("Token: " + token.getOrth() + ", num chunks: " + chunksInToken.size());

                        if(chunksInToken.size() > 0) {
                            Entity.Builder entBuilder = Entity.newBuilder()
                                    .setOrth(token.getOrth())
                                    .setLemma(token.getDisambTag().getBase());

                            for (Annotation ann : chunksInToken) {
                                int annIdx = 1;
                                for (final Annotation a : chunks) {
                                    if (a.getType().equals(ann.getType())) {
                                        if (a == ann) {
                                            break;
                                        }
                                        annIdx++;
                                    }
                                }
                                biggestAnnIdx = Math.max(biggestAnnIdx, annIdx);

                                entBuilder.addAnnotations(g419.liner2.daemon.grpc.Annotation.newBuilder()
                                        .setAnnotationType(mapAnnotationType(ann.getType().toLowerCase()))
                                        .setChannelIdx(annIdx + annChanelOffset)
                                        .build());
                            }
                            reply.addEntities(entBuilder.build());
                        }
                    }
                    annChanelOffset += biggestAnnIdx;
                }

                responseObserver.onNext(reply.build());
                responseObserver.onCompleted();
            } catch(Exception e) {
                getLogger().error("Error while tagging: ", e);
            }
        }
    }
}