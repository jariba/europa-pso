// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.4 2003-06-12 19:57:20 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent; 
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.MDIMenu;
import gov.nasa.arc.planworks.mdi.MDIWindowButtonBar;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>PlanWorks</code> - 
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

  private static String name;
  private static String osName;
  private static String userCollectionName; // e.g. /wtaylor
  private static String planWorksRoot;

  private static PlanWorks planWorks;
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
    // disable Project menu until init is done
    setProjectMenuEnabled( "Create ...", false);
    setProjectMenuEnabled( "Open ...", false);
    setProjectMenuEnabled( "Delete ...", false);
    // default project url
    defaultProjectUrl =
      FileUtils.getCanonicalPath( System.getProperty( "default.project.dir"));
    this.setVisible( true);

    try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep) {
      System.err.println( rnfExcep);
      System.exit( -1);
    }
    setProjectMenuEnabled( "Create ...", true);
    if (PwProject.listProjects().size() > 0) {
      setProjectMenuEnabled( "Open ...", true);
    }
  } //end constructor 


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
  }


  private static void setProjectMenuEnabled( String textName, boolean isEnabled) {
    for (int i = 0, n = projectMenu.getItemCount(); i < n; i++) {
      if (projectMenu.getItem( i).getText().equals( textName)) {
        projectMenu.getItem( i).setEnabled( isEnabled);
        break;
      }
    }
  } // end setProjectMenuEnabled


  private static JMenu[] buildConstantMenus() {
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
//     JMenuItem saveProjectItem = new JMenuItem( "Save");
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
//     saveProjectItem.addActionListener( new ActionListener() {
//         public void actionPerformed( ActionEvent evt) {
//           try {
//             PlanWorks.this.project.save();
//           } catch (Exception excp) {
//             System.err.println( excp ); System.exit( 0); }}});
//     projectMenu.add( saveProjectItem);


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
        // cache the project info
        try {
          PwProject.saveProjects();
        } catch (Exception excp) {
          System.err.println( excp );
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

      ParseProjectUrl urlMenuItem = new ParseProjectUrl();

      String inputUrl = urlMenuItem.getTypedText();
      if (inputUrl == null) { // user selected Cancel
        return null;
      } else {
        currentProjectUrl = urlMenuItem.getTypedText();
        if (! (new File( inputUrl)).exists()) {
          JOptionPane.showMessageDialog
            (PlanWorks.this, inputUrl, "URL Not Found", JOptionPane.ERROR_MESSAGE);
          continue;        
        }
      }
      try {
        isProjectCreated = true;
        System.err.println( "Create Project: " + currentProjectUrl);
        project = PwProject.createProject( currentProjectUrl);
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
    System.err.println( "response " + response);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          currentProjectUrl = (String) urlsLessCurrent.get( i);
          System.err.println( "Open Project: " + currentProjectUrl);
          try {
            project = PwProject.openProject( currentProjectUrl);
            this.setTitle( name + "  --  project: " + currentProjectUrl);
            setProjectMenuEnabled( "Delete ...", true);
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
            if (this.currentProjectUrl.equals( projectUrl)) {
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
          } catch (Exception excep) {
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




  private void createPartialPlanViewThread( String viewName,
                                            SeqPartPlanViewMenuItem menuItem) {
    new CreatePartialPlanViewThread( viewName, menuItem).start();
  }


  class CreatePartialPlanViewThread extends Thread {

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
        String sequenceUrl = currentProjectUrl + "/" + sequenceName;
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
          viewManager.openTimelineView( partialPlan,
                                        "Timeline View of " + sequenceName +
                                        "/" + partialPlanName);
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
        } catch (PropertyVetoException excp) {};

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


  class ParseProjectUrl extends JDialog {

    private String typedText = null;
    private JOptionPane optionPane;
    private JTextField textField;
    private String btnString1;
    private String btnString2;

    /**
     * <code>ParseProjectUrl</code> - constructor 
     *
     */
    public ParseProjectUrl () {
      // modal dialog - blocks other activity
      super( planWorks, true);

      setTitle( "Create Project");
      final String msgString1 = "url (string)";
      textField = new JTextField( 30);
      Object[] array = {msgString1, textField};
      btnString1 = "Enter";
      btnString2 = "Cancel";
      Object[] options = {btnString1, btnString2};
      // current value
      String url = PlanWorks.this.currentProjectUrl;
      if (url == null) {
        url =  PlanWorks.this.defaultProjectUrl;
      }
      textField.setText( String.valueOf( url));
      optionPane = new JOptionPane
        ( array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
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

      textField.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent e) {
            optionPane.setValue( btnString1);
          }
        });

      addInputListener();

      // size dialog appropriately
      pack();
      // place it in center of JFrame
      Point planWorksLocation = planWorks.getLocation();
      setLocation( (int) (planWorksLocation.getX() +
                          planWorks.getSize().getWidth() / 2 -
                          this.getPreferredSize().getWidth() / 2),
                   (int) (planWorksLocation.getY() +
                          planWorks.getSize().getHeight() / 2 -
                          this.getPreferredSize().getHeight() / 2));
      setBackground( ColorMap.getColor( "gray60"));
      setVisible( true);
    } // end constructor


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
                typedText = textField.getText();
                // we're done; dismiss the dialog
                setVisible( false);
              } else { // user closed dialog or clicked cancel
                typedText = null;
                setVisible( false);
              }
            }
          }
        });
    } // end addInputListener

    /**
     * <code>getTypedText</code>
     *
     * @return - <code>String</code> - 
     */
    public String getTypedText() {
      return typedText;
    }

  } // end class ParseProjectUrl



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
    userCollectionName = "/" + System.getProperty( "user");

    planWorks = new PlanWorks( buildConstantMenus());

  } // end main


} // end  class PlanWorks
