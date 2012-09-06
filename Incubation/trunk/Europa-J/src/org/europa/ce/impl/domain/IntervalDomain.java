package org.europa.ce.impl.domain;

import java.util.List;

import org.europa.ce.DataType;

public class IntervalDomain 
	extends DomainBase 
{
	double lb_;
	double ub_;
	
	public IntervalDomain(DataType dt, double lb, double ub) 
	{
		super(dt);
		lb_ = lb;
		ub_ = ub; 
	}

	@Override
	public boolean isEnumerated() { return false; }

	@Override
	public List<Object> getValues() 
	{
		throw new RuntimeException("getValues() can only be called on Enumerated domains, not on "+this.getClass().getName());
	}

	@Override
	public boolean isInterval() { return true; }

	@Override
	public double getLowerBound() { return lb_; }

	@Override
	public double getUpperBound() { return ub_; }

	@Override
	public boolean isSingleton() 
	{
		return (lb_ == ub_);
	}

	@Override
	public Object getSingletonValue() 
	{
		assert(isSingleton());
		return lb_;
	}
}
