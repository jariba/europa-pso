// $Id: PwProjectImpl.java,v 1.1 2003-05-10 01:00:32 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;

/**
 * <code>PwProjectImpl</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
class PwProjectImpl extends PwProject {


  private String url;
  private List planningSeqNames; // element String
  private List planningSeqObjects; // element PwPlanningSequence


  public PwProjectImpl( String url) {
    this.url = url;
    planningSeqNames = new ArrayList();
    planningSeqObjects = new ArrayList();

    if (url.indexOf( "/xml/test") >= 0) {
      // TESTING -- assumes constructor called with this url value
      // url = System.getProperty( "planworks.root") + "/xml/test";
      planningSeqNames.add( "monkey");
      List transactionList = new ArrayList();
      transactionList.add( new ArrayList());
      planningSeqObjects.add( new PwPlanningSequenceImpl( url + "/monkey",
                                                          new PwPartialPlanImpl(),
                                                          new PwModelImpl(),
                                                          transactionList));

      // END TESTING
    }


  } // end  constructor

  /**
   * <code>getUrl</code> - project pathname for planning sequences. e.g.
   *                       PlanWorks/xml/test
   *
   * @return - <code>String</code> - 
   */
  public String getUrl() {
    return url;
  } // end getUrl

  /**
   * <code>listPlanningSequences</code>
   *
   * @return - <code>List</code> -  List of String (name of sequence)
   *                                each sequence is set of partial plans
   *                                e.g. monkey (PlanWorks/xml/test/monkey)
   */
  public List listPlanningSequences() {
    return planningSeqNames;
  } // end listPlanningSequences

  /**
   * <code>getPlanningSequence</code>
   *
   * @param sequenceName - <code>String</code> - 
   * @return - <code>PwPlanningSequence</code> - 
   */
  public PwPlanningSequence getPlanningSequence( String sequenceName)
    throws ResourceNotFoundException {
    for (int i = 0, n = planningSeqNames.size(); i < n; i++) {
      String seqName = (String) planningSeqNames.get( i);
      if (seqName.equals( sequenceName)) {
        return (PwPlanningSequence) planningSeqObjects.get( i);
      }
    }
    throw new ResourceNotFoundException( "getPlanningSequence could not find " +
                                         sequenceName);
  } // end getPlanningSequence

  /**
   * <code>close</code>
   *
   * @exception Exception if an error occurs
   */
  public void close() throws Exception {
  } // end close

  /**
   * <code>requiresSaving</code>
   *
   * @return - <code>boolean</code> - 
   */
  public boolean requiresSaving() {
    return true;
  } // end requiresSaving

  /**
   * <code>save</code>
   *
   * @exception Exception if an error occurs
   */
  public void save() throws Exception {
  } // end save



} // end class PwProjectImpl
