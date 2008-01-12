// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: HistogramElement.java,v 1.2 2003-10-02 23:24:21 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 24sep03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Rectangle;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;


/**
 * <code>HistogramElement</code> - JGo widget to render an element of a histogram

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class HistogramElement extends JGoRectangle {


  /**
   * <code>HistogramElement</code> - constructor 
   *
   * @param x - <code>int</code> - 
   * @param y - <code>int</code> - 
   * @param width - <code>int</code> - 
   * @param height - <code>int</code> - 
   * @param lineWidth - <code>int</code> - 
   * @param lineType - <code>int</code> - 
   * @param lineColor - <code>Color</code> - 
   * @param bgColor - <code>Color</code> - 
   */
  public HistogramElement( int x, int y, int width, int height, int lineWidth,
                           int lineType, Color lineColor, Color bgColor) {
    super(  new Rectangle( x, y, width, height));
    setDraggable( false);
    setResizable( false);
    setPen( new JGoPen( lineType, lineWidth,  lineColor));
    setBrush( JGoBrush.makeStockBrush( bgColor));
  } // end constructor



} // end class HistogramElement
