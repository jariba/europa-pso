#ifndef _H_ModuleBedrest
#define _H_ModuleBedrest

#include "Module.hh"

namespace EUROPA {
  class ModuleBedrest : public Module
  {
    public:
      ModuleBedrest();
      virtual ~ModuleBedrest();

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

  typedef Id<ModuleBedrest> ModuleBedrestId;  
}  


#endif /* #ifndef _H_ModuleBedrest */
