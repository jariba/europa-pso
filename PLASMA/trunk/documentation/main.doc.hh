/**
 * @mainpage EUROPA 2 
 * @section intro Introduction
 * EUROPA 2 is a specialized database for specification, storage and manipulation of plans.
 * It is used primarily for developing planning and scheduling applications and/or
 * embedding planning and scheduling capabilities within a broader system architecture.
 *
 * EUROPA 2 is built on the formulation of planning and scheduling as a process of 
 * refinement of a dynamic constraint network. To that end it provides the following key capabilities:
 * @li A high-level modelling language (@ref nddl "NDDL") for describing particular planning domains.
 * @li A database in which plans may be created and manipulated in accordance with the
 * semantics of the specified planning domain. The database employs an extensive array
 * of automated reasoning techniques to exclude avenues of refinement that lead to inconsistent
 * plans and to suggest avenues of refinement that lead to complete plans. 
 * @li A solver for automated refinement of plans such that the resulting plan is consistent
 * and complete with respect to the planning domain semantics and the requiremenets of the
 * planning problem.
 * @li A development tool (PlanWorks) to assist in development and deployment of EUROPA 2 based
 * applications.
 *
 * EUROPA 2 is provided as a C++ library designed to permit easy integration as an embedded component
 * of a larger system, and to support extensive customization and enhancement to adapt to the needs
 * of new applications and new research developments.
 *
 * EUROPA 2 is tested and supported on Red Hat Linux, Mac OSX, and Solaris.
 *
 * @section guide Reader's Guide
 * This documentation repository integrates all the assets of EUROPA, including code, examples and test cases, to provide documentation for users interested in building solvers, using solvers, or making alterations and extensions in the underlying technology. It includes:
 * @li @ref background "Background Material". Introduces the concepts underlying EUROPA 2. By example, it develops the key elements of plan representation and automated reasoning upon which EUROPA 2 is built.
 * @li @ref building "Build Instructions". In order to get started working with the software, the reader should consult the @ref readme file for general information and the @ref building "build instructions" to compile, link and test the installation. 
 * @li @ref helloWorld "Hello World Example Project". A chapter describing a simple @ref helloWorld "Hello World" domain is presented to familiarize the reader with the various tools available in @ref europa "EUROPA".
 * @li @ref nddl. Provides an in-depth tutorial on the practice of building domain models using the NDDL modeling language. @ref solvers will teach the reader to define problem instances, assemble a suitable solver for the problem at hand, and customize search control. 
 * @li @ref system. The system architecture is described in detail to convey the underlying structure of EUROPA and the roles and responsibilities of different modules. 
 * @li @ref trouble. A trouble-shooting guide presents tools and techniques for detection and diagnosis of problems using EUROPA. 
 * @li @ref relatedwork. For the interested reader a chapter on @ref relatedwork is provided with @ref references.
 * @li @ref appendix. Contains a variety of detailed reference material that may be of interest.
 *
 * @section acknowledgements Acknowledgements
 * EUROPA 2 is a culmination of many years of research, development and deployment of constraint-based planning
 * technology. The precursor to EUROPA was HSTS, designed and developed by Nicola Muscettola. HSTS set out the
 * initial domain description language and essentials of the planning paradigm that became the starting point for
 * EUROPA, under the leadership of Ari Jonsson. Ari's team included Jeremy Frank, Paul Morris and Will Edgington, who all
 * made valuable contributions to the development of EUROPA. EUROPA 2 is a further evolution of this line of work,
 * targetted mainly at making the technology easier to use, more efficient, easier to integrate and easier to extend. The
 * development of EUROPA 2 has been lead by Conor McGann in collaboration with Andrew Bachmann, Tania Bedrax-Weiss, 
 * Patrick Daley, Will Edgington, Jeremy Frank, Michael Iatauro, Peter Jarvis, Ari Jonsson, Sailesh Ramakrishnan and 
 * Will Taylor. Funding for this work has been provided by the NASA Intelligent Systems and Collaborative Decision Systems Programmes.
 */
