// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: BasicNodeLink.java,v 1.1 2003-07-30 00:43:19 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 28july03
//

package gov.nasa.arc.planworks.viz.nodes;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoLabeledLink;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;


/**
 * <code>BasicNodeLink</code> - JGo widget to render a link with a
 *                          label between two BasicNode nodes
 *             Object->JGoObject->JGoArea->JGoLabeledLink->BasicNodeLink
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class BasicNodeLink extends JGoLabeledLink {

  private BasicNode fromNode;
  private BasicNode toNode;

  /**
   * <code>BasicNodeLink</code> - constructor 
   *
   * @param fromVariableNode - <code>TokenNode</code> - 
   * @param toTokenNode - <code>TokenNode</code> - 
   */
  public BasicNodeLink( BasicNode fromNode, BasicNode toNode) {
    super( fromNode.getPort(), toNode.getPort());
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.setArrowHeads( false, true); // fromArrowHead toArrowHead
  } // end constructor

  /**
   * <code>getFromNode</code>
   *
   * @return - <code>BasicNode</code> - 
   */
  public BasicNode getFromNode() {
    return this.fromNode;
  }

  /**
   * <code>getToNode</code>
   *
   * @return - <code>BasicNode</code> - 
   */
  public BasicNode getToNode() {
    return this.toNode;
  }


} // end class BasicNodeLink
