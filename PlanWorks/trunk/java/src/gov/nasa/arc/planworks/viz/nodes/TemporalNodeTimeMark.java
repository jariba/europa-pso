// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNodeTimeMark.java,v 1.1 2003-08-26 01:37:12 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 21August03
//

package gov.nasa.arc.planworks.viz.nodes;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoPolygon;

import gov.nasa.arc.planworks.db.PwDomain;

/**
 * <code>TemporalNodeTimeMark</code> - JGo widget to render a temporal token's
 *                             time mark extents with tool tip displaying the time
 *             Object->JGoObject->JGoDrawable->JGoPolygon>TemporalNodeTimeMark

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalNodeTimeMark extends JGoPolygon {

  private int time;

  /**
   * <code>TemporalNodeTimeMark</code> - constructor 
   *
   */
  public TemporalNodeTimeMark( int time) {
    this.time = time;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (time == PwDomain.PLUS_INFINITY_INT) {
      return "Infinity";
    } else if (time == PwDomain.MINUS_INFINITY_INT) {
      return "-Infinity";
    } else {
      return String.valueOf( time);
    }
  }

} // end class TemporalNodeTimeMark
                               
