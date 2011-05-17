package ontopoly.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.FieldAssignment;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.FieldDefinition;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.IdentityField;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.NameField;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.OccurrenceField;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.RoleField;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicMap;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicType;
import net.ontopia.utils.ObjectUtils;
import ontopoly.utils.OntopolyContext;

import org.apache.wicket.model.LoadableDetachableModel;

public class FieldAssignmentModel extends LoadableDetachableModel {

  private String topicMapId;
  
  private String topicTypeId;
  private String declaredTopicTypeId;
  
  private int fieldType;
  private String fieldId;
  
  public FieldAssignmentModel(FieldAssignment fieldAssignment) {
    super(fieldAssignment);
    if (fieldAssignment == null)
      throw new NullPointerException("fieldAssignment parameter cannot be null.");
       
    TopicType topicType = fieldAssignment.getTopicType();
    this.topicTypeId = topicType.getId();

    TopicType declaredTopicType = fieldAssignment.getDeclaredTopicType();
    this.declaredTopicTypeId = declaredTopicType.getId();
    
    TopicMap topicMap = topicType.getTopicMap();
    this.topicMapId = topicMap.getId();
    
    FieldDefinition fieldDefinition = fieldAssignment.getFieldDefinition();    
    this.fieldType = fieldDefinition.getFieldType();
      
    this.fieldId = fieldDefinition.getId();
  }
  
  public FieldAssignment getFieldAssignment() {
    return (FieldAssignment)getObject();
  }

  @Override
  protected Object load() {
    TopicMap tm = OntopolyContext.getTopicMap(topicMapId);

    TopicIF topicTypeIf = tm.getTopicIFById(topicTypeId);
    TopicType topicType = new TopicType(topicTypeIf, tm);

    TopicIF declaredTopicTypeIf = tm.getTopicIFById(declaredTopicTypeId);
    TopicType declaredTopicType = new TopicType(declaredTopicTypeIf, tm);
    
    TopicIF fieldTopic = tm.getTopicIFById(fieldId);
      
    FieldDefinition fieldDefinition;
    switch (fieldType) {
    case FieldDefinition.FIELD_TYPE_ROLE:
      fieldDefinition = new RoleField(fieldTopic, tm);
      break;
    case FieldDefinition.FIELD_TYPE_OCCURRENCE:
      fieldDefinition = new OccurrenceField(fieldTopic, tm);
      break;
    case FieldDefinition.FIELD_TYPE_NAME:
      fieldDefinition = new NameField(fieldTopic, tm);
      break;
    case FieldDefinition.FIELD_TYPE_IDENTITY:
      fieldDefinition = new IdentityField(fieldTopic, tm);
      break;
    default:
      throw new RuntimeException("Unknown field type: " + fieldType);
    }
    return new FieldAssignment(topicType, declaredTopicType, fieldDefinition);
  }

  public static List wrapInFieldAssignmentModels(List fieldAssignments) {
    List result = new ArrayList(fieldAssignments.size());
    Iterator iter = fieldAssignments.iterator();
    while (iter.hasNext()) {
      FieldAssignment fieldAssignment = (FieldAssignment)iter.next();
      result.add(new FieldAssignmentModel(fieldAssignment));
    }
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof FieldAssignmentModel))
      return false;
    
    FieldAssignmentModel fam = (FieldAssignmentModel)other;
    return ObjectUtils.equals(getFieldAssignment(), fam.getFieldAssignment());
  }
  @Override
  public int hashCode() {
    return getFieldAssignment().hashCode();
  }

}