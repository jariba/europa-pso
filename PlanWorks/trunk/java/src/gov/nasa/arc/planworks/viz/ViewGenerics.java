// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ViewGenerics.java,v 1.12 2004-04-09 23:11:25 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 14nov03
//

package gov.nasa.arc.planworks.viz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.Overview;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>ViewGenerics</code> - generic static methods for view operations
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ViewGenerics {

  public static final String VIEW_TITLE = "View for ";
  public static final String OVERVIEW_TITLE = "Overview for ";
  public static final String RULE_VIEW_TITLE = "RuleView for ";

  private static final int RULE_VIEW_WIDTH = 400;
  private static final int RULE_VIEW_HEIGHT = 100;
  private static final ViewGenerics generics = new ViewGenerics();

  private ViewGenerics() {
  }

  /**
   * <code>partialPlanViewsPopupMenu</code> - open/hide/close views or open a particular one
   *
   * @param stepNumber - <code>int</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public static void partialPlanViewsPopupMenu( int stepNumber, PwPlanningSequence planSequence,
                                                VizView vizView, Point viewCoords) {
    JPopupMenu mouseRightPopup = new PartialPlanViewMenu();
    String partialPlanName = "step" + String.valueOf( stepNumber);
    JMenuItem header = new JMenuItem( partialPlanName);
    mouseRightPopup.add( header);
    mouseRightPopup.addSeparator();

    ((PartialPlanViewMenu) mouseRightPopup).
      buildPartialPlanViewMenu( partialPlanName, planSequence);
    PwPartialPlan partialPlanIfLoaded = null;
    try {
      partialPlanIfLoaded = planSequence.getPartialPlanIfLoaded( partialPlanName);
    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      // rnfExcep.printStackTrace();
      return;
    }
    vizView.createAllViewItems( partialPlanIfLoaded, partialPlanName,
                                planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, vizView, viewCoords);

  } // end partialPlanViewsPopupMenu

  /**
   * <code>openOverviewFrame</code>
   *
   * @param viewName - <code>String</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param jGoView - <code>JGoView</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @return - <code>VizViewOverview</code> - 
   */
  public static VizViewOverview openOverviewFrame( String viewName, ViewableObject viewable,
                                                   VizView vizView, ViewSet viewSet,
                                                   JGoView jGoView, Point viewCoords) {
    String overviewTitle = Utilities.trimView( viewName).replaceAll( " ", "") +
      OVERVIEW_TITLE + viewable.getName();
    // System.err.println( "openOverviewFrame( String, " + overviewTitle);
    return openOverviewFrameCommon( overviewTitle, viewable, vizView, viewSet, jGoView,
                                     viewCoords);
  } // openOverviewFrame( String, ...

  /**
   * <code>openOverviewFrame</code> - for Navigator View, suffixed (n) on frame title
   *
   * @param viewFrame - <code>MDIInternalFrame</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param jGoView - <code>JGoView</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @return - <code>VizViewOverview</code> - 
   */
  public static VizViewOverview openOverviewFrame( MDIInternalFrame viewFrame,
                                                   ViewableObject viewable,
                                                   VizView vizView, ViewSet viewSet,
                                                   JGoView jGoView, Point viewCoords) {
    String overviewTitle = viewFrame.getTitle();
    overviewTitle = overviewTitle.replaceAll( VIEW_TITLE, OVERVIEW_TITLE);
    // System.err.println( "openOverviewFrame( MDIInternalFrame, " + overviewTitle);
    return openOverviewFrameCommon( overviewTitle, viewable, vizView, viewSet, jGoView,
                                     viewCoords);
  } // openOverviewFrame( MDIInternalFrame, ...

  /**
   * <code>openOverviewFrameCommon</code>
   *
   * @param overviewTitle - <code>String</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param jGoView - <code>JGoView</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @return - <code>VizViewOverview</code> - 
   */
  public static VizViewOverview openOverviewFrameCommon( String overviewTitle,
                                                         ViewableObject viewable,
                                                         VizView vizView, ViewSet viewSet,
                                                         JGoView jGoView, Point viewCoords) {
    MDIInternalFrame overviewFrame = (MDIInternalFrame) viewSet.getView( overviewTitle);
    VizViewOverview overview = null;
    // System.err.println( "openOverviewFrame " + overviewFrame);
    if (overviewFrame == null) {
      overviewFrame = viewSet.getDesktopFrame().createFrame( overviewTitle, viewSet,
                                                             true, true, true, true);
      viewSet.getViews().put( overviewTitle, overviewFrame);
      // System.err.println( "views " + viewSet.getViews());
      Container contentPane = overviewFrame.getContentPane();

      overview = new VizViewOverview( overviewTitle, vizView);
      vizView.setOverview( overview);
      overview.setObserved( jGoView);
      // do not allow drag & drop copying 
      jGoView.setInternalMouseActions( DnDConstants.ACTION_MOVE);
      overview.setInternalMouseActions( DnDConstants.ACTION_MOVE);
      jGoView.setDragDropEnabled( false);
      overview.setDragDropEnabled( false);
      overview.validate();
      overview.setVisible( true);
      contentPane.add( overview);

      overviewFrame.setLocation( viewCoords);
      Dimension overviewDocSize = new Dimension( overview.getDocumentSize());
      overview.convertDocToView( overviewDocSize);
//       vizView.expandViewFrame( overviewFrame,
//                                (int) (overviewDocSize.getWidth() +
//                                       (ViewConstants.MDI_FRAME_DECORATION_WIDTH * 0.8)),
//                                (int) (overviewDocSize.getHeight() +
//                                       (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 0.6)));
      vizView.expandViewFrame( overviewFrame,
                               (int) overviewDocSize.getWidth(),
                               (int) overviewDocSize.getHeight());
    }

    raiseFrame( overviewFrame);
    
    return overview;
  } // end openOverviewFrameCommon

  /**
   * <code>raiseFrame</code>
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   */
  public static void raiseFrame( MDIInternalFrame frame) {
    // make window appear & bring to the front
    try {
      // in case view existed and was iconified
      if (frame.isIcon()) {
        frame.setIcon( false);
      }
      frame.setSelected( false);
      frame.setSelected( true);
    } catch (PropertyVetoException excp) {
    }
  } // end raiseFrame

  /**
   * <code>setRedrawCursor</code>
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public static void setRedrawCursor( MDIInternalFrame frame) {
    ((Component) frame.getRootPane()).setCursor
      ( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR));
  } // end setRedrawCursor

  /**
   * <code>resetRedrawCursor</code>
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   */
  public static void resetRedrawCursor( MDIInternalFrame frame) {
    ((Component) frame.getRootPane()).setCursor
      ( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR));
  } // end resetRedrawCursor

  /**
   * <code>openRuleViewFrame</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @param tokenNetworkView - <code>TokenNetworkView</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @return - <code>VizViewRuleView</code> - 
   */
  public static VizViewRuleView openRuleViewFrame( PwPartialPlan partialPlan,
                                                   TokenNetworkView tokenNetworkView,
                                                   ViewSet viewSet, Point viewCoords) {
    String ruleViewTitle = RULE_VIEW_TITLE + partialPlan.getName();
    MDIInternalFrame ruleViewFrame = (MDIInternalFrame) viewSet.getView( ruleViewTitle);
    VizViewRuleView ruleView = null;
    // System.err.println( "openRuleViewFrame " + ruleViewFrame);
    if (ruleViewFrame == null) {
      ruleViewFrame = viewSet.getDesktopFrame().createFrame( ruleViewTitle, viewSet,
                                                             true, true, true, true);
      viewSet.getViews().put( ruleViewTitle, ruleViewFrame);
      // System.err.println( "views " + viewSet.getViews());
      Container contentPane = ruleViewFrame.getContentPane();
      ruleView = new VizViewRuleView( ruleViewTitle, tokenNetworkView);
      ruleView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
      tokenNetworkView.setRuleView( ruleView);
      ruleView.validate();
      ruleView.setVisible( true);
      contentPane.add( ruleView);

      ruleViewFrame.setLocation( viewCoords);
      ruleView.getDocument().setDocumentSize( RULE_VIEW_WIDTH, RULE_VIEW_HEIGHT);
      Dimension ruleViewDocSize = new Dimension( ruleView.getDocument().getDocumentSize());
      ruleView.convertDocToView( ruleViewDocSize);
      tokenNetworkView.expandViewFrame( ruleViewFrame,
                                        (int) ruleViewDocSize.getWidth(),
                                        (int) ruleViewDocSize.getHeight());
    }

    raiseFrame( ruleViewFrame);
    
    return ruleView;
  } // end openRuleViewFrame




  class TemporalExtentViewFinder implements BooleanFunctor {
    public TemporalExtentViewFinder(){}
    public final boolean func(Object o){return (o instanceof TemporalExtentView);}
  }

  /**
   * <code>getTemporalExtentView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>TemporalExtentView</code> - 
   */
  public static TemporalExtentView getTemporalExtentView( MDIInternalFrame frame) {
    return generics._getTemporalExtentView(frame);
  } // end getTemporalExtentView

  private final TemporalExtentView _getTemporalExtentView(MDIInternalFrame frame) {
    return (TemporalExtentView) CollectionUtils.findFirst(new TemporalExtentViewFinder(),
                                                          frame.getContentPane().getComponents());
  }

} // end class ViewGenerics 

