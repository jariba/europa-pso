package org.ops.ui.gantt;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Locale;

import dsa.DSA;
import dsa.Action;
import dsa.Instant;

import org.ops.ui.gantt.PSGanttActivity;
import org.ops.ui.gantt.PSGanttActivityImpl;

public class PSGanttDSAModel 
    implements PSGanttModel 
{
	DSA dsa_;
	Calendar startHorizon_;
	
	public PSGanttDSAModel(DSA dsa, Calendar startHorizon)
	{
	    dsa_ = dsa;	
	    startHorizon_ = startHorizon;
	}

	public Iterator<PSGanttActivity> getActivities(int resource) 
	{
		assert (resource >=0 && resource < getResourceCount());
		
		// TODO: cache activities?
		List<PSGanttActivity> acts = new ArrayList<PSGanttActivity>();
		
		List<Action> actions = dsa_.getComponents().get(resource).getActions();
		for (int i=0;i<actions.size();i++) {
			Action action = actions.get(i);
			acts.add(new PSGanttActivityImpl(action.getKey(),
					                         instantToCalendar(new Instant(action.getEarliestStart())),
					                         instantToCalendar(new Instant(action.getEarliestEnd())),
					                         action.getViolation()
					                         )
			);
		}
		
		return acts.iterator();
	}
	
	protected Calendar instantToCalendar(Instant i)
	{
		Calendar retval = (Calendar)startHorizon_.clone();
		// TODO: time unit must be a parameter, assuming minutes for now
		retval.add(Calendar.MINUTE, i.value());
		//System.out.println("instantToCalendar:"+i.value()+" -> "+SimpleDateFormat.getInstance().format(retval.getTime()));
		return retval;
	}

	public String getResourceColumn(int resource, int column) 
	{
		if (column == 0 && resource < getResourceCount())
			return dsa_.getComponents().get(resource).getName();
		
		return "";
	}

	static String resourceColumnNames_[] = { "Name" };
	public String[] getResourceColumnNames() 
	{
		return resourceColumnNames_;
	}

	public int getResourceCount() 
	{
		return dsa_.getComponents().size();
	}

	public void setActivityStart(Object key, Calendar start) 
	{
		// TODO Auto-generated method stub
		notifyChange(key,"StartChanged",start);
	}

	public void setActivityFinish(Object key, Calendar finish) 
	{
		// TODO Auto-generated method stub
		notifyChange(key,"FinishChanged",finish);
	}

    static int notificationCnt_=0;
	protected void notifyChange(Object key, String type, Object value)
	{
		Object newValue=value;
		
		if (value instanceof Calendar) {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", new Locale("en","US"));
			newValue = formatter.format(((Calendar)value).getTime());
		}
		
        System.out.println(++notificationCnt_ + " - Object changed - {"+
        		"id:"+key+" "+
        		"type:"+type+" "+
        		"newValue:"+newValue+ "}"
        );		
	}
}
