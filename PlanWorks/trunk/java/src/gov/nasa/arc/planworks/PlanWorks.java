// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.77 2003-12-11 22:24:48 miatauro Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileFilter;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.db.util.PwSQLFilenameFilter;
import gov.nasa.arc.planworks.mdi.MDIDesktopFrame;
import gov.nasa.arc.planworks.mdi.MDIDesktopPane;
import gov.nasa.arc.planworks.mdi.MDIDynamicMenuBar;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.mdi.SplashWindow;
import gov.nasa.arc.planworks.util.DirectoryChooser;
import gov.nasa.arc.planworks.util.PlannerCommandLineDialog;
import gov.nasa.arc.planworks.util.ProjectNameDialog;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
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

  private static final int DESKTOP_FRAME_WIDTH;// = 900;
  private static final int DESKTOP_FRAME_HEIGHT;// = 750;
  private static final int FRAME_X_LOCATION;// = 100;
  private static final int FRAME_Y_LOCATION;// = 125;

  protected static final String PROJECT_MENU = "Project";
  protected static final String CREATE_MENU_ITEM = "Create ...";
  protected static final String OPEN_MENU_ITEM = "Open ...";
  protected static final String DELETE_MENU_ITEM = "Delete ...";
  protected static final String NEWSEQ_MENU_ITEM = "New Sequence ...";
  protected static final String ADDSEQ_MENU_ITEM = "Add Sequence ...";
  protected static final String DELSEQ_MENU_ITEM = "Delete Sequence ...";
  protected static final String CREATE = "create";
  protected static final String OPEN = "open";

  public static final String PLANSEQ_MENU = "Planning Sequence";
  public static final Map viewClassNameMap;
  public static final String CONSTRAINT_NETWORK_VIEW = "Constraint Network View";
  public static final String TEMPORAL_EXTENT_VIEW    = "Temporal Extent View";
  public static final String TEMPORAL_NETWORK_VIEW   = "Temporal Network View";
  public static final String TIMELINE_VIEW           = "Timeline View";
  public static final String TOKEN_NETWORK_VIEW      = "Token Network View";
  public static final String TRANSACTION_VIEW      = "Transaction View";
  public static final List PARTIAL_PLAN_VIEW_LIST;

  public static final String SEQUENCE_STEPS_VIEW     = "Sequence Steps View";
  public static final String MODEL_RULES_VIEW     = "Model Rules View";
  public static final List SEQUENCE_VIEW_LIST;

  static {
    GraphicsDevice [] devices = 
      GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    DESKTOP_FRAME_WIDTH = (int)(devices[0].getDisplayMode().getWidth() * (5./6.));
    DESKTOP_FRAME_HEIGHT = (int)(devices[0].getDisplayMode().getHeight() * 0.9);
    INTERNAL_FRAME_WIDTH = (int)(DESKTOP_FRAME_WIDTH * 0.75);
    INTERNAL_FRAME_HEIGHT = (int)(DESKTOP_FRAME_HEIGHT * 0.75);
    FRAME_X_LOCATION = (devices[0].getDisplayMode().getWidth() - DESKTOP_FRAME_WIDTH) / 2;
    FRAME_Y_LOCATION = devices[0].getDisplayMode().getHeight() - DESKTOP_FRAME_HEIGHT;
    System.err.println(FRAME_X_LOCATION + " " + FRAME_Y_LOCATION);

    viewClassNameMap = new HashMap();
    viewClassNameMap.put
      ( CONSTRAINT_NETWORK_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView");
    viewClassNameMap.put
      ( TEMPORAL_EXTENT_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView");
//     viewClassNameMap.put
//       ( TEMPORAL_NETWORK_VIEW,
//         "gov.nasa.arc.planworks.viz.partialPlan.temporalNetwork.TemporalNetworkView");
    viewClassNameMap.put
      ( TIMELINE_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView");
    viewClassNameMap.put
      ( TOKEN_NETWORK_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView");
    viewClassNameMap.put
      ( TRANSACTION_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.transaction.TransactionView");

    viewClassNameMap.put
      ( SEQUENCE_STEPS_VIEW,
        "gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView");
    viewClassNameMap.put
      ( MODEL_RULES_VIEW,
        "gov.nasa.arc.planworks.viz.sequence.modelRules.ModelRulesView");

    PARTIAL_PLAN_VIEW_LIST = new ArrayList();
    PARTIAL_PLAN_VIEW_LIST.add( CONSTRAINT_NETWORK_VIEW);
    PARTIAL_PLAN_VIEW_LIST.add( TEMPORAL_EXTENT_VIEW);
    PARTIAL_PLAN_VIEW_LIST.add( TIMELINE_VIEW);
    PARTIAL_PLAN_VIEW_LIST.add( TOKEN_NETWORK_VIEW);
    PARTIAL_PLAN_VIEW_LIST.add( TRANSACTION_VIEW);

    SEQUENCE_VIEW_LIST = new ArrayList();
    SEQUENCE_VIEW_LIST.add( SEQUENCE_STEPS_VIEW);
    SEQUENCE_VIEW_LIST.add( MODEL_RULES_VIEW);
  }

  /**
   * constant <code>INTERNAL_FRAME_WIDTH</code>
   *
   */
    public static final int INTERNAL_FRAME_WIDTH;// = 400;

  /**
   * constant <code>INTERNAL_FRAME_HEIGHT</code>
   *
   */
    public static final int INTERNAL_FRAME_HEIGHT; // = 350;

  /**
   * variable <code>name</code> - make it accessible to JFCUnit tests
   *
   */
  public static String name;

  /**
   * variable <code>isMaxScreen</code> - make it accessible to JFCUnit tests
   *
   */
  public static boolean isMaxScreen;

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
  public static List supportedViewNames; // List of String
  public static JMenu projectMenu;
  public PwProject currentProject;
  public Map sequenceStepsViewMap;

  protected final DirectoryChooser sequenceDirChooser;
  protected final PlannerCommandLineDialog executeDialog;
  protected static String sequenceParentDirectory; // pathname
  protected static File [] sequenceDirectories; // directory name

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

  protected String currentProjectName;
  protected ViewManager viewManager;
  protected Map sequenceNameMap; // postfixes (1), etc for duplicate seq names


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
    if (isMaxScreen) {
      Rectangle maxRectangle =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
      this.setSize( (int) maxRectangle.getWidth(), (int) maxRectangle.getHeight());
      this.setLocation( 0, 0);
    } else {
      this.setSize( DESKTOP_FRAME_WIDTH, DESKTOP_FRAME_HEIGHT);
      this.setLocation( FRAME_X_LOCATION, FRAME_Y_LOCATION);
    }
    Container contentPane = getContentPane();
    for (int i = 0, n = contentPane.getComponentCount(); i < n; i++) {
      // System.err.println( "i " + i + " " +
      //                    contentPane.getComponent( i).getClass().getName());
      if (contentPane.getComponent(i) instanceof MDIDesktopPane) {
        ((MDIDesktopPane) contentPane.getComponent(i)).
          setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
        break;
      }
    }
    supportedViewNames = Utilities.sortStringKeySet( viewClassNameMap);
    this.setVisible( true);
    if(usingSplash) {
      this.toBack();
    }

    setProjectMenuEnabled(CREATE_MENU_ITEM, true);
    setProjectMenuEnabled( ADDSEQ_MENU_ITEM, false);
    setProjectMenuEnabled(DELSEQ_MENU_ITEM, false);
    setProjectMenuEnabled(NEWSEQ_MENU_ITEM, false);
    if ((PwProject.listProjects() != null) && (PwProject.listProjects().size() > 0)) {
      setProjectMenuEnabled( OPEN_MENU_ITEM, true);
      setProjectMenuEnabled( DELETE_MENU_ITEM, true);
    } else {
      setProjectMenuEnabled( OPEN_MENU_ITEM, false);
      setProjectMenuEnabled( DELETE_MENU_ITEM, false);
    }
    projectMenu.setEnabled(true);
    windowBuilt = true;
    if(usingSplash) {
      this.toFront();
    }

    ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
//     System.err.println( "PlanWorks.toolTipManager InitialDelay " +
//                         toolTipManager.getInitialDelay() + " DismissDelay " +
//                         toolTipManager.getDismissDelay() + " ReshowDelay " +
//                         toolTipManager.getReshowDelay());
    // milliseconds
    toolTipManager.setInitialDelay( 100); // default 750
    // toolTipManager.setDismissDelay( 8000); // default 4000
    toolTipManager.setReshowDelay( 100); // default 500
    executeDialog = new PlannerCommandLineDialog(this);
    executeDialog.hide();
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

  protected List getProjectsLessCurrent() {
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


  protected static void setProjectMenuEnabled( String textName, boolean isEnabled) {
    for (int i = 0, n = projectMenu.getItemCount(); i < n; i++) {
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem( i).getText().equals( textName))) {
        projectMenu.getItem( i).setEnabled( isEnabled);
        break;
      }
    }
  } // end setProjectMenuEnabled

  private String getSequenceMenuItemName( String seqName, JMenu planSequenceViewMenu) {
    int nameCount = 0;
    // System.err.println( "getSequenceMenuItemName: seqName " + seqName);
    // check for e.g. monkey1066690986042
    seqName = seqName.substring( 0, seqName.length() - DbConstants.LONG_INT_LENGTH);
    for (int i = 0; i < planSequenceViewMenu.getItemCount(); i++) {
      JMenuItem item = planSequenceViewMenu.getItem(i);
      String itemName = item.getText();
      int index = itemName.indexOf(" (");
      if (index != -1) {
        itemName = itemName.substring(0, index);
      }
      if (itemName.equals( seqName)) {
        nameCount++;
      }
    }
    if (nameCount > 0) {
      seqName = seqName.concat(" (").concat(Integer.toString(nameCount)).concat(")");
    }
    // System.err.println( "   seqName " + seqName);
    return seqName;
  } // end getSequenceMenuItemName

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
    JMenuItem createProjectItem = new JMenuItem( CREATE_MENU_ITEM);
    JMenuItem openProjectItem = new JMenuItem( OPEN_MENU_ITEM);
    JMenuItem deleteProjectItem = new JMenuItem( DELETE_MENU_ITEM);
    JMenuItem newSequenceItem = new JMenuItem( NEWSEQ_MENU_ITEM);
    JMenuItem addSequenceItem = new JMenuItem( ADDSEQ_MENU_ITEM);
    JMenuItem deleteSequenceItem = new JMenuItem(DELSEQ_MENU_ITEM);
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
    newSequenceItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          PlanWorks.planWorks.newSequenceThread();
        }});
    projectMenu.add(newSequenceItem);
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
    sequenceStepsViewMap = new HashMap();
    Thread thread = new InstantiateProjectThread(type);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    //new InstantiateProjectThread( type).start();
  }

  private void deleteProjectThread() {
    Thread thread = new DeleteProjectThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    //new DeleteProjectThread().start();
  }

  private void newSequenceThread() {
    Thread thread = new NewSequenceThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    //new NewSequenceThread().start();
  }

  private void addSequenceThread() {
    Thread thread = new AddSequenceThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    //new AddSequenceThread().start();
  }

  private void deleteSequenceThread() {
    Thread thread = new DeleteSequenceThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    //new DeleteSequenceThread().start();
  }

  /**
   * <code>getPlanSequence</code>
   *
   * @return - <code>PwPlanningSequence</code> - 
   */
  public PwPlanningSequence getPlanSequence( PwPartialPlan partialPlan) {
    PwPlanningSequence planSequence = null;
    String partialPlanUrl = partialPlan.getUrl();
    String seqUrl =
      partialPlanUrl.substring( 0,
                                partialPlanUrl.lastIndexOf
                                ( System.getProperty( "file.separator")));
    try {
      planSequence = currentProject.getPlanningSequence( seqUrl);
    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }
    return planSequence;
  } // end getPlanSequence


  public void addPlanSeqViewMenu( PwProject project, JMenu planSeqMenu) {
    // Create Dynamic Cascading Seq/PartialPlan/View Menu
    MDIDynamicMenuBar dynamicMenuBar = (MDIDynamicMenuBar) PlanWorks.this.getJMenuBar();
    if (planSeqMenu == null) {
      dynamicMenuBar.addConstantMenu
        ( buildPlanSeqViewMenu( project, planSeqMenu));
    } else {
      buildPlanSeqViewMenu( project, planSeqMenu);
    }
    dynamicMenuBar.validate();
  } // end addSeqPartialPlanMenu


  protected JMenu buildPlanSeqViewMenu( PwProject project, JMenu planSeqViewMenu) {
    if (planSeqViewMenu == null) {
      planSeqViewMenu = new JMenu( PLANSEQ_MENU);
    }
    planSeqViewMenu.removeAll();
    sequenceNameMap = new HashMap();
    //System.err.println( "buildPlanSeqViewMenu");
    List planSeqNames = project.listPlanningSequences();
    Collections.sort(planSeqNames, new SeqNameComparator());
    Iterator seqUrlsItr = planSeqNames.iterator();
    while (seqUrlsItr.hasNext()) {
      String seqUrl = (String) seqUrlsItr.next();
      String seqName = getSequenceMenuItemName( Utilities.getUrlLeaf( seqUrl),
                                                planSeqViewMenu);
      //System.err.println( "  sequenceName " + seqName);
      sequenceNameMap.put(seqUrl, seqName);
      SequenceViewMenuItem planDbSizeItem =
        new SequenceViewMenuItem( seqName, seqUrl, seqName);
      planDbSizeItem.addActionListener( new ActionListener() {
          public void actionPerformed( ActionEvent evt) {
            PlanWorks.planWorks.createSequenceViewThread
              ( SEQUENCE_STEPS_VIEW, (SequenceViewMenuItem) evt.getSource());
          }
        });
      planSeqViewMenu.add( planDbSizeItem);
    }
    return planSeqViewMenu;
  } // end buildPlanSeqViewMenu


  private void createSequenceViewThread( String viewName, SequenceViewMenuItem menuItem) {
    Thread thread = new CreateSequenceViewThread(viewName, menuItem);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    //new CreateSequenceViewThread( viewName, menuItem).start();
  } // end createSequenceViewThread

  private final void createDirectoryChooser() {
    if (! System.getProperty( "default.sequence.dir").equals( "")) {
      sequenceDirChooser.setCurrentDirectory
        ( new File( System.getProperty( "default.sequence.dir")));
    }
    sequenceDirChooser.setDialogTitle
      ( "Select Sequence Directory of Partial Plan Directory(ies)");
    sequenceDirChooser.setMultiSelectionEnabled( true);
    sequenceDirChooser.getOkButton().addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String dirChoice = sequenceDirChooser.getCurrentDirectory().getAbsolutePath();
          File [] seqDirs = sequenceDirChooser.getSelectedFiles();
//           System.err.println( "PlanWorks sequence parent directory" + dirChoice);
//           System.err.println( "sequenceDirectories");
//           for (int i = 0, n = seqDirs.length; i < n; i++) {
//             System.err.println( "  " + seqDirs[i].getName());
//           }
          if ((dirChoice != null) && (dirChoice.length() > 0) &&
              (new File( dirChoice)).isDirectory() &&
              (seqDirs.length != 0)) {
            PlanWorks.sequenceParentDirectory = dirChoice;
            PlanWorks.sequenceDirectories = seqDirs;
            sequenceDirChooser.approveSelection();
          } else {
            String seqDir = "<null>";
            if (seqDirs.length != 0) {
              seqDir = seqDirs[0].getName();
            }
            JOptionPane.showMessageDialog
              ( PlanWorks.this, "`" + dirChoice +
                System.getProperty( "file.separator") +  seqDir +
                "'\nis not a valid sequence directory.",
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
     * accept - Accept all files, and directories which are not partial plan 
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
          String [] fileNames = file.list(new PwSQLFilenameFilter());
          if (fileNames.length == DbConstants.NUMBER_OF_PP_FILES) {
            isValid = false;
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
    String maxScreenValue = "false";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        name = args[argc];
      } else if (argc == 1) {
         maxScreenValue = args[argc];
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit(-1);
      }
    }
    osType = System.getProperty("os.type");
    // System.err.println( "osType " + osType);
    planWorksRoot = System.getProperty( "planworks.root");
    isMaxScreen = false;
    if (maxScreenValue.equals( "true")) {
      isMaxScreen = true;
    }

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice [] gs = ge.getScreenDevices();
    for(int i = 0; i < gs.length; i++) {
	DisplayMode dm = gs[i].getDisplayMode();
	System.err.println(dm.getWidth() + " " + dm.getHeight());
    }
    
    planWorks = new PlanWorks( buildConstantMenus());
  } // end main

  private class SeqNameComparator implements Comparator {
    public SeqNameComparator() {
    }
    public int compare(Object o1, Object o2) {
      String s1 = Utilities.getUrlLeaf((String) o1);
      String s2 = Utilities.getUrlLeaf((String) o2);
      return s1.compareTo(s2);
    }
    public boolean equals(Object o1, Object o2) {
      String s1 = Utilities.getUrlLeaf((String)o1);
      String s2 = Utilities.getUrlLeaf((String)o2);
      return s1.equals(s2);
    }
  }


} // end  class PlanWorks
        
