
// $Id: PropertyUtils.java,v 1.12 2008/12/04 11:32:39 lars.garshol Exp $

package net.ontopia.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
  
/** 
 * INTERNAL: Utility class for handling properties and their values.
 */

public class PropertyUtils {

  // Define a logging category.
  static Logger log = LoggerFactory.getLogger(PropertyUtils.class.getName());
  
  /**
   * INTERNAL: Helper method used to get better error messages with
   * less typing.
   */
  public static String getProperty(Map properties, String name) {
    return getProperty(properties, name, true);
  }

  /**
   * INTERNAL: Helper method used to get better error messages with
   * less typing.
   */
  public static String getProperty(Map properties, String name,
                                   boolean required) {
    String value = (String) properties.get(name);
    if (value == null) {
      if (!required)
        return null;
      else
        throw new IllegalArgumentException("No value for required property '" +
                                           name + "'");
    }
    return value;
  }

  /**
   * INTERNAL: Helper method used to get the value of boolean
   * properties. This method will return true if the property has the
   * values 'yes', 'true'. Otherwise the default value is returned.
   */
  public static boolean isTrue(Map properties, String name, boolean default_value) {
    return isTrue((String)properties.get(name), default_value);
  }
  
  /**
   * INTERNAL: Same as isTrue(Map, String, boolean) with the default
   * set to false;
   */
  public static boolean isTrue(Map properties, String name) {
    return isTrue(properties, name, false);
  }
  
  public static boolean isTrue(String property_value) {
    return isTrue(property_value, false);
  }
  
  public static boolean isTrue(String property_value, boolean default_value) {
    if (property_value == null)
      return default_value;
    else
      return (property_value.equalsIgnoreCase("true") || property_value.equalsIgnoreCase("yes"));
  }
  
  /**
   * INTERNAL: Returns the property value as an int. If the value is
   * not set or any problems occur the default value is returned.
   */
  public static int getInt(String property_value, int default_value) {
    if (property_value == null)
      return default_value;
    else
      try {
        return getInt(property_value);
      } catch (NumberFormatException e) {
        log.warn(e.toString());
        return default_value;
      }
  }

  public static int getInt(String property_value) throws NumberFormatException {
    return Integer.parseInt(property_value);
  }

  /**
   * INTERNAL; Reads properties from a file. 
   */
  public static Properties loadProperties(String propfile) throws IOException {
    return loadProperties(new File(propfile));
  }
  
  /**
   * INTERNAL; Reads properties from a file. 
   */
  public static Properties loadProperties(File propfile) throws IOException {
    if (!propfile.exists())
      throw new OntopiaRuntimeException("Property file '" + propfile.getPath() + "' does not exist.");
    
    // Load properties from file
    Properties properties = new Properties();
    properties.load(new FileInputStream(propfile));
    return properties;
  }

  public static Properties loadPropertiesFromClassPath(String resource) throws IOException {
    // Load properties from classpath
    ClassLoader cloader = PropertyUtils.class.getClassLoader();
    Properties properties = new Properties();
    InputStream istream = cloader.getResourceAsStream(resource);
    if (istream == null)
      return null;
    properties.load(istream);
    return properties;
  }

  public static Properties loadProperties(InputStream istream) throws IOException {
    Properties properties = new Properties();
    properties.load(istream);
    return properties;
  }
  
}