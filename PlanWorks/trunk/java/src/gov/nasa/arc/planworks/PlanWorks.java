// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.28 2003-07-08 22:57:32 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.DirectoryChooser;
import gov.nasa.arc.planworks.util.ProjectNameDialog;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
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

  /**
   * constant <code>DESKTOP_FRAME_WIDTH</code>
   *
   */
  public static final int DESKTOP_FRAME_WIDTH = 900;

  /**
   * constant <code>DESKTOP_FRAME_HEIGHT</code>
   *
   */
  public static final int DESKTOP_FRAME_HEIGHT = 850;

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
   * constant <code>FRAME_X_LOCATION</code>
   *
   */
  public static final int FRAME_X_LOCATION = 100;

  /**
   * constant <code>FRAME_Y_LOCATION</code>
   *
   */
  public static final int FRAME_Y_LOCATION = 100;

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

  /**
   * variable <code>defaultProjectName</code> - make it accessible to JFCUnit tests
   *
   */
  public static String defaultProjectName;

  /**
   * variable <code>defaultSequenceDirectory</code> - make it accessible to JFCUnit tests
   *
   */
  public static String defaultSequenceDirectory;

  private static JMenu projectMenu;
  private final DirectoryChooser sequenceDirChooser;
  private static String sequenceDirectory; // pathname

  private String currentProjectName;
  private String currentSequenceDirectory; // pathname
  private PwProject currentProject;
  private ViewManager viewManager;

  /**
   * <code>PlanWorks</code> - constructor 
   *
   * @param constantMenus - <code>JMenu[]</code> -
   */                                
  public PlanWorks( JMenu[] constantMenus) {
    super( name, constantMenus);
    currentSequenceDirectory = "";
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
    
    setProjectMenuEnabled( "Create ...", true);
    if ((PwProject.listProjects() != null) && (PwProject.listProjects().size() > 0)) {
      setProjectMenuEnabled( "Open ...", true);
      setProjectMenuEnabled( "Delete ...", true);
    } else {
      setProjectMenuEnabled( "Open ...", false);
      setProjectMenuEnabled( "Delete ...", false);
    }
  } //end constructor 


  /**
   * <code>getCurrentProjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getCurrentProjectName() {
    return currentProjectName;
  }

  /**
   * <code>getDefaultProjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getDefaultProjectName() {
    return defaultProjectName;
  }

  /**
   * <code>getCurrentProject</code>
   *
   * @return - <code>PwProject</code> - 
   */
  public PwProject getCurrentProject() {
    return currentProject;
  }

  /**
   * <code>getViewManager</code>
   *
   * @return - <code>ViewManager</code> - 
   */
  public ViewManager getViewManager() {
    return viewManager;
  }

  /**
   * <code>setPlanWorks</code> - needed by TimelineViewTest (JFCUnit Test)
   *
   * @param planWorksInstance - <code>PlanWorks</code> - 
   */
  public static void setPlanWorks( PlanWorks planWorksInstance) {
    planWorks = planWorksInstance;
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
      if (projectMenu.getItem( i).getText().equals( textName)) {
        projectMenu.getItem( i).setEnabled( isEnabled);
        break;
      }
    }
  } // end setProjectMenuEnabled


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

    projectMenu = new JMenu( "Project");
    JMenuItem createProjectItem = new JMenuItem( "Create ...");
    JMenuItem openProjectItem = new JMenuItem( "Open ...");
    JMenuItem deleteProjectItem = new JMenuItem( "Delete ...");
    createProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.instantiateProjectThread( "create");
        }});
    projectMenu.add( createProjectItem);
    openProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.instantiateProjectThread( "open");
        }});
    projectMenu.add( openProjectItem);
    deleteProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.deleteProjectThread();
        }});
    projectMenu.add( deleteProjectItem);

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
      if (type.equals( "create")) {
        instantiatedProject = createProject();
      } else if (type.equals( "open")) {
        instantiatedProject = openProject();
      } else {
        System.err.println( "InstantiateProjectThread.run: " + type + " not handled");
        System.exit( -1);
      }
      if (instantiatedProject != null) {
        currentProject = instantiatedProject;
        JMenu partialPlanMenu = clearSeqPartialPlanViewMenu();
        addSeqPartialPlanViewMenu( instantiatedProject, partialPlanMenu);
        // clear the old project's views
        if (viewManager != null) {
          viewManager.clearViewSets();
        }
        viewManager = new ViewManager( PlanWorks.this);
      }
    } //end run

  } // end class InstantiateProjectThread


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
        while (true) {
          // ask user for a single sequence directory of partialPlan directories
          int returnVal = sequenceDirChooser.showDialog( this, "");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (! validateSequenceDirectory( sequenceDirectory)) {
              continue;
            } else {
              break;
            }
          } else {
            return null;
          }
        }
        project = PwProject.createProject( inputName);
        project.addPlanningSequence( sequenceDirectory);
        isProjectCreated = true;
        currentProjectName = inputName;
        currentSequenceDirectory = sequenceDirectory;
        System.err.println( "Create Project: " + currentProjectName);
        this.setTitle( name + "  --  project: " + currentProjectName);
        setProjectMenuEnabled( "Delete ...", true);
        if (PwProject.listProjects().size() > 1) {
          setProjectMenuEnabled( "Open ...", true);
        }
      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
          System.err.println( rnfExcep);
          isProjectCreated = false;
      } catch (DuplicateNameException dupExcep) {
        int index = dupExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, dupExcep.getMessage().substring( index + 1),
           "Duplicate Name Exception", JOptionPane.ERROR_MESSAGE);
          System.err.println( dupExcep);
          isProjectCreated = false; 
      } catch (SQLException sqlExcep) {
        StringBuffer errorOutput =
          new StringBuffer(sqlExcep.getMessage().substring(sqlExcep.getMessage().
                                                           indexOf(":") + 1));
        StackTraceElement [] stackTrace = sqlExcep.getStackTrace();
        for(int i = 0; i < stackTrace.length; i++) {
          errorOutput.append(stackTrace[i].getFileName()).append(":");
          errorOutput.append(stackTrace[i].getLineNumber()).append(" ");
          errorOutput.append(stackTrace[i].getClassName()).append(".");
          errorOutput.append(stackTrace[i].getMethodName()).append("\n");
        }
        JOptionPane.showMessageDialog
          (PlanWorks.this, errorOutput.toString(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
        isProjectCreated = false; 
      }/* catch(Exception e) {
        int index = e.getMessage().indexOf(":");
        JOptionPane.showMessageDialog(PlanWorks.this, e.getMessage().substring(index+1),
                                      "Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println(e);
        isProjectCreated = false;
        }*/
    }
    return project;
  } // end createProject

  private boolean validateSequenceDirectory( String sequenceDirectory) {
    // System.err.println( "validateSequenceDirectory: sequenceDirectory '" +
    //                    sequenceDirectory + "'");
    // determine sequence's partial plan directories
    List partialPlanDirs = new ArrayList();
    String [] fileNames = new File( sequenceDirectory).list();
    for (int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      if ((! fileName.equals( "CVS")) &&
          (new File( sequenceDirectory + System.getProperty( "file.separator") +
                     fileName)).isDirectory()) {
        // System.err.println( "Sequence " + sequenceDirectory +
        //                     " => partialPlanDirName: " + fileName);
        partialPlanDirs.add( fileName);
      }
    }
    if (partialPlanDirs.size() == 0) {
      JOptionPane.showMessageDialog
        (PlanWorks.this, sequenceDirectory,
         "Sequence Directory Does Not Have Any Partial Plan Directories",
         JOptionPane.ERROR_MESSAGE);
      return false;
    }
    // determine existence of the 14 SQL-input files in partial plan directories (steps)
    for (int i = 0, n = partialPlanDirs.size(); i < n; i++) {
      String partialPlanPath = sequenceDirectory + System.getProperty( "file.separator") +
        partialPlanDirs.get( i);
      fileNames = new File( partialPlanPath).list( new FilenameFilter () {
          public boolean accept(File dir, String name) {
            return (name.indexOf( DbConstants.PP_PARTIAL_PLAN_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_OBJECTS_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_TIMELINES_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_SLOTS_EXT) != -1 || 
                    name.indexOf( DbConstants.PP_TOKENS_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_VARIABLES_EXT) != -1 || 
                    name.indexOf( DbConstants.PP_PREDICATES_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_PARAMETERS_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_ENUMERATED_DOMAINS_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_INTERVAL_DOMAINS_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_CONSTRAINTS_EXT) != -1 ||
                    name.indexOf( DbConstants.PP_TOKEN_RELATIONS_EXT) != -1 || 
                    name.indexOf( DbConstants.PP_PARAM_VAR_TOKEN_MAP_EXT) != -1 || 
                    name.indexOf( DbConstants.PP_CONSTRAINT_VAR_MAP_EXT) != -1);
          }
        });
      if (! validateSQLInputFiles( partialPlanPath, fileNames)) {
        return false;
      }
    }
    return true;
  } // end validateSequenceDirectory

  private boolean validateSQLInputFiles( String partialPlanPath, String[] fileNames) {
    // determine whether the 14 SQL-input files have the correct extentions.
    // Using Arrays.asList( DbConstants.PARTIAL_PLAN_FILE_EXTS)
    // gets java.lang.UnsupportedOperationException
    //      at java.util.AbstractList.remove(AbstractList.java:167)
    List requiredFiles = new ArrayList();
    requiredFiles.add( DbConstants.PP_PARTIAL_PLAN_EXT);
    requiredFiles.add( DbConstants.PP_OBJECTS_EXT);
    requiredFiles.add( DbConstants.PP_TIMELINES_EXT);
    requiredFiles.add( DbConstants.PP_SLOTS_EXT);
    requiredFiles.add( DbConstants.PP_TOKENS_EXT);
    requiredFiles.add( DbConstants.PP_VARIABLES_EXT);
    requiredFiles.add( DbConstants.PP_PREDICATES_EXT);
    requiredFiles.add( DbConstants.PP_PARAMETERS_EXT);
    requiredFiles.add( DbConstants.PP_ENUMERATED_DOMAINS_EXT);
    requiredFiles.add( DbConstants.PP_INTERVAL_DOMAINS_EXT);
    requiredFiles.add( DbConstants.PP_CONSTRAINTS_EXT);
    requiredFiles.add( DbConstants.PP_TOKEN_RELATIONS_EXT);
    requiredFiles.add( DbConstants.PP_PARAM_VAR_TOKEN_MAP_EXT);
    requiredFiles.add( DbConstants.PP_CONSTRAINT_VAR_MAP_EXT);
    for (int i = 0, n = fileNames.length; i < n; i++) {
      String fileName = fileNames[i];
      int index = fileName.indexOf( ".");
      String fileExt = fileName.substring( index);
      // System.err.println( "fileName " + fileName);
      for (int j = 0, m = requiredFiles.size(); j < m; j++) {
        if (fileExt.equals( requiredFiles.get( j))) {
          // System.err.println( "found " + requiredFiles.get( j));
          requiredFiles.remove( j);
          break;
        }
      }
    }
    if (requiredFiles.size() != 0) {
      JOptionPane.showMessageDialog
        (PlanWorks.this, requiredFiles,
         "Partial Plan Files Not Found", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  } // end validateSQLInputFiles

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
            System.err.println( "Open Project: " + currentProjectName);
            this.setTitle( name + "  --  project: " + currentProjectName);
            if (getProjectsLessCurrent().size() == 0) {
              setProjectMenuEnabled( "Open ...", false);
            }
          } catch (ResourceNotFoundException rnfExcep) {
            // System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
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
              setProjectMenuEnabled( "Delete ...", false);
              setProjectMenuEnabled( "Open ...", false);
            } else if (getProjectsLessCurrent().size() == 0) {
              setProjectMenuEnabled( "Open ...", false);
            } else {
              setProjectMenuEnabled( "Open ...", true);
            }
          } catch (ResourceNotFoundException rnfExcep) {
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
          } catch (Exception excep) {
            excep.printStackTrace();
            System.err.println( " delete: excep " + excep);
            int index = excep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, excep.getMessage().substring( index + 1),
               "Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( excep);
          }
          break;
        }
      }
    }
    // JOptionPane.showInputDialog returns null if user selected "cancel"
  } // end deleteProject


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
      seqPartialPlanViewMenu = new JMenu( "Partial Plan");
    }
    System.err.println( "buildSeqPartialPlanViewMenu");
    Iterator seqUrlsItr = project.listPlanningSequences().iterator();
    while (seqUrlsItr.hasNext()) {
      String seqUrl = (String) seqUrlsItr.next();
      // System.err.println( " seqUrl " + seqUrl);
      String seqName = getUrlLeaf( seqUrl);
      System.err.println( "  sequenceName " + seqName);
      JMenu seqMenu = new JMenu( seqName);
      seqPartialPlanViewMenu.add( seqMenu);

      try {
        Iterator ppNamesItr =
          project.getPlanningSequence( seqUrl).listPartialPlanNames().iterator();
        while (ppNamesItr.hasNext()) {
          String partialPlanName = (String) ppNamesItr.next();
          System.err.println( "    partialPlanName " + partialPlanName);
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
                ( "constraintNetworkView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( constraintNetworkViewItem);

    SeqPartPlanViewMenuItem temporalExtentViewItem =
          new SeqPartPlanViewMenuItem( "Temporal Extent", seqUrl, partialPlanName);
    temporalExtentViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "temporalExtentView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( temporalExtentViewItem);

    SeqPartPlanViewMenuItem temporalNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Temporal Network", seqUrl, partialPlanName);
    temporalNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "temporalNetworkView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( temporalNetworkViewItem);
    SeqPartPlanViewMenuItem timelineViewItem =
          new SeqPartPlanViewMenuItem( "Timeline", seqUrl, partialPlanName);
    timelineViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "timelineView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( timelineViewItem);

    SeqPartPlanViewMenuItem tokenNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Token Network", seqUrl, partialPlanName);
    tokenNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "tokenNetworkView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( tokenNetworkViewItem);

  } // end buildViewSubMenu


  private JMenu clearSeqPartialPlanViewMenu() {
    // clear out previous project's Partial Plan cascading menu
    JMenu partialPlanMenu = null;
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
    for (int i = 0, n = dynamicMenuBar.getMenuCount(); i < n; i++) {
      if (((JMenu) dynamicMenuBar.getMenu( i)).getText().equals( "Partial Plan")) {
        partialPlanMenu = (JMenu) dynamicMenuBar.getMenu( i);
        partialPlanMenu.removeAll();
        if(PwProject.listProjects().size() == 0) {
          dynamicMenuBar.remove(partialPlanMenu);
        }
        //dynamicMenuBar.validate();
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
      } /*catch (SQLException sqlExcep) {
        StringBuffer errorOutput =
          new StringBuffer(sqlExcep.getMessage().substring(sqlExcep.getMessage().
                                                           indexOf(":") + 1));
        StackTraceElement [] stackTrace = sqlExcep.getStackTrace();
        for(int i = 0; i < stackTrace.length; i++) {
          errorOutput.append(stackTrace[i].getFileName()).append(":");
          errorOutput.append(stackTrace[i].getLineNumber()).append(" ");
          errorOutput.append(stackTrace[i].getClassName()).append(".");
          errorOutput.append(stackTrace[i].getMethodName()).append("\n");
        }
        JOptionPane.showMessageDialog
          (PlanWorks.this, errorOutput.toString(), "SQL Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println(sqlExcep);
        }*/
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
      if (viewName.equals( "timelineView")) {
        if (! viewExists) {
          System.err.println( "Rendering Timeline View ...");
        }
        viewFrame = viewManager.openTimelineView( partialPlan, sequenceName +
                                                  System.getProperty( "file.separator") +
                                                  partialPlanName);
        finishViewRendering( viewFrame, viewExists, startTimeMSecs);
      } else if (viewName.equals( "tokenNetworkView")) {
        if (! viewExists) {
          System.err.println( "Rendering Token Network View ...");
        }
        viewFrame = viewManager.openTokenNetworkView( partialPlan, sequenceName +
                                                      System.getProperty( "file.separator") +
                                                      partialPlanName);        
        System.err.println("Finish view rendering..");
        finishViewRendering( viewFrame, viewExists, startTimeMSecs);
      } else if (viewName.equals( "temporalExtentView")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else if (viewName.equals( "constraintNetworkView")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else if (viewName.equals( "temporalNetworkView")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      }
    } // end renderView

    private void finishViewRendering( MDIInternalFrame viewFrame, boolean viewExists,
                                      long startTimeMSecs) {
      if (! viewExists) {
        long stopTimeMSecs = (new Date()).getTime();
        System.err.println( "   ... elapsed time: " +
                            (stopTimeMSecs - startTimeMSecs) + " msecs.");
        viewFrame.setSize( INTERNAL_FRAME_WIDTH, INTERNAL_FRAME_HEIGHT);
        viewFrame.setLocation( FRAME_X_LOCATION, FRAME_Y_LOCATION);
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
    sequenceDirChooser.setCurrentDirectory( new File( defaultSequenceDirectory));
    sequenceDirChooser.setDialogTitle
      ( "Select Sequence Directory of Partial Plan Directory(ies)");
    sequenceDirChooser.getOkButton().addActionListener( new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          File ff = sequenceDirChooser.getCurrentDirectory();
          String dirChoice = ff.getAbsolutePath();
          if ((dirChoice != null) && (dirChoice.length() > 0) &&
              (new File( dirChoice)).isDirectory()) {
            // System.err.println( "directory choice " + dirChoice);
            PlanWorks.sequenceDirectory = dirChoice;
            sequenceDirChooser.approveSelection();
          } else {
            JOptionPane.showMessageDialog
              ( PlanWorks.this,
                "`" + dirChoice + "'\ndoes not exist or is not a directory.",
                "No Directory Selected", JOptionPane.ERROR_MESSAGE);
          }
        }
      });
  } // end createDirectoryChooser

  private String getUrlLeaf( String seqUrl) {
    int index = seqUrl.lastIndexOf( System.getProperty( "file.separator"));
    return seqUrl.substring( index + 1);
  }

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
    defaultProjectName = "";
    defaultSequenceDirectory = "";
    defaultProjectName = System.getProperty( "default.project.name");
    defaultSequenceDirectory = System.getProperty( "default.sequence.dir");

    try {
      PwProject.initProjects();
    } catch (IOException excp) {
      System.err.println( excp);
      System.exit( -1);
    } catch (ResourceNotFoundException rnfExcp) {
      System.err.println( rnfExcp);
      System.exit( -1);
    } catch (SQLException sqlExcp) {
      System.err.println( sqlExcp);
      System.exit( -1);
    }

    planWorks = new PlanWorks( buildConstantMenus());

  } // end main


} // end  class PlanWorks
        
