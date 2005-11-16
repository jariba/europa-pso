#include "../../NDDL/test/nddl-test-module.hh"
#include "../../HSTS/test/hsts-test-module.hh"
#include "../../Utils/test/util-test-module.hh"
#include "../../Solvers/test/solvers-test-module.hh"
#include "../../Aver/test/aver-test-module.hh"
#include "../../CBPlanner/test/cbp-test-module.hh"
#include "../../ConstraintEngine/test/ce-test-module.hh"
#include "../../HeuristicsEngine/test/he-test-module.hh"
#include "../../PlanDatabase/test/db-test-module.hh"
#include "../../Resource/test/rs-test-module.hh"
#include "../../RulesEngine/test/re-test-module.hh"
#include "../../TemporalNetwork/test/tn-test-module.hh"

/**
 * @brief Test harness for running all module tests from a single binary to enable code coverage
 *        analysis tools to examine the codebase.
 */

// stubbed out - just provide a defintion of this symbol.
extern "C" void loadSchema() {}


int main(int argc, const char** argv) {

  // PLASMA problems

  //SolversModuleTests::runTests("../../Solvers/test");  // Factory "A" is not registered.
  //HSTSModuleTests::runTests("../../hsts/test"); // factory for constraint TEstOnly is not regestered.
 
  ConstraintEngineModuleTests::runTests("../../ConstraintEngine/test");
  AverModuleTests::runTests("../../Aver/test");
  CBPlannerModuleTests::runTests("../../CBPlanner/test");
  //PlanDatabaseModuleTests::runTests("../../PlanDatabase/test");
  NDDLModuleTests::runTests("../../NDDL/test"); 
  ResourceModuleTests::runTests("../../Resource/test");
  RulesEngineModuleTests::runTests("../../RulesEngine/test");
  HeuristicsEngineModuleTests::runTests("../../HeuristicsEngine/test");
  TemporalNetworkModuleTests::runTests("../../TemporalNetwork/test"); 
  // Keep this test last. Leaves some debug messages set as a side effect. 
  UtilModuleTests::runTests("../../Utils/test");
   
  return 0;
}
