// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConfigureNewSequenceDialog.java,v 1.3 2004-09-10 20:02:32 taylor Exp $
//
package gov.nasa.arc.planworks.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
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
 * <code>ConfigureNewSequenceDialog</code> - create JOptionPane for user to enter new sequence
 *                                       configure info for online mode.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConfigureNewSequenceDialog extends JDialog {

  private static final int NAME_FIELD_WIDTH = 15;
  private static final int PATH_FIELD_WIDTH = 30;

  private JOptionPane optionPane;
  private String modelName;
  private JTextField modelNameField;
  private String modelPath;
  private JTextField modelPathField;
  private String modelOutputDestDir;
  private JTextField modelOutputDestDirField;
  private String modelInitStatePath;
  private JTextField modelInitStatePathField;

  private String btnString1;
  private String btnString2;


  /**
   * <code>ConfigureNewSequenceDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   */
  public ConfigureNewSequenceDialog ( final PlanWorks planWorks) {
    // modal dialog - blocks other activity
    super( planWorks, true);
    setTitle( "Create New Sequence");
    String browseTitle = "browse ...";
//     final JLabel modelNameLabel = new JLabel( "model name");
//     modelNameField = new JTextField( NAME_FIELD_WIDTH);

    final JLabel modelPathLabel = new JLabel( "model path");
    modelPathField = new JTextField( PATH_FIELD_WIDTH);
    final JButton modelPathBrowseButton = new JButton( browseTitle);
    modelPathBrowseButton.addActionListener( new ModelPathButtonListener());

    final JLabel modelOutputDestDirLabel = new JLabel( "model output destination directory");
    modelOutputDestDirField = new JTextField( PATH_FIELD_WIDTH);
    final JButton modelOutputDestDirBrowseButton = new JButton( browseTitle);
     modelOutputDestDirBrowseButton.addActionListener( new ModelOutputDestDirButtonListener());

    final JLabel modelInitStatePathLabel =  new JLabel( "model init state path");
    modelInitStatePathField = new JTextField( PATH_FIELD_WIDTH);
    final JButton modelInitStatePathBrowseButton = new JButton( browseTitle);
    modelInitStatePathBrowseButton.addActionListener( new ModelInitStatePathButtonListener());
    // current values
    try {
      String currentProjectName = planWorks.getCurrentProjectName();
//       modelName = ConfigureAndPlugins.getProjectConfigValue
//         ( ConfigureAndPlugins.PROJECT_MODEL_NAME, currentProjectName);
      modelPath = new File( ConfigureAndPlugins.getProjectConfigValue
                            ( ConfigureAndPlugins.PROJECT_MODEL_PATH,
                              currentProjectName)).getCanonicalPath();
      modelOutputDestDir = new File( ConfigureAndPlugins.getProjectConfigValue
                                     ( ConfigureAndPlugins.PROJECT_MODEL_OUTPUT_DEST_DIR,
                                       currentProjectName)).getCanonicalPath();
      modelInitStatePath = new File( ConfigureAndPlugins.getProjectConfigValue
                                     ( ConfigureAndPlugins.PROJECT_MODEL_INIT_STATE_PATH,
                                       currentProjectName)).getCanonicalPath();
    } catch (IOException ioExcep) {
    }
//     modelNameField.setText( modelName);
    modelPathField.setText( modelPath);
    modelOutputDestDirField.setText( modelOutputDestDir);
    modelInitStatePathField.setText( modelInitStatePath);
    btnString1 = "Start";
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

//     c.gridy++;
//     gridBag.setConstraints( modelNameLabel, c);
//     dialogPanel.add( modelNameLabel);
//     c.gridy++;
//     gridBag.setConstraints( modelNameField, c);
//     dialogPanel.add( modelNameField);

    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints( modelPathLabel, c);
    dialogPanel.add( modelPathLabel);
    c.gridy++;
    gridBag.setConstraints( modelPathField, c);
    dialogPanel.add( modelPathField);
    c.gridx++;
    gridBag.setConstraints( modelPathBrowseButton, c);
    dialogPanel.add( modelPathBrowseButton);

    c.gridx = 0;   
    c.gridy++;
    gridBag.setConstraints( modelInitStatePathLabel, c);
    dialogPanel.add( modelInitStatePathLabel);
    c.gridy++;
    gridBag.setConstraints( modelInitStatePathField, c);
    dialogPanel.add( modelInitStatePathField);
    c.gridx++;
    gridBag.setConstraints( modelInitStatePathBrowseButton, c);
    dialogPanel.add( modelInitStatePathBrowseButton);

    c.gridx = 0;  
    c.gridy++;
    gridBag.setConstraints( modelOutputDestDirLabel, c);
    dialogPanel.add( modelOutputDestDirLabel);
    c.gridy++;
    gridBag.setConstraints( modelOutputDestDirField, c);
    dialogPanel.add( modelOutputDestDirField);
    c.gridx++;
    gridBag.setConstraints( modelOutputDestDirBrowseButton, c);
    dialogPanel.add( modelOutputDestDirBrowseButton);

    optionPane = new JOptionPane
      ( dialogPanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
        null, options, options[0]);
    setContentPane( optionPane);
    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE);
    addWindowListener( new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          /*
           * Instead of directly closing the window,
           * we're going to change the JOptionPane's
           * value property.
           */
          optionPane.setValue( new Integer( JOptionPane.CLOSED_OPTION));
        }
      });

//     projectNameField.addActionListener( new ActionListener() {
//         public void actionPerformed( ActionEvent e) {
//           optionPane.setValue( btnString1);
//         }
//       });

    addInputListener();

    // size dialog appropriately
    pack();
    // place it in center of JFrame
    Utilities.setPopupLocation( this, PlanWorks.getPlanWorks());
    setBackground( ColorMap.getColor( "gray60"));
    setVisible( true);
  } // end constructor

  class ModelPathButtonListener implements ActionListener {
    public ModelPathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      JFileChooser fileChooser = new JFileChooser( new File( modelPath));
      fileChooser.setDialogTitle( "Select File");
      fileChooser.setMultiSelectionEnabled( false);
      fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES);
      int retval = fileChooser.showOpenDialog( PlanWorks.getPlanWorks());
      if (retval == JFileChooser.APPROVE_OPTION) {
        String currentSelectedFile = fileChooser.getSelectedFile().getAbsolutePath();
        modelPathField.setText( currentSelectedFile);
      }
    }
  } // end class ModelPathButtonListener

  class ModelOutputDestDirButtonListener implements ActionListener {
    public ModelOutputDestDirButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      DirectoryChooser dirChooser =
        PlanWorks.getPlanWorks().createDirectoryChooser( new File( modelOutputDestDir));
      int returnVal = dirChooser.showDialog( PlanWorks.getPlanWorks(), "");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String currentSelectedDir = dirChooser.getCurrentDirectory().getAbsolutePath();
        modelOutputDestDirField.setText( currentSelectedDir);
      }
    }
  } // end class ModelOutputDestDirButtonListener

  class ModelInitStatePathButtonListener implements ActionListener {
    public ModelInitStatePathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      JFileChooser fileChooser = new JFileChooser( new File( modelInitStatePath));
      fileChooser.setDialogTitle( "Select File");
      fileChooser.setMultiSelectionEnabled( false);
      fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES);
      int retval = fileChooser.showOpenDialog( PlanWorks.getPlanWorks());
      if (retval == JFileChooser.APPROVE_OPTION) {
        String currentSelectedFile = fileChooser.getSelectedFile().getAbsolutePath();
        modelInitStatePathField.setText( currentSelectedFile);
      }
    }
  } // end class ModelInitStatePathButtonListener


  private void addInputListener() {
    optionPane.addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange( PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (isVisible() && (e.getSource() == optionPane) &&
              (prop.equals(JOptionPane.VALUE_PROPERTY) ||
               prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
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
              if (handleTextFieldValues()) {
                return;
              }
              // we're done; dismiss the dialog
              setVisible( false);
            } else { // user closed dialog or clicked cancel
              modelName = null;
              modelPath = null;
              modelOutputDestDir = null;
              modelInitStatePath = null;
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

  private boolean handleTextFieldValues() {
    boolean haveSeenError = false;
//     modelName = modelNameField.getText().trim();

    String modelPathTemp = modelPathField.getText().trim();
    if (! modelPath.equals( modelPathTemp)) {
      if (! doesPathExist( modelPathTemp)) {
        haveSeenError = true;
      } else {
        modelPath = modelPathTemp;
      }
    }

    String modelInitStatePathTemp = modelInitStatePathField.getText().trim();
    if (! modelInitStatePath.equals( modelInitStatePathTemp)) {
      if (! doesPathExist( modelInitStatePathTemp)) {
        haveSeenError = true;
      } else {
        modelInitStatePath = modelInitStatePathTemp;
      }
    }

    String modelOutputDestDirTemp = modelOutputDestDirField.getText().trim();
    if (! modelOutputDestDir.equals( modelOutputDestDirTemp)) {
      if (! doesPathExist( modelOutputDestDirTemp)) {
        haveSeenError = true;
      } else {
        modelOutputDestDir = modelOutputDestDirTemp;
      }
    }

    return haveSeenError;
  } // end handleTextFieldValues

  private boolean doesPathExist( String path) {
    boolean doesExist = true;
    if (! (new File( path)).exists()) {
      doesExist = false;
      JOptionPane.showMessageDialog
        (PlanWorks.getPlanWorks(), "Path does not exist: '" + path + "'",
         "Invalid Path", JOptionPane.ERROR_MESSAGE);
    }
    return doesExist;
  } // end doesPathExist

  /**
   * <code>getModelName</code>
   *
   * @return - <code>String</code> - 
   */
//   public String getModelName() {
//     return modelName;
//   }

  /**
   * <code>getModelPath</code>
   *
   * @return - <code>String</code> - 
   */
  public String getModelPath() {
    return modelPath;
  }

  /**
   * <code>getModelOutputDestDir</code>
   *
   * @return - <code>String</code> - 
   */
  public String getModelOutputDestDir() {
    return modelOutputDestDir;
  }

  /**
   * <code>getModelInitStatePath</code>
   *
   * @return - <code>String</code> - 
   */
  public String getModelInitStatePath() {
    return modelInitStatePath;
  }

} // end class ConfigureNewSequenceDialog

 
