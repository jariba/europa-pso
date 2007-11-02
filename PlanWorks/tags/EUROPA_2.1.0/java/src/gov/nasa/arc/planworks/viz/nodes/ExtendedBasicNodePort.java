// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ExtendedBasicNodePort.java,v 1.9 2004-08-05 00:24:25 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 05jan04
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.awt.Rectangle;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoRectangle;

// PlanWorks/java/lib/com/nwoods/jgo/examples/*.class
// source /home/wtaylor/pub/JGo41/com/nwoods/jgo/examples/*.java
import com.nwoods.jgo.examples.Diamond;
import com.nwoods.jgo.examples.LeftTrapezoid;
import com.nwoods.jgo.examples.RightTrapezoid;
import com.nwoods.jgo.examples.Hexagon;
import com.nwoods.jgo.examples.PinchedRectangle;
import com.nwoods.jgo.examples.PinchedHexagon;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;
import com.nwoods.jgo.examples.BasicNodePort;

import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.ConstraintNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.ModelClassNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.ResourceNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.SlotNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.TimelineNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.TokenNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.VariableNavNode;


/**
 * <code>ExtendedBasicNodePort</code> - subclass BasicNodePort to handle
 *                                      ExtendedBasicNode
 *             Object->JGoObject->JGoDrawable->JGoPort->BasicNodePort
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ExtendedBasicNodePort extends BasicNodePort {

  private int nodeType;

  /**
   * <code>ExtendedBasicNodePort</code> - constructor 
   *
   */
  public ExtendedBasicNodePort( int nodeType) {
    super();
    this.nodeType = nodeType;
  }

  /**
   * <code>getLinkPointFromPoint</code>
   *
   * Return a point on the edge of or inside the ellipse, rectangle, or diamond rather
   * than a point on the port itself
   *
   * @param x - <code>int</code> - 
   * @param y - <code>int</code> - 
   * @param p - <code>Point</code> - 
   * @return - <code>Point</code> - 
   */
  public Point getLinkPointFromPoint(int x, int y, Point p) {
    BasicNode node = getNode();
    JGoDrawable obj = node.getDrawable();
    Rectangle rect = obj.getBoundingRect();
    int a = rect.width / 2;
    int b = rect.height / 2;
    int cx = getLeft() + getWidth() / 2;
    int cy = getTop() + getHeight() / 2;

    if (p == null) {
      p = new Point();
    }
    p.x = x;
    p.y = y;
    // if (x,y) is inside the object, just return it instead of finding the edge intersection
    //if (! obj.isPointInObj(p)) {
    if (node.isRectangular() || (node instanceof TokenNavNode) ||
        (node instanceof TokenNode) || (node instanceof TokenNetworkTokenNode)) {
        JGoRectangle.getNearestIntersectionPoint( rect.x, rect.y, rect.width,
                                                  rect.height, x, y, cx, cy, p);
      } else if (node instanceof ConstraintNode) {
        ((Diamond) ((ConstraintNode) node).getDrawable()).
          getNearestIntersectionPoint( x, y, cx, cy, p);
      } else if (node instanceof ConstraintNavNode) {
        ((Diamond) ((ConstraintNavNode) node).getDrawable()).
          getNearestIntersectionPoint( x, y, cx, cy, p);
      } else if (node instanceof RuleInstanceNode) {
        JGoEllipse.getNearestIntersectionPoint( rect.x, rect.y, rect.width,
                                                rect.height, x, y, p);
      } else if (node instanceof VariableNode) {
        ((PinchedRectangle) ((VariableNode) node).getDrawable()).
          getNearestIntersectionPoint(x, y, cx, cy, p);
      } else if (node instanceof VariableNavNode) {
        ((PinchedRectangle) ((VariableNavNode) node).getDrawable()).
          getNearestIntersectionPoint(x, y, cx, cy, p);
      }
      else if (node instanceof ObjectNode) {
        ((LeftTrapezoid) ((ObjectNode) node).getDrawable()).
          getNearestIntersectionPoint(x, y, cx, cy, p);
      }
      else if (node instanceof TimelineNode) {
        ((RightTrapezoid) ((TimelineNode) node).getDrawable()).
          getNearestIntersectionPoint(x, y, cx, cy, p);
      }
      else if (node instanceof SlotNavNode) {
        ((Hexagon) ((SlotNavNode) node).getDrawable()).
          getNearestIntersectionPoint(x, y, cx, cy, p);
      }
      else if (node instanceof ResourceNode) {
        ((PinchedHexagon) ((ResourceNode) node).getDrawable()).
          getNearestIntersectionPoint(x, y, cx, cy, p);
      }
      // }
    return p;
  } // end getLinkPointFromPoint



} // end class ExtendedBasicNodePort
