#ifndef _H_ModuleLogistics
#define _H_ModuleLogistics

#include "Module.hh"

namespace EUROPA {
  class ModuleLogistics : public Module
  {
    public:
      ModuleLogistics();
      virtual ~ModuleLogistics();

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

  typedef Id<ModuleLogistics> ModuleLogisticsId;  
}  


#endif /* #ifndef _H_ModuleLogistics */
