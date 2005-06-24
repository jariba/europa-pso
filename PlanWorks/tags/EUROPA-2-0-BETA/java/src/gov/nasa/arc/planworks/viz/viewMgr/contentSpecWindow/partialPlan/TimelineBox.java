//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimelineBox.java,v 1.1 2003-10-01 23:54:02 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.util.Map;
/**
 * <code>TimelineBox</code> -
 *                      SpecBox->PredicateBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A ContentSpecElement for specifying a particular timeline.
 */

public class TimelineBox extends SpecBox {
  public TimelineBox(boolean first, Map timelineNames) {
    super(first, "Timeline", timelineNames);
  }
}
