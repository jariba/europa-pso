// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: VizView.java,v 1.2 2003-05-21 23:48:37 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 18May03
//

package gov.nasa.arc.planworks.viz.views;

import javax.swing.JPanel;

import gov.nasa.arc.planworks.db.PwPartialPlan;


/**
 * <code>VizView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizView extends JPanel {

  protected PwPartialPlan partialPlan;
  protected String collectionName;

  /**
   * <code>VizView</code> - constructor 
   *
   * @param partialPlan - <code>PwPartialPlan</code> - 
   */
  public VizView( PwPartialPlan partialPlan) {
    super();
    this.partialPlan = partialPlan;
    this.collectionName = partialPlan.getCollectionName();
  }

} // end class VizView

