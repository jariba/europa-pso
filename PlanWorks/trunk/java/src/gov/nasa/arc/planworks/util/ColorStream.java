package gov.nasa.arc.planworks.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorStream {

  private Map hashMap;
  private static final int COLOR_INC = 69/*51*/;
  private static final float MIN_BRIGHTNESS = 0.75f;
  private int r, g, b;

  public ColorStream() {
    hashMap = new HashMap();
    r = 255;
    g = 153;
    b = 204;
  }
  public ColorStream(int r, int g, int b) {
    this();
  }
  public ColorStream(Color startColor) {
    this();
  }
  public Color nextColor() {
    float [] HSB = Color.RGBtoHSB(r, g, b, null);
    Color retval = new Color(r, g, b);
    while(HSB[2] < MIN_BRIGHTNESS) {
      retval = retval.brighter();
      HSB = Color.RGBtoHSB(retval.getRed(), retval.getGreen(), retval.getBlue(), null);
    }
    System.err.println("Color <" + r + ", " + g + ", " + b + "> {" + HSB[0] + ", " + HSB[1] + ", " +
                       HSB[2] + "}");
    r = retval.getRed();
    g = retval.getGreen();
    b = retval.getBlue();

    r += COLOR_INC;
    if(r > 255) {
      r %= 255;
      g += COLOR_INC;
    }
    if(g > 255) {
      g %= 255;
      b += COLOR_INC;
    }
    if(b > 255) {
      b %= 255;
      r = COLOR_INC;
      g = COLOR_INC * 2;
    }
    return retval;
  }

  public Color getColor(int index) {
    Color indexColor = (Color) hashMap.get( new Integer( index));
    // System.err.println( "getColor index " + index + " indexColor " + indexColor);
    if (indexColor == null) {
      indexColor = nextColor();
      hashMap.put( new Integer( index), indexColor);
     }
    return indexColor;
  }

  public void setColor(Color color) {
    r = color.getRed();
    g = color.getGreen();
    b = color.getBlue();
  }

  public void setColor(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }
}


