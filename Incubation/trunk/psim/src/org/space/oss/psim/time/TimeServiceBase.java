package org.space.oss.psim.time;

import java.util.ArrayList;
import java.util.List;

import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.TimeService;
import org.space.oss.psim.TimeServiceObserver;

public class TimeServiceBase implements TimeService 
{
	protected PSim psim_;
	protected long currentTime_;
	protected List<TimeServiceObserver> observers_;
	
	public TimeServiceBase()
	{
	}

	@Override
	public void init(PSim psim, Config cfg) 
	{
		psim_ = psim;
		currentTime_ = 0;
		observers_ = new ArrayList<TimeServiceObserver>();		
	}

	@Override
	public void shutdown() 
	{
	}

	
	@Override
	public long getCurrentTime() { return currentTime_; }

	@Override
	public void setCurrentTime(long t) 
	{ 
		currentTime_ = t; 
		notifyCurrentTimeChanged();
	}

	protected void notifyCurrentTimeChanged()
	{
		for (TimeServiceObserver o : observers_)
			o.handleCurrentTime(currentTime_);
	}
	
	@Override
	public void addObserver(TimeServiceObserver o) 
	{
		observers_.add(o);
	}

	@Override
	public void removeObserver(TimeServiceObserver o) 
	{
		observers_.remove(o);
	}
}
