//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: SequenceQueryWindow.java,v 1.12 2003-12-03 02:29:51 taylor Exp $
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
import java.util.Iterator;
import java.util.List;
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
// import gov.nasa.arc.planworks.db.util.SequenceContentSpec;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.StepQueryView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TransactionQueryView;

/**
 * <code>SequenceQueryWindow</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The complete, displayable window for defining a query for the associated 
 * plan sequence.  
 */
public class SequenceQueryWindow extends JPanel implements MouseListener { 

  private static final String QUERY_RESULT_FRAME = "QueryResultFrame";
  private static final String QUERY_VERB = "Get";
  private static final String QUERY_FOR_STEPS = "Steps ...";
  private static final String QUERY_FOR_TRANSACTIONS = "Transactions ...";
  private static final String STEPS_WHERE_CONSTRAINT_TRANSACTED =
    "Where Constraint Transacted ...";
  private static final String STEPS_WHERE_TOKEN_TRANSACTED = "Where Token Transacted ...";
  private static final String STEPS_WHERE_VARIABLE_TRANSACTED =
    "Where Variable Transacted ...";
  private static final String STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS =
    "With Non-Unit Variable Decisions";
  private static final String STEPS_WITH_RELAXATIONS = "With Relaxations";
  private static final String STEPS_WITH_RESTRICTIONS = "With Restrictions";
  private static final String STEPS_WITH_UNIT_VARIABLE_DECISIONS =
    "With Unit Variable Decisions";
  private static final List STEP_QUERIES;
  private static final String TRANSACTIONS_FOR_CONSTRAINT = "For Constraint ...";
  private static final String TRANSACTIONS_FOR_TOKEN = "For Token ...";
  private static final String TRANSACTIONS_FOR_VARIABLE = "For Variable ...";
  private static final String TRANSACTIONS_IN_RANGE = "In Range ...";
  private static final List TRANSACTION_QUERIES;
  private static final List CONSTRAINT_TRANSACTION_TYPES;
  private static final List TOKEN_TRANSACTION_TYPES;
  private static final List VARIABLE_TRANSACTION_TYPES;

  static {
    STEP_QUERIES = new ArrayList();
    STEP_QUERIES.add( STEPS_WHERE_CONSTRAINT_TRANSACTED);
    STEP_QUERIES.add( STEPS_WHERE_TOKEN_TRANSACTED);
    STEP_QUERIES.add( STEPS_WHERE_VARIABLE_TRANSACTED);
    STEP_QUERIES.add( STEPS_WITH_NON_UNIT_VARIABLE_DECISIONS); 
    // STEP_QUERIES.add( STEPS_WITH_RELAXATIONS); // PlanWriter cannot get this from Europa
    STEP_QUERIES.add( STEPS_WITH_RESTRICTIONS);
    STEP_QUERIES.add( STEPS_WITH_UNIT_VARIABLE_DECISIONS); 
    TRANSACTION_QUERIES = new ArrayList();
    TRANSACTION_QUERIES.add( TRANSACTIONS_FOR_CONSTRAINT);
    TRANSACTION_QUERIES.add( TRANSACTIONS_FOR_TOKEN);
    TRANSACTION_QUERIES.add( TRANSACTIONS_FOR_VARIABLE);
    TRANSACTION_QUERIES.add( TRANSACTIONS_IN_RANGE);
    CONSTRAINT_TRANSACTION_TYPES = new ArrayList();
    CONSTRAINT_TRANSACTION_TYPES.add( DbConstants.CONSTRAINT_CREATED);
    CONSTRAINT_TRANSACTION_TYPES.add( DbConstants.CONSTRAINT_DELETED);
    TOKEN_TRANSACTION_TYPES = new ArrayList();
    TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_CREATED);
    TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_DELETED);
    TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_FREED);
    TOKEN_TRANSACTION_TYPES.add( DbConstants.TOKEN_INSERTED);
    VARIABLE_TRANSACTION_TYPES = new ArrayList();
    VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_CREATED);
    VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DELETED);
    // VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_EMPTIED); // no MySql query yet
    // VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_RELAXED); // no MySql query yet
    VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_RESET); 
    VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_RESTRICTED);
    VARIABLE_TRANSACTION_TYPES.add( DbConstants.VARIABLE_DOMAIN_SPECIFIED);
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
  protected ConstraintTransComboBox constraintTransComboBox;
  protected TokenTransComboBox tokenTransComboBox;
  protected VariableTransComboBox variableTransComboBox;
  protected int queryResultFrameCnt;


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
    this.sequenceQueryFrame = sequenceQueryFrame;
    this.desktopFrame = desktopFrame;
    this.viewable = viewable;
    this.viewSet = (SequenceViewSet) viewSet;
    
    queryResultFrameCnt = 0;

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

    minorTypeBox = new MinorTypeComboBox();
    minorTypeBox.addItemListener(new MinorTypeListener( this));
    queryConstraints.gridx++;
    queryGridBag.setConstraints( minorTypeBox, queryConstraints);
    queryPanel.add( minorTypeBox);

    gridBag.setConstraints( queryPanel, constraints);
    add( queryPanel);
    
    GridBagLayout buttonGridBag = new GridBagLayout();
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    JPanel buttonPanel = new JPanel(buttonGridBag);
    buttonConstraints.weightx = 0;
    buttonConstraints.weighty = 0;
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 0;

    JButton queryButton = new JButton("Apply Query");
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

  private void refresh() {
    validate();
    repaint();
    sequenceQueryFrame.pack();
  }

  private void removeExtendedQueryComponents() {
    // remove all components with index > 1
    Component [] components = queryPanel.getComponents();
    for (int i = 0, n = components.length; i < n; i++) {
      Component comp = components[i];
      if (i > 1) {
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
   * <code>QueryListener</code> -
   *                       ActionListener->QueryListener
   * The listener that provides the query functionality for the button.
   */
  class QueryListener implements ActionListener {

    private SequenceQueryWindow queryWindow;

    public QueryListener(SequenceQueryWindow queryWindow) {
      this.queryWindow = queryWindow;
    }

    public void actionPerformed(ActionEvent ae) {
      String stepsQuery = "", transactionsQuery = "";
      if (ae.getActionCommand().equals("Apply Query")) {
        long startTimeMSecs = System.currentTimeMillis();
        System.err.println( "Querying and Rendering Sequence Query View ...");
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
        }
      } else if (ae.getActionCommand().equals("Reset Query")) {
        if (majorTypeBox != null) {
          majorTypeBox.setSelectedItem( QUERY_FOR_STEPS);
        }
        if (minorTypeBox != null) {
          minorTypeBox.removeAllItems();
          Iterator stepsItr = STEP_QUERIES.iterator();
          while (stepsItr.hasNext()) {
            minorTypeBox.addItem( (String) stepsItr.next());
          }
          SequenceQueryWindow.this.removeExtendedQueryComponents();
          SequenceQueryWindow.this.extendQueryForStepsWhereConstraintTransacted();
        }
        queryWindow.refresh();
      }
    } // end actionPerformed

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
            ( PlanWorks.planWorks.viewClassNameMap.get( PlanWorks.SEQUENCE_STEPS_VIEW))) {
          sequenceStepsViewExists = true;
        }
      }
      if (! sequenceStepsViewExists) {
        String seqName = ((PwPlanningSequence) viewable).getName();
        String seqUrl = ((PwPlanningSequence) viewable).getUrl();
        SequenceViewMenuItem seqViewItem =
          new SequenceViewMenuItem( seqName, seqUrl, seqName);
        boolean isInvokeAndWait = true;
        new CreateSequenceViewThread( PlanWorks.SEQUENCE_STEPS_VIEW, seqViewItem,
                                      isInvokeAndWait).start();
      }
    } // end ensureSequenceStepsViewExists

    private void renderStepQueryFrame( String stepsQuery, List stepList,
                                       long startTimeMSecs) {
      MDIInternalFrame stepQueryFrame =
        desktopFrame.createFrame( ContentSpec.SEQUENCE_QUERY_RESULTS_TITLE +
                                  " for " + viewable.getName(),
                                  viewSet, true, true, true, true);
      viewSet.getViews().put( new String( QUERY_RESULT_FRAME + queryResultFrameCnt),
                              stepQueryFrame);
      Container contentPane = stepQueryFrame.getContentPane();
      StringBuffer queryStringBuf = new StringBuffer( QUERY_FOR_STEPS);
      int ellipsesIndx = stepsQuery.indexOf( " ...");
      if (ellipsesIndx > 0) {
        stepsQuery = stepsQuery.substring( 0, ellipsesIndx);
      }
      queryStringBuf.append( " ").append( stepsQuery);
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
                         startTimeMSecs));
      queryResultFrameCnt++;
    } // end renderStepQueryFrame

    private void renderTransactionQueryFrame( String transactionsQuery,
                                              List transactionList, long startTimeMSecs) {
      MDIInternalFrame transactionQueryFrame =
        desktopFrame.createFrame( ContentSpec.SEQUENCE_QUERY_RESULTS_TITLE +
                                  " for " + viewable.getName(),
                                  viewSet, true, true, true, true);
      viewSet.getViews().put( new String( QUERY_RESULT_FRAME + queryResultFrameCnt),
                              transactionQueryFrame);
      Container contentPane = transactionQueryFrame.getContentPane();
      StringBuffer queryStringBuf = new StringBuffer( QUERY_FOR_TRANSACTIONS);
      String transactionsQueryShort = transactionsQuery;
      int ellipsesIndx = transactionsQuery.indexOf( " ...");
      if (ellipsesIndx > 0) {
        transactionsQueryShort = transactionsQuery.substring( 0, ellipsesIndx);
      }
      queryStringBuf.append( " ").append( transactionsQueryShort);
      if (transactionsQuery.equals( TRANSACTIONS_IN_RANGE)) {
        queryStringBuf.append( " StartStep ").append( startStepString);
        queryStringBuf.append( " EndStep ").append( endStepString);
      } else {
        queryStringBuf.append( " Key ").append( keyString);
      }
      contentPane.add( new TransactionQueryView
                       ( transactionList, queryStringBuf.toString(), viewable,
                         viewSet, SequenceQueryWindow.this, transactionQueryFrame,
                         startTimeMSecs));
      queryResultFrameCnt++;
    } // end renderTransactionQueryFrame


    private String getKeyString( String type) {
      keyString = (String) queryWindow.integerField.getText();
      if (((String) queryWindow.majorTypeBox.getSelectedItem()).
          equals( QUERY_FOR_TRANSACTIONS) && keyString.equals( "")) {
        JOptionPane.showMessageDialog
          (PlanWorks.planWorks, type + " Key is required",
           "Invalid Query", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      return keyString;
    } // end getKeyString

    private List getStepsWhereConstraintTransacted() {
      List stepList = null;
      try {
        String constraintKeyString = getKeyString( "Constraint");
        transactionType = (String) queryWindow.constraintTransComboBox.getSelectedItem();
        String queryTransactionType = transactionType;
        if (transactionType.equals( "CONSTRAINT_ALL")) {
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
        if (transactionType.equals( "TOKEN_ALL")) {
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
        if (transactionType.equals( "VARIABLE_ALL")) {
          queryTransactionType = DbConstants.VARIABLE_ALL_TYPES;
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
            (PlanWorks.planWorks, " StartStep is required",
             "Invalid Query", JOptionPane.ERROR_MESSAGE);
          return null;
        }
        endStepString = (String) queryWindow.intEndField.getText();
        if (endStepString.equals( "")) {
          JOptionPane.showMessageDialog
            (PlanWorks.planWorks, " EndStep is required",
             "Invalid Query", JOptionPane.ERROR_MESSAGE);
          return null;
        }
        transactionList = ((PwPlanningSequence) queryWindow.viewable).
          getTransactionsInRange( Integer.parseInt( startStepString),
                                  Integer.parseInt( endStepString));
      } catch (IllegalArgumentException e) {
        return null;
      }
      return transactionList;
    } // end getTransactionsInRange

  } // end class QueryListener


  class MajorTypeComboBox extends JComboBox {

    public MajorTypeComboBox() {
      addItem( QUERY_FOR_STEPS);
      addItem(QUERY_FOR_TRANSACTIONS );
      setSize(58, 44);
    }
  }


  class MinorTypeComboBox extends JComboBox {

    public MinorTypeComboBox() {
      Iterator stepsItr = STEP_QUERIES.iterator();
      while (stepsItr.hasNext()) {
        addItem( (String) stepsItr.next());
      }
      setSize(108, 44);
    }
  }


  class ConstraintTransComboBox extends JComboBox {

    public ConstraintTransComboBox() {
      addItem( "CONSTRAINT_ALL");
      Iterator typesItr = CONSTRAINT_TRANSACTION_TYPES.iterator();
      while (typesItr.hasNext()) {
        addItem( (String) typesItr.next());
      }
      setSize(108, 44);
    }
  }


  class TokenTransComboBox extends JComboBox {

    public TokenTransComboBox() {
      addItem( "TOKEN_ALL");
      Iterator typesItr = TOKEN_TRANSACTION_TYPES.iterator();
      while (typesItr.hasNext()) {
        addItem( (String) typesItr.next());
      }
      setSize(108, 44);
    }
  }


  class VariableTransComboBox extends JComboBox {

    public VariableTransComboBox() {
      addItem( "VARIABLE_ALL");
      Iterator typesItr = VARIABLE_TRANSACTION_TYPES.iterator();
      while (typesItr.hasNext()) {
        addItem( (String) typesItr.next());
      }
      setSize(108, 44);
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
        minorTypeBox.removeAllItems();

        if (((String) ie.getItem()).equals( QUERY_FOR_STEPS)) {
          Iterator stepsItr = STEP_QUERIES.iterator();
          while (stepsItr.hasNext()) {
            minorTypeBox.addItem( (String) stepsItr.next());
          }
        } else if (((String) ie.getItem()).equals( QUERY_FOR_TRANSACTIONS)) {
          Iterator transactionsItr = TRANSACTION_QUERIES.iterator();
          while (transactionsItr.hasNext()) {
            minorTypeBox.addItem( (String) transactionsItr.next());
          }
        }
        refresh();
      }
    }
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
        SequenceQueryWindow.this.removeExtendedQueryComponents();

        if (((String) ie.getItem()).equals( STEPS_WHERE_CONSTRAINT_TRANSACTED)) {
          extendQueryForStepsWhereConstraintTransacted();
        } else if (((String) ie.getItem()).equals( STEPS_WHERE_TOKEN_TRANSACTED)) {
          extendQueryForStepsWhereTokenTransacted();
        } else if (((String) ie.getItem()).equals( STEPS_WHERE_VARIABLE_TRANSACTED)) {
          extendQueryForStepsWhereVariableTransacted();
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_FOR_CONSTRAINT)) {
          addIntegerField();
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_FOR_TOKEN)) {
          addIntegerField();
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_FOR_VARIABLE)) {
          addIntegerField();
        } else if (((String) ie.getItem()).equals( TRANSACTIONS_IN_RANGE)) {
          extendQueryForTransactionsInRange();
        }
        refresh();
      }
    } // end itemStateChanged

    private void addIntegerField() {
      JLabel keyLabel = new JLabel( "Key");
      queryConstraints.gridx++;
      queryGridBag.setConstraints( keyLabel, queryConstraints);
      queryPanel.add( keyLabel);
      integerField = new JTextField( 5);
      queryConstraints.gridx++;
      queryGridBag.setConstraints( integerField, queryConstraints);
      queryPanel.add( integerField);
    } // end addIntegerField

    private void extendQueryForStepsWhereConstraintTransacted() {
      addIntegerField();
      constraintTransComboBox = new ConstraintTransComboBox();
      queryConstraints.gridx++;
      queryGridBag.setConstraints( constraintTransComboBox, queryConstraints);
      queryPanel.add( constraintTransComboBox);
    } // end extendQueryForStepsWhereConstraintTransacted

    private void extendQueryForStepsWhereTokenTransacted() {
      addIntegerField();
      tokenTransComboBox = new TokenTransComboBox();
      queryConstraints.gridx++;
      queryGridBag.setConstraints( tokenTransComboBox, queryConstraints);
      queryPanel.add( tokenTransComboBox);
    } // end extendQueryForStepsWhereTokenTransacted

    private void extendQueryForStepsWhereVariableTransacted() {
      addIntegerField();
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
    JMenuItem discardWindowsItem = new JMenuItem( "Discard Query Results Windows");
    createDiscardWindowsItem( discardWindowsItem);
    mouseRightPopup.add( discardWindowsItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createDiscardWindowsItem( JMenuItem discardWindowsItem) {
    discardWindowsItem.addActionListener( new ActionListener() {
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
        }
      });
  } // end createDiscardWindowsItem

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


} // end class SequenceQueryWindow

