//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.cc,v 1.21 2003-12-22 20:56:18 miatauro Exp $
//
#include <cstring>
#include <string>
#include <errno.h>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
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
const String TIMELINES(".timelines");
const String SLOTS(".slots");
const String TOKENS(".tokens");
const String PARAM_VAR_TOKEN_MAP(".paramVarTokenMap");
const String TOKEN_RELATIONS(".tokenRelations");
const String VARIABLES(".variables");
const String ENUMERATED_DOMAINS(".enumeratedDomains");
const String INTERVAL_DOMAINS(".intervalDomains");
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

  //!!should be changed to an ofstream --wedgingt@ptolemy.arc.nasa.gov 2003 Dec 12
  FILE *sequenceOut;
  char timestr[21]; //!!should use sizeof(sequenceID) and log base 10; see String.cc --wedgingt 2003 Dec 12
  sprintf(timestr, "%lld", sequenceId); //!!see String.cc --wedgingt 2003 Dec 12
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
    if (!(sequenceOut = fopen(sequenceStr.chars(), "w"))) {
      cerr << "Failed to open " << sequenceStr << endl;
      FatalError(strerror(errno));
    }
    fprintf(sequenceOut, "%s\t%lld", dest.chars(), sequenceId);
    fclose(sequenceOut);
    
    if(!(transactionOut = fopen(ppTransactions.chars(), "w"))) {
      FatalError(strerror(errno));
    }
    if(!(statsOut = fopen(ppStats.chars(), "w"))) {
      FatalError(strerror(errno));
    }
  }
};

PartialPlanWriter::~PartialPlanWriter(void) {
  if(stepsPerWrite) {
    fclose(transactionOut);
    fclose(statsOut);
  }
}

void PartialPlanWriter::write(void) {
  struct timeval currTime;
  long long int partialPlanId;
  //char timestr[16];
  numTokens = numVariables = numConstraints = 0;
  FILE *partialPlanOut, *objectOut, *timelineOut, *slotOut, *tokenOut, *variableOut, 
    *tokenRelationOut, *enumDomainOut, *intDomainOut, *constraintOut, *predOut, *paramOut,
    *paramVarTokenMapOut, *constraintVarMapOut;
  
  if(!stepsPerWrite) {
    return;
  }

  tokenRelationId = enumeratedDomainId = intervalDomainId = 1;
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
  if(!(partialPlanOut = fopen(ppPartialPlan.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  fprintf(partialPlanOut, "%s\t%lld\t%s\t%lld", stepnum.chars(), partialPlanId,
          tnet->getModelId().getModelName().chars(), sequenceId);
  fclose(partialPlanOut);

  String ppObject = partialPlanDest + SLASH + stepnum + OBJECTS;
  if(!(objectOut = fopen(ppObject.chars(), "w"))) {
   FatalError(strerror(errno));
  }
  String ppTimeline = partialPlanDest + SLASH + stepnum + TIMELINES;
  if(!(timelineOut = fopen(ppTimeline.chars(), "w"))) {
   FatalError(strerror(errno));
  }
  String ppSlot = partialPlanDest + SLASH + stepnum + SLOTS;
  if(!(slotOut = fopen(ppSlot.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppToken = partialPlanDest + SLASH + stepnum + TOKENS;
  if(!(tokenOut = fopen(ppToken.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppPVTM = partialPlanDest + SLASH + stepnum + PARAM_VAR_TOKEN_MAP;
  if(!(paramVarTokenMapOut = fopen(ppPVTM.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppTokenRelation = partialPlanDest + SLASH + stepnum + TOKEN_RELATIONS;
  if(!(tokenRelationOut = fopen(ppTokenRelation.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppVariables = partialPlanDest + SLASH + stepnum + VARIABLES;
  if(!(variableOut = fopen(ppVariables.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppEnumDomain = partialPlanDest + SLASH + stepnum + ENUMERATED_DOMAINS;
  if(!(enumDomainOut = fopen(ppEnumDomain.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppIntDomain = partialPlanDest + SLASH + stepnum + INTERVAL_DOMAINS;
  if(!(intDomainOut = fopen(ppIntDomain.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppConstraints = partialPlanDest + SLASH + stepnum + CONSTRAINTS;
  if(!(constraintOut = fopen(ppConstraints.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppCVM = partialPlanDest + SLASH + stepnum + CONSTRAINT_VAR_MAP;
  if(!(constraintVarMapOut = fopen(ppCVM.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppPredicates = partialPlanDest + SLASH + stepnum + PREDICATES;
  if(!(predOut = fopen(ppPredicates.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppParameters = partialPlanDest + SLASH + stepnum + PARAMETERS;
  if(!(paramOut = fopen(ppParameters.chars(), "w"))) {
    FatalError(strerror(errno));
  }

  /*List<VarId> globalVars = tnet->getGlobalVars();
  ListIterator<VarId> globalVarIterator = ListIterator<VarId>(globalVars);
  while(!globalVarIterator.isDone()) {
    outputVariable(globalVarIterator.item(), "GLOBAL_VAR", partialPlanId, variableOut, intDomainOut,
                   enumDomainOut);
    globalVarIterator.step();
    }*/
  //List<TokenId> allTokens = tnet->getAllTokens();

  //Nobody else's EUROPA has the getFreeValueTokensWithoutCompatUpdate method. ~MJI
  //List<TokenId> freeTokenList = tnet->getFreeValueTokens();
  List<TokenId> freeTokenList = tnet->getFreeValueTokensWithoutCompatUpdate();
  //numTokens = allTokens.getSize();

  ListIterator<TokenId> freeTokenIterator = ListIterator<TokenId>(freeTokenList);
  while(!freeTokenIterator.isDone()) {
    TokenId tokenId = freeTokenIterator.item();
    outputToken(tokenId, true, partialPlanId, NULL, 0, NULL, tokenOut, tokenRelationOut, 
                variableOut, intDomainOut, enumDomainOut, paramVarTokenMapOut);
    numTokens++;
    //allTokens.deleteIfEqual(tokenId);
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
    fprintf(objectOut, "%d\t%lld\t%s\n", objectId->getKey(), partialPlanId,
            objectId->getName().chars());
    List<AttributeId> timelineNames = tnet->getAttributes(objectId);
    ListIterator<AttributeId> timelineNameIterator = ListIterator<AttributeId>(timelineNames);

    while(!timelineNameIterator.isDone()) {
      AttributeId timelineAttId = timelineNameIterator.item();
      fprintf(timelineOut, "%d\t%d\t%lld\t%s\n", timelineId, objectId->getKey(),
              partialPlanId, modelId.getAttributeName(timelineAttId).chars());
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
        fprintf(slotOut, "%d\t%d\t%lld\t%d\t%d\n", slotId->getKey(), timelineId, 
                partialPlanId, objectId->getKey(), slotIndex);
        List<TokenId> tokenList = slotId->listValueTokensCoveringSlot();
        ListIterator<TokenId> tokenIterator = ListIterator<TokenId>(tokenList);
        while(!tokenIterator.isDone()) {
          TokenId tokenId = tokenIterator.item();
          //allTokens.deleteIfEqual(tokenId);
          outputToken(tokenId, false, partialPlanId, &objectId, timelineId, &slotId,
                      tokenOut, tokenRelationOut, variableOut, intDomainOut, enumDomainOut,
                      paramVarTokenMapOut);
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

  /*ListIterator<TokenId> remainingTokenIterator = ListIterator<TokenId>(allTokens);
  while(!remainingTokenIterator.isDone()) {
    TokenId tokenId = remainingTokenIterator.item();
    outputToken(tokenId, true, partialPlanId, &modelId, NULL, NULL, NULL, tokenOut, tokenRelationOut, 
                variableOut, intDomainOut, enumDomainOut, paramVarTokenMapOut);
    allTokens.deleteIfEqual(tokenId);
    remainingTokenIterator.step();
    }*/


  List<ConstraintId> constraints = tnet->getConstraints();
  numConstraints = constraints.getSize();
  ListIterator<ConstraintId> constraintIterator = ListIterator<ConstraintId>(constraints);

  while(!constraintIterator.isDone()) {
    ConstraintId constraintId = constraintIterator.item();
    outputConstraint(constraintId, partialPlanId, constraintOut, constraintVarMapOut);
    constraintIterator.step();
  }

  fprintf(statsOut, "%lld\t%lld\t%d\t%d\t%d\t%d\t%d\n", sequenceId, partialPlanId, nstep, numTokens,
          numVariables, numConstraints, numTransactions);
  fflush(statsOut);
  ListIterator<Transaction> transactionIterator = ListIterator<Transaction>(*transactionList);
  while(!transactionIterator.isDone()) {
    Transaction &transaction = (Transaction &) transactionIterator.item();
    transaction.write(transactionOut, partialPlanId);
    transactionIterator.step();
  }
  //fclose(transactionOut);
  fclose(objectOut);
  fclose(timelineOut);
  fclose(slotOut);
  fclose(tokenOut);
  fclose(paramVarTokenMapOut);
  fclose(tokenRelationOut);
  fclose(variableOut);
  fclose(enumDomainOut);
  fclose(intDomainOut);
  fclose(constraintOut);
  fclose(predOut);
  fclose(paramOut);
  fclose(constraintVarMapOut);
  nstep++;
}

void PartialPlanWriter::outputPredicate(PredicateId &predicate, const long long int partialPlanId, 
                                        FILE *predOut, FILE *paramOut) {
  fprintf(predOut, "%d\t%s\t%lld\n", predicate.getKey(), 
          modelId.getPredicateName(predicate).chars(), partialPlanId);
  Vector<Symbol> params = modelId.getPredicateArgumentNames(predicate);
  VectorIterator<Symbol> paramIterator = VectorIterator<Symbol>(params);
  int paramIndex = 0;
  while(!paramIterator.isDone()) {
    Symbol parameter = paramIterator.item();
    fprintf(paramOut, "%d\t%d\t%lld\t%s\n", paramIndex++, predicate.getKey(),
            partialPlanId, parameter.chars());
    paramIterator.step();
  }
}

void PartialPlanWriter::outputToken(const TokenId &tokenId, const bool isFree, 
                                    const long long int partialPlanId, const ObjectId *objectId,
                                    const int timelineId, const SlotId *slotId, FILE *tokenOut,
                                    FILE *tokenRelationOut, FILE *variableOut, FILE *intDomainOut,
                                    FILE *enumDomainOut, FILE *paramVarTokenMapOut) {
  PredicateId predicateId = tokenId->getPredicate();
  if(isFree) {
    fprintf(tokenOut, "%d\tNULL\t%lld\t1\t1\t%d\t%d\t%d\t%d\t%d\tNULL\tNULL\t%d\n",
            tokenId->getKey(), partialPlanId, tokenId->getStartVariable()->getKey(), 
            tokenId->getEndVariable()->getKey(), tokenId->getDurationVariable()->getKey(),
            tokenId->getRejectVariable()->getKey(), predicateId.getKey(), 
            tokenId->getObjectVariable()->getKey());
  }
  else {
    fprintf(tokenOut, "%d\t%d\t%lld\t0\t1\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\n",
            tokenId->getKey(), (*slotId)->getKey(), partialPlanId, 
            tokenId->getStartVariable()->getKey(), tokenId->getEndVariable()->getKey(),
            tokenId->getDurationVariable()->getKey(), tokenId->getRejectVariable()->getKey(),
            predicateId.getKey(), timelineId, (*objectId)->getKey(), 
            tokenId->getObjectVariable()->getKey());
  }
  if(tokenId->getMasterToken().isValid()) {
    fprintf(tokenRelationOut, "%lld\t%d\t%d\tCAUSAL\t%d\n", partialPlanId, 
            tokenId->getMasterToken()->getKey(), tokenId->getKey(), tokenRelationId++);
  }
  outputVariable(tokenId->getStartVariable(), "START_VAR", partialPlanId, tokenId, variableOut,
                 intDomainOut, enumDomainOut);
  outputVariable(tokenId->getEndVariable(), "END_VAR", partialPlanId, tokenId, variableOut,
                 intDomainOut, enumDomainOut);
  outputVariable(tokenId->getDurationVariable(), "DURATION_VAR", partialPlanId, tokenId, 
                 variableOut, intDomainOut, enumDomainOut);
  outputVariable(tokenId->getRejectVariable(), "REJECT_VAR", partialPlanId, tokenId, variableOut,
                 intDomainOut, enumDomainOut);
  outputVariable(tokenId->getObjectVariable(), "OBJECT_VAR", partialPlanId, tokenId, variableOut,
                 intDomainOut, enumDomainOut);
  List<VarId> paramVarList = tokenId->getParameterVariables();
  ListIterator<VarId> paramVarIterator = ListIterator<VarId>(paramVarList);
  int paramIndex = 0;
  while(!paramVarIterator.isDone()) {
    VarId variableId = paramVarIterator.item();
    outputVariable(variableId, "PARAMETER_VAR", partialPlanId, tokenId, variableOut,
                   intDomainOut, enumDomainOut);
    fprintf(paramVarTokenMapOut, "%d\t%d\t%d\t%lld\n", variableId->getKey(),
            tokenId->getKey(), paramIndex, partialPlanId);
    paramIndex++;
    paramVarIterator.step();
  }
}

void PartialPlanWriter::outputVariable(const VarId &variable, const char *type, 
                                       const long long int partialPlanId, const TokenId &tokenId,
                                       FILE *variableOut, FILE *intervalDomainOut,
                                       FILE *enumeratedDomainOut) {
  numVariables++;
  //Domain domain = tnet->getVariableDomain(variable);
  Domain domain = variable->getCurrentDomain();
  fprintf(variableOut, "%d\t%lld\t%d\t", variable->getKey(), partialPlanId, tokenId->getKey());
  if(domain.isDynamic() || domain.isEnumerated()) {
    fprintf(variableOut, "EnumeratedDomain\t%d\t%s\n", enumeratedDomainId, type);
    outputEnumDomain(domain, partialPlanId, enumeratedDomainOut);
  }
  else if (domain.isInterval()) {
    fprintf(variableOut, "IntervalDomain\t%d\t%s\n", intervalDomainId, type);
    outputIntervalDomain(domain, partialPlanId, intervalDomainOut);
  }
}

void PartialPlanWriter::outputIntervalDomain(const Domain &domain, 
                                             const long long int partialPlanId, 
                                             FILE *intervalDomainOut) {
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
  fprintf(intervalDomainOut, "%d\t%lld\t%s\t%s\t%s\n", intervalDomainId, partialPlanId,
          lowerBoundStr.chars(), upperBoundStr.chars(), sort.chars());
  intervalDomainId++;
}

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

void PartialPlanWriter::outputEnumDomain(const Domain &domain, const long long int partialPlanId,
                                         FILE *enumeratedDomainOut) {
  String enumStr = getEnumString(domain);
  fprintf(enumeratedDomainOut, "%d\t%lld\t%s\n", enumeratedDomainId, partialPlanId,
          enumStr.chars());
  enumeratedDomainId++;
}


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
                                         const long long int partialPlanId, FILE *constraintOut, 
                                         FILE *constraintVarMapOut) {
  String temporality, name;
  name = getNameForConstraint(constraintId);
  temporality = getTemporalityForConstraint(constraintId);
  fprintf(constraintOut, "%d\t%lld\t%s\t%s\n", constraintId->getKey(), partialPlanId,
          name.chars(), temporality.chars());
  List<VarId> constrainedVars = tnet->getConstraintScope(constraintId);
  ListIterator<VarId> varIterator = ListIterator<VarId>(constrainedVars);
  while(!varIterator.isDone()) {
    fprintf(constraintVarMapOut, "%d\t%d\t%lld\n", constraintId->getKey(),
            varIterator.item()->getKey(), partialPlanId);
    varIterator.step();
  }
}

const String PartialPlanWriter::getNameForConstraint(const ConstraintId &constraintId) {
  String retval("");
  //  switch(tnet->get
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
void PartialPlanWriter::notifyTokenIsInserted(TokenId tokenId) { //signals plan step
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
void PartialPlanWriter::notifySpecifiedDomainChanged(VarId varId) { //signals plan step
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

void PartialPlanWriter::notifyOfDeletedVariable(VarId /* varId */ ) {
//   if(stepsPerWrite) {
//     if(varId->getParentToken().isNoId()) {
//       return;
//     }
//     transactionList->append(Transaction(VAR_DELETED, varId->getKey(), UNKNOWN, transactionId++,
//                                         sequenceId, nstep, getVarInfo(varId)));
//     numTransactions++;
//   }
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
  if(writeCounter == stepsPerWrite) {
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
  if(parentToken->getTokenClass() == valueTokenClass) {
    //retval += modelId.getPredicateName(parentToken->getPredicate());
    retval += parentToken->getPredicate().getName();
  }
  else {
    retval += CONSTRAINT_TOKEN;
    }
  retval += COMMA + paramName + COMMA;
  
  //Domain derivedDomain = varId->getDerivedDomain();
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

void Transaction::write(FILE *out, long long int partialPlanId) {
  if(transactionType == -1) {
    FatalError("Attempted to write invalid transaction.");
  }
  fprintf(out, "%s\t%d\t%s\t%d\t%d\t%lld\t%lld\t%s\n", transactionTypeNames[transactionType], 
          objectKey, sourceTypeNames[source], id, stepNum, sequenceId, partialPlanId,
          info.chars());
}
