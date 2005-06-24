// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepButton.java,v 1.3 2004-06-10 01:36:12 taylor Exp $
//
// PlanWorks
//
//

package gov.nasa.arc.planworks.viz.util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;


/**
 * <code>StepButton</code> - 
 */
public class StepButton extends BasicNode implements OverviewToolTip{

  protected Point location;
  protected String nodeLabel;
  protected String toolTipText;
  private List actionListeners;

  /**
   * <code>StepButton</code> - constructor 
   *
   * @param location - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param label - <code>String</code> - 
   * @param toolTipText - <code>String</code> - 
   */
  public StepButton(Point location, Color backgroundColor, String label, String toolTipText) {
    super();
    this.location = location;
    this.toolTipText = toolTipText;
    nodeLabel = label;
    actionListeners = new LinkedList();
    configure(location, backgroundColor, false);
  } // end constructor

  private final void configure( Point location, Color backgroundColor,
                                boolean isDraggable) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( location, nodeLabel, isRectangular);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setSelectable(false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline(false);
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>StepButton</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( StepButton node) {
    return this.nodeLabel.equals(node.nodeLabel);
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return nodeLabel;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append(toolTipText);
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    return getToolTipText();
  } // end getToolTipText


  public void addActionListener(ActionListener l) {
    actionListeners.add(l);
  }

  /**
   * <code>getActionListeners</code>
   *
   */
  public List getActionListeners() {
    return actionListeners;
  }

  /**
   * <code>doMouseClick</code> - 
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    //JGoObject obj = view.pickDocObject( docCoords, false);
    //TokenNode tokenNode = (TokenNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      ListIterator actionList = actionListeners.listIterator();
      ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "LeftClick", 
                                      (int) AWTEvent.MOUSE_EVENT_MASK);
      while(actionList.hasNext()) {
        ((ActionListener) actionList.next()).actionPerformed(e);
      }
    } 
    else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
    }
    return false;
  }
} // end class StepButton

