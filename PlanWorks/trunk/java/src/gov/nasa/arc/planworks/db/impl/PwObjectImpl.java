// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwObjectImpl.java,v 1.23 2004-03-12 23:19:50 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 14May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;


/**
 * <code>PwObjectImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwObjectImpl implements PwObject {
  private int type;
  protected Integer id;
  private Integer parentId;
  private String name;
  //private String emptySlotInfo;
  private List componentIdList; // element Integer
  private List variableIdList;
  private List tokenIdList;
  protected PwPartialPlanImpl partialPlan;
  //  private boolean haveCreatedSlots;
  //   private boolean haveCalculatedSlotTimes;

  /**
   * <code>PwObjectImpl</code> - constructor 
   *
   * @param id - <code>Integer</code> - 
   * @param name - <code>String</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   */
  public PwObjectImpl( final Integer id, final int objectType, final Integer parentId,
                       final String name, final String componentIds, final String variableIds,
                       final String tokenIds, final PwPartialPlanImpl partialPlan) {
    this.id = id;
    this.name = name;
    this.partialPlan = partialPlan;
    this.parentId = parentId;
    //this.emptySlotInfo = info;
    type = objectType;
//     haveCreatedSlots = false;
//     haveCalculatedSlotTimes = false;
    componentIdList = new ArrayList();
    if(componentIds != null) {
      StringTokenizer strTok = new StringTokenizer(componentIds, ",");
      while(strTok.hasMoreTokens()) {
        componentIdList.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    variableIdList = new ArrayList();
    if(variableIds != null) {
      StringTokenizer strTok = new StringTokenizer(variableIds, ",");
      while(strTok.hasMoreTokens()) {
        variableIdList.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    tokenIdList = new ArrayList();
    if(tokenIds != null) {
      StringTokenizer strTok = new StringTokenizer(tokenIds, ",");
      while(strTok.hasMoreTokens()) {
        tokenIdList.add(Integer.valueOf(strTok.nextToken()));
      }
    }
  } // end constructor

  /**
   * <code>getId</code>
   *
   * @return id - <code>Integer</code> -
   */
  public Integer getId() {
    return this.id;
  }

  /**
   * <code>getName</code>
   *
   * @return name - <code>String</code> -
   */
  public String getName() {
    return this.name;
  }

  public PwObject getParent() {
    return partialPlan.getObject(parentId);
  }

  public Integer getParentId() {
    return parentId;
  }

  public List getComponentList() {
    return partialPlan.getObjectList(componentIdList);
  }

  public int getObjectType(){return type;}

  public List getVariables() {
    return partialPlan.getVariableList(variableIdList);
  }

  public List getTokens() {
    return partialPlan.getTokenList(tokenIdList);
  }

  public void addToken(Integer tokenId) {
    if(!tokenIdList.contains(tokenId)) {
      tokenIdList.add(tokenId);
    }
  }

  public String toString() {
    return id.toString();
  }

} // end class PwObjectImpl
