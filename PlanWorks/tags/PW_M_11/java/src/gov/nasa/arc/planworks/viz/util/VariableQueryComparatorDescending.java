// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableQueryComparatorDescending.java,v 1.1 2003-12-20 01:54:52 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 19dec03
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;

import gov.nasa.arc.planworks.db.PwVariableQuery;
import gov.nasa.arc.planworks.viz.ViewConstants;


public class VariableQueryComparatorDescending implements Comparator {

  private String field;

  public VariableQueryComparatorDescending( String field) {
    this.field = field;
  }

  public int compare(Object o1, Object o2) {
    boolean isAscending = false;
    if (field.equals( ViewConstants.QUERY_VARIABLE_KEY_HEADER)) {
      Integer s1 = ((PwVariableQuery) o1).getId();
      Integer s2 = ((PwVariableQuery) o2).getId();
      return s2.compareTo(s1);
    } else if (field.equals( ViewConstants.QUERY_VARIABLE_NAME_HEADER)) {
      String s1 = ((PwVariableQuery) o1).getName();
      String s2 = ((PwVariableQuery) o2).getName();
      return SortStringComparator.compareTo(s2, s1, isAscending);
    } else if (field.equals( ViewConstants.QUERY_VARIABLE_STEP_NUM_HEADER)) {
      Integer s1 = ((PwVariableQuery) o1).getStepNumber();
      Integer s2 = ((PwVariableQuery) o2).getStepNumber();
      return s2.compareTo(s1);
    }
    System.err.println( "VariableQueryComparatorDescending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return 0;
  }

  public boolean equals(Object o1, Object o2) {
    if (field.equals( ViewConstants.QUERY_VARIABLE_KEY_HEADER)) {
      Integer s1 = ((PwVariableQuery) o1).getId();
      Integer s2 = ((PwVariableQuery) o2).getId();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.QUERY_VARIABLE_NAME_HEADER)) {
      String s1 = ((PwVariableQuery) o1).getName();
      String s2 = ((PwVariableQuery) o2).getName();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.QUERY_VARIABLE_STEP_NUM_HEADER)) {
      Integer s1 = ((PwVariableQuery) o1).getStepNumber();
      Integer s2 = ((PwVariableQuery) o2).getStepNumber();
      return s2.equals(s1);
    }
    System.err.println( "VariableQueryComparatorDescending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return true;
  }

} // end class VariableQueryComparatorDescending
