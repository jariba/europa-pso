package gov.nasa.arc.planworks.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorStream {

  private static final float H_DIFF = 0.15f;
  private static final float SAT = 1.0f;
  private static final float B_DIFF = 0.09f;
  private static final float B_MIN = 0.75f;

  private Map hashMap;
  private float h, b;
  private int multiplier;

  public ColorStream() {
    hashMap = new HashMap();
    //h = 0.9166667f;
    h = 0.0f;
    b = B_MIN;
    multiplier = 1;
  }
  public ColorStream(int r, int g, int b) {
    this();
    float [] hsb = Color.RGBtoHSB(r, g, b, null);
    h = hsb[0];
    this.b = hsb[2];
  }
  public ColorStream(Color startColor) {
    this(startColor.getRed(), startColor.getGreen(), startColor.getBlue());
  }
  public Color nextColor() {
    Color retval;
    if((h == 0.6f || h == 0.75f) && b == 0.75) {
        retval = Color.getHSBColor(h + 0.09f, SAT, b);
    }
    else {
      retval = Color.getHSBColor(h, SAT, b);
    }
    System.err.println("<" + h + ", " + b + ">");
    h += H_DIFF;
    if(h >= 1.0f) {
      h = 0.014f * multiplier;
      b += B_DIFF;
      multiplier++;
    }
    if(b >= 1.0f) {
      b = B_MIN;
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
    setColor(color.getRed(), color.getGreen(), color.getBlue());
  }

  public void setColor(int r, int g, int b) {
    float [] hsb = Color.RGBtoHSB(r, g, b, null);
    h = hsb[0];
    this.b = hsb[2];
  }
}


