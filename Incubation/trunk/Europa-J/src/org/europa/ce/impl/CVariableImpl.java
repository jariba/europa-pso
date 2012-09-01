package org.europa.ce.impl;

import org.europa.ce.CVariable;
import org.europa.ce.DataType;
import org.europa.ce.Domain;

public class CVariableImpl implements CVariable 
{
	protected DataType dataType_;
	protected Domain baseDomain_;
	protected Domain currentDomain_;
	
	public CVariableImpl(
				DataType dt,
				Domain baseDomain)
	{
		dataType_ = dt;
		baseDomain_ = baseDomain;
		// TODO: currentDomain_ = baseDomain_.clone();
	}
	
	@Override public Domain getBaseDomain() { return baseDomain_; }
	@Override public Domain getCurrentDomain() { return currentDomain_; }
}
