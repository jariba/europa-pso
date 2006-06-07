#include "SAVH_Instant.hh"
#include "SAVH_InstantTokens.hh"
#include "SAVH_Profile.hh"
#include "SAVH_Reservoir.hh"
#include "SAVH_Transaction.hh"
#include "ConstraintEngine.hh"
#include "IntervalDomain.hh"
#include "IntervalIntDomain.hh"
#include "PlanDatabase.hh"
#include "Token.hh"
#include "TokenVariable.hh"

namespace EUROPA {
  namespace SAVH {
    Reservoir::Reservoir(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name, const LabelStr& detectorName, const LabelStr& profileName,
			 double initCapacityLb, double initCapacityUb, double lowerLimit, double upperLimit, double maxInstProduction, double maxInstConsumption,
			 double maxProduction, double maxConsumption) :
      Resource(planDatabase, type, name, detectorName, profileName, initCapacityLb, initCapacityUb, lowerLimit, upperLimit, maxInstProduction,
	       maxInstConsumption, maxProduction, maxConsumption) {}

    Reservoir::Reservoir(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name, bool open) :
      Resource(planDatabase, type, name, open) {}

    Reservoir::Reservoir(const ObjectId& parent, const LabelStr& type, const LabelStr& localName, bool open) :
      Resource(parent, type, localName, open) {}

    void Reservoir::addToProfile(const TokenId& tok) {
      checkError(m_tokensToTransactions.find(tok) == m_tokensToTransactions.end(),
		 "Token " << tok->getPredicateName().toString() << "(" << tok->getKey() << ") is already in the profile.");
      ReservoirTokenId t(tok);
      debugMsg("Reservoir:addToProfile", "Adding " << (t->isConsumer() ? "consumer " : "producer ") << "token " << 
	       tok->getPredicateName().toString() << "(" << tok->getKey() << ")");
      TransactionId trans = (new Transaction(t->getTime(), t->getQuantity(), t->isConsumer()))->getId();
      m_transactionsToTokens.insert(std::pair<TransactionId, TokenId>(trans, tok));
      m_tokensToTransactions.insert(std::pair<TokenId, TransactionId>(tok, trans));
      m_profile->addTransaction(trans);
    }

    void Reservoir::removeFromProfile(const TokenId& tok) {
//       checkError(m_tokensToTransactions.find(tok) != m_tokensToTransactions.end(),
// 		 "Token " << tok->getPredicateName().toString() << "(" << tok->getKey() << ") isn't in the profile.");
      if(m_tokensToTransactions.find(tok) == m_tokensToTransactions.end())
	return;
      debugMsg("Reservoir:removeFromProfile", "Removing token " << tok->getPredicateName().toString() << "(" << tok->getKey() << ")");
      ReservoirTokenId t(tok); //just for error checking
      TransactionId trans = m_tokensToTransactions.find(tok)->second;
      m_profile->removeTransaction(trans);
      m_tokensToTransactions.erase(tok);
      m_transactionsToTokens.erase(trans);
      m_flawedTokens.erase(tok);
      delete (Transaction*) trans;
    }

    void Reservoir::getOrderingChoices(const TokenId& token,
				       std::vector<std::pair<TokenId, TokenId> >& results,
				       unsigned int limit) {
      checkError(m_tokensToTransactions.find(token) != m_tokensToTransactions.end(), "Token " << token->getPredicateName().toString() << 
		 "(" << token->getKey() << ") not in profile.");
      Resource::getOrderingChoices(token, results, limit);
    }
  }
}
