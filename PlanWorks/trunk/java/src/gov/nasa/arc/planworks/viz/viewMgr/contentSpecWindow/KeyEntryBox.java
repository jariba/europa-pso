package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class KeyEntryBox extends JTextField
{
  public KeyEntryBox()
  {
    super(5);
    add(new JLabel("Key"));
  }
  public static void main(String [] args)
  {
    JFrame frame = new JFrame("test");
    frame.setBounds(100, 100, 100, 100);
    Container contentPane = frame.getContentPane();
    contentPane.add(new KeyEntryBox());
    frame.setVisible(true);
  }
}
