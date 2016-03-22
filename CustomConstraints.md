# Adding a Constraint to EUROPA #

A new project automatically includes its own module that is loaded into the EUROPA engine and stub C++ files to hold custom code (see [makeproject](MakeprojectPage.md)).  Therefore, extending EUROPA components simply requires C++ code be added to those existing files. Consider the Example project [here](http://code.google.com/p/europa-pso/source/browse/benchmarks/trunk/ExampleCustomConstraint), which can be downloaded with:
```
svn co http://europa-pso.googlecode.com/svn/benchmarks/tags/EUROPA-2.2/ExampleCustomConstraint ExampleCustomConstraint
```

We have added an 'ExampleConstraint' that restricts a variable to have integer bounds.  To create and use the constraint involved these steps:

  1. Declare and define the constraint in [ExampleCustomConstraintCustomCode.hh](http://code.google.com/p/europa-pso/source/browse/benchmarks/trunk/ExampleCustomConstraint/ExampleCustomConstraintCustomCode.hh) and [ExampleCustomConstraintCustomCode.cc](http://code.google.com/p/europa-pso/source/browse/benchmarks/trunk/ExampleCustomConstraint/ExampleCustomConstraintCustomCode.cc).
  1. Register the constraint. In [ModuleExampleCustomConstraint.cc](http://code.google.com/p/europa-pso/source/browse/benchmarks/trunk/ExampleCustomConstraint/ModuleExampleCustomConstraint.cc):
```
	  CESchema* schema = (CESchema*)engine->getComponent("CESchema");

	  REGISTER_CONSTRAINT(schema, ExampleConstraint, "example", "Default");
```
  1. Use the constraint.  In [ExampleCustomConstraint-initial-state.nddl](http://code.google.com/p/europa-pso/source/browse/benchmarks/trunk/ExampleCustomConstraint/ExampleCustomConstraint-initial-state.nddl):
```
    float x = [0.5, 3.5];
    example(x);  // will be constrained to [1, 3];
```

The result is that the global variable `x` will have range [1, 3] once EUROPA has been run.  This range is output in the 'RUN\_ExampleCustomConstraint-planner\_g\_rt.ExampleCustomConstraint-initial-state.xml.PlannerConfig.xml.output' log file; notice these lines in [source:/benchmarks/trunk/ExampleCustomConstraint/ExampleCustomConstraint-Main.cc ExampleCustomConstraint-Main.cc]:
```
        PSVariable* v = engine->getVariableByName("x");
        std::cout << "x lower bound: " << v->getLowerBound() << std::endl;
        std::cout << "x upper bound: " << v->getUpperBound() << std::endl;
```


For many other examples of how to write EUROPA constraints, see [Constraints.cc](http://code.google.com/p/europa-pso/source/browse/PLASMA/trunk/src/PLASMA/ConstraintEngine/base/Constraint.cc).