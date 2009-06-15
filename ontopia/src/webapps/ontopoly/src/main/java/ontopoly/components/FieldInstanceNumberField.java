package ontopoly.components;

import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.FieldInstance;
import net.ontopia.utils.ObjectUtils;
import ontopoly.models.FieldValueModel;
import ontopoly.pages.AbstractOntopolyPage;
import ontopoly.validators.ExternalValidation;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.PatternValidator;


public class FieldInstanceNumberField extends TextField {

  private FieldValueModel fieldValueModel;
  private String oldValue;
  private String cols = "20";
  
  public FieldInstanceNumberField(String id, FieldValueModel fieldValueModel) {
    super(id);
    this.fieldValueModel = fieldValueModel;
    
    OccurrenceIF occ = (OccurrenceIF)fieldValueModel.getObject();
    this.oldValue = (occ == null ? null : occ.getValue());
    setModel(new Model(oldValue));
    
        
    //add(new PatternValidator("-?\\d+\\.?\\d*"));
    add(new PatternValidator("^[-+]?\\d+(\\.\\d+)?$"));

    // validate field using registered validators
    ExternalValidation.validate(this, oldValue);
  }

  public void setCols(int cols) {
    this.cols = Integer.toString(cols);
  }
  
  @Override
  protected void onComponentTag(ComponentTag tag) {
    tag.setName("input");
    tag.put("type", "text");
    tag.put("size", cols);
    super.onComponentTag(tag);
  }
  
  @Override
  protected void onModelChanged() {
    super.onModelChanged();
    String newValue = (String)getModelObject();
    if (ObjectUtils.equals(newValue, oldValue)) return;
    AbstractOntopolyPage page = (AbstractOntopolyPage)getPage();
    FieldInstance fieldInstance = fieldValueModel.getFieldInstanceModel().getFieldInstance();
    if (fieldValueModel.isExistingValue() && oldValue != null)
      fieldInstance.removeValue(oldValue, page.getListener());
    if (newValue != null && !newValue.equals("")) {
      fieldInstance.addValue(newValue, page.getListener());
      fieldValueModel.setExistingValue(newValue);
    }
    oldValue = newValue;
  }
  
}
