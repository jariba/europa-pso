// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlotImpl.java,v 1.28 2004-03-17 23:34:20 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwToken;


/**
 * <code>PwSlotImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwSlotImpl implements PwSlot {

  private boolean hasSetTimes;
  private Integer id;
  private Integer timelineId;
  private List tokenIdList; // element String
  private PwDomain startTime;
  private PwDomain endTime;
  private PwPartialPlanImpl partialPlan;
  private Integer baseTokenId;

  /**
   * <code>PwSlotImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwSlotImpl( final Integer id, final Integer timelineId, 
                     final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.timelineId = timelineId;
    this.partialPlan = partialPlan;
    startTime = endTime = null;
    hasSetTimes = false;
    tokenIdList = new ArrayList();
    baseTokenId = null;
  } // end constructor


  /**
   * <code>getId</code>
   *
   * @return name - <code>Integer</code> -
   */
  public Integer getId() {
    return id;
  }
	
  /**
   * <code>getTimelineId</code>
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getTimelineId() {
    return timelineId;
  }

  /**
   * <code>getTokenList</code>
   *
   * @return - <code>List</code> - of PwToken
   */
  public List getTokenList() {
    return partialPlan.getTokenList(tokenIdList);
  }

  /**
   * <code>addToken</code>
   *
   * @param token - <code>PwTokenImpl</code> - 
   * @return - <code>PwTokenImpl</code> - 
   */
  public PwTokenImpl addToken(final PwTokenImpl token) {
    if(!tokenIdList.contains(token.getId())) {
      tokenIdList.add(token.getId());
    }
    partialPlan.addToken(token.getId(), token);
    return token;
  }

  public boolean isEmpty() {
    return tokenIdList.isEmpty();
  }

  public void calcTimes(PwSlot prev, PwSlot next) {
    if(hasSetTimes) {
      return;
    }
    if(isEmpty()) {
      if(prev == null || next == null) {
        return;
      }
      startTime = prev.getBaseToken().getEndVariable().getDomain();
      endTime = next.getBaseToken().getStartVariable().getDomain();
    }
    else {
      PwToken base = getBaseToken();
      startTime = base.getStartVariable().getDomain();
      endTime = base.getEndVariable().getDomain();
    }
  }
 
  public PwDomain getStartTime() {return startTime;}
  public PwDomain getEndTime() {return endTime;}

  /**
   * <code>getBaseToken</code>
   *
   * @return - <code>PwToken</code> - 
   */
  public PwToken getBaseToken() {
    if(baseTokenId == null) {
      ListIterator tokenIdIterator = tokenIdList.listIterator();
      // int slotIndex = 99;
      while(tokenIdIterator.hasNext()) {
        Integer tokId = (Integer) tokenIdIterator.next();
//         System.err.println( "slot id " + id + " tokId " + tokId.toString() +
//                             " tokSlotIndex " +
//                             ((PwTokenImpl)partialPlan.getToken(tokId)).getSlotIndex());
        // if(((PwTokenImpl)partialPlan.getToken(tokId)).getSlotIndex() < slotIndex) {
        if (((PwTokenImpl) partialPlan.getToken(tokId)).getSlotIndex() == 0) {
          baseTokenId = tokId;
          break;
        }
      }
    }
//     System.err.println( "slot id " + id + " baseTokenId " + baseTokenId);
    return partialPlan.getToken(baseTokenId);
  }
} // end class PwSlotImpl
