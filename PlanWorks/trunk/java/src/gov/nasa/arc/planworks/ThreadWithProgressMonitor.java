// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ThreadWithProgressMonitor.java,v 1.1 2004-07-27 21:58:04 taylor Exp $
//
//
// PlanWorks -- 
//
// Will Taylor -- started 24july04
//

package gov.nasa.arc.planworks;

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

  protected ProgressMonitor progressMonitor;
  protected boolean isProgressMonitorCancel;


  /**
   * <code>ThreadWithProgressMonitor</code> - constructor 
   *
   */
  public ThreadWithProgressMonitor() {
    super();
  }  // end constructor

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
    } // end run

  } // end class ProgressMonitorThread

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

} // end class ThreadWithProgressMonitor
