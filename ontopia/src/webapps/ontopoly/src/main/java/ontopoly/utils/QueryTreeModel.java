package ontopoly.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.ontopia.topicmaps.nav2.webapps.ontopoly.model.TopicMap;
import net.ontopia.topicmaps.query.core.DeclarationContextIF;
import net.ontopia.topicmaps.query.core.InvalidQueryException;
import net.ontopia.topicmaps.query.core.QueryProcessorIF;
import net.ontopia.topicmaps.query.core.QueryResultIF;
import net.ontopia.topicmaps.query.utils.QueryWrapper;
import net.ontopia.utils.OntopiaRuntimeException;

public abstract class QueryTreeModel extends DefaultTreeModel {
  
  public QueryTreeModel(TopicMap topicmap, String query, Map params) {
    super(new DefaultMutableTreeNode("<root>"));    
    
    QueryWrapper qw = topicmap.getQueryWrapper();
    QueryProcessorIF qp = qw.getQueryProcessor();
    DeclarationContextIF dc = qw.getDeclarationContext();

    DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot();
    Map nodes = new HashMap();
    
    try {
      QueryResultIF qr = qp.execute(query, params, dc);

      // ignore query if it contains less than two columns
      if (qr.getWidth() < 2) return;
      
      try {
        while (qr.next()) {
          Object p = qr.getValue(0);
          Object c = qr.getValue(1);
          if (!filter(p, c)) continue;
          
          DefaultMutableTreeNode parent;
          DefaultMutableTreeNode child;
          
          if (p == null) {
            parent = (DefaultMutableTreeNode)root; // orphans are children of root
          } else {
            parent = (DefaultMutableTreeNode)nodes.get(p);
            if (parent == null) {
              parent = createTreeNode(p);
              nodes.put(p, parent);
            }
          }
          
          if (c == null) {
            continue; // ignore children that are null
          } else {
            child = (DefaultMutableTreeNode)nodes.get(c);
            if (child == null) {
              child = createTreeNode(c);
              nodes.put(c, child);
            }
          }
          parent.add(child);
        }
      } finally {
        qr.close();
      }
      // add rootless nodes to root
      Iterator iter = nodes.values().iterator();
      while (iter.hasNext()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)iter.next();
        if (node.getParent() == null)
          root.add(node);
      }
      
    } catch (InvalidQueryException e) {
      throw new OntopiaRuntimeException(e);
    }
  }
  
  /**
   * Filter method that should return true if objects should be part of result set.p
   * @param p Parent object.
   * @param c Child object.
   * @return
   */
  protected boolean filter(Object p, Object c) {
    return true;
  }

  protected abstract DefaultMutableTreeNode createTreeNode(Object o);
  
}