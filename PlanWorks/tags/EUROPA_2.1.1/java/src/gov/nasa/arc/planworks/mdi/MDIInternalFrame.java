//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIInternalFrame.java,v 1.12 2004-03-30 22:01:01 taylor Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/*
 * <code>MDIInternalFrame</code> -
 *                     JInternalFrame->MDIInternalFrame
 *                     MDIFrame->MDIInternalFrame
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The MDIInternalFrame is the primary MDI class.  Everything to be in the desktop should be
 * added to an MDIInternalFrame creates through the MDIDesktopFrame's createFrame methods.
 * The MDIInternalFrame provides a mechanism for associating menus with it, so when a frame is
 * selected, the menus in the menu bar change.  All MDIInternalFrames have buttons associated with
 * them in the MDIWindowButtonBar, which functions more or less like Windows's task bar.
 */

public class MDIInternalFrame extends JInternalFrame implements MDIFrame {
  private ArrayList menus = null;
  private ArrayList children = null;
  private JButton button = null;
  /**
   * Creates the MDIInternalFrame with a default name of "Window n" and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param n The number of the unnamed window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   */  
  public MDIInternalFrame(final int n, final MDIMenu menuBar, final MDIWindowBar windowBar) {
    super();
    button = new JButton("Window " + n);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText("Window " + n);
    // windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar, menuBar));
  }
  /**
   * Creates the MDIInternalFrame with the given title and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param title The displayed title of the window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   */  
  public MDIInternalFrame(final String title, final MDIMenu menuBar, final MDIWindowBar windowBar) {
    super(title);
    button = new JButton(title);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText(title);
    // windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar, menuBar));
  }
  /**
   * Creates a resizable MDIInternalFrame with the given title and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param title The displayed title of the window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   * @param resizable The boolean value of the frame's resizable attribute.
   */  
  public MDIInternalFrame(final String title, final MDIMenu menuBar, final MDIWindowBar windowBar,
                          final boolean resizable) {
    super(title, resizable);
    button = new JButton(title);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText(title);
    // windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar, menuBar));
 }
  /**
   * Creates a resizable, closable MDIInternalFrame with the given title and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param title The displayed title of the window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   * @param resizable The boolean value of the frame's resizable attribute.
   * @param closable The boolean value of the frame's closable attribute.
   */  
  public MDIInternalFrame(final String title, final MDIMenu menuBar, final MDIWindowBar windowBar, 
                          final boolean resizable, final boolean closable) {
    super(title, resizable, closable);
    button = new JButton(title);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e){
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText(title);
    // windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
                                                          menuBar));
  }
  /**
   * Creates a resizable, closable MDIInternalFrame with the given title and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param title The displayed title of the window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   * @param resizable The boolean value of the frame's resizable attribute.
   * @param closable The boolean value of the frame's closable attribute.
   * @param maximizable The boolean value of the frame's maximizable attribute.
   */  
  public MDIInternalFrame(final String title, final MDIMenu menuBar, final MDIWindowBar windowBar, 
                          final boolean resizable, final boolean closable, 
                          final boolean maximizable) {
    super(title, resizable, closable, maximizable);
    button = new JButton(title);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e){ 
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText(title);
    // windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
                                                          menuBar));
  }
  /**
   * Creates a resizable, closable MDIInternalFrame with the given title and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param title The displayed title of the window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   * @param resizable The boolean value of the frame's resizable attribute.
   * @param closable The boolean value of the frame's closable attribute.
   * @param maximizable The boolean value of the frame's maximizable attribute.
   * @param iconifiable The boolean value of the frame's iconifiable attribute.
   */  
  public MDIInternalFrame(final String title, final MDIMenu menuBar, final MDIWindowBar windowBar, 
                          final boolean resizable, final boolean closable, 
                          final boolean maximizable, final boolean iconifiable) {
    super(title, resizable, closable, maximizable, iconifiable);
    button = new JButton(title);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText(title);
    // windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar, menuBar));
  }

  /**
   * Creates a resizable, closable MDIInternalFrame with the given title and tells the MDIMenu and
   * MDIWindowBar that it exists.
   * @param title The displayed title of the window
   * @param menuBar The menu bar onto which any associated menus will be drawn.
   * @param windowBar The bar that will contain a button associated with this frame such that
   *                  clicking on it will select the frame.
   * @param resizable The boolean value of the frame's resizable attribute.
   * @param closable The boolean value of the frame's closable attribute.
   * @param maximizable The boolean value of the frame's maximizable attribute.
   * @param iconifiable The boolean value of the frame's iconifiable attribute.
   */  
  public MDIInternalFrame(final String title, final MDIMenu menuBar, final MDIWindowBar windowBar, 
                          final MDIWindowBar viewSet, final boolean resizable, 
                          final boolean closable, final boolean maximizable, 
                          final boolean iconifiable) {
    super(title, resizable, closable, maximizable, iconifiable);
    button = new JButton(title);
    final MDIInternalFrame temp = this;
    button.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          try {
            if(temp.isIcon()) {
              temp.setIcon(false);
            }
            temp.setSelected(true);
          }
          catch(PropertyVetoException pve){}
        }
      });
    button.setToolTipText(title);
//     windowBar.add(button);
    menuBar.addWindow(this);
    addInternalFrameListener(new MDIInternalFrameListener(this, windowBar, menuBar, viewSet));
  }
  /**
   * Associates a menu with this frame.
   * @param menu The menu to associate.
   */
  public void associateMenu(final JMenu menu) {
    if(this.menus == null) {
      this.menus = new ArrayList(3);
    }
    menus.add(menu);
    //repaint();
    validate();
  }
  /**
   * Associates multiple menus with this frame.
   * @param menus The menus to associate
   */
  public void associateMenus(final JMenu [] menus) {
    for(int i = 0; i < menus.length; i++) {
      associateMenu(menus[i]);
    }
    //repaint();
    validate();
  }
  /**
   * Gets the menus associated with this frame.
   * @return JMenu [] The menus associated with this frame.
   */
  public JMenu [] getAssociatedMenus() {
    if(menus == null || menus.isEmpty()) {
      return null;
    }
    Object [] temp = menus.toArray();
    JMenu [] retval = new JMenu [temp.length];
    for(int i = 0; i < temp.length; i++) {
      retval[i] = (JMenu) temp[i];
    }
    return retval;
  }
  /**
   * Gets the MDIWindowButtonBar button associated with this frame.
   * @return JButton the associated button.
   */
  public JButton getButton() {
    return button;
  }
  /**
   * Add a child to this frame.  If a parent frame is closed, all of the children will closed as 
   * well.
   * @param child The MDIInternalFrame to add as a child of this frame.
   */
  public void addChild(final MDIInternalFrame child) throws IllegalArgumentException {
    if(this.children == null) {
      this.children = new ArrayList(3);
    }
    if(this.children.contains(child)) {
      throw new IllegalArgumentException("Attempted to add a child twice");
    }
    this.children.add(child);
  }
  /**
   * Get all children of this frame.
   * @return MDIInternalFrame [] the array of children.
   */
  public MDIInternalFrame [] getChildren() {
    if(this.children == null || this.children.isEmpty()) {
      return null;
    }
    return (MDIInternalFrame []) this.children.toArray();
  }
  /**
   *<code>MDIInternalFrameListener</code> -
   *                            InternalFrameListener->MDIInternalFrameListener
   * The MDIInternalFrameListener handles the closing of children and deletion of the
   * MDIWindowButtonBar button when the frame is closed, as well as the changing of menus when
   * the frame is selected.
   */
  class MDIInternalFrameListener implements InternalFrameListener {
    private MDIInternalFrame frame = null;
    private MDIWindowBar windowBar = null;
    private MDIWindowBar viewSet = null;
    private MDIMenu menu = null;
    
    /**
     * Creates the MDIInternalFrameListener.
     * @param frame The MDIInternalFrame on which this listener listens.
     * @param windowBar The MDIWindowButtonBar on the MDIDesktopFrame.
     * @param menu The MDIDynamicWindowBar on the MDIDesktopFrame
     */
    public MDIInternalFrameListener(final MDIInternalFrame frame, final MDIWindowBar windowBar, 
                                    final MDIMenu menu) {
      this.frame = frame;
      this.windowBar = windowBar;
      this.menu = menu;
    }
    /**
     * Creates the MDIInternalFrameListener.
     * @param frame The MDIInternalFrame on which this listener listens.
     * @param windowBar The MDIWindowButtonBar on the MDIDesktopFrame.
     * @param menu The MDIDynamicWindowBar on the MDIDesktopFrame
     */
    public MDIInternalFrameListener(final MDIInternalFrame frame, final MDIWindowBar windowBar, 
                                    final MDIMenu menu, final MDIWindowBar viewSet) {
      this.frame = frame;
      this.windowBar = windowBar;
      this.menu = menu;
      this.viewSet = viewSet;
    }

    /**
     * Handles the closing of the frames children.
     */
    public void internalFrameClosing(InternalFrameEvent e) {
      MDIInternalFrame [] children = frame.getChildren();
      if(children == null) {
        return;
      }
      for(int i = 0; i < children.length; i++) {
        try{
          children[i].setClosed(true);
        }
        catch(java.beans.PropertyVetoException pve) {
          //what on earth should be done here?
        }
        //desktop.remove(children[i]); this may not be necessary
      }
    }
    /**
     * Handles the changing of the volatile menus in the MDIDynamicMenuBar.
     */
    public void internalFrameActivated(InternalFrameEvent e) {
      // System.out.println("Activated frame: " + frame.getTitle());
      menu.notifyActivated(frame);
      frame.validate();
    }
    /**
     * Handles the deletion of the button in the MDIWindowButtonBar.
     */
    public void internalFrameClosed(InternalFrameEvent e){
      // windowBar.notifyDeleted(frame);
      menu.notifyDeleted(frame);
      if(viewSet != null) {
        viewSet.notifyDeleted(frame);
      }
      else {
        System.err.println("View set is null??");
      }
    }
    public void internalFrameDeactivated(InternalFrameEvent e) {}
    public void internalFrameDeiconified(InternalFrameEvent e) {}
    public void internalFrameIconified(InternalFrameEvent e) {}
    public void internalFrameOpened(InternalFrameEvent e) {}
  }
}


