#ifndef H_OpenConditionManager
#define H_OpenConditionManager

#include "SolverDefs.hh"
#include "FlawManager.hh"
#include "OpenConditionDecisionPoint.hh"

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

      virtual bool staticMatch(const EntityId& entity);

      virtual IteratorId createIterator();

      virtual DecisionPointId nextZeroCommitmentDecision();

      virtual std::string toString(const EntityId& entity) const;

      bool noMoreFlaws();
    protected:
      virtual void addFlaw(const TokenId& token);
      virtual void removeFlaw(const TokenId& token);
      virtual void handleInitialize();

    private:
      friend class OpenConditionIterator;
      void notifyRemoved(const ConstrainedVariableId& variable);
      void notifyChanged(const ConstrainedVariableId& variable, const DomainListener::ChangeType& changeType);

      TokenSet m_flawCandidates; /*!< The set of candidate token flaws */
    };
  }
}

#define REGISTER_TOKEN_DECISION_FACTORY(CLASS, NAME)\
REGISTER_DECISION_FACTORY(CLASS, EUROPA::Token, EUROPA::SOLVERS::TokenMatchingRule, NAME);

#endif
