// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPlanningSequence.java,v 1.40 2005-11-10 01:22:08 miatauro Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db;

import java.util.List;

import gov.nasa.arc.planworks.util.CreatePartialPlanException;
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

  public static final String EVT_PP_ADDED = "PartialPlan Added";

  public static final String EVT_PP_REMOVED = "PartialPlan Removed";

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
   * <code>doesPartialPlanExist</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>boolean</code> - 
   */
  public abstract boolean doesPartialPlanExist( String planName);

  /**
   * <code>hasLoadedTransactionFile</code>
   *
   * @return - <code>boolean</code> - 
   */
//   public boolean hasLoadedTransactionFile();

//   /**
//    * <code>hasLoadedTransactionFile</code>
//    *
//    * @param partialPlanId - <code>Long</code> - 
//    * @return - <code>boolean</code> - 
//    */
//   public boolean hasLoadedTransactionFile( Long partialPlanId);

//   /**
//    * <code>hasLoadedTransactionFile</code>
//    *
//    * @param partialPlanName - <code>String</code> - 
//    * @return - <code>boolean</code> - 
//    */
//   public boolean hasLoadedTransactionFile( String partialPlanName);

//   /**
//    * <code>isTransactionFileOnDisk</code>
//    *
//    * @return - <code>boolean</code> - 
//    */
//   public boolean isTransactionFileOnDisk();

//   /**
//    * <code>listTransactions</code>
//    *
//    * @param step - <code>int</code> - 
//    * @return - <code>List</code> - 
//    * @exception IndexOutOfBoundsException if an error occurs
//    */
//   public abstract List getTransactionsList( final int step) throws IndexOutOfBoundsException;

//   /**
//    * <code>getTransactionsList</code>
//    *
//    * @param ppId - <code>Long</code> - 
//    * @return - <code>List</code> - 
//    */
//   public abstract List getTransactionsList(final Long ppId);

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
   * @return - <code>PwPartialPlan</code> - 
   * @exception IndexOutOfBoundsException if an error occurs
   * @exception ResourceNotFoundException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( final int step)
    throws IndexOutOfBoundsException, ResourceNotFoundException, CreatePartialPlanException;

  /**
   * <code>getPartialPlan</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan( final String planName)
    throws ResourceNotFoundException, CreatePartialPlanException;

  /**
   * <code>getPartialPlan</code>
   *
   * @param ppid - <code>Long</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   * @exception CreatePartialPlanException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlan(final Long ppid)
    throws ResourceNotFoundException, CreatePartialPlanException;

  /**
   * <code>getPartialPlanIfLoaded</code>
   *
   * @param planName - <code>String</code> - 
   * @return - <code>PwPartialPlan</code> - 
   * @exception ResourceNotFoundException if an error occurs
   */
  public abstract PwPartialPlan getPartialPlanIfLoaded( final String planName)
    throws ResourceNotFoundException;

  public abstract PwPartialPlan getNextPartialPlan(final int step) 
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException;

  public abstract PwPartialPlan getNextPartialPlan(final String planName) 
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException;
  
  public abstract PwPartialPlan getPrevPartialPlan(final int step) 
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException;

  public abstract PwPartialPlan getPrevPartialPlan(final String planName)
    throws ResourceNotFoundException, IndexOutOfBoundsException, CreatePartialPlanException;
  
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
  public abstract void setContentSpec( final List spec);

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
  public void setName( final String name);

  /**
   * <code>getName</code> - sequenceDir
   *
   * @return - <code>String</code> 
   */
  public abstract String getName();
  
  // end extend ViewableObject

  public List getOpenDecisionsForStep(final int stepnum)
    throws ResourceNotFoundException, CreatePartialPlanException;

//   public Integer getCurrentDecisionIdForStep(final int stepNum) throws ResourceNotFoundException;

    /**
   * <code>getTransactionsForConstraint</code>
   *
   * @param id - <code>Integer</code> - 
   * @return - <code>List</code> - 
   */
//   public List getTransactionsForConstraint(final Integer id);

//   /**
//    * <code>getTransactionsForToken</code>
//    *
//    * @param id - <code>Integer</code> - 
//    * @return - <code>List</code> - 
//    */
//   public List getTransactionsForToken(final Integer id);
 
//   /**
//    * <code>getTransactionsForVariable</code>
//    *
//    * @param id - <code>Integer</code> - 
//    * @return - <code>List</code> - 
//    */
//   public List getTransactionsForVariable(final Integer id);
  
//   /**
//    * <code>getTransactionsInRange</code>
//    *
//    * @param start - <code>int</code> - 
//    * @param end - <code>int</code> - 
//    * @return - <code>List</code> - 
//    */
//   public List getTransactionsInRange(final int istart, final int iend);

//   /**
//    * <code>getTransactionsInRange</code>
//    *
//    * @param start - <code>Integer</code> - 
//    * @param end - <code>Integer</code> - 
//    * @return - <code>List</code> - 
//    */
//   public List getTransactionsInRange(final Integer start, final Integer end);
  
//   /**
//    * <code>getStepsWhereTokenTransacted</code>
//    *
//    * @param id - <code>Integer</code> - 
//    * @param type - <code>String</code> - 
//    * @return - <code>List</code> - 
//    * @exception IllegalArgumentException if an error occurs
//    */
//   public List getStepsWhereTokenTransacted(final Integer id, final String type) 
//     throws IllegalArgumentException;

//   /**
//    * <code>getStepsWhereVariableTransacted</code>
//    *
//    * @param id - <code>Integer</code> - 
//    * @param type - <code>String</code> - 
//    * @return - <code>List</code> - 
//    * @exception IllegalArgumentException if an error occurs
//    */
//   public List getStepsWhereVariableTransacted(final Integer id, final String type) 
//     throws IllegalArgumentException;

//   /**
//    * <code>getStepsWhereConstraintTransacted</code>
//    *
//    * @param id - <code>Integer</code> - 
//    * @param type - <code>String</code> - 
//    * @return - <code>List</code> - 
//    * @exception IllegalArgumentException if an error occurs
//    */
//   public List getStepsWhereConstraintTransacted(final Integer id, final String type) 
//     throws IllegalArgumentException;

//   /**
//    * <code>getStepsWhereTokenTransacted</code>
//    *
//    * @param type - <code>String</code> - 
//    * @return - <code>List</code> - 
//    * @exception IllegalArgumentException if an error occurs
//    */
//   public List getStepsWhereTokenTransacted(final String type) throws IllegalArgumentException;

//   /**
//    * <code>getStepsWhereVariableTransacted</code>
//    *
//    * @param type - <code>String</code> - 
//    * @return - <code>List</code> - 
//    * @exception IllegalArgumentException if an error occurs
//    */
//   public List getStepsWhereVariableTransacted(final String type) throws IllegalArgumentException;

//   /**
//    * <code>getStepsWhereConstraintTransacted</code>
//    *
//    * @param type - <code>String</code> - 
//    * @return - <code>List</code> - 
//    * @exception IllegalArgumentException if an error occurs
//    */
//   public List getStepsWhereConstraintTransacted(final String type) throws IllegalArgumentException;

//   /**
//    * <code>getStepsWithRestrictions</code>
//    *
//    * @return - <code>List</code> - 
//    */
//   public List getStepsWithRestrictions();

//   /**
//    * <code>getStepsWithRelaxations</code>
//    *
//    * @return - <code>List</code> - 
//    */
//   public List getStepsWithRelaxations();

//   /**
//    * <code>getStepsWithUnitVariableBindingDecisions</code>
//    *
//    * @return - <code>List</code> - 
//    */
//   public List getStepsWithUnitVariableBindingDecisions();

//   /**
//    * <code>getStepsWithNonUnitVariableBindingDecisions</code>
//    *
//    * @return - <code>List</code> - 
//    */
//   public List getStepsWithNonUnitVariableBindingDecisions();

  /**
   * <code>getFreeTokensAtStep</code>
   *
   * @return - <code>List</code> - 
   */
  public List getFreeTokensAtStep( final int stepNum) throws ResourceNotFoundException;

  /**
   * <code>getUnboundVariablesAtStep</code>
   *
   * @return - <code>List</code> - 
   */
  public List getUnboundVariablesAtStep(final int stepNum) throws ResourceNotFoundException;

  public List getPartialPlanNameList();

  /**
   * <code>getPlanDBSize</code>
   *
   * @param stepNum - <code>int</code> - 
   * @return - <code>int[]</code> - 
   */
  public int [] getPlanDBSize(final int stepNum);
  
  /**
   * <code>getPlanDBSizeList</code>
   *
   * @return - <code>List</code> - 
   */
  public List getPlanDBSizeList();

  public void refresh();

  public boolean isPartialPlanLoaded(final int step);
  public boolean isPartialPlanInDb(final int step);
  public boolean isPartialPlanInFilesystem(final int step);

  public void addListener(PwListener l);
  public void removeListener(PwListener l);
  public void handleEvent(String evtName);

  public PwRule getRule(Integer rId);

} // end interface PwPlanningSequence
