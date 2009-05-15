#include "ModuleBedrest.hh"
#include "BedrestCustomCode.hh"

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
          return (new ModuleBedrest())->getId();
	}
}

  static bool & BedrestInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleBedrest::ModuleBedrest()
      : Module("Bedrest")
  {
  }

  ModuleBedrest::~ModuleBedrest()
  {
  }

  void ModuleBedrest::initialize()
  {
      if(BedrestInitialized())
    	  return;
	  BedrestInitialized() = true;
  }

  void ModuleBedrest::uninitialize()
  {
	  BedrestInitialized() = false;
  }

  void ModuleBedrest::initialize(EngineId engine)
  {
  }

  void ModuleBedrest::uninitialize(EngineId engine)
  {
  }
}
