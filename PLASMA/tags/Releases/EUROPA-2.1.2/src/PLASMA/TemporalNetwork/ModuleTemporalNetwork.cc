#include "ModuleTemporalNetwork.hh"
#include "ConstraintEngine.hh"
#include "ConstraintFactory.hh"
#include "Constraints.hh"
#include "PlanDatabase.hh"
#include "TemporalPropagator.hh"
#include "STNTemporalAdvisor.hh"

namespace EUROPA {

  ModuleTemporalNetwork::ModuleTemporalNetwork()
      : Module("TemporalNetwork")
  {	  
  }

  ModuleTemporalNetwork::~ModuleTemporalNetwork()
  {	  
  }  
  
  void ModuleTemporalNetwork::initialize()
  {
  }  

  void ModuleTemporalNetwork::uninitialize()
  {
  }  
  
  void ModuleTemporalNetwork::initialize(EngineId engine)
  {
      ConstraintEngine* ce = (ConstraintEngine*)engine->getComponent("ConstraintEngine");
      CESchema* ces = ce->getCESchema();

      REGISTER_SYSTEM_CONSTRAINT(ces,EqualConstraint, "concurrent", "Temporal");
      REGISTER_SYSTEM_CONSTRAINT(ces,LessThanEqualConstraint, "precedes", "Temporal"); 
      REGISTER_SYSTEM_CONSTRAINT(ces,AddEqualConstraint, "temporalDistance", "Temporal");

	  new TemporalPropagator(LabelStr("Temporal"), ce->getId());
	  PropagatorId temporalPropagator = ce->getPropagatorByName(LabelStr("Temporal"));

	  PlanDatabase* pdb = (PlanDatabase*)engine->getComponent("PlanDatabase");
	  if (pdb != NULL)
	     pdb->setTemporalAdvisor((new STNTemporalAdvisor(temporalPropagator))->getId());
  }
  
  void ModuleTemporalNetwork::uninitialize(EngineId engine)
  {	 
      // TODO: cleanup
  }  
}
