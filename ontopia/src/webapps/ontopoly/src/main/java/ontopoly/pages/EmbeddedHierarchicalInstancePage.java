package ontopoly.pages;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import net.ontopia.topicmaps.core.OccurrenceIF;
import net.ontopia.topicmaps.core.TopicIF;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.PSI;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.Topic;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicType;
import net.ontopia.topicmaps.nav2.webapps.ontopoly.utils.OntopolyModelUtils;
import net.ontopia.utils.ObjectUtils;
import ontopoly.components.LinkPanel;
import ontopoly.components.TreePanel;
import ontopoly.models.TopicModel;
import ontopoly.pojos.TopicNode;
import ontopoly.utils.OntopolyUtils;
import ontopoly.utils.TreeModels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.Model;

public class EmbeddedHierarchicalInstancePage extends EmbeddedInstancePage {
  
  private TopicModel hierarchyModel;
  
  public EmbeddedHierarchicalInstancePage(PageParameters parameters) {
    // expect there to be a topicId parameter
    super(parameters);
    
    // find hierarchy topic
    String hierarchyId = parameters.getString("hierarchyId");
    if (hierarchyId == null)
      this.hierarchyModel = new TopicModel(getHierarchyTopic(getTopic()));      
    else
      this.hierarchyModel = new TopicModel(parameters.getString("topicMapId"), hierarchyId);
    
    // create a tree
    TreePanel treePanel = createTreePanel("treePanel", createTreeModel(getHierarchyTopic(), getTopic()));
    treePanel.setOutputMarkupId(true);
    add(treePanel); 
  }
  
  @Override
  protected boolean isTraversable() {
    return true;
  }
  
  protected Topic getHierarchyTopic() {
    return hierarchyModel.getTopic();
  }

  protected Topic getHierarchyTopic(Topic topic) {
    // find hierarchy definition query for topic
    String query = getDefinitionQuery(topic);
    if (query != null) return topic;
    
    // find hierarchy definition query for topic's topic types
    Iterator titer = topic.getTopicTypes().iterator();
    while (titer.hasNext()) {
      TopicType topicType = (TopicType)titer.next();
      if (getDefinitionQuery(topicType) != null) 
        return topicType;
    }
    return null;
  }

  protected String getDefinitionQuery(Topic topic) {
    TopicIF typeIf = OntopolyModelUtils.getTopicIF(topic.getTopicMap(), PSI.ON, "hierarchy-definition-query");
    if (typeIf == null) return null;
    OccurrenceIF occ = OntopolyModelUtils.findOccurrence(typeIf, topic.getTopicIF());
    return (occ == null ? null : occ.getValue());    
  }
  
  protected TreeModel createTreeModel(Topic hierarchyTopic, Topic currentNode) {

    // find hierarchy definition query for topic
    String query = (hierarchyTopic == null ? null : getDefinitionQuery(hierarchyTopic));

    if (query != null) {
      Map params = new HashMap(2);
      params.put("hierarchyTopic", hierarchyTopic.getTopicIF());
      params.put("currentNode", currentNode.getTopicIF());
      return TreeModels.createQueryTreeModel(currentNode.getTopicMap(), query, params);
    } else if (currentNode.isTopicType()) {
      // if no definition query found, then show topic in instance hierarchy
      return TreeModels.createTopicTypesTreeModel(currentNode.getTopicMap(), isAnnotationEnabled(), isAdministrationEnabled());
    } else {
      return TreeModels.createInstancesTreeModel(OntopolyUtils.getDefaultTopicType(currentNode), isAdministrationEnabled());
      // return new DefaultTreeModel(new DefaultMutableTreeNode("<root>"));      
    }    
  }
  
  protected TreePanel createTreePanel(final String id, TreeModel treeModel) {
    return new TreePanel(id, treeModel) {
      @Override
      protected boolean isMenuEnabled() {
        return true;
      }
      @Override
      protected void initializeTree(AbstractTree tree) {
        // expand current node
        TreeModel treeModel =  (TreeModel)tree.getModelObject();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
        DefaultMutableTreeNode treeNode = findTopicNode(root, getTopic());
        if (treeNode != null)
          expandNode(tree, treeNode);
      }

      protected DefaultMutableTreeNode findTopicNode(DefaultMutableTreeNode parent, Topic topic) {
        Enumeration e = parent.children();
        while (e.hasMoreElements()) {
          DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
          Topic nodeTopic = ((TopicNode)child.getUserObject()).getTopic();
          if (ObjectUtils.equals(nodeTopic, topic))
            return child;
          DefaultMutableTreeNode found = findTopicNode(child, topic);
          if (found != null)
            return found;
        }
        return null;
      }
      
      @Override
      protected void populateNode(WebMarkupContainer container, String id, TreeNode treeNode, int level) {
        DefaultMutableTreeNode mTreeNode = (DefaultMutableTreeNode)treeNode; 
        final TopicNode node = (TopicNode)mTreeNode.getUserObject();
        Topic topic = node.getTopic();
        final boolean isCurrentTopic = ObjectUtils.equals(topic, getTopic());
        // create link with label
        container.add(new LinkPanel(id) {
          @Override
          protected Label newLabel(String id) {
            return new Label(id, new Model(node.getTopic().getName())) {
              @Override
              protected void onComponentTag(final ComponentTag tag) {
                if (isCurrentTopic)
                  tag.put("class", "emphasis");
                super.onComponentTag(tag);              
              }
            };
          }
          @Override
          protected Link newLink(String id) {
            Topic topic = node.getTopic();
            return new BookmarkablePageLink(id, getPageClass(topic), getPageParameters(topic));
          }
        });
      }
    };
  }
  
  @Override  
  public PageParameters getPageParameters(Topic topic) {
    // add hierarchyId to parent parameters
    PageParameters params = super.getPageParameters(topic);            
    Topic hierarchyTopic = getHierarchyTopic();
    if (hierarchyTopic != null)
      params.put("hierarchyId", hierarchyTopic.getId());
    return params;
  }
  
}