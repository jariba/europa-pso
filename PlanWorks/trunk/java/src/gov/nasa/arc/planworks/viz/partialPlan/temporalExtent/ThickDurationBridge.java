// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ThickDurationBridge.java,v 1.2 2004-01-02 18:58:59 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 15Dec03
//

package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;

import java.awt.Color;
import java.awt.Rectangle;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.util.ColorMap;


/**
 * <code>ThickDurationBridge</code> - JGo widget to render a temporal token's
 *                             min/max duration extents with tool tip and rectangle

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ThickDurationBridge extends JGoRectangle {

  private int minDurationTime;
  private int maxDurationTime;
  private String [] labelLines;

  /**
   * <code>ThickDurationBridge</code> - constructor 
   *
   * @param minDurationTime - <code>int</code> - 
   * @param maxDurationTime - <code>int</code> - 
   * @param x - <code>int</code> - 
   * @param y - <code>int</code> - 
   * @param width - <code>int</code> - 
   * @param height - <code>int</code> - 
   * @param backgroundColor - <code>Color</code> - 
   * @param labelLines - <code>String[]</code> - 
   */
  public ThickDurationBridge( int minDurationTime, int maxDurationTime, int x, int y,
                              int width, int height, Color backgroundColor,
                              String [] labelLines) {
    super( new Rectangle( x, y, width, height));
    this.minDurationTime = minDurationTime;
    this.maxDurationTime = maxDurationTime;
    this.labelLines = labelLines;
    setDraggable( false);
    setResizable(false);
    setPen( new JGoPen( JGoPen.SOLID, 1, ColorMap.getColor( "black")));
    setBrush( JGoBrush.makeStockBrush( backgroundColor));
  } // end constructor

  /**
   * <code>getToolTipText</code>
   *
   * @return - <code>String</code> - 
   */
  public String getToolTipText() {
    StringBuffer buffer = new StringBuffer( "<html>");
    buffer.append( labelLines[0]).append( "<br>");
    buffer.append( labelLines[1]).append( "<br>");
    if (minDurationTime != maxDurationTime) {
      buffer.append( "[");
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
      buffer.append( "]");
    } else {
      if (maxDurationTime == DbConstants.PLUS_INFINITY_INT) {
        buffer.append( "Infinity");
      } else {
        buffer.append( String.valueOf( maxDurationTime));
      }
    }
    return buffer.append( "</html>").toString();
    
  } // end getToolTipText

  /**
   * <code>getToolTipText</code> - when over 1/8 scale overview temporal node
   *
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer buffer = new StringBuffer( "<html> ");
    buffer.append( labelLines[0]).append( "<br>");
    buffer.append( labelLines[1]).append( "</html>");
    return buffer.toString();
  } // end getToolTipText

} // end class ThickDurationBridge
                               
    
