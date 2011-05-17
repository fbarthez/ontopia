package ontopoly.models;


import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicMap;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicType;
import ontopoly.utils.OntopolyContext;

import org.apache.wicket.model.LoadableDetachableModel;

public class TopicTypeModel extends LoadableDetachableModel {

  private static final long serialVersionUID = -8374148020034895666L;

  private String topicMapId;

  private String topicId;

  public TopicTypeModel(TopicType topicType) {
    super(topicType);
    if (topicType != null) {
      this.topicMapId = topicType.getTopicMap().getId();
      this.topicId = topicType.getId();
    }
  }
  
  public TopicTypeModel(String topicMapId, String topicId) {
    if (topicMapId == null)
      throw new NullPointerException("topicMapId parameter cannot be null.");
    if (topicId == null)
      throw new NullPointerException("topicId parameter cannot be null.");
    this.topicMapId = topicMapId;
    this.topicId = topicId;
  }

  public TopicType getTopicType() {
    return (TopicType)getObject();
  }

  @Override
  protected Object load() {
    if (topicMapId == null) return null;
    TopicMap tm = OntopolyContext.getTopicMap(topicMapId);
    TopicIF topicIf = tm.getTopicIFById(topicId);
    return new TopicType(topicIf, tm);
  }
}