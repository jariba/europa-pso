// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNetworkTokenNode.java,v 1.1 2003-09-30 19:18:56 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- subclassed from TokenNode 30sep03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;


/**
 * <code>ConstraintNetworkTokenNode</code> - TokenNode behavior in the Constraint Network
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConstraintNetworkTokenNode extends TokenNode {

  private List variableNodeList; // element VariableNode
  private boolean areNeighborsShown;
  private int variableLinkCount;


  /**
   * <code>ConstraintNetworkTokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ConstraintNetworkTokenNode( PwToken token, Point tokenLocation,
                                     Color backgroundColor, boolean isFreeToken,
                                     boolean isDraggable,
                                     PartialPlanView partialPlanView) {
    super( token, tokenLocation, backgroundColor, isFreeToken, isDraggable,
           partialPlanView);
    variableNodeList = new ArrayList();
    areNeighborsShown = false;
    variableLinkCount = 0;
  } // end constructor

  /**
   * <code>incrVariableLinkCount</code>
   *
   */
  public void incrVariableLinkCount() {
    variableLinkCount++;
  }

  /**
   * <code>decVariableLinkCount</code>
   *
   */
  public void decVariableLinkCount() {
    variableLinkCount--;
  }

  /**
   * <code>getVariableLinkCount</code>
   *
   * @return - <code>int</code> - 
   */
  public int getVariableLinkCount() {
    return variableLinkCount;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    String operation = null;
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    return "<html> " + token.toString() +
      "<br> Mouse-L: " + operation + " nearest variables</html>";
  } // end getToolTipText


  /**
   * <code>getVariableNodeList</code>
   *
   * @return - <code>List</code> - of VariableNode
   */
  public List getVariableNodeList() {
    return variableNodeList;
  }

  /**
   * <code>addVariableNode</code>
   *
   * @param variableNode - <code>VariableNode</code> - 
   */
  public void addVariableNode( VariableNode variableNode) {
    variableNodeList.add( variableNode);
  }

  /**
   * <code>doMouseClick</code> - For Constraint Network View, Mouse-Left opens/closes
   *            tokenNode to show variableNodes.  Mouse-Right: Set Active Token
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
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    ConstraintNetworkTokenNode tokenNode =
      (ConstraintNetworkTokenNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! areNeighborsShown) {
        //System.err.println( "doMouseClick: Mouse-L show variable nodes of " +
        //                    tokenNode.getPredicateName());
        addTokenNodeVariables( this);
        areNeighborsShown = true;
      } else {
        //System.err.println( "doMouseClick: Mouse-L hide variable nodes of " +
        //                    tokenNode.getPredicateName());
        removeTokenNodeVariables( this);
        areNeighborsShown = false;
      }
      ((ConstraintNetworkView) partialPlanView).setFocusNode( tokenNode);
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // handled by super
    }
    return false;
  } // end doMouseClick   

  private void addTokenNodeVariables( ConstraintNetworkTokenNode tokenNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) tokenNode.getPartialPlanView();
    boolean areNodesChanged = constraintNetworkView.addVariableNodes( tokenNode);
    boolean areLinksChanged = constraintNetworkView.addVariableToTokenLinks( tokenNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addTokenNodeVariables

  private void removeTokenNodeVariables( ConstraintNetworkTokenNode tokenNode) {
    ConstraintNetworkView constraintNetworkView =
      (ConstraintNetworkView) tokenNode.getPartialPlanView();
    boolean areLinksChanged = constraintNetworkView.removeVariableToTokenLinks( tokenNode);
    boolean areNodesChanged = constraintNetworkView.removeVariableNodes( tokenNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end adremoveTokenNodeVariables



} // end class ConstraintNetworkTokenNode
