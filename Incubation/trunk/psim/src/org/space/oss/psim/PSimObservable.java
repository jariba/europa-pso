package org.space.oss.psim;


public interface PSimObservable 
{
	public void addObserver(PSimObserver o);
	public void removeObserver(PSimObserver o);
}
