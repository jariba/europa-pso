// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ProgressMonitorThread.java,v 1.2 2004-08-21 00:32:00 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 13aug04
//

package gov.nasa.arc.planworks.viz.util;

import javax.swing.JPanel;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.viz.ViewConstants;


public class ProgressMonitorThread extends Thread {

  private String title;
  private int minValue;
  private int maxValue;
  private Thread monitoredThread;
  private JPanel view;
  private FindEntityPath findEntityPath;

  private boolean isThreadCancel;
  private PWProgressMonitor progressMonitor;
  private boolean isProgressMonitorCancel;

  /**
   * <code>ProgressMonitorThread</code> - constructor 
   *
   * @param title - <code>String</code> - 
   * @param minValue - <code>int</code> - 
   * @param maxValue - <code>int</code> - 
   * @param monitoredThread - <code>Thread</code> - 
   * @param view - <code>JPanel</code> - 
   */
  public ProgressMonitorThread( String title, int minValue, int maxValue,
				Thread monitoredThread, JPanel view) {
    progressMonitor = null;
    isProgressMonitorCancel = false;
    isThreadCancel = false;
    this.title = title;
    this.minValue = minValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
    this.maxValue = maxValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
    this.monitoredThread = monitoredThread;
    this.view = view;
    this.findEntityPath = null;
  }  // end constructor

  /**
   * <code>ProgressMonitorThread</code> - constructor 
   *
   * @param title - <code>String</code> - 
   * @param minValue - <code>int</code> - 
   * @param maxValue - <code>int</code> - 
   * @param monitoredThread - <code>Thread</code> - 
   * @param view - <code>JPanel</code> - 
   * @param findEntityPath - <code>FindEntityPath</code> - 
   */
  public ProgressMonitorThread( String title, int minValue, int maxValue,
				Thread monitoredThread, JPanel view,
                                FindEntityPath findEntityPath) {
    progressMonitor = null;
    isProgressMonitorCancel = false;
    isThreadCancel = false;
    this.title = title;
    this.minValue = minValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
    this.maxValue = maxValue * ViewConstants.MONITOR_MIN_MAX_SCALING;
    this.monitoredThread = monitoredThread;
    this.view = view;
    this.findEntityPath = findEntityPath;
  }  // end constructor

  public PWProgressMonitor getProgressMonitor() {
    return progressMonitor;
  }

  public void setPMThreadCancel() {
    // System.err.println( "ProgressMonitorThread.setPMThreadCancel");
    isThreadCancel = true;
  }

  public void setProgressMonitorCancel() {
    // System.err.println( "ProgressMonitorThread.setProgressMonitorCancel");
    isProgressMonitorCancel = true;
  }

  /**
   * <code>run</code>
   *
   */
  public void run() {
    progressMonitor = new PWProgressMonitor( PlanWorks.getPlanWorks(), title, "",
					     minValue, maxValue, monitoredThread, view,
                                             this, findEntityPath);
    progressMonitor.setMillisToDecideToPopup( 0);
    progressMonitor.setMillisToPopup( 0);
    // these two must be set to 0 before calling setProgress, which puts up the dialog
    progressMonitor.setProgress( 0);

    while ((! isProgressMonitorCancel) && (! isThreadCancel)) {
      try {
	Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL * 2);
      }
      catch (InterruptedException ie) {}
      // System.err.println( view.getName() + " wait for isProgressMonitorCancel = true");
    }
    if (progressMonitor != null) {
      progressMonitor.close();
      progressMonitor = null;
    }
  } // end run

} // end class ProgressMonitorThread
