
// $Id: SharedStoreRegistry.java,v 1.19 2007/04/30 09:16:09 grove Exp $

package net.ontopia.topicmaps.entry;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * INTERNAL: Registry of topicmap repositories that are to be shared
 * between web applications. Instances of this class can be put into
 * JNDI.
 *
 * @since 1.3.2
 */
public class SharedStoreRegistry {
  
  // Define a logging category.
  static Logger log = Logger.getLogger(SharedStoreRegistry.class.getName());

  // Create default registry, which has registryName == null
  protected static final SharedStoreRegistry defaultRegistry = new SharedStoreRegistry();
  
  protected static Map repositories = new HashMap();
  
  protected String sourceLocation;
  protected String resourceName;
  protected String registryName;
  
  /**
   * INTERNAL: Sets the location of the topicmap sources xml file. If
   * null, load tm-sources.xml from classpath.
   */
  public void setSourceLocation(String sourceLocation) {
    this.sourceLocation = sourceLocation;
  }
  
  /**
   * INTERNAL: Sets the resource name of the topicmap sources xml file
   * to load from classpath. The default is 'tm-sources.xml'.
   */
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }
  
  /**
   * INTERNAL: Sets the registry name. Think at least twice before
   * using it.
   */
  public void setRegistryName(String registryName) {
    this.registryName = registryName;
  }

  /**
   * INTERNAL: Returns the default shared registry, of which there is one per JVM.
   */
  public static final SharedStoreRegistry getDefaultRegistry() {
    return defaultRegistry;
  }
  
  /**
   * INTERNAL: Method which returns the shared topic map repository
   * instance.
   *
   * @since 2.2
   */ 
  public synchronized TopicMapRepositoryIF getTopicMapRepository() {
    
    synchronized (repositories) {
      // look up repository in shared map
      TopicMapRepositoryIF repository = (TopicMapRepositoryIF)repositories.get(registryName);
      
      // return repository if already initialized
      if (repository != null) 
        return repository;
      
      if (sourceLocation != null)
        // load repository from file
        repository = XMLConfigSource.getRepository(sourceLocation);
      else if (resourceName != null)
        // load named repository from the classpath
        repository = XMLConfigSource.getRepositoryFromClassPath(resourceName);
      else
        // load repository from the classpath
        repository = XMLConfigSource.getRepositoryFromClassPath();
      
      // update shared map
      repositories.put(registryName, repository);
      
      return repository;
    }
  }
  
  
  /**
   * INTERNAL: Method which replaces the currently shared topic map
   * repository with another one. The repository is closed, the
   * configuration reread and a new shared repository instance
   * created.
   *
   * @since 2.2
   */ 
  public synchronized TopicMapRepositoryIF replaceTopicMapRepository() {
    
    synchronized (repositories) {
      
      // load repository from file
      TopicMapRepositoryIF newRepository;
      if (sourceLocation != null)
        newRepository = XMLConfigSource.getRepository(sourceLocation);
      else
        // load repository from the classpath
        newRepository = XMLConfigSource.getRepositoryFromClassPath();
      
      // look up repository in shared map
      TopicMapRepositoryIF repository = (TopicMapRepositoryIF)repositories.get(registryName);
      
      // close exising repository
      if (repository != null) {
        try {
          repository.close();
        } catch (Throwable t) {
          log.error("Problems occurred when closing shared store registry.", t);
        }      
      }
      
      // update shared map
      repositories.put(registryName, newRepository);
      
      return newRepository;
    }
  }

}
