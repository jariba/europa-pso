// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: BasicNodeWDiamond.java,v 1.1 2003-12-13 00:40:55 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 08dec03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.awt.Rectangle;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoRectangle;

// PlanWorks/java/lib/com/nwoods/jgo/examples/Diamond.class
import com.nwoods.jgo.examples.Diamond;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;
import com.nwoods.jgo.examples.BasicNodePort;

import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.sequence.modelRules.ParamNode;


/**
 * <code>BasicNodeWDiamond</code> - subclass BasicNode to handle Diamond
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class BasicNodeWDiamond extends BasicNode {


  private boolean isDiamond;

  /**
   * <code>BasicNodeWDiamond</code> - constructor 
   *
   */
  public BasicNodeWDiamond() {
    super();
    isDiamond = true;
  }

   /**
   * <code>initialize</code> - modified from BasicNode to handle Diamond node shape
   *
   * @param loc - <code>Point</code> - 
   * @param labeltext - <code>String</code> - 
   */
  public void initialize(Point loc, String labeltext) {
    // the area as a whole is not directly selectable using a mouse,
    // but the area can be selected by trying to select any of its
    // children, all of whom are currently !isSelectable().
    setSelectable(false);
    setGrabChildSelection(true);
    // the user can move this node around
    setDraggable(true);
    // the user cannot resize this node
    setResizable(false);

    // create the circle/ellipse around and behind the port
    myDrawable = createDrawable();
    // can't setLocation until myDrawable exists
    setLocation(loc);

    // if there is a string, create a label with a transparent
    // background that is centered
    if (labeltext != null) {
      myLabel = createLabel(labeltext);
    }

    // create a Port, which knows how to make sure
    // connected JGoLinks have a reasonable end point
    // myPort = new BasicNodePort();
    myPort = new BasicNodePortWDiamond();
    myPort.setSize(7, 7);
    if (getLabelSpot() == Center) {
      getPort().setStyle(JGoPort.StyleHidden);
    } else {
      getPort().setStyle(JGoPort.StyleEllipse);
    }

    // add all the children to the area
    addObjectAtHead(myDrawable);
    addObjectAtTail(myPort);
    if (myLabel != null) {
      addObjectAtTail(myLabel);
    }
  }


  /**
   * <code>createDrawable</code> - modified from BasicNode to handle Diamond node shape
   *
   * @return - <code>JGoDrawable</code> - 
   */
  public JGoDrawable createDrawable() {
    JGoDrawable d;
    if (isRectangular()) {
      d = new JGoRectangle();
    } else if (isDiamond) {
      d = new Diamond();
    } else {
      d = new JGoEllipse();
    }
    d.setSelectable(false);
    d.setDraggable(false);
    d.setSize(20, 20);
    return d;
  } // end createDrawable



} // end class BasicNodeWDiamond
