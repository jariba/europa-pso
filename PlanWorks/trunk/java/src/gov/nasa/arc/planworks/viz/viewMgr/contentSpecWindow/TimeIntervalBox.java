//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: TimeIntervalBox.java,v 1.3 2003-06-16 16:28:08 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

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

public class TimeIntervalBox extends JPanel implements ContentSpecElement {
  private LogicComboBox logicBox;
  private NegationCheckBox negationBox;
  private JTextField startValue, endValue;
  private static final Pattern valuePattern = Pattern.compile("\\d+");

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
  }
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
  protected void removeTimeIntervalBox() {
    GroupBox parent = (GroupBox) getParent();
    parent.remove(this);
    parent.validate();
    parent.repaint();
  }
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
    if(negationBox.isSelected()) {
      connective.append(" not");
    }
    retval.add(connective.toString());
    retval.add(startValue.getText().trim());
    retval.add(endValue.getText().trim());
    return retval;
  }
  public void reset() {
    logicBox.setSelectedItem("");
    negationBox.setSelected(false);
    startValue.setText("");
    endValue.setText("");
  }
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
        }
        else if((itemStateChangedFrom.equals("AND") || itemStateChangedFrom.equals("OR")) &&
                ((String)ie.getItem()).equals("")) {
          box.removeTimeIntervalBox();
        }
      }
    }
  }
}
