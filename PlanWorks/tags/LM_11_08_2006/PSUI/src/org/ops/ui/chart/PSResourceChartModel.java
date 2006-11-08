package org.ops.ui.chart;


// TODO: introduce UI specific classes?

import dsa.ResourceProfile;

public interface PSResourceChartModel 
{
    public ResourceProfile getCapacity();
    public ResourceProfile getUsage();
}
