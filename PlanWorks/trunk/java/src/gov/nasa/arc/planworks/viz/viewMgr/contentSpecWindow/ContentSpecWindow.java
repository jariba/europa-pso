//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecWindow.java,v 1.18 2003-09-11 23:41:55 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.db.util.ContentSpec;

/**
 * <code>ContentSpecWindow</code> -
 *                      JPanel->ContentSpecWindow
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The complete, displayable window for defining a content specification for the associated 
 * partial plan.  Provides input fields for specifying timelines, constraints, predicates,
 * variables by type, and time intervals.
 */

public class ContentSpecWindow extends JPanel {
  //  protected ConstraintGroupBox constraintGroup;
  protected PredicateGroupBox predicateGroup;
  protected TimeIntervalGroupBox timeIntervalGroup;
  protected TimelineGroupBox timelineGroup;
  //protected VariableTypeGroupBox variableTypeGroup;
  //private McLaughlanGroupBox mcLaughlanGroup

  protected ContentSpec contentSpec;

  private static boolean queryTestExists;
  /**
   * <code>ContentSpecWindow
   * Constructs the entire content specification window.
   * @param window The <code>MDIInternalFrame</code> to which this pane is added.  Used for the 
   *               <code>pack()</code> method.
   * @param contentSpec The ContentSpec with which this window is associated.  Instantiated in
   *                    ViewSet
   */
  public ContentSpecWindow(MDIInternalFrame window, ContentSpec contentSpec) {
    this.contentSpec = contentSpec;
    queryTestExists = false;

    Map predicateNames = contentSpec.getPredicateNames();
    Map timelineNames = contentSpec.getTimelineNames();

    //System.err.println("Pred: " + predicateNames);
    //System.err.println("Time: " + timelineNames);
    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;
    
    /*constraintGroup = new ConstraintGroupBox(window);
    gridBag.setConstraints(constraintGroup, c);
    add(constraintGroup);*/

    predicateGroup = new PredicateGroupBox(window, predicateNames);
    gridBag.setConstraints(predicateGroup, c);
    add(predicateGroup);

    timelineGroup = new TimelineGroupBox(window, timelineNames);
    //c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(timelineGroup, c);
    add(timelineGroup);

    timeIntervalGroup = new TimeIntervalGroupBox(window);
    //c.gridx++;
    c.gridy++;
    gridBag.setConstraints(timeIntervalGroup, c);
    add(timeIntervalGroup);

    /*variableTypeGroup = new VariableTypeGroupBox(window);
    c.gridx++;
    c.gridy++;
    gridBag.setConstraints(variableTypeGroup, c);
    add(variableTypeGroup);*/
    
    GridBagLayout buttonGridBag = new GridBagLayout();
    GridBagConstraints buttonConstraints = new GridBagConstraints();
    JPanel buttonPanel = new JPanel(buttonGridBag);

    buttonConstraints.weightx = 0;
    buttonConstraints.weighty = 0;
    buttonConstraints.gridx = 0;
    buttonConstraints.gridy = 0;

    JButton valueButton = new JButton("Apply Spec");
    valueButton.addActionListener(new SpecButtonListener(this));
    buttonGridBag.setConstraints(valueButton, buttonConstraints);
    buttonPanel.add(valueButton);

    JButton resetButton = new JButton("Reset Spec");
    resetButton.addActionListener(new SpecButtonListener(this));
    buttonConstraints.gridx++;
    buttonGridBag.setConstraints(resetButton, buttonConstraints);
    buttonPanel.add(resetButton);
    
    //c.gridx++;
    c.gridy++;
    gridBag.setConstraints(buttonPanel, c);
    add(buttonPanel);
  }

  /**
   * <code>SpecButtonListener</code> -
   *                       ActionListener->SpecButtonListener
   * The listener that provides the apply and reset functionality for the buttons.
   */
  class SpecButtonListener implements ActionListener {
    private ContentSpecWindow specWindow;
    public SpecButtonListener(ContentSpecWindow specWindow) {
      this.specWindow = specWindow;
    }
    public void actionPerformed(ActionEvent ae) {
      if(ae.getActionCommand().equals("Apply Spec")) {
        StringBuffer output = new StringBuffer();
        List /*constraint, */timeInterval, variableType, predicate, timeline;
        try {
          //constraint = specWindow.constraintGroup.getValues();
          timeInterval = specWindow.timeIntervalGroup.getValues();
          //variableType = specWindow.variableTypeGroup.getValues();
          predicate = specWindow.predicateGroup.getValues();
          timeline = specWindow.timelineGroup.getValues();
        }
        catch(IllegalArgumentException e){return;}
        //if they're all null, put up a dialog
        /*output.append("Constraint: ");
        if(constraint == null) {
          output.append(" null");
        }
        else {
          ListIterator constraintIterator = constraint.listIterator();
          while(constraintIterator.hasNext()) {
            output.append(constraintIterator.next()).append(" ");
          }
        }
        output.append("\n");*/
        output.append("TimeInterval: ");
        if(timeInterval == null) {
          output.append(" null");
        }
        else {
          ListIterator timeIntervalIterator = timeInterval.listIterator();
          while(timeIntervalIterator.hasNext()) {
            output.append(timeIntervalIterator.next()).append(" ");
          }
        }
        output.append("\n");
        /*output.append("VariableType: ");
        if(variableType == null) {
            output.append(" null");
        }
        else {
          ListIterator variableTypeIterator = variableType.listIterator();
          while(variableTypeIterator.hasNext()) {
            output.append(variableTypeIterator.next()).append(" ");
          }
        }
        output.append("\n");*/
        output.append("Predicate: ");
        if(predicate == null) {
          output.append(" null");
        }
        else {
          ListIterator predicateIterator = predicate.listIterator();
          while(predicateIterator.hasNext()) {
            output.append(predicateIterator.next()).append(" ");
          }
        }
        output.append("\n");
        output.append("Timeline: ");
        if(timeline == null) {
          output.append(" null");
        }
        else {
          ListIterator timelineIterator = timeline.listIterator();
          while(timelineIterator.hasNext()) {
            output.append(timelineIterator.next()).append(" ");
          }
        }
        output.append("\n");
        System.err.println(output.toString());
        //timeline, predicate, constraint, variableType, timeInterval
        System.err.println("Applying Specification...");
        try {
          specWindow.contentSpec.applySpec(timeline, predicate, timeInterval);
        }
        catch(Exception e){
          System.err.println(e);
          e.printStackTrace();
        }
        System.err.println("Done applying Specification.");
        specWindow.contentSpec.printSpec();
      }
      else if(ae.getActionCommand().equals("Reset Spec")) {
        
        try{specWindow.contentSpec.resetSpec();}catch(Exception e){}
        //specWindow.constraintGroup.reset();
        specWindow.timeIntervalGroup.reset();
        //specWindow.variableTypeGroup.reset();
        specWindow.predicateGroup.reset();
        specWindow.timelineGroup.reset();
      }
    }
  }
}
