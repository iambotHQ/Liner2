package g419.liner2.daemon.utils;

import g419.corpus.HasLogger;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.liner2.core.Liner2;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.util.LinkedHashSet;
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
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    static class LinerServerImpl extends LinerGrpc.LinerImplBase implements HasLogger {
        final Liner2 linerService;

        public LinerServerImpl(final Liner2 linerService) {
            this.linerService = linerService;
        }

        private EntityType mapEntityType(String entType) throws Exception {
            switch(entType) {
                case "date":
                    return EntityType.DATE;
                case "geogname":
                    return EntityType.GEOG_NAME;
                case "orgname":
                    return EntityType.ORG_NAME;
                case "persname":
                    return EntityType.PERS_NAME;
                case "persname_addName":
                    return EntityType.PERS_NAME_ADD_NAME;
                case "persname_forename":
                    return EntityType.PERS_NAME_FORENAME;
                case "persname_surname":
                    return EntityType.PERS_NAME_SURNAME;
                case "placename":
                    return EntityType.PLACE_NAME;
                case "placename_bloc":
                    return EntityType.PLACE_NAME_BLOC;
                case "placename_country":
                    return EntityType.PLACE_NAME_COUNTRY;
                case "placename_district":
                    return EntityType.PLACE_NAME_DISTRICT;
                case "placename_region":
                    return EntityType.PLACE_NAME_REGION;
                case "placename_settlement":
                    return EntityType.PLACE_NAME_SETTLEMENT;
                case "time":
                    return EntityType.TIME;
                default:
                    throw new Exception("Unknown entity type `" + entType + "`");
            }
        }

        @Override
        public void tagNamedEntities(g419.liner2.daemon.utils.TagRequest request,
                                     io.grpc.stub.StreamObserver<g419.liner2.daemon.utils.TagResponse> responseObserver) {
            TagResponse.Builder reply = TagResponse.newBuilder();

            try {
                AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
                    "grpc input",
                    IOUtils.toInputStream(request.getText(), "UTF-8"),
                    "plain:wcrft"
                );
                Document text = reader.nextDocument();
                reader.close();

                Map<Sentence, AnnotationSet> chunked = linerService.chunk(text);
                for(Sentence sentence : chunked.keySet()) {
                    final LinkedHashSet<Annotation> chunks = sentence.getChunks();

                    for(Annotation ann : chunks) {
                        Entity.Builder entBuilder = Entity.newBuilder()
                                .setOrth(ann.getBaseText())
                                .setLemma(ann.getLemma())
                                .setEntityType(mapEntityType(ann.getType().toLowerCase()))
                                .setEntityId(ann.getChannelIdx());

                        reply.addEntites(entBuilder.build());
                    }
                }

                responseObserver.onNext(reply.build());
                responseObserver.onCompleted();
            } catch(Exception e) {
                getLogger().error("Error while tagging: ", e);
            }
        }
    }
}