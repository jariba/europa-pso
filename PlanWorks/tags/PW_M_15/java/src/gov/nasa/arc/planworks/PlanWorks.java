// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanWorks.java,v 1.100 2004-05-28 20:21:14 taylor Exp $
//
package gov.nasa.arc.planworks;

import java.awt.Color;
import java.awt.Container;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
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
import gov.nasa.arc.planworks.util.BooleanFunctor;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.DirectoryChooser;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.FunctorFactory;
import gov.nasa.arc.planworks.util.PlannerCommandLineDialog;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UnaryFunctor;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.viewMgr.ViewManager;


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
  public static final Map VIEW_CLASS_NAME_MAP;

  public static final String FILE_MENU = "File";
  public static final String EXIT_MENU_ITEM = "Exit";

  public static final String PROJECT_MENU = "Project";
  public static final String CREATE_MENU_ITEM = "Create ...";
  public static final String OPEN_MENU_ITEM = "Open ...";
  public static final String DELETE_MENU_ITEM = "Delete ...";
  public static final String NEWSEQ_MENU_ITEM = "New Sequence ...";
  public static final String ADDSEQ_MENU_ITEM = "Add Sequence ...";
  public static final String DELSEQ_MENU_ITEM = "Delete Sequence ...";
  public static final String CREATE = "create";
  public static final String OPEN = "open";

  public static final String PLANSEQ_MENU = "Planning Sequence";

  public static final String WINDOW_MENU = "Window";
  public static final String TILE_WINDOWS_MENU_ITEM = "Tile Windows";
  public static final String CASCADE_WINDOWS_MENU_ITEM = "Cascade Windows";

  static {
    GraphicsDevice [] devices = 
      GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    DESKTOP_FRAME_WIDTH = (int)(devices[0].getDisplayMode().getWidth() * (5./6.));
    DESKTOP_FRAME_HEIGHT = (int)(devices[0].getDisplayMode().getHeight() * 0.9);
    INTERNAL_FRAME_WIDTH = (int)(DESKTOP_FRAME_WIDTH * 0.75);
    INTERNAL_FRAME_HEIGHT = (int)(DESKTOP_FRAME_HEIGHT * 0.75);
    FRAME_X_LOCATION = (devices[0].getDisplayMode().getWidth() - DESKTOP_FRAME_WIDTH) / 2;
    FRAME_Y_LOCATION = devices[0].getDisplayMode().getHeight() - DESKTOP_FRAME_HEIGHT;

    // System.err.println( FRAME_X_LOCATION + " " + FRAME_Y_LOCATION);

    VIEW_CLASS_NAME_MAP = new HashMap();
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.CONSTRAINT_NETWORK_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.TEMPORAL_EXTENT_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.temporalExtent.TemporalExtentView");
//     VIEW_CLASS_NAME_MAP.put
//       ( ViewConstants.TEMPORAL_NETWORK_VIEW,
//         "gov.nasa.arc.planworks.viz.partialPlan.temporalNetwork.TemporalNetworkView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.TIMELINE_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.timeline.TimelineView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.TOKEN_NETWORK_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.tokenNetwork.TokenNetworkView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.DB_TRANSACTION_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.dbTransaction.DBTransactionView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.RESOURCE_PROFILE_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.resourceProfile.ResourceProfileView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.RESOURCE_TRANSACTION_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.resourceTransaction.ResourceTransactionView");
    // not in map, since it is created from nodes in views, not from other views
//     VIEW_CLASS_NAME_MAP.put
//       ( ViewConstants.NAVIGATOR_VIEW,
//         "gov.nasa.arc.planworks.viz.partialPlan.navigator.NavigatorView");
    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.DECISION_VIEW,
        "gov.nasa.arc.planworks.viz.partialPlan.decision.DecisionView");

    VIEW_CLASS_NAME_MAP.put
      ( ViewConstants.SEQUENCE_STEPS_VIEW,
        "gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView");
    // not in map, since it is created by M-R on SequenceStepsView
//     VIEW_CLASS_NAME_MAP.put
//       ( ViewConstants.OBJECT_TREE_VIEW,
//         "gov.nasa.arc.planworks.viz.sequence.objectTree.ObjectTreeView");
    // not implemented yet
//     VIEW_CLASS_NAME_MAP.put
//       ( ViewConstants.MODEL_RULES_VIEW,
//         "gov.nasa.arc.planworks.viz.sequence.modelRules.ModelRulesView");

  } // end static

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

  private static String planWorksTitle;
  private static boolean isMaxScreen;
  private static String osType;
  private static String planWorksRoot;
  private static PlanWorks planWorks;

  protected static JMenu projectMenu;
  protected static List supportedViewNames; // List of String

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
  
  /**
   * <code>isWindowBuilt</code>
   *
   * @return - <code>boolean</code> - 
   */
  public static synchronized boolean isWindowBuilt() { return windowBuilt; }

  private Map sequenceStepsViewMap;
  private Map sequenceNameMap; // postfixes (1), etc for duplicate seq names

  private DirectoryChooser sequenceDirChooser; // not final, since PlanWorksGUITest
                                               // creates multiple instances
  //protected final PlannerCommandLineDialog executeDialog;
  private long [] viewRenderingStartTime;

  protected PwProject currentProject;
  protected String currentProjectName;
  protected ViewManager viewManager;


  /**
   * <code>PlanWorks</code> - constructor 
   *
   * @param constantMenus - <code>JMenu[]</code> -
   */                                
  public PlanWorks( final JMenu[] constantMenus) {
    super( planWorksTitle, constantMenus);
    planWorksCommon();
  }

  /**
   * <code>PlanWorks</code> - constructor for PlanWorksGUITest
   *
   * @param constantMenus - <code>JMenu[]</code> - 
   * @param title - <code>String</code> - 
   */
  public PlanWorks( final JMenu[] constantMenus, final String title,
                    final String maxScreenValue, final String osType,
                    final String planWorksRoot) {

    super( title, constantMenus);
    planWorksTitle = title;
    this.isMaxScreen = false;
    if (maxScreenValue.equals( "true")) {
      this.isMaxScreen = true;
    }
    this.osType = osType;
    this.planWorksRoot = planWorksRoot;
    planWorksCommon();
  }

  private void planWorksCommon() {
    projectMenu.setEnabled(false);
    currentProjectName = "";
    currentProject = null;
    viewManager = null;
    createDirectoryChooser();
    // Closes from title bar 
    addWindowListener( new WindowAdapter() {
        public final void windowClosing( final WindowEvent e) {
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
    supportedViewNames = Utilities.sortStringKeySet( VIEW_CLASS_NAME_MAP);
    viewRenderingStartTime = new long [supportedViewNames.size()];
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
    //executeDialog = new PlannerCommandLineDialog(this);
    //executeDialog.hide();
  } // end constructor 

  private PlanWorks getPlanWorksInternal() {
    return planWorks;
  }

  /**
   * <code>getPlanWorks</code> - do not allow access to planWorks static object
   *
   * @return - <code>PlanWorks</code> - 
   */
  public static PlanWorks getPlanWorks() {
    return planWorks.getPlanWorksInternal();
  }
    
  /**
   * <code>setPlanWorks</code>
   *
   * @param planWorksInstance - <code>PlanWorks</code> - 
   */
  public static void setPlanWorks( final PlanWorks planWorksInstance) {
    planWorks = planWorksInstance;
  }

  /**
   * <code>getPlanWorksTitle</code>
   *
   * @return - <code>String</code> - 
   */
  public static String getPlanWorksTitle() {
    return planWorksTitle;
  }

  /**
   * <code>getViewManager</code>
   *
   * @return - <code>ViewManager</code> - 
   */
  public final ViewManager getViewManager() {
    return viewManager;
  }

  /**
   * <code>getCurrentProjectName</code>
   *
   * @return - <code>String</code> - 
   */
  public final String getCurrentProjectName() {
    return currentProjectName;
  }

  public void setCurrentProjectName( final String name) {
    currentProjectName = name;
  }

  /**
   * <code>getCurrentProject</code> - 
   *
   * @return - <code>PwProject</code> - 
   */
  public final PwProject getCurrentProject() {
    return currentProject;
  }

  /**
   * <code>getProjectMenu</code>
   *
   * @return - <code>JMenu</code> - 
   */
  public static JMenu getProjectMenu() {
    return projectMenu;
  }

  /**
   * <code>getViewClassName</code>
   *
   * @param viewName - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public static String getViewClassName( final String viewName) {
    return (String) VIEW_CLASS_NAME_MAP.get( viewName);
  }

  /**
   * <code>getProjectsLessCurrent</code>
   *
   * @return - <code>List</code> - 
   */
  protected final List getProjectsLessCurrent() {
    return CollectionUtils.lGrep(FunctorFactory.notEqualFunctor(currentProjectName), 
                                 PwProject.listProjects());
  } // end getProjectsLessCurrent

  /**
   * <code>getSequenceStepsViewFrame</code>
   *
   * @param seqUrl - <code>MDIInternalFrame</code> - 
   * @return - <code>String</code> - 
   */
  public final MDIInternalFrame getSequenceStepsViewFrame( final String seqUrl) {
    return (MDIInternalFrame) sequenceStepsViewMap.get( seqUrl);
  }
  
  /**
   * <code>setSequenceStepsViewFrame</code>
   *
   * @param seqUrl - <code>String</code> - 
   * @param frame - <code>MDIInternalFrame</code> - 
   */
  protected final void setSequenceStepsViewFrame( final String seqUrl,
                                                  final MDIInternalFrame frame) {
    sequenceStepsViewMap.put( seqUrl, frame);
  }

  /**
   * <code>getSequenceMenuName</code>
   *
   * @param seqUrl - <code>String</code> - 
   * @return - <code>String</code> - 
   */
  public final String getSequenceMenuName( final String seqUrl) {
    return (String) sequenceNameMap.get( seqUrl);
  }

  /**
   * <code>getSequenceDirChooser</code> - for PlanWorksGUITest
   *
   * @return - <code>DirectoryChooser</code> - 
   */
  public final DirectoryChooser getSequenceDirChooser() {
    return sequenceDirChooser;
  }

  /**
   * <code>getViewRenderingStartTime</code>
   *
   * @param viewName - <code>String</code> - 
   * @return - <code>long</code> - 
   */
  public final long getViewRenderingStartTime( String viewName) {
    return viewRenderingStartTime[supportedViewNames.indexOf( viewName)];
  }

  /**
   * <code>setViewRenderingStartTime</code>
   *
   * @param time - <code>long</code> - 
   * @param viewName - <code>String</code> - 
   */
  public final void setViewRenderingStartTime( long time, String viewName) {
     viewRenderingStartTime[supportedViewNames.indexOf( viewName)] = time;
  }

 /**
   * <code>setProjectMenuEnabled</code>
   *
   * @param textName - <code>String</code> - 
   * @param isEnabled - <code>boolean</code> - 
   */
  protected static void setProjectMenuEnabled( final String textName, final boolean isEnabled) {
    for (int i = 0, n = projectMenu.getItemCount(); i < n; i++) {
      if ((projectMenu.getItem( i) != null) &&
          (projectMenu.getItem( i).getText().equals( textName))) {
        projectMenu.getItem( i).setEnabled( isEnabled);
        break;
      }
    }
  } // end setProjectMenuEnabled

  private String getSequenceMenuItemName( final String seqName,
                                          final JMenu planSequenceViewMenu) {
    int nameCount = 0;
    // System.err.println( "getSequenceMenuItemName: seqName " + seqName);
    // check for e.g. monkey1066690986042
    String newSeqName = seqName.substring( 0, seqName.length() - DbConstants.LONG_INT_LENGTH);
    for (int i = 0; i < planSequenceViewMenu.getItemCount(); i++) {
      JMenuItem item = planSequenceViewMenu.getItem(i);
      String itemName = item.getText();
      int index = itemName.indexOf(" (");
      if (index != -1) {
        itemName = itemName.substring(0, index);
      }
      if (itemName.equals( newSeqName)) {
        nameCount++;
      }
    }
    if (nameCount > 0) {
      newSeqName = newSeqName.concat(" (").concat(Integer.toString(nameCount)).concat(")");
    }
    // System.err.println( "   seqName " + seqName);
    return newSeqName;
  } // end getSequenceMenuItemName

  /**
   * <code>buildConstantMenus</code> - make it accessible to JFCUnit tests
   *
   * @return - <code>JMenu[]</code> - 
   */
  public static JMenu[] buildConstantMenus() {
    JMenu [] jMenuArray = new JMenu [2];
    JMenu fileMenu = new JMenu( FILE_MENU);
    JMenuItem exitItem = new JMenuItem( EXIT_MENU_ITEM);
    exitItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          System.exit(0);
        } });
    fileMenu.add( exitItem);

    projectMenu = new JMenu( PROJECT_MENU);
    JMenuItem createProjectItem = new JMenuItem( CREATE_MENU_ITEM);
    JMenuItem openProjectItem = new JMenuItem( OPEN_MENU_ITEM);
    JMenuItem deleteProjectItem = new JMenuItem( DELETE_MENU_ITEM);
    //JMenuItem newSequenceItem = new JMenuItem( NEWSEQ_MENU_ITEM);
    JMenuItem addSequenceItem = new JMenuItem( ADDSEQ_MENU_ITEM);
    JMenuItem deleteSequenceItem = new JMenuItem(DELSEQ_MENU_ITEM);
    createProjectItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          while(PlanWorks.planWorks == null) {
            Thread.yield();
          }
          PlanWorks.planWorks.instantiateProjectThread( CREATE);
        }});
    projectMenu.add( createProjectItem);
    openProjectItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          while(PlanWorks.planWorks == null) {
            Thread.yield();
          }
          PlanWorks.planWorks.instantiateProjectThread( OPEN);
        }});
    projectMenu.add( openProjectItem);
    deleteProjectItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          PlanWorks.planWorks.deleteProjectThread();
          Thread.yield();
        }});
    projectMenu.add( deleteProjectItem);
    projectMenu.addSeparator();
//     newSequenceItem.addActionListener(new ActionListener() {
//         public void actionPerformed(ActionEvent e) {
//           PlanWorks.planWorks.newSequenceThread();
//         }});
//     projectMenu.add(newSequenceItem);
    addSequenceItem.addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          PlanWorks.planWorks.addSequenceThread();
        }});
    projectMenu.add( addSequenceItem);
    deleteSequenceItem.addActionListener(new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          PlanWorks.planWorks.deleteSequenceThread();
        }
      });
    projectMenu.add(deleteSequenceItem);

    jMenuArray[0] = fileMenu;
    jMenuArray[1] = projectMenu;
    return jMenuArray;
  } // end buildConstantMenus

  private void instantiateProjectThread( final String type) {
    if(sequenceStepsViewMap == null) {
      sequenceStepsViewMap = new HashMap();
    }
    Thread thread = new InstantiateProjectThread(type);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  private void deleteProjectThread() {
    Thread thread = new DeleteProjectThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  private void newSequenceThread() {
    Thread thread = new NewSequenceThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  private void addSequenceThread() {
    Thread thread = new AddSequenceThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  private void deleteSequenceThread() {
    Thread thread = new DeleteSequenceThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  /**
   * <code>getPlanSequence</code>
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   */
  public final PwPlanningSequence getPlanSequence( final PwPartialPlan partialPlan) {
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


  /**
   * <code>addPlanSeqViewMenu</code>
   *
   * @param project - <code>PwProject</code> - 
   * @param planSeqMenu - <code>JMenu</code> - 
   */
  public final void addPlanSeqViewMenu( final PwProject project, final JMenu planSeqMenu) {
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


  /**
   * <code>buildPlanSeqViewMenu</code>
   *
   * @param project - <code>PwProject</code> - 
   * @param planSeqViewMenu - <code>JMenu</code> - 
   * @return - <code>JMenu</code> - 
   */
  protected final JMenu buildPlanSeqViewMenu( final PwProject project,
                                              JMenu planSeqViewMenu) {
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
          public final void actionPerformed( final ActionEvent evt) {
            PlanWorks.planWorks.createSequenceViewThread
              ( ViewConstants.SEQUENCE_STEPS_VIEW, (SequenceViewMenuItem) evt.getSource());
          }
        });
      planSeqViewMenu.add( planDbSizeItem);
    }
    return planSeqViewMenu;
  } // end buildPlanSeqViewMenu


  private void createSequenceViewThread( final String viewName, 
                                         final SequenceViewMenuItem menuItem) {
    Thread thread = new CreateSequenceViewThread(viewName, menuItem);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  } // end createSequenceViewThread

  /**
   * <code>createDirectoryChooser</code> - public for PWTestHelper
   *
   */
  public final void createDirectoryChooser() {
    sequenceDirChooser = new DirectoryChooser();
    if (! System.getProperty( "default.sequence.dir").equals( "")) {
      sequenceDirChooser.setCurrentDirectory
        ( new File( System.getProperty( "default.sequence.dir")));
    }
    sequenceDirChooser.setDialogTitle
      ( "Select Planning Sequence Directory(ies)");
    sequenceDirChooser.setMultiSelectionEnabled( true);
    sequenceDirChooser.getOkButton().addActionListener( new ActionListener() {
        public final void actionPerformed( final ActionEvent e) {
          String dirChoice = sequenceDirChooser.getCurrentDirectory().getAbsolutePath();
          File [] seqDirs = sequenceDirChooser.getSelectedFiles();
          // System.err.println( "PlanWorks sequence parent directory" + dirChoice);
          // System.err.println( "sequenceDirectories");
          // for (int i = 0, n = seqDirs.length; i < n; i++) {
          //   System.err.println( "i " + i + " " + seqDirs[i].getName());
          // }
          if ((dirChoice != null) && (dirChoice.length() > 0) &&
              (new File( dirChoice)).isDirectory() &&
              (seqDirs.length != 0)) {
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
     * accept - Do not accept files, and only directories which are not partial plan 
     *          step directories
     *
     * @param file - a directory or file name
     * @return true, if a directory is valid
     */
    public final boolean accept( final File file) {
      boolean isValid = false;
      if (file.isDirectory()) {
        isValid = true;
        if (file.getName().equals( "CVS")) {
          isValid = false;
        } else {
          String [] allFileNames = file.list();
          String [] ppFileNames = file.list( new PwSQLFilenameFilter());
					if(allFileNames == null || ppFileNames == null)
						return false;
          if ((ppFileNames.length == DbConstants.NUMBER_OF_PP_FILES) ||
              (ppFileNames.length == allFileNames.length)) {
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
    public final String getDescription() { 
      return "Sequence Directories";
    }
  } // end class SequenceDirectoryFilter


  /**
   * <code>askSequenceDirectory</code>
   *
   * @return - <code>List</code> - of  List selectedSequenceUrls & List invalidSequenceUrls 
   */
  protected List askSequenceDirectory() {
    List returnList = new ArrayList();
    List invalidSequenceUrls = null, selectedSequenceUrls = null;
    while (true) {
      selectedSequenceUrls = new ArrayList(); invalidSequenceUrls = new ArrayList();
      // ask user for a single or multiple sequence directory(ies) of partialPlans
      int returnVal =
        PlanWorks.planWorks.sequenceDirChooser.showDialog( PlanWorks.planWorks, "");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String currentSelectedDir =
          PlanWorks.planWorks.getSequenceDirChooser().getCurrentDirectory().
          getAbsolutePath();
        File [] selectedFiles =
          PlanWorks.planWorks.sequenceDirChooser.getSelectedFiles();
        for (int i = 0, n = selectedFiles.length; i < n; i++) {
          // System.err.println( "i " + i + " name " + selectedFiles[i].getName());
          String sequenceUrl = currentSelectedDir +
            System.getProperty( "file.separator") + selectedFiles[i].getName();
          selectedSequenceUrls.add( sequenceUrl);
          // System.err.println( "sequenceUrl " + sequenceUrl);
          String validateMsg = FileUtils.validateSequenceDirectory( sequenceUrl);
          // System.err.println( "validateMsg " + validateMsg);
          if (validateMsg != null) {
            JOptionPane.showMessageDialog
              (PlanWorks.getPlanWorks(), validateMsg, "Invalid Sequence Directory",
               JOptionPane.ERROR_MESSAGE);
            invalidSequenceUrls.add( sequenceUrl);
          }
        }
        // System.err.println( "invalid size " + invalidSequenceUrls.size() +
        //                     " selected size " + selectedSequenceUrls.size());
        if (invalidSequenceUrls.size() == selectedSequenceUrls.size()) {
          continue; // user may reselect
        } else {
          break; // some sequences are valid
        }
      } else {
        break; // exit dialog with no sequences added - use Project->Add Sequence
      }
    } // end while
    returnList.add( selectedSequenceUrls);
    returnList.add( invalidSequenceUrls);
    return returnList;
  } // end askSequenceDirectory

  /**
   * <code>addPlanningSequences</code>
   *
   * @param project - <code>PwProject</code> - 
   * @param selectedSequenceUrls - <code>List</code> - 
   * @param invalidSequenceUrls - <code>List</code> - 
   * @return - <code>boolean</code> - 
   * @exception DuplicateNameException if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   */
  protected boolean addPlanningSequences( PwProject project, List selectedSequenceUrls,
                                          List invalidSequenceUrls)
    throws DuplicateNameException, ResourceNotFoundException {
    boolean isSequenceAdded = false;
    for (int i = 0, n = selectedSequenceUrls.size(); i < n; i++) {
      String sequenceUrl = (String) selectedSequenceUrls.get( i);
      boolean isValidSequence = true;
      for (int j = 0, m = invalidSequenceUrls.size(); j < m; j++) {
                                  
        if (((String) invalidSequenceUrls.get( j)).indexOf( sequenceUrl) >= 0) {
          isValidSequence = false;
          break;
        }
      }
      if (isValidSequence) {
        System.err.println( "project.addPlanningSequence " + sequenceUrl);
        project.addPlanningSequence( sequenceUrl);
        isSequenceAdded = true;
      }
    }
    return isSequenceAdded;
  } // end addPlanningSequences

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
  public static void main ( final String[] args) {
    planWorksTitle = "";
    String maxScreenValue = "false";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        planWorksTitle = args[argc];
      } else if (argc == 1) {
         maxScreenValue = args[argc];
      } 
      else {
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
    public final int compare( final Object o1, final Object o2) {
      String s1 = Utilities.getUrlLeaf((String) o1);
      String s2 = Utilities.getUrlLeaf((String) o2);
      return s1.compareTo(s2);
    }
    public final boolean equals( final Object o1, final Object o2) {
      String s1 = Utilities.getUrlLeaf((String)o1);
      String s2 = Utilities.getUrlLeaf((String)o2);
      return s1.equals(s2);
    }
  }


} // end  class PlanWorks
        
