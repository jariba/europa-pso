// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDBTransactionImpl.java,v 1.3 2004-05-04 01:27:12 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 09May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDBTransaction;


/**
 * <code>PwDBTransactionImpl</code> - data base transactions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwDBTransactionImpl implements PwDBTransaction {

  private String type;
  private Integer transactionId;
  private String source;
  private String [] info;
  private Integer objectId;
  private Integer stepNumber;
  private Long sequenceId;
  private Long partialPlanId;

  public PwDBTransactionImpl( final String type, final Integer transactionId,
                              final String source, final Integer objectId,
                              final Integer stepNumber, final Long sequenceId,
                              final Long partialPlanId) {
    this.type = type;
    this.transactionId = transactionId;
    this.source = source;
    this.objectId = objectId;
    this.stepNumber = stepNumber;
    this.sequenceId = sequenceId;
    this.partialPlanId = partialPlanId;
    info = new String [] {"", "", ""};
  } // end constructor


  /**
   * <code>getType</code>
   *
   * @return - <code>String</code> - 
   */
  public String getType() {
    return type;
  }

  /**
   * <code>getId</code>
   *
   * @return - <code>Integer</code> - transaction id
   */
  public Integer getId() {
    return transactionId;
  }

  /**
   * <code>getSource</code> - one of DBConstants.SOURCE_USER/SOURCE_SYSTEM/SOURCE_UNKNOWN
   *
   * @return - <code>String</code> - 
   */
  public String getSource() {
    return source;
  }

  /**
   * <code>getObjectId</code> - id of object acted on by this transaction
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getObjectId() {
    return objectId;
  }

  /**
   * <code>getStepNumber</code> - step number of sequence in which transaction occurred
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getStepNumber() {
    return stepNumber;
  }

  /**
   * <code>getSequenceId</code> - id of sequence of object acted on by this transaction
   *
   * @return - <code>Long</code> - 
   */
  public Long getSequenceId() {
    return sequenceId;
  }

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan of object acted on by this transaction
   */
  public Long getPartialPlanId() {
    return partialPlanId;
  }
  public void setInfo(final String [] info) {
    System.arraycopy(info, 0, this.info, 0, info.length);
  }

  public String [] getInfo() {
    return info;
  }
  
  public String toOutputString() {
    StringBuffer retval = new StringBuffer(type); retval.append("\t");
    retval.append(getTypeCategory(type)).append("\t");
    retval.append(objectId).append("\t").append(source).append("\t");
    retval.append(transactionId).append("\t").append(stepNumber).append("\t").append(sequenceId);
    retval.append("\t").append(partialPlanId).append("\t");
    for(int i = 0; i < info.length; i++) {
      retval.append(info[i]).append(",");
    }
    retval.append("\n");
    return retval.toString();
  }

  private String getTypeCategory( String type) {
    String typeCategory = null;
    if (findElementInStringArray( DbConstants.TT_CREATION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_CREATION;
    } else if (findElementInStringArray( DbConstants.TT_DELETION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_DELETION;
    } else if (findElementInStringArray( DbConstants.TT_ADDITION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_ADDITION;
    } else if (findElementInStringArray( DbConstants.TT_REMOVAL_TYPES, type) != null) {
      typeCategory = DbConstants.TT_REMOVAL;
    } else if (findElementInStringArray( DbConstants.TT_CLOSURE_TYPES, type) != null) {
      typeCategory = DbConstants.TT_CLOSURE;
    } else if (findElementInStringArray( DbConstants.TT_RESTRICTION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_RESTRICTION;
    } else if (findElementInStringArray( DbConstants.TT_RELAXATION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_RELAXATION;
    } else if (findElementInStringArray( DbConstants.TT_EXECUTION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_EXECUTION;
    } else if (findElementInStringArray( DbConstants.TT_SPECIFICATION_TYPES, type) != null) {
      typeCategory = DbConstants.TT_SPECIFICATION;
    } else if (findElementInStringArray( DbConstants.TT_UNDO_TYPES, type) != null) {
      typeCategory = DbConstants.TT_UNDO;
    }
    if (typeCategory == null) {
      System.err.println( "PwDBTransactionImpl.toOutputString.getTypeCategory" +
                          " returns null for " + type);
    }
    return typeCategory;
  }

  private String findElementInStringArray( String [] stringArray, String element) {
    for (int i = 0, n = stringArray.length; i < n; i++) {
      if (stringArray[i].equals( element)) {
        return stringArray[i];
      }
    }
    return null;
  }

} // end class PwDBTransactionImpl
