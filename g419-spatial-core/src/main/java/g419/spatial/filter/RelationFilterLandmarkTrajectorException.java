package g419.spatial.filter;

import g419.spatial.structure.SpatialExpression;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtr sprawdza, czy przyimek występuje przed potencjalnych landmarkiem.
 * Celem filtru jest odrzucenie tych kandydatów, wygenerowanych głównie przez MaltParser,
 * dla których przyimek wystąpuje po landmarku.
 *
 * @author czuk
 */
public class RelationFilterLandmarkTrajectorException implements IRelationFilter {

  Set<String> exceptions = new HashSet<String>();


  public RelationFilterLandmarkTrajectorException() throws IOException {
    this.exceptions.add("odcinek");
  }

  @Override
  public boolean pass(SpatialExpression relation) {
    return !this.exceptions.contains(relation.getTrajector().getSpatialObject().getHeadToken().getDisambTag().getBase())
        && !this.exceptions.contains(relation.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase());
  }

}
