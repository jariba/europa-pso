// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenQueryComparatorDescending.java,v 1.1 2003-12-20 01:54:52 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;

import gov.nasa.arc.planworks.db.PwTokenQuery;
import gov.nasa.arc.planworks.viz.ViewConstants;


public class TokenQueryComparatorDescending implements Comparator {

  private String field;

  public TokenQueryComparatorDescending( String field) {
    this.field = field;
  }

  public int compare(Object o1, Object o2) {
    boolean isAscending = false;
    if (field.equals( ViewConstants.QUERY_TOKEN_KEY_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getId();
      Integer s2 = ((PwTokenQuery) o2).getId();
      return s2.compareTo(s1);
    } else if (field.equals( ViewConstants.QUERY_TOKEN_PREDICATE_HEADER)) {
      String s1 = ((PwTokenQuery) o1).getPredicateName();
      String s2 = ((PwTokenQuery) o2).getPredicateName();
      return SortStringComparator.compareTo(s2, s1, isAscending);
    } else if (field.equals( ViewConstants.QUERY_TOKEN_STEP_NUM_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getStepNumber();
      Integer s2 = ((PwTokenQuery) o2).getStepNumber();
      return s2.compareTo(s1);
    }
    System.err.println( "TokenQueryComparatorDescending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return 0;
  }

  public boolean equals(Object o1, Object o2) {
    if (field.equals( ViewConstants.QUERY_TOKEN_KEY_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getId();
      Integer s2 = ((PwTokenQuery) o2).getId();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.QUERY_TOKEN_PREDICATE_HEADER)) {
      String s1 = ((PwTokenQuery) o1).getPredicateName();
      String s2 = ((PwTokenQuery) o2).getPredicateName();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.QUERY_TOKEN_STEP_NUM_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getStepNumber();
      Integer s2 = ((PwTokenQuery) o2).getStepNumber();
      return s2.equals(s1);
    }
    System.err.println( "TokenQueryComparatorDescending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return true;
  }

} // end class TokenQueryComparatorDescending
