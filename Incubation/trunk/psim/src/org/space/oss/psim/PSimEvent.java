package org.space.oss.psim;

public class PSimEvent 
{
	protected long time_;
	protected String source_;
	protected Object eventData_;
	
	public PSimEvent(Long time, String source, Object data)
	{
		time_ = time;
		source_ = source;
		eventData_ = data;
	}
	
	public long getTime() { return time_; }
	public String getSource() { return source_; }
	public Object getEventData() { return eventData_; }
}
