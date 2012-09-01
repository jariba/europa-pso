package org.europa.ce.impl.datatype;

import org.europa.ce.CVariable;
import org.europa.ce.ConstraintEngine;
import org.europa.ce.DataType;
import org.europa.ce.Domain;
import org.europa.ce.impl.CVariableImpl;

public abstract class DataTypeBase 
	implements DataType 
{
	protected String name_;
	protected Domain baseDomain_;
	
	public DataTypeBase(String name, Domain baseDomain)
	{
		name_ = name;
		baseDomain_ = baseDomain;
	}
	
	@Override public String getName() { return name_; }

	@Override
	public CVariable createVariable(
			ConstraintEngine constraintEngine,
			String name, 
			Object parent, 
			int index,
			Domain restrictedBaseDomain, 
			boolean internal,
			boolean canBeSpecified) 
	{
		Domain baseDomain = (restrictedBaseDomain==null ? baseDomain_ : restrictedBaseDomain);
		return new CVariableImpl(constraintEngine,name,this,baseDomain,parent,index,internal,canBeSpecified);
	}
}
