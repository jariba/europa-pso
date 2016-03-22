# Quick Start #

_Option 1_:  Add plugin to existing Eclipse IDE.  Recommended.

  1. Make sure you have EUROPA installed and running.
  1. [Make sure](http://code.google.com/p/europa-pso/wiki/EuropaInstallation) EUROPA dynamic libraries are in LD\_LIBRARY\_PATH (PATH for Windows  and DYLD\_LIBRARY\_PATH for Mac) for Eclipse.
    * NOTE: for Mac to recognize the library path update, Eclipse needs to be started from the the command-line terminal windows (not by double-clicking on the Eclipse icon from Mac's Finder windows)
  1. [Download](http://code.google.com/p/europa-pso/downloads/list) the version of the Eclipse plugin that is compatible with your version of EUROPA.
  1. Put the downloaded **.jar** file into your Eclipse dropins directory.
  1. Restart Eclipse.

_Option 2_:  Build the plugin site yourself, and add plugin to existing Eclipse IDE.

  1. Make sure you have EUROPA installed, and running.
  1. [Make sure](http://code.google.com/p/europa-pso/wiki/EuropaInstallation) EUROPA dynamic libraries are in LD\_LIBRARY\_PATH (PATH for Windows and DYLD\_LIBRARY\_PATH for Mac) for Eclipse.
    * NOTE: for Mac to recognize the library path update, Eclipse needs to be started from the the command-line terminal windows (not by double-clicking on the Eclipse icon from Mac's Finder windows)
  1. Check out EUROPA plugin code
  1. Build the Eclipse plugin update site (there is an ant target for that in build.xml)
  1. There are two options to install:
    * Copy **.jar** from the produced update site structure into Eclipse's _dropins directory_ and restart Eclipse
    * From Eclipse, point your update manager at the directory produced by the ant script

_Option 3_:  Run as a separate Eclipse Application (instead of updating your Eclipse IDE).
  1. Make sure you have EUROPA checked out, built, and running.
  1. [Make sure](http://code.google.com/p/europa-pso/wiki/EuropaInstallation) EUROPA\_HOME is on your LD\_LIBRARY\_PATH (PATH for Windows and DYLD\_LIBRARY\_PATH for Mac).
    * NOTE: for Mac to recognize the library path update, Eclipse needs to be started from the the command-line terminal windows (not by double-clicking on the Eclipse icon from Mac's Finder windows)
  1. You need the plugin to be at the root level, in order for things to run correctly. So:
    * File->Import->Existing Projects Into Worspace
    * Choose _PLASMA/src/Java/JavaUI_ which will be the root directory (you now have two different 'views' into the same files in the workspace)
    * Use the default name for the new 'project', which is **Europa.java**
  1. Copy **PSEngine.jar** from EUROPA (_PLASMA/build/lib_) into _Europa.java/lib_
    * NOTE 1: Whenever EUROPA gets updated, you'll need to redo this copy
    * NOTE 2: this 'copy' operation is done within the Eclipse navigation pane, not in the file system.
  1. _Activate_: Right click the newly added _Europa.java_ project from Eclipse's _Project Explorer_ pane, choose _Run As -> Eclipse Application_ where you can utilize the _Editor_ and _Execution Perspective_ components as described below.
    * NOTE: After the EclipseIDE is brought up, user may need to create an empty project in the _Navigator_ pane (by right click on the pane, then choose _New -> Project -> General -> Project_). After a dummy blank project is created, user can copy in the NDDL files (good starting point is the examples) and run the NDDL file from there.

# Overview #

The Eclipse plugin has two major components: an **Editor** and an **Execution Perspective**.

### Components ###

  * _NDDL Editor_:  Syntax highlighting, syntax errors reported, structure linked to standard Outline View.
  * _Solver View_:  Start/stop the EUROPA engine, and configure and run a solver.
  * _Statistics View_:  Graphs of solver stats.
  * _Open Decision View_:  View of open decisions at each step of solving.
  * _Schema Browser View_:  View the schema for the active NDDL model.
  * _Gantt View_:  Once a solution is found, view the plan.
  * _Details View_:  Click on a token in the Gantt View to see it's details in this view.
  * _Run NDDL model_ perspective:  Includes all of the above components.

## Editor ##

Eclipse plugin registers a file type for ".nddl" and a default editor for it. The editor has syntax highlighting and an outline. The outline is updated every time an editor is saved. I am creating a brand new instance of the engine and deleting it after getting an AST tree. If the parser detects any errors, they are displayed as error markers in the editor.

Clicking in the outline navigates in the editor. Folding, control-clicking, and code assist are in future plans.

Note: I am not an artist, so complains about ugly icons will not be accepted. Better icons will be very much appreciated. (TODO Update images with new icons)

![http://europa-pso.googlecode.com/svn/wiki/images/EclipseNddlEditor.png](http://europa-pso.googlecode.com/svn/wiki/images/EclipseNddlEditor.png)

There is a new "Run as" action that shows up for NDDL files, both in the editor and in the Package Explorer. It creates a launch configuration and switches the perspective to NDDL model execution.

![http://europa-pso.googlecode.com/svn/wiki/images/EclipseRunAs.png](http://europa-pso.googlecode.com/svn/wiki/images/EclipseRunAs.png)

Like the Swing version, it takes a NDDL file and a planner configuration file to run. In Swing these were passed from the command line. In Eclipse plugin they are part of a launch configuration

![http://europa-pso.googlecode.com/svn/wiki/images/LaunchConfiguration.png](http://europa-pso.googlecode.com/svn/wiki/images/LaunchConfiguration.png)

## Run NDDL model perspective ##

_How_: Right click on the **-initial-state.nddl** file and choose "Run As -> NDDL".

The Run as NDDL model perspective is the Eclipse version of the Swing PSDesktop user interface. The main difference is the new button that lets you start and stop the engine. The plugin can run multiple NDDL sessions at the same time. You can switch between them using the pulldown list. EUROPA sessions are also visible in the Debug perspective and can be killed or restarted from there.

![http://europa-pso.googlecode.com/svn/wiki/images/EclipseRunPerspective.png](http://europa-pso.googlecode.com/svn/wiki/images/EclipseRunPerspective.png)

# Details #

See also [Developer Notes](JavaUINotes.md) for the various EUROPA Java UI interfaces.