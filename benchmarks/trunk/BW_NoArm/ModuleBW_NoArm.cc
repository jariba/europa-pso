#include "ModuleBW_NoArm.hh"
#include "BW_NoArmCustomCode.hh"

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
          return (new ModuleBW_NoArm())->getId();
	}
}

  static bool & BW_NoArmInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleBW_NoArm::ModuleBW_NoArm()
      : Module("BW_NoArm")
  {
  }

  ModuleBW_NoArm::~ModuleBW_NoArm()
  {
  }

  void ModuleBW_NoArm::initialize()
  {
      if(BW_NoArmInitialized())
    	  return;
	  BW_NoArmInitialized() = true;
  }

  void ModuleBW_NoArm::uninitialize()
  {
	  BW_NoArmInitialized() = false;
  }

  void ModuleBW_NoArm::initialize(EngineId engine)
  {
  }

  void ModuleBW_NoArm::uninitialize(EngineId engine)
  {
  }
}
