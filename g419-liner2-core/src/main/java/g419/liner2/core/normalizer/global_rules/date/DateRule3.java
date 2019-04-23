package g419.liner2.core.normalizer.global_rules.date;

import g419.liner2.core.normalizer.global_rules.AbstractRule;

public class DateRule3 extends AbstractRule {
  @Override
  public boolean matches(String lval, String base) {
    return lval.equals("+0000-00-00");
  }

  @Override
  protected String doNormalize(String lval, String base, String previous, String first, String creationDate) {
    return creationDate;
  }
}
