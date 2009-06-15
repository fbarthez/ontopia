
// $Id: RAPServlet.java,v 1.36 2008/07/18 13:24:56 lars.garshol Exp $

package net.ontopia.topicmaps.utils.tmrap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.infoset.impl.basic.URILocator;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.nav2.core.NavigatorApplicationIF;
import net.ontopia.topicmaps.nav2.core.NavigatorRuntimeException;
import net.ontopia.topicmaps.nav2.utils.NavigatorUtils;
import net.ontopia.topicmaps.xml.XTMTopicMapWriter;
import net.ontopia.xml.PrettyPrinter;

import org.apache.log4j.Logger;

/**
 * EXPERIMENTAL: Implements the TMRAP protocol.
 */
public class RAPServlet extends HttpServlet {
  
  // Much wanted by Serializable. (The number is randomly typed).
  private static final long serialVersionUID = 3585458045457498992l;

  // initialization of logging facility
  private static Logger log = Logger.getLogger(RAPServlet.class.getName());
  
  // Static names for request parameters
  public static final String CLIENT_PARAMETER_NAME    = "client";
  public static final String FRAGMENT_PARAMETER_NAME  = "fragment";
  public static final String INDICATOR_PARAMETER_NAME = "identifier";
  public static final String SOURCE_PARAMETER_NAME    = "item";
  public static final String SUBJECT_PARAMETER_NAME   = "subject";
  public static final String SYNTAX_PARAMETER_NAME    = "syntax";
  public static final String TOLOG_PARAMETER_NAME     = "tolog";
  public static final String TOPICMAP_PARAMETER_NAME  = "topicmap";
  public static final String VIEW_PARAMETER_NAME      = "view";
  public static final String COMPRESS_PARAMETER_NAME  = "compress";

  public static final String SYNTAX_ASTMA  = "text/x-astma";
  public static final String SYNTAX_LTM    = "text/x-ltm";
  public static final String SYNTAX_TM_XML = "text/x-tmxml";
  public static final String SYNTAX_TOLOG  = "text/x-tolog";
  public static final String SYNTAX_XTM    = "application/x-xtm";
  
  public static final String RAP_NAMESPACE = "http://psi.ontopia.net/tmrap/";
  
  private static final String LKEY_PARAMETER_NAME     = "iwant";
  private static final String LKEY_PARAMETER_VALUE    = "alicensekeyplease";
  
  // Used to register type listeners
  Map clientListeners = new HashMap();
  
  private TMRAPConfiguration rapconfig;

  // --- Servlet interface implementation
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    rapconfig = new TMRAPConfiguration(config);
  }

  /** 
   * Supported TMRAP protocol requests:
   * <pre>
   *  GET /xtm-fragment?topicmap=[]&source=[]&indicator=[]
   *  GET /topic-page?topicmap=[]&source=[]&indicator=[]
   * </pre>
   */         
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doGet(request, response, request.getRequestURL().toString());
  }
  
  /** INTERNAL
   * A variant of 'doGet' that allows the caller to specify the URLString.
   * Useful when 'request' doesn't support getRequestURL() (e.g. when testing).
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response,
      String URLString) throws IOException, ServletException {
    if (URLString.endsWith("get-tolog"))//
      getTolog(request, response);
    else if (URLString.endsWith("get-topic"))//
      getTopic(request, response); 
    else if (URLString.endsWith("get-topic-page"))//
      getTopicPage(request, response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response, request.getRequestURL().toString());
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response,
      String URLString) throws IOException {
    if (URLString.endsWith("add-fragment"))//
      addFragment(request, response);
    else if (URLString.endsWith("update-topic"))//
      updateTopic(request, response);
    else if (URLString.endsWith("delete-topic"))//
      deleteTopic(request, response);
    else if (URLString.endsWith("add-type-listener"))//
      addTypeListener(request, response);
    else if (URLString.endsWith("remove-type-listener"))//
      removeTypeListener(request, response);
  }

  // --- TMRAP request implementations
  
  private void getTopicPage(HttpServletRequest request,
                            HttpServletResponse response)
    throws IOException { 

    try {
      // get context
      NavigatorApplicationIF navapp =
        NavigatorUtils.getNavigatorApplication(getServletContext());
      
      // get parameters
      Collection indicators = getIndicators(request);
      Collection items = getItemIdentifiers(request);
      Collection subjects = getSubjectLocators(request);
      String allowedSyntaxes[] = new String[]{SYNTAX_XTM};
      getParameter(request, response, "get-topic-page", 
          SYNTAX_PARAMETER_NAME, false, allowedSyntaxes, SYNTAX_XTM);
      String[] tmids = request.getParameterValues(TOPICMAP_PARAMETER_NAME);

      TopicMapIF tm = TMRAPImplementation.getTopicPage(navapp, rapconfig,
                                                       items, subjects, indicators,
                                                       tmids);

      // write the response
      response.setContentType("application/xml; charset=utf-8");
      new XTMTopicMapWriter(response.getWriter(), "utf-8").write(tm);
    } catch (Exception e) {
      reportError(response, e);
    } 
  }

  /**
   * Get a tolog query result.
   */  
  private void getTolog(HttpServletRequest request, 
                        HttpServletResponse response)
    throws IOException {
    
    try {
      // get context
      NavigatorApplicationIF navapp =
        NavigatorUtils.getNavigatorApplication(getServletContext()); 
      
      // set up parameters
      String query = request.getParameter(TOLOG_PARAMETER_NAME);
      String tmid = request.getParameter(TOPICMAP_PARAMETER_NAME);
      String syntax = request.getParameter(SYNTAX_PARAMETER_NAME);
      String view = request.getParameter(VIEW_PARAMETER_NAME);
      String compress_string = request.getParameter(COMPRESS_PARAMETER_NAME);
      boolean compress = compress_string != null &&
                         compress_string.equals("true");

      // invoke real implementation
      if (compress) {
        response.setContentType("application/x-gzip");
        GZIPOutputStream out = new GZIPOutputStream(response.getOutputStream());
        PrettyPrinter pp = new PrettyPrinter(out);
        TMRAPImplementation.getTolog(navapp, query, tmid, syntax, view, pp);
        out.finish(); // ensures we get a complete gzip stream...
      } else {
        // not compressed
        response.setContentType("text/xml; charset=utf-8");
        PrettyPrinter pp = new PrettyPrinter(response.getWriter(), "utf-8");
        TMRAPImplementation.getTolog(navapp, query, tmid, syntax, view, pp);
      }
    } catch (Exception e) {
      reportError(response, e);
    }
  }

  /**
   * Add a fragment to a topic map.
   */
  private void addFragment(HttpServletRequest request, 
                          HttpServletResponse response) throws IOException {
    try {
      // get context
      NavigatorApplicationIF navapp =
        NavigatorUtils.getNavigatorApplication(getServletContext()); 
      
      // set up parameters
      String syntax = request.getParameter(SYNTAX_PARAMETER_NAME);
      String fragment = request.getParameter(FRAGMENT_PARAMETER_NAME);
      String tmid = request.getParameter(TOPICMAP_PARAMETER_NAME);

      TMRAPImplementation.addFragment(navapp, fragment, syntax, tmid);
    } catch (Exception e) {
      reportError(response, e);
    }
  }

  /**
   * Update a topic with a fragment.
   */
  private void updateTopic(HttpServletRequest request, 
                           HttpServletResponse response) throws IOException {
    try {
      // get context
      NavigatorApplicationIF navapp =
        NavigatorUtils.getNavigatorApplication(getServletContext()); 
      
      // set up parameters
      String syntax = request.getParameter(SYNTAX_PARAMETER_NAME);
      String fragment = request.getParameter(FRAGMENT_PARAMETER_NAME);
      String tmid = request.getParameter(TOPICMAP_PARAMETER_NAME);
      Collection indicators = getIndicators(request);
      Collection items = getItemIdentifiers(request);
      Collection subjects = getSubjectLocators(request);

      TMRAPImplementation.updateTopic(navapp, fragment, syntax, tmid,
                                      indicators, items, subjects);
    } catch (Exception e) {
      reportError(response, e);
    }
  }
    
  /**
   * Delete a topic.
   */  
  private void deleteTopic(HttpServletRequest request, 
                          HttpServletResponse response) throws IOException {
    try {
      // get context
      NavigatorApplicationIF navapp =
        NavigatorUtils.getNavigatorApplication(getServletContext()); 
      
      // set up parameters
      Collection subjectIndicators = getIndicators(request);
      Collection sourceLocators = getItemIdentifiers(request);
      Collection subjectLocators = getSubjectLocators(request);
      String[] tmids = request.getParameterValues(TOPICMAP_PARAMETER_NAME);

      String msg = TMRAPImplementation.deleteTopic(navapp,
                                                   sourceLocators,
                                                   subjectLocators,
                                                   subjectIndicators,
                                                   tmids);

      response.setContentType("text/plain; charset=us-ascii");
      response.getWriter().write(msg);
    } catch (Exception e) {
      reportError(response, e);
    }
  }
  
  /**
   * Write XTM response for topic fragment. The requested topic is
   * serialized as a fragment.  If more than one topic is located then
   * a unifying topic is added at the end of the XTM fragment.  This
   * topic has all the identities contained in the request.
   */  
  private void getTopic(HttpServletRequest request, 
                        HttpServletResponse response)
    throws IOException, ServletException {
    // this is a magic undocumented request used by the Vizlet to get
    // the license key used on the server side.    
    if (LKEY_PARAMETER_VALUE.equals(request.getParameter(LKEY_PARAMETER_NAME))) {
      getLKEY(response);
      return;
    }

    // get context
    NavigatorApplicationIF navapp =
      NavigatorUtils.getNavigatorApplication(getServletContext()); 
    
    try {
      // fetch topic identity uris from request parameters
      Collection indicators = getIndicators(request);
      Collection items = getItemIdentifiers(request);
      Collection subjects = getSubjectLocators(request);
      String[] tmids = request.getParameterValues(TOPICMAP_PARAMETER_NAME);
      String syntax = request.getParameter(SYNTAX_PARAMETER_NAME);
      String view = request.getParameter(VIEW_PARAMETER_NAME);

      // call real implementation
      response.setContentType("text/xml; charset=utf-8");
      PrettyPrinter pp = new PrettyPrinter(response.getWriter(), "utf-8");
      TMRAPImplementation.getTopic(navapp,
                                   items, subjects, indicators,
                                   tmids, syntax, view, pp);
    } catch (Exception e) {
      reportError(response, e);
    }
  }

  private void addTypeListener(HttpServletRequest request, 
                          HttpServletResponse response) throws IOException {
    // -----------------------------------------------------------------------
    // | Parameter  | Required? | Repeatable? | Type   | Value      | Default |
    // -----------------------------------------------------------------------
    // | item       | no        | yes         | URI    |            |         |
    // | subject    | no        | yes         | URI    |            |         |
    // | identifier | no        | yes         | URI    |            |         |
    // | topicmap   | yes       | no          | String |            |         |
    // | client     | yes       | no          | Handle |            |         |
    // | syntax     | no        | no          | String |            |         |
    // -----------------------------------------------------------------------
    // (+) means other values may be allowed later.
    
    TopicIndexIF topicIndex = null;
    try {
      // fetch topic identity uris from request parameters
      Collection subjectIndicators = getIndicators(request);
      Collection sourceLocators = getItemIdentifiers(request);
      Collection subjectLocators = getSubjectLocators(request);
        
      // Check that the topicmap parameter was given (since it's required).
      getParameter(request, response, "add-type-listener", 
          TOPICMAP_PARAMETER_NAME, true, null, null);
  
      // Once supported, syntax will determine the output syntax.
      String allowedSyntaxes[] = new String[]{SYNTAX_XTM};
      String syntax = getParameter(request, response, "add-type-listener", 
          SYNTAX_PARAMETER_NAME, false, allowedSyntaxes, SYNTAX_XTM);
      
      String client = getParameter(request, response, "add-type-listener", 
          CLIENT_PARAMETER_NAME, false, null, SYNTAX_XTM);
      
      // get topic(s)
      topicIndex = getTopicIndex(request.getParameterValues(
          TOPICMAP_PARAMETER_NAME));
      Collection topics = topicIndex.getTopics(subjectIndicators, sourceLocators, subjectLocators);
      
      if (topics.size() != 1)
        reportError(response, "add-type-listener: Wrong number of topics.");
      
      TopicIF topic = (TopicIF)topics.iterator().next();
      
      Map currentTypeListeners = (Map)clientListeners.get(topic);
      if (currentTypeListeners == null) {
        currentTypeListeners = new HashMap();
        clientListeners.put(topic, currentTypeListeners);
      }
      
      // Register the client as a listener for topics of type 'topic'.
      currentTypeListeners.put(client, syntax);
    } catch (RAPServletException e) {
      reportError(response, e);
    } finally {
      closeIndex(topicIndex);
    }
  }
  
  private void removeTypeListener(HttpServletRequest request, 
      HttpServletResponse response) throws IOException {
    // -----------------------------------------------------------------------
    // | Parameter  | Required? | Repeatable? | Type   | Value      | Default |
    // -----------------------------------------------------------------------
    // | item       | no        | yes         | URI    |            |         |
    // | subject    | no        | yes         | URI    |            |         |
    // | identifier | no        | yes         | URI    |            |         |
    // | topicmap   | yes       | no          | String |            |         |
    // | client     | yes       | no          | Handle |            |         |
    // | syntax     | no        | no          | String |            |         |
    // -----------------------------------------------------------------------
    // (+) means other values may be allowed later.
    
    TopicIndexIF topicIndex = null;
    try {
      // fetch topic identity uris from request parameters
      Collection subjectIndicators = getIndicators(request);
      Collection sourceLocators = getItemIdentifiers(request);
      Collection subjectLocators = getSubjectLocators(request);
      
      // Check that the topicmap parameter was given (since it's required).
      getParameter(request, response, "remove-type-listener",
          TOPICMAP_PARAMETER_NAME, true, null, null);
      
      String client = getParameter(request, response, "remove-type-listener", 
      CLIENT_PARAMETER_NAME, false, null, SYNTAX_XTM);
      
      // get topic(s)
      topicIndex = getTopicIndex(request.getParameterValues(
          TOPICMAP_PARAMETER_NAME));
      Collection topics = topicIndex.getTopics(subjectIndicators, sourceLocators, subjectLocators);
      
      if (topics.size() != 1)
        reportError(response, "remove-type-listener: Wrong number of topics.");
      
      TopicIF topic = (TopicIF)topics.iterator().next();
      
      Map currentTypeListeners = (Map)clientListeners.get(topic);
      if (currentTypeListeners == null)
        reportError(response, "remove-type-listener: " +
            "Listener not found. You have to register a listener before it can " +
            "be removed.");
      
      String currentListener = (String)currentTypeListeners.remove(client);
      if (currentListener == null)
        reportError(response, "remove-type-listener: " +
            "Listener not found. You have to register a listener before it can " +
            "be removed.");
    } catch (RAPServletException e) {
      reportError(response, e);
    } finally {
      closeIndex(topicIndex);
    }
  }
  
  // --- Internal helpers

  /**
   * Gets and validates a request parameter.
   * @param request The source of the parameters.
   * @param response The receiver of any error messages.
   * @param operationName The name or the calling operation.
   * @param parameterName The name of the parameter.
   * @param required true iff this parameter is required.
   * @param supported true iff this parameter is supported 
            (allows others than the default value).
   * @param defaultValue The value used if no parameter value is found.
   * @return The parameter value, or defaultValue if it cannot be found.
   * @throws IOException If an error occurs and the error reporting doesn't work
   */
  private String getParameter(HttpServletRequest request,
      HttpServletResponse response, String operationName,
      String parameterName, boolean required, String supported[], 
      String defaultValue) throws RAPServletException {
    String parameters[] = request.getParameterValues(parameterName);
    
    if (parameters == null || parameters.length == 0) {
      if (required)
        throw new RAPServletException("The '" + parameterName + 
            "'-parameter is required for the " + operationName + " operation.");
      return defaultValue;
    } else if (parameters.length == 1) {
      String parameter = parameters[0];
      if (!(supported == null 
          || Arrays.asList(supported).contains(parameter))) {
        throw new RAPServletException("The '" + parameterName
            + "'-parameter of the " + operationName 
            + " does not support the value: \"" + parameter
            + "\". The supported values are "
            + makeSeparatedWords(supported, ", ", " and "));
      }
      
      return parameter;
    }
    // Never suport repeated values.
    throw new RAPServletException("The '" + parameterName 
        + "'-parameter of the " + operationName + " operation does not"
        + " support repeated values.");
  }
  
  private TopicIndexIF getTopicIndex(String[] tmids)
      throws RAPServletException {
    NavigatorApplicationIF navApp =
      NavigatorUtils.getNavigatorApplication(getServletContext()); 

    if (tmids == null || tmids.length == 0)
      return new RegistryTopicIndex(navApp.getTopicMapRepository(), true,
                                    rapconfig.getEditURI(),
                                    rapconfig.getViewURI());

    List topicIndexes = new ArrayList();
    for (int i = 0; i < tmids.length; i++) {
      TopicMapIF topicmap;
      try {
        topicmap = navApp.getTopicMapById(tmids[i], true);
      } catch (NavigatorRuntimeException e) {
        log.warn("Couldn't open topic map " + tmids[i] + " because of " +
            e.getClass().getName() + " with message: " + e.getMessage());
        throw new RAPServletException("Couldn't open topic map " + tmids[i]);
      }
      TopicIndexIF currentIndex =
        new TopicMapTopicIndex(topicmap, rapconfig.getEditURI(),
                               rapconfig.getViewURI(), tmids[i]);
      topicIndexes.add(currentIndex);
    }
    return new FederatedTopicIndex(topicIndexes);
  }
  
  private Collection getURICollection(HttpServletRequest request, 
      String paramName) throws RAPServletException {
    String[] value = request.getParameterValues(paramName);
    if (value == null)
      return Collections.EMPTY_SET;
      
    HashSet uriLocators = new HashSet();
    for (int i = 0; i < value.length; i++) {
      try {
        uriLocators.add(new URILocator(value[i]));
      } catch (MalformedURLException e) {          
        log.warn("MalformedURL: " + value[i]);
        throw new RAPServletException("Malformed URL: " + value[i]);  
      }  
    }       
    return uriLocators;     
  }
    
  private Collection getIndicators(HttpServletRequest request) 
    throws RAPServletException {
    return getURICollection(request, INDICATOR_PARAMETER_NAME);
  }

  private Collection getItemIdentifiers(HttpServletRequest request) 
    throws RAPServletException  {
    return getURICollection(request, SOURCE_PARAMETER_NAME);
  }

  private Collection getSubjectLocators(HttpServletRequest request) 
    throws RAPServletException  {
    return getURICollection(request, SUBJECT_PARAMETER_NAME);
  }

  
  private class RAPServletException extends Exception {
    // Much wanted by Serializable. (The number is randomly typed).
    private static final long serialVersionUID = 7912425438445764224l;

    String message;
    
    public RAPServletException(String message) {
      this.message = message;
      log.warn(message, this);      
    }
    
    public String getMessage() {
      return message;
    }
  }
  
  private String makeSeparatedWords(String[] words, String separator,
      String lastSeparator) {
    if (words.length == 0)
      return "";
    if (words.length == 1)
      return words[0];
    
    int length = words.length;
    String retVal = words[length - 2] + lastSeparator + words[length - 1];
    
    for (int i = length - 3; i >= 0; i--) {
      retVal = words[i] + separator + retVal;
    }
    return retVal;
  }
  
  /**
   * Magic undocumented TMRAP method used by the Vizlet to get its
   * license key.
   */
  private void getLKEY(HttpServletResponse response)
    throws IOException, ServletException {
    response.setContentType("text/plain; charset=iso-8859-1");
    response.getWriter().write("no-need-for-a-licence-anymore");
  }
  
  private void closeIndex(TopicIndexIF topicIndex) {
    if (topicIndex != null)
      topicIndex.close();
  }
  
  private void reportError(HttpServletResponse response, String message) 
      throws IOException {
    log.warn(message);
    try {
      response.sendError(400, message);
    } catch (IOException e) {
      log.warn("Failed to report error: " + message + 
          " because sendError gave " + IOException.class.getName());
      throw e;
    }
  }

  private void reportError(HttpServletResponse response, Throwable t) 
      throws IOException {
    log.warn("Error occurred.", t);
    try {
      response.sendError(400, t.toString());
    } catch (IOException e) {
      log.warn("Failed to report error: " + t.getMessage() + 
               " because sendError gave " + IOException.class.getName());
      throw e;
    }
  }
  
}
