/**
 * @file Main.cc
 *
 * @brief Provides an executable for your project which will use a
 * standard Chronological backtracking planner and a Test Assembly of
 * EUROPA
 */

#include "Nddl.hh" /*!< Includes protypes required to load a model */
#include "SolverAssembly.hh" /*!< For using a test EUROPA Assembly */
//#include "../PLASMA/DSA/base/DSA.hh"
#include "PSEngine.hh" 

using namespace EUROPA;


void printFlaws(int it, PSList<std::string>& flaws)
{
	std::cout << "Iteration:" << it << " " << flaws.size() << " flaws" << std::endl;
	
	for (int i=0; i<flaws.size(); i++) {
		std::cout << "    " << (i+1) << " - " << flaws.get(i) << std::endl;
	}
}

bool runPSEngineTest(const char* plannerConfig, const char* txSource)
{
	PSEngine engine;
	
	engine.start();
	engine.executeTxns(txSource,true,true);
	
	PSSolver* solver = engine.createSolver(plannerConfig);
	solver->configure(0,100);
	
	for (int i = 0; i<50; i++) {
		solver->step();
		PSList<std::string> flaws = solver->getFlaws();
		if (flaws.size() == 0)
		    break;
		printFlaws(i,flaws);
	}
	
	delete solver;	
	engine.shutdown();

	return false;
}

/*
bool runDSATest(const char* plannerConfig, const char* txSource)
{
    DSA::DSA& dsa = DSA::DSA::instance();
    dsa.addPlan(txSource,true);
    dsa.solverConfigure(plannerConfig,0,100);
    dsa.solverSolve(500,500);
    dsa.writePlan(std::cout);
    
	return false;
}
*/

/**
 * @brief Uses the planner to solve a planning problem
 */
int main(int argc, const char ** argv){
  if (argc != 3) {
    std::cerr << "Must provide initial transactions file." << std::endl;
    return -1;
  }

  const char* txSource = argv[1];

  const char* plannerConfig = argv[2];
  
  if (!runPSEngineTest(plannerConfig,txSource)) 
      return 0;

  /* 
  if (!runDSATest(plannerConfig,txSource))
      return 0;
  */ 

  // Initialize Library  
  SolverAssembly::initialize();

  // Allocate the schema with a call to the linked in model function - eventually
  // make this called via dlopen
  SchemaId schema = NDDL::loadSchema();

  // Enacpsualte allocation so that they go out of scope before calling terminate
  {  
    // Allocate the test assembly.
    SolverAssembly assembly(schema);

    // Run the planner
    assembly.plan(txSource, plannerConfig);

    // Dump the results
    assembly.write(std::cout);
  }

  // Terminate the library
  SolverAssembly::terminate();

  std::cout << "Finished\n";
}
