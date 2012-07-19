#ifndef _H_ResourceTokenRelation
#define _H_ResourceTokenRelation

#include "ConstraintEngineDefs.hh"
#include "Constraint.hh"
#include "DomainListener.hh"
#include "PlanDatabaseDefs.hh"
#include "ResourceDefs.hh"
#include "Resource.hh"
#include "LabelStr.hh"

#include <vector>

namespace EUROPA {

	//listens on the state and object var.
    //when the state is singleton and ACTIVE and object is singleton, then adds the token to the profile
    class ResourceTokenRelation : public Constraint {
    public:
      ResourceTokenRelation(const ConstraintEngineId& constraintEngine,
			    const std::vector<ConstrainedVariableId>& scope,
			    const TokenId& tok);
      ~ResourceTokenRelation();
      static const LabelStr& CONSTRAINT_NAME() {
	static const LabelStr sl_const("ResourceObjectRelation");
	return sl_const;
      }
      static const LabelStr& PROPAGATOR_NAME() {
	static const LabelStr sl_const("Resource");
	return sl_const;
      }
      static const int STATE_VAR = 0;
      static const int OBJECT_VAR = 1;

      virtual std::string getViolationExpl() const;

      TokenId getToken() const;
      ResourceId getResource() const;
      std::pair<eint,Resource::ProblemType> getViolationInfo() const;

    protected:
      virtual void notifyViolated(Resource::ProblemType problem, const InstantId inst);
      virtual void notifyNoLongerViolated();

      // TODO: these should be handleDeactivate and handleActivate, but need to fix ViolationManager first
      friend class ProfilePropagator;
      friend class BatchModeListener;
      virtual void disable();
      virtual void enable();

      void connect();
      void disconnect();
      void safeConnect();
      void safeDisconnect();

    private:
      void handleDiscard();
      bool canIgnore(const ConstrainedVariableId& variable,
		     int argIndex,
		     const DomainListener::ChangeType& changeType);
      void handleExecute(){}
      TokenId m_token;
      ResourceId m_resource;

      eint m_violationTime;
      Resource::ProblemType m_violationProblem;

      friend class Resource;
    };
}

#endif
