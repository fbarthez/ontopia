/*
 * #!
 * Ontopoly Editor
 * #-
 * Copyright (C) 2001 - 2013 The Ontopia Project
 * #-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * !#
 */
package ontopoly.utils;

import java.io.Serializable;
import java.util.Comparator;
import ontopoly.model.Topic;
import org.apache.commons.lang3.StringUtils;

public class TopicComparator implements Comparator<Topic>, Serializable {

  public static final TopicComparator INSTANCE = new TopicComparator();
  
  private TopicComparator() {
    // don't call me
  }
  
  public int compare(Topic t1, Topic t2) {
    if (t1 == null && t2 == null) return 0;
    else if (t1 == null)
      return 1;
    else if (t2 == null)
      return -1;
    return StringUtils.compareIgnoreCase(t1.getName(), t2.getName());
  }

}
