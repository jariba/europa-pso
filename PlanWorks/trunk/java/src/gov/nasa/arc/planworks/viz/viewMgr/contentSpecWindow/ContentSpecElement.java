//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecElement.java,v 1.3 2003-06-16 16:28:06 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.util.List;

public interface ContentSpecElement {
  public List getValue() throws NullPointerException;
  public void reset();
}
