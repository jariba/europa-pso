//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: FunctorFactory.java,v 1.1 2004-03-12 23:21:29 miatauro Exp $
//
package gov.nasa.arc.planworks.util;

public final class FunctorFactory {
  private static final FunctorFactory factory = new FunctorFactory();
  private FunctorFactory(){}

  /***************FACTORY METHODS***************/

  /*boolean functor factory methods*/
  
  public static final BooleanFunctor equalFunctor(Object n) {
    return factory._equalFunctor(n);
  }

  private final BooleanFunctor _equalFunctor(Object n) {
    return new EqualsFunctor(n);
  }

  public static final BooleanFunctor notEqualFunctor(Object n) {
    return factory._notEqualFunctor(n);
  }

  private final BooleanFunctor _notEqualFunctor(Object n) {
    return (new NotEqualsFunctor(n));
  }

  public static final BooleanFunctor greaterThanFunctor(Object n) {
    return factory._greaterThanFunctor(n);
  }

  private final BooleanFunctor _greaterThanFunctor(Object n) {
    return new GreaterThanFunctor(n);
  }

  public static final BooleanFunctor lessThanFunctor(Object n) {
    return factory._lessThanFunctor(n);
  }

  private final BooleanFunctor _lessThanFunctor(Object n) {
    return new LessThanFunctor(n);
  }

  public static final BooleanFunctor strContainsFunctor(String n) {
    return factory._strContainsFunctor(n);
  }

  private final BooleanFunctor _strContainsFunctor(String s) {
    return new StrContainsFunctor(s);
  }

  public static final BooleanFunctor containsStrFunctor(String n) {
    return factory._containsStrFunctor(n);
  }

  private final BooleanFunctor _containsStrFunctor(String s) {
    return new ContainsStrFunctor(s);
  }
  /*unary functor factory methods*/

  /*****************FUNCTOR CLASSES******************/
  
  /*boolean functor classes*/

  private class ObjectBoolFunctor implements BooleanFunctor {
    protected Object o;
    public ObjectBoolFunctor(Object o) {
      this.o = o;
    }
    public boolean func(Object n){return false;}
  }

  private class EqualsFunctor extends ObjectBoolFunctor {
    public EqualsFunctor(Object o) {
      super(o);
    }
    public boolean func(Object n) {
      return o.equals(n);
    }
  }

  private class NotEqualsFunctor extends ObjectBoolFunctor {
    public NotEqualsFunctor(Object o) {
      super(o);
    }
    public boolean func(Object n) {
      return !o.equals(n);
    }
  }

  private class LessThanFunctor extends ObjectBoolFunctor {
    public LessThanFunctor(Object o) {
      super(o);
    }
    public boolean func(Object n) {
      return ((Comparable)n).compareTo((Comparable)o) == -1;
    }
  }

  private class GreaterThanFunctor extends ObjectBoolFunctor {
    public GreaterThanFunctor(Object o) {
      super(o);
    }
    public boolean func(Object n) {
      return ((Comparable)n).compareTo((Comparable)o) == 1;
    }
  }

  //returns true if the string s contains the argument n
  private class StrContainsFunctor extends ObjectBoolFunctor {
    public StrContainsFunctor(String s) {
      super(s);
    }
    public boolean func(Object n) {
      return ((String)o).indexOf((String)n) != -1;
    }
  }

  //returns true if the argument n contains the string s
  private class ContainsStrFunctor extends ObjectBoolFunctor {
    public ContainsStrFunctor(String s) {
      super(s);
    }
    public boolean func(Object n) {
      return ((String)n).indexOf((String)o) != -1;
    }
  }
  /*unary functor classes*/
}
