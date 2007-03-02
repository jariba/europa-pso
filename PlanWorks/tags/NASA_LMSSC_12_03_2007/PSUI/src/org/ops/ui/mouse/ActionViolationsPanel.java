package org.ops.ui.mouse;

import java.util.List;
import dsa.DSA;
import dsa.Action;
import dsa.Violation;

import psengine.PSEngine;
import psengine.PSToken;

public class ActionViolationsPanel 
    extends MouseListenerPanel 
{
	private static final long serialVersionUID = 7462693069863073725L;

	protected DSA dsa_ = null;
	protected PSEngine psengine_ = null;
	
    public ActionViolationsPanel(DSA dsa)
    {
    	dsa_ = dsa;
    }
	
    public ActionViolationsPanel(PSEngine pse)
    {
    	psengine_ = pse;
    }
	
	@Override
	public void mouseMoved(Object key) 
	{
		if (key == null) 
			text_.setText("");
		else 
		    text_.setText(getViolations((Integer)key));
	}
		
	protected String getViolations(Integer key)
	{
		if (dsa_ != null) 
			return actionViolations(dsa_.getAction(key)); 
			
		
		return tokenViolations(psengine_.getTokenByKey(key));
	}
		
	protected String tokenViolations(PSToken t)
	{
		return t.getViolationExpl();
	}
	
	protected String actionViolations(Action a)
	{
		List<Violation> violations = a.getViolations();
		
		if (violations==null || violations.size()==0)
			return "No Violations";
		
		StringBuffer buf = new StringBuffer();

		for (Violation s : violations) {
		    buf.append(s.toString()).append("\n");	
		}
		
		return buf.toString();
	}
}
