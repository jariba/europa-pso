// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DuplicateNameException.java,v 1.2 2003-05-15 18:38:46 taylor Exp $
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
