// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionContentView.java,v 1.4 2003-10-23 19:22:33 taylor Exp $
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
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.db.PwVariable;
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
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanningSequence
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private List transactionFieldList; // element TransactionField;

  /**
   * <code>TransactionContentView</code> - constructor 
   *
   * @param transactionList - <code>List</code> - 
   * @param headerJGoView - <code>TransactionHeaderView</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
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
      TransactionField keyField =
        new TransactionField( transaction.getId().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( keyField);
      keyField.setSize( (int) headerJGoView.getKeyNode().getSize().getWidth(),
                       (int) keyField.getSize().getHeight());
      x += headerJGoView.getKeyNode().getSize().getWidth();
      transactionFieldList.add( keyField);

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

      TransactionField objectKeyField =
        new TransactionField( transaction.getObjectId().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( objectKeyField);
      objectKeyField.setSize( (int) headerJGoView.getObjectKeyNode().getSize().getWidth(),
                             (int) objectKeyField.getSize().getHeight());
      x += headerJGoView.getObjectKeyNode().getSize().getWidth();
      transactionFieldList.add( objectKeyField);

      TransactionField stepNumField =
        new TransactionField( transaction.getStepNumber().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( stepNumField);
      stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                            (int) stepNumField.getSize().getHeight());
      x += headerJGoView.getStepNumNode().getSize().getWidth();
      transactionFieldList.add( stepNumField);

      TransactionField objectNameField =
        new TransactionField( getObjectName( transaction.getObjectId()),
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail(objectNameField );
      objectNameField.setSize( (int) headerJGoView.getObjectNameNode().getSize().getWidth(),
                           (int) objectNameField.getSize().getHeight());
      x += headerJGoView.getObjectNameNode().getSize().getWidth();
      transactionFieldList.add( objectNameField);

      TransactionField predicateField =
        new TransactionField( getPredicateName( transaction.getObjectId()),
                              new Point( x, y), JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( predicateField);
      predicateField.setSize( (int) headerJGoView.getPredicateNode().getSize().getWidth(),
                           (int) predicateField.getSize().getHeight());
      x += headerJGoView.getPredicateNode().getSize().getWidth();
      transactionFieldList.add( predicateField);

      y += keyField.getSize().getHeight();
      i++;
    }
  } // end renderTransactions

  private String getObjectName( Integer objectId) {
    String objectName = "";
    boolean isNameFound = false;
    // System.err.println( "\ngetObjectName: objectId " + objectId.toString());
    if (viewableObject instanceof PwPartialPlan) {
      PwConstraint constraint = ((PwPartialPlan) viewableObject).getConstraint( objectId);
      if (constraint != null) {
        objectName = constraint.getName();
        isNameFound = true;
        // System.err.println( "  isConstraint");
      }
      if (! isNameFound) {
        PwToken token = ((PwPartialPlan) viewableObject).getToken( objectId);
        if (token != null) {
          objectName = token.getPredicate().getName();
          isNameFound = true;
          // System.err.println( "  isToken");
        }
        if (! isNameFound) {
          PwVariable variable = ((PwPartialPlan) viewableObject).getVariable( objectId);
          if (variable != null) {
            objectName = variable.getType();
            // System.err.println( "  isVariable");
            isNameFound = true;
          }
        }
      }
    } else if (viewableObject instanceof PwPlanningSequence) {
      // accessing a step of a planSequence will cause Java data structures to be built
    }
    if (isNameFound) {
      // check name is less than column width
      trimName( objectName, TransactionHeaderView.OBJ_NAME_HEADER);
    }
    return objectName;
  } // end getObjectName 

  private String getPredicateName( Integer objectId) {
    String predicateName = "";
    boolean isNameFound = false;
    System.err.println( "\ngetPredicateName: objectId " + objectId.toString());
    if (viewableObject instanceof PwPartialPlan) {
      PwConstraint constraint = ((PwPartialPlan) viewableObject).getConstraint( objectId);
      if (constraint != null) {
        // predicateName = constraint.getName();
        isNameFound = true;
        // System.err.println( "  isConstraint");
      }
      if (! isNameFound) {
        PwToken token = ((PwPartialPlan) viewableObject).getToken( objectId);
        if (token != null) {
          predicateName = token.getPredicate().getName();
          isNameFound = true;
          // System.err.println( "  isToken");
        }
        if (! isNameFound) {
          PwVariable variable = ((PwPartialPlan) viewableObject).getVariable( objectId);
          if (variable != null) {
            List predicateNameList = new ArrayList();
            List tokenList = variable.getTokenList();
            System.err.println( "  tokenList.size " + tokenList.size());
            Iterator tokenListItr = tokenList.iterator();
            while (tokenListItr.hasNext()) {
              predicateNameList.add( ((PwToken) tokenListItr.next()).getPredicate().getName());
            }
            System.err.println( "  isVariable");
            System.err.println( "  predicateNameList " + predicateNameList);
            isNameFound = true;
          }
        }
      }
    } else if (viewableObject instanceof PwPlanningSequence) {
      // accessing a step of a planSequence will cause Java data structures to be built
    }
    if (isNameFound) {
      // check name is less than column width
      trimName( predicateName, TransactionHeaderView.PREDICATE_HEADER);
    }
    return predicateName;
  } // end getPredicateName 

  private String trimName( String name, String columnHeader) {
    int columnWidth = (int) headerJGoView.getObjectNameNode().getSize().getWidth();
    int nameWidth = SwingUtilities.computeStringWidth( vizView.getFontMetrics(), name);
    if (nameWidth > columnWidth) {
      name = name.substring( 0, columnHeader.length() - 2).concat( "..");
    }
    return name;
  } // end trimName



} // end class TransactionContentView



