//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PredicateBox.java,v 1.4 2003-07-14 20:52:20 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.util.Map;

/**
 * <code>PredicateBox</code> -
 *                      SpecBox->PredicateBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A ContentSpecElement for specifying a particular predicate.
 */

public class PredicateBox extends SpecBox {
  public PredicateBox(boolean first, Map predicateNames) {
    super(first, "Predicate", predicateNames);
  }
}
