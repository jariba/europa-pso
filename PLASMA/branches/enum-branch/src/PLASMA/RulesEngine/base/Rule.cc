#include "Rule.hh"
#include "RuleInstance.hh"
#include "RulesEngine.hh"
#include "RuleVariableListener.hh"
#include "Token.hh"
#include "PlanDatabase.hh"
#include "Schema.hh"

namespace EUROPA {

    RuleSchema::RuleSchema()
        : m_id(this)
    {        
    }
    
    RuleSchema::~RuleSchema()
    {
        purgeAll();
        m_id.remove();        
    }
    
    const RuleSchemaId& RuleSchema::getId() const {return m_id;}
    
    void RuleSchema::registerRule(const RuleId& rule)
    {
        m_rulesByName.insert(std::make_pair(rule->getName().getKey(), rule->getId()));      
    }

    void RuleSchema::getRules(const PlanDatabaseId& pdb, const LabelStr& name, std::vector<RuleId>& results)
    {
        const SchemaId& schema = pdb->getSchema();

        std::multimap<edouble, RuleId>::const_iterator it = m_rulesByName.find(name.getKey());
        while(it != m_rulesByName.end()){
            RuleId rule = it->second;
            check_error(rule.isValid());

            if(rule->getName() != name)
                break;

            results.push_back(rule);
            ++it;
        }

        // If the predicate is defined on the parent class, then
        // call this function recursively
        if(schema->hasParent(name))
            getRules(pdb,schema->getParent(name), results);
    }

    const std::multimap<edouble, RuleId>& RuleSchema::getRules()
    {
        return m_rulesByName;
    }

    void RuleSchema::purgeAll()
    {
        std::multimap<edouble, RuleId>& rules = m_rulesByName;
        for(std::multimap<edouble, RuleId>::const_iterator it = rules.begin(); it != rules.end(); ++it){
            RuleId rule = it->second;
            delete (Rule*) rule;
        }

        rules.clear();
    }

    Rule::Rule(const LabelStr& name)
        : m_id(this)
        , m_name(name)
        , m_source("noSrc") 
    {
    }

    Rule::Rule(const LabelStr &name, const LabelStr &source)
        : m_id(this)
        , m_name(name)
        , m_source(source) 
    {
    }

    Rule::~Rule()
    {
        check_error(m_id.isValid());
        m_id.remove();
    }

    const RuleId& Rule::getId() const {return m_id;}

    const LabelStr& Rule::getName() const {return m_name;}
    
    const LabelStr& Rule::getSource() const {return m_source;}
}
