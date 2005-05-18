// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: FindEntityPathAdapter.java,v 1.1 2004-08-21 00:31:54 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 19aug04
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.util.List;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.util.FindEntityPath;


public interface FindEntityPathAdapter {

  public abstract void invokeFindEntityPathClasses( final Integer entityKey1,
                                                    final Integer entityKey2,
                                                    final List pathClasses,
                                                    final boolean doPathExists,
                                                    final int maxPathLength,
                                                    final MDIInternalFrame dialogWindowFrame);

  public abstract List renderEntityPathNodes( final FindEntityPath findEntityPath);

} // end FindEntityPathAdapter

