package g419.spatial.tools;

import com.google.common.collect.Lists;
import g419.corpus.HasLogger;
import g419.corpus.structure.Sentence;
import g419.spatial.filter.IRelationFilter;
import g419.spatial.filter.RelationFilterSemanticPattern;
import g419.spatial.structure.SpatialExpression;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class ISpatialRelationRecognizer implements HasLogger {

  protected List<IRelationFilter> filters = Lists.newArrayList();

  protected final RelationFilterSemanticPattern semanticFilter = new RelationFilterSemanticPattern();

  protected ISpatialRelationRecognizer() throws IOException {
  }

  public abstract List<SpatialExpression> findCandidates(final Sentence sentence);

  /**
   * @return
   */
  public List<IRelationFilter> getFilters() {
    return filters;
  }

  /**
   * @return
   */
  public RelationFilterSemanticPattern getSemanticFilter() {
    return semanticFilter;
  }

  /**
   * Passes the spatial expression through the list of filters and return the first filter, for which
   * the expressions was discarded.
   *
   * @param se Spatial expression to test
   * @return
   */
  public Optional<String> getFilterDiscardingRelation(final SpatialExpression se) {
    final Iterator<IRelationFilter> filters = this.getFilters().iterator();
    while (filters.hasNext()) {
      final IRelationFilter filter = filters.next();
      if (!filter.pass(se)) {
        return Optional.ofNullable(filter.getClass().getSimpleName());
      }
    }
    return Optional.ofNullable(null);
  }
}
