// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ContentSpec.java,v 1.18 2004-05-04 01:27:13 taylor Exp $
//
// PlanWorks
//

package gov.nasa.arc.planworks.db.util;

import java.util.List;

public interface ContentSpec {

  public List getValidIds();
  public List getCurrentSpec();
  public void resetSpec();
  public void applySpec(final List spec);
}
