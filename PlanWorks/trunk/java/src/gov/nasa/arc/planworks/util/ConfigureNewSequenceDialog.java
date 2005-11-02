// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConfigureNewSequenceDialog.java,v 1.13 2005-11-02 23:36:43 miatauro Exp $
//
package gov.nasa.arc.planworks.util;


import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.PlannerControlJNI;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


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
  private String plannerPath;
  private JTextField plannerPathField;
  private String modelName;
  private JTextField modelNameField;
  private String modelPath;
  private JTextField modelPathField;
  private String modelOutputDestDir;
  private JTextField modelOutputDestDirField;
  private String modelInitStatePath;
  private JTextField modelInitStatePathField;
  private String plannerConfigPath;
  private JTextField plannerConfigPathField;
    private String heuristicsPath;
    private JTextField heuristicsPathField;
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
    final JLabel plannerPathLabel = new JLabel( "planner library path");
    plannerPathField = new JTextField( PATH_FIELD_WIDTH);
    final JButton plannerPathBrowseButton = new JButton( browseTitle);
    plannerPathBrowseButton.addActionListener( new PlannerPathButtonListener());
//     final JLabel modelNameLabel = new JLabel( "model name");
//     modelNameField = new JTextField( NAME_FIELD_WIDTH);

    final JLabel modelPathLabel = new JLabel( "model library path");
    modelPathField = new JTextField( PATH_FIELD_WIDTH);
    final JButton modelPathBrowseButton = new JButton( browseTitle);
    modelPathBrowseButton.addActionListener( new ModelPathButtonListener());

    final JLabel modelOutputDestDirLabel = new JLabel( "model output destination directory");
    modelOutputDestDirField = new JTextField( PATH_FIELD_WIDTH);
    final JButton modelOutputDestDirBrowseButton = new JButton( browseTitle);
     modelOutputDestDirBrowseButton.addActionListener( new ModelOutputDestDirButtonListener());

    final JLabel modelInitStatePathLabel =  new JLabel( "model initial state path");
    modelInitStatePathField = new JTextField( PATH_FIELD_WIDTH);
    final JButton modelInitStatePathBrowseButton = new JButton( browseTitle);
    modelInitStatePathBrowseButton.addActionListener( new ModelInitStatePathButtonListener());

    final JLabel plannerConfigPathLabel = new JLabel("planner config path");
    plannerConfigPathField = new JTextField(PATH_FIELD_WIDTH);
    final JButton plannerConfigPathBrowseButton = new JButton(browseTitle);
    plannerConfigPathBrowseButton.addActionListener(new PlannerConfigPathButtonListener());

    final JLabel heuristicsPathLabel = new JLabel("heuristics path");
    heuristicsPathField = new JTextField(PATH_FIELD_WIDTH);
    final JButton heuristicsPathBrowseButton = new JButton(browseTitle);
    heuristicsPathBrowseButton.addActionListener(new HeuristicsPathButtonListener());

    // current values
    try {
      String currentProjectName = planWorks.getCurrentProjectName();
      plannerPath = new File( ConfigureAndPlugins.getProjectConfigValue
                              ( ConfigureAndPlugins.PROJECT_PLANNER_PATH,
                                currentProjectName)).getCanonicalPath();
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
      plannerConfigPath = new File(ConfigureAndPlugins.getProjectConfigValue
                                   (ConfigureAndPlugins.PROJECT_PLANNER_CONFIG_PATH,
                                    currentProjectName)).getCanonicalPath();
      heuristicsPath = (new File(ConfigureAndPlugins.getProjectConfigValue(ConfigureAndPlugins.PROJECT_HEURISTICS_PATH,
									   currentProjectName))).getCanonicalPath();
							   
    } catch (IOException ioExcep) {
    }

    plannerPathField.setText( plannerPath);
//     modelNameField.setText( modelName);
    modelPathField.setText( modelPath);
    modelOutputDestDirField.setText( modelOutputDestDir);
    modelInitStatePathField.setText( modelInitStatePath);
    plannerConfigPathField.setText(plannerConfigPath);
    heuristicsPathField.setText(heuristicsPath);

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

    c.gridy++;
    gridBag.setConstraints( plannerPathLabel, c);
    dialogPanel.add( plannerPathLabel);
    c.gridy++;
    gridBag.setConstraints( plannerPathField, c);
    dialogPanel.add( plannerPathField);
    c.gridx++;
    gridBag.setConstraints( plannerPathBrowseButton, c);
    dialogPanel.add( plannerPathBrowseButton);

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

    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(plannerConfigPathLabel, c);
    dialogPanel.add(plannerConfigPathLabel);
    c.gridy++;
    gridBag.setConstraints(plannerConfigPathField, c);
    dialogPanel.add(plannerConfigPathField);
    c.gridx++;
    gridBag.setConstraints(plannerConfigPathBrowseButton, c);
    dialogPanel.add(plannerConfigPathBrowseButton);

    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(heuristicsPathLabel, c);
    dialogPanel.add(heuristicsPathLabel);
    c.gridy++;
    gridBag.setConstraints(heuristicsPathField, c);
    dialogPanel.add(heuristicsPathField);
    c.gridx++;
    gridBag.setConstraints(heuristicsPathBrowseButton, c);
    dialogPanel.add(heuristicsPathBrowseButton);

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

  class PlannerPathButtonListener implements ActionListener {
    public PlannerPathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      FileChooser fileChooser = new FileChooser( "Select File", new File( plannerPath));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if (currentSelectedFile == null) {
        return;
      }
      plannerPathField.setText( currentSelectedFile);
    }
  } // end class PlannerPathButtonListener

  class ModelPathButtonListener implements ActionListener {
    public ModelPathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      FileChooser fileChooser = new FileChooser( "Select File", new File( modelPath));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if (currentSelectedFile == null) {
        return;
      }
      modelPathField.setText( currentSelectedFile);
    }
  } // end class ModelPathButtonListener

  class ModelInitStatePathButtonListener implements ActionListener {
    public ModelInitStatePathButtonListener() {
    }
    public void actionPerformed( ActionEvent ae) {
      FileChooser fileChooser = new FileChooser( "Select File",
                                                   new File( modelInitStatePath));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if (currentSelectedFile == null) {
        return;
      }
      modelInitStatePathField.setText( currentSelectedFile);
    }
  } // end class ModelInitStatePathButtonListener

  class ModelOutputDestDirButtonListener implements ActionListener {
    public ModelOutputDestDirButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      DirectoryChooser dirChooser =
        PlanWorks.getPlanWorks().createDirectoryChooser( new File( modelOutputDestDir));
      String currentSelectedDir = dirChooser.getValidSelectedDirectory();
      if (currentSelectedDir == null) {
        return;
      }
      modelOutputDestDirField.setText( currentSelectedDir);
    }
  } // end class ModelOutputDestDirButtonListener

  class PlannerConfigPathButtonListener implements ActionListener {
    public PlannerConfigPathButtonListener() {}
    public void actionPerformed(ActionEvent ae) {
      FileChooser fileChooser = new FileChooser("Select File", new File(plannerConfigPath));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if(currentSelectedFile == null)
        return;
      plannerConfigPathField.setText(currentSelectedFile);
    }
  }

    class HeuristicsPathButtonListener implements ActionListener {
	public HeuristicsPathButtonListener() {}
	public void actionPerformed(ActionEvent ae) {
	    FileChooser fileChooser = new FileChooser("Select File", new File(heuristicsPath));
	    String currentSelectedFile = fileChooser.getValidSelectedFile();
	    if(currentSelectedFile == null)
		return;
	    plannerConfigPathField.setText(currentSelectedFile);
	}
    }

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
              plannerPath = null;
              // modelName = null;
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
    
    String plannerPathTemp = plannerPathField.getText().trim();
    // if (! plannerPath.equals( plannerPathTemp)) {
    String plannerLibExtension;
    if (PlanWorks.isMacOSX()) {
      plannerLibExtension = ConfigureAndPlugins.MACOSX_PLANNER_LIB_NAME_MATCH;
    } else {
      plannerLibExtension = ConfigureAndPlugins.PLANNER_LIB_NAME_MATCH;
    }
    if (! doesPathExist( plannerPathTemp)) {
      haveSeenError = true;
    } else if (plannerPathTemp.indexOf( plannerLibExtension) == -1) {
      JOptionPane.showMessageDialog
        ( PlanWorks.getPlanWorks(),
          "Library name does not match 'lib<planner-name>" +
          plannerLibExtension + "'",
          "Invalid Planner Library", JOptionPane.ERROR_MESSAGE);
      haveSeenError = true;
    } else {
      plannerPath = plannerPathTemp;
    }
    // }
    
    //     modelName = modelNameField.getText().trim();
    
    String modelPathTemp = modelPathField.getText().trim();
    // if (! modelPath.equals( modelPathTemp)) {
    if (! doesPathExist( modelPathTemp)) {
      haveSeenError = true;
    } else {
      modelPath = modelPathTemp;
    }
    // }
    
    String modelInitStatePathTemp = modelInitStatePathField.getText().trim();
    // if (! modelInitStatePath.equals( modelInitStatePathTemp)) {
    if (! doesPathExist( modelInitStatePathTemp)) {
      haveSeenError = true;
    } else {
      modelInitStatePath = modelInitStatePathTemp;
    }
    // }
    
    String modelOutputDestDirTemp = modelOutputDestDirField.getText().trim();
    // if (! modelOutputDestDir.equals( modelOutputDestDirTemp)) {
    //if (! doesPathExist( modelOutputDestDirTemp)) {
    //haveSeenError = true;
    //} else {
    modelOutputDestDir = modelOutputDestDirTemp;
    // }
    // }
    
    String plannerConfigPathTemp = plannerConfigPathField.getText().trim();
    if(!doesPathExist(plannerConfigPathTemp))
      haveSeenError = true;
    else
      plannerConfigPath = plannerConfigPathTemp;

    String heuristicsPathTemp = heuristicsPathField.getText().trim();
    if(!doesPathExist(heuristicsPathTemp))
	haveSeenError = true;
    else
	heuristicsPath = heuristicsPathTemp;

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
   * <code>getPlannerPath</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPlannerPath() {
    return plannerPath;
  }

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

  public String getPlannerConfigPath() {
    return plannerConfigPath;
  }


    public String getHeuristicsPath() {
	return heuristicsPath;
    }

} // end class ConfigureNewSequenceDialog

 
