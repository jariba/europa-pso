// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepQueryView.java,v 1.16 2004-10-13 23:49:22 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17oct03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceQuery;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.TransactionHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.util.DBTransactionComparatorAscending;
import gov.nasa.arc.planworks.viz.util.DBTransactionTable;
import gov.nasa.arc.planworks.viz.util.DBTransactionTableModel;
import gov.nasa.arc.planworks.viz.util.FixedHeightPanel;
import gov.nasa.arc.planworks.viz.util.TableSorter;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;


/**
 * <code>StepQueryView</code> - render the step results of a sequence query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class StepQueryView extends SequenceView {

  private PwPlanningSequence planSequence;
  private List stepList; // element Integer
  private String key;
  private String query;
  private SequenceQueryWindow sequenceQueryWindow;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private JScrollPane contentScrollPane;
  private JTable stepTable;
  private int objectKeyColumnIndx;
  private int stepNumberColumnIndx;
  private QueryHeaderView headerJGoView;


  /**
   * <code>StepQueryView</code> - constructor 
   *
   * @param stepList - <code>List</code> - 
   * @param key - <code>String</code> - 
   * @param query - <code>String</code> - 
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param sequenceQueryWindow - <code>JPanel</code> - 
   * @param stepQueryFrame - <code>MDIInternalFrame</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public StepQueryView( final List stepList, final String key, final String query,
                        final ViewableObject planSequence, final ViewSet viewSet,
                        final JPanel sequenceQueryWindow,
                        final MDIInternalFrame stepQueryFrame, final long startTimeMSecs,
                        final ViewListener viewListener) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.stepList = stepList;
    this.key = key;
    Collections.sort( stepList,
                      new DBTransactionComparatorAscending
                      ( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER));
    this.query = query;
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;
    this.sequenceQueryWindow = (SequenceQueryWindow) sequenceQueryWindow;
    viewFrame = stepQueryFrame;
    // for PWTestHelper.findComponentByName
    this.setName( stepQueryFrame.getTitle());
    this.startTimeMSecs = startTimeMSecs;
    if (viewListener != null) {
      addViewListener( viewListener);
    }

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

 
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
    // wait for StepQueryView instance to become displayable
    if (! ViewGenerics.displayableWait( StepQueryView.this)) {
      closeView( this);
      return;
    }
    this.computeFontMetrics( this);

    headerJGoView = new QueryHeaderView( query);
    headerJGoView.validate();
    headerJGoView.setVisible( true);

    FixedHeightPanel stepHeaderPanel = new FixedHeightPanel( headerJGoView, this);
    stepHeaderPanel.setLayout( new BoxLayout( stepHeaderPanel, BoxLayout.Y_AXIS));
    stepHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( stepHeaderPanel, BorderLayout.NORTH);

    TableSorter sorter = createTableModelAndSorter();
    sorter.setColumnComparator( Integer.class, DBTransactionTableModel.INTEGER_COMPARATOR);
    stepTable = new DBTransactionTable( sorter, stepNumberColumnIndx, this);
    sorter.setTableHeader( stepTable.getTableHeader());
    contentScrollPane = new JScrollPane( stepTable);
    add( contentScrollPane, BorderLayout.NORTH);

    this.setVisible( true);

    int maxViewWidth = (int) headerJGoView.getDocumentSize().getWidth();
    int maxViewHeight = (int) ( headerJGoView.getDocumentSize().getHeight() +
                                // contentJGoView.getDocumentSize().getHeight());
                                // keep contentJGoView small
                                (ViewConstants.INTERNAL_FRAME_X_DELTA));
    viewFrame.setSize( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
                       maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
    int maxQueryFrameY =
      (int) (sequenceQueryWindow.getSequenceQueryFrame().getLocation().getY() +
             sequenceQueryWindow.getSequenceQueryFrame().getSize().getHeight());
    int delta = Math.min( ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4 *
                          sequenceQueryWindow.getQueryResultFrameCnt(),
                          (int) (PlanWorks.getPlanWorks().getSize().getHeight() -
                                 maxQueryFrameY -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    viewFrame.setLocation( ViewConstants.INTERNAL_FRAME_X_DELTA + delta,
                           maxQueryFrameY + delta);
    // prevent right edge from going outside the MDI frame
    expandViewFrame( viewFrame,
                     Math.max( (int) headerJGoView.getDocumentSize().getWidth(),
                               stepTable.getColumnModel().getTotalColumnWidth()),
                     (int) (headerJGoView.getDocumentSize().getHeight() +
                            stepTable.getRowCount() *
                            stepTable.getRowHeight()));

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... '" + this.getName() + "' elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    handleEvent( ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init

  /**
   * <code>getStepList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getStepList() {
    return stepList;
  }

  /**
   * <code>getHeaderJGoView</code>
   *
   * @return - <code>QueryHeaderView</code> - 
   */
  public QueryHeaderView getHeaderJGoView() {
    return headerJGoView;
  }

  private TableSorter createTableModelAndSorter() {
    // " With " queries & non " With " queries with null key show ENTITY_KEY in table,
    // the others do not since the query specifies the key
    String[] columnNames = null;
    Object[][] data = null;
    if ((query.indexOf( " With ") >= 0) ||
        ((query.indexOf( " With ") == -1) && key.equals( "") &&
         (query.indexOf( "Variable") >= 0))) {
      // only variable transactions
      objectKeyColumnIndx = 3;
      stepNumberColumnIndx = 0;
      columnNames = new String[] { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                   ViewConstants.DB_TRANSACTION_KEY_HEADER,
                                   ViewGenerics.computeTransactionNameHeader(),
                                   ViewConstants.QUERY_VARIABLE_KEY_HEADER,
                                   ViewConstants.QUERY_VARIABLE_TYPE_HEADER,
                                   ViewConstants.DB_TRANSACTION_PARENT_HEADER,
                                   ViewConstants.DB_TRANSACTION_PARAMETER_HEADER,
                                   // empty last column to allow user adjusting of column widths
                                   "" };
      data = new Object [stepList.size()] [columnNames.length];
      for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
        PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
        data[row][0] = transaction.getStepNumber().toString();
        data[row][1] = transaction.getId().toString();
        data[row][2] = transaction.getName();
        data[row][3] = transaction.getEntityId().toString();
        data[row][4] = transaction.getInfo()[0];
        data[row][5] = transaction.getInfo()[1];
        data[row][6] = transaction.getInfo()[2];
      }
      return new TableSorter( new DBTransactionTableModel( columnNames, data) {
	  public Class getColumnClass(int columnIndex) {
	    if ((columnIndex == 0) || (columnIndex == 1) || (columnIndex == 3)) {
	      return Integer.class;
	    } else {
	      return String.class;
	    }
	  }
	});
    } else if ((query.indexOf( " With ") == -1) && key.equals( "")) {
      // all constraints, or tokens, for sequence
      objectKeyColumnIndx = 3;
      stepNumberColumnIndx = 0;
     if (query.indexOf( "Constraint") >= 0) {
        columnNames = new String[] { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                     ViewConstants.DB_TRANSACTION_KEY_HEADER,
                                     ViewGenerics.computeTransactionNameHeader(),
                                     ViewConstants.QUERY_CONSTRAINT_KEY_HEADER,
                                     ViewConstants.QUERY_CONSTRAINT_TYPE_HEADER,
                                     // empty last column to allow user adjusting of column widths
                                     "" };
        data = new Object [stepList.size()] [columnNames.length];
        for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
          PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
          data[row][0] = transaction.getStepNumber().toString();
          data[row][1] = transaction.getId().toString();
          data[row][2] = transaction.getName();
          data[row][3] = transaction.getEntityId().toString();
          data[row][4] = transaction.getInfo()[0];
        }
      } else if (query.indexOf( "Token") >= 0) {
        columnNames = new String[] { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                     ViewConstants.DB_TRANSACTION_KEY_HEADER,
                                     ViewGenerics.computeTransactionNameHeader(),
                                     ViewConstants.QUERY_TOKEN_KEY_HEADER,
                                     ViewConstants.QUERY_TOKEN_PREDICATE_HEADER,
                                     // empty last column to allow user adjusting of column widths
                                     "" };
        data = new Object [stepList.size()] [columnNames.length];
        for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
          PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
          data[row][0] = transaction.getStepNumber().toString();
          data[row][1] = transaction.getId().toString();
          data[row][2] = transaction.getName();
          data[row][3] = transaction.getEntityId().toString();
          data[row][4] = transaction.getInfo()[0];
        }
      }
      return new TableSorter( new DBTransactionTableModel( columnNames, data) {
	  public Class getColumnClass(int columnIndex) {
	    if ((columnIndex == 0) || (columnIndex == 1) || (columnIndex == 3)) {
	      return Integer.class;
	    } else {
	      return String.class;
	    }
	  }
	});
    } else {
      // specific constraint, token, or variable for sequence
      objectKeyColumnIndx = -1;
      stepNumberColumnIndx = 0;
      if (query.indexOf( "Constraint") >= 0) {
        columnNames = new String[] { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                     ViewConstants.DB_TRANSACTION_KEY_HEADER,
                                     ViewGenerics.computeTransactionNameHeader(),
                                     // ViewConstants.QUERY_CONSTRAINT_KEY_HEADER,
                                     ViewConstants.QUERY_CONSTRAINT_TYPE_HEADER,
                                     // empty last column to allow user adjusting of column widths
                                     "" };
        data = new Object [stepList.size()] [columnNames.length];
        for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
          PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
          data[row][0] = transaction.getStepNumber().toString();
          data[row][1] = transaction.getId().toString();
          data[row][2] = transaction.getName();
          // data[row][3] = transaction.getEntityId().toString();
          data[row][3] = transaction.getInfo()[0];
        }
      } else if (query.indexOf( "Token") >= 0) {
        columnNames = new String[] { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                     ViewConstants.DB_TRANSACTION_KEY_HEADER,
                                     ViewGenerics.computeTransactionNameHeader(),
                                     // ViewConstants.QUERY_TOKEN_KEY_HEADER,
                                     ViewConstants.QUERY_TOKEN_PREDICATE_HEADER,
                                     // empty last column to allow user adjusting of column widths
                                     "" };
        data = new Object [stepList.size()] [columnNames.length];
        for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
          PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
          data[row][0] = transaction.getStepNumber().toString();
          data[row][1] = transaction.getId().toString();
          data[row][2] = transaction.getName();
          // data[row][3] = transaction.getEntityId().toString();
          data[row][3] = transaction.getInfo()[0];
        }
      } else if (query.indexOf( "Variable") >= 0) {
        columnNames = new String[] { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                     ViewConstants.DB_TRANSACTION_KEY_HEADER,
                                     ViewGenerics.computeTransactionNameHeader(),
                                     // ViewConstants.DB_TRANSACTION_ENTITY_KEY_HEADER,
                                     ViewConstants.QUERY_VARIABLE_TYPE_HEADER,
                                     ViewConstants.DB_TRANSACTION_PARENT_HEADER,
                                     ViewConstants.DB_TRANSACTION_PARAMETER_HEADER,
                                     // empty last column to allow user adjusting of column widths
                                     "" };
        data = new Object [stepList.size()] [columnNames.length];
        for (int row = 0, nRows = stepList.size(); row < nRows; row++) {
          PwDBTransaction transaction = (PwDBTransaction) stepList.get( row);
          data[row][0] = transaction.getStepNumber().toString();
          data[row][1] = transaction.getId().toString();
          data[row][2] = transaction.getName();
          // data[row][3] = transaction.getEntityId().toString();
          data[row][3] = transaction.getInfo()[0];
          data[row][4] = transaction.getInfo()[1];
          data[row][5] = transaction.getInfo()[2];
        }
      }
      return new TableSorter( new DBTransactionTableModel( columnNames, data) {
	  public Class getColumnClass(int columnIndex) {
	    if ((columnIndex == 0) || (columnIndex == 1)) {
	      return Integer.class;
	    } else {
	      return String.class;
	    }
	  }
	});
    }
  }


  /**
   * <code>QueryHeaderView</code> - 
   *
   */
  public class QueryHeaderView extends TransactionHeaderView {

    public QueryHeaderView( final String query) {
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
        if ((query.indexOf( " With ") >= 0) ||
            ((query.indexOf( " With ") == -1) && key.equals( ""))) {
          mouseRightPopupMenu( viewCoords);
        }
      }
    } // end doBackgroundClick 

    private void mouseRightPopupMenu( final Point viewCoords) {
      JPopupMenu mouseRightPopup = new JPopupMenu();
      String keyString = "";
      if (query.indexOf( "Constraint") >= 0) {
        keyString = ViewConstants.QUERY_CONSTRAINT_KEY_HEADER;
      } else if (query.indexOf( "Token") >= 0) {
        keyString = ViewConstants.QUERY_TOKEN_KEY_HEADER;
      } else if ((query.indexOf( "Variable") >= 0) ||
                 (query.indexOf( "Restrictions") >= 0)) {
        keyString = ViewConstants.QUERY_VARIABLE_KEY_HEADER;
      }
      JMenuItem transByKeyItem = new JMenuItem( "Find Transaction by " + keyString);
      createTransByKeyItem( transByKeyItem, contentScrollPane, stepTable,
                            objectKeyColumnIndx, StepQueryView.this);
      mouseRightPopup.add( transByKeyItem);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightPopupMenu

  } // end class QueryHeaderView



} // end class StepQueryView

