// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ResourceNameNode.java,v 1.6 2004-05-28 20:21:17 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 04feb04
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.ResourceView;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>ResourceNameNode</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceNameNode extends JGoText {

  private PwResource resource;
  private ResourceView resourceView;


  public ResourceNameNode( final Point nameLoc, final PwResource resource,
                           ResourceView resourceView) {
    super( nameLoc, resource.getName());
    this.resource = resource;
    this.resourceView = resourceView;
  }

  /**
   * <code>getResource</code>
   *
   * @return - <code>PwResource</code> - 
   */
  public final PwResource getResource() {
    return resource;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getToolTipText() {
    StringBuffer tip = new StringBuffer( "<html>key = ");
    tip.append( resource.getId().toString());
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
//     System.err.println( "doMouseClick obj class " +
//                         obj.getTopLevelObject().getClass().getName());
    ResourceNameNode resourceNameNode = (ResourceNameNode) obj.getTopLevelObject();
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
          contentPane.add( new NavigatorView( ResourceNameNode.this.getResource(),
                                              partialPlan, resourceView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    JMenuItem activeResourceItem = new JMenuItem( "Set Active Resource");
    activeResourceItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          ((PartialPlanViewSet) resourceView.getViewSet()).setActiveResource( resource);
          System.err.println( "ResourceNameNode setActiveResource: " + resource.getName() +
                              " (key=" + resource.getId().toString() + ")");
        }
      });
    mouseRightPopup.add( activeResourceItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, resourceView, viewCoords);
  } // end mouseRightPopupMenu



} // end class ResourceNameNode
