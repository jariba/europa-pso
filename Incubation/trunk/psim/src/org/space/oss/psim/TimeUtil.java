package org.space.oss.psim;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtil 
{
	public static String toGMTPlusLongString(long time)
	{
		StringBuffer buf = new StringBuffer();
		
		TimeZone gmt = TimeZone.getTimeZone(TimeZone.getDefault().getID()); // TODO: use GMT
		//Calendar c = Calendar.getInstance(gmt,true);
		buf.append("(").append(time).append(")");
		return buf.toString();
	}
}
