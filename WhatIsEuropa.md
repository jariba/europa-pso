# What is EUROPA #

## Overview ##
EUROPA (Extensible Universal Remote Operations Planning Architecture) is a class library and tool set for building planners (and/or schedulers) within a Constraint-based Temporal Planning paradigm. Constraint-based Temporal Planning<sup>1</sup> (and Scheduling) is a paradigm of planning based on an explicit notion of time and a deep commitment to a constraint-based formulation of planning problems.
This paradigm has been successfully applied in a wide range of practical planning problems and has a legacy of success in NASA applications including :

  * Observation scheduling for the Hubble Telescope ([Muscettola 1998](DocReferences.md))
  * Autonomous control of DS-1 ([ref?](DocReferences.md))
  * Ground-based activity planning for MER ([Ai-Chang et al.](DocReferences.md))
  * Autonomous control of EO-1 ([Tran et al. 2004](DocReferences.md))

EUROPA is now at version 2 and is the successor of the original EUROPA which in turn was based upon HSTS ([Muscettola 1998](DocReferences.md)). EUROPA offers capabilities in 3 key areas of problem solving:

  1. **Representation:** EUROPA allows a rich representation for actions, states, resources and constraints that allows concise declarative descriptions of problem domains and powerful expressions of plan structure. This representation is supported with a high-level object-oriented modeling language for describing problem domains and data structures for instantiating and manipulating problem instances.
  1. **Reasoning:** Algorithms are provided which exploit the formal structure of problem representation to enforce domain rules and propagate consequences as updates are made to the problem state. These algorithms are based on logical inference and constraint-processing. In particular, specialized techniques are included for reasoning about temporal quantities and relations.
  1. **Search:** Problem solving in EUROPA requires search. Effective problem solving typically requires heuristics to make search tractable and to find good solutions.  EUROPA provides a framework for integrating heuristics into a basic search algorithm and for developing new search algorithms.

EUROPA is not an end-user application. Rather, it is a means to integrate advanced planning, scheduling and constraint reasoning into an end-user application. EUROPA is not a specific planner or a scheduler. Rather it is a framework for developing specific planners and/or schedulers. It is designed to be open and extendable to accommodate diverse and highly specialized problem solving techniques within a common design framework and around a common technology core.

EUROPA is unconventional in providing a separate Plan Database that can integrate in a wide variety of applications. This reflects the common needs for representation and manipulation of plan data in different application contexts and different problem solving approaches. Possible approaches include:

  * A batch planning application where an initial state is input and a final plan is output without any interaction with other actors.
  * A mixed-initiative planning application where human users interact directly with a plan database but also employ an automated problem solver to work on parts of the planning problem in an interleaved fashion.
  * An autonomous execution system where the plan database stores the plan data as it evolves in time, being updated from data in the environment, commitments from the executive, and the accompanying automated solver which plans ahead and fixes plans when they break.

## EUROPA-related Projects ##
EUROPA has been used for a variety of missions, mission-oriented research, and demonstrations, including:

  * [ATHLETE](http://www-robotics.jpl.nasa.gov/systems/system.cfm?System=11) support for foot fall planning for a lunar robot
  * [STAR](http://ic.arc.nasa.gov/tech/groups/index.php?gid=56&ta=3) Advanced Spaceflight Training Systems Development
  * [SACE](http://ase.arc.nasa.gov/news/story.php?id=478) Support for operation of the International Space Station's solar arrays
  * [Bedrest](http://www.bedreststudy.com) study at Johnson Space Center
  * [Mars ‘09? - MSL](http://mars.jpl.nasa.gov/msl/) : Support for planning and scheduling  for Mars Science Laboratory Science Operations
  * [Crew Planning](http://www.nasa.gov/missions/shuttle/f_schedule.html) Research project on Planning and Scheduling for space missions
  * [T-REX](http://code.google.com/p/trex-autonomy) A model based executive that integrates planning and execution for autonomous mobile robots and things that seem just like them. TREX has been deployed at sea on AUVs and on land for mobile manipulation.
  * MER Tactical Activity Planning. EUROPA is the core planning technology behind MAPGEN, a decision support tool for generating detailed activity plans on a daily basis for the MER robotic mission to Mars.
  * Mars ‘03: MER –Mars Exploration Rover Science Operations
  * On-board Planning and Plan Execution. EUROPA was the core planning technolgoy for deliberative and reactive planning on-board a variety of mobile robots. It has been fielded in the Atacama Desert and was the cornerstone of a 2005 milestone of human-robotic collaboration for the Collaborative Decision Systems program.
  * Mission Simulation. EUROPA was used to simulate a prospective robotic mission (LORAX) to the Antarctic for the purposes of system design evaluation.
  * Intelligent Distributed Execution Architecture (IDEA –EUROPA, EUROPA 2)
  * Earth-observing satellite scheduling project (EOS –EUROPA, EUROPA 2)
  * [SOFIA](http://www.nasa.gov/centers/ames/research/exploringtheuniverse/exploringtheuniverse-sofia.html) flight scheduling project (SOFIA –EUROPA)
  * Contingent Planning for ROVER operations (PiCO–EUROPA 2)
  * Personal Satellite Assistant (PSA –EUROPA)
  * Spoken Interface Prototype for PSA (RIALIST –EUROPA)
  * IS Milestone (EUROPA 2, support ended in 2004)
  * [CDS](http://ti.arc.nasa.gov/projects/cds) Milestone (EUROPA 2, currently supporting)
  * DS1: RAX –Remote Agent Experiment (original version of technology)

## Acknowledgements ##
EUROPA is the result of many years of research, development and deployment of constraint-based planning technology.

  * The precursor to EUROPA was HSTS, designed and developed by Nicola Muscettola. HSTS set out the initial domain description language and essentials of the planning paradigm that became the starting point for EUROPA
  * [Ari Jonsson](http://www.ru.is/ari) led the implemenation of the first version of EUROPA. Ari's team included [Jeremy Frank](http://ti.arc.nasa.gov/profile/frank/), Paul Morris and Will Edgington, who all made valuable contributions.
  * Conor McGann led the implementation of EUROPA 2, which is a further evolution of this line of work, targeted mainly at making the technology easier to use, more efficient, easier to integrate and easier to extend. EUROPA 2's main contributors were Andrew Bachmann, Tania Bedrax-Weiss,  Matthew Boyce, Patrick Daley, Will Edgington, Jeremy Frank, Michael Iatauro, Peter Jarvis, Ari Jonsson, Paul Morris, Sailesh Ramakrishnan and Will Taylor.
  * [Javier Barreiro](http://ti.arc.nasa.gov/profile/javier/) took over as the EUROPA team lead in the Fall of 2006 and has been working on it since then, improving EUROPA's technology and packaging. Javier's main in collaborators at NASA Ames are Michael Iatauro, Matthew Boyce,  [Tristan Smith](http://ti.arc.nasa.gov/profile/tsmith/) and [David Smith](http://ti.arc.nasa.gov/profile/de2smith/).
  * EUROPA was approved to be released under a NASA open source license in October of 2007.

Funding for this work has been provided by the NASA Intelligent Systems and Collaborative Decision Systems Programs.  Much of this wiki documentation is derived from ''How to Solve It: Problem Solving in EUROPA 2.0'' by Conor McGann, which in turn borrowed heavily from the EUROPA user guide written by Ari Jonsson an Jeremy Frank.


---

1. EUROPA can be used for planning and scheduling, and is typically used for both.  For convenience, we usually refer to planning exclusively, presuming it includes scheduling.