#include "ModuleExample.hh"
#include "ExampleCustomCode.hh"

// Pieces necessary for various customizations:
#include "PSPlanDatabase.hh"
#include "TransactionInterpreter.hh"
#include "Schema.hh"
#include "FlawHandler.hh"


namespace EUROPA {

// static C init method to get handle when loading module as shared library
extern "C" 
{
	ModuleId initializeModule()
	{
		return (new ModuleExample())->getId();
	}
}

  static bool & ExampleInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleExample::ModuleExample()
      : Module("Example")
  {
  }

  ModuleExample::~ModuleExample()
  {	  
  }  
  
  void ModuleExample::initialize()
  {
 
  }  

  void ModuleExample::uninitialize()
  {
  }  
  
  void ModuleExample::initialize(EngineId engine)
  {
	  if(ExampleInitialized())
		  return;

	  CESchema* schema = (CESchema*)engine->getComponent("CESchema");

	  REGISTER_CONSTRAINT(schema, ExampleConstraint, "example", "Default");
	  ExampleInitialized() = true; 
  }

  
  void ModuleExample::uninitialize(EngineId engine)
  {	  
	  ExampleInitialized() = false;
  }  
}
