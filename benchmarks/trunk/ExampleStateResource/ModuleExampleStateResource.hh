#ifndef _H_ModuleExampleStateResource
#define _H_ModuleExampleStateResource

#include "Module.hh"

namespace EUROPA {
  class ModuleExampleStateResource : public Module
  {
    public:
      ModuleExampleStateResource();
      virtual ~ModuleExampleStateResource();

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

  typedef Id<ModuleExampleStateResource> ModuleExampleStateResourceId;  
}  


#endif /* #ifndef _H_ModuleExampleStateResource */
