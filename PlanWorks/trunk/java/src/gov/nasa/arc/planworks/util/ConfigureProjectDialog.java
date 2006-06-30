// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ConfigureProjectDialog.java,v 1.17 2006-06-30 22:40:54 meboyce Exp $
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;


/**
 * <code>ConfigureProjectDialog</code> - create JOptionPane for user to enter project
 *                                       configure info for online mode.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
                    NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ConfigureProjectDialog extends JDialog {

  private static final int DELIMS_FIELD_WIDTH = 2;
  private static final int NAME_FIELD_WIDTH = 15;
  private static final int PATH_FIELD_WIDTH = 30;

    private static final String BROWSE = "browse ...";

  private JOptionPane optionPane;
  private String projectName;
  private JTextField projectNameField;
  private String workingDir;
  private JTextField workingDirField;
  private String plannerPath;
  private JTextField plannerPathField;
  private String modelName;
  private JTextField modelNameField;
  private String modelPath;
  private JTextField modelPathField;
  private String modelInitStatePath;
  private JTextField modelInitStatePathField;
  private String modelOutputDestDir;
  private JTextField modelOutputDestDirField;
  private String modelRuleDelimiters;
  private JTextField modelRuleDelimitersField;
  private String plannerConfigPath;
  private JTextField plannerConfigPathField;
    private String heuristicsPath;
    private JTextField heuristicsPathField;
    private String sourcePaths;
    private SourcePathPanel spp;

    private String btnString1;
    private String btnString2;

    // Used to control "default" settings on configure project dialog.
    // When locked, defaults are not updated. Locking occurs when user has specified
    // values in project configuration (press either <enter> or <browse>)
    private boolean plannerConfigurationLocked = false;

  /**
   * <code>ConfigureProjectDialog</code> - constructor 
   *
   * @param planWorks - <code>PlanWorks</code> - 
   */
  public ConfigureProjectDialog ( final PlanWorks planWorks) {
    // modal dialog - blocks other activity
    super( planWorks, true);
    setTitle( "Configure Project");

    projectNameField = new JTextField( NAME_FIELD_WIDTH);

    workingDirField = new JTextField( PATH_FIELD_WIDTH);


    plannerPathField = new JTextField( PATH_FIELD_WIDTH);

//     final JLabel modelNameLabel = new JLabel( "model name");
//     modelNameField = new JTextField( NAME_FIELD_WIDTH);

    modelPathField = new JTextField( PATH_FIELD_WIDTH);


    modelOutputDestDirField = new JTextField( PATH_FIELD_WIDTH);


    modelInitStatePathField = new JTextField( PATH_FIELD_WIDTH);



    plannerConfigPathField = new JTextField(PATH_FIELD_WIDTH);



    heuristicsPathField = new JTextField(PATH_FIELD_WIDTH);

    modelRuleDelimitersField = new JTextField( DELIMS_FIELD_WIDTH);
    // current values
    try {
      String currentProjectName = planWorks.getCurrentProjectName();
      projectName = currentProjectName;
      projectNameField.setText( currentProjectName);
      projectNameField.setEnabled( false);
      workingDir = new File( ConfigureAndPlugins.getProjectConfigValue
                             ( ConfigureAndPlugins.PROJECT_WORKING_DIR,
                               currentProjectName)).getCanonicalPath();
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
      plannerConfigPath = (new File(ConfigureAndPlugins.getProjectConfigValue
                                    (ConfigureAndPlugins.PROJECT_PLANNER_CONFIG_PATH,
                                     currentProjectName))).getCanonicalPath();
      heuristicsPath = (new File(ConfigureAndPlugins.getProjectConfigValue(ConfigureAndPlugins.PROJECT_HEURISTICS_PATH,
									   currentProjectName))).getCanonicalPath();
      modelRuleDelimiters = ConfigureAndPlugins.getProjectConfigValue
        ( ConfigureAndPlugins.PROJECT_MODEL_RULE_DELIMITERS, currentProjectName);
      
      sourcePaths = ConfigureAndPlugins.getProjectConfigValue(ConfigureAndPlugins.PROJECT_SOURCE_PATH, currentProjectName);
      System.err.println("Project configured sourcePaths value: " + sourcePaths);
    } catch (IOException ioExcep) {
    }
    workingDirField.setText( workingDir);
    plannerPathField.setText( plannerPath);
//     modelNameField.setText( modelName);
    modelPathField.setText( modelPath);
    modelOutputDestDirField.setText( modelOutputDestDir);
    modelInitStatePathField.setText( modelInitStatePath);
    plannerConfigPathField.setText(plannerConfigPath);
    heuristicsPathField.setText(heuristicsPath);
    modelRuleDelimitersField.setText( modelRuleDelimiters);
    propogateWorkingDirectoryIntoDefaults();

    btnString1 = "Enter";
    btnString2 = "Cancel";
    Object[] options = {btnString1, btnString2};

    //JPanel dialogPanel = new JPanel();
    JTabbedPane dialogPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);

    dialogPanel.add("Project", createProjectConfigTab());
    dialogPanel.add("Planner", createPlannerConfigTab());

    optionPane = new JOptionPane
      ( dialogPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION,
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
    setVisible( true);
  } // end constructor

  class WorkingDirButtonListener implements ActionListener {
    public WorkingDirButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      DirectoryChooser dirChooser =
	  PlanWorks.getPlanWorks().createDirectoryChooser( new File( workingDirField.getText()));
      String currentSelectedDir = dirChooser.getValidSelectedDirectory();
      if (currentSelectedDir == null) {
        return;
      }
      workingDirField.setText( currentSelectedDir);
      propogateWorkingDirectoryIntoDefaults();
    } // end actionPerformed

  } // end class WorkingDirButtonListener

  class PlannerPathButtonListener implements ActionListener {
    public PlannerPathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
	FileChooser fileChooser = new FileChooser( "Select File", new File( plannerPathField.getText() ));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if (currentSelectedFile == null) {
        return;
      }
      plannerPathField.setText( currentSelectedFile);
      plannerConfigurationLocked = true;
    }
  } // end class PlannerPathButtonListener

  class ModelPathButtonListener implements ActionListener {
    public ModelPathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
	FileChooser fileChooser = new FileChooser( "Select File", new File( modelPathField.getText()));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if (currentSelectedFile == null) {
        return;
      }
      modelPathField.setText( currentSelectedFile);
      plannerConfigurationLocked = true;
    }
  } // end class ModelPathButtonListener

  class ModelInitStatePathButtonListener implements ActionListener {
    public ModelInitStatePathButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
	FileChooser fileChooser = new FileChooser( "Select File", new File( modelInitStatePathField.getText() ));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if (currentSelectedFile == null) {
        return;
      }
      modelInitStatePathField.setText( currentSelectedFile);
      plannerConfigurationLocked = true;
    }
  } // end class ModelInitStatePathButtonListener


  class ModelOutputDestDirButtonListener implements ActionListener {
    public ModelOutputDestDirButtonListener() {
    }
    public void actionPerformed(ActionEvent ae) {
      DirectoryChooser dirChooser =
	  PlanWorks.getPlanWorks().createDirectoryChooser( new File( modelOutputDestDirField.getText() ));
      String currentSelectedDir = dirChooser.getValidSelectedDirectory();
      if (currentSelectedDir == null) {
        return;
      }
      modelOutputDestDirField.setText( currentSelectedDir );
      plannerConfigurationLocked = true;
    }
  } // end class ModelOutputDestDirButtonListener


  class PlannerConfigPathButtonListener implements ActionListener {
    public PlannerConfigPathButtonListener() {}
    public void actionPerformed(ActionEvent ae) {
	FileChooser fileChooser = new FileChooser("Select File", new File(plannerConfigPathField.getText()));
      String currentSelectedFile = fileChooser.getValidSelectedFile();
      if(currentSelectedFile == null)
        return;
      plannerConfigPathField.setText(currentSelectedFile);
      plannerConfigurationLocked = true;
    }
  }

    class HeuristicsPathButtonListener implements ActionListener {
	public HeuristicsPathButtonListener(){}
	public void actionPerformed(ActionEvent ae) {
	    FileChooser fileChooser = new FileChooser("Select File", new File(heuristicsPathField.getText()));
	    String currentSelectedFile = fileChooser.getValidSelectedFile();
	    if(currentSelectedFile == null)
		return;
	    heuristicsPathField.setText(currentSelectedFile);
            plannerConfigurationLocked = true;
	}
    }

    private JPanel createProjectConfigTab() {
	JPanel projectPanel = new JPanel();
	projectPanel.setLayout( new BoxLayout(projectPanel, BoxLayout.Y_AXIS));
	

	final JLabel projectNameLabel = new JLabel( "name");
	projectNameLabel.setMaximumSize(projectNameLabel.getPreferredSize());
	projectNameField.setMaximumSize(projectNameField.getPreferredSize());
	projectPanel.add(Box.createHorizontalGlue());
	projectPanel.add( projectNameLabel);
	projectPanel.add(Box.createHorizontalGlue());
	projectPanel.add( projectNameField);
	projectPanel.add(Box.createHorizontalGlue());
	
	final JLabel workingDirLabel =  new JLabel( "working directory");
	projectPanel.add( workingDirLabel);
	
	JPanel workingDirPanel = new JPanel();
	workingDirPanel.setLayout(new BoxLayout(workingDirPanel, BoxLayout.X_AXIS));
	workingDirPanel.add(workingDirField);
	final JButton workingDirBrowseButton = new JButton( BROWSE);
	workingDirBrowseButton.addActionListener( new WorkingDirButtonListener());
	workingDirPanel.add(workingDirBrowseButton);
	projectPanel.add( workingDirPanel);
	
	
	final JLabel modelOutputDestDirLabel = new JLabel( "model output destination directory");
	projectPanel.add( modelOutputDestDirLabel);
	JPanel outputDestPanel = new JPanel();
	outputDestPanel.setLayout(new BoxLayout(outputDestPanel, BoxLayout.X_AXIS));
	outputDestPanel.add( modelOutputDestDirField);
	final JButton modelOutputDestDirBrowseButton = new JButton( BROWSE);
	modelOutputDestDirBrowseButton.addActionListener( new ModelOutputDestDirButtonListener());
	outputDestPanel.add(modelOutputDestDirBrowseButton);
	projectPanel.add( outputDestPanel);
	
	final JLabel modelRuleDelimitersLabel= new JLabel( "model rule delimiters");
	projectPanel.add( modelRuleDelimitersLabel);
	projectPanel.add( modelRuleDelimitersField);

	spp = new SourcePathPanel(Arrays.asList(sourcePaths.split(":")));
	projectPanel.add(spp);
	
	
	return projectPanel;
    //end project config

    }

    private JPanel createPlannerConfigTab() {
	JPanel plannerPanel = new JPanel();
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	plannerPanel.setLayout(gridBag);

	c.weightx = 0;
	c.weighty = 0;
	c.gridx = 0;
	c.gridy = 0;

	//begin planner config
	final JLabel plannerPathLabel = new JLabel( "planner library file");
	gridBag.setConstraints( plannerPathLabel, c);
	plannerPanel.add( plannerPathLabel);
	c.gridy++;
	gridBag.setConstraints( plannerPathField, c);
	plannerPanel.add( plannerPathField);
	c.gridx++;
	final JButton plannerPathBrowseButton = new JButton( BROWSE);
	plannerPathBrowseButton.addActionListener( new PlannerPathButtonListener());
	gridBag.setConstraints( plannerPathBrowseButton, c);
	plannerPanel.add( plannerPathBrowseButton);

	//     c.gridx = 0;
	//     c.gridy++;
	//     gridBag.setConstraints( modelNameLabel, c);
	//     plannerPanel.add( modelNameLabel);
	//     c.gridy++;
	//     gridBag.setConstraints( modelNameField, c);
	//     plannerPanel.add( modelNameField);

	c.gridx = 0;
	c.gridy++;
	final JLabel modelPathLabel = new JLabel( "model library file");
	gridBag.setConstraints( modelPathLabel, c);
	plannerPanel.add( modelPathLabel);
	c.gridy++;
	gridBag.setConstraints( modelPathField, c);
	plannerPanel.add( modelPathField);
	c.gridx++;
	final JButton modelPathBrowseButton = new JButton( BROWSE);
	modelPathBrowseButton.addActionListener( new ModelPathButtonListener());
	gridBag.setConstraints( modelPathBrowseButton, c);
	plannerPanel.add( modelPathBrowseButton);

	c.gridx = 0;   
	c.gridy++;
	final JLabel modelInitStatePathLabel =  new JLabel( "model initial state file");
	gridBag.setConstraints( modelInitStatePathLabel, c);
	plannerPanel.add( modelInitStatePathLabel);
	c.gridy++;
	gridBag.setConstraints( modelInitStatePathField, c);
	plannerPanel.add( modelInitStatePathField);
	c.gridx++;
	final JButton modelInitStatePathBrowseButton = new JButton( BROWSE);
	modelInitStatePathBrowseButton.addActionListener( new ModelInitStatePathButtonListener());
	gridBag.setConstraints( modelInitStatePathBrowseButton, c);
	plannerPanel.add( modelInitStatePathBrowseButton);


	c.gridx = 0;
	c.gridy++;
	final JLabel plannerConfigPathLabel = new JLabel("planner config file");
	gridBag.setConstraints(plannerConfigPathLabel, c);
	plannerPanel.add(plannerConfigPathLabel);
	c.gridy++;
	gridBag.setConstraints(plannerConfigPathField, c);
	plannerPanel.add(plannerConfigPathField);
	c.gridx++;
	final JButton plannerConfigPathBrowseButton = new JButton(BROWSE);
	plannerConfigPathBrowseButton.addActionListener(new PlannerConfigPathButtonListener());
	gridBag.setConstraints(plannerConfigPathBrowseButton, c);
	plannerPanel.add(plannerConfigPathBrowseButton);

	c.gridx = 0;
	c.gridy++;
	final JLabel heuristicsPathLabel = new JLabel("heuristics file");
	gridBag.setConstraints(heuristicsPathLabel, c);
	plannerPanel.add(heuristicsPathLabel);
	c.gridy++;
	gridBag.setConstraints(heuristicsPathField, c);
	plannerPanel.add(heuristicsPathField);
	c.gridx++;
	final JButton heuristicsPathBrowseButton = new JButton(BROWSE);
	heuristicsPathBrowseButton.addActionListener(new HeuristicsPathButtonListener());

	gridBag.setConstraints(heuristicsPathBrowseButton, c);
	plannerPanel.add(heuristicsPathBrowseButton);
	return plannerPanel;
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
              projectName = null; workingDir = null;
              plannerPath = null; // modelName = null;
              modelPath = null; modelOutputDestDir = null;
              modelInitStatePath = null; modelRuleDelimiters = null;
	      heuristicsPath = null;
	      sourcePaths = null;
	      System.err.println("Setting sourcePaths = null");
              setVisible( false);
            }
          }
        }
      });
  } // end addInputListener

    public void propogateWorkingDirectoryIntoDefaults() {
	if (! plannerConfigurationLocked ) {
	    System.err.println("Propergating working directory information to other project settings defaults");
         
            String workingDirWithoutPath;
            if (workingDir.lastIndexOf("plans") != -1) {
               workingDirWithoutPath = workingDir.substring(0, workingDir.lastIndexOf("plans"));
            } else {
		workingDirWithoutPath = workingDir;
            }
            plannerPath = workingDirWithoutPath;
            plannerPathField.setText( plannerPath );
            modelPath = workingDirWithoutPath;
            modelPathField.setText( modelPath );
	    modelInitStatePath = workingDirWithoutPath;
            modelInitStatePathField.setText( modelInitStatePath );
            plannerConfigPath = workingDirWithoutPath;
            plannerConfigPathField.setText(plannerConfigPath);
            heuristicsPath = workingDirWithoutPath;
            heuristicsPathField.setText(heuristicsPath);

        }
    }

  private boolean handleTextFieldValues() {
    boolean haveSeenError = false;
    String workingDirTemp = workingDirField.getText().trim();
    //    if (! workingDir.equals( workingDirTemp)) {
      if (! doesPathExist( workingDirTemp)) {
        haveSeenError = true;
      } else {
        workingDir = workingDirTemp;
      }
      //}

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
//    if (! doesPathExist( modelOutputDestDirTemp)) {
//      haveSeenError = true;
//    } else {
//      modelOutputDestDir = modelOutputDestDirTemp;
//    }
    modelOutputDestDir = modelOutputDestDirTemp;

    String modelRuleDelimitersTemp = modelRuleDelimitersField.getText().trim();
    // if (! modelRuleDelimiters.equals( modelRuleDelimitersTemp)) {
      if (modelRuleDelimitersTemp.length() != 2) {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(), "Not exactly two characters",
            "Invalid Delimiters", JOptionPane.ERROR_MESSAGE);
        haveSeenError = true;
      } else {
        modelRuleDelimiters = modelRuleDelimitersTemp;
      }
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

      List paths = spp.getPaths();
      StringBuffer pathBuf = new StringBuffer(":");
      for(Iterator it = paths.iterator(); it.hasNext();)
	  pathBuf.append((String)it.next()).append(":");

      sourcePaths = pathBuf.toString();
      System.err.println("Set sourcePaths to " + sourcePaths);

    return haveSeenError;
  } // end handleTextFieldValues

  private boolean doesPathExist( String path) {
    boolean doesExist = true;
    if (! (new File( path)).exists()) {
      doesExist = false;
      JOptionPane.showMessageDialog
        ( PlanWorks.getPlanWorks(), "Path does not exist: '" + path + "'",
         "Invalid Path", JOptionPane.ERROR_MESSAGE);
    }
    return doesExist;
  } // end doesPathExist

  /**
   * <code>getProjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getProjectName() {
    return projectName;
  }

  /**
   * <code>getPlannerPath</code>
   *
   * @return - <code>String</code> - 
   */
  public String getPlannerPath() {
    return plannerPath;
  }

  public String getWorkingDir() {
    return workingDir;
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

  public String getModelRuleDelimiters() {
    return modelRuleDelimiters;
  }

  public String getPlannerConfigPath() {
    return plannerConfigPath;
  }

    public String getHeuristicsPath() {
	return heuristicsPath;
    }

    public String getSourcePaths() {
	return sourcePaths;
    }

} // end class ConfigureProjectDialog

 
