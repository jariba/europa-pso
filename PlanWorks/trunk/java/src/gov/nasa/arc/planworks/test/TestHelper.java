package gov.nasa.arc.planworks.test;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JMenu;

public class TestHelper {
  private static final EventQueue events = Toolkit.getDefaultToolkit().getSystemEventQueue();
  private static int testsPassed = 0;
  private static boolean allTestsPassed = true;

  public static void critAssertTrue(final String s, final boolean b) {
    assertTrue(s, b);
    if(!b) {
      System.exit(-1);
    }
  }
  public static void assertTrue(final String s, final boolean b) {
    if(!b) {
      System.err.println(s);
      allTestsPassed = b;
    }
  }

  public static Component findComponent(JMenu m, String name) {
    for(int i = 0; i < m.getItemCount(); i++) {
      if(m.getItem(i).getName().equals(name)) {
        return m.getItem(i);
      }
    }
    return null;
  }

  public static Component findComponent(Container c, String name) {
    System.err.println("in findComponent.");
    Component [] comps = c.getComponents();
    System.err.println(comps.length);
    for(int i = 0; i < comps.length; i++) {
      if(comps[i] != null && comps[i].getName() != null && comps[i].getName().equals(name)) {
        System.err.println("Testing component " + comps[i].getName());
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

  public static void enter(Component c) {
    int x = (c.getX() + c.getWidth()) / 2;
    int y = (c.getY() + c.getHeight()) / 2;
    c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(),
                                   0, x, y, 0, false, MouseEvent.NOBUTTON));
  }

  public static void press(Component c) {
    press(c, MouseEvent.BUTTON1_MASK, false, MouseEvent.BUTTON1);
  }

  public static void press(Component c, int modifiers, boolean popupTrigger, int button) {
    int x = (c.getX() + c.getWidth()) / 2;
    int y = (c.getY() + c.getHeight()) / 2;
    c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                                   modifiers, x, y, 0, popupTrigger, button));
  }

  public static void release(Component c) {
    release(c, MouseEvent.BUTTON1_MASK, false, MouseEvent.BUTTON1);
  }

  public static void release(Component c, int modifiers, boolean popupTrigger, int button) {
    int x = (c.getX() + c.getWidth()) / 2;
    int y = (c.getY() + c.getHeight()) / 2;
    c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
                                   modifiers, x, y, 0, popupTrigger, button));
  }

  public static void click(Component c) {
    click(c, MouseEvent.BUTTON1_MASK, false, MouseEvent.BUTTON1);
  }

  public static void click(Component c, int modifiers, boolean popupTrigger, int button) {
    int x = (c.getX() + c.getWidth()) / 2;
    int y = (c.getY() + c.getHeight()) / 2;
    c.dispatchEvent(new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                                   modifiers, x, y, 0, popupTrigger, button));
  }


  public static void enterClickAndLeave(Component c) {
    enterClickAndLeave(c, MouseEvent.BUTTON1_MASK, 1);
  }

  public static void enterClickAndLeave(Component c, int modifiers, int clickCount) {
    enterClickAndLeave(c, modifiers, clickCount, false, MouseEvent.BUTTON1);
  }

  public static void enterClickAndLeave(Component c, int modifiers, int clickCount, 
                                        boolean popupTrigger, int button) {
    int x = (c.getX() + c.getWidth()) / 2;
    int y = (c.getY() + c.getHeight()) / 2;
    MouseEvent enter = new MouseEvent(c, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 
                                      modifiers, x, y, 0, false, 
                                      MouseEvent.NOBUTTON);
    MouseEvent press = new MouseEvent(c, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                                      modifiers, x, y, clickCount, popupTrigger, button);
    MouseEvent release = new MouseEvent(c, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(),
                                        modifiers, x, y, clickCount, popupTrigger, button);
    MouseEvent click = new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 
                                      modifiers, x, y, clickCount, popupTrigger,
                                      button);
    MouseEvent leave = new MouseEvent(c, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 
                                      modifiers, x, y, 0, false,
                                      MouseEvent.NOBUTTON);
    c.dispatchEvent(enter);
    c.dispatchEvent(press);
    c.dispatchEvent(release);
    c.dispatchEvent(click);
    c.dispatchEvent(leave);
  }
}
