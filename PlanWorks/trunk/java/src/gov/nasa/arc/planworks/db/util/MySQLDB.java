package gov.nasa.arc.planworks.db.util;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.arc.planworks.db.impl.PwConstraintImpl;
import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwParameterImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPredicateImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenRelationImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;


public class MySQLDB {
  
  protected static boolean dbIsStarted;
  private static Connection conn;
  private static long queryTime;
  public static final MySQLDB INSTANCE = new MySQLDB();
  private static final Integer NULL = new Integer(0);
  private static final Integer M1 = new Integer(-1);
  private MySQLDB() {
    dbIsStarted = false;
    queryTime = 0;
  }
  public static void startDatabase() throws IllegalArgumentException, IOException, SecurityException
  {
    if(dbIsStarted) {
      return;
    }
    StringBuffer dbStartString = new StringBuffer(System.getProperty("mysql.bindir"));
    dbStartString.append("/mysqld --basedir=").append(System.getProperty("mysql.basedir"));
    dbStartString.append(" --skip-bdb --bind-address=127.0.0.1 --datadir=");
    dbStartString.append(System.getProperty("mysql.datadir")).append(" --log=");
    dbStartString.append(System.getProperty("mysql.log")).append(" --log-error=");
    dbStartString.append(System.getProperty("mysql.log.err")).append(" --skip-symlink ");
    dbStartString.append("--socket=").append(System.getProperty("mysql.sock"));
    dbStartString.append(" --tmpdir=").append(System.getProperty("mysql.tmpdir"));
    System.err.println("Starting db with: " + dbStartString.toString());
    Runtime.getRuntime().exec(dbStartString.toString());
    //    try{Thread.sleep(100000);}catch(Exception e){}
    dbIsStarted = true;
    Runtime.getRuntime().addShutdownHook(new Thread() 
      { 
        public void start() throws IllegalThreadStateException {
          if(MySQLDB.dbIsStarted) {
            System.err.println("Shutting down database.");
            System.err.println("Total time spent in queries: " + queryTime + "ms");
            try{MySQLDB.stopDatabase();}
            catch(Exception e){System.err.println("FAILED TO STOP DATABASE: " + e);}
          }
        }
      });
  }
  public static void stopDatabase() throws IllegalArgumentException, IOException, SecurityException
  {
    if(!dbIsStarted) {
      return;
    }
    StringBuffer dbStopString = new StringBuffer(System.getProperty("mysql.bindir"));
    dbStopString.append("/mysqladmin --user=root --host=127.0.0.1 --socket=");
    dbStopString.append(System.getProperty("mysql.sock")).append(" shutdown");
    Runtime.getRuntime().exec(dbStopString.toString());
  }
  public static void registerDatabase() throws IOException {
    if(!dbIsStarted) {
      startDatabase();
    }
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      for(int triedConnections = 0; triedConnections <= 10; triedConnections++) {
        try {
          conn = DriverManager.getConnection("jdbc:mysql://localhost/PlanWorks?user=root");
        }
        catch(Exception e) {
          if(triedConnections == 10) {
            e.printStackTrace();
            System.exit(-1);
          }
          System.err.println("Connection faled.  Trying again...");
          Thread.sleep(500);
        }
      }
    }
    catch(Exception e) {
      System.err.println(e);
      System.exit(-1);
    }
  }
  public static void unregisterDatabase() {
    if(!dbIsStarted) {
      return;
    }
    try {
      conn.close();
      conn = null;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  public static ResultSet queryDatabase(String query) {
    Statement stmt = null;
    ResultSet result = null;
    try {
      long t1 = System.currentTimeMillis();
      stmt = conn.createStatement();
      result = stmt.executeQuery(query);
      queryTime += System.currentTimeMillis() - t1;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
      return null;
    }
    return result;
  }
  public static int updateDatabase(String update) {
    Statement stmt = null;
    int result = -1;
    try {
      long t1 = System.currentTimeMillis();
      stmt = conn.createStatement();
      result = stmt.executeUpdate(update);
      queryTime += System.currentTimeMillis() - t1;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
      return -1;
    }
    return result;
  }
  public static String queryPartialPlanModelByKey(Long partialPlanKey) {
    try {
      ResultSet model = 
        queryDatabase("SELECT (Model) FROM PartialPlan WHERE PartialPlanId=".concat(partialPlanKey.toString()));
      model.first();
      return model.getString("Model");
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
      return null;
    }
  }
  public static void createTimelineSlotTokenNodesStructure(PwPartialPlanImpl partialPlan) {
    List objectIdList = partialPlan.getObjectIdList();
    ListIterator objectIdIterator = objectIdList.listIterator();
    PwObjectImpl object = null;
    PwTimelineImpl timeline = null;
    PwSlotImpl slot = null;
    PwTokenImpl token = null;

    try {
      ResultSet timelineSlotTokens = 
        queryDatabase("SELECT Timeline.TimelineId, Timeline.TimelineName, Timeline.ObjectId, Slot.SlotId, Token.TokenId, Token.IsValueToken, Token.StartVarId, Token.EndVarId, Token.ObjectId, Token.RejectVarId, Token.DurationVarId, Token.ObjectVarId, Token.PredicateId, Token.TimelineId, ParamVarTokenMap.VariableId, TokenRelation.TokenRelationId FROM Timeline, Slot, Token LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.TokenId=Token.TokenId && ParamVarTokenMap.PartialPlanId=Token.PartialPlanId LEFT JOIN TokenRelation ON TokenRelation.PartialPlanId=Token.PartialPlanId && (TokenRelation.TokenAId=Token.TokenId || TokenRelation.TokenBId=Token.TokenId) WHERE Timeline.PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" && Slot.TimelineId=Timeline.TimelineId && Token.SlotId=Slot.SlotId ORDER BY Timeline.TimelineId, Slot.SlotId, Token.TokenId, ParamVarTokenMap.ParameterId"));

      Integer timelineId = new Integer(-1);
      String timelineName = "";
      Integer objectId = new Integer(-1);
      Integer slotId = new Integer(-1);
      Integer tokenId = new Integer(-1);
      boolean tokenIsValueToken = false;
      Integer tokenStartVarId = new Integer(-1);
      Integer tokenEndVarId = new Integer(-1);
      Integer tokenObjectId = new Integer(-1);
      Integer tokenRejectVarId = new Integer(-1);
      Integer tokenObjectVarId = new Integer(-1);
      Integer tokenPredicateId = new Integer(-1);
      Integer paramVarId = new Integer(-1);
      Integer tokenRelationId = new Integer(-1);
      Integer tokenSlotId = new Integer(-1);
      Integer tokenDurationVarId = new Integer(-1);
      Integer tokenTimelineId = new Integer(-1);
      ArrayList tokenRelations = new ArrayList();
      ArrayList paramVars = new ArrayList();
      ArrayList slots = new ArrayList();
      while(timelineSlotTokens.next()) {
        Integer currTimelineId = new Integer(timelineSlotTokens.getInt("Timeline.TimelineId"));
        String currTimelineName = timelineSlotTokens.getString("Timeline.TimelineName");
        Integer currObjectId = new Integer(timelineSlotTokens.getInt("Timeline.ObjectId"));
        Integer currSlotId = new Integer(timelineSlotTokens.getInt("Slot.SlotId"));
        Integer currTokenId = new Integer(timelineSlotTokens.getInt("Token.TokenId"));
        boolean currTokenIsValueToken = timelineSlotTokens.getBoolean("Token.IsValueToken");
        Integer currTokenStartVarId = new Integer(timelineSlotTokens.getInt("Token.StartVarId"));
        Integer currTokenEndVarId = new Integer(timelineSlotTokens.getInt("Token.EndVarId"));
        Integer currTokenObjectId = new Integer(timelineSlotTokens.getInt("Token.ObjectId"));
        Integer currTokenRejectVarId = new Integer(timelineSlotTokens.getInt("Token.RejectVarId"));
        Integer currTokenObjectVarId = new Integer(timelineSlotTokens.getInt("Token.ObjectVarId"));
        Integer currTokenPredicateId = new Integer(timelineSlotTokens.getInt("Token.PredicateId"));
        Integer currParamVarId = 
          new Integer(timelineSlotTokens.getInt("ParamVarTokenMap.VariableId"));
        Integer currTokenRelationId =
          new Integer(timelineSlotTokens.getInt("TokenRelation.TokenRelationId"));
        //Integer currTokenSlotId = new Integer(timelineSlotTokens.getInt("Token.SlotId"));
        Integer currTokenDurationVarId = 
          new Integer(timelineSlotTokens.getInt("Token.DurationVarId"));
        Integer currTokenTimelineId = new Integer(timelineSlotTokens.getInt("Token.TimelineId"));

        if(!paramVarId.equals(currParamVarId) && !currParamVarId.equals(NULL)) {
          if(!paramVars.contains(currParamVarId)) {
            paramVars.add(currParamVarId);
          }
          paramVarId = currParamVarId;
        }
        if(!tokenRelationId.equals(currTokenRelationId) && !currTokenRelationId.equals(NULL)) {
          if(!tokenRelations.contains(currTokenRelationId)) {
            tokenRelations.add(currTokenRelationId);
          }
          tokenRelationId = currTokenRelationId;
        }
        if(!objectId.equals(currObjectId) && !currObjectId.equals(NULL)) {
          if(!objectId.equals(M1)) {
            token = slot.addToken(tokenId, tokenIsValueToken, tokenSlotId, tokenPredicateId, 
                          tokenStartVarId, tokenEndVarId, tokenDurationVarId, tokenObjectId,
                          tokenRejectVarId, tokenObjectVarId, tokenTimelineId, tokenRelations,
                          paramVars);
            paramVarId = M1;
            tokenRelationId = M1;
            tokenRelations.clear();
            paramVars.clear();
          }
          objectId = currObjectId;
          object = partialPlan.getObjectImpl(objectId);
        }
        if(!timelineId.equals(currTimelineId) && !currTimelineId.equals(NULL)) {
          if(!timelineId.equals(M1)) {
            token = slot.addToken(tokenId, tokenIsValueToken, tokenSlotId, tokenPredicateId, 
                          tokenStartVarId, tokenEndVarId, tokenDurationVarId, tokenObjectId,
                          tokenRejectVarId, tokenObjectVarId, tokenTimelineId, tokenRelations,
                          paramVars);
            paramVarId = M1;
            tokenRelationId = M1;
            tokenRelations.clear();
            paramVars.clear();
          }
          timelineId = currTimelineId;
          timeline = object.addTimeline(currTimelineName, timelineId);
        }
        if(!slotId.equals(currSlotId) && !currSlotId.equals(NULL)) {
          if(!slotId.equals(M1)) {
            token = slot.addToken(tokenId, tokenIsValueToken, tokenSlotId, tokenPredicateId, 
                          tokenStartVarId, tokenEndVarId, tokenDurationVarId, tokenObjectId,
                          tokenRejectVarId, tokenObjectVarId, tokenTimelineId, tokenRelations,
                          paramVars);
            paramVarId = M1;
            tokenRelationId = M1;
            tokenRelations.clear();
            paramVars.clear();
          }
          slotId = currSlotId;
          slot = timeline.addSlot(slotId);
          slots.add(slot);
        }
        if(!tokenId.equals(currTokenId) && !currTokenId.equals(NULL)) {
          if(!tokenId.equals(M1) && !partialPlan.tokenExists(tokenId)) {
            token = slot.addToken(tokenId, tokenIsValueToken, tokenSlotId, tokenPredicateId, 
                          tokenStartVarId, tokenEndVarId, tokenDurationVarId, tokenObjectId,
                          tokenRejectVarId, tokenObjectVarId, tokenTimelineId, tokenRelations,
                          paramVars);
            paramVarId = M1;
            tokenRelationId = M1;
            tokenRelations.clear();
            paramVars.clear();
          }
          tokenId = currTokenId;
          tokenIsValueToken = currTokenIsValueToken;
          tokenSlotId = currSlotId;
          tokenPredicateId = currTokenPredicateId;
          tokenStartVarId = currTokenStartVarId;
          tokenEndVarId = currTokenEndVarId;
          tokenDurationVarId = currTokenEndVarId;
          tokenObjectId = currTokenObjectId;
          tokenRejectVarId = currTokenRejectVarId;
          tokenObjectVarId = currTokenObjectVarId;
          tokenTimelineId = currTokenTimelineId;
          if(!currTokenRelationId.equals(NULL) && !tokenRelations.contains(currTokenRelationId)) {
            tokenRelations.add(currTokenRelationId);
          }
          if(!currParamVarId.equals(NULL) && !paramVars.contains(currParamVarId)) {
            paramVars.add(currParamVarId);
          }
        }
      }
      slot.addToken(tokenId, tokenIsValueToken, tokenSlotId, tokenPredicateId, 
                    tokenStartVarId, tokenEndVarId, tokenDurationVarId, tokenObjectId,
                    tokenRejectVarId, tokenObjectVarId, tokenTimelineId, tokenRelations,
                    paramVars);
      paramVars.clear();
      tokenRelations.clear();
      ResultSet freeTokens = queryDatabase("Select Token.TokenId, Token.IsValueToken, Token.ObjectVarId, Token.StartVarId, Token.EndVarId, Token.DurationVarId, Token.RejectVarId, Token.PredicateId, ParamVarTokenMap.VariableId, ParamVarTokenMap.ParameterId, TokenRelation.TokenRelationId FROM Token LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.TokenId=Token.TokenId && ParamVarTokenMap.PartialPlanId=Token.PartialPlanId LEFT JOIN TokenRelation ON TokenRelation.PartialPlanId=Token.PartialPlanId && (TokenRelation.TokenAId=Token.TokenId || TokenRelation.TokenBId=Token.TokenId) WHERE Token.IsFreeToken=1 && Token.PartialPlanId=".concat(partialPlan.getKey().toString()));
      while(freeTokens.next()) {
        Integer currTokenId = new Integer(freeTokens.getInt("Token.TokenId"));
        boolean currTokenIsValueToken = freeTokens.getBoolean("Token.IsValueToken");
        Integer currTokenStartVarId = new Integer(freeTokens.getInt("Token.StartVarId"));
        Integer currTokenEndVarId = new Integer(freeTokens.getInt("Token.EndVarId"));
        Integer currTokenRejectVarId = new Integer(freeTokens.getInt("Token.RejectVarId"));
        Integer currTokenObjectVarId = new Integer(freeTokens.getInt("Token.ObjectVarId"));
        Integer currTokenPredicateId = new Integer(freeTokens.getInt("Token.PredicateId"));
        Integer currParamVarId = 
          new Integer(freeTokens.getInt("ParamVarTokenMap.VariableId"));
        Integer currTokenRelationId =
          new Integer(freeTokens.getInt("TokenRelation.TokenRelationId"));
        Integer currTokenDurationVarId = 
          new Integer(freeTokens.getInt("Token.DurationVarId"));
        if(!paramVarId.equals(currParamVarId) && !currParamVarId.equals(NULL)) {
          if(!paramVars.contains(currParamVarId)) {
            paramVars.add(currParamVarId);
          }
          paramVarId = currParamVarId;
        }
        if(!tokenRelationId.equals(currTokenRelationId) && !currTokenRelationId.equals(NULL)) {
          if(!tokenRelations.contains(currTokenRelationId)) {
            tokenRelations.add(currTokenRelationId);
          }
          tokenRelationId = currTokenRelationId;
        }
        if(!tokenId.equals(currTokenId) && !currTokenId.equals(NULL)) {
          if(!tokenId.equals(M1) && !partialPlan.tokenExists(tokenId)) {
            token = new PwTokenImpl(tokenId, tokenIsValueToken, (Integer) null,
                                    tokenPredicateId, tokenStartVarId, tokenEndVarId,
                                    tokenDurationVarId, (Integer)null,tokenRejectVarId,
                                    tokenObjectVarId, (Integer)null, tokenRelations, 
                                    paramVars, partialPlan);
            partialPlan.addToken(tokenId, token);
            paramVarId = M1;
            tokenRelationId = M1;
            tokenRelations.clear();
            paramVars.clear();
          }
          tokenId = currTokenId;
          tokenIsValueToken = currTokenIsValueToken;
          tokenPredicateId = currTokenPredicateId;
          tokenStartVarId = currTokenStartVarId;
          tokenEndVarId = currTokenEndVarId;
          tokenDurationVarId = currTokenEndVarId;
          tokenRejectVarId = currTokenRejectVarId;
          tokenObjectVarId = currTokenObjectVarId;
          if(!currTokenRelationId.equals(NULL) && !tokenRelations.contains(currTokenRelationId)) {
            tokenRelations.add(currTokenRelationId);
          }
          if(!currParamVarId.equals(NULL) && !paramVars.contains(currParamVarId)) {
            paramVars.add(currParamVarId);
          }
        }
      }
      token = new PwTokenImpl(tokenId, tokenIsValueToken, (Integer) null,
                              tokenPredicateId, tokenStartVarId, tokenEndVarId,
                              tokenDurationVarId, (Integer)null, tokenRejectVarId,
                              tokenObjectVarId, (Integer)null, tokenRelations,
                              paramVars, partialPlan);
      partialPlan.addToken(tokenId, token);
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  public static void queryConstraints(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet constraints = 
        queryDatabase("SELECT VConstraint.ConstraintId, VConstraint.ConstraintName, VConstraint.ConstraintType, ConstraintVarMap.VariableId FROM VConstraint LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=VConstraint.PartialPlanId && ConstraintVarMap.ConstraintId=VConstraint.ConstraintId WHERE VConstraint.PartialPlanId=".concat(partialPlan.getKey().toString()));
      Integer constraintId = new Integer(-1);
      Integer variableId = new Integer(-1);
      String constraintName = new String("");
      String constraintType = new String("");
      ArrayList constrainedVarIds = new ArrayList();
      while(constraints.next()) {
        Integer currConstraintId = new Integer(constraints.getInt("VConstraint.ConstraintId"));
        String currConstraintName = constraints.getString("VConstraint.ConstraintName");
        String currConstraintType = constraints.getString("VConstraint.ConstraintType");
        Integer currVariableId = new Integer(constraints.getInt("ConstraintVarMap.VariableId"));
        if(!variableId.equals(currVariableId) && !currVariableId.equals(NULL)) {
          if(!constrainedVarIds.contains(currVariableId)) {
            constrainedVarIds.add(currVariableId);
          }
          variableId = currVariableId;
        }
        if(!constraintId.equals(currConstraintId) && !currConstraintId.equals(NULL)) {
          if(!constraintId.equals(M1)) {
            partialPlan.addConstraint(constraintId,
                                      new PwConstraintImpl(constraintName, constraintId,
                                                           constraintType, constrainedVarIds,
                                                           partialPlan));
          }
          constraintId = currConstraintId;
          constraintName = currConstraintName;
          constraintType = currConstraintType;
          constrainedVarIds.clear();
        }
      }
      partialPlan.addConstraint(constraintId, new PwConstraintImpl(constraintName, constraintId,
                                                                   constraintType, 
                                                                   constrainedVarIds,partialPlan));
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  public static void queryPredicates(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet predicates = 
        queryDatabase("SELECT PredicateId, PredicateName FROM Predicate WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
      while(predicates.next()) {
        Integer predicateId = new Integer(predicates.getInt("PredicateId"));
        PwPredicateImpl predicate =  new PwPredicateImpl(predicateId,
                                                         predicates.getString("PredicateName"),
                                                         partialPlan);
        partialPlan.addPredicate(predicateId, predicate);
        ResultSet parameters =
          queryDatabase("SELECT ParameterId, ParameterName FROM Parameter WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" && PredicateId=").concat(predicateId.toString()));
        while(parameters.next()) {
          Integer parameterId = new Integer(parameters.getInt("ParameterId"));
          partialPlan.addParameter(parameterId,
                                   predicate.addParameter(parameterId,
                                                          parameters.getString("ParameterName")));
        }
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  public static void queryVariables(PwPartialPlanImpl partialPlan) {
    PwDomainImpl domainImpl = null;
    try {
      System.err.println("Executing variable query...");
      ResultSet variables =
        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.DomainId, IntervalDomain.IntervalDomainType, IntervalDomain.LowerBound, IntervalDomain.UpperBound, EnumeratedDomain.Domain, ConstraintVarMap.ConstraintId, ParamVarTokenMap.ParameterId, ParamVarTokenMap.TokenId FROM Variable LEFT JOIN IntervalDomain ON IntervalDomain.PartialPlanId=Variable.PartialPlanId && IntervalDomain.IntervalDomainId=Variable.DomainId LEFT JOIN EnumeratedDomain ON EnumeratedDomain.PartialPlanId=Variable.PartialPlanId && EnumeratedDomain.EnumeratedDomainId=Variable.DomainId LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId && ConstraintVarMap.VariableId=Variable.VariableId LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.PartialPlanId=Variable.PartialPlanId && ParamVarTokenMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" ORDER BY Variable.VariableId"));

      Integer variableId = new Integer(-1);
      String domainType = "";
      String variableType = "";
      String intDomainType = "";
      String intDomainLB = "";
      String intDomainUB = "";
      String enumDomain = "";
      Integer constraintId = new Integer(-1);
      Integer parameterId = new Integer(-1);
      Integer tokenId = new Integer(-1);
      ArrayList constraintIdList = new ArrayList();
      ArrayList parameterIdList = new ArrayList();
      ArrayList tokenIdList = new ArrayList();
      while(variables.next()) {
        Integer currVariableId = new Integer(variables.getInt("Variable.VariableId"));
        String currVariableType = variables.getString("Variable.VariableType");
        String currDomainType = variables.getString("Variable.DomainType");
        String currIntDomainType = "";
        String currIntDomainLB = "";
        String currIntDomainUB = "";
        String currEnumDomain = "";
        Integer currConstraintId = new Integer(variables.getInt("ConstraintVarMap.ConstraintId"));
        Integer currParameterId = new Integer(variables.getInt("ParamVarTokenMap.ParameterId"));
        Integer currTokenId = new Integer(variables.getInt("ParamVarTokenMap.TokenId"));
        if(!constraintId.equals(currConstraintId) && !currConstraintId.equals(NULL)) {
          if(!constraintId.equals(M1) && !constraintIdList.contains(currConstraintId)) {
            constraintIdList.add(currConstraintId);
          }
          constraintId = currConstraintId;
        }
        if(!parameterId.equals(currParameterId) && !currParameterId.equals(NULL)) {
          if(!parameterId.equals(M1) && !parameterIdList.contains(currParameterId)) {
            parameterIdList.add(currParameterId);
          }
          parameterId = currParameterId;
        }
        if(!tokenId.equals(currTokenId) && !currTokenId.equals(NULL)) {
          if(!tokenId.equals(M1) && !tokenIdList.contains(currTokenId)) {
            tokenIdList.add(currTokenId);
          }
          tokenId = currTokenId;
        }
        if(domainType.equals("")) {
          domainType = currDomainType;
        }
        if(currDomainType.equals("IntervalDomain")) {
          currIntDomainType = variables.getString("IntervalDomain.IntervalDomainType");
          currIntDomainLB = variables.getString("IntervalDomain.LowerBound");
          currIntDomainUB = variables.getString("IntervalDomain.UpperBound");
          if((intDomainType == null || intDomainType.equals("")) && 
             (intDomainLB == null || intDomainLB.equals("")) && 
             (intDomainUB == null || intDomainUB.equals(""))) {
            intDomainType = currIntDomainType;
            intDomainLB = currIntDomainLB;
            intDomainUB = currIntDomainUB;
          }
        }
        else if(currDomainType.equals("EnumeratedDomain")) {
          Blob blob = variables.getBlob("EnumeratedDomain.Domain");
          if(blob == null) {
            throw new SQLException("Undefined EnumeratedDomain");
          }
          currEnumDomain = new String(blob.getBytes(1, (int) blob.length()));
          if(enumDomain.equals("")) {
            enumDomain = currEnumDomain;
          }
        }
        if(!variableId.equals(currVariableId) && !currVariableId.equals(NULL)) {
          if(!variableId.equals(M1)) {
            partialPlan.addVariable(variableId, new PwVariableImpl(variableId, variableType,
                                                                   constraintIdList,
                                                                   parameterIdList, tokenIdList,
                                                                   domainImpl, partialPlan));
          }
          variableId = currVariableId;
          variableType = currVariableType;
          domainType = currDomainType;
          enumDomain = currEnumDomain;
          intDomainType = currIntDomainType;
          intDomainLB = currIntDomainLB;
          intDomainUB = currIntDomainUB;
          constraintIdList.clear();
          parameterIdList.clear();
          tokenIdList.clear();
        }
        if(domainType.equals("IntervalDomain")) {
          domainImpl = new PwIntervalDomainImpl(intDomainType, intDomainLB, intDomainUB);
        }
        else if(domainType.equals("EnumeratedDomain")) {
          Blob blob = variables.getBlob("EnumeratedDomain.Domain");
          enumDomain = new String(blob.getBytes(1, (int) blob.length()));
          domainImpl = new PwEnumeratedDomainImpl(enumDomain);
        }
      }
      partialPlan.addVariable(variableId, new PwVariableImpl(variableId, variableType,
                                                             constraintIdList,
                                                             parameterIdList, tokenIdList,
                                                             domainImpl, partialPlan));
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  public static void queryTokenRelations(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet tokenRelations = 
        queryDatabase("SELECT TokenRelationId, TokenAId, TokenBId, RelationType FROM TokenRelation WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
      while(tokenRelations.next()) {
        Integer key = new Integer(tokenRelations.getInt("TokenRelationId"));
        partialPlan.addTokenRelation(key, new PwTokenRelationImpl(key,
                                                                  new Integer(tokenRelations.getInt("TokenAId")),
                                                                  new Integer(tokenRelations.getInt("TokenBId")),
                                                                  tokenRelations.getString("RelationType"), partialPlan));
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
}
