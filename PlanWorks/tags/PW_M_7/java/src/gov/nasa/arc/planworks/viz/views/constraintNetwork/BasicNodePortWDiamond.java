// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: BasicNodePortWDiamond.java,v 1.1 2003-09-18 20:48:47 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 05aug03
//

package gov.nasa.arc.planworks.viz.views.constraintNetwork;

import java.awt.Point;
import java.awt.Rectangle;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoRectangle;

// PlanWorks/java/lib/com/nwoods/jgo/examples/Diamond.class
import com.nwoods.jgo.examples.Diamond;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;
import com.nwoods.jgo.examples.BasicNodePort;

/**
 * <code>BasicNodePortWDiamond</code> - subclass BasicNodePort to handle Diamond
 *             Object->JGoObject->JGoDrawable->JGoPort->BasicNodePort
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class BasicNodePortWDiamond extends BasicNodePort {


  /**
   * <code>BasicNodePortWDiamond</code> - constructor 
   *
   */
  public BasicNodePortWDiamond() {
    super();
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
    if (! obj.isPointInObj(p)) {
      if (node.isRectangular()) {
        JGoRectangle.getNearestIntersectionPoint( rect.x, rect.y, rect.width,
                                                  rect.height, x, y, cx, cy, p);
      } if (node instanceof ConstraintNode) {
        ((Diamond) ((ConstraintNode) node).getDrawable()).
          getNearestIntersectionPoint( x, y, cx, cy, p);
      } else {
        JGoEllipse.getNearestIntersectionPoint( rect.x, rect.y, rect.width,
                                                rect.height, x, y, p);
      }
    }
    return p;
  } // end getLinkPointFromPoint



} // end class BasicNodePortWDiamond
