// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DirectoryChooser.java,v 1.4 2004-02-03 19:23:55 miatauro Exp $
//
//
// Will Taylor -- started 26mar03
//

package gov.nasa.arc.planworks.util;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

/**
 * <code>DirectoryChooser</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class DirectoryChooser extends JFileChooser {

  private JButton okButton;

  /**
   * <code>DirectoryChooser</code> - constructor 
   *
   */
  public DirectoryChooser() {
    super();
    okButton = new JButton( "OK");
    JButton canButton = new JButton( "Cancel");
    JPanel buttonPanel = new JPanel( new GridLayout( 2, 1));
    // with this, files do not show up
    // setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
    // with this, files can be selected
    // setFileSelectionMode( JFileChooser.FILES_ONLY);
    setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES);
    setDialogType( JFileChooser.CUSTOM_DIALOG);
    setAccessory( buttonPanel);
    buttonPanel.add( okButton);
    buttonPanel.add( canButton);
    setControlButtonsAreShown( false);

    canButton.addActionListener( new ActionListener()
      {
        public void actionPerformed( ActionEvent e) {
          cancelSelection();
        }
      });
  } // end constructor

  public DirectoryChooser(final int mode) throws IllegalArgumentException {
    this();
    if(mode != JFileChooser.DIRECTORIES_ONLY && mode != JFileChooser.FILES_ONLY && 
       mode != JFileChooser.FILES_AND_DIRECTORIES) {
      throw new IllegalArgumentException();
    }
  }

    /**
     * <code>getOkButton</code>
     *
     * @return okButton - <code>JButton</code> - 
     */
  public JButton getOkButton() {
    return okButton;
  }


} // end class DirectoryChooser
