// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionHeaderNode.java,v 1.1 2003-10-28 20:42:00 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 27oct03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;
import java.util.Collections;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.StepContentView;
import gov.nasa.arc.planworks.viz.TransactionContentView;
import gov.nasa.arc.planworks.viz.TransactionHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.transaction.TransactionView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TransactionQueryView;
import gov.nasa.arc.planworks.viz.util.TransactionComparatorAscending;
import gov.nasa.arc.planworks.viz.util.TransactionComparatorDescending;


/**
 * <code>TransactionHeaderNode</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a> 
 *                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionHeaderNode extends TextNode {

  private String headerLabel;
  private VizView vizView;
  private boolean  isAscending;


  /**
   * <code>TransactionHeaderNode</code> - constructor 
   *
   * @param headerLabel - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TransactionHeaderNode( String headerLabel, VizView vizView) {
    super( headerLabel);
    this.headerLabel = headerLabel;
    this.vizView = vizView;

    if ((((vizView instanceof TransactionView) ||
          (vizView instanceof TransactionQueryView)) &&
         headerLabel.equals( ViewConstants.TRANSACTION_KEY_HEADER)) ||
        ((vizView instanceof StepQueryView) &&
         headerLabel.equals( ViewConstants.TRANSACTION_STEP_NUM_HEADER))) {
      isAscending = false;
    } else {
      isAscending = true;
    }
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (isAscending) {
      return "Sort in ascending order";
    } else {
      return "Sort in descending order";
    }
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - alternately sort ascending/sort descending the values
   *                             in this column of the transaction entries
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    // System.err.println( "TokenNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    TransactionHeaderNode tokenNode = (TransactionHeaderNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      sortTransactionList();
      isAscending = ! isAscending;
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    }
    return false;
  } // end doMouseClick   


  private void sortTransactionList() {
    TransactionContentView transactionContentView = null;
    StepContentView stepContentView = null;
    List transactionList = null;
    if (vizView instanceof TransactionView) {
      transactionContentView = ((TransactionView) vizView).getTransactionContentView();
      transactionList = transactionContentView.getTransactionList();
    } else if (vizView instanceof TransactionQueryView) {
      transactionContentView = ((TransactionQueryView) vizView).getTransactionContentView();
      transactionList = transactionContentView.getTransactionList();
    } else if (vizView instanceof StepQueryView) {
      stepContentView = ((StepQueryView) vizView).getStepContentView();
      transactionList = stepContentView.getTransactionList();
    } else {
      System.err.println( "TransactionHeaderNode.sortTransactionList: vizView " +
                          vizView + " not handled");
      System.exit( -1);
    }
    if (isAscending) {
      Collections.sort( transactionList,
                        new TransactionComparatorAscending( headerLabel));
    } else {
      Collections.sort( transactionList,
                        new TransactionComparatorDescending( headerLabel));
    }
    if (transactionContentView != null) {
      transactionContentView.redraw();
    } else if (stepContentView != null) {
      stepContentView.redraw();
    }

  } // end sortTransactionList


} // end class TransactionHeaderNode

  
