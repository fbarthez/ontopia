
// $Id: TMRoleEdge.java,v 1.20 2007/05/02 15:07:39 eirik.opland Exp $

package net.ontopia.topicmaps.viz;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import net.ontopia.topicmaps.core.AssociationIF;
import net.ontopia.topicmaps.core.AssociationRoleIF;
import net.ontopia.topicmaps.core.TopicIF;

import com.touchgraph.graphlayout.Node;

/**
 * INTERNAL: Edge class representing association roles as edges in
 * n-ary associations connecting role player with the association
 * node.
 */
public class TMRoleEdge extends TMAbstractEdge {
  private AssociationRoleIF role;

  public TMRoleEdge(TMAssociationNode an, TMTopicNode tn, AssociationRoleIF r,
      TopicIF aScopingTopic) {
    super(an, tn);
    this.role = r;

    //Just for role lengths, set the length to a quarter of the default.
    //This makes the resulting view more managable
    this.setLength(this.getLength() / 4);
    this.setScopingTopic(aScopingTopic);
    this.setID(r.getObjectId());
  }
  
  public AssociationRoleIF getRole() {
    return role;
  }

  protected void paintToolTip(Graphics g) {
    if (!shouldDisplayRoleHoverHelp) return;

    Point p = this.getMidPoint();

    this.paintToolTipText(g, this.getMainHoverHelpText(),
        (int) p.getX(), (int) p.getY());
  }

  protected void paintBowTie(Graphics2D g) {
    double x1 = from.drawx;
    double x2 = to.drawx;
    double y1 = from.drawy;
    double y2 = to.drawy;

    Dimension offset = calculateOffset(x1, x2, y1, y2, getLineWeight());

    g.setColor(this.getColor());

    int xPoints[] = new int[3];
    xPoints[0] = (int) x1;
    int yPoints[] = new int[3];
    yPoints[0] = (int) y1;

    xPoints[1] = (int) (x2 - offset.width);
    yPoints[1] = (int) (y2 + offset.height);
    xPoints[2] = (int) (x2 + offset.width);
    yPoints[2] = (int) (y2 - offset.height);

    g.fillPolygon(xPoints, yPoints, 3);

    g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
  }

  public TopicIF getTopicMapType() {
    // For display purposes we show the associated association type

    return role.getAssociation().getType();
  }

  public AssociationIF getAssociation() {
    return role.getAssociation();
  }

  public void setIcon(Icon icon) {
    // Icons and not currently supported (read not wanted) on roles !
  }
  
  public boolean represents(Object object) {
    return this.role.equals(object);
  }

  public List getTargetsFrom(Node find) {
    return Collections.singletonList(getOtherEndpt(find));
  }
  
  protected String getMainHoverHelpText() {
    return this.getStringifier().toString(role.getType());
  }
  
  protected GeneralPath getCurvedBowTie(int index) {
    double x1 = from.drawx;
    double x2 = to.drawx;
    double y1 = from.drawy;
    double y2 = to.drawy;
    double midx = this.calculateMidPointBetween(x1, x2);
    double midy = this.calculateMidPointBetween(y1, y2);
    int weight = index / 2;
    if (index % 2 == 1) {
      weight++;
      weight = -weight;
    }
    Dimension offset = calculateOffset(x1, x2, y1, y2, LOADING * weight);
    Dimension toExtra = calculateOffset(x2, (int)midx - offset.width, y2, (int)midy + offset.height, getLineWeight());

    GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
    path.moveTo((int)x1, (int)y1);
    path.quadTo((float)midx-offset.width, (float)midy+offset.height, (float)x2-toExtra.width, (float)y2+toExtra.height);
    path.lineTo((int)x2+toExtra.width, (int)y2-toExtra.height);
    path.quadTo((float)midx-offset.width, (float)midy+offset.height, (float)x1, (float)y1);
    
    return path;
  }

  public RecoveryObjectIF getDesctructor() {
    return new DeleteTMRoleEdge(getRole());
  }

  public RecoveryObjectIF getRecreator() {
    return new CreateTMRoleEdge(role);
  }
}