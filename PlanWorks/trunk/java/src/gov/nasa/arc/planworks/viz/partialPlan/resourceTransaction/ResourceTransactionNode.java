// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceTransactionNode.java,v 1.1 2004-03-07 01:49:29 taylor Exp $
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
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TransactionObject</code> - render resource transaction as a rectangle
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public  class ResourceTransactionNode extends JGoRectangle {  

    private PwResourceTransaction transaction;
    private ResourceView resourceView;

    /**
     * <code>ResourceTransactionNode</code> - constructor 
     *
     * @param transaction - <code>PwResourceTransaction</code> - 
     * @param location - <code>Point</code> - 
     * @param size - <code>Dimension</code> - 
     */
    public ResourceTransactionNode( PwResourceTransaction transaction, Point location,
                              Dimension size, ResourceView resourceView) {
      super( location, size);
      this.transaction = transaction;
      this.resourceView = resourceView;
    }

    /**
     * <code>getTransaction</code>
     *
     * @return - <code>PwResourceTransaction</code> - 
     */
    public PwResourceTransaction getTransaction() {
      return transaction;
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
   *
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
            MDIInternalFrame navigatorFrame = resourceView.openNavigatorViewFrame();
            Container contentPane = navigatorFrame.getContentPane();
            PwPartialPlan partialPlan = resourceView.getPartialPlan();
            contentPane.add( new NavigatorView( ResourceTransactionNode.this, partialPlan,
                                                resourceView.getViewSet(),
                                                navigatorFrame));
          }
        });
      mouseRightPopup.add( navigatorItem);

      NodeGenerics.showPopupMenu( mouseRightPopup, resourceView, viewCoords);
    } // end mouseRightPopupMenu


  } // end class ResourceTransactionNode
