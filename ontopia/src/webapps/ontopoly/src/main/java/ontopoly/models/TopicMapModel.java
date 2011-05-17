package ontopoly.models;


import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicMap;
import ontopoly.utils.OntopolyContext;

import org.apache.wicket.model.LoadableDetachableModel;

public class TopicMapModel extends LoadableDetachableModel {

  private static final long serialVersionUID = -6589204980069242599L;

  private String topicMapId;

  public TopicMapModel(TopicMap topicMap) {
    super(topicMap);
    if (topicMap != null) {
      this.topicMapId = topicMap.getId();
    }
  }

  public TopicMapModel(String topicMapId) {
    if (topicMapId == null)
      throw new NullPointerException("topicMapId parameter cannot be null.");
    this.topicMapId = topicMapId;    
  }

  public TopicMap getTopicMap() {
    return (TopicMap)getObject();
  }

  @Override
  protected Object load() {
    // retrive topicMap from ontopoly model
    return OntopolyContext.getTopicMap(topicMapId);
  }
}