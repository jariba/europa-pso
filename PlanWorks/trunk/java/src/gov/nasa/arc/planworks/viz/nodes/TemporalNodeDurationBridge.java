// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TemporalNodeDurationBridge.java,v 1.1 2003-08-26 01:37:12 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 21August03
//

package gov.nasa.arc.planworks.viz.nodes;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoStroke;


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
    buffer.append( String.valueOf( minDurationTime));
    buffer.append( ", ").append( String.valueOf( maxDurationTime));
    return  buffer.append( "]").toString();
  }

} // end class TemporalNodeDurationBridge
                               
