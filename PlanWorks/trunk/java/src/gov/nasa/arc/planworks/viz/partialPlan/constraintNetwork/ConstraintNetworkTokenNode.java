// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ConstraintNetworkTokenNode.java,v 1.13 2004-02-13 21:23:27 miatauro Exp $
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
import java.util.Iterator;
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
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
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
  private Map connectedTokenMap;
  private List variableNodeList; // element VariableNode
  private List connectedTokenNodes;
  private boolean areNeighborsShown;
  private boolean hasDiscoveredLinks;
  private int variableLinkCount;
  private int connectedTokenCount;
  private Color backgroundColor;

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
    variableNodeList = new UniqueSet();
    areNeighborsShown = false;
    hasDiscoveredLinks = false;
    variableLinkCount = 0;
    connectedTokenCount = 0;
    connectedTokenMap = new HashMap();
    connectedTokenNodes = new UniqueSet();
    this.backgroundColor = backgroundColor;
  } // end constructor

  public Color getColor(){return backgroundColor;}

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
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
    // check for free token
    if (slot != null) {
      tip.append( "<br>");
      tip.append( "slot key=");
      tip.append( slot.getId().toString());
    }
    tip.append( "<br> Mouse-L: ").append( operation).append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (token != null) {
      tip.append( getPredicateName());
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
    tip.append( "<br>key=");
    tip.append( token.getId().toString());
    tip.append( "</html>");
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
    if(!variableNodeList.contains(variableNode)) {
      variableNodeList.add( variableNode);
    }
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
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
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
    constraintNetworkView.setStartTimeMSecs( System.currentTimeMillis());
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
    ListIterator varIterator = token.getVariablesList().listIterator();
    while(varIterator.hasNext()) {
      PwVariable var = (PwVariable) varIterator.next();
      ListIterator constraintIterator = var.getConstraintList().listIterator();
      while(constraintIterator.hasNext()) {
	PwConstraint constr = (PwConstraint) constraintIterator.next();
	ListIterator constrainedVarIterator = constr.getVariablesList().listIterator();
	while(constrainedVarIterator.hasNext()) {
	  PwVariable constrVar = (PwVariable) constrainedVarIterator.next();
	  if(constrVar.equals(var)) {
	    continue;
	  }
          //THIS NEEDS TO CHANGE
	  //ListIterator tokenNodeIterator = constrVar.getTokenList().listIterator();
	  //while(tokenNodeIterator.hasNext()) {
          if(!(constrVar.getParent() instanceof PwToken)) {
            continue;
          }
          PwToken varToken = (PwToken) constrVar.getParent();
          if(!connectedTokenMap.containsKey(varToken)) {
            connectedTokenMap.put(varToken, new Integer(0));
          }
          connectedTokenMap.put(varToken, new Integer(((Integer)connectedTokenMap.
                                                       get(varToken)).intValue() + 1));
          connectedTokenCount++;
	}
      }
    }
    hasDiscoveredLinks = true;
  }

  public int getTokenLinkCount() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return connectedTokenCount;
  }

  public int getTokenLinkCount(PwToken other) {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    Integer retval = (Integer) connectedTokenMap.get(other);
    if(retval == null) {
      return 0;
    }
    return retval.intValue();
  }

  public int getTokenLinkCount(ConstraintNetworkTokenNode other) {
    return getTokenLinkCount(other.getToken());
  }

  public List getConnectedTokens() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return new ArrayList(connectedTokenMap.keySet());
  }

  public void connectNodes(Map tokenNodeMap) {
    Iterator tokenIterator = connectedTokenMap.keySet().iterator();
    while(tokenIterator.hasNext()) {
      PwToken otherToken = (PwToken) tokenIterator.next();
      if(tokenNodeMap.containsKey(otherToken.getId())) {
        connectedTokenNodes.add(tokenNodeMap.get(otherToken.getId()));
      }
    }
  }

  public List getConnectedTokenNodes() {
    if(!hasDiscoveredLinks) {
      discoverLinkage();
    }
    return new ArrayList(connectedTokenNodes);
  }
  
  public boolean equals(ConstraintNetworkTokenNode n) {
    return token.getId().equals(n.getToken().getId());
  }
} // end class ConstraintNetworkTokenNode
