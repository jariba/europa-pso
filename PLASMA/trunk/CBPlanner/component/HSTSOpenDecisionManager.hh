#ifndef _H_HSTSOpenDecisionManager
#define _H_HSTSOpenDecisionManager

#include "CBPlannerDefs.hh"
#include "DefaultOpenDecisionManager.hh"

namespace Prototype {

  class HSTSObjectDecisionPointComparator : ObjectDecisionPointComparator {
  public:
    bool operator()(const ObjectDecisionPointId& o1, const ObjectDecisionPointId& o2) const {
      return o1->getEntityKey() == o2->getEntityKey() && o1->getToken()->getKey() == o2->getToken()->getKey();
    }
  };

  class HSTSTokenDecisionPointComparator : TokenDecisionPointComparator {
  public:
    bool operator()(const TokenDecisionPointId& t1, const TokenDecisionPointId& t2) const {
      return t1->getKey() < t2->getKey();
    }
  };

  class HSTSConstrainedVariableDecisionPointComparator : ConstrainedVariableDecisionPointComparator {
  public:
    bool operator()(const ConstrainedVariableDecisionPointId& t1, const ConstrainedVariableDecisionPointId& t2) const {
      return t1->getKey() < t2->getKey();
    }
  };

  typedef std::set<ObjectDecisionPointId, HSTSObjectDecisionPointComparator> HSTSObjectDecisionSet;
  typedef std::set<TokenDecisionPointId, HSTSTokenDecisionPointComparator> HSTSTokenDecisionSet;
  typedef std::set<ConstrainedVariableDecisionPointId, HSTSConstrainedVariableDecisionPointComparator> HSTSVariableDecisionSet;
  

  class HSTSOpenDecisionManager : public DefaultOpenDecisionManager {
  public:

    HSTSOpenDecisionManager(const DecisionManagerId& dm);
    ~HSTSOpenDecisionManager();

    virtual DecisionPointId getNextDecision();
    virtual const ChoiceId getNextChoice();

    // order returned is different
    virtual void getOpenDecisions(std::list<DecisionPointId>& decisions);
    virtual void printOpenDecisions(std::ostream& os);
  protected:
    friend class DecisionManager;

    // need to add sorted object decs
    virtual void cleanupAllDecisionCaches();

    // need to add sorted object store
    virtual void deleteAllMatchingObjects(const ObjectId& object, const TokenId& token);
    virtual void add(const ObjectId& object, const TokenId& token);
    virtual void add(const ObjectId& object);
    virtual void condAdd(const TokenId& token);
    virtual void add(const TokenId& token);
    virtual void condAdd(const ConstrainedVariableId& var, const bool units);
    virtual void add(const ConstrainedVariableId& variable);

    // need to add sorted object store
    virtual void removeObject(const ObjectId& object, const TokenId& token, const bool deleting);

    HSTSVariableDecisionSet m_sortedUnitVarDecs;
    HSTSVariableDecisionSet m_sortedNonUnitVarDecs;
    HSTSTokenDecisionSet m_sortedTokDecs;
    HSTSObjectDecisionSet m_sortedObjectDecs;
  };

}

#endif
