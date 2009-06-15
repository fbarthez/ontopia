package ontopoly.utils;

import java.io.Serializable;
import java.util.Comparator;

import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.Topic;
import net.ontopia.utils.StringUtils;

public class TopicComparator implements Comparator, Serializable {

  public static final TopicComparator INSTANCE = new TopicComparator();
  
  private TopicComparator() {
    // don't call me
  }
  
  public int compare(Object o1, Object o2) {
    Topic t1 = (Topic)o1;
    Topic t2 = (Topic)o2;
    if (t1 == null && t2 == null) return 0;
    else if (t1 == null)
      return 1;
    else if (t2 == null)
      return -1;
    return StringUtils.compareToIgnoreCase(t1.getName(), t2.getName());
  }

}
