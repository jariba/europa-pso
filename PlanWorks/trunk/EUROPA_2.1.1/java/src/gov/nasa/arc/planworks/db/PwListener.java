// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwListener.java,v 1.2 2004-04-22 19:26:18 taylor Exp $
//
// PlanWorks -- 
//

package gov.nasa.arc.planworks.db;

public interface PwListener {
  public void fireEvent(String evtName);
}
