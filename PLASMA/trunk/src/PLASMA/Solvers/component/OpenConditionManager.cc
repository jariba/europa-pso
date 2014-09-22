#include "Utils.hh"
#include "Debug.hh"
#include "Error.hh"
#include "Token.hh"
#include "TokenVariable.hh"
#include "OpenConditionManager.hh"
#include "PlanDatabase.hh"


/**
 * @author Conor McGann
 * @date March, 2005
 */
namespace EUROPA {
  namespace SOLVERS {

    OpenConditionManager::OpenConditionManager(const TiXmlElement& configData)
      : FlawManager(configData) {}

    void OpenConditionManager::handleInitialize(){
      // FILL UP TOKENS
      const TokenSet& allTokens = m_db->getTokens();
      for(TokenSet::const_iterator it = allTokens.begin(); it != allTokens.end(); ++it){
	TokenId token = *it;
	addFlaw(token);
      }
    }
  
  bool OpenConditionManager::noMoreFlaws() {
    return m_flawCandidates.empty();
  }


    /**
     * Filter out if not a token
     */
    bool OpenConditionManager::staticMatch(const EntityId& entity){
      return !TokenId::convertable(entity) || FlawManager::staticMatch(entity);
    }

    void OpenConditionManager::addFlaw(const TokenId& token){
      if(token->isInactive() && !staticMatch(token)){
	debugMsg("OpenConditionManager:addFlaw",
		 "Adding " << token->toString() << " as a candidate flaw.");
	m_flawCandidates.insert(token);
      }
    }

    void OpenConditionManager::removeFlaw(const TokenId& token){
      condDebugMsg(m_flawCandidates.find(token) != m_flawCandidates.end(), "OpenConditionManager:removeFlaw", "Removing " << token->toString() << " as a flaw.");
      m_flawCandidates.erase(token);
    }

    void OpenConditionManager::notifyRemoved(const ConstrainedVariableId& variable){
      FlawManager::notifyRemoved(variable);
      if(Token::isStateVariable(variable))
	removeFlaw(variable->parent());
    }

    void OpenConditionManager::notifyChanged(const ConstrainedVariableId& variable, 
					     const DomainListener::ChangeType& changeType){
      if(!Token::isStateVariable(variable))
	return;

      if(changeType == DomainListener::RESET)
	addFlaw(variable->parent());
      else if(changeType == DomainListener::SET_TO_SINGLETON)
	removeFlaw(variable->parent());
      else if(changeType == DomainListener::CLOSED)
	addFlaw(variable->parent());
    }

    DecisionPointId OpenConditionManager::nextZeroCommitmentDecision()
    {
    	static LabelStr explanation("Processing action effects first");

    	for(TokenSet::const_iterator it=m_flawCandidates.begin();it != m_flawCandidates.end();++it) {
    		TokenId token = *it;
    		// Deal with action effects right away
    		if (token->hasAttributes(PSTokenType::EFFECT)) {
    		      FlawHandlerId flawHandler = getFlawHandler(token);
    		      checkError(flawHandler.isValid(), "No flawHandler for " << token->toString());
    		      DecisionPointId dp =  flawHandler->create(m_db->getClient(), token, explanation);
    		      return dp;
    		}
    	}

    	return DecisionPointId::noId();
    }

    std::string OpenConditionManager::toString(const EntityId& entity) const {
      checkError(TokenId::convertable(entity), entity->toString());
      TokenId token = entity;
      std::stringstream os;
      os << "TOKEN: " << token->toString();
      return os.str();
    }

    class OpenConditionIterator : public FlawIterator {
    public:
      OpenConditionIterator(OpenConditionManager& manager)
	: FlawIterator(manager), m_it(manager.m_flawCandidates.begin()), m_end(manager.m_flawCandidates.end()) {
	advance();
      }

    private:
      const EntityId nextCandidate() {
	EntityId candidate;
	if(m_it != m_end){
	  candidate = *m_it;
	  ++m_it;
	}
	return candidate;
      }

      TokenSet::const_iterator m_it;
      TokenSet::const_iterator m_end;
    };

    IteratorId OpenConditionManager::createIterator() {
      return (new OpenConditionIterator(*this))->getId();
    }
  }
}
