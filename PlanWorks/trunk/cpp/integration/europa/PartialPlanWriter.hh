//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.hh,v 1.18 2004-01-02 21:16:35 miatauro Exp $
//

#ifndef PARTIALPLANWRITER_H
#define PARTIALPLANWRITER_H
#include "ConstraintNetwork.hh"
#include "List.hh"
#include "String.hh"
#include "Subscriber.hh"
#include "TokenNetwork.hh"
//#include <stl.h>
//#include <stdio.h>
#include <fstream>

using namespace Europa;

class Transaction {
public:

  //Transaction(enum transactionTypes type, int key, enum sourceTypes source, int id,
  //            long long int seqid) 

  Transaction(int type, int key, int source2, int id2, long long int seqid, int nstep, 
              const String &info2)
    : transactionType(type), objectKey(key), source(source2), id(id2),
      stepNum(nstep), sequenceId(seqid), info(info2) { }

  Transaction() : transactionType(-1), objectKey(-1), source(-1), id(-1),
                  sequenceId(-1) { }

  Transaction(const Transaction &other) {
    transactionType = other.transactionType;
    objectKey = other.objectKey;
    source = other.source;
    id = other.id;
    sequenceId = other.sequenceId;
    info = other.info;
  }

  void write(std::ostream &, long long int);

private:
  int transactionType, objectKey, source, id, stepNum;
  long long int sequenceId;
  String info;
};

class PartialPlanWriter : public Subscriber {
public:
  PartialPlanWriter(TokenNetwork *, String &);//  { this->tnet = ptnet; this->dest = pdest; this->nstep = 0; this->izero = Value(0); this->rzero = Value(0.); this->sequenceId = 0ll;};
  ~PartialPlanWriter(void);
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
  int nstep, tokenRelationId, /*enumeratedDomainId, intervalDomainId,*/ transactionId, stepsPerWrite;
  int writeCounter, numTransactions, numTokens, numVariables, numConstraints, noWrite;
  long long int sequenceId;
  String dest;
  TokenNetwork *tnet;
  Value izero, rzero;
  List<Transaction> *transactionList;
  //FILE *transactionOut, *statsOut;
  std::ofstream *transactionOut, *statsOut;
  ModelId modelId;
  void outputVariable(const VarId &, const char *, const long long int, const TokenId &, 
                      int paramId, std::ofstream &/*, std::ofstream &, std::ofstream &*/);
  void outputToken(const TokenId &, const bool, const long long int, const ObjectId *, 
                   const int,  const String &timelineName, const SlotId *, const int,
                   std::ofstream &, std::ofstream &, std::ofstream &);
  void outputPredicate(PredicateId &, const long long int partialPlanId, 
                       std::ofstream &, std::ofstream &);
  void outputConstraint(const ConstraintId &, const long long int, std::ofstream &, std::ofstream &);
  //void outputIntervalDomain(const Domain &, const long long int, std::ofstream &);
  //void outputEnumDomain(const Domain &, const long long int, std::ofstream &);
  String getBoundString(const Domain &, const Value &);
  String getEnumString(const Domain &);
  const String getNameForConstraint(const ConstraintId &);
  const String getTemporalityForConstraint(const ConstraintId &);
  String PartialPlanWriter::getVarInfo(const VarId &);

};

#endif //PARTIALPLANWRITER_H
