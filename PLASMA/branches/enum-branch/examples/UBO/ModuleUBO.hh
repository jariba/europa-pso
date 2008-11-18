#ifndef _H_ModuleUBO
#define _H_ModuleUBO

#include "Module.hh"

namespace EUROPA {
  class ModuleUBO : public Module
  {
    public:
      ModuleUBO();
      virtual ~ModuleUBO();

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

  typedef Id<ModuleUBO> ModuleUBOId;  
}  


#endif /* #ifndef _H_ModuleUBO */
