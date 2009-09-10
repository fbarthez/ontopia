
// $Id: SimpleQueryTracer.java,v 1.1 2006/10/19 13:56:41 larsga Exp $

package net.ontopia.topicmaps.query.impl.utils;

import java.io.Writer;
import java.io.IOException;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.topicmaps.query.impl.basic.QueryTracer;

/**
 * INTERNAL: Used to get a trace from a specific query into a writer
 * for ease of display. <b>Warning:</b> this method is not
 * thread-safe; any other queries running simultaneously will pollute
 * the trace.
 */
public class SimpleQueryTracer extends QueryTracer.TracePrinter {
  private Writer out;

  public SimpleQueryTracer(Writer out) {
    this.out = out;
  }
  
  public boolean isEnabled() {
    return true;
  }

  public void output(String message) {
    try {
      out.write(message + "\n");
      out.flush();
    } catch (IOException e) {
      throw new OntopiaRuntimeException(e);
    }
  }  
}
