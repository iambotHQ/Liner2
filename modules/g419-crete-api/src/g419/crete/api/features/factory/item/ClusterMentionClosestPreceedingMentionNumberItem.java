package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.clustermention.ClusterMentionClosestPreceedingMentionNumber;
import g419.crete.api.features.enumvalues.Number;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingMentionNumberItem   implements IFeatureFactoryItem<Pair<Annotation, AnnotationCluster>, Number> {

	@Override
	public AbstractFeature<Pair<Annotation, AnnotationCluster>, Number> createFeature() {
		return new ClusterMentionClosestPreceedingMentionNumber();
	}

}
