// $Id: DuplicateNameException.java,v 1.1 2003-05-10 01:00:34 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.util;


public class DuplicateNameException extends Exception {
    
    /**
     * Creates a new instance of <code>DuplicateNameException</code>
     * without detail message.
     */
    public DuplicateNameException() {
    }
    
    
    /**
     * Constructs an instance of <code>DuplicateNameException</code>
     * with the specified detail message.
     * @param msg the detail message.
     */
    public DuplicateNameException( String msg) {
        super(msg);
    }
} // end class DuplicateNameException
