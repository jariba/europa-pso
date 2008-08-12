// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ThreadListener.java,v 1.1 2004-08-25 18:40:59 taylor Exp $
//
// PlanWorks -- 
//

package gov.nasa.arc.planworks;

public abstract class ThreadListener {

  public static final String EVT_THREAD_BEGUN = "threadBegun";
  public static final String EVT_THREAD_ENDED = "threadEnded";

  public void threadBegun() {}
  public void threadEnded() {}

  public abstract void threadWait();
  public abstract void reset();
}
