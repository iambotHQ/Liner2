package g419.crete.api.features.factory;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.pair.AnnotationPairFeatureFirstLength;
import g419.crete.api.features.factory.item.IFeatureFactoryItem;

import org.apache.commons.lang3.tuple.Pair;

public class AnnotationPairFeatureFirstLengthItem implements IFeatureFactoryItem<Pair<Annotation, Annotation>, Integer>  {

	@Override
	public AbstractFeature<Pair<Annotation, Annotation>, Integer> createFeature() {
		return new AnnotationPairFeatureFirstLength();
	}

}
