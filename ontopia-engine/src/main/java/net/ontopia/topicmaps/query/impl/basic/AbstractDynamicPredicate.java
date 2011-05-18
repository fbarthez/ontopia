
// $Id: AbstractDynamicPredicate.java,v 1.6 2008/06/25 11:24:15 lars.garshol Exp $

package net.ontopia.topicmaps.query.impl.basic;

import java.util.Iterator;

import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.topicmaps.core.TopicIF;

/**
 * INTERNAL: Common code-sharing superclass for dynamic predicates.
 */
public abstract class AbstractDynamicPredicate implements BasicPredicateIF {
  protected String name;
  protected TopicIF type;
  protected LocatorIF base;

  public AbstractDynamicPredicate(String name) {
    // used for DynamicFailurePredicates generated by optimizer
    this.name = name;
  }

  public AbstractDynamicPredicate(TopicIF type, LocatorIF base) {
    this.type = type;
    this.base = base;
  }

  public String getName() {
    if (name != null)
      return name;
    
    if (base == null)
      return "@" + type.getObjectId();
    
    Iterator it = type.getItemIdentifiers().iterator();
    String baseadr = base.getAddress();
    while (it.hasNext()) {
      LocatorIF loc = (LocatorIF) it.next();
      String address = loc.getAddress();
      if (address.startsWith(baseadr))
        return address.substring(baseadr.length() + 1);
    }
    return "@" + type.getObjectId();
  }

  public TopicIF getType() {
    return type;
  }
}