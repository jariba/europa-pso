//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ClassBox.java,v 1.1 2004-08-21 00:31:59 taylor Exp $
//
package gov.nasa.arc.planworks.viz.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.ContentSpecElement;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan.GroupBox;

/**
 * <code>ClassBox</code> -
 *                      SpecBox->ClassBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A ContentSpecElement for specifying a particular class.
 */

public class ClassBox extends JPanel implements ContentSpecElement {

  private static final String ALL_CLASSES = "All Classes";

  protected JComboBox classField;
  private String name;
  private Map allNames;
  private Map unselectedNames;
  private boolean isFirstClick;

  /**
   * Constructs the ClassBox and arranges the appropriate input fields.
   * @param first <code>boolean</code> value determining whether or not this ClassBox is the first
   *              of its type.  
   * @param name the name of the type of ClassBox.  
   */
  public ClassBox(boolean first, String name, Map allNames, Map unselectedNames) {
    this.name = name.toString();
    this.allNames = allNames;
    this.unselectedNames = unselectedNames;
    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    isFirstClick = true;
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);

    c.weightx = 1;
    c.gridx = 0;
    c.gridy = 0;
    JLabel label1 = new JLabel(name.toString());
    gridBag.setConstraints(label1, c);
    add(label1);
    classField = new JComboBox();
    classField.addItemListener(new ClassFieldListener(this));
    if (first) {
      classField.addItem( ALL_CLASSES);
    } else {
      classField.addItem("");
    }
    Object [] nameArray = unselectedNames.keySet().toArray();
    Arrays.sort(nameArray);

    for(int i = 0; i < nameArray.length; i++) {
      classField.addItem(nameArray[i]);
    }
//     if(!first) {
//       classField.setEnabled(false);
//     }
    c.gridx = 1;
    gridBag.setConstraints(classField, c);
    add(classField);
  }
  /**
   * Returns the value of the ContentSpecElement.  Always one of "and", "or", "and not", 
   * or "or not" followed by the appropriate key.
   * @return <code>List</code> containg the connective and key.
   */
  public List getValue() throws NullPointerException, IllegalArgumentException {
    ArrayList retval = new ArrayList();
    if(classField.getSelectedItem().equals("")) {
      return null;
    }
    retval.add( allNames.get(classField.getSelectedItem()));
    return retval;
  }

  public String getName() {
    return name;
  }

  /**
   * Adds a new <code>ContentSpecElement</code> to the parent <code>GroupBox</code>.  Called when
   * the classField is moved from a blank value to a class combo value.
   */
  protected void addClassBox( String selectedItem) {
    GroupBox parent = (GroupBox) getParent();
    GridBagLayout gridBag = (GridBagLayout) parent.getLayout();
    GridBagConstraints c = new GridBagConstraints();
    unselectedNames.remove( selectedItem);
    if (unselectedNames.size() > 0) {
      ClassBox box = new ClassBox(false, name, allNames, unselectedNames);
      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = GridBagConstraints.RELATIVE;
      gridBag.setConstraints(box, c);
      parent.add((ContentSpecElement)box);
      parent.validate();
    }
  }
  /**
   * Removes the current <code>ContentSpecElement</code> from the parent <code>GroupBox</code>.
   * Called when the classField is moved from a value to the blank value.
   */
  protected void removeClassBox() {
    GroupBox parent = (GroupBox) getParent();
    parent.remove((ContentSpecElement)this);
    parent.validate();
    parent.repaint();
  }
  /**
   * Removes all values input by the user.
   */
  public void reset() {
    classField.setSelectedItem("");
  }

  public JComboBox getComboBox() {
    return classField;
  }

  public void setSelectedComboItem(Object item) {
    if(item instanceof Integer) {
      Iterator nameIterator = unselectedNames.keySet().iterator();
      while(nameIterator.hasNext()) {
        String name = (String) nameIterator.next();
        if(((Integer)unselectedNames.get(name)).equals(item)) {
        classField.setSelectedItem(name);
        }
      }
    }
    else if(item instanceof String) {
      classField.setSelectedItem(item);
    }
  }

  /**
   * <code>ClassFieldListener</code> -
   * Class that adds or removes a ContentSpecElement from a GroupBox when the proper action is
   * performed.
   */
  class ClassFieldListener implements ItemListener {
    private ClassBox box;
    private String itemStateChangedFrom;
    public ClassFieldListener(ClassBox box) {
      super();
      this.box = box;
      itemStateChangedFrom = null;
    }
    public void itemStateChanged(ItemEvent ie) {
      // System.err.println( "itemStateChangedFrom: " + (String) ie.getItem());
      if(ie.getStateChange() == ItemEvent.DESELECTED) {
	itemStateChangedFrom = (String) ie.getItem();
      }
      else if ((ie.getStateChange() == ItemEvent.SELECTED) &&
               (itemStateChangedFrom != null)) {
        // System.err.println( "itemStateChangedFrom " + itemStateChangedFrom);
        if (itemStateChangedFrom.equals("")) {
          box.addClassBox( (String)ie.getItem());
        }
        else if (isFirstClick && itemStateChangedFrom.equals( ALL_CLASSES)) {
          isFirstClick = false;
          box.addClassBox( (String)ie.getItem());
        }
         else if (((String)ie.getItem()).equals("")) {
          box.removeClassBox();
        }
      }
    }
  } // end class ClassFieldListener



} // end ClassBox 
