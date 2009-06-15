
// $Id: TMLoginModule.java,v 1.32 2007/08/27 15:36:00 geir.gronmo Exp $

package net.ontopia.topicmaps.nav2.realm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.security.MessageDigest;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.ontopia.net.Base64Encoder;
import net.ontopia.topicmaps.entry.DefaultTopicMapSource;
import net.ontopia.topicmaps.entry.SharedStoreRegistry;
import net.ontopia.topicmaps.entry.TopicMapReferenceIF;
import net.ontopia.topicmaps.entry.TopicMapRepositoryIF;
import net.ontopia.topicmaps.entry.TopicMaps;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.core.TopicMapIF;
import net.ontopia.topicmaps.core.TopicMapStoreIF;
import net.ontopia.topicmaps.core.TMObjectIF;
import net.ontopia.topicmaps.nav2.utils.NavigatorUtils;
import net.ontopia.topicmaps.nav2.impl.basic.NavigatorApplication;
import net.ontopia.topicmaps.query.core.QueryProcessorIF;
import net.ontopia.topicmaps.query.core.QueryResultIF;
import net.ontopia.topicmaps.query.core.InvalidQueryException;
import net.ontopia.topicmaps.query.utils.QueryUtils;
import net.ontopia.utils.OntopiaRuntimeException;

import org.apache.log4j.Logger;

/**
 * INTERNAL: TMLoginModule provides authentication to web applications by 
 * checking user credentials against information stored in a topicmap.
 */
public class TMLoginModule implements LoginModule {
  
  // initialization of logging facility
  private static Logger log = Logger.getLogger(TMLoginModule.class.getName());
  
  // state obtained in the initialize() method
  private Subject subject;
  private CallbackHandler callbackHandler;
  private Map sharedState;
  private Map options;

  // the authentication status
  private boolean loginSucceeded;
  private boolean commitSucceeded;

  // username, password and hash method
  private String username;
  private String password;
  private String hashMethod;
  
  // principals
  private Principal userPrincipal;
  private List rolePrincipals;

  private String jndiname;
  private String topicmapId;
  private String repositoryId;
  
  public TMLoginModule() {
    log.debug("TMLoginModule: constructor");
    rolePrincipals = new ArrayList();
  }

  // LoginModule interface methods ...  
  
  public boolean abort() throws LoginException {
    if (!loginSucceeded) {
      return false;
    } else if (commitSucceeded == false) {
      // login succeeded but overall authentication failed      
      loginSucceeded = false;
      username = null;
      password = null;
      userPrincipal = null;
      rolePrincipals.clear();
    } else {
      // overall authentication succeeded and commit succeeded,
      // but someone else's commit failed
      logout();
    }
    return true;
  }

  /**
   * Add relevant Principals to the subject.
   */
  public boolean commit() throws LoginException {
    if (!loginSucceeded)
      return false;

    // add user principal if not already exists
    userPrincipal = new UserPrincipal(username);
    if (!subject.getPrincipals().contains(userPrincipal))
      subject.getPrincipals().add(userPrincipal);
    
    // Use a query to find all the RolePrincipals of the user.
    processRoles();
    
    // Add all the roleprincipals (whenever necessary) to subject.  
    Iterator iter = rolePrincipals.iterator();
    while (iter.hasNext()) {
      Principal rolePrincipal = (Principal)iter.next();
      if (!subject.getPrincipals().contains(rolePrincipal))
        subject.getPrincipals().add(rolePrincipal);
    }
    
    log.debug("TMLoginModule: committed");      
    commitSucceeded = true;

    // clean out state
    username = null;
    password = null;

    return true;
  }
  
  public void initialize(Subject subject, CallbackHandler callbackHandler,
    Map sharedState, Map options) {
    log.debug("TMLoginModule: initialize");
    
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    this.sharedState = sharedState;
    this.options = options;
    
    // get options
    jndiname = (String)options.get("jndi_repository");
    if (jndiname == null) jndiname = (String)options.get("jndiname");
    topicmapId = (String)options.get("topicmap");
    repositoryId = (String)options.get("repository");
    if (topicmapId == null)
      throw new OntopiaRuntimeException("'topicmap' option is not provided to the JAAS module. Check jaas.config file.");
    
    hashMethod = (String)options.get("hashmethod");
    if (hashMethod == null)
      hashMethod = "plaintext";
  }

  /** 
   * Prompt the user for username and password, and verify those.
   */
  public boolean login() throws LoginException {
    log.debug("TMLoginModule: login");
    
    if (callbackHandler == null)
      throw new LoginException("Error: no CallbackHandler available " +
              "to garner authentication information from the user");
    
    // prompt for a user name and password
    NameCallback nameCallback =  new NameCallback("user name: ");
    PasswordCallback passwordCallback = new PasswordCallback("password: ",
            false);
    
    try {
      callbackHandler.handle(new Callback[] {nameCallback, passwordCallback});

      this.username = nameCallback.getName();
      char[] password = passwordCallback.getPassword();
      this.password = (password == null ? "" : new String(password));
      passwordCallback.clearPassword();
      
    } catch (java.io.IOException ioe) {
      throw new LoginException(ioe.toString());
    } catch (UnsupportedCallbackException uce) {
      throw new LoginException("Error: " + uce.getCallback() +
              " not available to garner authentication information " +
              "from the user");
    }
    // verify the username/password
    loginSucceeded = verifyUsernamePassword(username, password);
    return loginSucceeded;
  }
  
  public boolean logout() throws LoginException {    
    // clear out principals
    subject.getPrincipals().remove(userPrincipal);

    Iterator iter = rolePrincipals.iterator();
    while (iter.hasNext()) {
      Principal rolePrincipal = (Principal)iter.next();
      if (!subject.getPrincipals().contains(rolePrincipal))
        subject.getPrincipals().remove(rolePrincipal);
    }
    log.debug("TMLoginModule: logout");

    // clean out state
    loginSucceeded = false;
    commitSucceeded = false;
    username = null;
    password = null;
    userPrincipal = null;
    rolePrincipals.clear();
    return true;
  }

  // ... LoginModule interface methods.  

  private static String getName(TopicIF topic) {
    return net.ontopia.topicmaps.utils.TopicStringifiers.getDefaultStringifier().toString(topic);
  }
    
  private static String getId(Object that) {
    if (that instanceof TMObjectIF)
      return NavigatorUtils.getStableId((TMObjectIF) that);
    else if (that instanceof TopicMapReferenceIF) 
      return ((TopicMapReferenceIF)that).getId();
    else
      return null;
  }

  protected TopicMapIF getTopicMap() {

    TopicMapStoreIF store;
    boolean readonly = true;
    if (jndiname != null) {
      SharedStoreRegistry ssr = NavigatorApplication.lookupSharedStoreRegistry(jndiname);
      TopicMapRepositoryIF repository = ssr.getTopicMapRepository();
      TopicMapReferenceIF ref = repository.getReferenceByKey(topicmapId);
      
      try {
        store = ref.createStore(readonly);
      } catch (java.io.IOException e) {
        throw new OntopiaRuntimeException("Unable to create store for '" + topicmapId + "'", e);
      }
    } else {
      if (repositoryId == null)
        store = TopicMaps.createStore(topicmapId, readonly);
      else
        store = TopicMaps.createStore(topicmapId, readonly, repositoryId);
      
    }      
    log.debug("TMLoginModule Initialised Correctly");
    return store.getTopicMap();
  }

  public static String hashPassword(String username, String password,
                                    String hashMethod) {
    String encodedPassword;
    if (hashMethod.equals("base64")) {
      try {
        Base64Encoder encoder = new Base64Encoder();
        encodedPassword = encoder.encode(username+password);
      } catch (Exception e) {
        throw new OntopiaRuntimeException(
                "Problem occurred when attempting to hash password", e);
      }
    } else if (hashMethod.equals("md5")) {
      try {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        Base64Encoder encoder = new Base64Encoder();
        byte[] digest = messageDigest.digest((username+password).getBytes("ISO-8859-1"));
        encodedPassword = encoder.encode(new String(digest, "ISO-8859-1"));
      } catch (Exception e) {
        throw new OntopiaRuntimeException(
                "Problems occurrend when attempting to hash password", e);
      }
    } else if (hashMethod.equals("plaintext")) {
      encodedPassword = password;
    } else {
      throw new OntopiaRuntimeException("Invalid password encoding: "
              + hashMethod);
    }
    return encodedPassword;
  }
  
  /**
    * Query the topicmap for any roles played by the USER.
    * Create RolePrincipals for each of those roles.
    */
  private void processRoles() { // NOTE: ignoring roles for now

    TopicMapIF topicMap = getTopicMap();  
    QueryResultIF queryResult = null;
    try {
      QueryProcessorIF queryProcessor = QueryUtils.getQueryProcessor(topicMap);
      log.info("Processing roles for user '" + username + "'");
      String query = 
        "using um for i\"http://psi.ontopia.net/userman/\""
        + "select $ROLE, $PRIVILEGE from "
        + "instance-of($USER, um:user), "
        + "occurrence($USER, $O1), type($O1, um:username), value($O1, %USERNAME%), "
        + "um:plays-role($USER : um:user, $ROLE : um:role), "
        + "{ um:has-privilege($ROLE : um:receiver, $PRIVILEGE : um:privilege) }?";
      
      Map params = Collections.singletonMap("USERNAME", username);
      queryResult = queryProcessor.execute(query, params);

      Collection visited = new HashSet();
      while (queryResult.next()) {
        // register role (aka user-group)
        TopicIF r = (TopicIF) queryResult.getValue(0);
        if (!visited.contains(r)) {
          String rolename = getName(r);
          if (rolename != null)
            rolePrincipals.add(new RolePrincipal(rolename));
          visited.add(r);
          log.info("Added role-principal from user-group '" + rolename + "' for user '" + username + "'");
        }
        // register privilege
        TopicIF p = (TopicIF) queryResult.getValue(1);
        if (!visited.contains(p)) {
          String rolename = getName(p);
          if (rolename != null)
            rolePrincipals.add(new RolePrincipal(rolename));
          visited.add(p);
          log.info("Added role-principal from privilege '" + rolename + "' for user '" + username + "'");
        }
      }
      // all users have implicit role 'user'
      log.info("Added implicit role-principal 'user' for user '" + username + "'");
      rolePrincipals.add(new RolePrincipal("user"));
      
    } catch (InvalidQueryException e) {
      throw new OntopiaRuntimeException(e);
    } finally {
      if (queryResult != null) queryResult.close();
      if (topicMap != null) topicMap.getStore().close();
    }
  }
  
  private boolean verifyUsernamePassword(String username, String password) {
    if (username == null || password == null) return false;
    
    TopicMapIF topicMap = getTopicMap();
    QueryResultIF queryResult = null;
    try {
      log.debug("Topic map: " + topicMap);    
      QueryProcessorIF queryProcessor = QueryUtils.getQueryProcessor(topicMap);
      String query = 
        "using um for i\"http://psi.ontopia.net/userman/\" "
        + "select $USER from "
        + "instance-of($USER, um:user), "
        + "occurrence($USER, $O1), type($O1, um:username), value($O1, %USERNAME%), "
        + "occurrence($USER, $O2), type($O2, um:password), value($O2, %PASSWORD%)?";

      Map params = new HashMap(2);
      params.put("USERNAME", username);
      params.put("PASSWORD", hashPassword(username, password, hashMethod));
      queryResult = queryProcessor.execute(query, params);

      if (queryResult.next()) {
        TopicIF user = (TopicIF) queryResult.getValue(0);
        log.info("Authenticated user: " + user);
        return true;
      } else {
        log.info("User '" + username + "' not authenticated");
        return false;
      }
    } catch (InvalidQueryException e) {
      throw new OntopiaRuntimeException(e);
    } finally {
      if (queryResult != null) queryResult.close();
      if (topicMap != null) topicMap.getStore().close();
    }
  }
}
