//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: RedrawNotifier.java,v 1.3 2003-06-16 18:50:39 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr;

/**
 * <code>ContentSpecChecker</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * Interface to expose only the notifyRedraw method to ContentSpecs.
 */

public interface RedrawNotifier {
  /**
   * Calls the redraw() method on all available VizViews.
   */
  public void notifyRedraw();
}
