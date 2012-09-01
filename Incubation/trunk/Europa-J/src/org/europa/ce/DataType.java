package org.europa.ce;

public interface DataType 
{
	public String getName();
	public boolean isNumeric();
	
	public boolean canBeCompared(DataType rhs);
	public boolean isAssignableFrom(DataType rhs);
	
	public Domain getBaseDomain();
	
	public CVariable createVariable(
				ConstraintEngine constraintEngine,
				String name, 
				Object parent, 
				int index,
				Domain restrictedBaseDomain, // specify as null to use DataType's default base domain
				boolean internal,
				boolean canBeSpecified);
}
