package org.ops.ui.mouse;


import dsa.Action;
import dsa.DSA;
import dsa.Parameter;

import psengine.PSEngine;
import psengine.PSToken;
import psengine.PSVariable;
import psengine.PSVariableList;

public class ActionDetailsPanel 
    extends MouseListenerPanel 
{
	private static final long serialVersionUID = 7779941401503562818L;

	protected DSA dsa_ = null;
	protected PSEngine psengine_ = null;
	
    public ActionDetailsPanel(DSA dsa)
    {
    	dsa_ = dsa;
    }
	
    public ActionDetailsPanel(PSEngine pse)
    {
    	psengine_ = pse;
    }
	
	@Override
	public void mouseMoved(Object key) 
	{
		if (key == null) 
			text_.setText("");
		else 
		    text_.setText(getDetails((Integer)key));
	}
	
	protected String getDetails(Integer key)
	{
		if (dsa_ != null) 
			return actionDetails(dsa_.getAction(key)); 
			
		
		return tokenDetails(psengine_.getTokenByKey(key));
	}
		
	protected String tokenDetails(PSToken t)
	{
		StringBuffer buf = new StringBuffer();

        buf.append("ID     : ").append(t.getKey()).append("\n")
           .append("Name   : ").append(t.getName()).append("\n")
           .append("Start  : ").append(t.getParameter("start")).append("\n")
           .append("Finish : ").append(t.getParameter("end")).append("\n")
        ;
        
        if (t.getParameters().size() > 0) {
            buf.append("Parameters:").append("\n");
            PSVariableList parameters = t.getParameters();
            for (int i = 0; i< parameters.size(); i++) {
            	PSVariable p = parameters.get(i);
                buf.append(p.getName()).append(" : ").append(p.toString()).append("\n");
            }
        }
           
		return buf.toString();
	}
	
	protected String actionDetails(Action act)
	{
		StringBuffer buf = new StringBuffer();

        buf.append("ID     : ").append(act.getKey()).append("\n")
           .append("Name   : ").append(act.getName()).append("\n")
           .append("Start  : ").append(act.getEarliestStart()).append("\n")
           .append("Finish : ").append(act.getEarliestEnd()).append("\n")
        ;
        
        if (act.getParameters().size() > 0) {
            buf.append("Parameters:").append("\n");
            for (Parameter p : act.getParameters()) {
                buf.append(p.getName()).append(" : ").append(p.getValue()).append("\n");
            }
        }
           
		return buf.toString();
	}
}
