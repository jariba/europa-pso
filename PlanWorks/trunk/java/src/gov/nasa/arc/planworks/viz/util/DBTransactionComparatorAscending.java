// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: DBTransactionComparatorAscending.java,v 1.1 2004-02-03 20:44:02 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 27oct03
//

package gov.nasa.arc.planworks.viz.util;

import java.util.Comparator;

import gov.nasa.arc.planworks.db.PwDBTransaction;
import gov.nasa.arc.planworks.viz.ViewConstants;


public class DBTransactionComparatorAscending implements Comparator {

  private String field;

  public DBTransactionComparatorAscending( String field) {
    this.field = field;
  }

  public int compare(Object o1, Object o2) {
    boolean isAscending =true;
    if (field.equals( ViewConstants.DB_TRANSACTION_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getId();
      Integer s2 = ((PwDBTransaction) o2).getId();
      return s1.compareTo(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_TYPE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getType();
      String s2 = ((PwDBTransaction) o2).getType();
      return SortStringComparator.compareTo( s1, s2, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_SOURCE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getSource();
      String s2 = ((PwDBTransaction) o2).getSource();
      return SortStringComparator.compareTo( s1, s2, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getObjectId();
      Integer s2 = ((PwDBTransaction) o2).getObjectId();
      return s1.compareTo(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getStepNumber();
      Integer s2 = ((PwDBTransaction) o2).getStepNumber();
      return s1.compareTo(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[0];
      String s2 = ((PwDBTransaction) o2).getInfo()[0];
      return SortStringComparator.compareTo( s1, s2, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[1];
      String s2 = ((PwDBTransaction) o2).getInfo()[1];
      return SortStringComparator.compareTo( s1, s2, isAscending);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[2];
      String s2 = ((PwDBTransaction) o2).getInfo()[2];
      return SortStringComparator.compareTo( s1, s2, isAscending);
    }
    System.err.println( "DBTransactionComparatorAscending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return 0;
  }

  public boolean equals(Object o1, Object o2) {
    if (field.equals( ViewConstants.DB_TRANSACTION_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getId();
      Integer s2 = ((PwDBTransaction) o2).getId();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_TYPE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getType();
      String s2 = ((PwDBTransaction) o2).getType();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_SOURCE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getSource();
      String s2 = ((PwDBTransaction) o2).getSource();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJECT_KEY_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getObjectId();
      Integer s2 = ((PwDBTransaction) o2).getObjectId();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_STEP_NUM_HEADER)) {
      Integer s1 = ((PwDBTransaction) o1).getStepNumber();
      Integer s2 = ((PwDBTransaction) o2).getStepNumber();
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_OBJ_NAME_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[0];
      String s2 = ((PwDBTransaction) o2).getInfo()[0];
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PREDICATE_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[1];
      String s2 = ((PwDBTransaction) o2).getInfo()[1];
      return s1.equals(s2);
    } else if (field.equals( ViewConstants.DB_TRANSACTION_PARAMETER_HEADER)) {
      String s1 = ((PwDBTransaction) o1).getInfo()[2];
      String s2 = ((PwDBTransaction) o2).getInfo()[2];
      return s1.equals(s2);
    }
    System.err.println( "DBTransactionComparatorAscending: field '" + field +
                        "' not handled");
    System.exit( -1);
    return true;
  }

} // end class DBTransactionComparatorAscending

