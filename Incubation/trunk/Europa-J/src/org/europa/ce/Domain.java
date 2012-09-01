package org.europa.ce;

import java.util.List;

public interface Domain 
{
	public DataType getDataType();
	
	public boolean isEnumerated();
	public boolean isInterval();
	
	public void isSingleton();

	public double getLowerBound();
	public double getUpperBound();
	
	public List<Object> getValues();	
}
