package gov.nasa.arc.planworks.util;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.ExecutePlannerThread;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;

public class PlannerCommandLineDialog extends JDialog {
  private JTextField commandLine;
  private JTextField stepsPerWrite;
  private JTextField writeDest;
  private JTextField sleepSeconds;
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

    JLabel ss = new JLabel("Seconds to wait ");
    gridBag.setConstraints(ss, c);
    contentPane.add(ss);
    
    sleepSeconds = new JTextField(4);
    c.gridx++;
    gridBag.setConstraints(sleepSeconds, c);
    contentPane.add(sleepSeconds);

    JLabel spw = new JLabel("Steps per write ");
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(spw, c);
    contentPane.add(spw);
    
    stepsPerWrite = new JTextField(4);
    c.gridx++;
    gridBag.setConstraints(stepsPerWrite, c);
    contentPane.add(stepsPerWrite);

    JLabel dest = new JLabel("Write destination ");
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(dest, c);
    contentPane.add(dest);
    
    writeDest = new JTextField(30);
    c.gridx++;
    gridBag.setConstraints(writeDest, c);
    contentPane.add(writeDest);

    JLabel cl = new JLabel("Command line ");
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(cl, c);
    contentPane.add(cl);
    
    commandLine = new JTextField(30);
    c.gridx++;
    gridBag.setConstraints(commandLine, c);
    contentPane.add(commandLine);

    JButton executeButton = new JButton("Execute");
    executeButton.addActionListener(new ExecuteButtonListener(this));
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(executeButton, c);
    contentPane.add(executeButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new CancelButtonListener(this));
    c.gridx++;
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
    return commandLine.getText();
  }
  
  public String getWriteDest() {
    return writeDest.getText();
  }
  
  public String getStepsPerWrite() {
    return stepsPerWrite.getText();
  }

  public int getWaitMillis() {
    return Integer.parseInt(sleepSeconds.getText().trim()) * 1000;
  }
  class ExecuteButtonListener implements ActionListener {
    PlannerCommandLineDialog dialog;
    public ExecuteButtonListener(PlannerCommandLineDialog dialog) {
      this.dialog = dialog;
    }
    public void actionPerformed(ActionEvent e) {
      if(!(new File(dialog.getWriteDest())).exists()) {
        //create directory dialog
      }
      if(!(new File(dialog.getWriteDest())).isDirectory()) {
        //execption dialog, then return to above
      }
      ExecutePlannerThread thread = new ExecutePlannerThread(dialog.getCommandLine(), 
                                                             dialog.getWriteDest(),
                                                             dialog.getStepsPerWrite());
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
      try {Thread.sleep(dialog.getWaitMillis());}
      catch(Exception ex){System.err.println(ex); System.exit(-1);}
      try {
        String url = getNewSequenceUrl(dialog.getWriteDest(),
                                       getModelName(dialog.getCommandLine()));
        System.err.println("Got " + url);
        PlanWorks.planWorks.currentProject.addPlanningSequence(url);
        MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.planWorks.getJMenuBar();
        int numProjects = PwProject.listProjects().size();
        JMenu planSeqMenu = dynamicMenuBar.clearMenu( PlanWorks.PLANSEQ_MENU, numProjects);
        PlanWorks.planWorks.addPlanSeqViewMenu(PlanWorks.planWorks.currentProject, planSeqMenu);
      }
      catch(Exception ex) {
        System.err.println("Odd.  Bailing out: " + ex);  
        System.exit(-1);
      }
      dialog.hide();
    }
    private String getModelName(String commandLine) {
      int suffixIndex = Math.max(commandLine.indexOf(".ddl"), commandLine.indexOf(".nddl"));
      int nameIndex = Math.max(commandLine.lastIndexOf("/", suffixIndex),
                               commandLine.lastIndexOf(" ", suffixIndex));
      return commandLine.substring(nameIndex + 1, suffixIndex);
    }
    private String getNewSequenceUrl(String writeDest, String modelName) {
      File dest = new File(writeDest);

      /****WEIRDLY SCOPED CLASS DEFINITION HERE****/
      class InternalFilter implements FilenameFilter {
        private String comp;
        public InternalFilter(String comp) {
          this.comp = comp;
        }
        public boolean accept(File dir, String name) {
          return name.indexOf(comp) != -1;
        }
      }
      /****END WEIRDLY SCOPED CLASS DEFINITION****/

      File [] candidates = dest.listFiles(new InternalFilter(modelName));
      Arrays.sort(candidates, new Comparator() {
          public int compare(Object o1, Object o2) {
            File f1 = (File) o1;
            File f2 = (File) o2;
            return (int)(f2.lastModified() - f1.lastModified());
          }
          public boolean equals(Object o){return false;}
        });
      return candidates[0].getAbsolutePath();
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
