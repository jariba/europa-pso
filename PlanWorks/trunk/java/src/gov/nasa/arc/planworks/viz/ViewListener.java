// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ViewListener.java,v 1.2 2004-04-22 19:26:21 taylor Exp $
//
// PlanWorks -- 
//

package gov.nasa.arc.planworks.viz;

import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoDocumentEvent;

public abstract class ViewListener {

  public static final String EVT_INIT_BEGUN_DRAWING = "initDrawingBegun";
  public static final String EVT_REDRAW_BEGUN_DRAWING = "redrawDrawingBegun";
  public static final String EVT_INIT_ENDED_DRAWING = "initDrawingEnded";
  public static final String EVT_REDRAW_ENDED_DRAWING = "redrawDrawingEnded";
  public static final String EVT_JGO_VIEW_CHANGED = "jGoViewChanged";
  public static final String EVT_JGO_DOCUMENT_CHANGED = "jGoDocumentChanged";

  public void initDrawingBegun() {}
  public void redrawDrawingBegun() {}
  public void initDrawingEnded() {}
  public void redrawDrawingEnded() {}
  public void jGoViewChanged(JGoViewEvent e) {}
  public void jGoDocumentChanged(JGoDocumentEvent e) {}
  public void jGoViewChanged(JGoDocumentEvent e) {}
  public void jGoDocumentChanged(JGoViewEvent e) {}

  public abstract void viewWait();
  public abstract void reset();
}
