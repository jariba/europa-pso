#ifndef _H_ModuleResourceViolations
#define _H_ModuleResourceViolations

#include "Module.hh"

namespace EUROPA {
  class ModuleResourceViolations : public Module
  {
    public:
      ModuleResourceViolations();
      virtual ~ModuleResourceViolations();

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

  typedef Id<ModuleResourceViolations> ModuleResourceViolationsId;  
}  


#endif /* #ifndef _H_ModuleResourceViolations */
