package org.europa.ce.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.europa.ce.CESchema;
import org.europa.ce.DataType;
import org.europa.engine.impl.EngineComponentBase;

public class CESchemaImpl 
	extends EngineComponentBase
	implements CESchema 
{
	protected Map<String,DataType> dataTypes_;

	public CESchemaImpl(String name)
	{
		super(name);
		dataTypes_ = new TreeMap<String,DataType>();
	}
	
	/* (non-Javadoc)
	 * @see org.europa.ce.CESchema#addDataType(org.europa.ce.DataType)
	 */
	@Override
	public void addDataType(DataType dt)
	{
		dataTypes_.put(dt.getName(),dt);
	}
	
	/* (non-Javadoc)
	 * @see org.europa.ce.CESchema#getDataType(java.lang.String)
	 */
	@Override
	public DataType getDataType(String name)
	{
		return dataTypes_.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.europa.ce.CESchema#getAllDataTypes()
	 */
	@Override
	public Collection<DataType> getAllDataTypes()
	{
		return dataTypes_.values();
	}
}
