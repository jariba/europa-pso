// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ViewGenerics.java,v 1.7 2004-02-03 20:43:52 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 14nov03
//

package gov.nasa.arc.planworks.viz;

import java.awt.BorderLayout;
import java.awt.Container;
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
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
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

  public static final String OVERVIEW_TITLE = "Overview for ";

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
      rnfExcep.printStackTrace();
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
    MDIInternalFrame overviewFrame = (MDIInternalFrame) viewSet.getViews().get( overviewTitle);
    VizViewOverview overview = null;
    // System.err.println( "openOverviewFrame " + overviewFrame);
    if (overviewFrame == null) {
      overviewFrame = viewSet.getDesktopFrame().createFrame( overviewTitle, viewSet,
                                                             true, true, true, true);
      viewSet.getViews().put( overviewTitle, overviewFrame);
      // System.err.println( "views " + viewSet.getViews());
      Container contentPane = overviewFrame.getContentPane();

      overview = new VizViewOverview( overviewTitle, vizView);
      vizView.setOverview(overview);
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

    raiseOverviewFrame( overviewFrame);
    
    return overview;
  } // end openOverviewFrame


  /**
   * <code>openExistingOverviewFrame</code>
   *
   * @param viewName - <code>String</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public static void openExistingOverviewFrame( String viewName, ViewableObject viewable,
                                                ViewSet viewSet) {
    String overviewTitle = Utilities.trimView( viewName).replaceAll( " ", "") +
      OVERVIEW_TITLE + viewable.getName();
    // System.err.println( "overviewTitle " + overviewTitle);
    // System.err.println( "views " + viewSet.getViews());
    MDIInternalFrame overviewFrame = (MDIInternalFrame) viewSet.getViews().get( overviewTitle);
    // System.err.println( "openOverviewFrame " + overviewFrame);
    if (overviewFrame != null) {
      raiseOverviewFrame( overviewFrame);
    }
  } // end openExistingOverviewFrame

  private static void raiseOverviewFrame( MDIInternalFrame overviewFrame) {
    // make window appear & bring to the front
    try {
      // in case view existed and was iconified
      if (overviewFrame.isIcon()) {
        overviewFrame.setIcon( false);
      }
      overviewFrame.setSelected( false);
      overviewFrame.setSelected( true);
    } catch (PropertyVetoException excp) {
    }
  } // end raiseOverviewFrame


  /**
   * <code>getTemporalExtentView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>TemporalExtentView</code> - 
   */
  public static TemporalExtentView getTemporalExtentView( MDIInternalFrame frame) {
    Container contentPane = frame.getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      //       System.err.println( "i " + i + " " +
      //                           contentPane.getComponent( i).getClass().getName());
      if (contentPane.getComponent(i) instanceof TemporalExtentView) {
        return (TemporalExtentView) contentPane.getComponent(i);
      }
    }
    return null;
  } // end getTemporalExtentView

} // end class ViewGenerics 

