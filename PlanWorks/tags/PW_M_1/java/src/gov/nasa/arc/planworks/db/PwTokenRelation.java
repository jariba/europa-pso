// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenRelation.java,v 1.2 2003-05-18 00:02:25 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 16May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;


/**
 * <code>PwTokenRelation</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwTokenRelation {

  /**
   * <code>getKey</code>
   *
   * @return name - <code>String</code> -
   */
  public String getKey();
	
  /**
   * <code>getMasterToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getMasterToken();
 
  /**
   * <code>getSlaveToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getSlaveToken();


} // end interface PwTokenRelation
