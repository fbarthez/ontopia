
package net.ontopia.topicmaps.utils.sdshare;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ontopia.xml.DefaultXMLReaderFactory;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.topicmaps.entry.TopicMapRepositoryIF;
import net.ontopia.topicmaps.entry.TopicMapReferenceIF;

/**
 * INTERNAL: Loads the SDshare client configuration and makes the
 * contents available for the web application.
 */
public class ClientConfig {
  private int checkInterval;
  private Collection<TopicMap> topicmaps;
  static Logger log = LoggerFactory.getLogger(ClientConfig.class.getName());

  public ClientConfig(TopicMapRepositoryIF repository) {
    this.topicmaps = new ArrayList<TopicMap>();

    // locate config file as resource
    String filename = "sdshare-client.xml";
    ClassLoader cloader = ClientConfig.class.getClassLoader();
    if (cloader == null)
      throw new OntopiaRuntimeException("Cannot find class loader.");
    InputStream istream = cloader.getResourceAsStream(filename);
    if (istream == null)
      throw new OntopiaRuntimeException("Cannot find resource: " + filename);

    // actually do the parsing
    try {
      ConfigReader handler = new ConfigReader(repository);
      XMLReader parser = new DefaultXMLReaderFactory().createXMLReader();
      parser.setContentHandler(handler);
      parser.parse(new InputSource(istream));
    } catch (Exception e) {
      throw new OntopiaRuntimeException(e);
    }
  }

  public Collection<TopicMap> getTopicMaps() {
    return topicmaps;
  }

  public int getCheckInterval() {
    return checkInterval;
  }

  // --- Topic map to be synced

  public class TopicMap {
    private String id;
    private Collection<SyncSource> sources;

    public TopicMap(String id) {
      this.id = id;
      this.sources = new ArrayList<SyncSource>();
    }

    public void addSource(SyncSource source) {
      sources.add(source);
    }

    public String getId() {
      return id;
    }

    public Collection<SyncSource> getSources() {
      return sources;
    }
  }

  // --- Source to sync from

  public class SyncSource {
    private String url; // URL of collection feed
    ConsumerClient.CollectionFeed feed;
    ConsumerClient client; // created on demand
    TopicMapReferenceIF reference;

    public SyncSource(String url, TopicMapReferenceIF reference) {
      this.url = url;
      this.reference = reference;
    }
    
    public String getURL() { // of collection feed
      return url;
    }

    public String getSnapshotFeedURL() {
      getClient(); // download feed
      return feed.getSnapshotFeed().getAddress();
    }

    public ConsumerClient getClient() {
      if (client == null) {
        try {
          feed = ConsumerClient.readCollectionFeed(url);
        } catch (IOException e) {
          throw new OntopiaRuntimeException(e);
        } catch (SAXException e) {
          throw new OntopiaRuntimeException(e);
        }
        client = new ConsumerClient(reference,
                                    feed.getFragmentFeed().getAddress());
      }
      return client;
    }

    public TopicMapReferenceIF getReference() {
      return reference;
    }
  }
  
  // --- SAX ContentHandler

  class ConfigReader extends DefaultHandler {
    private TopicMap topicmap; // current <topicmap>
    private String property;   // name of current property
    private StringBuilder buf; // used to collect code
    private boolean keep;      // keep character data?
    private TopicMapRepositoryIF repository;
    private TopicMapReferenceIF reference;

    public ConfigReader(TopicMapRepositoryIF repository) {
      this.buf = new StringBuilder();
      this.repository = repository;
    }
    
    public void startElement(String ns, String localname, String qname,
                              Attributes atts) {
      if (qname.equals("property")) {
        property = atts.getValue("name");
        keep = true;
        
      } else if (qname.equals("topicmap")) {
        String id = atts.getValue("id");
        reference = repository.getReferenceByKey(id);
        if (reference == null)
          log.error("Topic map with id '" + id + "' not found");
        topicmap = new TopicMap(id);
        topicmaps.add(topicmap);
        
      } else if (qname.equals("source"))
        keep = true;
    }

    public void characters (char[] ch, int start, int length) {
      if (keep)
        buf.append(ch, start, length);
    }
    
    public void endElement(String ns, String localname, String qname) {
      if (qname.equals("property")) {
        if (property.equals("check-interval")) {
          try {
            checkInterval = Integer.parseInt(buf.toString());
          } catch (NumberFormatException e) {
            log.error("Couldn't parse checkInterval: '" + buf.toString() + "'");
          }
        } else
          log.warn("Unknown property: '" + property + "'");

        property = null;
        keep = false;
        buf.setLength(0);

      } else if (qname.equals("source")) {
        SyncSource src = new SyncSource(buf.toString(), reference);
        topicmap.addSource(src);
        keep = false;
        buf.setLength(0);
      }
    }
  }
}
