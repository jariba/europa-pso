//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimelineBox.java,v 1.4 2003-07-14 20:52:21 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

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
