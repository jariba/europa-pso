// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwChoice.java,v 1.2 2004-05-28 20:21:15 taylor Exp $
//
package gov.nasa.arc.planworks.db;

public interface PwChoice extends PwEntity {
  public int getType();
  public Integer getTokenId();
  public double getValue();
  public PwDomain getDomain();
  public String toString();
}
