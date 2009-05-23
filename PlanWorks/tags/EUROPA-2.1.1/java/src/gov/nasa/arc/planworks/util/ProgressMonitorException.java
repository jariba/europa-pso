// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ProgressMonitorException.java,v 1.1 2004-07-27 21:58:08 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26july04
//

package gov.nasa.arc.planworks.util;


public class ProgressMonitorException extends Exception {
    
    /**
     * Creates a new instance of <code>ProgressMonitorException</code>
     * without detail message.
     */
    public ProgressMonitorException() {
    }
    
    
    /**
     * Constructs an instance of <code>ProgressMonitorException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public ProgressMonitorException( String msg) {
        super(msg);
    }
} // end class ProgressMonitorException
