package gov.nasa.arc.planworks.test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import junit.framework.*;

import gov.nasa.arc.planworks.db.*;
import gov.nasa.arc.planworks.db.impl.*;
import gov.nasa.arc.planworks.db.util.MySQLDB;

public class BackendTest extends TestCase {
  private static PwPartialPlanImpl plan1, plan2, plan3, plan4, plan5;
  private static PwPlanningSequenceImpl sequence;
  private static final String step1 = "step2";
  private static final String step2 = "step9";
  private static final String step3 = "step0";
  private static final String step4 = "step1";
  private static final String step5 = "step8";
  private static final String sequenceName = "Camera1065545818740";
  private static final int numTests = 3;
  private static int testsRun = 0;
  static {
    try {
      MySQLDB.startDatabase();
      MySQLDB.registerDatabase();
      MySQLDB.loadFile(System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat("test.sequence"), "Sequence");
      String p1 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step1).concat(System.getProperty("file.separator")).concat(step1).concat(".");
      String p2 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step2).concat(System.getProperty("file.separator")).concat(step2).concat(".");
      String p3 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step3).concat(System.getProperty("file.separator")).concat(step3).concat(".");
      String p4 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step4).concat(System.getProperty("file.separator")).concat(step4).concat(".");
      String p5 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step5).concat(System.getProperty("file.separator")).concat(step5).concat(".");
      for(int i = 0; i < DbConstants.NUMBER_OF_PP_FILES; i++) {
        MySQLDB.loadFile(p1.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
        MySQLDB.loadFile(p2.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
        MySQLDB.loadFile(p3.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
        MySQLDB.loadFile(p4.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
        MySQLDB.loadFile(p5.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
      }
      sequence = new PwPlanningSequenceImpl(System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName), MySQLDB.latestSequenceId(), new PwModelImpl());
      plan1 = (PwPartialPlanImpl) sequence.getPartialPlan(step1);
      plan2 = (PwPartialPlanImpl) sequence.getPartialPlan(step2);
      plan3 = (PwPartialPlanImpl) sequence.getPartialPlan(step3);
      plan4 = (PwPartialPlanImpl) sequence.getPartialPlan(step4);
      plan5 = (PwPartialPlanImpl) sequence.getPartialPlan(step5);
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  } 
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
  }
  protected void setUp() {
    //while(PlanWorksTest.TEST_RUNNING != 0 && PlanWorksTest.TEST_RUNNING != 1) {
    //  try{Thread.sleep(50);}catch(Exception e){}
    //}
    setTestRunning(1);
  }
  protected void tearDown() {
    if(testsRun != numTests) {
      return;
    }
    for(int i = 0; i < DbConstants.PW_DB_TABLES.length; i++) {
      MySQLDB.updateDatabase("DELETE FROM ".concat(DbConstants.PW_DB_TABLES[i]));
    }
    setTestRunning(0);
  }
  synchronized private void setTestRunning(int t) {
    PlanWorksTest.TEST_RUNNING = t;
  }
  synchronized private void incTestsRun() {
    testsRun++;
  }
  public BackendTest(String testType) {
    super(testType);
  }
  public static TestSuite suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new BackendTest("testPlanLoad"));
    suite.addTest(new BackendTest("testDataConsistency"));
    suite.addTest(new BackendTest("testTransactionQueries"));
    return suite;
  }
  public void testPlanLoad() {
    assertTrue("Plan 1 is null", plan1 != null);
    assertTrue("Plan 2 is null", plan2 != null);
    assertTrue("Plan 3 is null", plan3 != null);
    assertTrue("Plan 4 is null", plan4 != null);
    assertTrue("Plan 5 is null", plan5 != null);
    assertTrue("Plan 1 is inconsistant", plan1.checkPlan());
    assertTrue("Plan 2 is inconsistant", plan2.checkPlan());
    assertTrue("Plan 3 is inconsistant", plan3.checkPlan());
    assertTrue("Plan 4 is inconsistant", plan4.checkPlan());
    assertTrue("Plan 5 is inconsistant", plan5.checkPlan());
    //testsRun++;
    incTestsRun();
  }
  public void testDataConsistency() {
    PwPartialPlanImpl [] temp = new PwPartialPlanImpl [] {plan1, plan2, plan3, plan4, plan5};
    for(int i = 0; i < temp.length; i++) {
      Map ids = MySQLDB.queryAllIdsForPartialPlan(temp[i].getId());
      List objectIdList = (List) ids.get(DbConstants.TBL_OBJECT);
      List tokenIdList = (List) ids.get(DbConstants.TBL_TOKEN);
      List variableIdList = (List) ids.get(DbConstants.TBL_VARIABLE);
      List predicateIdList = (List) ids.get(DbConstants.TBL_PREDICATE);

      List objectList = temp[i].getObjectList();
      ListIterator objectIterator = objectList.listIterator();
      while(objectIterator.hasNext()) {
        PwObject object = (PwObject) objectIterator.next();
        if(objectIdList.contains(object.getId())) {
          objectIterator.remove();
          objectIdList.remove(object.getId());
          List timelineIdList = MySQLDB.queryTimelineIdsForObject(temp[i].getId(), object.getId()); 
          List timelineList = object.getTimelineList();
          ListIterator timelineIterator = timelineList.listIterator();
          while(timelineIterator.hasNext()) {
            PwTimeline timeline = (PwTimeline) timelineIterator.next();
            if(timelineIdList.contains(timeline.getId())) {
              timelineIterator.remove();
              timelineIdList.remove(timeline.getId());
            }
            List slotIdList = MySQLDB.querySlotIdsForTimeline(temp[i].getId(), object.getId(), 
                                                              timeline.getId());
            List slotList = timeline.getSlotList();
            ListIterator slotIterator = slotList.listIterator();
            while(slotIterator.hasNext()) {
              PwSlot slot = (PwSlot) slotIterator.next();
              if(slotIdList.contains(slot.getId())) {
                slotIterator.remove();
                slotIdList.remove(slot.getId());
              }
              List tokenList = slot.getTokenList();
              ListIterator tokenIterator = tokenList.listIterator();
              while(tokenIterator.hasNext()) {
                PwToken token = (PwToken) tokenIterator.next();
                if(tokenIdList.contains(token.getId())) {
                  tokenIterator.remove();
                  tokenIdList.remove(token.getId());
                }
                assertTrue("Instantiated predicate not in db.", 
                           predicateIdList.contains(token.getPredicate().getId()));

                if(variableIdList.contains(token.getStartVariable().getId())) {
                  variableIdList.remove(token.getStartVariable().getId());
                }
                else {
                  assertTrue("Instantiated variable not in db.", false);
                }
                if(variableIdList.contains(token.getEndVariable().getId())) {
                  variableIdList.remove(token.getEndVariable().getId());
                }
                else {
                  assertTrue("Instantiated variable not in db.", false);
                }
                if(variableIdList.contains(token.getDurationVariable().getId())) {
                  variableIdList.remove(token.getDurationVariable().getId());
                }
                else {
                  assertTrue("Instantiated variable not in db.", false);
                }
                if(variableIdList.contains(token.getObjectVariable().getId())) {
                  variableIdList.remove(token.getObjectVariable().getId());
                }
                else {
                  assertTrue("Instantiated variable not in db.", false);
                }
                if(variableIdList.contains(token.getRejectVariable().getId())) {
                  variableIdList.remove(token.getRejectVariable().getId());
                }
                else {
                  assertTrue("Instantiated variable not in db.", false);
                }
                List paramVarsList = token.getParamVarsList();
                ListIterator paramVarIterator = paramVarsList.listIterator();
                while(paramVarIterator.hasNext()) {
                  PwVariable var = (PwVariable) paramVarIterator.next();
                  if(variableIdList.contains(var.getId())) {
                    paramVarIterator.remove();
                    variableIdList.remove(var.getId());
                  }
                }
                assertTrue("Instantiated more variables than in db.", paramVarsList.size() == 0);
                List tokenRelationIdList = MySQLDB.queryTokenRelationIdsForToken(temp[i].getId(),
                                                                                 token.getId());
                List lTokenRelationIdList = token.getTokenRelationIdsList();
                ListIterator tokenRelationIdIterator = lTokenRelationIdList.listIterator();
                while(tokenRelationIdIterator.hasNext()) {
                  Integer trId = (Integer) tokenRelationIdIterator.next();
                  if(tokenRelationIdList.contains(trId)) {
                    tokenRelationIdIterator.remove();
                    tokenRelationIdList.remove(trId);
                  }
                }
                assertTrue("Instantiated more token relations than in db.", 
                           lTokenRelationIdList.size() == 0);
                assertTrue("Failed to instantiate all token relations in db.",
                           tokenRelationIdList.size() == 0);
              }
              assertTrue("Instantiated more tokens than in db.", tokenList.size() == 0);
            }
            assertTrue("Instantiated more slots than in db.",
                       slotList.size() == 1 || slotList.size() == 0);
            assertTrue("Failed to instantiate all slots in db.", slotIdList.size() == 0);
          }
          assertTrue("Instantiated more timelines than in db.", timelineList.size() == 0);
          assertTrue("Failed to instantiate all timelines in db.", timelineIdList.size() == 0);
        }
        assertTrue("Instantiated more objects than in db.", objectList.size() == 0);
      }
      List freeTokens = temp[i].getFreeTokenList();
      ListIterator freeTokenIterator = freeTokens.listIterator();
      while(freeTokenIterator.hasNext()) {
        PwToken token = (PwToken) freeTokenIterator.next();
        if(tokenIdList.contains(token.getId())) {
          freeTokenIterator.remove();
          tokenIdList.remove(token.getId());
        }
        assertTrue("Instantiated predicate not in db.",
                   predicateIdList.contains(token.getPredicate().getId()));
        if(variableIdList.contains(token.getStartVariable().getId())) {
          variableIdList.remove(token.getStartVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(token.getEndVariable().getId())) {
          variableIdList.remove(token.getEndVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(token.getDurationVariable().getId())) {
          variableIdList.remove(token.getDurationVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(token.getObjectVariable().getId())) {
          variableIdList.remove(token.getObjectVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(token.getRejectVariable().getId())) {
          variableIdList.remove(token.getRejectVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        List paramVarsList = token.getParamVarsList();
        ListIterator paramVarIterator = paramVarsList.listIterator();
        while(paramVarIterator.hasNext()) {
          PwVariable var = (PwVariable) paramVarIterator.next();
          if(variableIdList.contains(var.getId())) {
            paramVarIterator.remove();
            variableIdList.remove(var.getId());
          }
        }
        assertTrue("Instantiated more variables than in db.", paramVarsList.size() == 0);
        List tokenRelationIdList = MySQLDB.queryTokenRelationIdsForToken(temp[i].getId(), token.getId());
        List lTokenRelationIdList = token.getTokenRelationIdsList();
        ListIterator tokenRelationIdIterator = lTokenRelationIdList.listIterator();
        while(tokenRelationIdIterator.hasNext()) {
          Integer trId = (Integer) tokenRelationIdIterator.next();
          if(tokenRelationIdList.contains(trId)) {
            tokenRelationIdIterator.remove();
            tokenRelationIdList.remove(trId);
          }
        }
        assertTrue("Instantiated more token relations than in db.", lTokenRelationIdList.size() == 0);
        assertTrue("Failed to instantiate all token relations in db.", tokenRelationIdList.size() == 0);
      }
      assertTrue("Instantiated more free tokens than in db.", freeTokens.size() == 0);
      assertTrue("Instantiated more objects than in db.", objectList.size() == 0);
      assertTrue("Failed to instantiate all objects in db.", objectIdList.size() == 0);
      assertTrue("Failed to instantiate all tokens in db.", tokenIdList.size() == 0);
      assertTrue("Failed to instantiate all variables in db.", variableIdList.size() == 0);
    }
    //testsRun++;
    incTestsRun();
  }

  public void testTransactionQueries() {
    List plan1Transactions = sequence.getTransactionsList(plan1.getId());
    List plan2Transactions = sequence.getTransactionsList(plan2.getId());
    ListIterator plan1TransactionIterator = plan1Transactions.listIterator();
    ListIterator plan2TransactionIterator = plan2Transactions.listIterator();
    List plan1TransactionIds = MySQLDB.queryTransactionIdsForPartialPlan(sequence.getId(),
                                                                         plan1.getId());
    List plan2TransactionIds = MySQLDB.queryTransactionIdsForPartialPlan(sequence.getId(),
                                                                         plan2.getId());
    while(plan1TransactionIterator.hasNext()) {
      PwTransaction transaction = (PwTransaction) plan1TransactionIterator.next();
      if(plan1TransactionIds.contains(transaction.getId())) {
        plan1TransactionIds.remove(transaction.getId());
        plan1TransactionIterator.remove();
      }
    }
    while(plan2TransactionIterator.hasNext()) {
      PwTransaction transaction = (PwTransaction) plan2TransactionIterator.next();
      if(plan2TransactionIds.contains(transaction.getId())) {
        plan2TransactionIds.remove(transaction.getId());
        plan2TransactionIterator.remove();
      }
    }
    assertTrue("Failed to instantiate all transactions in db.", plan1TransactionIds.size() == 0);
    assertTrue("Instantiated transactions not in db.", plan1Transactions.size() == 0);
    assertTrue("Failed to instantiate all transactions in db.", plan2TransactionIds.size() == 0);
    assertTrue("Instantiated transactions not in db.", plan2Transactions.size() == 0);
    testQueriesForConstraint();
    testQueriesForToken();
    testQueriesForVariable();
    testQueriesForRestrictionsAndRelaxations();
    testQueriesForDecisions();
    //testsRun++;
    incTestsRun();
  }

  private void testQueriesForConstraint() {
    List transactions = MySQLDB.queryTransactionsForConstraint(sequence.getId(), new Integer(16));
    assertTrue("Wrong number of constraint transactions.  Is " + transactions.size() + " should be 0",
               transactions.size() == 0);
    transactions = MySQLDB.queryTransactionsForConstraint(sequence.getId(), new Integer(53));
    assertTrue("Wrong number of constraint transactions.  Is " + transactions.size() + " should be 1",
               transactions.size() == 1);
    List steps = MySQLDB.queryStepsWithConstraintTransaction(sequence.getId(), new Integer(53), 
                                                             "CONSTRAINT_CREATED");
    assertTrue("Wrong number of steps.  Is " + steps.size() + " should be 1", steps.size() == 1);
    assertTrue("Incorret step.  Is " + ((Integer)steps.get(0)) + " should be 2", 
               ((Integer)steps.get(0)).equals(new Integer(2)));
  }

  private void testQueriesForToken() {
    List transactions = MySQLDB.queryTransactionsForToken(sequence.getId(), new Integer(2));
    assertTrue(transactions.size() == 0);
    transactions = MySQLDB.queryTransactionsForToken(sequence.getId(), new Integer(21));
    assertTrue("Wrong number of token transactions.  Is " + transactions.size() + " should be 2", 
               transactions.size() == 2);
    List steps = MySQLDB.queryStepsWithTokenTransaction(sequence.getId(), new Integer(21),
                                                        "TOKEN_INSERTED");
    assertTrue("Wrong number of steps.  Is " + steps.size() + " should be 1", steps.size() == 1);
    assertTrue("Incorrect step.  Is " + ((Integer)steps.get(0)) + " should be 2", 
               ((Integer)steps.get(0)).equals(new Integer(2)));
  }

  private void testQueriesForVariable() {
    List transactions = MySQLDB.queryTransactionsForVariable(sequence.getId(), new Integer(2));
    assertTrue(transactions.size() == 0);
    transactions = MySQLDB.queryTransactionsForVariable(sequence.getId(), new Integer(50));
    assertTrue(transactions.size() == 1);
    List steps = MySQLDB.queryStepsWithVariableTransaction(sequence.getId(), new Integer(50),
                                                           "VARIABLE_CREATED");
    assertTrue(steps.size() == 1);
    assertTrue(((Integer)steps.get(0)).equals(new Integer(2)));
  }

  private void testQueriesForRestrictionsAndRelaxations() {
    List steps = MySQLDB.queryStepsWithRestrictions(sequence.getId());
    assertTrue("Wrong number of steps.  Was " + steps.size() + " should be 4", steps.size() == 4);
    assertTrue(((Integer)steps.get(0)).equals(new Integer(2)));
    steps = MySQLDB.queryStepsWithRelaxations(sequence.getId());
    assertTrue(steps.size() == 0);
  }
  private void testQueriesForDecisions() {
    List steps = MySQLDB.queryStepsWithUnitVariableDecisions(sequence);
    assertTrue(steps.size() == 1);
    assertTrue(((Integer)steps.get(0)).equals(new Integer(9)));
    steps = MySQLDB.queryStepsWithNonUnitVariableDecisions(sequence);
    assertTrue(steps.size() == 1);
    assertTrue(((Integer)steps.get(0)).equals(new Integer(1)));
  }
}
