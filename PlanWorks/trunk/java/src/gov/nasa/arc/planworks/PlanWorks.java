// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.11 2003-06-18 21:44:10 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.ParseProjectUrl;
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
   * variable <code>osName</code> - make it accessible to JFCUnit tests
   *
   */
  public static String osName;

  /**
   * variable <code>userCollectionName</code> - make it accessible to JFCUnit tests
   *                e.g. /wtaylor 
   */
  public static String userCollectionName; 

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

  private String defaultProjectUrl;
  private String currentProjectUrl;
  private PwProject currentProject;
  private ViewManager viewManager;

  /**
   * <code>PlanWorks</code> - constructor 
   *
   * @param constantMenus - <code>JMenu[]</code> -
   */                                
  public PlanWorks( JMenu[] constantMenus) {
    super( name, constantMenus);
    currentProjectUrl = null;
    currentProject = null;
    viewManager = null;
    // Closes from title bar 
    addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent e) {
//           if (JOptionPane.showConfirmDialog
//               (PlanWorks.this, "", "Exiting: Save Active Projects?",
//                JOptionPane.YES_NO_OPTION) == 0) {
            try {
              PwProject.saveProjects();
            } catch (Exception excp) {
              System.err.println( excp );
            }
//           }
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
    // default project url
    defaultProjectUrl =
      FileUtils.getCanonicalPath( System.getProperty( "default.project.dir"));
    this.setVisible( true);

    setProjectMenuEnabled( "Create ...", true);
    if (PwProject.listProjects().size() > 0) {
      setProjectMenuEnabled( "Open ...", true);
      setProjectMenuEnabled( "Delete ...", true);
    } else {
      setProjectMenuEnabled( "Open ...", false);
      setProjectMenuEnabled( "Delete ...", false);
    }
  } //end constructor 


  /**
   * <code>getCurrentProjectUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getCurrentProjectUrl() {
    return currentProjectUrl;
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
   * <code>getDefaultProjectUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public String getDefaultProjectUrl() {
    return defaultProjectUrl;
  }

  /**
   * <code>setPlanWorks</code> - needed by TimelineViewTest (JFCUnit Test)
   *
   * @param planWorks - <code>PlanWorks</code> - 
   * @return - <code>PlanWorks</code> - 
   */
  public static void setPlanWorks( PlanWorks planWorksInstance) {
    planWorks = planWorksInstance;
  }

  private List getUrlsLessCurrent() {
    List projectUrls = PwProject.listProjects();
    List urlsLessCurrent = new ArrayList();
    for (int i = 0, n = projectUrls.size(); i < n; i++) {
      String projectUrl = (String) projectUrls.get( i);
      // discard current project
      if (! projectUrl.equals( this.currentProjectUrl)) {
        urlsLessCurrent.add( projectUrl);
      }
    }
    return urlsLessCurrent;
  } // end getUrlsLessCurrent


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
//           if (JOptionPane.showConfirmDialog
//               (PlanWorks.planWorks, "", "Exiting: Save Active Projects?",
//                JOptionPane.YES_NO_OPTION) == 0) {
            try {
              PwProject.saveProjects();
            } catch (Exception excp) {
              System.err.println( excp );
            }
//           }
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
        if (type.equals( "create")) {
          // cache the project info
          try {
            PwProject.saveProjects();
          } catch (Exception excp) {
            System.err.println( excp );
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


  private PwProject createProject() {
    boolean isProjectCreated = false;
    PwProject project = null;
    while (! isProjectCreated) {

      ParseProjectUrl urlMenuItem = new ParseProjectUrl( this);

      String inputUrl = urlMenuItem.getTypedText();
      if (inputUrl == null) { // user selected Cancel
        return null;
      } else {
        if (! (new File( inputUrl)).exists()) {
          JOptionPane.showMessageDialog
            (PlanWorks.this, inputUrl, "URL Not Found", JOptionPane.ERROR_MESSAGE);
          continue;        
        }
      }
      try {
        isProjectCreated = true;
        project = PwProject.createProject( inputUrl);
        currentProjectUrl = inputUrl;
        System.err.println( "Create Project: " + currentProjectUrl);
        this.setTitle( name + "  --  project: " + currentProjectUrl);
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
      }
    }
    return project;
  } // end createProject


  private PwProject openProject() {
    PwProject project = null;
    List projectUrls = PwProject.listProjects();
    // System.err.println( "projectUrls " + projectUrls);
    List urlsLessCurrent = getUrlsLessCurrent();
    // System.err.println( "urlsLessCurrent " + urlsLessCurrent);
    Object[] options = new Object[urlsLessCurrent.size()];
    for (int i = 0, n = urlsLessCurrent.size(); i < n; i++) {
        options[i] = (String) urlsLessCurrent.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( this, "", "Open Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    // System.err.println( "response " + response);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          currentProjectUrl = (String) urlsLessCurrent.get( i);
          System.err.println( "Open Project: " + currentProjectUrl);
          try {
            project = PwProject.getProject( currentProjectUrl);
            this.setTitle( name + "  --  project: " + currentProjectUrl);
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
    List projectUrls = PwProject.listProjects();
    Object[] options = new Object[projectUrls.size()];
    for (int i = 0, n = projectUrls.size(); i < n; i++) {
      options[i] = (String) projectUrls.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( this, "", "Delete Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          String projectUrl = (String) projectUrls.get( i);
          System.out.println( "Delete Project: " + projectUrl);
          try {
              PwProject.getProject( projectUrl).close();
            try {
              PwProject.saveProjects();
            } catch (Exception excp) {
              System.err.println( excp );
            }
            if ((this.currentProjectUrl != null) &&
                this.currentProjectUrl.equals( projectUrl)) {
              viewManager.clearViewSets();
              this.setTitle( name);
              clearSeqPartialPlanViewMenu();
            }
            if (PwProject.listProjects().size() == 0) {
              setProjectMenuEnabled( "Delete ...", false);
              setProjectMenuEnabled( "Open ...", false);
            } else if (getUrlsLessCurrent().size() == 0) {
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
    Iterator seqNamesItr = project.getPlanningSequenceNames().iterator();
    while (seqNamesItr.hasNext()) {
      String seqName = (String) seqNamesItr.next();
      System.err.println( "  planningSequenceName " + seqName);
      JMenu seqMenu = new JMenu( seqName);
      seqPartialPlanViewMenu.add( seqMenu);
      Iterator ppNamesItr = project.getPartialPlanNames( seqName).iterator();
      while (ppNamesItr.hasNext()) {
        String partialPlanName = (String) ppNamesItr.next();
        System.err.println( "    partialPlanName " + partialPlanName);
        JMenu partialPlanMenu = new JMenu( partialPlanName);
        buildViewSubMenu( partialPlanMenu, seqName, partialPlanName);
        seqMenu.add( partialPlanMenu);
      }
    }
    return seqPartialPlanViewMenu;
  } // end buildSeqPartialPlanViewMenu


  private void buildViewSubMenu( JMenu partialPlanMenu, String seqName,
                                 String partialPlanName) {
    SeqPartPlanViewMenuItem constraintNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Constraint Network", seqName, partialPlanName);
    constraintNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "constraintNetworkView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( constraintNetworkViewItem);

    SeqPartPlanViewMenuItem temporalExtentViewItem =
          new SeqPartPlanViewMenuItem( "Temporal Extent", seqName, partialPlanName);
    temporalExtentViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "temporalExtentView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( temporalExtentViewItem);

    SeqPartPlanViewMenuItem temporalNetworkViewItem =
          new SeqPartPlanViewMenuItem( "Temporal Network", seqName, partialPlanName);
    temporalNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "temporalNetworkView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( temporalNetworkViewItem);
    SeqPartPlanViewMenuItem timelineViewItem =
          new SeqPartPlanViewMenuItem( "Timeline", seqName, partialPlanName);
    timelineViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "timelineView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( timelineViewItem);

    SeqPartPlanViewMenuItem tokenGraphViewItem =
          new SeqPartPlanViewMenuItem( "Token Graph", seqName, partialPlanName);
    tokenGraphViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanViewThread
                ( "tokenGraphView", (SeqPartPlanViewMenuItem) e.getSource());
            }});
    partialPlanMenu.add( tokenGraphViewItem);

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
        dynamicMenuBar.validate();
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
          }
          catch(Exception e){}
        }
      }
     ).start();
  }


  class CreatePartialPlanViewThread implements Runnable {

    private String sequenceName;
    private String partialPlanName;
    private PwPartialPlan partialPlan;
    private String viewName;

    public CreatePartialPlanViewThread( String viewName, SeqPartPlanViewMenuItem menuItem) {
      this.sequenceName = menuItem.getSequenceName();
      this.partialPlanName = menuItem.getPartialPlanName();
      this.viewName = viewName;
    }  // end constructor

    public void run() { 
      try {
        String sequenceUrl = currentProjectUrl + System.getProperty( "file.separator") +
          sequenceName;
        PwPlanningSequence planSequence = currentProject.getPlanningSequence( sequenceUrl);
        PwPartialPlan partialPlan = planSequence.getPartialPlan( partialPlanName);
        if (partialPlan == null) {
          partialPlan = planSequence.addPartialPlan( sequenceUrl, partialPlanName);
        }

        renderView( viewName, sequenceName, partialPlanName, partialPlan);

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
      }
    } //end run


    private void renderView( String viewName, String sequenceName, String partialPlanName,
                             PwPartialPlan partialPlan) {
      ViewSet viewSet = viewManager.getViewSet( partialPlan);
      boolean viewExists = false;
      if ((viewSet != null) && viewSet.viewExists( viewName)) {
        viewExists = true;
      }
      if (viewName.equals( "timelineView")) {
        long startTimeMSecs = 0L, stopTimeMSecs = 0L;
        if (! viewExists) {
          System.err.println( "Rendering Timeline View ...");
          startTimeMSecs = (new Date()).getTime();
        }
        MDIInternalFrame viewFrame =
          viewManager.openTimelineView( partialPlan, sequenceName +
                                        System.getProperty( "file.separator") +
                                        partialPlanName);
        if (! viewExists) {
          stopTimeMSecs = (new Date()).getTime();
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

      } else if (viewName.equals( "tokenGraphView")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
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

  } // end class CreatePartialPlanViewThread



  class SeqPartPlanViewMenuItem extends JMenuItem {

    private String sequenceName;
    private String partialPlanName;

    public SeqPartPlanViewMenuItem( String viewName, String seqName, String partialPlanName) {
      super( viewName);
      this.sequenceName = seqName;
      this.partialPlanName = partialPlanName;
    }

    public String getSequenceName() {
      return sequenceName;
    }

    public String getPartialPlanName() {
      return partialPlanName;
    }

  } // end class SeqPartPlanViewMenuItem


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
    osName = System.getProperty("os.name");
    planWorksRoot = System.getProperty( "planworks.root");
    userCollectionName = System.getProperty( "file.separator") + System.getProperty( "user");

    try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep) {
      System.err.println( rnfExcep);
      System.exit( -1);
    }

    planWorks = new PlanWorks( buildConstantMenus());

  } // end main


} // end  class PlanWorks
