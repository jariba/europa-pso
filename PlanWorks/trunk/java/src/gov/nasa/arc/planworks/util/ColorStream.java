package gov.nasa.arc.planworks.util;

import java.awt.Color;

//temporary
import java.util.Random;

public class ColorStream {
  Random random;
  public ColorStream() {
    random = new Random();
  }
  public ColorStream(int r, int g, int b) {
    this();
  }
  public ColorStream(Color startColor) {
    this();
  }
  public Color nextColor() {
    return new Color(random.nextInt() % 256, random.nextInt() % 256, random.nextInt() % 256);
  }
  public Color getColor(int index) {
    return new Color(random.nextInt() % 256, random.nextInt() % 256, random.nextInt() % 256);
  }
  public void setColor(Color color) {
  }
  public void setColor(int r, int g, int b) {
  }
}
