// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceTransactionSet.java,v 1.16 2005-06-08 20:58:43 pdaley Exp $
//
// PlanWorks
//
// Will Taylor -- started 04feb04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;


/**
 * <code>ResourceTransactionSet</code> - JGo widget to render a resource's extents

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceTransactionSet extends BasicNode {

  private PwResource resource;
  private int earliestStartTime;
  private int latestStartTime;
  private int earliestEndTime;
  private int latestEndTime;
  private int earliestDurationTime;
  private int latestDurationTime;
  private Color backgroundColor;
  private ResourceTransactionView resourceTransactionView;

  private String nodeLabel;
  private int nodeLabelWidth;
  private String resourceId;
  private int transactionSetYOrigin;
  private int levelScaleWidth;
  private List transactionNodeList;
  private int maxCellRow;

  /**
   * <code>ResourceTransactionSet</code> - constructor 
   *
   * @param resource - <code>PwResource</code> - 
   * @param resourceHorizonEnd - <code>int</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param resourceTransactionView - <code>ResourceTransactionView</code> - 
   */
  public ResourceTransactionSet( final PwResource resource, final int resourceHorizonEnd,
                                 final Color backgroundColor,
                                 final ResourceTransactionView resourceTransactionView) {
    super();
    this.resource = resource;
    earliestStartTime = resourceTransactionView.getTimeScaleStart();
    latestEndTime = resourceHorizonEnd;
    resourceId = resource.getId().toString();
//     System.err.println( "Resource Node: " + resourceId + " eS " +
//                         earliestStartTime + " lE " + latestEndTime);

    this.backgroundColor = backgroundColor;
    this.resourceTransactionView = resourceTransactionView;

    nodeLabel = resource.getName();
    nodeLabelWidth =
      ResourceTransactionSet.getNodeLabelWidth( nodeLabel, resourceTransactionView);
    transactionNodeList = new ArrayList();
    maxCellRow = 0;

    configure();
  } // end constructor


  /**
   * <code>getNodeLabelWidth</code>
   *
   * @param label - <code>String</code> - 
   * @param view - <code>ResourceTransactionView</code> - 
   * @return - <code>int</code> - 
   */
  public static int getNodeLabelWidth( final String label, final ResourceTransactionView view) {
    while (view.getFontMetrics() == null) {
      Thread.yield();
    }
    return SwingUtilities.computeStringWidth( view.getFontMetrics(), label) +
      ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  } // end getNodeLabelWidth


  /**
   * <code>configure</code> - called by ResourceTransactionView.layoutResourceTransactionSets
   *
   */
  public final void configure() {
    // put the label in the LevelScaleView, rather than the ExtentView
    // System.err.println( "currentYLoc " + resourceTransactionView.getCurrentYLoc());
    int currentYLoc = resourceTransactionView.getCurrentYLoc();
    transactionSetYOrigin = currentYLoc;
    levelScaleWidth = resourceTransactionView.getLevelScaleViewWidth() -
      ViewConstants.RESOURCE_LEVEL_SCALE_WIDTH_OFFSET;
    resourceTransactionView.renderBordersUpper
      ( resource,
        resourceTransactionView.getJGoRulerView().scaleTimeNoZoom( earliestStartTime),
        resourceTransactionView.getJGoRulerView().scaleTimeNoZoom( latestEndTime),
        currentYLoc, resourceTransactionView.getJGoExtentDocument());
    resourceTransactionView.renderBordersUpper
      ( resource, 0, levelScaleWidth, currentYLoc,
        resourceTransactionView.getJGoLevelScaleDocument());
    resourceTransactionView.renderResourceName
      ( resource, resourceTransactionView.getLevelScaleViewWidth() - nodeLabelWidth,
        currentYLoc, resourceTransactionView.getJGoLevelScaleDocument(),
        resourceTransactionView);

    currentYLoc = currentYLoc + ViewConstants.RESOURCE_PROFILE_MAX_Y_OFFSET +
      ResourceView.Y_MARGIN;
    resourceTransactionView.setCurrentYLoc( currentYLoc);

    renderTransactions( currentYLoc);

    currentYLoc += ResourceTransactionSet.scaleY( maxCellRow + 1, 0);

    resourceTransactionView.renderBordersLower
      ( resource,
        resourceTransactionView.getJGoRulerView().scaleTimeNoZoom( earliestStartTime),
        resourceTransactionView.getJGoRulerView().scaleTimeNoZoom( latestEndTime),
        currentYLoc, resourceTransactionView.getJGoExtentDocument());
    resourceTransactionView.renderBordersLower
      ( resource, 0, levelScaleWidth, currentYLoc,
        resourceTransactionView.getJGoLevelScaleDocument());

    currentYLoc += ViewConstants.RESOURCE_PROFILE_MIN_Y_OFFSET;
    resourceTransactionView.setCurrentYLoc( currentYLoc);
  } // end configure


  /**
   * <code>getResource</code>
   *
   * @return - <code>PwResource</code> - 
   */
  public final PwResource getResource() {
    return resource;
  }

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getName() {
    return resource.getName();
  }

  /**
   * <code>getTransactionNodeList</code>
   *
   * @return - <code>List</code> - 
   */
  public final List getTransactionNodeList() {
    return transactionNodeList;
  }

  /**
   * <code>getTransactionSetYOrigin</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getTransactionSetYOrigin() {
    return transactionSetYOrigin;
  }

  private void renderTransactions( int currentYLoc) {
    List transactionSet = resource.getTransactionSet();
    Iterator transSetItr = transactionSet.iterator();
    while (transSetItr.hasNext()) {
      Object t = transSetItr.next();
      if (! (t instanceof PwResourceTransaction)) {
        continue;
      }
      PwResourceTransaction transaction = (PwResourceTransaction) t;//transSetItr.next();
      PwIntervalDomain transInterval = transaction.getInterval();
      int transStart = transInterval.getLowerBoundInt();
      String transIdString = transaction.getId().toString();
      checkIntervalForInfinity( transIdString, transStart);
      int transEnd = transInterval.getUpperBoundInt();
      checkIntervalForInfinity( transIdString, transEnd);
      int transStartX =
        resourceTransactionView.getJGoRulerView().scaleTimeNoZoom( transStart);
      int transEndX =
        resourceTransactionView.getJGoRulerView().scaleTimeNoZoom( transEnd);
//       System.err.println( "transId = " + transIdString + " start " + transStart +
//                           " end " + transEnd + " transStartX " + transStartX +
//                           " transEndX " + transEndX);
      int yDelta = ViewConstants.RESOURCE_TRANSACTION_HEIGHT;
      // force min width to 2, so that toolTip will show up
      ResourceTransactionNode transactionNode =
        new ResourceTransactionNode( transaction,
                                     new Point( transStartX, currentYLoc),
                                     new Dimension( Math.max( transEndX - transStartX, 2),
                                                    yDelta), resourceTransactionView, this);
      transactionNode.setResizable( false); transactionNode.setDraggable( false);
      // allow Mouse-Right menu
      // transactionNode.setSelectable( false);
      resourceTransactionView.getJGoExtentDocument().addObjectAtTail( transactionNode);
      transactionNodeList.add( transactionNode);
    }

    layoutTransactionNodes();

  } // end renderTransactions

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public final String toString() {
    return resource.getId().toString();
  }

  /**
   * <code>equals</code>
   *
   * @param other - <code>ResourceTransactionSet</code> - 
   * @return - <code>boolean</code> - 
   */
  public final boolean equals( final ResourceTransactionSet other) {
    return resource.getId().equals( other.getResource().getId());
  }

  /**
   * <code>hashCode</code>
   *
   * @return - <code>int</code> - 
   */
  public final int hashCode() {
    return resource.getId().intValue();
  }

  /**
   * <code>scaleY</code>
   *
   * @param cellRow - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public static int scaleY( int cellRow, int yOrigin) {
    return (yOrigin + (int) (cellRow * (ViewConstants.RESOURCE_TRANSACTION_HEIGHT +
                                        ResourceView.Y_MARGIN)));
  }

  private void layoutTransactionNodes() {
    /*List extents = new ArrayList();
    Iterator temporalNodeIterator = temporalNodeList.iterator();
    while (temporalNodeIterator.hasNext()) {
      TemporalNode temporalNode = (TemporalNode) temporalNodeIterator.next();
      extents.add( temporalNode);
      }*/
    List extents = new ArrayList( transactionNodeList);
    // do the layout -- compute cellRow for each node
    List results =
      Algorithms.allocateRows
      ( resourceTransactionView.getJGoRulerView().
        scaleTimeNoZoom( (double) resourceTransactionView.getTimeScaleStart()),
        resourceTransactionView.getJGoRulerView().
        scaleTimeNoZoom( (double) resourceTransactionView.getTimeScaleEnd()),extents);
//     List results =
//       Algorithms.betterAllocateRows
//       ( resourceTransactionView.getJGoRulerView().
//         scaleTimeNoZoom( (double) resourceTransactionView.getTimeScaleStart()),
//         resourceTransactionView.getJGoRulerView().
//         scaleTimeNoZoom( (double) resourceTransactionView.getTimeScaleEnd()), extents);
    if (transactionNodeList.size() != results.size()) {
      String message = String.valueOf( transactionNodeList.size() - results.size()) +
        " nodes not successfully allocated";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Resource Transaction View Layout Exception",
                                     JOptionPane.ERROR_MESSAGE);
      return;
    }
    for (Iterator it = extents.iterator(); it.hasNext();) {
      ResourceTransactionNode transactionNode = (ResourceTransactionNode) it.next();
//       System.err.println( "id  " + transactionNode.getTransaction().getId().toString() +
//                           " cellRow " + transactionNode.getRow());
      if (transactionNode.getRow() > maxCellRow) {
        maxCellRow = transactionNode.getRow();
      }
      // render the node
      transactionNode.configure();
    }
//     System.err.println( "maxCellRow " + maxCellRow);
  } // end  layoutTransactionNodes

  private void checkIntervalForInfinity( String transIdString, int time) {
    if (time == DbConstants.MINUS_INFINITY_INT) {
      System.err.println( "ResourceTransactionSet transId = " + transIdString +
                          "; value = " + DbConstants.MINUS_INFINITY);
    }
    if (time == DbConstants.PLUS_INFINITY_INT) {
      System.err.println( "ResourceTransactionSet transId = " + transIdString +
                          "; value = " + DbConstants.PLUS_INFINITY);
    }
  } // end checkIntervalForInfinity


} // end class ResourceTransactionSet


  
  
