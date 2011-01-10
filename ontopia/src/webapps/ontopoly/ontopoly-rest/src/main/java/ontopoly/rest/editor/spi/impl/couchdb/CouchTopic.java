package ontopoly.rest.editor.spi.impl.couchdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ontopoly.rest.editor.spi.PrestoDataProvider;
import ontopoly.rest.editor.spi.PrestoField;
import ontopoly.rest.editor.spi.PrestoTopic;
import ontopoly.rest.editor.spi.PrestoType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

public class CouchTopic implements PrestoTopic {

  private final PrestoDataProvider dataProvider;
  
  private final ObjectNode data;

  private CouchTopic(CouchDataProvider dataProvider, ObjectNode data) {
    this.dataProvider = dataProvider;
    this.data = data;    
  }

  public static CouchTopic existing(CouchDataProvider dataProvider, ObjectNode doc) {
    return new CouchTopic(dataProvider, doc);
  }

  public static CouchTopic newInstance(CouchDataProvider dataProvider, PrestoType type) {
    ObjectNode data = dataProvider.getObjectMapper().createObjectNode();
    data.put(":type", type.getId());
    return new CouchTopic(dataProvider, data);
  }
  
  ObjectNode getData() {
    return data;
  }
  
  public PrestoDataProvider getDataProvider() {
    return dataProvider;
  }

  public String getId() {
    return data.get("_id").getTextValue();
  }

  public String getName() {
    JsonNode name = data.get(":name");
    return name == null ? null : name.getTextValue();
  }

  public String getTypeId() {
    return data.get(":type").getTextValue();
  }

  public Collection<Object> getValues(PrestoField field) {
    List<Object> values = new ArrayList<Object>();
    JsonNode fieldNode = data.get(field.getId());
    if (fieldNode != null) {
      for (JsonNode value : fieldNode) {
        values.add(value.toString());
      }
    }
    return values;
  }

}
