//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIDesktopFrame.java,v 1.4 2003-06-30 21:20:46 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/*
 * <code>MDIDesktopFrame</code> -
 *                  JFrame->MDIDesktopFrame
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * Toplevel frame that contains an MDIDynamicMenuBar, an MDIWindowButtonBar, and MDIInternalFrames.
 */

public class MDIDesktopFrame extends JFrame {
  private MDIDynamicMenuBar menuBar = null;
  private MDIWindowButtonBar windowBar = null;
  private MDIDesktopPane desktopPane = null;
  private int unnamedwindows = 0;
  /**
   * Creates the MDIDesktopFrame object with a name, adds an MDIDynamicMenuBar, MDIWindowButtonBar,
   * and an MDIDesktopPane.
   * @param name The displayed name of the frame.
   */
  public MDIDesktopFrame(String name) {
    super(name);
    Container contentPane = getContentPane();
    menuBar = new MDIDynamicMenuBar();
    windowBar = new MDIWindowButtonBar(30, 80);
    desktopPane = new MDIDesktopPane();
    // contentPane.add(menuBar, BorderLayout.NORTH);
    contentPane.add(desktopPane, BorderLayout.CENTER);
    contentPane.add(windowBar, BorderLayout.SOUTH);
    setJMenuBar(menuBar);
    desktopPane.setVisible(true);
    menuBar.setVisible(true);
    windowBar.setVisible(true);
    this.setVisible(true);
  }
  /**
   * Creates the MDIDesktopFrame object with a name, adds an MDIDynamicMenuBar, MDIWindowButtonBar,
   * and an MDIDesktopPane, then adds the constant menus to the MDIDynamicMenuBar.
   * @param name The displayed name of the frame.
   * @param constantMenus An array of JMenus that remain visible regardless of the selected
   *                      MDIInternalFrame.
   */
  public MDIDesktopFrame(String name, JMenu [] constantMenus) {
    super(name);
    Container contentPane = getContentPane();
    menuBar = new MDIDynamicMenuBar(constantMenus, true);
    windowBar = new MDIWindowButtonBar(30, 80);
    desktopPane = new MDIDesktopPane();
    // contentPane.add(menuBar, BorderLayout.NORTH);
    contentPane.add(desktopPane, BorderLayout.CENTER);
    contentPane.add(windowBar, BorderLayout.SOUTH);
    setJMenuBar(menuBar);
    desktopPane.setVisible(true);
    menuBar.setVisible(true);
    windowBar.setVisible(true);
    this.setVisible(true);
  }
  /**
   * Creates a new MDIInternalFrame with a default name of "Window n", where n is the current
   * number of unnamed MDIInternalFrames.
   */
  public MDIInternalFrame createFrame() {
    MDIInternalFrame newFrame = new MDIInternalFrame(unnamedwindows, menuBar, windowBar);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    unnamedwindows++;
    return newFrame;
  }
  /**
   * Creates a new MDIInternalFrame with the given title.
   * @param title The displayed title of the MDIInternalFrame
   */
  public MDIInternalFrame createFrame(String title) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    return newFrame;
  }
  /**
   * Creates a new, resizable, MDIInternalFrame with the given title.
   * @param title The displayed title of the MDIInternalFrame
   * @param resizable The boolean value of the resizable attribute of the frame.
   */
  public MDIInternalFrame createFrame(String title, boolean resizable) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar, resizable);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    return newFrame;
  }
  /**
   * Creates a new, resizable, closable, MDIInternalFrame with the given title.
   * @param title The displayed title of the MDIInternalFrame
   * @param resizable The boolean value of the resizable attribute of the frame.
   * @param closable The boolean value of the closable attribute of the frame.
   */
  public MDIInternalFrame createFrame(String title, boolean resizable, boolean closable) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar, resizable,
                                                     closable);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    return newFrame;
  }
  /**
   * Creates a new, resizable, closable, maximizable, MDIInternalFrame with the given title.
   * @param title The displayed title of the MDIInternalFrame
   * @param resizable The boolean value of the resizable attribute of the frame.
   * @param closable The boolean value of the closable attribute of the frame.
   * @param maximizable The boolean value of the maximizable attribute of the frame.
   */
  public MDIInternalFrame createFrame(String title, boolean resizable, boolean closable,
                                      boolean maximizable) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar, resizable,
                                                     closable, maximizable);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    return newFrame;
  }
  /**
   * Creates a new, resizable, closable, maximizable, iconifiable (minimizable) MDIInternalFrame
   * with the given title.
   * @param title The displayed title of the MDIInternalFrame
   * @param resizable The boolean value of the resizable attribute of the frame.
   * @param closable The boolean value of the closable attribute of the frame.
   * @param maximizable The boolean value of the maximizable attribute of the frame.
   * @param iconifiable The boolean value of the iconifiable attribute of the frame.
   */
  public MDIInternalFrame createFrame(String title, boolean resizable, boolean closable, 
                                      boolean maximizable, boolean iconifiable) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar, resizable,
                                                     closable, maximizable, iconifiable);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    return newFrame;
  }
  /**
   * Creates a new, resizable, closable, maximizable, iconifiable (minimizable) MDIInternalFrame
   * with the given title.
   * @param title The displayed title of the MDIInternalFrame
   * @param resizable The boolean value of the resizable attribute of the frame.
   * @param closable The boolean value of the closable attribute of the frame.
   * @param maximizable The boolean value of the maximizable attribute of the frame.
   * @param iconifiable The boolean value of the iconifiable attribute of the frame.
   */
  public MDIInternalFrame createFrame(String title, MDIWindowBar viewSet, boolean resizable,
                                      boolean closable, boolean maximizable, boolean iconifiable) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar, viewSet, resizable,
                                                     closable, maximizable, iconifiable);
    desktopPane.add(newFrame);
    newFrame.setVisible(true);
    return newFrame;
  }
}
