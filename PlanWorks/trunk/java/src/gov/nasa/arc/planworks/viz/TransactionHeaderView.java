// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionHeaderView.java,v 1.4 2003-10-23 19:22:33 taylor Exp $
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
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.util.ColorMap;


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

  protected static final String KEY_HEADER =        "TX_KEY "; 
  protected static final String TYPE_HEADER =       "      TRANSACTION_TYPE     "; 
  protected static final String SOURCE_HEADER =     " SOURCE  ";   
  protected static final String OBJECT_KEY_HEADER = "OBJ_KEY";
  protected static final String STEP_NUM_HEADER =   "  STEP  ";
  protected static final String OBJ_NAME_HEADER =   "     OBJ_NAME     ";
  protected static final String PREDICATE_HEADER =  "  PREDICATE  ";


  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private TextNode keyNode;
  private TextNode typeNode;
  private TextNode sourceNode;
  private TextNode objectKeyNode;
  private TextNode stepNumNode;
  private TextNode objectNameNode;
  private TextNode predicateNode;

  /**
   * <code>TransactionHeaderView</code> - constructor 
   *
   * @param vizView - <code>VizView</code> - 
   * @param query - <code>String</code> - 
   */
  public TransactionHeaderView( VizView vizView, String query) {
    super();
    this.vizView= vizView;

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
    keyNode = new TextNode( KEY_HEADER);
    configureTextNode( keyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( keyNode);
    x += keyNode.getSize().getWidth();

    typeNode = new TextNode( TYPE_HEADER);
    configureTextNode( typeNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( typeNode);
    x += typeNode.getSize().getWidth();

    sourceNode = new TextNode( SOURCE_HEADER);
    configureTextNode( sourceNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( sourceNode);
    x += sourceNode.getSize().getWidth();

    objectKeyNode = new TextNode( OBJECT_KEY_HEADER);
    configureTextNode( objectKeyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectKeyNode);
    x += objectKeyNode.getSize().getWidth();

    stepNumNode = new TextNode( STEP_NUM_HEADER);
    configureTextNode( stepNumNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( stepNumNode);
    x += stepNumNode.getSize().getWidth();

    objectNameNode = new TextNode( OBJ_NAME_HEADER);
    configureTextNode( objectNameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectNameNode);
    x += objectNameNode.getSize().getWidth();

    predicateNode = new TextNode( PREDICATE_HEADER);
    configureTextNode( predicateNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( predicateNode);
    x += predicateNode.getSize().getWidth();
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
  public TextNode getKeyNode() {
    return this.keyNode;
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
   * Gets the value of objectKeyNode
   *
   * @return the value of objectKeyNode
   */
  public TextNode getObjectKeyNode()  {
    return this.objectKeyNode;
  }

  /**
   * Gets the value of stepNumNode
   *
   * @return the value of stepNumNode
   */
  public TextNode getStepNumNode()  {
    return this.stepNumNode;
  }

  /**
   * Gets the value of objectNameNode
   *
   * @return the value of objectNameNode
   */
  public TextNode getObjectNameNode()  {
    return this.objectNameNode;
  }

  /**
   * Gets the value of predicateNode
   *
   * @return the value of predicateNode
   */
  public TextNode getPredicateNode()  {
    return this.predicateNode;
  }



} // end class TransactionHeaderView
