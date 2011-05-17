package ontopoly.components;

import java.util.Comparator;
import java.util.List;

import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.FieldAssignment;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.FieldInstance;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.RoleField;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.Topic;
import net.ontopia.utils.ObjectUtils;
import ontopoly.models.FieldDefinitionModel;
import ontopoly.models.FieldInstanceModel;
import ontopoly.models.FieldValueModel;
import ontopoly.models.FieldValuesModel;
import ontopoly.models.FieldsViewModel;
import ontopoly.models.RoleFieldModel;
import ontopoly.models.TopicModel;
import ontopoly.pages.AbstractOntopolyPage;
import ontopoly.utils.RoleFieldsValueComparator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class FieldInstanceAssociationNaryPanel extends AbstractFieldInstancePanel {
  
	public FieldInstanceAssociationNaryPanel(String id, 
	    final FieldInstanceModel fieldInstanceModel, final FieldsViewModel fieldsViewModel, 
	    final boolean readonly, final boolean traversable, final int arity) {
		super(id, fieldInstanceModel);

		FieldInstance fieldInstance = fieldInstanceModel.getFieldInstance();
		FieldAssignment fieldAssignment = fieldInstance.getFieldAssignment();
		RoleField roleField = (RoleField)fieldAssignment.getFieldDefinition(); 

		final RoleFieldModel roleFieldModel = new RoleFieldModel(roleField);
	  final List otherRoleFieldModels = RoleFieldModel.wrapInRoleFieldModels(roleField.getFieldsForOtherRoles());
    
    final boolean allowRemove = !roleField.getEditMode().isNoEdit();
	  
	//! add(new Label("fieldLabel", new Model(rolefield.getFieldName() + ":")));
	//! add(new Label("fieldLabel", new Model(rolefield.getAssociationField().getFieldName() + " (" + rolefield.getName() + ")")));
    add(new FieldDefinitionLabel("fieldLabel", new FieldDefinitionModel(roleField)));
//    add(new TopicLink("fieldLabel", roleFieldModel) {
//      @Override
//      public String getLabel() {
//        return roleFieldModel.getRoleField().getFieldName();
//      }
//      @Override
//      public boolean isEnabled() {
//        AbstractOntopolyPage page = (AbstractOntopolyPage)getPage();
//        return page.isAnnotationEnabled() && traversable && super.isEnabled();
//      }
//    });
    
    // set up container
		this.fieldValuesContainer = new WebMarkupContainer("fieldValuesContainer");
		fieldValuesContainer.setOutputMarkupId(true);    
    add(fieldValuesContainer);

		// add feedback panel
    this.feedbackPanel = new FeedbackPanel("feedback", new AbstractFieldInstancePanelFeedbackMessageFilter());
    feedbackPanel.setOutputMarkupId(true);
    fieldValuesContainer.add(feedbackPanel);

    // add field values component(s)
    Comparator comparator = new RoleFieldsValueComparator(new TopicModel(fieldInstance.getInstance()), otherRoleFieldModels);
    this.fieldValuesModel = new FieldValuesModel(fieldInstanceModel, comparator);
    
    this.listView = new ListView("fieldValues", fieldValuesModel) {
      @Override
      protected void onBeforeRender() {
        validateCardinality();        
        super.onBeforeRender();
      }
		  public void populateItem(final ListItem item) {
		    FieldValueModel fieldValueModel = (FieldValueModel)item.getModelObject();

        // TODO: make sure non-existing value field gets focus if last edit happened there
        
        final WebMarkupContainer fieldValueButtons = new WebMarkupContainer("fieldValueButtons");
        fieldValueButtons.setOutputMarkupId(true);
        item.add(fieldValueButtons);
        
        FieldInstanceRemoveButton removeButton = 
          new FieldInstanceRemoveButton("remove", "remove-value.gif", fieldValueModel) { 
            @Override
            public boolean isVisible() {
              boolean visible = !readonly && fieldValueModel.isExistingValue() && allowRemove;
              if (visible) {
                
                // filter by player
                AbstractOntopolyPage page = (AbstractOntopolyPage)getPage();
                RoleField.ValueIF value = (RoleField.ValueIF)fieldValueModel.getObject();
                Topic[] players = value.getPlayers();
                for (int i=0; i < players.length; i++) {
                  if (!page.filterTopic(players[i])) return false;
                }
//                // show remove button on 1:1 field unless just one value left
//                FieldInstance fi = fieldValueModel.getFieldInstanceModel().getFieldInstance();
//                Cardinality cardinality = fi.getFieldAssignment().getCardinality();
//                if (cardinality.isMinOne() && cardinality.isMaxOne() && 
//                    ((fieldValuesModel.size() == 1 && !fieldValuesModel.getShowExtraField()) ||
//                      (fieldValuesModel.size() == 2 && fieldValuesModel.getShowExtraField())))
//                  return false;                
              }
              return visible;
            }
            @Override
            public void onClick(AjaxRequestTarget target) {
              FieldInstance fieldInstance = fieldValueModel.getFieldInstanceModel().getFieldInstance();
              Object value = fieldValueModel.getObject();

              Topic currentTopic = fieldInstance.getInstance();
              RoleField currentField = (RoleField)fieldInstance.getFieldAssignment().getFieldDefinition();          

              RoleField.ValueIF valueIf = (RoleField.ValueIF)value;
              RoleField[] fields = valueIf.getRoleFields();
              // check with page to see if add is allowed
              boolean removeAllowed = true;
              AbstractOntopolyPage page = (AbstractOntopolyPage)getPage();
              for (int i=0;  i < fields.length; i++) {
                RoleField selectedField = fields[i];
                if (ObjectUtils.different(currentField, selectedField)) {
                  Topic selectedTopic = valueIf.getPlayer(selectedField, fieldInstance.getInstance());                
                  if (!page.isRemoveAllowed(currentTopic, currentField, selectedTopic, selectedField)) {
                    removeAllowed = false;
                  }
                }
              }
              // perform removal
              if (removeAllowed) {
                fieldInstance.removeValue(value, page.getListener());              
                listView.removeAll();
                updateDependentComponents(target);
              }
            }
          };
        fieldValueButtons.add(removeButton);
        
        // n-ary
        FieldInstanceAssociationNaryField naryField = new FieldInstanceAssociationNaryField("fieldValue", FieldInstanceAssociationNaryPanel.this, 
            roleFieldModel, otherRoleFieldModels, fieldValueModel, fieldsViewModel, readonly, traversable, arity);
        item.add(naryField);                    
	    }
		};
	  listView.setReuseItems(true);	  
	  fieldValuesContainer.add(listView);

	  // figure out which buttons to show
    this.fieldInstanceButtons = new WebMarkupContainer("fieldInstanceButtons");
    fieldInstanceButtons.setOutputMarkupId(true);
    add(fieldInstanceButtons);	  

	  // "add" button
	  OntopolyImageLink addButton = new OntopolyImageLink("add", "add.gif") { 
      @Override
      public void onClick(AjaxRequestTarget target) {
        boolean showExtraField = !fieldValuesModel.getShowExtraField();
        fieldValuesModel.setShowExtraField(showExtraField);
        listView.removeAll();
        updateDependentComponents(target);
      }
      @Override
      public boolean isVisible() {
        if (readonly) return false;
//        Cardinality cardinality = fieldValuesModel.getFieldInstanceModel().getFieldInstance().getFieldAssignment().getCardinality();
//        return !(cardinality.isMaxOne() && fieldValuesModel.getNumberOfValues() > 1);
        return fieldValuesModel.containsExisting();
      }
      @Override public String getImage() {
        return fieldValuesModel.getShowExtraField() ? "remove.gif" : "add.gif";
      }
      @Override public IModel getTitleModel() {
        return new ResourceModel(fieldValuesModel.getShowExtraField() ? "icon.remove.hide-field" : "icon.add.add-value");
      }
    };
    fieldInstanceButtons.add(addButton);    
	}
 
}