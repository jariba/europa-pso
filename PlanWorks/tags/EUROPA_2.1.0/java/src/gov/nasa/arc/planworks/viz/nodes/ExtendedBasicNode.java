// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ExtendedBasicNode.java,v 1.4 2004-06-10 01:36:00 taylor Exp $
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
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoRectangle;

// PlanWorks/java/lib/JGo/com/nwoods/jgo/examples/*.class
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
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;


/**
 * <code>ExtendedBasicNode</code> - subclass BasicNode to handle Diamond,
 *                                  LeftTrapezoid, RightTrapezoid, Hexagon,
 *                                  PinchedRectangle, PinchedHexagon
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ExtendedBasicNode extends BasicNode {


  private int nodeType;
  private boolean areNeighborsShown;

  /**
   * <code>ExtendedBasicNode</code> - constructor 
   *
   */
  public ExtendedBasicNode( int nodeType) {
    super();
    this.nodeType = nodeType;
  }

   /**
   * <code>initialize</code> - modified from BasicNode to handle other node shapes
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
    myPort = new ExtendedBasicNodePort( nodeType);
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
   * <code>createDrawable</code> - modified from BasicNode to handle other node shapes
   *
   * @return - <code>JGoDrawable</code> - 
   */
  public JGoDrawable createDrawable() {
    JGoDrawable d = null;
    if (nodeType == ViewConstants.RECTANGLE) { // PwToken
      d = new JGoRectangle();
    } else if (nodeType == ViewConstants.ELLIPSE) { // PwRule
      d = new JGoEllipse();
    } else if (nodeType == ViewConstants.DIAMOND) { // PwConstraint
      d = new Diamond();
    } else if (nodeType == ViewConstants.LEFT_TRAPEZOID) { // PwObject
      d = new LeftTrapezoid();
    } else if (nodeType == ViewConstants.RIGHT_TRAPEZOID) { // PwTimeline
      d = new RightTrapezoid();
    } else if (nodeType == ViewConstants.HEXAGON) { // PwSlot
      d = new Hexagon();
    } else if (nodeType == ViewConstants.PINCHED_RECTANGLE) {//  PwVariable
      d = new PinchedRectangle();
    } else if (nodeType == ViewConstants.PINCHED_HEXAGON) { // PwResource
      d = new PinchedHexagon();
    }
    d.setSelectable(false);
    d.setDraggable(false);
    d.setSize(20, 20);
    return d;
  } // end createDrawable

  /**
   * <code>setAreNeighborsShown</code>
   *
   * @param areShown - <code>boolean</code> - 
   */
  public void setAreNeighborsShown( final boolean areShown) {
    areNeighborsShown = areShown;
  }

  /**
   * <code>areNeighborsShown</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean areNeighborsShown() {
    return areNeighborsShown;
  }



} // end class ExtendedBasicNode
  
 
