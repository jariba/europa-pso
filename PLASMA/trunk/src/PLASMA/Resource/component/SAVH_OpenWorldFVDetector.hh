#ifndef _H_SAVH_OpenWorldFVDetector
#define _H_SAVH_OpenWorldFVDetector

#include "SAVH_GenericFVDetector.hh"
namespace EUROPA {
  namespace SAVH {

    class OpenWorldFVDetector : public GenericFVDetector {
  public:
   OpenWorldFVDetector(const ResourceId res);
  protected:
   Resource::ProblemType getResourceLevelViolation(const InstantId inst) const;
    void handleResourceLevelFlaws(const InstantId inst);
    };
  }
}

#endif
