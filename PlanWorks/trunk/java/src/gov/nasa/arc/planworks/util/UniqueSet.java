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
  public UniqueSet(Collection c) {
    super();
    this.addAll(c);
  }
  public UniqueSet(int initialCapacity) {
    super(initialCapacity);
  }
  public void add(int index, Object element) {
    if(!super.contains(element)) {
      super.add(index, element);
    }
  }
  public boolean add(Object o) {
    if(!super.contains(o)) {
      return super.add(o);
    }
    return false;
  }
  public boolean addAll(Collection c) {
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
  public boolean addAll(int index, Collection c) {
    ArrayList temp = new ArrayList(c);
    Iterator iterator = temp.iterator();
    while(iterator.hasNext()) {
      if(super.contains(iterator.next())) {
        iterator.remove();
      }
    }
    return super.addAll(index, temp);
  }
  public Object set(int index, Object element) {
    if(super.contains(element)) {
      return null;
    }
    return super.set(index, element);
  }
}
