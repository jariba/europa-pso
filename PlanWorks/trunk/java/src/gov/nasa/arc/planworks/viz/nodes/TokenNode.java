// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNode.java,v 1.1 2003-06-25 17:04:05 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.util.Iterator;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.TextNode;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.tokenNetwork.TokenNetworkView;


/**
 * <code>TokenNode</code> - JGo widget to render a token with a
 *                          label consisting of the slot's predicate name.
 *             Object->JGoObject->JGoArea->TextNode->TokenNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNode extends TextNode {

  // top left bottom right
  private static final Insets NODE_INSETS =
    new Insets( ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE_HALF,
                ViewConstants.TIMELINE_VIEW_INSET_SIZE);

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwToken token;
  private int objectCnt;
  private TokenNetworkView view;
  private String predicateName;

  /**
   * <code>TokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> -
   * @param type - <code>String</code> -
   * @param tokenLocation - <code>Point</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param view - <code>TokenNetworkView</code> - 
   */
  public TokenNode( PwToken token, Point tokenLocation, int objectCnt,
                    TokenNetworkView view) {
    super( token.getPredicate().getName() + " " + token.getKey());
    this.token = token;
    this.objectCnt = objectCnt;
    this.view = view;
    this.predicateName = token.getPredicate().getName();
    System.err.println( "TokenNode: predicateName " + predicateName + 
                        " key: " + token.getKey());
    configure( tokenLocation);
  } // end constructor

  private final void configure( Point tokenLocation) {
    String backGroundColor = ((objectCnt % 2) == 0) ?
      ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
      ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( false);
    // allow links
    getTopPort().setVisible( true);
    getTopPort().setStyle( JGoPort.StyleHidden);
    getLeftPort().setVisible( true);
    getLeftPort().setStyle( JGoPort.StyleHidden);
    getBottomPort().setVisible( true);
    getBottomPort().setStyle( JGoPort.StyleHidden);
    getRightPort().setVisible( true);
    getRightPort().setStyle( JGoPort.StyleHidden);
    // but not user links
    getTopPort().setValidSource( false);
    getTopPort().setValidDestination( false);
    getLeftPort().setValidSource( false);
    getLeftPort().setValidDestination( false);
    getBottomPort().setValidSource( false);
    getBottomPort().setValidDestination( false);
    getRightPort().setValidSource( false);
    getRightPort().setValidDestination( false);

    setLocation( (int) tokenLocation.getX(), (int) tokenLocation.getY());
    setInsets( NODE_INSETS);
  } // end configure


  /**
   * <code>getToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToken() {
    return token;
  }

  /**
   * <code>getPredicateName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPredicateName() {
    return predicateName;
  }


} // end class TokenNode
