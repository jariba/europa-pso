package org.ops.ui.gantt;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.ops.ui.gantt.PSGanttActivity;
import org.ops.ui.gantt.PSGanttActivityImpl;

import psengine.PSEngine;
import psengine.PSToken;
import psengine.PSTokenList;

public class PSGanttPSEModel 
    implements PSGanttModel 
{
	PSEngine psengine_;
	Calendar startHorizon_;
	String objectsType_;
	
	public PSGanttPSEModel(PSEngine pse, Calendar startHorizon, String objectsType)
	{
	    psengine_ = pse;	
	    startHorizon_ = startHorizon;
	    objectsType_ = objectsType;
	}

	public Iterator<PSGanttActivity> getActivities(int resource) 
	{
		assert (resource >=0 && resource < getResourceCount());
		
		// TODO: cache activities?
		List<PSGanttActivity> acts = new ArrayList<PSGanttActivity>();
		
		PSTokenList tokens = psengine_.getObjectByKey(resource).getTokens();
		for (int i=0;i<tokens.size();i++) {
			PSToken token = tokens.get(i);
			acts.add(new PSGanttActivityImpl(token.getKey(),
					                         instantToCalendar(token.getParameter("start").getLowerBound()),
					                         instantToCalendar(token.getParameter("end").getLowerBound()),
					                         token.getViolation()
					                         )
			);
		}
		
		return acts.iterator();
	}
	
	protected Calendar instantToCalendar(double i)
	{
		Calendar retval = (Calendar)startHorizon_.clone();
		// TODO: time unit must be a parameter, assuming minutes for now
		retval.add(Calendar.MINUTE, (int)i);
		//System.out.println("instantToCalendar:"+i.value()+" -> "+SimpleDateFormat.getInstance().format(retval.getTime()));
		return retval;
	}

	public String getResourceColumn(int resource, int column) 
	{
		if (column == 0 && resource < getResourceCount())
			return psengine_.getObjectByKey(resource).getName();
		
		return "";
	}

	static String resourceColumnNames_[] = { "Name" };
	public String[] getResourceColumnNames() 
	{
		return resourceColumnNames_;
	}

	public int getResourceCount() 
	{
		return psengine_.getObjectsByType(objectsType_).size();
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
