package org.europa.ce.impl.domain;

import org.europa.ce.DataType;
import org.europa.ce.Domain;

public abstract class DomainBase 
	implements Domain 
{
	protected DataType dataType_;
	
	public DomainBase(DataType dt)
	{
		dataType_ = dt;
	}
	
	public DataType getDataType() { return dataType_; } 
}
