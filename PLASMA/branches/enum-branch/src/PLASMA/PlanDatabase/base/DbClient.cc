#include "DbClient.hh"
#include "PlanDatabase.hh"
#include "Entity.hh"
#include "Utils.hh"
#include "ObjectFactory.hh"
#include "Object.hh"
#include "Token.hh"
#include "TokenVariable.hh"
#include "DbClientListener.hh"
#include "ConstraintEngine.hh"
#include "ConstraintType.hh"
#include "Debug.hh"

#include <string>
#include <iostream>

#define publish(message) { \
   for (std::set<DbClientListenerId>::const_iterator it = m_listeners.begin(); it != m_listeners.end(); ++it) { \
     DbClientListenerId listener = *it; \
     listener->message; \
   } \
 }

namespace EUROPA {

  const char* DELIMITER = ":"; /*!< Used for delimiting streamed output */

  DbClient::DbClient(const PlanDatabaseId& db)
    : m_id((DbClient*)this), m_deleted(false), m_transactionLoggingEnabled(false) {
    check_error(db.isValid());
    m_planDb = db;
  }

  DbClient::~DbClient(){
    m_deleted = true;
    m_id.remove();
  }

  const DbClientId& DbClient::getId() const{return m_id;}

  const CESchemaId& DbClient::getCESchema() const
  {
      return m_planDb->getConstraintEngine()->getCESchema();
  }

  const SchemaId& DbClient::getSchema() const
  {
      return m_planDb->getSchema();
  }


  ConstrainedVariableId
  DbClient::createVariable(const char* typeName, const Domain& baseDomain, const char* name, bool isTmpVar, bool canBeSpecified)
  {
    ConstrainedVariableId variable = m_planDb->getConstraintEngine()->createVariable(typeName, baseDomain, isTmpVar, canBeSpecified, name);
    if (m_planDb->getSchema()->isObjectType(typeName) && !variable->isClosed()) {
      m_planDb->makeObjectVariableFromType(typeName, variable);
    }

    if (!isTmpVar) {
       // Register as a global variable
        m_planDb->registerGlobalVariable(variable);
    }

    publish(notifyVariableCreated(variable));
    return variable;
  }

  ConstrainedVariableId
  DbClient::createVariable(const char* typeName, const char* name, bool isTmpVar)
  {
    ConstrainedVariableId variable = m_planDb->getConstraintEngine()->createVariable(typeName, isTmpVar, true, name);
    if (m_planDb->getSchema()->isObjectType(typeName)) {
      m_planDb->makeObjectVariableFromType(typeName, variable);
    }

    // TODO: register TmpVariables so that they can be cleaned up easily
    if (!isTmpVar) {
       // Register as a global variable
        m_planDb->registerGlobalVariable(variable);
    }

    publish(notifyVariableCreated(variable));

    return variable;
  }

  void DbClient::deleteVariable(const ConstrainedVariableId& var) {
    if(isGlobalVariable(var->getName()))
      m_planDb->unregisterGlobalVariable(var);
    publish(notifyVariableDeleted(var));
    delete (ConstrainedVariable*) var;
  }

  ObjectId DbClient::createObject(const char* type, const char* name){
    static const std::vector<const Domain*> noArguments;
    ObjectId object = m_planDb->createObject(type, name, noArguments);
    debugMsg("DbClient:createObject", object->toLongString());
    publish(notifyObjectCreated(object));
    return object;
  }

  ObjectId DbClient::createObject(const char* type, const char* name, const std::vector<const Domain*>& arguments){
    ObjectId object = m_planDb->createObject(type, name, arguments);
    debugMsg("DbClient:createObject", object->toLongString());
    publish(notifyObjectCreated(object, arguments));
    return object;
  }

  void DbClient::deleteObject(const ObjectId& obj) {
    publish(notifyObjectDeleted(obj));
    delete (Object*) obj;
  }

  void DbClient::close(){
    m_planDb->close();
    debugMsg("DbClient:close", "CLOSE ALL TYPES");
    publish(notifyClosed());
  }

  void DbClient::close(const char* objectType) {
    m_planDb->close(objectType);
    debugMsg("DbClient:close", objectType);
    publish(notifyClosed(objectType));
  }

  TokenId DbClient::createToken(const char* tokenType,
                                const char* tokenName,
                                bool rejectable,
                                bool isFact) {
    TokenId token = allocateToken(tokenType, tokenName, rejectable, isFact);
    debugMsg("DbClient:createToken", token->toString());
    publish(notifyTokenCreated(token));
    return(token);
  }

  void DbClient::deleteToken(const TokenId& token, const std::string& name) {
    check_error(token.isValid());
    checkError(token->isInactive() || token->isFact(),
	       "Attempted to delete active, non-fact token " << token->toLongString());
    if(isGlobalToken(token->getName()))
      m_planDb->unregisterGlobalToken(token);
    publish(notifyTokenDeleted(token, name));

    //the keys are only recorded if logging is enabled
    //this may not be the right thing...
    if(!m_keysOfTokensCreated.empty()) {
      if(m_keysOfTokensCreated.back() == token->getKey()) {
	debugMsg("DbClient:deleteToken",
		 "Removing token key " << m_keysOfTokensCreated.back());
	m_keysOfTokensCreated.pop_back();
      }
      checkError(std::find(m_keysOfTokensCreated.begin(), m_keysOfTokensCreated.end(),
			   token->getKey()) == m_keysOfTokensCreated.end(),
		 "Attempted to delete " << token->toString() << " out of order.");
    }
    delete (Token*) token;
  }

  void DbClient::constrain(const ObjectId& object, const TokenId& predecessor, const TokenId& successor){
    object->constrain(predecessor, successor);
    debugMsg("DbClient:constrain", predecessor->toString() << " before " << successor->toString());
    publish(notifyConstrained(object, predecessor, successor));
  }

  void DbClient::free(const ObjectId& object, const TokenId& predecessor, const TokenId& successor){
    object->free(predecessor, successor);
    debugMsg("DbClient:free", predecessor->toString() << " before " << successor->toString());
    publish(notifyFreed(object, predecessor, successor));
  }

  void DbClient::activate(const TokenId& token){
    token->activate();
    debugMsg("DbClient:activate", token->toString());
    publish(notifyActivated(token));
  }

  void DbClient::merge(const TokenId& token, const TokenId& activeToken){
    static unsigned int sl_counter(0);
    sl_counter++;
    checkError(token.isValid(), token << ":" << sl_counter);
    token->doMerge(activeToken);
    debugMsg("DbClient:merge", token->toString() << " onto " << activeToken->toString());
    publish(notifyMerged(token, activeToken));
  }

  void DbClient::reject(const TokenId& token){
    check_error(token.isValid());
    token->reject();
    debugMsg("DbClient:reject", token->toString());
    publish(notifyRejected(token));
  }

  void DbClient::cancel(const TokenId& token){
    check_error(token.isValid());

    publish(notifyCancelled(token));

    TokenId activeToken;

    if(token->isMerged())
       activeToken = token->getActiveToken();

    checkError(activeToken.isNoId() || activeToken.isValid(), activeToken);

    if(activeToken.isId() && activeToken->refCount() == 1){
      // Expect that the last key created and stored will be this key
      checkError(!isTransactionLoggingEnabled() ||
		 m_keysOfTokensCreated.back() == activeToken->getKey(),
		 "If transaction logging enabled then we require chronological retraction. " << activeToken->toString());
      if(isTransactionLoggingEnabled()) {
	debugMsg("DbClient:cancel",
		 "Removing token key " << m_keysOfTokensCreated.back());
	m_keysOfTokensCreated.pop_back();
      }
    }


    token->cancel();

    debugMsg("DbClient:cancel", token->toString());
  }

  ConstraintId DbClient::createConstraint(const char* name,
				 const std::vector<ConstrainedVariableId>& scope,
				 const char* violationExpl)
  {

    // Use the constraint library factories to create the constraint
    ConstraintId constraint = m_planDb->getConstraintEngine()->createConstraint(name,scope,violationExpl);
    debugMsg("DbClient:createConstraint", constraint->toString());
    publish(notifyConstraintCreated(constraint));
    return constraint;
  }

  void DbClient::deleteConstraint(const ConstraintId& c)
  {
    publish(notifyConstraintDeleted(c));
    m_planDb->getConstraintEngine()->deleteConstraint(c);
  }

  void DbClient::restrict(const ConstrainedVariableId& variable, const Domain& domain){
    debugMsg("DbClient:restrict", variable->toLongString() << " to " << domain.toString());
    variable->restrictBaseDomain(domain);
    publish(notifyVariableRestricted(variable));
  }

  void DbClient::specify(const ConstrainedVariableId& variable, edouble value){
    debugMsg("DbClient:specify", "before:" << variable->toLongString() << " to " << variable->toString(value));
    variable->specify(value);
    debugMsg("DbClient:specify", "after:" << variable->toLongString());
    publish(notifyVariableSpecified(variable));
  }

  void DbClient::close(const ConstrainedVariableId& variable){
    variable->close();
    debugMsg("DbClient:close", variable->toLongString());
    publish(notifyVariableClosed(variable));
  }
  void DbClient::reset(const ConstrainedVariableId& variable){
    debugMsg("DbClient:reset","before:" << variable->toLongString());
    variable->reset();
    debugMsg("DbClient:reset", "after:" << variable->toLongString());
    publish(notifyVariableReset(variable));
  }

  bool DbClient::propagate(){
    m_planDb->getConstraintEngine()->propagate();

    return m_planDb->getConstraintEngine()->constraintConsistent();
  }


  ObjectId DbClient::getObject(const char* name) const {return m_planDb->getObject(name);}

  /**
   * @brief Traverse the path and obtain the right token
   */
  TokenId DbClient::getTokenByPath(const std::vector<int>& relativePath) const
  {
    check_error(isTransactionLoggingEnabled());
    check_error(!relativePath.empty());
    check_error(!m_keysOfTokensCreated.empty());
    check_error(relativePath[0] >= 0); // Can never be a valid path

    // Quick check for the root of the path
    if((unsigned)relativePath[0] >= m_keysOfTokensCreated.size()) // Cannot be a path for a token with this key set
      return TokenId::noId();

    // Obtain the root token key using the first element in the path to index the tokenKeys.
    eint rootTokenKey = m_keysOfTokensCreated[relativePath[0]];

    // Now source the token as an enityt lookup by key. This works because we have a shared pool
    // of entities per process
    EntityId entity = Entity::getEntity(rootTokenKey);

    if (entity.isNoId())
      return(TokenId::noId());

    TokenId rootToken = entity;
    for (unsigned int i = 1;
         !rootToken.isNoId() && i < relativePath.size();
         i++)
      rootToken = rootToken->getSlave(relativePath[i]);

    return(rootToken);
  }

  const ConstrainedVariableId DbClient::getGlobalVariable(const LabelStr& varName) const{
    return m_planDb->getGlobalVariable(varName);
  }

  bool DbClient::isGlobalVariable(const LabelStr& varName) const {
    return m_planDb->isGlobalVariable(varName);
  }

  const TokenId DbClient::getGlobalToken(const LabelStr& name) const{
    return m_planDb->getGlobalToken(name);
  }

  bool DbClient::isGlobalToken(const LabelStr& name) const {
    return m_planDb->isGlobalToken(name);
  }

  /**
   * @brief Build the path from the bottom up, and return it from the top down.
   */
  std::vector<int> DbClient::getPathByToken(const TokenId& targetToken) const
  {
    check_error(isTransactionLoggingEnabled());
    std::list<int> path; // Used to build up the path from the bottom up.

    TokenId master = targetToken->master();
    TokenId slave = targetToken;

    while(!master.isNoId()){
      int slavePosition = master->getSlavePosition(slave);
      check_error(slavePosition >= 0);
      path.push_front(slavePosition);
      slave = slave->master();
      master = master->master();
    }

    // Loop terminates where slave is the root, so get the masters key from the slave pointer.
    eint keyOfMaster = slave->getKey();

    // Now we must obtain a key value based on relative position in the sequence of created master
    // tokens. This is done so that we can use the path to replay transactions, but resulting in different
    // key values.
    int indexOfMaster = -1;
    for(unsigned int i=0; i< m_keysOfTokensCreated.size(); i++)
      if(m_keysOfTokensCreated[i] == keyOfMaster){
	indexOfMaster = i;
	break;
      }

    check_error(indexOfMaster >= 0);

    // Now push the key for the root and generate path going from top down
    path.push_front(indexOfMaster);

    std::vector<int> pathAsVector;
    for(std::list<int>::const_iterator it = path.begin(); it != path.end(); ++it)
      pathAsVector.push_back(*it);

    return pathAsVector;
  }

  std::string DbClient::getPathAsString(const TokenId& targetToken) const {
    check_error(isTransactionLoggingEnabled());
    const std::vector<int> path = getPathByToken(targetToken);
    std::stringstream s;
    std::vector<int>::const_iterator it = path.begin();
    s << *it;
    for(++it ; it != path.end() ; ++it) {
      s << "." << *it;
    }
    return s.str();
  }

  ConstrainedVariableId DbClient::getVariableByIndex(unsigned int index){
    return m_planDb->getConstraintEngine()->getVariable(index);
  }

  unsigned int DbClient::getIndexByVariable(const ConstrainedVariableId& var){
    return m_planDb->getConstraintEngine()->getIndex(var);
  }

  ConstraintId DbClient::getConstraintByIndex(unsigned int index) {
    return m_planDb->getConstraintEngine()->getConstraint(index);
  }

  unsigned int DbClient::getIndexByConstraint(const ConstraintId& constr) {
    return m_planDb->getConstraintEngine()->getIndex(constr);
  }

  void DbClient::notifyAdded(const DbClientListenerId& listener){
    check_error(m_listeners.find(listener) == m_listeners.end());
    m_listeners.insert(listener);
  }

  void DbClient::notifyRemoved(const DbClientListenerId& listener){
    check_error(m_listeners.find(listener) != m_listeners.end());
    if(!m_deleted){
      m_listeners.erase(listener);
    }
  }

  bool DbClient::constraintConsistent() const {
    return m_planDb->getConstraintEngine()->constraintConsistent();
  }

  bool DbClient::supportsAutomaticAllocation() const{
    return m_planDb->hasTokenTypes();
  }

  TokenId DbClient::allocateToken(const char* tokenType,
                                  const char* tokenName,
                                  bool rejectable,
                                  bool isFact) {
    checkError(supportsAutomaticAllocation(), "Cannot allocate tokens from the schema.");
    TokenId token = m_planDb->createToken(tokenType, tokenName, rejectable, isFact);

    if (isTransactionLoggingEnabled()) {
      debugMsg("DbClient:allocateToken",
	       "Saving token key " << token->getKey());
      m_keysOfTokensCreated.push_back(token->getKey());
    }

    checkError(token.isValid(), "Failed to allocate token for " << tokenType);
    return token;
  }

  void DbClient::enableTransactionLogging() {
    check_error(!isTransactionLoggingEnabled());
    m_transactionLoggingEnabled = true;
  }

  void DbClient::disableTransactionLogging() {
    check_error(isTransactionLoggingEnabled());
    m_transactionLoggingEnabled = false;
  }

  bool DbClient::isTransactionLoggingEnabled() const { return m_transactionLoggingEnabled; }

  edouble DbClient::createValue(const char* typeName, const std::string& value)
  {
    return m_planDb->getConstraintEngine()->createValue(typeName,value);
  }

  PSPlanDatabaseClientImpl::PSPlanDatabaseClientImpl(const DbClientId& c)
      : m_client(c)
  {
  }

  PSVariable* PSPlanDatabaseClientImpl::createVariable(const std::string& typeName, const std::string& name, bool isTmpVar)
  {
      ConstrainedVariableId var = m_client->createVariable(typeName.c_str(),name.c_str(),isTmpVar);
      return dynamic_cast<PSVariable*>((ConstrainedVariable*)var);
  }

  void PSPlanDatabaseClientImpl::deleteVariable(PSVariable* var)
  {
      m_client->deleteVariable(toId(var));
  }

  PSObject* PSPlanDatabaseClientImpl::createObject(const std::string& type, const std::string& name)
  {
      ObjectId obj = m_client->createObject(type.c_str(),name.c_str());
      return dynamic_cast<PSObject*>((Object*)obj);
  }

  //PSObject* PSPlanDatabaseClientImpl::createObject(const std::string& type, const std::string& name, const PSList<PSVariable*>& arguments){}

  void PSPlanDatabaseClientImpl::deleteObject(PSObject* obj)
  {
      m_client->deleteObject(toId(obj));
  }

  PSToken* PSPlanDatabaseClientImpl::createToken(const std::string& predicateName, bool rejectable, bool isFact)
  {
      TokenId tok = m_client->createToken(predicateName.c_str(),NULL, rejectable,isFact);
      return dynamic_cast<PSToken*>((Token*)tok);
  }

  void PSPlanDatabaseClientImpl::deleteToken(PSToken* token)
  {
      m_client->deleteToken(toId(token));
  }

  void PSPlanDatabaseClientImpl::constrain(PSObject* object, PSToken* predecessor, PSToken* successor)
  {
      m_client->constrain(toId(object),toId(predecessor),toId(successor));
  }

  void PSPlanDatabaseClientImpl::free(PSObject* object, PSToken* predecessor, PSToken* successor)
  {
      m_client->free(toId(object),toId(predecessor),toId(successor));
  }

  void PSPlanDatabaseClientImpl::activate(PSToken* token)
  {
      m_client->activate(toId(token));
  }

  void PSPlanDatabaseClientImpl::merge(PSToken* token, PSToken* activeToken)
  {
      m_client->merge(toId(token),toId(activeToken));
  }

  void PSPlanDatabaseClientImpl::reject(PSToken* token)
  {
      m_client->reject(toId(token));
  }

  void PSPlanDatabaseClientImpl::cancel(PSToken* token)
  {
      m_client->cancel(toId(token));
  }

  PSConstraint* PSPlanDatabaseClientImpl::createConstraint(const std::string& name, PSList<PSVariable*>& scope)
  {
      std::vector<ConstrainedVariableId> idScope;
      for (int i=0;i<scope.size();i++)
          idScope.push_back(toId(scope.get(i)));

      ConstraintId c = m_client->createConstraint(name.c_str(),idScope);
      return dynamic_cast<PSConstraint*>((Constraint*)c);
  }

  void PSPlanDatabaseClientImpl::deleteConstraint(PSConstraint* c)
  {
      m_client->deleteConstraint(toId(c));
  }

  void PSPlanDatabaseClientImpl::specify(PSVariable* variable, double value)
  {
      m_client->specify(toId(variable),value);
  }

  void PSPlanDatabaseClientImpl::reset(PSVariable* variable)
  {
      m_client->reset(toId(variable));
  }

  void PSPlanDatabaseClientImpl::close(PSVariable* variable)
  {
      m_client->close(toId(variable));
  }

  void PSPlanDatabaseClientImpl::close(const std::string& objectType)
  {
      m_client->close(objectType.c_str());
  }

  void PSPlanDatabaseClientImpl::close()
  {
      m_client->close();
  }

  ConstrainedVariableId PSPlanDatabaseClientImpl::toId(PSVariable* v)
  {
      return dynamic_cast<ConstrainedVariable*>(v)->getId();
  }

  ConstraintId PSPlanDatabaseClientImpl::toId(PSConstraint* c)
  {
      return dynamic_cast<Constraint*>(c)->getId();
  }

  ObjectId PSPlanDatabaseClientImpl::toId(PSObject* o)
  {
      return dynamic_cast<Object*>(o)->getId();
  }

  TokenId PSPlanDatabaseClientImpl::toId(PSToken* t)
  {
      return dynamic_cast<Token*>(t)->getId();
  }
}
