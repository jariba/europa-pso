//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecWindow.java,v 1.21 2003-09-25 23:52:48 taylor Exp $
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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.PartialPlanContentSpec;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;

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
  protected MergeBox mergeBox;
  protected TokenTypeBox tokenTypeBox;
  //protected VariableTypeGroupBox variableTypeGroup;
  //private McLaughlanGroupBox mcLaughlanGroup

  protected PartialPlanContentSpec contentSpec;

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
    this.contentSpec = (PartialPlanContentSpec) contentSpec;
    queryTestExists = false;

    Map predicateNames = this.contentSpec.getPredicateNames();
    Map timelineNames = this.contentSpec.getTimelineNames();

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

    mergeBox = new MergeBox();
    c.gridy++;
    gridBag.setConstraints(mergeBox, c);
    add(mergeBox);
    
    tokenTypeBox = new TokenTypeBox();
    c.gridy++;
    gridBag.setConstraints(tokenTypeBox, c);
    add(tokenTypeBox);

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
    buildFromSpec();
  }

  private void buildFromSpec() {
    List currentSpec = contentSpec.getCurrentSpec();
    if(currentSpec.size() == 0) {
      return;
    }
    List timelines = (List) currentSpec.get(0);
    List predicates = (List) currentSpec.get(1);
    List timeIntervals = (List) currentSpec.get(2);
    boolean mergeTokens = ((Boolean)currentSpec.get(3)).booleanValue();
    int tokenTypes = ((Integer)currentSpec.get(4)).intValue();

    for(int i = 0; i < mergeBox.getComponentCount(); i++) {
      if(mergeBox.getComponent(i) instanceof JCheckBox) {
        ((JCheckBox)mergeBox.getComponent(i)).setSelected(mergeTokens);
        break;
      }
    }
    for(int i = 0; i < tokenTypeBox.getComponentCount(); i++) {
      if(tokenTypeBox.getComponent(i) instanceof JRadioButton) {
        JRadioButton button = (JRadioButton) tokenTypeBox.getComponent(i);
        if(button.getText().equals("all") && tokenTypes == PartialPlanContentSpec.ALL) {
          button.setSelected(true);
          break;
        }
        else if(button.getText().equals("slotted") && tokenTypes ==
                PartialPlanContentSpec.SLOTTED_ONLY) {
          button.setSelected(true);
          break;
        }
        else if(button.getText().equals("free") && tokenTypes ==
                PartialPlanContentSpec.FREE_ONLY) {
          button.setSelected(true);
          break;
        }
      }
    }
    if(predicates != null && predicates.size() != 0) {
      List predicateBoxes = predicateGroup.getElements();
      PredicateBox firstPredicate = (PredicateBox) predicateBoxes.get(0);
      if(((String)predicates.get(0)).indexOf("not") != -1) {
        firstPredicate.getNegationBox().setSelected(true);
      }
      firstPredicate.setSelectedComboItem((Integer)predicates.get(1));
      for(int i = 2; i < predicates.size(); i += 2) {
        String connective = (String)predicates.get(i);
        Integer predicate = (Integer)predicates.get(i+1);
        predicateBoxes = predicateGroup.getElements();
        ListIterator predicateBoxIterator = predicateBoxes.listIterator();
        while(predicateBoxIterator.hasNext()) {
          PredicateBox box = (PredicateBox) predicateBoxIterator.next();
          if(box.getLogicBox().isEnabled() && !box.getComboBox().isEnabled()) {
            if(connective.indexOf("and") != -1) {
              box.getLogicBox().setSelectedItem("AND");
            }
            else if(connective.indexOf("or") != -1) {
              box.getLogicBox().setSelectedItem("OR");
            }
            else {
              System.err.println("Logical connective without 'and' or 'or'!");
              System.exit(-1);
            }
            if(connective.indexOf("not") != -1) {
              box.getNegationBox().setSelected(true);
            }
            box.setSelectedComboItem(predicate);
            break;
          }
        }
      }
    }
    if(timelines != null && timelines.size() != 0) {
      List timelineBoxes = timelineGroup.getElements();
      TimelineBox firstTimeline = (TimelineBox) timelineBoxes.get(0);
      if(((String)timelines.get(0)).indexOf("not") != -1) {
        firstTimeline.getNegationBox().setSelected(true);
      }
      firstTimeline.setSelectedComboItem((Integer)timelines.get(1));
      for(int i = 2; i < timelines.size(); i += 2) {
        String connective = (String)timelines.get(i);
        Integer timeline = (Integer)timelines.get(i+1);
        timelineBoxes = timelineGroup.getElements();
        ListIterator timelineBoxIterator = timelineBoxes.listIterator();
        while(timelineBoxIterator.hasNext()) {
          TimelineBox box = (TimelineBox) timelineBoxIterator.next();
          if(box.getLogicBox().isEnabled() && !box.getComboBox().isEnabled()) {
            if(connective.indexOf("and") != -1) {
              box.getLogicBox().setSelectedItem("AND");
            }
            else if(connective.indexOf("or") != -1) {
              box.getLogicBox().setSelectedItem("OR");
            }
            else {
              System.err.println("Logical connective without 'and' or 'or'!");
              System.exit(-1);
            }
            if(connective.indexOf("not") != -1) {
              box.getNegationBox().setSelected(true);
            }
            box.setSelectedComboItem(timeline);
            break;
          }
        }
      }
    }
    if(timeIntervals != null && timeIntervals.size() != 0) {
      List timeIntervalBoxes = timeIntervalGroup.getElements();
      TimeIntervalBox firstTimeInterval = (TimeIntervalBox) timeIntervalBoxes.get(0);
      if(((String) timeIntervals.get(0)).indexOf("not") != -1) {
        firstTimeInterval.getNegationBox().setSelected(true);
      }
      firstTimeInterval.getStartValue().setText(((Integer)timeIntervals.get(1)).toString());
      firstTimeInterval.getEndValue().setText(((Integer)timeIntervals.get(2)).toString());
      for(int i = 3; i < timeIntervals.size(); i += 3) {
        String connective = (String) timeIntervals.get(i);
        Integer startTime = (Integer) timeIntervals.get(i+1);
        Integer endTime = (Integer) timeIntervals.get(i+2);
        timeIntervalBoxes = timeIntervalGroup.getElements();
        ListIterator timeIntervalIterator = timeIntervalBoxes.listIterator();
        while(timeIntervalIterator.hasNext()) {
          TimeIntervalBox box = (TimeIntervalBox) timeIntervalIterator.next();
          if(box.getLogicBox().isEnabled() && !box.getStartValue().isEnabled()) {
            if(connective.indexOf("and") != -1) {
              box.getLogicBox().setSelectedItem("AND");
            }
            else if(connective.indexOf("or") != -1) {
              box.getLogicBox().setSelectedItem("OR");
            }
            else {
              System.err.println("Logical connective without 'and' or 'or'!");
              System.exit(-1);
            }
            if(connective.indexOf("not") != -1) {
              box.getNegationBox().setSelected(true);
            }
            box.getStartValue().setText(startTime.toString());
            box.getEndValue().setText(endTime.toString());
            break;
          }
        }
      }
    }
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
        List /*constraint, */timeInterval, /*variableType, */predicate, timeline;
        boolean mergeTokens;
        int tokenType;
        try {
          //constraint = specWindow.constraintGroup.getValues();
          timeInterval = specWindow.timeIntervalGroup.getValues();
          //variableType = specWindow.variableTypeGroup.getValues();
          predicate = specWindow.predicateGroup.getValues();
          timeline = specWindow.timelineGroup.getValues();
          mergeTokens = specWindow.mergeBox.getValue();
          tokenType = specWindow.tokenTypeBox.getValue();
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
        output.append(" merge ").append(mergeTokens);
        output.append(" type ").append(tokenType);
        output.append("\n");
        System.err.println(output.toString());
        //timeline, predicate, constraint, variableType, timeInterval
        System.err.println("Applying Specification...");
        try {
          List specList = new ArrayList();
          specList.add( timeline);
          specList.add( predicate);
          specList.add( timeInterval);
          specList.add( new Boolean( mergeTokens));
          specList.add( new Integer( tokenType));
          specWindow.contentSpec.applySpec(specList);
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
        specWindow.mergeBox.reset();
        specWindow.tokenTypeBox.reset();
      }
    }
  }
}
