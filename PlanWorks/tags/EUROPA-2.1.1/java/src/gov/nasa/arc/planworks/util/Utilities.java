// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: Utilities.java,v 1.13 2004-08-26 23:03:23 miatauro Exp $
//
// PlanWorks
//
// Will Taylor -- 
//

package gov.nasa.arc.planworks.util;

import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;


/**
 * <code>Utilities</code> - singleton class with static methods
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *              NASA Ames Research Center - Code IC
 * @version 1.4
 */
public abstract class Utilities {

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
  public static Point computeNestedLocation( final Point currentLocation, 
                                             final Container argContainer,
                                             final boolean isLocationAbsolute) {
    Point location = null;
    Container container = argContainer;
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
  public static String getUrlLeaf( final String seqUrl) {
    int index = seqUrl.lastIndexOf( System.getProperty( "file.separator"));
    return seqUrl.substring( index + 1);
  }


  /**
   * <code>sortStringKeySet</code> - sort Hash map keys, that are Strings
   *
   * @param map - <code>Map</code> - 
   * @return - <code>List</code> - of String
   */
  public static List sortStringKeySet( final Map map) {
    List names = new ArrayList();
    Iterator keyItr = map.keySet().iterator();
    while (keyItr.hasNext()) {
      names.add( (String) keyItr.next());
    }
    Collections.sort( names, new StringNameComparator());
    return names;
  } // end sortStringKeySet


  /**
   * <code>getPopupLocation</code> - center of frame
   *
   * @param frame - <code>JFrame</code> - 
   * @return - <code>Point</code> -
   */
  public static Point getPopupLocation( final JFrame frame) {
    return new Point( (int) (frame.getLocation().getX() + (frame.getSize().getWidth() / 2)),
                      (int) (frame.getLocation().getY() + (frame.getSize().getHeight() / 2)));
  } // end getPopupLocation


  /**
   * <code>setPopupLocation</code> - place pop up window in center of frame
   *
   * @param popUp - <code>Window</code> - 
   * @param frame - <code>JFrame</code> - 
   */
  public static void setPopupLocation( final Window popUp, final JFrame frame) {
    popUp.setLocation( (int) (frame.getLocation().getX() +
                              (frame.getSize().getWidth() / 2) -
                              (popUp.getSize().getWidth() / 2)),
                       (int) (frame.getLocation().getY() +
                              (frame.getSize().getHeight() / 2) -
                              (popUp.getSize().getHeight() / 2)));
  } // end setPopupLocation


  /**
   * <code>trimView</code> - return view name string with "View" suffix removed
   *
   * @param viewName - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String trimView( final String viewName) {
    int temp = 0;
    if((temp = viewName.indexOf(" View")) != -1) {
      return viewName.substring(0, temp);
    }
    return viewName.substring( 0, viewName.indexOf( "View"));
  }


  /**
   * <code>getStepNumber</code> - strip "step" prefix and make into int
   *
   * @param partialPlanName - <code>String</code> - 
   * @return - <code>int</code> - 
   */
  public static int getStepNumber( final String partialPlanName) {
    return Integer.parseInt( partialPlanName.substring( 4)); // strip off step
  }


  public static int countOccurrences(char item, String str) {
    int retval = 0;
    char [] chars = str.toCharArray();
    for(int i = 0; i < chars.length; i++) {
      if(chars[i] == item) {
        retval++;
      }
    }
    return retval;
  }

  public static final void tracePrint(final boolean print) {
    if(print) {
      StackTraceElement st = null;
      System.err.println("-------------------");
      st = (new Throwable()).getStackTrace()[2];
      System.err.println(st.getClassName() + "." + st.getMethodName() + "() | " +
                         st.getFileName() + ":" + st.getLineNumber());
      st = (new Throwable()).getStackTrace()[1];
      System.err.println(st.getClassName() + "." + st.getMethodName() + "() | " +
                         st.getFileName() + ":" + st.getLineNumber());
      System.err.println("-------------------");
    }
  }

  public static final void tracePrint(final boolean print, final String extra) {
    if(print) {
      StackTraceElement st = (new Throwable()).getStackTrace()[1];
      System.err.println(st.getClassName() + "." + st.getMethodName() + "() | " +
                         st.getFileName() + ":" + st.getLineNumber() + " (" + extra + ")");
    }
  }

  public static final void debugPrint(final boolean print, final String text) {
    if(print)
      System.err.println(text);
  }

  public static final int currentLineNumber() {
    StackTraceElement st = (new Throwable()).getStackTrace()[1];
    return st.getLineNumber();
  }
} // end class Utilities
