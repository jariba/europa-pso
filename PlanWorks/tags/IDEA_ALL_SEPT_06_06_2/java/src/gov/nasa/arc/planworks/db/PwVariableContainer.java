// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: PwVariableContainer.java,v 1.3 2004-02-25 02:30:13 taylor Exp $
//
// PlanWorks
//
package gov.nasa.arc.planworks.db;

import java.util.List;

public interface PwVariableContainer extends PwEntity {
  public Integer getId();
  public List getVariables();
  public String getName();
}
