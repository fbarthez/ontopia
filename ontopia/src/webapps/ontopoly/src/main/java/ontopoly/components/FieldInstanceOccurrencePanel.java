package ontopoly.components;

import net.ontopia.topicmaps.core.OccurrenceIF;
import ontopoly.model.Cardinality;
import ontopoly.model.DataType;
import ontopoly.model.FieldAssignment;
import ontopoly.model.FieldDefinition;
import ontopoly.model.FieldInstance;
import ontopoly.model.OccurrenceField;
import ontopoly.models.FieldDefinitionModel;
import ontopoly.models.FieldInstanceModel;
import ontopoly.models.FieldValueModel;
import ontopoly.models.FieldValuesModel;
import ontopoly.utils.OccurrenceComparator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;

public class FieldInstanceOccurrencePanel extends AbstractFieldInstancePanel {
  
  public FieldInstanceOccurrencePanel(String id, final FieldInstanceModel fieldInstanceModel, 
                                      final boolean readonly) {
    super(id, fieldInstanceModel);

    FieldInstance fieldInstance = fieldInstanceModel.getFieldInstance();
    FieldAssignment fieldAssignment = fieldInstance.getFieldAssignment();
    FieldDefinition fieldDefinition = fieldAssignment.getFieldDefinition(); 

    //! add(new Label("fieldLabel", new Model(fieldDefinition.getFieldName())));
    add(new FieldDefinitionLabel("fieldLabel", new FieldDefinitionModel(fieldDefinition)));
    
    // set up container
    this.fieldValuesContainer = new WebMarkupContainer("fieldValuesContainer");
    fieldValuesContainer.setOutputMarkupId(true);    
    add(fieldValuesContainer);

    // add feedback panel
    this.feedbackPanel = new FeedbackPanel("feedback", new AbstractFieldInstancePanelFeedbackMessageFilter());
    feedbackPanel.setOutputMarkupId(true);
    fieldValuesContainer.add(feedbackPanel);

    // add field values component(s)
    this.fieldValuesModel = new FieldValuesModel(fieldInstanceModel, OccurrenceComparator.INSTANCE);
    
    this.listView = new ListView<FieldValueModel>("fieldValues", fieldValuesModel) {
      @Override
      protected void onBeforeRender() {
        validateCardinality();        
        super.onBeforeRender();
      }
      @Override
      public void populateItem(final ListItem<FieldValueModel> item) {
        final FieldValueModel fieldValueModel = item.getModelObject();
        FieldInstanceModel fieldInstanceModel = fieldValueModel.getFieldInstanceModel();
        FieldInstance fieldInstance = fieldInstanceModel.getFieldInstance();

        // TODO: make sure non-existing value field gets focus if last edit happened there

        WebMarkupContainer fieldValueButtons = new WebMarkupContainer("fieldValueButtons");
        fieldValueButtons.setOutputMarkupId(true);
        item.add(fieldValueButtons);

        // remove button
        FieldInstanceRemoveButton removeButton = 
        new FieldInstanceRemoveButton("remove", "remove-value.gif", fieldValueModel) { 
          @Override
          public boolean isVisible() {
            Cardinality cardinality = fieldValuesModel.getFieldInstanceModel().getFieldInstance().getFieldAssignment().getCardinality();
            if (fieldValuesModel.size() == 1 && cardinality.isMinOne())
              return false;
            else
              return !readonly && fieldValueModel.isExistingValue();
          }
          @Override
          public void onClick(AjaxRequestTarget target) {
            super.onClick(target);
            listView.removeAll();
            updateDependentComponents(target);
          }
        };
        fieldValueButtons.add(removeButton);  
        
        // we know what kind of field this is, so we can just cast to it directly
        OccurrenceField of = (OccurrenceField)fieldInstance.getFieldAssignment().getFieldDefinition();
        DataType dataType = of.getDataType();

        if (readonly) {
          if (dataType.isImage()) {
            item.add(new FieldInstanceImageField("fieldValue", fieldValueModel, readonly));
          } else {
            item.add(new Label("fieldValue", new LoadableDetachableModel() {
              @Override
              protected Object load() {
                OccurrenceIF occ = (OccurrenceIF)fieldValueModel.getObject();
                return (occ == null ? null : occ.getValue());              
              }
              
            }));          
          }
        } else {
          final FieldUpdatingBehaviour fuBehaviour = new FieldUpdatingBehaviour(true);
          if (dataType.isDate()) {
            FieldInstanceDateField occField = new FieldInstanceDateField("fieldValue", fieldValueModel);
            occField.add(fuBehaviour);
            item.add(occField);

          } else if (dataType.isDateTime()) {
            FieldInstanceDateTimeField occField = new FieldInstanceDateTimeField("fieldValue", fieldValueModel);
            occField.add(fuBehaviour);
            item.add(occField);
            
          } else if (dataType.isURI()) {
            FieldInstanceURIField occField = new FieldInstanceURIField("fieldValue", fieldValueModel);
            occField.setOutputMarkupId(true);
            occField.getTextField().add(fuBehaviour);
            item.add(occField);
            
          } else if (dataType.isNumber()) {
            FieldInstanceNumberField occField = new FieldInstanceNumberField("fieldValue", fieldValueModel);
            occField.add(fuBehaviour);
            item.add(occField);
  
          } else if (dataType.isHTML()) {
            FieldInstanceHTMLArea occField = new FieldInstanceHTMLArea("fieldValue", fieldValueModel);
            occField.getTextArea().add(fuBehaviour);
            item.add(occField);
  
          } else if (dataType.isImage()) {
            FieldInstanceImageField occField = new FieldInstanceImageField("fieldValue", fieldValueModel, readonly) {
              @Override
              public void callOnUpdate(AjaxRequestTarget target) {
                fuBehaviour.onUpdate(target);
              }              
            };
            item.add(occField);

          } else {
            int height = of.getHeight(); 
            if (height > 1) {
              FieldInstanceTextArea occField = new FieldInstanceTextArea("fieldValue", fieldValueModel);
              occField.setCols(of.getWidth());
              occField.setRows(height);
              occField.add(fuBehaviour);
              item.add(occField);

            } else {
              FieldInstanceTextField occField = new FieldInstanceTextField("fieldValue", fieldValueModel);
              occField.setCols(of.getWidth());
              occField.add(fuBehaviour);
              item.add(occField);           
            }
          }
        }
        
        addNewFieldValueCssClass(item, fieldValuesModel, fieldValueModel);
      }
    };
    listView.setReuseItems(true);
    fieldValuesContainer.add(listView);
    
    this.fieldInstanceButtons = new WebMarkupContainer("fieldInstanceButtons");
    fieldInstanceButtons.setOutputMarkupId(true);
    add(fieldInstanceButtons);
    
    OntopolyImageLink addButton = new OntopolyImageLink("add", "add.gif") { 
      @Override
      public void onClick(AjaxRequestTarget target) {
        boolean showExtraField = !fieldValuesModel.getShowExtraField();
        fieldValuesModel.setShowExtraField(showExtraField, true);
        listView.removeAll();
        updateDependentComponents(target);
      }      
      @Override
      public boolean isVisible() {
        if (readonly) return false;
        Cardinality cardinality = fieldValuesModel.getFieldInstanceModel().getFieldInstance().getFieldAssignment().getCardinality();
        return !cardinality.isMaxOne() && fieldValuesModel.containsExisting();
      }      
      @Override public String getImage() {
        return fieldValuesModel.getShowExtraField() ? "remove.gif" : "add.gif";
      }
      @Override public IModel getTitleModel() {
        return new ResourceModel(fieldValuesModel.getShowExtraField() ? "icon.remove.hide-field" : "icon.add.add-value");
      }
    };
    fieldInstanceButtons.add(addButton);
    
    Cardinality cardinality = fieldAssignment.getCardinality();
    if (cardinality.isMaxOne())
      addButton.setVisible(false);
  }

}
