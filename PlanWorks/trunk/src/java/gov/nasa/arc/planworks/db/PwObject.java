// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObject.java,v 1.11 2004-08-14 01:39:09 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 14May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwObject</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwObject extends PwVariableContainer {


  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public abstract Integer getId();

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public abstract String getName();


  /**
   * <code>getComponentList</code> -
   *
   * @return list - <code>List</code> - of PwObject
   */
  public abstract List getComponentList();

  public abstract int getObjectType();
  
  public abstract PwObject getParent();

  public abstract Integer getParentId();

  public abstract List getVariables();

  public abstract List getTokens();

} // end interface PwObject
