//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.hh,v 1.10 2003-10-28 22:13:04 miatauro Exp $
//

#ifndef PARTIALPLANWRITER_H
#define PARTIALPLANWRITER_H
#include "ConstraintNetwork.hh"
#include "List.hh"
#include "String.hh"
#include "Subscriber.hh"
#include "TokenNetwork.hh"
#include <stdio.h>

using namespace Europa;

class Transaction {
public:
  //Transaction(enum transactionTypes type, int key, enum sourceTypes source, int id,
  //            long long int seqid) 
  Transaction(int type, int key, int source, int id, long long int seqid, int nstep)
  {transactionType = type; objectKey = key; this->source = source; this->id = id;
  sequenceId = seqid; stepNum = nstep;}
  Transaction() {transactionType = -1; objectKey = -1; source = -1; id = -1; sequenceId = -1;}
  Transaction(Transaction &other) {
    transactionType = other.transactionType;
    objectKey = other.objectKey;
    source = other.source;
    id = other.id;
    sequenceId = other.sequenceId;
  }
  void write(FILE *, long long int);
private:
  int transactionType;
  int objectKey;
  int source;
  int id;
  int stepNum;
  long long int sequenceId;
};

class PartialPlanWriter : public Subscriber {
public:
  PartialPlanWriter(TokenNetwork *, String &);//  { this->tnet = ptnet; this->dest = pdest; this->nstep = 0; this->izero = Value(0); this->rzero = Value(0.); this->sequenceId = 0ll;};
  void notifyOfNewToken(TokenId);
  void notifyTokenIsInserted(TokenId); //signals plan step
  void notifyTokenIsNotInserted(TokenId);
  void notifyAfterTokenIsNotInserted(TokenId);
  void notifyOfDeletedToken(TokenId);
  void notifyOfNewVariable(VarId);
  void notifySpecifiedDomainChanged(VarId); //signals plan step
  void notifySpecifiedDomainReset(VarId);
  void notifyDerivedDomainChanged(VarId);
  void notifyOfDeletedVariable(VarId);
  void notifyConstraintInserted(ConstraintId&);
  void notifyConstraintRemoved(ConstraintId&);
  void notifyFlushed(void);
  void write();
private:
  int nstep;
  long long int sequenceId;
  int tokenRelationId;
  int enumeratedDomainId;
  int intervalDomainId;
  int transactionId;
  int stepsPerWrite;
  int writeCounter;
  String dest;
  TokenNetwork *tnet;
  Value izero, rzero;
  List<Transaction> *transactionList;
  void outputVariable(const VarId &, const char *, const long long int, const TokenId &, FILE *,
                      FILE *, FILE *);
  void outputToken(const TokenId &, const bool, const long long int, const ModelId *, const ObjectId *, 
                   const int,  const SlotId *, FILE *, FILE *, FILE *, FILE *, FILE *, FILE *);
  void outputPredicate(PredicateId &, const ModelId &, const long long int partialPlanId, 
                       FILE *, FILE *);
  void outputConstraint(const ConstraintId &, const long long int, FILE *, FILE *);
};

#endif //PARTIALPLANWRITER_H
