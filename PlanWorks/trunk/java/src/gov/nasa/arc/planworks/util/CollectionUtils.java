//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: CollectionUtils.java,v 1.1 2004-03-12 23:21:29 miatauro Exp $
//
package gov.nasa.arc.planworks.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
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
  public static final Object [] aInPlaceMap(final UnaryFunctor f, Object [] a) {
    for(int i = 0; i < a.length; i++) {
      a[i] = f.func(a[i]);
    }
    return a;
  }
  public static final Collection cInPlaceMap(final UnaryFunctor f, Collection a) {
    Object [] temp = aInPlaceMap(f, a.toArray());
    a = Arrays.asList(temp);
    //a.clear();
    //a.addAll(Arrays.asList(temp));
    return a;
  }
  public static final List lInPlaceMap(final UnaryFunctor f, List a) {
    return (List) cInPlaceMap(f, a);
  }
  public static final Set sInPlaceMap(final UnaryFunctor f, Set a) {
    return (Set) cInPlaceMap(f, a);
  }
  public static final Object [] aGrep(final BooleanFunctor f, final Object [] a) {
    return cGrep(f, Arrays.asList(a)).toArray();
  }
  public static final Collection cGrep(final BooleanFunctor f, final Collection a) {
    Collection retval = null;
    try {retval = (Collection) a.getClass().newInstance();}
    catch(Exception e) {return null;}
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
  public static final Object [] aInPlaceGrep(final BooleanFunctor f, Object [] a) {
    return cInPlaceGrep(f, Arrays.asList(a)).toArray();
  }
  public static final Collection cInPlaceGrep(final BooleanFunctor f, Collection a) {
    for(Iterator i = a.iterator(); i.hasNext();) {
      if(!f.func(i.next())) {
        i.remove();
      }
    }
    return a;
  }
  public static final List lInPlaceGrep(final BooleanFunctor f, List a) {
    return (List) cInPlaceGrep(f,a);
  }
  public static final Set sInPlaceGrep(final BooleanFunctor f, Set a) {
    return (Set) cInPlaceGrep(f,a);
  }

  public static final List validValues(Map m) {
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

  public static final List validKeys(Map m, List k) {
    List retval = new ArrayList();
    for(Iterator i = k.iterator(); i.hasNext();) {
      Object o = i.next();
      if(m.get(o) != null) {
        retval.add(o);
      }
    }
    return retval;
  }
  
  public static final List validValues(Map m, List k) {
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

  public static final Object findFirst(BooleanFunctor f, List l) {
    for(Iterator i = l.iterator(); i.hasNext();) {
      Object o = i.next();
      if(f.func(o)) {
        return o;
      }
    }
    return null;
  }

  public static final Object findFirst(BooleanFunctor f, Object [] a) {
    for(int i = 0; i < a.length; i++) {
      if(f.func(a[i])) {
        return a[i];
      }
    }
    return null;
  }

}
