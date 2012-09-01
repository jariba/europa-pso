package org.europa.ce.impl;

import org.europa.ce.CVariable;
import org.europa.ce.ConstraintEngine;
import org.europa.ce.DataType;
import org.europa.ce.Domain;

public class CVariableImpl implements CVariable 
{
	protected ConstraintEngine constraintEngine_;
	protected String name_;
	protected DataType dataType_;
	protected Domain baseDomain_;
	protected Domain currentDomain_;
	protected Object parent_;
	protected int index_;
	protected boolean isInternal_;
	protected boolean canBeSpecified_;
	
	public CVariableImpl(
				ConstraintEngine ce,
				String name,
				DataType dt,
				Domain baseDomain,
				Object parent,
				int index,
				boolean isInternal,
				boolean canBeSpecified)
	{
		constraintEngine_ = ce;
		name_ = name;
		dataType_ = dt;
		baseDomain_ = baseDomain;
		// TODO: currentDomain_ = baseDomain_.clone();
		parent_ = parent;
		index_ = index;
		isInternal_ = isInternal;
		canBeSpecified_ = canBeSpecified;
	}
	
	@Override public Domain getBaseDomain() { return baseDomain_; }
	@Override public Domain getCurrentDomain() { return currentDomain_; }
}
