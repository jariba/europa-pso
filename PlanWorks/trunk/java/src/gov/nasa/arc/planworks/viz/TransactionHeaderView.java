// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionHeaderView.java,v 1.9 2003-11-07 00:04:58 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13oct03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TransactionHeaderNode;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewMenu;
import gov.nasa.arc.planworks.viz.partialPlan.transaction.TransactionView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TransactionQueryView;


/**
 * <code>TransactionHeaderView</code> - render field names of transaction as column headers
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionHeaderView extends JGoView {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private List transactionList; // element PwTransaction
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private TransactionHeaderNode keyNode;
  private TransactionHeaderNode typeNode;
  private TransactionHeaderNode sourceNode;
  private TransactionHeaderNode objectKeyNode;
  private TransactionHeaderNode stepNumNode;
  private TransactionHeaderNode objectNameNode;
  private TransactionHeaderNode predicateNode;
  private TransactionHeaderNode parameterNode;

  /**
   * <code>TransactionHeaderView</code> - constructor 
   *
   * @param transactionList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TransactionHeaderView( List transactionList, String query, VizView vizView) {
    super();
    this.transactionList = transactionList;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    jGoDocument = this.getDocument();

    renderTransactionHeader( query);

  } // end constructor


  private void renderTransactionHeader( String query) {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 3;
    if (query != null) {
      TextNode queryNode = new TextNode( " Query: " + query + " ");
      configureTextNode( queryNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( queryNode);
      y += (int) queryNode.getSize().getHeight() + 2;
    }
    keyNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_KEY_HEADER, vizView);
    configureTextNode( keyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( keyNode);
    x += keyNode.getSize().getWidth();

    typeNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_TYPE_HEADER, vizView);
    configureTextNode( typeNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( typeNode);
    x += typeNode.getSize().getWidth();

    sourceNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_SOURCE_HEADER, vizView);
    configureTextNode( sourceNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( sourceNode);
    x += sourceNode.getSize().getWidth();

    objectKeyNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_OBJECT_KEY_HEADER,
                                               vizView);
    configureTextNode( objectKeyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectKeyNode);
    x += objectKeyNode.getSize().getWidth();

    stepNumNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_STEP_NUM_HEADER, vizView);
    configureTextNode( stepNumNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( stepNumNode);
    x += stepNumNode.getSize().getWidth();

    objectNameNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_OBJ_NAME_HEADER,
                                                vizView);
    configureTextNode( objectNameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectNameNode);
    x += objectNameNode.getSize().getWidth();

    predicateNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_PREDICATE_HEADER,
                                               vizView);
    configureTextNode( predicateNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( predicateNode);
    x += predicateNode.getSize().getWidth();

    parameterNode = new TransactionHeaderNode( ViewConstants.TRANSACTION_PARAMETER_HEADER,
                                               vizView);
    configureTextNode( parameterNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( parameterNode);
    x += parameterNode.getSize().getWidth();
  } // end renderTransactionHeader


  private void configureTextNode( TextNode node, Point location, Color bgColor) {
    node.setBrush( JGoBrush.makeStockBrush( bgColor));
    node.getLabel().setEditable( false);
    node.getLabel().setBold( true);
    node.getLabel().setMultiline( false);
    node.getLabel().setAlignment( JGoText.ALIGN_CENTER);
    node.setDraggable( false);
    // do not allow user links
    node.getTopPort().setVisible( false);
    node.getLeftPort().setVisible( false);
    node.getBottomPort().setVisible( false);
    node.getRightPort().setVisible( false);
    node.setLocation( (int) location.getX(), (int) location.getY());
    // node.setInsets( NODE_INSETS);
  } // end configureTextNode

  /**
   * Gets the value of keyNode
   *
   * @return the value of keyNode
   */
  protected TransactionHeaderNode getKeyNode() {
    return this.keyNode;
  }

  /**
   * Gets the value of typeNode
   *
   * @return the value of typeNode
   */
  protected TransactionHeaderNode getTypeNode()  {
    return this.typeNode;
  }

  /**
   * Gets the value of sourceNode
   *
   * @return the value of sourceNode
   */
  protected TransactionHeaderNode getSourceNode()  {
    return this.sourceNode;
  }

  /**
   * Gets the value of objectKeyNode
   *
   * @return the value of objectKeyNode
   */
  protected TransactionHeaderNode getObjectKeyNode()  {
    return this.objectKeyNode;
  }

  /**
   * Gets the value of stepNumNode
   *
   * @return the value of stepNumNode
   */
  protected TransactionHeaderNode getStepNumNode()  {
    return this.stepNumNode;
  }

  /**
   * Gets the value of objectNameNode
   *
   * @return the value of objectNameNode
   */
  protected TransactionHeaderNode getObjectNameNode()  {
    return this.objectNameNode;
  }

  /**
   * Gets the value of predicateNode
   *
   * @return the value of predicateNode
   */
  protected TransactionHeaderNode getPredicateNode()  {
    return this.predicateNode;
  }

  /**
   * Gets the value of parameterNode
   *
   * @return the value of parameterNode
   */
  protected TransactionHeaderNode getParameterNode()  {
    return this.parameterNode;
  }


  /**
   * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
    }
  } // end doBackgroundClick 

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem transByKeyItem = new JMenuItem( "Find Transaction by Obj_Key");
    createTransByKeyItem( transByKeyItem);
    mouseRightPopup.add( transByKeyItem);

    if (vizView instanceof TransactionView) {
      PwPartialPlan partialPlan = ((TransactionView) vizView).getPartialPlan();
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence = PlanWorks.planWorks.getPlanSequence( partialPlan);
      JMenuItem changeViewItem = new JMenuItem( "Open a View");
      ((TransactionView) vizView).createChangeViewItem( changeViewItem, partialPlan,
                                                        viewCoords);
      mouseRightPopup.add( changeViewItem);

      ((TransactionView) vizView).createAllViewItems( partialPlan, partialPlanName,
                                                      planSequence, mouseRightPopup);
    }

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createTransByKeyItem( JMenuItem transByKeyItem) {
    transByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskTransactionObjectKey transByKeyDialog =
            new AskTransactionObjectKey( TransactionHeaderView.this.transactionList,
                                         "Find Transaction by Obj_Key", "key (int)");
          Integer objectKey = transByKeyDialog.getObjectKey();
          if (objectKey != null) {
            System.err.println( "createTransByKeyItem: objectKey " + objectKey.toString());
            int entryIndx = transByKeyDialog.getTransactionListIndex();
            if (vizView instanceof TransactionView) {
              ((TransactionView) vizView).getTransactionContentView().
                scrollEntries( entryIndx);
            } else if (vizView instanceof TransactionQueryView) {
              ((TransactionQueryView) vizView).getTransactionContentView().
                scrollEntries( entryIndx);
            } else {
              System.err.println( "TransactionHeaderView.createTransByKeyItem: " +
                                  vizView + " not handled");
              System.exit( -1);
            }
          }
        }
      });
  } // end createTransByKeyItem



} // end class TransactionHeaderView
