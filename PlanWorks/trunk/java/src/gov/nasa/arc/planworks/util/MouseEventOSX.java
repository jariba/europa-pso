// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: MouseEventOSX.java,v 1.4 2005-01-20 21:01:01 meboyce Exp $
//
// Will Taylor -- started 12sept02 - in PlanViz
//

package gov.nasa.arc.planworks.util;

import java.awt.event.MouseEvent;

  /**
   * MouseEventOSX - handle mouse clicks for Apple Laptop with three button mouse
   *         M-left: modifiers = 16
   *         M-middle: modifiers = 8
   *         M-right: modifiers = 4
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
  public static boolean isMouseLeftClick( final MouseEvent mouseEvent, final boolean isMacOSX) {
		if (isMacOSX && (mouseEvent.getModifiers() & MouseEvent.META_MASK) != 0) {
			return false;
		}
    if ((mouseEvent.getModifiers() & MouseEvent.BUTTON1_MASK) > 0) {
      return true;
    } else {
      return false;
    }
//     if ((! isMacOSX) &&
//         (mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) {
//       return true;
//     } else if (isMacOSX &&
//                (((mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) &&
//                 (! ((mouseEvent.getModifiers() & mouseEvent.SHIFT_MASK) > 0)) &&
//                 (! ((mouseEvent.getModifiers() & mouseEvent.CTRL_MASK) > 0)))) {
//       return true;
//     } else {
//       return false;
//     }
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
  public static boolean isMouseLeftClick( final int modifiers, final boolean isMacOSX) {
//     System.err.println( "isMouseLeftClick modifiers " + modifiers + " mask " +
//                         MouseEvent.BUTTON1_MASK);
		if (isMacOSX && (modifiers & MouseEvent.META_MASK) != 0) {
			return false;
		}
    if ((modifiers & MouseEvent.BUTTON1_MASK) > 0) {
      return true;
    } else {
      return false;
    }
//     if ((! isMacOSX) && (modifiers & MouseEvent.BUTTON1_MASK) > 0) {
//       return true;
//     }               (((modifiers & MouseEvent.BUTTON1_MASK) > 0) &&
//                 (! ((modifiers & MouseEvent.SHIFT_MASK) > 0)) &&
//                 (! ((modifiers & MouseEvent.CTRL_MASK) > 0)))) {
//       return true;
//     } else {
//       return false;
//     }
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
  public static boolean isMouseMiddleClick( final MouseEvent mouseEvent, final boolean isMacOSX) {
    if ((mouseEvent.getModifiers() & MouseEvent.BUTTON2_MASK) > 0) {
      return true;
    } else {
      return false;
    }
//     if ((! isMacOSX) &&
//         (mouseEvent.getModifiers() & mouseEvent.BUTTON2_MASK) > 0) {
//       return true;
//     } else if (isMacOSX &&
//                (((mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) &&
//                 ((mouseEvent.getModifiers() & mouseEvent.SHIFT_MASK) > 0) &&
//                 (! ((mouseEvent.getModifiers() & mouseEvent.CTRL_MASK) > 0)))) {
//       return true;
//     } else {
//       return false;
//     }
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
  public static boolean isMouseMiddleClick( final int modifiers, final boolean isMacOSX) {
//     System.err.println( "isMouseMiddleClick modifiers " + modifiers + " mask " +
//                         MouseEvent.BUTTON2_MASK);
    if ((modifiers & MouseEvent.BUTTON2_MASK) > 0) {
      return true;
    } else {
      return false;
    }
//     if ((! isMacOSX) && (modifiers & MouseEvent.BUTTON2_MASK) > 0) {
//       return true;
//     } else if (isMacOSX &&
//                (((modifiers & MouseEvent.BUTTON1_MASK) > 0) &&
//                 ((modifiers & MouseEvent.SHIFT_MASK) > 0) &&
//                 (! ((modifiers & MouseEvent.CTRL_MASK) > 0)))) {
//       return true;
//     } else {
//       return false;
//     }
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
  public static boolean isMouseRightClick( final MouseEvent mouseEvent, final boolean isMacOSX) {
		if (isMacOSX &&
		    (mouseEvent.getModifiers() & MouseEvent.META_MASK) != 0 &&
				(mouseEvent.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
			return true;
		}
    if ((mouseEvent.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
      return true;
    } else {
      return false;
    }
//     if ((! isMacOSX) &&
//         (mouseEvent.getModifiers() & mouseEvent.BUTTON3_MASK) > 0) {
//       return true;
//     } else if (isMacOSX &&
//                ((mouseEvent.getModifiers() & MAC_OSX_CTRL_MOUSE_LEFT) > 0)) {
//       // (((mouseEvent.getModifiers() & mouseEvent.BUTTON1_MASK) > 0) &&
//       // (! ((mouseEvent.getModifiers() & mouseEvent.SHIFT_MASK) > 0)) &&
//       // ((mouseEvent.getModifiers() & mouseEvent.CTRL_MASK) > 0))) {
//       return true;
//     } else {
//       return false;
//     }
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
  public static boolean isMouseRightClick( final int modifiers, final boolean isMacOSX) {
//     System.err.println( "isMouseRightClick modifiers " + modifiers + " mask " +
//                         MouseEvent.BUTTON3_MASK);
		if (isMacOSX &&
		    (modifiers & MouseEvent.META_MASK) != 0 &&
				(modifiers & MouseEvent.BUTTON1_MASK) != 0) {
			return true;
		}
    if ((modifiers & MouseEvent.BUTTON3_MASK) > 0) {
      return true;
    } else {
      return false;
    }
//     if ((! isMacOSX) && (modifiers & MouseEvent.BUTTON3_MASK) > 0) {
//       return true;
//     } else if (isMacOSX &&
//                ((modifiers & MAC_OSX_CTRL_MOUSE_LEFT) > 0)) {
//       // (((modifiers & MouseEvent.BUTTON1_MASK) > 0) &&
//       // (! ((modifiers & MouseEvent.SHIFT_MASK) > 0)) &&
//       // ((modifiers & MouseEvent.CTRL_MASK) > 0))) {
//       return true;
//     } else {
//       return false;
//     }
  } // end isMouseRightClick

} // end class MouseEventOSX


