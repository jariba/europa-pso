package gov.nasa.arc.planworks.test;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.*;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.util.SQLDB;
import gov.nasa.arc.planworks.db.util.MySQLDB;

public class MySQLDBTest extends TestCase {
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
  }
  protected void setUp() {
    try {
      new MySQLDB();
      SQLDB.startDatabase();
      SQLDB.registerDatabase();
      SQLDB.cleanDatabase();
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    PlanWorksTest.TEST_RUNNING = 2;
  }
  protected void tearDown() {
      SQLDB.cleanDatabase();
    PlanWorksTest.TEST_RUNNING = 0;
  }
  public static TestSuite suite() {
    return new TestSuite(MySQLDBTest.class);
  }

  public void testLoadFile() {
    try {
      String datadir = 
        System.getProperty("planworks.test.data.dir").concat(
        System.getProperty("file.separator")).concat("loadTest").concat(
        System.getProperty("file.separator"));
      checkConstraintLoad(datadir);
      checkConstraintVarMapLoad(datadir);
      checkObjectLoad(datadir);
      checkPartialPlanLoad(datadir);
      checkProjectLoad(datadir);
      checkSequenceLoad(datadir);
      checkTokenLoad(datadir);
      checkVariableLoad(datadir);
      //checkTransactionLoad(datadir);
      checkPartialPlanStatsLoad(datadir);
      checkResourceInstantsLoad(datadir);
      checkRulesLoad(datadir);
      checkRuleInstanceLoad(datadir);
      checkDecisionLoad(datadir);

    // catch assert errors and Exceptions here, since JUnit seems to not do it
    } catch (AssertionFailedError err) {
      err.printStackTrace();
      System.exit( -1);
    } catch (Exception excp) {
      excp.printStackTrace();
      System.exit( -1);
    }
  }

  private void checkConstraintLoad(String datadir) {
    System.err.println("Checking VConstraint table load");
    SQLDB.loadFile(datadir + "loadCTest", "VConstraint");
    try {
      ResultSet c = SQLDB.queryDatabase("SELECT * FROM VConstraint");
      c.last();
      assertTrue(c.getInt("ConstraintId") == 1);
      assertTrue(c.getLong("PartialPlanId") == 1L);
      assertTrue(c.getString("ConstraintName").equals("constraintname"));
      assertTrue(c.getString("ConstraintType").equals(DbConstants.ATEMPORAL_CONSTRAINT_TYPE));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM VConstraint");
  }
  private void checkObjectLoad(String datadir) {
    System.err.println("Checking Object table load");
    SQLDB.loadFile(datadir + "loadOTest", "Object");
    try {
      ResultSet o = SQLDB.queryDatabase("SELECT * FROM Object");
      o.last();
      assertTrue(o.getInt("ObjectId") == 1);
      assertTrue(o.getInt("ObjectType") == 2);
      assertTrue(o.getInt("ParentId") == 3);
      assertTrue(o.getLong("PartialPlanId") == 4L);
      assertTrue(o.getString("ObjectName").equals("objectname"));
      assertTrue(o.getString("ChildObjectIds").equals("childobjectids"));
      assertTrue(o.getString("VariableIds").equals("variableids"));
      assertTrue(o.getString("TokenIds").equals("tokenids"));
      assertTrue(o.getString("ExtraInfo").equals("extrainfo"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Object");
  }
  private void checkPartialPlanLoad(String datadir) {
    System.err.println("Checking PartialPlan table load");
    SQLDB.loadFile(datadir + "loadPPTest", "PartialPlan");
    try {
      ResultSet p = SQLDB.queryDatabase("SELECT * FROM PartialPlan");
      p.last();
      assertTrue(p.getString("PlanName").equals("planname"));
      assertTrue(p.getLong("PartialPlanId") == 1L);
      assertTrue(p.getString("Model").equals("model"));
      assertTrue(p.getLong("SequenceId") == 1L);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM PartialPlan");
  }
  private void checkProjectLoad(String datadir) {
    System.err.println("Checking Project table load");
    SQLDB.loadFile(datadir + "loadPJTest", "Project");
    try {
      ResultSet p = SQLDB.queryDatabase("SELECT * FROM Project");
      p.last();
      assertTrue(p.getInt("ProjectId") == 1);
      assertTrue(p.getString("ProjectName").equals("projectname"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Project"); 
  }
  private void checkSequenceLoad(String datadir) {
    System.err.println("Checking Sequence table load");
    SQLDB.loadFile(datadir + "loadSQTest", "Sequence");
    try {
      ResultSet s = SQLDB.queryDatabase("SELECT * FROM Sequence");
      s.last();
      assertTrue(s.getString("SequenceURL").equals("sequenceurl"));
      assertTrue(s.getLong("SequenceId") == 1L);
      assertTrue(s.getString("RulesText").equals("rulestext"));
      assertTrue(s.getInt("ProjectId") == 1);
      assertTrue(s.getInt("SequenceOrdering") > 0);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Sequence");
  }
  private void checkConstraintVarMapLoad(String datadir) {
    System.err.println("Checking ConstraintVarMap table load");
    SQLDB.loadFile(datadir + "loadCVMTest", "ConstraintVarMap");
    try {
      ResultSet c = SQLDB.queryDatabase("SELECT * FROM ConstraintVarMap");
      c.last();
      System.err.println("Got: " + c.getInt("ConstraintId") + " " + c.getInt("VariableId") + " " + c.getLong("PartialPlanId"));
      assertTrue(c.getInt("ConstraintId") == 1);
      assertTrue(c.getInt("VariableId") == 2);
      assertTrue(c.getLong("PartialPlanId") == 3L);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM ConstraintVarMap");
  }
  private void checkTokenLoad(String datadir) {
    System.err.println("Checking Token table load");
    SQLDB.loadFile(datadir + "loadTKTest", "Token");
    try {
      ResultSet t = SQLDB.queryDatabase("SELECT * FROM Token");
      t.last();
      assertTrue(t.getInt("TokenId") == 1);
      assertTrue(t.getInt("TokenType") == 1);
      assertTrue(t.getInt("SlotId") == 1);
      assertTrue(t.getInt("SlotIndex") == 1);
      assertTrue(t.getLong("PartialPlanId") == 1L);
      assertTrue(t.getBoolean("IsFreeToken"));
      assertTrue(t.getBoolean("IsValueToken"));
      assertTrue(t.getInt("StartVarId") == 1);
      assertTrue(t.getInt("EndVarId") == 1);
      assertTrue(t.getInt("DurationVarId") == 1);
      assertTrue(t.getInt("StateVarId") == 1);
      assertTrue(t.getString("PredicateName").equals("1"));
      assertTrue(t.getInt("ParentId") == 1);
      assertTrue(t.getString("ParentName").equals("1"));
      assertTrue(t.getInt("ObjectVarId") == 1);
      assertTrue(t.getString("ParamVarIds").equals("1"));
      assertTrue(t.getString("ExtraData").equals("1"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Token");
  }
  private void checkVariableLoad(String datadir) {
    System.err.println("Checking Variable table load");
    SQLDB.loadFile(datadir + "loadVTest", "Variable");
    try {
      ResultSet v = SQLDB.queryDatabase("SELECT * FROM Variable");
      v.last();
      assertTrue(v.getInt("VariableId") == 1);
      assertTrue(v.getLong("PartialPlanId") == 2L);
      assertTrue(v.getInt("ParentId") == 3);
      assertTrue(v.getString("ParameterName").equals("parametername"));
      assertTrue(v.getString("DomainType").equals("EnumeratedDomain"));
      assertTrue(v.getString("EnumDomain").equals("enumdomain"));
      assertTrue(v.getString("IntDomainType").equals("INTEGER_SORT"));
      assertTrue(v.getString("IntDomainLowerBound").equals("1"));
      assertTrue(v.getString("IntDomainUpperBound").equals("10"));
      assertTrue(v.getString("VariableType").equals(DbConstants.GLOBAL_VAR));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Variable");
  }
//   private void checkTransactionLoad(String datadir) {
//     System.err.println("Checking Transaction table load");
//     SQLDB.loadFile(datadir + "loadTATest", "Transaction");
//     try {
//       ResultSet t = SQLDB.queryDatabase("SELECT * FROM Transaction");
//       t.last();
//       assertTrue(t.getString("TransactionName").equals("VARIABLE_DOMAIN_RELAXED"));
//       assertTrue(t.getString("TransactionType").equals("RELAXATION"));
//       assertTrue(t.getInt("ObjectId") == 1);
//       assertTrue(t.getString("Source").equals("SYSTEM"));
//       assertTrue(t.getInt("TransactionId") == 1);
//       assertTrue(t.getInt("StepNumber") == 1);
//       assertTrue(t.getLong("PartialPlanId") == 1L);
//       assertTrue(t.getLong("SequenceId") == 1L);
//       assertTrue(t.getString("TransactionInfo").equals("1"));
//     }
//     catch(SQLException sqle) {
//       sqle.printStackTrace();
//       System.exit(-1);
//     }
//     SQLDB.updateDatabase("DELETE FROM Transaction");
//   }
  private void checkPartialPlanStatsLoad(String datadir) {
    System.err.println("Checking PartialPlanStats table load");
    SQLDB.loadFile(datadir + "loadPSTest", "PartialPlanStats");
    try {
      ResultSet p = SQLDB.queryDatabase("SELECT * FROM PartialPlanStats");
      p.last();
      assertTrue(p.getLong("SequenceId") == 1L);
      assertTrue(p.getLong("PartialPlanId") == 2L);
      assertTrue(p.getInt("StepNum") == 3);
      assertTrue(p.getInt("NumTokens") == 10);
      assertTrue(p.getInt("NumVariables") == 20);
      assertTrue(p.getInt("NumConstraints") == 30);
      //assertTrue(p.getInt("NumTransactions") == 40);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM PartialPlanStats");
  }
  private void checkResourceInstantsLoad(String datadir) {
    System.err.println("Checking ResourceInstants table load");
    SQLDB.loadFile(datadir + "loadINTest", "ResourceInstants");
    try {
      ResultSet r = SQLDB.queryDatabase("SELECT * FROM ResourceInstants");
      r.last();
      assertTrue(r.getLong("PartialPlanId") == 1L);
      assertTrue(r.getInt("ResourceId") == 2);
      assertTrue(r.getInt("InstantId") == 3);
      assertTrue(r.getInt("TimePoint") == 10);
      assertTrue(r.getDouble("LevelMin") == 20.0);
      assertTrue(r.getDouble("LevelMax") == 30.0);
      assertTrue(r.getString("Transactions").equals("transactions"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM ResourceInstants");
  }
  private void checkRulesLoad(String datadir) {
    System.err.println("Checking Rules table load");
    SQLDB.loadFile(datadir + "loadRUTest", "Rules");
    try {
      ResultSet r = SQLDB.queryDatabase("SELECT * FROM Rules");
      r.last();
      assertTrue(r.getLong("SequenceId") == 1L);
      assertTrue(r.getInt("RuleId") == 2);
      assertTrue(r.getString("RuleSource").equals("rulesource"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Rules");
  }
  private void checkRuleInstanceLoad(String datadir) {
    System.err.println("Checking RuleInstance table load");
    SQLDB.loadFile(datadir + "loadRITest", "RuleInstance");
    try {
      ResultSet r = SQLDB.queryDatabase("SELECT * FROM RuleInstance");
      r.last();
      assertTrue(r.getInt("RuleInstanceId") == 1);
      assertTrue(r.getLong("PartialPlanId") == 2L);
      assertTrue(r.getLong("SequenceId") == 3L);
      assertTrue(r.getInt("RuleId") == 4);
      assertTrue(r.getInt("MasterTokenId") == 5);
      assertTrue(r.getString("SlaveTokenIds").equals("slavetokenids"));
      assertTrue(r.getString("RuleVarIds").equals("rulevarids"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM RuleInstance");
  }
  private void checkDecisionLoad(String datadir) {
    System.err.println("Checking Decision table load");
    SQLDB.loadFile(datadir + "loadDTest", "Decision");
    try {
      ResultSet d = SQLDB.queryDatabase("SELECT * FROM Decision");
      d.last();
      assertTrue(d.getLong("PartialPlanId") == 1L);
      assertTrue(d.getInt("DecisionId") == 2);
      assertTrue(d.getInt("DecisionType") == 3);
      assertTrue(d.getInt("EntityId") == 4);
      assertTrue(d.getBoolean("IsUnit"));
      assertTrue(d.getString("Choices").equals("choices"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    SQLDB.updateDatabase("DELETE FROM Decision");
  }
}

