// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ThreadWithProgressMonitor.java,v 1.3 2004-08-25 18:41:00 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- started 24july04
//

package gov.nasa.arc.planworks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ProgressMonitor;

import gov.nasa.arc.planworks.viz.ViewConstants;


/**
 * <code>ThreadWithProgressMonitor</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 
 */
public class ThreadWithProgressMonitor extends Thread {

  private  List listenerList;

  protected ProgressMonitor progressMonitor;
  protected boolean isProgressMonitorCancel;


  /**
   * <code>ThreadWithProgressMonitor</code> - constructor 
   *
   */
  public ThreadWithProgressMonitor() {
    super();
    listenerList = new LinkedList();
  }  // end constructor

  /**
   * <code>addThreadListener</code>
   *
   * @param l - <code>ThreadListener</code> - 
   */
  protected void addThreadListener(ThreadListener l) {
    // System.err.println( "ThreadWithProgressMonitor.addThreadListener " + l);
    listenerList.add(l);
  }

  /**
   * <code>progressMonitorThread</code>
   *
   * @param title - <code>String</code> - 
   * @param minValue - <code>int</code> - 
   * @param maxValue - <code>int</code> - 
   */
  protected void progressMonitorThread( String title, int minValue, int maxValue) {
    Thread thread = new ProgressMonitorThread( title, minValue, maxValue);
    thread .setPriority(Thread.MAX_PRIORITY);
    thread.start();
  }

  class ProgressMonitorThread extends Thread {

    private String title;
    private int minValue;
    private int maxValue;
    private Thread monitoredThread;

    public ProgressMonitorThread( String title, int minValue, int maxValue) {
      this.title = title;
      this.minValue = minValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
      this.maxValue = maxValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
    }  // end constructor

    public void run() {
      isProgressMonitorCancel = false;
      progressMonitor = new ProgressMonitor( PlanWorks.getPlanWorks(), title, "",
                                             minValue, maxValue);
      progressMonitor.setMillisToDecideToPopup( 0);
      progressMonitor.setMillisToPopup( 0);
      // these two must be set to 0 before calling setProgress, which puts up the dialog
      progressMonitor.setProgress( 0);

      while (! isProgressMonitorCancel) {
        try {
          Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL * 2);
        }
        catch (InterruptedException ie) {}
      }
      progressMonitor.close();
      progressMonitor = null;
    } // end run

  } // end class ProgressMonitorThread

  /**
   * <code>progressMonitorWait</code>
   *
   * @return - <code>boolean</code> - 
   */
  protected boolean progressMonitorWait() {
    int numCycles = ViewConstants.WAIT_NUM_CYCLES;
    while ((progressMonitor == null) && numCycles != 0) {
      try {
        Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
      }
      catch (InterruptedException ie) {}
      numCycles--;
      // System.err.println( "progressMonitorWait numCycles " + numCycles);
    }
    if (numCycles == 0) {
      System.err.println( "progressMonitorWait failed after " +
                          (ViewConstants.WAIT_INTERVAL * ViewConstants.WAIT_NUM_CYCLES) +
                          " for " + this.getClass().getName());
    }
    return numCycles != 0;
  } // end progressMonitorWait

  /**
   * <code>handleEvent</code>
   *
   * @param eventName - <code>String</code> - 
   * @param params - <code>Object[]</code> - 
   */
  protected void handleEvent(String eventName, Object [] params) {
    Class [] paramTypes = new Class [params.length];
    for(int i = 0; i < params.length; i++) {
      paramTypes[i] = params[i].getClass();
    }
    for(Iterator it = listenerList.iterator(); it.hasNext();) {
      ThreadListener l = (ThreadListener) it.next();
      try {
        // System.err.println("Accessing method " + l.getClass().getName() + "." + eventName + "("
        //                    + paramTypes + ")");
        l.getClass().getMethod(eventName, paramTypes).invoke(l, params);
        
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * <code>handleEvent</code>
   *
   * @param eventName - <code>String</code> - 
   */
  protected void handleEvent(String eventName) {
    Object [] params = {};
    handleEvent(eventName, params);
  }

} // end class ThreadWithProgressMonitor
