/*
 * #!
 * Ontopia Webed
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

package net.ontopia.topicmaps.webed.impl.actions.occurrence;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import net.ontopia.infoset.impl.basic.URILocator;
import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapBuilderIF;
import net.ontopia.topicmaps.webed.core.ActionIF;
import net.ontopia.topicmaps.webed.core.ActionParametersIF;
import net.ontopia.topicmaps.webed.core.ActionResponseIF;
import net.ontopia.topicmaps.webed.core.ActionRuntimeException;
import net.ontopia.topicmaps.webed.impl.basic.Constants;
import net.ontopia.topicmaps.webed.impl.utils.ActionSignature;

/**
 * PUBLIC: Action for adding an external occurrence to a topic. The
 * occurrence type and scope may optionally be specified.
 */
public class AddExtOccurrence implements ActionIF {

  @Override
  public void perform(ActionParametersIF params, ActionResponseIF response) {
    //test params
    ActionSignature paramsType = ActionSignature.getSignature("t t t?&");
    paramsType.validateArguments(params, this);

    TopicIF topic = (TopicIF) params.get(0);
    TopicIF occtype = (TopicIF) params.get(1);
    Collection themes = params.getCollection(2);
    String value = params.getStringValue().trim();
    
    TopicMapBuilderIF builder = topic.getTopicMap().getBuilder();
    // do not create occurrence with empty string value
    if (value == null || value.equals(""))
      return;

    if (value.equals(Constants.RPV_DEFAULT))
      value = Constants.DUMMY_LOCATOR;
    
    // create new (external) occurrence for topic
    URILocator locator = null;
    try {
      locator = new URILocator(value);
    } catch (URISyntaxException e) {
      throw new ActionRuntimeException("Malformed URL for occurrence: '" + value + "'", false);
    }

    OccurrenceIF occurrence = builder.makeOccurrence(topic, occtype, locator);

    // set scope, if provided
    if (themes != null) {
      Iterator it = themes.iterator();
      while (it.hasNext()) 
        occurrence.addTheme((TopicIF) it.next());
    }
  }
}
