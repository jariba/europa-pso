//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecWindow.java,v 1.3 2003-06-16 16:28:07 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JPanel;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.viewMgr.ContentSpec;

public class ContentSpecWindow extends JPanel {
  protected ConstraintGroupBox constraintGroup;
  protected PredicateGroupBox predicateGroup;
  protected TimeIntervalGroupBox timeIntervalGroup;
  protected TimelineGroupBox timelineGroup;
  protected VariableTypeGroupBox variableTypeGroup;
  //private McLaughlanGroupBox mcLaughlanGroup

  protected ContentSpec contentSpec;

  public ContentSpecWindow(MDIInternalFrame window, ContentSpec contentSpec) {
    this.contentSpec = contentSpec;

    GridBagLayout gridBag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(gridBag);
    
    c.weightx = 0;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;
    
    constraintGroup = new ConstraintGroupBox(window);
    gridBag.setConstraints(constraintGroup, c);
    add(constraintGroup);

    predicateGroup = new PredicateGroupBox(window);
    c.gridx++;
    gridBag.setConstraints(predicateGroup, c);
    add(predicateGroup);

    timeIntervalGroup = new TimeIntervalGroupBox(window);
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(timeIntervalGroup, c);
    add(timeIntervalGroup);

    timelineGroup = new TimelineGroupBox(window);
    c.gridx++;
    gridBag.setConstraints(timelineGroup, c);
    add(timelineGroup);
    
    variableTypeGroup = new VariableTypeGroupBox(window);
    c.gridx = 0;
    c.gridy++;
    gridBag.setConstraints(variableTypeGroup, c);
    add(variableTypeGroup);
    
    JButton valueButton = new JButton("Apply Spec");
    valueButton.addActionListener(new SpecButtonListener(this));
    c.gridx++;
    gridBag.setConstraints(valueButton, c);
    add(valueButton);

    JButton resetButton = new JButton("Reset Spec");
    resetButton.addActionListener(new SpecButtonListener(this));
    c.gridx++;
    gridBag.setConstraints(resetButton, c);
    add(resetButton);
  }
  class SpecButtonListener implements ActionListener {
    private ContentSpecWindow specWindow;
    public SpecButtonListener(ContentSpecWindow specWindow) {
      this.specWindow = specWindow;
    }
    public void actionPerformed(ActionEvent ae) {
      if(ae.getActionCommand().equals("Apply Spec")) {
        StringBuffer output = new StringBuffer();
        List constraint, timeInterval, variableType, predicate, timeline;
        try {
          constraint = specWindow.constraintGroup.getValues();
          timeInterval = specWindow.timeIntervalGroup.getValues();
          variableType = specWindow.variableTypeGroup.getValues();
          predicate = specWindow.predicateGroup.getValues();
          timeline = specWindow.timelineGroup.getValues();
        }
        catch(IllegalArgumentException e){return;}
        //if they're all null, put up a dialog
        output.append("Constraint: ");
        if(constraint == null) {
          output.append(" null");
        }
        else {
          ListIterator constraintIterator = constraint.listIterator();
          while(constraintIterator.hasNext()) {
            output.append(constraintIterator.next()).append(" ");
          }
        }
        output.append("\n");
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
        output.append("VariableType: ");
        if(variableType == null) {
            output.append(" null");
        }
        else {
          ListIterator variableTypeIterator = variableType.listIterator();
          while(variableTypeIterator.hasNext()) {
            output.append(variableTypeIterator.next()).append(" ");
          }
        }
        output.append("\n");
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
        specWindow.contentSpec.applySpec(timeline, predicate, constraint, variableType, 
                                         timeInterval);
        System.err.println("Done applying Specification.");
        specWindow.contentSpec.printSpec();
      }
      else if(ae.getActionCommand().equals("Reset Spec")) {
        specWindow.contentSpec.resetSpec();
        specWindow.constraintGroup.reset();
        specWindow.timeIntervalGroup.reset();
        specWindow.variableTypeGroup.reset();
        specWindow.predicateGroup.reset();
        specWindow.timelineGroup.reset();
      }
    }
  }
}
