// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwTokenImpl.java,v 1.4 2003-05-18 00:02:26 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 15May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwPredicate;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.db.util.XmlDBeXist;


/**
 * <code>PwTokenImpl</code> - Java mapping of XML structure
 *                       /PartialPlan/Object/Timeline/Slot/Token
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwTokenImpl implements PwToken {

  private String key;
  private String isValueToken;
  private String predicateId;
  private String startVarId;
  private String endVarId;
  private String durationVarId;
  private String objectVarId;
  private String rejectVarId;
  private List tokenRelationIds; // element String
  private List paramVarIds; // element String
  private String slotId;
  
  private PwPartialPlanImpl partialPlan;
  private String collectionName;


  /**
   * <code>PwTokenImpl</code> - constructor 
   *
   * @param attributeList - <code>List</code> - 
   * @param partialPlan - <code>PwPartialPlanImpl</code> - 
   * @param collectionName - <code>String</code> - 
   */
  public PwTokenImpl( List attributeList, PwPartialPlanImpl partialPlan,
                      String collectionName) {
    if (attributeList.size() != XmlDBeXist.NUM_TOKEN_ATTRIBUTES) {
      System.err.println( "Token constructor: attribute list does not contain 11 items" +
                          " => " + attributeList);
      try {
        throw new Exception();
      } catch (Exception e) { e.printStackTrace(); }
      System.exit( 0);
    }
    this.collectionName = collectionName;
    this.partialPlan = partialPlan;
    for (int i = 0, n = attributeList.size(); i < n; i++) {
      switch ( i) {
      case 0: this.key = (String) attributeList.get( 0); break;
      case 1: this.isValueToken = (String) attributeList.get( 1); break;
      case 2: this.predicateId = (String) attributeList.get( 2); break;
      case 3: this.startVarId = (String) attributeList.get( 3); break;
      case 4: this.endVarId = (String) attributeList.get( 4); break;
      case 5: this.durationVarId = (String) attributeList.get( 5); break;
      case 6: this.objectVarId = (String) attributeList.get( 6); break;
      case 7: this.rejectVarId = (String) attributeList.get( 7); break;
      case 8:
        this.tokenRelationIds = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer( (String) attributeList.get( 8));
        while (tokenizer.hasMoreTokens()) {
          this.tokenRelationIds.add( tokenizer.nextToken());
        }
        break;
      case 9:
        this.paramVarIds = new ArrayList();
        tokenizer = new StringTokenizer( (String) attributeList.get( 9));
        while (tokenizer.hasMoreTokens()) {
          this.paramVarIds.add( tokenizer.nextToken());
        }
        break;
      case 10: this.slotId = (String) attributeList.get( 10); break;
      }
    }
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
   * <code>getPredicate</code>
   *
   * @return - <code>PwPredicate</code> - 
   */
  public PwPredicate getPredicate() {
    return partialPlan.getPredicate( predicateId, collectionName);
  }

  /**
   * <code>getStartVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getStartVariable() {
    return partialPlan.getVariable( startVarId, collectionName);
  }
		
  /**
   * <code>getEndVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getEndVariable() {
    return partialPlan.getVariable( endVarId, collectionName);
  }

  /**
   * <code>getDurationVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getDurationVariable() {
    return partialPlan.getVariable( durationVarId, collectionName);
  }

  /**
   * <code>getObjectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getObjectVariable() {
    return partialPlan.getVariable( objectVarId, collectionName);
  }

  /**
   * <code>getRejectVariable</code>
   *
   * @return - <code>PwVariable</code> - 
   */
  public PwVariable getRejectVariable() {
    return partialPlan.getVariable( rejectVarId, collectionName);
  }

  /**
   * <code>getTokenRelationsList</code>
   *
   * @return - <code>List</code> - of PwTokenRelation
   */
  public List getTokenRelationsList() {
    ArrayList retval = new ArrayList( tokenRelationIds.size());
    for (int i = 0; i < tokenRelationIds.size(); i++) {
      retval.set( i, partialPlan.getTokenRelation( (String) tokenRelationIds.get( i),
                                                   collectionName));
    }
    return retval;
  }

  /**
   * <code>getParamVarsList</code>
   *
   * @return - <code>List</code> - of PwVariable
   */
  public List getParamVarsList() {
    ArrayList retval = new ArrayList( paramVarIds.size());
    for (int i = 0; i < paramVarIds.size(); i++) {
      retval.set( i, partialPlan.getVariable( (String) paramVarIds.get( i),
                                              collectionName));
    }
    return retval;
  }

  /**
   * <code>getSlotId</code>
   *
   * @return - <code>String</code> - 
   */
  public String getSlotId() {
    return this.slotId;
  }

} // end class PwTokenImpl

