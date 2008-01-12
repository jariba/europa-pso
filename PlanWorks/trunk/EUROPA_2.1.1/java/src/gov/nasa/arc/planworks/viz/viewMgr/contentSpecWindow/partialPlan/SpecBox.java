//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: SpecBox.java,v 1.3 2004-02-11 01:09:22 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.*;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <code>SpecBox</code> -
 *            JPanel->SpecBox
 *            ContentSpecElement->SpecBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A single element for specifying content.  Includes logical connectives for specification
 * chaining.
 */
public class SpecBox extends JPanel implements ContentSpecElement {
  protected LogicComboBox logicBox;
  protected NegationCheckBox negationBox;
  protected JComboBox keyField;
  private String name;
  private Map names;
  private static final Pattern keyPattern = Pattern.compile("\\d+");
  /**
   * Constructs the SpecBox and arranges the appropriate input fields.
   * @param first <code>boolean</code> value determining whether or not this SpecBox is the first
   *              of its type.  If it is, the LogicComboBox is disabled--the connective is always 
   *              OR.
   * @param name the name of the type of SpecBox.  One of Timeline, Predicate, or Constraint.
   */
  public SpecBox(boolean first, String name, Map names) {
    this.name = name.toString();
    this.names = names;
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);

    logicBox = new LogicComboBox();
    logicBox.addItemListener(new LogicListener(this));
    if(first) {
      logicBox.setEnabled(false);
    }
    c.weightx = 1;
    c.gridx = 0;
    c.gridy = 0;
    gridBag.setConstraints(logicBox, c);
    add(logicBox);

    negationBox = new NegationCheckBox();
    if(!first) {
      negationBox.setEnabled(false);
    }
    c.gridx = 1;
    gridBag.setConstraints(negationBox, c);
    add(negationBox);

    JLabel label1 = new JLabel(name.toString());
    c.gridx = 2;
    gridBag.setConstraints(label1, c);
    add(label1);
    keyField = new JComboBox();
    keyField.addItem("");

    Object [] nameArray = names.keySet().toArray();
    Arrays.sort(nameArray);

    for(int i = 0; i < nameArray.length; i++) {
      keyField.addItem(nameArray[i]);
    }
    if(!first) {
      keyField.setEnabled(false);
    }
    c.gridx = 3;
    gridBag.setConstraints(keyField, c);
    add(keyField);
  }
  /**
   * Returns the value of the ContentSpecElement.  Always one of "and", "or", "and not", 
   * or "or not" followed by the appropriate key.
   * @return <code>List</code> containg the connective and key.
   */
  public List getValue() throws NullPointerException, IllegalArgumentException {
    ArrayList retval = new ArrayList();
    StringBuffer connective = new StringBuffer();
    if(keyField.getSelectedItem().equals("")) {
      return null;
    }
    if(logicBox.isEnabled()) {
      if(((String)logicBox.getSelectedItem()).equals("")) {
        return null;
      }
      connective.append(((String)logicBox.getSelectedItem()).toLowerCase());
    }
    else {
      connective.append("or");
    }
    if(negationBox.isSelected()) {
      connective.append(" not");
    }
    retval.add(connective.toString());
    retval.add(names.get(keyField.getSelectedItem()));
    return retval;
  }

  public String getName() {return name;}
  /**
   * Adds a new <code>ContentSpecElement</code> to the parent <code>GroupBox</code>.  Called when
   * the LogicComboBox is moved from blank to one of the connectives.
   */
  protected void addSpecBox() {
    GroupBox parent = (GroupBox) getParent();
    GridBagLayout gridBag = (GridBagLayout) parent.getLayout();
    GridBagConstraints c = new GridBagConstraints();
    SpecBox box = new SpecBox(false, name, names);
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    gridBag.setConstraints(box, c);
    parent.add((ContentSpecElement)box);
    parent.validate();
  }
  /**
   * Removes the current <code>ContentSpecElement</code> from the parent <code>GroupBox</code>.
   * Called when the LogicComboBox is moved from one of the connectives to the blank value.
   */
  protected void removeSpecBox() {
    GroupBox parent = (GroupBox) getParent();
    parent.remove((ContentSpecElement)this);
    parent.validate();
    parent.repaint();
  }
  /**
   * Removes all values input by the user.
   */
  public void reset() {
    negationBox.setSelected(false);
    keyField.setSelectedItem("");
    logicBox.setSelectedItem("");
  }

  public LogicComboBox getLogicBox() {
    return logicBox;
  }
  public NegationCheckBox getNegationBox() {
    return negationBox;
  }
  public JComboBox getComboBox() {
    return keyField;
  }

  public void setSelectedComboItem(Object item) {
    if(item instanceof Integer) {
      Iterator nameIterator = names.keySet().iterator();
      while(nameIterator.hasNext()) {
        String name = (String) nameIterator.next();
        if(((Integer)names.get(name)).equals(item)) {
        keyField.setSelectedItem(name);
        }
      }
    }
    else if(item instanceof String) {
      keyField.setSelectedItem(item);
    }
  }

  /**
   * <code>LogicListener</code> -
   *                  ItemListener->LogicListener
   * Class that adds or removes a ContentSpecElement from a GroupBox when the proper action is
   * performed.
   */
  class LogicListener implements ItemListener {
    private SpecBox box;
    private String itemStateChangedFrom;
    public LogicListener(SpecBox box) {
      super();
      this.box = box;
      itemStateChangedFrom = null;
    }
    public void itemStateChanged(ItemEvent ie) {
      if(ie.getStateChange() == ItemEvent.DESELECTED) {
	itemStateChangedFrom = (String) ie.getItem();
      }
      else if(ie.getStateChange() == ItemEvent.SELECTED) {
        if(itemStateChangedFrom.equals("") && 
           (((String)ie.getItem()).equals("AND") || 
            ((String)ie.getItem()).equals("OR"))) {
          box.addSpecBox();
          box.keyField.setEnabled(true);
          box.negationBox.setEnabled(true);
        }
        else if((itemStateChangedFrom.equals("AND") || 
                 itemStateChangedFrom.equals("OR")) && 
                ((String)ie.getItem()).equals("")) {
          box.removeSpecBox();
        }
      }
    }
  }
}
