// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionComparatorDescending.java,v 1.2 2004-05-08 01:44:18 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 27oct03
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;

import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.viz.ViewConstants;


public class DBTransactionComparatorDescending implements Comparator {

  private String field;

  public DBTransactionComparatorDescending( String field) {
    this.field = field;
  }

  public int compare(Object o1, Object o2) {
    boolean isAscending = false;
    if (field.equals( ViewConstants.DB_TRANSACTION_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getId();
      Integer s2 = ((PwDBTransaction) o2).getId();
      return s2.compareTo(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_NAME_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getName();
      String s2 = ((PwDBTransaction) o2).getName();
      return SortStringComparator.compareTo(s2, s1, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_SOURCE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getSource();
      String s2 = ((PwDBTransaction) o2).getSource();
      return SortStringComparator.compareTo(s2, s1, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getObjectId();
      Integer s2 = ((PwDBTransaction) o2).getObjectId();
      return s2.compareTo(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getStepNumber();
      Integer s2 = ((PwDBTransaction) o2).getStepNumber();
      return s2.compareTo(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[0];
      String s2 = ((PwDBTransaction) o2).getInfo()[0];
      return SortStringComparator.compareTo(s2, s1, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[1];
      String s2 = ((PwDBTransaction) o2).getInfo()[1];
      return SortStringComparator.compareTo(s2, s1, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[2];
      String s2 = ((PwDBTransaction) o2).getInfo()[2];
      return SortStringComparator.compareTo(s2, s1, isAscending);
    }
    System.err.println( "DBTransactionComparatorDescending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return 0;
  }

  public boolean equals(Object o1, Object o2) {
    if (field.equals( ViewConstants.DB_TRANSACTION_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getId();
      Integer s2 = ((PwDBTransaction) o2).getId();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_NAME_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getName();
      String s2 = ((PwDBTransaction) o2).getName();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_SOURCE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getSource();
      String s2 = ((PwDBTransaction) o2).getSource();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getObjectId();
      Integer s2 = ((PwDBTransaction) o2).getObjectId();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getStepNumber();
      Integer s2 = ((PwDBTransaction) o2).getStepNumber();
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[0];
      String s2 = ((PwDBTransaction) o2).getInfo()[0];
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[1];
      String s2 = ((PwDBTransaction) o2).getInfo()[1];
      return s2.equals(s1);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[2];
      String s2 = ((PwDBTransaction) o2).getInfo()[2];
      return s2.equals(s1);
    }
    System.err.println( "DBTransactionComparatorDescending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return true;
  }

} // end class DBTransactionComparatorDescending
