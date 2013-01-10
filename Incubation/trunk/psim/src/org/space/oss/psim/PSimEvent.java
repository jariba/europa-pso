package org.space.oss.psim;

public class PSimEvent 
	implements Comparable<PSimEvent>
{
	protected long time_;
	protected PSimEventGenerator source_;
	protected Object eventData_;
	
	public PSimEvent(Long time, PSimEventGenerator source, Object data)
	{
		time_ = time;
		source_ = source;
		eventData_ = data;
	}
	
	public long getTime() { return time_; }
	public PSimEventGenerator getSource() { return source_; }
	public Object getEventData() { return eventData_; }

	@Override
	public int compareTo(PSimEvent rhs) 
	{
		long diff = getTime() - rhs.getTime();
		
		return (diff < 0 ? -1 : (diff > 0 ? 1 : 0));
	}
}
