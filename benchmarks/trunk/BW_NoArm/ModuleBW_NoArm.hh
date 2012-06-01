#ifndef _H_ModuleBW_NoArm
#define _H_ModuleBW_NoArm

#include "Module.hh"

namespace EUROPA {
  class ModuleBW_NoArm : public Module
  {
    public:
      ModuleBW_NoArm();
      virtual ~ModuleBW_NoArm();

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

  typedef Id<ModuleBW_NoArm> ModuleBW_NoArmId;  
}  


#endif /* #ifndef _H_ModuleBW_NoArm */
