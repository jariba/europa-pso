// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionContentView.java,v 1.12 2003-12-12 01:23:04 taylor Exp $
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

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwParameter;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TransactionField;
import gov.nasa.arc.planworks.viz.partialPlan.transaction.TransactionView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TransactionQueryView;
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
  private TransactionField keyField;
  private List objectKeyFieldList;  // element TransactionField


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

  private void renderTransactionContent() {
    getDocument().deleteContents();
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    Iterator transItr = transactionList.iterator();
    int i = 1;
    boolean isTransactionQueryView = (vizView instanceof TransactionQueryView);
    boolean isObjectKeyField =
      ((isTransactionQueryView && // "In Range" only
        (((TransactionQueryView) vizView).getQuery().indexOf( "For ") == -1)) ||
       (vizView instanceof TransactionView));
    while (transItr.hasNext()) {
      x = 0;
      PwTransaction transaction = (PwTransaction) transItr.next();
      keyField =
        new TransactionField( transaction.getId().toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( keyField);
      keyField.setSize( (int) headerJGoView.getKeyNode().getSize().getWidth(),
                       (int) keyField.getSize().getHeight());
      x += headerJGoView.getKeyNode().getSize().getWidth();

      TransactionField typeField =
        new TransactionField( transaction.getType(), new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( typeField);
      typeField.setSize( (int) headerJGoView.getTypeNode().getSize().getWidth(),
                         (int) typeField.getSize().getHeight());
      x += headerJGoView.getTypeNode().getSize().getWidth();

      TransactionField sourceField =
        new TransactionField( transaction.getSource(), new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( sourceField);
      sourceField.setSize( (int) headerJGoView.getSourceNode().getSize().getWidth(),
                           (int) sourceField.getSize().getHeight());
      x += headerJGoView.getSourceNode().getSize().getWidth();

      if (isObjectKeyField) {
        TransactionField objectKeyField =
          new TransactionField( transaction.getObjectId().toString(), new Point( x, y),
                                JGoText.ALIGN_RIGHT, bgColor, viewableObject);
        objectKeyFieldList.add( objectKeyField);
        jGoDocument.addObjectAtTail( objectKeyField);
        objectKeyField.setSize( (int) headerJGoView.getObjectKeyNode().getSize().getWidth(),
                                (int) objectKeyField.getSize().getHeight());
        x += headerJGoView.getObjectKeyNode().getSize().getWidth();
      }

      if (isTransactionQueryView) {
        TransactionField stepNumField =
          new TransactionField( transaction.getStepNumber().toString(), new Point( x, y),
                                JGoText.ALIGN_RIGHT, bgColor, viewableObject, vizView);
        jGoDocument.addObjectAtTail( stepNumField);
        stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                              (int) stepNumField.getSize().getHeight());
        x += headerJGoView.getStepNumNode().getSize().getWidth();
      }

      // String objectName = getObjectName( transaction.getObjectId());
      if (transaction.getInfo()[0] == null) {
        System.err.println( "0 key " + transaction.getId().toString() +
                            " type " + transaction.getType() +
                            " objectKey " + transaction.getObjectId().toString() +
                            " step " + transaction.getStepNumber().toString());
      }
      String objectName =
        NodeGenerics.trimName( transaction.getInfo()[0], headerJGoView.getObjectNameNode(),
                               vizView);
      TransactionField objectNameField =
        new TransactionField( objectName, new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail(objectNameField );
      objectNameField.setSize( (int) headerJGoView.getObjectNameNode().getSize().getWidth(),
                           (int) objectNameField.getSize().getHeight());
      x += headerJGoView.getObjectNameNode().getSize().getWidth();

        // new TransactionField( getPredicateName( transaction.getObjectId()),
      if (transaction.getInfo()[1] == null) {
        System.err.println( "1 key " + transaction.getId().toString() +
                            " type " + transaction.getType() +
                            " objectKey " + transaction.getObjectId().toString() +
                            " step " + transaction.getStepNumber().toString());
      }
      String predicateName =
        NodeGenerics.trimName( transaction.getInfo()[1], headerJGoView.getPredicateNode(),
                               vizView);
      TransactionField predicateField =
        new TransactionField( predicateName, new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( predicateField);
      predicateField.setSize( (int) headerJGoView.getPredicateNode().getSize().getWidth(),
                           (int) predicateField.getSize().getHeight());
      x += headerJGoView.getPredicateNode().getSize().getWidth();

        // new TransactionField( getParameterName( transaction.getObjectId(), objectName),
      if (transaction.getInfo()[2] == null) {
        System.err.println( "2 key " + transaction.getId().toString() +
                            " type " + transaction.getType() +
                            " objectKey " + transaction.getObjectId().toString() +
                            " step " + transaction.getStepNumber().toString());
      }
      String parameterName =
         NodeGenerics.trimName( transaction.getInfo()[2], headerJGoView.getParameterNode(),
                                vizView);
      TransactionField parameterField =
        new TransactionField( parameterName, new Point( x, y),
                              JGoText.ALIGN_CENTER, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( parameterField);
      parameterField.setSize( (int) headerJGoView.getParameterNode().getSize().getWidth(),
                           (int) parameterField.getSize().getHeight());
      x += headerJGoView.getParameterNode().getSize().getWidth();

      y += keyField.getSize().getHeight();
      i++;
    }
  } // end renderTransactions

//   private String getObjectName( Integer objectId) {
//     String objectName = "";
//     boolean isNameFound = false;
//     // System.err.println( "\ngetObjectName: objectId " + objectId.toString());
//     if (viewableObject instanceof PwPartialPlan) {
//       PwConstraint constraint = ((PwPartialPlan) viewableObject).getConstraint( objectId);
//       if (constraint != null) {
//         objectName = constraint.getName();
//         isNameFound = true;
//         // System.err.println( "  isConstraint");
//       }
//       if (! isNameFound) {
//         PwToken token = ((PwPartialPlan) viewableObject).getToken( objectId);
//         if (token != null) {
//           objectName = token.getPredicate().getName();
//           isNameFound = true;
//           // System.err.println( "  isToken");
//         }
//         if (! isNameFound) {
//           PwVariable variable = ((PwPartialPlan) viewableObject).getVariable( objectId);
//           if (variable != null) {
//             objectName = variable.getType();
//             // System.err.println( "  isVariable");
//             isNameFound = true;
//           }
//         }
//       }
//     } else if (viewableObject instanceof PwPlanningSequence) {
//       // accessing a step of a planSequence will cause Java data structures to be built
//     }
//     if (isNameFound) {
//       // check name is less than column width
//       objectName = trimName( objectName, headerJGoView.getObjectNameNode(), vizView);
//     }
//     return objectName;
//   } // end getObjectName 

//   private String getPredicateName( Integer objectId) {
//     String predicateName = "";
//     boolean isNameFound = false;
//     // System.err.println( "\ngetPredicateName: objectId " + objectId.toString());
//     if (viewableObject instanceof PwPartialPlan) {
//       PwConstraint constraint = ((PwPartialPlan) viewableObject).getConstraint( objectId);
//       if (constraint != null) {
//         // System.err.println( "  isConstraint");
//         UniqueSet predicateNameList = new UniqueSet();
//         List variableList = constraint.getVariablesList();
//         // System.err.println( "  variableList.size " + variableList.size());
//         Iterator variableListItr = variableList.iterator();
//         while (variableListItr.hasNext()) {
//            List tokenList = ((PwVariable) variableListItr.next()).getTokenList();
//            addPredicateName( tokenList, predicateNameList);
//         }
//         // System.err.println( "  predicateNameList " + predicateNameList);
//         // for a constraint, may have multiple predicate names ???
//         isNameFound = true;
//       }
//       if (! isNameFound) {
//         PwToken token = ((PwPartialPlan) viewableObject).getToken( objectId);
//         if (token != null) {
//           // System.err.println( "  isToken");
//           predicateName = token.getPredicate().getName();
//           isNameFound = true;
//         }
//         if (! isNameFound) {
//           PwVariable variable = ((PwPartialPlan) viewableObject).getVariable( objectId);
//           if (variable != null) {
//             // System.err.println( "  isVariable");
//             UniqueSet predicateNameList = new UniqueSet();
//             List tokenList = variable.getTokenList();
//             addPredicateName( tokenList, predicateNameList);
//             // System.err.println( "  predicateNameList " + predicateNameList);
//             // Europa guarantees only one predicate name
//             predicateName = (String) predicateNameList.get( 0);
//             isNameFound = true;
//           }
//         }
//       }
//     } else if (viewableObject instanceof PwPlanningSequence) {
//       // accessing a step of a planSequence will cause Java data structures to be built
//     }
//     if (isNameFound) {
//       // check name is less than column width
//       predicateName = trimName( predicateName, headerJGoView.getPredicateNode(), vizView);
//     }
//     return predicateName;
//   } // end getPredicateName 

//   private void addPredicateName( List tokenList, UniqueSet predicateNameList) {
//     // System.err.println( "  tokenList.size " + tokenList.size());
//     Iterator tokenListItr = tokenList.iterator();
//     while (tokenListItr.hasNext()) {
//       predicateNameList.add( ((PwToken) tokenListItr.next()).getPredicate().getName());
//     }
//   } // end addPredicateName

//   private String getParameterName( Integer objectId, String objectName) {
//     String parameterName = "";
//     boolean isNameFound = false;
//     // System.err.println( "\ngetParameterName: objectId " + objectId.toString());
//     if (viewableObject instanceof PwPartialPlan) {
//       PwConstraint constraint = ((PwPartialPlan) viewableObject).getConstraint( objectId);
//       if (constraint != null) {
//         // System.err.println( "  isConstraint");
//       }
//       if (! isNameFound) {
//         PwToken token = ((PwPartialPlan) viewableObject).getToken( objectId);
//         if (token != null) {
//           // System.err.println( "  isToken");
//         }
//         if (! isNameFound) {
//           PwVariable variable = ((PwPartialPlan) viewableObject).getVariable( objectId);
//           if ((variable != null) && objectName.equals( "PARAMETER_VAR")) {
//             // System.err.println( "  isVariable");
//             UniqueSet parameterNameList = new UniqueSet();
//             List parameterList = variable.getParameterList();
//             Iterator paramListItr = parameterList.iterator();
//             while (paramListItr.hasNext()) {
//               parameterNameList.add( ((PwParameter) paramListItr.next()).getName());
//             }
//             // System.err.println( "  parameterNameList " + parameterNameList);
//             // Europa guarantees only one parameter name
//             parameterName = (String) parameterNameList.get( 0);
//             isNameFound = true;
//           }
//         }
//       }
//     } else if (viewableObject instanceof PwPlanningSequence) {
//       // accessing a step of a planSequence will cause Java data structures to be built
//     }
//     if (isNameFound) {
//       // check name is less than column width
//       parameterName = trimName( parameterName, headerJGoView.getParameterNode(), vizView);
//     }
//     return parameterName;
//   } // end getParameterName 

  /**
   * <code>getObjectKeyField</code>
   *
   * @param lineIndex - <code>int</code> - 
   * @return - <code>TransactionField</code> - 
   */
  public TransactionField getObjectKeyField( int lineIndex) {
    return (TransactionField) objectKeyFieldList.get( lineIndex);
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




} // end class TransactionContentView



