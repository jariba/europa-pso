// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDomain.java,v 1.9 2004-03-23 18:20:03 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;


/**
 * <code>PwDomain</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwDomain extends PwEntity {

  /**
   * <code>getLowerBound</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getLowerBound();

  /**
   * <code>getUpperBound</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUpperBound();

  /**
   * <code>getLowerBoundInt</code> - 
   *
   * @return - <code>int</code> - 
   */
  public abstract int getLowerBoundInt();

  /**
   * <code>getUpperBoundInt</code> - 
   *
   * @return - <code>int</code> - 
   */
  public abstract int getUpperBoundInt();

  public abstract boolean isSingleton();

  /**
   * <code>toString</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String toString();


} // end interface PwDomain
