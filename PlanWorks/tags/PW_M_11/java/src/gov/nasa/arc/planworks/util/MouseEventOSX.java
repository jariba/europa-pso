// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: MouseEventOSX.java,v 1.1 2003-07-07 17:30:24 taylor Exp $
//
// Will Taylor -- started 12sept02 - in PlanViz
//

package gov.nasa.arc.planworks.util;

import java.awt.event.MouseEvent;

  /**
   * MouseEventOSX - handle mouse clicks for Apple Laptop with
   *     one-button mouse
   *         M-left: M-left
   *         M-middle: shift M-left
   *         M-right: control M-left
   *
   * @version .0.
   * @author Will Taylor NASA Ames Research Center, Code IC
   */
public class MouseEventOSX {

  // Linux/JDK 1.4.0 ctrl-M-l => 18
  // MacOSX/JDK 1.3.1 ctrl-M-l => 10  ?????
  private static final int MAC_OSX_CTRL_MOUSE_LEFT = 10;

  /**
   * <code>MouseEventOSX</code> - constructor 
   *
   */
  private MouseEventOSX() {
  } // end constructor 


  /**
   * <code>isMouseLeftClick</code>
   *
   *       Swing method  mouseClicked( MouseEvent mouseEvent)
   *
   * @param mouseEvent - <code>MouseEvent</code> - 
   * @param isMacOSX - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isMouseLeftClick( MouseEvent mouseEvent, boolean isMacOSX) {
    if ((! isMacOSX) &&
        (mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) {
      return true;
    } else if (isMacOSX &&
               (((mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) &&
                (! ((mouseEvent.getModifiers() & mouseEvent.SHIFT_MASK) > 0)) &&
                (! ((mouseEvent.getModifiers() & mouseEvent.CTRL_MASK) > 0)))) {
      return true;
    } else {
      return false;
    }
  } // end isMouseLeftClick

  /**
   * <code>isMouseLeftClick</code>
   *
   *       JGo method  doMouseClick( int modifiers, Point dc, Point vc, JGoView view) 
   *
   * @param modifiers - <code>int</code> - 
   * @param isMacOSX - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isMouseLeftClick( int modifiers, boolean isMacOSX) {
    if ((! isMacOSX) && (modifiers & MouseEvent.BUTTON1_MASK) > 0) {
      return true;
    } else if (isMacOSX &&
               (((modifiers & MouseEvent.BUTTON1_MASK) > 0) &&
                (! ((modifiers & MouseEvent.SHIFT_MASK) > 0)) &&
                (! ((modifiers & MouseEvent.CTRL_MASK) > 0)))) {
      return true;
    } else {
      return false;
    }
  } // end isMouseLeftClick

  /**
   * <code>isMouseMiddleClick</code>
   *
   *       Swing method  mouseClicked( MouseEvent mouseEvent)
   *
   * @param mouseEvent - <code>MouseEvent</code> - 
   * @param isMacOSX - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isMouseMiddleClick( MouseEvent mouseEvent, boolean isMacOSX) {
    if ((! isMacOSX) &&
        (mouseEvent.getModifiers() & mouseEvent.BUTTON2_MASK) > 0) {
      return true;
    } else if (isMacOSX &&
               (((mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) &&
                ((mouseEvent.getModifiers() & mouseEvent.SHIFT_MASK) > 0) &&
                (! ((mouseEvent.getModifiers() & mouseEvent.CTRL_MASK) > 0)))) {
      return true;
    } else {
      return false;
    }
  } // end isMouseMiddleClick

  /**
   * <code>isMouseMiddleClick</code>
   *
   *       JGo method  doMouseClick( int modifiers, Point dc, Point vc, JGoView view) 
   *
   * @param mouseEvent - <code>MouseEvent</code> - 
   * @param isMacOSX - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isMouseMiddleClick( int modifiers, boolean isMacOSX) {
    if ((! isMacOSX) && (modifiers & MouseEvent.BUTTON2_MASK) > 0) {
      return true;
    } else if (isMacOSX &&
               (((modifiers & MouseEvent.BUTTON1_MASK) > 0) &&
                ((modifiers & MouseEvent.SHIFT_MASK) > 0) &&
                (! ((modifiers & MouseEvent.CTRL_MASK) > 0)))) {
      return true;
    } else {
      return false;
    }
  } // end isMouseMiddleClick

  /**
   * <code>isMouseRightClick</code>
   *
   *       Swing method  mouseClicked( MouseEvent mouseEvent)
   *
   * @param mouseEvent - <code>MouseEvent</code> - 
   * @param isMacOSX - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isMouseRightClick( MouseEvent mouseEvent, boolean isMacOSX) {
    if ((! isMacOSX) &&
        (mouseEvent.getModifiers() & mouseEvent.BUTTON3_MASK) > 0) {
      return true;
    } else if (isMacOSX &&
               ((mouseEvent.getModifiers() & MAC_OSX_CTRL_MOUSE_LEFT) > 0)) {
      // (((mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) &&
      // (! ((mouseEvent.getModifiers() & mouseEvent.SHIFT_MASK) > 0)) &&
      // ((mouseEvent.getModifiers() & mouseEvent.CTRL_MASK) > 0))) {
      return true;
    } else {
      return false;
    }
  } // end isMouseRightClick

  /**
   * <code>isMouseRightClick</code>
   *
   *       JGo method  doMouseClick( int modifiers, Point dc, Point vc, JGoView view) 
   *
   * @param mouseEvent - <code>MouseEvent</code> - 
   * @param isMacOSX - <code>boolean</code> - 
   * @return - <code>boolean</code> - 
   */
  public static boolean isMouseRightClick( int modifiers, boolean isMacOSX) {
    if ((! isMacOSX) && (modifiers & MouseEvent.BUTTON3_MASK) > 0) {
      return true;
    } else if (isMacOSX &&
               ((modifiers & MAC_OSX_CTRL_MOUSE_LEFT) > 0)) {
      // (((modifiers & MouseEvent.BUTTON1_MASK) > 0) &&
      // (! ((modifiers & MouseEvent.SHIFT_MASK) > 0)) &&
      // ((modifiers & MouseEvent.CTRL_MASK) > 0))) {
      return true;
    } else {
      return false;
    }
  } // end isMouseRightClick

} // end class MouseEventOSX


