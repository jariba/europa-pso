// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: HistogramElement.java,v 1.1 2003-09-25 23:52:43 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 24sep03
//

package gov.nasa.arc.planworks.viz.nodes;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.Extent;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>TemporalNode</code> - JGo widget to render an element of a histogram

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


  /**
   * <code>doMouseClick</code> - 
   *
   * @param modifiers - <code>int</code> - 
   * @param docCoords - <code>Point</code> - 
   * @param viewCoords - <code>Point</code> - 
   * @param view - <code>JGoView</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean doMouseClick( int modifiers, Point docCoords, Point viewCoords,
                               JGoView view) {
    JGoObject obj = view.pickDocObject( docCoords, false);
    //         System.err.println( "doMouseClick obj class " +
    //                             obj.getTopLevelObject().getClass().getName());
    HistogramElement histogramElement = (HistogramElement) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      // mouseRightPopupMenu( viewCoords);
      // return true;
    }
    return false;
  } // end doMouseClick   


} // end class HistogramElement
