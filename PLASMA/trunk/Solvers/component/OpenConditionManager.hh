#ifndef H_OpenConditionManager
#define H_OpenConditionManager

#include "SolverDefs.hh"
#include "FlawManager.hh"
#include "OpenConditionDecisionPoint.hh"
#include "PlanDatabaseListener.hh"

#include <vector>

/**
 * @author Conor McGann
 * @date March, 2005
 */
namespace EUROPA {
  namespace SOLVERS {

    class OpenConditionManager: public FlawManager {
    public:
      OpenConditionManager(const TiXmlElement& configData);

      bool inScope(const EntityId& entity);

      IteratorId createIterator();

      std::string toString(const EntityId& entity) const;

    private:

      void notifyRemoved(const ConstrainedVariableId& variable);
      void notifyChanged(const ConstrainedVariableId& variable, const DomainListener::ChangeType& changeType);
      void handleInitialize();
      void addFlaw(const TokenId& token);
      void removeFlaw(const TokenId& token);

      TokenSet m_flawCandidates; /*!< The set of candidate token flaws */

      class FlawIterator : public Iterator {
      public:
	FlawIterator(OpenConditionManager& manager);
	bool done() const;
	const EntityId next();
	unsigned int visited() const {return m_visited;}
      protected:
      private:
	unsigned int m_visited;
	unsigned int m_timestamp;
	OpenConditionManager& m_manager;
	TokenSet::const_iterator m_it;
	TokenSet::const_iterator m_end;
      };
    };
  }
}

#define REGISTER_TOKEN_DECISION_FACTORY(CLASS, NAME)\
REGISTER_DECISION_FACTORY(CLASS, EUROPA::Token, EUROPA::SOLVERS::TokenMatchingRule, NAME);

#endif
