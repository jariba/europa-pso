// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwProjectTest.java,v 1.3 2003-05-20 18:25:35 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 06May03
//         derived from skunkworks/planViz/java/src/.../PlanViz.java
//

package gov.nasa.arc.planworks.proj.test;

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
import gov.nasa.arc.planworks.proj.PwProjectMgmt;
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
  private static boolean isJvmGtEq1_4;
  private static String osType;

  // XML XPath - eXist-0.9.1
  private static String xmlFilesDirectory;
  private static String userCollectionName; // e.g. /wtaylor

  private static final int FRAME_WIDTH = 900;
  private static final int FRAME_HEIGHT = 850;
  private static final int FRAME_X_LOCATION = 100;
  private static final int FRAME_Y_LOCATION = 100;
  private Container contentPane;
  private VizView timelineView;
  private PwPartialPlan partialPlan;

  private JTabbedPane tabbedPane;


  /**
   * <code>PwProjectTest</code> - constructor 
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
    String url = System.getProperty( "planworks.root") + "/xml/test";
    partialPlan = createTestPartialPlan( url);
    System.out.println( "Test partialPlan " + partialPlan);

    contentPane = getContentPane();
    contentPane.setLayout( new BoxLayout( contentPane, BoxLayout.Y_AXIS));


    JPanel fileRequestPane2 = new FixedHeightJPanel();
    fileRequestPane2.setBackground( ColorMap.getColor( "green3"));
    fileRequestPane2.setBorder( BorderFactory.createLineBorder
                                ( ColorMap.getColor( "black"), 1));
    JLabel fileRequestPathnameLabel = new JLabel ( "will taylor");
    fileRequestPathnameLabel.setForeground( Color.black);
    fileRequestPathnameLabel.setBackground( Color.green);
    fileRequestPane2.add( fileRequestPathnameLabel, BorderLayout.NORTH);
    contentPane.add( fileRequestPane2, BorderLayout.NORTH);

    
//     tabbedPane = new JTabbedPane( SwingConstants.TOP);
//     tabbedPane.addTab( "fill1" , null, new JPanel(), "fill2");
//     contentPane.add( tabbedPane);

    buildMenuBar();

//     timelineView = new TimelineView( partialPlan);
//     contentPane.add( timelineView);
//     contentPane.validate(); // IMPORTANT
//     addComponentListener( new ComponentListener() {
//         public void componentHidden( ComponentEvent e) { }
//         public void componentMoved ( ComponentEvent e) {}
//         public void componentResized ( ComponentEvent e) {}
//         public void componentShown ( ComponentEvent e) {
//           // render the JGo widgets
//           System.err.println( "constructor componentShown: " + e.getComponent());
//           TimelineView timelineView =
//             (TimelineView) ((PwProjectTest) e.getComponent()).timelineView;
//           timelineView.init();
//         }    
//       });

  } // end constructor


  class FixedHeightJPanel extends JPanel {

  public FixedHeightJPanel() {
    super();

  }

  /**
   * <code>getMaximumSize</code> - height constrained to minimum size
   *
   * @return - <code>Dimension</code> - 
   */ 
  public Dimension getMaximumSize() { 
    int h = super.getMinimumSize().height;
    int w = super.getMaximumSize().width; 
    return new Dimension( w, h);
  };

  } // end class FixedHeightJPanel


  class RenderThread extends Thread {

    public RenderThread() {
    }  // end constructor

    public void run() {
      renderTimelineView();
    } //end run

  } // end class RenderThread


  private PwPartialPlan createTestPartialPlan( String url) {
    PwProject project = null; PwPlanningSequence planSeq = null;
    PwPartialPlan partialPlan = null;
    try {
      project = PwProjectMgmt.createProject( url);
    } catch (ResourceNotFoundException rnfExcep1) {
      rnfExcep1.printStackTrace();
    } catch (DuplicateNameException dupExcep) {
      dupExcep.printStackTrace();
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

  private PwPartialPlan getTestPartialPlan() {
    String projectName = "", sequenceName = "";
    PwProject project = null; PwPlanningSequence planSeq = null;
    PwPartialPlan partialPlan = null;
    // ONE PROJECT HARD-CODED FOR NOW, WITH ONE SEQUENCE, WITH ONE PARTIAL PLAN
    // PlanWorks/xml/test/monkey/monkey.xml
    List projectList = PwProjectMgmt.listProjects();
    Iterator projIterator = projectList.iterator();
    while (projIterator.hasNext()) {
      projectName = (String) projIterator.next();
      System.out.println( "Project: " + projectName);
      try {
        project = PwProjectMgmt.openProject( projectName);
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
  } // end getTestPartialPlan

  private final void buildMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu renderMenu = new JMenu( "Render");
    JMenuItem renderTimelineViewItem = new JMenuItem( "Timeline View");
    renderTimelineViewItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          new RenderThread().start();
        }
      });
    renderMenu.add( renderTimelineViewItem);
    menuBar.add( renderMenu);
    setJMenuBar( menuBar);
  } // end buildMenuBar

  // called from RenderThread - in JPanel
  private void renderTimelineView() {
    System.err.println( "renderTimelineView");
    timelineView = new TimelineView( partialPlan);
    contentPane.add( timelineView);
    contentPane.validate(); // IMPORTANT
    timelineView.addComponentListener( new ComponentListener() {
        public void componentHidden( ComponentEvent e) { }
        public void componentMoved ( ComponentEvent e) {
          System.err.println( "RenderThread componentMoved: " + e.getComponent());
        }
        public void componentResized ( ComponentEvent e) {
          System.err.println( "RenderThread componentResized: " + e.getComponent());
        }
        public void componentShown ( ComponentEvent e) {
          // render the JGo widgets
          System.err.println( "RenderThread componentShown: " + e.getComponent());
          TimelineView timelineView = (TimelineView) e.getComponent();
          timelineView.init();
        }    
      });
  } // end renderTimelineView


  // called from RenderThread - in JTabbedPane
//   private void renderTimelineView() {
//     timelineView = new TimelineView( partialPlan);
//     tabbedPane.addTab( "will" , null, timelineView, "taylor");
//     Component tabComponent =
//       tabbedPane.getComponentAt( tabbedPane.getTabCount() - 1);
//     tabComponent.addComponentListener( new ComponentListener() {
//         public void componentHidden( ComponentEvent e) { }
//         public void componentMoved ( ComponentEvent e) { }
//         public void componentResized ( ComponentEvent e) { }
//         public void componentShown ( ComponentEvent e) {
//           // render the JGo widgets
//           System.err.println( "componentShown: " + e.getComponent());
//           TimelineView timelineView = (TimelineView) tabbedPane.getSelectedComponent();
//           timelineView.init();
//         }    
//       });
//   } // end renderTimelineView


  private static void processArguments( String[] args) {
    // input args - defaults
    isJvmGtEq1_4 = true;
    xmlFilesDirectory = "";
    String pathname = "";
    for (int argc = 0; argc < args.length; argc++) {
      // System.err.println( "argc " + argc + " " + args[argc]);
      if (argc == 0) {
        // linux | solaris | darwin (MacOSX)
        osType = args[argc];
      } else if (argc == 1) {
         if (args[argc].equals( "true")) {
          isJvmGtEq1_4 = true;
        } else if (args[argc].equals( "false")) {
          isJvmGtEq1_4 = false;    
        } else {
          System.err.println( "isJvmGtEq1_4 '" + args[argc] +
                              "' was not either 'true' or 'false'\n");
          System.exit( 0);
        }
      } else if (argc == 2) {
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

