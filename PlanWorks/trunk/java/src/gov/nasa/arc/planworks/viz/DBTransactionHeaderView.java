// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionHeaderView.java,v 1.3 2004-05-08 01:44:12 taylor Exp $
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
import gov.nasa.arc.planworks.viz.nodes.DBTransactionHeaderNode;
import gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.DBTransactionQueryView;


/**
 * <code>DBTransactionHeaderView</code> - render field names of plan db
 *                                        transaction as column headers
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DBTransactionHeaderView extends JGoView {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private List transactionList; // element PwDBTransaction
  private String query;
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private DBTransactionHeaderNode keyNode;
  private DBTransactionHeaderNode nameNode;
  private DBTransactionHeaderNode sourceNode;
  private DBTransactionHeaderNode objectKeyNode;
  private DBTransactionHeaderNode stepNumNode;
  private DBTransactionHeaderNode objectNameNode;
  private DBTransactionHeaderNode predicateNode;
  private DBTransactionHeaderNode parameterNode;

  /**
   * <code>DBTransactionHeaderView</code> - constructor 
   *
   * @param transactionList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public DBTransactionHeaderView( List transactionList, String query, VizView vizView) {
    super();
    this.transactionList = transactionList;
    this.query = query;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    jGoDocument = this.getDocument();

    renderTransactionHeader( query, ViewGenerics.computeTransactionNameHeader());

  } // end constructor


  private void renderTransactionHeader( String query, String transactionNameHeader) {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 3;
    boolean isDBTransactionQueryView = (vizView instanceof DBTransactionQueryView);
    boolean isObjectKeyNode =
      ((isDBTransactionQueryView && // "In Range" only
        (((DBTransactionQueryView) vizView).getQuery().indexOf( "For ") == -1)) ||
       (vizView instanceof DBTransactionView));
    if (query != null) {
      TextNode queryNode = new TextNode( " Query: " + query + " ");
      configureTextNode( queryNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( queryNode);
      y += (int) queryNode.getSize().getHeight() + 2;
    }
    keyNode = new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_KEY_HEADER, vizView);
    configureTextNode( keyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( keyNode);
    x += keyNode.getSize().getWidth();

    nameNode = new DBTransactionHeaderNode( transactionNameHeader, vizView);
    configureTextNode( nameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( nameNode);
    x += nameNode.getSize().getWidth();

    sourceNode =
      new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_SOURCE_HEADER, vizView);
    configureTextNode( sourceNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( sourceNode);
    x += sourceNode.getSize().getWidth();

    if (isObjectKeyNode) {
      objectKeyNode =
        new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER, vizView);
      configureTextNode( objectKeyNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( objectKeyNode);
      x += objectKeyNode.getSize().getWidth();
    }

    if (isDBTransactionQueryView) {
      stepNumNode =
        new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER, vizView);
      configureTextNode( stepNumNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( stepNumNode);
      x += stepNumNode.getSize().getWidth();
    }

    objectNameNode = new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER,
                                                vizView);
    configureTextNode( objectNameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectNameNode);
    x += objectNameNode.getSize().getWidth();

    predicateNode = new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER,
                                               vizView);
    configureTextNode( predicateNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( predicateNode);
    x += predicateNode.getSize().getWidth();

    parameterNode = new DBTransactionHeaderNode( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER,
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
  protected DBTransactionHeaderNode getKeyNode() {
    return this.keyNode;
  }

  /**
   * Gets the value of nameNode
   *
   * @return the value of nameNode
   */
  protected DBTransactionHeaderNode getNameNode()  {
    return this.nameNode;
  }

  /**
   * Gets the value of sourceNode
   *
   * @return the value of sourceNode
   */
  protected DBTransactionHeaderNode getSourceNode()  {
    return this.sourceNode;
  }

  /**
   * Gets the value of objectKeyNode
   *
   * @return the value of objectKeyNode
   */
  protected DBTransactionHeaderNode getObjectKeyNode()  {
    return this.objectKeyNode;
  }

  /**
   * Gets the value of stepNumNode
   *
   * @return the value of stepNumNode
   */
  protected DBTransactionHeaderNode getStepNumNode()  {
    return this.stepNumNode;
  }

  /**
   * Gets the value of objectNameNode
   *
   * @return the value of objectNameNode
   */
  protected DBTransactionHeaderNode getObjectNameNode()  {
    return this.objectNameNode;
  }

  /**
   * Gets the value of predicateNode
   *
   * @return the value of predicateNode
   */
  protected DBTransactionHeaderNode getPredicateNode()  {
    return this.predicateNode;
  }

  /**
   * Gets the value of parameterNode
   *
   * @return the value of parameterNode
   */
  protected DBTransactionHeaderNode getParameterNode()  {
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

    boolean isFindTransByKey = 
      (((vizView instanceof DBTransactionQueryView) && (query.indexOf( "For ") == -1)) ||
       (vizView instanceof DBTransactionView));
    if (isFindTransByKey) {
      JMenuItem transByKeyItem = new JMenuItem( "Find Transaction by Obj_Key");
      createTransByKeyItem( transByKeyItem);
      mouseRightPopup.add( transByKeyItem);
    }

    if (vizView instanceof DBTransactionView) {
      PwPartialPlan partialPlan = ((DBTransactionView) vizView).getPartialPlan();
      String partialPlanName = partialPlan.getPartialPlanName();
      PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);

      ((DBTransactionView) vizView).createOpenViewItems( partialPlan, partialPlanName,
                                                       planSequence, mouseRightPopup,
                                                       ViewConstants.DB_TRANSACTION_VIEW);
    
      ((DBTransactionView) vizView).createAllViewItems( partialPlan, partialPlanName,
                                                      planSequence, mouseRightPopup);
    }

    if (isFindTransByKey) {
      NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
    }
  } // end mouseRightPopupMenu

  private void createTransByKeyItem( JMenuItem transByKeyItem) {
    transByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskQueryObjectKey transByKeyDialog =
            new AskQueryObjectKey( DBTransactionHeaderView.this.transactionList,
                                    "Find Transaction by Obj_Key", "key (int)",
                                    DBTransactionHeaderView.this);
          Integer objectKey = transByKeyDialog.getObjectKey();
          if (objectKey != null) {
            System.err.println( "createTransByKeyItem: objectKey " + objectKey.toString());
            int entryIndx = transByKeyDialog.getObjectListIndex();
            DBTransactionContentView transactionContentView = null;
            if (vizView instanceof DBTransactionView) {
              transactionContentView =
                ((DBTransactionView) vizView).getDBTransactionContentView();
            } else if (vizView instanceof DBTransactionQueryView) {
              transactionContentView =
                ((DBTransactionQueryView) vizView).getDBTransactionContentView();
            } else {
              System.err.println( "DBTransactionHeaderView.createTransByKeyItem: " +
                                  vizView + " not handled");
              System.exit( -1);
            }
            if (transactionContentView != null) {
              transactionContentView.scrollEntries( entryIndx);
              transactionContentView.getSelection().clearSelection();
              transactionContentView.getSelection().extendSelection
                ( transactionContentView.getObjectKeyField( entryIndx));
            }
          }
        }
      });
  } // end createTransByKeyItem



} // end class DBTransactionHeaderView
