// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionContentView.java,v 1.4 2004-05-13 20:24:07 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13oct03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.QueryResultField;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>DBTransactionContentView</code> - render values of plan db transaction
 *                                         object as QueryResultField's
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DBTransactionContentView extends JGoView {

  private List transactionList; // element PwDBTransaction
  private DBTransactionHeaderView headerJGoView;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanningSequence
  private VizView vizView; // PartialPlanView  or SequenceView

  private JGoDocument jGoDocument;
  private QueryResultField keyField;
  private List objectKeyFieldList;  // element QueryResultField
  private List transactionEntryList; // List of List of QueryResultField


  /**
   * <code>DBTransactionContentView</code> - constructor 
   *
   * @param transactionList - <code>List</code> - 
   * @param headerJGoView - <code>DBTransactionHeaderView</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public DBTransactionContentView( List transactionList, DBTransactionHeaderView headerJGoView,
                                 ViewableObject viewableObject, VizView vizView) {
    super();
    this.transactionList = transactionList;
    this.headerJGoView = headerJGoView;
    this.viewableObject = viewableObject;
    this.vizView = vizView;

    objectKeyFieldList = new ArrayList();
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument = this.getDocument();

    renderTransactionContent();
  }

  /**
   * <code>redraw</code>
   *
   */
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      renderTransactionContent();
    } //end run

  } // end class RedrawViewThread

  /**
   * <code>getTransactionList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getTransactionList() {
    return transactionList;
  }

  /**
   * <code>getTransactionEntryList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getTransactionEntryList() {
    return transactionEntryList;
  }

  private void renderTransactionContent() {
    getDocument().deleteContents();
    transactionEntryList = new ArrayList();
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    Iterator transItr = transactionList.iterator();
    int i = 1;
    boolean isDBTransactionQueryView = (vizView instanceof DBTransactionQueryView);
    boolean isObjectKeyField =
      ((isDBTransactionQueryView && // "In Range" only
        (((DBTransactionQueryView) vizView).getQuery().indexOf( "For ") == -1)) ||
       (vizView instanceof DBTransactionView));
    while (transItr.hasNext()) {
      List transactionEntry = new ArrayList();
      x = 0;
      PwDBTransaction transaction = (PwDBTransaction) transItr.next();
      keyField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_KEY_HEADER,
                              transaction.getId().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( keyField);
      keyField.setSize( (int) headerJGoView.getKeyNode().getSize().getWidth(),
                       (int) keyField.getSize().getHeight());
      x += headerJGoView.getKeyNode().getSize().getWidth();
      transactionEntry.add( keyField);

      QueryResultField nameField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_NAME_HEADER,
                              transaction.getName(), new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( nameField);
      nameField.setSize( (int) headerJGoView.getNameNode().getSize().getWidth(),
                         (int) nameField.getSize().getHeight());
      x += headerJGoView.getNameNode().getSize().getWidth();
      transactionEntry.add( nameField);

      QueryResultField sourceField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_SOURCE_HEADER,
                              transaction.getSource(), new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( sourceField);
      sourceField.setSize( (int) headerJGoView.getSourceNode().getSize().getWidth(),
                           (int) sourceField.getSize().getHeight());
      x += headerJGoView.getSourceNode().getSize().getWidth();
      transactionEntry.add( sourceField);

      if (isObjectKeyField) {
        QueryResultField objectKeyField =
          new QueryResultField( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER,
                                transaction.getObjectId().toString(), new Point( x, y),
                                JGoText.ALIGN_RIGHT, bgColor, viewableObject);
        objectKeyFieldList.add( objectKeyField);
        jGoDocument.addObjectAtTail( objectKeyField);
        objectKeyField.setSize( (int) headerJGoView.getObjectKeyNode().getSize().getWidth(),
                                (int) objectKeyField.getSize().getHeight());
        x += headerJGoView.getObjectKeyNode().getSize().getWidth();
        transactionEntry.add( objectKeyField);
      }

      if (isDBTransactionQueryView) {
        QueryResultField stepNumField =
          new QueryResultField( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER,
                                transaction.getStepNumber().toString(), new Point( x, y),
                                JGoText.ALIGN_RIGHT, bgColor, viewableObject, vizView);
        jGoDocument.addObjectAtTail( stepNumField);
        stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                              (int) stepNumField.getSize().getHeight());
        x += headerJGoView.getStepNumNode().getSize().getWidth();
        transactionEntry.add( stepNumField);
      }

      // String objectName = getObjectName( transaction.getObjectId());
      if (transaction.getInfo()[0] == null) {
        System.err.println( "0 key " + transaction.getId().toString() +
                            " name " + transaction.getName() +
                            " objectKey " + transaction.getObjectId().toString() +
                            " step " + transaction.getStepNumber().toString());
      }
      String objectName =
        NodeGenerics.trimName( transaction.getInfo()[0], headerJGoView.getObjectNameNode(),
                               vizView);
      QueryResultField objectNameField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER,
                              objectName, new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail(objectNameField );
      objectNameField.setSize( (int) headerJGoView.getObjectNameNode().getSize().getWidth(),
                           (int) objectNameField.getSize().getHeight());
      x += headerJGoView.getObjectNameNode().getSize().getWidth();
      transactionEntry.add( objectNameField);

        // new QueryResultField( getPredicateName( transaction.getObjectId()),
      if (transaction.getInfo()[1] == null) {
        System.err.println( "1 key " + transaction.getId().toString() +
                            " name " + transaction.getName() +
                            " objectKey " + transaction.getObjectId().toString() +
                            " step " + transaction.getStepNumber().toString());
      }
      String predicateName =
        NodeGenerics.trimName( transaction.getInfo()[1], headerJGoView.getPredicateNode(),
                               vizView);
      QueryResultField predicateField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER,
                              predicateName, new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( predicateField);
      predicateField.setSize( (int) headerJGoView.getPredicateNode().getSize().getWidth(),
                           (int) predicateField.getSize().getHeight());
      x += headerJGoView.getPredicateNode().getSize().getWidth();
      transactionEntry.add( predicateField);

        // new QueryResultField( getParameterName( transaction.getObjectId(), objectName),
      if (transaction.getInfo()[2] == null) {
        System.err.println( "2 key " + transaction.getId().toString() +
                            " name " + transaction.getName() +
                            " objectKey " + transaction.getObjectId().toString() +
                            " step " + transaction.getStepNumber().toString());
      }
      String parameterName =
         NodeGenerics.trimName( transaction.getInfo()[2], headerJGoView.getParameterNode(),
                                vizView);
      QueryResultField parameterField =
        new QueryResultField( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER,
                              parameterName, new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( parameterField);
      parameterField.setSize( (int) headerJGoView.getParameterNode().getSize().getWidth(),
                           (int) parameterField.getSize().getHeight());
      x += headerJGoView.getParameterNode().getSize().getWidth();
      transactionEntry.add( parameterField);

      y += keyField.getSize().getHeight();
      i++;
      transactionEntryList.add( transactionEntry);
    }
  } // end renderTransactions

  /**
   * <code>getObjectKeyField</code>
   *
   * @param lineIndex - <code>int</code> - 
   * @return - <code>QueryResultField</code> - 
   */
  public QueryResultField getObjectKeyField( int lineIndex) {
    return (QueryResultField) objectKeyFieldList.get( lineIndex);
  }

  /**
   * <code>scrollEntries</code>
   *
   * @param entryIndx - <code>int</code> - 
   */
  public void scrollEntries( int entryIndx) {
    int newPosition = ((int) keyField.getSize().getHeight()) * entryIndx;
    getVerticalScrollBar().setValue( newPosition);
  } // end scrollEntries




} // end class DBTransactionContentView



