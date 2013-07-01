package org.space.oss.psim.spacecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.space.oss.psim.PSimEventGenerator;
import org.space.oss.psim.PSimObservable;
import org.space.oss.psim.PSimObserver;
import org.space.oss.psim.Spacecraft;

public class SubsystemBase 
	implements Subsystem, PSimObservable 
{
	protected String name_;
	transient protected Spacecraft spacecraft_;
	protected Map<String,Subsystem> subsystems_;
	protected ArrayList<PSimObserver> observers_;

	public SubsystemBase(Spacecraft s, String n)
	{
		name_ = n;
		spacecraft_ = s;
		subsystems_ = new HashMap<String,Subsystem>();
		observers_ = new ArrayList<PSimObserver>();
	}
	
	@Override
	public String getName() { return name_; }
	
	@Override
	public Spacecraft getSpacecraft() { return spacecraft_; }

	@Override
	public List<PSimEventGenerator> getEventGenerators() 
	{
		List<PSimEventGenerator> retval = new ArrayList<PSimEventGenerator>();
		for (Subsystem ss : this.getSubsystems())
			retval.addAll(ss.getEventGenerators());
		
		return retval;
	}

	@Override
	public Collection<Subsystem> getSubsystems() { return this.subsystems_.values(); }

	@Override
	public Subsystem getSubsystem(String name) 
	{
		return this.subsystems_.get(name);
	}

	@Override
	public void addObserver(PSimObserver o) { observers_.add(o); }

	@Override
	public void removeObserver(PSimObserver o) { observers_.remove(o); }
	
	protected void notifyEvent(int eventType, Object eventInfo)
	{
		for (PSimObserver o : observers_)
			o.handleEvent(eventType, eventInfo);
	}
}
