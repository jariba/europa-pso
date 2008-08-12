//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: BackendTest.java,v 1.19 2006-10-03 16:14:17 miatauro Exp $
//
package gov.nasa.arc.planworks.test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import junit.framework.*;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.db.*;
import gov.nasa.arc.planworks.db.impl.*;
import gov.nasa.arc.planworks.db.util.SQLDB;

public class BackendTest extends TestCase implements IdSource {

  private static final String BACKEND_TEST_DIR = "backendTest";
  private static PwPartialPlanImpl plan;
  private static PwPlanningSequenceImpl sequence;
  private static String sequenceName;
  private static List sequenceUrls;
  private static final String step0 = "step0";
  private static final int numTests = 3;
  private static int testsRun = 0;
  private int entityIdInt;

  //static initializer block runs when class is first loaded
  static {
    //inner class to provide IdSource methods to buildTestData method since they won't have
    //been created yet for BackendTest at the time the class is first loaded.
    class IdSourceImpl implements IdSource {
      // implements IdSource
      private int entityIdInt;
      public int incEntityIdInt() {
        return ++entityIdInt;
      }
      public void resetEntityIdInt() {
        entityIdInt = 0;
      }
    }
    IdSourceImpl idSource = new IdSourceImpl();

    int numSequences = 1, numSteps = 1;
    sequenceUrls = PWSetupHelper.buildTestData(numSequences, numSteps, idSource ,
                                               BACKEND_TEST_DIR);
    sequenceName = (String) sequenceUrls.get( 0);

    File configFile = new File( System.getProperty( "projects.config") + ".template");
    ConfigureAndPlugins.processProjectsConfigFile( configFile);
    try {
      SQLDB.startDatabase();
      SQLDB.registerDatabase();
      String seq = sequenceName.concat(System.getProperty("file.separator"));
      
      SQLDB.loadFile(seq + "sequence", "Sequence", DbConstants.SEQ_COL_SEP_HEX, 
                       DbConstants.SEQ_LINE_SEP_HEX);
      for(int i = 1; i < DbConstants.NUMBER_OF_SEQ_FILES; i++) {
        SQLDB.loadFile(seq.toString().concat(DbConstants.SEQUENCE_FILES[i]), 
                         DbConstants.PW_DB_TABLES[i + DbConstants.NUMBER_OF_PP_FILES + 1]);
      }
      String p1 = sequenceName.concat(System.getProperty("file.separator")).concat(step0).concat(
                  System.getProperty("file.separator")).concat(step0).concat(".");
      for(int i = 0; i < DbConstants.NUMBER_OF_PP_FILES; i++) {
        SQLDB.loadFile(p1.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
      }
      sequence = new PwPlanningSequenceImpl(sequenceName, SQLDB.latestSequenceId(),
                                            ConfigureAndPlugins.DEFAULT_PROJECT_NAME);
      plan = (PwPartialPlanImpl) sequence.getPartialPlan(step0);
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
    setTestRunning(1);
  }
  protected void tearDown() {
    if(testsRun != numTests) {
      return;
    }
    for(int i = 0; i < DbConstants.PW_DB_TABLES.length; i++) {
      SQLDB.updateDatabase("DELETE FROM ".concat(DbConstants.PW_DB_TABLES[i]));
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
    //suite.addTest(new BackendTest("testTransactionQueries"));
    return suite;
  }

  public void testPlanLoad() {
    try {
      assertTrue("Test plan is null", plan != null);
      assertTrue("Test plan is inconsistant", plan.checkPlan());
      System.err.println("Done with testPlanLoad");

      incTestsRun();

    // catch assert errors and Exceptions here, since JUnit seems to not do it
    } catch (AssertionFailedError err) {
      err.printStackTrace();
      System.exit( -1);
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  }

  public void testDataConsistency() throws Exception {
    System.err.println("testDataConsistancy...");
    try {
      Map ids = SQLDB.queryAllIdsForPartialPlan(plan.getId());
      List objectIdList = (List) ids.get(DbConstants.TBL_OBJECT);
      List tokenIdList = (List) ids.get(DbConstants.TBL_TOKEN);
      List variableIdList = (List) ids.get(DbConstants.TBL_VARIABLE);
      List ruleInstanceIdList = (List) ids.get(DbConstants.TBL_RULE_INSTANCE);
      List instantIdList = (List) ids.get(DbConstants.TBL_INSTANTS);
      List constraintIdList = (List) ids.get(DbConstants.TBL_CONSTRAINT);
      //System.err.println("Number of variable constraint ids is " + constraintIdList.size());

      System.err.println("Checking resource transaction tokens and variables");
      List resourceTransactionList = plan.getResTransactionList();
      ListIterator resourceTransactionIterator = resourceTransactionList.listIterator();
      //System.err.println("Number of resource transactions is " + resourceTransactionList.size());
      while(resourceTransactionIterator.hasNext()) {
        PwResourceTransaction resourceTransaction = (PwResourceTransaction) resourceTransactionIterator.next();
        if(tokenIdList.contains(resourceTransaction.getId())) {
          resourceTransactionIterator.remove();
          tokenIdList.remove(resourceTransaction.getId());
        }
        if(variableIdList.contains(resourceTransaction.getStartVariable().getId())) {
          variableIdList.remove(resourceTransaction.getStartVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(resourceTransaction.getDurationVariable().getId())) {
          variableIdList.remove(resourceTransaction.getDurationVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(resourceTransaction.getObjectVariable().getId())) {
          variableIdList.remove(resourceTransaction.getObjectVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        if(variableIdList.contains(resourceTransaction.getStateVariable().getId())) {
          variableIdList.remove(resourceTransaction.getStateVariable().getId());
        }
        else {
          assertTrue("Instantiated variable not in db.", false);
        }
        List paramVarsList = resourceTransaction.getParamVarsList();
        ListIterator paramVarIterator = paramVarsList.listIterator();
        while(paramVarIterator.hasNext()) {
          PwVariable var = (PwVariable) paramVarIterator.next();
          if(variableIdList.contains(var.getId())) {
            paramVarIterator.remove();
            variableIdList.remove(var.getId());
          }
        }
        assertTrue("Instantiated more variables than in db.", paramVarsList.size() == 0);
      }
      assertTrue("Instantiated more resource transaction tokens than in db.", 
                 resourceTransactionList.size() == 0);

      System.err.println("Checking objects, member variables, and resources");
      System.err.println("Checking timelines, slots, tokens and variables");
      List objectList = plan.getObjectList();
      ListIterator objectIterator = objectList.listIterator();
      while(objectIterator.hasNext()) {
        PwObject object = (PwObject) objectIterator.next();
        if(objectIdList.contains(object.getId())) {
          objectIterator.remove();
          objectIdList.remove(object.getId());
          //check member variables for all types of objects
          List objectVarList = object.getVariables();
          ListIterator objectVarIterator = objectVarList.listIterator();
          while(objectVarIterator.hasNext()) {
            PwVariable var = (PwVariable) objectVarIterator.next();
            if(variableIdList.contains(var.getId())) {
              objectVarIterator.remove();
              variableIdList.remove(var.getId());
            }
          }
          assertTrue("Instantiated more object member variables than in db.", objectVarList.size() == 0);

          List resourceIdList = SQLDB.queryResourceIdsForObject(plan.getId(), object.getId()); 
          List componentList = object.getComponentList();
          //first, check resource objects for this parent object
          ListIterator componentIterator = componentList.listIterator();
          while(componentIterator.hasNext()) {
            PwObject component = (PwObject) componentIterator.next();
            if (component.getObjectType() == DbConstants.O_RESOURCE) {
              componentIterator.remove();
              resourceIdList.remove(component.getId());
            }
          }
          assertTrue("Failed to instantiate all resources in db.", resourceIdList.size() == 0);
  
          List timelineIdList = SQLDB.queryTimelineIdsForObject(plan.getId(), object.getId()); 
          ListIterator timelineIterator = componentList.listIterator();
          while(timelineIterator.hasNext()) {
            PwTimeline timeline = (PwTimeline) timelineIterator.next();
            if(timelineIdList.contains(timeline.getId())) {
              timelineIterator.remove();
              timelineIdList.remove(timeline.getId());
            }
            List slotIdList = SQLDB.querySlotIdsForTimeline(plan.getId(), object.getId(), 
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
                if(variableIdList.contains(token.getStateVariable().getId())) {
                  variableIdList.remove(token.getStateVariable().getId());
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
                assertTrue("Instantiated more variables than in db.", 
                            paramVarsList.size() == 0);
              }
              assertTrue("Instantiated more tokens than in db.", tokenList.size() == 0);
            }
            assertTrue("Instantiated more slots than in db.",
                       slotList.size() == 1 || slotList.size() == 0);
            assertTrue("Failed to instantiate all slots in db.", slotIdList.size() == 0);
          }
          assertTrue("Instantiated more timelines and/or resources than in db.", 
                     componentList.size() == 0);
          assertTrue("Failed to instantiate all timelines in db.", 
                     timelineIdList.size() == 0);
        }
      }
      assertTrue("Instantiated more objects than in db.", objectList.size() == 0);

      //checking all resource instants in plan
      System.err.println("Checking resource instants");
      List resourceInstants = plan.getInstantList();
      ListIterator instantIterator = resourceInstants.listIterator();
      while(instantIterator.hasNext()) {
        PwResourceInstant instant = (PwResourceInstant) instantIterator.next();
          instantIterator.remove();
          instantIdList.remove(instant.getId());
      }
      assertTrue("Instantiated more resource instants than in db.", 
                 resourceInstants.size() == 0);
      assertTrue("Failed to instantiate all resource instants in db.", 
                 instantIdList.size() == 0);

      //checking all variable constrainsts in plan
      System.err.println("Checking constraints");
      List vConstraints = plan.getConstraintList();
      ListIterator constraintIterator = vConstraints.listIterator();
      while(constraintIterator.hasNext()) {
        PwConstraint constraint = (PwConstraint) constraintIterator.next();
          //System.err.println("Found constraint id " + constraint.getId());
          constraintIterator.remove();
          constraintIdList.remove(constraint.getId());
      }
      assertTrue("Instantiated more constraints than in db.", vConstraints.size() == 0);
      assertTrue("Failed to instantiate all constraints in db.", 
                 constraintIdList.size() == 0);

      System.err.println("Checking free tokens and variables");
      List freeTokens = plan.getFreeTokenList();
      ListIterator freeTokenIterator = freeTokens.listIterator();
      while(freeTokenIterator.hasNext()) {
        PwToken token = (PwToken) freeTokenIterator.next();
        if(tokenIdList.contains(token.getId())) {
          freeTokenIterator.remove();
          tokenIdList.remove(token.getId());
        }
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
        if(variableIdList.contains(token.getStateVariable().getId())) {
          variableIdList.remove(token.getStateVariable().getId());
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
      }
      assertTrue("Instantiated more free tokens than in db.", freeTokens.size() == 0);
      assertTrue("Instantiated more objects than in db.", objectList.size() == 0);
      assertTrue("Failed to instantiate all objects in db.", objectIdList.size() == 0);
      assertTrue("Failed to instantiate all tokens in db.", tokenIdList.size() == 0);

      System.err.println("Checking rules, rule instances and rule variables");
      List ruleIdList = SQLDB.queryRuleIdsForSequence(sequence.getId()); 
      List rules = sequence.getRuleList();
      ListIterator ruleIterator = rules.listIterator();
      while(ruleIterator.hasNext()) {
        PwRule rule = (PwRule) ruleIterator.next();
        if(ruleIdList.contains(rule.getId())) {
          //System.err.println("Found rule id " + rule.getId());
          ruleIterator.remove();
          ruleIdList.remove(rule.getId());
        }
      }
      assertTrue("Failed to instantiate all rules in db.", ruleIdList.size() == 0);
      assertTrue("Instantiated more rules than in db.", rules.size() == 0);

      List ruleInstances = plan.getRuleInstanceList();
      ListIterator ruleInstanceIterator = ruleInstances.listIterator();
      while(ruleInstanceIterator.hasNext()) {
        PwRuleInstance ri = (PwRuleInstance) ruleInstanceIterator.next();
        if(ruleInstanceIdList.contains(ri.getId())) {
          ruleInstanceIdList.remove(ri.getId());
        }
        else {
          assertTrue("Instantiated rule instance not in db.", false);
        }
        List ruleVarList = ri.getVariables();
        ListIterator ruleVarIterator = ruleVarList.listIterator();
        while(ruleVarIterator.hasNext()) {
          PwVariable var = (PwVariable) ruleVarIterator.next();
          if(variableIdList.contains(var.getId())) {
            ruleVarIterator.remove();
            variableIdList.remove(var.getId());
          }
        }
        assertTrue("Instantiated more rule variables than in db.", 
                   ruleVarList.size() == 0);
      }
      assertTrue("Failed to instantiate all rule instances in db.", 
                 ruleInstanceIdList.size() == 0);
      assertTrue("Failed to instantiate all variables in db.", 
                 variableIdList.size() == 0);
      System.err.println("Done with testDataConsistency");

      incTestsRun();

    // catch assert errors and Exceptions here, since JUnit seems to not do it
    } catch (AssertionFailedError err) {
      err.printStackTrace();
      System.exit( -1);
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  }

//   public void testTransactionQueries() {
//     try {
//       Map ids = SQLDB.queryAllIdsForPartialPlan(plan.getId());
//       List tokenIdList = (List) ids.get(DbConstants.TBL_TOKEN);
//       List variableIdList = (List) ids.get(DbConstants.TBL_VARIABLE);
//       List constraintIdList = (List) ids.get(DbConstants.TBL_CONSTRAINT);
//       List planTransactions = sequence.getTransactionsList(plan.getId());
//       ListIterator planTransactionIterator = planTransactions.listIterator();
//       List planTransactionIds = SQLDB.queryTransactionIdsForPartialPlan(sequence.getId(),
//                                                                          plan.getId());
//       while(planTransactionIterator.hasNext()) {
//         PwDBTransaction transaction = (PwDBTransaction) planTransactionIterator.next();
//         if(planTransactionIds.contains(transaction.getId())) {
//           planTransactionIds.remove(transaction.getId());
//           planTransactionIterator.remove();
//         }
//       }
//       assertTrue("Failed to instantiate all transactions in db", planTransactionIds.size() == 0);
//       assertTrue("Instantiated transactions not in db.", planTransactions.size() == 0);

//       testQueriesForConstraint(constraintIdList.size());
//       testQueriesForToken(tokenIdList.size());
//       testQueriesForVariable(variableIdList.size());
//       System.err.println("Done with testTransactionQueries");

//       incTestsRun();

//     // catch assert errors and Exceptions here, since JUnit seems to not do it
//     } catch (AssertionFailedError err) {
//       err.printStackTrace();
//       System.exit( -1);
//     } catch (Exception excp) {
//       excp.printStackTrace();
//       System.exit( -1);
//     }
//   }

//   private void testQueriesForConstraint(int numConstraints) {
//     System.err.println("Checking constraint transactions");
//     List transactions = SQLDB.queryStepsWithConstraintTransaction(sequence.getId(), "CONSTRAINT_CREATED");
//     //System.err.println("Found " + transactions.size() +" CONSTRAINT_CREATED transactions");
//     assertTrue("Wrong number of CONSTRAINT_CREATED transactions. Is " + transactions.size() + " should be " + 
//                 numConstraints, transactions.size() == numConstraints);

//     //get the constraint id for the first transaction in the list
//     PwDBTransactionImpl constraintTrans = (PwDBTransactionImpl) transactions.get(0);
//     Integer constraintId = constraintTrans.getEntityId();
//     //System.err.println("Constraint id for the first transaction is " + constraintId);
//     transactions = SQLDB.queryTransactionsForConstraint(sequence.getId(), constraintId);
//     assertTrue("Wrong number of constraint transactions.  Is " + transactions.size() + " should be 1",
//                transactions.size() == 1);
//     transactions = SQLDB.queryTransactionsForConstraint(sequence.getId(), new Integer(1));
//     assertTrue("Wrong number of constraint transactions.  Is " + transactions.size() + " should be 0",
//                transactions.size() == 0);
//   }

//   private void testQueriesForToken(int numTokens) {
//     System.err.println("Checking token transactions");
//     List transactions = SQLDB.queryStepsWithTokenTransaction(sequence.getId(), "TOKEN_CREATED");
//     //System.err.println("Found " + transactions.size() +" TOKEN_CREATED transactions");
//     assertTrue("Wrong number of TOKEN_CREATED transactions. Is " + transactions.size() + " should be " + 
//                 numTokens, transactions.size() == numTokens);

//     //get the token id for the first transaction in the list
//     PwDBTransactionImpl tokenTrans = (PwDBTransactionImpl) transactions.get(0);
//     Integer tokenId = tokenTrans.getEntityId();
//     //System.err.println("Token id for the first transaction is " + tokenId);
//     transactions = SQLDB.queryTransactionsForToken(sequence.getId(), tokenId);
//     assertTrue("Wrong number of token transactions.  Is " + transactions.size() + " should be 1",
//                transactions.size() == 1);
//     transactions = SQLDB.queryTransactionsForToken(sequence.getId(), new Integer(1));
//     assertTrue("Wrong number of token transactions.  Is " + transactions.size() + " should be 0",
//                transactions.size() == 0);
//   }

//   private void testQueriesForVariable(int numVariables) {
//     System.err.println("Checking variable transactions");
//     List transactions = SQLDB.queryStepsWithVariableTransaction(sequence.getId(), "VARIABLE_CREATED");
//     //System.err.println("Found " + transactions.size() +" VARIABLE_CREATED transactions");
//     assertTrue("Wrong number of VARIABLE_CREATED transactions. Is " + transactions.size() + " should be " + 
//                 numVariables, transactions.size() == numVariables);

//     //get the variable id for the first transaction in the list
//     PwDBTransactionImpl variableTrans = (PwDBTransactionImpl) transactions.get(0);
//     Integer variableId = variableTrans.getEntityId();
//     //System.err.println("Variable id for the first transaction is " + variableId);
//     transactions = SQLDB.queryTransactionsForVariable(sequence.getId(), variableId);
//     assertTrue("Wrong number of variable transactions.  Is " + transactions.size() + " should be 1",
//                transactions.size() == 1);
//     transactions = SQLDB.queryTransactionsForVariable(sequence.getId(), new Integer(1));
//     assertTrue("Wrong number of variable transactions.  Is " + transactions.size() + " should be 0",
//                transactions.size() == 0);
//   }


  // implements IdSource
  public int incEntityIdInt() {
    return ++entityIdInt;
  }

  // implements IdSource
  public void resetEntityIdInt() {
    entityIdInt = 0;
  }

}

