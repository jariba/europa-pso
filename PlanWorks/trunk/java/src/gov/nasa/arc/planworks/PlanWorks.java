// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.1 2003-06-08 00:14:07 taylor Exp $
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

  private String projectUrl;
  private PwProject project;

  /**
   * <code>PlanWorks</code> - constructor 
   *
   * @param name - <code>String</code> - 
   * @param constantMenus - <code>JMenu[]</code> - 
   */
  public PlanWorks( String name, JMenu[] constantMenus) {
    super( name, constantMenus);
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
    projectUrl = FileUtils.getCanonicalPath( System.getProperty( "default.project.dir"));
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
          PlanWorks.planWorks.createProjectThread();
        }});
    projectMenu.add( createProjectItem);
    openProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.openProject();
        }});
    projectMenu.add( openProjectItem);
    deleteProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PlanWorks.planWorks.deleteProject();
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

  private void createProjectThread() {
    new CreateProjectThread().start();
  }

  class CreateProjectThread extends Thread {

    public CreateProjectThread() {
    }  // end constructor

    public void run() {
      PwProject createdProject = createProject();
      if (createdProject != null) {
        project = createdProject;
        addSeqPartialPlanMenu( createdProject);
      }
    } //end run

  } // end class CreateProjectThread


  private PwProject createProject() {
    boolean isProjectCreated = false;
    PwProject project = null;
    while (! isProjectCreated) {

      ParseProjectUrl urlMenuItem = new ParseProjectUrl();

      String inputUrl = urlMenuItem.getTypedText();
      System.err.println( "createProject: url " + inputUrl);
      if (inputUrl == null) { // user selected Cancel
        return null;
      } else {
        projectUrl = urlMenuItem.getTypedText();
        if (! (new File( inputUrl)).exists()) {
          JOptionPane.showMessageDialog
            (PlanWorks.this, inputUrl, "URL Not Found", JOptionPane.ERROR_MESSAGE);
          continue;        
        }
      }
      try {
        isProjectCreated = true;
        project = PwProject.createProject( projectUrl);
        this.setTitle( name + "  --  project: " + projectUrl);
        setProjectMenuEnabled( "Delete ...", true);
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


  private void openProject() {
    List projectUrls = PwProject.listProjects();
    Object[] options = new Object[projectUrls.size()];
    for (int i = 0, n = projectUrls.size(); i < n; i++) {
      options[i] = (String) projectUrls.get( i);
    }
    Object response = JOptionPane.showInputDialog
      ( this, "", "Open Project", JOptionPane.QUESTION_MESSAGE, null,
        options, options[0]);
    if (response instanceof String) {
      for (int i = 0, n = options.length; i < n; i++) {
        if (((String) options[i]).equals( response)) {
          projectUrl = (String) projectUrls.get( i);
          System.out.println( "Open Project: " + projectUrl);
          try {
            project = PwProject.openProject( projectUrl);
            this.setTitle( name + "  --  project: " + projectUrl);
            setProjectMenuEnabled( "Delete ...", true);
          } catch (ResourceNotFoundException rnfExcep) {
            // System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
            int index = rnfExcep.getMessage().indexOf( ":");
            JOptionPane.showMessageDialog
              (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
               "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
            System.err.println( rnfExcep);
          }
          addSeqPartialPlanMenu( project);
          break;
        }
      }
    } 
    // JOptionPane.showInputDialog returns null if user selected "cancel"
  } // end openProject


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
            if (PwProject.listProjects().size() == 0) {
              setProjectMenuEnabled( "Delete ...", false);
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


  private void addSeqPartialPlanMenu( PwProject project) {
    // Create Dynamic Cascading Seq/PartialPlan Menu
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
    JMenu seqPartialPlanMenu = buildSeqPartialPlanMenu( project);
    if (seqPartialPlanMenu != null) {
      dynamicMenuBar.addConstantMenu( seqPartialPlanMenu);
      dynamicMenuBar.validate();
    }    
  } // end addSeqPartialPlanMenu


  private void addPartialPlanViewsMenu( PwPartialPlan partialPlan) {
    // Create Dynamic Cascading PartialPlan View Menu
    MDIDynamicMenuBar dynamicMenuBar =
      (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
    JMenu partialPlanViewsMenu = buildPartialPlanViewsMenu( partialPlan);
    if (partialPlanViewsMenu != null) {
      dynamicMenuBar.addConstantMenu( partialPlanViewsMenu);
      dynamicMenuBar.validate();
    }    
  } // end addSeqPartialPlanMenu


  private JMenu buildSeqPartialPlanMenu( PwProject project) {
    JMenu seqPartialPlanMenu = new JMenu( "Partial Plan");
    System.err.println( "buildSeqPartialPlanMenu");
    Iterator seqNamesItr = project.getPlanningSequenceNames().iterator();
    while (seqNamesItr.hasNext()) {
      String seqName = (String) seqNamesItr.next();
      System.err.println( "  planningSequenceName " + seqName);
      JMenu seqMenu = new JMenu( seqName);
      seqPartialPlanMenu.add( seqMenu);
      Iterator ppNamesItr = project.getPartialPlanNames( seqName).iterator();
      while (ppNamesItr.hasNext()) {
        String partialPlanName = (String) ppNamesItr.next();
        System.err.println( "    partialPlanName " + partialPlanName);
        SeqPartialPlanMenuItem partialPlanItem =
          new SeqPartialPlanMenuItem( seqName, partialPlanName);
        partialPlanItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createPartialPlanThread
                ( (SeqPartialPlanMenuItem) e.getSource());
            }});
        seqMenu.add( partialPlanItem);
      }
    }
    return seqPartialPlanMenu;
  } // end buildSeqPartialPlanMenu


  private JMenu buildPartialPlanViewsMenu( PwPartialPlan partialPlan) {
    JMenu partialPlanViewsMenu = new JMenu( "Views");
    // System.err.println( "buildPartialPlanViewsMenu");
    PartialPlanViewMenuItem timelineViewItem =
          new PartialPlanViewMenuItem( "Timeline", partialPlan);
    timelineViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createViewThread
                ( "timeline", (PartialPlanViewMenuItem) e.getSource());
            }});
    partialPlanViewsMenu.add( timelineViewItem);

    PartialPlanViewMenuItem tokenGraphViewItem =
          new PartialPlanViewMenuItem( "Token Graph", partialPlan);
    tokenGraphViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createViewThread
                ( "tokenGraph", (PartialPlanViewMenuItem) e.getSource());
            }});
    partialPlanViewsMenu.add( tokenGraphViewItem);

    PartialPlanViewMenuItem temporalExtentViewItem =
          new PartialPlanViewMenuItem( "Temporal Extent", partialPlan);
    temporalExtentViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createViewThread
                ( "temporalExtent", (PartialPlanViewMenuItem) e.getSource());
            }});
    partialPlanViewsMenu.add( temporalExtentViewItem);

    PartialPlanViewMenuItem constraintNetworkViewItem =
          new PartialPlanViewMenuItem( "Constraint Network", partialPlan);
    constraintNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createViewThread
                ( "constraintNetwork", (PartialPlanViewMenuItem) e.getSource());
            }});
    partialPlanViewsMenu.add( constraintNetworkViewItem);

    PartialPlanViewMenuItem temporalNetworkViewItem =
          new PartialPlanViewMenuItem( "Temporal Network", partialPlan);
    temporalNetworkViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e) {
              PlanWorks.planWorks.createViewThread
                ( "temporalNetwork", (PartialPlanViewMenuItem) e.getSource());
            }});
    partialPlanViewsMenu.add( temporalNetworkViewItem);
    return partialPlanViewsMenu;
  } // end buildPartialPlanViewsMenu




  private void createPartialPlanThread( SeqPartialPlanMenuItem menuItem) {
    new CreatePartialPlanThread( menuItem).start();
  }


  class CreatePartialPlanThread extends Thread {

    private String seqName;
    private String partialPlanName;

    public CreatePartialPlanThread( SeqPartialPlanMenuItem menuItem) {
      this.seqName = menuItem.getSeqName();
      this.partialPlanName = menuItem.getPartialPlanName();
    }  // end constructor

    public void run() {
      try {
        String seqUrl = projectUrl + "/" + seqName;
        PwPlanningSequence planSequence = project.getPlanningSequence( seqUrl);
        PwPartialPlan partialPlan =
          planSequence.addPartialPlan( seqUrl, partialPlanName);

        addPartialPlanViewsMenu( partialPlan);

      } catch (ResourceNotFoundException rnfExcep) {
        int index = rnfExcep.getMessage().indexOf( ":");
        JOptionPane.showMessageDialog
          (PlanWorks.this, rnfExcep.getMessage().substring( index + 1),
           "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
        System.err.println( rnfExcep);
      }
    } //end run

  } // end class CreatePartialPlanThread



  private void createViewThread( String viewName, PartialPlanViewMenuItem menuItem) {
    new CreateViewThread(viewName, menuItem).start();
  }


  class CreateViewThread extends Thread {

    private String viewName;
    private PwPartialPlan partialPlan;
    private String sequenceName;
    private String partialPlanName;

    public CreateViewThread( String viewName, PartialPlanViewMenuItem menuItem) {
      this.viewName = viewName;
      this.partialPlan = menuItem.getPartialPlan();
      int indx = partialPlan.getUrl().lastIndexOf( "/");
      this.partialPlanName = partialPlan.getUrl().substring( indx + 1);
      String tmp = partialPlan.getUrl().substring( 0, indx);
      indx = tmp.lastIndexOf( "/");
      sequenceName = tmp.substring( indx + 1);
    }  // end constructor

    public void run() {
      if (viewName.equals( "timeline")) {
        boolean resizable = true, closable = true, maximizable = true;
        boolean iconifiable = true;
        MDIInternalFrame viewFrame =
          PlanWorks.this.createFrame( "Timeline View of " + sequenceName +
                                      "/" + partialPlanName,
                                      resizable, closable, maximizable, iconifiable);
        viewFrame.setSize( INTERNAL_FRAME_WIDTH, INTERNAL_FRAME_HEIGHT);
        viewFrame.setLocation( FRAME_X_LOCATION, FRAME_Y_LOCATION);
        viewFrame.setVisible( true);
        // make associated menus appear
        try {
          viewFrame.setSelected( false);
          viewFrame.setSelected( true);
        } catch (PropertyVetoException excp) {
        }

        long startTimeMSecs = (new Date()).getTime();
        TimelineView timelineView = new TimelineView( partialPlan);
        Container contentPane = viewFrame.getContentPane();
        contentPane.add( timelineView);
        contentPane.validate(); // IMPORTANT

        long stopTimeMSecs = (new Date()).getTime();
        String timeString = "Render Timeline View \n   ... elapsed time: " +
          //       writeTime( (stopTimeMSecs - startTimeMSecs)) + " seconds.";
          (stopTimeMSecs - startTimeMSecs) + " msecs.";
        System.err.println( timeString);

      } else if (viewName.equals( "tokenGraph")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else if (viewName.equals( "temporalExtent")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else if (viewName.equals( "constraintNetwork")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else if (viewName.equals( "temporalNetwork")) {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog
          (PlanWorks.this, viewName, "View Not Supported", 
           JOptionPane.INFORMATION_MESSAGE);
      }
    } //end run

  } // end class CreateViewThread



  class SeqPartialPlanMenuItem extends JMenuItem {

    private String seqName;
    private String partialPlanName;

    public SeqPartialPlanMenuItem( String seqName, String partialPlanName) {
      super( partialPlanName);
      this.seqName = seqName;
      this.partialPlanName = partialPlanName;
    }

    public String getSeqName() {
      return seqName;
    }

    public String getPartialPlanName() {
      return partialPlanName;
    }

  } // end class SeqPartialPlanMenuItem


  class PartialPlanViewMenuItem extends JMenuItem {

    private PwPartialPlan partialPlan;

    public PartialPlanViewMenuItem( String viewName, PwPartialPlan partialPlan) {
      super( viewName);
      this.partialPlan = partialPlan;
    }

    public PwPartialPlan getPartialPlan() {
      return partialPlan;
    }

  } // end class PartialPlanViewMenuItem


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
      textField.setText( String.valueOf( projectUrl));
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

    planWorks = new PlanWorks( name, buildConstantMenus());

  } // end main


} // end  class PlanWorks
