#ifndef _H_TestSupport
#define _H_TestSupport


#include "ConstraintEngine.hh"
#include "DefaultPropagator.hh"
#include "ConstraintLibrary.hh"
#include "Constraints.hh"
#include "InstrumentationLogger.hh"

using namespace Prototype;

class DefaultEngineAccessor{
public:
  static const ConstraintEngineId& instance(){
    if (s_instance.isNoId()){
      s_instance = (new ConstraintEngine())->getId();
      new DefaultPropagator(LabelStr("Default"), s_instance);
#ifndef _EUROPA_FAST_VERSION_
      new InstrumentationLogger(s_instance);
#endif
    }

    return s_instance;
  }

  static void reset(){
    if(!s_instance.isNoId()){
      delete (ConstraintEngine*) s_instance;
      s_instance = ConstraintEngineId::noId();
    }
  }

private:
  static ConstraintEngineId s_instance;
};

#define ENGINE DefaultEngineAccessor::instance()

#define runTest(test) { \
  std::cout << "      " << #test; \
  int id_count = Europa::IdTable::size();\
  bool result = test(); \
  DefaultEngineAccessor::reset(); \
  if(result && Europa::IdTable::size() == id_count) \
    std::cout << " PASSED." << std::endl; \
  else if(result)\
    std::cout << " FAILED = DID NOT CLEAN UP ALLOCATED ID'S." << std::endl; \
  else \
    std::cout << " FAILED TO PASS UNIT TEST." << std::endl; \
}

#define runTestSuite(test) { \
  std::cout << #test << "***************" << std::endl; \
  if(test()) \
    std::cout << #test << " PASSED." << std::endl; \
  else \
    std::cout << #test << " FAILED." << std::endl; \
}

void initConstraintLibrary();

#endif
