#include "PSResources.hh"
#include "FlawHandler.hh"
#include "SAVH_Resource.hh"
#include "SAVH_Profile.hh"
#include "SAVH_ProfilePropagator.hh"
#include "ResourcePropagator.hh"
// TODO: registration for these needs to happen somewhere else
#include "SAVH_ReusableFVDetector.hh"
#include "SAVH_FlowProfile.hh"
#include "SAVH_IncrementalFlowProfile.hh"
#include "SAVH_TimetableProfile.hh"
#include "ResourceThreatDecisionPoint.hh"
#include "TransactionInterpreterResources.hh"

namespace EUROPA {

  PSEngineWithResources::PSEngineWithResources()
      : PSEngineImpl()
  {
	  REGISTER_PROFILE(EUROPA::SAVH::TimetableProfile, TimetableProfile );
      REGISTER_PROFILE(EUROPA::SAVH::FlowProfile, FlowProfile);
      REGISTER_PROFILE(EUROPA::SAVH::IncrementalFlowProfile, IncrementalFlowProfile );
      
      REGISTER_FVDETECTOR(EUROPA::SAVH::ReusableFVDetector, ReusableFVDetector);
      REGISTER_FLAW_HANDLER(EUROPA::SOLVERS::ResourceThreatDecisionPoint, ResourceThreatDecisionPoint);
      
   	  // Explicit reference is needed so that static initializer isn't dropped when static libs are used.
   	  TransactionInterpreterResourcesInitializer::getInstance(); 	
  }

  void PSEngineWithResources::start() 
  {
    PSEngineImpl::start();

  }

  void PSEngineWithResources::initDatabase() 
  {
    PSEngineImpl::initDatabase();
    new SAVH::ProfilePropagator(LabelStr("SAVH_Resource"), m_constraintEngine);
    new ResourcePropagator(LabelStr("Resource"), m_constraintEngine, m_planDatabase);
  }

  PSList<PSResource*> PSEngineWithResources::getResourcesByType(const std::string& objectType) {
    check_runtime_error(m_planDatabase.isValid());
    
    PSList<PSResource*> retval;
    
    const ObjectSet& objects = m_planDatabase->getObjects();
    for(ObjectSet::const_iterator it = objects.begin(); it != objects.end(); ++it){
      ObjectId object = *it;
      if(Schema::instance()->isA(object->getType(), objectType.c_str()))
	    retval.push_back(dynamic_cast<PSResource*>(getObjectWrapperGenerator(object->getType())->wrap(object)));
    }
    
    return retval;
  }
  
  PSResource* PSEngineWithResources::getResourceByKey(PSEntityKey id) {
    check_runtime_error(m_planDatabase.isValid());

    EntityId entity = Entity::getEntity(id);
    check_runtime_error(entity.isValid());
    return new PSResource(entity);
  }
  
  PSResource::PSResource(const SAVH::ResourceId& res) : PSObjectImpl(res), m_res(res) {}

  PSResourceProfile* PSResource::getLimits() {
    return new PSResourceProfile(m_res->getLowerLimit(), m_res->getUpperLimit());
  }

  PSResourceProfile* PSResource::getLevels() {
    return new PSResourceProfile(m_res->getProfile());
  }
  
  PSList<PSEntityKey> PSResource::getOrderingChoices(TimePoint t)
  {
	  PSList<PSEntityKey> retval;
	  
	  SAVH::InstantId instant;
	  
	  SAVH::ProfileIterator it(m_res->getProfile());
	  while(!it.done()) {
	      TimePoint inst = (TimePoint) it.getTime();
	      if (inst == t) {
	          instant = it.getInstant();
	          break;
	      }
	      it.next();
	  }
	  
	  if (instant.isNoId()) {
		  // TODO: log error
		  return retval;
	  }
	  
	  std::vector<std::pair<SAVH::TransactionId, SAVH::TransactionId> > results;
	  m_res->getOrderingChoices(instant,results);
	  for (unsigned int i = 0;i<results.size(); i++) {
	      SAVH::TransactionId predecessor = results[i].first;
	      SAVH::TransactionId successor = results[i].second;	
	      retval.push_back(predecessor->time()->getParent()->getKey());
	      retval.push_back(successor->time()->getParent()->getKey());
	  }
	  
	  return retval;
  }


  PSResourceProfile::PSResourceProfile(const double lb, const double ub)
    : m_isConst(true), m_lb(lb), m_ub(ub) {
    TimePoint inst = (TimePoint) MINUS_INFINITY;
    m_times.push_back(inst);
  }

  PSResourceProfile::PSResourceProfile(const SAVH::ProfileId& profile)
    : m_isConst(false), m_profile(profile) {
    SAVH::ProfileIterator it(m_profile);
    while(!it.done()) {
      TimePoint inst = (TimePoint) it.getTime();
      m_times.push_back(inst);
      it.next();
    }
  }

  double PSResourceProfile::getLowerBound(TimePoint time) {
    if(m_isConst)
      return m_lb;

    IntervalDomain dom;
    m_profile->getLevel((int) time, dom);
    return dom.getLowerBound();
  }

  double PSResourceProfile::getUpperBound(TimePoint time) {
    if(m_isConst)
      return m_ub;
    IntervalDomain dom;
    m_profile->getLevel((int) time, dom);
    return dom.getUpperBound();
  }

  const PSList<TimePoint>& PSResourceProfile::getTimes() {return m_times;}

  class ResourceWrapperGenerator : public ObjectWrapperGenerator {
  public:
    PSObject* wrap(const ObjectId& obj) {
      checkRuntimeError(SAVH::ResourceId::convertable(obj),
			"Object " << obj->toString() << " is not a resource.");
      return new PSResource(SAVH::ResourceId(obj));
    }
  };
    
  class PSResourceLocalStatic {
  public:
    PSResourceLocalStatic() {
      PSEngineImpl::addObjectWrapperGenerator("Reservoir", new ResourceWrapperGenerator());
      PSEngineImpl::addObjectWrapperGenerator("Reusable", new ResourceWrapperGenerator());
      PSEngineImpl::addObjectWrapperGenerator("Unary", new ResourceWrapperGenerator());
    }
  };

  namespace PSResources {
    PSResourceLocalStatic s_localStatic;
  }
}
