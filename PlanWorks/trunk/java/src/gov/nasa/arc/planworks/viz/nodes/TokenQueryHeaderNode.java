// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenQueryHeaderNode.java,v 1.1 2003-12-20 01:54:50 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
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
import gov.nasa.arc.planworks.viz.TokenQueryContentView;
import gov.nasa.arc.planworks.viz.TokenQueryHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TokenQueryView;
import gov.nasa.arc.planworks.viz.util.TokenQueryComparatorAscending;
import gov.nasa.arc.planworks.viz.util.TokenQueryComparatorDescending;


/**
 * <code>TokenQueryHeaderNode</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a> 
 *                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenQueryHeaderNode extends TextNode {

  private String headerLabel;
  private VizView vizView;
  private boolean  isAscending;


  /**
   * <code>TokenQueryHeaderNode</code> - constructor 
   *
   * @param headerLabel - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TokenQueryHeaderNode( String headerLabel, VizView vizView) {
    super( headerLabel);
    this.headerLabel = headerLabel;
    this.vizView = vizView;

    if ((vizView instanceof TokenQueryView) &&
         headerLabel.equals( ViewConstants.QUERY_TOKEN_STEP_NUM_HEADER)) {
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
    // System.err.println( "TokenNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    TokenQueryHeaderNode tokenQueryNode = (TokenQueryHeaderNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      sortTokenQueryList();
      isAscending = ! isAscending;
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    }
    return false;
  } // end doMouseClick   


  private void sortTokenQueryList() {
    TokenQueryContentView tokenQueryContentView = null;
    List tokenQueryList = null;
    if (vizView instanceof TokenQueryView) {
      tokenQueryContentView = ((TokenQueryView) vizView).getTokenQueryContentView();
      tokenQueryList = tokenQueryContentView.getTokenList();
    } else {
      System.err.println( "TokenQueryHeaderNode.sortTokenQueryList: vizView " +
                          vizView + " not handled");
      System.exit( -1);
    }
    if (isAscending) { 
      Collections.sort( tokenQueryList,
                        new TokenQueryComparatorAscending( headerLabel));
    } else {
      Collections.sort( tokenQueryList,
                        new TokenQueryComparatorDescending( headerLabel));
    }
    if (tokenQueryContentView != null) {
      tokenQueryContentView.redraw();
    }
  } // end sortTokenQueryList


} // end class TokenQueryHeaderNode

  
