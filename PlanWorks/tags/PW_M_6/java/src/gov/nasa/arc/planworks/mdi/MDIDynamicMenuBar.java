//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIDynamicMenuBar.java,v 1.8 2003-09-18 23:35:04 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
  private ArrayList windows = new ArrayList();
  private TileCascader tileCascader;
  private JMenu windowMenu;
    /**
     * creates a new MDIDynamicMenuBar and registers itself with the MDIDesktop
     * @param desktop the MDIDesktop to which the MDIDynamicMenuBar is added.
     */
  public MDIDynamicMenuBar(TileCascader tileCascader) {
    super();
    for(int i = 0; i < getMenuCount(); i++) {
      constantMenus.add(getMenu(i));
    }
    this.tileCascader = tileCascader;
    buildWindowMenu();
    this.setVisible(true);
  }
  /**
   * Creates a new MDIDynamicMenuBar with a set of initial menus that may or may not be constant.
   * @param initialMenus An array of JMenus to display initially.
   * @param constant Determines whether or not the initialMenus are considered constant.
   */
  public MDIDynamicMenuBar(JMenu [] initialMenus, boolean constant, TileCascader tileCascader) {
    super();
    for(int i = 0; i < initialMenus.length; i++) {
      if(constant) {
        constantMenus.add(initialMenus[i]);
      }
      add(initialMenus[i]);
    }
    this.tileCascader = tileCascader;
    buildWindowMenu();
    this.setVisible(true);
  }
  /**
   * Creates a new MDIDynamicMenuBar with a set of initial constant menus and a set of initial
   * volatile menus.
   * @param constantMenus An array of JMenus to be treated as constant.
   * @param initialMenus An array of JMenus to be treated as volatile.
   */
  public MDIDynamicMenuBar(JMenu [] constantMenus, JMenu [] initialMenus, TileCascader tileCascader) {
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
    this.tileCascader = tileCascader;
    buildWindowMenu();
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
    add(windowMenu);
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
  public void remove(Component c) {
    if(c == null) {
      return;
    }
    super.remove(c);
    if(constantMenus.contains(c)) {
      constantMenus.remove(constantMenus.indexOf(c));
    }
  }
  public void addWindow(MDIInternalFrame frame) {
    windows.add(frame);
    buildWindowMenu();
  }
  
  private void buildWindowMenu() {
    if(windowMenu != null) {
      windowMenu.removeAll();
      remove(windowMenu);
    }
    else {
      windowMenu = new JMenu("Window");
    }
    JMenuItem tileItem = new JMenuItem("Tile Windows");
    JMenuItem cascadeItem = new JMenuItem("Cascade");
    tileItem.addActionListener(new TileActionListener(tileCascader));
    cascadeItem.addActionListener(new CascadeActionListener(tileCascader));
    windowMenu = new JMenu("Window");
    windowMenu.add(tileItem);
    windowMenu.add(cascadeItem);
    windowMenu.addSeparator();
    windowMenu.validate();
    ListIterator windowIterator = windows.listIterator();
    while(windowIterator.hasNext()) {
      MDIInternalFrame frame = (MDIInternalFrame) windowIterator.next();
      JMenuItem temp = new JMenuItem(frame.getTitle());
      temp.addActionListener(new SelectedActionListener(frame));
      windowMenu.add(temp);
    }
    if(windows.size() == 0) {
      windowMenu.setEnabled(false);
    }
    super.add(windowMenu);
    windowMenu.validate();
    validate();
  }
  public void notifyDeleted(MDIFrame frame) {
    windows.remove(frame);
    buildWindowMenu();
  }
  public void add(JButton button){}
  public JMenu add(JMenu menu) {
    remove(windowMenu);
    super.add(menu);
    if(windowMenu != null) {
      super.add(windowMenu);
    }
    validate();
    return menu;
  }
}

class SelectedActionListener implements ActionListener {
  private MDIInternalFrame frame;
  public SelectedActionListener(MDIInternalFrame frame) {
    this.frame = frame;
  }
  public void actionPerformed(ActionEvent e) {
    try {
      frame.setIcon(false);
      frame.setSelected(true);
    }
    catch(Exception f){}
  }
}

class TileActionListener implements ActionListener {
  private TileCascader tiler;
  public TileActionListener(TileCascader tiler) {
    this.tiler = tiler;
  }
  public void actionPerformed(ActionEvent e) {
    tiler.tileWindows();
  }
}

class CascadeActionListener implements ActionListener {
  private TileCascader cascader;
  public CascadeActionListener(TileCascader cascader) {
    this.cascader = cascader;
  }
  public void actionPerformed(ActionEvent e) {
    cascader.cascadeWindows();
  }
}


