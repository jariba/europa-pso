// $Id: Factory.java,v 1.1 2003-05-10 01:00:31 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 08May03
//

package gov.nasa.arc.planworks.db.impl;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.arc.planworks.db.PwProject;


/**
 * <code>Factory</code> - singleton class to return Abstract class instances
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class Factory {

  /**
   * constant <code>INSTANCE</code>
   *
   */
  public static final Factory INSTANCE = new Factory();

  private Factory() {
  } 

  /**
   * <code>getOpenProject</code>
   *
   * @return - <code>Project</code> - 
   */
  public PwProject getOpenProject( String url) {
    if (url.indexOf( "/xml/test") >= 0) {
      // TESTING
      return new PwProjectImpl( url);
    } else {
      System.err.println( "Factory.getOpenProject: find PwProject " +
                          "in appropriate persistent store");
      System.exit( 1);
    }
    return null;
  }

  /**
   * <code>getCreateProject</code>
   *
   * @return - <code>Project</code> - 
   */
  public PwProject getCreateProject( String url) {
    // SAME AS getOpenProject FOR NOW
    return new PwProjectImpl( url);
  }



} // end class Factory
