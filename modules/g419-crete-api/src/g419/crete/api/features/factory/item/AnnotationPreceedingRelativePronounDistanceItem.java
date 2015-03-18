package g419.crete.api.features.factory.item;

import g419.corpus.structure.Annotation;
import g419.crete.api.features.AbstractFeature;
import g419.crete.api.features.annotations.AnnotationFeaturePreceedingRelativePronounDistance;

public class AnnotationPreceedingRelativePronounDistanceItem implements IFeatureFactoryItem<Annotation, Integer> {

	@Override
	public AbstractFeature<Annotation, Integer> createFeature() {
		return new AnnotationFeaturePreceedingRelativePronounDistance();
	}

}
