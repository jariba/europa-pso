#include "ModuleExampleBooleanStateResource.hh"
#include "ExampleBooleanStateResourceCustomCode.hh"

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
          return (new ModuleExampleBooleanStateResource())->getId();
	}
}

  static bool & ExampleBooleanStateResourceInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleExampleBooleanStateResource::ModuleExampleBooleanStateResource()
      : Module("ExampleBooleanStateResource")
  {
  }

  ModuleExampleBooleanStateResource::~ModuleExampleBooleanStateResource()
  {
  }

  void ModuleExampleBooleanStateResource::initialize()
  {
      if(ExampleBooleanStateResourceInitialized())
    	  return;
	  ExampleBooleanStateResourceInitialized() = true;
  }

  void ModuleExampleBooleanStateResource::uninitialize()
  {
	  ExampleBooleanStateResourceInitialized() = false;
  }

  void ModuleExampleBooleanStateResource::initialize(EngineId engine)
  {
      FactoryMgr* pfm = (FactoryMgr*)engine->getComponent("ProfileFactoryMgr");
      REGISTER_PROFILE(pfm, StateProfile, StateProfile);
  }

  void ModuleExampleBooleanStateResource::uninitialize(EngineId engine)
  {
  }
}
