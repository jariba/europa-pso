# EUROPA Documentation #
If you don't know where to start, or just want a quick overview of how to use EUROPA, take a look at the [EUROPA Quick Start](QuickStart.md).  You can also find an overview of the EUROPA framework and philosophy at [Europa Background](EuropaBackground.md).

### Architecture ###
  * [Overview](ArchitectureOverview.md)
  * Propagation Services
  * Plan Database Services
  * Modeling Services
  * Problem Solving Services
  * Ancillary Modules

### Development Tools ###
  * [How to embed EUROPA in an application](EmbeddingEUROPA.md)
  * [makeproject](MakeprojectPage.md):  Automatically create all the pieces for a new project.
  * EUROPA UI Options:
    * [Eclipse Plugin (SWT)](EclipseIDE.md):  Edit NDDL, view schema, configure and run solver, and view solution details, all from within your Eclipse IDE.
    * [Java PSUI (Swing)](PSUIDocs.md): View schema, configure and run solver, interact with EUROPA, and view solution details from a plain Java UI.
    * [Java UI (Swing, deprecated)](SwingUI.md): another set of Java UI components, currently being merged with PSUI.
  * Low-level debugging:
    * [EUROPA Logging](DebugOutputManagement.md)
    * Timelines
    * The Token Network
    * The Constraint Network
    * Metric Resources
    * Common Debugging Scenarios

### EUROPA Components ###
  * API
    * [PSEngine](PSEngine.md) is the Client API and the recommended way to embed EUROPA into and application.  This interface is also available in Java (we use [SWIG](http://www.swig.org) to do the mapping automatically).
    * Listeners
      * Adding a Listener
    * [Calling your custom C++ code from Java](CwithJava.md)
    * Doxygen documentation for all the EUROPA classes can be found [here](https://babelfish.arc.nasa.gov/trac/europa/doxygen/).
  * NDDL:
    * [NDDL Language Reference](NDDLReference.md)
    * [Complete NDDL Grammar](http://code.google.com/p/europa-pso/source/browse/PLASMA/trunk/src/PLASMA/NDDL/base/antlr/NDDL3.g)
  * Constraints:
    * [Constraint Library Reference](ConstraintLibraryReference.md)
    * [Example: Adding a Constraint](CustomConstraints.md)
  * Resources:
    * [How to Use and Configure Resources](EuropaResources.md)
    * [Notes on Using Resource Search Operators](ResourceSearchNotes.md)
    * [Example: Customizing Profile to Fake a Unary State Resource](StateResourceExample.md)
  * Solver:
    * [Built-in Solver Description](SolverReference.md)
    * [Built-in Solver Configuration](PlannerCfg.md)
    * [Extending the built-in Solver](SolverExtensions.md)
    * [Building your own Solver](BuildingOwnSolver.md)


### Miscellaneous ###
  * [Glossary](DocGlossary.md)
  * [References](DocReferences.md)
  * [EUROPA Publications](EuropaPublications.md)