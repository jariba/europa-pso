package psengine;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;
import psengine.util.LibraryLoader;
import psengine.PSObjectList;

public class PSUtil
{
	/*
	 * transforms a swig-wrapped PSList into a java.util.List
	 */
    public static List<Object> toJavaList(Object psList)
    {
    	try {
    	    List<Object> retval = new Vector<Object>();
    	
    	    Method m = psList.getClass().getMethod("size", (Class[])null);
    	    int size = (Integer)m.invoke(psList,(Object[])null);
    	    m = psList.getClass().getMethod("get", new Class[]{int.class});
    	    Object args[] = new Object[1];
    	    for (int i=0; i<size;i++) {
    	    	args[0] = i;
    	    	retval.add(m.invoke(psList, args));
    	    }
    	    	
        	return retval;
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }	
    
    // TODO: unify this one with the toJavaList by using generics
    public static List<PSResource> toResourceList(PSObjectList psList)
    {
   	    List<PSResource> retval = new Vector<PSResource>();
    	
   	    for (int i=0; i<psList.size();i++) 
   	    	retval.add(PSResource.asPSResource(psList.get(i)));
    	    	
       	return retval;
    }	
    
    public static void loadLibraries(String debugMode)
    {
        LibraryLoader.loadLibrary("System_"+debugMode);	
    }
    
    /** 
     * Try various modifiers to pick the libraries. If all fail, throws 
     * UnsatisfiedLinkError 
     */
    public static void loadLibraries() {
    	try { loadLibraries("g"); return; }
    	catch (UnsatisfiedLinkError e) {}
    	try { loadLibraries("o"); return; }
    	catch (UnsatisfiedLinkError e) {}
    	loadLibraries("p"); // do not catch if this throws
    }
}

