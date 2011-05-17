package ontopoly.pages;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.ontopia.infoset.core.LocatorIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicMap;
import net.ontopia.topicmaps.xml.XTMFragmentExporter;
import ontopoly.components.FunctionBoxesPanel;
import ontopoly.components.TitleHelpPanel;
import ontopoly.models.TopicModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

public class VizigatorPage extends OntopolyAbstractPage {

  protected TopicModel topicModel;
  
  public VizigatorPage() {	  
  }
  
  public VizigatorPage(PageParameters parameters) {
    super(parameters);
  
    this.topicModel = new TopicModel(parameters.getString("topicMapId"), parameters.getString("topicId"));
    
    // Adding part containing title and help link
    createTitle();

    // Add fields panel
    createApplet();
    
    // Function boxes
    createFunctionBoxes(); 
    
    // initialize parent components
    initParentComponents();    
  }

  private void createApplet() {
    TopicIF topic = topicModel.getTopic().getTopicIF();

    String idtype = "source";
    String idvalue = null;
    Iterator it = topic.getItemIdentifiers().iterator();
    if (it.hasNext())
      idvalue = ((LocatorIF) it.next()).getExternalForm();

    if (idvalue == null) {
      it = topic.getSubjectIdentifiers().iterator();
      if (it.hasNext()) {
        idvalue = ((LocatorIF) it.next()).getExternalForm();
        idtype = "indicator";
      }
    }

    if (idvalue == null) {
      it = topic.getSubjectLocators().iterator();
      if (it.hasNext()) {
        idvalue = ((LocatorIF) it.next()).getExternalForm();
        idtype = "subject";
      }
    }

    if (idvalue == null) {
      TopicMap topicMap = this.getTopicMapModel().getTopicMap();
      idvalue = XTMFragmentExporter.makeVirtualReference(topic, topicMap.getId());
      idtype = "source";
    }
    final String _idtype = idtype;
    final String _idvalue = idvalue;
    add(new AppletParamLabel("tmid") {
      @Override
      public String getValue() {
        return getTopicMapModel().getTopicMap().getId();
      }      
    });
    add(new AppletParamLabel("idtype") {
      @Override
      public String getValue() {
        return _idtype;
      }      
    });
    add(new AppletParamLabel("idvalue") {
      @Override
      public String getValue() {
        return _idvalue;
      }      
    });
    // TODO: implement support for "config" parameter
  }
  
  private static abstract class AppletParamLabel extends Label {
    AppletParamLabel(String id) {
      super(id);
    }
    public String getName() {
      return getId();
    }
    public abstract String getValue();
    
    @Override
    protected void onComponentTag(ComponentTag tag) {
      tag.setName("param");
      tag.put("name", getName());
      tag.put("value", getValue());
      super.onComponentTag(tag);
    }
  }

  @Override
  protected int getMainMenuIndex() {
    return INSTANCES_PAGE_INDEX_IN_MAINMENU; 
  }
  
  private void createTitle() {
    // Adding part containing title and help link
    TitleHelpPanel titlePartPanel = new TitleHelpPanel("titlePartPanel",
        new PropertyModel(topicModel, "name"), new ResourceModel("help.link.instancepage"));
    titlePartPanel.setOutputMarkupId(true);
    add(titlePartPanel);    
  }

  private void createFunctionBoxes() {

    add(new FunctionBoxesPanel("functionBoxes") {

      @Override
      protected List getFunctionBoxesList(String id) {
        return Collections.EMPTY_LIST;
      }

    });
  }

  @Override
  public void onDetach() {
    topicModel.detach();
    super.onDetach();
  }
  
}