package org.europa.ce;

public interface DataType 
{
	public String getName();
	public boolean isNumeric();
	
	public boolean canBeCompared(DataType rhs);
	public boolean isAssignableFrom(DataType rhs);
	
	public CVariable createVariable(
				ConstraintEngine constraintEngine,
				Domain baseDomain,
				boolean internal,
				boolean canBeSpecified,
				String name,
				Object parent,
				int index);	
}
