//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MySQLDB.java,v 1.41 2003-09-09 20:40:30 miatauro Exp $
//
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
import gov.nasa.arc.planworks.util.ResourceNotFoundException;

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
    //System.err.println("Starting db with: " + dbStartString.toString());
    Runtime.getRuntime().exec(dbStartString.toString());
    //    try{Thread.sleep(100000);}catch(Exception e){}
    dbIsStarted = true;
    Runtime.getRuntime().addShutdownHook(new Thread() 
      { 
        public void start() throws IllegalThreadStateException {
          if(MySQLDB.dbIsStarted) {
            //System.err.println("Shutting down database.");
            //System.err.println("Total time spent in queries: " + queryTime + "ms");
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
    //analyzeDatabase();
    return result;
  }
  synchronized public static void analyzeDatabase() {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      long t1 = System.currentTimeMillis();
      stmt.execute("ANALYZE TABLE PartialPlan");
      stmt.execute("ANALYZE TABLE Object");
      stmt.execute("ANALYZE TABLE Timeline");
      stmt.execute("ANALYZE TABLE Slot");
      stmt.execute("ANALYZE TABLE Token");
      stmt.execute("ANALYZE TABLE Variable");
      stmt.execute("ANALYZE TABLE EnumeratedDomain");
      stmt.execute("ANALYZE TABLE IntervalDomain");
      stmt.execute("ANALYZE TABLE VConstraint");
      stmt.execute("ANALYZE TABLE ConstraintVarMap");
      stmt.execute("ANALYZE TABLE Predicate");
      stmt.execute("ANALYZE TABLE Parameter");
      stmt.execute("ANALYZE TABLE ParamVarTokenMap");
      stmt.execute("ANALYZE TABLE TokenRelation");
      //System.err.println((System.currentTimeMillis() - t1) + "ms in database analysis.");
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }

  /**
   * Load a file into the database
   *
   * @param file The file to load
   * @param tableName The name of the table into which the file should be loaded
   */

  synchronized public static void loadFile(String file, String tableName) {
    updateDatabase("LOAD DATA INFILE '".concat(file).concat("' IGNORE INTO TABLE ").concat(tableName));
    //analyzeDatabase();
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

  synchronized public static boolean sequenceExists(Integer id) {
    try {
      ResultSet rows =
        queryDatabase("SELECT * FROM Sequence WHERE SequenceId=".concat(id.toString()));
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

  synchronized public static void deleteProject(Integer id) throws ResourceNotFoundException {
    try {
      ResultSet sequenceIds = 
        queryDatabase("SELECT SequenceId FROM Sequence WHERE ProjectId=".concat(id.toString()));
      while(sequenceIds.next()) {
        Integer sequenceId = new Integer(sequenceIds.getInt("SequenceId"));
        deletePlanningSequence(sequenceId);
      }
      updateDatabase("DELETE FROM Project WHERE ProjectId=".concat(id.toString()));
    }
    catch(SQLException sqle) {
    }
  }

  synchronized public static void deletePlanningSequence(Integer sequenceId) throws ResourceNotFoundException{
    if(!sequenceExists(sequenceId)) {
      throw new ResourceNotFoundException("Sequence with id " + sequenceId + " not in database.");
    }
    try {
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
      updateDatabase("DELETE FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
    }
    catch(SQLException sqle){}
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

    try {
      long t1 = System.currentTimeMillis();
      ResultSet timelineSlotTokens = 
        queryDatabase("SELECT Timeline.TimelineId, Timeline.TimelineName, Timeline.ObjectId, Slot.SlotId, Token.TokenId, Token.IsValueToken, Token.StartVarId, Token.EndVarId, Token.RejectVarId, Token.DurationVarId, Token.ObjectVarId, Token.PredicateId, ParamVarTokenMap.VariableId, TokenRelation.TokenRelationId FROM Timeline LEFT JOIN Slot ON Slot.TimelineId=Timeline.TimelineId && Slot.PartialPlanId=Timeline.PartialPlanId LEFT JOIN Token ON Token.PartialPlanId=Slot.PartialPlanId && Token.SlotId=Slot.SlotId LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.PartialPlanId=Token.PartialPlanId && ParamVarTokenMap.TokenId=Token.TokenId LEFT JOIN TokenRelation ON TokenRelation.PartialPlanId=Token.PartialPlanId && (TokenRelation.TokenAId=Token.TokenId || TokenRelation.TokenBId=Token.TokenId) WHERE Timeline.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" ORDER BY Timeline.ObjectId, Timeline.TimelineId, Slot.SlotIndex, Token.TokenId, ParamVarTokenMap.ParameterId"));
      //System.err.println("Time spent in token query: " + (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
      PwObjectImpl object = null;
      PwTimelineImpl timeline = null;
      PwSlotImpl slot = null;
      PwTokenImpl token = null;
      while(timelineSlotTokens.next()) {
        Integer objectId = new Integer(timelineSlotTokens.getInt("Timeline.ObjectId"));
        Integer timelineId = new Integer(timelineSlotTokens.getInt("Timeline.TimelineId"));
        Integer slotId = new Integer(timelineSlotTokens.getInt("Slot.SlotId"));
        Integer tokenId = new Integer(timelineSlotTokens.getInt("Token.TokenId"));
        if(timelineSlotTokens.wasNull()) {
          tokenId = null;
        }

        if(object == null || !object.getId().equals(objectId)) {
          object = partialPlan.getObjectImpl(objectId);
        }
        if(timeline == null || !timeline.getId().equals(timelineId)) {
          if(object == null) {
            //System.err.println("object " + objectId + " is null...?");
          }
          if(timelineSlotTokens.getString("Timeline.TimelineName") == null) {
            //System.err.println("name for timeline " + timelineId + " is null");
          }
          if(timelineId == null) {
            //System.err.println("timelineId is null");
          }
          timeline = object.addTimeline(timelineSlotTokens.getString("Timeline.TimelineName"),
                                        timelineId);
        }
        if(slot == null || !slot.getId().equals(slotId)) {
          slot = timeline.addSlot(slotId);
        }
        if(tokenId == null) {
          token = null;
          continue;
        }
        if(token == null || !token.getId().equals(tokenId)) {
          token = new PwTokenImpl(tokenId, timelineSlotTokens.getBoolean("Token.IsValueToken"),
                                  slot.getId(), 
                                  new Integer(timelineSlotTokens.getInt("Token.PredicateId")),
                                  new Integer(timelineSlotTokens.getInt("Token.StartVarId")),
                                  new Integer(timelineSlotTokens.getInt("Token.EndVarId")),
                                  new Integer(timelineSlotTokens.getInt("Token.DurationVarId")),
                                  object.getId(), 
                                  new Integer(timelineSlotTokens.getInt("Token.RejectVarId")),
                                  new Integer(timelineSlotTokens.getInt("Token.ObjectVarId")),
                                  timeline.getId(), partialPlan);
          slot.addToken(token);
        }
        Integer paramVarId = new Integer(timelineSlotTokens.getInt("ParamVarTokenMap.VariableId"));
        if(!timelineSlotTokens.wasNull()) {
          token.addParamVar(paramVarId);
        }
        Integer tokenRelationId = 
          new Integer(timelineSlotTokens.getInt("TokenRelation.TokenRelationId"));
        if(!timelineSlotTokens.wasNull()) {
          token.addTokenRelation(tokenRelationId);
        }
      }
      t1 = System.currentTimeMillis();
      ResultSet freeTokens = queryDatabase("Select Token.TokenId, Token.IsValueToken, Token.ObjectVarId, Token.StartVarId, Token.EndVarId, Token.DurationVarId, Token.RejectVarId, Token.PredicateId, ParamVarTokenMap.VariableId, ParamVarTokenMap.ParameterId, TokenRelation.TokenRelationId FROM Token LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.TokenId=Token.TokenId && ParamVarTokenMap.PartialPlanId=Token.PartialPlanId LEFT JOIN TokenRelation ON TokenRelation.PartialPlanId=Token.PartialPlanId && (TokenRelation.TokenAId=Token.TokenId || TokenRelation.TokenBId=Token.TokenId) WHERE Token.IsFreeToken=1 && Token.PartialPlanId=".concat(partialPlan.getId().toString()));
      //System.err.println("Time spent in free token query: " + 
      //                   (System.currentTimeMillis() - t1));
      token = null;
      t1 = System.currentTimeMillis();
      while(freeTokens.next()) {
        Integer tokenId = new Integer(freeTokens.getInt("Token.TokenId"));
        if(token == null || !token.getId().equals(tokenId)) {
          token = new PwTokenImpl(tokenId, freeTokens.getBoolean("Token.IsValueToken"),
                                  (Integer) null, 
                                  new Integer(freeTokens.getInt("Token.PredicateId")),
                                  new Integer(freeTokens.getInt("Token.StartVarId")),
                                  new Integer(freeTokens.getInt("Token.EndVarId")),
                                  new Integer(freeTokens.getInt("Token.DurationVarId")),
                                  (Integer) null, 
                                  new Integer(freeTokens.getInt("Token.RejectVarId")),
                                  new Integer(freeTokens.getInt("Token.ObjectVarId")),
                                  (Integer) null, partialPlan);
          partialPlan.addToken(tokenId, token);
        }
        Integer paramVarId = new Integer(freeTokens.getInt("ParamVarTokenMap.VariableId"));
        if(!freeTokens.wasNull()) {
          token.addParamVar(paramVarId);
        }
        Integer tokenRelationId = new Integer(freeTokens.getInt("TokenRelation.TokenRelationId"));
        if(!freeTokens.wasNull()) {
          token.addTokenRelation(tokenRelationId);
        }
      }
      //System.err.println("Time spent creating free tokens: " + 
      //                   (System.currentTimeMillis() - t1));
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
      //System.err.println("Time spent in constraint query: " + 
      //                  (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
      /*Integer constraintId = new Integer(-1);
      Integer variableId = new Integer(-1);
      String constraintName = new String("");
      String constraintType = new String("");
      ArrayList constrainedVarIds = new ArrayList();*/
      PwConstraintImpl constraint = null;
      while(constraints.next()) {
        Integer constraintId = new Integer(constraints.getInt("VConstraint.ConstraintId"));
        
        if(constraint == null || !constraint.getId().equals(constraintId)) {
          constraint = new PwConstraintImpl(constraints.getString("VConstraint.ConstraintName"),
                                            constraintId,
                                            constraints.getString("VConstraint.ConstraintType"),
                                            partialPlan);
          partialPlan.addConstraint(constraintId, constraint);
        }
        Integer variableId = new Integer(constraints.getInt("ConstraintVarMap.VariableId"));
        if(!constraints.wasNull()) {
          constraint.addVariable(variableId);
        }
      }
      //System.err.println("Time spent creating constraints: " + 
      //                   (System.currentTimeMillis() - t1));
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
    //PwDomainImpl domainImpl = null;
    try {
      //System.err.println("Executing variable query...");
      long t1 = System.currentTimeMillis();
      ResultSet variables =
        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.DomainId, IntervalDomain.IntervalDomainType, IntervalDomain.LowerBound, IntervalDomain.UpperBound, EnumeratedDomain.Domain, ConstraintVarMap.ConstraintId, ParamVarTokenMap.ParameterId, ParamVarTokenMap.TokenId FROM Variable LEFT JOIN IntervalDomain ON IntervalDomain.PartialPlanId=Variable.PartialPlanId && IntervalDomain.IntervalDomainId=Variable.DomainId LEFT JOIN EnumeratedDomain ON EnumeratedDomain.PartialPlanId=Variable.PartialPlanId && EnumeratedDomain.EnumeratedDomainId=Variable.DomainId LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId && ConstraintVarMap.VariableId=Variable.VariableId LEFT JOIN ParamVarTokenMap ON ParamVarTokenMap.PartialPlanId=Variable.PartialPlanId && ParamVarTokenMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getId().toString()));
      //System.err.println("Time spent in variable query: " +
      //                   (System.currentTimeMillis() - t1));
      t1 = System.currentTimeMillis();
      PwVariableImpl variable = null;
      while(variables.next()) {
        Integer variableId = new Integer(variables.getInt("Variable.VariableId"));
        if(variable == null || !variable.getId().equals(variableId)) {
          PwDomainImpl domain = null;
          String domainType = variables.getString("Variable.DomainType");
          if(domainType.equals("EnumeratedDomain")) {
            Blob blob = variables.getBlob("EnumeratedDomain.Domain");
            domain = new PwEnumeratedDomainImpl(new String(blob.getBytes(1, (int) blob.length())));
          }
          else if(domainType.equals("IntervalDomain")) {
            domain = 
              new PwIntervalDomainImpl(variables.getString("IntervalDomain.IntervalDomainType"),
                                       variables.getString("IntervalDomain.LowerBound"),
                                       variables.getString("IntervalDomain.UpperBound"));
          }
          variable = new PwVariableImpl(variableId, variables.getString("Variable.VariableType"),
                                        domain, partialPlan);
          partialPlan.addVariable(variableId, variable);
        }
        Integer constraintId = new Integer(variables.getInt("ConstraintVarMap.ConstraintId"));
        if(!variables.wasNull()) {
          variable.addConstraint(constraintId);
        }
        Integer parameterId = new Integer(variables.getInt("ParamVarTokenMap.ParameterId"));
        if(!variables.wasNull()) {
          variable.addParameter(parameterId);
        }
        Integer tokenId = new Integer(variables.getInt("ParamVarTokenMap.TokenId"));
        if(!variables.wasNull()) {
          variable.addToken(tokenId);
        }
      }
      //System.err.println("Time spent creating variables: " +
      //                   (System.currentTimeMillis() - t1));
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
