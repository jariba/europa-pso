#include "ModuleLogistics.hh"
#include "LogisticsCustomCode.hh"

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
          return (new ModuleLogistics())->getId();
	}
}

  static bool & LogisticsInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleLogistics::ModuleLogistics()
      : Module("Logistics")
  {
  }

  ModuleLogistics::~ModuleLogistics()
  {
  }

  void ModuleLogistics::initialize()
  {
      if(LogisticsInitialized())
    	  return;
	  LogisticsInitialized() = true;
  }

  void ModuleLogistics::uninitialize()
  {
	  LogisticsInitialized() = false;
  }

  void ModuleLogistics::initialize(EngineId engine)
  {
  }

  void ModuleLogistics::uninitialize(EngineId engine)
  {
  }
}
