// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionView.java,v 1.15 2004-08-10 21:17:10 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 10oct03
//

package gov.nasa.arc.planworks.viz.partialPlan.dbTransaction;

import java.awt.BorderLayout;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.TransactionHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.util.DBTransactionComparatorAscending;
import gov.nasa.arc.planworks.viz.util.DBTransactionTable;
import gov.nasa.arc.planworks.viz.util.DBTransactionTableModel;
import gov.nasa.arc.planworks.viz.util.FixedHeightPanel;
import gov.nasa.arc.planworks.viz.util.TableSorter;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>DBTransactionView</code> - render a planning sequence step's plan db transactions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DBTransactionView extends PartialPlanView {

  private PwPlanningSequence planSequence;
  private List dbTransactionList; // element PwDBTransaction
  private int stepNumber;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private JScrollPane contentScrollPane;
  private JTable dbTransactionTable;
  private int objectKeyColumnIndx;
  private JGoView stepJGoView;
  private FixedHeightPanel stepsPanel;
  private boolean isStepButtonView;
  private DBTransactionHeaderView headerJGoView;

  /**
   * <code>DBTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public DBTransactionView( final ViewableObject partialPlan,  final ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.viewSet = (PartialPlanViewSet) viewSet;
    isStepButtonView = false;
    dBTransactionViewInit();

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

  public DBTransactionView( final ViewableObject partialPlan,  final ViewSet viewSet,
                            final PartialPlanViewState s) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.viewSet = (PartialPlanViewSet) viewSet;
    isStepButtonView = true;
    dBTransactionViewInit();
    setState(s);

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

  /**
   * <code>DBTransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public DBTransactionView( final ViewableObject partialPlan,  final ViewSet viewSet,
                            final ViewListener viewListener) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.viewSet = (PartialPlanViewSet) viewSet;
    isStepButtonView = false;
    dBTransactionViewInit();
    if (viewListener != null) {
      addViewListener( viewListener);
    }

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

  private void dBTransactionViewInit() {
    ViewListener viewListener = null;
    viewFrame = viewSet.openView( this.getClass().getName(), viewListener);
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());
    viewName = ViewConstants.DB_TRANSACTION_VIEW;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
  } // end DBTransactionInit

  public void setState( PartialPlanViewState s) {
    super.setState( s);
    if(s == null) {
      return;
    }
  } // end setState

//   Runnable runInit = new Runnable() {
//       public final void run() {
//         init();
//       }
//     };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use SwingWorker in constructor
   */
  public final void init() {
    handleEvent( ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for TimelineView instance to become displayable
    if (! ViewGenerics.displayableWait( DBTransactionView.this)) {
      closeView( this);
      return;
    }
    this.computeFontMetrics( this);

    int numOperations = 6;
    progressMonitorThread( "Rendering DB Transaction View:", 0, numOperations,
                           Thread.currentThread(), this);
    if (! progressMonitorWait( this)) {
      closeView( this);
      return;
    }
    progressMonitor.setNote( "Get Transactions ...");
    progressMonitor.setProgress( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);
 
    planSequence = PlanWorks.getPlanWorks().getPlanSequence( this.partialPlan);
    dbTransactionList = planSequence.getTransactionsList( this.partialPlan.getId());
    Collections.sort( dbTransactionList,
                      new DBTransactionComparatorAscending
                      ( ViewConstants.DB_TRANSACTION_KEY_HEADER));
    stepNumber = this.partialPlan.getStepNumber();

    progressMonitor.setNote( "Create Table ...");
    progressMonitor.setMaximum( dbTransactionList.size());
    numOperations = 4;
    progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
    String query = null;
    headerJGoView = new DBTransactionHeaderView( query);
    headerJGoView.validate();
    headerJGoView.setVisible( true);

    FixedHeightPanel transactionHeaderPanel = new FixedHeightPanel( headerJGoView, this);
    transactionHeaderPanel.setLayout( new BoxLayout( transactionHeaderPanel, BoxLayout.Y_AXIS));
    transactionHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( transactionHeaderPanel, BorderLayout.NORTH);

    String[] columnNames =
      { ViewConstants.DB_TRANSACTION_KEY_HEADER,
        ViewGenerics.computeTransactionNameHeader(),
        ViewConstants.DB_TRANSACTION_SOURCE_HEADER,
        ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER,
        // ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
        ViewConstants.DB_TRANSACTION_ENTITY_NAME_HEADER,
        ViewConstants.DB_TRANSACTION_PARENT_HEADER,
        ViewConstants.DB_TRANSACTION_PARAMETER_HEADER,
        // empty last column to allow user adjusting of column widths
        "" };
    Object[][] data = new Object [dbTransactionList.size()] [columnNames.length];
    for (int row = 0, nRows = dbTransactionList.size(); row < nRows; row++) {
      PwDBTransaction transaction = (PwDBTransaction) dbTransactionList.get( row);
      data[row][0] = transaction.getId().toString();
      data[row][1] = transaction.getName();
      data[row][2] = transaction.getSource();
      data[row][3] = transaction.getEntityId().toString();
      // data[row][4] = transaction.getStepNumber().toString();
      data[row][4] = transaction.getInfo()[0];
      data[row][5] = transaction.getInfo()[1];
      data[row][6] = transaction.getInfo()[2];
      if (progressMonitor.isCanceled()) {
        String msg = "User Canceled DB Transaction View Rendering";
        System.err.println( msg);
        isProgressMonitorCancel = true;
        closeView( this);
        return;
      }
      numOperations++;
      progressMonitor.setProgress( numOperations * ViewConstants.MONITOR_MIN_MAX_SCALING);
    }
    objectKeyColumnIndx = 3;
    int stepNumberColumnIndx = -1;

    TableSorter sorter =
      new TableSorter( new DBTransactionTableModel( columnNames, data) {
	  public Class getColumnClass(int columnIndex) {
	    if ((columnIndex == 0) || (columnIndex == 3)) {
	      return Integer.class;
	    } else {
	      return String.class;
	    }
	  }
	});
    sorter.setColumnComparator( Integer.class, DBTransactionTableModel.INTEGER_COMPARATOR);

    dbTransactionTable = new DBTransactionTable( sorter, stepNumberColumnIndx, this);
    sorter.setTableHeader( dbTransactionTable.getTableHeader());
    contentScrollPane = new JScrollPane( dbTransactionTable);
    add( contentScrollPane, BorderLayout.NORTH);

    stepJGoView = new JGoView();
    stepJGoView.setHorizontalScrollBar( null);
    stepJGoView.setVerticalScrollBar( null);
    stepJGoView.validate();
    stepJGoView.setVisible( true);

    stepsPanel = new FixedHeightPanel( stepJGoView, this);
    stepsPanel.setLayout( new BoxLayout( stepsPanel, BoxLayout.Y_AXIS));

    // force step icon to be in ~center of fixed height panel
    JGoStroke fixedHeightLine = new JGoStroke();
    fixedHeightLine.addPoint( ViewConstants.TIMELINE_VIEW_X_INIT,
                              ViewConstants.TIMELINE_VIEW_Y_INIT);
    fixedHeightLine.addPoint( (ViewConstants.TIMELINE_VIEW_X_INIT * 4),
                              (ViewConstants.TIMELINE_VIEW_Y_INIT * 3));
    fixedHeightLine.setPen( new JGoPen( JGoPen.SOLID, 1, ViewConstants.VIEW_BACKGROUND_COLOR));
    // fixedHeightLine.setPen( new JGoPen( JGoPen.SOLID, 1, ColorMap.getColor( "black")));
    stepJGoView.getDocument().addObjectAtTail( fixedHeightLine);

    stepJGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    stepsPanel.add( stepJGoView, BorderLayout.NORTH);

    add( stepsPanel, BorderLayout.NORTH);

    this.setVisible( true);

    expandViewFrame( viewFrame, dbTransactionTable.getColumnModel().getTotalColumnWidth(),
                     (int) (((dbTransactionTable.getRowCount() + 2) *
                             dbTransactionTable.getRowHeight()) +
                            headerJGoView.getDocumentSize().getHeight() +
                            stepJGoView.getDocumentSize().getHeight() +
                            contentScrollPane.getHorizontalScrollBar().getSize().getHeight()));

    addStepButtons( stepJGoView);

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... " + ViewConstants.DB_TRANSACTION_VIEW + " elapsed time: " +
                        (stopTimeMSecs -
                         PlanWorks.getPlanWorks().getViewRenderingStartTime
                         ( ViewConstants.DB_TRANSACTION_VIEW)) + " msecs.");
    isProgressMonitorCancel = true;
    handleEvent( ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init

  /**
   * <code>getDBTransactionList</code>
   *
   * @return - <code>List</code> - 
   */
  public final List getDBTransactionList() {
    return dbTransactionList;
  }

  /**
   * <code>getDBTransactionTable</code>
   *
   * @return - <code>JTable</code> - 
   */
  public final JTable getDBTransactionTable() {
    return dbTransactionTable;
  }

  /**
   * <code>getJGoView</code>
   *
   * @return - <code>JGoView</code> - 
   */
  public final JGoView getJGoView() {
    return headerJGoView;
  }

  /**
   * <code>getContentScrollPane</code>
   *
   * @return - <code>JScrollPane</code> - 
   */
  public final JScrollPane getContentScrollPane() {
    return contentScrollPane;
  }

  /**
   * <code>DBTransactionHeaderView</code> - 
   *
   */
  public class DBTransactionHeaderView extends TransactionHeaderView {

    /**
     * <code>DBTransactionHeaderView</code> - constructor 
     *
     * @param query - <code>String</code> - 
     */
    public DBTransactionHeaderView( final String query) {
      super( query);
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point docCoords,
                                         final Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick 

    private void mouseRightPopupMenu( final Point viewCoords) {
      JPopupMenu mouseRightPopup = new JPopupMenu();
      JMenuItem transByKeyItem = new JMenuItem( "Find Transaction by Entity_Key");
      createTransByKeyItem( transByKeyItem, dbTransactionList, contentScrollPane,
                            dbTransactionTable, objectKeyColumnIndx, DBTransactionView.this);
      mouseRightPopup.add( transByKeyItem);

      PwPartialPlan partialPlan = DBTransactionView.this.getPartialPlan();
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);

      DBTransactionView.this.createOpenViewItems( partialPlan, partialPlanName,
                                                  planSequence, mouseRightPopup,
                                                  viewListenerList,
                                                  ViewConstants.DB_TRANSACTION_VIEW);
    
      DBTransactionView.this.createAllViewItems( partialPlan, partialPlanName,
                                                 planSequence, viewListenerList,
                                                 mouseRightPopup);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightPopupMenu

  } // end class DBTransactionHeaderView


} // end class DBTransactionView

