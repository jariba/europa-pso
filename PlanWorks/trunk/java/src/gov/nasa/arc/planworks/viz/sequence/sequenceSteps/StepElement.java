// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepElement.java,v 1.1 2003-10-02 23:24:22 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 02oct03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceSteps;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.HistogramElement;


/**
 * <code>StepElement</code> - JGo widget to render a sequence step as a histogram
 *                             element

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class StepElement extends HistogramElement {

  private static final int STEP_WIDTH = 10;
  private static final int STEP_LINE_WIDTH = 1;
  private static final int STEP_LINE_TYPE = JGoPen.SOLID;
  private static final Color STEP_LINE_COLOR = ColorMap.getColor( "black");
  private static final Color STEP_BG_COLOR = ViewConstants.VIEW_BACKGROUND_COLOR;

  private int stepNumber;
  private PwToken activeToken;
  private String tokenTransaction;

  /**
   * <code>StepElement</code> - constructor 
   *
   * @param x - <code>int</code> - 
   * @param y - <code>int</code> - 
   * @param height - <code>int</code> - 
   * @param activeToken - <code>PwToken</code> - 
   * @param tokenTransaction - <code>String</code> - 
   */
  public StepElement( int x, int y, int height, int stepNumber,PwToken activeToken,
                      String tokenTransaction) {
    super( x, y, STEP_WIDTH, height, STEP_LINE_WIDTH, STEP_LINE_TYPE, STEP_LINE_COLOR,
           STEP_BG_COLOR); 
    this.stepNumber = stepNumber;
    this.activeToken = activeToken;
    this.tokenTransaction = tokenTransaction;  
  } // end constructor


  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>Step #");
    tip.append( String.valueOf( stepNumber));
    tip.append( ": ");
    // tip.append( activeToken.toString());
    tip.append( "token-predicate-name");
    tip.append( "<br>");
    tip.append( tokenTransaction);
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>doMouseClick</code> - 
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    StepElement stepElement = (StepElement) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // mouseRightPopupMenu( viewCoords);
      // return true;
    }
    return false;
  } // end doMouseClick   


} // end class StepElement
