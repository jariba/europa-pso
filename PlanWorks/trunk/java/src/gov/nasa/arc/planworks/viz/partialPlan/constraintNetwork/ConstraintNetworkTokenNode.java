// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNetworkTokenNode.java,v 1.6 2003-11-13 23:21:17 taylor Exp $
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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
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

  private PwSlot slot;
  private Map connectedTokenNodeMap;
  private List variableNodeList; // element VariableNode
  private boolean areNeighborsShown;
  private boolean hasDiscoveredLinks;
  private int variableLinkCount;
  private int connectedTokenNodeCount;


  /**
   * <code>ConstraintNetworkTokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public ConstraintNetworkTokenNode( PwToken token, PwSlot slot, Point tokenLocation,
                                     Color backgroundColor, boolean isFreeToken,
                                     boolean isDraggable,
                                     PartialPlanView partialPlanView) {
    super( token, slot, tokenLocation, backgroundColor, isFreeToken, isDraggable,
           partialPlanView);
    this.slot = slot;
    variableNodeList = new ArrayList();
    areNeighborsShown = false;
    hasDiscoveredLinks = false;
    variableLinkCount = 0;
    connectedTokenNodeCount = 0;
    connectedTokenNodeMap = new HashMap();
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
    StringBuffer tip = new StringBuffer( "<html> ");
    String operation = null;
    if (areNeighborsShown) {
      operation = "close";
    } else {
      operation = "open";
    }
    if (token != null) {
      tip.append( token.toString());
    } else {
      tip.append( "<empty>");
    }
    // check for free token
    if (slot != null) {
      tip.append( "<br>");
      tip.append( "slot key=");
      tip.append( slot.getId().toString());
    }
    tip.append( "<br> Mouse-L: ").append( operation).append( " nearest variables</html>");
    return tip.toString();
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
   * <code>setAreNeighborsShown</code>
   *
   * @param areShown - <code>boolean</code> - 
   */
  protected void setAreNeighborsShown( boolean areShown) {
    areNeighborsShown = areShown;
  }

  /**
   * <code>areNeighborsShown</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean areNeighborsShown() {
    return areNeighborsShown;
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
    // System.err.println( "ConstraintNetworkTokenNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    ConstraintNetworkTokenNode tokenNode =
      (ConstraintNetworkTokenNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      if (! areNeighborsShown) {
        //System.err.println( "doMouseClick: Mouse-L show variable nodes of " +
        //                    tokenNode.getPredicateName());
        addTokenNodeVariables( this, (ConstraintNetworkView) partialPlanView);
        areNeighborsShown = true;
      } else {
        // System.err.println( "doMouseClick: Mouse-L hide variable nodes of " +
        //                    tokenNode.getPredicateName());
        removeTokenNodeVariables( this, (ConstraintNetworkView) partialPlanView);
        areNeighborsShown = false;
      }
      return true;
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      super.doMouseClick( modifiers, docCoords, viewCoords, view);
      return true;
    }
    return false;
  } // end doMouseClick   

  /**
   * <code>addTokenNodeVariables</code> - protected since
   *                                           needed by ConstraintJGoView
   *
   * @param tokenNode - <code>ConstraintNetworkTokenNode</code> - 
   * @param constraintNetworkView - <code>ConstraintNetworkView</code> - 
   */
  protected void addTokenNodeVariables( ConstraintNetworkTokenNode tokenNode,
                                        ConstraintNetworkView constraintNetworkView) {
    boolean areNodesChanged = constraintNetworkView.addVariableNodes( tokenNode);
    boolean areLinksChanged = constraintNetworkView.addVariableToTokenLinks( tokenNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( tokenNode);
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 2,  ColorMap.getColor( "black")));
  } // end addTokenNodeVariables

  private void removeTokenNodeVariables( ConstraintNetworkTokenNode tokenNode,
                                         ConstraintNetworkView constraintNetworkView) {
    boolean areLinksChanged = constraintNetworkView.removeVariableToTokenLinks( tokenNode);
    boolean areNodesChanged = constraintNetworkView.removeVariableNodes( tokenNode);
    if (areNodesChanged || areLinksChanged) {
      constraintNetworkView.setLayoutNeeded();
      constraintNetworkView.setFocusNode( tokenNode);
      constraintNetworkView.redraw();
    }
    setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "black")));
  } // end adremoveTokenNodeVariables

  public void discoverLinkage() {
    ListIterator varIterator = variableNodeList.listIterator();
    while(varIterator.hasNext()) {
      VariableNode varNode = (VariableNode) varIterator.next();
      ListIterator constraintIterator = varNode.getConstraintNodeList().listIterator();
      while(constraintIterator.hasNext()) {
	ConstraintNode conNode = (ConstraintNode) constraintIterator.next();
	ListIterator constrainedVarIterator = conNode.getVariableNodeList().listIterator();
	while(constrainedVarIterator.hasNext()) {
	  VariableNode constrainedVarNode = (VariableNode) constrainedVarIterator.next();
	  if(constrainedVarNode.equals(varNode)) {
	    continue;
	  }
	  ListIterator tokenNodeIterator = constrainedVarNode.getTokenNodeList().listIterator();
	  while(tokenNodeIterator.hasNext()) {
	    ConstraintNetworkTokenNode tokenNode = (ConstraintNetworkTokenNode) tokenNodeIterator.next();
	    if(tokenNode.equals(this)) {
	      continue;
	    }
	    if(!connectedTokenNodeMap.containsKey(tokenNode)) {
	      connectedTokenNodeMap.put(tokenNode, new Integer(0));
	    }
	    connectedTokenNodeMap.put(tokenNode, 
				      new Integer(((Integer)connectedTokenNodeMap.get(tokenNode)).intValue() + 1));
	    connectedTokenNodeCount++;
	  }
	}
      }
    }
    hasDiscoveredLinks = true;
  }

  public int getTokenLinkCount() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return connectedTokenNodeCount;
  }

  public int getTokenLinkCount(ConstraintNetworkTokenNode other) {
    Integer retval = (Integer) connectedTokenNodeMap.get(other);
    if(retval == null) {
      return 0;
    }
    return retval.intValue();
  }

  public List getConnectedTokenNodes() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return new ArrayList(connectedTokenNodeMap.keySet());
  }
} // end class ConstraintNetworkTokenNode
