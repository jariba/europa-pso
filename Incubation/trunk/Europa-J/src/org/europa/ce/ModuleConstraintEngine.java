package org.europa.ce;

import org.europa.ce.impl.CESchemaImpl;
import org.europa.ce.impl.ConstraintEngineImpl;
import org.europa.engine.Engine;
import org.europa.engine.EngineModule;

public class ModuleConstraintEngine 
	implements EngineModule 
{
	@Override
	public String getName() { return "ConstraintEngineModule"; }

	@Override
	public void initialize() 
	{
	}

	@Override
	public void uninitialize() 
	{
	}

	@Override
	public void initialize(Engine e) 
	{
		CESchema ces = new CESchemaImpl("CESchema");
		
		e.addComponent(ces);
		e.addComponent(new ConstraintEngineImpl("ConstraintEngine", ces));
	}

	@Override
	public void uninitialize(Engine e) 
	{
		e.removeComponent("ConstraintEngine");
		e.removeComponent("CESchema");
	}
}
