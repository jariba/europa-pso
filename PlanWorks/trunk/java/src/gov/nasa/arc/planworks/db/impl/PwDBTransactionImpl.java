// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDBTransactionImpl.java,v 1.7 2004-06-21 22:42:58 taylor Exp $
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

  private String name;
  private Integer transactionId;
  private String source;
  private String [] info;
  private Integer entityId;
  private Integer stepNumber;
  private Long sequenceId;
  private Long partialPlanId;

  public PwDBTransactionImpl( final String name, final Integer transactionId,
                              final String source, final Integer entityId,
                              final Integer stepNumber, final Long sequenceId,
                              final Long partialPlanId) {
    this.name = name;
    this.transactionId = transactionId;
    this.source = source;
    this.entityId = entityId;
    this.stepNumber = stepNumber;
    this.sequenceId = sequenceId;
    this.partialPlanId = partialPlanId;
    info = new String [] {"", "", ""};
  } // end constructor


  /**
   * <code>equals</code>
   *
   * @param transaction - <code>PwDBTransaction</code> - 
   * @return - <code>boolean</code> - 
   */
  public boolean equals( PwDBTransaction transaction) {
    return transactionId.equals( transaction.getId());
  }

  /**
   * <code>getName</code>
   *
   * @return - <code>String</code> - 
   */
  public String getName() {
    return name;
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
   * <code>getEntityId</code> - id of entity acted on by this transaction
   *
   * @return - <code>Integer</code> - 
   */
  public Integer getEntityId() {
    return entityId;
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
   * <code>getSequenceId</code> - id of sequence of entity acted on by this transaction
   *
   * @return - <code>Long</code> - 
   */
  public Long getSequenceId() {
    return sequenceId;
  }

  /**
   * <code>getPartialPlanId</code>
   *
   * @return - <code>Long</code> - id of partial plan of entity acted on by this transaction
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
    StringBuffer retval = new StringBuffer(name); retval.append("\t");
    retval.append(getTransactionType(name)).append("\t");
    retval.append(entityId).append("\t").append(source).append("\t");
    retval.append(transactionId).append("\t").append(stepNumber).append("\t").append(sequenceId);
    retval.append("\t").append(partialPlanId).append("\t");
    if (! getTransactionType(name).equals( DbConstants.TT_ASSIGNMENT)) {
      for(int i = 0; i < info.length; i++) {
        retval.append(info[i]).append(",");
      }
    } else {
      retval.append("\\N");
    }
    retval.append("\n");
    return retval.toString();
  }

  private String getTransactionType( String name) {
    String transactionType = null;
    if (findElementInStringArray( DbConstants.TT_CREATION_NAMES, name) != null) {
      transactionType = DbConstants.TT_CREATION;
    } else if (findElementInStringArray( DbConstants.TT_DELETION_NAMES, name) != null) {
      transactionType = DbConstants.TT_DELETION;
    } else if (findElementInStringArray( DbConstants.TT_ADDITION_NAMES, name) != null) {
      transactionType = DbConstants.TT_ADDITION;
    } else if (findElementInStringArray( DbConstants.TT_REMOVAL_NAMES, name) != null) {
      transactionType = DbConstants.TT_REMOVAL;
    } else if (findElementInStringArray( DbConstants.TT_CLOSURE_NAMES, name) != null) {
      transactionType = DbConstants.TT_CLOSURE;
    } else if (findElementInStringArray( DbConstants.TT_RESTRICTION_NAMES, name) != null) {
      transactionType = DbConstants.TT_RESTRICTION;
    } else if (findElementInStringArray( DbConstants.TT_RELAXATION_NAMES, name) != null) {
      transactionType = DbConstants.TT_RELAXATION;
    } else if (findElementInStringArray( DbConstants.TT_EXECUTION_NAMES, name) != null) {
      transactionType = DbConstants.TT_EXECUTION;
    } else if (findElementInStringArray( DbConstants.TT_SPECIFICATION_NAMES, name) != null) {
      transactionType = DbConstants.TT_SPECIFICATION;
    } else if (findElementInStringArray( DbConstants.TT_UNDO_NAMES, name) != null) {
      transactionType = DbConstants.TT_UNDO;
    } else if (findElementInStringArray( DbConstants.TT_ASSIGNMENT_NAMES, name) != null) {
      transactionType = DbConstants.TT_ASSIGNMENT;
    }
    if (transactionType == null) {
      System.err.println( "PwDBTransactionImpl.toOutputString.getTransactionType" +
                          " returns null for transaction name " + name);
    }
    return transactionType;
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
