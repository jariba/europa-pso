// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwToken.java,v 1.3 2003-05-21 23:48:35 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db;

import java.awt.FontMetrics;
import java.util.List;


/**
 * <code>PwToken</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwToken {


  /**
   * <code>getKey</code>
   *
   * @return name - <code>String</code> -
   */
  public String getKey();
	
  /**
   * <code>getPredicate</code>
   *
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate();

  /**
   * <code>getStartVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getStartVariable();
		
  /**
   * <code>getEndVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getEndVariable();

  /**
   * <code>getDurationVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getDurationVariable();

  /**
   * <code>getObjectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getObjectVariable();

  /**
   * <code>getRejectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getRejectVariable();

  /**
   * <code>getTokenRelationsList</code>
   *
   * @return - <code>List</code> - of PwTokenRelation
   */
  public List getTokenRelationsList();

  /**
   * <code>getParamVarsList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getParamVarsList();

  /**
   * <code>getSlotId</code>
   *
   * @return - <code>String</code> - 
   */
  public String getSlotId();
 

} // end interface PwToken
