package org.europa.ce;

import java.util.Collection;

import org.europa.engine.EngineComponent;

public interface CESchema
	extends EngineComponent
{
	public abstract void addDataType(DataType dt);
	public abstract DataType getDataType(String name);
	public abstract Collection<DataType> getAllDataTypes();
}