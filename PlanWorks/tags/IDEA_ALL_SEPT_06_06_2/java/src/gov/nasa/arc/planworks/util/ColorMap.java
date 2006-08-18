// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ColorMap.java,v 1.5 2006-06-30 22:40:54 meboyce Exp $
//
// PlanWorks
//
// Will Taylor -- taken form PlanViz
//

package gov.nasa.arc.planworks.util;

import java.awt.SystemColor;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

  /**
   * ColorMap - singleton class
   * map X11 color names to rgb values
   * using Linux Mandrake 7.2  gcolorsel utility
   * and HashMap, which is not synchronized
   *
   * @version 1.3
   * @author Will Taylor NASA Ames Research Center, Code IC
   */
public class ColorMap {

  private static Map x11Colors = new HashMap();

  static {
    fillColorMap();
  }

  // constructor
  private ColorMap() {
  } // end constructor 


  private static void fillColorMap() {
    x11Colors.put( "aquamarine", new Color( 127, 255, 212));
    x11Colors.put( "aquamarine3", new Color( 102, 205, 170));
    x11Colors.put( "black", new Color( 0, 0, 0));
    x11Colors.put( "blue", new Color( 0, 0, 255));
    x11Colors.put( "chartreuse1", new Color( 127, 255, 0));
    x11Colors.put( "cornflowerBlue", new Color( 100, 149, 237));
    x11Colors.put( "cyan", new Color( 0, 255, 255));
    x11Colors.put( "darkSeaGreen1", new Color( 193, 255, 193));
    x11Colors.put( "deepSkyBlue", new Color( 0, 191, 255));
    x11Colors.put( "gray25", new Color( 64, 64, 64));  // almost black
    x11Colors.put( "gray60", new Color( 153, 153, 153));  // java.awt.Color.gray
    x11Colors.put( "green", new Color( 0, 255, 0)); // java.awt.Color.green
    x11Colors.put( "green3", new Color( 0, 205, 0));
    x11Colors.put( "khaki2", new Color( 238, 230, 133));  
    x11Colors.put( "lightGray", new Color( 192, 192, 192));  // java.awt.Color.lightGray
    x11Colors.put( "lightYellow", new Color( 255, 255, 224)); 
    x11Colors.put( "magenta", new Color( 255, 0, 255));
    x11Colors.put( "mediumBlue", new Color( 0, 0, 205));
    x11Colors.put( "mediumOrchid4", new Color( 122, 55, 139));
    x11Colors.put( "paleGreen", new Color( 152, 251, 152));
    x11Colors.put( "pink", new Color( 255, 192, 203));
    x11Colors.put( "plum", new Color( 221, 160, 221));
    x11Colors.put( "purple", new Color( 160, 32, 240));
    x11Colors.put( "red", new Color( 255, 0, 0));
    x11Colors.put( "rosyBrown", new Color( 188, 143, 143));
    x11Colors.put( "seaGreen1", new Color( 84, 255, 159));
    x11Colors.put( "skyBlue", new Color( 135, 206, 235));
    x11Colors.put( "white", new Color( 255, 255, 255));
    x11Colors.put( "yellow", new Color( 255, 255, 0));
    x11Colors.put( "System.background", SystemColor.window);
    x11Colors.put( "System.text.background", SystemColor.text);
    x11Colors.put( "System.text.foreground", SystemColor.textText);
  } // end fillColorMap 


  /**
   * getColor - maps X11 color name to rgb value
   *            (static method)
   *
   * @param colorName - String - X11 color name
   * @return colorObject - Color - instance with rgb value
   */
  public static Color getColor( final String colorName) {
    Color colorObject = (Color) x11Colors.get( colorName);
    if (colorObject == null) {
      System.err.println( "ColorMap.getColor cannot handle " + colorName +
                          ", use white ");
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
      colorObject = (Color) x11Colors.get( "white");
    }
    return colorObject;
  }
        
}
