//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIWindowButtonBar.java,v 1.2 2003-06-16 22:32:15 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Dimension;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * <code>MDIWindowButtonBar</code> -
 *                       JToolBar->MDIWindowButtonBar
 *                       MDIWindowBar->MDIWindowButtonBar
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The MDIWindowButtonBar provides functionality much like the Windows taskbar.  Buttons 
 * representing windows can be clicked to select or de-iconify a window.
 */

public class MDIWindowButtonBar extends JToolBar implements MDIWindowBar {
  private ButtonGroup buttonGroup = null;
  private int minWidth, maxWidth;

  /**
   * Initializes the MDIWindowButtonBar with a minimum and maximum width (in pixels) for the 
   * buttons.
   * @param minWidth The minimum width (in pixels) of the buttons.
   * @param maxWidth The maximum width (in pixels) of the buttons.
   */
  public MDIWindowButtonBar(int minWidth, int maxWidth) {
    buttonGroup = new ButtonGroup();
    setFloatable(false);
    this.minWidth = minWidth;
    this.maxWidth = maxWidth;
    addComponentListener(new WindowButtonBarListener(this));
  }
  /**
   * Overrides the JToolBar add() method to add the button to the buttonGroup and resize all
   * extant buttons.
   * @param button The button to be added.
   */
  public void add(JButton button) {
    buttonGroup.add(button);
    super.add(button);
    button.setSelected(true);
    resizeButtons();
  }
  /**
   * Overrides the JToolBar add() method to add the button to the buttonGroup and resize all
   * extant buttons.
   * @param button The button to be added.
   */
  public void add(AbstractButton button) {
    buttonGroup.add(button);
    super.add(button);
    button.setSelected(true);
    resizeButtons();
  }
  /**
   * Overrides the JToolBar remove() method to remove the button from the buttonGroup and resize
   * all extant buttons.
   * @param button The button to be added.
   */
  public void remove(AbstractButton button) {
    super.remove(button);
    buttonGroup.remove(button);
    resizeButtons();
    repaint();
  }
  /**
   * Gets all available buttons
   * @return Enumeration An enumeration of the buttons in the bar.
   */
  public Enumeration getElements() {
    return buttonGroup.getElements();
  }
  /**
   * Gets the number of buttons in the button bar.
   * @return int The number of buttons in the bar.
   */
  public int getButtonCount() {
    return buttonGroup.getButtonCount();
  }
  /**
   * Resizes the buttons to fit available space, widening to the maximum or thinning to the 
   * minimum.
   */
    private void resizeButtons() {
      final float exactButtonWidth = getCurrentButtonWidth();
      SwingUtilities.invokeLater(new Runnable()
        {
          public void run() {
            //JToggleButton b = null;
            JComponent b = null;
            Enumeration e = getElements();
            float currentButtonXLocation = 0.0f;
            while(e.hasMoreElements()) {
              //b = (JToggleButton) e.nextElement();
              b = (JComponent) e.nextElement();
              int buttonWidth = Math.round(currentButtonXLocation + exactButtonWidth) - 
                Math.round(currentButtonXLocation);
              assignWidth(b, buttonWidth);
              currentButtonXLocation += exactButtonWidth;
            }
            revalidate();
          }
        });
    }
  /**
   * Gets the current actual width of the buttons.
   * @return float The width of the buttons.
   */
    private float getCurrentButtonWidth() {
      int width = getWidth() - getInsets().left - getInsets().right;
      float buttonWidth = ((width <= 0) ? maxWidth : width);
      int numButtons = getButtonCount();
      if(numButtons > 0) {
        buttonWidth /= numButtons;
      }
      if(buttonWidth < minWidth) {
        buttonWidth = minWidth;
      }
      else if(buttonWidth > maxWidth) {
        buttonWidth = maxWidth;
      }
      return buttonWidth;
    }
  /**
   * Assign a new width to the button.
   * @param b The button whose width is being set.
   * @param buttonWidth The new width for the button.
   */
  private void assignWidth(JComponent b, int buttonWidth) {
    b.setMinimumSize(new Dimension(buttonWidth - 2, b.getPreferredSize().height));
    b.setPreferredSize(new Dimension(buttonWidth, b.getPreferredSize().height));
    Dimension newSize = b.getPreferredSize();
    b.setMaximumSize(newSize);
    b.setSize(newSize);
  }
  /**
   * Delete the button associated with a closed frame.
   * @param frame The frame that has closed.
   */
  public void notifyDeleted(MDIFrame frame) {
    JButton button = frame.getButton();
    remove(button);
  }
  /**
   * <code>WindowButtonBarListener</code> -
   *                            ComponentListener->WindowButtonBarListener
   * Manages the resizing of buttons when the MDIWindowButtonBar is resized.
   */
  class WindowButtonBarListener implements ComponentListener {
    private MDIWindowButtonBar bar = null;
    public WindowButtonBarListener(MDIWindowButtonBar bar) {
      this.bar = bar;
    }
    public void componentResized(ComponentEvent e) {
      bar.resizeButtons();
    }
    public void componentShown(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentHidden(ComponentEvent e){}
  }
}
