package gov.nasa.arc.planworks.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ColorStream {

  private static final float H_DIFF = 0.15f;
  private static final float SAT = 1.0f;
  private static final float B_DIFF = 0.09f;
  private static final float B_MIN = 0.78f;

  private Map hashMap;
  private LinkedList colorList;
  private float h, b;
  private int multiplier;

  public ColorStream() {
    hashMap = new HashMap();
    //h = 0.9166667f;
    h = 0.0f;
    b = B_MIN;
    multiplier = 1;
    initColorList();
  }
  public ColorStream(final int r, final int g, final int b) {
    hashMap = new HashMap();
    multiplier = 1;
    float [] hsb = Color.RGBtoHSB(r, g, b, null);
    h = hsb[0];
    this.b = hsb[2];
    initColorList();
  }
  public ColorStream(final Color startColor) {
    this(startColor.getRed(), startColor.getGreen(), startColor.getBlue());
  }
  
  private void initColorList() {
    colorList = new LinkedList();
    for(int i = 0; i < 10; i++) {
      colorList.addLast(nextColor());
    }
  }

  private Color nextColor() {
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

  public Color getColor(Object o) {
    Color retval = null;
    if(!hashMap.containsKey(o)) {
      retval = (Color) colorList.removeFirst();
      hashMap.put(o, retval);
      colorList.addLast(nextColor());
    }
    else {
      retval = (Color) hashMap.get(o);
    }
    return retval;
  }
}


