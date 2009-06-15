
// $Id: InPredicateTest.java,v 1.2 2005/07/13 08:56:48 grove Exp $

package net.ontopia.topicmaps.query.core.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ontopia.topicmaps.query.core.InvalidQueryException;

public class InPredicateTest extends AbstractPredicateTest {
  
  public InPredicateTest(String name) {
    super(name);
  }

  /// tests

  public void testHumanInList() throws InvalidQueryException, IOException {
    load("family2.ltm");

    List matches = new ArrayList();
    addMatch(matches, "HUMAN", getTopicById("dan"));
    addMatch(matches, "HUMAN", getTopicById("sharon"));
    addMatch(matches, "HUMAN", getTopicById("spencer"));
    
    verifyQuery(matches, "instance-of($HUMAN, human), in($HUMAN, dan, sharon, spencer)?");
    closeStore();
  }

  public void testFemaleInList() throws InvalidQueryException, IOException {
    load("family2.ltm");

    List matches = new ArrayList();
    addMatch(matches, "FEMALE", getTopicById("sharon"));
    
    verifyQuery(matches, "instance-of($FEMALE, female), in($FEMALE, dan, sharon, spencer)?");
    closeStore();
  }

  public void testMaleInList() throws InvalidQueryException, IOException {
    load("family2.ltm");

    List matches = new ArrayList();
    addMatch(matches, "MALE", getTopicById("dan"));
    addMatch(matches, "MALE", getTopicById("spencer"));
    
    verifyQuery(matches, "instance-of($MALE, male), in($MALE, dan, sharon, spencer)?");
    closeStore();
  }

  public void testHumanNotInList() throws InvalidQueryException, IOException {
    load("family2.ltm");

    List matches = new ArrayList();
    addMatch(matches, "HUMAN", getTopicById("alan"));
    addMatch(matches, "HUMAN", getTopicById("peter"));
    addMatch(matches, "HUMAN", getTopicById("andy"));
    addMatch(matches, "HUMAN", getTopicById("philip"));
    addMatch(matches, "HUMAN", getTopicById("bruce"));
    addMatch(matches, "HUMAN", getTopicById("clyde"));
    addMatch(matches, "HUMAN", getTopicById("james"));
    
    verifyQuery(matches, "instance-of($HUMAN, male), not(in($HUMAN, dan, sharon, spencer))?");
    closeStore();
  }
  
}
