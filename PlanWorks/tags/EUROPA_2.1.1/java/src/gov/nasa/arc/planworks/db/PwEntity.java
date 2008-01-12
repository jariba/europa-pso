// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PwEntity.java,v 1.5 2004-09-30 22:02:31 miatauro Exp $
//
// PlanWorks
//
package gov.nasa.arc.planworks.db;

import java.util.List;
import java.util.Set;

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

  //get all neighbors
  public List getNeighbors();
  //get all neighbors that are members of particular classes
  public List getNeighbors(List classes);
  //get all neighbors that are members of particular classes and which are in
  //a set of ids
  public List getNeighbors(List classes, Set ids);

  public List getNeighbors(List classes, List linkTypes);
}
