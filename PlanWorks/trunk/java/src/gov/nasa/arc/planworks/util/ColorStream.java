package gov.nasa.arc.planworks.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorStream {

  private static final float H_DIFF = 0.15f;
  private static final float SAT = 1.0f;
  private static final float B_DIFF = 0.09f;
  private static final float B_MIN = 0.75f;

  private Map hashMap;
  //private 
  private float h, b;
  private int multiplier;

  public ColorStream() {
    hashMap = new HashMap();
    //h = 0.9166667f;
    h = 0.0f;
    b = B_MIN;
    multiplier = 1;
  }
  public ColorStream(final int r, final int g, final int b) {
    this();
    float [] hsb = Color.RGBtoHSB(r, g, b, null);
    h = hsb[0];
    this.b = hsb[2];
    for(int i = 0; i < 10; i++) {
      hashMap.put(new Integer(i), nextColor());
    }
  }
  public ColorStream(final Color startColor) {
    this(startColor.getRed(), startColor.getGreen(), startColor.getBlue());
  }
  public Color nextColor() {
    Color retval;
    //I know, it's bad style, but some values of blue simply *refuse* to cooperate. ~MJI
    if((h == 0.6f || h == 0.75f) && b == 0.75) {
        retval = Color.getHSBColor(h + 0.09f, SAT, b);
    }
    else {
      retval = Color.getHSBColor(h, SAT, b);
    }
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

  public Color getColor(final int index) {
    Color indexColor = (Color) hashMap.get( new Integer( index));
    // System.err.println( "getColor index " + index + " indexColor " + indexColor);
    if (indexColor == null) {
      List indices = new ArrayList(hashMap.keySet());
      if(!indices.isEmpty()) {
        Collections.sort(indices);
        for(int i = ((Integer)indices.get(indices.size()-1)).intValue(); i < index; i++) {
          hashMap.put(new Integer(i), nextColor());
        }
      }
      indexColor = nextColor();
      hashMap.put( new Integer( index), indexColor);
    }
    System.err.println("for index " + index + " returning color " + indexColor);
    return indexColor;
  }

  public void setColor(final Color color) {
    setColor(color.getRed(), color.getGreen(), color.getBlue());
  }

  public void setColor(final int r, final int g, final int b) {
    float [] hsb = Color.RGBtoHSB(r, g, b, null);
    h = hsb[0];
    this.b = hsb[2];
  }
}


