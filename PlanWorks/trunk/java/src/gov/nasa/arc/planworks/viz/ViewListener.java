package gov.nasa.arc.planworks.viz;

import com.nwoods.jgo.JGoViewEvent;
import com.nwoods.jgo.JGoDocumentEvent;

public abstract class ViewListener {
  public static final String BEGIN_DRAW = "drawingBegun";
  public static final String END_DRAW = "drawingEnded";
  public static final String VIEW_CHANGED = "jGoViewChanged";
  public static final String DOC_CHANGED = "jGoDocumentChanged";
  public void drawingBegun(){}
  public void drawingEnded(){}
  public void jGoViewChanged(JGoViewEvent e){}
  public void jGoDocumentChanged(JGoDocumentEvent e){}
  public void jGoViewChanged(JGoDocumentEvent e){}
  public void jGoDocumentChanged(JGoViewEvent e){}
}
