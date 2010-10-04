package ontopoly.conversion;

import ontopoly.model.OntopolyTopicMapIF;
import net.ontopia.topicmaps.query.core.InvalidQueryException;

public class Upgrade_1_4 extends UpgradeBase {
  
  Upgrade_1_4(OntopolyTopicMapIF topicmap) throws InvalidQueryException {
    super(topicmap);
  }
  
  @Override
  protected void importLTM(StringBuffer sb) {
    sb.append("[on:untyped-name : on:system-topic]\n");
  }
  
  @Override
  protected void transform() throws InvalidQueryException {
  }
  
}
