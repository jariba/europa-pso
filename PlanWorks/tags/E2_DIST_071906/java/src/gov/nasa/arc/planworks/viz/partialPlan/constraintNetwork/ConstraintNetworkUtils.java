//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ConstraintNetworkUtils.java,v 1.10 2004-08-26 20:51:25 taylor Exp $
//
package gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;

public final class ConstraintNetworkUtils {
  protected static int discoverLinkage(VariableContainerNode contNode, Map connectedContainerMap) {
    int connectedContainerCount = 0;
    ListIterator varIt = contNode.getContainer().getVariables().listIterator();
    while(varIt.hasNext()) {
      PwVariable var = (PwVariable) varIt.next();
      ListIterator constrIt = var.getConstraintList().listIterator();
      while(constrIt.hasNext()) {
        PwConstraint constr = (PwConstraint) constrIt.next();
        ListIterator constrVarIt = constr.getVariablesList().listIterator();
        while(constrVarIt.hasNext()) {
          PwVariable constrVar = (PwVariable) constrVarIt.next();
          if(constrVar.equals(var)) {
            continue;
          }
          PwVariableContainer varCont = (PwVariableContainer) constrVar.getParent();
          if(!connectedContainerMap.containsKey(varCont)) {
            connectedContainerMap.put(varCont, new Integer(0));
          }
          connectedContainerMap.put(varCont, new Integer(((Integer)connectedContainerMap.get(varCont)).intValue() + 1));
          connectedContainerCount++;
        }
      }
    }
    return connectedContainerCount;
  }

  protected static void connectNodes(Map containerNodeMap, Map connectedContainerMap,
                                     List connectedContainerNodes) {
    Iterator contIt = connectedContainerMap.keySet().iterator();
    while(contIt.hasNext()) {
      PwVariableContainer otherCont = (PwVariableContainer) contIt.next();
      if(containerNodeMap.containsKey(otherCont.getId())) {
        connectedContainerNodes.add(containerNodeMap.get(otherCont.getId()));
      }
    }
  }

  protected static void addContainerNodeVariables(VariableContainerNode node,
                                                  ConstraintNetworkView view, boolean doRedraw) {
    view.setStartTimeMSecs(System.currentTimeMillis());
    boolean areNodesChanged = view.addVariableNodes(node);
    boolean areLinksChanged = view.addVariableToContainerLinks(node);
    if(doRedraw && (areNodesChanged || areLinksChanged)) {
      view.setLayoutNeeded();
      view.setFocusNode((JGoArea) node);
      view.redraw( true);
    }
  }

  protected static void removeContainerNodeVariables(VariableContainerNode node,
                                              ConstraintNetworkView view) {
    view.setStartTimeMSecs(System.currentTimeMillis());
    boolean areLinksChanged = view.removeVariableToContainerLinks(node);
    boolean areNodesChanged = view.removeVariableNodes(node);
    if(areNodesChanged || areLinksChanged) {
      view.setLayoutNeeded();
      view.setFocusNode((JGoArea) node);
      view.redraw( true);
    }
  }

  protected static void mouseRightPopupMenu(final Point viewCoords, 
                                            final BasicNode node,
                                            final PartialPlanView view) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem navItem = new JMenuItem("Open Navigator View");
    navItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          try {
            String viewSetKey = view.getNavigatorViewSetKey();
            MDIInternalFrame navFrame = view.openNavigatorViewFrame( viewSetKey);
            Container contentPane = navFrame.getContentPane();
            PwPartialPlan partialPlan = view.getPartialPlan();
            if (node instanceof VariableContainerNode) {
              Class [] constructorParams = {
                // node.getClass(), 
                Class.forName("gov.nasa.arc.planworks.viz.nodes.VariableContainerNode"),
                Class.forName("gov.nasa.arc.planworks.viz.viewMgr.ViewableObject"), 
                Class.forName("gov.nasa.arc.planworks.viz.viewMgr.ViewSet"),
                viewSetKey.getClass(),
                navFrame.getClass()
              };
              Class navClass = Class.
                forName("gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView");
              Constructor navConstructor = navClass.getConstructor(constructorParams);
              Object [] constructorArgs = {node, partialPlan, view.getViewSet(), viewSetKey,
                                           navFrame};
              contentPane.add((NavigatorView)navConstructor.newInstance(constructorArgs));
            } else if (node instanceof ConstraintNode) {
              contentPane.add(new NavigatorView( ((ConstraintNode) node).getConstraint(),
                                                 partialPlan, view.getViewSet(),
                                                 viewSetKey, navFrame));
            } else if (node instanceof VariableNode) {
              contentPane.add(new NavigatorView( ((VariableNode) node).getVariable(),
                                                 partialPlan, view.getViewSet(),
                                                 viewSetKey, navFrame));
            }
          }
          catch(Exception e) {
            e.printStackTrace();
          }
        }
      });
    menu.add(navItem);

    if (node instanceof ConstraintNetworkRuleInstanceNode) {
      ViewListener viewListener = null;
      menu.add( ViewGenerics.createRuleInstanceViewItem( (RuleInstanceNode) node, view,
                                                         viewListener));
    }

    ViewGenerics.showPopupMenu(menu, view, viewCoords);
  }

  protected static boolean containerDoMouseClick(int modifiers, Point docCoords, Point viewCoords, 
                                                 JGoView view, VariableContainerNode node,
                                                 ConstraintNetworkView cNetView) {
    JGoObject obj = view.pickDocObject(docCoords, false);
    if(MouseEventOSX.isMouseLeftClick(modifiers, PlanWorks.isMacOSX())) {
      if(!node.areNeighborsShown()) {
        boolean doRedraw = true;
        node.addContainerNodeVariables(node, cNetView, doRedraw);
      }
      else {
        node.removeContainerNodeVariables(node, cNetView);
      }
      node.setAreNeighborsShown(!node.areNeighborsShown());
      return true;
    }
    else if(MouseEventOSX.isMouseRightClick(modifiers, PlanWorks.isMacOSX())) {
      node.mouseRightPopupMenu(viewCoords);
      return true;
    }
    return false;
  }
}
