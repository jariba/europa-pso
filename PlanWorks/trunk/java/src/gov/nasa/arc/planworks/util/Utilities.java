// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: Utilities.java,v 1.4 2003-10-07 02:13:34 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- 
//

package gov.nasa.arc.planworks.util;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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


  /**
   * <code>getUrlLeaf</code>
   *
   * @param seqUrl - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String getUrlLeaf( String seqUrl) {
    int index = seqUrl.lastIndexOf( System.getProperty( "file.separator"));
    return seqUrl.substring( index + 1);
  }


  /**
   * <code>sortStringKeySet</code> - sort Hash map keys, that are Strings
   *
   * @param map - <code>Map</code> - 
   * @return - <code>List</code> - of String
   */
  public static List sortStringKeySet( Map map) {
    List names = new ArrayList();
    Iterator keyItr = map.keySet().iterator();
    while (keyItr.hasNext()) {
      names.add( (String) keyItr.next());
    }
    Collections.sort( names, new StringNameComparator());
    return names;
  } // end sortStringKeySet


} // end class Utilities
