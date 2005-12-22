// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceTransactionNode.java,v 1.7 2004-06-10 01:36:05 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 006march04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>TransactionObject</code> - render resource transaction as a rectangle
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public  class ResourceTransactionNode extends JGoRectangle implements Extent, OverviewToolTip {  

  private PwResourceTransaction transaction;
  private Point location;
  private Dimension size;
  private ResourceView resourceView;
  private ResourceTransactionSet resourceTransactionSet;
  private int cellRow; // for layout algorithm

  /**
   * <code>ResourceTransactionNode</code> - constructor 
   *
   * @param transaction - <code>PwResourceTransaction</code> - 
   * @param location - <code>Point</code> - 
   * @param size - <code>Dimension</code> - 
   */
  public ResourceTransactionNode( PwResourceTransaction transaction, Point location,
                                  Dimension size, ResourceView resourceView,
                                  ResourceTransactionSet resourceTransactionSet) {
    super( location, size);
    this.transaction = transaction;
    this.location = location;
    this.size = size;
    this.resourceView = resourceView;
    this.resourceTransactionSet = resourceTransactionSet;
  }

  // called by ResourceTransactionSet.layoutTransactionNodes
  public void configure() {
    Point layoutLocation =
      new Point( (int) this.getLocation().getX(),
                 ResourceTransactionSet.scaleY
                 ( cellRow, ((ResourceTransactionView) resourceView).getCurrentYLoc()));
    this.setLocation( layoutLocation);
  } // configure

  /**
   * <code>getTransaction</code>
   *
   * @return - <code>PwResourceTransaction</code> - 
   */
  public PwResourceTransaction getTransaction() {
    return transaction;
  }

  /**
   * <code>getStart</code> - implements Extent
   *
   * @return - <code>int</code> - 
   */
  public int getStart() {
    int xStart = (int) location.getX();
    return xStart - ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  }

  /**
   * <code>getEnd</code> - implements Extent
   *
   * @return - <code>int</code> - 
   */
  public int getEnd() {
    int xEnd = (int) (location.getX() + size.getWidth());
    return xEnd + ViewConstants.TIMELINE_VIEW_INSET_SIZE;
  }

  /**
   * <code>getRow</code> - implements Extent
   *
   * @return - <code>int</code> - 
   */
  public int getRow() {
    return cellRow;
  }

  /**
   * <code>setRow</code> - implements Extent
   *
   * @param row - <code>int</code> - 
   */
  public void setRow( int row) {
    cellRow = row;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>");
    tip.append( transaction.getInterval().toString());
    tip.append( ": ");
    double maxDelta = transaction.getQuantityMax();
    if (maxDelta > 0) {
      tip.append( "+");
    }
    tip.append( new Double( maxDelta).toString());
    tip.append( ", ");
    double minDelta = transaction.getQuantityMin();
    if (minDelta > 0) {
      tip.append( "+");
    }
    tip.append( new Double( minDelta).toString());
    tip.append( "<br>key = ");
    tip.append( transaction.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview resource node
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( "transaction key=");
    tip.append( transaction.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

    /**
     * <code>doMouseClick</code> - 
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     * @param view - <code>JGoView</code> - 
     * @return - <code>boolean</code> - 
     */
  public final boolean doMouseClick( final int modifiers, final Point docCoords,
                                     final Point viewCoords, final JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    ResourceTransactionNode resourceTransactionNode =
      (ResourceTransactionNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( final Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          String viewSetKey = resourceView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame = resourceView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = resourceView.getPartialPlan();
          contentPane.add( new NavigatorView( ResourceTransactionNode.this.getTransaction(),
                                              partialPlan, resourceView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken = (PwToken) ResourceTransactionNode.this.getTransaction();
          ((PartialPlanViewSet) resourceView.getViewSet()).setActiveToken( activeToken);
          ((PartialPlanViewSet) resourceView.getViewSet()).setSecondaryTokens( null);
          System.err.println( "ResourceTransactionNode setActiveToken: " +
                              activeToken.getPredicateName() +
                              " (key=" + activeToken.getId().toString() + ")");
        }
      });
    mouseRightPopup.add( activeTokenItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, resourceView, viewCoords);
  } // end mouseRightPopupMenu


} // end class ResourceTransactionNode
