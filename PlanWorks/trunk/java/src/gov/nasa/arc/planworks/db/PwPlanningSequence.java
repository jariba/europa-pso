// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequence.java,v 1.28 2003-12-20 01:54:47 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;


/**
 * <code>PwPlanningSequence</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PwPlanningSequence extends ViewableObject {


  /**
   * <code>getStepCount</code>
   *
   * @return - <code>int</code> - 
   */
  public abstract int getStepCount();

  /**
   * <code>getUrl</code>
   *
   * @return - <code>String</code> - 
   */
  public abstract String getUrl();

  public abstract Long getId();

  /**
   * <code>getModel</code> - lazy construction of model done here
   *
   * @return - <code>PwModel</code> - 
   */
  public abstract PwModel getModel();

  /**
   * <code>listTransactions</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>List</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public abstract List getTransactionsList( int step) throws IndexOutOfBoundsException;

  /**
   * <code>getTransactionsList</code>
   *
   * @param ppId - <code>Long</code> - 
   * @return - <code>List</code> - 
   */
  public abstract List getTransactionsList(Long ppId);

  /**
   * <code>listPartialPlans</code>
   *
   * @return - <code>List</code> of PwPartialPlan
   */

  public abstract List getPartialPlansList();

  /**
   * <code>listPartialPlans</code>
   *
   * @return - <code>List</code> of String
   */
  public abstract List getPartialPlanNamesList();

  /**
   * <code>getPartialPlan</code>
   *
   * @param step - <code>int</code> - 
   * @return - <code>PartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( int step)
    throws IndexOutOfBoundsException, ResourceNotFoundException;

  /**
   * <code>getPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( String planName)
    throws ResourceNotFoundException;

  /**
   * <code>getPartialPlanIfLoaded</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlanIfLoaded( String planName)
    throws ResourceNotFoundException;

  public abstract PwPartialPlan getNextPartialPlan(int step) throws ResourceNotFoundException, 
  IndexOutOfBoundsException;
  public abstract PwPartialPlan getNextPartialPlan(String planName) 
    throws ResourceNotFoundException, IndexOutOfBoundsException;
  
  public abstract PwPartialPlan getPrevPartialPlan(int step) throws ResourceNotFoundException,
  IndexOutOfBoundsException;
  public abstract PwPartialPlan getPrevPartialPlan(String planName)
    throws ResourceNotFoundException, IndexOutOfBoundsException;
  
  /**
   * <code>delete</code>
   *
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract void delete() throws ResourceNotFoundException;

  // extend ViewableObject

  /**
   * <code>setContentSpec</code>
   *
   * @param spec - <code>List</code> - 
   */
  public abstract void setContentSpec( List spec);

  /**
   * <code>getContentSpec</code>
   *
   * @return - <code>List</code> - 
   */
  public abstract List getContentSpec();

  /**
   * <code>setName</code> - sequenceDir
   *
   * @param name - <code>String</code> - 
   */
  public void setName( String name);

  /**
   * <code>getName</code> - sequenceDir
   *
   * @return - <code>String</code> 
   */
  public abstract String getName();
  
  // end extend ViewableObject

  /**
   * <code>getTransactionsForConstraint</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>List</code> - 
   */
  public List getTransactionsForConstraint(Integer id);

  /**
   * <code>getTransactionsForToken</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>List</code> - 
   */
  public List getTransactionsForToken(Integer id);
 
  /**
   * <code>getTransactionsForVariable</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>List</code> - 
   */
  public List getTransactionsForVariable(Integer id);
  
  /**
   * <code>getTransactionsInRange</code>
   *
   * @param start - <code>int</code> - 
   * @param end - <code>int</code> - 
   * @return - <code>List</code> - 
   */
  public List getTransactionsInRange(int start, int end);

  /**
   * <code>getTransactionsInRange</code>
   *
   * @param start - <code>Integer</code> - 
   * @param end - <code>Integer</code> - 
   * @return - <code>List</code> - 
   */
  public List getTransactionsInRange(Integer start, Integer end);
  
  /**
   * <code>getStepsWhereTokenTransacted</code>
   *
   * @param id - <code>Integer</code> - 
   * @param type - <code>String</code> - 
   * @return - <code>List</code> - 
   * @exception IllegalArgumentException if an error occurs
   */
  public List getStepsWhereTokenTransacted(Integer id, String type) throws IllegalArgumentException;

  /**
   * <code>getStepsWhereVariableTransacted</code>
   *
   * @param id - <code>Integer</code> - 
   * @param type - <code>String</code> - 
   * @return - <code>List</code> - 
   * @exception IllegalArgumentException if an error occurs
   */
  public List getStepsWhereVariableTransacted(Integer id, String type) throws IllegalArgumentException;

  /**
   * <code>getStepsWhereConstraintTransacted</code>
   *
   * @param id - <code>Integer</code> - 
   * @param type - <code>String</code> - 
   * @return - <code>List</code> - 
   * @exception IllegalArgumentException if an error occurs
   */
  public List getStepsWhereConstraintTransacted(Integer id, String type) throws IllegalArgumentException;

  /**
   * <code>getStepsWhereTokenTransacted</code>
   *
   * @param type - <code>String</code> - 
   * @return - <code>List</code> - 
   * @exception IllegalArgumentException if an error occurs
   */
  public List getStepsWhereTokenTransacted(String type) throws IllegalArgumentException;

  /**
   * <code>getStepsWhereVariableTransacted</code>
   *
   * @param type - <code>String</code> - 
   * @return - <code>List</code> - 
   * @exception IllegalArgumentException if an error occurs
   */
  public List getStepsWhereVariableTransacted(String type) throws IllegalArgumentException;

  /**
   * <code>getStepsWhereConstraintTransacted</code>
   *
   * @param type - <code>String</code> - 
   * @return - <code>List</code> - 
   * @exception IllegalArgumentException if an error occurs
   */
  public List getStepsWhereConstraintTransacted(String type) throws IllegalArgumentException;

  /**
   * <code>getStepsWithRestrictions</code>
   *
   * @return - <code>List</code> - 
   */
  public List getStepsWithRestrictions();

  /**
   * <code>getStepsWithRelaxations</code>
   *
   * @return - <code>List</code> - 
   */
  public List getStepsWithRelaxations();

  /**
   * <code>getStepsWithUnitVariableBindingDecisions</code>
   *
   * @return - <code>List</code> - 
   */
  public List getStepsWithUnitVariableBindingDecisions();

  /**
   * <code>getStepsWithNonUnitVariableBindingDecisions</code>
   *
   * @return - <code>List</code> - 
   */
  public List getStepsWithNonUnitVariableBindingDecisions();

  /**
   * <code>getFreeTokensAtStep</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFreeTokensAtStep( int stepNum);

  /**
   * <code>getUnboundVariablesAtStep</code>
   *
   * @return - <code>List</code> - 
   */
  public List getUnboundVariablesAtStep(int stepNum);

  /**
   * <code>getPlanDBSize</code>
   *
   * @param stepNum - <code>int</code> - 
   * @return - <code>int[]</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   */
  public int [] getPlanDBSize(int stepNum) throws IndexOutOfBoundsException;
  
  /**
   * <code>getPlanDBSizeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getPlanDBSizeList();

  public void refresh();

} // end interface PwPlanningSequence
