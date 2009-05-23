// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ViewRenderingException.java,v 1.2 2004-07-29 01:36:37 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.util;


public class ViewRenderingException extends ProgressMonitorException {
    
    /**
     * Creates a new instance of <code>ViewRenderingException</code>
     * without detail message.
     */
    public ViewRenderingException() {
    }
    
    
    /**
     * Constructs an instance of <code>ViewRenderingException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public ViewRenderingException( String msg) {
        super(msg);
    }
} // end class ViewRenderingException
