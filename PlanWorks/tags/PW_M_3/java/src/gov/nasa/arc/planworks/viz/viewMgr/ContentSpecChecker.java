//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecChecker.java,v 1.5 2003-07-08 23:19:54 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.List;
/**
 * <code>ContentSpecChecker</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * Interface to expose only the isInContentSpec() method to VizViews.
 */

public interface ContentSpecChecker {
  /**
   * Checks whether or not a key is in the current specification.
   * @param key The key being checked.
   * @return boolean The truth value of the statement "The key is in the specification."
   */
  public boolean isInContentSpec(Integer key);
  public List getValidTokenIds();
}
