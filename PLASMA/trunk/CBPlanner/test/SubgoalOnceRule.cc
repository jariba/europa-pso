#include "PlanDatabaseDefs.hh"
#include "ConstraintLibrary.hh"
#include "RuleInstance.hh"
#include "PlanDatabase.hh"
#include "IntervalToken.hh"
#include "SubgoalOnceRule.hh"
#include "Utils.hh"

namespace Prototype {

  class SubgoalOnceRuleRoot : public RuleInstance {
  public:
    SubgoalOnceRuleRoot(const RuleId& rule, const TokenId& token, const PlanDatabaseId& pdb, int count) : RuleInstance(rule, token, pdb, token->getObject()) { 
      m_count = count;
    }
    void handleExecute();
  private:
    int m_count;
  }; 

  int SubgoalOnceRule::m_count = 0;

  RuleInstanceId SubgoalOnceRule::createInstance(const TokenId& token, const PlanDatabaseId& pdb) const {
    RuleInstanceId rootInstance = (new SubgoalOnceRuleRoot(m_id, token, pdb, m_count))->getId();
    m_count++; 
    return rootInstance;
  }

  SubgoalOnceRule::SubgoalOnceRule(const LabelStr& name, int count) : Rule(name) {}

  SubgoalOnceRule::~SubgoalOnceRule() { m_count = 0; }

  void SubgoalOnceRuleRoot::handleExecute() {
    TokenId tok =  (new IntervalToken(m_token,  
				      LabelStr("P1"), 
				      IntervalIntDomain(0, 10),
				      IntervalIntDomain(0, 20),
				      IntervalIntDomain(1, 1000),
				      Token::noObject(), false))->getId();
    std::list<Prototype::LabelStr> values;
    values.push_back(Prototype::LabelStr("L1"));
    values.push_back(Prototype::LabelStr("L4"));
    values.push_back(Prototype::LabelStr("L2"));
    values.push_back(Prototype::LabelStr("L5"));
    values.push_back(Prototype::LabelStr("L3"));
    tok->addParameter(LabelSet(values, true));
    tok->addParameter(IntervalIntDomain(1,20));
    tok->close();
    TokenId slave = addSlave(tok);
    if (m_count < 2)
      addConstraint(LabelStr("eq"), makeScope(m_token->getEnd(), slave->getStart()));
    else {
      ConstraintId start = ConstraintLibrary::createConstraint(LabelStr("Singleton"), m_planDb->getConstraintEngine(), slave->getStart(), IntervalIntDomain(4,4));
      ConstraintId end = ConstraintLibrary::createConstraint(LabelStr("Singleton"), m_planDb->getConstraintEngine(), slave->getEnd(), IntervalIntDomain(10,10));
      addConstraint(start);
      addConstraint(end);
      addConstraint(LabelStr("lt"), makeScope(slave->getEnd(), slave->getStart()));
    }
  }

}
