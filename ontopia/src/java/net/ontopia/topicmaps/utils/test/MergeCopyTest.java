
// $Id: MergeCopyTest.java,v 1.9 2008/06/13 08:36:29 geir.gronmo Exp $

package net.ontopia.topicmaps.utils.test;

import java.util.Collection;
import net.ontopia.topicmaps.test.*;
import net.ontopia.topicmaps.impl.basic.InMemoryTopicMapStore;
import net.ontopia.infoset.core.*;
import net.ontopia.infoset.impl.basic.URILocator;
import net.ontopia.topicmaps.core.*;
import net.ontopia.utils.DeciderUtils;
import net.ontopia.topicmaps.utils.PSI;
import net.ontopia.topicmaps.utils.MergeUtils;

public class MergeCopyTest extends AbstractTopicMapTestCase {
  protected TopicMapIF    topicmap1; 
  protected TopicMapIF    topicmap2; 
  protected TopicMapBuilderIF builder1;
  protected TopicMapBuilderIF builder2;

  public MergeCopyTest(String name) {
    super(name);
  }
    
  public void setUp() {
    topicmap1 = makeTopicMap();
    topicmap2 = makeTopicMap();
    builder1 = topicmap1.getBuilder();
    builder2 = topicmap2.getBuilder();
  }
    
  // intended to be overridden
  protected TopicMapIF makeTopicMap() {
    InMemoryTopicMapStore store = new InMemoryTopicMapStore();
    return store.getTopicMap();
  }

  public URILocator makeLocator(String uri) {
    try {
      return new URILocator(uri);
    }
    catch (java.net.MalformedURLException e) {
      fail("malformed URL given: " + e);
      return null; // never executed...
    }
  }

  public void onlyContains(String what, Collection coll, Object element) {
    assertTrue(what + " collection has wrong number of elements",
               coll.size() == 1);

    assertTrue(what + " collection contains wrong element",
               coll.iterator().next().equals(element));
  }
    
  // --- Test cases for mergeInto(TopicMapIF, TopicIF)

  public void testMergeEmptyTopics() {
    TopicIF topic = builder2.makeTopic();

    MergeUtils.mergeInto(topicmap1, topic);
    
    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 1);
    topic = (TopicIF) topicmap1.getTopics().iterator().next();
    assertTrue("empty topic suddenly has source locators",
               topic.getItemIdentifiers().isEmpty());
    assertTrue("empty topic suddenly has subject indicators",
               topic.getSubjectIdentifiers().isEmpty());
    assertTrue("empty topic suddenly has subject address",
               topic.getSubjectLocators().isEmpty());
    assertTrue("empty topic suddenly has base names",
               topic.getTopicNames().isEmpty());
    assertTrue("empty topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("empty topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("empty topic suddenly has types",
               topic.getTypes().isEmpty());
  }

  public void testMergeTopicWithURIs() {
    TopicIF topic = builder2.makeTopic();
    topic.addSubjectIdentifier(makeLocator("http://www.ontopia.net"));
    topic.addSubjectLocator(makeLocator("http://www.example.com"));
    topic.addItemIdentifier(makeLocator("http://www.ontopia.com"));

    MergeUtils.mergeInto(topicmap1, topic);
    
    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 1);
    topic = (TopicIF) topicmap1.getTopics().iterator().next();
    onlyContains("source locator", topic.getItemIdentifiers(),
                 makeLocator("http://www.ontopia.com"));
    onlyContains("subject indicator", topic.getSubjectIdentifiers(),
                 makeLocator("http://www.ontopia.net"));
    assertTrue("topic has wrong subject address",
               topic.getSubjectLocators().contains(makeLocator("http://www.example.com")));
    
    assertTrue("empty topic suddenly has base names",
               topic.getTopicNames().isEmpty());
    assertTrue("empty topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("empty topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("empty topic suddenly has types",
               topic.getTypes().isEmpty());
  }  

  public void testMergeTopicsWithURIs() {
    TopicIF topic1 = builder1.makeTopic();
    topic1.addSubjectIdentifier(makeLocator("http://www.ontopia.net"));
    topic1.addSubjectLocator(makeLocator("http://www.example.com"));
    topic1.addItemIdentifier(makeLocator("http://www.ontopia.com"));
    
    TopicIF topic2 = builder2.makeTopic();
    topic2.addSubjectIdentifier(makeLocator("http://www.ontopia.net"));
    topic2.addSubjectLocator(makeLocator("http://www.example.com"));
    topic2.addItemIdentifier(makeLocator("http://www.ontopia.com"));

    MergeUtils.mergeInto(topicmap1, topic2);

    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 1);
    
    TopicIF topic = (TopicIF) topicmap1.getTopics().iterator().next();
    onlyContains("source locator", topic.getItemIdentifiers(),
                 makeLocator("http://www.ontopia.com"));
    onlyContains("subject indicator", topic.getSubjectIdentifiers(),
                 makeLocator("http://www.ontopia.net"));
    assertTrue("topic has wrong subject address",
               topic.getSubjectLocators().contains(makeLocator("http://www.example.com")));
    
    assertTrue("empty topic suddenly has base names",
               topic.getTopicNames().isEmpty());
    assertTrue("empty topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("empty topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("empty topic suddenly has types",
               topic.getTypes().isEmpty());
  }  


  public void testTopicOtherObjectCollision() {
    TopicIF topic1 = builder1.makeTopic();
    TopicNameIF bn = builder1.makeTopicName(topic1, "");
    bn.addItemIdentifier(makeLocator("http://www.ontopia.com"));
    
    TopicIF topic2 = builder2.makeTopic();
    topic2.addSubjectIdentifier(makeLocator("http://www.ontopia.net"));
    topic2.addSubjectLocator(makeLocator("http://www.example.com"));
    topic2.addItemIdentifier(makeLocator("http://www.ontopia.com"));

    try {
      MergeUtils.mergeInto(topicmap1, topic2);
      fail("collision not detected");
    } catch (ConstraintViolationException e) {
    }
  }  

  public void testTopicNameCopying() {
    TopicIF topic = builder2.makeTopic();
    builder2.makeTopicName(topic, "The topic");

    MergeUtils.mergeInto(topicmap1, topic);
    
    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 1);
    topic = (TopicIF) topicmap1.getTopics().iterator().next();
    assertTrue("empty topic suddenly has source locators",
               topic.getItemIdentifiers().isEmpty());
    assertTrue("empty topic suddenly has subject indicators",
               topic.getSubjectIdentifiers().isEmpty());
    assertTrue("empty topic suddenly has subject address",
               topic.getSubjectLocators().isEmpty());
    assertTrue("empty topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("empty topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("empty topic suddenly has types",
               topic.getTypes().isEmpty());
    
    assertTrue("topic lost base name",
               topic.getTopicNames().size() == 1);
    TopicNameIF bn = (TopicNameIF) topic.getTopicNames().iterator().next();
    assertTrue("base name has variants",
               bn.getVariants().isEmpty());
    assertTrue("base name has wrong value",
               bn.getValue().equals("The topic"));
    assertTrue("base name has non-empty scope",
               bn.getScope().isEmpty());
  }

  public void testTopicNameScopeCopying() {
    TopicIF topic = builder2.makeTopic();
    topic.addSubjectIdentifier(makeLocator("http://www.ontopia.com"));
    TopicNameIF bn = builder2.makeTopicName(topic, "The topic");
    
    TopicIF theme = builder2.makeTopic();
    theme.addSubjectIdentifier(makeLocator("http://www.ontopia.net"));
    builder2.makeTopicName(theme, "The theme");
    bn.addTheme(theme);

    MergeUtils.mergeInto(topicmap1, topic);
    
    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 2); // topic + theme
    topic = topicmap1.getTopicBySubjectIdentifier(makeLocator("http://www.ontopia.com"));
    assertTrue("empty topic suddenly has source locators",
               topic.getItemIdentifiers().isEmpty());
    onlyContains("subject indicators",
                 topic.getSubjectIdentifiers(),
                 makeLocator("http://www.ontopia.com"));
    assertTrue("empty topic suddenly has subject address",
               topic.getSubjectLocators().isEmpty());
    assertTrue("empty topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("empty topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("empty topic suddenly has types",
               topic.getTypes().isEmpty());
    
    assertTrue("topic lost base name",
               topic.getTopicNames().size() == 1);
    bn = (TopicNameIF) topic.getTopicNames().iterator().next();
    assertTrue("base name has variants",
               bn.getVariants().isEmpty());
    assertTrue("base name has wrong value",
               bn.getValue().equals("The topic"));

    assertTrue("base name scope has wrong size",
               bn.getScope().size() == 1);
    topic = (TopicIF) bn.getScope().iterator().next();
    assertTrue("theme suddenly has base names",
               topic.getTopicNames().isEmpty());    
    assertTrue("theme suddenly has source locators",
               topic.getItemIdentifiers().isEmpty());
    assertTrue("theme suddenly has subject address",
               topic.getSubjectLocators().isEmpty());

    onlyContains("theme subject indicator",
                 topic.getSubjectIdentifiers(),
                 makeLocator("http://www.ontopia.net"));
  }

  public void testTopicNameScopeCopyingFalse() {
    TopicIF topic = builder2.makeTopic();
    topic.addSubjectIdentifier(makeLocator("http://www.ontopia.com"));
    TopicNameIF bn = builder2.makeTopicName(topic, "The topic");
    
    TopicIF theme = builder2.makeTopic();
    theme.addSubjectIdentifier(makeLocator("http://www.ontopia.net"));
    builder2.makeTopicName(theme, "The theme");
    bn.addTheme(theme);

    MergeUtils.mergeInto(topicmap1, topic, DeciderUtils.getFalseDecider());
    
    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 1); // topic
    topic = topicmap1.getTopicBySubjectIdentifier(makeLocator("http://www.ontopia.com"));
    assertTrue("empty topic suddenly has source locators",
               topic.getItemIdentifiers().isEmpty());
    onlyContains("subject indicators",
                 topic.getSubjectIdentifiers(),
                 makeLocator("http://www.ontopia.com"));
    assertTrue("empty topic suddenly has subject address",
               topic.getSubjectLocators().isEmpty());
    assertTrue("empty topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("empty topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("empty topic suddenly has types",
               topic.getTypes().isEmpty());
    
    assertTrue("topic has base names, despite using false decider",
               topic.getTopicNames().isEmpty());
  }

  public void testVariantNames() {
    TopicIF topic = builder2.makeTopic();
    topic.addSubjectLocator(makeLocator("http://www.ontopia.com"));
    TopicNameIF bn = builder2.makeTopicName(topic, "The Ontopia Website");
    VariantNameIF vn = builder2.makeVariantName(bn, "ontopia website, the");
    
    TopicIF theme = builder2.makeTopic();
    theme.addSubjectIdentifier(PSI.getXTMSort());
    builder2.makeTopicName(theme, "Sort name");
    vn.addTheme(theme);

    MergeUtils.mergeInto(topicmap1, topic, DeciderUtils.getTrueDecider());
    
    assertTrue("topic map has wrong number of topics after merge",
               topicmap1.getTopics().size() == 2); // topic + theme
    topic = topicmap1.getTopicBySubjectLocator(makeLocator("http://www.ontopia.com"));
    theme = topicmap1.getTopicBySubjectIdentifier(PSI.getXTMSort());

    assertTrue("can't find test topic after merge", topic != null);
    assertTrue("can't find theme after merge", theme != null);
    
    assertTrue("test topic suddenly has source locators",
               topic.getItemIdentifiers().isEmpty());
    assertTrue("test topic suddenly has subject indicators",
               topic.getSubjectIdentifiers().isEmpty());
    assertTrue("test topic has lost subject locator",
               topic.getSubjectLocators().contains(makeLocator("http://www.ontopia.com")));

    assertTrue("theme topic suddenly has source locators",
               theme.getItemIdentifiers().isEmpty());
    onlyContains("subject indicator", theme.getSubjectIdentifiers(),
                 PSI.getXTMSort());
    assertTrue("theme topic suddenly has subject locator",
               theme.getSubjectLocators().isEmpty());
    
    assertTrue("test topic suddenly has occurrences",
               topic.getOccurrences().isEmpty());
    assertTrue("test topic suddenly has roles",
               topic.getRoles().isEmpty());
    assertTrue("test topic suddenly has types",
               topic.getTypes().isEmpty());
    
    assertTrue("test topic has lost base names",
               !topic.getTopicNames().isEmpty());

    bn = (TopicNameIF) topic.getTopicNames().iterator().next();

    assertTrue("test topic has lost variant name",
               bn.getVariants().size() == 1);

    vn = (VariantNameIF) bn.getVariants().iterator().next();

    assertTrue("variant name has lost scope",
               vn.getScope().size() == 1 && vn.getScope().contains(theme));
    assertTrue("variant name has value",
               vn.getValue().equals("ontopia website, the"));
  }  
}
