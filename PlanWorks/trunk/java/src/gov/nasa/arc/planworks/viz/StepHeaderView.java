// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepHeaderView.java,v 1.2 2003-10-23 18:28:10 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17oct03
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
 * <code>StepHeaderView</code> - render field names of step object as column headers
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class StepHeaderView extends JGoView {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private static final String STEP_NUM_HEADER =   "  STEP  ";
  private static final String KEY_HEADER =        "TX_KEY "; 
  private static final String TYPE_HEADER =       "      TRANSACTION_TYPE     "; 
  private static final String OBJ_NAME_HEADER =   "     OBJ_NAME     ";
  private static final String PREDICATE_HEADER =  "  PREDICATE  ";


  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private TextNode stepNumNode;
  private TextNode keyNode;
  private TextNode typeNode;
  private TextNode objectNameNode;
  private TextNode predicateNode;

  /**
   * <code>StepHeaderView</code> - constructor 
   *
   * @param vizView - <code>VizView</code> - 
   * @param query - <code>String</code> - 
   */
  public StepHeaderView( VizView vizView, String query) {
    super();
    this.vizView= vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    jGoDocument = this.getDocument();

    renderStepHeader( query);

  } // end constructor


  private void renderStepHeader( String query) {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 3;
    if (query != null) {
      TextNode queryNode = new TextNode( " Query: " + query + " ");
      configureTextNode( queryNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( queryNode);
      y += (int) queryNode.getSize().getHeight() + 2;
    }
    stepNumNode = new TextNode( STEP_NUM_HEADER);
    configureTextNode( stepNumNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( stepNumNode);
    x += stepNumNode.getSize().getWidth();

    keyNode = new TextNode( KEY_HEADER);
    configureTextNode( keyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( keyNode);
    x += keyNode.getSize().getWidth();

    typeNode = new TextNode( TYPE_HEADER);
    configureTextNode( typeNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( typeNode);
    x += typeNode.getSize().getWidth();

    objectNameNode = new TextNode( OBJ_NAME_HEADER);
    configureTextNode( objectNameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( objectNameNode);
    x += objectNameNode.getSize().getWidth();

    predicateNode = new TextNode( PREDICATE_HEADER);
    configureTextNode( predicateNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( predicateNode);
  } // end renderStepHeader


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
   * Gets the value of stepNumNode
   *
   * @return the value of stepNumNode
   */
  public TextNode getStepNumNode()  {
    return this.stepNumNode;
  }

  /**
   * Gets the value of keyNode
   *
   * @return the value of keyNode
   */
  public TextNode getKeyNode()  {
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



} // end class StepHeaderView
