//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: SequenceQueryWindow.java,v 1.32 2004-07-30 18:24:26 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.CreateSequenceViewThread;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.SequenceViewMenuItem;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
//import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DecisionQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TokenQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.VariableQueryView;
import gov.nasa.arc.planworks.viz.util.PWProgressMonitor;

/**
 * <code>SequenceQueryWindow</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The complete, displayable window for defining a query for the associated 
 * plan sequence.  
 */
public class SequenceQueryWindow extends JPanel implements MouseListener { 

  public static final String QUERY_RESULT_FRAME = "QueryResultFrame";
  public static final String QUERY_FOR_STEPS = "Steps ...";
  public static final String QUERY_FOR_TRANSACTIONS = "Transactions ...";
  public static final String QUERY_FOR_FREE_TOKENS = "Free Tokens ...";
  public static final String QUERY_FOR_UNBOUND_VARIABLES = "Unbound Variables ...";
  public static final String QUERY_FOR_ALL_DECISIONS = "All Decisions ...";

  public static final String STEPS_WHERE_CONSTRAINT_TRANSACTED =
    "Where Constraint Transacted ...";
  public static final String STEPS_WHERE_TOKEN_TRANSACTED = "Where Token Transacted ...";
  public static final String STEPS_WHERE_VARIABLE_TRANSACTED =
    "Where Variable Transacted ...";
  public static final String STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS =
    "With Non-Unit Variable Decisions";
  public static final String STEPS_WITH_RELAXATIONS = "With Relaxations";
  public static final String STEPS_WITH_RESTRICTIONS = "With Restrictions";
  public static final String STEPS_WITH_UNIT_VARIABLE_DECISIONS =
    "With Unit Variable Decisions";
  public static final List STEP_QUERIES;

  public static final String TRANSACTIONS_FOR_CONSTRAINT = "For Constraint ...";
  public static final String TRANSACTIONS_FOR_TOKEN = "For Token ...";
  public static final String TRANSACTIONS_FOR_VARIABLE = "For Variable ...";
  public static final String TRANSACTIONS_IN_RANGE = "In Range ...";
  public static final String TRANSACTIONS_OF_TYPE = "Of Type ...";
  public static final List DB_TRANSACTION_QUERIES;

  //public static final List CONSTRAINT_TRANSACTION_TYPES;
  public static Set CONSTRAINT_TRANSACTION_TYPES;
  public static final String CONSTRAINT_TRANSACTION_TYPE_ALL = "CONSTRAINT_ALL";
  //public static final List TOKEN_TRANSACTION_TYPES;
  public static Set TOKEN_TRANSACTION_TYPES;
  public static final String TOKEN_TRANSACTION_TYPE_ALL = "TOKEN_ALL";
  //public static final List VARIABLE_TRANSACTION_TYPES;
  public static Set VARIABLE_TRANSACTION_TYPES;
  public static final String VARIABLE_TRANSACTION_TYPE_ALL = "VARIABLE_ALL";

  public static final String FREE_TOKENS_AT_STEP = "At ...";
  public static final List FREE_TOKEN_QUERIES;

  public static final String UNBOUND_VARIABLES_AT_STEP = "At ...";
  public static final List UNBOUND_VARIABLE_QUERIES;

  public static final String ALL_DECISIONS_AT_STEP = "At ...";
  public static final List ALL_DECISIONS_QUERIES;

  public static final String APPLY_QUERY_BUTTON = "Apply Query";
  static {
		CONSTRAINT_TRANSACTION_TYPES = new HashSet();
		VARIABLE_TRANSACTION_TYPES = new HashSet();
		TOKEN_TRANSACTION_TYPES = new HashSet();

    STEP_QUERIES = new ArrayList();
    STEP_QUERIES.add( STEPS_WHERE_CONSTRAINT_TRANSACTED);
    STEP_QUERIES.add( STEPS_WHERE_TOKEN_TRANSACTED);
    STEP_QUERIES.add( STEPS_WHERE_VARIABLE_TRANSACTED);
    STEP_QUERIES.add( STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS); 
    // STEP_QUERIES.add( STEPS_WITH_RELAXATIONS); // PlanWriter cannot get this from Europa
    STEP_QUERIES.add( STEPS_WITH_RESTRICTIONS);
    STEP_QUERIES.add( STEPS_WITH_UNIT_VARIABLE_DECISIONS); 
    DB_TRANSACTION_QUERIES = new ArrayList();
    DB_TRANSACTION_QUERIES.add( TRANSACTIONS_FOR_CONSTRAINT);
    DB_TRANSACTION_QUERIES.add( TRANSACTIONS_FOR_TOKEN);
    DB_TRANSACTION_QUERIES.add( TRANSACTIONS_FOR_VARIABLE);
    DB_TRANSACTION_QUERIES.add( TRANSACTIONS_IN_RANGE);
    // DB_TRANSACTION_QUERIES.add(TRANSACTIONS_OF_TYPE);
    //CONSTRAINT_TRANSACTION_TYPES = MySQLDB.queryConstraintTransactionNames();
//     CONSTRAINT_TRANSACTION_TYPES = new ArrayList();
//     CONSTRAINT_TRANSACTION_TYPES.add( DbConstants.CONSTRAINT_CREATED);
//     CONSTRAINT_TRANSACTION_TYPES.add( DbConstants.CONSTRAINT_DELETED);
    //TOKEN_TRANSACTION_TYPES = MySQLDB.queryTokenTransactionNames();
//     TOKEN_TRANSACTION_TYPES = new ArrayList();
//     TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_CREATED);
//     TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_DELETED);
//     TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_FREED);
//     TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_INSERTED);
//     VARIABLE_TRANSACTION_TYPES = new ArrayList();
//     VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_CREATED);
//     VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DELETED);
    // VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_EMPTIED); // no MySql query yet
    // VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_RELAXED); // no MySql query yet
//     VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_RESET); 
//     VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_RESTRICTED);
//     VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_SPECIFIED);
    //VARIABLE_TRANSACTION_TYPES = MySQLDB.queryVariableTransactionNames();
    VARIABLE_TRANSACTION_TYPES.add("VARIABLE_RESTRICTED");
    VARIABLE_TRANSACTION_TYPES.add("VARIABLE_RELAXED");
    FREE_TOKEN_QUERIES = new ArrayList();
    FREE_TOKEN_QUERIES.add( FREE_TOKENS_AT_STEP);
    UNBOUND_VARIABLE_QUERIES = new ArrayList();
    UNBOUND_VARIABLE_QUERIES.add( UNBOUND_VARIABLES_AT_STEP);
    ALL_DECISIONS_QUERIES = new ArrayList();
    ALL_DECISIONS_QUERIES.add(ALL_DECISIONS_AT_STEP);
  }

  protected MDIInternalFrame sequenceQueryFrame;
  protected MDIDesktopFrame desktopFrame;
  protected ViewableObject viewable;
  protected ViewSet viewSet;
  protected GridBagConstraints constraints;
  protected MajorTypeComboBox majorTypeBox;
  protected MinorTypeComboBox minorTypeBox;
  protected GridBagLayout queryGridBag;
  protected GridBagConstraints queryConstraints;
  protected JPanel queryPanel;
  protected String transactionType;
  protected JTextField integerField;
  protected String keyString;
  protected JTextField intStartField;
  protected String startStepString;
  protected JTextField intEndField;
  protected String endStepString;
  protected JTextField intStepField;
  protected String stepString;
  protected ConstraintTransComboBox constraintTransComboBox;
  protected TokenTransComboBox tokenTransComboBox;
  protected VariableTransComboBox variableTransComboBox;
  protected int queryResultFrameCnt;
  protected ViewListener viewListener;
  protected PWProgressMonitor progressMonitor;
  protected boolean isProgressMonitorCancel;
  protected JButton queryButton;

  /**
   * <code>SequenceQueryWindow</code> - constructor 
   *
   * @param sequenceQueryFrame - <code>MDIInternalFrame</code> - 
   * @param desktopFrame - <code>MDIDesktopFrame</code> - 
   * @param viewable - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public SequenceQueryWindow( MDIInternalFrame sequenceQueryFrame, MDIDesktopFrame desktopFrame,
                              ViewableObject viewable, ViewSet viewSet) {
    MySQLDB.queryTransactionNames((PwPlanningSequence) viewable, CONSTRAINT_TRANSACTION_TYPES,
                                  TOKEN_TRANSACTION_TYPES, VARIABLE_TRANSACTION_TYPES);
    this.sequenceQueryFrame = sequenceQueryFrame;
    // for PWTestHelper.findComponentByName
    this.setName( sequenceQueryFrame.getTitle());
    this.desktopFrame = desktopFrame;
    this.viewable = viewable;
    this.viewSet = (SequenceViewSet) viewSet;
    viewListener = null;
    
    queryResultFrameCnt = 1;

    GridBagLayout gridBag = new GridBagLayout();
    constraints = new GridBagConstraints();
    setLayout(gridBag);
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.gridx = 0;
    constraints.gridy = 0;

    queryGridBag = new GridBagLayout();
    queryConstraints = new GridBagConstraints();
    queryPanel = new JPanel( queryGridBag);
    queryConstraints.weightx = 0;
    queryConstraints.weighty = 0;
    queryConstraints.gridx = 0;
    queryConstraints.gridy = 0;

    majorTypeBox = new MajorTypeComboBox();
    majorTypeBox.addItemListener(new MajorTypeListener( this));
    queryGridBag.setConstraints( majorTypeBox, queryConstraints);
    queryPanel.add( majorTypeBox);

    addMinorTypeBox( this);

    gridBag.setConstraints( queryPanel, constraints);
    add( queryPanel);
    
    GridBagLayout buttonGridBag = new GridBagLayout();
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    JPanel buttonPanel = new JPanel(buttonGridBag);
    buttonConstraints.weightx = 0;
    buttonConstraints.weighty = 0;
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 0;

    queryButton = new JButton(APPLY_QUERY_BUTTON);
    queryButton.addActionListener(new QueryListener(this));
    buttonGridBag.setConstraints(queryButton, buttonConstraints);
    buttonPanel.add(queryButton);

//     JButton resetButton = new JButton("Reset Query");
//     resetButton.addActionListener(new QueryListener(this));
//     buttonConstraints.gridx++;
//     buttonGridBag.setConstraints(resetButton, buttonConstraints);
//     buttonPanel.add(resetButton);

    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.anchor = GridBagConstraints.CENTER;
    gridBag.setConstraints(buttonPanel, constraints);
    add( buttonPanel);

    extendQueryForStepsWhereConstraintTransacted();
    addMouseListener( this);
  }

  private void addMinorTypeBox( SequenceQueryWindow queryWindow) {
    minorTypeBox = new MinorTypeComboBox();
    minorTypeBox.addItemListener( new MinorTypeListener( queryWindow));
    queryConstraints.gridx++;
    queryGridBag.setConstraints( minorTypeBox, queryConstraints);
    queryPanel.add( minorTypeBox);
  }
  
  private void refresh() {
    validate();
    repaint();
    sequenceQueryFrame.pack();
  }

  private void removeExtendedQueryComponents( int base) {
    // remove all components with index > base
    Component [] components = queryPanel.getComponents();
    for (int i = 0, n = components.length; i < n; i++) {
      Component comp = components[i];
      if (i > base) {
        queryPanel.remove( comp);
      }
    }
  } // end removeExtendedQueryComponents

  private void extendQueryForStepsWhereConstraintTransacted() {
    ItemListener[] listeners = minorTypeBox.getItemListeners();
    for (int i = 0, n = listeners.length; i < n; i++) {
      ItemListener listener = listeners[i];
      // System.err.println( listener);
      if (listener instanceof MinorTypeListener) {
        ((MinorTypeListener) listener).extendQueryForStepsWhereConstraintTransacted();
        break;
      }
    }
  } // extendQueryForStepsWhereConstraintTransacted

  /**
   * <code>getSequenceQueryFrame</code>
   *
   * @return - <code>MDIInternalFrame</code> - 
   */
  public MDIInternalFrame getSequenceQueryFrame() {
    return sequenceQueryFrame;
  }

  /**
   * <code>getQueryResultFrameCnt</code>
   *
   * @return - <code>int</code> - 
   */
  public int getQueryResultFrameCnt() {
    return queryResultFrameCnt;
  }

  /**
   * <code>resetQueryResultFrameCnt</code>
   *
   */
  public void resetQueryResultFrameCnt() {
    queryResultFrameCnt = 1;
  }

  /**
   * <code>setViewListener</code> - to be passed to Query Results views
   *
   * @param listener - <code>ViewListener</code> - 
   */
  public final void setViewListener( ViewListener listener) {
    viewListener = listener;
  }

  /**
   * <code>getProgressMonitor</code>
   *
   * @return - <code>PWProgressMonitor</code> - 
   */
  public final PWProgressMonitor getProgressMonitor() {
    return progressMonitor;
  }

  /**
   * <code>setProgressMonitor</code>
   *
   * @param monitor - <code>PWProgressMonitor</code> - 
   */
  public final void setProgressMonitor( PWProgressMonitor monitor) {
    progressMonitor = monitor;
  }

  /**
   * <code>QueryListener</code> -
   *                       ActionListener->QueryListener
   * The listener that provides the query functionality for the button.
   */
  class QueryListener implements ActionListener {

    private SequenceQueryWindow queryWindow;

    public QueryListener(SequenceQueryWindow queryWindow) {
      this.queryWindow = queryWindow;
    }

    public void actionPerformed( final ActionEvent ae) {
      final SwingWorker worker = new SwingWorker() {
          public Object construct() {
            try {
              queryButton.setEnabled( false);
              queryAndRenderView( ae);
            } finally {
              queryButton.setEnabled( true);
            }
            return null;
          }
        };
      worker.start();  
    } 

    private void queryAndRenderView( ActionEvent ae) {
      String stepsQuery = "", transactionsQuery = "";
      if (ae.getActionCommand().equals( APPLY_QUERY_BUTTON)) {
        long startTimeMSecs = System.currentTimeMillis();
        System.err.println( "Querying and Rendering Sequence Query View ...");
        progressMonitorThread( "Querying/Rendering Sequence Query View ...", 0, 6,
                               Thread.currentThread(), SequenceQueryWindow.this);
        if (! progressMonitorWait()) {
          return;
        }
        progressMonitor.setProgress( 3 * ViewConstants.MONITOR_MIN_MAX_SCALING);
        if (((String) queryWindow.majorTypeBox.getSelectedItem()).
            equals( QUERY_FOR_STEPS)) {
          List stepList = null;
          stepsQuery = (String) queryWindow.minorTypeBox.getSelectedItem();
          if (! stepsQuery.equals( "")) {
            if (stepsQuery.equals( STEPS_WHERE_CONSTRAINT_TRANSACTED)) {
              stepList = getStepsWhereConstraintTransacted();
            } else if (stepsQuery.equals( STEPS_WHERE_TOKEN_TRANSACTED)) {
              stepList = getStepsWhereTokenTransacted();
            } else if (stepsQuery.equals( STEPS_WHERE_VARIABLE_TRANSACTED)) {
              stepList = getStepsWhereVariableTransacted();
            } else if (stepsQuery.equals( STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS)) {
              stepList = ((PwPlanningSequence) queryWindow.viewable).
                getStepsWithNonUnitVariableBindingDecisions();
            } else if (stepsQuery.equals( STEPS_WITH_RELAXATIONS)) {
              stepList = ((PwPlanningSequence) queryWindow.viewable).
                getStepsWithRelaxations();
            } else if (stepsQuery.equals( STEPS_WITH_RESTRICTIONS)) {
              stepList = ((PwPlanningSequence) queryWindow.viewable).
                getStepsWithRestrictions();
            } else if (stepsQuery.equals( STEPS_WITH_UNIT_VARIABLE_DECISIONS)) {
              stepList = ((PwPlanningSequence) queryWindow.viewable).
                getStepsWithUnitVariableBindingDecisions();
            }
            if (stepList != null) {
              System.err.println( "   Query elapsed time: " +
                                  (System.currentTimeMillis() - startTimeMSecs) + " msecs.");
              ensureSequenceStepsViewExists();
              renderStepQueryFrame( stepsQuery, stepList, startTimeMSecs);
            }

          }
        } else if (((String) queryWindow.majorTypeBox.getSelectedItem()).
                   equals( QUERY_FOR_TRANSACTIONS)) {
          List transactionList = null;
          transactionsQuery = (String) queryWindow.minorTypeBox.getSelectedItem();
          if (! transactionsQuery.equals( "")) {
            if (transactionsQuery.equals( TRANSACTIONS_FOR_CONSTRAINT)) {
              transactionList = getTransactionsForConstraint();
            } else if (transactionsQuery.equals( TRANSACTIONS_FOR_TOKEN)) {
              transactionList = getTransactionsForToken();
            } else if (transactionsQuery.equals( TRANSACTIONS_FOR_VARIABLE)) {
              transactionList = getTransactionsForVariable();
            } else if (transactionsQuery.equals( TRANSACTIONS_IN_RANGE)) {
              transactionList =  getTransactionsInRange();
            }
            if (transactionList != null) {
              System.err.println( "   Query elapsed time: " +
                                  (System.currentTimeMillis() - startTimeMSecs) + " msecs.");
              ensureSequenceStepsViewExists();
              renderTransactionQueryFrame( transactionsQuery, transactionList, startTimeMSecs);
            }
          }
        } else if (((String) queryWindow.majorTypeBox.getSelectedItem()).
                   equals( QUERY_FOR_FREE_TOKENS)) {
          String tokenQuery = (String) queryWindow.minorTypeBox.getSelectedItem();
          List tokenList = getFreeTokensAtStep();
          if (tokenList != null) {
            System.err.println( "   Query elapsed time: " +
                                (System.currentTimeMillis() - startTimeMSecs) + " msecs.");
            ensureSequenceStepsViewExists();
            // System.err.println( "SequenceQueryWindow.tokenList " + tokenList);
            renderFreeTokenQueryFrame( tokenQuery, tokenList, startTimeMSecs);
          }
        } else if (((String) queryWindow.majorTypeBox.getSelectedItem()).
                   equals( QUERY_FOR_UNBOUND_VARIABLES)) {
          String variableQuery = (String) queryWindow.minorTypeBox.getSelectedItem();
          List variableList = getUnboundVariablesAtStep();
          if (variableList != null) {
            System.err.println( "   Query elapsed time: " +
                                (System.currentTimeMillis() - startTimeMSecs) + " msecs.");
            ensureSequenceStepsViewExists();
            // System.err.println( "variableList " + variableList);
            renderUnboundVariableQueryFrame( variableQuery, variableList, startTimeMSecs);
          }
        }
        else if(((String) queryWindow.majorTypeBox.getSelectedItem()).
                equals(QUERY_FOR_ALL_DECISIONS)) {
          String decisionQuery = (String) queryWindow.minorTypeBox.getSelectedItem();
          //List decisionList = getDecisionsAtStep();
          List variableList = getUnboundVariablesAtStep();
          List tokenList = getFreeTokensAtStep();
          if(variableList != null) {
            System.err.println("   Query elapsed time: " +
                               (System.currentTimeMillis() - startTimeMSecs) + " msecs.");
            ensureSequenceStepsViewExists();
            renderUnboundVariableQueryFrame(decisionQuery, variableList, startTimeMSecs);
          }
          if(tokenList != null) {
            System.err.println("   Query elapsed time: " +
                               (System.currentTimeMillis() - startTimeMSecs) + " msecs.");
            ensureSequenceStepsViewExists();
            renderFreeTokenQueryFrame(decisionQuery, tokenList, startTimeMSecs);
          }
        }
//       } else if (ae.getActionCommand().equals("Reset Query")) {
//         if (majorTypeBox != null) {
//           majorTypeBox.setSelectedItem( QUERY_FOR_STEPS);
//         }
//         if (minorTypeBox != null) {
//           minorTypeBox.removeAllItems();
//           Iterator stepsItr = STEP_QUERIES.iterator();
//           while (stepsItr.hasNext()) {
//             minorTypeBox.addItem( (String) stepsItr.next());
//           }
//           SequenceQueryWindow.this.removeExtendedQueryComponents( 1);
//           SequenceQueryWindow.this.extendQueryForStepsWhereConstraintTransacted();
//         }
//         queryWindow.refresh();
        // close monitor if queries return null
        isProgressMonitorCancel = true;
      }
    } // end queryAndRenderView

    private void ensureSequenceStepsViewExists() {
      // since SequenceStepsView is closable by user, we must recreate it prior to
      // creating a QueryResults window
      boolean sequenceStepsViewExists = false;
      List windowKeyList = new ArrayList( viewSet.getViews().keySet());
      Iterator windowListItr = windowKeyList.iterator();
      while (windowListItr.hasNext()) {
        Object windowKey = (Object) windowListItr.next();
        if ((windowKey instanceof Class) &&
            ((Class) windowKey).getName().equals
            ( PlanWorks.getViewClassName( ViewConstants.SEQUENCE_STEPS_VIEW))) {
          sequenceStepsViewExists = true;
        }
      }
      if (! sequenceStepsViewExists) {
        String seqName = ((PwPlanningSequence) viewable).getName();
        String seqUrl = ((PwPlanningSequence) viewable).getUrl();
        SequenceViewMenuItem seqViewItem =
          new SequenceViewMenuItem( seqName, seqUrl, seqName);
        Thread thread =
          new CreateSequenceViewThread( ViewConstants.SEQUENCE_STEPS_VIEW, seqViewItem);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    } // end ensureSequenceStepsViewExists

    private void renderStepQueryFrame( String stepsQuery, List stepList,
                                       long startTimeMSecs) {
      String frameTitle = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE + " for " +
        viewable.getName() + " - " + String.valueOf( queryResultFrameCnt);
      MDIInternalFrame stepQueryFrame =
        desktopFrame.createFrame( frameTitle, viewSet, true, true, true, true);
      viewSet.getViews().put( new String( QUERY_RESULT_FRAME + queryResultFrameCnt),
                              stepQueryFrame);
      Container contentPane = stepQueryFrame.getContentPane();
      StringBuffer queryStringBuf =
        getQueryStringBuf( QUERY_FOR_STEPS, stepsQuery);
      if (stepsQuery.indexOf( "Where") >= 0) {
        queryStringBuf.append( " Key ");
        if (keyString.equals( "")) {
          queryStringBuf.append( "All");
        } else {
          queryStringBuf.append( keyString);
        }
        queryStringBuf.append( " ").append( transactionType);
      }
      contentPane.add( new StepQueryView
                       ( stepList, keyString, queryStringBuf.toString(), viewable,
                         viewSet, SequenceQueryWindow.this, stepQueryFrame,
                         startTimeMSecs, viewListener));
      queryResultFrameCnt++;
      isProgressMonitorCancel = true;
    } // end renderStepQueryFrame

    private void renderTransactionQueryFrame( String transactionsQuery,
                                              List transactionList, long startTimeMSecs) {
      String frameTitle = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE + " for " +
        viewable.getName() + " - " + String.valueOf( queryResultFrameCnt);
      MDIInternalFrame transactionQueryFrame =
        desktopFrame.createFrame( frameTitle, viewSet, true, true, true, true);
      viewSet.getViews().put( new String( QUERY_RESULT_FRAME + queryResultFrameCnt),
                              transactionQueryFrame);
      Container contentPane = transactionQueryFrame.getContentPane();
      StringBuffer queryStringBuf =
        getQueryStringBuf( QUERY_FOR_TRANSACTIONS, transactionsQuery);
      if (transactionsQuery.equals( TRANSACTIONS_IN_RANGE)) {
        queryStringBuf.append( " StartStep ").append( startStepString);
        queryStringBuf.append( " EndStep ").append( endStepString);
      } else {
        queryStringBuf.append( " Key ").append( keyString);
      }
      contentPane.add( new DBTransactionQueryView
                       ( transactionList, queryStringBuf.toString(), viewable,
                         viewSet, SequenceQueryWindow.this, transactionQueryFrame,
                         startTimeMSecs, viewListener));
      queryResultFrameCnt++;
      isProgressMonitorCancel = true;
    } // end renderTransactionQueryFrame

    private void renderFreeTokenQueryFrame( String tokenQuery, List tokenList,
                                            long startTimeMSecs) {
      String frameTitle = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE + " for " +
        viewable.getName() + " - " + String.valueOf( queryResultFrameCnt);
      MDIInternalFrame tokenQueryFrame =
        desktopFrame.createFrame( frameTitle, viewSet, true, true, true, true);
      viewSet.getViews().put( new String( QUERY_RESULT_FRAME + queryResultFrameCnt),
                              tokenQueryFrame);
      Container contentPane = tokenQueryFrame.getContentPane();
      StringBuffer queryStringBuf =
        getQueryStringBuf( QUERY_FOR_FREE_TOKENS, tokenQuery);
      queryStringBuf.append( " Step ").append( stepString);
      contentPane.add( new TokenQueryView
                       ( tokenList, queryStringBuf.toString(), viewable,
                         viewSet, SequenceQueryWindow.this, tokenQueryFrame,
                         startTimeMSecs, viewListener));
      queryResultFrameCnt++;
      isProgressMonitorCancel = true;
    } // end renderTokenQueryFrame

    private void renderUnboundVariableQueryFrame( String variableQuery, List variableList,
                                                  long startTimeMSecs) {
      String frameTitle = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE + " for " +
        viewable.getName() + " - " + String.valueOf( queryResultFrameCnt);
      MDIInternalFrame variablesQueryFrame =
        desktopFrame.createFrame( frameTitle, viewSet, true, true, true, true);
      viewSet.getViews().put( new String( QUERY_RESULT_FRAME + queryResultFrameCnt),
                              variablesQueryFrame);
      Container contentPane = variablesQueryFrame.getContentPane();
      StringBuffer queryStringBuf =
        getQueryStringBuf( QUERY_FOR_UNBOUND_VARIABLES, variableQuery);
      queryStringBuf.append( " Step ").append( stepString);
      contentPane.add( new VariableQueryView
                       ( variableList, queryStringBuf.toString(), viewable, stepString,
                         viewSet, SequenceQueryWindow.this, variablesQueryFrame,
                         startTimeMSecs, viewListener));
      queryResultFrameCnt++;
      isProgressMonitorCancel = true;
    } // end renderVariableQueryFrame

//     private void renderDecisionQueryFrame(String decisionQuery, List decisionList, 
//                                           long startTimeMSecs) {
//       String frameTitle = ViewConstants.SEQUENCE_QUERY_RESULTS_TITLE + " for " +
//         viewable.getName() + " - " + String.valueOf( queryResultFrameCnt);
//       MDIInternalFrame decisionsQueryFrame = 
//         desktopFrame.createFrame( frameTitle, viewSet, true, true, true, true);
//       viewSet.getViews().put(new String(QUERY_RESULT_FRAME + queryResultFrameCnt), 
//                              decisionsQueryFrame);
//       Container contentPane = decisionsQueryFrame.getContentPane();
//       StringBuffer queryStringBuf = getQueryStringBuf(QUERY_FOR_ALL_DECISIONS, decisionQuery);
//       queryStringBuf.append(" Step ").append(stepString);
//       contentPane.add(new DecisionQueryView(decisionList, queryStringBuf.toString(), viewable,
//                                             viewSet, SequenceQueryWindow.this, 
//                                             decisionsQueryFrame, startTimeMSecs, viewListener));
//       queryResultFrameCnt++;
//       isProgressMonitorCancel = true;
//     }

    private StringBuffer getQueryStringBuf( String majorQuery, String minorQuery) {
      String majorQueryShort = majorQuery;
      int ellipsesIndx = majorQuery.indexOf( " ...");
      if (ellipsesIndx > 0) {
        majorQueryShort = majorQuery.substring( 0, ellipsesIndx);
      }
      StringBuffer queryStringBuf = new StringBuffer( majorQueryShort);
      String minorQueryShort = minorQuery;
      ellipsesIndx = minorQuery.indexOf( " ...");
      if (ellipsesIndx > 0) {
        minorQueryShort = minorQuery.substring( 0, ellipsesIndx);
      }
      queryStringBuf.append( " ").append( minorQueryShort);
      return queryStringBuf;
    } // end getQueryStringBuf

    private String getKeyString( String type) {
      keyString = (String) queryWindow.integerField.getText();
      if (((String) queryWindow.majorTypeBox.getSelectedItem()).
          equals( QUERY_FOR_TRANSACTIONS) && keyString.equals( "")) {
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), type + " key is required",
           "Invalid Query", JOptionPane.ERROR_MESSAGE);
        return "";
      }
      return keyString;
    } // end getKeyString

    private String getStepString() {
      stepString = (String) queryWindow.intStepField.getText();
      if (stepString.equals( "")) {
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), "Step is required",
           "Invalid Query", JOptionPane.ERROR_MESSAGE);
        return "";
      }
      return stepString;
    } // end getStepString

    private List getStepsWhereConstraintTransacted() {
      List stepList = null;
      try {
        String constraintKeyString = getKeyString( "Constraint");
        transactionType = (String) queryWindow.constraintTransComboBox.getSelectedItem();
        String queryTransactionType = transactionType;
        if (transactionType.equals( CONSTRAINT_TRANSACTION_TYPE_ALL)) {
          queryTransactionType = DbConstants.CONSTRAINT_ALL_TYPES;
        }
        if (! constraintKeyString.equals( "")) {
          stepList = ((PwPlanningSequence) queryWindow.viewable).
            getStepsWhereConstraintTransacted( new Integer( constraintKeyString),
                                               queryTransactionType);
        } else {
          stepList = ((PwPlanningSequence) queryWindow.viewable).
            getStepsWhereConstraintTransacted( queryTransactionType);
        }
      } catch (IllegalArgumentException e) {
        return null;
      }
      return stepList;
    } // end getStepsWhereConstraintTransacted

    private List getStepsWhereTokenTransacted() {
      List stepList = null;
      try {
        String tokenKeyString = getKeyString( "Token");
        transactionType = (String) queryWindow.tokenTransComboBox.getSelectedItem();
        String queryTransactionType = transactionType;
        if (transactionType.equals( TOKEN_TRANSACTION_TYPE_ALL)) {
          queryTransactionType = DbConstants.TOKEN_ALL_TYPES;
        }
        if (! tokenKeyString.equals( "")) {
          stepList = ((PwPlanningSequence) queryWindow.viewable).
            getStepsWhereTokenTransacted( new Integer( tokenKeyString), queryTransactionType);
        } else {
          stepList = ((PwPlanningSequence) queryWindow.viewable).
            getStepsWhereTokenTransacted( queryTransactionType);
        }
      } catch (IllegalArgumentException e) {
        return null;
      }
      return stepList;
    } // end getStepsWhereTokenTransacted

    private List getStepsWhereVariableTransacted() {
      List stepList = null;
      try {
        String variableKeyString = getKeyString( "Variable");
        transactionType = (String) queryWindow.variableTransComboBox.getSelectedItem();
        String queryTransactionType = transactionType;
        //FIXME: cheesy hack to get restrictions/relaxations in the steps query.
        //change query method to be more flexible with types and names
        if (transactionType.equals( VARIABLE_TRANSACTION_TYPE_ALL)) {
          queryTransactionType = DbConstants.VARIABLE_ALL_TYPES;
        }
        else if(transactionType.equals("VARIABLE_RESTRICTED")) {
          queryTransactionType = "%' && TransactionType='RESTRICTION";
        }
        else if(transactionType.equals("VARIABLE_RELAXED")) {
          queryTransactionType = "%' && TransactionType='RELAXATION";
        }
        if (! variableKeyString.equals( "")) {
          stepList = ((PwPlanningSequence) queryWindow.viewable).
            getStepsWhereVariableTransacted( new Integer( variableKeyString),
                                             queryTransactionType);
        } else {
          stepList = ((PwPlanningSequence) queryWindow.viewable).
            getStepsWhereVariableTransacted( queryTransactionType);
        }
      } catch (IllegalArgumentException e) {
        return null;
      }
      return stepList;
    } // end getStepsWhereVariableTransacted

    private List getTransactionsForConstraint() {
      List transactionList = null;
      try {
        String constraintKeyString = getKeyString( "Constraint");
        transactionList = ((PwPlanningSequence) queryWindow.viewable).
          getTransactionsForConstraint( new Integer( constraintKeyString));
      } catch (IllegalArgumentException e) {
        return null;
      }
      return transactionList;
    } // end getTransactionsForConstraint

    private List getTransactionsForToken() {
      List transactionList = null;
      try {
        String tokenKeyString = getKeyString( "Token");
        transactionList = ((PwPlanningSequence) queryWindow.viewable).
          getTransactionsForToken( new Integer( tokenKeyString));
      } catch (IllegalArgumentException e) {
        return null;
      }
      return transactionList;
    } // end getTransactionsForToken

    private List getTransactionsForVariable() {
      List transactionList = null;
      try {
        String variableKeyString = getKeyString( "Variable");
        transactionList = ((PwPlanningSequence) queryWindow.viewable).
          getTransactionsForVariable( new Integer( variableKeyString));
      } catch (IllegalArgumentException e) {
        return null;
      }
      return transactionList;
    } // end getTransactionsForVariable

    private List getTransactionsInRange() {
      List transactionList = null;
      try {
        startStepString = (String) queryWindow.intStartField.getText();
        if (startStepString.equals( "")) {
          JOptionPane.showMessageDialog
            (PlanWorks.getPlanWorks(), " StartStep is required",
             "Invalid Query", JOptionPane.ERROR_MESSAGE);
          return null;
        }
        int startStep = Integer.parseInt( startStepString);
        if (! isStepNumberValid( startStep)) {
          return null;
        }
        endStepString = (String) queryWindow.intEndField.getText();
        if (endStepString.equals( "")) {
          JOptionPane.showMessageDialog
            (PlanWorks.getPlanWorks(), " EndStep is required",
             "Invalid Query", JOptionPane.ERROR_MESSAGE);
          return null;
        }
        int endStep = Integer.parseInt( endStepString);
        if (! isStepNumberValid( endStep)) {
          return null;
        }
        transactionList = ((PwPlanningSequence) queryWindow.viewable).
          getTransactionsInRange( startStep, endStep);
      } catch (IllegalArgumentException e) {
        return null;
      }
      return transactionList;
    } // end getTransactionsInRange

    private boolean isStepNumberValid( int stepNumber) {
      List partialPlanNameList =
        ((PwPlanningSequence) queryWindow.viewable).getPartialPlanNamesList();
      int maxStepNumber =
        Integer.parseInt( ((String) partialPlanNameList.get
                           ( partialPlanNameList.size() - 1)).substring( 4));
      // System.err.println ( "isStepNumberValid: maxStepNumber " + maxStepNumber);
      if ((stepNumber >= 0) && (stepNumber <= maxStepNumber)) {
        return true;
      } else {
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(), "'" + stepNumber + "' not >= 0 and <= " + maxStepNumber,
           "Invalid Step Number", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    } // end isStepNumberValid

    private List getFreeTokensAtStep() {
      List freeTokenList = null;
      try {
        String stepString = getStepString();
        if (stepString.equals( "")) {
          return null;
        }
        int step = Integer.parseInt( stepString);
        if (! isStepNumberValid( step)) {
          return null;
        }
        // System.err.println( "getFreeTokensAtStep " + stepString);
        freeTokenList = ((PwPlanningSequence) queryWindow.viewable).
          getFreeTokensAtStep( step);
      } catch (IllegalArgumentException e) {
        return null;
      }
      catch(ResourceNotFoundException rnfe) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), rnfe.getMessage(),
                                      "ResourceNotFound",
                                      JOptionPane.ERROR_MESSAGE);
        return null;
      }
      return freeTokenList;
    } // end getFreeTokensAtStep

    private List getUnboundVariablesAtStep() {
      List unboundVariableList = null;
      try {
        String stepString = getStepString();
        if (stepString.equals( "")) {
          return null;
        }
        int step = Integer.parseInt( stepString);
        if (! isStepNumberValid( step)) {
          return null;
        }
        // System.err.println( "getUnboundVariablesAtStep" + stepString);
        unboundVariableList = ((PwPlanningSequence) queryWindow.viewable).
          getUnboundVariablesAtStep( step);
      } catch (IllegalArgumentException e) {
        return null;
      }
      catch(ResourceNotFoundException rnfe) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), rnfe.getMessage(),
                                      "ResourceNotFound",
                                      JOptionPane.ERROR_MESSAGE);
        return null;
      }
      return unboundVariableList;
    } // end getUnboundVariablesAtStep

    private List getDecisionsAtStep() {
      List retval = new ArrayList(2);
      try {
        String stepString = getStepString();
        if(stepString.equals("")) {
          return null;
        }
        int step = Integer.parseInt(stepString);
        if(!isStepNumberValid(step)) {
          return null;
        }
        retval.add(((PwPlanningSequence)queryWindow.viewable).getFreeTokensAtStep(step));
        retval.add(((PwPlanningSequence)queryWindow.viewable).getUnboundVariablesAtStep(step));
      }
      catch(IllegalArgumentException e) {
        return null;
      }
      catch(ResourceNotFoundException rnfe) {
        JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), rnfe.getMessage(),
                                      "ResourceNotFound",
                                      JOptionPane.ERROR_MESSAGE);
        return null;
      }
      return retval;
    }

  } // end class QueryListener


  public class MajorTypeComboBox extends JComboBox {

    public MajorTypeComboBox() {
      addItem( QUERY_FOR_STEPS);
      addItem( QUERY_FOR_TRANSACTIONS);
      addItem( QUERY_FOR_FREE_TOKENS);
      addItem( QUERY_FOR_UNBOUND_VARIABLES);
      addItem(QUERY_FOR_ALL_DECISIONS);
      setSize(58, 44);
      setEditable( false); // needed for JFCUnit JComboBoxMouseEventData
    }
  }


  public class MinorTypeComboBox extends JComboBox {

    public MinorTypeComboBox() {
      Iterator stepsItr = STEP_QUERIES.iterator();
      while (stepsItr.hasNext()) {
        addItem( (String) stepsItr.next());
      }
      setSize(108, 44);
      setEditable( false); // needed for JFCUnit JComboBoxMouseEventData
    }
  }


  public class ConstraintTransComboBox extends JComboBox {

    public ConstraintTransComboBox() {
      addItem( CONSTRAINT_TRANSACTION_TYPE_ALL);
      Iterator typesItr = CONSTRAINT_TRANSACTION_TYPES.iterator();
      while (typesItr.hasNext()) {
        addItem( (String) typesItr.next());
      }
      setSize(108, 44);
      setEditable( false); // needed for JFCUnit JComboBoxMouseEventData
    }
  }


  public class TokenTransComboBox extends JComboBox {

    public TokenTransComboBox() {
      addItem( TOKEN_TRANSACTION_TYPE_ALL);
      Iterator typesItr = TOKEN_TRANSACTION_TYPES.iterator();
      while (typesItr.hasNext()) {
        addItem( (String) typesItr.next());
      }
      setSize(108, 44);
      setEditable( false); // needed for JFCUnit JComboBoxMouseEventData
    }
  }


  public class VariableTransComboBox extends JComboBox {

    public VariableTransComboBox() {
      addItem( VARIABLE_TRANSACTION_TYPE_ALL);
      Iterator typesItr = VARIABLE_TRANSACTION_TYPES.iterator();
      while (typesItr.hasNext()) {
        addItem( (String) typesItr.next());
      }
      setSize(108, 44);
      setEditable( false); // needed for JFCUnit JComboBoxMouseEventData
    }
  }


  class MajorTypeListener implements ItemListener {

    private SequenceQueryWindow queryWindow;
    private String itemStateChangedFrom;

    public MajorTypeListener( SequenceQueryWindow queryWindow) {
      super();
      this.queryWindow = queryWindow;
      itemStateChangedFrom = null;
    }

    public void itemStateChanged(ItemEvent ie) {
      if (ie.getStateChange() == ItemEvent.DESELECTED) {
        itemStateChangedFrom = (String) ie.getItem();
      } else if (ie.getStateChange() == ItemEvent.SELECTED) {
        // remove everything except major combo box
        removeExtendedQueryComponents( 0);
        addMinorTypeBox( SequenceQueryWindow.this);
        minorTypeBox.removeAllItems();
        if (((String) ie.getItem()).equals( QUERY_FOR_STEPS)) {
          Iterator stepsItr = STEP_QUERIES.iterator();
          while (stepsItr.hasNext()) {
            minorTypeBox.addItem( (String) stepsItr.next());
          }
        } else if (((String) ie.getItem()).equals( QUERY_FOR_TRANSACTIONS)) {
          Iterator transactionsItr = DB_TRANSACTION_QUERIES.iterator();
          while (transactionsItr.hasNext()) {
            minorTypeBox.addItem( (String) transactionsItr.next());
          }
        } else if (((String) ie.getItem()).equals( QUERY_FOR_FREE_TOKENS)) {
          Iterator freeTokensItr = FREE_TOKEN_QUERIES.iterator();
          while (freeTokensItr.hasNext()) {
            minorTypeBox.addItem( (String) freeTokensItr.next());
          }
          addStepField();
        } else if (((String) ie.getItem()).equals( QUERY_FOR_UNBOUND_VARIABLES)) {
          Iterator unboundVariablesItr = UNBOUND_VARIABLE_QUERIES.iterator();
          while (unboundVariablesItr.hasNext()) {
            minorTypeBox.addItem( (String) unboundVariablesItr.next());
          }
          addStepField();
        }
        else if(((String)ie.getItem()).equals(QUERY_FOR_ALL_DECISIONS)) {
          Iterator allDecisionsIterator = ALL_DECISIONS_QUERIES.iterator();
          while(allDecisionsIterator.hasNext()) {
            minorTypeBox.addItem((String)allDecisionsIterator.next());
          }
          addStepField();
        }
        refresh();
      }
    } // end itemStateChanged

    private void addStepField() {
      JLabel keyLabel = new JLabel( "Step");
      queryConstraints.gridx++;
      queryGridBag.setConstraints( keyLabel, queryConstraints);
      queryPanel.add( keyLabel);
      intStepField = new JTextField( 5);
      queryConstraints.gridx++;
      queryGridBag.setConstraints( intStepField, queryConstraints);
      queryPanel.add( intStepField);
    } // end addStepField

  } // end class MajorTypeListener


  class MinorTypeListener implements ItemListener {

    private SequenceQueryWindow queryWindow;
    private String itemStateChangedFrom;

    public MinorTypeListener( SequenceQueryWindow queryWindow) {
      super();
      this.queryWindow = queryWindow;
      itemStateChangedFrom = null;
    }

    public void itemStateChanged(ItemEvent ie) {
      if (ie.getStateChange() == ItemEvent.DESELECTED) {
        itemStateChangedFrom = (String) ie.getItem();
      } else if (ie.getStateChange() == ItemEvent.SELECTED) {
        SequenceQueryWindow.this.removeExtendedQueryComponents( 1);

        if (((String) ie.getItem()).equals( STEPS_WHERE_CONSTRAINT_TRANSACTED)) {
          extendQueryForStepsWhereConstraintTransacted();
        } else if (((String) ie.getItem()).equals( STEPS_WHERE_TOKEN_TRANSACTED)) {
          extendQueryForStepsWhereTokenTransacted();
        } else if (((String) ie.getItem()).equals( STEPS_WHERE_VARIABLE_TRANSACTED)) {
          extendQueryForStepsWhereVariableTransacted();
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_FOR_CONSTRAINT)) {
          addIntegerField( "Key");
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_FOR_TOKEN)) {
          addIntegerField( "Key");
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_FOR_VARIABLE)) {
          addIntegerField( "Key");
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_IN_RANGE)) {
          extendQueryForTransactionsInRange();
        }
        refresh();
      }
    } // end itemStateChanged

    private void addIntegerField( String label) {
      JLabel keyLabel = new JLabel( label);
      queryConstraints.gridx++;
      queryGridBag.setConstraints( keyLabel, queryConstraints);
      queryPanel.add( keyLabel);
      integerField = new JTextField( 5);
      queryConstraints.gridx++;
      queryGridBag.setConstraints( integerField, queryConstraints);
      queryPanel.add( integerField);
    } // end addIntegerField

    private void extendQueryForStepsWhereConstraintTransacted() {
      addIntegerField( "Key");
      constraintTransComboBox = new ConstraintTransComboBox();
      queryConstraints.gridx++;
      queryGridBag.setConstraints( constraintTransComboBox, queryConstraints);
      queryPanel.add( constraintTransComboBox);
    } // end extendQueryForStepsWhereConstraintTransacted

    private void extendQueryForStepsWhereTokenTransacted() {
      addIntegerField( "Key");
      tokenTransComboBox = new TokenTransComboBox();
      queryConstraints.gridx++;
      queryGridBag.setConstraints( tokenTransComboBox, queryConstraints);
      queryPanel.add( tokenTransComboBox);
    } // end extendQueryForStepsWhereTokenTransacted

    private void extendQueryForStepsWhereVariableTransacted() {
      addIntegerField( "Key");
      variableTransComboBox = new VariableTransComboBox();
      queryConstraints.gridx++;
      queryGridBag.setConstraints( variableTransComboBox, queryConstraints);
      queryPanel.add( variableTransComboBox);
    } // end extendQueryForStepsWhereVariableTransacted

    private void extendQueryForTransactionsInRange() {
      JLabel startLabel = new JLabel( "StartStep");
      queryConstraints.gridx++;
      queryGridBag.setConstraints( startLabel, queryConstraints);
      queryPanel.add( startLabel);
      intStartField = new JTextField( 5);
      queryConstraints.gridx++;
      queryGridBag.setConstraints( intStartField, queryConstraints);
      queryPanel.add( intStartField);
 
      JLabel endLabel = new JLabel( "EndStep");
      queryConstraints.gridx++;
      queryGridBag.setConstraints( endLabel, queryConstraints);
      queryPanel.add( endLabel);
      intEndField = new JTextField( 5);
      queryConstraints.gridx++;
      queryGridBag.setConstraints( intEndField, queryConstraints);
      queryPanel.add( intEndField);
     } // end extendQueryForTransactionsInRange

  } // end class MinorTypeListener

  /**
   * mouseEntered - implement MouseListener - do nothing
   *
   * @param mouseEvent - MouseEvent 
   */
  public void mouseEntered( MouseEvent mouseEvent) {
    // System.err.println( "mouseEntered " + mouseEvent.getPoint());
  }

  /**
   * mouseExited - implement MouseListener -  do nothing
   *
   * @param mouseEvent - MouseEvent 
   */
  public void mouseExited( MouseEvent mouseEvent) {
    // System.err.println( "mouseExited " + mouseEvent.getPoint());
  }

  /**
   * mouseClicked - implement MouseListener -
   *
   * @param mouseEvent - MouseEvent 
   */ 
  public void mouseClicked( MouseEvent mouseEvent) {
    // System.err.println( "mouseClicked " + mouseEvent.getModifiers());
    if (MouseEventOSX.isMouseLeftClick( mouseEvent, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( mouseEvent, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( mouseEvent.getPoint());
    }
  } // end mouseClicked 

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem closeWindowsItem = new JMenuItem( "Close Query Results Windows");
    createCloseWindowsItem( closeWindowsItem);
    mouseRightPopup.add( closeWindowsItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createCloseWindowsItem( JMenuItem closeWindowsItem) {
    closeWindowsItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          List windowKeyList =
            new ArrayList( viewSet.getViews().keySet());
          Iterator windowListItr = windowKeyList.iterator();
          while (windowListItr.hasNext()) {
            Object windowKey = (Object) windowListItr.next();
            if (windowKey instanceof String) {
              String resultsWindowKey = (String) windowKey;
              if (resultsWindowKey.indexOf( QUERY_RESULT_FRAME) >= 0) {
                MDIInternalFrame window = 
                  (MDIInternalFrame) viewSet.getViews().get( resultsWindowKey);
                try {
                  window.setClosed( true);
                } catch ( PropertyVetoException pve){
                }
              }
            }
          }
          // force query window x,y coords back to near top of MDI frame
          SequenceQueryWindow.this.resetQueryResultFrameCnt();
        }
      });
  } // end createCloseWindowsItem

  /**
   * mousePressed - implement MouseListener - do nothing
   *
   * @param mouseEvent - MouseEvent 
   */
  public void mousePressed( MouseEvent mouseEvent) {
    // System.err.println( "mousePressed " + mouseEvent.getPoint());
  } // end mousePressed

  /**
   * mouseReleased - implement MouseListener - do nothing
   *
   * @param mouseEvent - MouseEvent
   */
  public void mouseReleased( MouseEvent mouseEvent) {
    // System.err.println( "mouseReleased " + mouseEvent.getPoint());
  } // end mouseReleased

  protected void progressMonitorThread( String title, int minValue, int maxValue,
                                        Thread monitoredThread, JPanel view) {
    Thread thread = new ProgressMonitorThread( title, minValue, maxValue, monitoredThread,
                                                view);
    thread.setPriority(Thread.MAX_PRIORITY);
    thread.start();
  }

  public class ProgressMonitorThread extends Thread {

    private String title;
    private int minValue;
    private int maxValue;
    private Thread monitoredThread;
    private JPanel view;

    public ProgressMonitorThread( String title, int minValue, int maxValue,
                                  Thread monitoredThread, JPanel view) {
      isProgressMonitorCancel = false;
      this.title = title;
      this.minValue = minValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
      this.maxValue = maxValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
      this.monitoredThread = monitoredThread;
      this.view = view;
    }  // end constructor

    public void run() {
      progressMonitor = new PWProgressMonitor( PlanWorks.getPlanWorks(), title, "",
                                              minValue, maxValue, monitoredThread, view);
      progressMonitor.setMillisToDecideToPopup( 0);
      progressMonitor.setMillisToPopup( 0);
      // these two must be set to 0 before calling setProgress, which puts up the dialog
      progressMonitor.setProgress( 0);

      while (! isProgressMonitorCancel) {
        try {
          Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL * 2);
        }
        catch (InterruptedException ie) {}
        // System.err.println( "ProgressMonitorThread wait for isProgressMonitorCancel = true");
      }
      progressMonitor.close();
      progressMonitor = null;
    } // end run

  } // end class ProgressMonitorThread

  private boolean progressMonitorWait() {
    int maxCycles = ViewConstants.WAIT_NUM_CYCLES;
    int numCycles = maxCycles;
    while ((progressMonitor == null) && numCycles != 0) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      numCycles--;
      // System.err.println( "progressMonitorWait numCycles " + numCycles);
    }
    if (numCycles == 0) {
      System.err.println( "progressMonitorWait failed after " +
                          (ViewConstants.WAIT_INTERVAL * maxCycles) + " msec for " +
                          "Querying/Rendering Sequence Query View");
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
    }
    // System.err.println( "progressMonitorWait took " + (maxCycles - numCycles) + " numCycles");
    return numCycles != 0;
  } // end progressMonitorWait

} // end class SequenceQueryWindow

