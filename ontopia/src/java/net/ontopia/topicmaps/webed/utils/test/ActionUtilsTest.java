
// $Id: ActionUtilsTest.java,v 1.10 2008/03/18 09:10:44 geir.gronmo Exp $

package net.ontopia.topicmaps.webed.utils.test;

import java.io.File;

import net.ontopia.test.AbstractOntopiaTestCase;
import net.ontopia.utils.OntopiaRuntimeException;

import net.ontopia.topicmaps.core.*;
import net.ontopia.topicmaps.impl.basic.InMemoryTopicMapStore;

import net.ontopia.topicmaps.webed.core.*;
import net.ontopia.topicmaps.webed.impl.basic.*;
import net.ontopia.topicmaps.webed.impl.utils.*;
import net.ontopia.topicmaps.webed.impl.actions.basename.AddBasename;
import net.ontopia.topicmaps.webed.impl.utils.ActionUtils;
import net.ontopia.topicmaps.webed.impl.utils.ActionConfigurator;

public class ActionUtilsTest extends AbstractOntopiaTestCase {

  ActionRegistryIF registry;
  TopicMapIF topicmap;
  TopicMapBuilderIF builder;
  
  public ActionUtilsTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
    // only read in registry once (we are not modifying it)
    if (registry == null) {
      String baseDir = getTestDirectory() + File.separator + "webed";
      String configFile = baseDir + File.separator + "actionConfig.xml";
      ActionConfigurator ac = new ActionConfigurator("omnieditor", "/", configFile);
      ac.readAndWatchRegistry();
      registry = ac.getRegistry();
    }
    topicmap = makeTopicMap();
  }

  
  public void testGetActionAvail() {
    ActionInGroup action = ActionUtils.getAction(registry, "topicEditNames",
                                            "addBasename");

    assertEquals("Retrieved action had wrong name",
                 action.getName(), "addBasename");
    assertTrue("Retrieved action of wrong class",
               action.getAction() instanceof AddBasename);
  }
  
  public void testGetActionFailWrongName() {
    // -- wrong name
    ActionInGroup retrAction = ActionUtils.getAction(registry, "topicEditNames",
                                                     "non-existing");
    ActionInGroup expAction = null;
    assertEquals("Action should not exist.", expAction, retrAction);
  }
  
  public void testGetActionFailWrongGroup() {
    // -- wrong group
    try {
      ActionInGroup retrAction = ActionUtils.getAction(registry, "non-existing",
                                                  "addBasename");
      fail("It should have been signaled that no group is available.");
    } catch (OntopiaRuntimeException e) {
      assertTrue("Fine.", true);
    }
  }
  
  // ---------- internal helper methods
  
  protected TopicMapIF makeTopicMap() {
    InMemoryTopicMapStore store = new InMemoryTopicMapStore();
    builder = store.getTopicMap().getBuilder();
    return store.getTopicMap();
  }
 
}
