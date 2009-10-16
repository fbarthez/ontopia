package net.ontopia.topicmaps.query.toma.impl.basic.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ontopia.topicmaps.query.core.InvalidQueryException;
import net.ontopia.topicmaps.query.toma.impl.utils.Stringifier;

/**
 * INTERNAL: Converts a string into a number.
 */
public class ToNumFunction extends AbstractSimpleFunction {

  private static Pattern pattern = Pattern
      .compile("^\\s*([+\\-]?[0-9]+[\\.]?[0-9]*(?:[eE][+\\-]?[0-9]+)?).*");

  public ToNumFunction() {
    super("TO_NUM", 0);
  }

  public static String convertToNumber(Object obj) throws InvalidQueryException {
    String str = Stringifier.toString(obj);
    if (str != null) {
      Matcher m = pattern.matcher(str);
      if (m.matches()) {
        return m.group(1);
      } else {
        return "0";
      }
    } else {
      return "0";
    }
  }
  
  public String evaluate(Object obj) throws InvalidQueryException {
    return convertToNumber(obj);
  }
}