//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: PartialPlanWriter.cc,v 1.12 2003-11-06 21:51:05 miatauro Exp $
//
#include <cstring>
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
    stepsPerWrite = 1;
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
  dest = String(realpath(dest.chars(), destBuf));
  delete [] destBuf;

  if(dest.getChar(dest.getLength()) != '/') {
    dest += String("/");
  }

  FILE *sequenceOut;
  char timestr[16];
  sprintf(timestr, "%lld", sequenceId);
  modelId = tnet->getModelId();
  String modelName = modelId.getModelName();
  {
    char *tempIndex = rindex(modelName.chars(), '/');
    if(tempIndex != NULL) {
      tempIndex++;
      char *temp = new char[strlen(tempIndex)+1];
      strcpy(temp, tempIndex);
      modelName = temp;
      delete [] temp;
    }
  }
  char *seqname = (char *) modelName.chars();
  char *extStart = rindex(seqname, '.');
  *extStart = '\0';
  if(mkdir(dest.chars(), 0777) && errno != EEXIST) {
    cerr << "Failed to make directory " << dest << endl;
    FatalError(strerror(errno));
  }
  dest += seqname;
  dest += timestr;
  if(mkdir(dest.chars(), 0777) && errno != EEXIST) {
    cerr << "Failed to make directory " << dest << endl;
    FatalError(strerror(errno));
  }
  String ppStats = dest + String("/partialPlanStats");
  String ppTransactions = dest + String("/transactions");
  String sequenceStr = dest + String("/sequence");
  if(!(sequenceOut = fopen(sequenceStr.chars(), "w"))) {
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
};

PartialPlanWriter::~PartialPlanWriter(void) {
  fclose(transactionOut);
  fclose(statsOut);
}

void PartialPlanWriter::write(void) {
  struct timeval currTime;
  long long int partialPlanId;
  //char timestr[16];
  numTokens = numVariables = numConstraints = 0;
  FILE *partialPlanOut, *objectOut, *timelineOut, *slotOut, *tokenOut, *variableOut, 
    *tokenRelationOut, *enumDomainOut, *intDomainOut, *constraintOut, *predOut, *paramOut,
    *paramVarTokenMapOut, *constraintVarMapOut;
  
  tokenRelationId = enumeratedDomainId = intervalDomainId = 1;
  if(gettimeofday(&currTime, NULL)) {
    FatalError("Failed to get current time");
  }
  partialPlanId = (((long long int)currTime.tv_sec) * 1000) + (currTime.tv_usec / 1000);

  String stepnum = String("step") + String(nstep);

  String partialPlanDest = dest + String("/") + stepnum;
  if(mkdir(partialPlanDest.chars(), 0777) && errno != EEXIST) {
    FatalError(strerror(errno));
  }
  String ppPartialPlan = partialPlanDest + String("/") + stepnum + String(".partialPlan");
  if(!(partialPlanOut = fopen(ppPartialPlan.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  fprintf(partialPlanOut, "%s\t%lld\t%s\t%lld", stepnum.chars(), partialPlanId,
          tnet->getModelId().getModelName().chars(), sequenceId);
  fclose(partialPlanOut);

  String ppObject = partialPlanDest + String("/") + stepnum + String(".objects");
  if(!(objectOut = fopen(ppObject.chars(), "w"))) {
   FatalError(strerror(errno));
  }
  String ppTimeline = partialPlanDest + String("/") + stepnum + String(".timelines");
  if(!(timelineOut = fopen(ppTimeline.chars(), "w"))) {
   FatalError(strerror(errno));
  }
  String ppSlot = partialPlanDest + String("/") + stepnum + String(".slots");
  if(!(slotOut = fopen(ppSlot.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppToken = partialPlanDest + String("/") + stepnum + String(".tokens");
  if(!(tokenOut = fopen(ppToken.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppPVTM = partialPlanDest + String("/") + stepnum + String(".paramVarTokenMap");
  if(!(paramVarTokenMapOut = fopen(ppPVTM.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppTokenRelation = partialPlanDest + String("/") + stepnum + String(".tokenRelations");
  if(!(tokenRelationOut = fopen(ppTokenRelation.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppVariables = partialPlanDest + String("/") + stepnum + String(".variables");
  if(!(variableOut = fopen(ppVariables.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppEnumDomain = partialPlanDest + String("/") + stepnum + String(".enumeratedDomains");
  if(!(enumDomainOut = fopen(ppEnumDomain.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppIntDomain = partialPlanDest + String("/") + stepnum + String(".intervalDomains");
  if(!(intDomainOut = fopen(ppIntDomain.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppConstraints = partialPlanDest + String("/") + stepnum + String(".constraints");
  if(!(constraintOut = fopen(ppConstraints.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppCVM = partialPlanDest + String("/") + stepnum + String(".constraintVarMap");
  if(!(constraintVarMapOut = fopen(ppCVM.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppPredicates = partialPlanDest + String("/") + stepnum + String(".predicates");
  if(!(predOut = fopen(ppPredicates.chars(), "w"))) {
    FatalError(strerror(errno));
  }
  String ppParameters = partialPlanDest + String("/") + stepnum + String(".parameters");
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
  List<TokenId> allTokens = tnet->getAllTokens();
  List<TokenId> freeTokenList = tnet->getFreeValueTokens();
  numTokens = allTokens.getSize();
  
  ListIterator<TokenId> freeTokenIterator = ListIterator<TokenId>(freeTokenList);
  while(!freeTokenIterator.isDone()) {
    TokenId tokenId = freeTokenIterator.item();
    outputToken(tokenId, true, partialPlanId, NULL, 0, NULL, tokenOut, tokenRelationOut, 
                variableOut, intDomainOut, enumDomainOut, paramVarTokenMapOut);
    allTokens.deleteIfEqual(tokenId);
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
          allTokens.deleteIfEqual(tokenId);
          outputToken(tokenId, false, partialPlanId, &objectId, timelineId, &slotId,
                      tokenOut, tokenRelationOut, variableOut, intDomainOut, enumDomainOut,
                      paramVarTokenMapOut);
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
  Domain domain = tnet->getVariableDomain(variable);
  fprintf(variableOut, "%d\t%lld\t%d\t", variable->getKey(), partialPlanId, tokenId->getKey());
  if(domain.isDynamic()) {
    //should this be otherwise?
    fprintf(variableOut, "EnumeratedDomain\t%d\t%s\n", enumeratedDomainId, type);
    Set<Value> enumeration = domain.getSort().getCurrentMembers();
    SetIterator<Value> enumIterator = SetIterator<Value>(enumeration);
    String enumStr = String("");
    while(!enumIterator.isDone()) {
      Value value = enumIterator.item();
      if(value.isObject()) {
        enumStr += String(value.getObjectValue()->getKey());
      }
      else if(value.isLabel()) {
        enumStr += domain.getSort().getMemberName(value);
      }
      else if(value.isBool()) {
        if(value.getBoolValue()) {
          enumStr += String("true");
        }
        else {
          enumStr += String("false");
        }
      }
      else if(value.isReal()) {
        enumStr += String(value.getRealValue());
      }
      else if(value.isInt()) {
        enumStr += String(value.getIntValue());
      }
      enumStr += " ";
      enumIterator.step();
    }
    fprintf(enumeratedDomainOut, "%d\t%lld\t%s\n", enumeratedDomainId, partialPlanId,
            enumStr.chars());
    enumeratedDomainId++;
  }
  else if(domain.isEnumerated()) {
    fprintf(variableOut, "EnumeratedDomain\t%d\t%s\n", enumeratedDomainId, type);
    Set<Value> enumeration = domain.getMembers();
    SetIterator<Value> enumIterator = SetIterator<Value>(enumeration);
    String enumStr = String("");
    while(!enumIterator.isDone()) {
      Value value = enumIterator.item();
      if(value.isObject()) {
        enumStr += String(value.getObjectValue()->getKey());
      }
      else if(value.isLabel()) {
        enumStr += domain.getSort().getMemberName(value);
      }
      else if(value.isBool()) {
        if(value.getBoolValue()) {
          enumStr += String("true");
        }
        else {
          enumStr += String("false");
        }
      }
      else if(value.isReal()) {
        enumStr += String(value.getRealValue());
      }
      else if(value.isInt()) {
        enumStr += String(value.getIntValue());
      }
      enumStr += " ";
      enumIterator.step();
    }
    fprintf(enumeratedDomainOut, "%d\t%lld\t%s\n", enumeratedDomainId, partialPlanId,
            enumStr.chars());
    enumeratedDomainId++;
  }
  else if (domain.isInterval()) {
    fprintf(variableOut, "IntervalDomain\t%d\t%s\n", intervalDomainId, type);
    Value upperBound = domain.getUpperBound();
    Value lowerBound = domain.getLowerBound();
    String upperBoundStr = String("");
    String lowerBoundStr = String("");
    
    if(upperBound.isObject()) {
      upperBoundStr += String(upperBound.getObjectValue()->getKey());
    }
    else if(upperBound.isLabel()) {
      upperBoundStr += domain.getSort().getMemberName(upperBound);
    }
    else if(upperBound.isBool()) {
      if(upperBound.getBoolValue()) {
        upperBoundStr += String("true");
      }
      else {
        upperBoundStr += String("false");
      }
    }
    else if(upperBound.isReal()) {
      if(upperBound.isInfinite()) {
        if(upperBound < rzero) {
          upperBoundStr += String("-Infinity");
        }
        else {
          upperBoundStr += String("Infinity");
        }
      }
      else {
        upperBoundStr += String(upperBound.getRealValue());
      }
    }
    else if(upperBound.isInt()) {
      if(upperBound.isInfinite()) {
        if(upperBound < izero) {
          upperBoundStr += String("-Infinity");
        }
        else {
          upperBoundStr += String("Infinity");
        }
      }
      else {
        String ubs = String(upperBound.getIntValue());
        upperBoundStr += ubs;
      }
    }
    if(lowerBound.isObject()) {
      lowerBoundStr += String(lowerBound.getObjectValue()->getKey());
    }
    else if(lowerBound.isLabel()) {
      lowerBoundStr += domain.getSort().getMemberName(lowerBound);
    }
    else if(lowerBound.isBool()) {
      if(lowerBound.getBoolValue()) {
        lowerBoundStr += String("true");
      }
      else {
        lowerBoundStr += String("false");
      }
    }
    else if(lowerBound.isReal()) {
      if(lowerBound.isInfinite()) {
        if(lowerBound < rzero) {
          lowerBoundStr += String("-Infinity");
        }
        else {
          lowerBoundStr += String("Infinity");
        }
      }
      else {
        lowerBoundStr += String(lowerBound.getRealValue());
      }
    }
    else if(lowerBound.isInt()) {
      if(lowerBound.isInfinite()) {
        if(lowerBound < izero) {
          lowerBoundStr += String("-Infinity");
        }
        else {
          lowerBoundStr += String("Infinity");
        }
      }
      else {
        lowerBoundStr += String(lowerBound.getIntValue());
      }
    }
    SortId sortId = domain.getSort();
    String sort;
    if(sortId.isInt()) {
      sort = String("INTEGER_SORT");
    }
    else if(sortId.isReal()) {
      sort = String("REAL_SORT");
    }
    fprintf(intervalDomainOut, "%d\t%lld\t%s\t%s\t%s\n", intervalDomainId, partialPlanId,
            lowerBoundStr.chars(), upperBoundStr.chars(), sort.chars());
    intervalDomainId++;
  }
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

const String &PartialPlanWriter::getNameForConstraint(const ConstraintId &constraintId) {
  String retval = String("");
  if(tnet->isTemporalVariableConstraint(constraintId)) {
    retval = String("variableTempConstr");
  }
  else if(tnet->isTemporalBoundConstraint(constraintId)) {
    retval = String("unaryTempConstr");
  }
  else if(tnet->isTemporalRelationConstraint(constraintId)) {
    retval = String("fixedTempConstr");
  }
  else if(tnet->isArgumentBoundConstraint(constraintId)) {
    retval = String("unaryConstr");
  }
  else if(tnet->isArgumentEqualsConstraint(constraintId)) {
    retval = String("equlityConstr");
  }
  else if(tnet->isExpertConstraint(constraintId)) {
    retval = tnet->getExpertConstraintName(constraintId);
  }
  else {
    retval = String("bugConstr");
    cerr << "FOO: " << constraintId << " " << retval << endl;
  }
  if(retval.chars() == NULL) {
    retval = String("bugConstr");
    cerr << "FOO: " << constraintId << " " << retval << endl;
  }
  return retval;
}

const String &PartialPlanWriter::getTemporalityForConstraint(const ConstraintId &constraintId) {
  String retval("");
  if(tnet->isTemporalVariableConstraint(constraintId) || 
     tnet->isTemporalBoundConstraint(constraintId) || 
     tnet->isTemporalRelationConstraint(constraintId) ) {
    retval = String("TEMPORAL");
  }
  else {
    retval = String("ATEMPORAL");
  }
  return retval;
}

void PartialPlanWriter::notifyOfNewToken(TokenId tokenId) {
  if(stepsPerWrite) {
    String info = modelId.getPredicateName(tokenId->getPredicate());
    transactionList->append(Transaction(TOKEN_CREATED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
  }
}
void PartialPlanWriter::notifyTokenIsInserted(TokenId tokenId) { //signals plan step
  if(stepsPerWrite) {
    String info = modelId.getPredicateName(tokenId->getPredicate());
    transactionList->append(Transaction(TOKEN_INSERTED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
  }
}
void PartialPlanWriter::notifyTokenIsNotInserted(TokenId tokenId) {
  if(stepsPerWrite) {
    String info = modelId.getPredicateName(tokenId->getPredicate());
    transactionList->append(Transaction(TOKEN_FREED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
  }
}
void PartialPlanWriter::notifyAfterTokenIsNotInserted(TokenId tokenId) {
}

void PartialPlanWriter::notifyOfDeletedToken(TokenId tokenId) {
  if(stepsPerWrite) {
    String info = modelId.getPredicateName(tokenId->getPredicate());
    transactionList->append(Transaction(TOKEN_DELETED, tokenId->getKey(), UNKNOWN, transactionId++,
                                        sequenceId, nstep, info));
    numTransactions++;
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
    type = String("OBJECT_VAR");
    break;
  case Variable::rejectVariable:
    type = String("REJECT_VAR");
    break;
  case Variable::startVariable:
    type = String("START_VAR");
    break;
  case Variable::endVariable:
    type = String("END_VAR");
    break;
  case Variable::durationVariable:
    type = String("DURATION_VAR");
    break;
  case Variable::compatGuardVariable:
  case Variable::parameterVariable:
    type = String("PARAMETER_VAR");
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

  String retval = type + String(",") + modelId.getPredicateName(parentToken->getPredicate()) + 
    String(",") + paramName;
  return retval;
}

void Transaction::write(FILE *out, long long int partialPlanId) {
  if(transactionType == -1) {
    FatalError("Attempted to write invalid transaction.");
  }
  fprintf(out, "%s\t%d\t%s\t%d\t%d\t%lld\t%lld\t%s\n", transactionTypeNames[transactionType], 
          objectKey, sourceTypeNames[source], id, stepNum, sequenceId, partialPlanId, info.chars());
}
