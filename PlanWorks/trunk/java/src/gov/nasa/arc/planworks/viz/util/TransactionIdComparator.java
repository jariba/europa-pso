// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionIdComparator.java,v 1.1 2003-10-18 01:27:55 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 16oct03
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;

import gov.nasa.arc.planworks.db.PwTransaction;


public class TransactionIdComparator implements Comparator {

  public TransactionIdComparator() {
  }

  public int compare(Object o1, Object o2) {
    Integer s1 = ((PwTransaction) o1).getId();
    Integer s2 = ((PwTransaction) o2).getId();
    return s1.compareTo(s2);
  }

  public boolean equals(Object o1, Object o2) {
    Integer s1 = ((PwTransaction) o1).getId();
    Integer s2 = ((PwTransaction) o2).getId();
    return s1.equals(s2);
  }

} // end class TransactionIdComparator
