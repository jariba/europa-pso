package gov.nasa.arc.planworks.util;

import java.awt.Color;
import java.util.ArrayList;

public class ColorStream {
  private static final int COLOR_INC = 51;
  private int r, g, b;
  private List usedColors;
  public ColorStream() {
    usedColors = new ArrayList();
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
    Color retval = new Color(r, g, b);
    usedColors.add(retval);
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
      r = 0;
      g = 0;
    }
    return retval;
  }
  public Color getColor(int index) {
    return (Color) usedColors.get(index);
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
