// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNodeDurationBridge.java,v 1.6 2004-05-08 01:44:16 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 21August03
//

package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;


// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.util.ColorMap;


/**
 * <code>TemporalNodeDurationBridge</code> - JGo widget to render a temporal token's
 *                             min/max duration extents with tool tip 

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TemporalNodeDurationBridge extends JGoStroke {

  private int minDurationTime;
  private int maxDurationTime;

  /**
   * <code>TemporalNodeDurationBridge</code> - constructor 
   *
   * @param minDurationTime - <code>int</code> - 
   * @param maxDurationTime - <code>int</code> - 
   */
  public TemporalNodeDurationBridge( int minDurationTime, int maxDurationTime, int penWidth) {
    this.minDurationTime = minDurationTime;
    this.maxDurationTime = maxDurationTime;
    setDraggable( false);
    setResizable(false);
    setPen( new JGoPen( JGoPen.SOLID, penWidth, ColorMap.getColor( "black")));
  }

  /**
   * <code>getMinDurationTime</code>
   *
   * @return - <code>int</code> - 
   */
  public int getMinDurationTime() {
    return minDurationTime;
  }

  /**
   * <code>getMaxDurationTime</code>
   *
   * @return - <code>int</code> - 
   */
  public int getMaxDurationTime() {
    return maxDurationTime;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    if (minDurationTime != maxDurationTime) {
      StringBuffer buffer = new StringBuffer( "[");
      if (minDurationTime == DbConstants.MINUS_INFINITY_INT) {
        buffer.append( "-Infinity");
      } else {
        buffer.append( String.valueOf( minDurationTime));
      }
      if (maxDurationTime == DbConstants.PLUS_INFINITY_INT) {
        buffer.append( ", ").append( "Infinity");
      } else {
        buffer.append( ", ").append( String.valueOf( maxDurationTime));
      }
      return  buffer.append( "]").toString();
    } else {
      if (maxDurationTime == DbConstants.PLUS_INFINITY_INT) {
        return "Infinity";
      } else {
        return String.valueOf( maxDurationTime);
      }
    }
  }

} // end class TemporalNodeDurationBridge
                               
    
