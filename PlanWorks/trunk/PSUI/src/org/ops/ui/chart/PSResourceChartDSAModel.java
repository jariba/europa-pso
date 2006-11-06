package org.ops.ui.chart;

import dsa.Resource;
import dsa.ResourceProfile;

public class PSResourceChartDSAModel 
    implements PSResourceChartModel 
{
	protected Resource resource_;
	
	public PSResourceChartDSAModel(Resource resource)
	{
	    resource_ = resource;
	}

	public ResourceProfile getCapacity() 
	{
		return resource_.getCapacity();
	}

	public ResourceProfile getUsage() 
	{
		return resource_.getUsage();
	}
}
