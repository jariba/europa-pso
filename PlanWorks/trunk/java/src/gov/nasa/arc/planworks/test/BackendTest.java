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
  private static PwPartialPlanImpl plan1, plan2;
  private static PwPlanningSequenceImpl sequence;
  private static final String step1 = "step2";
  private static final String step2 = "step9";
  private static final String sequenceName = "Camera1065545818740";
  private static final int numTests = 2;
  private static int testsRun = 0;
  static {
    try {
      MySQLDB.startDatabase();
      MySQLDB.registerDatabase();
      MySQLDB.loadFile(System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat("test.sequence"), "Sequence");
      String p1 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step1).concat(System.getProperty("file.separator")).concat(step1).concat(".");
      String p2 = System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName).concat(System.getProperty("file.separator")).concat(step2).concat(System.getProperty("file.separator")).concat(step2).concat(".");
      for(int i = 0; i < DbConstants.NUMBER_OF_PP_FILES; i++) {
        MySQLDB.loadFile(p1.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
        MySQLDB.loadFile(p2.toString().concat(DbConstants.PARTIAL_PLAN_FILE_EXTS[i]),
                         DbConstants.PW_DB_TABLES[i]);
      }
      sequence = new PwPlanningSequenceImpl(System.getProperty("planworks.test.data.dir").concat(System.getProperty("file.separator")).concat(sequenceName), MySQLDB.latestSequenceId(), new PwModelImpl());
      plan1 = (PwPartialPlanImpl) sequence.getPartialPlan(step1);
      plan2 = (PwPartialPlanImpl) sequence.getPartialPlan(step2);
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
  }
  protected void tearDown() {
    if(testsRun != numTests) {
      return;
    }
    for(int i = 0; i < DbConstants.PW_DB_TABLES.length; i++) {
      MySQLDB.updateDatabase("DELETE FROM ".concat(DbConstants.PW_DB_TABLES[i]));
    }
  }
  public BackendTest(String testType) {
    super(testType);
  }
  public static TestSuite suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new BackendTest("testPlanLoad"));
    suite.addTest(new BackendTest("testDataConsistency"));
    return suite;
  }
  public void testPlanLoad() {
    assertTrue("Plan 1 is null", plan1 != null);
    assertTrue("Plan 2 is null", plan2 != null);
    assertTrue("Plan 1 is inconsistant", plan1.checkPlan());
    assertTrue("Plan 2 is inconsistant", plan2.checkPlan());
    testsRun++;
  }
  public void testDataConsistency() {
    PwPartialPlanImpl [] temp = new PwPartialPlanImpl [] {plan1, plan2};
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
    testsRun++;
  }
}
