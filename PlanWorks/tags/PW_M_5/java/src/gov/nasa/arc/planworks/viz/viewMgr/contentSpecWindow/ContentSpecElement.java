//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecElement.java,v 1.4 2003-06-16 18:51:08 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.util.List;

/**
 * <code>ContentSpecElement</code> -
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * An interface to provide only the <code>getValue</code> and <code>reset</code> methods
 * to the GroupBoxes.
 */

public interface ContentSpecElement {

  /**
   * <code>getValue</code> -
   * @return <code>List</code> of Strings containing the logical value of the ContentSpecElement.
   */
  public List getValue() throws NullPointerException;
  /**
   * <code>reset</code> -
   * Clears all information entered into the ContentSpecElement by the user.
   */
  public void reset();
}
