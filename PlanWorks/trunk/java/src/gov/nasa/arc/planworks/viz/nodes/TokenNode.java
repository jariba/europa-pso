// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNode.java,v 1.25 2003-09-30 21:14:55 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;


/**
 * <code>TokenNode</code> - JGo widget to render a token with a
 *                          label consisting of the slot's predicate name.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNode extends BasicNode {

  protected PwToken token;
  protected Point tokenLocation;
  protected boolean isFreeToken;
  protected PartialPlanView partialPlanView;
  protected String predicateName;
  protected String nodeLabel;


  /**
   * <code>TokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TokenNode( PwToken token, Point tokenLocation, Color backgroundColor,
                    boolean isFreeToken, boolean isDraggable, PartialPlanView partialPlanView) {
    super();
    this.token = token;
    this.tokenLocation = tokenLocation;
    this.isFreeToken = isFreeToken;
    this.partialPlanView = partialPlanView;
    if (token != null) {
      predicateName = token.getPredicate().getName();
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    }
    StringBuffer labelBuf = new StringBuffer( predicateName);
    labelBuf.append( "\nkey=").append( token.getId().toString());
    nodeLabel = labelBuf.toString();
    
    // System.err.println( "TokenNode: " + nodeLabel);
    configure( tokenLocation, backgroundColor, isDraggable);
  } // end constructor

  private final void configure( Point tokenLocation, Color backgroundColor,
                                boolean isDraggable) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel, isRectangular);
    setBrush( JGoBrush.makeStockBrush( backgroundColor));  
    getLabel().setEditable( false);
    setDraggable( isDraggable);
    // do not allow user links
    getPort().setVisible( false);
    getLabel().setMultiline( true);
  } // end configure

  /**
   * <code>equals</code>
   *
   * @param node - <code>TokenNode</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( TokenNode node) {
    return (this.getToken().getId().equals( node.getToken().getId()));
  }

  /**
   * <code>getToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken() {
    return token;
  }

  /**
   * <code>isFreeToken</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean isFreeToken() {
    return isFreeToken;
  }

  /**
   * <code>getPartialPlanView</code>
   *
   * @return - <code>PartialPlanView</code> - 
   */
  public PartialPlanView getPartialPlanView() {
    return partialPlanView;
  }

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPredicateName() {
    return predicateName;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return token.getId().toString();
  }

  /**
   * <code>doMouseClick</code> - For Constraint Network View, Mouse-Left opens/closes
   *            tokenNode to show variableNodes.  Mouse-Right: Set Active Token
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    // System.err.println( "TokenNode: doMouseClick obj class " +
    //                     obj.getTopLevelObject().getClass().getName());
    TokenNode tokenNode = (TokenNode) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // do nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          ((PartialPlanViewSet) partialPlanView.getViewSet()).
            setActiveToken( TokenNode.this.getToken());
          ((PartialPlanViewSet) partialPlanView.getViewSet()).setSecondaryTokens( null);
          System.err.println( "TokenNode setActiveToken: " +
                              TokenNode.this.getToken().getPredicate().getName());
        }
      });
    mouseRightPopup.add( activeTokenItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, partialPlanView, viewCoords);
  } // end mouseRightPopupMenu



} // end class TokenNode
