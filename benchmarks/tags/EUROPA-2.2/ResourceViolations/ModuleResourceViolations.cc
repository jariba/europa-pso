#include "ModuleResourceViolations.hh"
#include "ResourceViolationsCustomCode.hh"

// Pieces necessary for various customizations:
#include "PSPlanDatabase.hh"
#include "Schema.hh"
#include "FlawHandler.hh"


namespace EUROPA {

// static C init method to get handle when loading module as shared library
extern "C"
{
	ModuleId initializeModule()
	{
          return (new ModuleResourceViolations())->getId();
	}
}

  static bool & ResourceViolationsInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleResourceViolations::ModuleResourceViolations()
      : Module("ResourceViolations")
  {
  }

  ModuleResourceViolations::~ModuleResourceViolations()
  {
  }

  void ModuleResourceViolations::initialize()
  {
      if(ResourceViolationsInitialized())
    	  return;
	  ResourceViolationsInitialized() = true;
  }

  void ModuleResourceViolations::uninitialize()
  {
	  ResourceViolationsInitialized() = false;
  }

  void ModuleResourceViolations::initialize(EngineId engine)
  {
  }

  void ModuleResourceViolations::uninitialize(EngineId engine)
  {
  }
}
