/*
 * PSEngine.cc
 *
 */
 
#include "PSEngineImpl.hh"

#include "LabelStr.hh"
#include "Pdlfcn.hh"
#include "Schema.hh"

#include "Constraint.hh"
#include "PlanDatabase.hh"
#include "PSConstraintEngineImpl.hh"
#include "PSPlanDatabaseImpl.hh"
#include "PSSolversImpl.hh"

namespace EUROPA {
  
  void PSEngine::initialize()
  {
	PSEngineImpl::initialize();  
  }
  
  void PSEngine::terminate()
  {
	PSEngineImpl::terminate();  
  }
  
  PSEngine* PSEngine::makeInstance()
  {
	  return new PSEngineImpl();
  }

  PSEngineImpl::PSEngineImpl() 
      : m_started(false)
  {
  }

  PSEngineImpl::~PSEngineImpl() 
  {
	  if (m_started)
		  shutdown();
  }

  void PSEngineImpl::initialize()
  {
    Error::doThrowExceptions(); // throw exceptions!
    Error::doDisplayErrors();    
	EngineBase::initialize();  
  }
  
  void PSEngineImpl::terminate()
  {
	EngineBase::terminate();  
  }
 

  void PSEngineImpl::start() 
  {		
	if (m_started)
		return;
	
    doStart();   
    m_started = true;
  }

  void PSEngineImpl::shutdown() 
  {
	if (!m_started)
		return;
	
    doShutdown();    
    m_started = false;
  }
    
  void PSEngineImpl::allocateComponents()
  {
	  EngineBase::allocateComponents();
	  m_psSolverManager = new PSSolverManagerImpl(m_constraintEngine,m_planDatabase,m_rulesEngine);
  }
  
  void PSEngineImpl::deallocateComponents()
  {
	  delete m_psSolverManager;
	  EngineBase::deallocateComponents();
  }
  
  EngineComponent* PSEngineImpl::getComponentPtr(const std::string& name)
  {
  	  if (name == "PSSolverManager")
  		  return (EngineComponent*)m_psSolverManager;
  	  
      return EngineBase::getComponent(name);  	
  }   
  
  
  // TODO:  Note similarity with loadModel (ugly code duplication).  However, the last line here is different.
  void PSEngineImpl::loadModule(const std::string& moduleFileName) 
   {
     check_runtime_error(m_started,"PSEngine has not been started");
 	    
     void* libHandle = p_dlopen(moduleFileName.c_str(), RTLD_NOW);
     checkRuntimeError(libHandle != NULL,
 	       "Error opening module " << moduleFileName << ": " << p_dlerror());

     ModuleId (*fcn_module)();
     fcn_module = (ModuleId (*)()) p_dlsym(libHandle, "initializeModule");
     checkError(fcn_module != NULL,
 	       "Error locating symbol 'initializeModule' in " << moduleFileName << ": " <<
 	       p_dlerror());

     ModuleId module = (*fcn_module)();
     EngineBase::addModule(module);
  }
  
  void PSEngineImpl::loadModel(const std::string& modelFileName) 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
	    
    void* libHandle = p_dlopen(modelFileName.c_str(), RTLD_NOW);
    checkRuntimeError(libHandle != NULL,
	       "Error opening model " << modelFileName << ": " << p_dlerror());

    SchemaId (*fcn_schema)();
    fcn_schema = (SchemaId (*)()) p_dlsym(libHandle, "loadSchema");
    checkError(fcn_schema != NULL,
	       "Error locating symbol 'loadSchema' in " << modelFileName << ": " <<
	       p_dlerror());

    SchemaId schema = (*fcn_schema)();
  }
  
  std::string PSEngineImpl::executeScript(const std::string& language, const std::string& script, bool isFile) 
  {
    return EngineBase::executeScript(language,script,isFile);
  }

  // Plan Database methods
  PSList<PSObject*> PSEngineImpl::getObjectsByType(const std::string& objectType) 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_planDatabase->getObjectsByType(objectType);  
  }

  PSObject* PSEngineImpl::getObjectByKey(PSEntityKey id) 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_planDatabase->getObjectByKey(id);  
  }

  PSObject* PSEngineImpl::getObjectByName(const std::string& name) 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_planDatabase->getObjectByName(name);  
  }

  PSList<PSToken*> PSEngineImpl::getTokens() 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_planDatabase->getAllTokens();  
  }

  PSToken* PSEngineImpl::getTokenByKey(PSEntityKey id) 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_planDatabase->getTokenByKey(id);  
  }

  PSList<PSVariable*>  PSEngineImpl::getGlobalVariables() 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_planDatabase->getAllGlobalVariables();  
  }  

  std::string PSEngineImpl::planDatabaseToString() 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
	return m_planDatabase->toString();  
  }

  // Constraint Engine methods
  PSVariable* PSEngineImpl::getVariableByKey(PSEntityKey id)
  {
    check_runtime_error(m_started,"PSEngine has not been started");
	return m_constraintEngine->getVariableByKey(id);  
  }

  PSVariable* PSEngineImpl::getVariableByName(const std::string& name)
  {
    check_runtime_error(m_started,"PSEngine has not been started");
	return m_constraintEngine->getVariableByName(name);  
  }

  // TODO: these 2 need to be pushed into the PSConstraintEngine
  PSConstraint* PSEngineImpl::addConstraint(const std::string& type, PSList<PSVariable*> args)
  {
      std::vector<ConstrainedVariableId> variables;
      for (int i=0;i<args.size();i++) {
          ConstrainedVariableId arg = m_planDatabase->getEntityByKey(args.get(i)->getEntityKey());
          variables.push_back(arg);
      }
      
      ConstraintId c = m_planDatabase->getClient()->createConstraint(type.c_str(), variables);

      // TODO: this must be pushed to the CE
      if (getAutoPropagation())
          propagate();
      
      return (Constraint*)c;      
  }
  
  void PSEngineImpl::removeConstraint(PSEntityKey id)
  {
      ConstraintId c = m_planDatabase->getEntityByKey(id);
      m_planDatabase->getClient()->deleteConstraint(c);      
      // TODO: this must be pushed to the CE
      if (getAutoPropagation())
          propagate();      
  }
  
  bool PSEngineImpl::getAutoPropagation() const
  {
    return m_constraintEngine->getAutoPropagation();
  }

  void PSEngineImpl::setAutoPropagation(bool v)      
  {
    m_constraintEngine->setAutoPropagation(v);
  }

  bool PSEngineImpl::propagate() 
  {
    return m_constraintEngine->propagate();
  }
    
  bool PSEngineImpl::getAllowViolations() const
  {
  	return m_constraintEngine->getAllowViolations();
  }

  void PSEngineImpl::setAllowViolations(bool v)
  {
    m_constraintEngine->setAllowViolations(v);
  }

  double PSEngineImpl::getViolation() const
  {
  	return m_constraintEngine->getViolation();
  }
   
  std::string PSEngineImpl::getViolationExpl() const
  {
  	return m_constraintEngine->getViolationExpl();
  }

  // Solver methods
  PSSolver* PSEngineImpl::createSolver(const std::string& configurationFile) 
  {
    check_runtime_error(m_started,"PSEngine has not been started");
    return m_psSolverManager->createSolver(configurationFile);
  }
}

