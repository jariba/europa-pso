// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: StepContentView.java,v 1.1 2003-10-18 01:27:54 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17oct03
//

package gov.nasa.arc.planworks.viz;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.viz.nodes.StepField;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>StepContentView</code> - render values of step object as TransactionField's
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class StepContentView extends JGoView {

  private List stepList; // element Integer
  private StepHeaderView headerJGoView;
  private ViewableObject viewableObject; // PwPartialPlan or PwPlanning Sequence
  private VizView vizView; // PartialPlanView  or SequenceView
  private JGoDocument jGoDocument;
  private List stepFieldList; // element StepField;

  /**
   * <code>StepContentView</code> - constructor 
   *
   * @param stepList - <code>List</code> - of Integer
   * @param headerJGoView - <code>StepHeaderView</code> - 
   * @param viewableObject - <code>ViewableObject</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public StepContentView( List stepList, StepHeaderView headerJGoView,
                                 ViewableObject viewableObject, VizView vizView) {
    super();
    this.stepList = stepList;
    this.headerJGoView = headerJGoView;
    this.viewableObject = viewableObject;
    this.vizView = vizView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument = this.getDocument();

    renderStepContent();
  }

  private void renderStepContent() {
    Color bgColor = ViewConstants.VIEW_BACKGROUND_COLOR;
    int x = 0, y = 5;
    List stepFieldList = new ArrayList();
    Iterator stepsItr = stepList.iterator();
    int i = 1;
    while (stepsItr.hasNext()) {
      x = 0;
      Integer stepNumber = (Integer) stepsItr.next();
      StepField stepNumField =
        new StepField( stepNumber.toString(), new Point( x, y),
                              JGoText.ALIGN_RIGHT, bgColor, viewableObject);
      jGoDocument.addObjectAtTail( stepNumField);
      stepNumField.setSize( (int) headerJGoView.getStepNumNode().getSize().getWidth(),
                            (int) stepNumField.getSize().getHeight());
      x += headerJGoView.getStepNumNode().getSize().getWidth();
      stepFieldList.add( stepNumField);

      y += stepNumField.getSize().getHeight();
      i++;
    }
  } // end renderSteps


} // end class StepContentView
