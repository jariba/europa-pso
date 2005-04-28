// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenQueryComparatorAscending.java,v 1.2 2004-05-21 21:47:17 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;

import gov.nasa.arc.planworks.db.PwTokenQuery;
import gov.nasa.arc.planworks.viz.ViewConstants;


public class TokenQueryComparatorAscending implements Comparator {

  private String field;

  public TokenQueryComparatorAscending( String field) {
    this.field = field;
  }

  public int compare(Object o1, Object o2) {
    boolean isAscending =true;
    if (field.equals( ViewConstants.QUERY_TOKEN_KEY_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getId();
      Integer s2 = ((PwTokenQuery) o2).getId();
      return s1.compareTo(s2);
    } else if (field.equals( ViewConstants.QUERY_TOKEN_PREDICATE_HEADER)) {
      String s1 = ((PwTokenQuery) o1).getPredicateName();
      String s2 = ((PwTokenQuery) o2).getPredicateName();
      return SortStringComparator.compareTo( s1, s2, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getStepNumber();
      Integer s2 = ((PwTokenQuery) o2).getStepNumber();
      return s1.compareTo(s2);
    }
    System.err.println( "TokenQueryComparatorAscending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return 0;
  }

  public boolean equals(Object o1, Object o2) {
    if (field.equals( ViewConstants.QUERY_TOKEN_KEY_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getId();
      Integer s2 = ((PwTokenQuery) o2).getId();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.QUERY_TOKEN_PREDICATE_HEADER)) {
      String s1 = ((PwTokenQuery) o1).getPredicateName();
      String s2 = ((PwTokenQuery) o2).getPredicateName();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
      Integer s1 = ((PwTokenQuery) o1).getStepNumber();
      Integer s2 = ((PwTokenQuery) o2).getStepNumber();
      return s1.equals(s2);
    }
    System.err.println( "TokenQueryComparatorAscending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return true;
  }

} // end class TokenQueryComparatorAscending

