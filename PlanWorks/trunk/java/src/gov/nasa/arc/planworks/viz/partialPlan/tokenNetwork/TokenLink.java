// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenLink.java,v 1.5 2004-06-10 01:36:08 taylor Exp $
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

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwEntity;
import gov.nasa.arc.planworks.db.PwRule;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.ExtendedBasicNode;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;


/**
 * <code>TokenLink</code> - JGo widget to render a link 
 *                          between a token (PwToken) and a rule (PwRule),
 *                          or a rule and a token
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenLink extends JGoLabeledLink {

  private BasicNode fromNode;
  private BasicNode toNode;
  private TokenNetworkView tokenNetworkView;

  public TokenLink( BasicNode fromNode, BasicNode toNode, int penWidth,
                    TokenNetworkView tokenNetworkView) {
    super( fromNode.getPort(), toNode.getPort());
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.tokenNetworkView = tokenNetworkView;
    this.setArrowHeads( false, true); // fromArrowHead toArrowHead
    // do no allow user to select and move links
    this.setRelinkable( false);
    this.setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
  } // end constructor

//   /**
//    * <code>getFromEntity</code>
//    *
//    * @return - <code>PwEntity</code> - 
//    */
//   public PwEntity getFromEntity() {
//     if (fromNode instanceof TokenNode) {
//       return ((TokenNode) this.fromNode).getToken();
//     } else if (fromNode instanceof RuleNode) {
//       return ((RuleNode) this.fromNode).getRule();
//     } else {
//       return null;
//     }
//   }

//   /**
//    * <code>getFromEntityNode</code>
//    *
//    * @return - <code>BasicNode</code> - 
//    */
//   public BasicNode getFromEntityNode() {
//     return this.fromNode;
//   }

//   /**
//    * <code>getToEntity</code>
//    *
//    * @return - <code>PwEntity</code> - 
//    */
//   public PwEntity getToEntity() {
//     if (toNode instanceof TokenNode) {
//       return ((TokenNode) this.toNode).getToken();
//     } else if (toNode instanceof RuleNode) {
//       return ((RuleNode) this.toNode).getRule();
//     } else {
//       return null;
//     }
//   }

//   /**
//    * <code>getToEntityNode</code>
//    *
//    * @return - <code>BasicNode</code> - 
//    */
//   public BasicNode getToTokenNode() {
//     return this.toNode;
//   }


} // end class TokenLink
