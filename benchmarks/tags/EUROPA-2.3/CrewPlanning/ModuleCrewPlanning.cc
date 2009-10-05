#include "ModuleCrewPlanning.hh"
#include "CrewPlanningCustomCode.hh"

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
          return (new ModuleCrewPlanning())->getId();
	}
}

  static bool & CrewPlanningInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleCrewPlanning::ModuleCrewPlanning()
      : Module("CrewPlanning")
  {
  }

  ModuleCrewPlanning::~ModuleCrewPlanning()
  {
  }

  void ModuleCrewPlanning::initialize()
  {
      if(CrewPlanningInitialized())
    	  return;
	  CrewPlanningInitialized() = true;
  }

  void ModuleCrewPlanning::uninitialize()
  {
	  CrewPlanningInitialized() = false;
  }

  void ModuleCrewPlanning::initialize(EngineId engine)
  {
  }

  void ModuleCrewPlanning::uninitialize(EngineId engine)
  {
  }
}
