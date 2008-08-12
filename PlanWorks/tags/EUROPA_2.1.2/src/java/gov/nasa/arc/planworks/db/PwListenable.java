// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwListenable.java,v 1.2 2004-04-22 19:26:18 taylor Exp $
//
// PlanWorks -- 
//

package gov.nasa.arc.planworks.db;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class PwListenable {
  private List listeners;
  private void initListeners() { if(listeners == null){listeners = new LinkedList();}}
  public void addListener(PwListener listener) {
    initListeners();
    listeners.add(listener);
  }
  public void removeListener(PwListener listener) {
    initListeners();
    listeners.remove(listener);
  }
  public void handleEvent(String evtName) {
    initListeners();
    for(Iterator it = listeners.iterator(); it.hasNext();) {
      ((PwListener)it.next()).fireEvent(evtName);
    }
  }
}
