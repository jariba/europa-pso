//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.hh,v 1.5 2003-09-30 17:12:38 miatauro Exp $
//
#include "TokenNetwork.hh"
#include "ConstraintNetwork.hh"
#include "String.hh"
#include <stdio.h>

using namespace Europa;

class PartialPlanWriter {
public:
  PartialPlanWriter(TokenNetwork *ptnet, String &pdest)  { this->tnet = ptnet; this->dest = pdest; this->nstep = 0; this->izero = Value(0); this->rzero = Value(0.); this->sequenceId = 0ll;};
  void write();
private:
  int nstep;
  long long int sequenceId;
  int tokenRelationId;
  int enumeratedDomainId;
  int intervalDomainId;
  String dest;
  TokenNetwork *tnet;
  Value izero, rzero;
  void outputVariable(const VarId &, const char *, const long long int, FILE *, FILE *, 
                      FILE *);
  void outputToken(const TokenId &, const bool, const long long int, const ModelId *, const ObjectId *, 
                   const int,  const SlotId *, FILE *, FILE *, FILE *, FILE *, FILE *, FILE *);
  void outputPredicate(PredicateId &, const ModelId &, const long long int partialPlanId, 
                       FILE *, FILE *);
  void outputConstraint(const ConstraintId &, const long long int, FILE *, FILE *);
};
