// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlotImpl.java,v 1.14 2003-08-12 22:54:02 miatauro Exp $
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

  private Integer id;
  private List tokenIdList; // element String
  private PwPartialPlanImpl partialPlan;

  /**
   * <code>PwSlotImpl</code> - constructor 
   *
   * @param id - <code>int</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwSlotImpl( Integer id, PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.partialPlan = partialPlan;
    tokenIdList = new ArrayList();
  } // end constructor


  /**
   * <code>getId</code>
   *
   * @return name - <code>int</code> -
   */
  public Integer getId() {
    return id;
  }
	
  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public List getTokenList() {
    List retval = new ArrayList( tokenIdList.size());
    for (int i = 0; i < tokenIdList.size(); i++)
      retval.add( partialPlan.getToken( (Integer)tokenIdList.get( i)));
    return retval;
  }

  /**
   * <code>addToken</code>
   *
   * @param attributeList - <code>List</code> - 
   * @return - <code>PwTokenImpl</code> - 
   */
  public PwTokenImpl addToken(Integer id, boolean isValueToken, Integer slotId, 
                              Integer predicateId, Integer startVarId, Integer endVarId, 
                              Integer durationVarId, Integer objectId, Integer rejectVarId,
                              Integer objectVarId, Integer timelineId, List tokenRelationIds, 
                              List paramVarIds) {
    PwTokenImpl token = new PwTokenImpl(id, isValueToken, slotId, predicateId, startVarId, 
                                        endVarId, durationVarId, objectId, rejectVarId, 
                                        objectVarId, timelineId, tokenRelationIds, paramVarIds, 
                                        partialPlan);
    if(!tokenIdList.contains(id)) {
      tokenIdList.add( id);
    }
    partialPlan.addToken( id, token);
    return token;
  } // end addToken

} // end class PwSlotImpl
