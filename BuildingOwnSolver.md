While EUROPA contains a built-in chronological backtracking algorithm that may suffice for many applications, a benefit of EUROPA's modularity is the ability to replace that solver with another approach.  This section describes how to use the client-side API to build a solver.  For many users, this will be sufficient.  However, some other users will want to access the Europa internals - for this approach, see the (TODO) documentation.

## A Glimpse at the Current Solver ##

TODO: Is it helpful to look at how the current solver is built/invoked to get a sense of how the user might do their own

## Building a Solver ##

TODO: Likely steps and why they may be necessary

## Example ##

A Tabu Search solver was written [here](http://code.google.com/p/europa-pso/source/browse/PLASMA/trunk/examples/NQueens/java/NQueens/TSNQueensSolver.java) to solve the [NQueens](ExampleNQueens.md) problem.