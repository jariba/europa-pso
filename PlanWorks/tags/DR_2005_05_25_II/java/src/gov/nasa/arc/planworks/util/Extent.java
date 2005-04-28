// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: Extent.java,v 1.3 2004-02-03 19:23:55 miatauro Exp $
//
// PlanWorks -- 
//
// Conor McGann -- started 21July03
//

package gov.nasa.arc.planworks.util;

public interface Extent
{
    int getStart();
    int getEnd();
    int getRow();
    void setRow(final int row);
}
