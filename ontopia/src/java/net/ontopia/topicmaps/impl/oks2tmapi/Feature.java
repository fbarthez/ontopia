
// $Id: Feature.java,v 1.1 2004/11/18 07:36:46 grove Exp $

package net.ontopia.topicmaps.impl.oks2tmapi;

import java.util.*;
import net.ontopia.topicmaps.core.*;

/**
 * INTERNAL: OKS->TMAPI feature class.
 */

class Feature {

  String name;
  boolean fixed;
  boolean defval;

  Feature(String name, boolean fixed, boolean defval) {
    this.name = name;
    this.fixed = fixed;
    this.defval = defval;
  }
}
