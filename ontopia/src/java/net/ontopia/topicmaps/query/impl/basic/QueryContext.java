
// $Id: QueryContext.java,v 1.7 2006/07/18 12:56:42 larsga Exp $

package net.ontopia.topicmaps.query.impl.basic;

import java.util.Map;

import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.query.parser.TologOptions;
import net.ontopia.topicmaps.query.parser.TologQuery;

/**
 * INTERNAL: Object used to hold the global query execution context;
 * that is, the context beginning with the start of the execution of a
 * query and ending with the completion of its execution. Different
 * queries, and different executions of the same query, have different
 * contexts.
 */
public class QueryContext {
  private TopicMapIF topicmap;
  private TologQuery query; // null inside rule predicates, since variable type
                            // information is entirely different there
  private Map arguments;    // parameter values
  private TologOptions options;
  
  public QueryContext(TopicMapIF topicmap, TologQuery query, Map arguments,
                      TologOptions options) {
    this.topicmap = topicmap;
    this.query = query;
    this.arguments = arguments;
    this.options = options;
  }

  public TopicMapIF getTopicMap() {
    return topicmap;
  }

  public Object[] getVariableTypes(String varname) {
    if (query == null)
      return null;
    else
      return (Object[]) query.getVariableTypes().get(varname);
  }

  public Object getParameterValue(String paramname) {
    return arguments.get(paramname);
  }

  public Map getParameters() {
    return arguments;
  }

  public TologOptions getTologOptions() {
    return options;
  }

  public TologQuery getQuery() {
    return query;
  }
}
