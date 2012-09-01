package org.europa.ce.impl;

import java.util.List;
import java.util.Set;

import org.europa.ce.CESchema;
import org.europa.ce.CVariable;
import org.europa.ce.Constraint;
import org.europa.ce.ConstraintEngine;
import org.europa.ce.Domain;
import org.europa.engine.impl.EngineComponentBase;

public class ConstraintEngineImpl 
	extends EngineComponentBase
	implements ConstraintEngine
{
	protected CESchema schema_;

	public ConstraintEngineImpl(String name, CESchema schema)
	{	
		super(name);
		schema_ = schema;
	}
	
	@Override
	public CVariable createVariable(String name, String dataType,
			Domain baseDomain, Object parent, int index, boolean isInternal,
			boolean canBeSpecified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CVariable getVariableByKey(long key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<CVariable> getAllVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Constraint createConstraint(String constraintType,
			List<CVariable> scope, String violationExpl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Constraint getConstraintByKey(long key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteConstraint(Constraint c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<Constraint> getAllConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getViolation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> getViolationExpl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Constraint> getViolatedConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void propagate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getAutopropagate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAutopropagate(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CESchema getSchema() { return schema_; }	
}
