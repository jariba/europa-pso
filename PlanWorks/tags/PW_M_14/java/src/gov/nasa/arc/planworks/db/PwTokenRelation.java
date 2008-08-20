// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenRelation.java,v 1.8 2004-03-23 18:20:05 miatauro Exp $
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
public interface PwTokenRelation extends PwEntity {

  /**
   * <code>getId</code>
   *
   * @return name - <code>int</code> -
   */
  public abstract Integer getId();
	
  /**
   * <code>getTokenAId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getTokenAId();
 
  /**
   * <code>getSlaveTokenId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public abstract Integer getTokenBId();

  /**
   * <code>getTokenA</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public abstract PwToken getTokenA();
 
  /**
   * <code>getTokenB</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public abstract PwToken getTokenB();

  public abstract String getType();

} // end interface PwTokenRelation
