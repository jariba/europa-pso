package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;

public class TestHelper {
  public static Component findComponent(Container c, String name) {
    Component [] comps = c.getComponents();
    for(int i = 0; i < comps.length; i++) {
      if(comps[i].getName().equals(name)) {
        return comps[i];
      }
    }
    return null;
  }
}
