package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts set of annotations and relations between annotations into a set of spatial expressions.
 */
public class DocumentToSpatialExpressionConverter {

  /**
   * Annotation type which represents spatial objects
   */
  private final String annotationSpatialObject = "spatial_object3";

  /**
   * Annotation type which represents spatial indicator
   */
  private final String annotationSpatialIndicator = "spatial_indicator3";

  private final String annotationRegion = "region3";

  private final String annotationPathIndicator = "path_indicator3";

  private final String annotationMotionIndicator = "motion_indicator3";

  private final String annotationDirection = "direction3";

  private final String annotationDistance = "distance3";

  /**
   *
   */
  private final String relationLandmark = "landmark";

  private final String relationTrajector = "trajector";

  private final String relationArgument = "argument";

  private final Set<String> annotationTypes = Sets.newHashSet();

  private final Set<String> relationTypes = Sets.newHashSet();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   *
   */
  public DocumentToSpatialExpressionConverter() {
    annotationTypes.add(annotationDirection);
    annotationTypes.add(annotationDistance);
    annotationTypes.add(annotationRegion);
    annotationTypes.add(annotationSpatialObject);
    annotationTypes.add(annotationMotionIndicator);
    annotationTypes.add(annotationPathIndicator);
    annotationTypes.add(annotationSpatialIndicator);

    relationTypes.add(relationLandmark);
    relationTypes.add(relationTrajector);
    relationTypes.add(relationArgument);
  }

  /**
   * Convert document annotations and relations into a set of spatial expression structures.
   *
   * @param document
   * @return
   */
  public List<SpatialExpression> convert(final Document document) {
    if (document.getAnnotations().size() > 0 && document.getRelationsSet().size() == 0) {
      return Lists.newArrayList();
    }

    final Set<Relation> relationsToProcess = Sets.newHashSet(document.getRelationsSet()
        .stream().filter(r -> relationTypes.contains(r.getType())).collect(Collectors.toList()));
    final Set<Relation> relationsProcessed = Sets.newHashSet();

    final Map<Annotation, SpatialExpression> annotationToExpression = Maps.newHashMap();
    final Map<Annotation, SpatialObjectPath> objectPathMap = Maps.newHashMap();

    /**
     * TRAJECTOR relation from SPATIAL_INDICATOR to REGION
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(
          r -> relationTrajector.equals(r.getType())
              && annotationRegion.equals(r.getAnnotationTo().getType())
              && annotationSpatialIndicator.equals(r.getAnnotationFrom().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = new SpatialExpression();
        se.getTrajector().setRegion(rel.getAnnotationTo());
        se.setSpatialIndicator(rel.getAnnotationFrom());

        annotationToExpression.put(se.getSpatialIndicator(), se);
        annotationToExpression.put(se.getTrajector().getRegion(), se);
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * TRAJECTOR relation from SPATIAL_INDICATOR to SPATIAL_OBJECT
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(
          r -> relationTrajector.equals(r.getType())
              && annotationSpatialObject.equals(r.getAnnotationTo().getType())
              && annotationSpatialIndicator.equals(r.getAnnotationFrom().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = new SpatialExpression();
        se.getTrajector().setSpatialObject(rel.getAnnotationTo());
        se.setSpatialIndicator(rel.getAnnotationFrom());

        annotationToExpression.put(se.getSpatialIndicator(), se);
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * ARGUMENT relation from SPATIAL_OBJECT to DIRECTION
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(
          r -> relationArgument.equals(r.getType())
              && annotationDirection.equals(r.getAnnotationTo().getType())
              && annotationSpatialObject.equals(r.getAnnotationFrom().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = new SpatialExpression();
        se.getTrajector().setSpatialObject(rel.getAnnotationFrom());
        se.getDirections().add(rel.getAnnotationTo());

        annotationToExpression.put(se.getTrajector().getSpatialObject(), se);
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }


    /**
     * TRAJECTOR between from REGION to SPATIAL_INDICATOR
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(
          r -> relationTrajector.equals(r.getType())
              && annotationSpatialObject.equals(r.getAnnotationTo().getType())
              && annotationRegion.equals(r.getAnnotationFrom().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = annotationToExpression.get(rel.getAnnotationFrom());
        if (se == null) {
          logger.warn("Spatial expressions expected to exists but not found for: {}", rel);
        } else {
          se.getTrajector().setSpatialObject(rel.getAnnotationTo());
        }
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * TRAJECTOR relation from MOTION_INDICATOR to SPATIAL_OBJECT
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(r -> relationTrajector.equals(r.getType())
          && annotationMotionIndicator.equals(r.getAnnotationFrom().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = new SpatialExpression();
        se.getTrajector().setSpatialObject(rel.getAnnotationTo());
        se.setMotionIndicator(rel.getAnnotationFrom());

        annotationToExpression.put(se.getMotionIndicator(), se);
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * LANDMARK relation from X to REGION
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(r -> relationLandmark.equals(r.getType())
          && annotationRegion.equals(r.getAnnotationTo().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = annotationToExpression.get(rel.getAnnotationFrom());
        if (se == null) {
          logger.warn("Spatial expression with a region but without trajector: {}", rel);
        } else {
          se.getLandmark().setRegion(rel.getAnnotationTo());
          annotationToExpression.put(rel.getAnnotationTo(), se);
        }
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * LANDMARK relation from X to REGION
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(r -> relationLandmark.equals(r.getType())
          && !annotationRegion.equals(r.getAnnotationTo().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialExpression se = annotationToExpression.get(rel.getAnnotationFrom());
        if (se == null) {
          logger.warn("Spatial expression with a landmark but without trajector: {}", rel);
        } else {
          se.getLandmark().setSpatialObject(rel.getAnnotationTo());
          annotationToExpression.put(rel.getAnnotationTo(), se);
        }
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * ARGUMENT relation from PATH to SPATIAL_OBJECT
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(r -> relationArgument.equals(r.getType())
          && annotationPathIndicator.equals(r.getAnnotationFrom().getType())
          && annotationSpatialObject.equals(r.getAnnotationTo().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialObjectPath path = new SpatialObjectPath();
        path.setPathIndicator(rel.getAnnotationFrom());
        path.getSpatialObject().setSpatialObject(rel.getAnnotationTo());

        objectPathMap.put(path.getPathIndicator(), path);
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }

    /**
     * ARGUMENT relation from PATH to REGION
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(r -> relationArgument.equals(r.getType())
          && annotationPathIndicator.equals(r.getAnnotationFrom().getType())
          && annotationRegion.equals(r.getAnnotationTo().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialObjectPath path = new SpatialObjectPath();
        path.setPathIndicator(rel.getAnnotationFrom());
        path.getSpatialObject().setRegion(rel.getAnnotationTo());
        objectPathMap.put(path.getPathIndicator(), path);
        objectPathMap.put(path.getSpatialObject().getRegion(), path);
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }


    /**
     * ARGUMENT relation from REGION to SPATIAL_OPJECT
     */
    {
      final List<Relation> fRelations = relationsToProcess.stream().filter(r -> relationArgument.equals(r.getType())
          && annotationRegion.equals(r.getAnnotationFrom().getType())
          && annotationSpatialObject.equals(r.getAnnotationTo().getType())).collect(Collectors.toList());
      for (final Relation rel : fRelations) {
        final SpatialObjectPath sor = objectPathMap.get(rel.getAnnotationFrom());
        if (sor == null) {
          logger.warn("Path expression not found for: {}", rel);
        } else {
          sor.getSpatialObject().setSpatialObject(rel.getAnnotationTo());
        }
      }
      relationsProcessed.addAll(fRelations);
      relationsToProcess.removeAll(fRelations);
    }


    /**
     *
     */
    for (final Relation rel : relationsToProcess) {
      SpatialExpression se = annotationToExpression.get(rel.getAnnotationFrom());
      if (se == null) {
        logger.warn("Spatial expression not found for argument: {}", rel);
      } else if (annotationMotionIndicator.equals(rel.getAnnotationFrom().getType())) {
        if (annotationDirection.equals(rel.getAnnotationTo().getType())) {
          se.getDirections().add(rel.getAnnotationTo());
        } else if (annotationDistance.equals(rel.getAnnotationTo().getType())) {
          se.getDistances().add(rel.getAnnotationTo());
        } else if (annotationPathIndicator.equals(rel.getAnnotationTo().getType())) {
          final SpatialObjectPath path = objectPathMap.get(rel.getAnnotationTo());
          if (path == null) {
            logger.warn("No path found for: " + rel);
          } else {
            se.getPathsIndicators().add(path);
          }
        } else {
          logger.warn("Handle relation between {} and {}", rel.getAnnotationFrom().getType(), rel.getAnnotationTo().getType());
          se = null;
        }
      }
      if (se != null) {
        relationsProcessed.add(rel);
      }
    }
    relationsToProcess.removeAll(relationsProcessed);

    final Set<Annotation> annotationsToProcess = Sets.newHashSet(document.getAnnotations()
        .stream().filter(a -> annotationTypes.contains(a.getType())).collect(Collectors.toList()));
    for (final Relation rel : relationsProcessed) {
      annotationsToProcess.remove(rel.getAnnotationFrom());
      annotationsToProcess.remove(rel.getAnnotationTo());
    }

    if (annotationsToProcess.size() > 0) {
      logger.warn("There are annotations which were not processed:");
      for (final Annotation an : annotationsToProcess) {
        logger.warn("  => {}", an);
      }
    }

    if (relationsToProcess.size() > 0) {
      logger.warn("There are relations which were not processed:");
      for (final Relation rel : relationsToProcess) {
        logger.warn("  => {}", rel);
      }
    }

    return Lists.newArrayList(Sets.newHashSet(annotationToExpression.values()));
  }

}
