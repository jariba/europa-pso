// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PwEntity.java,v 1.2 2004-03-23 18:20:03 miatauro Exp $
//
// PlanWorks
//
package gov.nasa.arc.planworks.db;

/**
 * Describe interface <code>PwObjectContainer</code> here.
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
