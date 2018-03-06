/*
 * #!
 * Ontopia Engine
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

package net.ontopia.topicmaps.utils;

import junit.framework.TestCase;
import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.infoset.impl.basic.URILocator;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapBuilderIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.impl.basic.InMemoryTopicMapStore;
import net.ontopia.utils.OntopiaRuntimeException;

public class QNameTests extends TestCase {
  protected TopicMapIF        topicmap; 
  protected TopicIF           topic; 
  protected TopicMapBuilderIF builder;
  protected QNameRegistry     registry; 
  protected QNameLookup       q; 

  public QNameTests(String name) {
    super(name);
  }
    
  @Override
  public void setUp() {
    InMemoryTopicMapStore store = new InMemoryTopicMapStore();
    builder = store.getTopicMap().getBuilder();
    topicmap = store.getTopicMap();
    topic = builder.makeTopic();

    registry = new QNameRegistry();
    q = registry.getLookup(topicmap);
  }
 
  // --- Test cases for QNameRegistry

  public void testBadSyntaxEmpty() {
    try {
      registry.resolve("");
      fail("empty string accepted as qname");
    } catch (OntopiaRuntimeException e) {
      // expected
    }
  }

  public void testBadSyntaxIdentifier() {
    try {
      registry.resolve("foo");
      fail("identifier accepted as qname");
    } catch (OntopiaRuntimeException e) {
      // expected
    }
  }

  public void testNoSuchPrefix() {
    try {
      registry.resolve("foo:bar");
      fail("unregistered prefix accepted");
    } catch (OntopiaRuntimeException e) {
      // expected
    }
  }

  public void testSimpleLookup() {
    registry.registerPrefix("tst", "http://psi.example.org/");
    LocatorIF loc = registry.resolve("tst:test");
    assertTrue("qname resolved incorrectly: " + loc,
               loc.getAddress().equals("http://psi.example.org/test"));
  }

  public void testRegisterTwice() {
    registry.registerPrefix("tst", "http://www.example.org/");
    registry.registerPrefix("tst", "http://psi.example.org/");
    LocatorIF loc = registry.resolve("tst:test");
    assertTrue("qname resolved incorrectly: " + loc,
               loc.getAddress().equals("http://psi.example.org/test"));
  }

  // --- Test cases for QNameLookup

  public void testBadSyntaxEmpty2() {
    try {
      q.lookup("");
      fail("empty string accepted as qname");
    } catch (OntopiaRuntimeException e) {
      // expected
    }
  }

  public void testBadSyntaxIdentifier2() {
    try {
      q.lookup("foo");
      fail("identifier accepted as qname");
    } catch (OntopiaRuntimeException e) {
      // expected
    }
  }

  public void testNoSuchPrefix2() {
    try {
      q.lookup("foo:bar");
      fail("unregistered prefix accepted");
    } catch (OntopiaRuntimeException e) {
      // expected
    }
  }

  public void testSimpleLookupFails() {
    registry.registerPrefix("tst", "http://psi.example.org/");
    TopicIF found = q.lookup("tst:test");
    assertTrue("lookup returned topic when none was to be found",
               found == null);
  }

  public void testSimpleLookupSucceeds() {
    URILocator loc = URILocator.create("http://psi.example.org/test");
    topic.addSubjectIdentifier(loc);
    
    registry.registerPrefix("tst", "http://psi.example.org/");
    TopicIF found = q.lookup("tst:test");
    assertTrue("lookup returned wrong topic",
               topic == found);
  }

  public void testSimpleLookupFails2() {
    URILocator loc = URILocator.create("http://psi.example.org/test");
    topic.addItemIdentifier(loc);
    
    registry.registerPrefix("tst", "http://psi.example.org/");
    TopicIF found = q.lookup("tst:test");
    assertTrue("lookup returned wrong topic: " + found,
               found == null);
  }

  public void testSimpleLookupFails3() {
    URILocator loc = URILocator.create("http://psi.example.org/test");
    topic.addSubjectLocator(loc);
    
    registry.registerPrefix("tst", "http://psi.example.org/");
    TopicIF found = q.lookup("tst:test");
    assertTrue("lookup returned wrong topic: " + found,
               found == null);
  }
  
  public void testRegisterTwice2() {
    URILocator loc = URILocator.create("http://psi.example.org/test");
    topic.addSubjectIdentifier(loc);
    
    registry.registerPrefix("tst", "http://www.example.org/");
    registry.registerPrefix("tst", "http://psi.example.org/");
    TopicIF found = q.lookup("tst:test");
    assertTrue("qname resolved incorrectly: " + found,
               topic == found);
  }
  
}
