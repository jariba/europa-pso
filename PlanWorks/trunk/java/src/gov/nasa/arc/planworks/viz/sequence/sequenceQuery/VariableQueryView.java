// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableQueryView.java,v 1.9 2004-06-16 22:09:18 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceQuery;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwVariableQuery;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.TransactionHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.util.VariableQueryComparatorAscending;
import gov.nasa.arc.planworks.viz.util.DBTransactionTable;
import gov.nasa.arc.planworks.viz.util.DBTransactionTableModel;
import gov.nasa.arc.planworks.viz.util.FixedHeightPanel;
import gov.nasa.arc.planworks.viz.util.TableSorter;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;


/**
 * <code>VariableQueryView</code> - render the unbound variable
 *                           results of a sequence query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableQueryView extends SequenceView {

  private PwPlanningSequence planSequence;
  private List variableList; // element PwVariableQuery
  private String query;
  private SequenceQueryWindow sequenceQueryWindow;
  private int stepNumber;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private JScrollPane contentScrollPane;
  private JTable variableTable;
  private int objectKeyColumnIndx;
  private int stepNumberColumnIndx;


  /**
   * <code>VariableQueryView</code> - constructor 
   *
   * @param variableList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param planSequence - <code>ViewableObject</code> - 
   * @param stepString - <code>String</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param sequenceQueryWindow - <code>JPanel</code> - 
   * @param variableQueryFrame - <code>MDIInternalFrame</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public VariableQueryView( final List variableList, final String query,
                            final ViewableObject planSequence, final String stepString,
                            final ViewSet viewSet, final JPanel sequenceQueryWindow,
                            final MDIInternalFrame variableQueryFrame,
                            final long startTimeMSecs, final ViewListener viewListener) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.variableList = variableList;
    Collections.sort( variableList,
                      new VariableQueryComparatorAscending
                      ( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER));
    this.query = query;
    this.planSequence = (PwPlanningSequence) planSequence;
    stepNumber = Integer.parseInt( stepString);
    this.viewSet = (SequenceViewSet) viewSet;
    this.sequenceQueryWindow = (SequenceQueryWindow) sequenceQueryWindow;
    viewFrame = variableQueryFrame;
    // for PWTestHelper.findComponentByName
    setName( variableQueryFrame.getTitle());
    this.startTimeMSecs = startTimeMSecs;
    if (viewListener != null) {
      addViewListener( viewListener);
    }

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    SwingUtilities.invokeLater( runInit);
  } // end constructor

 
  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public final void init() {
    handleEvent( ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for VariableQueryView instance to become displayable
    if (! ViewGenerics.displayableWait( VariableQueryView.this)) {
      return;
    }
    this.computeFontMetrics( this);

    QueryHeaderView headerJGoView = new QueryHeaderView( query);
    headerJGoView.validate();
    headerJGoView.setVisible( true);

    FixedHeightPanel variableHeaderPanel = new FixedHeightPanel( headerJGoView, this);
    variableHeaderPanel.setLayout( new BoxLayout( variableHeaderPanel,
                                                         BoxLayout.Y_AXIS));
    variableHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( variableHeaderPanel, BorderLayout.NORTH);

    String[] columnNames = { ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                             ViewConstants.QUERY_VARIABLE_KEY_HEADER,
                             ViewConstants.QUERY_VARIABLE_TYPE_HEADER,
                             ViewConstants.DB_TRANSACTION_PARENT_HEADER,
                             // empty last column to allow user adjusting of column widths
                             "" };
    PwPartialPlan partialPlan = null;
    try {
      partialPlan = planSequence.getPartialPlan( stepNumber);
    } catch ( ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }
    Object[][] data = new Object [variableList.size()] [columnNames.length];
    for (int row = 0, nRows = variableList.size(); row < nRows; row++) {
      PwVariableQuery variable = (PwVariableQuery) variableList.get( row);
      data[row][0] = variable.getStepNumber().toString();
      data[row][1] = variable.getId().toString();
      data[row][2] = variable.getType();
      data[row][3] = partialPlan.getVariableParentName( variable.getParentId());
    }
    objectKeyColumnIndx = 1;
    stepNumberColumnIndx = 0;
    TableSorter sorter = new TableSorter( new DBTransactionTableModel( columnNames, data));
    variableTable = new DBTransactionTable( sorter, stepNumberColumnIndx, this);
    sorter.setTableHeader( variableTable.getTableHeader());
    contentScrollPane = new JScrollPane( variableTable);
    add( contentScrollPane, BorderLayout.SOUTH);

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
                               variableTable.getColumnModel().getTotalColumnWidth()),
                     (int) (headerJGoView.getDocumentSize().getHeight() +
                            variableTable.getRowCount() * variableTable.getRowHeight()));

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... '" + this.getName() + "'elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    handleEvent( ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init

  class QueryHeaderView extends TransactionHeaderView {

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
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick 

    private void mouseRightPopupMenu( final Point viewCoords) {
      JPopupMenu mouseRightPopup = new JPopupMenu();
      JMenuItem transByKeyItem = new JMenuItem( "Find Variable by " +
                                                ViewConstants.QUERY_VARIABLE_KEY_HEADER);
      createTransByKeyItem( transByKeyItem, variableList, contentScrollPane,
                            variableTable, objectKeyColumnIndx, VariableQueryView.this);
      mouseRightPopup.add( transByKeyItem);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    } // end mouseRightPopupMenu

  } // end class QueryHeaderView


} // end class VariableQueryView

