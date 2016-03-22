I have divided this approach into two pieces:
  1. Calculating profiles.  They're not really profiles anymore, but it's similar, at least philosophically, to resource profile calculations.
  1. Using profiles to calculate flaws and violations.

At Jeremy's suggestion, I have tried to get everything working without too much concern for efficiency.  For example, the above separation may make it harder to do things efficiently, but the two pieces can be mixed without messing up the approach, I believe.

Here are the transactions involved:
  * **SET:**  an instantaneous action that changes the state to some value.
  * **REQ, -REQ:**  the start and end points of a durative requirement (ie token) that the state be some value for the time between the REQ and -REQ transactions.
  * **PRE:**  a 'deleted precondition', or requirement that the state be some value at the instant before this transaction occurs.

For each 'es' and 'ls' represent earliest and latest starts.  So, for example, -REQ(ls) is the latest time the -REQ (ie the end of the requirement) could occur.

NOTE:  In addition to a requirement on the previous state, a deleted precondition usually refers to change of state as well.  However, we ignore that here, since it is equivalent to a PRE and a SET together.

# Calculating profiles #

As we pass through time, we update 4 sets whenever we hit one of the above six classes of time points:
  * **A:**  The set of possible SET instances that could be valid at this time.
  * **B:**  The set of REQ tokens that _might_ be underway at this time.
  * **C:**  The set of REQ tokens that _must_ be underway at this time.
  * **D:**  The set of PRE instances that could be valid at this time.


### Building A ###

To build A, we start with the empty set for simplicity (having an initial state, or set of states is no different).  Then:
  * If we reach a SET(es), we add SET to A
  * If we reach a SET(ls), we can remove any SET' for which SET'(ls) < SET(es)  (Mike, this is equivalent to the bar we talked about)

If there is an initial state, or set of states, that can be represented with a SET where SET(es) = SET(ls) = 0 (or negative infinity?).

### Building B ###

To build B, we start with the empty set.  Then:
  * If we reach REQ(es), we add REQ
  * If we reach -REQ(ls), we remove REQ

### Building C ###

To build C, we start with the empty set.  Then:
  * If we reach REQ(ls) and REQ(ls) < -REQ(es), we add REQ
  * If we reach -REQ(es) and REQ(ls) < -REQ(es), we remove REQ

Note that C is a subset of B.

### Building D ###

To build D, we start with the empty set.  Then:
  * If we reach PRE(es), we add PRE
  * If we reach PRE(ls), we remove PRE


# Detection flaws and violations #

This can be broken into a number of cases:

  1. **FV detection just within A:**  For any pair SET and SET' in A:
    1. It is a violation if they both _must_ start at the same time and _must_ set the state to different values (assuming for now that the state can be set to the same thing  by multiple SETs at the same time).
    1. The same thing with _might_ and _must_ is a flaw:  It is a flaw if they both _might_ start at the same time and _must_ set the state to different values (assuming for now that the state can be set to the same thing  by multiple SETs at the same time).
    1. The same thing with _must_ and _might_ is a flaw.
    1. The same things with _might_ and _might_ is a flaw.
  1. **FV detection within B and C:**
    1. If C contains REQ1 and REQ2, and their values _must_ not intersect, it is a violation.
    1. If B contains REQ1 and REQ2, and their values _might_ not intersect, it is a flaw
  1. **FV detection between A and B,C:**
    1. If C contains REQ, and there is no SET in A whose value might match that of REQ, it is a violation.
    1. If B contains REQ, and there is no SET in A whose value might match that of REQ, it is a flaw.
    1. If B contains REQ, and there is any SET in A whose value might not match that of REQ, it is a flaw.
  1. **FV detection just within D:**  For any pair PRE and PRE' in D:
    1. It is a violation if they both _must_ start at the same time and their precondition states _must_ be different values.
    1. The same thing with _might_ and _must_ is a flaw:  It is a flaw if they both _might_ start at the same time and their precondition states _must_ be different values.
    1. The same thing with _must_ and _might_ is a flaw.
    1. The same thing with _might_ and _might_ is a flaw.
  1. **FV detection between D and A', where A' is the set A for the _preceding instant_:**
    1. If D contains PRE, and PRE _must_ occur here and there is no SET in A' whose value might match that of PRE, it is a violation.
    1. If D contains PRE and there is no SET in A' whose value might match that of PRE, it is a flaw.
    1. If D contains PRE and there is any SET in A' whose value might not match that of REQ, it is a flaw.
  1. **FV detection between D and B',C', where B'/C' is the set B/C for the _preceding instant_ :**
    1. If D contains PRE, and PRE _must_ occur here, and C' contains REQ and there is no overlap between the state values of PRE and REQ, it is a violation.
    1. If D contains PRE, and B' contains REQ, unless the state values for PRE and REQ are both singleton and equal, it is a flaw.

NOTE:  I think that comparing D with B',C' is not strictly necessary.  For example, if there is a flaw between D and B', there should also be a flaw between D and A' and/or a flaw between B and A', and resolving that flaw will either remove the flaw between D and B' or add a flaw between either D and A' or B' and A'.  Ok, that's confusing, but I think the idea is intuitive.

The obvious example where this extra detection is warranted is the case where there is a violation between D and B' (or C') but only flaws between D and A' and between B' (or C') and A.  It would be nice to notice the violation right away instead of needing some number of flaw resolutions before the violation is noticed!


# Adding temporal constraints #

Most of the above ingores the fact that fewer flaws (and possibly more violations?) could be detecting by being aware of necessary (implicit or explicit) temporal constraints between SET, REQ, and -REQ.

Some of this could be handled when A,B,C are calculated.  In fact, notice that we've used temporal information to decide when a SET is removed from A.  However, it is not possible to handle all constraints during the construction of A,B,C, I think.  For example, even if there is an SET->SET' temporal constraint, the domains for SET and SET' can overlap).  Therefore, I propose:

  * Timetable-style FV detection does the above.
  * Flow-profile-style FV detection that augments the above with
    * A precomputed all-pairs temporal constraint network (ie be able to quickly look up whether X is temporally constrained to be before Y)
    * Update the above flaw/violation rules slightly.

Here is an updated of the above flaw/violation detection cases for using whatever temporal constraint information is pre-computed (new stuff in bold).  Note that nothing changes within set A because we assume we use the extra information when building A.

  1. **FV detection just within A:**  For any pair SET and SET' in A:
    1. It is a violation if they both _must_ start at the same time and _must_ set the state to different values (assuming for now that the state can be set to the same thing  by multiple SETs at the same time) **(no change)**.
    1. The same thing with _might_ and _must_ is a flaw:  It is a flaw if they both _might_ start at the same time and _must_ set the state to different values (assuming for now that the state can be set to the same thing  by multiple SETs at the same time). **(no change)**
    1. The same thing with _must_ and _might_ is a flaw. **(no change)**
    1. The same things with _might_ and _might_ is a flaw. **(no change)**
  1. **FV detection within B and C:**
    1. If C contains REQ1 and REQ2, and their values _must_ not intersect, it is a violation **(no change here because if we're in C, there must not be a relevant temporal constraint between REQ1 and REQ2)**
    1. If B contains REQ1 and REQ2, and their values _might_ not intersect **and there is no precedence constraint between them**, it is a flaw
  1. **FV detection between A and B,C:**
    1. If C contains REQ, and there is no SET in A **which might occur before REQ ends and** whose value might match that of REQ, it is a violation.
    1. If B contains REQ, and there is no SET in A **which might occur before REQ ends and** whose value might match that of REQ, it is a flaw.
    1. If B contains REQ, and there is any SET in A **which might occur before REQ ends and**whose value might not match that of REQ, it is a flaw.

This may not be quite right, but the basic idea is to avoid the cases that we know, based on extra temporal information, cannot occur.

# Resolving flaws and violations #

Haven't thought about this! :)  However, it feels like there will be some pretty good heuristics that will tend to guide search in the right direction.