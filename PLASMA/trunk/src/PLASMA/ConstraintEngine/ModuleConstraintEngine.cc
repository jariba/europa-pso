#include "ModuleConstraintEngine.hh"
#include "DataTypes.hh"
#include "Constraints.hh"
#include "ConstraintType.hh"
#include "Propagators.hh"
#include "CFunctions.hh"

namespace EUROPA {

  ModuleConstraintEngine::ModuleConstraintEngine()
      : Module("ConstraintEngine")
  {

  }

  ModuleConstraintEngine::~ModuleConstraintEngine()
  {
  }

  void ModuleConstraintEngine::initialize()
  {
  }

  void ModuleConstraintEngine::uninitialize()
  {
  }

  void ModuleConstraintEngine::initialize(EngineId engine)
  {
      CESchema* ces = new CESchema();
      ces->registerDataType((new VoidDT())->getId());
      ces->registerDataType((new BoolDT())->getId());
      ces->registerDataType((new IntDT())->getId());
      ces->registerDataType((new FloatDT())->getId());
      ces->registerDataType((new StringDT())->getId());
      ces->registerDataType((new SymbolDT())->getId());
      engine->addComponent("CESchema",ces);

      ces->registerCFunction((new IsSingleton())->getId());
      ces->registerCFunction((new IsSpecified())->getId());

      ConstraintEngine* ce = new ConstraintEngine(ces->getId());
	  new DefaultPropagator(LabelStr("Default"), ce->getId());
      engine->addComponent("ConstraintEngine",ce);
  }

  void ModuleConstraintEngine::uninitialize(EngineId engine)
  {
      ConstraintEngine* ce = (ConstraintEngine*)engine->removeComponent("ConstraintEngine");
      delete ce;

      CESchema* ces = (CESchema*)engine->removeComponent("CESchema");
      delete ces;
  }

  /**************************************************************************************/

  ModuleConstraintLibrary::ModuleConstraintLibrary()
      : Module("ConstraintLibrary")
  {
  }

  ModuleConstraintLibrary::~ModuleConstraintLibrary()
  {
  }

  void ModuleConstraintLibrary::initialize()
  {
  }

  void ModuleConstraintLibrary::uninitialize()
  {
  }

  void ModuleConstraintLibrary::initialize(EngineId engine)
  {
      debugMsg("ModuleConstraintLibrary:initialize", "Initializing the constraint library");

      CESchema* ces = (CESchema*)engine->getComponent("CESchema");

      // Register constraint Factories
      REGISTER_CONSTRAINT_TYPE(ces,AbsoluteValueCT, "absVal", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,AddEqualCT, "AddEqual", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,AddEqualCT, "addeq", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,AddEqualCT, "addEq", "Default");
      REGISTER_CONSTRAINT(ces,AddMultEqualConstraint, "AddMultEqual", "Default");
      REGISTER_CONSTRAINT(ces,AddMultEqualConstraint, "addMulEq", "Default");
      REGISTER_CONSTRAINT(ces,AddMultEqualConstraint, "addmuleq", "Default");
      REGISTER_CONSTRAINT(ces,AllDiffConstraint, "adiff", "Default"); // all different
      REGISTER_CONSTRAINT(ces,AllDiffConstraint, "fadiff", "Default"); // flexible all different
      REGISTER_CONSTRAINT(ces,AllDiffConstraint, "fneq", "Default"); // flexible not equal
      REGISTER_CONSTRAINT(ces,AllDiffConstraint, "AllDiff", "Default");
      REGISTER_CONSTRAINT(ces,CalcDistanceConstraint, "calcDistance", "Default");
      REGISTER_CONSTRAINT(ces,CardinalityConstraint, "card", "Default"); // cardinality not more than
      REGISTER_CONSTRAINT(ces,CardinalityConstraint, "Cardinality", "Default");
      REGISTER_CONSTRAINT(ces,CondAllDiffConstraint, "CondAllDiff", "Default");
      REGISTER_CONSTRAINT(ces,CondAllSameConstraint, "CondAllSame", "Default");
      REGISTER_CONSTRAINT(ces,CondAllSameConstraint, "condEq", "Default");
      REGISTER_CONSTRAINT(ces,CondAllSameConstraint, "condeq", "Default");
      REGISTER_CONSTRAINT(ces,CondAllSameConstraint, "condasame", "Default");
      REGISTER_CONSTRAINT(ces,CondEqualSumConstraint, "CondEqualSum", "Default");
      REGISTER_CONSTRAINT(ces,CountNonZerosConstraint, "CountNonZeros", "Default");
      REGISTER_CONSTRAINT(ces,CountNonZerosConstraint, "cardeq", "Default"); // cardinality equals
      REGISTER_CONSTRAINT(ces,CountZerosConstraint, "CountZeros", "Default");
      REGISTER_CONSTRAINT(ces,DistanceFromSquaresConstraint, "distanceSquares", "Default");

      REGISTER_CONSTRAINT_TYPE(ces,EqualCT, "asame", "Default"); // all same
      REGISTER_CONSTRAINT_TYPE(ces,EqualCT, "eq", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,EqualCT, "fasame", "Default"); // flexible all same
      REGISTER_CONSTRAINT_TYPE(ces,EqualCT, "Equal", "Default");

      REGISTER_CONSTRAINT(ces,EqualMaximumConstraint, "EqualMaximum", "Default");
      REGISTER_CONSTRAINT(ces,EqualMaximumConstraint, "fallmax", "Default"); // flexible all max
      REGISTER_CONSTRAINT(ces,EqualMinimumConstraint, "fallmin", "Default"); // flexible all min
      REGISTER_CONSTRAINT(ces,EqualMinimumConstraint, "EqualMinimum", "Default");
      REGISTER_CONSTRAINT(ces,EqualProductConstraint, "product", "Default");
      REGISTER_CONSTRAINT(ces,EqualProductConstraint, "EqualProduct", "Default");

      REGISTER_CONSTRAINT(ces,EqualSumConstraint, "EqualSum", "Default");
      REGISTER_CONSTRAINT(ces,EqualSumConstraint, "sum", "Default");

      REGISTER_CONSTRAINT(ces,GreaterThanSumConstraint, "GreaterThanSum", "Default");
      REGISTER_CONSTRAINT(ces,GreaterOrEqThanSumConstraint, "GreaterOrEqThanSum", "Default");
      REGISTER_CONSTRAINT(ces,LessOrEqThanSumConstraint, "leqsum", "Default");
      REGISTER_CONSTRAINT(ces,LessOrEqThanSumConstraint, "LessOrEqThanSum", "Default");

      REGISTER_CONSTRAINT_TYPE(ces,LessThanCT, "lt", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,LessThanCT, "lessThan", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,LessThanCT, "LessThan", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,LessThanEqualCT, "leq", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,LessThanEqualCT, "LessThanEqual", "Default");

      REGISTER_CONSTRAINT(ces,LessThanSumConstraint, "LessThanSum", "Default");
      REGISTER_CONSTRAINT(ces,LockConstraint, "Lock", "Default");
      REGISTER_CONSTRAINT(ces,MemberImplyConstraint, "MemberImply", "Default");
      REGISTER_CONSTRAINT(ces,MemberImplyConstraint, "memberImply", "Default");
      REGISTER_CONSTRAINT(ces,MultEqualConstraint, "mulEq", "Default");
      REGISTER_CONSTRAINT(ces,MultEqualConstraint, "multEq", "Default");
      REGISTER_CONSTRAINT(ces,MultEqualConstraint, "MultEqual", "Default");
      REGISTER_CONSTRAINT(ces,NegateConstraint, "neg", "Default");
      REGISTER_CONSTRAINT(ces,NotEqualConstraint, "neq", "Default");
      REGISTER_CONSTRAINT(ces,NotEqualConstraint, "NotEqual", "Default");
      REGISTER_CONSTRAINT(ces,OrConstraint, "Or", "Default");
      REGISTER_CONSTRAINT(ces,OrConstraint, "for", "Default"); // flexible or
      REGISTER_CONSTRAINT(ces,OrConstraint, "or", "Default");
      REGISTER_CONSTRAINT(ces,SineFunction, "sin", "Default");
      REGISTER_CONSTRAINT(ces,SquareOfDifferenceConstraint, "diffSquare", "Default");
      REGISTER_CONSTRAINT(ces,SubsetOfConstraint, "SubsetOf", "Default");
      REGISTER_CONSTRAINT(ces,SubsetOfConstraint, "subsetOf", "Default");
      REGISTER_CONSTRAINT(ces,SubsetOfConstraint, "Singleton", "Default");

      REGISTER_CONSTRAINT_TYPE(ces,TestAndCT, "testAnd", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestEQCT, "TestEqual", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestEQCT, "testEQ", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestLessThanCT, "TestLessThan", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestLessThanCT, "condlt", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestLEQCT, "condleq", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestLEQCT, "testLEQ", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestNEQCT, "testNEQ", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestOrCT, "testOr", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestSingletonCT, "testSingleton", "Default");
      REGISTER_CONSTRAINT_TYPE(ces,TestSpecifiedCT, "testSpecified", "Default");

      REGISTER_CONSTRAINT(ces,UnaryConstraint, "UNARY", "Default");

      REGISTER_CONSTRAINT(ces,WithinBounds, "WithinBounds", "Default");
      REGISTER_CONSTRAINT(ces,WithinBounds, "withinBounds", "Default");

      // Rotate scope right one (last var moves to front) to ...
      // ... change addleq constraint to GreaterOrEqThan constraint:
      REGISTER_ROTATED_CONSTRAINT(ces,"addleq", "Default", "GreaterOrEqThanSum", 1);
      // ... change addlt constraint to GreaterThanSum constraint:
      REGISTER_ROTATED_CONSTRAINT(ces,"addlt", "Default", "GreaterThanSum", 1);
      // ... change allmax and max constraint to EqualMaximum constraint:
      REGISTER_ROTATED_CONSTRAINT(ces,"allmax", "Default", "EqualMaximum", 1);
      REGISTER_ROTATED_CONSTRAINT(ces,"max", "Default", "EqualMaximum", 1);
      // ... change allmin and min constraint to EqualMinimum constraint:
      REGISTER_ROTATED_CONSTRAINT(ces,"allmin", "Default", "EqualMinimum", 1);
      REGISTER_ROTATED_CONSTRAINT(ces,"min", "Default", "EqualMinimum", 1);

      // But addeqcond is harder, requiring two "steps":
      REGISTER_SWAP_TWO_VARS_CONSTRAINT(ces,"eqcondsum", "Default", "CondEqualSum", 0, 1);
      REGISTER_ROTATED_CONSTRAINT(ces,"addeqcond", "Default", "eqcondsum", 2);
  }

  void ModuleConstraintLibrary::uninitialize(EngineId engine)
  {
      CESchema* ces = (CESchema*)engine->getComponent("CESchema");
      // TODO: should be more selective and only remove the constraints we added above
      ces->purgeConstraintTypes();
  }
}
