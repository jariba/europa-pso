#include <cstring>
#include <errno.h>
#include <iostream>
#include <stdio.h>
#include <strings.h>
#include <strings.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include "PartialPlanWriter.hh"
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

void PartialPlanWriter::write(void) {
  struct timeval currTime;
  long long int partialPlanId;
  FILE *partialPlanOut, *objectOut, *timelineOut, *slotOut, *tokenOut, *variableOut, 
    *tokenRelationOut, *enumDomainOut, *intDomainOut, *constraintOut, *predOut, *paramOut,
    *paramVarTokenMapOut, *constraintVarMapOut;

  tokenRelationId = enumeratedDomainId = intervalDomainId = 1;
  if(gettimeofday(&currTime, NULL)) {
    handleError(generalUnknownError, "Failed to get current time", fatalError,);
  }
  partialPlanId = (((long long int)currTime.tv_sec) * 1000) + (currTime.tv_usec / 1000);
  ModelId modelId = tnet->getModelId();
  String modelName = modelId.getModelName();
  char *seqname;
  if(nstep == 0) {
    seqname = (char *) modelName.chars();
    char *extStart = rindex(seqname, '.');
    *extStart = '\0';
    dest += seqname;
    if(mkdir(dest.chars(), 0777) && errno != EEXIST) {
      handleError(generalUnknownError, strerror(errno), fatalError,);
    }
  }
  String stepnum = String("step") + String(nstep);
  String partialPlanDest = dest + String("/") + stepnum;
  if(mkdir(partialPlanDest.chars(), 0777) && errno != EEXIST) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppPartialPlan = partialPlanDest + String("/") + stepnum + String(".partialPlan");
  if(!(partialPlanOut = fopen(ppPartialPlan.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  fprintf(partialPlanOut, "%s\t%lld\t%s\t-1", stepnum.chars(), partialPlanId,
          modelName.chars());
  fclose(partialPlanOut);

  String ppObject = partialPlanDest + String("/") + stepnum + String(".objects");
  if(!(objectOut = fopen(ppObject.chars(), "w"))) {
   handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppTimeline = partialPlanDest + String("/") + stepnum + String(".timelines");
  if(!(timelineOut = fopen(ppTimeline.chars(), "w"))) {
   handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppSlot = partialPlanDest + String("/") + stepnum + String(".slots");
  if(!(slotOut = fopen(ppSlot.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppToken = partialPlanDest + String("/") + stepnum + String(".tokens");
  if(!(tokenOut = fopen(ppToken.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppPVTM = partialPlanDest + String("/") + stepnum + String(".paramVarTokenMap");
  if(!(paramVarTokenMapOut = fopen(ppPVTM.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppTokenRelation = partialPlanDest + String("/") + stepnum + String(".tokenRelations");
  if(!(tokenRelationOut = fopen(ppTokenRelation.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppVariables = partialPlanDest + String("/") + stepnum + String(".variables");
  if(!(variableOut = fopen(ppVariables.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppEnumDomain = partialPlanDest + String("/") + stepnum + String(".enumeratedDomains");
  if(!(enumDomainOut = fopen(ppEnumDomain.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppIntDomain = partialPlanDest + String("/") + stepnum + String(".intervalDomains");
  if(!(intDomainOut = fopen(ppIntDomain.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppConstraints = partialPlanDest + String("/") + stepnum + String(".constraints");
  if(!(constraintOut = fopen(ppConstraints.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppCVM = partialPlanDest + String("/") + stepnum + String(".constraintVarMap");
  if(!(constraintVarMapOut = fopen(ppCVM.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppPredicates = partialPlanDest + String("/") + stepnum + String(".predicates");
  if(!(predOut = fopen(ppPredicates.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
  }
  String ppParameters = partialPlanDest + String("/") + stepnum + String(".parameters");
  if(!(paramOut = fopen(ppParameters.chars(), "w"))) {
    handleError(generalUnknownError, strerror(errno), fatalError,);
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
  ListIterator<TokenId> freeTokenIterator = ListIterator<TokenId>(freeTokenList);
  while(!freeTokenIterator.isDone()) {
    TokenId tokenId = freeTokenIterator.item();
    outputToken(tokenId, true, partialPlanId, &modelId, NULL, 0, NULL, tokenOut, tokenRelationOut, 
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
        outputPredicate(predicate, modelId, partialPlanId, predOut, paramOut);
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
          outputToken(tokenId, false, partialPlanId, &modelId, &objectId, timelineId, &slotId,
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
  ListIterator<ConstraintId> constraintIterator = ListIterator<ConstraintId>(constraints);

  while(!constraintIterator.isDone()) {
    ConstraintId constraintId = constraintIterator.item();
    outputConstraint(constraintId, partialPlanId, constraintOut, constraintVarMapOut);
    constraintIterator.step();
  }

  fclose(objectOut);
  fclose(timelineOut);
  fclose(slotOut);
  fclose(tokenOut);
  fclose(paramVarTokenMapOut);
  fclose(tokenRelationOut);
  nstep++;
}

void PartialPlanWriter::outputPredicate(PredicateId &predicate, const ModelId &modelId, 
                                        const long long int partialPlanId, FILE *predOut, 
                                        FILE *paramOut) {
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
                                    const long long int partialPlanId, const ModelId *modelId,
                                    const ObjectId *objectId, const int timelineId, 
                                    const SlotId *slotId, FILE *tokenOut, 
                                    FILE *tokenRelationOut, FILE *variableOut, 
                                    FILE *intDomainOut, FILE *enumDomainOut,
                                    FILE *paramVarTokenMapOut) {
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
  outputVariable(tokenId->getStartVariable(), "START_VAR", partialPlanId, variableOut,
                 intDomainOut, enumDomainOut);
  outputVariable(tokenId->getEndVariable(), "END_VAR", partialPlanId, variableOut,
                 intDomainOut, enumDomainOut);
  outputVariable(tokenId->getDurationVariable(), "DURATION_VAR", partialPlanId,
                 variableOut, intDomainOut, enumDomainOut);
  outputVariable(tokenId->getRejectVariable(), "REJECT_VAR", partialPlanId, variableOut,
                 intDomainOut, enumDomainOut);
  outputVariable(tokenId->getObjectVariable(), "OBJECT_VAR", partialPlanId, variableOut,
                 intDomainOut, enumDomainOut);
  List<VarId> paramVarList = tokenId->getParameterVariables();
  ListIterator<VarId> paramVarIterator = ListIterator<VarId>(paramVarList);
  int paramIndex = 0;
  while(!paramVarIterator.isDone()) {
    VarId variableId = paramVarIterator.item();
    outputVariable(variableId, "PARAMETER_VAR", partialPlanId, variableOut,
                   intDomainOut, enumDomainOut);
    fprintf(paramVarTokenMapOut, "%d\t%d\t%d\t%lld\n", variableId->getKey(),
            tokenId->getKey(), paramIndex++, partialPlanId);
    paramVarIterator.step();
  }
}

void PartialPlanWriter::outputVariable(const VarId &variable, const char *type, 
                                       const long long int partialPlanId, FILE *variableOut,
                                       FILE *intervalDomainOut, FILE *enumeratedDomainOut) {
  Domain domain = tnet->getVariableDomain(variable);
  
  fprintf(variableOut, "%d\t%lld\t", variable->getKey(), partialPlanId);
  if(domain.isEnumerated()) {
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
  if(tnet->isTemporalVariableConstraint(constraintId)) {
    temporality = String("TEMPORAL");
    name = String("variableTempConstr");
  }
  else if(tnet->isTemporalBoundConstraint(constraintId)) {
    temporality = String("TEMPORAL");
    name = String("unaryTempConstr");
  }
  else if(tnet->isTemporalRelationConstraint(constraintId)) {
    temporality = String("TEMPORAL");
    name = String("fixedTempConstr");
  }
  else if(tnet->isArgumentBoundConstraint(constraintId)) {
    temporality = String("ATEMPORAL");
    name = String("unaryConstr");
  } 
  else if(tnet->isArgumentEqualsConstraint(constraintId)) {
    temporality = String("ATEMPORAL");
    name = String("equalityConstr");
  }
  else if(tnet->isExpertConstraint(constraintId)) {
    temporality = String("ATEMPORAL");
    name = tnet->getExpertConstraintName(constraintId);
  }
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
