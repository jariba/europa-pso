package org.ops.ui.mouse;

import java.util.List;
import dsa.DSA;
import dsa.Action;
import dsa.Violation;

public class ActionViolationsPanel 
    extends MouseListenerPanel 
{
	private static final long serialVersionUID = 7462693069863073725L;

	protected DSA dsa_;
	
    public ActionViolationsPanel(DSA dsa)
    {
    	dsa_ = dsa;
    }
	
	@Override
	public void mouseMoved(Object key) 
	{
		if (key == null) {
			text_.setText("");
		}
		else {
		    Action act = dsa_.getAction((Integer)key);
		    text_.setText(violationsToText(act.getViolations()));
		}
	}
		
	protected String violationsToText(List<Violation> violations)
	{
		if (violations==null || violations.size()==0)
			return "No Violations";
		
		StringBuffer buf = new StringBuffer();

		for (Violation s : violations) {
		    buf.append(s.toString()).append("\n");	
		}
		
		return buf.toString();
	}
}
