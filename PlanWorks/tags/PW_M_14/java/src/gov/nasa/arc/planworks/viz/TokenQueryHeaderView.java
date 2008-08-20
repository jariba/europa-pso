// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenQueryHeaderView.java,v 1.1 2003-12-20 01:54:49 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
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
import gov.nasa.arc.planworks.viz.nodes.TokenQueryHeaderNode;
import gov.nasa.arc.planworks.viz.sequence.sequenceQuery.TokenQueryView;

/**
 * <code>TokenQueryHeaderView</code> - render field names of token object as column headers
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenQueryHeaderView extends JGoView {

  private List tokenList; // element PwTokenQuery
  private String query;
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private TokenQueryHeaderNode stepNumNode;
  private TokenQueryHeaderNode keyNode;
  private TokenQueryHeaderNode predicateNameNode;
  private TokenQueryHeaderNode objectKeyNode;
  private TokenQueryHeaderNode objectNameNode;
  private TokenQueryHeaderNode predicateNode;
  private TokenQueryHeaderNode parameterNode;
  
  /**
   * <code>TokenQueryHeaderView</code> - constructor 
   *
   * @param tokenList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public TokenQueryHeaderView( List tokenList, String query, VizView vizView) {
    super();
    this.tokenList = tokenList;
    this.query = query;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    jGoDocument = this.getDocument();

    renderTokenQueryHeader( query);

  } // end constructor


  private void renderTokenQueryHeader( String query) {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 3;
    if (query != null) {
      TextNode queryNode = new TextNode( " Query: " + query + " ");
      configureTextNode( queryNode, new Point( x, y), bgColor);
      jGoDocument.addObjectAtTail( queryNode);
      y += (int) queryNode.getSize().getHeight() + 2;
    }
    stepNumNode = new TokenQueryHeaderNode( ViewConstants.QUERY_TOKEN_STEP_NUM_HEADER,
                                             vizView);
    configureTextNode( stepNumNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( stepNumNode);
    x += stepNumNode.getSize().getWidth();

    keyNode = new TokenQueryHeaderNode( ViewConstants.QUERY_TOKEN_KEY_HEADER, vizView);
    configureTextNode( keyNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( keyNode);
    x += keyNode.getSize().getWidth();

    predicateNameNode =
      new TokenQueryHeaderNode( ViewConstants.QUERY_TOKEN_PREDICATE_HEADER, vizView);
    configureTextNode( predicateNameNode, new Point( x, y), bgColor);
    jGoDocument.addObjectAtTail( predicateNameNode);
    x += predicateNameNode.getSize().getWidth();

  } // end renderTokenQueryHeader


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
   * Gets the value of predicateNameNode
   *
   * @return the value of predicateNameNode
   */
  protected TextNode getPredicateNameNode()  {
    return this.predicateNameNode;
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

    JMenuItem tokenByKeyItem = new JMenuItem( "Find Token by " +
                                              ViewConstants.QUERY_TOKEN_KEY_HEADER);
    createTokenByKeyItem( tokenByKeyItem);
    mouseRightPopup.add( tokenByKeyItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu

  private void createTokenByKeyItem( JMenuItem tokenByKeyItem) {
    tokenByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskQueryObjectKey tokenByKeyDialog =
            new AskQueryObjectKey( TokenQueryHeaderView.this.tokenList,
                                   "Find Token by " +
                                   ViewConstants.QUERY_TOKEN_KEY_HEADER,
                                   "key (int)", TokenQueryHeaderView.this);
          Integer objectKey = tokenByKeyDialog.getObjectKey();
          if (objectKey != null) {
            System.err.println( "createTokenByKeyItem: objectKey " + objectKey.toString());
            int entryIndx = tokenByKeyDialog.getObjectListIndex();
            TokenQueryContentView tokenContentView = null;
            if (vizView instanceof TokenQueryView) {
              tokenContentView = ((TokenQueryView) vizView).getTokenQueryContentView();
            } else {
              System.err.println( "TokenQueryHeaderView.createTokenByKeyItem: " +
                                  vizView + " not handled");
              System.exit( -1);
            }
            if (tokenContentView != null) {
              tokenContentView.scrollEntries( entryIndx);
              tokenContentView.getSelection().clearSelection();
              tokenContentView.getSelection().extendSelection
                ( tokenContentView.getTokenKeyField( entryIndx));
            }
          }
        }
      });
  } // end createTokenByKeyItem

} // end class TokenQueryHeaderView
