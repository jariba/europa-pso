package dsa.impl;

import java.util.*;

import dsa.Action;
import dsa.Component;
import dsa.Attribute;
import dsa.DSA;
import dsa.InvalidSourceException;
import dsa.NoModelException;
import dsa.Proposition;
import dsa.Resource;

// nanoxml support
import net.n3.nanoxml.IXMLElement;

public class DSAImpl 
    implements DSA
{

    /** Static Data Members **/
    private static DSA s_instance = null;

    private String m_model = null;

    private DSAImpl(){}

    /**
     * @todo Make the suffix _g settable as input from the build or command line, or a configuration file
     */
    static public DSA instance(){
	if(s_instance == null){
	    s_instance = new DSAImpl();
	    System.loadLibrary("DSA_g");
	}
	return s_instance;
    }

    public void loadModel(String model) throws InvalidSourceException {
	m_model = model;
	JNI.load(model);
    }

    public void addPlan(String txSource) throws InvalidSourceException, NoModelException {
	if(m_model == null)
	    throw new NoModelException();

	JNI.addPlan(txSource);
    }

    public List<Component> getComponents() {
	List<Component> components =  new Vector<Component>();
	String responseStr = JNI.getComponents();

	try{
	    IXMLElement response = Util.toXML(responseStr);
	    
	    Enumeration children = response.enumerateChildren();
	    while(children.hasMoreElements()){
		IXMLElement componentXml = (IXMLElement) children.nextElement();
		int key = componentXml.getAttribute("key", 0);
		String name = componentXml.getAttribute("name", "NO_NAME");
		components.add(new ComponentImpl(key, name));
	    }
	}
	catch(Exception e){
	    e.printStackTrace();
	}

	return components;
    }

    public List<Attribute> getAttributes() {
	return new Vector<Attribute>();
    }

    public List<Action> getActions() {
	return new Vector<Action>();
    }

	public Action getAction(int actionKey) 
	{
    	String xml = JNI.getAction(actionKey);
		List<Action> actions = Util.getActionsFromXML(xml);
		return (actions.size() > 0 ? actions.get(0) : null);
	}

    public List<Proposition> getPropositions() 
    {
	    return new Vector<Proposition>();
    }

	public List<Resource> getResources() 
	{
		
		Vector<Resource> resources = new Vector<Resource>();
		
		String responseStr = JNI.getResources();

		try{
		    IXMLElement response = Util.toXML(responseStr);
		    
		    Enumeration children = response.enumerateChildren();
		    while(children.hasMoreElements()) {
		    	IXMLElement componentXml = (IXMLElement) children.nextElement();
		    	int key = componentXml.getAttribute("key", 0);
		    	String name = componentXml.getAttribute("name", "NO_NAME");
		    	resources.add(new ResourceImpl(key, name));
		    }
		}
		catch(Exception e){
		    e.printStackTrace();
		}

		return resources;
	}	
}
