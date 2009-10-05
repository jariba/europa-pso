%module ExampleCustomConstraint
%include "std_string.i"

%{
  #include "ExampleCustomConstraintCustomCode.hh"
  #include "ModuleExampleCustomConstraint.hh"
%}
