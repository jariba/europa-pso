// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepHeaderView.java,v 1.1 2003-10-18 01:27:54 taylor Exp $
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

  private static final String KEY_HEADER =        "  KEY  "; 
  private static final String TYPE_HEADER =      "           TYPE            "; 
  private static final String SOURCE_HEADER =    " SOURCE  ";   
  private static final String OBJECT_KEY_HEADER = "OBJ_KEY";
  private static final String STEP_NUM_HEADER =  "STEP_NUM";


  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private TextNode keyNode;
  private TextNode typeNode;
  private TextNode sourceNode;
  private TextNode objectKeyNode;
  private TextNode stepNumNode;

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



} // end class StepHeaderView
