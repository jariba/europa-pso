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

public class VariableTypeBox extends JPanel implements ContentSpecElement
{
  private LogicComboBox logicBox;
  private NegationCheckBox negationBox;
  private JComboBox typeBox;
  public VariableTypeBox(boolean first)
  {
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    logicBox = new LogicComboBox();
    logicBox.addItemListener(new LogicBoxListener(this));
    if(first)
      logicBox.setEnabled(false);
    
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
  public List getValue() throws NullPointerException
  {
    ArrayList retval = new ArrayList();
    StringBuffer connective = new StringBuffer();
    if(((String)typeBox.getSelectedItem()).equals(""))
      return null;
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
    retval.add((String)typeBox.getSelectedItem());
    return retval;
  }
  protected void addVariableTypeBox()
  {
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
  protected void removeVariableTypeBox()
  {
    GroupBox parent = (GroupBox) getParent();
    parent.remove(this);
    parent.validate();
    parent.repaint();
  }
  class LogicBoxListener implements ItemListener
  {
    private VariableTypeBox box;
    private String itemStateChangedFrom;
    
    public LogicBoxListener(VariableTypeBox box)
    {
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
            box.addVariableTypeBox();
          else if((itemStateChangedFrom.equals("AND") || itemStateChangedFrom.equals("OR")) &&
                  ((String)ie.getItem()).equals(""))
            box.removeVariableTypeBox();
        }
    }
  }
  public static void main(String [] args)
  {
    JFrame frame = new JFrame("test");
    frame.setBounds(100, 100, 500, 200);
    Container contentPane = frame.getContentPane();
    contentPane.add(new VariableTypeBox(true));
    frame.setVisible(true);
  }
}
