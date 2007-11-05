/**
 * @file Main.cc
 *
 * @brief Provides an executable for your project which will use a
 * standard Chronological backtracking planner and a StandardAssembly or a PSEngine
 * to encapsulate EUROPA
 */

#include "Nddl.hh" /*!< Includes protypes required to load a model */
#include "SolverAssemblyWithResources.hh" /*!< For using a test EUROPA Assembly */
#include "PSResources.hh" 
#include "Debug.hh"

using namespace EUROPA;

void executeWithAssembly(const char* plannerConfig, const char* txSource);
bool executeWithPSEngine(const char* plannerConfig, const char* txSource, int startHorizon, int endHorizon, int maxSteps);
void printFlaws(int it, PSList<std::string>& flaws);

int main(int argc, const char ** argv)
{
  if (argc != 3) {
    std::cerr << "Must provide initial transactions file." << std::endl;
    return -1;
  }

  const char* txSource = argv[1];
  const char* plannerConfig = argv[2];
  
  executeWithAssembly(plannerConfig,txSource);
  
  /*
  executeWithPSEngine(
      plannerConfig,
      txSource,
      0,   // startHorizon
      100, // endHorizon
      1000 // maxSteps
  ); 
  */
     
  return 0;
}

void executeWithAssembly(const char* plannerConfig, const char* txSource)
{
  // Initialize Library  
  SolverAssemblyWithResources::initialize();

  // Allocate the schema with a call to the linked in model function - eventually
  // make this called via dlopen
  SchemaId schema = NDDL::loadSchema();

  // Enacpsualte allocation so that they go out of scope before calling terminate
  {  
    // Allocate the test assembly.
    SolverAssemblyWithResources assembly(schema);

    // Run the planner
    assembly.plan(txSource, plannerConfig);

    // Dump the results
    assembly.write(std::cout);
  }

  // Terminate the library
  SolverAssemblyWithResources::terminate();

  std::cout << "Finished\n";
}

bool executeWithPSEngine(const char* plannerConfig, const char* txSource, int startHorizon, int endHorizon, int maxSteps)
{
    try {
	  PSEngineWithResources engine;
	
	  engine.start();
	  engine.executeTxns(txSource,true,true);
	
	  PSSolver* solver = engine.createSolver(plannerConfig);
	  solver->configure(startHorizon,endHorizon);
	  int i;
      for (i = 0; 
           !solver->isExhausted() &&
           !solver->isTimedOut() &&
           i<maxSteps; i = solver->getStepCount()) {
		  solver->step();
		  PSList<std::string> flaws;
		  if (solver->isConstraintConsistent()) {
	          flaws = solver->getFlaws();
        	  printFlaws(i,flaws);
			  if (flaws.size() == 0)
			      break;
		  }
		  else
			debugMsg("Main","Iteration " << i << " Solver is not constraint consistent");
	  }
	  
	  if (solver->isExhausted()) {
	      debugMsg("Main","Solver was exhausted after " << i << " steps");	  
	  }
	  else if (solver->isTimedOut()) {
	      debugMsg("Main","Solver timed out after " << i << " steps");
	  }
	  else {    
	      debugMsg("Main","Solver finished after " << i << " steps");
	  }	      
	      
	  delete solver;	
	  engine.shutdown();

	  return true;
	}
	catch (Error& e) {
		std::cerr << "PSEngine failed:" << e.getMsg() << std::endl;
		return false;
	}	
}

void printFlaws(int it, PSList<std::string>& flaws)
{
	debugMsg("Main","Iteration:" << it << " " << flaws.size() << " flaws");
	
	for (int i=0; i<flaws.size(); i++) {
		std::cout << "    " << (i+1) << " - " << flaws.get(i) << std::endl;
	}
}


