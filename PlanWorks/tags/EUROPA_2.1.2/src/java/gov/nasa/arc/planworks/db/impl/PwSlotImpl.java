// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwSlotImpl.java,v 1.33 2004-09-30 22:03:03 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.viz.ViewConstants;


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

  public String toOutputString() {
    return null;
  }

//   public List getNeighbors(){return null;}
//   public List getNeighbors(List classes){return null;}
//   public List getNeighbors(List classes, Set ids){return null;}


  public List getNeighbors() {
    List classes = new LinkedList();
    classes.add(PwTimeline.class);
    classes.add(PwToken.class);
    return getNeighbors(classes);
  }

  public List getNeighbors(List classes) {
    List retval = new LinkedList();
    for(Iterator classIt = classes.iterator(); classIt.hasNext();) {
      Class cclass = (Class) classIt.next();
      if(PwToken.class.isAssignableFrom( cclass)) {
        retval.addAll( getTokenList());
      } else if (PwTimeline.class.isAssignableFrom( cclass)) {
	retval.add( partialPlan.getTimeline( timelineId));
      }
    }
    return retval;
  }

  public List getNeighbors(List classes, List linkTypes) {
    List retval = new LinkedList();
    for(Iterator it = linkTypes.iterator(); it.hasNext();) {
      String linkType = (String) it.next();
      if(linkType.equals(ViewConstants.SLOT_TO_TOKEN_LINK_TYPE) &&
         CollectionUtils.findFirst(new AssignableFunctor(PwToken.class), classes) != null) {
        retval.addAll(getTokenList());
      }
      else if(linkType.equals(ViewConstants.TIMELINE_TO_SLOT_LINK_TYPE) &&
              CollectionUtils.findFirst(new AssignableFunctor(PwTimeline.class), classes) != null) {
        retval.add(partialPlan.getTimeline(timelineId));
      }
    }
    return retval;
  }

  public List getNeighbors(List classes, Set ids) {
    return PwEntityImpl.getNeighbors(this, classes, ids);
  }



} // end class PwSlotImpl
