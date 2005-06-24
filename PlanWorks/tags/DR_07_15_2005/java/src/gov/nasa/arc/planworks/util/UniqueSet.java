//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: UniqueSet.java,v 1.4 2004-02-03 19:24:00 miatauro Exp $
//
package gov.nasa.arc.planworks.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A class that provides List semantics while guarenteeing that all objects contained are unique 
 * as determined by their equals() methods.
 */

public class UniqueSet extends ArrayList {
  public UniqueSet() {
    super();
  }
  public UniqueSet(final Collection c) {
    super();
    this.addAll(c);
  }
  public UniqueSet(final int initialCapacity) {
    super(initialCapacity);
  }
  public void add(final int index, final Object element) {
    if(!super.contains(element)) {
      super.add(index, element);
    }
  }
  public boolean add(final Object o) {
    if(!super.contains(o)) {
      return super.add(o);
    }
    return false;
  }
  public boolean addAll(final Collection c) {
    ArrayList temp = new ArrayList(c);
    Iterator iterator = c.iterator();
    boolean retval = false;
    while(iterator.hasNext()) {
      if(super.contains(iterator.next())) {
        iterator.remove();
      }
    }
    return super.addAll(c);
  }
  public boolean addAll(final int index, final Collection c) {
    ArrayList temp = new ArrayList(c);
    Iterator iterator = temp.iterator();
    while(iterator.hasNext()) {
      if(super.contains(iterator.next())) {
        iterator.remove();
      }
    }
    return super.addAll(index, temp);
  }
  public Object set(final int index, final Object element) {
    if(super.contains(element)) {
      return null;
    }
    return super.set(index, element);
  }
}
