package ontopoly.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ontopia.topicmaps.core.TopicMapStoreIF;
import ontopoly.model.TopicMap;
import ontopoly.models.TopicMapModel;
import ontopoly.pages.SearchPage;
import ontopoly.pages.StartPage;
import ontopoly.pojos.MenuItem;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

public class TopicMapHeaderPanel extends HeaderPanel {
  
  public TopicMapHeaderPanel(String id, TopicMapModel model, List<MenuItem> tabMenuItem, int selectedTab) {
    super(id);
    
    final TopicMapModel topicMapModel = model;

    add(new MenuPanel("tabMenu", tabMenuItem, selectedTab));

    add(new Label("topicMap", new PropertyModel<Object>(topicMapModel, "name")));

    final TextField<String> searchField = new TextField<String>("searchField", new Model<String>(""));
    
    Form<Object> form = new Form<Object>("searchForm") {
      @Override
      protected void onSubmit() {
        Map<String,String> pageParametersMap = new HashMap<String,String>();
        pageParametersMap.put("topicMapId", topicMapModel.getTopicMap().getId());
        pageParametersMap.put("searchTerm", searchField.getModel().getObject());
        setResponsePage(SearchPage.class, new PageParameters(pageParametersMap));
        setRedirect(true);
      }
    };
    add(form);
    form.add(searchField);
    
    WebMarkupContainer openContainer = new WebMarkupContainer("openContainer");
    AjaxFallbackLink<Object> openLink = new AjaxFallbackLink<Object>("open") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        setResponsePage(StartPage.class);
        setRedirect(true);
      }      
    };
    openLink.add(new Label("label", new ResourceModel("open")));
    openContainer.add(openLink);
    form.add(openContainer);
    
    WebMarkupContainer saveContainer = new WebMarkupContainer("saveContainer") {
        @Override
        public boolean isVisible() {
          return (topicMapModel.getTopicMap().getTopicMapIF().getStore().getImplementation() == TopicMapStoreIF.IN_MEMORY_IMPLEMENTATION);
        }
    };
    AjaxFallbackLink<Object> saveLink = new AjaxFallbackLink<Object>("save") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        ((TopicMap)topicMapModel.getObject()).save();
      }
    };
    saveLink.add(new Label("label", new ResourceModel("save")));    
    saveContainer.add(saveLink);
    form.add(saveContainer);    
    
    form.add(new Button("searchButton", new ResourceModel("button.find")));
  }
}