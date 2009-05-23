// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ResourceNotFoundException.java,v 1.2 2003-05-15 18:38:46 taylor Exp $
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
