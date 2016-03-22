**NOTE** : work in progress, still migrating from 2.5 statement

### Modeling ###
  * PDDL
    * New PDDL-to-NDDL translator
    * Benchmark problems
  * ANML
    * New ANML-to-NDDL translator
    * Benchmark problems
  * NDDL
    * Add method invocation for predicate definition
    * add nicer error reporting for parser, verify line numbers are not lost.
    * Add [Valgrind](http://valgrind.org) output and checks to regression tests

### Search Support ###
  * Add visualization tool for search tree (see decisions made in compressed format, zoom, etc).

### Architecture enhancements/re-factoring/cleanup ###
  * Drop architectural assumption that Constraint Engine stops computation at first constraint violation
    * Remove this assumption from built-in solver, propagators, etc.
    * Modify Constraint Engine API?
  * Make constraint violation reporting robust : [issue 216](https://code.google.com/p/europa-pso/issues/detail?id=216), [issue 173](https://code.google.com/p/europa-pso/issues/detail?id=173),[issue 204](https://code.google.com/p/europa-pso/issues/detail?id=204),[issue 205](https://code.google.com/p/europa-pso/issues/detail?id=205), and a bunch of A4O tickets.
    * Make sure Timelines can be substituted with Unary resources under the covers so that timeline overload is reported as a violation, not as a flaw
  * Systematically expose listeners through PSEngine
  * Add State resource class

### Visualization/Debugging tools ###
  * Provide logging api for standard (log4j) logging behavior. Support with our home-grown logging class first, also possibly with log4cxx : [issue 142](https://code.google.com/p/europa-pso/issues/detail?id=142)
  * Come up with a plan to integrate Tatiana's UI work into distribution
  * Provide schema browser : [issue 38](https://code.google.com/p/europa-pso/issues/detail?id=38)
  * Improve gantt chart : [issue 39](https://code.google.com/p/europa-pso/issues/detail?id=39),[issue 40](https://code.google.com/p/europa-pso/issues/detail?id=40)

### Automated Build ###
  * Report test failures correctly on windows : [issue 252](https://code.google.com/p/europa-pso/issues/detail?id=252)
  * Add missing regression tests
    * PSEngine : [issue 219](https://code.google.com/p/europa-pso/issues/detail?id=219)
    * constraint violation reporting : [issue 220](https://code.google.com/p/europa-pso/issues/detail?id=220)
  * Add plan comparison to regression tests : [issue 50](https://code.google.com/p/europa-pso/issues/detail?id=50), [issue 104](https://code.google.com/p/europa-pso/issues/detail?id=104)
  * Add performance measurements to regression tests and autobuild : [issue 64](https://code.google.com/p/europa-pso/issues/detail?id=64)
  * Use [Valgrind](http://valgrind.org) to verify sound memory management
  * Set up bitten build for Solaris

### Documentation ###
  * Improve PSEngine docs :  [issue 221](https://code.google.com/p/europa-pso/issues/detail?id=221)
  * Architecture
  * Add mandatory tips & tricks for beginners
  * Add specific section on debugging
  * Add docs for power user

### Bug Fixes ###