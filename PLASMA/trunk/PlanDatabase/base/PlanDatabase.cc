#include "PlanDatabase.hh"
#include "Object.hh"
#include "Schema.hh"
#include "Token.hh"
#include "TokenVariable.hh"
#include "DefaultTemporalAdvisor.hh"
#include "Constraints.hh"
#include "DbClient.hh"
#include "Utils.hh"
#include "ConstraintEngine.hh"
#include "ConstraintLibrary.hh"
#include "LabelStr.hh"
#include "Entity.hh"
#include "Debug.hh"
#include "Utils.hh"
#include "EntityIterator.hh"
#include "ObjectTokenRelation.hh"
#include "CommonAncestorConstraint.hh"
#include "HasAncestorConstraint.hh"
#include <iostream>


/**
 * @file PlanDatabase.cc
 * @author Conor McGann
 * @brief Implements core of relationship management for plandatabase module.
 * @see DbClient, Token, Object
 */
namespace EUROPA{

  DEFINE_GLOBAL_CONST(std::string, g_ClassDelimiter, ":");

  class PlanDatabaseLocalStatic{
  public:
    PlanDatabaseLocalStatic(){
      static bool sl_registerConstraints = false;
      check_error(sl_registerConstraints == false, "Should only be called once.");
      if(sl_registerConstraints == false){
	REGISTER_SYSTEM_CONSTRAINT(CommonAncestorConstraint, "commonAncestor", "Default");
	REGISTER_SYSTEM_CONSTRAINT(HasAncestorConstraint, "hasAncestor", "Default");
	REGISTER_SYSTEM_CONSTRAINT(ObjectTokenRelation, "ObjectTokenRelation", "Default");
        // Temporal Constraints
        REGISTER_SYSTEM_CONSTRAINT(EqualConstraint, "concurrent", "Temporal");
        REGISTER_SYSTEM_CONSTRAINT(LessThanEqualConstraint, "precedes", "Temporal"); 
        REGISTER_SYSTEM_CONSTRAINT(AddEqualConstraint, "temporaldistance", "Temporal");
        REGISTER_SYSTEM_CONSTRAINT(AddEqualConstraint, "temporalDistance", "Temporal");
        sl_registerConstraints = true;
      }
    }
  };

  PlanDatabaseLocalStatic sl_planDatabase;

  /**
   * @brief Impements a Listener to handle deletions of variables of type ObjectDomain. 
   *
   * Object Variable deletions must be handled where the object type is dynamic, so that 
   * cached variables used in synchronization of dynamic contents can be updated.
   */
  class ObjectVariableListener: public ConstrainedVariableListener {
  public:
    ~ObjectVariableListener(){
      check_error(m_var.isValid());
      check_error(m_planDb.isValid());
      if(!Entity::isPurging()) // Don't bother with synching cached data if we are cleaning up
        m_planDb->handleObjectVariableDeletion(m_var);
    }

  private:
    friend class PlanDatabase; // Only one, since use of this listener is only for internal data synch for plandb.
    ObjectVariableListener(const ConstrainedVariableId& objectVar, const PlanDatabaseId& planDb)
      : ConstrainedVariableListener(objectVar), m_planDb(planDb){}

    const PlanDatabaseId m_planDb;
  };

#define  publish(message){						\
    check_error(!Entity::isPurging());					\
    for(std::set<PlanDatabaseListenerId>::const_iterator lit = m_listeners.begin(); lit != m_listeners.end(); ++lit) \
      (*lit)->message;							\
  }

  PlanDatabase::PlanDatabase(const ConstraintEngineId& constraintEngine, const SchemaId& schema)
    : m_id(this), m_constraintEngine(constraintEngine), m_schema(schema), m_state(OPEN), m_deleted(false) {
    m_client = (new DbClient(m_id))->getId();
    check_error(m_constraintEngine.isValid());
    check_error(m_schema.isValid());
  }

  PlanDatabase::~PlanDatabase(){
    check_error(m_constraintEngine.isValid());
    m_deleted = true;

    if(!isPurged())
      purge();

    if (!m_temporalAdvisor.isNoId())
      delete (TemporalAdvisor*) m_temporalAdvisor;

    cleanup(m_listeners);

    // Delete the client
    check_error(m_client.isValid());
    delete (DbClient*) m_client;

    m_id.remove();
  }

  void PlanDatabase::purge(){
    check_error(m_constraintEngine.isValid());
    check_error(m_state != PURGED);
    m_state = PURGED;

    if(!Entity::isPurging()){ // Clean up exploiting relationships
      // Retrieve only the tokens at the root
      std::set<TokenId> masterTokens;
      for(TokenSet::const_iterator it = m_tokens.begin(); it != m_tokens.end(); ++it){
        TokenId token = *it;
        check_error(token.isValid());
        if(token->getMaster().isNoId()) // It is a root object and so can be deleted
          masterTokens.insert(token);
      }

      cleanup(masterTokens);

      // Retrieve only the objects at the root
      std::set<ObjectId> rootObjects;
      for(ObjectSet::const_iterator it = m_objects.begin(); it != m_objects.end(); ++it){
        ObjectId object = *it;
        check_error(object.isValid());
        if(object->getParent().isNoId()) // It is a root object and so can be deleted
          rootObjects.insert(object);
      }

      // Now clean up root objects - which will cascade delete to the children
      cleanup(rootObjects);
    }
    else { // Just cleanup by blasting through the tokens and objects
      cleanup(m_tokens);
      cleanup(m_objects);
    }
    // Purge global variables
    cleanup(m_globalVariables);

    // Now purge the Constraint Engine
    m_constraintEngine->purge();
  }

  void PlanDatabase::notifyAdded(const ObjectId& object){
    check_error(!Entity::isPurging(), "Should not be in this method if in purgeMode.");

    check_error(object.isValid());

    check_error(!isClosed(object->getType()), 
                "Cannot add object " + object->getName().toString() + 
                " if type " + object->getType().toString() + " is already closed.");

    check_error(m_objects.find(object) == m_objects.end(),
                "Object with the name " + object->getName().toString() + " already added.");

    check_error(m_objectsByName.find(object->getName().getKey()) == m_objectsByName.end(),
                "Object with the name " + object->getName().toString() + " already added.");

    m_objects.insert(object);

    // Cache by name
    m_objectsByName.insert(std::pair<double, ObjectId>(object->getName().getKey(), object));

    // Now cache by type
    LabelStr type = object->getType();
    m_objectsByType.insert(std::pair<double, ObjectId>(type.getKey(), object));
    while(m_schema->hasParent(type)){
      type = m_schema->getParent(type);
      m_objectsByType.insert(std::pair<double, ObjectId>(type.getKey(), object));
    }

    // Now we must push the insertion to any connected variables.
    std::multimap<double, ConstrainedVariableId>::const_iterator it = m_objectVariablesByObjectType.find(object->getType());
    while (it != m_objectVariablesByObjectType.end() && it->first == object->getType()){
      ConstrainedVariableId connectedObjectVariable = it->second;
      check_error(connectedObjectVariable.isValid());
      if(!connectedObjectVariable->isClosed())
        connectedObjectVariable->insert(object);
      ++it;
    }

    publish(notifyAdded(object));

    debugMsg("PlanDatabase:notifyAdded:Object", 
             object->getType().toString() << CLASS_DELIMITER << object->getName().toString() << " (" << object->getKey() << ")");
  }

  void PlanDatabase::notifyRemoved(const ObjectId& object){
    check_error(!Entity::isPurging());
    check_error(object.isValid());
    check_error(m_objects.find(object) != m_objects.end());
    check_error(m_objectsByName.find(object->getName().getKey()) != m_objectsByName.end());

    // Clean up cached values
    m_objects.erase(object);
    m_objectsByName.erase(object->getName().getKey());
    for(std::multimap<double, ObjectId>::iterator it = m_objectsByPredicate.begin(); it != m_objectsByPredicate.end();){
      if(it->second == object)
        m_objectsByPredicate.erase(it++);
      else
        ++it;
    }

    for(std::multimap<double, ObjectId>::iterator it = m_objectsByType.begin(); it != m_objectsByType.end();){
      if(it->second == object)
        m_objectsByType.erase(it++);
      else
        ++it;
    }

    // Now we must push the removal to any connected variables.
    std::multimap<double, ConstrainedVariableId>::const_iterator it = m_objectVariablesByObjectType.find(object->getType());
    while (it != m_objectVariablesByObjectType.end() && it->first == object->getType()){
      ConstrainedVariableId connectedObjectVariable = it->second;
      check_error(connectedObjectVariable.isValid());
      connectedObjectVariable->remove(object);
      ++it;
    }

    publish(notifyRemoved(object));

    debugMsg("PlanDatabase:notifyRemoved:Object", 
             object->getType().toString() << CLASS_DELIMITER << object->getName().toString() << " (" << object->getKey() << ")");
  }

  void PlanDatabase::notifyAdded(const TokenId& token){
    check_error(m_tokens.find(token) == m_tokens.end());
    m_tokens.insert(token);
    publish(notifyAdded(token));

    debugMsg("PlanDatabase:notifyAdded:Token",  token->toString());
  }

  void PlanDatabase::notifyRemoved(const TokenId& token){
    check_error(!Entity::isPurging());
    check_error(m_tokens.find(token) != m_tokens.end());
    m_tokens.erase(token);
    m_tokensToOrder.erase(token->getKey());
    publish(notifyRemoved(token));

    debugMsg("PlanDatabase:notifyRemoved:Token", 
             token->getPredicateName().toString()  << " (" << token->getKey() << ")");
  }

  void PlanDatabase::notifyAdded(const ObjectId& object, const TokenId& token){
    publish(notifyAdded(object, token));

    debugMsg("PlanDatabase:notifyAdded:Object:Token", 
             token->toString() << " added to " << object->toString());
  }

  void PlanDatabase::notifyRemoved(const ObjectId& object, const TokenId& token){
    publish(notifyRemoved(object,token));
    debugMsg("PlanDatabase:notifyRemoved:Object:Token", 
             token->toString() << " removed from " << object->toString());
  }

  void PlanDatabase::notifyAdded(const PlanDatabaseListenerId& listener){
    check_error(listener.isValid());
    check_error(m_listeners.count(listener) == 0);
    m_listeners.insert(listener);
  }

  void PlanDatabase::notifyRemoved(const PlanDatabaseListenerId& listener){
    if(!m_deleted)
      m_listeners.erase(listener);
  }

  const PlanDatabaseId& PlanDatabase::getId() const {return m_id;}

  const ConstraintEngineId& PlanDatabase::getConstraintEngine() const {return m_constraintEngine;}

  const SchemaId& PlanDatabase::getSchema() const {return m_schema;}

  const TemporalAdvisorId& PlanDatabase::getTemporalAdvisor() {
    if (m_temporalAdvisor.isNoId()) {
      m_temporalAdvisor = (new DefaultTemporalAdvisor(m_constraintEngine))->getId();
    }
    return m_temporalAdvisor;
  }

  void PlanDatabase::setTemporalAdvisor(const TemporalAdvisorId& temporalAdvisor) { 
    check_error(m_temporalAdvisor.isNoId()); 
    m_temporalAdvisor = temporalAdvisor; 
  }

  const DbClientId& PlanDatabase::getClient() const {
    return m_client;
  }

  const ObjectSet& PlanDatabase::getObjects() const {
    return m_objects;
  }

  const std::set<ObjectId>& PlanDatabase::getObjectsByType(const LabelStr& objectType) const {
    static std::set<ObjectId> sl_results;
    check_error(m_schema->isObjectType(objectType));
    sl_results.clear();

    for (std::multimap<double, ObjectId>::const_iterator it = m_objectsByType.find(objectType.getKey());
         it != m_objectsByType.end() && it->first == objectType.getKey();
         ++it) {
      debugMsg("PlanDatabase:getObjectsByType", "Adding object '" << it->second->getName().toString() << "' of type '" << 
	       it->second->getType().toString() << "' for type '" << objectType.toString() << "'");
      sl_results.insert(it->second);
    }

    return sl_results;
  }

  void PlanDatabase::registerGlobalVariable(const ConstrainedVariableId& var){
    const LabelStr& varName = var->getName();
    checkError(!isGlobalVariable(varName), var->toString() << " is not unique.");
    m_globalVariables.insert(var);
    m_globalVarsByName.insert(std::pair<double, ConstrainedVariableId>(varName, var));

    checkError(isGlobalVariable(varName), var->toString() << " is not registered after all. This cannot be!.");
    debugMsg("PlanDatabase:registerGlobalVariable", "Registered " << var->toString());
  }

  const ConstrainedVariableSet& PlanDatabase::getGlobalVariables() const {
    return m_globalVariables;
  }

  const ConstrainedVariableId& PlanDatabase::getGlobalVariable(const LabelStr& varName) const{
    checkError(isGlobalVariable(varName), "No variable with name='" << varName.toString() << "' is present.");
    return m_globalVarsByName.find(varName)->second;
  }

  bool PlanDatabase::isGlobalVariable(const LabelStr& varName) const{
    return (m_globalVarsByName.find(varName) != m_globalVarsByName.end());
  }

  bool PlanDatabase::hasCompatibleTokens(const TokenId& inactiveToken){
    if(countCompatibleTokens(inactiveToken, 1) > 0)
      return true;
    else
      return false;
  }

  void PlanDatabase::getCompatibleTokens(const TokenId& inactiveToken, 
                                         std::vector<TokenId>& results,
                                         unsigned int limit,
                                         bool useExactTest) {
    // Draw from list of active tokens of the same predicate
    const TokenSet& candidates = getActiveTokens(inactiveToken->getPredicateName());

    condDebugMsg(candidates.empty(), 
		 "PlanDatabase:getCompatibleTokens", "No candidates to evaluate for " << inactiveToken->toString());

    const std::vector<ConstrainedVariableId>& inactiveTokenVariables = inactiveToken->getVariables();
    int variableCount = inactiveTokenVariables.size();

    TemporalAdvisorId temporalAdvisor = getTemporalAdvisor();
    unsigned int choiceCount = 0; // Used for comparison against given limit

    for(TokenSet::const_iterator it = candidates.begin(); it != candidates.end(); ++it){
      TokenId candidate = *it;

      debugMsg("PlanDatabase:getCompatibleTokens",
               "Evaluating candidate token (" << candidate->getKey() << ") for token (" 
               << inactiveToken->getKey() << ")");

      // Validate expectation about being active and predicate being the same
      check_error(Schema::instance()->isA(candidate->getPredicateName(), inactiveToken->getPredicateName()),
                  candidate->getPredicateName().toString() + " is not a " + inactiveToken->getPredicateName().toString());

      check_error(candidate->isActive(), "Should not be trying to merge an active token.");

      const std::vector<ConstrainedVariableId>& candidateTokenVariables = candidate->getVariables();

      // Check assumption that the set of variables is the same
      checkError(candidateTokenVariables.size() == (unsigned int) variableCount,
		 "Candidate token (" << candidate->getKey() << ") has " <<
		 candidateTokenVariables.size() << " variables, while inactive token (" <<
		 inactiveToken->getKey() << ") has " << variableCount);

      // Iterate and ensure there is an intersection. This could possibly be optmized based on
      // the cost of comparing domains, or the likelihood of a variable excluding choice. Smaller domains
      // would seem to offer better options on both counts, in general. Don't yet know if this even needs
      // optimization
      bool isCompatible = true;

      check_error(inactiveTokenVariables[0] == inactiveToken->getState(),
                  "We expect the first var to be the state var, which we must skip.");

      for(int i=1;i<variableCount;i++){
	const AbstractDomain& domA = inactiveTokenVariables[i]->lastDomain();
	const AbstractDomain& domB = candidateTokenVariables[i]->lastDomain();

	checkError(AbstractDomain::canBeCompared(domA, domB),
		   domA.toString() << " cannot be compared to " << domB.toString() << ".");

	// If there is no intersection, it is not compatible for merging. The order may be important for
	// efficiency.
	if(domA.getSize() == 0 && domB.getSize() == 0)
	  isCompatible = true;
	else if(domA.isOpen() && domB.isOpen())
		isCompatible = true;
	else if(domA.getSize() < domB.getSize())
	  isCompatible = domA.intersects(domB);
	else
	  isCompatible = domB.intersects(domA);

	if(!isCompatible) {
	  debugMsg("PlanDatabase:getCompatibleTokens",
		   "EXCLUDING (" << candidate->getKey() << ")" << 
		   "VAR=" << candidateTokenVariables[i]->getName().toString() << 
		   "(" << candidateTokenVariables[i]->getKey() << ") " <<
		   "Cannot intersect " << domA.toString() << " with " << domB.toString());
	  break;
	}

	debugMsg("PlanDatabase:getCompatibleTokens",
		 "VAR=" << candidateTokenVariables[i]->getName().toString() << 
		 "(" << candidateTokenVariables[i]->getKey() << ") " <<
		 "Can intersect " << domA.toString() << " with " << domB.toString());
      }

      // If it is still compatible, we may wish to do a double check on the
      // Temporal Variables, since we could get more pruning from the TemporalNetwork based on
      // temporal distance. This is because temporal propagation is insufficient to ensure that if 2 timepoints
      // have an intersection that they can actually co-exist. For example, if a < b, then there may well
      // be an intersection but t would be immediately inconsistent of they were required to be concurrent.
      if (isCompatible && 
          (!useExactTest || temporalAdvisor->canBeConcurrent(inactiveToken, candidate))){
        results.push_back(candidate);

        debugMsg("PlanDatabase:getCompatibleTokens", 
                 "EXACT=" << useExactTest << ". Adding " << candidate->getKey() << 
                 " for token " << inactiveToken->getKey());

        choiceCount++;
      }

      if(choiceCount == limit)
        return;
    }
  }

  void PlanDatabase::getCompatibleTokens(const TokenId& inactiveToken, 
                                         std::vector<TokenId>& results) {
    getCompatibleTokens(inactiveToken, results, PLUS_INFINITY, false);
  }

  unsigned int PlanDatabase::countCompatibleTokens(const TokenId& inactiveToken, 
                                                   unsigned int limit,
                                                   bool useExactTest){
    std::vector<TokenId> results;
    getCompatibleTokens(inactiveToken, results, limit, useExactTest);
    return results.size();
  }

  const std::map<int, std::pair<TokenId, ObjectSet> >& PlanDatabase::getTokensToOrder(){
    return m_tokensToOrder;
  }

  void PlanDatabase::getOrderingChoices(const TokenId& tokenToOrder,
                                        std::vector< OrderingChoice >& results,
                                        unsigned int limit){
    checkError(m_tokensToOrder.find(tokenToOrder->getKey()) != m_tokensToOrder.end(), 
               "Should not be calling this method if it is not a token in need of ordering. " << tokenToOrder->toString());

    ObjectSet& objects = m_tokensToOrder.find(tokenToOrder->getKey())->second.second;

    checkError(!objects.empty(), "There should be at least one source of induced constraint on the token." << tokenToOrder->toString());

    unsigned int count = 0;

    for (ObjectSet::iterator it_a = objects.begin(); it_a != objects.end(); ++it_a) {
      if(count == limit)
        continue;

      ObjectId object = *it_a;
      check_error(object.isValid());
      std::vector<std::pair<TokenId, TokenId> > choices;
      object->getOrderingChoices(tokenToOrder, choices, limit - count);

      for (std::vector<std::pair<TokenId, TokenId> >::iterator it_b = choices.begin(); it_b != choices.end(); ++it_b) {
        const std::pair<TokenId, TokenId>& tokenPair = *it_b;
        results.push_back(std::make_pair<ObjectId, std::pair<TokenId, TokenId> >(object, tokenPair));
        count++;
      }
    }

    checkError(results.size() <= limit, "Cutoff must be enforced.");
  }

  unsigned int PlanDatabase::countOrderingChoices(const TokenId& token,
                                                  unsigned int limit){
    checkError(m_constraintEngine->constraintConsistent(),
               "Cannot query for ordering choices while database is not constraintConsistent.");
    std::list<double> objects;
    token->getObject()->lastDomain().getValues(objects);
    unsigned int choiceCount = 0;
    for(std::list<double>::const_iterator it = objects.begin(); it != objects.end(); ++it){
      ObjectId object = *it;
      choiceCount = choiceCount + object->countOrderingChoices(token, limit-choiceCount);
      if(choiceCount >= limit)
        break;
    }

    return choiceCount;
  }

  unsigned int PlanDatabase::lastOrderingChoiceCount(const TokenId& token) const{
    checkError(m_constraintEngine->constraintConsistent(),
               "Cannot query for ordering choices while database is not constraintConsistent.");
    std::list<double> objects;
    unsigned int choiceCount = 0;
    token->getObject()->lastDomain().getValues(objects);
    for(std::list<double>::const_iterator it = objects.begin(); it != objects.end(); ++it){
      ObjectId object = *it;
      choiceCount = choiceCount + object->lastOrderingChoiceCount(token);
    }
    
    return choiceCount;
  }
  /**
   * @todo Really inefficient implementation. Improve later.
   */
  bool PlanDatabase::hasOrderingChoice(const TokenId& token){
    if(countOrderingChoices(token, 1) > 0)
      return true;
    else
      return false;
  }

  void PlanDatabase::getObjectsByPredicate(const LabelStr& predicate, std::list<ObjectId>& results) {
    check_error(results.empty());
    check_error(m_schema->isPredicate(predicate));

    // First try a cache hit.
    for(std::multimap<double, ObjectId>::const_iterator it = m_objectsByPredicate.find(predicate.getKey());
        (it != m_objectsByPredicate.end() && it->first == predicate.getKey());
        ++it){
      results.push_back(it->second);
    }

    if(results.empty()){ // We do not have a hit, so we must construct the set by iterating over all objects and checking with the schema
      for (ObjectSet::const_iterator it = m_objects.begin(); it != m_objects.end(); ++it){
        ObjectId object = *it;
        check_error(object.isValid());
        if(m_schema->canBeAssigned(object->getType(), predicate)){
          results.push_back(object);
          m_objectsByPredicate.insert(std::pair<double, ObjectId>(predicate.getKey(), object));
        }
      }
    }
  }

  const ObjectId& PlanDatabase::getObject(const LabelStr& name) const{
    if (m_objectsByName.find(name.getKey()) == m_objectsByName.end())
      return ObjectId::noId();
    return m_objectsByName.find(name.getKey())->second;
  }
  
  const TokenSet& PlanDatabase::getTokens() const {
    return m_tokens;
  }


  const TokenSet& PlanDatabase::getActiveTokens(const LabelStr& predicate) const {
    static const TokenSet sl_noTokens;
    std::map<double, TokenSet>::const_iterator it = m_activeTokensByPredicate.find(predicate);
    if(it != m_activeTokensByPredicate.end())
      return it->second;
    else
      return sl_noTokens;
  }

  bool PlanDatabase::isClosed() const {
    return (m_state == CLOSED);
  }

  bool PlanDatabase::isClosed(const LabelStr& objectType) const {
    check_error(m_schema->isObjectType(objectType), "Type '" + objectType.toString() + "' not defined in the model.");
    return(m_state == CLOSED ||
           (m_state == OPEN &&m_closedObjectTypes.find(objectType) != m_closedObjectTypes.end()));
  }

  bool PlanDatabase::isOpen() const {
    return (m_state == OPEN);
  }

  bool PlanDatabase::isPurged() const {
    return (m_state == PURGED);
  }

  void PlanDatabase::close() {
    check_error(m_state == OPEN);
    std::multimap<double, ConstrainedVariableId>::iterator it = m_objectVariablesByObjectType.begin();
    while (it != m_objectVariablesByObjectType.end()){
      ConstrainedVariableId connectedObjectVariable = it->second;
      check_error(connectedObjectVariable.isValid());
      if(!connectedObjectVariable->isClosed())
        connectedObjectVariable->close();
      m_objectVariablesByObjectType.erase(it++);
    }

    m_state = CLOSED;
  }

  void PlanDatabase::close(const LabelStr& objectType){
    check_error(m_state == OPEN);
    check_error(!isClosed(objectType));

    // Now we must close all the object variables associated with this type
    std::multimap<double, ConstrainedVariableId>::iterator it = m_objectVariablesByObjectType.find(objectType);
    while (it != m_objectVariablesByObjectType.end() && it->first == objectType){
      ConstrainedVariableId connectedObjectVariable = it->second;
      check_error(connectedObjectVariable.isValid());
      if(!connectedObjectVariable->isClosed())
        connectedObjectVariable->close();
      m_objectVariablesByObjectType.erase(it++);
    }
    m_closedObjectTypes.insert(objectType);
  }

  PlanDatabase::State PlanDatabase::getState() const {
    return m_state;
  }

  void PlanDatabase::notifyActivated(const TokenId& token){
    // Need to insert this token in the activeToken index
    check_error(token.isValid());
    check_error(token->isActive());

    insertActiveToken(token);

    publish(notifyActivated(token));

    debugMsg("PlanDatabase:notifyActivated", 
             token->getPredicateName().toString()  << "(" << token->getKey() << "}");
  }

  /**
   * @todo Can make thos more efficient by using the inheritance model as was donw on
   * insertion.
   */
  void PlanDatabase::notifyDeactivated(const TokenId& token){
    check_error(!Entity::isPurging());
    check_error(token.isValid());

    removeActiveToken(token);

    publish(notifyDeactivated(token));

    debugMsg("PlanDatabase:notifyDeactivated",
             token->getPredicateName().toString()  << "(" << token->getKey() << "}");
  }

  void PlanDatabase::notifyMerged(const TokenId& token){
    publish(notifyMerged(token));

    debugMsg("PlanDatabase:notifyMerged",
             token->getPredicateName().toString()  << "(" << token->getKey() << "}" <<
             " merged with (" << token->getActiveToken()->getKey() << ")"); 
  }

  void PlanDatabase::notifySplit(const TokenId& token){
    check_error(!Entity::isPurging());
    publish(notifySplit(token));

    debugMsg("PlanDatabase:notifySplit",
             token->getPredicateName().toString()  << "(" << token->getKey() << "}");
  }

  void PlanDatabase::notifyRejected(const TokenId& token){
    publish(notifyRejected(token));

    debugMsg("PlanDatabase:notifyRejected",
             token->getPredicateName().toString()  << "(" << token->getKey() << "}");
  }

  void PlanDatabase::notifyReinstated(const TokenId& token){
    check_error(!Entity::isPurging());
    publish(notifyReinstated(token));

    debugMsg("PlanDatabase:notifyReinstated",
             token->getPredicateName().toString()  << "(" << token->getKey() << "}");
  }


  void PlanDatabase::notifyCommitted(const TokenId& token){
    check_error(!Entity::isPurging());
    publish(notifyCommitted(token));

    debugMsg("PlanDatabase:notifyCommitted",
             token->getPredicateName().toString()  << "(" << token->getKey() << "}");
  }

  void PlanDatabase::notifyConstrained(const ObjectId& object, const TokenId& predecessor, const TokenId& successor) {
    publish(notifyConstrained(object, predecessor, successor));

    debugMsg("PlanDatabase:notifyConstrained",
             "(" << predecessor->getKey() << ") On Object " << 
             object->getType().toString() << CLASS_DELIMITER << object->getName().toString() << " (" 
             << object->getKey() << ") Constrained Before Token (" << successor->getKey() << ")");
  }

  void PlanDatabase::notifyFreed(const ObjectId& object, const TokenId& predecessor, const TokenId& successor) {
    check_error(!Entity::isPurging());
    publish(notifyFreed(object, predecessor, successor));

    debugMsg("PlanDatabase:notifyFreed",
             "(" << predecessor->getKey() << ") On Object " << 
             object->getType().toString() << CLASS_DELIMITER << object->getName().toString() << " (" 
             << object->getKey() << ") Freed from Before Token (" << successor->getKey() << ")");
  }

  void PlanDatabase::notifyOrderingRequired(const ObjectId& object, const TokenId& token){
    debugMsg("PlanDatabase:notifyOrderingRequired", 
	     object->getName().toString() << "(" << object->getKey() << ") from " << token->toString());

    checkError(token->isActive(), "Token must be active to induce an ordering:" << token->toString());

    // Obtain the set of it exists already
    std::map<int, std::pair<TokenId, ObjectSet> >::iterator it = m_tokensToOrder.find(token->getKey());
    if(it == m_tokensToOrder.end()){
      std::pair<TokenId, ObjectSet> entry(token, ObjectSet());
      m_tokensToOrder.insert(std::pair<int, std::pair<TokenId, ObjectSet> >(token->getKey(), entry));
      it = m_tokensToOrder.find(token->getKey());
    }

    checkError(it != m_tokensToOrder.end(), "Should be take care of by now.");

    ObjectSet& objects = it->second.second;

    checkError(objects.find(object) == objects.end(), "Should not be present. Must be a synchronization bug with extra notifications.");

    objects.insert(object);
  }

  void PlanDatabase::notifyOrderingNoLongerRequired(const ObjectId& object, const TokenId& token){
    debugMsg("PlanDatabase:notifyOrderingNoLongerRequired", 
	     object->getName().toString() << "(" << object->getKey() << ") from " << token->toString());
    std::map<int, std::pair<TokenId, ObjectSet> >::iterator it = m_tokensToOrder.find(token->getKey());

    checkError(it != m_tokensToOrder.end(), 
	       "Expect there to be a stored entry. Must be a bug in synchronization. Failed to send initial message.");

    ObjectSet& objects = it->second.second;
    checkError(objects.find(object) != objects.end(), 
	       "Expect the object to be present. Again, must have failed to send initial message.");

    // Now remove the object
    objects.erase(object);

    // If the object set is now empty, remove the entry
    if(objects.empty())
      m_tokensToOrder.erase(it);
  }


  void PlanDatabase::makeObjectVariableFromType(const LabelStr& objectType, 
						const ConstrainedVariableId& objectVar,
						bool leaveOpen){
    std::list<ObjectId> objects;
    getObjectsByType(objectType, objects);
    makeObjectVariable(objectType, objects, objectVar, leaveOpen);
  }

  void PlanDatabase::makeObjectVariable(const LabelStr& objectType, 
					const std::list<ObjectId>& objects, 
					const ConstrainedVariableId& objectVar,
					bool leaveOpen){
    check_error(objectVar.isValid());
    check_error(!objectVar->isClosed());

    for(std::list<ObjectId>::const_iterator it = objects.begin(); it != objects.end(); ++it){
      ObjectId object = *it;
      check_error(object.isValid());
      objectVar->insert(object);
      debugMsg("PlanDatabase:makeObjectVariable",
               "Inserting object " << object->getName().toString() << " of type " 
               << object->getType().toString() << " for base type " << objectType.toString());
    }

    handleObjectVariableCreation(objectType, objectVar, leaveOpen);
  }

  /**
   * @brief Remove
   */
  void PlanDatabase::handleObjectVariableDeletion(const ConstrainedVariableId& objectVar){

    // Now iterate over objectVariables stored and remove them - should be at least one reference
    for(std::multimap<double, ConstrainedVariableId>::iterator it = m_objectVariablesByObjectType.begin();
        it != m_objectVariablesByObjectType.end();){
      if(it->second == objectVar){
        m_objectVariablesByObjectType.erase(it++);
      }
      else
        ++it;
    }
  }

  void PlanDatabase::handleObjectVariableCreation(const LabelStr& objectType, 
						  const ConstrainedVariableId& objectVar,
						  bool leaveOpen){
    if(!isClosed(objectType)){
      new ObjectVariableListener(objectVar, m_id);
      m_objectVariablesByObjectType.insert(std::pair<double, ConstrainedVariableId>(objectType, objectVar));
    }
    else if(!leaveOpen)// Close the given variable
      objectVar->close();
  }

  EntityId PlanDatabase::getEntityByKey(int key) const {
    return Entity::getEntity(key);
  }

  unsigned int PlanDatabase::archive(unsigned int tick){
    checkError(getConstraintEngine()->constraintConsistent(),
	       "Must be propagated to a consistent state before archiving.");

    const unsigned int initialCount = getTokens().size();

    // Build a collection of tokens ordered by earliest start time. This is done to make cleaning up
    // of structures like a timeline more efficient. No measurements backing this up or evaluating the  true cost
    // of this algorithm
    std::multimap<int, TokenId> tokensToRemove;
    {
      EntityIterator< TokenSet::const_iterator > tokenIterator(m_tokens.begin(), m_tokens.end());
      while(!tokenIterator.done()){
	TokenId token = tokenIterator.next();

	// Do not store merged tokens for removal since we will terminate them when we terminate the
	// supporting token.
	if(token->isMerged())
	  continue;

	unsigned int latestEndTime = (unsigned int) token->getEnd()->lastDomain().getUpperBound();

	if(latestEndTime <= tick && token->canBeTerminated()){
	  debugMsg("PlanDatabase:archive:remove", 
		   token->toString() << " ending by " << latestEndTime << " for tick " << tick);
	  int earliestStartTime = (int) token->getStart()->lastDomain().getLowerBound();
	  std::pair<int, TokenId> entry(earliestStartTime , token);
	  tokensToRemove.insert(entry);
	}
	else {
	  condDebugMsg(!token->isMerged(), "PlanDatabase:archive:skip", 
		       token->toString() << " with end time " << token->getEnd()->toString() << " for tick " << tick);
	}
      }
    }

    for(std::multimap<int, TokenId>::const_iterator it = tokensToRemove.begin(); it != tokensToRemove.end(); ++it){
      TokenId token = it->second;
      token->terminate();
      token->discard();
    }

    return initialCount-getTokens().size();
  }

  void PlanDatabase::insertActiveToken(const TokenId& token){
    static const LabelStr sl_objectRoot("Object");
    static const LabelStr sl_timelineRoot("Timeline");
    LabelStr objectType = token->getObject()->baseDomain().getTypeName();
    LabelStr predicate = token->getPredicateName();
    LabelStr predicateSuffix = token->getUnqualifiedPredicateName();

    debugMsg("PlanDatabase:insertActiveToken", token->toString());

    while(getSchema()->isPredicate(predicate)){
      std::map<double, TokenSet>::iterator it = m_activeTokensByPredicate.find(predicate);
      if(it == m_activeTokensByPredicate.end()){
	static const TokenSet emptySet;
	std::pair<double, TokenSet > entry(predicate, emptySet);
	m_activeTokensByPredicate.insert(entry);
	it = m_activeTokensByPredicate.find(predicate);
      }

      TokenSet& activeTokens = it->second;
      activeTokens.insert(token);
      debugMsg("PlanDatabase:insertActiveToken", token->toString() << " added for " << predicate.toString());

      // Break if we hit a built in class
      if(objectType == sl_timelineRoot || objectType == sl_objectRoot)
	break;

      objectType = getSchema()->getParent(objectType);

      std::string predStr = objectType.toString() + "." + predicateSuffix.c_str();
      predicate = predStr;
    }
  }

  void PlanDatabase::removeActiveToken(const TokenId& token){
    static const LabelStr sl_objectRoot("Object");
    static const LabelStr sl_timelineRoot("Timeline");
    LabelStr objectType = token->getObject()->baseDomain().getTypeName();
    LabelStr predicate = token->getPredicateName();
    LabelStr predicateSuffix = token->getUnqualifiedPredicateName();

    debugMsg("PlanDatabase:removeActiveToken", token->toString());

    while(getSchema()->isPredicate(predicate)){
      std::map<double, TokenSet>::iterator it = m_activeTokensByPredicate.find(predicate);
      checkError(it != m_activeTokensByPredicate.end(), token->toString() << " must be present but isn't.")
      TokenSet& activeTokens = it->second;
      activeTokens.erase(token);
      debugMsg("PlanDatabase:insertActiveToken", token->toString() << " removed for " << predicate.toString());

      // Break if we hit a built in class
      if(objectType == sl_timelineRoot || objectType == sl_objectRoot)
	break;

      objectType = getSchema()->getParent(objectType);

      std::string predStr = objectType.toString() + "." + predicateSuffix.c_str();

      predicate = predStr;
    }
  }
}
