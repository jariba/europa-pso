// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlotImpl.java,v 1.1 2003-05-15 22:16:23 taylor Exp $
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
  private List tokenList; // element PwTokenImpl


  /**
   * <code>Slot</code> - constructor 
   *
   * @param key - <code>String</code> - 
   */
  public PwSlotImpl( String key) {
    this.key = key;
    tokenList = new ArrayList();

  } // end constructor


  /**
   * <code>addToken</code>
   *
   * @param attributeList - <code>List</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   * @param collectionName - <code>String</code> - 
   * @return - <code>PwTokenImpl</code> - 
   */
  public PwTokenImpl addToken( List attributeList, PwPartialPlanImpl partialPlan,
                               String collectionName) {
    PwTokenImpl token = new PwTokenImpl( attributeList, partialPlan, collectionName);
    tokenList.add( token);
    return token;
  } // end addToken


} // end class PwSlotImpl
