#include "ModuleExampleStateResource.hh"
#include "ExampleStateResourceCustomCode.hh"

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
          return (new ModuleExampleStateResource())->getId();
	}
}

  static bool & ExampleStateResourceInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleExampleStateResource::ModuleExampleStateResource()
      : Module("ExampleStateResource")
  {
  }

  ModuleExampleStateResource::~ModuleExampleStateResource()
  {
  }

  void ModuleExampleStateResource::initialize()
  {
      if(ExampleStateResourceInitialized())
    	  return;
	  ExampleStateResourceInitialized() = true;
  }

  void ModuleExampleStateResource::uninitialize()
  {
	  ExampleStateResourceInitialized() = false;
  }

  void ModuleExampleStateResource::initialize(EngineId engine)
  {
      FactoryMgr* pfm = (FactoryMgr*)engine->getComponent("ProfileFactoryMgr");
      REGISTER_PROFILE(pfm, StateProfile, StateProfile);
  }

  void ModuleExampleStateResource::uninitialize(EngineId engine)
  {
  }
}
