package org.ops.ui.chart;

import java.util.Iterator;

import dsa.ResourceProfile;

import psengine.PSResource;
import psengine.PSResourceProfile;
import psengine.PSTimePointList;

public class PSResourceChartPSEModel 
    implements PSResourceChartModel 
{
	protected PSResource resource_;
	
	public PSResourceChartPSEModel(PSResource resource)
	{
	    resource_ = resource;
	}

	public ResourceProfile getCapacity() 
	{
		return new RPWrapper(resource_.getLimits());
	}

	public ResourceProfile getUsage() 
	{
		return new RPWrapper(resource_.getLevels());
	}

	private static class RPWrapper
	    implements ResourceProfile
	{
		PSResourceProfile profile_;
		
		public RPWrapper(PSResourceProfile p)
		{
			profile_ = p;
		}

		public double getLowerBound(int time) 
		{
			return profile_.getLowerBound(time);
		}

		public Iterator<Integer> getTimes() 
		{
			return new RPIterator(profile_.getTimes());
		}

		public double getUpperBound(int time) {
			return profile_.getUpperBound(time);
		}
	}
	
	private static class RPIterator
	    implements Iterator<Integer>
	{
		int index_;
		PSTimePointList list_;
		
		public RPIterator(PSTimePointList l)
		{
			index_ = 0;
			list_ = l;
		}

		public boolean hasNext() 
		{
			return index_ < list_.size();
		}

		public Integer next() 
		{
			Integer retval = list_.get(index_); 
			index_++;
			return retval;
		}

		public void remove() 
		{
			throw new RuntimeException("not supported");
		}		
	}
}
