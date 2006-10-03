// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ViewGenerics.java,v 1.29 2006-10-03 16:14:17 miatauro Exp $
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.Overview;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.SQLDB;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.JarClassLoader;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.RuleInstanceNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
//import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView;
import gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView;
import gov.nasa.arc.planworks.viz.partialPlan.rule.RuleInstanceView;
import gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView;
import gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView;
import gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;
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
                                                List viewListenerList) {
    JPopupMenu mouseRightPopup = new PartialPlanViewMenu();
    String partialPlanName = "step" + String.valueOf( stepNumber);
    JMenuItem header = new JMenuItem( partialPlanName);
    mouseRightPopup.add( header);
    mouseRightPopup.addSeparator();

    int numItemsAdded = ((PartialPlanViewMenu) mouseRightPopup).
      buildPartialPlanViewMenu( partialPlanName, planSequence, viewListenerList);
    if (numItemsAdded > 0) {
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
      if (numItemsAdded == PlanWorks.PARTIAL_PLAN_VIEW_LIST.size()) {
	vizView.createAllViewItems( partialPlanIfLoaded, partialPlanName, planSequence,
				    viewListenerList, mouseRightPopup);
      }

      ViewGenerics.showPopupMenu( mouseRightPopup, vizView, viewCoords);
    } else {
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), "Sequence " +
				     planSequence.getName() + "/" + partialPlanName,
				     "No Views Available", 
				     JOptionPane.ERROR_MESSAGE);
    }
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
   * <code>openNodeShapesView</code>
   *
   * @param menuItem - <code>JMenuItem</code> -
   */
  public static void openNodeShapesView( JMenuItem menuItem) {
    JFrame nodeShapesFrame = PlanWorks.getPlanWorks().getNodeShapesFrame();
    if (nodeShapesFrame == null) {
      nodeShapesFrame = new JFrame( ViewConstants.NODE_SHAPES_FRAME);
      Container contentPane = nodeShapesFrame.getContentPane();
      NodeShapes nodeShapesView = new NodeShapes( nodeShapesFrame, menuItem);
      contentPane.add( nodeShapesView);
      
      nodeShapesFrame.setSize( ViewConstants.NODE_SHAPES_FRAME_WIDTH,
                               ViewConstants.NODE_SHAPES_FRAME_HEIGHT);
      nodeShapesFrame.setVisible( true);
      Point popupLocation = Utilities.getPopupLocation( PlanWorks.getPlanWorks());
      nodeShapesFrame.setLocation( ((int) popupLocation.getX()) -
                                   (ViewConstants.NODE_SHAPES_FRAME_WIDTH / 2),
                                   ((int) popupLocation.getY()) -
                                   (ViewConstants.NODE_SHAPES_FRAME_HEIGHT / 2));
      PlanWorks.getPlanWorks().setNodeShapesFrame( nodeShapesFrame);
    }
  } // end openNodesShapesView

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

//   class DBTransactionViewFinder implements BooleanFunctor {
//     public DBTransactionViewFinder(){}
//     public final boolean func(Object o){return (o instanceof DBTransactionView);}
//   }

  /**
   * <code>getDBTransactionView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>DBTransactionView</code> - 
   */
//   public static DBTransactionView getDBTransactionView( MDIInternalFrame frame) {
//     return generics._getDBTransactionView(frame);
//   } // end getDBTransactionView

//   private final DBTransactionView _getDBTransactionView(MDIInternalFrame frame) {
//     return (DBTransactionView) CollectionUtils.findFirst
//       ( new DBTransactionViewFinder(), frame.getContentPane().getComponents());
//   }

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
  } // end getRuleInstanceView

  private final RuleInstanceView _getRuleInstanceView(MDIInternalFrame frame) {
    return (RuleInstanceView) CollectionUtils.findFirst
      ( new RuleInstanceViewFinder(), frame.getContentPane().getComponents());
  }


  class SequenceStepsViewFinder implements BooleanFunctor {
    public SequenceStepsViewFinder(){}
    public final boolean func(Object o){return (o instanceof SequenceStepsView);}
  }

  /**
   * <code>getSequenceStepsView</code> - cannot be generalized (jdk1.4)
   *
   * @param frame - <code>MDIInternalFrame</code> - 
   * @return - <code>SequenceStepsView</code> - 
   */
  public static SequenceStepsView getSequenceStepsView( MDIInternalFrame frame) {
    return generics._getSequenceStepsView(frame);
  } // end getSequenceStepsView

  private final SequenceStepsView _getSequenceStepsView(MDIInternalFrame frame) {
//     Component [] components = frame.getContentPane().getComponents();
//     System.err.println( "_getSequenceStepsView num components " +
//                         frame.getContentPane().getComponentCount());
//     for (int i = 0, n = frame.getContentPane().getComponentCount(); i < n; i++) {
//       System.err.println( "_getSequenceStepsView " + components[i].getClass().getName());
//     }
    return (SequenceStepsView) CollectionUtils.findFirst
      ( new SequenceStepsViewFinder(), frame.getContentPane().getComponents());
  }


  class OverviewViewFinder implements BooleanFunctor {
    public OverviewViewFinder(){}
    public final boolean func(Object o){return (o instanceof VizViewOverview);}
  }

  public static VizViewOverview getOverviewView( MDIInternalFrame frame) {
    return generics._getOverviewView(frame);
  } // end getOverviewView

  private final VizViewOverview _getOverviewView(MDIInternalFrame frame) {
    return (VizViewOverview) CollectionUtils.findFirst
      ( new OverviewViewFinder(), frame.getContentPane().getComponents());
  }


  /**
   * <code>computeTransactionNameHeader</code>
   *
   * @return - <code>String</code> - 
   */
//   public static String computeTransactionNameHeader() {
//     //List nameList = SQLDB.queryConstraintTransactionNames();
//     //nameList.addAll( SQLDB.queryTokenTransactionNames());
//     //nameList.addAll( SQLDB.queryVariableTransactionNames());
//     List nameList = SQLDB.queryTransactionNameList();
//     StringBuffer transactionNameHeader =
//       new StringBuffer( ViewConstants.DB_TRANSACTION_NAME_HEADER);
//     int minLength = ViewConstants.DB_TRANSACTION_NAME_HEADER.length();
//     int maxLength = 0;
//     Iterator nameItr = nameList.iterator();
//     while (nameItr.hasNext()) {
//       String name = (String) nameItr.next();
//       if (name.length() > maxLength) {
//         maxLength = name.length();
//       }
//     }
//     if (maxLength > minLength) { 
//       boolean prepend = true;
//       for (int i = 0, n = maxLength - minLength; i < n; i++) {
//         if (prepend) {
//           transactionNameHeader.insert( 0, " ");
//         } else {
//           transactionNameHeader.append( " ");
//         }
//         prepend = (! prepend);
//       }
//     }
//     return transactionNameHeader.toString();
//   } // end computeTransactionNameHeader

  /**
   * <code>displayableWait</code>
   *
   * @param component - <code>Component</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean displayableWait( Component component) {
    int numCycles = ViewConstants.WAIT_NUM_CYCLES;
    while (! component.isDisplayable() && numCycles != 0) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      numCycles--;
    }
    if (numCycles == 0) {
      System.err.println( "displayableWait failed after " +
                          (ViewConstants.WAIT_INTERVAL * ViewConstants.WAIT_NUM_CYCLES) +
                          " msec for " + component.getClass().getName());
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
    }
    return numCycles != 0;
  } // end displayableWait

  /**
   * <code>createRuleInstanceViewItem</code>
   *
   * @param ruleInstanceNode - <code>RuleInstanceNode</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @return - <code>JMenuItem</code> - 
   */
  public static JMenuItem createRuleInstanceViewItem( final RuleInstanceNode ruleInstanceNode,
                                                      final PartialPlanView partialPlanView,
                                                      final ViewListener viewListener) {
    JMenuItem ruleInstanceViewItem = new JMenuItem( "Open Rule Instance View");
    ruleInstanceViewItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent evt) {
          MDIInternalFrame ruleInstanceViewFrame = null;
          RuleInstanceView ruleInstanceView = null;
          ViewSet viewSet = partialPlanView.getViewSet();
          Iterator viewItr = viewSet.getViews().keySet().iterator();
          while (viewItr.hasNext()) {
            MDIInternalFrame viewFrame = viewSet.getView( (Object) viewItr.next());
            if (((ruleInstanceView = ViewGenerics.getRuleInstanceView( viewFrame)) != null) &&
                (ruleInstanceView.getRuleInstanceId().equals
                 ( ruleInstanceNode.getRuleInstance().getId()))) {
              ruleInstanceViewFrame = viewFrame;
              break;
            }
          }
          if (ruleInstanceViewFrame == null) {
            String viewSetKey = partialPlanView.getRuleViewSetKey();
            ruleInstanceViewFrame = partialPlanView.openRuleViewFrame( viewSetKey);
            Container contentPane = ruleInstanceViewFrame.getContentPane();
            PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
            contentPane.add( new RuleInstanceView( ruleInstanceNode, partialPlan,
                                                   partialPlanView.getViewSet(),
                                                   viewSetKey, ruleInstanceViewFrame,
                                                   viewListener));
          } else {
            ViewGenerics.raiseFrame( ruleInstanceViewFrame);
          }
        }
      });
    return ruleInstanceViewItem;
  } // end createRuleInstanceViewItem

  /**
   * <code>createConfigPopupItems</code>
   *
   * @param viewName - <code>String</code> - 
   * @param view - <code>PartialPlanView</code> - 
   * @param mouseRightPopup - <code>JPopupMenu</code> - 
   */
  public static void createConfigPopupItems( String viewName, final VizView view,
                                             final JPopupMenu mouseRightPopup) {
    boolean isFirst = true;
    List viewPopupSpecs = new ArrayList();
    Iterator popupSpecsItr = PlanWorks.VIEW_MOUSE_RIGHT_MAP.keySet().iterator();
    while (popupSpecsItr.hasNext()) {
      String viewNameItemName = (String) popupSpecsItr.next();
      System.err.println( "createConfigPopupItems: viewName " + viewName +
                          " viewNameItemName " + viewNameItemName);
      if (viewNameItemName.indexOf( viewName) >= 0) {
        int index = viewNameItemName.indexOf( ":");
        final String itemName = viewNameItemName.substring( index + 1);
        List classMethodNameList = (List) PlanWorks.VIEW_MOUSE_RIGHT_MAP.get( viewNameItemName);
        final String className = (String) classMethodNameList.get( 0);
        final String methodName = (String) classMethodNameList.get( 1);
        if (isFirst) {
          isFirst = false;
          mouseRightPopup.addSeparator();
          mouseRightPopup.addSeparator();
        }
        JMenuItem menuItem = new JMenuItem( itemName);
        menuItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt) {
              boolean resolveIt = true;
              Iterator pluginNameItr = ConfigureAndPlugins.PLUG_IN_LOADER_MAP.keySet().iterator();
              while (pluginNameItr.hasNext()) {
                try {
                  JarClassLoader classLoader =
                    (JarClassLoader) ConfigureAndPlugins.PLUG_IN_LOADER_MAP.
                    get( (String) pluginNameItr.next());
                  Class classObject = classLoader.loadClassAndResolveIt( className);
                  // System.err.println( "classObject name " + classObject.getName());
                  Method method =
                    classObject.getMethod
                    ( methodName,
                      new Class[] {  Class.forName( "java.lang.String"),
                                     Class.forName( "javax.swing.JPopupMenu"),
                                     Class.forName( "gov.nasa.arc.planworks.viz.VizView")});
                  method.setAccessible( true);
                  try {
                    Object [] args = new Object [] { itemName, mouseRightPopup, view};
                    method.invoke( null, args); 
                  } catch (IllegalAccessException e) {
                    // This should not happen, as we have disabled access checks
                  }
                  break;
                } catch (ClassNotFoundException cnfExcep) {
                  cnfExcep.printStackTrace();
                  // System.exit( -1);
                } catch (NoSuchMethodException nsmExcep) {
                  nsmExcep.printStackTrace();
                  // System.exit( -1);
                } catch (InvocationTargetException itExcep) {
                  itExcep.printStackTrace();
                  // System.exit( -1);
                }
              }
            }
          });
        mouseRightPopup.add( menuItem);
      }
    }
  } // end createConfigPopupItems


} // end class ViewGenerics 

