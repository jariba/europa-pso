// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: StringNameComparator.java,v 1.1 2003-10-07 02:13:34 taylor Exp $
//
// PlanWorks - started 06oct03
//
// Will Taylor -- 
//

package gov.nasa.arc.planworks.util;

import java.util.Comparator;


/**
 * <code>StringNameComparator</code> - alphabetic sort of String objects
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *        NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class StringNameComparator implements Comparator {

  /**
   * <code>StringNameComparator</code> - constructor 
   *
   */
  public StringNameComparator() {
  }

  /**
   * <code>compare</code>
   *
   * @param o1 - <code>Object</code> - 
   * @param o2 - <code>Object</code> - 
   * @return - <code>int</code> - 
   */
  public int compare(Object o1, Object o2) {
    String s1 = (String) o1;
    String s2 = (String) o2;
    return s1.compareTo(s2);
  }

  /**
   * <code>equals</code>
   *
   * @param o1 - <code>Object</code> - 
   * @param o2 - <code>Object</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals(Object o1, Object o2) {
    String s1 = (String)o1;
    String s2 = (String)o2;
    return s1.equals(s2);
  }

} // end class StringNameComparator
