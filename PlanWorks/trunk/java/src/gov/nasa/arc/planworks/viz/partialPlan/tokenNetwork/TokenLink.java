// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenLink.java,v 1.3 2004-03-30 22:01:04 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 01july03
//

package gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork;

import java.awt.Container;
import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoLabeledLink;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewRuleView;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;


/**
 * <code>TokenLink</code> - JGo widget to render a link with a
 *                          label between two tokens
 *             Object->JGoObject->JGoArea->JGoLabeledLink->TokenLink
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenLink extends JGoLabeledLink {

  private TokenNode fromTokenNode;
  private TokenNode toTokenNode;
  private TokenNetworkView tokenNetworkView;

  public TokenLink( TokenNode fromTokenNode, TokenNode toTokenNode, int penWidth,
                    TokenNetworkView tokenNetworkView) {
    super( fromTokenNode.getPort(), toTokenNode.getPort());
    this.fromTokenNode = fromTokenNode;
    this.toTokenNode = toTokenNode;
    this.tokenNetworkView = tokenNetworkView;
    this.setArrowHeads( false, true); // fromArrowHead toArrowHead
    // do no allow user to select and move links
    this.setRelinkable( false);
    this.setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
  } // end constructor

  /**
   * <code>getFromToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getFromToken() {
    return this.fromTokenNode.getToken();
  }

  /**
   * <code>getFromTokenNode</code>
   *
   * @return - <code>TokenNode</code> - 
   */
  public TokenNode getFromTokenNode() {
    return this.fromTokenNode;
  }

  /**
   * <code>getToToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getToToken() {
    return this.toTokenNode.getToken();
  }

  /**
   * <code>getToTokenNode</code>
   *
   * @return - <code>TokenNode</code> - 
   */
  public TokenNode getToTokenNode() {
    return this.toTokenNode;
  }


  /**
   * <code>doUncapturedMouseMove</code> -- handles RuleView window
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doUncapturedMouseMove( int modifiers, Point docCoords, Point viewCoords,
                                        JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    TokenLink tokenLink = (TokenLink) obj.getTopLevelObject();
    TokenLink currentMouseOverLink = tokenNetworkView.getMouseOverLink();
    PwToken toToken = tokenLink.getToToken();
    if ((currentMouseOverLink == null) ||
        ((currentMouseOverLink != null) &&
         (! ((TokenLink) currentMouseOverLink).getToToken().getId().equals
          ( toToken.getId())))) {
      tokenNetworkView.setMouseOverLink( tokenLink);
      String ruleViewKey = ViewGenerics.RULE_VIEW_TITLE +
        tokenNetworkView.getPartialPlan().getName();
      MDIInternalFrame ruleViewFrame = tokenNetworkView.getViewSet().getView( ruleViewKey);
      if (tokenNetworkView.getViewSet().getView( ruleViewKey) != null) {
        Container contentPane = ruleViewFrame.getContentPane();
        for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
          // System.err.println( "i " + i + " " +
          //                    contentPane.getComponent( i).getClass().getName());
          if (contentPane.getComponent( i) instanceof VizViewRuleView) {
            VizViewRuleView ruleView = (VizViewRuleView) contentPane.getComponent( i);
            ruleView.renderRuleText( toToken, tokenLink.getFromToken());
            break;
          }
        }
      } else {
        tokenNetworkView.setMouseOverLink( null);
      }
      return true;
    } else {
      return false;
    }
  } // end doUncapturedMouseMove



} // end class TokenLink
