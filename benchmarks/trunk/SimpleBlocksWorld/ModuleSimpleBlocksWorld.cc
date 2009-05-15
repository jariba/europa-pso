#include "ModuleSimpleBlocksWorld.hh"
#include "SimpleBlocksWorldCustomCode.hh"

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
          return (new ModuleSimpleBlocksWorld())->getId();
	}
}

  static bool & SimpleBlocksWorldInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleSimpleBlocksWorld::ModuleSimpleBlocksWorld()
      : Module("SimpleBlocksWorld")
  {
  }

  ModuleSimpleBlocksWorld::~ModuleSimpleBlocksWorld()
  {
  }

  void ModuleSimpleBlocksWorld::initialize()
  {
      if(SimpleBlocksWorldInitialized())
    	  return;
	  SimpleBlocksWorldInitialized() = true;
  }

  void ModuleSimpleBlocksWorld::uninitialize()
  {
	  SimpleBlocksWorldInitialized() = false;
  }

  void ModuleSimpleBlocksWorld::initialize(EngineId engine)
  {
  }

  void ModuleSimpleBlocksWorld::uninitialize(EngineId engine)
  {
  }
}
