# EUROPA Examples #

A simple EUROPA example is described on the [Quick Start](QuickStart.md) page.  Here are some more advanced examples that can serve as starting points for your own model, or just to learn more about what EUROPA can do in different domains.  Some of these examples are described in depth; the code for all of these and other examples can be found under the $EUROPA\_HOME/examples directory. You can also browse them online at the EUROPA repository, [here](http://code.google.com/p/europa-pso/source/browse/PLASMA/trunk/examples). You can build them and run them yourself just by running 'ant' (to run the Java version) or 'make' (to run the C++ version) in the corresponding directory.

### Constraint Programming ###
  * **NQueens** : One of the [workhorses](http://en.wikipedia.org/wiki/Eight_queens_puzzle) of constraint programming. This problem consists of placing N queens on a chessboard in such a way that they don't attack each other. [More...](ExampleNQueens.md)

### Scheduling ###
  * **RCPSP** ( Resource Constrained Project Scheduling Problem) : A [well known problem](http://www.sciencedirect.com/science/article/pii/S0377221798002045) in the OR community that consists of scheduling a set of activities with temporal and resource constraints.  [More...](ExampleRCPSP.md)

### Planning ###
  * **Shopping**:  One way to implement the shopping example discussed in Russel and Norvig's AI textbook ([Russel/Norvig 1995](DocReferences.md)).  They describe the problem as "Get a quart of milk and a bunch of bananas and a variable-speed cordless drill" and refer to it throughout chapter 11 (Planning). [More...](ExampleShopping.md)
  * **Blocks World** : One of the [workhorses](http://en.wikipedia.org/wiki/Blocks_world) of early AI planners. This problem consists of building one or more vertical stacks of cubes (blocks) which sit on a table, using a robotic arm. [More...](ExampleBlocksWorld.md)
  * **Rover**: An in-depth tutorial describes how to design and implement a robotic (Mars Rover) planning problem.  Many of the example snippets on the [NDDL Reference](NDDLReference.md) page originate in this example. [More...](ExampleRover.md)
