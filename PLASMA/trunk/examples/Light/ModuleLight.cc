#include "ModuleLight.hh"
#include "LightCustomCode.hh"

// Pieces necessary for various customizations:
#include "PSPlanDatabase.hh"


namespace EUROPA {

// static C init method to get handle when loading module as shared library
extern "C"
{
	Module* initializeModule()
	{
          return (new ModuleLight());
	}
}

  static bool & LightInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleLight::ModuleLight()
      : Module("Light")
  {
  }

  ModuleLight::~ModuleLight()
  {
  }

  void ModuleLight::initialize()
  {
      if(LightInitialized())
    	  return;
	  LightInitialized() = true;
  }

  void ModuleLight::uninitialize()
  {
	  LightInitialized() = false;
  }

  void ModuleLight::initialize(EngineId engine)
  {
  }

  void ModuleLight::uninitialize(EngineId engine)
  {
  }
}
