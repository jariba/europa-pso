#include "ModuleExample.hh"
#include "ExampleCustomCode.hh"

// Pieces necessary for various customizations:
#include "ConstraintLibrary.hh"
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
		return new ModuleExample();
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
      if(ExampleInitialized())
    	  return;
      
      REGISTER_CONSTRAINT(ExampleConstraint, "example", "Default");
	  ExampleInitialized() = true;
  }  

  void ModuleExample::uninitialize()
  {
	  ExampleInitialized() = false;
  }  
  
  void ModuleExample::initialize(EngineId engine)
  {
  }
  
  void ModuleExample::uninitialize(EngineId engine)
  {	  
  }  
}
