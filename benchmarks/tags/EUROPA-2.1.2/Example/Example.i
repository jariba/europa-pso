%module Example
%include "std_string.i"

%{
  #include "ExampleCustomCode.hh"
  #include "ModuleExample.hh"
%}

class Foo 
{
public:
void bar();
};