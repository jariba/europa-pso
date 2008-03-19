#ifndef _H_ExampleCustomCode
#define _H_ExampleCustomCode

#include "ConstraintEngineDefs.hh"
#include "Constraint.hh"
#include "Variable.hh"
#include "IntervalDomain.hh"
#include "BoolDomain.hh"
#include "Constraints.hh"
#include "Token.hh"

using namespace EUROPA;




/**
 * @class ExampleConstraint
 * @brief Given an interval variable x, restrict the domain to be bounded by integers
 * 
 * @note See Constraints.cc for plenty of more involved examples (all built-in constraints)
 * */
class ExampleConstraint : public Constraint {
public:
  ExampleConstraint(const LabelStr& name,
                    const LabelStr& propagatorName,
                    const ConstraintEngineId& constraintEngine,
                    const std::vector<ConstrainedVariableId>& variables);

  static void registerSelf(std::string name, std::string propagator);
  void handleExecute();

private:
  static const int X = 0;  
  static const int ARG_COUNT = 1;
};

#endif
