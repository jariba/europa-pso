// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionContentView.java,v 1.1 2003-10-16 21:40:40 taylor Exp $
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
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.viz.nodes.TransactionField;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>TransactionContentView</code> - render values of transaction object as TransactionField's
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionContentView extends JGoView {

  private List transactionList; // element PwTransaction
  private TransactionHeaderView headerJGoView;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanning Sequence
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private List transactionFieldList; // element TransactionField;

  public TransactionContentView( List transactionList, TransactionHeaderView headerJGoView,
                                 ViewableObject viewableObject, VizView vizView) {
    super();
    this.transactionList = transactionList;
    this.headerJGoView = headerJGoView;
    this.viewableObject = viewableObject;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument = this.getDocument();

    renderTransactionContent();
  }

  private void renderTransactionContent() {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    List transactionFieldList = new ArrayList();
    Iterator transItr = transactionList.iterator();
    int i = 1;
    while (transItr.hasNext()) {
      x = 0;
      PwTransaction transaction = (PwTransaction) transItr.next();
      TransactionField idField =
        new TransactionField( transaction.getId().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( idField);
      idField.setSize( (int) headerJGoView.getIdNode().getSize().getWidth(),
                       (int) idField.getSize().getHeight());
      x += headerJGoView.getIdNode().getSize().getWidth();
      transactionFieldList.add( idField);

      TransactionField typeField =
        new TransactionField( transaction.getType(), new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( typeField);
      typeField.setSize( (int) headerJGoView.getTypeNode().getSize().getWidth(),
                         (int) typeField.getSize().getHeight());
      x += headerJGoView.getTypeNode().getSize().getWidth();
      transactionFieldList.add( typeField);

      TransactionField sourceField =
        new TransactionField( transaction.getSource(), new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( sourceField);
      sourceField.setSize( (int) headerJGoView.getSourceNode().getSize().getWidth(),
                           (int) sourceField.getSize().getHeight());
      x += headerJGoView.getSourceNode().getSize().getWidth();
      transactionFieldList.add( sourceField);

      TransactionField objectIdField =
        new TransactionField( transaction.getObjectId().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( objectIdField);
      objectIdField.setSize( (int) headerJGoView.getObjectIdNode().getSize().getWidth(),
                             (int) objectIdField.getSize().getHeight());
      x += headerJGoView.getObjectIdNode().getSize().getWidth();
      transactionFieldList.add( objectIdField);

      String stepNumString = transaction.getStepNumber().toString();
      TransactionField stepNumField =
        new TransactionField( transaction.getStepNumber().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( stepNumField);
      stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                            (int) stepNumField.getSize().getHeight());
      x += headerJGoView.getStepNumNode().getSize().getWidth();
      transactionFieldList.add( stepNumField);

      y += idField.getSize().getHeight();
      i ++;
    }
  } // end renderTransactions


} // end class TransactionContentView
