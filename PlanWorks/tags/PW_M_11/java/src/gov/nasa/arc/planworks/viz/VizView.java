// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.8 2003-12-11 22:25:08 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.partialPlan.CreatePartialPlanViewThread;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenuItem;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>VizView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizView extends JPanel {

  protected ViewSet viewSet;
  protected Font font;
  protected FontMetrics fontMetrics;
  protected VizViewOverview overview;

  /**
   * <code>VizView</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public VizView( ViewSet viewSet) {
    super();
    this.viewSet = viewSet;

    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    fontMetrics = null;  // see computeFontMetrics
    overview = null;
   
    JGoText.setDefaultFontFaceName( "Monospaced");
    JGoText.setDefaultFontSize( ViewConstants.TIMELINE_VIEW_FONT_SIZE);

    // Utilities.printFontNames();
  }

  /**
   * <code>getViewSet</code>
   *
   * @return - <code>ViewSet</code> - 
   */
  public ViewSet getViewSet() {
    return viewSet;
  }

  /**
   * <code>getOverview</code>
   *
   * @return - <code>VizViewOverview</code> - 
   */
  public VizViewOverview getOverview() {
    return overview;
  }

  /**
   * <code>setOverview</code>
   *
   * @param overview - <code>VizViewOverview</code> - 
   */
  public void setOverview( VizViewOverview overview) {
    this.overview = overview;
  }

  /**
   * <code>redraw</code> - each subclass of VizView will implement 
   *
   */  
  public void redraw() {
  }


  /**
   * <code>isContentSpecRendered</code> - each subclass of VizView will implement
   *
   * @param viewName - <code>String</code> - 
   * @param showDialog - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  protected boolean isContentSpecRendered( String viewName, boolean showDialog) {
    return true;
  } // end isContentSpecRendered

  /**
   * <code>expandViewFrame</code> - expand up to size of PlanWorks frame
   *
   * @param viewClassName - <code>String</code> - 
   * @param maxViewWidth - <code>int</code> - 
   * @param maxViewHeight - <code>int</code> - 
   */
  protected void expandViewFrame( MDIInternalFrame viewFrame, int maxViewWidth,
                                  int maxViewHeight) {
    maxViewWidth = Math.min( maxViewWidth, 
                             (int) PlanWorks.planWorks.getSize().getWidth() -
                             (int) viewFrame.getLocation().getX() -
                             ViewConstants.MDI_FRAME_DECORATION_WIDTH -
                             ViewConstants.FRAME_DECORATION_WIDTH); 
    maxViewHeight = Math.min( maxViewHeight, 
                              (int) PlanWorks.planWorks.getSize().getHeight() -
                              (int) viewFrame.getLocation().getY() -
                              ViewConstants.MDI_FRAME_DECORATION_HEIGHT -
                              ViewConstants.FRAME_DECORATION_HEIGHT); 
//     maxViewWidth = Math.min( Math.max( maxViewWidth, PlanWorks.INTERNAL_FRAME_WIDTH),
//                              (int) PlanWorks.planWorks.getSize().getWidth() -
//                              (int) viewFrame.getLocation().getX() -
//                              ViewConstants.MDI_FRAME_DECORATION_WIDTH -
//                              ViewConstants.FRAME_DECORATION_WIDTH); 
//     maxViewHeight = Math.min( Math.max( maxViewHeight, PlanWorks.INTERNAL_FRAME_HEIGHT),
//                               (int) PlanWorks.planWorks.getSize().getHeight() -
//                               (int) viewFrame.getLocation().getY() -
//                               ViewConstants.MDI_FRAME_DECORATION_HEIGHT -
//                               ViewConstants.FRAME_DECORATION_HEIGHT); 
    viewFrame.setSize( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
                       maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
  } // end expandViewFrame


  /**
   * <code>getFontMetrics</code> - called in "leaf" view class's init method
   *                       view must be displayable, before graphics is non-null
   *
   * @param view - <code>VizView</code> - 
   * @return - <code>FontMetrics</code> - 
   */
  protected void computeFontMetrics( VizView view) {
    Graphics graphics = ((JPanel) view).getGraphics();
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();
  } // end getFontMetrics


  /**
   * <code>getFontMetrics</code> - public so that node classes can have access
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }

  /**
   * <code>createAllViewItems</code>
   *
   * @param partialPlanIfLoaded - <code>PwPartialPlan</code> - StepElement may pass null
   * @param partialPlanName - <code>String</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public void createAllViewItems( PwPartialPlan partialPlanIfLoaded,
                                  String partialPlanName,
                                  PwPlanningSequence planSequence,
                                  JPopupMenu mouseRightPopup) { 
    mouseRightPopup.addSeparator();
    if (partialPlanIfLoaded != null) {
      PartialPlanViewSet partialPlanViewSet =
        (PartialPlanViewSet) viewSet.getViewManager().getViewSet( partialPlanIfLoaded);
      if (partialPlanViewSet != null) {
        JMenuItem closeAllItem = new JMenuItem( "Close All Views");
        createCloseAllItem( closeAllItem, partialPlanViewSet);
        mouseRightPopup.add( closeAllItem);

        JMenuItem hideAllItem = new JMenuItem( "Hide All Views");
        createHideAllItem( hideAllItem, partialPlanViewSet);
        mouseRightPopup.add( hideAllItem);
      }
    }

    JMenuItem openAllItem = new JMenuItem( "Open All Views");
    createOpenAllItem( openAllItem, partialPlanIfLoaded, partialPlanName, planSequence);
    mouseRightPopup.add( openAllItem);
  } // end createAllViewItems

  private void createCloseAllItem( JMenuItem closeAllItem,
                                   final PartialPlanViewSet partialPlanViewSet) {
    closeAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          partialPlanViewSet.close();
        }
      });
  } // end createCloseAllItem
 
  private void createHideAllItem( JMenuItem hideAllItem,
                                  final PartialPlanViewSet partialPlanViewSet) {
    hideAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          partialPlanViewSet.iconify();
        }
      });
  } // end createHideAllItem
 
  private void createOpenAllItem( JMenuItem openAllItem,
                                  final PwPartialPlan partialPlanIfLoaded,
                                  final String partialPlanName,
                                  final PwPlanningSequence planSequence) {
    openAllItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String seqUrl = planSequence.getUrl();
          String seqName = planSequence.getName();
          boolean isInvokeAndWait = true;
          Iterator viewListItr = PlanWorks.PARTIAL_PLAN_VIEW_LIST.iterator();
          while (viewListItr.hasNext()) {
            final String viewName = (String) viewListItr.next();
            final PartialPlanViewMenuItem viewItem =
              new PartialPlanViewMenuItem( viewName, seqUrl, seqName, partialPlanName);
            Thread thread = new CreatePartialPlanViewThread( viewName, viewItem, isInvokeAndWait);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
          }
        }
      });
  } // end createOpenAllItem

} // end class VizView

