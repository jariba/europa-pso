//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIDynamicMenuBar.java,v 1.2 2003-06-16 22:32:14 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

/**
 * <code>MDIDynamicMenuBar</code> -
 *                      JMenuBar->MDIDynamicMenuBar
 *                      MDIMenu->MDIDynamicMenuBar
 * A menubar with dynamic menus.  The menus are dependent on which 
 * MDIInternalFrame has focus.  The menubar also has constant menus, which 
 * are not dependent on the selected frame.
 */

public class MDIDynamicMenuBar extends JMenuBar implements MDIMenu {
  private ArrayList constantMenus = new ArrayList(3);
    /**
     * creates a new MDIDynamicMenuBar and registers itself with the MDIDesktop
     * @param desktop the MDIDesktop to which the MDIDynamicMenuBar is added.
     */
  public MDIDynamicMenuBar() {
    super();
    for(int i = 0; i < getMenuCount(); i++) {
      constantMenus.add(getMenu(i));
    }
    this.setVisible(true);
  }
  /**
   * Creates a new MDIDynamicMenuBar with a set of initial menus that may or may not be constant.
   * @param initialMenus An array of JMenus to display initially.
   * @param constant Determines whether or not the initialMenus are considered constant.
   */
  public MDIDynamicMenuBar(JMenu [] initialMenus, boolean constant) {
    super();
    for(int i = 0; i < initialMenus.length; i++) {
      if(constant) {
        constantMenus.add(initialMenus[i]);
      }
      add(initialMenus[i]);
    }
    this.setVisible(true);
  }
  /**
   * Creates a new MDIDynamicMenuBar with a set of initial constant menus and a set of initial
   * volatile menus.
   * @param constantMenus An array of JMenus to be treated as constant.
   * @param initialMenus An array of JMenus to be treated as volatile.
   */
  public MDIDynamicMenuBar(JMenu [] constantMenus, JMenu [] initialMenus) {
    super();
    this.constantMenus = new ArrayList(constantMenus.length);
    for(int i = 0; i < constantMenus.length; i++) {
      this.constantMenus.add(constantMenus[i]);
      this.add(constantMenus[i]);
    }
    if(initialMenus != null) {
      for(int i = 0; i < initialMenus.length; i++) {
        this.add(initialMenus[i]);
      }
    }
    this.setVisible(true);
  }
  /**
   * Removes all of the currently displayed JMenus, adds the constant menus back, then adds the
   * frame's associated menues.  Called when an MDIInternalFrame is selected.
   * @param frame the MDIFrame that was just selected.
   */
  public void notifyActivated(MDIFrame frame) {
    removeAll();
    if(constantMenus != null) {
      for(int i = 0; i < constantMenus.size(); i++) {
        add((JMenu)constantMenus.get(i));
      }
    }
    JMenu [] temp = frame.getAssociatedMenus();
    if(temp != null) {
      for(int i = 0; i < temp.length; i++) {
        add(temp[i]);
      }
    }
    //repaint(getVisibleRect());
    validate();
  }
  /**
   * Adds a JMenu to the list of constant menus.
   * @param constantMenu The menu to be added.
   */
  public void addConstantMenu(JMenu constantMenu) {
    if(constantMenus == null) {
      constantMenus = new ArrayList(3);
    }
    constantMenus.add(constantMenu);
    this.add(constantMenu);
  }
  /**
   * Adds an array of JMenus to the list of constant menus.
   * @param constantMenus The array of menus to be added.
   */
  public void addConstantMenus(JMenu [] constantMenus) {
    if(this.constantMenus == null) {
      this.constantMenus = new ArrayList(constantMenus.length);
    }
    for(int i = 0; i < constantMenus.length; i++) {
      addConstantMenu(constantMenus[i]);
    }
  }
  /**
   * Adds a JMenu to the MDIDynamicMenuBar as a volatile menu.
   * @param menu The menu to be added.
   */
  public void addMenu(JMenu menu) {
    this.add(menu);
  }
  /**
   * Adds an array of JMenus to the menu bar as volatile menus.
   * @param menus The menus to be added.
   */
  public void addMenus(JMenu [] menus) {
    for(int i = 0; i < menus.length; i++) {
      this.add(menus[i]);
    }
  }
}
