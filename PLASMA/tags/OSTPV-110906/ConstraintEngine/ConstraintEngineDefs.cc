#include "ConstraintEngineDefs.hh"
#include "BoolTypeFactory.hh"
#include "IntervalIntTypeFactory.hh"
#include "IntervalTypeFactory.hh"
#include "StringTypeFactory.hh"
#include "SymbolTypeFactory.hh"
#include "EnumeratedTypeFactory.hh"

namespace EUROPA {

  static std::string & testLoadLibraryPath() {
    static std::string sl_testLoadLibraryPath("");
    return sl_testLoadLibraryPath;
  }
 
  void setTestLoadLibraryPath(std::string path) {
    testLoadLibraryPath() = path;
  }

  std::string getTestLoadLibraryPath() {
    return testLoadLibraryPath();
  }

  static bool & constraintEngineInitialized() {
    static bool sl_alreadyDone(false);
    return sl_alreadyDone;
  }

  void initConstraintEngine(){
    if(!constraintEngineInitialized()){
      constraintEngineInitialized() = true;

      /* Allocate Standard Type Factories */
      new BoolTypeFactory();
      new IntervalIntTypeFactory();
      new IntervalTypeFactory();
      new StringTypeFactory();
      new SymbolTypeFactory();
      new EnumeratedTypeFactory("REAL_ENUMERATION", "ELEMENT", EnumeratedDomain(true, "REAL_ENUMERATION"));
    }
  }

  void uninitConstraintEngine(){
    if(constraintEngineInitialized()){
      TypeFactory::purgeAll();
      constraintEngineInitialized() = false;
    }
  }

 
}
