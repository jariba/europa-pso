//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: UniqueKeyBox.java,v 1.3 2004-07-13 23:54:10 taylor Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import javax.swing.JFrame;

import gov.nasa.arc.planworks.db.util.PartialPlanContentSpec;

public class UniqueKeyBox extends JPanel implements ContentSpecElement {
  protected JRadioButton requireButton;
  protected JRadioButton excludeButton;
  protected JTextField key;
  protected JButton addNew;
  protected JButton removeThis;
  private static final Pattern keyPattern = Pattern.compile("\\d+");
  protected static int numBoxes = 0;

  public UniqueKeyBox() {
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    ButtonGroup buttonGroup = new ButtonGroup();
    setLayout(gridBag);
    c.weightx = 0.5;
    c.gridx = c.gridy = 0;

    requireButton = new JRadioButton("require");
    requireButton.addItemListener(new RadioListener(this));
    gridBag.setConstraints(requireButton, c);
    add(requireButton);
    buttonGroup.add(requireButton);

    excludeButton = new JRadioButton("exclude");
    excludeButton.addItemListener(new RadioListener(this));
    c.gridx++;
    gridBag.setConstraints(excludeButton, c);
    add(excludeButton);
    buttonGroup.add(excludeButton);
    
    key = new JTextField(5);
    c.gridx++;
    gridBag.setConstraints(key, c);
    add(key);
    key.setEnabled(false);
    
    addNew = new JButton("Add");
    c.gridx++;
    gridBag.setConstraints(addNew, c);
    add(addNew);
    addNew.addActionListener(new AddButtonListener(this));

    removeThis = new JButton("Remove");
    c.gridx++;
    gridBag.setConstraints(removeThis, c);
    add(removeThis);
    removeThis.addActionListener(new RemoveButtonListener(this));
    numBoxes++;
  }

  protected void addUniqueKeyBox() {
    GroupBox parent = (GroupBox) getParent();
    GridBagLayout gridBag = (GridBagLayout) parent.getLayout();
    GridBagConstraints c = new GridBagConstraints();
    UniqueKeyBox box = new UniqueKeyBox();
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    gridBag.setConstraints(box, c);
    parent.add((ContentSpecElement)box);
    parent.validate(); 
  }

  synchronized protected void removeUniqueKeyBox() {
    System.err.println("Number of unique key boxes: " + numBoxes);
    if(numBoxes == 1) {
      return;
    }
    GroupBox parent = (GroupBox) getParent();
    parent.remove((ContentSpecElement)this);
    parent.validate();
    parent.repaint();
    numBoxes--;
    System.err.println("New number of unique key boxes: " + numBoxes);
  }

  public List getValue() throws NullPointerException, IllegalArgumentException {
    List retval = new ArrayList();
    if(key.getText().trim().equals("")) {
      return null;
    }
    if(requireButton.isSelected()) {
      retval.add(PartialPlanContentSpec.REQUIRE);
    }
    else if(excludeButton.isSelected()) {
      retval.add(PartialPlanContentSpec.EXCLUDE);
    }
    if(!keyPattern.matcher(key.getText().trim()).matches()) {
      JOptionPane.showMessageDialog(getParent().getParent().getParent().getParent().getParent().getParent().getParent(), "Invalid key format.  Must be only digits.", "Error!", JOptionPane.ERROR_MESSAGE);
      throw new IllegalArgumentException();
    }
    Integer keyValue = new Integer(key.getText().trim());
    retval.add(keyValue);
    return retval;
  }

  public void reset() {
    requireButton.setSelected(false);
    excludeButton.setSelected(false);
    key.setText("");
    key.setEnabled(false);
    removeUniqueKeyBox();
    try{Thread.sleep(50);}catch(Exception e){}
  }

  public JRadioButton getRequireButton() {
    return requireButton;
  }

  public JRadioButton getExcludeButton() {
    return excludeButton;
  }

  public JTextField getKeyField() {
    return key;
  }

  public JButton getAddButton() {
    return addNew;
  }

  public JButton getRemoveButton() {
    return removeThis;
  }


  class RadioListener implements ItemListener {
    private UniqueKeyBox box;
    public RadioListener(UniqueKeyBox box) {
      super();
      this.box = box;
    }
    public void itemStateChanged(ItemEvent ie) {
      if(ie.getStateChange() == ItemEvent.SELECTED) {
        box.key.setEnabled(true);
      }
    }
  }

  class AddButtonListener implements ActionListener {
    private UniqueKeyBox box;
    public AddButtonListener(UniqueKeyBox box) {
      super();
      this.box = box;
    }
    public void actionPerformed(ActionEvent e) {
      box.addUniqueKeyBox();
    }
  }

  class RemoveButtonListener implements ActionListener {
    private UniqueKeyBox box;
    public RemoveButtonListener(UniqueKeyBox box) {
      super();
      this.box = box;
    }
    public void actionPerformed(ActionEvent e) {
      box.removeUniqueKeyBox();
    }
  }
}
