
// $Id: URILocatorTest.java,v 1.15 2009/02/27 11:57:40 lars.garshol Exp $	

package net.ontopia.infoset.impl.basic.test;

import java.io.File;
import net.ontopia.test.*;
import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.infoset.impl.basic.URILocator;
import net.ontopia.utils.OntopiaRuntimeException;

public class URILocatorTest extends AbstractLocatorTest {

  protected static final String NOTATION = "URI";
  protected static final String ADDRESS = "http://www.ontopia.net/"; // Note: it is normalized
  
  public URILocatorTest(String name) {
    super(name);
  }

  protected LocatorIF createLocator() {
    return createLocator(NOTATION, ADDRESS);
  }

  protected LocatorIF createLocator(String notation, String address) {
    if (!NOTATION.equals(notation))
      throw new OntopiaRuntimeException("Notation '" + notation +
					"' unsupported.");
    try {
      return new URILocator(address);
    } catch (java.net.MalformedURLException e) {
      throw new OntopiaRuntimeException(e);
    }
  }
    
  // --- tests

  public void testProperties() {
    LocatorIF locator = createLocator(NOTATION, ADDRESS);
    assertTrue("notation property not correctly set",
	   NOTATION.equals(locator.getNotation()));
    assertTrue("address property not correctly set",
	   ADDRESS.equals(locator.getAddress()));
  }

  public void testFileWithPlus() {
    File file = new File("+");
    LocatorIF locator = new URILocator(file);
    String correct = getCorrectFileURI(file);
    assertTrue("+ character not escaped correctly, got '" + locator.getAddress() + "'"
               + ", correct: '" + correct + "'",
               locator.getAddress().equals(correct));
  }

  public void testFileWithPercent() {
    File file = new File("%");
    LocatorIF locator = new URILocator(file);
    String correct = getCorrectFileURI(file);
    assertTrue("% character not escaped correctly: '" + locator.getAddress() + "', " +
               "correct: '" + correct + "'",
               locator.getAddress().equals(correct));
  }

  public void testFileWithSpace() {
    File file = new File("/My Toilet Paper Rolls/roll1.rl");
    LocatorIF locator = new URILocator(file);
    String correct = getCorrectFileURI(file);
    assertTrue("incorrect file2url conversion: '" + locator.getAddress() + "', " +
               "correct: '" + correct + "'",
               locator.getAddress().equals(correct));
  }

  public void testFileWithNorwegian() {
    File file = new File("d\u00E5j\u00E6.mov");
    LocatorIF locator = new URILocator(file);
    String correct = getCorrectFileURI(file);
    assertTrue("incorrect file2url conversion: '" + locator.getAddress() + "', " +
               "correct: '" + correct + "'",
               locator.getAddress().equals(correct));
  }

  public void testGetExternalFormSimple() {
    testExternalForm("http://www.example.com", "http://www.example.com/");
  }

  public void testGetExternalFormSimple2() {
    testExternalForm("http://www.example.com/index.jsp",
                     "http://www.example.com/index.jsp");
  }

  public void testGetExternalFormSimple3() {
    testExternalForm("http://www.example.com/index.jsp?bongo",
                     "http://www.example.com/index.jsp?bongo");
  }

  public void testGetExternalFormSimple4() {
    testExternalForm("http://www.example.com/index.jsp?bongo#bash",
                     "http://www.example.com/index.jsp?bongo#bash");
  }

  public void testGetExternalFormHostname() {
    testExternalForm("http://www.%F8l.no/", "http://www.%F8l.no/");
  }
  
  public void testGetExternalFormDirname() {
    testExternalForm("http://www.ontopia.no/%F8l.html",
                     "http://www.ontopia.no/%F8l.html");
  }

  public void testGetExternalFormDirnameSpace() {
    testExternalForm("http://www.ontopia.no/space%20in%20url.html",
                     "http://www.ontopia.no/space%20in%20url.html");
  }

  public void testGetExternalFormDirnameSpace2() {
    testExternalForm("http://www.ontopia.no/space+in+url.html",
                     "http://www.ontopia.no/space%20in%20url.html");
  }

  public void testGetExternalFormOfWindowsFile() {
    testExternalForm("file:///C|/topicmaps/opera/occurs/region.htm",
                     "file:/C|/topicmaps/opera/occurs/region.htm");
  }

  public void testGetExternalFormWithSillyPipe() {
    testExternalForm("http://www.ontopia.net/this|that/",
                     "http://www.ontopia.net/this%7Cthat/");
  }

  public void _testGetExternalFormBug2105() {
    testExternalForm("http://en.wikipedia.org/wiki/Anton\u00EDn_Dvo\u0159\u00E1k",
                     "http://en.wikipedia.org/wiki/Anton%C3%ADn_Dvo%C5%99%C3%A1k");
  }
  
  // --- Internal

  private void testExternalForm(String uri, String external) {
    try {
      LocatorIF locator = new URILocator(uri);
      assertTrue("incorrect external form for URI '" + uri + "': '" +
                 locator.getExternalForm() + "', correct '" + external + "'",
                 locator.getExternalForm().equals(external));
    } catch (java.net.MalformedURLException e) {
      fail("INTERNAL ERROR: " + e);
    }
  }
  
  private String getCorrectFileURI(File file) {
    // produce initial string
    String uri = file.getAbsolutePath().replace(File.separatorChar, '/');
    if (!uri.startsWith("/"))
      uri = "/" + uri;
    uri = "file:" + uri;

    // now, transcode to UTF-8
    try {
      byte raw[] = uri.getBytes("UTF-8");
      return new String(raw, 0, raw.length, "8859_1");
    } catch (java.io.UnsupportedEncodingException e) {
      throw new OntopiaRuntimeException(e);
    }
  }
  
}
