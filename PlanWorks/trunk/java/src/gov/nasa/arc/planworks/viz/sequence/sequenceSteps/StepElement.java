// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepElement.java,v 1.2 2003-10-07 02:13:35 taylor Exp $
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
import java.util.Iterator;
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
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTransaction;
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

  private static final int STEP_WIDTH = ViewConstants.STEP_VIEW_STEP_WIDTH;
  private static final int STEP_LINE_WIDTH = 1;
  private static final int STEP_LINE_TYPE = JGoPen.SOLID;
  private static final Color STEP_LINE_COLOR = ColorMap.getColor( "black");
  private static final Color STEP_BG_COLOR = ViewConstants.VIEW_BACKGROUND_COLOR;

  private int stepNumber;
  private String partialPlanName;
  private PwPartialPlan partialPlan;
  private int planDBSize;
  private int height;
  private List transactionList;
  private int numTransactions;

  /**
   * <code>StepElement</code> - constructor 
   *
   * @param x - <code>int</code> - 
   * @param y - <code>int</code> - 
   * @param planDBSize - <code>int</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param transactionList - <code>List</code> - 
   */
  public StepElement( int x, int y, int planDBSize, String partialPlanName,
                      PwPartialPlan partialPlan, List transactionList) {
    super( x, y, STEP_WIDTH, planDBSize / ViewConstants.STEP_VIEW_DB_SIZE_SCALING,
           STEP_LINE_WIDTH, STEP_LINE_TYPE, STEP_LINE_COLOR, STEP_BG_COLOR);
    this.planDBSize = planDBSize;
    this.height = planDBSize / ViewConstants.STEP_VIEW_DB_SIZE_SCALING;
    this.partialPlanName = partialPlanName;
    this.stepNumber = Integer.parseInt( partialPlanName.substring( 4)); // discard prefix "step"
    this.partialPlan = partialPlan;
    this.transactionList = transactionList;

    numTransactions = 0;
    if (transactionList != null) {
      numTransactions = transactionList.size();
      Iterator transItr = transactionList.iterator();
      System.err.println( "\n\nStep " + this.stepNumber);
      while (transItr.hasNext()) {
        PwTransaction transaction = (PwTransaction) transItr.next();
        System.err.println( "   type " + transaction.getType() + " objectId " +
                            transaction.getObjectId());
      }
    }
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
    tip.append( "<br>planDBSize: ");
    tip.append( String.valueOf( planDBSize));
    tip.append( "; numTransactions: ");
    tip.append( String.valueOf( numTransactions));
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
