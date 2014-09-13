#include "rs-flow-test-module.hh"

#include "ResourceDefs.hh"
#include "Profile.hh"
#include "FVDetector.hh"
#include "Instant.hh"
#include "Transaction.hh"
#include "TimetableProfile.hh"
#include "FlowProfile.hh"
#include "IncrementalFlowProfile.hh"
#include "ProfilePropagator.hh"
#include "ClosedWorldFVDetector.hh"
#include "BoostFlowProfile.hh"
#include "BoostFlowProfileGraph.hh"

#include "Debug.hh"
#include "Engine.hh"
#include "Utils.hh"
#include "Constraints.hh"
#include "Domains.hh"
#include "Propagators.hh"
#include "PlanDatabaseDefs.hh"
#include "PlanDatabase.hh"
#include "Schema.hh"
#include "Object.hh"
#include "EventToken.hh"
#include "TokenVariable.hh"
#include "STNTemporalAdvisor.hh"
#include "Reusable.hh"
#include "DurativeTokens.hh"

#include <iostream>
#include <string>
#include <list>

using namespace EUROPA;

class ResourceTestEngine  : public EngineBase
{
  public:
    ResourceTestEngine();
    virtual ~ResourceTestEngine();

  protected:
    void createModules();
};

#define RESOURCE_DEFAULT_SETUP(ce, db, autoClose) \
    ResourceTestEngine rte; \
    ConstraintEngine& ce = *((ConstraintEngine*)rte.getComponent("ConstraintEngine")); \
    PlanDatabase& db = *((PlanDatabase*)rte.getComponent("PlanDatabase")); \
    if (autoClose) \
      db.close();

#define RESOURCE_DEFAULT_TEARDOWN()

class DefaultSetupTest {
public:
  static bool test() {
    EUROPA_runTest(testDefaultSetup);
    return true;
  }
private:
  static bool testDefaultSetup() {
    RESOURCE_DEFAULT_SETUP(ce,db,false);

    CPPUNIT_ASSERT(db.isClosed() == false);
    db.close();
    CPPUNIT_ASSERT(db.isClosed() == true);
    CPPUNIT_ASSERT(ce.constraintConsistent() == true);

    RESOURCE_DEFAULT_TEARDOWN();
    return true;
  }
};

class DummyDetector : public FVDetector {
public:
  DummyDetector(const ResourceId res) : FVDetector(res) {};
  bool detect(const InstantId inst) {return false;}
  void initialize(const InstantId inst) {}
  void initialize() {}

  virtual PSResourceProfile* getFDLevelProfile() { return NULL; }
  virtual PSResourceProfile* getVDLevelProfile() { return NULL; }
};

// class BoostFlowProfile : public FlowProfile {
//  public:
//   BoostFlowProfile(const PlanDatabaseId db, const FVDetectorId flawDetector)
//       : FlowProfile(db, flawDetector) {
//     initializeGraphs<EUROPA::BoostFlowProfileGraph>();
//   }
// };

class FlowProfileTest
{
public:

  static bool flowProfileTest() {
    debugMsg("ResourceTest"," FlowProfile ");

    testAddAndRemove< EUROPA::FlowProfile> ();
    testScenario0< EUROPA::FlowProfile>();
    testScenario1< EUROPA::FlowProfile>();
    testScenario2< EUROPA::FlowProfile>();
    testScenario3< EUROPA::FlowProfile>();
    testScenario4< EUROPA::FlowProfile>();
    testScenario5< EUROPA::FlowProfile>();
    testScenario6< EUROPA::FlowProfile>();
    testScenario7< EUROPA::FlowProfile>();
    // testScenario8< EUROPA::FlowProfile>();
    testScenario9< EUROPA::FlowProfile>();
    // testScenario10< EUROPA::FlowProfile>();
    // testScenario11< EUROPA::FlowProfile>();
    // testScenario12< EUROPA::FlowProfile>();
    // testScenario13< EUROPA::FlowProfile>();
    // testScenario14< EUROPA::FlowProfile>();
    testPaulBug<EUROPA::FlowProfile>();

    return true;
  }
  
  static bool boostFlowProfileTest() {
    debugMsg("ResourceTest"," BoostFlowProfile ");

    testAddAndRemove<BoostFlowProfile> ();
    testScenario0< BoostFlowProfile>();
    testScenario1< BoostFlowProfile>();
    testScenario2< BoostFlowProfile>();
    testScenario3< BoostFlowProfile>();
    testScenario4< BoostFlowProfile>();
    testScenario5< BoostFlowProfile>();
    testScenario6< BoostFlowProfile>();
    testScenario7< BoostFlowProfile>();
    testScenario8< BoostFlowProfile>();
    testScenario9< BoostFlowProfile>();
    testScenario10<BoostFlowProfile>();
    testScenario11<BoostFlowProfile>();
    testScenario12<BoostFlowProfile>();
    testScenario13<BoostFlowProfile>();
    testScenario14<BoostFlowProfile>();
    testPaulBug<BoostFlowProfile>();
    return true;

  }

  static bool incrementalFlowProfileTest() {
     debugMsg("ResourceTest"," IncrementalFlowProfile ");

     testAddAndRemove< EUROPA::IncrementalFlowProfile> ();
     testScenario0< EUROPA::IncrementalFlowProfile>();
     testScenario1< EUROPA::IncrementalFlowProfile>();
     testScenario2< EUROPA::IncrementalFlowProfile>();
     testScenario3< EUROPA::IncrementalFlowProfile>();
     testScenario4< EUROPA::IncrementalFlowProfile>();
     testScenario5< EUROPA::IncrementalFlowProfile>();
     testScenario6< EUROPA::IncrementalFlowProfile>();
     testScenario7< EUROPA::IncrementalFlowProfile>();
     testScenario8< EUROPA::IncrementalFlowProfile>();
     testScenario9< EUROPA::IncrementalFlowProfile>();
     testScenario10< EUROPA::IncrementalFlowProfile>();
     testScenario11< EUROPA::IncrementalFlowProfile>();
     testScenario12< EUROPA::IncrementalFlowProfile>();
     testScenario13< EUROPA::IncrementalFlowProfile>();
     testScenario14< EUROPA::IncrementalFlowProfile>();
     //testPaulBug<EUROPA::IncrementalFlowProfile>();
     return true;
  }

  static bool test(){
    return 
        // flowProfileTest() && 
        boostFlowProfileTest() //&& 
        //incrementalFlowProfileTest()
        ;
  }
private:
  static bool verifyProfile( Profile& profile, int instances, eint times[], edouble lowerLevel[], edouble upperLevel[] ) {
    int counter = 0;

    ProfileIterator ite( profile.getId() );

    while( !ite.done() ) {
      if( counter >= instances ) {
	debugMsg("ResourceTest:verifyProfile","Profile has more instances than expected, now at instance "
		 << counter << " and expected only "
		 << instances );

	return false;
      }

      if( times[counter] != ite.getTime() ) {
	debugMsg("ResourceTest:verifyProfile","Profile has no instant at time "
		 << times[counter] << " the nearest instant is at " << ite.getTime() );

	return false;
      }

      if( lowerLevel[counter] != ite.getLowerBound() ) {
	debugMsg("ResourceTest:verifyProfile","Profile has incorrect lower level at instant at time "
		 << times[counter] << " the level is " << ite.getLowerBound() << " and is supposed to be "
		 << lowerLevel[counter] );
	return false;

      }

      if( upperLevel[counter] != ite.getUpperBound() ) {
	debugMsg("ResourceTest:verifyProfile","Profile has incorrect upper level at instant at time "
		 << times[counter] << " the level is " << ite.getUpperBound() << " and is supposed to be "
		 << upperLevel[counter] );

	++counter;

	return false;
      }

      ++counter;

      ite.next();
    }

    return true;
  }

  static void executeScenario0( Profile& profile, ConstraintEngine& ce ) {
    // no transactions
    profile.recompute();
  }

  static void executePaulBug(Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[] ) {
    /*!
     * T+ precedes T-
     * t- precedes t+
     *
     * T+   <10-------(+1000)---------------------------------inf>
     * T-    |     <120010--(-1000)---------------------------inf>
     * t-    |      |       <960000--------(-1)---------------inf>
     * t+    |      |        |      <961200--------(+1)-------inf>
     *       |      |        |       |                        |
     *       |      |        |       |                        |
     *       1000   1000     1000    1000                     0
     *       0      0       -1      -1                        0
     *
     */

    Variable<IntervalIntDomain> t1(ce.getId(), IntervalIntDomain(10, PLUS_INFINITY),
                                   true, "T+");
    Variable<IntervalIntDomain> t2(ce.getId(), IntervalIntDomain(120010, PLUS_INFINITY), 
                                   true, "T-");
    Variable<IntervalIntDomain> t3(ce.getId(), IntervalIntDomain(960000, PLUS_INFINITY),
                                   true, "t-");
    Variable<IntervalIntDomain> t4(ce.getId(), IntervalIntDomain(961200, PLUS_INFINITY), 
                                   true, "t+");
    
    Variable<IntervalDomain> q1(ce.getId(), IntervalDomain(1000), true, "qT+");
    Variable<IntervalDomain> q2(ce.getId(), IntervalDomain(1000), true, "qT-");
    Variable<IntervalDomain> q3(ce.getId(), IntervalDomain(1), true, "qt-");    
    Variable<IntervalDomain> q4(ce.getId(), IntervalDomain(1), true, "qt+");

    LessThanEqualConstraint c0(LabelStr("precedes"), LabelStr("Temporal"), ce.getId(),
                               makeScope(t1.getId(), t2.getId()));
    LessThanEqualConstraint c1(LabelStr("precedes"), LabelStr("Temporal"), ce.getId(),
                               makeScope(t3.getId(), t4.getId()));

    ce.propagate();

    Transaction trans1(t1.getId(), q1.getId(), false);
    Transaction trans2(t2.getId(), q2.getId(), true);
    Transaction trans3(t3.getId(), q3.getId(), true);
    Transaction trans4(t4.getId(), q4.getId(), false);

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );
    profile.addTransaction( trans4.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );

  }

  static void executeScenario1( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[] ) {

    /*!
     * No explicit ordering between transactions
     *
     * Transaction1   <0-------(+1)-------10>
     * Transaction2    |                 <10--(-1)--15>
     * Transaction3    |       <5--------(-1)-------15>
     * Transaction4    |       <5--------(+1)-------15>
     *                 |        |         |          |
     *                 |        |         |          |
     * Max level       1        2         2          0
     * Min level       0       -1        -1          0
     *
     */

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain(10, 15), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 5, 15), true, "t3" );
    Variable<IntervalIntDomain> t4( ce.getId(), IntervalIntDomain( 5, 15), true, "t4" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 1), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 1), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(1, 1), true, "q3" );
    Variable<IntervalDomain> q4( ce.getId(), IntervalDomain(1, 1), true, "q4" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );
    Transaction trans3( t3.getId(), q3.getId(), true);
    Transaction trans4( t4.getId(), q4.getId(), false);

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );
    profile.addTransaction( trans4.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario2( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 0, 10), true, "t2" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 1), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 1), true, "q2" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario3( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {
    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain(  0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 10, 10), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 10, 20), true, "t3" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 2), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 2), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(1, 2), true, "q3" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );
    Transaction trans3( t3.getId(), q3.getId(), false );

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario4( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {
    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain(  0, 5), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 10, 15), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 20, 25), true, "t3" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 2), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 2), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(1, 2), true, "q3" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );
    Transaction trans3( t3.getId(), q3.getId(), false );

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario5( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 0, 10), true, "t2" );


    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 1), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 1), true, "q2" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );

    EqualConstraint c0(LabelStr("concurrent"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), t2.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario6( Profile& profile, ConstraintEngine& ce ) {
    /*!
     * No explicit ordering between transactions
     *
     * Transaction1   <0------[1,2]-------100>
     * Transaction2   <0-----[-1,-2]------100>
     * Transaction3   <0------[1,2]-------100>
     * Transaction4   <0-----[-1,-2]------100>
     *                 |                   |
     *                 |                   |
     * Max level       4                   2
     * Min level      -4                  -2
     *
     */
    debugMsg("ResourceTest","    Case 1");

    const int nrInstances = 2;

    eint itimes[nrInstances] = {0,100};
    edouble lowerLevels[nrInstances] = {-4,-2};
    edouble upperLevels[nrInstances] = {4,2};

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 100), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 0, 100), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 0, 100), true, "t3" );
    Variable<IntervalIntDomain> t4( ce.getId(), IntervalIntDomain( 0, 100), true, "t4" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 2), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 2), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(1, 2), true, "q3" );
    Variable<IntervalDomain> q4( ce.getId(), IntervalDomain(1, 2), true, "q4" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );
    Transaction trans3( t3.getId(), q3.getId(), false);
    Transaction trans4( t4.getId(), q4.getId(), true );

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );
    profile.addTransaction( trans4.getId() );

    profile.recompute();

    {
      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    /*!
     * Change the quantity of transaction 2 to a singleton
     * (only lower levels needs to be recalculated, optimization possible)
     *
     * No explicit ordering between transactions
     *
     * Transaction1   <0------[1,2]-------100>
     * Transaction2   <0-------[-1]-------100>
     * Transaction3   <0------[1,2]-------100>
     * Transaction4   <0-----[-1,-2]------100>
     *                 |                   |
     *                 |                   |
     * Max level       4                   2
     * Min level      -3                  -1
     *
     */
    debugMsg("ResourceTest","    Case 2");

    edouble postq2eq1LowerLevels[nrInstances] = {-3,-1};

    q2.restrictBaseDomain( IntervalDomain(1,1) );

    {
      bool profileMatches = verifyProfile( profile, nrInstances, itimes, postq2eq1LowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    /*!
     * Change the time of transaction 2
     *
     * No explicit ordering between transactions
     *
     * Transaction1   <0------[1,2]-------100>
     * Transaction2       <10--[-1]-------100>
     * Transaction3   <0------[1,2]-------100>
     * Transaction4   <0-----[-1,-2]------100>
     *                 |   |               |
     *                 |   |               |
     * Max level       4   4               2
     * Min level      -2  -3              -1
     *
     */
    debugMsg("ResourceTest","    Case 3");

    t2.restrictBaseDomain( IntervalIntDomain( 10, 100) );

    const int postt2to10NrInstances = 3;

    eint postt2to10Itimes[postt2to10NrInstances] = {0,10,100};
    edouble postt2to10LowerLevels[postt2to10NrInstances] = {-2,-3,-1};
    edouble postt2to10UpperLevels[postt2to10NrInstances] = {4,4,2};

    {
      bool profileMatches = verifyProfile( profile, postt2to10NrInstances, postt2to10Itimes, postt2to10LowerLevels, postt2to10UpperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    /*!
     * Change the time of transaction 2 a little more
     * (this should skip the recalculation of the first Instant)
     *
     * No explicit ordering between transactions
     *
     * Transaction1   <0------[1,2]-------100>
     * Transaction2       <11--[-1]-------100>
     * Transaction3   <0------[1,2]-------100>
     * Transaction4   <0-----[-1,-2]------100>
     *                 |   |               |
     *                 |   |               |
     * Max level       4   4               2
     * Min level      -2  -3              -1
     *
     */
    debugMsg("ResourceTest","    Case 4");

    t2.restrictBaseDomain( IntervalIntDomain( 11, 100) );

    const int postt2to11NrInstances = 3;

    eint postt2to11Itimes[postt2to11NrInstances] = {0,11,100};
    edouble postt2to11LowerLevels[postt2to11NrInstances] = {-2,-3,-1};
    edouble postt2to11UpperLevels[postt2to11NrInstances] = {4,4,2};

    {
      bool profileMatches = verifyProfile( profile, postt2to11NrInstances, postt2to11Itimes, postt2to11LowerLevels, postt2to11UpperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    /*!
     * Constrain Transaction3 and Transaction4 to be concurrent
     *
     * No explicit ordering between transactions
     *
     * Transaction1   <0------[1,2]-------100>
     * Transaction2       <11--[-1]-------100>
     * Transaction3   <0------[1,2]-------100>
     * Transaction4   <0-----[-1,-2]------100>
     *                 |   |               |
     *                 |   |               |
     * Max level       3   3               2
     * Min level      -1  -2              -1
     *
     */
    debugMsg("ResourceTest","    Case 5");

    EqualConstraint c0(LabelStr("concurrent"), LabelStr("Temporal"), ce.getId() ,
                       makeScope( t3.getId(), t4.getId()));

    ce.propagate();

    profile.recompute();

    const int postt3eqt4NrInstances = 3;

    eint postt3eqt4Itimes[postt3eqt4NrInstances] = {0,11,100};
    edouble postt3eqt4LowerLevels[postt3eqt4NrInstances] = {-1,-2,-1};
    edouble postt3eqt4UpperLevels[postt3eqt4NrInstances] = {3,3,2};

    {
      bool profileMatches = verifyProfile( profile, postt3eqt4NrInstances, postt3eqt4Itimes, postt3eqt4LowerLevels, postt3eqt4UpperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }
  }

  static void executeScenario7( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {
    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 0, 10), true, "t2" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(2, 2), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 2), true, "q2" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );

    EqualConstraint c0(LabelStr("concurrent"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), t2.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario8( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {
    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 0, 10), true, "t2" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 2), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(2, 2), true, "q2" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );

    EqualConstraint c0(LabelStr("concurrent"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), t2.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario9( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 1, 3), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain(10, 12), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 9, 9), true, "t3" );
    Variable<IntervalIntDomain> t4( ce.getId(), IntervalIntDomain( 11, 11), true, "t4" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 1), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 1), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(1, 1), true, "q3" );
    Variable<IntervalDomain> q4( ce.getId(), IntervalDomain(1, 1), true, "q4" );

    Transaction trans1( t1.getId(), q1.getId(), true);
    Transaction trans2( t2.getId(), q2.getId(), false );
    Transaction trans3( t3.getId(), q3.getId(), true);
    Transaction trans4( t4.getId(), q4.getId(), false );

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );
    profile.addTransaction( trans4.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario10( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {

	  /*!
	   * Transaction1 constrained to be at Transaction2
	   *
	   * Transaction1   [0]-3
	   * Transaction2         [10]+3
	   * Transaction3         [10]-2
	   * Transaction4                               [100]+2
	   * Transaction5             <11------[-3]------100>
	   * Transaction6                  <12----(+3)---100>
	   *                 |     |    |   |             |
	   *                 |     |    |   |             |
	   * Max level(0)   -3    -2   -2  -2             0
	   * Min level(0)   -3    -2   -5  -5             0
	   *
	   */

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain(0,0), true, "t1" );
    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(3, 3), true, "q1" );
    Transaction trans1( t1.getId(), q1.getId(), true);

    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain(10, 10), true, "t2" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(3, 3), true, "q2" );
    Transaction trans2( t2.getId(), q2.getId(), false );

    LessThanEqualConstraint c0(LabelStr("precedes"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), t2.getId()));

    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain(10, 10), true, "t3" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(2, 2), true, "q3" );
    Transaction trans3( t3.getId(), q3.getId(), true);

    Variable<IntervalIntDomain> t4( ce.getId(), IntervalIntDomain(100, 100), true, "t4" );
    Variable<IntervalDomain> q4( ce.getId(), IntervalDomain(2, 2), true, "q4" );
    Transaction trans4( t4.getId(), q4.getId(), false );

    LessThanEqualConstraint c1(LabelStr("precedes"), LabelStr("Temporal"), ce.getId() , makeScope(t3.getId(), t4.getId()));

    Variable<IntervalIntDomain> t5( ce.getId(), IntervalIntDomain(11, 100), true, "t5" );
    Variable<IntervalDomain> q5( ce.getId(), IntervalDomain(3, 3), true, "q5" );
    Transaction trans5( t5.getId(), q5.getId(), true);

    Variable<IntervalIntDomain> t6( ce.getId(), IntervalIntDomain(12, 100), true, "t6" );
    Variable<IntervalDomain> q6( ce.getId(), IntervalDomain(3, 3), true, "q6" );
    Transaction trans6( t6.getId(), q6.getId(), false );

    LessThanEqualConstraint c2(LabelStr("precedes"), LabelStr("Temporal"), ce.getId() , makeScope(t5.getId(), t6.getId()));

    LessThanEqualConstraint c3(LabelStr("precedes"), LabelStr("Temporal"), ce.getId() , makeScope(t2.getId(), t3.getId()));
    LessThanEqualConstraint c4(LabelStr("precedes"), LabelStr("Temporal"), ce.getId() , makeScope(t2.getId(), t5.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );
    profile.addTransaction( trans4.getId() );
    profile.addTransaction( trans5.getId() );
    profile.addTransaction( trans6.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario11( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {
    /*!
     * Transaction1 constrained to be before or at Transaction2
     *
     * Transaction1   <-inf---------[-inf,0]---------inf>
     * Transaction2   <-inf---------[0, inf]---------inf>
     *                 |                              |
     *                 |                              |
     * Max level      inf                            inf
     * Min level     -inf                           -inf
     *
     */

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( MINUS_INFINITY, PLUS_INFINITY ), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( MINUS_INFINITY, PLUS_INFINITY ), true, "t2" );


    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(0, PLUS_INFINITY), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(0, PLUS_INFINITY), true, "q2" );

    Transaction trans1( t1.getId(), q1.getId(), true);
    Transaction trans2( t2.getId(), q2.getId(), false );

    LessThanEqualConstraint c1(LabelStr("precedes"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), t2.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario12( Profile& profile, ConstraintEngine& ce ) {
    /*!
     * Transaction1 constrained to be before or at Transaction2
     *
     * Transaction1   <-inf---------[-inf,0]---------inf>
     * Transaction2   <-inf---------[0, inf]---------inf>
     *                 |                              |
     *                 |                              |
     * Max level      inf                            inf
     * Min level     -inf                           -inf
     *
     */

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( MINUS_INFINITY, PLUS_INFINITY ), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( MINUS_INFINITY, PLUS_INFINITY ), true, "t2" );


    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(0, PLUS_INFINITY), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(0, PLUS_INFINITY), true, "q2" );

    Transaction trans1( t1.getId(), q1.getId(), true);
    Transaction trans2( t2.getId(), q2.getId(), false );


    Variable<IntervalIntDomain> distance( ce.getId(), IntervalIntDomain( 1, PLUS_INFINITY ), true, "distance" );
    AddEqualConstraint c1(LabelStr("temporalDistance"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), distance.getId(), t2.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );

    debugMsg("ResourceTest","    Case 1");

    profile.recompute();

    {
      const int nrInstances = 2;

      eint itimes[nrInstances] = {MINUS_INFINITY,PLUS_INFINITY};
      edouble lowerLevels[nrInstances] = {MINUS_INFINITY,MINUS_INFINITY};
      edouble upperLevels[nrInstances] = {PLUS_INFINITY,PLUS_INFINITY};

      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    debugMsg("ResourceTest","    Case 2");

    t1.restrictBaseDomain( IntervalIntDomain( 0, PLUS_INFINITY ) );

    ce.propagate();

    profile.recompute();

    {
      const int nrInstances = 3;

      eint itimes[nrInstances] = {0,1,PLUS_INFINITY};
      edouble lowerLevels[nrInstances] = {MINUS_INFINITY,MINUS_INFINITY,MINUS_INFINITY};
      edouble upperLevels[nrInstances] = {0,PLUS_INFINITY,PLUS_INFINITY};

      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

  }

  static void executeScenario13( Profile& profile, ConstraintEngine& ce, int nrInstances, eint itimes[], edouble lowerLevels[], edouble upperLevels[]  ) {
    /*!
     * Transaction1 constrained to be [1,inf) before Transaction2
     *
     * Transaction1   <0---------(-3)---------9>
     * Transaction2         <1---------(+3)---------10>
     * Transaction2   [0](-3)
     *                 |     |                |      |
     *                 |     |                |      |
     * Max level      -3    -3               -3      0
     * Min level      -6    -6               -6     -3
     *
     */

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 9 ), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 1,10 ), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 0, 0 ), true, "t3" );


    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(3, 3), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(3, 3), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(3, 3), true, "q3" );

    Transaction trans1( t1.getId(), q1.getId(), true);
    Transaction trans2( t2.getId(), q2.getId(), false );
    Transaction trans3( t3.getId(), q3.getId(), true );

    Variable<IntervalIntDomain> distance( ce.getId(), IntervalIntDomain( 1, PLUS_INFINITY ), true, "distance" );
    AddEqualConstraint c1(LabelStr("temporalDistance"), LabelStr("Temporal"), ce.getId() , makeScope(t1.getId(), distance.getId(), t2.getId()));

    ce.propagate();

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );

    profile.recompute();

    bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

    CPPUNIT_ASSERT( profileMatches );
  }

  static void executeScenario14( Profile& profile, ConstraintEngine& ce ) {
    /*!
    *
     * Transaction1   <0---------(-3)---------10>
     *                 |                       |
     * Max level       0                      -3
     * Min level      -3                      -3
     *
     */

    debugMsg("ResourceTest","    Case 1");

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10 ), true, "t1" );
    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(3, 3), true, "q1" );
    Transaction trans1( t1.getId(), q1.getId(), true);

    profile.addTransaction( trans1.getId() );

    ce.propagate();

    profile.recompute();

    {
      const int nrInstances = 2;
      eint itimes[nrInstances] = {0,10};
      edouble lowerLevels[nrInstances] = { -3, -3};
      edouble upperLevels[nrInstances] = {  0, -3};

      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    /*!
    *
     * Transaction1   <0---------(-3)---------10>
     * Transaction2               <5---------(-3)---------15>
     *                 |           |           |           |
     * Max level       0           0          -3          -6
     * Min level      -3          -6          -6          -6
     *
     */
    debugMsg("ResourceTest","    Case 2");

    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain( 5, 15 ), true, "t2" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(3, 3), true, "q2" );
    Transaction trans2( t2.getId(), q2.getId(), true );

    profile.addTransaction( trans2.getId() );

    ce.propagate();

    profile.recompute();

    {
      const int nrInstances = 4;
      eint itimes[nrInstances] = { 0,5,10,15 };
      edouble lowerLevels[nrInstances] = { -3, -6,-6,-6 };
      edouble upperLevels[nrInstances] = {  0,  0,-3,-6 };

      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }


    /*!
    *
     * Transaction1   <0---------(-3)---------10>
     * Transaction2               <5---------(-3)---------15>
     * Transaction3                                            <20---------(-3)---------25>
     *                 |           |           |           |    |                        |
     * Max level       0           0          -3          -6   -6                       -9
     * Min level      -3          -6          -6          -6   -9                       -9
     *
     */

    debugMsg("ResourceTest","    Case 3");

    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 20, 25  ), true, "t3" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(3, 3), true, "q3" );
    Transaction trans3( t3.getId(), q3.getId(), true );

    profile.addTransaction( trans3.getId() );

    ce.propagate();

    profile.recompute();

    {
      const int nrInstances = 6;
      eint itimes[nrInstances] = { 0,5,10,15,20,25 };
      edouble lowerLevels[nrInstances] = { -3,-6,-6,-6,-9,-9 };
      edouble upperLevels[nrInstances] = {  0, 0,-3,-6,-6,-9 };

      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }

    /*!
    *
     * Transaction1                 <0---------(-3)---------10>
     * Transaction2                             <5---------(-3)---------15>
     * Transaction3                                                          <20---------(-3)---------25>
     * Transaction4 <-10--(-3)------<0>
     *               |               |           |           |           |    |                        |
     * Max level     0              -3          -3          -6          -9   -9                       -12
     * Min level    -3              -6          -9          -9          -9   -12                      -12
     *
     */

    debugMsg("ResourceTest","    Case 4");

    Variable<IntervalIntDomain> t4( ce.getId(), IntervalIntDomain(-10, 0 ), true, "t4" );
    Variable<IntervalDomain> q4( ce.getId(), IntervalDomain(3, 3), true, "q4" );
    Transaction trans4( t4.getId(), q4.getId(), true );

    profile.addTransaction( trans4.getId() );

    ce.propagate();

    profile.recompute();

    {
      const int nrInstances = 7;
      eint itimes[nrInstances] = { -10,0,5,10,15,20,25 };
      edouble lowerLevels[nrInstances] = { -3, -6,-9,-9,-9,-12,-12 };
      edouble upperLevels[nrInstances] = {  0, -3,-3,-6,-9,-9,-12  };

      bool profileMatches = verifyProfile( profile, nrInstances, itimes, lowerLevels, upperLevels );

      CPPUNIT_ASSERT( profileMatches );
    }
  }


  static bool testNoTransactions() {
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    IncrementalFlowProfile profile(ce.getId(), detector.getId());

    profile.recompute();
    return true;
  }
  static bool testOnePositiveTransaction() {
    return true;
  }
  static bool testOneNegativeTransaction() {
    return true;
  }

  template< class Profile >
  static bool testAddAndRemove(){
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    Profile profile(db.getId(), detector.getId());

    Variable<IntervalIntDomain> t1( ce.getId(), IntervalIntDomain( 0, 10), true, "t1" );
    Variable<IntervalIntDomain> t2( ce.getId(), IntervalIntDomain(10, 15), true, "t2" );
    Variable<IntervalIntDomain> t3( ce.getId(), IntervalIntDomain( 5, 15), true, "t3" );
    Variable<IntervalIntDomain> t4( ce.getId(), IntervalIntDomain( 5, 15), true, "t4" );

    Variable<IntervalDomain> q1( ce.getId(), IntervalDomain(1, 2), true, "q1" );
    Variable<IntervalDomain> q2( ce.getId(), IntervalDomain(1, 1), true, "q2" );
    Variable<IntervalDomain> q3( ce.getId(), IntervalDomain(1, 1), true, "q3" );
    Variable<IntervalDomain> q4( ce.getId(), IntervalDomain(1, 1), true, "q4" );

    Transaction trans1( t1.getId(), q1.getId(), false);
    Transaction trans2( t2.getId(), q2.getId(), true );
    Transaction trans3( t3.getId(), q3.getId(), true);
    Transaction trans4( t4.getId(), q4.getId(), false);

    profile.addTransaction( trans1.getId() );
    profile.addTransaction( trans2.getId() );
    profile.addTransaction( trans3.getId() );
    profile.addTransaction( trans4.getId() );

    trans1.quantity()->restrictBaseDomain( IntervalIntDomain( cast_int(trans1.quantity()->lastDomain().getLowerBound()), 1 ) );

    profile.removeTransaction( trans1.getId() );
    profile.removeTransaction( trans2.getId() );
    profile.removeTransaction( trans3.getId() );
    profile.removeTransaction( trans4.getId() );

    //MJI- commented out because this can't happen.
//     profile.addTransaction( trans1.getId() );
//     profile.addTransaction( trans2.getId() );
//     profile.addTransaction( trans3.getId() );
//     profile.addTransaction( trans4.getId() );

    return true;
  }

  template< class Profile >
  static bool testScenario0(){
    debugMsg("ResourceTest","  Scenario 0");

    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    Profile profile( db.getId(), detector.getId());

    executeScenario0( profile, ce );
    return true;
  }

  template<typename Profile>
  static bool testPaulBug() {
    debugMsg("ResourceTest", "  Paul bug");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    
    Profile profile(db.getId(), detector.getId());

    const int nrInstances = 5;
    eint itimes[nrInstances] = {10, 120010, 960000, 961200, PLUS_INFINITY};
    edouble lowerLevels[nrInstances] = {0, 0, -1, -1, 0};
    edouble upperLevels[nrInstances] = {1000, 1000, 1000, 1000, 0};
    executePaulBug(profile, ce, nrInstances, itimes, lowerLevels, upperLevels);
    return true;
  }

  template< class Profile >
  static bool testScenario1(){
    debugMsg("ResourceTest","  Scenario 1");

    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());

    /*!
     * No explicit ordering between transactions
     *
     * Transaction1   <0-------(+1)-------10>
     * Transaction2    |                 <10--(-1)--15>
     * Transaction3    |       <5--------(-1)-------15>
     * Transaction4    |       <5--------(+1)-------15>
     *                 |        |         |          |
     *                 |        |         |          |
     * Max level       1        2         2          0
     * Min level       0       -1        -1          0
     *
     */

    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 4;

    eint itimes[nrInstances] = {0,5,10,15};
    edouble lowerLevels[nrInstances] = {0,-1,-1,0};
    edouble upperLevels[nrInstances] = {1,2,2,0};

    executeScenario1( profile, ce, nrInstances, itimes, lowerLevels, upperLevels );

    return true;
  }

  template< class Profile >
  static bool testScenario2(){
    debugMsg("ResourceTest","  Scenario 2");

    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    /*!
     * No explicit ordering between transactions
     *
     * Transaction1   <0-------(+1)-------10>
     * Transaction2   <0-------(-1)-------10>
     *                 |                   |
     *                 |                   |
     * Max level       1                   0
     * Min level      -1                   0
     *
     */

    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 2;

    eint itimes[nrInstances] = {0,10};
    edouble lowerLevels[nrInstances] = {-1,0};
    edouble upperLevels[nrInstances] = {1,0};

    executeScenario2( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );
    return true;
  }

  template< class Profile >
  static bool testScenario3(){
    debugMsg("ResourceTest","  Scenario 3");

    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    /*!
     * No explicit ordering between transactions
     *
     * Transaction1   <0-------[1,2]------10>
     * Transaction2                      <10>[-2,-1]
     * Transaction3                      <10------[1,2]-------20>
     *                 |                   |                   |
     *                 |                   |                   |
     * Max level       2                   3                   3
     * Min level       0                  -1                   0
     *
     */

    Profile profile( db.getId(), detector.getId() );

    const int nrInstances = 3;

    eint itimes[nrInstances] = {0,10,20};
    edouble lowerLevels[nrInstances] = {0,-1,0};
    edouble upperLevels[nrInstances] = {2,3,3};


    executeScenario3( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );
    return true;
  }

  template< class Profile >
  static bool testScenario4(){
    debugMsg("ResourceTest","  Scenario 4");

    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());

    /*!
     *
     * Transaction1   <0---[1,2]---5>
     * Transaction2                    <10---[-2,-1]---15>
     * Transaction3                                        <20---[1,2]---25>
     *                 |           |    |               |   |             |
     *                 |           |    |               |   |             |
     * Max level       2           2    2               1   3             3
     * Min level       0           1   -1              -1  -1             0
     *
     */
    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 6;

    eint itimes[nrInstances] = {0,5,10,15,20,25};
    edouble lowerLevels[nrInstances] = {0,1,-1,-1,-1,0};
    edouble upperLevels[nrInstances] = {2,2,2,1,3,3};

    executeScenario4( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );
    return true;
  }

  template< class Profile >
  static bool testScenario5(){
    debugMsg("ResourceTest","  Scenario 5");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());

    /*!
     * Transaction1 constrained to be at Transaction2
     *
     * Transaction1   <0-------(+1)-------10>
     * Transaction2   <0-------(-1)-------10>
     *                 |                   |
     *                 |                   |
     * Max level       0                   0
     * Min level       0                   0
     *
     */
    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 2;

    eint itimes[nrInstances] = {0,10};
    edouble lowerLevels[nrInstances] = {0, 0};
    edouble upperLevels[nrInstances] = {0, 0};

    executeScenario5( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );
    return true;
  }

  template< class Profile >
  static bool testScenario6(){
    debugMsg("ResourceTest","  Scenario 6");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    Profile profile( db.getId(), detector.getId());

    executeScenario6( profile, ce );
    return true;
  }

  template< class Profile >
  static bool testScenario7(){
    debugMsg("ResourceTest","  Scenario 7");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    /*!
     * Transaction1 constrained to be at Transaction2
     *
     * Transaction1   <0-------(+2)-------10>
     * Transaction2   <0-----[-1,-2]------10>
     *                 |                   |
     *                 |                   |
     * Max level       1                   1
     * Min level       0                   0
     *
     */
    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 2;

    eint itimes[nrInstances] = {0,10};
    edouble lowerLevels[nrInstances] = {0, 0};
    edouble upperLevels[nrInstances] = {1, 1};


    executeScenario7( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );
    return true;
  }

  template< class Profile >
  static bool testScenario8(){
    debugMsg("ResourceTest","  Scenario 8");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    /*!
     * Transaction1 constrained to be at Transaction2
     *
     * Transaction1   <0------[+1,+2]----10>
     * Transaction2   <0-------(-2)------10>
     *                 |                   |
     *                 |                   |
     * Max level       0                   0
     * Min level      -1                  -1
     *
     */
    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 2;

    eint itimes[nrInstances] = {0,10};
    edouble lowerLevels[nrInstances] = {-1, -1};
    edouble upperLevels[nrInstances] = {0, 0};

    executeScenario8( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );
    return true;
  }

  template< class Profile >
  static bool testScenario9(){
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    /*!
     * No explicit ordering between transactions
     *
     * Transaction1   <1----(-1)----3>
     * Transaction2    |            |            <10--(+1)--12>
     * Transaction3    |            |  <9(-1)>    |          |
     * Transaction4    |            |    |        | <11(+1)> |
     *                 |            |    |        |    |     |
     *                 |            |    |        |    |     |
     * Min level(1)   -1           -1   -2       -2   -1     0
     * Max level(1)    0           -1   -2       -1    0     0
     *
     */
    Profile profile( db.getId(), detector.getId());

    const int nrInstances = 6;

    eint itimes[nrInstances] =         {  1,  3,  9, 10, 11, 12};
    edouble lowerLevels[nrInstances] = { -1, -1, -2, -2, -1, 0 };
    edouble upperLevels[nrInstances] = {  0, -1, -2, -1,  0, 0 };

    executeScenario9( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );

    return true;
  }

  template< class Profile >
  static bool testScenario10(){
    debugMsg("ResourceTest","  Scenario 10");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());

    Profile profile( db.getId(), detector.getId());

    /*!
     * T5 <= T6
     *
     * Transaction1   [0]-3
     * Transaction2         [10]+3
     * Transaction3         [10]-2
     * Transaction4                               [100]+2
     * Transaction5             <11------[-3]------100>
     * Transaction6                  <12----(+3)---100>
     *                 |     |    |   |             |
     *                 |     |    |   |             |
     * Max level(0)   -3    -2   -2  +1             0
     * Min level(0)   -3    -2   -5  -5             0
     *
     */

    const int nrInstances = 6;

    eint itimes[nrInstances] =         { 0, 10, 11, 12, 100};
    edouble lowerLevels[nrInstances] = {-3, -2, -5, -5,   0};
    edouble upperLevels[nrInstances] = {-3, -2, -2, -2,   0};

    executeScenario10( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );

    return true;
  }

  template< class Profile >
  static bool testScenario11(){
    debugMsg("ResourceTest","  Scenario 11");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());

    Profile profile( db.getId(), detector.getId());

    /*!
     * Transaction1 constrained to be before or at Transaction2
     *
     * Transaction1   <-inf---------[-inf,0]---------inf>
     * Transaction2   <-inf---------[0, inf]---------inf>
     *                 |                              |
     *                 |                              |
     * Max level      inf                            inf
     * Min level     -inf                           -inf
     *
     */

    const int nrInstances = 2;


    eint itimes[nrInstances] = {MINUS_INFINITY,PLUS_INFINITY};
    edouble lowerLevels[nrInstances] = {MINUS_INFINITY,MINUS_INFINITY};
    edouble upperLevels[nrInstances] = {PLUS_INFINITY,PLUS_INFINITY};

    executeScenario11( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );

    return true;
  }

  template< class Profile >
  static bool testScenario12(){
    debugMsg("ResourceTest","  Scenario 12");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    Profile profile( db.getId(), detector.getId());

    executeScenario12( profile, ce );
    return true;
  }

  template< class Profile >
  static bool testScenario13(){
    debugMsg("ResourceTest","  Scenario 13");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    Profile profile( db.getId(), detector.getId());
    /*!
     * Transaction1 constrained to be [1,inf) before Transaction2
     *
     * Transaction1   <0---------(-3)---------9>
     * Transaction2         <1---------(+3)---------10>
     * Transaction2   [0](-3)
     *                 |     |                |      |
     *                 |     |                |      |
     * Max level      -3    -3               -3     -3
     * Min level      -6    -6               -6     -3
     *
     */
    const int nrInstances = 4;


    eint itimes[nrInstances] = {0,1,9,10};
    edouble lowerLevels[nrInstances] = {-6,-6,-6,-3};
    edouble upperLevels[nrInstances] = {-3,-3,-3,-3};

    executeScenario13( profile, ce, nrInstances, itimes, lowerLevels, upperLevels  );

    return true;
  }

  template< class Profile >
  static bool testScenario14(){
    debugMsg("ResourceTest","  Scenario 14");
    RESOURCE_DEFAULT_SETUP(ce, db, true);
    DummyDetector detector(ResourceId::noId());
    Profile profile( db.getId(), detector.getId());

    executeScenario14( profile, ce );
    return true;
  }


  static bool testDeltaTime(){
    return true;
  }
  static bool testDeltaQuantity(){
    return true;
  }
  static bool testDeltaOrdering(){
    return true;
  }
};

class FVDetectorTest {
public:
  static bool test() {
    EUROPA_runTest(testReusableDetector);
    return true;
  }
private:
  static bool testReusableDetector() {
    RESOURCE_DEFAULT_SETUP(ce, db, false);

    Reusable res(db.getId(), LabelStr("Reusable"), LabelStr("res1"), 
                 LabelStr("ClosedWorldFVDetector"), LabelStr("IncrementalFlowProfile"),
                 1, 1, 0);

    //create a token that violates the limit (i.e. consumes 2)
    ReusableToken tok1(db.getId(), LabelStr("Reusable.uses"), 
                       IntervalIntDomain(1), IntervalIntDomain(10),
                       IntervalIntDomain(9), IntervalDomain(2));
    CPPUNIT_ASSERT(!ce.propagate());
    tok1.discard(false);

    //create a token that doesn't
    ReusableToken tok2(db.getId(), LabelStr("Reusable.uses"), 
                       IntervalIntDomain(1, 3), IntervalIntDomain(10, 12),
                       IntervalIntDomain(9),
		       IntervalDomain(1));
    CPPUNIT_ASSERT(ce.propagate());
        //create a token that doesn't, but must start during the previous token, causing a violation
    ReusableToken tok3(db.getId(), LabelStr("Reusable.uses"), IntervalIntDomain(9),
                       IntervalIntDomain(11), IntervalIntDomain(2),
		       IntervalDomain(1));
    CPPUNIT_ASSERT(!ce.propagate());
    tok3.discard(false);
    CPPUNIT_ASSERT(ce.propagate());
    //create a token that doesn't, and may start afterwards, creating a flaw
    ReusableToken tok4(db.getId(), LabelStr("Reusable.uses"), IntervalIntDomain(10, 13), IntervalIntDomain(15, 18), IntervalIntDomain(5),
		       IntervalDomain(1));
    CPPUNIT_ASSERT(ce.propagate());
    CPPUNIT_ASSERT(db.hasOrderingChoice(tok4.getId()));
    debugMsg("ResourceTest","CREATING THE CONSTRAINT");
    res.constrain(tok2.getId(), tok4.getId());
    CPPUNIT_ASSERT(ce.propagate());
    CPPUNIT_ASSERT(!db.hasOrderingChoice(tok4.getId()));
    RESOURCE_DEFAULT_TEARDOWN();
    return true;
  }
};

void FlowProfileModuleTests::cppSetup(void)
{
    setTestLoadLibraryPath(".");
}

void FlowProfileModuleTests::defaultSetupTests(void)
{
	DefaultSetupTest::test();
}

void FlowProfileModuleTests::flowProfileTests(void)
{
  FlowProfileTest::test();
}

void FlowProfileModuleTests::FVDetectorTests(void)
{
  FVDetectorTest::test();
}



