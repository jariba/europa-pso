#include "ModuleBlocksWorld.hh"
#include "BlocksWorldCustomCode.hh"

// Pieces necessary for various customizations:
#include "PSPlanDatabase.hh"


namespace EUROPA {

// static C init method to get handle when loading module as shared library
extern "C"
{
	Module* initializeModule()
	{
          return new ModuleBlocksWorld();
	}
}

  static bool & BlocksWorldInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  ModuleBlocksWorld::ModuleBlocksWorld()
      : Module("BlocksWorld")
  {
  }

  ModuleBlocksWorld::~ModuleBlocksWorld()
  {
  }

  void ModuleBlocksWorld::initialize()
  {
      if(BlocksWorldInitialized())
    	  return;
	  BlocksWorldInitialized() = true;
  }

  void ModuleBlocksWorld::uninitialize()
  {
	  BlocksWorldInitialized() = false;
  }

  void ModuleBlocksWorld::initialize(EngineId engine)
  {
  }

  void ModuleBlocksWorld::uninitialize(EngineId engine)
  {
  }
}
