// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConstraintJGoView.java,v 1.1 2003-11-06 00:02:18 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- split off from ConstraintNetworkView 03nov03
//

package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;

/**
 * <code>ConstraintJGoView</code> - subclass JGoView to add doBackgroundClick and
 *                           handle Mouse-Right functionality
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
class ConstraintJGoView extends JGoView {

  private ConstraintNetworkView constraintNetworkView;
  private PwPartialPlan partialPlan;


  /**
   * <code>ConstraintJGoView</code> - constructor 
   *
   * @param constraintNetworkView - <code>ConstraintNetworkView</code> - 
   */
  public ConstraintJGoView( ConstraintNetworkView constraintNetworkView) {
    super();
    this.constraintNetworkView = constraintNetworkView;
    this.partialPlan = constraintNetworkView.getPartialPlan();
  }

  /**
   * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
    }
  } // end doBackgroundClick



  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem tokenByKeyItem = new JMenuItem( "Find by Key");
    createNodeByKeyItem( tokenByKeyItem);
    mouseRightPopup.add( tokenByKeyItem);

    JMenuItem changeViewItem = new JMenuItem( "Open a View");
    constraintNetworkView.createChangeViewItem( changeViewItem, partialPlan, viewCoords);
    mouseRightPopup.add( changeViewItem);
    
    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Spec");
    constraintNetworkView.createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
    createActiveTokenItem( activeTokenItem);
    mouseRightPopup.add( activeTokenItem);

    JMenuItem changeLayoutItem = null;
    if(constraintNetworkView.getNewLayout().layoutHorizontal()) {
      changeLayoutItem = new JMenuItem("Vertical Layout");
    }
    else {
      changeLayoutItem = new JMenuItem("Horizontal Layout");
    }
    createChangeLayoutItem(changeLayoutItem);
    mouseRightPopup.add(changeLayoutItem);

    constraintNetworkView.createAllViewItems( partialPlan, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken =
            ((PartialPlanViewSet) constraintNetworkView.getViewSet()).getActiveToken();
          if (activeToken != null) {
            boolean isByKey = false;
            findAndSelectToken( activeToken, isByKey);
          }
        }
      });
  } // end createActiveTokenItem

  private void createNodeByKeyItem( JMenuItem tokenByKeyItem) {
    tokenByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", constraintNetworkView);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());

            PwToken tokenToFind = partialPlan.getToken( nodeKey);
            if (tokenToFind != null) {
              boolean isByKey = true;
              findAndSelectToken( tokenToFind, isByKey);
            } else {
              PwVariable variableToFind = partialPlan.getVariable( nodeKey);
              if (variableToFind != null) {
                findAndSelectVariable( variableToFind);
              } else {
                PwConstraint constraintToFind = partialPlan.getConstraint( nodeKey);
                if (constraintToFind != null) {
                  findAndSelectConstraint( constraintToFind);
                }
              }
            }
          }
        }
      });
  } // end createNodeByKeyItem

  private void createChangeLayoutItem(JMenuItem changeLayoutItem) {
    changeLayoutItem.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          NewConstraintNetworkLayout newLayout =
            constraintNetworkView.getNewLayout();
          if(newLayout.layoutHorizontal()) {
            newLayout.setLayoutVertical();
          }
          else {
            newLayout.setLayoutHorizontal();
          }
          newLayout.performLayout();
          constraintNetworkView.redraw();
        }
      });
  }

  private void findAndSelectToken( PwToken tokenToFind, boolean isByKey) {
    boolean isTokenFound = false;
    boolean isHighlightNode = true;
    Iterator tokenNodeListItr = constraintNetworkView.getTokenNodeList().iterator();
    while (tokenNodeListItr.hasNext()) {
      TokenNode tokenNode = (TokenNode) tokenNodeListItr.next();
      if ((tokenNode.getToken() != null) &&
          (tokenNode.getToken().getId().equals( tokenToFind.getId()))) {
        System.err.println( "ConstraintNetworkView found token: " +
                            tokenToFind.getPredicate().getName() +
                            " (key=" + tokenToFind.getId().toString() + ")");
        NodeGenerics.focusViewOnNode( tokenNode, isHighlightNode, this);
        isTokenFound = true;
        break;
      }
    }
    if (isTokenFound && (! isByKey)) {
      NodeGenerics.selectSecondaryNodes
        ( NodeGenerics.mapTokensToTokenNodes
          (((PartialPlanViewSet) constraintNetworkView.getViewSet()).
           getSecondaryTokens(), constraintNetworkView.getTokenNodeList()), this);
    }
    if (! isTokenFound) {
      String message = "Token " + tokenToFind.getPredicate().getName() +
        " (key=" + tokenToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                     "Token Not Found in ConstraintNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
      System.exit( 1);
    }
  } // end findAndSelectToken

  private void findAndSelectVariable( PwVariable variableToFind) {
    boolean isVariableFound = false;
    boolean isHighlightNode = true;
    Iterator variableNodeListItr = constraintNetworkView.getVariableNodeList().iterator();
    while (variableNodeListItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeListItr.next();
      if (variableNode.getVariable().getId().equals( variableToFind.getId())) {
        System.err.println( "ConstraintNetworkView found variable: " +
                            variableToFind.getDomain().toString() + " (key=" +
                            variableToFind.getId().toString() + ")");
        if (! variableNode.inLayout()) {
          ConstraintNetworkTokenNode tokenNode = 
            (ConstraintNetworkTokenNode) variableNode.getTokenNodeList().get( 0);
          System.err.println( "ConstraintNetworkView found token: " +
                              tokenNode.getPredicateName() +
                              " (key=" + tokenNode.getToken().getId().toString() + ")");
          // open connecting token to display it
          tokenNode.addTokenNodeVariables( tokenNode, constraintNetworkView);
          tokenNode.setAreNeighborsShown( true);
        }
        constraintNetworkView.setFocusNode( variableNode);
        NodeGenerics.focusViewOnNode( variableNode, isHighlightNode, this);
        isVariableFound = true;
        break;
      }
    }
    if (! isVariableFound) {
      String message = "Variable " + variableToFind.getDomain().toString() +
        " (key=" + variableToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                     "Variable Not Found in ConstraintNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
      System.exit( 1);
    }
  } // end findAndSelectVariable

  private void findAndSelectConstraint( PwConstraint constraintToFind) {
    boolean isConstraintFound = false;
    boolean isHighlightNode = true;
    Iterator constraintNodeListItr = constraintNetworkView.getConstraintNodeList().iterator();
    while (constraintNodeListItr.hasNext()) {
      ConstraintNode constraintNode = (ConstraintNode) constraintNodeListItr.next();
      if (constraintNode.getConstraint().getId().equals( constraintToFind.getId())) {
        System.err.println( "ConstraintNetworkView found constraint: " +
                            constraintToFind.getName() + " (key=" +
                            constraintToFind.getId().toString() + ")");
        if (! constraintNode.inLayout()) {
          VariableNode variableNode = null;
          // look for open connected variableNode
          variableNode = getVariableNodeInLayout( constraintNode);
          if (variableNode == null) {
            variableNode = 
              (VariableNode) constraintNode.getVariableNodeList().get( 0);
          }
          System.err.println( "ConstraintNetworkView found variable: " +
                              variableNode.getVariable().getDomain().toString() +
                              " (key=" + variableNode.getVariable().getId().toString() + ")");
          if (! variableNode.inLayout()) {
            ConstraintNetworkTokenNode tokenNode = null;
            // look for open connected tokenNode
            tokenNode = getOpenTokenNode( variableNode);
            if (tokenNode == null) {
              tokenNode = 
                (ConstraintNetworkTokenNode) variableNode.getTokenNodeList().get( 0);
              // open connecting token to display variable node
              tokenNode.addTokenNodeVariables( tokenNode, constraintNetworkView);
              tokenNode.setAreNeighborsShown( true);
            }
            System.err.println( "ConstraintNetworkView found token: " +
                                tokenNode.getPredicateName() +
                                " (key=" + tokenNode.getToken().getId().toString() + ")");
          }
          // open connecting variableNode to display it
          variableNode.addVariableNodeTokensAndConstraints( variableNode,
                                                            constraintNetworkView);
          variableNode.setAreNeighborsShown( true);
        }
        constraintNetworkView.setFocusNode( constraintNode);
        NodeGenerics.focusViewOnNode( constraintNode, isHighlightNode, this);
        isConstraintFound = true;
        break;
      }
    }
    if (! isConstraintFound) {
      String message = "Constraint " + constraintToFind.getName() +
        " (key=" + constraintToFind.getId().toString() + ") not found.";
      JOptionPane.showMessageDialog( PlanWorks.planWorks, message,
                                     "Constraint Not Found in ConstraintNetworkView",
                                     JOptionPane.ERROR_MESSAGE);
      System.err.println( message);
      System.exit( 1);
    }
  } // end findAndSelectConstraint

  private ConstraintNetworkTokenNode getOpenTokenNode( VariableNode variableNode) {
    Iterator tokenNodeItr = variableNode.getTokenNodeList().iterator();
    while (tokenNodeItr.hasNext()) {
      ConstraintNetworkTokenNode tokenNode =
        (ConstraintNetworkTokenNode) tokenNodeItr.next();
      if (tokenNode.areNeighborsShown()) {
        return tokenNode;
      }
    }
    return null;
  } // end getOpenTokenNode

  private VariableNode getVariableNodeInLayout( ConstraintNode constraintNode) {
    Iterator variableNodeItr = constraintNode.getVariableNodeList().iterator();
    while (variableNodeItr.hasNext()) {
      VariableNode variableNode = (VariableNode) variableNodeItr.next();
      if (variableNode.inLayout()) {
        return variableNode;
      }
    }
    return null;
  } // end getVariableNodeInLayout


} // end class ConstraintJGoView
