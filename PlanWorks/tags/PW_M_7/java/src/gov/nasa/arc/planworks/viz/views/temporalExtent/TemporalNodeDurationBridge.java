// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNodeDurationBridge.java,v 1.1 2003-09-18 20:48:48 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 21August03
//

package gov.nasa.arc.planworks.viz.views.temporalExtent;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoStroke;

import gov.nasa.arc.planworks.db.PwDomain;


/**
 * <code>TemporalNodeDurationBridge</code> - JGo widget to render a temporal token's
 *                             min/max duration extents with tool tip 
 *             Object->JGoObject->JGoDrawable->JGoStroke->TemporalNodeDurationBridge

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
  public TemporalNodeDurationBridge( int minDurationTime, int maxDurationTime) {
    this.minDurationTime = minDurationTime;
    this.maxDurationTime = maxDurationTime;
  }

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer buffer = new StringBuffer( "[");
    if (minDurationTime == PwDomain.MINUS_INFINITY_INT) {
      buffer.append( "-Infinity");
    } else {
      buffer.append( String.valueOf( minDurationTime));
    }
    if (maxDurationTime == PwDomain.PLUS_INFINITY_INT) {
      buffer.append( ", ").append( "Infinity");
    } else {
      buffer.append( ", ").append( String.valueOf( maxDurationTime));
    }
    return  buffer.append( "]").toString();
  }

} // end class TemporalNodeDurationBridge
                               
    
