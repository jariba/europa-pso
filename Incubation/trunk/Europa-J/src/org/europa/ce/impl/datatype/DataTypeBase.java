package org.europa.ce.impl.datatype;

import org.europa.ce.CVariable;
import org.europa.ce.ConstraintEngine;
import org.europa.ce.DataType;
import org.europa.ce.Domain;

public abstract class DataTypeBase 
	implements DataType 
{
	protected String name_;
	
	public DataTypeBase(String name)
	{
		name_ = name;
	}
	
	@Override public String getName() { return name_; }

	@Override
	public CVariable createVariable(ConstraintEngine constraintEngine,
			Domain restrictedBaseDomain, boolean internal,
			boolean canBeSpecified, String name, Object parent, int index) 
	{
		// TODO Auto-generated method stub
		return null;
	}

}
