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
