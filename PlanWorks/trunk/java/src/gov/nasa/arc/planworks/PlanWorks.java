// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.46 2003-09-10 00:23:09 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.SplashWindow;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.DirectoryChooser;
import gov.nasa.arc.planworks.util.ProjectNameDialog;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>PlanWorks</code> - top-level application class, invoked from Ant  target
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0 -- started 02jun03
 */
public class PlanWorks extends MDIDesktopFrame {

  private static final int DESKTOP_FRAME_WIDTH = 900;
  private static final int DESKTOP_FRAME_HEIGHT = 850;
  private static final int FRAME_X_LOCATION = 100;
  private static final int FRAME_Y_LOCATION = 125;
  private static final int INTERNAL_FRAME_X_DELTA = 100;
  private static final int INTERNAL_FRAME_Y_DELTA = 75;
  private static final String PLANSEQ_MENU = "Planning Sequence";
  private static final String CREATE_MENU = "Create ...";
  private static final String ADDSEQ_MENU = "Add Sequence ...";
  private static final String DELSEQ_MENU = "Delete Sequence ...";
  private static final String OPEN_MENU = "Open ...";
  private static final String DELETE_MENU = "Delete ...";
  private static final String PROJECT_MENU = "Project";
  private static final String CREATE = "create";
  private static final String OPEN = "open";

  /**
   * constant <code>INTERNAL_FRAME_WIDTH</code>
   *
   */
  public static final int INTERNAL_FRAME_WIDTH = 400;

  /**
   * constant <code>INTERNAL_FRAME_HEIGHT</code>
   *
   */
  public static final int INTERNAL_FRAME_HEIGHT = 350;
  /**
   * variable <code>name</code> - make it accessible to JFCUnit tests
   *
   */
  public static String name;

  /**
   * variable <code>osType</code> - make it accessible to JFCUnit tests
   *
   */
  public static String osType;

  /**
   * variable <code>planWorksRoot</code> - make it accessible to JFCUnit tests
   *
   */
  public static String planWorksRoot;

  /**
   * variable <code>planWorks</code> - make it accessible to JFCUnit tests
   *
   */
  public static PlanWorks planWorks;

  private static JMenu projectMenu;
  private final DirectoryChooser sequenceDirChooser;
  private static String sequenceParentDirectory; // pathname
  private static File [] sequenceDirectories; // directory name

  private String currentProjectName;
  private PwProject currentProject;
  private ViewManager viewManager;
  private Map sequenceNameMap;
  private static boolean windowBuilt = false;
  private static boolean usingSplash;

  static {
    String imagePath = null;
    if((imagePath = System.getProperty("splash.image")) != null) {
      usingSplash = true;
      Image splashImage = Toolkit.getDefaultToolkit().createImage(imagePath);
      SplashWindow.splash(splashImage);
    }
    else {
      usingSplash = false;
    }
  }
  
  synchronized public static boolean isWindowBuilt() { return windowBuilt;}

  /**
   * <code>PlanWorks</code> - constructor 
   *
   * @param constantMenus - <code>JMenu[]</code> -
   */                                
  public PlanWorks( JMenu[] constantMenus) {
    super( name, constantMenus);
    projectMenu.setEnabled(false);
    currentProjectName = "";
    currentProject = null;
    viewManager = null;
    sequenceDirChooser = new DirectoryChooser();
    createDirectoryChooser();
    // Closes from title bar 
    addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent e) {
          System.exit( 0);
        }});
    this.setSize( DESKTOP_FRAME_WIDTH, DESKTOP_FRAME_HEIGHT);
    this.setLocation( FRAME_X_LOCATION, FRAME_Y_LOCATION);
    Container contentPane = getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      // System.err.println( "i " + i + " " +
      //                    contentPane.getComponent( i).getClass().getName());
      if (contentPane.getComponent(i) instanceof MDIDesktopPane) {
        ((MDIDesktopPane) contentPane.getComponent(i)).
          setBackground( ColorMap.getColor( "lightGray"));
        break;
      }
    }
    this.setVisible( true);
    if(usingSplash) {
      this.toBack();
    }

    setProjectMenuEnabled(CREATE_MENU, true);
    setProjectMenuEnabled( ADDSEQ_MENU, false);
    setProjectMenuEnabled(DELSEQ_MENU, false);
    if ((PwProject.listProjects() != null) && (PwProject.listProjects().size() > 0)) {
      setProjectMenuEnabled( OPEN_MENU, true);
      setProjectMenuEnabled( DELETE_MENU, true);
    } else {
      setProjectMenuEnabled( OPEN_MENU, false);
      setProjectMenuEnabled( DELETE_MENU, false);
    }
    projectMenu.setEnabled(true);
    windowBuilt = true;
    if(usingSplash) {
      this.toFront();
    }
  } // end constructor 


  /**
   * <code>getCurrentProjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getCurrentProjectName() {
    return currentProjectName;
  }

  /**
   * <code>setCurrentProjectName</code> - needed by PlanWorksTest (JFCUnit Test)
   *
   * @param name - <code>String</code> - 
   */
  public void setCurrentProjectName( String name) {
    currentProjectName = name;
  }

  /**
   * <code>getCurrentProject</code> - needed by PlanWorksTest (JFCUnit Test)
   *
   * @return - <code>PwProject</code> - 
   */
  public PwProject getCurrentProject() {
    return currentProject;
  }

  /**
   * <code>getViewManager</code> - needed by PlanWorksTest (JFCUnit Test)
   *
   * @return - <code>ViewManager</code> - 
   */
  public ViewManager getViewManager() {
    return viewManager;
  }

  /**
   * <code>setPlanWorks</code> - needed by PlanWorksTest (JFCUnit Test)
   *
   * @param planWorksInstance - <code>PlanWorks</code> - 
   */
  public static void setPlanWorks( PlanWorks planWorksInstance) {
    planWorks = planWorksInstance;
  }

  /**
   * <code>getSequenceDirChooser</code> - needed by PlanWorksTest (JFCUnit Test)
   *
   * @return - <code>DirectoryChooser</code> - 
   */
  public DirectoryChooser getSequenceDirChooser() {
    return sequenceDirChooser;
  }

  private List getProjectsLessCurrent() {
    List projectNames = PwProject.listProjects();
    List projectsLessCurrent = new ArrayList();
    for (int i = 0, n = projectNames.size(); i < n; i++) {
      String projectName = (String) projectNames.get( i);
      // discard current project
      if (! projectName.equals( this.currentProjectName)) {
        projectsLessCurrent.add( projectName);
      }
    }
    return projectsLessCurrent;
  } // end getProjectsLessCurrent


  private static void setProjectMenuEnabled( String textName, boolean isEnabled) {
    for (int i = 0, n = projectMenu.getItemCount(); i < n; i++) {
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem( i).getText().equals( textName))) {
        projectMenu.getItem( i).setEnabled( isEnabled);
        break;
      }
    }
  } // end setProjectMenuEnabled


  /**
   * <code>getUrlLeaf</code>
   *
   * @param seqUrl - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public String getUrlLeaf( String seqUrl) {
    int index = seqUrl.lastIndexOf( System.getProperty( "file.separator"));
    return seqUrl.substring( index + 1);
  }

  /**
   * <code>buildConstantMenus</code> - make it accessible to JFCUnit tests
   *
   * @return - <code>JMenu[]</code> - 
   */
  public static JMenu[] buildConstantMenus() {
    JMenu [] jMenuArray = new JMenu [2];
    JMenu fileMenu = new JMenu( "File");
    JMenuItem exitItem = new JMenuItem( "Exit");
    exitItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          System.exit(0);
        } });
    fileMenu.add( exitItem);

    projectMenu = new JMenu( PROJECT_MENU);
    JMenuItem createProjectItem = new JMenuItem( CREATE_MENU);
    JMenuItem openProjectItem = new JMenuItem( OPEN_MENU);
    JMenuItem deleteProjectItem = new JMenuItem( DELETE_MENU);
    JMenuItem addSequenceItem = new JMenuItem( ADDSEQ_MENU);
    JMenuItem deleteSequenceItem = new JMenuItem(DELSEQ_MENU);
    createProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          while(PlanWorks.planWorks == null) {
            Thread.yield();
          }
          PlanWorks.planWorks.instantiateProjectThread( CREATE);
        }});
    projectMenu.add( createProjectItem);
    openProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          while(PlanWorks.planWorks == null) {
            Thread.yield();
          }
          PlanWorks.planWorks.instantiateProjectThread( OPEN);
        }});
    projectMenu.add( openProjectItem);
    deleteProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.deleteProjectThread();
          Thread.yield();
        }});
    projectMenu.add( deleteProjectItem);
    projectMenu.addSeparator();
    addSequenceItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.addSequenceThread();
        }});
    projectMenu.add( addSequenceItem);
    deleteSequenceItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          PlanWorks.planWorks.deleteSequenceThread();
        }
      });
    projectMenu.add(deleteSequenceItem);

    jMenuArray[0] = fileMenu;
    jMenuArray[1] = projectMenu;
    return jMenuArray;
  } // end buildConstantMenus

  private void instantiateProjectThread( String type) {
    new InstantiateProjectThread( type).start();
  }

  class InstantiateProjectThread extends Thread {

    private String type;

    public InstantiateProjectThread( String type) {
      this.type = type;
    }  // end constructor

    public void run() {
      
      PwProject instantiatedProject = null;
      if (type.equals( CREATE)) {
        instantiatedProject = createProject();
      } else if (type.equals( OPEN)) {
        instantiatedProject = openProject();
      } else {
        System.err.println( "InstantiateProjectThread.run: " + type + " not handled");
        System.exit( -1);
      }
      if (instantiatedProject != null) {
        currentProject = instantiatedProject;
        JMenu partialPlanMenu = clearSeqPartialPlanViewMenu();
        addSeqPartialPlanViewMenu( instantiatedProject, partialPlanMenu);
        if(currentProject.listPlanningSequences().size() == 0) {
          JMenuBar menuBar = PlanWorks.this.getJMenuBar();
          for(int i = 0; i < menuBar.getMenuCount(); i++) {
            if(menuBar.getMenu(i).getText().equals("Planning Sequence")) {
              menuBar.getMenu(i).setEnabled(false);
            }
          }
        }
        // clear the old project's views
        if (viewManager != null) {
          viewManager.clearViewSets();
        }
        viewManager = new ViewManager( PlanWorks.this);
      }
    } //end run

  } // end class InstantiateProjectThread


  // projects can have 0 sequences: create project, even if selected sequences
  // are invalid
  private PwProject createProject() {
    boolean isProjectCreated = false;
    PwProject project = null;
    while (! isProjectCreated) {
      ProjectNameDialog projectNameDialog = new ProjectNameDialog( this);
      String inputName = projectNameDialog.getTypedText();
      if ((inputName == null) || (inputName.equals( ""))) {
        return null;
      }
      try {
        if (PwProject.listProjects().indexOf( inputName) >= 0) {
          throw new DuplicateNameException( "A project named '" + inputName +
                                            "' already exists.");
        }
        List invalidSequenceDirs = new ArrayList();
        while (true) {
          // ask user for a single sequence directory of partialPlan directories
          int returnVal = sequenceDirChooser.showDialog( this, "");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            for (int i = 0, n = sequenceDirectories.length; i < n; i++) {
              String sequenceDirectory = sequenceParentDirectory +
                System.getProperty( "file.separator") + sequenceDirectories[i].getName();
              if (! FileUtils.validateSequenceDirectory( sequenceDirectory)) {
                JOptionPane.showMessageDialog
                  (PlanWorks.this, sequenceDirectory, "Invalid Sequence Directory",
                   JOptionPane.ERROR_MESSAGE);
                invalidSequenceDirs.add( sequenceDirectory);
              }
            }
            if (invalidSequenceDirs.size() == sequenceDirectories.length) {
              continue; // user must reselect
            } else {
              break; // some sequences are valid
            }
          } else {
            return null; // exit dialog
          }
        } // end while
        project = PwProject.createProject( inputName);
        currentProjectName = inputName;
        isProjectCreated = true;
        //System.err.println( "Create Project: " + currentProjectName);
        this.setTitle( name + " of Project =>  " + currentProjectName);
        setProjectMenuEnabled( DELETE_MENU, true);
        setProjectMenuEnabled( ADDSEQ_MENU, true);
        setProjectMenuEnabled(DELSEQ_MENU, true);
        if (PwProject.listProjects().size() > 1) {
          setProjectMenuEnabled( OPEN_MENU, true);
        }
        for (int i = 0, n = sequenceDirectories.length; i < n; i++) {
          String sequenceDirectory = sequenceParentDirectory +
            System.getProperty( "file.separator") + sequenceDirectories[i].getName();
          if (invalidSequenceDirs.indexOf( sequenceDirectory) == -1) {
            System.err.println( "project.addPlanningSequence " + sequenceDirectory);
            project.addPlanningSequence( sequenceDirectory);
          }
        }

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        // rnfExcep.printStackTrace();
        isProjectCreated = false;
      } catch (DuplicateNameException dupExcep) {
        // duplicate project name or duplicate sequence
        int index = dupExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, dupExcep.getMessage().substring( index + 1),
           "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( dupExcep);
        // dupExcep.printStackTrace();
      } catch(Exception e) {
        //         int index = e.getMessage().indexOf(":");
        //         JOptionPane.showMessageDialog(PlanWorks.this, e.getMessage().substring(index+1),
        //                                       "Exception", JOptionPane.ERROR_MESSAGE);
        JOptionPane.showMessageDialog(PlanWorks.this, e.getMessage(),
                                      "Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println(e);
        e.printStackTrace();
        isProjectCreated = false;
      }
    }
    return project;
  } // end createProject

  private PwProject openProject() {
    PwProject project = null;
    List projectUrls = PwProject.listProjects();
    // System.err.println( "projectUrls " + projectUrls);
    List namesLessCurrent = getProjectsLessCurrent();
    // System.err.println( "namesLessCurrent " + namesLessCurrent);
    Object[] options = new Object[namesLessCurrent.size()];
    for (int i = 0, n = namesLessCurrent.size(); i < n; i++) {
        options[i] = (String) namesLessCurrent.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( this, "", "Open Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    // System.err.println( "response " + response);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectName = (String) namesLessCurrent.get( i);
          try {
            project = PwProject.getProject( projectName);
            currentProjectName = projectName;
            //System.err.println( "Open Project: " + currentProjectName);
            this.setTitle( name + " of Project =>  " + currentProjectName);
            if (getProjectsLessCurrent().size() == 0) {
              setProjectMenuEnabled( OPEN_MENU, false);
            }
            setProjectMenuEnabled( ADDSEQ_MENU, true);
            setProjectMenuEnabled(DELSEQ_MENU, true);
          } catch (ResourceNotFoundException rnfExcep) {
            // System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
          }
          break;
        }
      }
    } 
    // JOptionPane.showInputDialog returns null if user selected "cancel"
    return project;
  } // end openProject


  private void deleteProjectThread() {
    new DeleteProjectThread().start();
  }

  class DeleteProjectThread extends Thread {

    public DeleteProjectThread() {
    }  // end constructor

    public void run() {
      deleteProject();
    } //end run

  } // end class DeleteProjectThread


  private void deleteProject() {
    List projectNames = PwProject.listProjects();
    Object[] options = new Object[projectNames.size()];
    for (int i = 0, n = projectNames.size(); i < n; i++) {
      options[i] = (String) projectNames.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( this, "", "Delete Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectName = (String) projectNames.get( i);
          System.out.println( "Delete Project: " + projectName);
          try {

              PwProject.getProject( projectName).delete();

            if ((! this.currentProjectName.equals( "")) &&
                this.currentProjectName.equals( projectName)) {
              viewManager.clearViewSets();
              this.setTitle( name);
              clearSeqPartialPlanViewMenu();
            }
            if (PwProject.listProjects().size() == 0) {
              setProjectMenuEnabled( DELETE_MENU, false);
              setProjectMenuEnabled( OPEN_MENU, false);
              setProjectMenuEnabled( ADDSEQ_MENU, false);
              setProjectMenuEnabled(DELSEQ_MENU, false);
            } else if (getProjectsLessCurrent().size() == 0) {
              setProjectMenuEnabled( OPEN_MENU, false);
            } else {
              setProjectMenuEnabled( OPEN_MENU, true);
            }
          } catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
          } catch (Exception excep) {
            excep.printStackTrace();
            System.err.println( " delete: excep " + excep);
            int index = excep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, excep.getMessage().substring( index + 1),
               "Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( excep);
            excep.printStackTrace();
          }
          break;
        }
      }
    }
    // JOptionPane.showInputDialog returns null if user selected "cancel"
  } // end deleteProject


  private void addSequenceThread() {
    new AddSequenceThread().start();
  }

  class AddSequenceThread extends Thread {

    public AddSequenceThread() {
    }  // end constructor
    
    public void run() {
      MDIDynamicMenuBar dynamicMenuBar =
        (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
      JMenu planSeqMenu = null;
      for(int i = 0; i < dynamicMenuBar.getMenuCount(); i++) {
        if(dynamicMenuBar.getMenu(i) != null && 
           dynamicMenuBar.getMenu(i).getText().equals("Planning Sequence")) {
          planSeqMenu = dynamicMenuBar.getMenu(i);
        }
      }
      if(planSeqMenu != null) {
        planSeqMenu.setEnabled(false);
      }
      projectMenu.setEnabled(false);
      addSequence();
      projectMenu.setEnabled(true);
      setProjectMenuEnabled(DELSEQ_MENU, true);
      if(planSeqMenu != null) {
        planSeqMenu.setEnabled(true);
      }
    } //end run

  } // end class AddSequenceThread

  private void addSequence() {
    boolean isSequenceAdded = false;
    while (! isSequenceAdded) {
      List invalidSequenceDirs = new ArrayList();
      while (true) {
        // ask user for a single sequence directory of partialPlan directories
        int returnVal = sequenceDirChooser.showDialog( this, "");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          for (int i = 0, n = sequenceDirectories.length; i < n; i++) {
            String sequenceDirectory = sequenceParentDirectory +
              System.getProperty( "file.separator") + sequenceDirectories[i].getName();
            if (! FileUtils.validateSequenceDirectory( sequenceDirectory)) {
              JOptionPane.showMessageDialog
                (PlanWorks.this, sequenceDirectory, "Invalid Sequence Directory",
                 JOptionPane.ERROR_MESSAGE);
              invalidSequenceDirs.add( sequenceDirectory);
            }
          }
          if (invalidSequenceDirs.size() == sequenceDirectories.length) {
            System.err.println( "continue");
            continue; // user must reselect
          } else {
            System.err.println( "break");
            break; // some sequences are valid
          }
        } else {
          return; // exit dialog
        }
      } // end while

      try {
            System.err.println( "try");
        for (int i = 0, n = sequenceDirectories.length; i < n; i++) {
          String sequenceDirectory = sequenceParentDirectory +
            System.getProperty( "file.separator") + sequenceDirectories[i].getName();
          if (invalidSequenceDirs.indexOf( sequenceDirectory) == -1) {
            System.err.println( "project.addPlanningSequence " + sequenceDirectory);
            currentProject.addPlanningSequence( sequenceDirectory);
            isSequenceAdded = true;
         }
        }
        //System.err.println( "Adding sequence " + sequenceDirectory);
      } catch (DuplicateNameException dupExcep) {
        int index = dupExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, dupExcep.getMessage().substring( index + 1),
           "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( dupExcep);
        // dupExcep.printStackTrace();
      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        // rnfExcep.printStackTrace();
      }
      JMenu partialPlanMenu = clearSeqPartialPlanViewMenu();
      addSeqPartialPlanViewMenu( currentProject, partialPlanMenu);
    }
  } // end addSequence


  private void deleteSequenceThread() {
    new DeleteSequenceThread().start();
  }

  class DeleteSequenceThread extends Thread {
    public DeleteSequenceThread() {
    }

    public void run() {
      MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
      JMenu planSeqMenu = null;
      for(int i = 0; i < dynamicMenuBar.getMenuCount(); i++) {
        if(dynamicMenuBar.getMenu(i) != null &&
           dynamicMenuBar.getMenu(i).getText().equals("Planning Sequence")) {
          planSeqMenu = dynamicMenuBar.getMenu(i);
        }
      }
      if(planSeqMenu != null) {
        planSeqMenu.setEnabled(false);
      }
      projectMenu.setEnabled(false);
      deleteSequence();
      projectMenu.setEnabled(true);
      if(planSeqMenu != null) {
        planSeqMenu.setEnabled(true);
      }
    }
  }

  private void deleteSequence() {
    List sequenceNames = currentProject.listPlanningSequences();
    Object[] options = new Object[sequenceNames.size()];
    for (int i = 0, n = sequenceNames.size(); i < n; i++) {
      options[i] = (String) sequenceNames.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( this, "", "Delete Sequence", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String sequenceName = (String) sequenceNames.get( i);
          System.out.println( "Delete Sequence: " + sequenceName);
          try {
            PwPlanningSequence seq = currentProject.getPlanningSequence(sequenceName);
            currentProject.deletePlanningSequence(sequenceName);
            ListIterator partialPlanIterator = seq.listPartialPlans().listIterator();
            while(partialPlanIterator.hasNext()) {
              PwPartialPlan plan = (PwPartialPlan) partialPlanIterator.next();
              if(viewManager.getViewSet(plan) != null) {
                viewManager.getViewSet(plan).close();
                viewManager.removeViewSet(plan);
              }
            }
            seq.delete();
            MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
            JMenu sequenceMenu = null;
            for(int j = 0; j < dynamicMenuBar.getMenuCount(); j++) {
              if(dynamicMenuBar.getMenu(j).getText().equals("Planning Sequence")) {
                sequenceMenu = dynamicMenuBar.getMenu(j);
              }
            }
            if(sequenceMenu == null) {
              throw new Exception("Failed to find Planning Sequence menu when deleting sequence.");
            }
            String menuName = (String) sequenceNameMap.get(sequenceName);
            for(int j = 0; j < sequenceMenu.getItemCount(); j++) {
              if(sequenceMenu.getItem(j).getText().equals(menuName)) {
                sequenceMenu.remove(j);
                break;
              }
            }
            if(sequenceMenu.getItemCount() == 0) {
              dynamicMenuBar.remove(sequenceMenu);
              setProjectMenuEnabled(DELSEQ_MENU, false);
            }
          } catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
            rnfExcep.printStackTrace();
            return;
          } catch (Exception excep) {
            excep.printStackTrace();
            System.err.println( " delete: excep " + excep);
            int index = excep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, excep.getMessage().substring( index + 1),
               "Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( excep);
            excep.printStackTrace();
          }
          break;
        }
      }
    }
  }

  private void addSeqPartialPlanViewMenu( PwProject project, JMenu partialPlanMenu) {
    // Create Dynamic Cascading Seq/PartialPlan/View Menu
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
    if (partialPlanMenu == null) {
      dynamicMenuBar.addConstantMenu
        ( buildSeqPartialPlanViewMenu( project, partialPlanMenu));
    } else {
      buildSeqPartialPlanViewMenu( project, partialPlanMenu);
    }
    dynamicMenuBar.validate();
  } // end addSeqPartialPlanMenu


  private JMenu buildSeqPartialPlanViewMenu( PwProject project,
                                             JMenu seqPartialPlanViewMenu) {
    if (seqPartialPlanViewMenu == null) {
      seqPartialPlanViewMenu = new JMenu( PLANSEQ_MENU);
    }
    seqPartialPlanViewMenu.removeAll();
    sequenceNameMap = new HashMap();
    //System.err.println( "buildSeqPartialPlanViewMenu");
    List planSeqNames = project.listPlanningSequences();
    Collections.sort(planSeqNames, new SeqNameComparator());
    Iterator seqUrlsItr = planSeqNames.iterator();
    while (seqUrlsItr.hasNext()) {
      String seqUrl = (String) seqUrlsItr.next();
      String seqName = getUrlLeaf( seqUrl);
      int nameCount = 0;
      for(int i = 0; i < seqPartialPlanViewMenu.getItemCount(); i++) {
        JMenuItem item = seqPartialPlanViewMenu.getItem(i);
        String itemName = item.getText();
        int index = itemName.indexOf(" (");
        if(index != -1) {
          itemName = itemName.substring(0, index);
        }
        if(itemName.equals(seqName)) {
          nameCount++;
        }
      }
      if(nameCount > 0) {
        seqName = seqName.concat(" (").concat(Integer.toString(nameCount)).concat(")");
      }
      //System.err.println( "  sequenceName " + seqName);
      sequenceNameMap.put(seqUrl, seqName);
      JMenu seqMenu = new JMenu( seqName);
      seqPartialPlanViewMenu.add( seqMenu);

      try {
        Iterator ppNamesItr =
          project.getPlanningSequence( seqUrl).listPartialPlanNames().iterator();
        while (ppNamesItr.hasNext()) {
          String partialPlanName = (String) ppNamesItr.next();
          //System.err.println( "    partialPlanName " + partialPlanName);
          JMenu partialPlanMenu = new JMenu( partialPlanName);
          buildViewSubMenu( partialPlanMenu, seqUrl, partialPlanName);
          seqMenu.add( partialPlanMenu);
        }
      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        rnfExcep.printStackTrace();
      }
    }
    return seqPartialPlanViewMenu;
  } // end buildSeqPartialPlanViewMenu


  private void buildViewSubMenu( JMenu partialPlanMenu, String seqUrl,
                                 String partialPlanName) {
    SeqPartPlanViewMenuItem constraintNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Constraint Network", seqUrl, partialPlanName);
    constraintNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( ViewManager.CNET_VIEW, (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( constraintNetworkViewItem);

    SeqPartPlanViewMenuItem temporalExtentViewItem =
          new SeqPartPlanViewMenuItem( "Temporal Extent", seqUrl, partialPlanName);
    temporalExtentViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( ViewManager.TEMPEXT_VIEW, (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( temporalExtentViewItem);

    SeqPartPlanViewMenuItem temporalNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Temporal Network", seqUrl, partialPlanName);
    temporalNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( ViewManager.TEMPNET_VIEW, (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( temporalNetworkViewItem);
    temporalNetworkViewItem.setEnabled(false);
    SeqPartPlanViewMenuItem timelineViewItem =
          new SeqPartPlanViewMenuItem( "Timeline", seqUrl, partialPlanName);
    timelineViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( ViewManager.TIMELINE_VIEW, (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( timelineViewItem);

    SeqPartPlanViewMenuItem tokenNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Token Network", seqUrl, partialPlanName);
    tokenNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( ViewManager.TNET_VIEW, (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( tokenNetworkViewItem);

  } // end buildViewSubMenu


  private JMenu clearSeqPartialPlanViewMenu() {
    // clear out previous project's Partial Plan cascading menu
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
    for (int i = 0, n = dynamicMenuBar.getMenuCount(); i < n; i++) {
      if (((JMenu) dynamicMenuBar.getMenu( i)).getText().equals( PLANSEQ_MENU)) {
        JMenu partialPlanMenu = (JMenu) dynamicMenuBar.getMenu( i);
        if (PwProject.listProjects().size() == 0) {
          dynamicMenuBar.remove(partialPlanMenu);
          partialPlanMenu = null;
        } else {
          partialPlanMenu.removeAll();
        }
        dynamicMenuBar.validate();
        dynamicMenuBar.repaint();
        return partialPlanMenu;
      }
    }
    return null;
  } // end clearSeqPartialPlanViewMenu


  private void createPartialPlanViewThread( final String viewName,
                                            final SeqPartPlanViewMenuItem menuItem) {
    (new Thread() {
        public void run() {
          try {
            SwingUtilities.invokeAndWait(new CreatePartialPlanViewThread(viewName, menuItem));
          } catch(Exception e) { }
        }
      }
     ).start();
  }


  class CreatePartialPlanViewThread implements Runnable {

    private String seqUrl;
    private String sequenceName;
    private String partialPlanName;
    private PwPartialPlan partialPlan;
    private String viewName;

    public CreatePartialPlanViewThread( String viewName, SeqPartPlanViewMenuItem menuItem) {
      this.seqUrl = menuItem.getSeqUrl();
      this.sequenceName = menuItem.getSequenceName();
      this.partialPlanName = menuItem.getPartialPlanName();
      this.viewName = viewName;
    }  // end constructor

    public void run() { 
      try {
        PwPlanningSequence planSequence =
          currentProject.getPlanningSequence( seqUrl);

        //PwPartialPlan partialPlan = planSequence.addPartialPlan( partialPlanName);
        PwPartialPlan partialPlan = planSequence.getPartialPlan(partialPlanName);

        renderView( viewName, sequenceName, partialPlanName, partialPlan);

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
        rnfExcep.printStackTrace();
      }
    } //end run


    private void renderView( String viewName, String sequenceName, String partialPlanName,
                             PwPartialPlan partialPlan) {
      ViewSet viewSet = viewManager.getViewSet( partialPlan);
      MDIInternalFrame viewFrame = null;
      boolean viewExists = false;
      long startTimeMSecs = (new Date()).getTime();
      if ((viewSet != null) && viewSet.viewExists( viewName)) {
        viewExists = true;
      }
      if (viewName.equals( ViewManager.TIMELINE_VIEW)) {
        if (! viewExists) {
          System.err.println( "Rendering Timeline View ...");
        }
        viewFrame = viewManager.openTimelineView
          ( partialPlan, sequenceName + System.getProperty( "file.separator") +
            partialPlanName, startTimeMSecs);
        finishViewRendering( viewFrame, viewName, viewExists, startTimeMSecs);
      } else if (viewName.equals( ViewManager.TNET_VIEW)) {
        if (! viewExists) {
          System.err.println( "Rendering Token Network View ...");
        }
        viewFrame = viewManager.openTokenNetworkView
          ( partialPlan, sequenceName + System.getProperty( "file.separator") +
            partialPlanName, startTimeMSecs);        
        finishViewRendering( viewFrame, viewName, viewExists, startTimeMSecs);
      } else if (viewName.equals( ViewManager.TEMPEXT_VIEW)) {
        if (! viewExists) {
          System.err.println( "Rendering Temporal Extent View ...");
        }
        viewFrame = viewManager.openTemporalExtentView
          ( partialPlan, sequenceName + System.getProperty( "file.separator") +
            partialPlanName, startTimeMSecs);        
        finishViewRendering( viewFrame, viewName, viewExists, startTimeMSecs);          
      } else if (viewName.equals( ViewManager.CNET_VIEW)) {
        if (! viewExists) {
          System.err.println( "Rendering Constraint Network View ...");
        }
        viewFrame = viewManager.openConstraintNetworkView
          ( partialPlan, sequenceName + System.getProperty( "file.separator") +
            partialPlanName, startTimeMSecs);        
        finishViewRendering( viewFrame, viewName, viewExists, startTimeMSecs);          
      } else if (viewName.equals( ViewManager.TEMPNET_VIEW)) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      }
    } // end renderView

    private void finishViewRendering( MDIInternalFrame viewFrame, String viewName,
                                      boolean viewExists, long startTimeMSecs) {
      if (! viewExists) {
        viewFrame.setSize( INTERNAL_FRAME_WIDTH, INTERNAL_FRAME_HEIGHT);
        int viewIndex = 0;
        for (int i = 0, n = ViewConstants.orderedViewNames.length; i < n; i++) {
          if (ViewConstants.orderedViewNames[i].equals( viewName)) {
            viewIndex = i;
            break;
          }
        }
        viewFrame.setLocation( INTERNAL_FRAME_X_DELTA * viewIndex,
                               FRAME_Y_LOCATION + INTERNAL_FRAME_Y_DELTA * viewIndex);
        viewFrame.setVisible( true);
      }
      // make associated menus appear & bring window to the front
      try {
        viewFrame.setSelected( false);
        viewFrame.setSelected( true);
      } catch (PropertyVetoException excp) { };
    } // end finishViewRendering

  } // end class CreatePartialPlanViewThread



  /**
   * <code>SeqPartPlanViewMenuItem</code> - class is public for JFCUnit Test classes
   *
   */
  public class SeqPartPlanViewMenuItem extends JMenuItem {

    private String seqUrl;
    private String sequenceName;
    private String partialPlanName;

    public SeqPartPlanViewMenuItem( String viewName, String seqUrl, String partialPlanName) {
      super( viewName);
      this.seqUrl = seqUrl;
      this.sequenceName = getUrlLeaf( seqUrl);
      this.partialPlanName = partialPlanName;
    }

    public String getSeqUrl() {
      return seqUrl;
    }

    public String getSequenceName() {
      return sequenceName;
    }

    public String getPartialPlanName() {
      return partialPlanName;
    }

  } // end class SeqPartPlanViewMenuItem

  private final void createDirectoryChooser() {
    sequenceDirChooser.setCurrentDirectory
      ( new File( System.getProperty( "default.sequence.dir")));
    sequenceDirChooser.setDialogTitle
      ( "Select Sequence Directory of Partial Plan Directory(ies)");
    sequenceDirChooser.setMultiSelectionEnabled( true);
    sequenceDirChooser.getOkButton().addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String dirChoice = sequenceDirChooser.getCurrentDirectory().getAbsolutePath();
          File [] seqDirs = sequenceDirChooser.getSelectedFiles();
          // System.err.println( "sequence parent directory" + dirChoice);
          // System.err.println( "sequenceDirectories");
          // for (int i = 0, n = seqDirs.length; i < n; i++) {
          //   System.err.println( "  " + seqDirs[i].getName());
          // }
          if ((dirChoice != null) && (dirChoice.length() > 0) &&
              (new File( dirChoice)).isDirectory() &&
              (seqDirs.length != 0)) {
            PlanWorks.sequenceParentDirectory = dirChoice;
            PlanWorks.sequenceDirectories = seqDirs;
            sequenceDirChooser.approveSelection();
          } else {
            JOptionPane.showMessageDialog
              ( PlanWorks.this,
                "`" + dirChoice + "'\nis not a valid sequence directory.",
                "No Directory Selected", JOptionPane.ERROR_MESSAGE);
          }
        }
      });
    sequenceDirChooser.setFileFilter( new SequenceDirectoryFilter());
  } // end createDirectoryChooser


class SequenceDirectoryFilter extends FileFilter {

  public SequenceDirectoryFilter() {
    super();
  }

  /**
   * accept - Accept all files and directories which are not partial plan 
   *          step directories
   *
   * @param file - a directory or file name
   * @return true, if a directory is valid
   */
  public boolean accept( File file) {
    boolean isValid = true;
    if (! file.isDirectory()) {
      // accept all files
    } else if (file.isDirectory()) {
      if (file.getName().equals( "CVS")) {
        isValid = false;
      } else {
        int fileCnt = 0;
        File [] filePathNames = file.listFiles();
        for (int i = 0, n = filePathNames.length; i < n; i++) {
          if (! filePathNames[i].getName().equals( "CVS")) {
            fileCnt++;
          }
        }
        if (fileCnt != DbConstants.NUMBER_OF_PP_FILES) {
          // accept all directories with != partial plan file count
        } else {
          for (int i = 0, n = filePathNames.length; i < n; i++) {
            String ext = FileUtils.getExtension( filePathNames[i]);
            // System.err.println( "ext " + ext );
            if ((ext != null) && ext.equals( DbConstants.PP_PARTIAL_PLAN_EXT)) {
              isValid = false;
              break;
            }
          }
        }
      }
    }
    // System.err.println( "accept " + file.getName() + " isValid " + isValid); 
    return isValid;
  } // end accept

  /**
   * getDescription - string to describe this filter
   *
   * @return string to describe this filter
   */
  public String getDescription() { 
    return "Sequence Directories";
  }
} // end class SequenceDirectoryFilter


  /**
   * <code>isMacOSX</code>
   *
   * @return - <code>boolean</code> - 
   */
  public static boolean isMacOSX() {
    return (osType.equals( "darwin"));
  }

  /**
   * <code>main</code> - pass in JFrame name
   *
   * @param args - <code>String[]</code> - 
   */
  public static void main (String[] args) {
    name = "";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        name = args[argc];
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit(-1);
      }
    }
    osType = System.getProperty("os.type");
    // System.err.println( "osType " + osType);
    planWorksRoot = System.getProperty( "planworks.root");

    planWorks = new PlanWorks( buildConstantMenus());

  } // end main

  private class SeqNameComparator implements Comparator {
    public SeqNameComparator() {
    }
    public int compare(Object o1, Object o2) {
      String s1 = getUrlLeaf((String) o1);
      String s2 = getUrlLeaf((String) o2);
      return s1.compareTo(s2);
    }
    public boolean equals(Object o1, Object o2) {
      String s1 = getUrlLeaf((String)o1);
      String s2 = getUrlLeaf((String)o2);
      return s1.equals(s2);
    }
  }

} // end  class PlanWorks
        
