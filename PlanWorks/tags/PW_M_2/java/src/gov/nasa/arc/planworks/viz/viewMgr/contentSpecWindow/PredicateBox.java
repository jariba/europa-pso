//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PredicateBox.java,v 1.3 2003-06-16 18:51:09 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

/**
 * <code>PredicateBox</code> -
 *                      SpecBox->PredicateBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A ContentSpecElement for specifying a particular predicate.
 */

public class PredicateBox extends SpecBox {
  public PredicateBox(boolean first) {
    super(first, "Predicate");
  }
}
