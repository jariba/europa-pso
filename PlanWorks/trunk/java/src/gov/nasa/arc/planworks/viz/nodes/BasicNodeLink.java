// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: BasicNodeLink.java,v 1.2 2003-08-06 01:20:13 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 28july03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Point;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoLabeledLink;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoStroke;

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
  private String linkName;

  /**
   * <code>BasicNodeLink</code> - constructor 
   *
   * @param fromVariableNode - <code>TokenNode</code> - 
   * @param toTokenNode - <code>TokenNode</code> - 
   */
  public BasicNodeLink( BasicNode fromNode, BasicNode toNode, String linkName) {
    super( fromNode.getPort(), toNode.getPort());
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.linkName = linkName;
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

  /**
   * <code>getLinkName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getLinkName() {
    return linkName;
  }

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public String toString() {
    return linkName;
  }

  /**
   * <code>equals</code>
   *
   * @param node - <code>Object</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( BasicNodeLink link) {
    return (this.linkName.equals( link.getLinkName()));
  }



} // end class BasicNodeLink
