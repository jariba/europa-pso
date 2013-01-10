package org.space.oss.psim;

import java.util.ArrayList;
import java.util.List;

public class PSimEventManager 
	implements TimeServiceObserver
{
	protected PSim psim_;
	protected List<PSimEventGenerator> eventGenerators_;
	protected List<PSimEvent> events_;
	
	public PSimEventManager(PSim psim)
	{
		init();
	}
	
	public PSim getPSim() { return psim_; }
	
	protected void init()
	{
		eventGenerators_ = new ArrayList<PSimEventGenerator>();
		events_ = new ArrayList<PSimEvent>();
		
		for (Spacecraft s : psim_.getSpacecraftService().getAllSpacecraft()) {
			for (PSimEventGenerator eg : s.getEventGenerators())
				addGenerator(eg);
		}
		
		psim_.getTimeService().addObserver(this);
	}
	
	public void disable()
	{
		psim_.getTimeService().removeObserver(this);
		for (PSimEventGenerator eg : eventGenerators_)
			eg.disable();
		
		events_.clear();
		eventGenerators_.clear();
	}

	protected void addGenerator(PSimEventGenerator eg) 
	{
		setNextEvents(eg,eg.getNextEvents(psim_.getTimeService().getCurrentTime()));
		eventGenerators_.add(eg);
		eg.setManager(this);
	}

	private void setNextEvents(PSimEventGenerator eg, List<PSimEvent> nextEvents) 
	{
		// TODO remove all previous events from eg, then insert new ones
		
	}
	
	public void playNextEvents()
	{
		if (events_.size() == 0)
			return;
		
		long nextTime = events_.get(0).getTime();
		
		for (PSimEvent e : events_)
			if (e.getTime() == nextTime)
				events_.remove(e);
		
		// TODO: notify that event Queue changed
		psim_.getTimeService().setCurrentTime(nextTime);
	}

	@Override
	public void handleCurrentTime(long t) 
	{
		resetEventQueue(t);
	}
	
	protected void resetEventQueue(long time)
	{
		events_.clear();
		for (PSimEventGenerator eg : eventGenerators_)
			setNextEvents(eg,eg.getNextEvents(time));
	}
}
