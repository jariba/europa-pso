// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: SortStringComparator.java,v 1.1 2003-11-03 19:02:41 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 27oct03
//

package gov.nasa.arc.planworks.viz.util;


/**
 * <code>SortStringComparator</code> - static method to handle empty strings &
 *                                     upper/lower case alphabetics
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SortStringComparator {

  /**
   * <code>SortStringComparator</code> - constructor 
   *
   */
  private SortStringComparator() {
  }

  /**
   * <code>compareTo</code> - modify compareTo to prefer any non-empty string to an
   *                          empty string, and sort strings as upper case
   *
   * @param string - <code>String</code> - 
   * @param anotherString - <code>String</code> - 
   * @param isAscending - <code>boolean</code> - 
   * @return - <code>int</code> - 
   */
  public static int compareTo( String string, String anotherString, boolean isAscending) {
    int len1 = string.length();
    int len2 = anotherString.length();

    if ((len1 == 0) && (len2 == 0)) {
      return 0;
    } else if (len2 == 0) {
      if (isAscending) {
        return -1;
      } else {
        return 1;
      }
    } else if (len1 == 0) {
      if (isAscending) {
        return 1;
      } else {
        return -1;
      }
    } else {
      return string.toUpperCase().compareTo( anotherString.toUpperCase());
    }
  } // end compareTo

} // end class SortStringComparator

