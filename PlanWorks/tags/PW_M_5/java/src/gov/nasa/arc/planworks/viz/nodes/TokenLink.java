// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenLink.java,v 1.3 2003-08-20 18:52:36 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 01july03
//

package gov.nasa.arc.planworks.viz.nodes;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoLabeledLink;

import gov.nasa.arc.planworks.db.PwToken;


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

  /**
   * <code>TokenLink</code> - constructor 
   *
   * @param fromTokenNode - <code>TokenNode</code> - 
   * @param toTokenNode - <code>TokenNode</code> - 
   */
  public TokenLink( TokenNode fromTokenNode, TokenNode toTokenNode) {
    super( fromTokenNode.getPort(), toTokenNode.getPort());
    this.fromTokenNode = fromTokenNode;
    this.toTokenNode = toTokenNode;
    this.setArrowHeads( false, true); // fromArrowHead toArrowHead
    // do no allow user to select and move links
    this.setRelinkable( false);
  } // end constgructor

  /**
   * <code>getFromToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getFromToken() {
    return this.fromTokenNode.getToken();
  }

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

  public TokenNode getToTokenNode() {
    return this.toTokenNode;
  }
} // end class TokenLink
