package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import javax.swing.JComboBox;


public class LogicComboBox extends JComboBox
{
  public LogicComboBox()
  {
    addItem("");
    addItem("AND");
    addItem("OR");
    setSize(58, 44);
  }
}
