package gov.nasa.arc.planworks.db.util;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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
  protected static boolean dbIsConnected;
  private static Connection conn;
  private static long queryTime;
  public static final MySQLDB INSTANCE = new MySQLDB();
  private static final Integer NULL = new Integer(0);
  private static final Integer M1 = new Integer(-1);
  private MySQLDB() {
    dbIsStarted = false;
    dbIsConnected = false;
    queryTime = 0;
  }
  
  /**
   * Start the database and install a handler that shuts down the database for all exit types except
   * hard abort of the JVM
   */
  synchronized public static void startDatabase() throws IllegalArgumentException, IOException, SecurityException
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
    dbStartString.append(" --key_buffer_size=64M --join_buffer_size=16M --query_cache_size=16M");
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

  /**
   * Stop the database
   */

  synchronized public static void stopDatabase() throws IllegalArgumentException, IOException, SecurityException
  {
    if(!dbIsStarted) {
      return;
    }
    StringBuffer dbStopString = new StringBuffer(System.getProperty("mysql.bindir"));
    dbStopString.append("/mysqladmin --user=root --host=127.0.0.1 --socket=");
    dbStopString.append(System.getProperty("mysql.sock")).append(" shutdown");
    Runtime.getRuntime().exec(dbStopString.toString());
  }
  
  /**
   * Establish a connection to the database
   */

  synchronized public static void registerDatabase() throws IOException {
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
          System.err.println("Connection failed.  Trying again...");
          Thread.sleep(500);
        }
      }
      dbIsConnected = true;
    }
    catch(Exception e) {
      System.err.println(e);
      System.exit(-1);
    }
  }
  
  /**
   * Close the database connection.
   */

  synchronized public static void unregisterDatabase() {
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

  /**
   * Execute a SQL query.
   * @param query
   */

  synchronized public static ResultSet queryDatabase(String query) {
    Statement stmt = null;
    ResultSet result = null;
    try {
      if(conn.isClosed()) {
        registerDatabase();
      }
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
    catch(IOException ioe) {
      ioe.printStackTrace();
      System.exit(-1);
    }
    return result;
  }

  /**
   * Execute a SQL statement that modifies the database.
   * @param update
   */

  synchronized public static int updateDatabase(String update) {
    Statement stmt = null;
    int result = -1;
    try {
      if(conn.isClosed()) {
        registerDatabase();
      }
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
    catch(IOException ioe) {
      ioe.printStackTrace();
      System.exit(-1);
    }
    return result;
  }
  /* this may prove useful later, but just slows things down right now
  public static void analyzeDatabase() {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      long t1 = System.currentTimeMillis();
      System.err.println("Analyzing PartialPlan...");
      stmt.execute("ANALYZE TABLE PartialPlan");
      System.err.println("Analyzing Object...");
      stmt.execute("ANALYZE TABLE Object");
      System.err.println("Analyzing Timeline...");
      stmt.execute("ANALYZE TABLE Timeline");
      System.err.println("Analyzing Slot...");
      stmt.execute("ANALYZE TABLE Slot");
      System.err.println("Analyzing Token...");
      stmt.execute("ANALYZE TABLE Token");
      System.err.println("Analyzing Variable...");
      stmt.execute("ANALYZE TABLE Variable");
      System.err.println("Analyzing EnumeratedDomain...");
      stmt.execute("ANALYZE TABLE EnumeratedDomain");
      System.err.println("Analyzing IntervalDomain...");
      stmt.execute("ANALYZE TABLE IntervalDomain");
      System.err.println("Analyzing VConstraint...");
      stmt.execute("ANALYZE TABLE VConstraint");
      System.err.println("Analyzing ConstraintVarMap...");
      stmt.execute("ANALYZE TABLE ConstraintVarMap");
      System.err.println("Analyzing Predicate...");
      stmt.execute("ANALYZE TABLE Predicate");
      System.err.println("Analyzing Parameter...");
      stmt.execute("ANALYZE TABLE Parameter");
      System.err.println("Analyzing ParamVarTokenMap...");
      stmt.execute("ANALYZE TABLE ParamVarTokenMap");
      System.err.println("Analyzing TokenRelation...");
      stmt.execute("ANALYZE TABLE TokenRelation");
      System.err.println((System.currentTimeMillis() - t1) + "ms in database analysis.");
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  */

  /**
   * Load a file into the database
   *
   * @param file The file to load
   * @param tableName The name of the table into which the file should be loaded
   */

  synchronized public static void loadFile(String file, String tableName) {
    updateDatabase("LOAD DATA INFILE '".concat(file).concat("' IGNORE INTO TABLE ").concat(tableName));
  }

  /**
   * Get the model name of a partial plan by the partial plan's Id.
   *
   * @param partialPlanId
   * @return String - the name of the model.
   */

  synchronized public static String queryPartialPlanModelById(Long partialPlanId) {
    try {
      ResultSet model = 
        queryDatabase("SELECT (Model) FROM PartialPlan WHERE PartialPlanId=".concat(partialPlanId.toString()));
      model.first();
      return model.getString("Model");
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
      return null;
    }
  }

  /**
   * Get the names of all projects in the database.
   *
   * @return List - list of Strings
   */

  synchronized public static List getProjectNames() {
    ArrayList retval = new ArrayList();
    try {
      ResultSet names = queryDatabase("SELECT ProjectName FROM Project");
      while(names.next()) {
        retval.add(names.getString("ProjectName"));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  /**
   * Determine the existence of a project in the database by its name
   *
   * @param name
   * @return boolean
   */

  synchronized public static boolean projectExists(String name) {
    try {
      ResultSet rows = 
        queryDatabase("SELECT ProjectId FROM Project WHERE ProjectName='".concat(name).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle) {
    }
    return false;
  }

  /**
   * Determine the existence of a planning sequence in the database by its URL
   *
   * @param url
   * @return boolean
   */

  synchronized public static boolean sequenceExists(String url) {
    try {
      ResultSet rows =
        queryDatabase("SELECT * FROM Sequence WHERE SequenceURL='".concat(url).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){}
    return false;
  }
  
  /**
   * Determine the existence of a partial plan by its sequence Id and name.
   *
   * @param sequenceId
   * @param name
   * @return boolean
   */

  synchronized public static boolean partialPlanExists(Integer sequenceId, String name) {
    try {
      ResultSet rows =
        queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()).concat(" && PlanName='").concat(name).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){}
    return false;
  }

  /**
   * Create a project entry in the database
   *
   * @param name The name of the project.
   */

  synchronized public static void addProject(String name) {
    updateDatabase("INSERT INTO Project (ProjectName) VALUES ('".concat(name).concat("')"));
  }

  /**
   * Get the most recently created project's Id
   *
   * @return Integer
   */

  synchronized public static Integer latestProjectId() {
    try {
      ResultSet newId = queryDatabase("SELECT MAX(ProjectId) AS ProjectId from Project");
      newId.last();
      return new Integer(newId.getInt("ProjectId"));
    }
    catch(SQLException sqle) {
    }
    return null;
  }

  /**
   * Create a planning sequence entry in the database
   *
   * @param url The sequence's url
   * @param projectId The project to which the sequence will be added
   */

  synchronized public static void addSequence(String url, Integer projectId) {
    updateDatabase("INSERT INTO Sequence (SequenceURL, ProjectId) VALUES ('".concat(url).concat("', ").concat(projectId.toString()).concat(")"));
  }

  /**
   * Get the most recently created sequence's Id
   *
   * @return Integer
   */

  synchronized public static Integer latestSequenceId() {
    try {
      ResultSet newId = queryDatabase("SELECT MAX(SequenceId) AS SequenceId from Sequence");
      newId.last();
      return new Integer(newId.getInt("SequenceId"));
    }
    catch(SQLException sqle) {
    }
    return null;
  }

  /**
   * Get the Id of a project given its name
   *
   * @param name
   * @return Integer
   */

  synchronized public static Integer getProjectIdByName(String name) {
    try {
      ResultSet projectId = queryDatabase("SELECT ProjectId FROM Project WHERE ProjectName='".concat(name).concat("'"));
      projectId.first();
      Integer id = new Integer(projectId.getInt("ProjectId"));
      if(projectId.wasNull()) {
        return null;
      }
      return id;
    }
    catch(SQLException sqle) {
    }
    return null;
  }

  /**
   * Get a mapping of sequence Ids to sequence URLs given a project Id
   *
   * @param projectId
   * @return Map
   */

  synchronized public static Map getSequences(Integer projectId) {
    HashMap retval = null;
    try {
      ResultSet sequences = 
        queryDatabase("SELECT SequenceURL, SequenceId FROM Sequence WHERE ProjectId=".concat(projectId.toString()));
      retval = new HashMap();
      while(sequences.next()) {
        retval.put(new Integer(sequences.getInt("SequenceId")), sequences.getString("SequenceURL"));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  /**
   * Remove a project and all of its substructures from the database.
   *
   * @param id
   */

  synchronized public static void deleteProject(Integer id) {
    try {
      ResultSet sequenceIds = 
        queryDatabase("SELECT SequenceId FROM Sequence WHERE ProjectId=".concat(id.toString()));
      while(sequenceIds.next()) {
        Integer sequenceId = new Integer(sequenceIds.getInt("SequenceId"));
        ResultSet partialPlanIds =
          queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
        while(partialPlanIds.next()) {
          Long partialPlanId = new Long(partialPlanIds.getLong("PartialPlanId"));
          updateDatabase("DELETE FROM Object WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM Timeline WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM Slot WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM Token WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM Variable WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM EnumeratedDomain WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM IntervalDomain WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM VConstraint WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM TokenRelation WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM ParamVarTokenMap WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM ConstraintVarMap WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM Predicate WHERE PartialPlanId=".concat(partialPlanId.toString()));
          updateDatabase("DELETE FROM Parameter WHERE PartialPlanId=".concat(partialPlanId.toString()));
        }
        updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      }
      updateDatabase("DELETE FROM Project WHERE ProjectId=".concat(id.toString()));
    }
    catch(SQLException sqle) {
    }
  }

  /**
   * Get the names of all partial plans in a planning sequence
   *
   * @param sequenceId
   * @return List of Strings
   */

  synchronized public static List getPlanNamesInSequence(Integer sequenceId) {
    ArrayList retval = new ArrayList();
    try {
      ResultSet names = 
        queryDatabase("SELECT PlanName FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      while(names.next()) {
        retval.add(names.getString("PlanName"));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  /**
   * Set the sequence Id of a partial plan to a non-negative value
   * 
   * @param sequenceId
   */
  synchronized public static void updatePartialPlanSequenceId(Integer sequenceId) {
    updateDatabase("UPDATE PartialPlan SET SequenceId=".concat(sequenceId.toString()).concat(" WHERE SequenceId=-1"));
  }
  
  /**
   * Get the Id for the most recently created partial plan
   *
   * @param sequenceId
   * @param name The name of the partial plan
   * @return Long
   */

  synchronized public static Long getNewPartialPlanId(Integer sequenceId, String name) {
    Long retval = null;
    try {
      ResultSet partialPlan = 
        queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()).concat(" && PlanName='").concat(name).concat("'"));
      partialPlan.last();
      retval = new Long(partialPlan.getLong("PartialPlanId"));
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  /**
   * Instantiate PwObjectImpl objects from data in the database
   * 
   * @param partialPlan The partial plan object to which the PwObjectImpl objects should be attached
   */

  synchronized public static void createObjects(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet objects = 
        queryDatabase("SELECT ObjectName, ObjectId FROM Object WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(objects.next()) {
        Integer objectId = new Integer(objects.getInt("ObjectId"));
        partialPlan.addObject(objectId, new PwObjectImpl(objectId, 
                                                          objects.getString("ObjectName"),
                                                          partialPlan));
      }
    }
    catch(SQLException sqle) {
    }
  }

  /**
   * Instantiate the Free Token/Timeline/Slot/Token structure from data in the database
   *
   * @param partialPlan The partial plan object to which the structure should be attached
   */

  synchronized public static void createTimelineSlotTokenNodesStructure(PwPartialPlanImpl partialPlan) {
    List objectIdList = partialPlan.getObjectIdList();
    ListIterator objectIdIterator = objectIdList.listIterator();
    PwObjectImpl object = null;
    PwTimelineImpl timeline = null;
    PwSlotImpl slot = null;
    PwTokenImpl token = null;

    try {
      long t1 = System.currentTimeMillis();
      ResultSet timelineSlotTokens = 
        queryDatabase("SELECT Timeline.TimelineId, Timeline.TimelineName, Timeline.ObjectId, Slot.SlotId, Token.TokenId, Token.IsValueToken, Token.StartVarId, Token.EndVarId, Token.ObjectId, Token.RejectVarId, Token.DurationVarId, Token.ObjectVarId, Token.PredicateId, Token.TimelineId, ParamVarTokenMap.VariableId, TokenRelation.TokenRelationId FROM Timeline LEFT JOIN Slot ON Slot.TimelineId=Timeline.TimelineId && Slot.PartialPlanId=Timeline.PartialPlanId LEFT JOIN Token ON Token.PartialPlanId=Slot.PartialPlanId && Token.SlotId=Slot.SlotId LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.PartialPlanId=Token.PartialPlanId && ParamVarTokenMap.TokenId=Token.TokenId LEFT JOIN TokenRelation ON TokenRelation.PartialPlanId=Token.PartialPlanId && (TokenRelation.TokenAId=Token.TokenId || TokenRelation.TokenBId=Token.TokenId) WHERE Timeline.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" ORDER BY Timeline.TimelineId, Slot.SlotId, Token.TokenId, ParamVarTokenMap.ParameterId"));
      System.err.println("Time spent in token query: " + (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
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
      boolean isSlotEmpty = false;
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

        if(!currParamVarId.equals(NULL)) {
          if(!paramVarId.equals(M1) && !paramVars.contains(paramVarId)) {
            paramVars.add(paramVarId);
          }
          paramVarId = currParamVarId;
        }
        if(!currTokenRelationId.equals(NULL)) {
          if(!tokenRelationId.equals(M1) && !tokenRelations.contains(tokenRelationId)) {
            tokenRelations.add(tokenRelationId);
          }
          tokenRelationId = currTokenRelationId;
        }
        if(!objectId.equals(currObjectId) && !currObjectId.equals(NULL)) {
          if(!objectId.equals(M1) && !tokenId.equals(M1) && !isSlotEmpty) {
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
          if(!timelineId.equals(M1) && !tokenId.equals(M1) && !isSlotEmpty) {
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
          if(!slotId.equals(M1) && !tokenId.equals(M1) && !isSlotEmpty) {
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
          isSlotEmpty = currTokenId.equals(NULL);
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
          tokenDurationVarId = currTokenDurationVarId;
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
          tokenRelationId = currTokenRelationId;
          paramVarId = currParamVarId;
        }
      }
      if(!tokenRelations.contains(tokenRelationId) && !tokenRelationId.equals(NULL) &&
         !tokenRelationId.equals(M1)) {
        tokenRelations.add(tokenRelationId);
      }
      if(!paramVars.contains(paramVarId) && !paramVarId.equals(NULL) && !paramVarId.equals(M1)) {
        paramVars.add(paramVarId);
      }
      slot.addToken(tokenId, tokenIsValueToken, tokenSlotId, tokenPredicateId, 
                    tokenStartVarId, tokenEndVarId, tokenDurationVarId, tokenObjectId,
                    tokenRejectVarId, tokenObjectVarId, tokenTimelineId, tokenRelations,
                    paramVars);
      paramVars.clear();
      tokenRelations.clear();
      System.err.println("Time spent creating timeline/slot/token structure: " + 
                         (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
      ResultSet freeTokens = queryDatabase("Select Token.TokenId, Token.IsValueToken, Token.ObjectVarId, Token.StartVarId, Token.EndVarId, Token.DurationVarId, Token.RejectVarId, Token.PredicateId, ParamVarTokenMap.VariableId, ParamVarTokenMap.ParameterId, TokenRelation.TokenRelationId FROM Token LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.TokenId=Token.TokenId && ParamVarTokenMap.PartialPlanId=Token.PartialPlanId LEFT JOIN TokenRelation ON TokenRelation.PartialPlanId=Token.PartialPlanId && (TokenRelation.TokenAId=Token.TokenId || TokenRelation.TokenBId=Token.TokenId) WHERE Token.IsFreeToken=1 && Token.PartialPlanId=".concat(partialPlan.getId().toString()));
      System.err.println("Time spent in free token query: " + 
                         (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
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
        }
      }
      token = new PwTokenImpl(tokenId, tokenIsValueToken, (Integer) null,
                              tokenPredicateId, tokenStartVarId, tokenEndVarId,
                              tokenDurationVarId, (Integer)null, tokenRejectVarId,
                              tokenObjectVarId, (Integer)null, tokenRelations,
                              paramVars, partialPlan);
      partialPlan.addToken(tokenId, token);
      System.err.println("Time spent creating free tokens: " + 
                         (System.currentTimeMillis() - t1));
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }

  /**
   * Instantiate PwConstraintImpl objects from data in the database
   * 
   * @param partialPlan The partial plan to which the constraints should be attached.
   */

  synchronized public static void queryConstraints(PwPartialPlanImpl partialPlan) {
    try {
      long t1 = System.currentTimeMillis();
      ResultSet constraints = 
        queryDatabase("SELECT VConstraint.ConstraintId, VConstraint.ConstraintName, VConstraint.ConstraintType, ConstraintVarMap.VariableId FROM VConstraint LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=VConstraint.PartialPlanId && ConstraintVarMap.ConstraintId=VConstraint.ConstraintId WHERE VConstraint.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" ORDER BY VConstraint.ConstraintId"));
      System.err.println("Time spent in constraint query: " + 
                         (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
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
        //if(!variableId.equals(currVariableId) && !currVariableId.equals(NULL)) {
        //  if(!constrainedVarIds.contains(currVariableId) && !variableId.equals(M1)) {
        //    constrainedVarIds.add(variableId);
        //  }
        //  variableId = currVariableId;
        //}
        if(!currVariableId.equals(NULL)) {
          if(!variableId.equals(M1) && !constrainedVarIds.contains(variableId)) {
            constrainedVarIds.add(variableId);
          }
          variableId = currVariableId;
        }
        if(!constraintId.equals(currConstraintId) && !currConstraintId.equals(NULL)) {
          if(!constraintId.equals(M1)) {
            //System.err.println("Creating constraint on vars " + constrainedVarIds);
            partialPlan.addConstraint(constraintId,
                                      new PwConstraintImpl(constraintName, constraintId,
                                                           constraintType, constrainedVarIds,
                                                           partialPlan));
            constrainedVarIds.clear();
          }
          constraintId = currConstraintId;
          constraintName = currConstraintName;
          constraintType = currConstraintType;
        }
      }
      constrainedVarIds.add(variableId);
      partialPlan.addConstraint(constraintId, new PwConstraintImpl(constraintName, constraintId,
                                                                   constraintType, 
                                                                   constrainedVarIds,partialPlan));
      System.err.println("Time spent creating constraints: " + 
                         (System.currentTimeMillis() - t1));
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }

  /**
   * Instantiate PwPredicateImpl/PwParameterImpl objects from data in the database
   *
   * @param partialPlan The partial plan to which the PwPredicateImpl structure should be attached.
   */

  synchronized public static void queryPredicates(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet predicates = 
        queryDatabase("SELECT PredicateId, PredicateName FROM Predicate WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(predicates.next()) {
        Integer predicateId = new Integer(predicates.getInt("PredicateId"));
        PwPredicateImpl predicate =  new PwPredicateImpl(predicateId,
                                                         predicates.getString("PredicateName"),
                                                         partialPlan);
        partialPlan.addPredicate(predicateId, predicate);
        ResultSet parameters =
          queryDatabase("SELECT ParameterId, ParameterName FROM Parameter WHERE PartialPlanId=".concat(partialPlan.getId().toString()).concat(" && PredicateId=").concat(predicateId.toString()));
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

  /**
   * Instantiate PwVariableImpl objects from data in the database
   *
   * @param partialPlan The partial plan to which the PwVariableImpls should be attached
   */
  synchronized public static void queryVariables(PwPartialPlanImpl partialPlan) {
    //abandon all hope, ye who enter here...
    PwDomainImpl domainImpl = null;
    try {
      System.err.println("Executing variable query...");
      long t1 = System.currentTimeMillis();
      ResultSet variables =
        //        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.DomainId, IntervalDomain.IntervalDomainType, IntervalDomain.LowerBound, IntervalDomain.UpperBound, EnumeratedDomain.Domain, ConstraintVarMap.ConstraintId, ParamVarTokenMap.ParameterId, ParamVarTokenMap.TokenId FROM Variable LEFT JOIN IntervalDomain ON IntervalDomain.PartialPlanId=Variable.PartialPlanId && IntervalDomain.IntervalDomainId=Variable.DomainId LEFT JOIN EnumeratedDomain ON EnumeratedDomain.PartialPlanId=Variable.PartialPlanId && EnumeratedDomain.EnumeratedDomainId=Variable.DomainId LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId && ConstraintVarMap.VariableId=Variable.VariableId LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.PartialPlanId=Variable.PartialPlanId && ParamVarTokenMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getKey().toString()).concat(" ORDER BY Variable.VariableId, ConstraintVarMap.ConstraintId"));
        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.DomainId, IntervalDomain.IntervalDomainType, IntervalDomain.LowerBound, IntervalDomain.UpperBound, EnumeratedDomain.Domain, ConstraintVarMap.ConstraintId, ParamVarTokenMap.ParameterId, ParamVarTokenMap.TokenId FROM Variable LEFT JOIN IntervalDomain ON IntervalDomain.PartialPlanId=Variable.PartialPlanId && IntervalDomain.IntervalDomainId=Variable.DomainId LEFT JOIN EnumeratedDomain ON EnumeratedDomain.PartialPlanId=Variable.PartialPlanId && EnumeratedDomain.EnumeratedDomainId=Variable.DomainId LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId && ConstraintVarMap.VariableId=Variable.VariableId LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.PartialPlanId=Variable.PartialPlanId && ParamVarTokenMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getId().toString()));
      System.err.println("Time spent in variable query: " +
                         (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
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
            if(!constraintIdList.contains(constraintId) && !constraintId.equals(M1) && 
               !constraintId.equals(NULL) && !currConstraintId.equals(NULL)) {
              constraintIdList.add(constraintId);
            }
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
        if(!constraintId.equals(M1) && !constraintIdList.contains(currConstraintId) &&
           !currConstraintId.equals(NULL)) {
          constraintIdList.add(currConstraintId);
        }
        constraintId = currConstraintId;
      }
      partialPlan.addVariable(variableId, new PwVariableImpl(variableId, variableType,
                                                             constraintIdList,
                                                             parameterIdList, tokenIdList,
                                                             domainImpl, partialPlan));
      System.err.println("Time spent creating variables: " +
                         (System.currentTimeMillis() - t1));
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }

  /**
   * Instantiate PwTokenRelationImpl objects from data in the database.
   *
   * @param partialPlan The partial plan to which the PwTokenRelationImpls should be attached.
   */

  synchronized public static void queryTokenRelations(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet tokenRelations = 
        queryDatabase("SELECT TokenRelationId, TokenAId, TokenBId, RelationType FROM TokenRelation WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(tokenRelations.next()) {
        Integer id = new Integer(tokenRelations.getInt("TokenRelationId"));
        partialPlan.addTokenRelation(id, 
                                     new PwTokenRelationImpl(id,
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
