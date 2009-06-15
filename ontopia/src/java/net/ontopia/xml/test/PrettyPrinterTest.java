
// $Id: PrettyPrinterTest.java,v 1.10 2004/12/01 15:09:24 larsga Exp $	

package net.ontopia.xml.test;

import java.io.StringWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;
import net.ontopia.test.*;
import net.ontopia.xml.PrettyPrinter;

public class PrettyPrinterTest extends AbstractOntopiaTestCase {
  private static final String NL = System.getProperty("line.separator");
    
  public PrettyPrinterTest(String name) {
    super(name);
  }

  public void testMinimalDocument() {
    try {
      StringWriter writer = new StringWriter();
      PrettyPrinter printer = setUpPrinter(writer);
      printer.startElement("doc", new AttributeListImpl());
      printer.endElement("doc");
      printer.endDocument();

      verify(writer,
	     "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>" + NL
	     + "<doc>"
	     + "</doc>" + NL);
    }
    catch (SAXException e) {
      assertTrue("SAXException: " + e, false);
    }
  }

  public void testDocumentWithAllConstructs() {
    try {
      StringWriter writer = new StringWriter();
      PrettyPrinter printer = setUpPrinter(writer);

      AttributeListImpl attrs = new AttributeListImpl();
      attrs.addAttribute("a", "CDATA", "v");
      printer.startElement("doc", attrs);
      printer.processingInstruction("pi", "data");
      String str = "A bit of character data!";
      printer.characters(str.toCharArray(), 0, str.length());
      str = "    ";
      printer.ignorableWhitespace(str.toCharArray(), 0, str.length());
      printer.endElement("doc");
      printer.endDocument();

      verify(writer,
	     "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>" + NL
	     + "<doc a=\"v\"><?pi data?>"
	     + "A bit of character data!</doc>" + NL);
    }
    catch (SAXException e) {
      assertTrue("SAXException: " + e, false);
    }
  }

  public void testChardataEscaping() {
    try {
      StringWriter writer = new StringWriter();
      PrettyPrinter printer = setUpPrinter(writer);

      printer.startElement("doc", new AttributeListImpl());
      String str = "A <, and a & and a >.";
      printer.characters(str.toCharArray(), 0, str.length());
      printer.endElement("doc");
      printer.endDocument();

      verify(writer,
	     "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>" + NL
	     + "<doc>A &lt;, and a &amp; and a &gt;.</doc>" + NL);
    }
    catch (SAXException e) {
      assertTrue("SAXException: " + e, false);
    }
  }

  public void testAttributeEscaping() {
    try {
      StringWriter writer = new StringWriter();
      PrettyPrinter printer = setUpPrinter(writer);

      AttributeListImpl attrs = new AttributeListImpl();
      attrs.addAttribute("a", "CDATA", "\"<&");
      printer.startElement("doc", attrs);
      printer.endElement("doc");
      printer.endDocument();

      verify(writer,
	     "<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"yes\"?>" + NL
	     + "<doc a=\"&quot;&lt;&amp;\"></doc>" + NL);
    }
    catch (SAXException e) {
      assertTrue("SAXException: " + e, false);
    }
  }

  // --- Internal methods

  private PrettyPrinter setUpPrinter(StringWriter writer)
    throws SAXException {
    PrettyPrinter pp = new PrettyPrinter(writer, "iso-8859-1");
    pp.startDocument();
    return pp;
  }

  private void verify(StringWriter out, String expected) {
    String result = out.toString();

    int elen = expected.length();
    int rlen = result.length();

    int ix;
    for (ix = 0; ix < rlen && ix < elen && 
           result.charAt(ix) == expected.charAt(ix); ix++)
      ;

    if (ix < rlen && rlen > elen)
      fail("Result longer than expected; expected: " + elen + "; " +
           "result: " + rlen + "; rest: " + getRest(result, expected));
    else if (ix < elen && rlen < elen)
      fail("Result shorter than expected; expected: " + elen + "; " +
           "result: " + rlen + "; rest: " + getRest(expected, result));
    else if (ix < rlen && rlen == elen)
      fail("Result differs from expected in position " + ix + "; " +
           "result: " + result.charAt(ix) + " (" +
           encode(result.charAt(ix)) + "; " +
           "expected: " + expected.charAt(ix) + " (" +
           encode(expected.charAt(ix)));
  }

  // assumes s1.length() > s2.length()
  private String getRest(String s1, String s2) {
    StringBuffer buf = new StringBuffer();
    for (int ix = s2.length(); ix < s1.length(); ix++) 
      buf.append(encode(s1.charAt(ix)) + " ");
    return buf.toString();
  }

  private String encode(char ch) {
    return ("U+" +
            encodeHexDigit((ch & 0xF000) >> 12) +
            encodeHexDigit((ch & 0x0F00) >> 8) +
            encodeHexDigit((ch & 0x00F0) >> 4) +
            encodeHexDigit(ch & 0x000F));
  }
  
  private char encodeHexDigit(int value) {
    if (value <= 9)
      return (char) ('0' + value);
    else
      return (char) ('A' + (value - 10));
  }
}
