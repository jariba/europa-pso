//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: VariableTypeBox.java,v 1.4 2003-06-16 18:51:11 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JFrame;

/**
 * <code>VariableTypeBox</code> -
 *            JPanel->VariableTypeBox
 *            ContentSpecElement->VariableTypeBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A box for specifying variable types.  This and TimeIntervalBox don't inherit from the SpecBox
 * class because they have special input concerns.
 */

public class VariableTypeBox extends JPanel implements ContentSpecElement {
  private LogicComboBox logicBox;
  private NegationCheckBox negationBox;
  private JComboBox typeBox;

  /**
   * Creates the VariableTypeBox and adds the input widgets.
   * @param first <code>boolean</code> determining whether or not this is the first of its type.
   *              if it is, the LogicComboBox is disabled--the connective is always "OR".
   */

  public VariableTypeBox(boolean first) {
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    logicBox = new LogicComboBox();
    logicBox.addItemListener(new LogicBoxListener(this));
    if(first) {
      logicBox.setEnabled(false);
    }
    
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = 0;
    gridBag.setConstraints(logicBox, c);
    add(logicBox);
    
    negationBox = new NegationCheckBox();
    c.gridx++;
    gridBag.setConstraints(negationBox, c);
    add(negationBox);
    
    JLabel label1 = new JLabel("Variable Type");
    c.gridx++;
    gridBag.setConstraints(label1, c);
    add(label1);
    
    typeBox = new JComboBox();
    typeBox.addItem("");
    typeBox.addItem("START_VAR");
    typeBox.addItem("END_VAR");
    typeBox.addItem("DURATION_VAR");
    typeBox.addItem("OBJECT_VAR");
    typeBox.addItem("REJECT_VAR");
    typeBox.addItem("PARAMETER_VAR");
    c.gridx++;
    gridBag.setConstraints(typeBox, c);
    add(typeBox);
  }
  /**
   * Gets the logical value of the VariableTypeBox, which is always of the form: "and", "or",
   * "and not", or "or not" followed by one of "START_VAR", "END_VAR", "DURATION_VAR",
   * "OBJECT_VAR", "REJECT_VAR", "PARAMETER_VAR".
   * @return <code>List</code> containing the logical value of the VariableTypeBox.
   */
  public List getValue() throws NullPointerException {
    ArrayList retval = new ArrayList();
    StringBuffer connective = new StringBuffer();
    if(((String)typeBox.getSelectedItem()).equals("")) {
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
    retval.add((String)typeBox.getSelectedItem());
    return retval;
  }
  /**
   * Adds a new VariableTypeBox to the containing GroupBox.  This is done when a LogicComboBox's 
   * value is changed from blank to a connective.
   */
  protected void addVariableTypeBox() {
    GroupBox parent = (GroupBox) getParent();
    GridBagLayout gridBag = (GridBagLayout) parent.getLayout();
    GridBagConstraints c = new GridBagConstraints();
    VariableTypeBox box = new VariableTypeBox(false);
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    gridBag.setConstraints(box, c);
    parent.add((ContentSpecElement)box);
    parent.validate();
  }
  /**
   * Removes the current VariableTypeBox from the containing GroupBox.  This is done when a
   * LogicComboBox's value is changed from a connective to blank.
   */
  protected void removeVariableTypeBox() {
    GroupBox parent = (GroupBox) getParent();
    parent.remove((ContentSpecElement)this);
    parent.validate();
    parent.repaint();
  }
  /**
   * Resets the values input by the user.
   */
  public void reset() {
    logicBox.setSelectedItem("");
    negationBox.setSelected(false);
    typeBox.setSelectedItem("");
  }
  /**
   * <code>LogicBoxListener</code> -
   *    See the LogicBoxListener documentation in SpecBox.java
   */
  class LogicBoxListener implements ItemListener {
    private VariableTypeBox box;
    private String itemStateChangedFrom;
    
    public LogicBoxListener(VariableTypeBox box) {
      this.box = box;
      itemStateChangedFrom = null;
    }
    public void itemStateChanged(ItemEvent ie) {
      if(ie.getStateChange() == ItemEvent.DESELECTED) {
        itemStateChangedFrom = (String) ie.getItem();
      }
      else if(ie.getStateChange() == ItemEvent.SELECTED) {
        if(itemStateChangedFrom.equals("") &&
           (((String)ie.getItem()).equals("AND") || ((String)ie.getItem()).equals("OR"))) {
          box.addVariableTypeBox();
        }
        else if((itemStateChangedFrom.equals("AND") || itemStateChangedFrom.equals("OR")) &&
                ((String)ie.getItem()).equals("")) {
          box.removeVariableTypeBox();
        }
      }
    }
  }
}
