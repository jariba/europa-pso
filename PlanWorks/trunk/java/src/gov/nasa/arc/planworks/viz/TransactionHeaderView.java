// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionHeaderView.java,v 1.1 2003-10-16 21:40:40 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 13oct03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;


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

  private static final String ID_HEADER =        "   ID    "; 
  private static final String TYPE_HEADER =      "           TYPE            "; 
  private static final String SOURCE_HEADER =    " SOURCE  ";   
  private static final String OBJECT_ID_HEADER = "OBJECT_ID";
  private static final String STEP_NUM_HEADER =  " STEP_NUM";


  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private TextNode idNode;
  private TextNode typeNode;
  private TextNode sourceNode;
  private TextNode objectIdNode;
  private TextNode stepNumNode;

  /**
   * <code>TransactionHeaderView</code> - constructor 
   *
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TransactionHeaderView( VizView vizView) {
    super();
    this.vizView= vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    jGoDocument = this.getDocument();

    renderTransactionHeader();

  } // end constructor


  private void renderTransactionHeader() {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    idNode = new TextNode( ID_HEADER);
    configureTextNode( idNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( idNode);
    x += idNode.getSize().getWidth();

    typeNode = new TextNode( TYPE_HEADER);
    configureTextNode( typeNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( typeNode);
    x += typeNode.getSize().getWidth();

    sourceNode = new TextNode( SOURCE_HEADER);
    configureTextNode( sourceNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( sourceNode);
    x += sourceNode.getSize().getWidth();

    objectIdNode = new TextNode( OBJECT_ID_HEADER);
    configureTextNode( objectIdNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectIdNode);
    x += objectIdNode.getSize().getWidth();

    stepNumNode = new TextNode( STEP_NUM_HEADER);
    configureTextNode( stepNumNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( stepNumNode);
    x += stepNumNode.getSize().getWidth();    
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
   * Gets the value of idNode
   *
   * @return the value of idNode
   */
  public TextNode getIdNode() {
    return this.idNode;
  }

  /**
   * Gets the value of typeNode
   *
   * @return the value of typeNode
   */
  public TextNode getTypeNode()  {
    return this.typeNode;
  }

  /**
   * Gets the value of sourceNode
   *
   * @return the value of sourceNode
   */
  public TextNode getSourceNode()  {
    return this.sourceNode;
  }

  /**
   * Gets the value of objectIdNode
   *
   * @return the value of objectIdNode
   */
  public TextNode getObjectIdNode()  {
    return this.objectIdNode;
  }

  /**
   * Gets the value of stepNumNode
   *
   * @return the value of stepNumNode
   */
  public TextNode getStepNumNode()  {
    return this.stepNumNode;
  }



} // end class TransactionHeaderView
