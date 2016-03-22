# Introduction #

This page is to serve as developer notes for the three variants of the Java UI.  For usage instructions, see instead these pages:
  * [Eclipse Plugin (SWT)](EclipseIDE.md):  Edit NDDL, view schema, configure and run solver, and view solution details, all from within your Eclipse IDE.
  * [Java UI (Swing)](SwingUI.md):  View schema, configure and run solver, interact with EUROPA, and view solution details from a plain Java UI.
  * [Java PSUI (Swing)](PSUIDocs.md): Earlier implementation of Java UI.

# Original Design (Javier) #

# New Design (Tatiana) #

## The "vision" ##

Europa comes with a Java user interface package (PSDesktop). The purpose of the work described here is to create a new generation of PSDesktop. As before,

  * The user interface will consist of multiple views into Europa (open decisions, resource time lines, etc)

  * User applications can add new views to the standard set

_New features_

  * The biggest news is that now Java UI will come in two flavors: standalone Java application (Swing-based) and an Eclipse plugin. The plugin will include all the views of the Swing version plus have an editor for NDDL files with all the bells and whistles we love and expect in Eclipse

  * To make the above easier, the original PSDesktop code is refactored to separate logic of dealing with the Europa engine from user interfaces. For now at least, the new refactored code sits in a separate project.

  * Adding custom views to either Swing or Eclipse version will no longer require recompilation of the base code. The Swing version will have the ability to hook up new views through reflection. For Eclipse, user views will be organized as extension plugins.

### Package structure ###

The root package is **org.ops.ui**. I got this name from the Java code in Europa. Let me know if I should use something else.

The next level after the root says what the package is about. The last level is one of **model** (common UI-free code), **swing**, **swt**. In most cases, all three will be present.


## Building it and installing it ##

In the root of the project there is **build.xml**. The default target in this Ant build file is **build.site**. It produces a directory called **dist** in the root of the project (added to svn:ignore). This directory contains the structure of an update site for Eclipse with an additional folder for the Swing jar. You can point your Eclipse Update Manager to this local directory to install the plug-in.

**Note**: the plug-in expects **EUROPA\_HOME** to be set and Europa shared libraries to be in **LD\_LIBRARY\_PATH** or **PATH** (for Windows).

Since the Java UI is dependent on Europa runtime, the following versioning schema is used. Release of the JavaUI will have the same version number as Europa proper. Intermediate releases include the number the most recent Europa release followed by the build number with which the Java code was tested. The Java code should work with all later versions of the Europa runtime, at least in theory. The only place where the version is mentioned is **build.properties** (bundle.version). The build script substitutes this number to all necessary places, including file names and contents of the manifest.

When we are ready to release, we can include just jars or use the directory structure for the update site. The only problem with the latter approach that I see is that Eclipse update manager will not force update of the Europa runtime.

# Notes #

  * Both the Swing version and Eclipse version require Europa libraries to be in the library path.
  * At this stage in development I highly recommend running the plugin in a nested Eclipse (as opposed to exporting and installing the plugin in your main Eclipse). Europa is called through JNI, and when Europa coredumps, it takes the JVM with it. It has not happened to me since we fixed the 64-bit Antlr3 problem, but better safe than sorry.