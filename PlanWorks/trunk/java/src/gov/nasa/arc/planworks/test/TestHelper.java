package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class TestHelper {
  public static Component findComponent(Container c, String name) {
    Component [] comps = c.getComponents();
    for(int i = 0; i < comps.length; i++) {
      if(comps[i].getName().equals(name)) {
        return comps[i];
      }
    }
    for(int i = 0; i < comps.length; i++) {
      if(comps[i] instanceof Container) {
        Component n = findComponent((Container)comps[i], name);
        if(n != null) {
          return n;
        }
      }
    }
    return null;
  }

  public static void enterClickAndLeave(Component c) {
    enterClickAndLeave(c, 0, 1);
  }

  public static void enterClickAndLeave(Component c, int modifiers, int clickCount) {
    enterClickAndLeave(c, modifiers, clickCount, false, MouseEvent.BUTTON1);
  }

  public static void enterClickAndLeave(Component c, int modifiers, int clickCount, 
                                        boolean popupTrigger, int button) {
    MouseEvent enter = new MouseEvent(c, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 
                                      modifiers, c.getX(), c.getY(), MouseEvent.NOBUTTON, false);
    MouseEvent click = new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 
                                      modifiers, c.getX(), c.getY(), clickCount, popupTrigger,
                                      button);
    MouseEvent leave = new MouseEvent(c, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 
                                      modifiers, c.getX(), c.getY(), MouseEvent.NOBUTTON, false);

    MouseListener [] mml =
      (MouseListener []) (c.getListeners(MouseListener.class));
    
    for(int i = 0; i < mml.length; i++) {
      mml[i].mouseEntered(enter);
    }
    for(int i = 0; i < mml.length; i++) {
      mml[i].mouseClicked(click);
    }
    for(int i = 0; i < mml.length; i++) {
      mml[i].mouseExited(leave);
    }
  }
}
