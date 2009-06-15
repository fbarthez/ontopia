// $Id: TidyHTML.java,v 1.3 2002/05/29 13:38:47 hca Exp $

package net.ontopia.xml;

import java.io.IOException;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import org.w3c.tidy.Tidy;

import org.apache.log4j.Logger;

/**
 * Generate tidied HTML from a URL stream.
 * It's possible to use multiple Tidy-Processes
 * running on separate threads.
 *
 */
public class TidyHTML implements Runnable {

  private String strUrl;
  private String outFileName;
  private String errOutFileName;
  private boolean xmlOut;

  protected Logger log;
  {
    log = Logger.getLogger(this.getClass());
  }
  

  /**
   * Specify <code>InputStream</code> which should been parsed,
   * and the <code>OutputStream</code> to which the pretty-printed result
   * should been send.
   *
   * @param errOutFileName Name of the file for writing out errors/warnings
   * @param xmlOut generate XML output, otherwise HTML
   */
  public TidyHTML(String strUrl, String outFileName,
		  String errOutFileName, boolean xmlOut) {
    this.strUrl = strUrl;
    this.outFileName = outFileName;
    this.errOutFileName = errOutFileName;
    this.xmlOut = xmlOut;
  }

  /**
   * start the tidification
   */
  public void run() {
    URL url;
    BufferedInputStream in;
    FileOutputStream out;
    Tidy tidy = new Tidy();
    tidy.setXmlOut(xmlOut);
    try {
      tidy.setErrout(new PrintWriter(new FileWriter(errOutFileName), true));
      url = new URL(strUrl);
      in = new BufferedInputStream(url.openStream());
      out = new FileOutputStream(outFileName);
      tidy.parse(in, out);
    }
    catch ( IOException e ) {
      log.warn( this.toString() + e.toString() );
    }
  }

  /**
   * convenience wrapper to wait until one process has finished
   */
  public void tidify() {
    Thread thread = new Thread(this);
    thread.start();
    // wait until thread has finished
    do {
      try {
	Thread.sleep(10);
      } catch (InterruptedException e) {
      }
    } while (thread.isAlive());
  }

  /**
   * only for test purposes
   */
  public static void main( String[] args ) {
    TidyHTML t = new TidyHTML(args[0], args[1], args[2], true);
    t.tidify();
  }
  
}




