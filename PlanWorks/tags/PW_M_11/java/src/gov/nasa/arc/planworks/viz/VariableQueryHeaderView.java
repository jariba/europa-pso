// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableQueryHeaderView.java,v 1.1 2003-12-20 01:54:50 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 1pdec03
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
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.VariableQueryHeaderNode;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.VariableQueryView;

/**
 * <code>VariableQueryHeaderView</code> - render field names of variable object as column headers
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableQueryHeaderView extends JGoView {

  private List variableList; // element PwVariableQuery
  private String query;
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private VariableQueryHeaderNode stepNumNode;
  private VariableQueryHeaderNode keyNode;
  private VariableQueryHeaderNode nameNode;
  
  /**
   * <code>VariableQueryHeaderView</code> - constructor 
   *
   * @param variableList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public VariableQueryHeaderView( List variableList, String query, VizView vizView) {
    super();
    this.variableList = variableList;
    this.query = query;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    jGoDocument = this.getDocument();

    renderVariableQueryHeader( query);

  } // end constructor
  

  private void renderVariableQueryHeader( String query) {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 3;
    if (query != null) {
      TextNode queryNode = new TextNode( " Query: " + query + " ");
      configureTextNode( queryNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( queryNode);
      y += (int) queryNode.getSize().getHeight() + 2;
    }
    stepNumNode = new VariableQueryHeaderNode( ViewConstants.QUERY_VARIABLE_STEP_NUM_HEADER,
                                             vizView);
    configureTextNode( stepNumNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( stepNumNode);
    x += stepNumNode.getSize().getWidth();

    keyNode = new VariableQueryHeaderNode( ViewConstants.QUERY_VARIABLE_KEY_HEADER, vizView);
    configureTextNode( keyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( keyNode);
    x += keyNode.getSize().getWidth();

    nameNode =
      new VariableQueryHeaderNode( ViewConstants.QUERY_VARIABLE_NAME_HEADER, vizView);
    configureTextNode( nameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( nameNode);
    x += nameNode.getSize().getWidth();

  } // end renderVariableQueryHeader


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
  } // end configureTextNode

  /**
   * Gets the value of stepNumNode
   *
   * @return the value of stepNumNode
   */
  protected TextNode getStepNumNode()  {
    return this.stepNumNode;
  }

  /**
   * Gets the value of keyNode
   *
   * @return the value of keyNode
   */
  protected TextNode getKeyNode()  {
    return this.keyNode;
  }

  /**
   * Gets the value of nameNode
   *
   * @return the value of nameNode
   */
  protected TextNode getNameNode()  {
    return this.nameNode;
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

    JMenuItem variableByKeyItem = new JMenuItem( "Find Variable by " +
                                                 ViewConstants.QUERY_VARIABLE_KEY_HEADER);
    createVariableByKeyItem( variableByKeyItem);
    mouseRightPopup.add( variableByKeyItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createVariableByKeyItem( JMenuItem variableByKeyItem) {
    variableByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskQueryObjectKey variableByKeyDialog =
            new AskQueryObjectKey( VariableQueryHeaderView.this.variableList,
                                   "Find Variable by " +
                                   ViewConstants.QUERY_VARIABLE_KEY_HEADER,
                                   "key (int)", VariableQueryHeaderView.this);
          Integer objectKey = variableByKeyDialog.getObjectKey();
          if (objectKey != null) {
            System.err.println( "createVariableByKeyItem: objectKey " + objectKey.toString());
            int entryIndx = variableByKeyDialog.getObjectListIndex();
            VariableQueryContentView variableContentView = null;
            if (vizView instanceof VariableQueryView) {
              variableContentView = ((VariableQueryView) vizView).getVariableQueryContentView();
            } else {
              System.err.println( "VariableQueryHeaderView.createVariableByKeyItem: " +
                                  vizView + " not handled");
              System.exit( -1);
            }
            if (variableContentView != null) {
              variableContentView.scrollEntries( entryIndx);
              variableContentView.getSelection().clearSelection();
              variableContentView.getSelection().extendSelection
                ( variableContentView.getVariableKeyField( entryIndx));
            }
          }
        }
      });
  } // end createVariableByKeyItem

} // end class VariableQueryHeaderView
