// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: RuleLink.java,v 1.1 2003-12-03 02:29:51 taylor Exp $
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
import com.nwoods.jgo.JGoLabeledLink;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;

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

  private PredicateNode fromPredicateNode;
  private PredicateNode toPredicateNode;

  /**
   * <code>RuleLink</code> - constructor 
   *
   * @param fromPredicateNode - <code>PredicateNode</code> - 
   * @param toPredicateNode - <code>PredicateNode</code> - 
   */
  public RuleLink( PredicateNode fromPredicateNode, PredicateNode toPredicateNode) {
    super( fromPredicateNode.getPort(), toPredicateNode.getPort());
    this.fromPredicateNode = fromPredicateNode;
    this.toPredicateNode = toPredicateNode;
    this.setArrowHeads( false, true); // fromArrowHead toArrowHead
    // do no allow user to select and move links
    this.setRelinkable( false);

    fromPredicateNode.incrLinkCnt();
    toPredicateNode.incrLinkCnt();
    int maxLinkCnt = Math.min( fromPredicateNode.getLinkCnt(), toPredicateNode.getLinkCnt());
    // Bezier curve -- at least 4 points -- midPointLabel is *not* on curve :-(
    // this.setCubic( true);
    fromPredicateNode.getPort().setFromSpot( JGoObject.BottomCenter);
    fromPredicateNode.getPort().setToSpot( JGoObject.BottomCenter);
    toPredicateNode.getPort().setFromSpot( JGoObject.BottomCenter);
    toPredicateNode.getPort().setToSpot( JGoObject.BottomCenter);
    Point fromLinkPoint = fromPredicateNode.getPort().getFromLinkPoint();
    Point toLinkPoint = toPredicateNode.getPort().getToLinkPoint();
    Vector points = new Vector( NUM_LINK_POINTS);
    points.add( this.getStartPoint());
    int spanDelta = (int) (Math.abs( fromLinkPoint.getX() - toLinkPoint.getX()) /
                           (NUM_LINK_POINTS - 1));
    int yDelta = (int) (maxLinkCnt * ViewConstants.TIMELINE_VIEW_Y_DELTA);
    points.add( new Point( (int) (fromLinkPoint.getX() + spanDelta),
                           (int) (fromLinkPoint.getY() + (yDelta * 0.8))));
    points.add( new Point( (int) (fromLinkPoint.getX() + (NUM_LINK_POINTS - 4) * spanDelta),
                           (int) (fromLinkPoint.getY() + yDelta)));
    points.add( new Point( (int) (fromLinkPoint.getX() + (NUM_LINK_POINTS - 3) * spanDelta),
                           (int) (fromLinkPoint.getY() + yDelta)));
    points.add( new Point( (int) (fromLinkPoint.getX() + (NUM_LINK_POINTS - 2) * spanDelta),
                           (int) (fromLinkPoint.getY() + (yDelta * 0.8))));
    points.add( this.getEndPoint());
    // System.err.println( "points " + points);
    this.setPoints( points);
  } // end constructor

  /**
   * <code>getFromPredicateNode</code>
   *
   * @return - <code>PredicateNode</code> - 
   */
  public PredicateNode getFromPredicateNode() {
    return this.fromPredicateNode;
  }

  /**
   * <code>getToPredicateNode</code>
   *
   * @return - <code>PredicateNode</code> - 
   */
  public PredicateNode getToPredicateNode() {
    return this.toPredicateNode;
  }
} // end class RuleLink
