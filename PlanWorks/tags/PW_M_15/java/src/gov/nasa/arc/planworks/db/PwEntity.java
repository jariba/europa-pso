// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PwEntity.java,v 1.3 2004-05-21 21:38:55 taylor Exp $
//
// PlanWorks
//
package gov.nasa.arc.planworks.db;

/**
 * Describe interface <code>PwEntity</code> here.
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwEntity {

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getId();

  public String toOutputString();
}
