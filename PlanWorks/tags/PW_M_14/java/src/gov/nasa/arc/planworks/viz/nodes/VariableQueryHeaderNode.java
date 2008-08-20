// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableQueryHeaderNode.java,v 1.1 2003-12-20 01:54:51 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 19dec03
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
import gov.nasa.arc.planworks.viz.VariableQueryContentView;
import gov.nasa.arc.planworks.viz.VariableQueryHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.VariableQueryView;
import gov.nasa.arc.planworks.viz.util.VariableQueryComparatorAscending;
import gov.nasa.arc.planworks.viz.util.VariableQueryComparatorDescending;


/**
 * <code>VariableQueryHeaderNode</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a> 
 *                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableQueryHeaderNode extends TextNode {

  private String headerLabel;
  private VizView vizView;
  private boolean isAscending;


  /**
   * <code>VariableQueryHeaderNode</code> - constructor 
   *
   * @param headerLabel - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public VariableQueryHeaderNode( String headerLabel, VizView vizView) {
    super( headerLabel);
    this.headerLabel = headerLabel;
    this.vizView = vizView;

    if ((vizView instanceof VariableQueryView) &&
         headerLabel.equals( ViewConstants.QUERY_VARIABLE_STEP_NUM_HEADER)) {
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
      return "M-L: Sort in ascending order";
    } else {
      return "M-L: Sort in descending order";
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
    // System.err.println( "VariableNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    VariableQueryHeaderNode variableQueryNode = (VariableQueryHeaderNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      sortVariableQueryList();
      isAscending = ! isAscending;
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    }
    return false;
  } // end doMouseClick   


  private void sortVariableQueryList() {
    VariableQueryContentView variableQueryContentView = null;
    List variableQueryList = null;
    if (vizView instanceof VariableQueryView) {
      variableQueryContentView = ((VariableQueryView) vizView).getVariableQueryContentView();
      variableQueryList = variableQueryContentView.getVariableList();
    } else {
      System.err.println( "VariableQueryHeaderNode.sortVariableQueryList: vizView " +
                          vizView + " not handled");
      System.exit( -1);
    }
    if (isAscending) {
      Collections.sort( variableQueryList,
                        new VariableQueryComparatorAscending( headerLabel));
    } else {
      Collections.sort( variableQueryList,
                        new VariableQueryComparatorDescending( headerLabel));
    }
    if (variableQueryContentView != null) {
      variableQueryContentView.redraw();
    }
  } // end sortVariableQueryList


} // end class VariableQueryHeaderNode

  
