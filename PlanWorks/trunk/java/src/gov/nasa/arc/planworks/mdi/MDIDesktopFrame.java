//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MDIDesktopFrame.java,v 1.15 2005-11-10 01:22:09 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.viz.ViewConstants;


/*
 * <code>MDIDesktopFrame</code> -
 *                  JFrame->MDIDesktopFrame
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * Toplevel frame that contains an MDIDynamicMenuBar, an MDIWindowButtonBar, and MDIInternalFrames.
 */

public class MDIDesktopFrame extends JFrame implements TileCascader {
  private MDIDynamicMenuBar menuBar = null;
  private MDIWindowButtonBar windowBar = null;
  private MDIDesktopPane desktopPane = null;
  private int unnamedwindows = 0;
  /**
   * Creates the MDIDesktopFrame object with a name, adds an MDIDynamicMenuBar, MDIWindowButtonBar,
   * and an MDIDesktopPane.
   * @param name The displayed name of the frame.
   */
  public MDIDesktopFrame(final String name) {
    super(name);
    Container contentPane = getContentPane();
    menuBar = new MDIDynamicMenuBar(this);
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
  public MDIDesktopFrame(final String name, final JMenu [] constantMenus) {
    super(name);
    Container contentPane = getContentPane();
    menuBar = new MDIDynamicMenuBar(constantMenus, true, this);
    desktopPane = new MDIDesktopPane();
    contentPane.add(desktopPane, BorderLayout.CENTER);
    setJMenuBar(menuBar);
    desktopPane.setVisible(true);
    menuBar.setVisible(true);
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
  public MDIInternalFrame createFrame(final String title) {
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
  public MDIInternalFrame createFrame(final String title, final boolean resizable) {
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
  public MDIInternalFrame createFrame(final String title, final boolean resizable, 
                                      final boolean closable) {
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
  public MDIInternalFrame createFrame(final String title, final boolean resizable, 
                                      final boolean closable, final boolean maximizable) {
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
  public MDIInternalFrame createFrame(final String title, final boolean resizable, 
                                      final boolean closable, final boolean maximizable, 
                                      final boolean iconifiable) {
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
  public MDIInternalFrame createFrame(final String title, final MDIWindowBar viewSet, 
                                      final boolean resizable, final boolean closable, 
                                      final boolean maximizable, final boolean iconifiable) {
    MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar, windowBar, viewSet, resizable,
                                                     closable, maximizable, iconifiable);
    desktopPane.add(newFrame);
    try {
      newFrame.setVisible(true);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return newFrame;
  }

  //public int getHeight() {
  //  return super.getHeight() - windowBar.getHeight();
  //}

  public void tileWindows() {
    JInternalFrame [] frames = desktopPane.getAllFrames();
    int ymin = 0;
    int numNotIcon = 0;
    int numContentSpecWindows = 0;
    for(int i = 0; i < frames.length; i++) {
	if(frames[i].getTitle().indexOf(ViewConstants.CONTENT_SPEC_TITLE) != -1) {
        ymin = Math.max( ymin, frames[i].getHeight());
        numContentSpecWindows++;
      }
      else if(!frames[i].isIcon()) {
        numNotIcon++;
      }
    }
    if (numContentSpecWindows > 0) {
      int contentSpecWidth = desktopPane.getWidth() / numContentSpecWindows;
      int contentSpecX = 0;
      for(int i = 0; i < frames.length; i++) {
	  if(frames[i].getTitle().indexOf(ViewConstants.CONTENT_SPEC_TITLE) != -1) {
	      frames[i].setSize(Math.min(contentSpecWidth, frames[i].getWidth()),
				frames[i].getHeight());
	      frames[i].setLocation(contentSpecX, 0);
	      contentSpecX += Math.min(contentSpecWidth, frames[i].getWidth());
        }
      }
    }
    int curCol = 0;
    int curRow = 0;
    int i = 0;
    if(numNotIcon > 0) {
      int numCols = (int) Math.sqrt(numNotIcon);
      int frameWidth = desktopPane.getWidth() / numCols;
      for(curCol = 0; curCol < numCols; curCol++) {
        int numRows = numNotIcon / numCols;
        int remainder = numNotIcon % numCols;
        if((numCols - curCol) <= remainder) {
          numRows++;
        }
        int height = (desktopPane.getHeight() - ymin) / numRows;
        for(curRow = 0; curRow < numRows; curRow++, i++) {
          while(frames[i].isIcon() ||
                (frames[i].getTitle().indexOf(ViewConstants.CONTENT_SPEC_TITLE) != -1)) {
          
            i++;
          }
          // System.err.println("Setting bounds.  (" + (curCol * frameWidth) + ", " +
          //                    ((curRow * height) + ymin) + ") <" + frameWidth + ", " + height + ">");
          frames[i].setBounds(curCol * frameWidth, (curRow * height) + ymin, frameWidth, height);
        }
      }
    }
  }

  public void cascadeWindows() {
    JInternalFrame [] frames = desktopPane.getAllFrames();
    int xmin = 0;
    int ymin = 0;
    int contentSpecHeight = 0;
    int numContentSpecWindows = 0;
    int resetCnt = 0;
    int delta = 50;
    for(int i = 0; i < frames.length; i++) {
	if((frames[i].getTitle().indexOf(ViewConstants.CONTENT_SPEC_TITLE) != -1)) {
	    numContentSpecWindows++;
	    ymin = Math.max( ymin, frames[i].getHeight());
      }
    }
    contentSpecHeight = ymin;
    if (numContentSpecWindows > 0) {
      int contentSpecWidth = desktopPane.getWidth() / numContentSpecWindows;
      int contentSpecX = 0;
      for(int i = 0; i < frames.length; i++) {
	  if((frames[i].getTitle().indexOf(ViewConstants.CONTENT_SPEC_TITLE) != -1)) {
           
          frames[i].setLocation(contentSpecX, 0);
          contentSpecX += Math.min(contentSpecWidth, frames[i].getWidth());
        }
      }
    }
    for(int i = 0; i < frames.length; i++) {
	if((frames[i].getTitle().indexOf(ViewConstants.CONTENT_SPEC_TITLE) != -1))
        continue;
      frames[i].setLocation(xmin, ymin);
      try{frames[i].setSelected(true);}catch(Exception e){}
      xmin += delta;
      ymin += delta;
      if(frames[i].getY() + frames[i].getHeight() > desktopPane.getHeight()) {
        resetCnt++;
        xmin = resetCnt * delta;
        ymin = contentSpecHeight;
      }
    }
  }
}
