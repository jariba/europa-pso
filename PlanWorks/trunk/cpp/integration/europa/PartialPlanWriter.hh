#include "TokenNetwork.hh"
#include "ConstraintNetwork.hh"
#include "String.hh"
#include <stdio.h>

using namespace Europa;

class PartialPlanWriter {
public:
  PartialPlanWriter(TokenNetwork *ptnet, String &pdest)  { this->tnet = ptnet; this->dest = pdest; this->nstep = 0; this->izero = Value(0); this->rzero = Value(0.);};
  void write();
private:
  int nstep;
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
