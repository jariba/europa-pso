// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwDecision.java,v 1.2 2004-05-28 20:21:15 taylor Exp $
//
package gov.nasa.arc.planworks.db;

import java.util.List;

public interface PwDecision extends PwEntity {
  public int getType();
  public Integer getEntityId();
  public boolean isUnit();
  public List getChoices();
  public String toString();

}
