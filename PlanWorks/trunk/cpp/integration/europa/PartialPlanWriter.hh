//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.hh,v 1.21 2004-03-02 17:45:37 miatauro Exp $
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
  PartialPlanWriter(TokenNetwork *, String &);
  ~PartialPlanWriter(void);
  void notifyOfNewToken(TokenId);
  void notifyTokenIsInserted(TokenId); 
  void notifyTokenIsNotInserted(TokenId);
  void notifyAfterTokenIsNotInserted(TokenId);
  void notifyOfDeletedToken(TokenId);
  void notifyOfNewVariable(VarId);
  void notifySpecifiedDomainChanged(VarId);
  void notifySpecifiedDomainReset(VarId);
  void notifyDerivedDomainChanged(VarId);
  void notifyOfDeletedVariable(VarId);
  void notifyConstraintInserted(ConstraintId&);
  void notifyConstraintRemoved(ConstraintId&);
  void notifyFlushed(void);
  void write();
private:
  int nstep, tokenRelationId, transactionId, stepsPerWrite;
  int writeCounter, numTransactions, numTokens, numVariables, numConstraints, noWrite;
  long long int sequenceId;
  String dest;
  TokenNetwork *tnet;
  Value izero, rzero;
  List<Transaction> *transactionList;
  std::ofstream *transactionOut, *statsOut;
  ModelId modelId;
  void outputVariable(const VarId &, const char *, const long long int, const TokenId &, 
                      const Symbol &, std::ofstream &);
  void outputToken(const TokenId &, const bool, const long long int, const ObjectId *, 
                   const int,  const String &, const SlotId *, const int,
                   std::ofstream &, std::ofstream &, std::ofstream &);
  void outputConstraint(const ConstraintId &, const long long int, std::ofstream &, std::ofstream &);
  String getBoundString(const Domain &, const Value &);
  String getEnumString(const Domain &);
  const String getNameForConstraint(const ConstraintId &);
  const String getTemporalityForConstraint(const ConstraintId &);
  String getVarInfo(const VarId &);

};

#endif //PARTIALPLANWRITER_H
