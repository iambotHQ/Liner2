package g419.liner2.core.converter;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by michal on 9/17/14.
 */
public class AnnotationFlattenConverter extends Converter {

    ArrayList<String> categories;
    private Comparator<Annotation> flattenConparator;

    public AnnotationFlattenConverter(ArrayList<String> cats){
        this.categories = cats;

        flattenConparator = new Comparator<Annotation>() {
            public int compare(Annotation a, Annotation b) {
                if(a == b || Collections.disjoint(a.getTokens(), b.getTokens())){
                    return 0;
                }
                else {
                    if (a.getTokens().size() == b.getTokens().size()) {
                        return Integer.signum(categories.indexOf(a.getType()) - categories.indexOf(b.getType()));
                    }
                    return Integer.signum(b.getTokens().size() - a.getTokens().size());
                }
            }
        };
    }
    @Override
    public void finish(Document doc) {

    }

    @Override
    public void start(Document doc) {

    }

    @Override
    public void apply(Sentence sentence) {
        ArrayList<Annotation> toFlatten = new ArrayList<Annotation>();
        for(Annotation ann: sentence.getChunks()){
            if(categories.contains(ann.getType())){
                toFlatten.add(ann);
            }
        }
        for(Annotation annToRemove: flatten(toFlatten)){
            sentence.getChunks().remove(annToRemove);
        }
    }

    private HashSet<Annotation> flatten(ArrayList<Annotation> toFlatten){
        HashSet<Annotation> toRemove = new HashSet<Annotation>();
        for(Annotation ann: toFlatten){
            for(Annotation candidate: toFlatten){
                if(flattenConparator.compare(ann, candidate) == -1){
                    toRemove.add(candidate);
                }
            }
        }
        return toRemove;
    }
}
