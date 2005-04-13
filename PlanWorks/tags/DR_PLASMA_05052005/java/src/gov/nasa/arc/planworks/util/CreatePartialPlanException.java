// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: CreatePartialPlanException.java,v 1.1 2004-07-27 21:58:07 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26july04
//

package gov.nasa.arc.planworks.util;


public class CreatePartialPlanException extends ProgressMonitorException {
    
    /**
     * Creates a new instance of <code>CreatePartialPlanException</code>
     * without detail message.
     */
    public CreatePartialPlanException() {
    }
    
    
    /**
     * Constructs an instance of <code>CreatePartialPlanException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public CreatePartialPlanException( String msg) {
        super(msg);
    }
} // end class CreatePartialPlanException
