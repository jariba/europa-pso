// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.4 2003-10-16 21:40:40 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
// import gov.nasa.arc.planworks.util.Utilities;
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
  protected void expandViewFrame( String viewClassName, int maxViewWidth, int maxViewHeight) {
    MDIInternalFrame viewFrame = viewSet.openView( viewClassName);
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


} // end class VizView

