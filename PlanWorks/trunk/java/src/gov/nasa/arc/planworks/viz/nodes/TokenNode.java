// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenNode.java,v 1.6 2003-07-15 00:33:52 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20june03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.views.VizView;


/**
 * <code>TokenNode</code> - JGo widget to render a token with a
 *                          label consisting of the slot's predicate name.
 *             Object->JGoObject->JGoArea->TextNode->TokenNode
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenNode extends BasicNode {

  private static final boolean IS_FONT_BOLD = false;
  private static final boolean IS_FONT_UNDERLINED = false;
  private static final boolean IS_FONT_ITALIC = false;
  private static final int TEXT_ALIGNMENT = JGoText.ALIGN_LEFT;
  private static final boolean IS_TEXT_MULTILINE = false;
  private static final boolean IS_TEXT_EDITABLE = false;

  private PwToken token;
  private int objectCnt;
  private boolean isFreeToken;
  private VizView view;
  private String predicateName;
  private String nodeLabel;

  /**
   * <code>TokenNode</code> - constructor 
   *
   * @param token - <code>PwToken</code> -
   * @param tokenLocation - <code>Point</code> - 
   * @param objectCnt - <code>int</code> - 
   * @param isFreeToken - <code>boolean</code> - 
   * @param view - <code>VizView</code> - 
   */
  public TokenNode( PwToken token, Point tokenLocation, int objectCnt, boolean isFreeToken,
                    VizView view) {
    super();
    this.token = token;
    this.objectCnt = objectCnt;
    this. isFreeToken = isFreeToken;
    this.view = view;
    if (token != null) {
      predicateName = token.getPredicate().getName();
      // nodeLabel = predicateName + " " + token.getKey().toString();
      nodeLabel = predicateName;
    } else {
      predicateName = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL;
      nodeLabel = predicateName;
    }
    // System.err.println( "TokenNode: " + nodeLabel);

    configure( tokenLocation);
  } // end constructor

  private final void configure( Point tokenLocation) {
    boolean isRectangular = true;
    setLabelSpot( JGoObject.Center);
    initialize( tokenLocation, nodeLabel, isRectangular);
    String backGroundColor = null;
    if (isFreeToken) {
      backGroundColor = ViewConstants.FREE_TOKEN_BG_COLOR;
    } else {
      backGroundColor = ((objectCnt % 2) == 0) ?
        ViewConstants.EVEN_OBJECT_SLOT_BG_COLOR :
        ViewConstants.ODD_OBJECT_SLOT_BG_COLOR;
    }
    setBrush( JGoBrush.makeStockBrush( ColorMap.getColor( backGroundColor)));  
    getLabel().setEditable( false);
    setDraggable( false);
    // do not allow user links
    getPort().setVisible( false);
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

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return token.toString();
  } // end getToolTipText


} // end class TokenNode
