// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectTest.java,v 1.1 2003-06-02 17:49:59 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06May03
//         derived from skunkworks/planViz/java/src/.../PlanViz.java
//

package gov.nasa.arc.planworks.db.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;


import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.util.FileUtils;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.views.VizView;
import gov.nasa.arc.planworks.viz.views.timeline.TimelineView;


/**
 * <code>PwProjectTest</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwProjectTest extends JFrame {

 
  private static PwProjectTest projectTest;
  private static String planWorksRoot;
  private static String userName;
  private static String osType;

  // XML XPath - eXist-0.9.1
  private static String xmlFilesDirectory;
  private static String userCollectionName; // e.g. /wtaylor

  /**
   * constant <code>FRAME_WIDTH</code>
   *
   */
  public static final int FRAME_WIDTH = 900;

  /**
   * constant <code>FRAME_HEIGHT</code>
   *
   */
  public static final int FRAME_HEIGHT = 850;

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

  private Container contentPane;
  private VizView timelineView;
  private PwPartialPlan partialPlan;
  private PwProject project;

  /**
   * <code>PwProjectTest</code> - constructor
   *                     for cmd line invocation (reads arg list in main)
   *
   */
  public PwProjectTest() {
    super( "PlanWorks Timeline View");
    // Closes from title bar 
    addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent e) {
          System.exit( 0);
        }});

    // ONE PROJECT FOR NOW, WITH ONE SEQUENCE, WITH ONE PARTIAL PLAN
    // PlanWorks/xml/test/monkey/monkey.xml

    // PwProjectMgmt.openProject
    // PwPartialPlan partialPlan = getTestPartialPlan();

    // PwProjectMgmt.createProject
    // String url = System.getProperty( "planworks.root") + "/xml/test";
    // partialPlan = createTestPartialPlan( url);
    // System.out.println( "Test partialPlan " + partialPlan);

    contentPane = getContentPane();
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS));

    buildMenuBar();

    try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep1) {
      System.err.println( rnfExcep1);
      System.exit( 1);
    }
  } // end constructor


  /**
   * <code>PwProjectTest</code> - constructor
   *                     for TimelineViewTest invocation (args passed in)
   *
   */
  public PwProjectTest( String osType, String pathname) {
    super( "PlanWorks Timeline View");
    this.osType = osType;
    this.xmlFilesDirectory = FileUtils.getCanonicalPath( pathname);
    this.planWorksRoot = System.getProperty( "planworks.root");
    this.userName = System.getProperty( "user");
    this.userCollectionName = "/" + userName;

    // Closes from title bar 
    addWindowListener( new WindowAdapter() {
        public void windowClosing( WindowEvent e) {
          System.exit( 0);
        }});

    // ONE PROJECT FOR NOW, WITH ONE SEQUENCE, WITH ONE PARTIAL PLAN
    // PlanWorks/xml/test/monkey/monkey.xml

    // PwProject.openProject
    // PwPartialPlan partialPlan = getTestPartialPlan();

    // PwProject.createProject
   //  String url = System.getProperty( "planworks.root") + "/xml/test";
    // partialPlan = createTestPartialPlan( url);
    // System.out.println( "Test partialPlan " + partialPlan);

    contentPane = getContentPane();
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS));

    buildMenuBar();

    try {
      PwProject.initProjects();
    } catch (ResourceNotFoundException rnfExcep1) {
      System.err.println( rnfExcep1);
      System.exit( 1);
    }
  } // end constructor 


  private PwPartialPlan createTestPartialPlan( String url) {
    project = null; PwPlanningSequence planSeq = null;
    PwPartialPlan partialPlan = null;
    System.out.println( "Create Project: " + url);
    try {
      project = PwProject.createProject( url);
    } catch (ResourceNotFoundException rnfExcep1) {
      System.err.println( rnfExcep1);
      return null;
    } catch (DuplicateNameException dupExcep) {
      System.err.println( dupExcep);
      return null;
    }
    List sequenceList = project.listPlanningSequences();
    Iterator seqIterator = sequenceList.iterator();
    while (seqIterator.hasNext()) {
      String sequenceName = (String) seqIterator.next();
      System.out.println( "Sequence: " + sequenceName);
      try {
        planSeq = project.getPlanningSequence( sequenceName);
      } catch (ResourceNotFoundException rnfExcep2) {
        rnfExcep2.printStackTrace();
      }
      int stepCount = planSeq.getStepCount();
      for (int step = 0; step < stepCount; step++) {
        try {
          partialPlan = planSeq.getPartialPlan( step);
          System.out.println( "step " + step + " partialPlan " + partialPlan);
        } catch (IndexOutOfBoundsException indExcep) {
          indExcep.printStackTrace();
        }
      }
    }
    return partialPlan;
  } // end createTestPartialPlan

  private PwPartialPlan openTestPartialPlans() {
    String projectUrl = "", sequenceName = "";
    project = null; PwPlanningSequence planSeq = null;
    PwPartialPlan partialPlan = null;
    // ONE PROJECT HARD-CODED FOR NOW, WITH ONE SEQUENCE, WITH ONE PARTIAL PLAN
    // PlanWorks/xml/test/monkey/monkey.xml
    List projectUrls = PwProject.listProjects();
    Iterator projIterator = projectUrls.iterator();
    while (projIterator.hasNext()) {
      projectUrl = (String) projIterator.next();
      System.out.println( "Open Project: " + projectUrl);
      try {
        project = PwProject.openProject( projectUrl);
      } catch (ResourceNotFoundException rnfExcep1) {
        // System.err.println( "Project " + projectName + " not found: " + rnfExcep1);
        rnfExcep1.printStackTrace();
        System.exit( 1);
      }
      List sequenceList = project.listPlanningSequences();
      Iterator seqIterator = sequenceList.iterator();
      while (seqIterator.hasNext()) {
        sequenceName = (String) seqIterator.next();
        System.out.println( "Sequence: " + sequenceName);
        try {
          planSeq = project.getPlanningSequence( sequenceName);
        } catch (ResourceNotFoundException rnfExcep2) {
          // System.err.println( "Sequence " + sequenceName + " not found: " + rnfExcep2 );
          rnfExcep2.printStackTrace();
          System.exit( 1);
        }
        int stepCount = planSeq.getStepCount();
        for (int step = 0; step < stepCount; step++) {
          try {
            partialPlan = planSeq.getPartialPlan( step);
            System.out.println( "step " + step + " partialPlan " + partialPlan);
          } catch (IndexOutOfBoundsException indExcep) {
            // System.err.println( "Step " + step + " not found: " + indExcep);
            indExcep.printStackTrace();
            System.exit( 1);
          }
        }
      }
    }
    return partialPlan;
  } // end openTestPartialPlans


  private void closeProject() {
    try {
      this.project.close();
    } catch (Exception e) {
      System.err.println( e);
      System.exit( 1);
    }
  } // end closeProject


  private final void buildMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu( "File");
    JMenuItem exitItem = new JMenuItem( "Exit");
    exitItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          System.exit(0);
        } });
    fileMenu.add( exitItem);

    JMenu projectMenu = new JMenu( "Project");
    JMenuItem createProjectItem = new JMenuItem( "Create");
    JMenuItem create1ProjectItem = new JMenuItem( "Create1");
    JMenuItem openProjectItem = new JMenuItem( "Open");
    JMenuItem closeProjectItem = new JMenuItem( "Close");
    JMenuItem saveProjectItem = new JMenuItem( "Save");
    createProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          String url = System.getProperty( "planworks.root") + "/xml/test";
         PwProjectTest.this. partialPlan =
           PwProjectTest.this.createTestPartialPlan( url);
        }});
    projectMenu.add( createProjectItem);
    create1ProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          String url = System.getProperty( "planworks.root") + "/xml/test1";
         PwProjectTest.this. partialPlan =
           PwProjectTest.this.createTestPartialPlan( url);
        }});
    projectMenu.add( create1ProjectItem);
    openProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
         PwProjectTest.this. partialPlan =
           PwProjectTest.this.openTestPartialPlans();
        }});
    projectMenu.add( openProjectItem);
    closeProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PwProjectTest.this.closeProject();
        }});
    projectMenu.add( closeProjectItem);
    saveProjectItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          try {
            PwProjectTest.this.project.save();
          } catch (Exception excp) {
            System.err.println( excp ); System.exit( 0); }}});
    projectMenu.add( saveProjectItem);

    JMenu renderMenu = new JMenu( "Render");
    JMenuItem renderTimelineViewItem = new JMenuItem( "Timeline View");
    renderTimelineViewItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          PwProjectTest.this.renderTimelineView();
        }});
    renderMenu.add( renderTimelineViewItem);

    menuBar.add( fileMenu);
    menuBar.add( projectMenu);
    menuBar.add( renderMenu);
    setJMenuBar( menuBar);
  } // end buildMenuBar


  private void renderTimelineView() {
    // System.err.println( "renderTimelineView");
    if (partialPlan != null) {
      long startTimeMSecs = (new Date()).getTime();

      timelineView = new TimelineView( partialPlan);
      contentPane.add( timelineView);
      contentPane.validate(); // IMPORTANT

      long stopTimeMSecs = (new Date()).getTime();
      String timeString = "Render Timeline View \n   ... elapsed time: " +
        //       writeTime( (stopTimeMSecs - startTimeMSecs)) + " seconds.";
        (stopTimeMSecs - startTimeMSecs) + " msecs.";
      System.err.println( timeString);
    }
  } // end renderTimelineView


  private static void processArguments( String[] args) {
    // input args - defaults
    xmlFilesDirectory = "";
    String pathname = "";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        // linux | solaris | darwin (MacOSX)
        osType = args[argc];
      } else if (argc == 1) {
        pathname = args[argc];
        if (! pathname.equals( "null")) {
           xmlFilesDirectory = FileUtils.getCanonicalPath( pathname);
          System.err.println( "xmlFilesDirectory: " + xmlFilesDirectory);
        }
      } else {
        System.err.println( "argument '" + args[argc] + "' not handled");
        System.exit( 0);
      }
    }
  } // end processArguments

  
  /**
   * <code>main</code>
   *
   * @param args - <code>String[]</code> - 
   */
  public static void main( String[] args) {

    processArguments( args);

    // planWorksRoot = getEnvVar( "PLANWORKS_ROOT");
    planWorksRoot = System.getProperty( "planworks.root");
    // userName = getEnvVar( "USER");
    userName = System.getProperty( "user");
    userCollectionName = "/" + userName;

    projectTest = new PwProjectTest();
    projectTest.setSize( FRAME_WIDTH, FRAME_HEIGHT);
    projectTest.setLocation( FRAME_X_LOCATION, FRAME_Y_LOCATION);
    projectTest.setBackground( Color.gray);
    projectTest.setVisible( true);

  } // end main

} // end class PwProjectTest

