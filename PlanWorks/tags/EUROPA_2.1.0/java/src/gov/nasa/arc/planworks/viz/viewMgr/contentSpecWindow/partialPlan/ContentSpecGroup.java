//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecGroup.java,v 1.1 2003-10-01 23:54:01 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

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
