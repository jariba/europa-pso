//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.cc,v 1.26 2004-01-02 21:12:27 miatauro Exp $
//
#include <cstring>
#include <string>
#include <errno.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include "PartialPlanWriter.hh"
#include "Id.hh"
#include "Token.hh"
#include "Constraint.hh"
#include "ConstraintNetwork.hh"
#include "Object.hh"
#include "Timeline.hh"
#include "Slot.hh"
#include "Variable.hh"
#include "Domain.hh"
#include "ModelId.hh"
#include "List.hh"
#include "error.hh"
#include "PConstraint.hh"

using namespace std;
using namespace Europa;

const char *envStepsPerWrite = "PPW_WRITE_NSTEPS";

const char *envAltWriteDest = "PPW_WRITE_DEST";

const char *envPPWNoWrite = "PPW_DONT_WRITE";

const char *transactionTypeNames[13] = {"TOKEN_CREATED", "TOKEN_DELETED", "TOKEN_INSERTED",
                                        "TOKEN_FREED", "VARIABLE_CREATED", "VARIABLE_DELETED",
                                        "VARIABLE_DOMAIN_RELAXED", "VARIABLE_DOMAIN_RESTRICTED",
                                        "VARIABLE_DOMAIN_SPECIFIED", "VARIABLE_DOMAIN_RESET",
                                        "VARIABLE_DOMAIN_EMPTIED", "CONSTRAINT_CREATED",
                                        "CONSTRAINT_DELETED"};
enum transactionTypes {TOKEN_CREATED = 0, TOKEN_DELETED, TOKEN_INSERTED, TOKEN_FREED,
                             VAR_CREATED, VAR_DELETED, VAR_DOMAIN_RELAXED, VAR_DOMAIN_RESTRICTED,
                             VAR_DOMAIN_SPECIFIED, VAR_DOMAIN_RESET, VAR_DOMAIN_EMPTIED,
                             CONSTRAINT_CREATED, CONSTRAINT_DELETED, ERROR};

const char *sourceTypeNames[3] = {"SYSTEM", "USER", "UNKNOWN"};

#define TAB "\t"
const String SNULL("\\N");
const String CONSTRAINT_TOKEN("constraintToken");
const String COMMA(",");
const String SLASH("/");
const String SPACE(" ");
const String PARAMETER_VAR("PARAMETER_VAR");
const String DURATION_VAR("DURATION_VAR");
const String END_VAR("END_VAR");
const String START_VAR("START_VAR");
const String REJECT_VAR("REJECT_VAR");
const String OBJECT_VAR("OBJECT_VAR");
const String TEMPORAL("TEMPORAL");
const String ATEMPORAL("ATEMPORAL");
const String VAR_TEMP_CONSTR("variableTempConstr");
const String UNARY_TEMP_CONSTR("unaryTempConstr");
const String FIXED_TEMP_CONSTR("fixedTempConstr");
const String UNARY_CONSTR("unaryConstr");
const String EQ_CONSTR("equalityConstr");
const String BUG_CONSTR("bugConstr");
const String STRUE("true");
const String SFALSE("false");
const String PINFINITY("Infinity");
const String NINFINITY("-Infinity");
const String INTEGER_SORT("INTEGER_SORT");
const String REAL_SORT("REAL_SORT");
const String STEP("step");
const String PARTIAL_PLAN_STATS("/partialPlanStats");
const String TRANSACTIONS("/transactions");
const String SEQUENCE("/sequence");
const String PARTIAL_PLAN(".partialPlan");
const String OBJECTS(".objects");
//const String TIMELINES(".timelines");
//const String SLOTS(".slots");
const String TOKENS(".tokens");
//const String PARAM_VAR_TOKEN_MAP(".paramVarTokenMap");
const String TOKEN_RELATIONS(".tokenRelations");
const String VARIABLES(".variables");
//const String ENUMERATED_DOMAINS(".enumeratedDomains");
//const String INTERVAL_DOMAINS(".intervalDomains");
const String CONSTRAINTS(".constraints");
const String CONSTRAINT_VAR_MAP(".constraintVarMap");
const String PREDICATES(".predicates");
const String PARAMETERS(".parameters");
const String E_DOMAIN("E");
const String I_DOMAIN("I");

enum sourceTypes {SYSTEM = 0, USER, UNKNOWN};

#define FatalError(s) handleError(generalUnknownError, (s), fatalError,);

PartialPlanWriter::PartialPlanWriter(TokenNetwork *ptnet, String &pdest) { 
  tnet = ptnet; 
  dest = pdest; 
  nstep = 0;
  izero = Value(0);
  rzero = Value(0.);
  struct timeval currTime;
  if(gettimeofday(&currTime, NULL)) {
    FatalError("Failed to get current time.");
  }
  sequenceId = (((long long int)currTime.tv_sec) * 1000) + (currTime.tv_usec / 1000);
  transactionId = 0;
  transactionList = new List<Transaction>();
  writeCounter = 0;
  numTransactions = 0;

  char *spw = getenv(envStepsPerWrite);
  if(spw == NULL) {
    stepsPerWrite = 0;
  }
  else {
    stepsPerWrite = (int) strtol(spw, (char **) NULL, 10);
    if((stepsPerWrite == 0 && errno == EINVAL) || stepsPerWrite == INT_MAX) {
      FatalError(strerror(errno));
    }
  }
  char *altDest = getenv(envAltWriteDest);
  if(altDest != NULL) {
    dest = String(altDest);
  }

  char *dontWrite = getenv(envPPWNoWrite);
  if(dontWrite == NULL) {
    noWrite = 0;
  }
  else {
    noWrite = (int) strtol(dontWrite, (char **) NULL, 10);
  }

  char *destBuf = new char[PATH_MAX];
  if(realpath(dest.chars(), destBuf) == NULL && stepsPerWrite != 0) {
    if(mkdir(dest.chars(), 0777) && errno != EEXIST) {
      cerr << "Failed to make directory " << dest << endl;
      FatalError(strerror(errno));
    }
  }
  dest = destBuf;
  delete [] destBuf;

  if (dest.getChar(dest.getLength()) != '/')
    dest += "/";

  char timestr[NBBY * sizeof(sequenceId) * 28/93 + 4];
  sprintf(timestr, "%lld", sequenceId); 
  modelId = tnet->getModelId();
  std::string modelName = modelId.getModelName().chars();
  {
    std::string::size_type tempIndex = modelName.rfind('/');
    if (tempIndex > 0 && tempIndex < modelName.length())
      modelName = modelName.substr(tempIndex);
  }
  std::string seqname = modelName;
  std::string::size_type extStart = seqname.find('.');
  seqname = seqname.substr(0, extStart);
  if (stepsPerWrite) {
    if (mkdir(dest.chars(), 0777) && errno != EEXIST) {
      cerr << "Failed to make directory " << dest << endl;
      FatalError(strerror(errno));
    }
    dest += seqname.data();
    dest += timestr;
    if (mkdir(dest.chars(), 0777) && errno != EEXIST) {
      cerr << "Failed to make directory " << dest << endl;
      FatalError(strerror(errno));
    }
    String ppStats = dest + PARTIAL_PLAN_STATS;
    String ppTransactions = dest + TRANSACTIONS;
    String sequenceStr = dest + SEQUENCE;
    ofstream sequenceOut(sequenceStr.chars());
    if(!sequenceOut) {
      cerr << "Failed to open " << sequenceStr << endl;
      FatalError(strerror(errno));
    }
    sequenceOut << dest.chars() << TAB << sequenceId << endl;
    sequenceOut.close();
    
    transactionOut = new ofstream(ppTransactions.chars());
    if(!(*transactionOut)) {
      FatalError(strerror(errno));
    }
    statsOut = new ofstream(ppStats.chars());
    if(!(*statsOut)) {
      FatalError(strerror(errno));
    }
  }
};

PartialPlanWriter::~PartialPlanWriter(void) {
  if(stepsPerWrite) {
    transactionOut->close();
    statsOut->close();
  }
}

void PartialPlanWriter::write(void) {
  struct timeval currTime;
  long long int partialPlanId;
  numTokens = numVariables = numConstraints = 0;
  if(!stepsPerWrite) {
    return;
  }

  tokenRelationId = 1;
  if(gettimeofday(&currTime, NULL)) {
    FatalError("Failed to get current time");
  }
  partialPlanId = (((long long int)currTime.tv_sec) * 1000) + (currTime.tv_usec / 1000);

  String stepnum = STEP + String(nstep);

  String partialPlanDest = dest + SLASH + stepnum;
  if(mkdir(partialPlanDest.chars(), 0777) && errno != EEXIST) {
    FatalError(strerror(errno));
  }

  String ppPartialPlan = partialPlanDest + SLASH + stepnum + PARTIAL_PLAN;
  ofstream partialPlanOut(ppPartialPlan.chars());
  if(!partialPlanOut) {
    FatalError(strerror(errno));
  }
  partialPlanOut << stepnum << TAB << partialPlanId << TAB << tnet->getModelId().getModelName()
                 << TAB << sequenceId << endl;
  partialPlanOut.close();

  String ppObject = partialPlanDest + SLASH + stepnum + OBJECTS;
  ofstream objectOut(ppObject.chars());
  if(!objectOut) {
    FatalError(strerror(errno));
  }

  String ppToken = partialPlanDest + SLASH + stepnum + TOKENS;
  ofstream tokenOut(ppToken.chars());
  if(!tokenOut) {
    FatalError(strerror(errno));
  }

  String ppTokenRelation = partialPlanDest + SLASH + stepnum + TOKEN_RELATIONS;
  ofstream tokenRelationOut(ppTokenRelation.chars());
  if(!tokenRelationOut) {
    FatalError(strerror(errno));
  }

  String ppVariables = partialPlanDest + SLASH + stepnum + VARIABLES;
  ofstream variableOut(ppVariables.chars());
  if(!variableOut) {
    FatalError(strerror(errno));
  }

  String ppConstraints = partialPlanDest + SLASH + stepnum + CONSTRAINTS;
  ofstream constraintOut(ppConstraints.chars());
  if(!constraintOut) {
    FatalError(strerror(errno));
  }

  String ppCVM = partialPlanDest + SLASH + stepnum + CONSTRAINT_VAR_MAP;
  ofstream constraintVarMapOut(ppCVM.chars());
  if(!constraintVarMapOut) {
    FatalError(strerror(errno));
  }

  String ppPredicates = partialPlanDest + SLASH + stepnum + PREDICATES;
  ofstream predOut(ppPredicates.chars());
  if(!predOut) {
    FatalError(strerror(errno));
  }

  String ppParameters = partialPlanDest + SLASH + stepnum + PARAMETERS;
  ofstream paramOut(ppParameters.chars());
  if(!paramOut) {
    FatalError(strerror(errno));
  }

  /*List<VarId> globalVars = tnet->getGlobalVars();
  ListIterator<VarId> globalVarIterator = ListIterator<VarId>(globalVars);
  while(!globalVarIterator.isDone()) {
    outputVariable(globalVarIterator.item(), "GLOBAL_VAR", partialPlanId, variableOut, intDomainOut,
                   enumDomainOut);
    globalVarIterator.step();
    }*/

  List<TokenId> freeTokenList = tnet->getFreeValueTokensWithoutCompatUpdate();

  ListIterator<TokenId> freeTokenIterator = ListIterator<TokenId>(freeTokenList);
  while(!freeTokenIterator.isDone()) {
    TokenId tokenId = freeTokenIterator.item();

    outputToken(tokenId, true, partialPlanId, NULL, 0, SNULL, NULL, -1, tokenOut, 
                tokenRelationOut, 
                variableOut);
    numTokens++;
    freeTokenIterator.step();
  }
  List<ModelClassId> modelClassList = modelId.getAllModelClasses();
  ListIterator<ModelClassId> modelClassIterator = ListIterator<ModelClassId>(modelClassList);
  while(!modelClassIterator.isDone()) {
    ModelClassId modelClass = modelClassIterator.item();
    List<AttributeId> classAttributes = modelId.getAttributes(modelClass);
    ListIterator<AttributeId> attributeIterator = ListIterator<AttributeId>(classAttributes);
    while(!attributeIterator.isDone()) {
      AttributeId classAttribute = attributeIterator.item();
      List<PredicateId> predicates = modelId.getAttributePredicates(modelClass, classAttribute);
      ListIterator<PredicateId> predicateIterator = ListIterator<PredicateId>(predicates);
      while(!predicateIterator.isDone()) {
        PredicateId predicate = predicateIterator.item();
        outputPredicate(predicate, partialPlanId, predOut, paramOut);
        predicateIterator.step();
      }
      attributeIterator.step();
    }
    modelClassIterator.step();
  }
  List<ObjectId> objectList = tnet->getAllObjects();
  ListIterator<ObjectId> objectIterator = ListIterator<ObjectId>(objectList);
  int timelineId = 0;
  while(!objectIterator.isDone()) {
    ObjectId objectId = objectIterator.item();
    objectOut << objectId->getKey() << TAB << partialPlanId << TAB << objectId->getName() << endl;
    List<AttributeId> timelineNames = tnet->getAttributes(objectId);
    ListIterator<AttributeId> timelineNameIterator = ListIterator<AttributeId>(timelineNames);

    while(!timelineNameIterator.isDone()) {
      AttributeId timelineAttId = timelineNameIterator.item();
      List<SlotInfo> slotList = tnet->getAllSlots(objectId, timelineAttId);
      ListIterator<SlotInfo> slotIterator = ListIterator<SlotInfo>(slotList);
      while(!slotIterator.isDone()) {
        if(!slotIterator.item().getId()->isSlotEmpty()) {
          break;
        }
        slotIterator.step();
      }
      int slotIndex = 0;
      while(!slotIterator.isDone()) {
        SlotId slotId = slotIterator.item().getId();
        if(slotId->isSlotEmpty() && slotId->getNextSlotId() == SlotId::noId()) {
          slotIterator.step();
          continue;
        }
        List<TokenId> tokenList = slotId->listValueTokensCoveringSlot();
        ListIterator<TokenId> tokenIterator = ListIterator<TokenId>(tokenList);
        while(!tokenIterator.isDone()) {
          TokenId tokenId = tokenIterator.item();
          outputToken(tokenId, false, partialPlanId, &objectId, timelineId, 
                      modelId.getAttributeName(timelineAttId), &slotId, slotIndex,
                      tokenOut, tokenRelationOut, variableOut);
          numTokens++;
          tokenIterator.step();
        }
        slotIndex++;
        slotIterator.step();
      }
      timelineId++;
      timelineNameIterator.step();
    }
    objectIterator.step();
  }

  List<ConstraintId> constraints = tnet->getConstraints();
  numConstraints = constraints.getSize();
  ListIterator<ConstraintId> constraintIterator = ListIterator<ConstraintId>(constraints);

  while(!constraintIterator.isDone()) {
    ConstraintId constraintId = constraintIterator.item();
    outputConstraint(constraintId, partialPlanId, constraintOut, constraintVarMapOut);
    constraintIterator.step();
  }

  (*statsOut) << sequenceId << TAB << partialPlanId << TAB << nstep << TAB << numTokens << TAB 
           << numVariables << TAB << numConstraints << TAB << numTransactions << endl;
  statsOut->flush();
  ListIterator<Transaction> transactionIterator = ListIterator<Transaction>(*transactionList);
  while(!transactionIterator.isDone()) {
    Transaction &transaction = (Transaction &) transactionIterator.item();
    transaction.write((*transactionOut), partialPlanId);
    transactionIterator.step();
  }
  objectOut.close();
  tokenOut.close();
  tokenRelationOut.close();
  variableOut.close();
  constraintOut.close();
  predOut.close();
  paramOut.close();
  constraintVarMapOut.close();
  nstep++;
}

void PartialPlanWriter::outputPredicate(PredicateId &predicate, const long long int partialPlanId,
                                        ofstream &predOut, ofstream &paramOut) {
  predOut << predicate.getKey() << TAB << modelId.getPredicateName(predicate) << TAB 
          << partialPlanId << endl;
  Vector<Symbol> params = modelId.getPredicateArgumentNames(predicate);
  VectorIterator<Symbol> paramIterator = VectorIterator<Symbol>(params);
  int paramIndex = 0;
  while(!paramIterator.isDone()) {
    Symbol parameter = paramIterator.item();
    paramOut << paramIndex++ << TAB << predicate.getKey() << TAB << partialPlanId << TAB 
             << parameter << endl;
    paramIterator.step();
  }
}

void PartialPlanWriter::outputToken(const TokenId &tokenId, const bool isFree, 
                                    const long long int partialPlanId, const ObjectId *objectId,
                                    const int timelineId, const String &timelineName, 
                                    const SlotId *slotId, const int slotIndex,
                                    ofstream &tokenOut, ofstream &tokenRelationOut, 
                                    ofstream &variableOut) {
  PredicateId predicateId = tokenId->getPredicate();
  String tokenRelationIds("");
  String paramVarIds("");
  if(isFree) {
    tokenOut << tokenId->getKey() << TAB << SNULL << TAB << SNULL << TAB << partialPlanId << TAB 
             << 1 << TAB << 1
             << TAB << tokenId->getStartVariable()->getKey() << TAB
             << tokenId->getEndVariable()->getKey() << TAB
             << tokenId->getDurationVariable()->getKey() << TAB
             << tokenId->getRejectVariable()->getKey() << TAB
             << predicateId.getKey() << TAB << SNULL << TAB << SNULL << TAB << SNULL << TAB
             << tokenId->getObjectVariable()->getKey() << TAB;
  }
  else {
    tokenOut << tokenId->getKey() << TAB << (*slotId)->getKey() << TAB  << slotIndex << TAB
             << partialPlanId << TAB 
             << 0 << TAB << 1 << TAB << tokenId->getStartVariable()->getKey() << TAB
             << tokenId->getEndVariable()->getKey() << TAB
             << tokenId->getDurationVariable()->getKey() << TAB
             << tokenId->getRejectVariable()->getKey() << TAB
             << predicateId.getKey() << TAB << timelineId << TAB << timelineName << TAB
             << (*objectId)->getKey() << TAB
             << tokenId->getObjectVariable()->getKey() << TAB;
  }
  if(tokenId->getMasterToken().isValid()) {
    tokenRelationOut << partialPlanId << TAB << tokenId->getMasterToken()->getKey() << TAB
                     << tokenId->getKey() << TAB << "CAUSAL" << TAB << tokenRelationId << endl;
    tokenRelationIds += String(tokenRelationId) + String(":");
    tokenRelationId++;
  }
  if(tokenRelationIds == String("")) {
    tokenOut << SNULL << TAB;
  }
  else {
    tokenOut << tokenRelationIds << TAB;
  }
  outputVariable(tokenId->getStartVariable(), "START_VAR", partialPlanId, tokenId, -1,
                 variableOut/*, intDomainOut, enumDomainOut*/);
  outputVariable(tokenId->getEndVariable(), "END_VAR", partialPlanId, tokenId, -1, variableOut
                 /*,intDomainOut, enumDomainOut*/);
  outputVariable(tokenId->getDurationVariable(), "DURATION_VAR", partialPlanId, tokenId, -1,
                 variableOut/*, intDomainOut, enumDomainOut*/);
  outputVariable(tokenId->getRejectVariable(), "REJECT_VAR", partialPlanId, tokenId, -1,
                 variableOut/*, intDomainOut, enumDomainOut*/);
  outputVariable(tokenId->getObjectVariable(), "OBJECT_VAR", partialPlanId, tokenId, -1,
                 variableOut/*, intDomainOut, enumDomainOut*/);
  List<VarId> paramVarList = tokenId->getParameterVariables();
  ListIterator<VarId> paramVarIterator = ListIterator<VarId>(paramVarList);
  int paramIndex = 0;
  
  while(!paramVarIterator.isDone()) {
    VarId variableId = paramVarIterator.item();
    outputVariable(variableId, "PARAMETER_VAR", partialPlanId, tokenId, paramIndex, variableOut
                   /*,intDomainOut, enumDomainOut*/);
    //paramVarTokenMapOut << variableId->getKey() << TAB << tokenId->getKey() << TAB << paramIndex
    //                    << TAB << partialPlanId << endl;
    paramVarIds += String(variableId->getKey()) + String(":");
    paramIndex++;
    paramVarIterator.step();
  }
  if(paramVarIds == String("")) {
    tokenOut << SNULL << endl;
  }
  else {
    tokenOut << paramVarIds << endl;
  }
}

void PartialPlanWriter::outputVariable(const VarId &variable, const char *type, 
                                       const long long int partialPlanId, const TokenId &tokenId,
                                       int paramId, ofstream &variableOut
                                       /*, ofstream &intervalDomainOut,
                                         ofstream &enumeratedDomainOut*/) {
  numVariables++;
  Domain domain = variable->getCurrentDomain();
  variableOut << variable->getKey() << TAB << partialPlanId << TAB << tokenId->getKey() << TAB 
              << paramId << TAB;
  if(domain.isDynamic() || domain.isEnumerated()) {
    variableOut << "EnumeratedDomain" << TAB << getEnumString(domain) << TAB << SNULL << TAB
                << SNULL << TAB << SNULL << TAB;
    //outputEnumDomain(domain, partialPlanId, enumeratedDomainOut);
  }
  else if (domain.isInterval()) {
    variableOut << "IntervalDomain" << TAB << SNULL << TAB;
    String upperBoundStr = getBoundString(domain, ((Domain &)domain).getUpperBound());
    String lowerBoundStr = getBoundString(domain, ((Domain &)domain).getLowerBound());
    SortId sortId = domain.getSort();
    String sort;
    if(sortId.isInt()) {
      sort = INTEGER_SORT;
    }
    else if(sortId.isReal()) {
      sort = REAL_SORT;
    }
    variableOut << sort << TAB << lowerBoundStr << TAB << upperBoundStr << TAB;
    //outputIntervalDomain(domain, partialPlanId, intervalDomainOut);
  }
  variableOut << type << endl;
}

// void PartialPlanWriter::outputIntervalDomain(const Domain &domain, 
//                                              const long long int partialPlanId, 
//                                              ofstream &intervalDomainOut) {
//   String upperBoundStr = getBoundString(domain, ((Domain &)domain).getUpperBound());
//   String lowerBoundStr = getBoundString(domain, ((Domain &)domain).getLowerBound());

//   SortId sortId = domain.getSort();
//   String sort;
//   if(sortId.isInt()) {
//     sort = INTEGER_SORT;
//   }
//   else if(sortId.isReal()) {
//     sort = REAL_SORT;
//   }
//   intervalDomainOut << intervalDomainId << TAB << partialPlanId << TAB << lowerBoundStr << TAB
//                     << upperBoundStr << TAB << sort << endl;
//   intervalDomainId++;
// }

String PartialPlanWriter::getBoundString(const Domain &domain, const Value &bound) {
  String retval("");
  if(bound.isObject()) {
    retval += String(bound.getObjectValue()->getKey());
  }
  else if(bound.isLabel()) {
    retval += domain.getSort().getMemberName(bound);
  }
  else if(bound.isBool()) {
    if(bound.getBoolValue()) {
      retval += STRUE;
    }
    else {
      retval += SFALSE;
    }
  }
  else if(bound.isReal()) {
    if(bound.isInfinite()) {
      if(bound < rzero) {
        retval += NINFINITY;
      }
      else {
        retval += PINFINITY;
      }
    }
    else {
      retval += String(bound.getRealValue());
    }
  }
  else if(bound.isInt()) {
    if(bound.isInfinite()) {
      if(bound < izero) {
        retval += NINFINITY;
      }
      else {
        retval += PINFINITY;
      }
    }
    else {
      String ubs = String(bound.getIntValue());
      retval += ubs;
    }
  }
  return retval;
}

// void PartialPlanWriter::outputEnumDomain(const Domain &domain, const long long int partialPlanId,
//                                          ofstream &enumeratedDomainOut) {
//   String enumStr = getEnumString(domain);
//   enumeratedDomainOut << enumeratedDomainId << TAB << partialPlanId << TAB << enumStr << endl;
//   enumeratedDomainId++;
// }


String PartialPlanWriter::getEnumString(const Domain &domain) {
  Set<Value> enumeration;
  if(((Domain &)domain).isDynamic()) {
    enumeration = ((Domain &)domain).getSort().getCurrentMembers();
  }
  else if(((Domain &)domain).isEnumerated()) {
    enumeration = ((Domain &)domain).getMembers();
  }
  SetIterator<Value> enumIterator = SetIterator<Value>(enumeration);
  String retval("");
  while(!enumIterator.isDone()) {
    Value value = enumIterator.item();
    if(value.isObject()) {
      retval += value.getObjectValue()->getName();
    }
    else if(value.isLabel()) {
      retval += domain.getSort().getMemberName(value);
    }
    else if(value.isBool()) {
      if(value.getBoolValue()) {
        retval += STRUE;
      }
      else {
        retval += SFALSE;
      }
    }
    else if(value.isReal()) {
      retval += String(value.getRealValue());
    }
    else if(value.isInt()) {
      retval += String(value.getIntValue());
    }
    retval += SPACE;
    enumIterator.step();
  }
  return retval;
}

void PartialPlanWriter::outputConstraint(const ConstraintId &constraintId, 
                                         const long long int partialPlanId, ofstream &constraintOut, 
                                         ofstream &constraintVarMapOut) {
  String temporality, name;
  name = getNameForConstraint(constraintId);
  temporality = getTemporalityForConstraint(constraintId);
  constraintOut << constraintId->getKey() << TAB << partialPlanId << TAB << name << TAB 
                << temporality << endl;
  List<VarId> constrainedVars = tnet->getConstraintScope(constraintId);
  ListIterator<VarId> varIterator = ListIterator<VarId>(constrainedVars);
  while(!varIterator.isDone()) {
    constraintVarMapOut << constraintId->getKey() << TAB << varIterator.item()->getKey() << TAB
                        << partialPlanId << endl;
    varIterator.step();
  }
}

const String PartialPlanWriter::getNameForConstraint(const ConstraintId &constraintId) {
  String retval("");
  if(tnet->isTemporalVariableConstraint(constraintId)) {
    retval = VAR_TEMP_CONSTR;
  }
  else if(tnet->isTemporalBoundConstraint(constraintId)) {
    retval = UNARY_TEMP_CONSTR;
  }
  else if(tnet->isTemporalRelationConstraint(constraintId)) {
    retval = FIXED_TEMP_CONSTR;
  }
  else if(tnet->isArgumentBoundConstraint(constraintId)) {
    retval = UNARY_CONSTR;
  }
  else if(tnet->isArgumentEqualsConstraint(constraintId)) {
    retval = EQ_CONSTR;
  }
  else if(tnet->isExpertConstraint(constraintId)) {
    retval = tnet->getExpertConstraintName(constraintId);
  }
  else {
    retval = BUG_CONSTR;
  }
  if(retval.chars() == NULL) {
    retval = BUG_CONSTR;
  }
  return retval;
}

const String PartialPlanWriter::getTemporalityForConstraint(const ConstraintId &constraintId) {
  String retval("");
  if(tnet->isTemporalVariableConstraint(constraintId) || 
     tnet->isTemporalBoundConstraint(constraintId) || 
     tnet->isTemporalRelationConstraint(constraintId) ) {
    retval = TEMPORAL;
  }
  else {
    retval = ATEMPORAL;
  }
  return retval;
}

void PartialPlanWriter::notifyOfNewToken(TokenId tokenId) {
  if(stepsPerWrite) {
    String info;
    info = (tokenId->getTokenClass() == valueTokenClass ? tokenId->getPredicate()->getName() :
            CONSTRAINT_TOKEN);
    transactionList->append(Transaction(TOKEN_CREATED, tokenId->getKey(), UNKNOWN, 
                                        transactionId++, sequenceId, nstep, info));
    numTransactions++;
  }
}
void PartialPlanWriter::notifyTokenIsInserted(TokenId tokenId) {
  if(stepsPerWrite) {
    String info;
    info = (tokenId->getTokenClass() == valueTokenClass ? tokenId->getPredicate()->getName() :
            CONSTRAINT_TOKEN);
    transactionList->append(Transaction(TOKEN_INSERTED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
  }
}

void PartialPlanWriter::notifyTokenIsNotInserted(TokenId tokenId) {
  if(stepsPerWrite) {
    String info;
    info = (tokenId->getTokenClass() == valueTokenClass ? tokenId->getPredicate()->getName() :
            CONSTRAINT_TOKEN);
    transactionList->append(Transaction(TOKEN_FREED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
  }
}

void PartialPlanWriter::notifyAfterTokenIsNotInserted(TokenId /* tokenId */ ) {
}

void PartialPlanWriter::notifyOfDeletedToken(TokenId tokenId) {
  if(stepsPerWrite) {
    String info;
    info = (tokenId->getTokenClass() == valueTokenClass ? tokenId->getPredicate()->getName() :
            CONSTRAINT_TOKEN);
    transactionList->append(Transaction(TOKEN_DELETED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
    if(tokenId->getTokenClass() == valueTokenClass) {
      transactionList->append(Transaction(VAR_DELETED, tokenId->getStartVariable()->getKey(),
                                          UNKNOWN, transactionId++, sequenceId, nstep, 
                                          getVarInfo(tokenId->getStartVariable())));
      numTransactions++;
      transactionList->append(Transaction(VAR_DELETED, tokenId->getEndVariable()->getKey(),
                                          UNKNOWN, transactionId++, sequenceId, nstep, 
                                          getVarInfo(tokenId->getEndVariable())));
      numTransactions++;
      transactionList->append(Transaction(VAR_DELETED, tokenId->getObjectVariable()->getKey(),
                                          UNKNOWN, transactionId++, sequenceId, nstep, 
                                          getVarInfo(tokenId->getObjectVariable())));
      numTransactions++;
      transactionList->append(Transaction(VAR_DELETED, tokenId->getDurationVariable()->getKey(),
                                          UNKNOWN, transactionId++, sequenceId, nstep, 
                                          getVarInfo(tokenId->getDurationVariable())));
      numTransactions++;
      transactionList->append(Transaction(VAR_DELETED, tokenId->getRejectVariable()->getKey(),
                                          UNKNOWN, transactionId++, sequenceId, nstep, 
                                          getVarInfo(tokenId->getRejectVariable())));
      numTransactions++;
      List<VarId> paramVars = tokenId->getParameterVariables();
      ListIterator<VarId> paramVarIterator(paramVars);
      while(!paramVarIterator.isDone()) {
        transactionList->append(Transaction(VAR_DELETED, paramVarIterator.item()->getKey(),
                                            UNKNOWN, transactionId++, sequenceId, nstep, 
                                            getVarInfo(paramVarIterator.item())));
        paramVarIterator.step();
        numTransactions++;
      }
    }
  }
}
void PartialPlanWriter::notifyOfNewVariable(VarId varId) {
  if(stepsPerWrite) {
    if(varId->getParentToken().isNoId()) {
      return;
    }
    transactionList->append(Transaction(VAR_CREATED, varId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, getVarInfo(varId)));
    numTransactions++;
  }
}
void PartialPlanWriter::notifySpecifiedDomainChanged(VarId varId) {
  if(stepsPerWrite) {
    if(varId->getParentToken().isNoId()) {
      return;
    }
    transactionList->append(Transaction(VAR_DOMAIN_SPECIFIED, varId->getKey(), UNKNOWN,
                                        transactionId++, sequenceId, nstep, getVarInfo(varId)));
    numTransactions++;
  }
}
void PartialPlanWriter::notifySpecifiedDomainReset(VarId varId) {
  if(stepsPerWrite) {
    if(varId->getParentToken().isNoId()) {
      return;
    }
    transactionList->append(Transaction(VAR_DOMAIN_RESET, varId->getKey(), USER, transactionId++, 
					sequenceId, nstep, getVarInfo(varId)));
    numTransactions++;
  }
}
void PartialPlanWriter::notifyDerivedDomainChanged(VarId varId) {
  if(stepsPerWrite) {
    if(varId->getParentToken().isNoId()) {
      return;
    }
    transactionList->append(Transaction(VAR_DOMAIN_RESTRICTED, varId->getKey(), UNKNOWN, 
                                        transactionId++, sequenceId, nstep, getVarInfo(varId)));
    numTransactions++;
  }
}

void PartialPlanWriter::notifyOfDeletedVariable(VarId varId) {
  if(stepsPerWrite) {
    if(varId->getParentToken().isNoId()) {
      return;
    }
    transactionList->append(Transaction(VAR_DELETED, varId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, getVarInfo(varId)));
    numTransactions++;
  }
}

void PartialPlanWriter::notifyConstraintInserted(ConstraintId& constrId) {
  if(stepsPerWrite) {
    transactionList->append(Transaction(CONSTRAINT_CREATED, constrId->getKey(), UNKNOWN, 
                                        transactionId++, sequenceId, nstep, 
                                        getNameForConstraint(constrId)));
    numTransactions++;
  }
}
void PartialPlanWriter::notifyConstraintRemoved(ConstraintId& constrId) {
  if(stepsPerWrite) {
    transactionList->append(Transaction(CONSTRAINT_DELETED, constrId->getKey(), UNKNOWN,
                                        transactionId++, sequenceId, nstep,
                                        getNameForConstraint(constrId)));
    numTransactions++;
  }
}

void PartialPlanWriter::notifyFlushed(void) {
  writeCounter++;
  if(writeCounter == stepsPerWrite && noWrite == 0) {
    write();
    transactionList->makeEmpty();
    writeCounter = 0;
    numTransactions = 0;
  }
}

String PartialPlanWriter::getVarInfo(const VarId &varId) {
  TokenId parentToken = varId->getParentToken();
  String type("");
  String paramName("");

  Vector<Symbol> params;
  List<VarId> paramVarList;
  ListIterator<VarId> paramVarIterator;
  int index = 1;

  switch(varId->getType()) {
  case Variable::objectVariable:
    type = OBJECT_VAR;
    break;
  case Variable::rejectVariable:
    type = REJECT_VAR;
    break;
  case Variable::startVariable:
    type = START_VAR;
    break;
  case Variable::endVariable:
    type = END_VAR;
    break;
  case Variable::durationVariable:
    type = DURATION_VAR;
    break;
  case Variable::compatGuardVariable:
  case Variable::parameterVariable:
    type = PARAMETER_VAR;
    params = modelId.getPredicateArgumentNames(parentToken->getPredicate());
    paramVarList = parentToken->getParameterVariables();
    paramVarIterator = ListIterator<VarId>(paramVarList);
    while(!paramVarIterator.isDone()) {
      if(varId == paramVarIterator.item()) {
        paramName = params.getItemRef(index);
        break;
      }
      index++;
      paramVarIterator.step();
    }
    break;
  case Variable::globalVariable:
  case Variable::noVariable:
  default:
    break;
  }

  String retval = type + COMMA;
  if(Id<ValueToken>::convertable(parentToken)) {
    Id<ValueToken> vtokId(parentToken);
    retval += vtokId->getPredicate().getName();
  }
  else {
    retval += CONSTRAINT_TOKEN;
    }
  retval += COMMA + paramName + COMMA;
  
  Domain derivedDomain = varId->getCurrentDomain();
  Domain specifiedDomain = varId->getSpecifiedDomain();

  if(derivedDomain.isDynamic() || derivedDomain.isEnumerated()) {
    retval += E_DOMAIN + COMMA;
    retval += getEnumString(derivedDomain) + COMMA;
  }
  else if(derivedDomain.isInterval()) {
    retval += I_DOMAIN + COMMA;
    retval += getBoundString(derivedDomain, derivedDomain.getLowerBound()) + SPACE;
    retval += getBoundString(derivedDomain, derivedDomain.getUpperBound()) + COMMA;
  }
  if(specifiedDomain.isDynamic() || specifiedDomain.isEnumerated()) {
    retval += E_DOMAIN + COMMA;
    retval += getEnumString(specifiedDomain);
  }
  else if(specifiedDomain.isInterval()) {
    retval += I_DOMAIN + COMMA;
    retval += getBoundString(specifiedDomain, specifiedDomain.getLowerBound()) + SPACE;
    retval += getBoundString(specifiedDomain, specifiedDomain.getUpperBound());
  }
  return retval;
}

void Transaction::write(ostream &out, long long int partialPlanId) {
  if (transactionType == -1) {
    FatalError("Attempted to write invalid transaction.");
  }
  out << transactionTypeNames[transactionType] << TAB << objectKey << TAB 
      << sourceTypeNames[source] << TAB << id << TAB << stepNum << TAB << sequenceId << TAB 
      << partialPlanId << TAB << info << endl;
}
