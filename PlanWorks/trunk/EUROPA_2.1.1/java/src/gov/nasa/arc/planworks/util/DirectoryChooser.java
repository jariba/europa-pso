// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DirectoryChooser.java,v 1.7 2004-09-24 23:04:16 taylor Exp $
//
//
// Will Taylor -- started 26mar03
//

package gov.nasa.arc.planworks.util;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.PlanWorks;


/**
 * <code>DirectoryChooser</code> - these class instances are utilized by
 *                                 dirChooser.showDialog( PlanWorks.planWorks, "OK");
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DirectoryChooser extends JFileChooser {

  /**
   * <code>DirectoryChooser</code> - constructor 
   *
   * @param title - <code>String</code> - 
   * @param isMultiSelectionEnabled - <code>boolean</code> - 
   */
  public DirectoryChooser( String title, boolean isMultiSelectionEnabled) {
    super();
    commonConstructor( title, isMultiSelectionEnabled);
  }
    
  public DirectoryChooser( String title, boolean isMultiSelectionEnabled,
                           boolean isParentCurrentDir, File currentDirectory) {
    super( currentDirectory);
    if (isParentCurrentDir) {
      this.setCurrentDirectory( currentDirectory.getParentFile());
    }
    commonConstructor( title, isMultiSelectionEnabled);
  }

  private void commonConstructor( String title, boolean isMultiSelectionEnabled) {
    setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES);
    setDialogTitle( title);
    setMultiSelectionEnabled( isMultiSelectionEnabled);
  } // end commonConstructor


  /**
   * <code>getValidSelectedDirectory</code> - For setMultiSelectionEnabled( false)
   *
   * @return - <code>String</code> - 
   */
  public String getValidSelectedDirectory() {
    if (isMultiSelectionEnabled()) {
      JOptionPane.showMessageDialog
        ( PlanWorks.getPlanWorks(), "MultiSelection is not handled -- returning null",
          "No Directory Selected", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    boolean isValid =  false;
    while (! isValid) {
      int returnVal = this.showDialog( PlanWorks.getPlanWorks(), "OK");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File selectedFile = this.getSelectedFile();
        // System.err.println( "selectedFile " + selectedFile);
        if ((selectedFile != null) && selectedFile.isDirectory()) {
          this.approveSelection();
          isValid = true;
        } else {
          String selectedFileStr = this.getCurrentDirectory().getAbsolutePath() +
            System.getProperty( "file.separator") + "<null>";
          if (selectedFile != null) {
            selectedFileStr = this.getSelectedFile().getAbsolutePath();
          }
          JOptionPane.showMessageDialog
            ( PlanWorks.getPlanWorks(), "`" + selectedFileStr +
              "'\nis not a valid directory.",
              "No Directory Selected", JOptionPane.ERROR_MESSAGE);
          this.setCurrentDirectory( this.getCurrentDirectory());
        }
      } else {
        return null; // user selected cancel
      }
    }
    return this.getSelectedFile().getAbsolutePath();
  } // end getValidSelectedDirectory

} // end class DirectoryChooser
