//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecChecker.java,v 1.7 2003-09-25 23:52:47 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.List;
/**
 * <code>ContentSpecChecker</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * Interface to expose only the isInContentSpec() method to VizViews.
 */

public interface ContentSpecChecker {
  public List getValidIds();
}
