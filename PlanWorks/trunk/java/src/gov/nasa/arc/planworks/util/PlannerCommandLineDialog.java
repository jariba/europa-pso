package gov.nasa.arc.planworks.util;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.ExecutePlannerThread;

public class PlannerCommandLineDialog extends JDialog {
  private JTextField textField;
  public PlannerCommandLineDialog(Frame owner) {
    super(owner, "New Sequence Command Line", true);

    Container contentPane = getContentPane();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    contentPane.setLayout(gridBag);
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;

    JLabel label = new JLabel("Enter the command line to execute");
    gridBag.setConstraints(label, c);
    contentPane.add(label);
    
    textField = new JTextField(30);
    c.gridy++;
    gridBag.setConstraints(textField, c);
    contentPane.add(textField);

    JButton executeButton = new JButton("Execute");
    executeButton.addActionListener(new ExecuteButtonListener(this));
    c.gridx++;
    gridBag.setConstraints(executeButton, c);
    contentPane.add(executeButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new CancelButtonListener(this));
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(cancelButton, c);
    contentPane.add(cancelButton);
    pack();
    Point planWorksLocation = owner.getLocation();
    setLocation((int)(planWorksLocation.getX() + owner.getSize().getWidth() / 2 -
                      getPreferredSize().getWidth() / 2),
                (int)(planWorksLocation.getY() + owner.getSize().getHeight() / 2 -
                      getPreferredSize().getHeight() / 2));
    setBackground(ColorMap.getColor("gray60"));
  }

  public String getCommandLine() {
    return textField.getText();
  }
  class ExecuteButtonListener implements ActionListener {
    PlannerCommandLineDialog dialog;
    public ExecuteButtonListener(PlannerCommandLineDialog dialog) {
      this.dialog = dialog;
    }
    public void actionPerformed(ActionEvent e) {
      ExecutePlannerThread thread = new ExecutePlannerThread(dialog.getCommandLine());
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
      dialog.hide();
    }
  }
  class CancelButtonListener implements ActionListener {
    PlannerCommandLineDialog dialog;
    public CancelButtonListener(PlannerCommandLineDialog dialog) {
      this.dialog = dialog;
    }
    public void actionPerformed(ActionEvent e) {
      dialog.hide();
    }
  }
}
