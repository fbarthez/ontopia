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

package net.ontopia.topicmaps.core;

import java.util.Collections;

/**
 * TestCase that runs through each implementation of ReifiableIF
 * [Association, AssociationRole, Occurrence, TopicMap, TopicName, VariantName]
 * and reifies them with a topic that has been used to reify another object earlier.
 * A DuplicateReificationException is expected when doing so and the tests fail if
 * such exceptions are not being thrown.
 */
public abstract class DuplicateReificationTest extends AbstractTopicMapTest {

  public DuplicateReificationTest(String name) {
    super(name);
  }

  /**
   * Simple method to test the DuplicateReificationException itself for
   * NullPointerExceptions and possible unwanted behaviour.
   */
  public void testException() {
    ReifiableIF reifiable = builder.makeAssociation(builder.makeTopic());
    TopicIF reifier = builder.makeTopic();
    reifiable.setReifier(null);
    assertNull(reifiable.getReifier());
    reifiable.setReifier(reifier);
    assertEquals(reifier, reifiable.getReifier());
    reifiable.setReifier(reifier);
    assertEquals(reifier, reifiable.getReifier());
    reifiable.setReifier(null);
    assertNull(reifiable.getReifier());
    // No exceptions should have been thrown at this point
  }

  public void testAssociation() {
    assertThrowsDuplicateReificationException(
      builder.makeAssociation(builder.makeTopic()));
  }

  public void testAssociationRole() {
    AssociationIF a = builder.makeAssociation(builder.makeTopic());
    assertThrowsDuplicateReificationException(
      builder.makeAssociationRole(a, builder.makeTopic(), builder.makeTopic()));
  }

  public void testOccurrence() {
    assertThrowsDuplicateReificationException(
      builder.makeOccurrence(builder.makeTopic(), builder.makeTopic(), "occurrence"));
  }

  // TODO: Disabled because KeyGenerator cannot make keys for TopicMapIF objects
  // public void testTopicMap() {
  //   checkDuplicateReificationException(topicmap);
  // }

  public void testTopicName() {
    assertThrowsDuplicateReificationException(
      builder.makeTopicName(builder.makeTopic(), "name")
    );
  }

  public void testVariantName() {
    TopicNameIF name = builder.makeTopicName(builder.makeTopic(), "name");
    assertThrowsDuplicateReificationException(
      builder.makeVariantName(name, "variant", Collections.singleton(builder.makeTopic())));
  }

  /**
   * Internal method to set reification twice with the same reifier.
   * DuplicateReificationException should be detected, fail test otherwise.
   */
  private void assertThrowsDuplicateReificationException(ReifiableIF reifiable) {
    TopicIF reifier = builder.makeTopic();
    builder.makeAssociation(builder.makeTopic()).setReifier(reifier);
    try {
      reifiable.setReifier(reifier);
      fail("Failed to catch DuplicateReificationException");
    } catch (DuplicateReificationException e) {
      // test succeeded
    }
  }
}
