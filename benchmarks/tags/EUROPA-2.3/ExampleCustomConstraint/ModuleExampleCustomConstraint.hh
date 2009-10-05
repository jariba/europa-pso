#ifndef _H_ModuleExampleCustomConstraint
#define _H_ModuleExampleCustomConstraint

#include "Module.hh"

namespace EUROPA {
  class ModuleExampleCustomConstraint : public Module
  {
    public:
      ModuleExampleCustomConstraint();
      virtual ~ModuleExampleCustomConstraint();

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

  typedef Id<ModuleExampleCustomConstraint> ModuleExampleCustomConstraintId;  
}  


#endif /* #ifndef _H_ModuleExampleCustomConstraint */
