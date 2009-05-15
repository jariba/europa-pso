#ifndef _H_ModuleSimpleBlocksWorld
#define _H_ModuleSimpleBlocksWorld

#include "Module.hh"

namespace EUROPA {
  class ModuleSimpleBlocksWorld : public Module
  {
    public:
      ModuleSimpleBlocksWorld();
      virtual ~ModuleSimpleBlocksWorld();

      /**
       * @brief Initialize all default elements of the module 
       */
	  virtual void initialize();
	  /**
	   * @brief Uninitialize all default elements of the module 
	   */
	  virtual void uninitialize();   

	  virtual void initialize(EngineId engine);   // initialization of a particular engine instance

	  virtual void uninitialize(EngineId engine); // cleanup of a particular engine instance	  
  };

  typedef Id<ModuleSimpleBlocksWorld> ModuleSimpleBlocksWorldId;  
}  


#endif /* #ifndef _H_ModuleSimpleBlocksWorld */
