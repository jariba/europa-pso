package org.ops.ui.mouse;


import dsa.Action;
import dsa.DSA;

public class ActionDetailsPanel 
    extends MouseListenerPanel 
{
	private static final long serialVersionUID = 7779941401503562818L;

	protected DSA dsa_;
	
    public ActionDetailsPanel(DSA dsa)
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
		    text_.setText(actionDetails(act));
		}
	}
		
	protected String actionDetails(Action act)
	{
		StringBuffer buf = new StringBuffer();

        buf.append("ID     : ").append(act.getKey()).append("\n")
           .append("Name   : ").append(act.getName()).append("\n")
           .append("Start  : ").append(act.getEarliestStart()).append("\n")
           .append("Finish : ").append(act.getEarliestEnd()).append("\n")
        ;
           
		return buf.toString();
	}
}
