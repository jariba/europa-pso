// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlannerController.java,v 1.1 2004-09-03 00:35:39 taylor Exp $
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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.PlannerControlJNI;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
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

  public PlannerController( final PwPlanningSequence planSequence,
                            final MDIInternalFrame plannerControllerFrame,
                            final String projectName,
                            final SequenceStepsView sequenceStepsView) {
    this.planSequence = planSequence;
    this.plannerControllerFrame = plannerControllerFrame;
    this.projectName = projectName;
    this.sequenceStepsView = sequenceStepsView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    final JLabel writeStepLabel1 = new JLabel( "write step");
    writeStepField = new JTextField( STEP_FIELD_WIDTH);
    writeStepField.setText( "");
    final JLabel writeStepLabel2 = new JLabel( "       ");
    final JButton writeStepButton = new JButton( "Go");
    writeStepButton.addActionListener( new WriteStepButtonListener( sequenceStepsView));

    final JLabel writeNextStepsLabel1 = new JLabel( "write next");
    writeNextStepsField = new JTextField( STEP_FIELD_WIDTH);
    writeNextStepsField.setText( "");
    final JLabel writeNextStepsLabel2 = new JLabel( " steps ");
    final JButton writeNextStepsButton = new JButton( "Go");
    writeNextStepsButton.addActionListener( new WriteNextStepsButtonListener( sequenceStepsView));

    final JButton completeButton = new JButton( "Complete");
    completeButton.addActionListener( new CompleteButtonListener( sequenceStepsView,
                                                                  plannerControllerFrame));

    final JLabel separationLabel = new JLabel( "       ");

    final JButton terminateButton = new JButton( "Terminate");
    terminateButton.addActionListener( new TerminateButtonListener( sequenceStepsView,
                                                                    plannerControllerFrame));

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout( gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;

    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints( writeStepLabel1, c);
    add( writeStepLabel1);
    c.gridx++;
    gridBag.setConstraints( writeStepField, c);
    add( writeStepField);
    c.gridx++;
    gridBag.setConstraints( writeStepLabel2, c);
    add( writeStepLabel2);
    c.gridx++;
    gridBag.setConstraints( writeStepButton, c);
    add( writeStepButton);

    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints( writeNextStepsLabel1, c);
    add( writeNextStepsLabel1);
    c.gridx++;
    gridBag.setConstraints( writeNextStepsField, c);
    add( writeNextStepsField);
    c.gridx++;
    gridBag.setConstraints( writeNextStepsLabel2, c);
    add( writeNextStepsLabel2);
    c.gridx++;
    gridBag.setConstraints( writeNextStepsButton, c);
    add( writeNextStepsButton);

    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints( completeButton, c);
    add( completeButton);

    c.gridx++;
    gridBag.setConstraints( separationLabel, c);
    add( separationLabel);

    c.gridx++;
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
        PlannerControlJNI.writeStep( writeStepStep);
        // sequenceStepsView.refreshView();
      }
    }
  } // end class WriteStepButtonListener


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
        PlannerControlJNI.writeNext( writeNextSteps);
        // sequenceStepsView.refreshView();
      }
    }
  } // end class WriteNextStepsButtonListener


  class CompleteButtonListener implements ActionListener {
    private SequenceStepsView sequenceStepsView;
    private MDIInternalFrame plannerControllerFrame;
    public CompleteButtonListener( SequenceStepsView sequenceStepsView,
                                   MDIInternalFrame plannerControllerFrame) {
      this.sequenceStepsView = sequenceStepsView;
      this.plannerControllerFrame = plannerControllerFrame;
    }
    public void actionPerformed(ActionEvent ae) {
      PlannerControlJNI.completePlannerRun();
      // sequenceStepsView.refreshView();
      // PlannerControlJNI.getPlannerStatus
      // wait for PLANNER_TIMEOUT_REACHED or PLANNER_FOUND_PLAN
      // then PlannerControlJNI.terminatePlannerRun()
      // then plannerControllerFrame.dispose();
      PlannerControlJNI.terminatePlannerRun();
      plannerControllerFrame.dispose();
    }
  } // end class WriteNextStepsButtonListener


  class TerminateButtonListener implements ActionListener {
    private SequenceStepsView sequenceStepsView;
    private MDIInternalFrame plannerControllerFrame;
    public TerminateButtonListener( SequenceStepsView sequenceStepsView,
                                    MDIInternalFrame plannerControllerFrame) {
      this.sequenceStepsView = sequenceStepsView;
      this.plannerControllerFrame = plannerControllerFrame;
    }
    public void actionPerformed(ActionEvent ae) {
      PlannerControlJNI.terminatePlannerRun();
      plannerControllerFrame.dispose();
    }
  } // end class WriteNextStepsButtonListener


//   class ButtonListener implements ActionListener {

//     private PlannerController plannerControllerFrame;

//     public ButtonListener( PlannerController plannerControllerFrame) {
//       this.plannerControllerFrame = plannerControllerFrame;
//     }

//     public void actionPerformed( ActionEvent ae) {
//       if (ae.getActionCommand().equals( "Terminate")) {
//         plannerControllerFrame.dispose();
//         return;
//       }
//     }
//   } // end class ButtonListener

  

} // end class PlannerController

   
