package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NkjpSyntacticChunks {

  static public Pattern annotationsPrep = Pattern.compile("^PrepNG.*$");
  static public Pattern annotationsNg = Pattern.compile("^NG.*$");

  static public void splitPrepNg(Sentence sentence) {
    final Map<Integer, List<Annotation>> mapTokenIdToAnnotations = Maps.newHashMap();
    for (Annotation an : sentence.getAnnotations(NkjpSyntacticChunks.annotationsNg)) {
      for (int i = an.getBegin(); i <= an.getEnd(); i++) {
        mapTokenIdToAnnotations.computeIfAbsent(i, o -> Lists.newLinkedList()).add(an);
      }
    }

    for (Annotation an : sentence.getAnnotations(NkjpSyntacticChunks.annotationsPrep)) {
      if (!mapTokenIdToAnnotations.containsKey(an.getBegin() + 1)) {
        Annotation ani = new Annotation(an.getBegin() + 1, an.getEnd(), an.getType().substring(4), an.getSentence());
        ani.setHead(an.getHead());
        sentence.addChunk(ani);
      } else {
        Integer newNgStart = null;
        for (int i = an.getBegin() + 1; i <= an.getEnd(); i++) {
          if (mapTokenIdToAnnotations.get(i) == null) {
            if (newNgStart == null) {
              newNgStart = i;
            }
          } else if (newNgStart != null) {
            sentence.addChunk(new Annotation(newNgStart, i - 1, "NG", sentence));
            newNgStart = null;
          }
        }
        if (newNgStart != null) {
          sentence.addChunk(new Annotation(newNgStart, an.getEnd(), "NG", sentence));
        }
      }
    }
  }

}
