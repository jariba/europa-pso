package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
//import java.util.regex.Pattern;
//import java.util.regex.Matcher;
import java.util.regex.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SpecBox extends JPanel implements ContentSpecElement
{
  private LogicComboBox logicBox;
  private NegationCheckBox negationBox;
  private JTextField keyField;
  private String name;
  private static final Pattern keyPattern = Pattern.compile("[K|k]\\d+");

  public SpecBox(boolean first, String name)
  {
    this.name = name.toString();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    logicBox = new LogicComboBox();
    logicBox.addItemListener(new LogicListener(this));
    if(first)
      logicBox.setEnabled(false);

    c.weightx = 1;
    c.gridx = 0;
    c.gridy = 0;
    gridBag.setConstraints(logicBox, c);
    add(logicBox);

    negationBox = new NegationCheckBox();
    c.gridx = 1;
    gridBag.setConstraints(negationBox, c);
    add(negationBox);

    JLabel label1 = new JLabel(name.toString().concat(" Key"));
    c.gridx = 2;
    gridBag.setConstraints(label1, c);
    add(label1);
    
    keyField = new JTextField(5);
    c.gridx = 3;
    gridBag.setConstraints(keyField, c);
    add(keyField);
  }
  public List getValue() throws NullPointerException, IllegalArgumentException
  {
    ArrayList retval = new ArrayList();
    StringBuffer connective = new StringBuffer();
    if(keyField.getText().trim().equals(""))
      return null;
    if(!keyPattern.matcher(keyField.getText().trim()).matches())
      {
        JOptionPane.showMessageDialog(getParent().getParent().getParent().getParent().getParent().getParent().getParent(), (new StringBuffer("Invalid ")).append(name).append(" key format.  Must be of the form [K|k]\\d+.").toString(), "Error!", JOptionPane.ERROR_MESSAGE);
        throw new IllegalArgumentException();
      }
    if(logicBox.isEnabled())
      {
	if(((String)logicBox.getSelectedItem()).equals(""))
	  return null;
	connective.append(((String)logicBox.getSelectedItem()).toLowerCase());
      }
    else
      connective.append("or");
    if(negationBox.isSelected())
      connective.append(" not");
    retval.add(connective.toString());
    retval.add(keyField.getText().trim());
    return retval;
  }
  protected void addSpecBox()
  {
    GroupBox parent = (GroupBox) getParent();
    GridBagLayout gridBag = (GridBagLayout) parent.getLayout();
    GridBagConstraints c = new GridBagConstraints();
    SpecBox box = new SpecBox(false, name);
    c.weightx = 0.5;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    gridBag.setConstraints(box, c);
    parent.add((ContentSpecElement)box);
    parent.validate();
  }
  protected void removeSpecBox()
  {
    GroupBox parent = (GroupBox) getParent();
    parent.remove(this);
    parent.validate();
    parent.repaint();
  }
  public void reset()
  {
    negationBox.setSelected(false);
    keyField.setText("");
    logicBox.setSelectedItem("");
  }
  class LogicListener implements ItemListener
  {
    private SpecBox box;
    private String itemStateChangedFrom;
    public LogicListener(SpecBox box)
    {
      super();
      this.box = box;
      itemStateChangedFrom = null;
    }
    public void itemStateChanged(ItemEvent ie)
    {
      if(ie.getStateChange() == ItemEvent.DESELECTED)
	itemStateChangedFrom = (String) ie.getItem();
      else if(ie.getStateChange() == ItemEvent.SELECTED)
	{
	  if(itemStateChangedFrom.equals("") && 
	     (((String)ie.getItem()).equals("AND") || 
	      ((String)ie.getItem()).equals("OR")))
            {
              box.addSpecBox();
            }
	  else if((itemStateChangedFrom.equals("AND") || 
		   itemStateChangedFrom.equals("OR")) && 
		  ((String)ie.getItem()).equals(""))
            {
              box.removeSpecBox();
            }
	}
    }
  }
}
