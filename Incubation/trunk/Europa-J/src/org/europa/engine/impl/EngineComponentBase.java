package org.europa.engine.impl;

import org.europa.engine.Engine;
import org.europa.engine.EngineComponent;

public class EngineComponentBase 
	implements EngineComponent 
{
	protected String name_;
	protected Engine engine_ = null;
	
	public EngineComponentBase(String name)
	{
		name_ = name;
	}
	
	@Override
	public String getName() { return name_; }

	@Override
	public Engine getEngine() { return engine_; }

	@Override
	public void setEngine(Engine e) { engine_ = e; }
}
