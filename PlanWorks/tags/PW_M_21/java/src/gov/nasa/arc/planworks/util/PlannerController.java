// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlannerController.java,v 1.5 2004-11-24 00:38:05 pdaley Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 31aug04
//

package gov.nasa.arc.planworks.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JInternalFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlannerControlJNI;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;

/**
 * <code>PlannerController</code> - custom panel to allow user to invoke the planner
 *                                  thru a JNI interface
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PlannerController extends JPanel { 

  private static final int WINDOW_WIDTH = 300;
  private static final int WINDOW_HEIGHT= 200;
  private static final int STEP_FIELD_WIDTH = 5;
  private static final Pattern valuePattern = Pattern.compile("\\d+");

  private PwPlanningSequence planSequence;
  private MDIInternalFrame plannerControllerFrame;
  private String projectName;
  private SequenceStepsView sequenceStepsView;
  private JTextField writeStepField;
  private int writeStepStep;
  private JTextField writeNextStepsField;
  private int writeNextSteps;
  private JLabel currentStatusLabel;
  private JLabel currentStepLabel;

  public PlannerController( final PwPlanningSequence planSequence,
                            final MDIInternalFrame plannerControllerFrame,
                            final String projectName,
                            final SequenceStepsView sequenceStepsView) {
    this.planSequence = planSequence;
    this.plannerControllerFrame = plannerControllerFrame;
    this.projectName = projectName;
    this.sequenceStepsView = sequenceStepsView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    /*
     * Unload the planner model if user closes window
     */
    plannerControllerFrame.addInternalFrameListener( new InternalFrameAdapter() {
      public void internalFrameClosing(InternalFrameEvent we) {
        PlannerControlJNI.terminatePlannerRun();
      }});

    currentStatusLabel = new JLabel( "Planner: Ready  ");
    currentStepLabel = new JLabel( "Current step: " +
                                   (planSequence.getPlanDBSizeList().size() - 1));

    final JLabel writeStepLabel1 = new JLabel( "Skip to step number   ");
    writeStepField = new JTextField( STEP_FIELD_WIDTH);
    writeStepField.setText( "");
    final JButton writeStepButton = new JButton( "Go");
    writeStepButton.addActionListener( new WriteStepButtonListener( sequenceStepsView));

    final JLabel writeNextStepsLabel1 = new JLabel( "Write next N steps  ");
    writeNextStepsField = new JTextField( STEP_FIELD_WIDTH);
    writeNextStepsField.setText( "");
    final JButton writeNextStepsButton = new JButton( "Go");
    writeNextStepsButton.addActionListener( new WriteNextStepsButtonListener( sequenceStepsView));

    final JButton completeButton = new JButton( "Write Final Step");
    completeButton.addActionListener( new CompleteButtonListener( sequenceStepsView,
                                                                  plannerControllerFrame));

    final JButton terminateButton = new JButton( "Terminate Planner");
    terminateButton.addActionListener( new TerminateButtonListener( sequenceStepsView,
                                                                    plannerControllerFrame));

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout( gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;

    c.gridwidth = 2;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints( currentStatusLabel, c);
    add( currentStatusLabel);

    c.gridx = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gridBag.setConstraints( currentStepLabel, c);
    add( currentStepLabel);

    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    gridBag.setConstraints( writeStepLabel1, c);
    add( writeStepLabel1);
    c.gridx = 2;
    c.gridwidth = 1;
    gridBag.setConstraints( writeStepField, c);
    add( writeStepField);
    c.gridx++;
    c.anchor = GridBagConstraints.CENTER;
    gridBag.setConstraints( writeStepButton, c);
    add( writeStepButton);

    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    c.anchor = GridBagConstraints.WEST;
    gridBag.setConstraints( writeNextStepsLabel1, c);
    add( writeNextStepsLabel1);
    c.gridx = 2;
    c.gridwidth = 1;
    gridBag.setConstraints( writeNextStepsField, c);
    add( writeNextStepsField);
    c.gridx++;
    c.anchor = GridBagConstraints.CENTER;
    gridBag.setConstraints( writeNextStepsButton, c);
    add( writeNextStepsButton);

    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    gridBag.setConstraints( completeButton, c);
    add( completeButton);

    c.gridx = 2;
    gridBag.setConstraints( terminateButton, c);
    add( terminateButton);

    setVisible( true);
//     plannerControllerFrame.setSize
//       ( (int) Math.max( plannerControllerFrame.getSize().getWidth(), WINDOW_WIDTH),
//         (int) Math.max( plannerControllerFrame.getSize().getHeight(), WINDOW_HEIGHT));
    plannerControllerFrame.setLocation
      ( (int) ((PlanWorks.getPlanWorks().getSize().getWidth() / 2) - (WINDOW_WIDTH / 2)),
        (int) ((PlanWorks.getPlanWorks().getSize().getHeight() / 2) - (WINDOW_HEIGHT / 2)));
  } // end constructor

  public final MDIInternalFrame getWindowFrame() {
    return plannerControllerFrame;
  }


  class WriteStepButtonListener implements ActionListener {
    private SequenceStepsView sequenceStepsView;
    public WriteStepButtonListener( SequenceStepsView sequenceStepsView) {
      this.sequenceStepsView = sequenceStepsView;
    }
    public void actionPerformed(ActionEvent ae) {
      // validity check writeStepStep
      if (! valuePattern.matcher( writeStepField.getText().trim()).matches()) {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(),
            "Invalid format.  'write step' value must be only digits.",
            "Illegal Argument Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      writeStepStep = Integer.parseInt( writeStepField.getText().trim());
      int currentStepNum = planSequence.getPlanDBSizeList().size() - 1;
      if (writeStepStep <= currentStepNum) {
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(),
           "write step " + writeStepStep + " is <= " + currentStepNum,
           "Illegal Argument Error", JOptionPane.ERROR_MESSAGE);

      } else {
        Thread thread = new WriteStepJNIThread( writeStepStep, sequenceStepsView);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    }
  } // end class WriteStepButtonListener


  class WriteStepJNIThread extends Thread {
    private int writeStepStep;
    private SequenceStepsView sequenceStepsView;

    public WriteStepJNIThread( int writeStepStep, SequenceStepsView sequenceStepsView) {
      this.writeStepStep = writeStepStep;
      this.sequenceStepsView = sequenceStepsView;
    }

    public void run() {
      final SwingWorker worker = new SwingWorker() {
          public Object construct() {
            currentStatusLabel.setText( "Planner: Running");
            System.err.println( "WriteStepJNIThread( " + writeStepStep + " ) started");
            try {
               PlannerControlJNI.writeStep( writeStepStep);
            } catch (Exception excp) {
               PlannerControlJNI.terminatePlannerRun();
               plannerControllerFrame.dispose();
               notifyPlannerException();    
               return null;
            }
            writeStepField.setText( "");
            currentStepLabel.setText( "Current step: " + writeStepStep);
            currentStatusLabel.setText( "Planner: Ready  ");
            sequenceStepsView.refreshView();
            /*
             * if planner should finish, terminate, close window and notify user
             */
            int plannerStatus = PlannerControlJNI.getPlannerStatus();
            if ((plannerStatus == PlannerControlJNI.PLANNER_TIMEOUT_REACHED) ||
                (plannerStatus == PlannerControlJNI.PLANNER_SEARCH_EXHAUSTED) ||
                (plannerStatus == PlannerControlJNI.PLANNER_FOUND_PLAN)) {
               PlannerControlJNI.terminatePlannerRun();
               plannerControllerFrame.dispose();
               notifyPlannerStatus( plannerStatus);    
            }
            return null;
          }
        };
      worker.start();  
    } // end run

  } // end class WriteStepThread


  class WriteNextStepsButtonListener implements ActionListener {
    private SequenceStepsView sequenceStepsView;
    public WriteNextStepsButtonListener( SequenceStepsView sequenceStepsView) {
      this.sequenceStepsView = sequenceStepsView;
    }
    public void actionPerformed(ActionEvent ae) {
      if (! valuePattern.matcher( writeNextStepsField.getText().trim()).matches()) {
        JOptionPane.showMessageDialog
          ( PlanWorks.getPlanWorks(),
            "Invalid format. 'write next steps' value must be only digits.",
            "Illegal Argument Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      writeNextSteps = Integer.parseInt( writeNextStepsField.getText().trim());
      // validity check writeNextSteps
      if (writeNextSteps < 1) {
        JOptionPane.showMessageDialog
          (PlanWorks.getPlanWorks(),
           "write next steps " + writeNextSteps + " is < 1",
           "Illegal Argument Error", JOptionPane.ERROR_MESSAGE);

      } else {
        Thread thread = new WriteNextStepsJNIThread( writeNextSteps, sequenceStepsView);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    }
  } // end class WriteNextStepsButtonListener


  class WriteNextStepsJNIThread extends Thread {
    private int writeNextSteps;
    private SequenceStepsView sequenceStepsView;

    public WriteNextStepsJNIThread( int writeNextSteps, SequenceStepsView sequenceStepsView) {
      this.writeNextSteps = writeNextSteps;
      this.sequenceStepsView = sequenceStepsView;
    }

    public void run() {
      final SwingWorker worker = new SwingWorker() {
          public Object construct() {
            currentStatusLabel.setText( "Planner: Running");
            System.err.println( "WriteNextStepsJNIThread( " + writeNextSteps + " ) started");
            int currentStepNum = planSequence.getPlanDBSizeList().size() - 1;
            try {
               PlannerControlJNI.writeNext( writeNextSteps);
            } catch (Exception excp) {
               PlannerControlJNI.terminatePlannerRun();
               plannerControllerFrame.dispose();
               notifyPlannerException();    
               return null;
            }
            currentStatusLabel.setText( "Planner: Ready  ");
            currentStepLabel.setText( "Current step: " + (currentStepNum + writeNextSteps));
            sequenceStepsView.refreshView();
            /*
             * if planner should finish, terminate, close window and notify user
             */
            int plannerStatus = PlannerControlJNI.getPlannerStatus();
            if ((plannerStatus == PlannerControlJNI.PLANNER_TIMEOUT_REACHED) ||
                (plannerStatus == PlannerControlJNI.PLANNER_SEARCH_EXHAUSTED) ||
                (plannerStatus == PlannerControlJNI.PLANNER_FOUND_PLAN)) {
               PlannerControlJNI.terminatePlannerRun();
               plannerControllerFrame.dispose();
               notifyPlannerStatus( plannerStatus);    
            }
            return null;
          }
        };
      worker.start();  
    } // end run

  } // end class WriteNextStepsThread


  class CompleteButtonListener implements ActionListener {
    private SequenceStepsView sequenceStepsView;
    private MDIInternalFrame plannerControllerFrame;
    public CompleteButtonListener( SequenceStepsView sequenceStepsView,
                                   MDIInternalFrame plannerControllerFrame) {
      this.sequenceStepsView = sequenceStepsView;
      this.plannerControllerFrame = plannerControllerFrame;
    }
    public void actionPerformed(ActionEvent ae) {
        Thread thread = new CompleteJNIThread( sequenceStepsView, plannerControllerFrame);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
  } // end class WriteNextStepsButtonListener


  class CompleteJNIThread extends Thread {
    private SequenceStepsView sequenceStepsView;
    private MDIInternalFrame plannerControllerFrame;

    public CompleteJNIThread( SequenceStepsView sequenceStepsView,
                              MDIInternalFrame plannerControllerFrame) {
      this.sequenceStepsView = sequenceStepsView;
      this.plannerControllerFrame = plannerControllerFrame;
    }

    public void run() {
      final SwingWorker worker = new SwingWorker() {
          public Object construct() {
            currentStatusLabel.setText( "Planner: Running");
            System.err.println( "CompleteJNIThread() started");
            try {
               PlannerControlJNI.completePlannerRun();
            } catch (Exception excp) {
               PlannerControlJNI.terminatePlannerRun();
               plannerControllerFrame.dispose();
               notifyPlannerException();    
               return null;
            }
            int plannerStatus = -1;
            while ((plannerStatus != PlannerControlJNI.PLANNER_TIMEOUT_REACHED) &&
                   (plannerStatus != PlannerControlJNI.PLANNER_SEARCH_EXHAUSTED) &&
                   (plannerStatus != PlannerControlJNI.PLANNER_FOUND_PLAN)) {
              try {
                Thread.currentThread().sleep( ViewConstants.WAIT_INTERVAL);
              }
              catch (InterruptedException ie) {}
              System.err.println( "CompleteJNIThread waiting for FOUND_PLAN or " +
                                  "SEARCH_EXHAUSTED or TIMEOUT_REACHED");
              plannerStatus = PlannerControlJNI.getPlannerStatus();
            }
            sequenceStepsView.refreshView();
            PlannerControlJNI.terminatePlannerRun();
            plannerControllerFrame.dispose();
            notifyPlannerStatus( plannerStatus);    
            return null;
          }
        };
      worker.start();  
    } // end run

  } // end class CompleteThread


  class TerminateButtonListener implements ActionListener {
    private SequenceStepsView sequenceStepsView;
    private MDIInternalFrame plannerControllerFrame;
    public TerminateButtonListener( SequenceStepsView sequenceStepsView,
                                    MDIInternalFrame plannerControllerFrame) {
      this.sequenceStepsView = sequenceStepsView;
      this.plannerControllerFrame = plannerControllerFrame;
    }
    public void actionPerformed( ActionEvent ae) {
      Thread thread = new TerminateJNIThread( plannerControllerFrame);
      thread.setPriority( Thread.MIN_PRIORITY);
      thread.start();
    }
  } // end class TerminateButtonListener


  class TerminateJNIThread extends Thread {
    private MDIInternalFrame plannerControllerFrame;

    public TerminateJNIThread( MDIInternalFrame plannerControllerFrame) {
      this.plannerControllerFrame = plannerControllerFrame;
    }

    public void run() {
      final SwingWorker worker = new SwingWorker() {
          public Object construct() {
            System.err.println( "TerminateJNIThread() started");
            PlannerControlJNI.terminatePlannerRun();
            plannerControllerFrame.dispose();
            return null;
          }
        };
      worker.start();  
    } // end run

  } // end class TerminateThread


  private void notifyPlannerStatus( int plannerStatus) {    
    String plannerStatusMessage;
    if (plannerStatus == PlannerControlJNI.PLANNER_TIMEOUT_REACHED) {
      plannerStatusMessage = "Timeout reached. Failed finding plan.";
    } else if (plannerStatus == PlannerControlJNI.PLANNER_FOUND_PLAN) {
      plannerStatusMessage = "Found a plan.";
    } else if (plannerStatus == PlannerControlJNI.PLANNER_SEARCH_EXHAUSTED) {
      plannerStatusMessage = "Search Exhausted. Failed finding plan.";
    } else if (plannerStatus == PlannerControlJNI.PLANNER_IN_PROGRESS) {
      plannerStatusMessage = "Planner in progress.";
    } else {
      plannerStatusMessage = "Planner initially inconsistant.";
    }
    JOptionPane.showMessageDialog
      ( PlanWorks.getPlanWorks(), plannerStatusMessage,
        "Planner Status", JOptionPane.INFORMATION_MESSAGE);
  }

  private void notifyPlannerException() {    
    JOptionPane.showMessageDialog
      ( PlanWorks.getPlanWorks(), "An exception occured in the planner.",
        "Planner Exception", JOptionPane.INFORMATION_MESSAGE);
  }

} // end class PlannerController

   
