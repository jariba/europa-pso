// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: RuleLink.java,v 1.2 2003-12-12 01:23:06 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 01dec03
//

package gov.nasa.arc.planworks.viz.sequence.modelRules;

import java.awt.Point;
import java.util.Vector;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoLabeledLink;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>RuleLink</code> - JGo widget to render a "rule" link with a
 *                          label between two predicate nodes
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class RuleLink extends JGoLabeledLink {

  private static final int NUM_LINK_POINTS = 6;

  private BasicNode fromNode;
  private BasicNode toNode;
  private String ruleType;

  /**
   * <code>RuleLink</code> - constructor 
   *
   * @param fromNode - <code>BasicNode</code> - 
   * @param toNode - <code>BasicNode</code> - 
   * @param ruleType - <code>String</code> - 
   */
  public RuleLink( BasicNode fromNode, BasicNode toNode, String ruleType) {
    super( fromNode.getPort(), toNode.getPort());
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.ruleType = ruleType;
    this.setArrowHeads( false, true); // fromArrowHead toArrowHead
    // do no allow user to select and move links
    this.setRelinkable( false);
    this.setSelectable( false); 

//     System.err.println( "\nRuleLink.constructor: ruleType " + ruleType +
//                         " fromNode " + fromNode.getName() +
//                         " toNode " + toNode.getName());
    if (ModelRulesView.RULE_TYPE_LIST.indexOf( ruleType) == -1) {
      System.err.println( "RuleLink.constructor: ruleType " + ruleType + " not handled");
      System.exit( -1);
    }

//     Point fromLinkPoint = fromNode.getPort().getFromLinkPoint();
//     Point toLinkPoint = toNode.getPort().getToLinkPoint();
//     // Bezier curve -- at least 4 points -- midPointLabel is *not* on curve :-(
//     // this.setCubic( true);
//     Vector points = new Vector( NUM_LINK_POINTS);
//     points.add( fromLinkPoint);
//     int spanDelta = (int) (Math.abs( fromLinkPoint.getX() - toLinkPoint.getX()) /
//                            (NUM_LINK_POINTS - 1));
//     // RULE_MEETS & RULE_CONTAINS links are below link points 
//     int yDelta = ViewConstants.TIMELINE_VIEW_Y_DELTA;
//     if (ruleType.equals( ModelRulesView.RULE_MET_BY) ||
//         ruleType.equals( ModelRulesView.RULE_CONTAINED_BY)) {
//       // RULE_MET_BY & RULE_CONTAINED_BY links are "above" link points
//       yDelta = - yDelta;
//       spanDelta = - spanDelta;
//     }
//     points.add( new Point( (int) (fromLinkPoint.getX() + spanDelta),
//                            (int) (fromLinkPoint.getY() + (yDelta * 0.8))));
//     points.add( new Point( (int) (fromLinkPoint.getX() + (NUM_LINK_POINTS - 4) * spanDelta),
//                            (int) (fromLinkPoint.getY() + yDelta)));
//     points.add( new Point( (int) (fromLinkPoint.getX() + (NUM_LINK_POINTS - 3) * spanDelta),
//                            (int) (fromLinkPoint.getY() + yDelta)));
//     points.add( new Point( (int) (fromLinkPoint.getX() + (NUM_LINK_POINTS - 2) * spanDelta),
//                            (int) (fromLinkPoint.getY() + (yDelta * 0.8))));
//     points.add( toLinkPoint);
//     // System.err.println( "points " + points);
//     this.setPoints( points);
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
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    return ruleType;
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview token node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    return null;
  } // end getToolTipText


} // end class RuleLink
