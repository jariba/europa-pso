package org.space.oss.psim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PSimEventManager 
	implements TimeServiceObserver,PSimObservable
{
	public static final int EVENT_QUEUE_CHANGED=0;
	
	protected PSim psim_;
	protected List<PSimEventGenerator> eventGenerators_;
	protected List<PSimEvent> events_;
	protected List<PSimObserver> observers_;
	
	public PSimEventManager(PSim psim)
	{
		psim_ = psim;
		init();
	}
	
	public PSim getPSim() { return psim_; }
	
	protected void init()
	{
		observers_ = new ArrayList<PSimObserver>();
		eventGenerators_ = new ArrayList<PSimEventGenerator>();
		events_ = new ArrayList<PSimEvent>();
		
		for (Spacecraft s : psim_.getSpacecraftService().getAllSpacecraft()) {
			for (PSimEventGenerator eg : s.getEventGenerators())
				addGenerator(eg);
		}
		
		for (GroundStation gs : psim_.getCommandService().getGroundStations()) {
			for (PSimEventGenerator eg : gs.getEventGenerators())
				addGenerator(eg);			
		}
		
		resetEventQueue(psim_.getTimeService().getCurrentTime());
		psim_.getTimeService().addObserver(this);
	}
	
	public List<PSimEvent> getEvents() { return events_; }
	
	public boolean hasEvents() { return events_.size() > 0; }
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
		eventGenerators_.add(eg);
		eg.setManager(this);
	}

	public void setNextEvents(PSimEventGenerator eg, List<PSimEvent> nextEvents) 
	{
		// remove all previous events from eg, then insert new ones
		List<PSimEvent> toRemove = new ArrayList<PSimEvent>();
		for (PSimEvent e : this.events_) {
			if (e.getSource() == eg)
				toRemove.add(e);
		}
		events_.removeAll(toRemove);
		
		events_.addAll(nextEvents);
		Collections.sort(events_);		
		notifyEvent(EVENT_QUEUE_CHANGED,this);
	}
	
	public void playNextEvents()
	{
		if (events_.size() == 0)
			return;
		
		long nextTime = events_.get(0).getTime();
		
		List<PSimEvent> toRemove = new ArrayList<PSimEvent>();
		for (PSimEvent e : events_)
			if (e.getTime() == nextTime)
				toRemove.add(e);

		events_.removeAll(toRemove);

		notifyEvent(EVENT_QUEUE_CHANGED,this);
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

	@Override
	public void addObserver(PSimObserver o) 
	{
		observers_.add(o);
	}

	@Override
	public void removeObserver(PSimObserver o) 
	{
		observers_.remove(o);
	}
	
	protected void notifyEvent(int type,Object data)
	{
		for (PSimObserver o : observers_)
			o.handleEvent(type, data);
	}
}
