//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimeIntervalBox.java,v 1.1 2003-10-01 23:54:02 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.JFrame;

/**
 * <code>TimeIntervalBox</code> -
 *            JPanel->TimeIntervalBox
 *            ContentSpecElement->TimeIntervalBox
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * A box for specifying time intervals.  This and VariableTypeBox don't inherit from the SpecBox
 * class because they have special input concerns.
 */

public class TimeIntervalBox extends JPanel implements ContentSpecElement {
  private LogicComboBox logicBox;
  private NegationCheckBox negationBox;
  private JTextField startValue, endValue;
  private static final Pattern valuePattern = Pattern.compile("\\d+");

  /**
   * Creates the TimeIntervalBox and adds the input widgets.
   * @param first <code>boolean</code> determining whether or not this is the first of its type.
   *              if it is, the LogicComboBox is disabled--the connective is always "OR".
   */

  public TimeIntervalBox(boolean first) {
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    c.weightx = 0.5;
    c.gridy = 0;

    logicBox = new LogicComboBox();
    logicBox.addItemListener(new LogicBoxListener(this));
    if(first) {
      logicBox.setEnabled(false);
    }
    c.gridx = 0;
    gridBag.setConstraints(logicBox, c);
    add(logicBox);

    negationBox = new NegationCheckBox();
    c.gridx++;
    gridBag.setConstraints(negationBox, c);
    add(negationBox);

    JLabel label1 = new JLabel("Time Interval Start");
    c.gridx++;
    gridBag.setConstraints(label1, c);
    add(label1);

    startValue = new JTextField(5);
    c.gridx++;
    gridBag.setConstraints(startValue, c);
    add(startValue);
    
    JLabel endLabel = new JLabel("End");
    c.gridx++;
    gridBag.setConstraints(endLabel, c);
    add(endLabel);
    
    endValue = new JTextField(5);
    c.gridx++;
    gridBag.setConstraints(endValue, c);
    add(endValue);

    if(!first) {
      negationBox.setEnabled(false);
      startValue.setEnabled(false);
      endValue.setEnabled(false);
    }
  }
  /**
   * Adds a new TimeIntervalBox to the containing GroupBox.  This is done when a LogicComboBox's 
   * value is changed from blank to a connective.
   */
  protected void addTimeIntervalBox() {
    GroupBox parent = (GroupBox) getParent();
    GridBagLayout gridBag = (GridBagLayout) parent.getLayout();
    GridBagConstraints c = new GridBagConstraints();
    TimeIntervalBox box = new TimeIntervalBox(false);
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    gridBag.setConstraints(box, c);
    parent.add((ContentSpecElement)box);
    parent.validate();
  }
  /**
   * Removes the current TimeIntervalBox from the containing GroupBox.  This is done when a
   * LogicComboBox's value is changed from a connective to blank.
   */
  protected void removeTimeIntervalBox() {
    GroupBox parent = (GroupBox) getParent();
    parent.remove((ContentSpecElement)this);
    parent.validate();
    parent.repaint();
  }
  /**
   * Gets the logical value of the TimeIntervalBox, which is always of the form: "and", "or",
   * "and not", or "or not" followed by the start and end time.
   * @return <code>List</code> containing the logical value of the TimeIntervalBox.
   */
  public List getValue() throws NullPointerException, IllegalArgumentException {
    ArrayList retval = new ArrayList();
    StringBuffer connective = new StringBuffer();
    if(logicBox.isEnabled()) {
      if(((String)logicBox.getSelectedItem()).equals("")) {
        return null;
      }
      connective.append(((String)logicBox.getSelectedItem()).toLowerCase());
    }
    else {
      connective.append("or");
    }
    if(startValue.getText().trim().equals("") ^ endValue.getText().trim().equals("")) {
      JOptionPane.showMessageDialog(getParent().getParent().getParent().getParent().getParent().getParent().getParent(), "Both start and end times must be filled in.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    if(startValue.getText().trim().equals("") && endValue.getText().trim().equals("")) {
      return null;
    }
    if(!valuePattern.matcher(startValue.getText().trim()).matches()) {
      JOptionPane.showMessageDialog(getParent().getParent().getParent().getParent().getParent().getParent().getParent(), "Invalid start time format.  Must be only digits.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    if(!valuePattern.matcher(endValue.getText().trim()).matches()) {
      JOptionPane.showMessageDialog(getParent().getParent().getParent().getParent().getParent().getParent().getParent(), "Invalid end time format.  Must be only digits.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    Integer startTime = new Integer(startValue.getText().trim());
    Integer endTime = new Integer(endValue.getText().trim());
    if(startTime.compareTo(endTime) != -1) {
      JOptionPane.showMessageDialog(getParent().getParent().getParent().getParent().getParent().getParent().getParent(), "Invalid times--start time must be earlier than end time.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
    }
    if(negationBox.isSelected()) {
      connective.append(" not");
    }
    retval.add(connective.toString());
    retval.add(startTime);
    retval.add(endTime);
    return retval;
  }
  /**
   * Resets the values input by the user.
   */
  public void reset() {
    logicBox.setSelectedItem("");
    negationBox.setSelected(false);
    startValue.setText("");
    endValue.setText("");
  }

  public LogicComboBox getLogicBox() {
    return logicBox;
  }
  public NegationCheckBox getNegationBox() {
    return negationBox;
  }
  public JTextField getStartValue() {
    return startValue;
  }
  public JTextField getEndValue() {
    return endValue;
  }

  /**
   * <code>LogicBoxListener</code> -
   *    See the LogicBoxListener documentation in SpecBox.java
   */
  class LogicBoxListener implements ItemListener {
    private TimeIntervalBox box;
    private String itemStateChangedFrom;
    
    public LogicBoxListener(TimeIntervalBox box) {
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
           (((String)ie.getItem()).equals("AND") || ((String)ie.getItem()).equals("OR"))) {
          box.addTimeIntervalBox();
          box.startValue.setEnabled(true);
          box.endValue.setEnabled(true);
          box.negationBox.setEnabled(true);
        }
        else if((itemStateChangedFrom.equals("AND") || itemStateChangedFrom.equals("OR")) &&
                ((String)ie.getItem()).equals("")) {
          box.removeTimeIntervalBox();
        }
      }
    }
  }
}
