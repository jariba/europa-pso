// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepContentView.java,v 1.4 2003-10-28 18:01:24 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17oct03
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

import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.viz.nodes.StepField;
import gov.nasa.arc.planworks.viz.nodes.TransactionField;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>StepContentView</code> - render values of step object as TransactionField's
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class StepContentView extends JGoView {

  private List transactionList; // element PwTransaction
  private String query;
  private StepHeaderView headerJGoView;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanning Sequence
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private StepField keyField;

  /**
   * <code>StepContentView</code> - constructor 
   *
   * @param transactionList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param headerJGoView - <code>StepHeaderView</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public StepContentView( List transactionList, String query, StepHeaderView headerJGoView,
                                 ViewableObject viewableObject, VizView vizView) {
    super();
    this.transactionList = transactionList;
    this.query = query;
    this.headerJGoView = headerJGoView;
    this.viewableObject = viewableObject;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument = this.getDocument();

    renderStepContent( query);
  }

  /**
   * <code>redraw</code>
   *
   */
  public void redraw() {
    new RedrawViewThread().start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      renderStepContent( query);
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

  private void renderStepContent( String query) {
    getDocument().deleteContents();
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    Iterator transItr = transactionList.iterator();
    int i = 1;
    // System.err.println( "renderStepContent: transactionList " + transactionList);
    while (transItr.hasNext()) {
      x = 0;
      PwTransaction transaction = (PwTransaction) transItr.next();
      StepField stepNumField =
        new StepField( transaction.getStepNumber().toString(), new Point( x, y),
                       JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( stepNumField);
      stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                            (int) stepNumField.getSize().getHeight());
      x += headerJGoView.getStepNumNode().getSize().getWidth();

      keyField =
        new StepField( transaction.getId().toString(),
                       new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( keyField);
      keyField.setSize( (int) headerJGoView.getKeyNode().getSize().getWidth(),
                         (int) keyField.getSize().getHeight());
      x += headerJGoView.getKeyNode().getSize().getWidth();

      StepField typeField =
        new StepField( transaction.getType(),
                       new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( typeField);
      typeField.setSize( (int) headerJGoView.getTypeNode().getSize().getWidth(),
                         (int) typeField.getSize().getHeight());
      x += headerJGoView.getTypeNode().getSize().getWidth();

      if (query.indexOf( " With ") >= 0) {
        StepField objectKeyField =
          new StepField( transaction.getObjectId().toString(),
                         new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
        jGoDocument.addObjectAtTail(objectKeyField );
        objectKeyField.setSize( (int) headerJGoView.getObjectKeyNode().getSize().getWidth(),
                                 (int) objectKeyField.getSize().getHeight());
        x += headerJGoView.getObjectKeyNode().getSize().getWidth();
      }

      StepField objectNameField =
        new StepField( "",
                       new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail(objectNameField );
      objectNameField.setSize( (int) headerJGoView.getObjectNameNode().getSize().getWidth(),
                               (int) objectNameField.getSize().getHeight());
      x += headerJGoView.getObjectNameNode().getSize().getWidth();

      StepField predicateField =
        new StepField( "",
                       new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( predicateField);
      predicateField.setSize( (int) headerJGoView.getPredicateNode().getSize().getWidth(),
                              (int) predicateField.getSize().getHeight());
      x += headerJGoView.getPredicateNode().getSize().getWidth();

      TransactionField parameterField =
        new TransactionField( "",
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( parameterField);
      parameterField.setSize( (int) headerJGoView.getParameterNode().getSize().getWidth(),
                           (int) parameterField.getSize().getHeight());
      x += headerJGoView.getParameterNode().getSize().getWidth();

      y += stepNumField.getSize().getHeight();
      i++;
    }
  } // end renderSteps

  /**
   * <code>scrollEntries</code>
   *
   * @param entryIndx - <code>int</code> - 
   */
  public void scrollEntries( int entryIndx) {
    int newPosition = ((int) keyField.getSize().getHeight()) * entryIndx;
    getVerticalScrollBar().setValue( newPosition);
  } // end scrollEntries


} // end class StepContentView
