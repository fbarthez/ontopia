
// $Id: CollectionFactory.java,v 1.12 2004/11/29 19:10:44 grove Exp $

package net.ontopia.utils;

import java.util.*;

/**
 * INTERNAL: A collection factory that returns non-synchronized standard
 * java.util collection objects.</p>
 */

public class CollectionFactory implements CollectionFactoryIF, java.io.Serializable {

  static final long serialVersionUID = -4670702015296061304L;
  protected int initsize;

  public CollectionFactory() {
    initsize = 4;
  }

  public CollectionFactory(int initsize) {
    this.initsize = initsize;
  }

  public Set makeSmallSet() {
    return new HashSet(initsize);
  }

  public Set makeLargeSet() {
    return new HashSet();
  }

  public Map makeSmallMap() {
    return new HashMap(initsize);
  }

  public Map makeLargeMap() {
    return new HashMap();
  }
  
  public List makeSmallList() {
    return new ArrayList(initsize);
  }

  public List makeLargeList() {
    return new ArrayList();
  }

}
