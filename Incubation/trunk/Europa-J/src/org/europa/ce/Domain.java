package org.europa.ce;

import java.util.List;

public interface Domain 
{
	public DataType getDataType();
	
	public boolean isEnumerated();
	public List<Object> getValues();	

	public boolean isInterval();
	public double getLowerBound();
	public double getUpperBound();
	
	public boolean isSingleton();
	public Object getSingletonValue();
}
