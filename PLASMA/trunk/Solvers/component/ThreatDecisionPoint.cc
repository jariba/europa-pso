#include "ThreatDecisionPoint.hh"
#include "Token.hh"
#include "Object.hh"
#include "DbClient.hh"
#include "Debug.hh"
#include "PlanDatabase.hh"

/**
 * @author Conor McGann
 * @date March, 2005
 */

namespace EUROPA {
  namespace SOLVERS {

    bool ThreatDecisionPoint::test(const EntityId& entity){ 
      return(TokenId::convertable(entity) || TokenId(entity)->isActive());
    }

    ThreatDecisionPoint::ThreatDecisionPoint(const DbClientId& client, const TokenId& tokenToOrder, const TiXmlElement& configData)
      : DecisionPoint(client, tokenToOrder->getKey()), m_tokenToOrder(tokenToOrder), m_index(0) {
      // Here is where we would look for custom processing for configuration of the decision point
    }

    void ThreatDecisionPoint::handleExecute(){
      checkError(m_index < m_choiceCount, "Tried to execute past available choices:" << m_index << ">=" << m_choiceCount);
      ObjectId object;
      TokenId predecessor;
      TokenId successor;
      extractParts(m_index, object, predecessor, successor);

      checkError(predecessor == m_tokenToOrder || successor == m_tokenToOrder,
		 "Given token must be part of assignment." 
		 << m_tokenToOrder->toString() << ";" << predecessor->toString() << "; " << successor->toString());

      debugMsg("SolverDecisionPoint:handleExecute", "For " << m_tokenToOrder->getPredicateName().toString() << "(" <<
               m_tokenToOrder->getKey() << "), assigning " << predecessor->getPredicateName().toString() << "(" <<
               predecessor->getKey() << ") to be before " << successor->getPredicateName().toString() << "(" <<
               successor->getKey() << ").");
      m_client->constrain(object, predecessor, successor);
    }

    void ThreatDecisionPoint::handleUndo(){
      debugMsg("SolverDecisionPoint:handleUndo", "Retracting ordering decision on " << m_tokenToOrder->getPredicateName().toString() <<
               "(" << m_tokenToOrder->getKey() << ").");
      ObjectId object;
      TokenId predecessor;
      TokenId successor;
      extractParts(m_index, object, predecessor, successor);
      checkError(predecessor == m_tokenToOrder || successor == m_tokenToOrder,
		 "Given token must be part of assignment."
		 << m_tokenToOrder->toString() << ";" << predecessor->toString() << "; " << successor->toString());

      m_client->free(object, predecessor, successor);
      m_index++; // Advance to next choice
    }

    bool ThreatDecisionPoint::hasNext() const {
      return m_index < m_choiceCount;
    }

    /**
     * @brief populate over all objects in the tokens object domain. Should customize to change the ordering.
     */
    void ThreatDecisionPoint::handleInitialize() {
      m_tokenToOrder->getPlanDatabase()->getOrderingChoices(m_tokenToOrder, m_choices);
      m_choiceCount = m_choices.size();
    }

    std::string ThreatDecisionPoint::toString() const {
      std::stringstream strStream;
      strStream << "TOKEN=" << m_tokenToOrder->toString() << " CHOICES:";

      for (unsigned int i = 0; i < m_choiceCount; i++)
	strStream << toString(i, m_choices[i]) << " ";

      return strStream.str();
    }

    std::string ThreatDecisionPoint::toString(unsigned int index, const std::pair<ObjectId, std::pair<TokenId, TokenId> >& choice) const {
      std::stringstream strStream;
      ObjectId object;
      TokenId predecessor;
      TokenId successor;
      extractParts(index, object, predecessor, successor);
      strStream << "<" << object->getName().toString() << ":" << predecessor->toString() << ":" << successor->toString() << ">";
      return strStream.str();
    }

    void ThreatDecisionPoint::extractParts(unsigned int index, ObjectId& object, TokenId& predecessor, TokenId& successor) const{
      const std::pair<ObjectId, std::pair<TokenId, TokenId> >& choice = m_choices[index];
      object = choice.first;
      predecessor = choice.second.first;
      successor = choice.second.second;
    }
  }
}
