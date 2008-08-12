// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ProjectNameDialog.java,v 1.9 2006-06-30 22:40:54 meboyce Exp $
//
package gov.nasa.arc.planworks.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;


/**
 * <code>ProjectNameDialog</code> - create JOptionPane for user to enter project name, &
 *                                  working directory for CreateProject
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ProjectNameDialog extends JDialog {

  private static final int NAME_FIELD_WIDTH = 15;
  private static final int WORKING_DIR_FIELD_WIDTH = 30;

  private JOptionPane optionPane;
  private String projectName;
  private JTextField projectNameField;
  private String workingDir;
  private JTextField workingDirField;

  private String btnString1;
  private String btnString2;

  /**
   * <code>ProjectNameDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   */
  public ProjectNameDialog ( final PlanWorks planWorks) {
    // modal dialog - blocks other activity
    super( planWorks, true);
    setTitle( "Create Project");
    final JLabel projectNameLabel = new JLabel( "name");
    projectNameField = new JTextField( NAME_FIELD_WIDTH);
    final JLabel workingDirLabel =  new JLabel( "working directory");
    workingDirField = new JTextField( WORKING_DIR_FIELD_WIDTH);
    String workingDirBrowseLabel = "browse ...";
    // current values
    try {
      String currentProjectName = planWorks.getCurrentProjectName();
      projectNameField.setText( currentProjectName);
      projectNameField.setSelectionStart( 0);
      projectNameField.setSelectionEnd( currentProjectName.length());
      workingDir = new File( ConfigureAndPlugins.getProjectConfigValue
                             ( ConfigureAndPlugins.PROJECT_WORKING_DIR,
                               ConfigureAndPlugins.DEFAULT_PROJECT_NAME)).getCanonicalPath();
    } catch (IOException ioExcep) {
    }
    workingDirField.setText( workingDir);
    final JButton workingDirBrowseButton = new JButton( workingDirBrowseLabel);
    btnString1 = "Enter";
    btnString2 = "Cancel";
    Object[] options = {btnString1, btnString2};

    JPanel dialogPanel = new JPanel();
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    dialogPanel.setLayout( gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;

    c.gridy++;
    gridBag.setConstraints( projectNameLabel, c);
    dialogPanel.add( projectNameLabel);

    c.gridy++;
    gridBag.setConstraints( projectNameField, c);
    dialogPanel.add( projectNameField);

    c.gridy++;
    gridBag.setConstraints( workingDirLabel, c);
    dialogPanel.add( workingDirLabel);

    c.gridy++;
    gridBag.setConstraints( workingDirField, c);
    dialogPanel.add( workingDirField);

    c.gridx++;
    gridBag.setConstraints( workingDirBrowseButton, c);
    dialogPanel.add( workingDirBrowseButton);

    optionPane = new JOptionPane
      ( dialogPanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
        null, options, options[0]);
    setContentPane( optionPane);
    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE);
    addWindowListener( new WindowAdapter() {
        public final void windowClosing(final WindowEvent we) {
          /*
           * Instead of directly closing the window,
           * we're going to change the JOptionPane's
           * value property.
           */
          optionPane.setValue( new Integer( JOptionPane.CLOSED_OPTION));
        }
      });

    projectNameField.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          optionPane.setValue( btnString1);
        }
      });

    workingDirBrowseButton.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          DirectoryChooser dirChooser =
            PlanWorks.getPlanWorks().createDirectoryChooser( new File( workingDir));

          String currentSelectedDir = dirChooser.getValidSelectedDirectory();
          if (currentSelectedDir == null) {
            return;
          }
          workingDirField.setText( currentSelectedDir);
        }
      });

    addInputListener();

    // size dialog appropriately
    pack();
    // place it in center of JFrame
    Utilities.setPopupLocation( this, PlanWorks.getPlanWorks());
    setVisible( true);
  } // end constructor


  private void addInputListener() {
    optionPane.addPropertyChangeListener( new PropertyChangeListener() {
        public final void propertyChange( final PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (isVisible() && (e.getSource() == optionPane) &&
              (prop.equals( JOptionPane.VALUE_PROPERTY) ||
               prop.equals( JOptionPane.INPUT_VALUE_PROPERTY))) {
            Object value = optionPane.getValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE) {
              //ignore reset
              return;
            }
            // Reset the JOptionPane's value.
            // If you don't do this, then if the user
            // presses the same button next time, no
            // property change event will be fired.
            optionPane.setValue( JOptionPane.UNINITIALIZED_VALUE);

            if (value.equals( btnString1)) {
              boolean haveSeenError = false;
              projectName = projectNameField.getText();
              if (projectName.equals( ConfigureAndPlugins.DEFAULT_PROJECT_NAME)) {
                JOptionPane.showMessageDialog
                  ( PlanWorks.getPlanWorks(),
                   "Choose another project name, other than '" +
                   ConfigureAndPlugins.DEFAULT_PROJECT_NAME + "'",
                   "Invalid Name", JOptionPane.ERROR_MESSAGE);
                haveSeenError = true;
              }
              String workingDirTemp = workingDirField.getText().trim();
              if (! workingDir.equals( workingDirTemp)) {
                if (! (new File( workingDirTemp)).exists()) {
                  JOptionPane.showMessageDialog
                    ( PlanWorks.getPlanWorks(),
                      "Path does not exist: '" + workingDirTemp + "'",
                      "Invalid Path", JOptionPane.ERROR_MESSAGE);
                  haveSeenError = true;
                } else {
                  workingDir = workingDirTemp;
                }
              }
              if (haveSeenError) {
                return;
              }
              // we're done; dismiss the dialog
              setVisible( false);
            } else { // user closed dialog or clicked cancel
              projectName = null;
              workingDir = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

  /**
   * <code>getProjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getProjectName() {
    return projectName;
  }

  /**
   * <code>getWorkingDir</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getWorkingDir() {
    return workingDir;
  }

} // end class ProjectNameDialog

 
