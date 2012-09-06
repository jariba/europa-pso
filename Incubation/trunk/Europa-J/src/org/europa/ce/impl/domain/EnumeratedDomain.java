package org.europa.ce.impl.domain;

import java.util.ArrayList;
import java.util.List;

import org.europa.ce.DataType;

public class EnumeratedDomain 
	extends DomainBase 
{
	protected List<Object> values_;
	
	public EnumeratedDomain(DataType dt) 
	{
		super(dt);
		values_ = new ArrayList<Object>();
	}

	@Override
	public boolean isEnumerated() { return true; }

	@Override
	public List<Object> getValues() { return values_; }

	@Override
	public boolean isInterval() { return false; }

	@Override
	public double getLowerBound() 
	{ 
		throw new RuntimeException("getLowerBound() can only be called on IntervalDomains, not on "+this.getClass().getName());	
	}

	@Override
	public double getUpperBound() 
	{
		throw new RuntimeException("getUpperBound() can only be called on IntervalDomains, not on "+this.getClass().getName());	
	}

	@Override
	public boolean isSingleton() 
	{
		return (values_.size() == 1);
	}

	@Override
	public Object getSingletonValue() 
	{
		assert(isSingleton());
		return values_.get(0);
	}
}
