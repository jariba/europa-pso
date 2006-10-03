//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: ContentSpecWindow.java,v 1.18 2006-10-03 16:14:18 miatauro Exp $
//
package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.partialPlan;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.util.ContentSpec;
import gov.nasa.arc.planworks.db.util.PartialPlanContentSpec;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.constraintNetwork.ConstraintNetworkView;


/**
 * <code>ContentSpecWindow</code> -
 *                      JPanel->ContentSpecWindow
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * The complete, displayable window for defining a content specification for the associated 
 * partial plan.  Provides input fields for specifying timelines, constraints, predicates,
 * variables by type, and time intervals.
 */

public class ContentSpecWindow extends JPanel implements MouseListener {
  //  protected ConstraintGroupBox constraintGroup;
  protected PredicateGroupBox predicateGroup;
  protected TimeIntervalGroupBox timeIntervalGroup;
  protected TimelineGroupBox timelineGroup;
  protected MergeBox mergeBox;
  protected TokenTypeBox tokenTypeBox;
  protected UniqueKeyGroupBox uniqueKeyGroup;
  //protected VariableTypeGroupBox variableTypeGroup;
  //private McLaughlanGroupBox mcLaughlanGroup

  protected PartialPlanContentSpec contentSpec;
  protected PartialPlanViewSet partialPlanViewSet;

  private MDIInternalFrame frame;
  private JButton valueButton;
  private static boolean queryTestExists;

  /**
   * <code>ContentSpecWindow
   * Constructs the entire content specification window.
   * @param window The <code>MDIInternalFrame</code> to which this pane is added.  Used for the 
   *               <code>pack()</code> method.
   * @param contentSpec The ContentSpec with which this window is associated.  Instantiated in
   *                    ViewSet
   */
  public ContentSpecWindow(MDIInternalFrame window, ContentSpec contentSpec,
                           PartialPlanViewSet partialPlanViewSet) {
    this.frame = window;
    this.contentSpec = (PartialPlanContentSpec) contentSpec;
    this.partialPlanViewSet = partialPlanViewSet;
    queryTestExists = false;
    // for PWTestHelper.findComponentByName
    this.setName( window.getTitle());

    System.err.println("Getting predicate names...");
    Map predicateNames = this.contentSpec.getPredicateNames();
    System.err.println("Getting timeline names...");
    Map timelineNames = this.contentSpec.getTimelineNames();

    System.err.println("Pred: " + predicateNames);
    System.err.println("Time: " + timelineNames);
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

    uniqueKeyGroup = new UniqueKeyGroupBox(window);
    c.gridy++;
    gridBag.setConstraints(uniqueKeyGroup, c);
    add(uniqueKeyGroup);

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

    valueButton = new JButton("Apply Filter");
    valueButton.addActionListener(new SpecButtonListener(this));
    buttonGridBag.setConstraints(valueButton, buttonConstraints);
    buttonPanel.add(valueButton);

    JButton resetButton = new JButton("Reset Filter");
    resetButton.addActionListener(new SpecButtonListener(this));
    buttonConstraints.gridx++;
    buttonGridBag.setConstraints(resetButton, buttonConstraints);
    buttonPanel.add(resetButton);
    
    //c.gridx++;
    c.gridy++;
    gridBag.setConstraints(buttonPanel, c);
    add(buttonPanel);
    buildFromSpec();
    valueButton.doClick();
    addMouseListener( this);
  }

  public MDIInternalFrame getFrame() {
    return frame;
  }

  public PartialPlanContentSpec getSpec() {
    return contentSpec;
  }

  public void applySpec() {
    valueButton.doClick();
  }

  public void buildFromSpec() {
    List currentSpec = contentSpec.getCurrentSpec();
    if(currentSpec.size() == 0) {
      return;
    }
    List timelines = (List) currentSpec.get(0);
    List predicates = (List) currentSpec.get(1);
    List timeIntervals = (List) currentSpec.get(2);
    boolean mergeTokens = ((Boolean)currentSpec.get(3)).booleanValue();
    int tokenTypes = ((Integer)currentSpec.get(4)).intValue();
    List uniqueKeys = (List) currentSpec.get(5);

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
      firstPredicate.setSelectedComboItem(predicates.get(1));
      for(int i = 2; i < predicates.size(); i += 2) {
        String connective = (String)predicates.get(i);
        Object predicate = (Object)predicates.get(i+1);
        predicateBoxes = predicateGroup.getElements();
        ListIterator predicateBoxIterator = predicateBoxes.listIterator();
        while(predicateBoxIterator.hasNext()) {
          //SpecBox temp = (SpecBox) predicateBoxIterator.next();
          //System.err.println("=====>" + temp.getName() + " : " + temp.getClass().getName());
          //PredicateBox box = (PredicateBox) temp;//(PredicateBox) predicateBoxIterator.next();
          SpecBox box = (SpecBox) predicateBoxIterator.next();
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
          //TimelineBox box = (TimelineBox) timelineBoxIterator.next();
          SpecBox box = (SpecBox) timelineBoxIterator.next();
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
    if(uniqueKeys != null && uniqueKeys.size() != 0) {
      for(int i = 0; i < uniqueKeys.size(); i += 2) {
        List uniqueKeyBoxes = uniqueKeyGroup.getElements();
        String op = (String) uniqueKeys.get(i);
        Integer key = (Integer) uniqueKeys.get(i+1);
        ListIterator uniqueKeyIterator = uniqueKeyBoxes.listIterator();
        while(uniqueKeyIterator.hasNext()) {
          UniqueKeyBox box = (UniqueKeyBox) uniqueKeyIterator.next();
          if(!box.getKeyField().isEnabled()) {
            if(op.equals(PartialPlanContentSpec.REQUIRE)) {
              box.getRequireButton().setSelected(true);
            }
            else if(op.equals(PartialPlanContentSpec.EXCLUDE)) {
              box.getExcludeButton().setSelected(true);
            }
            else {
              System.err.println("Invalid unique key op: " + op);
              System.exit(-1);
            }
            box.getKeyField().setEnabled(true);
            box.getKeyField().setText(key.toString());
            if(i != uniqueKeys.size() - 2) {
              box.getAddButton().doClick();
            }
            //box.getAddButton().fireActionPerformed(new ActionEvent(null, 0, ""));
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
      if(ae.getActionCommand().equals("Apply Filter")) {
        List timeInterval, predicate, timeline, uniqueKeys;
        boolean mergeTokens;
        int tokenType;
        try {
          timeInterval = specWindow.timeIntervalGroup.getValues();
          predicate = specWindow.predicateGroup.getValues();
          timeline = specWindow.timelineGroup.getValues();
          mergeTokens = specWindow.mergeBox.getValue();
          tokenType = specWindow.tokenTypeBox.getValue();
          uniqueKeys = specWindow.uniqueKeyGroup.getValues();
        }
        catch(IllegalArgumentException e){return;}

        System.err.println("   Applying Filter ...");
        long startTimeMSecs = System.currentTimeMillis();
        forceConstraintNetworkViewLayout();
        try {
          List specList = new ArrayList();
          specList.add( timeline);
          specList.add( predicate);
          specList.add( timeInterval);
          specList.add( new Boolean( mergeTokens));
          specList.add( new Integer( tokenType));
          specList.add(uniqueKeys);
          //if(!specWindow.specChanged(specList)) {
          //  return;
          //}
          specWindow.contentSpec.applySpec(specList);
        }
        catch(Exception e){
          System.err.println(e);
          e.printStackTrace();
        }
        scrollViewsToZeroZero();
        long stopTimeMSecs = System.currentTimeMillis();
        System.err.println( "      ... elapsed time: " +
                            (stopTimeMSecs - startTimeMSecs) + " msecs.");
        //specWindow.contentSpec.printSpec();
      }
      else if(ae.getActionCommand().equals("Reset Filter")) {
        forceConstraintNetworkViewLayout();
        
        try{specWindow.contentSpec.resetSpec();}catch(Exception e){}
        specWindow.timeIntervalGroup.reset();
        specWindow.predicateGroup.reset();
        specWindow.timelineGroup.reset();
        specWindow.mergeBox.reset();
        specWindow.tokenTypeBox.reset();
        specWindow.uniqueKeyGroup.reset();
        scrollViewsToZeroZero();
      }
    }
  } // end class SpecButtonListener

  private void forceConstraintNetworkViewLayout() {
    int numToReturn = 0; // all of them
    List partialPlanViews = partialPlanViewSet.getPartialPlanViews( numToReturn);
    Iterator viewsItr = partialPlanViews.iterator();
    while (viewsItr.hasNext()) {
      PartialPlanView partialPlanView = (PartialPlanView) viewsItr.next();
      if (partialPlanView instanceof ConstraintNetworkView) {
        ((ConstraintNetworkView) partialPlanView).setLayoutNeeded();
        ((ConstraintNetworkView) partialPlanView).setFocusNode( null);
        break;
      }
    }
  } // end forceConstraintNetworkViewLayout

  private void scrollViewsToZeroZero() {
    int numToReturn = 0; // all of them
    List partialPlanViews = partialPlanViewSet.getPartialPlanViews( numToReturn);
    Iterator viewsItr = partialPlanViews.iterator();
    while (viewsItr.hasNext()) {
      JGoView jGoView = ((PartialPlanView) viewsItr.next()).getJGoView();
      if (jGoView != null) {
        if (jGoView.getHorizontalScrollBar() != null) {
          jGoView.getHorizontalScrollBar().setValue( 0);
        }
        if (jGoView.getVerticalScrollBar() != null) {
          jGoView.getVerticalScrollBar().setValue( 0);
        }
      }
    }
  } // end scrollViewsToZeroZero

  /**
   * mouseEntered - implement MouseListener - do nothing
   *
   * @param mouseEvent - MouseEvent 
   */
  public void mouseEntered( MouseEvent mouseEvent) {
    // System.err.println( "mouseEntered " + mouseEvent.getPoint());
  }

  /**
   * mouseExited - implement MouseListener -  do nothing
   *
   * @param mouseEvent - MouseEvent 
   */
  public void mouseExited( MouseEvent mouseEvent) {
    // System.err.println( "mouseExited " + mouseEvent.getPoint());
  }

  /**
   * mouseClicked - implement MouseListener -
   *
   * @param mouseEvent - MouseEvent 
   */ 
  public void mouseClicked( MouseEvent mouseEvent) {
    // System.err.println( "mouseClicked " + mouseEvent.getModifiers());
    if (MouseEventOSX.isMouseLeftClick( mouseEvent, PlanWorks.isMacOSX())) {

    } else if (MouseEventOSX.isMouseRightClick( mouseEvent, PlanWorks.isMacOSX())) {
      mouseRightPopupMenu( mouseEvent.getPoint());
    }
  } // end mouseClicked 

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    PwPartialPlan partialPlan = contentSpec.getPartialPlan();
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    int numToReturn = 1;
    List partialPlanViews = partialPlanViewSet.getPartialPlanViews( numToReturn);
    if (partialPlanViews.size() > 0) {
      PartialPlanView aPartialPlanView = (PartialPlanView) partialPlanViews.get(0);
      aPartialPlanView.createOpenViewItems( partialPlan, partialPlanName, planSequence,
                                            mouseRightPopup, "");
      aPartialPlanView.createAllViewItems( partialPlan, partialPlanName, planSequence,
                                           mouseRightPopup);
      aPartialPlanView.createStepAllViewItems( partialPlan, mouseRightPopup);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);

    }
  } // end mouseRightPopupMenu

  /**
   * <code>mouseRightPopupMenu</code>
   *
   * @param viewListenerList - <code>List</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  public void mouseRightPopupMenu( List viewListenerList, Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    PwPartialPlan partialPlan = contentSpec.getPartialPlan();
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    int numToReturn = 1;
    List partialPlanViews = partialPlanViewSet.getPartialPlanViews( numToReturn);
    if (partialPlanViews.size() > 0) {
      PartialPlanView aPartialPlanView = (PartialPlanView) partialPlanViews.get(0);
      aPartialPlanView.createOpenViewItems( partialPlan, partialPlanName, planSequence,
                                            mouseRightPopup, viewListenerList, "");
      aPartialPlanView.createAllViewItems( partialPlan, partialPlanName, planSequence,
                                           viewListenerList, mouseRightPopup);
      aPartialPlanView.createStepAllViewItems( partialPlan, mouseRightPopup);

      ViewGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);

    }
  } // end mouseRightPopupMenu

  /**
   * mousePressed - implement MouseListener - do nothing
   *
   * @param mouseEvent - MouseEvent 
   */
  public void mousePressed( MouseEvent mouseEvent) {
    // System.err.println( "mousePressed " + mouseEvent.getPoint());
  } // end mousePressed

  /**
   * mouseReleased - implement MouseListener - do nothing
   *
   * @param mouseEvent - MouseEvent
   */
  public void mouseReleased( MouseEvent mouseEvent) {
    // System.err.println( "mouseReleased " + mouseEvent.getPoint());
  } // end mouseReleased

} // end class ContentSpecWindow

