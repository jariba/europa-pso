//#include "../../Solvers/test/test-module.cc"
//#include "../../NDDL/test/nddl-test-module.cc"
//#include "../HSTS/test/hsts-test-module.hh"
//#include "../../Utils/test/util-test-module.cc"
#include "../Aver/test/aver-test-module.hh"
#include "../CBPlanner/test/cbp-test-module.hh"
#include "../ConstraintEngine/test/ce-test-module.hh"
#include "../../HeuristicsEngine/test/he-test-module.cc"
#include "../PlanDatabase/test/db-test-module.hh"
#include "../Resource/test/rs-test-module.hh"
#include "../RulesEngine/test/re-test-module.hh"
#include "../TemporalNetwork/test/tn-test-module.hh"

/**
 * @brief Test harness for running all module tests from a single binary to enable code coverage
 *        analysis tools to examine the codebase.
 */

int main(int argc, const char** argv) {
 
  //SolverModuleTests::runTests("../../Solvers/test");
  //NDDLModuleTets::runTests("../../NDDL/test");
  //UtilModuleTests::runTests("../../Utils/test");
  //HSTSModuleTests::runTests("../../hsts/test");
  AverModuleTests::runTests("../../Aver/test");
  CBPlannerModuleTests::runTests("../../CBPlanner/test");
  //ConstraintEngineModuleTests::runTests("../../ConstraintEngine/test"); // assertion failure in domian-tests.cc
  PlanDatabaseModuleTests::runTests("../../PlanDatabase/test");
  ResourceModuleTests::runTests("../../Resource/test");
  RulesEngineModuleTests::runTests("../../RulesEngine/test");
  HeuristicsEngineModuleTests::runTests("../../HeuristicsEngine/test");
  TemporalNetworkModuleTests::runTests("../../TemporalNetwork/test"); 

  return 0;
}
