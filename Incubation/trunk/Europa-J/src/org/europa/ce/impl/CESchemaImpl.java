package org.europa.ce.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.europa.ce.CESchema;
import org.europa.ce.DataType;
import org.europa.engine.Engine;

public class CESchemaImpl implements CESchema 
{
	public CESchemaImpl(String name)
	{
		name_ = name;
		dataTypes_ = new TreeMap<String,DataType>();
	}
	
	@Override
	public String getName() { return name_; }

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
	
	@Override
	public Engine getEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEngine(Engine e) {
		// TODO Auto-generated method stub
		
	}
	
	protected String name_;
	protected Map<String,DataType> dataTypes_;
}
