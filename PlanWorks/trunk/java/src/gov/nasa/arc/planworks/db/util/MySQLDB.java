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
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;


public class MySQLDB {
  
  private static boolean dbIsStarted;
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
    dbStartString.append("--tmpdir=").append(System.getProperty("mysql.tmpdir"));
    Runtime.getRuntime().exec(dbStartString.toString());
    dbIsStarted = true;
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
  public static void registerDatabase() {
    if(!dbIsStarted) {
      startDataBase();
    }
    try {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      conn = DriverManager.getConnection("jdbc:mysql://localhost/mysql?user=root&password=root");
    }
    catch(Exception e) {
      System.err.println(e);
      System.exit(-1);
    }
  }
  public static void unregisterDatabase() {
    if(!dbIdStarted) {
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
  public static String queryPartialPlanModelByKey(long partialPlanKey) {
    try {
      ResultSet model = 
        queryDatabase("SELECT (Model) FROM PartialPlan WHERE PartialPlanId=".concat(Long.toString(partialPlanKey)));
      model.first();
      return model.getString("Model");
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
  }
  public static List queryPartialPlanObjectsByKey(long partialPlanKey) {
    ArrayList retval = null;
    try {
      ResultSet objects = 
        queryDatabase("SELECT (ObjectName, ObjectId) FROM Object WHERE PartialPlanId=".concat(Long.toString(partialPlanKey)));
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
        int objectId = ((Integer) objectIdIterator.next()).intValue();
        object = partialPlan.getObjectImpl(objectId);
        ResultSet timelines =
          queryDatabase("SELECT (TimelineId, TimelineName) FROM Timeline WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND ObjectId=").concat(Integer.toString(objectId)));
        while(timelines.next()) {
          int timelineId = timelines.getInt("TimelineId");
          timeline = object.addTimeline(timelines.getString("TimelineName"), timelineId);
          ResultSet slots =
            queryDatabase("SELECT (SlotId) FROM Slot WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND TimelineId=").concat(Integer.toString(timelineId)));
          while(slots.next()) {
            int slotId = slots.getInt("SlotId");
            slot = timeline.addSlot(slotId);
            ResultSet tokens =
              queryDatabase("SELECT (TokenId, IsValueToken, StartVarId, EndVarId, DurationVarId, RejectVarId, PredicateId, MasterTokenId) FROM Token WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND SlotId=").concat(Integer.toString(slotId)));
            while(tokens.next()) {
              int tokenId = tokens.getInt("TokenId");
              ResultSet paramVarIds =
                queryDatabase("SELECT (VariableId) FROM ParamVarTokenMap WHERE PartialPlanId=".concat(partialPlan.getKey()).concat(" AND TokenId=").concat(Integer.toString(tokenId)));
              ArrayList variableIdList = new ArrayList();
              while(paramVarIds.next()) {
                variableIdList.add(new Integer(paramVarIds.getInt("VariableId")));
              }
              ResultSet slaveIds =
                queryDatabase("SELECT (TokenId) FROM Token WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND MasterTokenId=").concat(Integer.toString(tokenId)));
              ArrayList slaveIdList = new ArrayList();
              while(slaveIds.next()) {
                slaveIdList.add(new Integer(slaveIds.getInt("TokenId")));
              }
              slot.addToken(tokenId, tokens.getBoolean("IsValueToken"),
                            tokens.getInt("StartVarId"), tokens.getInt("EndVarId"),
                            tokens.getInt("DurationVarId"), tokens.getInt("RejectVarId"),
                            tokens.getInt("PredicateId"), tokens.getInt("MasterTokenId"),
                            variableIdList, slaveIdList);
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
        queryDatabase("SELECT (ConstraintId, ConstraintName, ConstraintType) FROM VConstraint WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())));
      while(constraints.next()) {
        int constraintId = constraints.getInt("ConstraintId");
        ResultSet variableIds =
          queryDatabase("SELECT (VariableId) FROM ConstraintVarMap WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND ConstraintId=").concat(Integer.toString(constraintId)));
        ArrayList constrainedVarIds = new ArrayList();
        while(variableIds.next()) {
          constrainedVarIds.add(new Integer(variableIds.getInt("VariableId")));
        }
        partialPlan.addConstraint(new PwConstraintImpl(constraints.getString("ConstraintName"), 
                                                       constraintId,
                                                       constraints.getString("ConstraintType"), 
                                                       constrainedVarIds));
       }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
    }
  }
  public static void queryPredicates(PwPartialPlanImpl partialPlan) {
    PwPredicateImpl predicate = null;
    try {
      ResultSet predicates = 
        queryDatabase("SELECT (PredicateId, PredicateName) FROM Predicate WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())));
      while(predicates.next()) {
        predicate = partialPlan.addPredicate(predicates.getInt("PredicateId"),
                                             predicates.getString("PredicateName"));
        ResultSet parameters =
          queryDatabase("SELECT (ParameterId, ParameterName) FROM Parameter WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())));
        while(parameters.next()) {
          partialPlan.addParameter(parameters.getInt("ParameterId"),
                                   predicate.addParameter(parameters.getString("ParameterName"),
                                                          parameters.getInt("ParameterId")));
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
        queryDatabase("SELECT (VariableId, DomainType, VariableType, DomainId, ParameterId) FROM Variable WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())));
      while(variables.next()) {
        int variableId = variables.getInt("VariableId");
        int domainId = variables.getInt("DomainId");
        String domainType = variables.getString("DomainType");
        ArrayList constraintIdList = new ArrayList();
        ResultSet constraintIds =
          queryDatabase("SELECT (ConstraintId) FROM ConstraintVarMap WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND VariableId=").concat(Integer.toString(variableId)));
        while(constraintIds.next()) {
          constraintIdList.add(new Integer(constraintIds.getInt("ConstraintId")));
        }
        if(domainType.equals("EnumeratedDomain")) {
          ResultSet domain = 
            queryDatabase("SELECT (Domain) FROM EnumeratedDomain WHERE EnumeratedDomainId=".concat(Integer.toString(domainId)));
          domain.first();
          domainImpl = 
            new PwEnumeratedDomainImpl(new String(domain.getBlob("Domain").getBytes(0, (int)domain.getBlob("Domain").length())));
        }
        else if(domainType.equals("IntervalDomain")) {
          ResultSet domain =
            queryDatabase("SELECT (IntervalDomainType, LowerBound, UpperBound) FROM IntervalDomain WHERE IntervalDomainId=".concat(Integer.toString(domainId)));
          domain.first();
          domainImpl =
            new PwIntervalDomainImpl(domain.getString("IntervalDomainType"),
                                 domain.getString("LowerBound"), domain.getString("UpperBound"));
        }
        partialPlan.addVariable(variableId, new PwVariableImpl(variableId, 
                                                               variables.getString("VariableType"),
                                                               constraintIdList,
                                                               variables.getInt("ParameterId"),
                                                               domainImpl, partialPlan));
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
    }
  }
  public static PwPredicateImpl queryPredicateByKey(int key, PwPartialPlanImpl partialPlan) {
    PwPredicateImpl predicateImpl = null;
    try {
      ResultSet predicate = 
        queryDatabase("SELECT (PredicateName) FROM Predicate WHERE PredicateId=".concat(Integer.toString(key)));
      predicate.first();
      predicateImpl = new PwPredicateImpl(predicate.getString("PredicateName"),
                                          key, partialPlan);
      ResultSet parameters =
        queryDatabase("SELECT (ParameterName, ParameterId) FROM Parameter WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND PredicateId=").concat(Integer.toString(key)));
      while(parameters.next()) {
        partialPlan.addParameter(parameters.getInt("ParameterId"),
                                 predicateImpl.addParameter(parameters.getString("ParameterName"),
                                                            parameters.getInt("ParameterId")));
      }
      return predicateImpl;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
  }
  public static PwConstraintImpl queryConstraintByKey(int key, PwPartialPlanImpl partialPlan) {
    PwConstraintImpl constraintImpl = null;
    try {
      ResultSet constraint =
        queryDatabase("SELECT (ConstraintName, ConstraintType) FROM VConstraint WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND ConstraintId=").concat(Integer.toString(key)));
      constraint.first();
      ResultSet constrainedVariables = 
        queryDatabase("SELECT (VariableId) from ConstraintVarMap WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND ConstraintId=").concat(constraint.getString("ConstraintId")));
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
  public static PwVariableImpl queryVariableByKey(int key, PwPartialPlan partialPlan) {
    PwVariableImpl variableImpl = null;
    PwDomainImpl domainImpl = null;
    try {
      ResultSet variable = 
        queryDatabase("SELECT (DomainType, VariableType, DomainId, ParameterId) FROM Variable WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND VariableId=").concat(Integer.toString(key)));
      variable.first();
      if(variable.getString("DomainType").equals("IntervalDomain")) {
        ResultSet domain =
          queryDatabase("SELECT (IntervalDomainType, LowerBound, UpperBound) FROM IntervalDomain WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND VariableId=").concat(Integer.toString(key)).concat(" AND IntervalDomainId=").concat(variable.getString("DomainId")));
        domain.first();
        domainImpl = new PwIntervalDomain(domain.getString("IntervalDomainType"),
                                          domain.getString("LowerBound"),
                                          domain.getString("UpperBound"));
      }
      else if(variable.getString("DomainType").equals("EnumeratedDomain")) {
        ResultSet domain =
          queryDatabase("SELECT (Domain) FROM EnumeratedDomain WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND VariableId=").concat(Integer.toString(key)).concat(" AND EnumeratedDomainId=").concat(variable.getString("DomainId")));
        domain.first();
        domainImpl = new PwEnumeratedDomainImpl(new String(domain.getBlob("Domain").getBytes(0, (int)domain.getBlob("Domain").length())));
      }
      ResultSet constraintIds =
        queryDatabase("SELECT (ConstraintId) FROM ConstraintVarMap WHERE PartialPlanId=".concat(Long.toString(partialPlan.getKey())).concat(" AND VariableId=").concat(Integer.toString(key)));
      ArrayList constraintIdList = new ArrayList();
      while(constraintIds.next()) {
        constraintIdList.add(new Integer(constraintIds.getInt("ConstraintId")));
      }
      variable = new PwVariableImpl(key, variable.getString("VariableType"), constraintIdList,
                                    variable.getInt("ParameterId"), domain, partialPlan);
      return variable;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      return null;
    }
  }
}
