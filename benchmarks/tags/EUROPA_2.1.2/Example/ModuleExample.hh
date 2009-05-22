#ifndef _H_ModuleExample
#define _H_ModuleExample

#include "Module.hh"

namespace EUROPA {
  class ModuleExample : public Module
  {
    public:
      ModuleExample();
      virtual ~ModuleExample();

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

  typedef Id<ModuleExample> ModuleExampleId;  
}  


#endif /* #ifndef _H_ModuleExample */
