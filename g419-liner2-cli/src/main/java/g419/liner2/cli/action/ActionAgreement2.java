package g419.liner2.cli.action;

import com.google.common.collect.Sets;
import g419.corpus.ConsolePrinter;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.ChunkerEvaluator;
import g419.liner2.core.tools.ChunkerEvaluatorMuc;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Porównuje zbiory anotacji dla wskazanych korpusów. Korpusy porównywane są parami.
 *
 * @author Michał Marcińczuk
 */
public class ActionAgreement2 extends Action {

    boolean debug_flag = false;

    private String[] input_files = null;
    private String input_format = null;


    public ActionAgreement2() {
        super("agreement2");
        setDescription("compare sets of annotations for each pair of corpora. One set is treated as a reference set and the other as a set to evaluate. It is a refactored version of the agreement action.");
        options.addOption(CommonOptions.getInputFileFormatOption());
        options.addOption(CommonOptions.getInputFileNamesOption());
        options.addOption(CommonOptions.getVerboseDeatilsOption());

    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        input_files = line.getOptionValues(CommonOptions.OPTION_INPUT_FILE);

        input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        if (line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
            ConsolePrinter.verboseDetails = true;
        }

    }

    @Override
    public void run() throws Exception {
        final List<Pattern> types = new ArrayList<>();
        types.add(Pattern.compile(".+"));

        for (int i1 = 0; i1 < input_files.length; i1++) {
            for (int i2 = i1 + 1; i2 < input_files.length; i2++) {
                final ChunkerEvaluator globalEval = new ChunkerEvaluator(types, false);
                final ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(types);

                final String referenceSet = input_files[i1];
                final String compareSet = input_files[i2];
                int onlyInReferenceSet = 0;
                int onlyInCompareSet = 0;
                int differentSentenceCount = 0;
                int foundInBoth = 0;

                final AbstractDocumentReader originalDocument = ReaderFactory.get().getStreamReader(compareSet, input_format);
                final AbstractDocumentReader referenceDocument = ReaderFactory.get().getStreamReader(referenceSet, input_format);

                Document document;

                final Map<String, Document> documentSet1 = new HashMap<>();
                while ((document = originalDocument.nextDocument()) != null) {
                    documentSet1.put(document.getName(), document);
                }

                final Map<String, Document> documentSet2 = new HashMap<>();
                while ((document = referenceDocument.nextDocument()) != null) {
                    documentSet2.put(document.getName(), document);
                }

                final Set<String> names = Sets.newTreeSet();
                names.addAll(documentSet1.keySet());
                names.addAll(documentSet2.keySet());

                for (final String name : names) {
                    final Document d1 = documentSet1.get(name);
                    final Document d2 = documentSet2.get(name);

                    if (d1 == null || d2 == null) {
                        getLogger().warn("Dokument {} znajduje się tylko w {}", name, d1 == null ? compareSet : referenceSet);
                        if (d1 == null) {
                            onlyInCompareSet++;
                        } else {
                            onlyInReferenceSet++;
                        }
                    } else if (d1.getSentences().size() != d2.getSentences().size()) {
                        getLogger().warn("Dokument {} ma różną liczbę zdań: {} vs {}", name, d1.getSentences().size(), d2.getSentences().size());
                        differentSentenceCount++;
                    } else {
                        foundInBoth++;
                        for (int i = 0; i < d1.getSentences().size(); i++) {
                            final Sentence s1 = d1.getSentences().get(i);
                            final Sentence s2 = d2.getSentences().get(i);
                            final AnnotationSet set1 = new AnnotationSet(s1, s1.getChunks());
                            final AnnotationSet set2 = new AnnotationSet(s2, s2.getChunks());
                            globalEval.evaluate(s1, set1, set2);
                            globalEvalMuc.evaluate(s1, set1, set2);
                        }
                    }
                }

                System.out.println();
                System.out.println(StringUtils.repeat("-", 90));
                System.out.println("Reference set: " + referenceSet);
                System.out.println("Testing set  : " + compareSet);
                System.out.println(StringUtils.repeat("-", 90));
                System.out.println(String.format("Documents only in 'reference set'             : %4d", onlyInReferenceSet));
                System.out.println(String.format("Documents only in 'compare set'               : %4d", onlyInCompareSet));
                System.out.println(String.format("Documents with different number of sentences  : %4d", differentSentenceCount));
                System.out.println(String.format("Documents found in both sets                  : %4d", foundInBoth));
                globalEval.printResults();
                globalEvalMuc.printResults();
            }
        }
    }

}
