
// $Id: CTMEncodingSniffer.java,v 1.1 2009/02/09 08:20:16 lars.garshol Exp $

package net.ontopia.topicmaps.utils.ctm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.topicmaps.impl.utils.EncodingSnifferIF;

/**
 * INTERNAL: An encoding sniffer for CTM.
 */
public class CTMEncodingSniffer implements EncodingSnifferIF {
  
  public String guessEncoding(PushbackInputStream stream) throws IOException {
    String encoding;

    // Look to seee if there's a UTF-8 BOM (Byte Order Mark) at the
    // start of the stream.
    byte[] bomBuffer = new byte[3];
    boolean foundBom = false;
    int bytesread = stream.read(bomBuffer, 0, 3);    
    if (bytesread == 3) {
      // Check if bomBuffer contains the UTF-8 BOM. Casts necessary to deal
      // with signedness issues. (Java needs unsigned byte!)
      foundBom = (bomBuffer[0] == (byte) 0xEF &&
                  bomBuffer[1] == (byte) 0xBB &&
                  bomBuffer[2] == (byte) 0xBF);
      
      if (!foundBom)
        stream.unread(bomBuffer, 0, 3);
    } else if (bytesread != -1)
      stream.unread(bomBuffer, 0, bytesread);

    if (foundBom) 
      encoding = "utf-8";
    else
      encoding = "iso-8859-1";

    // Now look for an encoding declaration
    byte[] buf = new byte[50];
    int read = stream.read(buf, 0, 50);
    if (read != -1) {
      String start = new String(buf, 0, read);
      stream.unread(buf, 0, read);
      
      // Get the encoding (if any) declared in the document.
      if (start.startsWith("%encoding"))
        encoding = getEncoding(start);
      
      // If a BOM is found then the encoding must be utf-8.
      if (foundBom && encoding != null && !encoding.equals("utf-8"))
        throw new OntopiaRuntimeException("Contradicting encoding information."
            + " The BOM indicates that the encoding should be utf-8,"
            + " but the encoding is declared to be: " + encoding + ".");

      return encoding;
    }
    return encoding;
  }

  private String getEncoding(String buf) {
    // characters 0 - 8 are taken up by '%encoding'
    // now scan past the whitespace
    int ix = 9;
    while (ix < buf.length() &&
           (buf.charAt(ix) == ' ' ||
            buf.charAt(ix) == '\u0009' ||
            buf.charAt(ix) == '\n' ||
            buf.charAt(ix) == '\r'))
      ix++;

    // FIXME: triple-quoted strings should be accepted here
    if (buf.charAt(ix) != '"')
      return null;
    ix++;

    int start = ix;
    while (ix < buf.length() && buf.charAt(ix) != '"')
      ix++;

    return buf.substring(start, ix);
  }
}