// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepElement.java,v 1.17 2005-11-10 01:22:14 miatauro Exp $
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
import javax.swing.JOptionPane;
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
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
//import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.HistogramElement;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;


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

  private String partialPlanName;
  private PwPlanningSequence planSequence;
  private List transactionList;
  private SequenceView sequenceView;
  private String dbType;
  private int planDBSize;
  private Color dbBgColor;

  private int stepNumber;
  private int height;
  private int numTransactions;
  private List viewListenerList; // element ViewListener


  /**
   * <code>StepElement</code> - constructor 
   *
   * @param x - <code>int</code> - 
   * @param y - <code>int</code> - 
   * @param height - <code>int</code> - 
   * @param dbType - <code>String</code> - 
   * @param planDBSize - <code>int</code> - 
   * @param dbBgColor - <code>Color</code> - 
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param sequenceView - <code>SequenceView</code> - 
   */
  public StepElement( int x, int y, int height, String dbType, int planDBSize,
                      Color dbBgColor, String partialPlanName,
                      PwPlanningSequence planSequence, SequenceView sequenceView) {
    super( x, y, STEP_WIDTH, height, STEP_LINE_WIDTH, STEP_LINE_TYPE,
           STEP_LINE_COLOR, dbBgColor);
    this.height = height;
    this.dbType = dbType;
    this.planDBSize = planDBSize;
    this.dbBgColor = dbBgColor;
    this.partialPlanName = partialPlanName;
    stepNumber = Utilities.getStepNumber( partialPlanName);
    this.planSequence = planSequence;
    this.sequenceView = sequenceView;
    viewListenerList = new ArrayList();
    for (int i = 0, n = PlanWorks.PARTIAL_PLAN_VIEW_LIST.size(); i < n; i++) {
      viewListenerList.add( null);
    }
  } // end constructor


  /**
   * <code>getStepNumber</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getStepNumber() {
    return stepNumber;
  }

  /**
   * <code>getPartialPlanName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getPartialPlanName() {
    return partialPlanName;
  }

  /**
   * <code>getDbBgColor</code>
   *
   * @return - <code>Color</code> - 
   */
  public final Color getDbBgColor() {
    return dbBgColor;
  }
    
  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html> step");
    tip.append( String.valueOf( stepNumber));
    tip.append( "<br> ").append( dbType).append( ": ");
    tip.append( String.valueOf( planDBSize));
//     tip.append( "<br> numTransactions: ");
//     tip.append( String.valueOf( numTransactions));
    tip.append( " </html>");
    return tip.toString();
  } // end getToolTipText

  
  /**
   * <code>doMouseClickWithListener</code> - called from PlanWorksGUITest
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @param viewListenerList - <code>List</code> - 
   */
  public void doMouseClickWithListener( int modifiers, Point docCoords,
                                        Point viewCoords, JGoView view,
                                        List viewListenerList) {
    this.viewListenerList = viewListenerList;
    doMouseClick( modifiers, docCoords, viewCoords, view);
  } // end doMouseClickWithListener

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
    // System.err.println( "doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    StepElement stepElement = (StepElement) obj.getTopLevelObject();
    ((SequenceStepsView) sequenceView).setSelectedStepElement( stepElement);
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      ViewGenerics.partialPlanViewsPopupMenu( stepNumber, planSequence, sequenceView,
                                              viewCoords, viewListenerList);
      return true;
    }
    return false;
  } // end doMouseClick   



} // end class StepElement
