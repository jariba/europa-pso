package org.ops.test.ui.gantt;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.ops.ui.gantt.PSGanttActivity;
import org.ops.ui.gantt.PSGanttActivityImpl;
import org.ops.ui.gantt.PSGanttModel;


/*
 * Hack to support Crew Planning demo
 */
public class PSGanttCrewModel 
    implements PSGanttModel
{
	List<List<PSGanttActivity>> activities_;
    
    public PSGanttCrewModel(Calendar start)
    {
        init(start);	
    }
	
	protected void init(Calendar start)
	{
    	int month = start.get(Calendar.MONTH), day = start.get(Calendar.DAY_OF_MONTH); 

    	// start times and end times in minutes
    	int times[][] = {
    			{
    				   0,  45,
    				1440,1500,
    			},
    			{     90,180,
    			    2000,2090,
    			},
    			{
    				1000,1050,
    				2000,2060,
    			}
    	};
    	
    	activities_ = new Vector<List<PSGanttActivity>>();
    	int actID = 1;
    	for (int i=0; i<times.length;i++) {
    		List<PSGanttActivity> ra = new Vector<PSGanttActivity>();
    		activities_.add(ra);
    		int t[] = times[i];
        	for (int j=0; j<t.length; j+=2) {
    			int sm = t[j];
    			int fm = t[j+1];
    	        Calendar actStart = new GregorianCalendar(2006,month,day+getDay(sm),getHour(sm),getMinute(sm));		
    	        Calendar actFinish = new GregorianCalendar(2006,month,day+getDay(fm),getHour(fm),getMinute(fm));
    	        Integer key = actID++;
    	        double violation = (Math.random() < 0.5 ? 1.0 : 0.0);
    	        ra.add(new PSGanttActivityImpl(key,actStart,actFinish, violation));
    	    	//System.out.println("ID:"+key+" Start:"+start.getTime()+" Finish:"+finish.getTime());
    		}
    	}
	}
	
	protected int getDay(int minutes) { return minutes/1440; }
	protected int getHour(int minutes) { return (minutes%1440) / 60; }
	protected int getMinute(int minutes) { return (minutes%1440) % 60; }

	public Iterator<PSGanttActivity> getActivities(int resource) 
	{	
		return activities_.get(resource).iterator();
	}

	public String getResourceColumn(int resource, int column) 
	{
		return "Crew Member "+ (resource+1);
	}

	public String[] getResourceColumnNames() {	return new String[]{"Resource"}; }

	public int getResourceCount() 
	{
		return activities_.size();
	}

	public void setActivityStart(Object key, Calendar value) 
	{
		notifyChange(key,"StartChanged",value);
	}

	public void setActivityFinish(Object key, Calendar value) 
	{
		notifyChange(key,"FinishChanged",value);
	}

    static int notificationCnt_=0;
	protected void notifyChange(Object key, String type, Object value)
	{
        System.out.println(++notificationCnt_ + " - Object changed - {"+
        		"id:"+key+" "+
        		"type:"+type+" "+
        		"newValue:"+value+ "}"
        );		
	}	
}
