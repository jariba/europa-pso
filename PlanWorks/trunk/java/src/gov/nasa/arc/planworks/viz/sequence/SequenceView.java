// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceView.java,v 1.1 2003-09-25 23:52:47 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 25sept03
//

package gov.nasa.arc.planworks.viz.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
// import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>SequenceView</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceView extends VizView {


  /**
   * <code>SequenceView</code> - constructor 
   *
   */
  public SequenceView( PwPartialPlan partialPlan, ViewSet viewSet) {
    super( partialPlan, viewSet);

    // Utilities.printFontNames();
  }




} // end class SequenceView

