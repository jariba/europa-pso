package gov.nasa.arc.planworks.test;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.*;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.util.MySQLDB;

public class MySQLDBTest extends TestCase {
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
  }
  protected void setUp() {
    //while(PlanWorksTest.TEST_RUNNING != 0) {
    //  try{Thread.sleep(50);}catch(Exception e){}
    // }
    try {
      MySQLDB.startDatabase();
      MySQLDB.registerDatabase();
    }
    catch(Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    PlanWorksTest.TEST_RUNNING = 2;
  }
  protected void tearDown() {
    PlanWorksTest.TEST_RUNNING = 0;
  }
  public static TestSuite suite() {
    return new TestSuite(MySQLDBTest.class);
  }
  public void testLoadFile() {
    String datadir = 
      System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat("loadTest").concat(System.getProperty("file.separator"));
    checkConstraintLoad(datadir);
    //checkEnumerationLoad(datadir);
    //checkIntervalLoad(datadir);
    checkObjectLoad(datadir);
    //checkPVTMLoad(datadir);
    checkParamLoad(datadir);
    checkPartialPlanLoad(datadir);
    checkPredicateLoad(datadir);
    checkProjectLoad(datadir);
    checkSequenceLoad(datadir);
    //checkSlotLoad(datadir);
    //checkTimelineLoad(datadir);
    checkTokenLoad(datadir);
    checkTokenRelationLoad(datadir);
    checkVariableLoad(datadir);
    checkTransactionLoad(datadir);
  }

  private void checkConstraintLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadCTest", "VConstraint");
    try {
      ResultSet c = MySQLDB.queryDatabase("SELECT * FROM VConstraint");
      c.last();
      assertTrue(c.getInt("ConstraintId") == 1);
      assertTrue(c.getLong("PartialPlanId") == 1L);
      assertTrue(c.getString("ConstraintName").equals("constraintname"));
      assertTrue(c.getString("ConstraintType").equals("ATEMPORAL"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM VConstraint");
  }
  private void checkEnumerationLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadEDTest", "EnumeratedDomain");
    try {
      ResultSet ed = MySQLDB.queryDatabase("SELECT * FROM EnumeratedDomain");
      ed.last();
      assertTrue(ed.getInt("EnumeratedDomainId") == 1);
      assertTrue(ed.getLong("PartialPlanId") == 1L);
      Blob blob = ed.getBlob("Domain");
      assertTrue((new String(blob.getBytes(1, (int) blob.length()))).equals("enumerateddomain"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM EnumeratedDomain");
  }
  private void checkIntervalLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadIDTest", "IntervalDomain");
    try {
      ResultSet id = MySQLDB.queryDatabase("SELECT * FROM IntervalDomain");
      id.last();
      assertTrue(id.getInt("IntervalDomainId") == 1);
      assertTrue(id.getLong("PartialPlanId") == 1L);
      assertTrue(id.getString("LowerBound").equals("lowerbound"));
      assertTrue(id.getString("UpperBound").equals("upperbound"));
      assertTrue(id.getString("IntervalDomainType").equals("INTEGER_SORT") ||
                 id.getString("IntervalDomainType").equals("REAL_SORT"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM IntervalDomain");
  }
  private void checkObjectLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadOTest", "Object");
    try {
      ResultSet o = MySQLDB.queryDatabase("SELECT * FROM Object");
      o.last();
      assertTrue(o.getInt("ObjectId") == 1);
      assertTrue(o.getLong("PartialPlanId") == 1L);
      assertTrue(o.getString("ObjectName").equals("objectname"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Object");
  }
  private void checkPVTMLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadPVTMTest", "ParamVarTokenMap");
    try {
      ResultSet pvtm = MySQLDB.queryDatabase("SELECT * FROM ParamVarTokenMap");
      pvtm.last();
      assertTrue("VariableId is not as expected:" + pvtm.getInt("VariableId"), pvtm.getInt("VariableId") == 1);
      assertTrue(pvtm.getInt("TokenId") == 1);
      assertTrue(pvtm.getInt("ParameterId") == 1);
      assertTrue(pvtm.getLong("PartialPlanId") == 1L);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM ParamVarTokenMap");
  }
  private void checkParamLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadPATest", "Parameter");
    try {
      ResultSet p = MySQLDB.queryDatabase("SELECT * FROM Parameter");
      p.last();
      assertTrue(p.getInt("ParameterId") == 1);
      assertTrue(p.getInt("PredicateId") == 1);
      assertTrue(p.getLong("PartialPlanId") == 1L);
      assertTrue(p.getString("ParameterName").equals("parametername"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Parameter");
  }
  private void checkPartialPlanLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadPPTest", "PartialPlan");
    try {
      ResultSet p = MySQLDB.queryDatabase("SELECT * FROM PartialPlan");
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
    MySQLDB.updateDatabase("DELETE FROM PartialPlan");
  }
  private void checkPredicateLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadPRTest", "Predicate");
    try {
      ResultSet p = MySQLDB.queryDatabase("SELECT * FROM Predicate");
      p.last();
      assertTrue(p.getInt("PredicateId") == 1);
      assertTrue(p.getString("PredicateName").equals("predicatename"));
      assertTrue(p.getLong("PartialPlanId") == 1L);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Predicate");
  }
  private void checkProjectLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadPJTest", "Project");
    try {
      ResultSet p = MySQLDB.queryDatabase("SELECT * FROM Project");
      p.last();
      assertTrue(p.getInt("ProjectId") == 1);
      assertTrue(p.getString("ProjectName").equals("projectname"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Project"); 
  }
  private void checkSequenceLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadSQTest", "Sequence");
    try {
      ResultSet s = MySQLDB.queryDatabase("SELECT * FROM Sequence");
      s.last();
      assertTrue(s.getString("SequenceURL").equals("sequenceurl"));
      assertTrue(s.getLong("SequenceId") == 1L);
      assertTrue(s.getInt("ProjectId") == 1);
      assertTrue(s.getInt("SequenceOrdering") > 0);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Sequence");
  }
  private void checkSlotLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadSLTest", "Slot");
    try {
      ResultSet s = MySQLDB.queryDatabase("SELECT * FROM Slot");
      s.last();
      assertTrue(s.getInt("SlotId") == 1);
      assertTrue(s.getInt("TimelineId") == 1);
      assertTrue(s.getLong("PartialPlanId") == 1L);
      assertTrue(s.getInt("ObjectId") == 1);
      assertTrue(s.getInt("SlotIndex") == 1);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Slot");
  }
    private void checkTimelineLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadTITest", "Timeline");
    try {
      ResultSet t = MySQLDB.queryDatabase("SELECT * FROM Timeline");
      t.last();
      assertTrue(t.getInt("TimelineId") == 1);
      assertTrue(t.getInt("ObjectId") == 1);
      assertTrue(t.getLong("PartialPlanId") == 1L);
      assertTrue(t.getString("TimelineName").equals("timelinename"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Timeline");
  }
  private void checkTokenLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadTKTest", "Token");
    try {
      ResultSet t = MySQLDB.queryDatabase("SELECT * FROM Token");
      t.last();
      assertTrue(t.getInt("TokenId") == 1);
      assertTrue(t.getInt("SlotId") == 1);
      assertTrue(t.getLong("PartialPlanId") == 1L);
      assertTrue(t.getBoolean("IsFreeToken"));
      assertTrue(t.getBoolean("IsValueToken"));
      assertTrue(t.getInt("StartVarId") == 1);
      assertTrue(t.getInt("EndVarId") == 1);
      assertTrue(t.getInt("DurationVarId") == 1);
      assertTrue(t.getInt("RejectVarId") == 1);
      assertTrue(t.getInt("PredicateId") == 1);
      assertTrue(t.getInt("TimelineId") == 1);
      assertTrue(t.getInt("ObjectId") == 1);
      assertTrue(t.getInt("ObjectVarId") == 1);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Token");
  }
  private void checkTokenRelationLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadTRTest", "TokenRelation");
    try {
      ResultSet t = MySQLDB.queryDatabase("SELECT * FROM TokenRelation");
      t.last();
      assertTrue(t.getLong("PartialPlanId") == 1L);
      assertTrue(t.getInt("TokenAId") == 1);
      assertTrue(t.getInt("TokenBId") == 1);
      assertTrue(t.getString("RelationType").equals("CONSTRAINT"));
      assertTrue(t.getInt("TokenRelationId") == 1);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM TokenRelation");
  }
  private void checkVariableLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadVTest", "Variable");
    try {
      ResultSet v = MySQLDB.queryDatabase("SELECT * FROM Variable");
      v.last();
      assertTrue(v.getInt("VariableId") == 1);
      assertTrue(v.getLong("PartialPlanId") == 1L);
      assertTrue(v.getString("DomainType").equals("EnumeratedDomain"));
      //assertTrue(v.getInt("DomainId") == 1);
      assertTrue(v.getString("VariableType").equals(DbConstants.GLOBAL_VAR));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Variable");
  }
  private void checkTransactionLoad(String datadir) {
    MySQLDB.loadFile(datadir + "loadTATest", "Transaction");
    try {
      ResultSet t = MySQLDB.queryDatabase("SELECT * FROM Transaction");
      t.last();
      assertTrue(t.getString("TransactionType").equals("VARIABLE_DOMAIN_RELAXED"));
      assertTrue(t.getInt("ObjectId") == 1);
      assertTrue(t.getString("Source").equals("SYSTEM"));
      assertTrue(t.getInt("TransactionId") == 1);
      assertTrue(t.getInt("StepNumber") == 1);
      assertTrue(t.getLong("PartialPlanId") == 1L);
      assertTrue(t.getLong("SequenceId") == 1L);
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    MySQLDB.updateDatabase("DELETE FROM Transaction");
  }
}

