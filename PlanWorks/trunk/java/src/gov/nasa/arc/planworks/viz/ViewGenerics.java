// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ViewGenerics.java,v 1.19 2004-06-10 01:35:59 taylor Exp $
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
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.rule.RuleInstanceView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
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

  private static final int RULE_VIEW_WIDTH = 400;
  private static final int RULE_VIEW_HEIGHT = 100;
  private static final ViewGenerics generics = new ViewGenerics();

  private ViewGenerics() {
  }

  /**
   * <code>partialPlanViewsPopupMenu</code> - open/hide/close all views or
   *                                          open a particular one
   *
   * @param stepNumber - <code>int</code> - 
   * @param planSequence - <code>PwPlanningSequence</code> - 
   * @param vizView - <code>VizView</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public static void partialPlanViewsPopupMenu( int stepNumber,
                                                PwPlanningSequence planSequence,
                                                VizView vizView, Point viewCoords,
                                                ViewListener viewListener) {
    JPopupMenu mouseRightPopup = new PartialPlanViewMenu();
    String partialPlanName = "step" + String.valueOf( stepNumber);
    JMenuItem header = new JMenuItem( partialPlanName);
    mouseRightPopup.add( header);
    mouseRightPopup.addSeparator();

    ((PartialPlanViewMenu) mouseRightPopup).
      buildPartialPlanViewMenu( partialPlanName, planSequence, viewListener);
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

    ViewGenerics.showPopupMenu( mouseRightPopup, vizView, viewCoords);

  } // end partialPlanViewsPopupMenu

  /**
   * <code>showPopupMenu</code> - show pop up menu in component at viewCoords location
   *
   * @param popupMenu - <code>JPopupMenu</code> - 
   * @param component - <code>JComponent</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public static void showPopupMenu( JPopupMenu popupMenu, JComponent component,
                                    Point viewCoords) {
    boolean isLocationAbsolute = false;
    Point popupPoint = Utilities.computeNestedLocation( viewCoords, component,
                                                        isLocationAbsolute);
    popupMenu.show( PlanWorks.getPlanWorks(), (int) popupPoint.getX(),
                    (int) popupPoint.getY());
  } // end showPopupMenu

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
      ViewConstants.OVERVIEW_TITLE + viewable.getName();
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
    overviewTitle = overviewTitle.replaceAll( ViewConstants.VIEW_TITLE,
                                              ViewConstants.OVERVIEW_TITLE);
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
      // for PWTestHelper.findComponentByName
      overview.setName( overviewTitle);
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
   */
  public static void setRedrawCursor( MDIInternalFrame frame) {
    ((Component) frame.getRootPane()).setCursor
      ( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR));
  } // end setRedrawCursor

  /**
   * <code>setRedrawCursor</code>
   *
   * @param frame - <code>MDIDesktopFrame</code> - 
   */
  public static void setRedrawCursor( MDIDesktopFrame frame) {
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
   * <code>resetRedrawCursor</code>
   *
   * @param frame - <code>MDIDesktopFrame</code> - 
   */
  public static void resetRedrawCursor( MDIDesktopFrame frame) {
    ((Component) frame.getRootPane()).setCursor
      ( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR));
  } // end resetRedrawCursor


  class ConstraintNetworkViewFinder implements BooleanFunctor {
    public ConstraintNetworkViewFinder(){}
    public final boolean func(Object o){return (o instanceof ConstraintNetworkView);}
  }

  /**
   * <code>getConstraintNetworkView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>ConstraintNetworkView</code> - 
   */
  public static ConstraintNetworkView getConstraintNetworkView( MDIInternalFrame frame) {
    return generics._getConstraintNetworkView(frame);
  } // end getConstraintNetworkView

  private final ConstraintNetworkView _getConstraintNetworkView(MDIInternalFrame frame) {
    return (ConstraintNetworkView) CollectionUtils.findFirst
      ( new ConstraintNetworkViewFinder(), frame.getContentPane().getComponents());
  }

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
    return (TemporalExtentView) CollectionUtils.findFirst
      ( new TemporalExtentViewFinder(), frame.getContentPane().getComponents());
  }

  class TimelineViewFinder implements BooleanFunctor {
    public TimelineViewFinder(){}
    public final boolean func(Object o){return (o instanceof TimelineView);}
  }

  /**
   * <code>getTimelineView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>TimelineView</code> - 
   */
  public static TimelineView getTimelineView( MDIInternalFrame frame) {
    return generics._getTimelineView(frame);
  } // end getTimelineView

  private final TimelineView _getTimelineView(MDIInternalFrame frame) {
//     Component [] components = frame.getContentPane().getComponents();
//     System.err.println( "getComponentCount " + frame.getContentPane().getComponentCount());
//     for (int i = 0, n = frame.getContentPane().getComponentCount(); i < n; i++) {
//       System.err.println( "components i " + i + " " + components[i]);
//     }
    return (TimelineView) CollectionUtils.findFirst
      ( new TimelineViewFinder(), frame.getContentPane().getComponents());
  }

  class TokenNetworkViewFinder implements BooleanFunctor {
    public TokenNetworkViewFinder(){}
    public final boolean func(Object o){return (o instanceof TokenNetworkView);}
  }

  /**
   * <code>getTokenNetworkView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>TokenNetworkView</code> - 
   */
  public static TokenNetworkView getTokenNetworkView( MDIInternalFrame frame) {
    return generics._getTokenNetworkView(frame);
  } // end getTokenNetworkView

  private final TokenNetworkView _getTokenNetworkView(MDIInternalFrame frame) {
    return (TokenNetworkView) CollectionUtils.findFirst
      ( new TokenNetworkViewFinder(), frame.getContentPane().getComponents());
  }

  class DBTransactionViewFinder implements BooleanFunctor {
    public DBTransactionViewFinder(){}
    public final boolean func(Object o){return (o instanceof DBTransactionView);}
  }

  /**
   * <code>getDBTransactionView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>DBTransactionView</code> - 
   */
  public static DBTransactionView getDBTransactionView( MDIInternalFrame frame) {
    return generics._getDBTransactionView(frame);
  } // end getDBTransactionView

  private final DBTransactionView _getDBTransactionView(MDIInternalFrame frame) {
    return (DBTransactionView) CollectionUtils.findFirst
      ( new DBTransactionViewFinder(), frame.getContentPane().getComponents());
  }

  class ResourceProfileViewFinder implements BooleanFunctor {
    public ResourceProfileViewFinder(){}
    public final boolean func(Object o){return (o instanceof ResourceProfileView);}
  }

  /**
   * <code>getResourceProfileView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>ResourceProfileView</code> - 
   */
  public static ResourceProfileView getResourceProfileView( MDIInternalFrame frame) {
    return generics._getResourceProfileView(frame);
  } // end getResourceProfileView

  private final ResourceProfileView _getResourceProfileView(MDIInternalFrame frame) {
    return (ResourceProfileView) CollectionUtils.findFirst
      ( new ResourceProfileViewFinder(), frame.getContentPane().getComponents());
  }

  class ResourceTransactionViewFinder implements BooleanFunctor {
    public ResourceTransactionViewFinder(){}
    public final boolean func(Object o){return (o instanceof ResourceTransactionView);}
  }

  /**
   * <code>getResourceTransactionView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>ResourceTransactionView</code> - 
   */
  public static ResourceTransactionView getResourceTransactionView( MDIInternalFrame frame) {
    return generics._getResourceTransactionView(frame);
  } // end getResourceTransactionView

  private final ResourceTransactionView _getResourceTransactionView(MDIInternalFrame frame) {
    return (ResourceTransactionView) CollectionUtils.findFirst
      ( new ResourceTransactionViewFinder(), frame.getContentPane().getComponents());
  }


  class RuleInstanceViewFinder implements BooleanFunctor {
    public RuleInstanceViewFinder(){}
    public final boolean func(Object o){return (o instanceof RuleInstanceView);}
  }

  /**
   * <code>getRuleInstanceView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>RuleInstanceView</code> - 
   */
  public static RuleInstanceView getRuleInstanceView( MDIInternalFrame frame) {
    return generics._getRuleInstanceView(frame);
  } // end getRuleView

  private final RuleInstanceView _getRuleInstanceView(MDIInternalFrame frame) {
    return (RuleInstanceView) CollectionUtils.findFirst
      ( new RuleInstanceViewFinder(), frame.getContentPane().getComponents());
  }


  /**
   * <code>computeTransactionNameHeader</code>
   *
   * @return - <code>String</code> - 
   */
  public static String computeTransactionNameHeader() {
    //List nameList = MySQLDB.queryConstraintTransactionNames();
    //nameList.addAll( MySQLDB.queryTokenTransactionNames());
    //nameList.addAll( MySQLDB.queryVariableTransactionNames());
    List nameList = MySQLDB.queryTransactionNameList();
    StringBuffer transactionNameHeader =
      new StringBuffer( ViewConstants.DB_TRANSACTION_NAME_HEADER);
    int minLength = ViewConstants.DB_TRANSACTION_NAME_HEADER.length();
    int maxLength = 0;
    Iterator nameItr = nameList.iterator();
    while (nameItr.hasNext()) {
      String name = (String) nameItr.next();
      if (name.length() > maxLength) {
        maxLength = name.length();
      }
    }
    if (maxLength > minLength) { 
      boolean prepend = true;
      for (int i = 0, n = maxLength - minLength; i < n; i++) {
        if (prepend) {
          transactionNameHeader.insert( 0, " ");
        } else {
          transactionNameHeader.append( " ");
        }
        prepend = (! prepend);
      }
    }
    return transactionNameHeader.toString();
  } // end computeTransactionNameHeader



} // end class ViewGenerics 

