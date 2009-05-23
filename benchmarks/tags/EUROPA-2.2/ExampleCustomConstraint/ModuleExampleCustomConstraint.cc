#include "ModuleExampleCustomConstraint.hh"
#include "ExampleCustomConstraintCustomCode.hh"

// Pieces necessary for various customizations:
#include "PSPlanDatabase.hh"
#include "ConstraintType.hh"
#include "FlawHandler.hh"


namespace EUROPA {

// static C init method to get handle when loading module as shared library
extern "C"
{
	ModuleId initializeModule()
	{
          return (new ModuleExampleCustomConstraint())->getId();
	}
}

  static bool & ExampleCustomConstraintInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleExampleCustomConstraint::ModuleExampleCustomConstraint()
      : Module("ExampleCustomConstraint")
  {
  }

  ModuleExampleCustomConstraint::~ModuleExampleCustomConstraint()
  {
  }

  void ModuleExampleCustomConstraint::initialize()
  {
      if(ExampleCustomConstraintInitialized())
    	  return;
	  ExampleCustomConstraintInitialized() = true;
  }

  void ModuleExampleCustomConstraint::uninitialize()
  {
	  ExampleCustomConstraintInitialized() = false;
  }

  void ModuleExampleCustomConstraint::initialize(EngineId engine)
  {
	  CESchema* schema = (CESchema*)engine->getComponent("CESchema");
	  REGISTER_CONSTRAINT(schema, ExampleConstraint, "example", "Default");
  }

  void ModuleExampleCustomConstraint::uninitialize(EngineId engine)
  {
  }
}
