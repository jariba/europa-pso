//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: IdSource.java,v 1.2 2004-06-14 22:11:23 taylor Exp $
//
package gov.nasa.arc.planworks.test;

public interface IdSource {
  public void resetEntityIdInt();
  public int incEntityIdInt();
}
