package org.ops.ui.gantt;

import java.util.Calendar;

public interface PSGanttActivity 
{
	public Object getKey();
    public Calendar getStart();
    public Calendar getFinish();
    public double getViolation();
}
