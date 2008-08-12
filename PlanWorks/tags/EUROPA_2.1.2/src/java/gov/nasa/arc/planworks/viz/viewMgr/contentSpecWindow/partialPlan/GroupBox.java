//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: GroupBox.java,v 1.2 2003-10-09 17:23:30 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JPanel;

import javax.swing.JFrame;
import java.awt.Container;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

/**
 * <code>GroupBox</code> -
 *                      JPanel->GroupBox
 *                      ContentSpecGroup->GroupBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The <code>JPanel</code> which contains a logical grouping of <code>ContentSpecElements</code>.  
 * Each <code>GroupBox</code> holds a group of elements by type: timeline, constraint, predicate,
 * time interval, and variable type.
 */

public class GroupBox extends JPanel implements ContentSpecGroup {
  protected ArrayList elements;
  protected MDIInternalFrame window;

  /**
   * <code>GroupBox</code>
   * Creates the GroupBox object, a list of associated elements, and a <code>GridBagLayout</code>,
   * @param window The MDIInternalFrame into which the GroupBox is being packed.  Used for the
   *               <code>pack()</code> method.
   */
  public GroupBox(MDIInternalFrame window) {
    this.window = window;
    elements = new ArrayList();
    GridBagLayout gridBag = new GridBagLayout();
    setLayout(gridBag);
  }
  /**
   * Adds a <code>ContentSpecElement</code> to the list and to the panel, then redraws the window.
   * @param element The <code>ContentSpecElement</code> to be added.
   */
  public void add(ContentSpecElement element) {
    super.add((Component)element);
    elements.add(element);
    invalidate();
    validate();
    repaint();
    window.pack();
  }
  /**
   * Removes a <code>ContentSpecElement</code> from the panel and list, then redraws the window.
   * @param element The <code>ContentSpecElement</code> to be removed.
   */
  public void remove(ContentSpecElement element) {
    super.remove((Component)element);
    elements.remove(elements.indexOf(element));
    invalidate();
    validate();
    repaint();
    window.pack();
  }
  /**
   * Returns the aggregation of the values of the contained ContentSpecElements.
   * @return List a <code>List</code> of <code>String</code>s that constitute the type
   *         specification for this group.
   */
  public List getValues() throws NullPointerException, IllegalArgumentException {
    if(elements.size() == 0) {
      return null;
    }
    ArrayList retval = new ArrayList();
    for(int i = 0; i < elements.size(); i++) {
      Collection c = ((ContentSpecElement)elements.get(i)).getValue();
      if(c == null) {
        continue;
      }
      retval.addAll(c);
    }
    if(retval.size() == 0) {
      return null;
    }
    return retval;
  }
  public List getElements() {
    return new ArrayList(elements);
  }
  /**
   * Clears the values of the contained elements.
   */
  public void reset() {
    while(elements.size() != 0) {
      remove((ContentSpecElement) elements.get(0));
    }
    /*
    for(int i = 0; i < elements.size(); i++) {
      System.err.println(elements.size() + " Resetting " + elements.get(i));
      ((ContentSpecElement)elements.get(i)).reset();
      }*/
  }
}
