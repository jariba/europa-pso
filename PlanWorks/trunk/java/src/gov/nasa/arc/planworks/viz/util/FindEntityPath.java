// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FindEntityPath.java,v 1.5 2004-10-01 20:04:34 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18aug04
//

package gov.nasa.arc.planworks.viz.util; 

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.VariableContainerNode;
import gov.nasa.arc.planworks.viz.partialPlan.FindEntityPathAdapter;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNode;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.ConstraintNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.ModelClassNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.ResourceNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.RuleInstanceNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.SlotNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.TimelineNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.TokenNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.VariableNavNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkRuleInstanceNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkTokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.util.MessageDialog;
import gov.nasa.arc.planworks.viz.util.ProgressMonitorThread;


/**
 * <code>FindEntityPath</code> - run find entity path algorithm with ProgressMonitorThread
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                          NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class FindEntityPath {

  private Integer entityKey1;
  private Integer entityKey2;
  private List pathClasses; // element Class
  private boolean doPathExists;
  private int maxPathLength;
  private PwPartialPlan partialPlan;
  private PartialPlanView partialPlanView;
  private MDIInternalFrame dialogWindowFrame;

  private List entityKeyList;
  private Boolean foundPathExists;
  private ProgressMonitorThread findPathPMThread;

  /**
   * <code>FindEntityPath</code> - constructor 
   *
   * @param entityKey1 - <code>Integer</code> - 
   * @param entityKey2 - <code>Integer</code> - 
   * @param pathClasses - <code>List</code> - 
   * @param doPathExists - <code>boolean</code> - 
   * @param maxPathLength - <code>int</code> - 
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   * @param dialogWindowFrame - <code>MDIInternalFrame</code> - 
   */
  public FindEntityPath( Integer entityKey1, Integer entityKey2, List pathClasses,
                         boolean doPathExists, int maxPathLength, PwPartialPlan partialPlan,
                         PartialPlanView partialPlanView, MDIInternalFrame dialogWindowFrame) {
    this.entityKey1 = entityKey1;
    this.entityKey2 = entityKey2;
    this.pathClasses = pathClasses;
    this.doPathExists = doPathExists;
    this.maxPathLength = maxPathLength;
    this.partialPlan = partialPlan;
    this.partialPlanView = partialPlanView;
    this.dialogWindowFrame = dialogWindowFrame;

    entityKeyList = null;
    foundPathExists = null;
  }

  /**
   * <code>getEntityKeyList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getEntityKeyList() {
    return entityKeyList;
  }

  /**
   * <code>setEntityKeyList</code>
   *
   * @param lst - <code>List</code> - 
   */
  public void setEntityKeyList( List lst) {
    // System.err.println( "setEntityKeyList " + lst);
    entityKeyList = lst;
  }

  /**
   * <code>invokeAndWait</code>
   *
   */
  public void invokeAndWait() {
    boolean disableEntityKeyPathDialog = false;
    invokeAndWait( disableEntityKeyPathDialog);
  }

  /**
   * <code>invokeAndWait</code> - for PlanWorksGUITest
   *
   * @param disableEntityKeyPathDialog - <code>boolean</code> - 
   */
  public void invokeAndWait( boolean disableEntityKeyPathDialog) {
    
    launchFindEntityPath();

    if (doPathExists) {
      while (foundPathExists == null) {
        try {
          Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL * 2);
        } catch (InterruptedException ie) {}
        // System.err.println("createFindEntityPathItemWorker wait for findEntityPath");
      }
    } else {
      while (entityKeyList == null) {
        try {
          Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL * 2);
        } catch (InterruptedException ie) {}
        // System.err.println("createFindEntityPathItemWorker wait for findEntityPath");
      }
    }

    StringBuffer strBuf = new StringBuffer( "key=");
    strBuf.append( entityKey1).append( " (");
    strBuf.append( getEntityKeyType( entityKey1, partialPlan)).append( ") => ");
    strBuf.append( "key=").append( entityKey2).append( " (");
    strBuf.append( getEntityKeyType( entityKey2, partialPlan)).append( ")");
    if (doPathExists) {
      if (foundPathExists.booleanValue()) {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(), "path exists for " + strBuf.toString(),
            "Entity Path Exists", JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(), "no path exists for " + strBuf.toString(),
            "Entity Path Existence Failure", JOptionPane.ERROR_MESSAGE);
      }
    } else {
      if (entityKeyList.size() == 0) {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(), "no path found for " + strBuf.toString(),
            "Find Entity Path Error", JOptionPane.ERROR_MESSAGE);
      } else {
        List nodeList = new ArrayList();
        if (partialPlanView instanceof FindEntityPathAdapter) {
          nodeList = ((FindEntityPathAdapter) partialPlanView).renderEntityPathNodes( this);
        } else {
          System.err.println( "FindEntityPath.invokeAndWait: partialPlanView " +
                              partialPlanView + " not handled");
        }
        if (dialogWindowFrame != null) {
          dialogWindowFrame.dispose();
        }

        if (! disableEntityKeyPathDialog) {
          outputEntityPathNodes( nodeList, partialPlanView);
        }
      }
    }
  } // end invokeAndWait

  private void launchFindEntityPath() {
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          String progressLabel = null;
          if (doPathExists) {
            progressLabel = "Determining Path Existence ...";
          } else {
            progressLabel = "Finding Entity Path ...";
          }
          findPathPMThread = partialPlanView.createProgressMonitorThread
            ( progressLabel, 0, 6, Thread.currentThread(), partialPlanView,
              FindEntityPath.this);
          if (! partialPlanView.progressMonitorWait( findPathPMThread, partialPlanView)) {
            System.err.println( "FindEntityPath.progressMonitorWait failed");
            entityKeyList = new ArrayList();
            return null;
          }
          findPathPMThread.getProgressMonitor().setProgress
            ( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);

          if (doPathExists) {
            foundPathExists =
              new Boolean ( partialPlan.pathExists( partialPlan.getEntity( entityKey1),
                                                    entityKey2, pathClasses,
                                                    partialPlanView.getDefaultLinkTypes()));
          } else {
            entityKeyList = partialPlan.getPath( entityKey1, entityKey2, pathClasses,
                                                 partialPlanView.getDefaultLinkTypes(),
                                                 maxPathLength);
          }
          findPathPMThread.setProgressMonitorCancel();
          return null;
        }
      };
    worker.start();  
  } // end launchFindEntityPath

  /**
   * <code>getEntityKeyType</code>
   *
   * @param entityKey - <code>Integer</code> - 
   * @return - <code>String</code> - 
   */
  public static String getEntityKeyType ( Integer entityKey, PwPartialPlan partialPlan) {
    // ResourceTransaction must precede Token
    // Resource & Timeline must precede Object
    if (partialPlan.getResourceTransaction( entityKey) != null) {
      return "ResourceTransaction";
    } else if (partialPlan.getToken( entityKey) != null) {
      return "Token";
    } else if (partialPlan.getVariable( entityKey) != null) {
      return "Variable";
    } else if (partialPlan.getConstraint( entityKey) != null) {
      return "Constraint";
    } else if (partialPlan.getSlot( entityKey) != null) {
      return "Slot";
    } else if (partialPlan.getRuleInstance( entityKey) != null) {
      return "RuleInstance";
    } else if (partialPlan.getResource( entityKey) != null) {
      return "Resource";
    } else if (partialPlan.getTimeline( entityKey) != null) {
      return "Timeline";
    } else if (partialPlan.getObject( entityKey) != null) {
      return "Object";
    } else {
      return "";
    }
  } // end getEntityKeyType

  /**
   * <code>outputEntityPathNodes</code>
   *
   * @param nodeList - <code>List</code> - 
   */
  public static void outputEntityPathNodes( List nodeList, PartialPlanView partialPlanView) {
    System.err.print( "Found Entity Path ");
    StringBuffer nodeBuffer = new StringBuffer( "(");
    nodeBuffer.append( partialPlanView.getName()).append( ") => ");
    Iterator nodeItr = nodeList.iterator();
    while (nodeItr.hasNext()) {
      Integer nodeId = null;
      Object object = (Object) nodeItr.next();
      if (object instanceof VariableContainerNode) {
        nodeId = ((VariableContainerNode) object).getContainer().getId();
      } else if (object instanceof ExtendedBasicNode) {
        ExtendedBasicNode node = (ExtendedBasicNode) object;
        if (node instanceof TokenNetworkTokenNode) {
          nodeId = ((TokenNetworkTokenNode) node).getToken().getId();
        } else if (node instanceof TokenNetworkRuleInstanceNode) {
          nodeId = ((TokenNetworkRuleInstanceNode) node).getRuleInstance().getId();
        } else if (node instanceof VariableNode) {
          nodeId = ((VariableNode) node).getVariable().getId();
        } else if (node instanceof ConstraintNode) {
          nodeId = ((ConstraintNode) node).getConstraint().getId();
        } else if (node instanceof TokenNavNode) {
          nodeId = ((TokenNavNode) node).getToken().getId();
        } else if (node instanceof VariableNavNode) {
          nodeId = ((VariableNavNode) node).getVariable().getId();
        } else if (node instanceof ConstraintNavNode) {
          nodeId = ((ConstraintNavNode) node).getConstraint().getId();
        } else if (node instanceof RuleInstanceNavNode) {
          nodeId = ((RuleInstanceNavNode) node).getRuleInstance().getId();
        } else if (node instanceof SlotNavNode) {
          nodeId = ((SlotNavNode) node).getSlot().getId();
        } else if (node instanceof ModelClassNavNode) {
          nodeId = ((ModelClassNavNode) node).getObject().getId();
        } else if (node instanceof TimelineNavNode) {
          nodeId = ((TimelineNavNode) node).getTimeline().getId();
        } else if (node instanceof ResourceNavNode) {
          nodeId = ((ResourceNavNode) node).getResource().getId();
        } else {
          System.err.println( "FindEntityPath.outputEntityPathNodes: node " +
                              node.getClass().getName() + " not handled");
        }
      }
      nodeBuffer.append( nodeId).append( " ");
    }
    System.err.println( nodeBuffer.toString());
    MessageDialog msgDialog = // non-modal
      new MessageDialog( PlanWorks.getPlanWorks(), "Found Entity Key Path",
                         nodeBuffer.toString());
  } // end outputEntityPathNodes

} // end class FindEntityPath


