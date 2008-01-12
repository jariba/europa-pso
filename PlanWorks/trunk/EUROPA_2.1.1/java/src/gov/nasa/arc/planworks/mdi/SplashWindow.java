//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: SplashWindow.java,v 1.3 2004-02-03 19:23:24 miatauro Exp $
//
package gov.nasa.arc.planworks.mdi;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import gov.nasa.arc.planworks.PlanWorks;

public class SplashWindow extends Window {    
  private Image image;    
  private boolean paintCalled = false;
  
  public SplashWindow(final Frame owner, final Image image) {
    super(owner);
    this.image = image;
    MediaTracker mt = new MediaTracker(this);
    mt.addImage(image,0);        
    try {            
      mt.waitForID(0);
    } 
    catch(InterruptedException ie){}

    int imgWidth = image.getWidth(this);
    int imgHeight = image.getHeight(this);
    setSize(imgWidth, imgHeight);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((screenSize.width - imgWidth) / 2, 
		(screenSize.height - imgHeight) / 2);
    addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          synchronized(SplashWindow.this) {
            SplashWindow.this.paintCalled = true;
            SplashWindow.this.notifyAll();
          }
          dispose();
        }
      });
    addWindowListener(new FocusSwitcher(this));
    new CompletionListenerThread(this).start();
  }    
  public void update(Graphics g) {
    g.setColor(getForeground());
    paint(g);
  }
  public void paint(Graphics g) {
    g.drawImage(image, 0, 0, this);
    if (! paintCalled) {            
      paintCalled = true;
      synchronized (this) { notifyAll(); }
    }
  }
  public static Frame splash(final Image image) {        
    Frame f = new Frame();        
    SplashWindow w = new SplashWindow(f, image);
    w.toFront();        
    w.show();        
    if (! EventQueue.isDispatchThread()) {            
      synchronized (w) {
	while (! w.paintCalled) {
	  try { w.wait(); } catch (InterruptedException e) {}
	}
      }
    }
    return f;
  }
}

class CompletionListenerThread extends Thread {
  private Window window;
  public CompletionListenerThread(Window window) {
    this.window = window;
  }
  public void run() {
    while(true) {
      if(PlanWorks.isWindowBuilt()) {
	window.dispose();
	return;
      }
      try {Thread.yield();} catch(Exception e){}
    }
  }
}

class FocusSwitcher extends WindowAdapter {
  private Window window;
  public FocusSwitcher(Window window) {
    this.window = window;
  }
  public void windowLostFocus(WindowEvent e) {
    window.toFront();
    window.show();
  }
  public void windowDeactivated(WindowEvent e) {
    window.toFront();
    window.show();
  }
  
}
