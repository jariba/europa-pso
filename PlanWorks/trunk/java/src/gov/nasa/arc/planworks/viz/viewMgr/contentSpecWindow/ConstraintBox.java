//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ConstraintBox.java,v 1.3 2003-06-16 18:51:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

/*
 * <code>ConstraintBox</code> -
 *                  JPanel->SpecBox->ConstraintBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * ConstraintBox provides the input widgets for constraint content specification.
 */
public class ConstraintBox extends SpecBox {
  public ConstraintBox(boolean first) {
    super(first, "Constraint");
  }
}
