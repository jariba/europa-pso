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
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPredicateImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenRelationImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;


public class MySQLDB {
  
  protected static boolean dbIsStarted;
  private static Connection conn;

  public static final MySQLDB INSTANCE = new MySQLDB();

  private MySQLDB() {
    dbIsStarted = false;
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
            System.err.println("Shuttong down database.");
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
    dbStopString.append("/mysqladmin --user=root --password=root --host=127.0.0.1 --socket=");
    dbStopString.append(System.getProperty("mysql.sock")).append(" shutdown");
    Runtime.getRuntime().exec(dbStopString.toString());
  }
  public static void registerDatabase() throws IOException {
    if(!dbIsStarted) {
      startDatabase();
    }
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection("jdbc:mysql://localhost/PlanWorks?user=PlanWorksUser&password=PlanWorksUser");
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
    }
  }
  public static ResultSet queryDatabase(String query) {
    Statement stmt = null;
    ResultSet result = null;
    try {
      stmt = conn.createStatement();
      result = stmt.executeQuery(query);
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
    return result;
  }
  public static int updateDatabase(String update) {
    Statement stmt = null;
    int result = -1;
    try {
      stmt = conn.createStatement();
      result = stmt.executeUpdate(update);
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
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
      return null;
    }
  }
  public static List queryPartialPlanObjectsByKey(Long partialPlanKey) {
    ArrayList retval = null;
    try {
      ResultSet objects = 
        queryDatabase("SELECT (ObjectName, ObjectId) FROM Object WHERE PartialPlanId=".concat(partialPlanKey.toString()));
      retval = new ArrayList(objects.getFetchSize() * 2);
      while(objects.next()) {
        retval.add(objects.getString("ObjectName"));
        retval.add(new Integer(objects.getInt("ObjectId")));
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
    return retval;
  }
  public static void createTimelineSlotTokenNodesStructure(PwPartialPlanImpl partialPlan) {
    List objectIdList = partialPlan.getObjectIdList();
    ListIterator objectIdIterator = objectIdList.listIterator();
    PwObjectImpl object = null;
    PwTimelineImpl timeline = null;
    PwSlotImpl slot = null;
    try {
      while(objectIdIterator.hasNext()) {
        Integer objectId = (Integer) objectIdIterator.next();
        object = partialPlan.getObjectImpl(objectId);
        ResultSet timelines =
          queryDatabase("SELECT (TimelineId, TimelineName) FROM Timeline WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND ObjectId=").concat(objectId.toString()));
        while(timelines.next()) {
          Integer timelineId = new Integer(timelines.getInt("TimelineId"));
          timeline = object.addTimeline(timelines.getString("TimelineName"), timelineId);
          ResultSet slots =
            queryDatabase("SELECT (SlotId) FROM Slot WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND TimelineId=").concat(timelineId.toString()));
          while(slots.next()) {
            Integer slotId = new Integer(slots.getInt("SlotId"));
            slot = timeline.addSlot(slotId);
            ResultSet tokens =
              queryDatabase("SELECT (TokenId, IsValueToken, StartVarId, EndVarId, DurationVarId, RejectVarId, PredicateId, MasterTokenId) FROM Token WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND SlotId=").concat(slotId.toString()));
            while(tokens.next()) {
              Integer tokenId = new Integer(tokens.getInt("TokenId"));
              ResultSet paramVarIds =
                queryDatabase("SELECT (VariableId) FROM ParamVarTokenMap WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND TokenId=").concat(tokenId.toString()));
              ArrayList variableIdList = new ArrayList();
              while(paramVarIds.next()) {
                variableIdList.add(new Integer(paramVarIds.getInt("VariableId")));
              }
              ResultSet tokenRelationIds =
                queryDatabase("SELECT (TokenRelationId) FROM TokenRelation WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND (TokenAId=").concat(tokenId.toString()).concat(" OR TokenBId=").concat(tokenId.toString()).concat(")"));
              ArrayList tokenRelationIdList = new ArrayList();
              while(tokenRelationIds.next()) {
                tokenRelationIdList.add(new Integer(tokenRelationIds.getInt("TokenRelationId")));
              }
              slot.addToken(tokenId, tokens.getBoolean("IsValueToken"),
                            new Integer(tokens.getInt("SlotId")),
                            new Integer(tokens.getInt("PredicateId")),
                            new Integer(tokens.getInt("StartVarId")), 
                            new Integer(tokens.getInt("EndVarId")),
                            new Integer(tokens.getInt("DurationVarId")),
                            new Integer(tokens.getInt("ObjectVarId")),
                            new Integer(tokens.getInt("RejectVarId")),
                            tokenRelationIdList, variableIdList);
            }
          }
        }
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
    }
  }
  public static void queryConstraints(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet constraints = 
        queryDatabase("SELECT (ConstraintId, ConstraintName, ConstraintType) FROM VConstraint WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
      while(constraints.next()) {
        Integer constraintId = new Integer(constraints.getInt("ConstraintId"));
        ResultSet variableIds =
          queryDatabase("SELECT (VariableId) FROM ConstraintVarMap WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND ConstraintId=").concat(constraintId.toString()));
        ArrayList constrainedVarIds = new ArrayList();
        while(variableIds.next()) {
          constrainedVarIds.add(new Integer(variableIds.getInt("VariableId")));
        }
        partialPlan.addConstraint(constraintId, 
                                  new PwConstraintImpl(constraints.getString("ConstraintName"), 
                                                       constraintId,
                                                       constraints.getString("ConstraintType"), 
                                                       constrainedVarIds, partialPlan));
       }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
    }
  }
  public static void queryPredicates(PwPartialPlanImpl partialPlan) {
    //PwPredicateImpl predicate = null;
    try {
      ResultSet predicates = 
        queryDatabase("SELECT (PredicateId, PredicateName) FROM Predicate WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
      while(predicates.next()) {
        Integer predicateId = new Integer(predicates.getInt("PredicateId"));
        PwPredicateImpl predicate =  new PwPredicateImpl(predicateId,
                                                         predicates.getString("PredicateName"),
                                                         partialPlan);
        partialPlan.addPredicate(predicateId, predicate);
        ResultSet parameters =
          queryDatabase("SELECT (ParameterId, ParameterName) FROM Parameter WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
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
    }
  }
  public static void queryVariables(PwPartialPlanImpl partialPlan) {
    PwDomainImpl domainImpl = null;
    try {
      ResultSet variables =
        queryDatabase("SELECT (VariableId, DomainType, VariableType, DomainId, ParameterId) FROM Variable WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
      while(variables.next()) {
        Integer variableId = new Integer(variables.getInt("VariableId"));
        Integer domainId = new Integer(variables.getInt("DomainId"));
        String domainType = variables.getString("DomainType");
        ArrayList constraintIdList = new ArrayList();
        ResultSet constraintIds =
          queryDatabase("SELECT (ConstraintId) FROM ConstraintVarMap WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND VariableId=").concat(variableId.toString()));
        while(constraintIds.next()) {
          constraintIdList.add(new Integer(constraintIds.getInt("ConstraintId")));
        }
        if(domainType.equals("EnumeratedDomain")) {
          ResultSet domain = 
            queryDatabase("SELECT (Domain) FROM EnumeratedDomain WHERE EnumeratedDomainId=".concat(domainId.toString()));
          domain.first();
          domainImpl = 
            new PwEnumeratedDomainImpl(new String(domain.getBlob("Domain").getBytes(0, (int)domain.getBlob("Domain").length())));
        }
        else if(domainType.equals("IntervalDomain")) {
          ResultSet domain =
            queryDatabase("SELECT (IntervalDomainType, LowerBound, UpperBound) FROM IntervalDomain WHERE IntervalDomainId=".concat(domainId.toString()));
          domain.first();
          domainImpl =
            new PwIntervalDomainImpl(domain.getString("IntervalDomainType"),
                                 domain.getString("LowerBound"), domain.getString("UpperBound"));
        }
        partialPlan.addVariable(variableId, new PwVariableImpl(variableId, 
                                                               variables.getString("VariableType"),
                                                               constraintIdList,
                                                               new Integer(variables.getInt("ParameterId")),
                                                               domainImpl, partialPlan));
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
    }
  }
  public static void queryTokenRelations(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet tokenRelations = 
        queryDatabase("SELECT (TokenRelationId, TokenAId, TokenBId, RelationType) FROM TokenRelation WHERE PartialPlanId=".concat(partialPlan.getKey().toString()));
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
    }
  }
  public static PwPredicateImpl queryPredicateByKey(Integer key, PwPartialPlanImpl partialPlan) {
    PwPredicateImpl predicateImpl = null;
    try {
      ResultSet predicate = 
        queryDatabase("SELECT (PredicateName) FROM Predicate WHERE PredicateId=".concat(key.toString()));
      predicate.first();
      predicateImpl = new PwPredicateImpl(key, predicate.getString("PredicateName"),
                                          partialPlan);
      ResultSet parameters =
        queryDatabase("SELECT (ParameterName, ParameterId) FROM Parameter WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND PredicateId=").concat(key.toString()));
      while(parameters.next()) {
        partialPlan.addParameter(new Integer(parameters.getInt("ParameterId")),
                                 predicateImpl.addParameter(new Integer(parameters.getInt("ParameterId")),
                                                            parameters.getString("ParameterName")));
      }
      return predicateImpl;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
  }
  public static PwConstraintImpl queryConstraintByKey(Integer key, PwPartialPlanImpl partialPlan) {
    PwConstraintImpl constraintImpl = null;
    try {
      ResultSet constraint =
        queryDatabase("SELECT (ConstraintName, ConstraintType) FROM VConstraint WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND ConstraintId=").concat(key.toString()));
      constraint.first();
      ResultSet constrainedVariables = 
        queryDatabase("SELECT (VariableId) from ConstraintVarMap WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND ConstraintId=").concat(constraint.getString("ConstraintId")));
      ArrayList constrainedVarIds = new ArrayList();
      while(constrainedVariables.next()) {
        constrainedVarIds.add(new Integer(constrainedVariables.getInt("VariableId")));
      }
      constraintImpl = new PwConstraintImpl(constraint.getString("ConstraintName"), key,
                                            constraint.getString("ConstraintType"), 
                                            constrainedVarIds, partialPlan);
      return constraintImpl;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
  }
  public static PwVariableImpl queryVariableByKey(Integer key, PwPartialPlanImpl partialPlan) {
    PwVariableImpl variableImpl = null;
    PwDomainImpl domainImpl = null;
    try {
      ResultSet variable = 
        queryDatabase("SELECT (DomainType, VariableType, DomainId, ParameterId) FROM Variable WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND VariableId=").concat(key.toString()));
      variable.first();
      if(variable.getString("DomainType").equals("IntervalDomain")) {
        ResultSet domain =
          queryDatabase("SELECT (IntervalDomainType, LowerBound, UpperBound) FROM IntervalDomain WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND VariableId=").concat(key.toString()).concat(" AND IntervalDomainId=").concat(variable.getString("DomainId")));
        domain.first();
        domainImpl = new PwIntervalDomainImpl(domain.getString("IntervalDomainType"),
                                              domain.getString("LowerBound"),
                                              domain.getString("UpperBound"));
      }
      else if(variable.getString("DomainType").equals("EnumeratedDomain")) {
        ResultSet domain =
          queryDatabase("SELECT (Domain) FROM EnumeratedDomain WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND VariableId=").concat(key.toString()).concat(" AND EnumeratedDomainId=").concat(variable.getString("DomainId")));
        domain.first();
        domainImpl = new PwEnumeratedDomainImpl(new String(domain.getBlob("Domain").getBytes(0, (int)domain.getBlob("Domain").length())));
      }
      ResultSet constraintIds =
        queryDatabase("SELECT (ConstraintId) FROM ConstraintVarMap WHERE PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" AND VariableId=").concat(key.toString()));
      ArrayList constraintIdList = new ArrayList();
      while(constraintIds.next()) {
        constraintIdList.add(new Integer(constraintIds.getInt("ConstraintId")));
      }
      variableImpl = new PwVariableImpl(key, variable.getString("VariableType"), constraintIdList,
                                    new Integer(variable.getInt("ParameterId")), domainImpl, 
                                    partialPlan);
      return variableImpl;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
  }
}
