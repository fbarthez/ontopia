
// $Id: WildcardTopicGenerator.java,v 1.2 2009/02/27 12:04:10 lars.garshol Exp $

package net.ontopia.topicmaps.utils.ctm;

import net.ontopia.topicmaps.core.TopicIF;

public class WildcardTopicGenerator implements TopicGeneratorIF {
  private ParseContextIF context;
  
  public WildcardTopicGenerator(ParseContextIF context) {
    this.context = context;
  }
  
  public TopicIF getTopic() {
    return context.makeAnonymousTopic();
  }

  public TopicGeneratorIF copyTopic() {
    return this; // no state, so...
  }
}