// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNode.java,v 1.44 2004-06-29 00:47:16 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariableContainer;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.VariableNode;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>TokenNode</code> - JGo widget to render a token with a
 *                          label consisting of the slot's predicate name.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNode extends BasicNode implements OverviewToolTip {

  protected PwToken token;
  protected PwSlot slot;
  protected Point tokenLocation;
  protected boolean isFreeToken;
  protected PartialPlanView partialPlanView;
  protected String predicateName;
  protected String nodeLabel;
  protected Color backgroundColor;

  /**
   * <code>TokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> - 
   * @param slot - <code>PwSlot</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param isDraggable - <code>boolean</code> - 
   * @param partialPlanView - <code>PartialPlanView</code> - 
   */
  public TokenNode( PwToken token, PwSlot slot, Point tokenLocation, Color backgroundColor,
                    boolean isFreeToken, boolean isDraggable, PartialPlanView partialPlanView) {
    super();
    this.backgroundColor = backgroundColor;
    this.token = token;
    this.slot = slot;
    this.tokenLocation = tokenLocation;
    this.isFreeToken = isFreeToken;
    this.partialPlanView = partialPlanView;
    if (token != null) {
      predicateName = token.getPredicateName();
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
    }
    StringBuffer labelBuf = new StringBuffer( predicateName);
    labelBuf.append( "\nkey=").append( token.getId().toString());
    nodeLabel = labelBuf.toString();
    
    // System.err.println( "TokenNode: " + nodeLabel);
    configure( tokenLocation, backgroundColor, isDraggable);
  } // end constructor

  /**
   * <code>TokenNode</code> - constructor - for NodeShapes frame
   *
   * @param predicateName - <code>String</code> - 
   * @param id - <code>Integer</code> - 
   * @param tokenLocation - <code>Point</code> - 
   * @param backgroundColor - <code>Color</code> - 
   */
  public TokenNode( String predicateName, Integer id, Point tokenLocation,
                    Color backgroundColor) {
    super();
    this.backgroundColor = backgroundColor;
    this.token = null;
    this.slot = null;
    this.tokenLocation = tokenLocation;
    this.partialPlanView = null;
    this.predicateName = predicateName;
    boolean isDraggable = false;
    StringBuffer labelBuf = new StringBuffer( predicateName);
    labelBuf.append( "\nkey=").append( id.toString());
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

  public Color getColor(){return backgroundColor;}

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (token == null) {
      return null;
    }
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( token.toString());
    if (partialPlanView.getZoomFactor() > 1) {
      tip.append( "<br>key=");
      tip.append( token.getId().toString());
    }
    // check for free token
    if (slot != null) {
      tip.append( "<br>");
      tip.append( "slot key=");
      tip.append( slot.getId().toString());
    }
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *                               implements OverviewToolTip 
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    if (token != null) {
      tip.append( predicateName);
    } else {
      tip.append( ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL);
    }
    tip.append( "<br>key=");
    tip.append( token.getId().toString());
    tip.append( "</html>");
    return tip.toString();
  } // end getToolTipText


  /**
   * <code>doMouseClick</code> - Mouse-Right: Set Active Token
   *
   * @param modifiers - <code>int</code> - 
   * @param dc - <code>Point</code> - 
   * @param vc - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    if (token == null) {
      return false;
    }
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

  public void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String viewSetKey = partialPlanView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame = partialPlanView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = partialPlanView.getPartialPlan();
          contentPane.add( new NavigatorView( TokenNode.this.getToken(), partialPlan,
                                              partialPlanView.getViewSet(), viewSetKey,
                                              navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    JMenuItem activeTokenItem = new JMenuItem( "Set Active Token");
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwToken activeToken = TokenNode.this.getToken();
          ((PartialPlanViewSet) partialPlanView.getViewSet()).setActiveToken( activeToken);
          ((PartialPlanViewSet) partialPlanView.getViewSet()).setSecondaryTokens( null);
          System.err.println( "TokenNode setActiveToken: " +
                              activeToken.getPredicateName() +
                              " (key=" + activeToken.getId().toString() + ")");
        }
      });
    mouseRightPopup.add( activeTokenItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, partialPlanView, viewCoords);
  } // end mouseRightPopupMenu



} // end class TokenNode
