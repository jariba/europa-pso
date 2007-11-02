// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: BrowseButton.java,v 1.5 2004-09-24 22:39:58 taylor Exp $
//
package gov.nasa.arc.planworks.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.util.DirectoryChooser;

public class BrowseButton extends JButton {
  protected JTextField dest;
  public BrowseButton(JTextField dest) {
    super("Browse ...");
    this.dest = dest;
    addActionListener(new BrowseButtonListener(this));
  }
  public BrowseButton(final JTextField dest, final boolean multiSelect, final int chooserMode, 
                      final String chooserTitle) 
    throws IllegalArgumentException {
    super("Browse ...");
    this.dest = dest;
    addActionListener(new BrowseButtonListener(this, multiSelect, chooserMode, chooserTitle));
  }
  class BrowseButtonListener implements ActionListener {
    private BrowseButton button;
    boolean multiSelect;
    int chooserMode;
    String chooserTitle;
    public BrowseButtonListener(final BrowseButton button) {
      this.button = button;
      multiSelect = true;
      chooserMode = JFileChooser.FILES_AND_DIRECTORIES;
    }
    public BrowseButtonListener(final BrowseButton button, final boolean multiSelect, 
                                final int chooserMode, final String chooserTitle) {
      this(button);
      this.multiSelect = multiSelect;
      this.chooserMode = chooserMode;
      this.chooserTitle = chooserTitle;
    }
    public void actionPerformed(ActionEvent e) {
      DirectoryChooser chooser = new DirectoryChooser( chooserTitle, multiSelect);
      chooser.setFileSelectionMode(chooserMode);
      int returnVal = chooser.showOpenDialog(PlanWorks.getPlanWorks());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        chooser.approveSelection();
        button.dest.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    }
  }
}
