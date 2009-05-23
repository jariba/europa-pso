// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ViewableObject.java,v 1.2 2003-11-06 00:02:20 taylor Exp $

package gov.nasa.arc.planworks.viz.viewMgr;

import java.util.List;

import gov.nasa.arc.planworks.db.util.ContentSpec;

public interface ViewableObject {
  public void setContentSpec(List spec);
  public List getContentSpec();
  public void setName(String name);
  public String getName();
}
