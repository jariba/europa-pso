package org.space.oss.psim;

public interface TimeService extends PSimService
{
	public long getCurrentTime();
	public void setCurrentTime(long t);
	
	public void addObserver(TimeServiceObserver o);
	public void removeObserver(TimeServiceObserver o);
}
