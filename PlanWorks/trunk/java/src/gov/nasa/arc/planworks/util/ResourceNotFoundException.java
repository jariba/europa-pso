// $Id: ResourceNotFoundException.java,v 1.1 2003-05-10 01:00:34 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.util;


public class ResourceNotFoundException extends Exception {
    
    /**
     * Creates a new instance of <code>ResourceNotFoundException</code>
     * without detail message.
     */
    public ResourceNotFoundException() {
    }
    
    
    /**
     * Constructs an instance of <code>ResourceNotFoundException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public ResourceNotFoundException( String msg) {
        super(msg);
    }
} // end class ResourceNotFoundException
