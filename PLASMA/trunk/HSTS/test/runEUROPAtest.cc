/**
 * @file Main.cc
 *
 * @brief Provides an executable which will use the HSTS variant of the
 * standard Chronological backtracking planner and a Standard Assembly of
 * EUROPA 
 */

#include "Nddl.hh" /**< Includes protypes required to load a model */
#include "HSTSAssembly.hh" /**< For using a standard EUROPA Assembly */
#include "DNPConstraints.hh" /**< Declares the DNP specific constraints */

using namespace EUROPA;

/**
 * @brief Uses the planner to solve a planning problem.
 */
int main(int argc, const char ** argv) {
  if (argc < 3) {
    std::cerr << "Error: must provide at least an initial transactions file." << std::endl;
    return -1;
  }
  if (argc > 5) {
    std::cerr << "Error: too many arguments.  Expecting at most 6: initial transactions, heuristics, and plan id files." << std::endl;
    return -1;
  } 

  const char* txSource = "";
  const char* heurSource = "";
  const char* pidSource = "";
  for (int i = 1; i < argc; i++) {
    const char* arg = argv[i];
    if (arg[0] != '-') break;
    if (strcmp(arg,"-h") == 0) {
      i++;
      if (i >= argc) {
	std::cerr << "Error: Expected heuristics filename" << std::endl;
	return -1;
      }
      heurSource = argv[i];
      continue;
    }
    if (strcmp(arg,"-i") == 0) {
      i++;
      if (i >= argc) {
	std::cerr << "Error: Expected initial transactions filename" << std::endl;
	return -1;
      }
      txSource = argv[i];
      continue;
    }
    if (strcmp(arg,"-p") == 0) {
      i++;
      if (i >= argc) {
	std::cerr << "Error: Expected plan id filename" << std::endl;
	return -1;
      }
      pidSource = argv[i];
      continue;
    }
    std::cerr << argv[0] << ": unrecognized option `" << arg << "'" << std::endl;
    return -1;
  }

  // Initialize Library  
  HSTSAssembly::initialize();

  // Register the DNP specific constraints.
  registerDNPConstraints();

  // Allocate the schema with a call to the linked in model function - eventually
  //   make this called via dlopen.
  SchemaId schema = NDDL::loadSchema();

  // Encapsulate allocation so that they go out of scope before calling terminate.
  {  
    std::cerr << "Allocating assembly..." << std::endl;


    // Allocate the standard assembly.
    HSTSAssembly assembly(schema);

    std::cerr << "Assembly planning..." << std::endl;

    // Run the planner
    assembly.plan(txSource, heurSource, pidSource);

    std::cerr << "Finished Planning. Printing the plan..." << std::endl;

    // Dump the results
    assembly.write(std::cout);
  }

  std::cerr << "Terminating the assembly ..." << std::endl;

  // Terminate the library
  HSTSAssembly::terminate();

  std::cerr << "Finished" << std::endl;
  exit(0);
}
