// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FileChooser.java,v 1.1 2004-09-24 22:39:59 taylor Exp $
//
//
// Will Taylor -- started 24sep04
//

package gov.nasa.arc.planworks.util;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import gov.nasa.arc.planworks.PlanWorks;


/**
 * <code>FileChooser</code> - these class instances are utilized by
 *                                 fileChooser.showDialog( PlanWorks.planWorks, "OK");
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class FileChooser extends JFileChooser {

  private File currentDirectory;

  /**
   * <code>FileChooser</code> - constructor 
   *
   * @param title - <code>String</code> - 
   */
  public FileChooser( String title) {
    super();
    this.currentDirectory = null;
    commonConstructor( title);
  }
    
  /**
   * <code>FileChooser</code> - constructor 
   *
   * @param title - <code>String</code> - 
   * @param currentDirectory - <code>File</code> - 
   */
  public FileChooser( String title, File currentDirectory) {
    super( currentDirectory);
    this.currentDirectory = currentDirectory;
    commonConstructor( title);
  }

  private void commonConstructor( String title) {
    setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES);
    setMultiSelectionEnabled( false);
    setDialogTitle( title);
    setApproveButtonToolTipText( "Accept selected file");
  } // end commonConstructor


  /**
   * <code>getValidSelectedFile</code> -
   *
   * @return - <code>String</code> - 
   */
  public String getValidSelectedFile() {
    boolean isValid =  false;
    while (! isValid) {
      int returnVal = this.showDialog( PlanWorks.getPlanWorks(), "OK");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File selectedFile = this.getSelectedFile();
        // System.err.println( "selectedFile " + selectedFile);
        if ((selectedFile != null) && selectedFile.isFile()) {
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
              "'\nis not a valid file.",
              "No File Selected", JOptionPane.ERROR_MESSAGE);
          this.setCurrentDirectory( this.getCurrentDirectory());
        }
      } else {
        return null; // user selected cancel
      }
    }
    return this.getSelectedFile().getAbsolutePath();
  } // end getValidSelectedFile

} // end class FileChooser
