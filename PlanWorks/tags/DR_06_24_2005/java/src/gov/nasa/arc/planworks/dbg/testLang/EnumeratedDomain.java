package gov.nasa.arc.planworks.dbg.testLang;

import java.util.ArrayList;
import java.util.Iterator;

public class EnumeratedDomain extends ArrayList implements Domain {
	public Comparable getItem(int index) {
		return (Comparable) get(index);
	}
	public Comparable getFirst() {
		return (Comparable) get(0);
	}
	public Comparable getLast() {
		return (Comparable) get(size()-1);
	}
	public boolean isSingleton() {
		return size() == 1;
	}
  public String toString() {
    StringBuffer retval = new StringBuffer("{");
    for(Iterator it = iterator(); it.hasNext();) {
      retval.append(it.next().toString());
      if(it.hasNext())
        retval.append(", ");
    }
    retval.append("}");
    return retval.toString();
  }
}
