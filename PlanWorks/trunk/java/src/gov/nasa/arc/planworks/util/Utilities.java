// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: Utilities.java,v 1.3 2003-08-29 01:21:39 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- 
//

package gov.nasa.arc.planworks.util;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;


/**
 * <code>Utilities</code> - singleton class with static methods
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *              NASA Ames Research Center - Code IC
 * @version 1.4
 */
public class Utilities {

  // constructor
  private Utilities() {
  } // end constructor 

  /**
   * <code>computeNestedLocation</code> - 
   *
   *  derived from code written by Steve Wragg - NASA Ames, Code IC
   *
   * @param currentLocation - <code>Point</code> - 
   * @param container - <code>Container</code> - 
   * @param isLocationAbsolute - <code>boolean</code> - location will be screen coords,
   *                           otherwise location will be relative to enclosing JFrame
   * @return currentLocation - <code>Point</code> - 
   */
  public static Point computeNestedLocation( Point currentLocation, Container container,
                                             boolean isLocationAbsolute) {
    Point location = null;
    while (container != null) {
      location = container.getLocation();
      // System.err.println( "locationX " + location.x + " locationY " + location.y);
      container = container.getParent();
      if (isLocationAbsolute || ((!isLocationAbsolute) && (container != null))) {
        currentLocation.translate( location.x, location.y);
        // System.err.println( "currentLocation " + currentLocation);
      }
    }
    return currentLocation;
  } // end computePopUpLocation


  /**
   * <code>printFontNames</code>
   *
   */
  public static void printFontNames() {
    GraphicsEnvironment graphicsEnv =
      GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] fontNames = graphicsEnv.getAvailableFontFamilyNames();
    System.err.println( " Font names: ");
    for (int i = 0, n = fontNames.length; i < n; i++) {
      System.err.println( "  " + fontNames[i]);
    }
  }



} // end class Utilities
