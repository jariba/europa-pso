//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecGroup.java,v 1.3 2003-06-16 18:51:08 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.util.List;

/**
 * <code>ContentSpecGroup</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * An interface to provide only the <code>getValues</code> method to the ContentSpecWindow.
 */

public interface ContentSpecGroup {
  /**
   * <code>getValues</code> -
   * @return A <code>List</code> of <code>String</code>s which is the aggregate of the values of 
   * the contained <code>ContentSpecElement</code>s.
   */
  public List getValues();
}
