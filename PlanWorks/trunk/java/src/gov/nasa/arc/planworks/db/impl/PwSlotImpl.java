// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlotImpl.java,v 1.8 2003-06-12 23:49:46 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwSlot;


/**
 * <code>PwSlotImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwSlotImpl implements PwSlot {

  private String key;
  private List tokenIdList; // element String
  private PwPartialPlanImpl partialPlan;

  /**
   * <code>PwSlotImpl</code> - constructor 
   *
   * @param key - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwSlotImpl( String key, PwPartialPlanImpl partialPlan) {
    this.key = key;
    this.partialPlan = partialPlan;
    tokenIdList = new ArrayList();
  } // end constructor


  /**
   * <code>getKey</code>
   *
   * @return name - <code>String</code> -
   */
  public String getKey() {
    return key;
  }
	
  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public List getTokenList() {
    List retval = new ArrayList( tokenIdList.size());
    for (int i = 0; i < tokenIdList.size(); i++)
      retval.add( partialPlan.getToken( (String)tokenIdList.get( i)));
    return retval;
  }

  /**
   * <code>addToken</code>
   *
   * @param attributeList - <code>List</code> - 
   * @return - <code>PwTokenImpl</code> - 
   */
  public PwTokenImpl addToken( List attributeList) {
    String tokenKey = (String) attributeList.get( 0);
    PwTokenImpl token = new PwTokenImpl( attributeList, partialPlan);
    tokenIdList.add( tokenKey);
    partialPlan.addToken( tokenKey, token);
    return token;
  } // end addToken

} // end class PwSlotImpl
