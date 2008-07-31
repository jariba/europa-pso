#ifndef _H_ExampleCustomCode
#define _H_ExampleCustomCode

#include "ConstraintEngineDefs.hh"
#include "Constraint.hh"
#include "Variable.hh"
#include "IntervalDomain.hh"
#include "BoolDomain.hh"
#include "Constraints.hh"
#include "Token.hh"
#include <iostream>

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
 void handleExecute();

private:
  static const int X = 0;  
  static const int ARG_COUNT = 1;
};



// Example code used to show how to access custom C++ code from Java:
class Foo
{
 public:
  void bar()
  { 
    std::cout << "Foo.bar called." << std::endl;
  }
};

#endif
