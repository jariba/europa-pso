A new project automatically includes its own module that is loaded into the EUROPA engine and stub C++ files to hold custom code (see [makeproject](MakeprojectPage.md)). How to easily extend EUROPA itself with C++ code is shown [here](CustomConstraints.md).  However, we have also made it easy to access your custom C++ code from Java.  Consider the ExampleCustomConstraint project [here](http://code.google.com/p/europa-pso/source/browse/#svn/benchmarks/tags/EUROPA-2.2/ExampleCustomConstraint), which can be downloaded with:
```
svn co http://europa-pso.googlecode.com/svn/benchmarks/tags/EUROPA-2.2/ExampleCustomConstraint ExampleCustomConstraint
```

We have added a simple 'Foo' class that is written in C++ and accessed in Java as follows:
  1. Define 'Foo' with method 'bar' in ExampleCustomConstraint.hh.
  1. Add the signature to ExampleCustomConstraint.i so that the method will be swig-wrapped.
  1. Call the method from ExampleCustomConstraint.bsh.

Note that we've also shown the same method called from the C++ side in ExampleCustomConstraint-Main.cc.