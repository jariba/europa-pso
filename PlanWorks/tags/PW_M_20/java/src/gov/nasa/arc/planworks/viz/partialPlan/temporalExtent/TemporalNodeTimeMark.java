// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNodeTimeMark.java,v 1.4 2004-07-12 18:16:30 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- started 21August03
//

package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoPolygon;

import gov.nasa.arc.planworks.db.DbConstants;

/**
 * <code>TemporalNodeTimeMark</code> - JGo widget to render a temporal token's
 *                             time mark extents with tool tip displaying the time

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalNodeTimeMark extends JGoPolygon {

  private int time;
  private int type;

  /**
   * <code>TemporalNodeTimeMark</code> - constructor 
   *
   * @param time - <code>int</code> - 
   * @param type - <code>int</code> - 
   */
  public TemporalNodeTimeMark( int time, int type) {
    this.time = time;
    this.type = type;
  }

  /**
   * <code>getTime</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTime() {
    return time;
  }
  
  /**
   * <code>getType</code>
   *
   * @return - <code>int</code> - 
   */
  public int getType() {
    return type;
  }
  
  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (time == DbConstants.PLUS_INFINITY_INT) {
      //return "Infinity";
      return DbConstants.PLUS_INFINITY_UNIC;
    } else if (time == DbConstants.MINUS_INFINITY_INT) {
      //return "-Infinity";
      return DbConstants.MINUS_INFINITY_UNIC;
    } else {
      return String.valueOf( time);
    }
  }

} // end class TemporalNodeTimeMark
                               
