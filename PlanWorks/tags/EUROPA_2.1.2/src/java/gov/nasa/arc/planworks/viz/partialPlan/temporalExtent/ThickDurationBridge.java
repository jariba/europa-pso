// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ThickDurationBridge.java,v 1.7 2004-06-10 01:36:06 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 15Dec03
//

package gov.nasa.arc.planworks.viz.partialPlan.temporalExtent;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView;


/**
 * <code>ThickDurationBridge</code> - JGo widget to render a temporal token's
 *                             min/max duration extents with tool tip and rectangle

 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *       NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ThickDurationBridge extends JGoRectangle implements OverviewToolTip {

  private int minDurationTime;
  private int maxDurationTime;
  private String [] labelLines;
  private TemporalNode temporalNode;
  private TemporalExtentView temporalExtentView;

  public ThickDurationBridge( int minDurationTime, int maxDurationTime, int x, int y,
                              int width, int height, Color backgroundColor,
                              String [] labelLines, TemporalNode temporalNode,
                              TemporalExtentView temporalExtentView) {
    super( new Rectangle( x, y, width, height));
    this.minDurationTime = minDurationTime;
    this.maxDurationTime = maxDurationTime;
    this.labelLines = labelLines; 
    this.temporalNode = temporalNode;
    this.temporalExtentView = temporalExtentView;
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
   *                               implements OverviewToolTip
   * @param isOverview - <code>boolean</code> - 
   * @return - <code>String</code> - 
   */
  public String getToolTipText( boolean isOverview) {
    StringBuffer buffer = new StringBuffer( "<html> ");
    buffer.append( labelLines[0]).append( "<br>");
    buffer.append( labelLines[1]).append( "</html>");
    return buffer.toString();
  } // end getToolTipText

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
    ThickDurationBridge thickBridge = (ThickDurationBridge) obj.getTopLevelObject();
    if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
      // nothing
    } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( viewCoords);
      return true;
    }
    return false;
  } // end doMouseClick   

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();

    JMenuItem navigatorItem = new JMenuItem( "Open Navigator View");
    navigatorItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          String viewSetKey = temporalExtentView.getNavigatorViewSetKey();
          MDIInternalFrame navigatorFrame =
            temporalExtentView.openNavigatorViewFrame( viewSetKey);
          Container contentPane = navigatorFrame.getContentPane();
          PwPartialPlan partialPlan = temporalExtentView.getPartialPlan();
          contentPane.add( new NavigatorView( temporalNode.getToken(), partialPlan,
                                              temporalExtentView.getViewSet(),
                                              viewSetKey, navigatorFrame));
        }
      });
    mouseRightPopup.add( navigatorItem);

    ViewGenerics.showPopupMenu( mouseRightPopup, temporalExtentView, viewCoords);
  } // end mouseRightPopupMenu

} // end class ThickDurationBridge
                               
    
