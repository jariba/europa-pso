#include "TestSupport.hh"

ConstraintEngineId DefaultEngineAccessor::s_instance;

// Wouldn't this be useful for planners, e.g., as well?
// --wedgingt 2004 Mar 10
void initConstraintLibrary() {
  static bool s_runAlready(false);

  if (!s_runAlready) {
    // Register constraint Factories
    REGISTER_UNARY(SubsetOfConstraint, "SubsetOf", "Default");
    REGISTER_NARY(EqualConstraint, "Equal", "Default");
    REGISTER_NARY(AddEqualConstraint, "AddEqual", "Default");
    REGISTER_NARY(LessThanEqualConstraint, "LessThanEqual", "Default");
    REGISTER_NARY(NotEqualConstraint, "NotEqual", "Default");
    REGISTER_NARY(MultEqualConstraint, "MultEqual", "Default");
    REGISTER_NARY(AddMultEqualConstraint, "AddMultEqual", "Default");
    REGISTER_NARY(EqualSumConstraint, "EqualSum", "Default");
    REGISTER_NARY(LessOrEqThanSumConstraint, "LessOrEqThanSum", "Default");
    REGISTER_NARY(CondAllSameConstraint, "CondAllSame", "Default");
    REGISTER_NARY(CondAllDiffConstraint, "CondAllDiff", "Default");

    // Europa (NewPlan/ConstraintNetwork) names for the same constraints:
    REGISTER_NARY(AddMultEqualConstraint, "addmuleq", "Default");
    REGISTER_NARY(LessThanEqualConstraint, "leq", "Default");
    REGISTER_NARY(EqualSumConstraint, "sum", "Default");
    REGISTER_NARY(EqualConstraint, "eq", "Default");
    REGISTER_NARY(NotEqualConstraint, "neq", "Default");
    REGISTER_NARY(AddEqualConstraint, "addeq", "Default");

    s_runAlready = true;
  }
}

bool loggingEnabled() {
  static const char *envStr = getenv("PROTOTYPE_ENABLE_LOGGING");
  static const bool enabled = (envStr != NULL && atoi(envStr) != 0);
  return(enabled);
}
