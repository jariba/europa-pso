#ifndef _H_SAVH_ResourceTokenRelation
#define _H_SAVH_ResourceTokenRelation

#include "ConstraintEngineDefs.hh"
#include "Constraint.hh"
#include "DomainListener.hh"
#include "PlanDatabaseDefs.hh"
#include "SAVH_ResourceDefs.hh"
#include "ResourceProblem.hh"
#include "LabelStr.hh"

#include <vector>

namespace EUROPA {
  namespace SAVH {

    //listens on the state and object var.
    //when the state is singleton and ACTIVE and object is singleton, then adds the token to the profile
    class ResourceTokenRelation : public Constraint {
    public:
      ResourceTokenRelation(const ConstraintEngineId& constraintEngine,
			    const std::vector<ConstrainedVariableId>& scope,
			    const TokenId& tok);
      static const LabelStr& CONSTRAINT_NAME() {
	static const LabelStr sl_const("ResourceObjectRelation");
	return sl_const;
      }
      static const LabelStr& PROPAGATOR_NAME() {
	static const LabelStr sl_const("SAVH_Resource");
	return sl_const;
      }
      static const int STATE_VAR = 0;
      static const int OBJECT_VAR = 1;
            
      virtual std::string getViolationExpl() const;
      
    protected:
      void notifyViolated(ResourceProblem::Type problem, const InstantId inst);
      
    private:
      bool canIgnore(const ConstrainedVariableId& variable,
		     int argIndex,
		     const DomainListener::ChangeType& changeType);
      void handleExecute(){}
      TokenId m_token;
      ResourceId m_resource;

      InstantId m_violationInstant;
      ResourceProblem::Type m_violationProblem;
      
      friend class Resource;
    };
  }
}
#endif
