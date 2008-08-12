//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: CollectionUtils.java,v 1.4 2004-08-26 23:03:23 miatauro Exp $
//
package gov.nasa.arc.planworks.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionUtils {
  private static CollectionUtils utils;
  private CollectionUtils(){}
  static {
    CollectionUtils.utils = new CollectionUtils();
  }
  public static final Object [] aMap(final UnaryFunctor f, final Object [] a) {
    Object [] retval = new Object [a.length];
    for(int i = 0; i < a.length; i++) {
      retval[i] = f.func(a[i]);
    }
    return retval;
  }
  public static final Collection cMap(final UnaryFunctor f, final Collection a) {
    Collection retval = null;
    try {retval = (Collection) a.getClass().newInstance();}
    catch(Exception e){return null;}
    for(Iterator i = a.iterator(); i.hasNext();) {
      retval.add(f.func(i.next()));
    }
    return retval;
  }
  public static final List lMap(final UnaryFunctor f, final List a) {
    return (List) cMap(f,a);
  }
  public static final Set sMap(final UnaryFunctor f, final Set a) {
    return (Set) cMap(f,a);
  }
  public static final void aInPlaceMap(final UnaryFunctor f, Object [] a) {
    for(int i = 0; i < a.length; i++) {
      a[i] = f.func(a[i]);
    }
  }

  //note: all of the collection in-place map functions require that the functor
  //actually modify the element, rather than this method
  public static final void cInPlaceMap(final UnaryFunctor f, Collection c) {
    for(Iterator i = c.iterator(); i.hasNext();) {
      f.func(i.next());
    }
  }
  public static final void lInPlaceMap(final UnaryFunctor f, List a) {
    cInPlaceMap(f, a);
  }
  public static final void sInPlaceMap(final UnaryFunctor f, Set a) {
    cInPlaceMap(f, a);
  }
  public static final Object [] aGrep(final BooleanFunctor f, final Object [] a) {
    ArrayList retval = new ArrayList();
    for(int i  = 0; i < a.length; i++) {
      if(f.func(a[i])) {
        retval.add(a[i]);
      }
    }
    return retval.toArray();
  }
  public static final Collection cGrep(final BooleanFunctor f, final Collection a) {
    Collection retval = null;
    try {retval = (Collection) a.getClass().newInstance();}
    catch(Exception e) {e.printStackTrace();return null;}
    for(Iterator i = a.iterator(); i.hasNext();) {
      Object temp = i.next();
      if(f.func(temp)) {
        retval.add(temp);
      }
    }
    return retval;
  }
  public static final List lGrep(final BooleanFunctor f, final List a) {
    return (List) cGrep(f,a);
  }
  public static final Set sGrep(final BooleanFunctor f, final Set a) {
    return (Set) cGrep(f,a);
  }
  public static final void cInPlaceGrep(final BooleanFunctor f, Collection a) {
    for(Iterator i = a.iterator(); i.hasNext();) {
      if(!f.func(i.next())) {
        i.remove();
      }
    }
  }
  public static final void lInPlaceGrep(final BooleanFunctor f, List a) {
    cInPlaceGrep(f,a);
  }
  public static final void sInPlaceGrep(final BooleanFunctor f, Set a) {
    cInPlaceGrep(f,a);
  }
  //get all non-null values in the map
  public static final List validValues(final Map m) {
    List retval = new ArrayList();
    for(Iterator i = m.keySet().iterator(); i.hasNext();) {
      Object k = i.next();
      Object v = m.get(k);
      if(v != null) {
        retval.add(v);
      }
    }
    return retval;
  }
  //given a list of possible keys, return a list of keys with non-null values in the map
  public static final List validKeys(final Map m, final List k) {
    List retval = new ArrayList();
    for(Iterator i = k.iterator(); i.hasNext();) {
      Object o = i.next();
      if(m.get(o) != null) {
        retval.add(o);
      }
    }
    return retval;
  }
  //given a list of possible keys, return a list of the non-null values to which they are mapped
  public static final List validValues(final Map m, final List k) {
    List retval = new ArrayList();
    for(Iterator i = k.iterator(); i.hasNext();) {
      Object o = i.next();
      Object v = m.get(o);
      if(v != null) {
        retval.add(v);
      }
    }
    return retval;
  }

  public static final Object findFirst(final BooleanFunctor f, final List l) {
    for(Iterator i = l.iterator(); i.hasNext();) {
      Object o = i.next();
      if(f.func(o)) {
        return o;
      }
    }
    return null;
  }

  public static final Object findFirst(final BooleanFunctor f, final Object [] a) {
    for(int i = 0; i < a.length; i++) {
      if(f.func(a[i])) {
        return a[i];
      }
    }
    return null;
  }

	public static final Object findGreatest(final List l) {
		List temp = tempSort(l);
		return temp.get(temp.size()-1);
	}
	
	public static final Object findGreatest(final Comparator c, final List l) {
		List temp = tempSort(l,c);
		return temp.get(temp.size()-1);
	}

	public static final Object findLeast(final List l) {
		List temp = tempSort(l);
		return temp.get(0);
	}
	
	public static final Object findLeast(final Comparator c, final List l) {
		List temp = tempSort(l,c);
		return temp.get(0);
	}

	public static final List tempSort(final List l) {
		ArrayList temp = new ArrayList(l);
		Collections.sort(temp);
		return temp;
	}
	
	public static final List tempSort(final List l, final Comparator c) {
		ArrayList temp = new ArrayList(l);
		Collections.sort(temp, c);
		return temp;
	}

  public static final String join(final String sep, final Object [] items) {
    StringBuffer retval = new StringBuffer();
    for(int i = 0; i < items.length; i++) {
      retval.append(items[i].toString());
      if(i != items.length - 1)
        retval.append(sep);
    }
    return retval.toString();
  }

  public static final String join(final String sep, final Collection c) {
    StringBuffer retval = new StringBuffer();
    for(Iterator it = c.iterator(); it.hasNext();) {
      retval.append(it.next().toString());
      if(it.hasNext())
        retval.append(sep);
    }
    return retval.toString();
  }
}
