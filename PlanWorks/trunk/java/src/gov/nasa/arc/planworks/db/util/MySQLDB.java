//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MySQLDB.java,v 1.75 2004-01-02 22:28:32 miatauro Exp $
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
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.impl.PwConstraintImpl;
import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwParameterImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwPredicateImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenRelationImpl;
import gov.nasa.arc.planworks.db.impl.PwTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableQueryImpl;
import gov.nasa.arc.planworks.util.OneToManyMap;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UniqueSet;

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
    dbStartString.append(" --key_buffer_size=64M --join_buffer_size=16M --query_cache_size=16M ");
    dbStartString.append(" -O bulk_insert_buffer_size=16M");
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
            catch(Exception e) {
              System.err.println("FAILED TO STOP DATABASE: " + e);
              System.err.println("Database may be corrupted.");
            }
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
          conn = DriverManager.getConnection("jdbc:mysql://localhost/PlanWorks?user=root&autoReconnect=true");
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
      stmt.execute("ANALYZE TABLE PartialPlan");
      stmt.execute("ANALYZE TABLE Object");
      stmt.execute("ANALYZE TABLE Token");
      stmt.execute("ANALYZE TABLE Variable");
      stmt.execute("ANALYZE TABLE VConstraint");
      stmt.execute("ANALYZE TABLE ConstraintVarMap");
      stmt.execute("ANALYZE TABLE Predicate");
      stmt.execute("ANALYZE TABLE Parameter");
      stmt.execute("ANALYZE TABLE TokenRelation");
      stmt.execute("ANALYZE TABLE Transaction");
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
//     try {
//       Statement stmt = conn.createStatement();
//       stmt.execute("ALTER TABLE ".concat(tableName).concat(" DISABLE KEYS"));
      updateDatabase("LOAD DATA INFILE '".concat(file).concat("' IGNORE INTO TABLE ").concat(tableName));
 //      stmt.execute("ALTER TABLE ".concat(tableName).concat(" ENABLE KEYS"));
//     }
//     catch(SQLException sqle) {
//       System.err.println(sqle);
//       sqle.printStackTrace();
//     }
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

  synchronized public static boolean sequenceExists(Long id) {
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

  synchronized public static boolean partialPlanExists(Long sequenceId, String name) {
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

  synchronized public static Long addSequence(String url, Integer projectId) {
    loadFile(url + System.getProperty("file.separator") + "sequence", "Sequence");
    Long latestSequenceId = latestSequenceId();
    updateDatabase("UPDATE Sequence SET ProjectId=".concat(projectId.toString()).concat(" WHERE SequenceId=").concat(latestSequenceId.toString()));
    return latestSequenceId;
  }

  /**
   * Get the most recently created sequence's Id
   *
   * @return Integer
   */

  synchronized public static Long latestSequenceId() {
    try {
      ResultSet newId = 
        queryDatabase("SELECT SequenceId FROM Sequence ORDER BY SequenceOrdering");
      newId.last();
      return new Long(newId.getLong("SequenceId"));
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
        retval.put(new Long(sequences.getLong("SequenceId")), sequences.getString("SequenceURL"));
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
        Long sequenceId = new Long(sequenceIds.getLong("SequenceId"));
        deletePlanningSequence(sequenceId);
      }
      updateDatabase("DELETE FROM Project WHERE ProjectId=".concat(id.toString()));
    }
    catch(SQLException sqle) {
    }
  }

  synchronized public static void deletePlanningSequence(Long sequenceId) throws ResourceNotFoundException{
    if(!sequenceExists(sequenceId)) {
      throw new ResourceNotFoundException("Sequence with id " + sequenceId + " not in database.");
    }
    try {
      ResultSet partialPlanIds =
        queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      StringBuffer whereClause = new StringBuffer(" WHERE PartialPlanId IN (");
      int nppIds = 0;
      while(partialPlanIds.next()) {
        whereClause.append(partialPlanIds.getLong("PartialPlanId"));
        if(!partialPlanIds.isLast()) {
          whereClause.append(", ");
        }
        nppIds++;
      }
      whereClause.append(")");
      if(nppIds != 0) {
        updateDatabase("DELETE FROM Object".concat(whereClause.toString()));
        updateDatabase("DELETE FROM Token".concat(whereClause.toString()));
        updateDatabase("DELETE FROM Variable".concat(whereClause.toString()));
        updateDatabase("DELETE FROM VConstraint".concat(whereClause.toString()));
        updateDatabase("DELETE FROM TokenRelation".concat(whereClause.toString()));
        updateDatabase("DELETE FROM ConstraintVarMap".concat(whereClause.toString()));
        updateDatabase("DELETE FROM Predicate".concat(whereClause.toString()));
        updateDatabase("DELETE FROM Parameter".concat(whereClause.toString()));
        updateDatabase("DELETE FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      }
      updateDatabase("DELETE FROM PartialPlanStats WHERE SequenceId=".concat(sequenceId.toString()));
      updateDatabase("DELETE FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()));
      updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      analyzeDatabase();
    }
    catch(SQLException sqle){}
  }

  /**
   * Get the names of all partial plans in a planning sequence
   *
   * @param sequenceId
   * @return List of Strings
   */

  synchronized public static List getPlanNamesInSequence(Long sequenceId) {
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
   * Get the Id for the most recently created partial plan
   *
   * @param sequenceId
   * @param name The name of the partial plan
   * @return Long
   */

  synchronized public static Long getNewPartialPlanId(Long sequenceId, String name) {
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

  synchronized public static Long getPartialPlanIdByName(Long sequenceId, String name) {
    Long retval = null;
    try {
      StringBuffer temp = 
        new StringBuffer("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=");
      temp.append(sequenceId.toString()).append(" && PlanName='").append(name).append("'");
      System.err.println(temp.toString());
      ResultSet partialPlan =
        queryDatabase(temp.toString());
      partialPlan.last();
      retval = new Long(partialPlan.getLong("PartialPlanId"));
      if(partialPlan.wasNull()) {
        return null;
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static Long getPartialPlanIdByStepNum(Long sequenceId, int stepNum) {
    Long retval = null;
    try {
      StringBuffer temp =
        new StringBuffer("SELECT PartialPlanId FROM PartialPlanStats WHERE SequenceId=");
      temp.append(sequenceId.toString()).append(" && StepNum=").append(stepNum);
      ResultSet ppId = queryDatabase(temp.toString());
      ppId.last();
      retval = new Long(ppId.getLong("PartialPlanId"));
      if(ppId.wasNull()) {
        return null;
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryPartialPlanNames(Long sequenceId) {
    List retval = new ArrayList();
    try {
      ResultSet stepNums = queryDatabase("SELECT StepNum FROM PartialPlanStats WHERE SequenceId=".concat(sequenceId.toString()).concat(" ORDER BY PartialPlanId"));
      while(stepNums.next()) {
        retval.add("step" + stepNums.getInt("StepNum"));
      }
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

  synchronized public static Map queryTransactions(Long sequenceId) {
    Map retval = new HashMap();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, ObjectId, Source, TransactionId, StepNumber, PartialPlanId, TransactionInfo FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" ORDER BY StepNumber, TransactionId"));
      String [] info = new String [3];
      while(transactions.next()) {
        info[0] = info[1] = info[2] = "";
        Long partialPlanId = new Long(transactions.getLong("PartialPlanId"));
        Integer transactionId = new Integer(transactions.getInt("TransactionId"));
        PwTransactionImpl transaction = 
          new PwTransactionImpl(transactions.getString("TransactionType"), 
                                transactionId,
                                transactions.getString("Source"),
                                new Integer(transactions.getInt("ObjectId")),
                                new Integer(transactions.getInt("StepNumber")),
                                sequenceId, partialPlanId);
        Blob blob = transactions.getBlob("TransactionInfo");
        String infoStr = new String(blob.getBytes(1, (int) blob.length()));
        StringTokenizer strTok = new StringTokenizer(infoStr, ",");
        for(int i = 0; i < 3; i++) {
          if(strTok.hasMoreTokens()) {
            info[i] = strTok.nextToken();
          }
          else {
            break;
          }
          if(i == 2 && !info[0].equals(DbConstants.PARAMETER_VAR)) {
            info[2] = "";
          }
        }
        transaction.setInfo(info);
        retval.put(partialPlanId.toString() + transactionId.toString(), transaction);
      }
    }
    catch(SQLException sqle) {}
    return retval;
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
      ResultSet timelineSlotTokens = 
        queryDatabase("SELECT Token.TimelineId, Token.TimelineName, Token.ObjectId, Token.SlotId, Token.TokenId, Token.IsValueToken, Token.StartVarId, Token.EndVarId, Token.RejectVarId, Token.DurationVarId, Token.ObjectVarId, Token.PredicateId, Token.ParamVarIds, Token.TokenRelationIds FROM Token WHERE Token.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" && Token.IsFreeToken=0 ORDER BY Token.ObjectId, Token.TimelineId, Token.SlotIndex, Token.TokenId"));
      PwObjectImpl object = null;
      PwTimelineImpl timeline = null;
      PwSlotImpl slot = null;
      PwTokenImpl token = null;
      while(timelineSlotTokens.next()) {
        Integer objectId = new Integer(timelineSlotTokens.getInt("Token.ObjectId"));
        Integer timelineId = new Integer(timelineSlotTokens.getInt("Token.TimelineId"));
        Integer slotId = new Integer(timelineSlotTokens.getInt("Token.SlotId"));
        Integer tokenId = new Integer(timelineSlotTokens.getInt("Token.TokenId"));
        if(timelineSlotTokens.wasNull()) {
          tokenId = null;
        }

        if(object == null || !object.getId().equals(objectId)) {
          object = partialPlan.getObjectImpl(objectId);
        }
        if(timeline == null || !timeline.getId().equals(timelineId)) {
          timeline = object.addTimeline(timelineSlotTokens.getString("Token.TimelineName"),
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
          Blob blob = timelineSlotTokens.getBlob("Token.ParamVarIds");
          if(!timelineSlotTokens.wasNull()) {
            String paramVarStr = new String(blob.getBytes(1, (int) blob.length()));
            if(!paramVarStr.equals("NULL")) {
              try {
                StringTokenizer paramVarTok = new StringTokenizer(paramVarStr, ":");
                while(paramVarTok.hasMoreTokens()) {
                  token.addParamVar(Integer.decode(paramVarTok.nextToken()));
                }
              }
              catch(NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
              }
            }
          }
          blob = timelineSlotTokens.getBlob("Token.TokenRelationIds");
          if(!timelineSlotTokens.wasNull()) {
            String tokenRelationStr = new String(blob.getBytes(1, (int) blob.length()));
            if(!tokenRelationStr.equals("NULL")) {
              try {
                StringTokenizer tokenRelationTok = new StringTokenizer(tokenRelationStr, ":");
                while(tokenRelationTok.hasMoreTokens()) {
                  token.addTokenRelation(Integer.decode(tokenRelationTok.nextToken()));
                }
              }
              catch(NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
              }
            }
          }
          slot.addToken(token);
        }
      }
      ResultSet freeTokens = queryDatabase("Select Token.TokenId, Token.IsValueToken, Token.ObjectVarId, Token.StartVarId, Token.EndVarId, Token.DurationVarId, Token.RejectVarId, Token.PredicateId, Token.ParamVarIds, Token.TokenRelationIds FROM Token WHERE Token.IsFreeToken=1 && Token.PartialPlanId=".concat(partialPlan.getId().toString()));
      token = null;
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
          Blob blob = freeTokens.getBlob("Token.ParamVarIds");
          if(!freeTokens.wasNull()) {
            String paramVarStr = new String(blob.getBytes(1, (int) blob.length()));
            if(!paramVarStr.equals("NULL")) {
              try {
                StringTokenizer paramVarTok = new StringTokenizer(paramVarStr, ":");
                while(paramVarTok.hasMoreTokens()) {
                  token.addParamVar(Integer.decode(paramVarTok.nextToken()));
                }
              }
              catch(NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
              }
            }
          }
          blob = freeTokens.getBlob("Token.TokenRelationIds");
          if(!freeTokens.wasNull()) {
            String tokenRelationStr = new String(blob.getBytes(1, (int) blob.length()));
            if(!tokenRelationStr.equals("NULL")) {
              try {
                StringTokenizer tokenRelationTok = new StringTokenizer(tokenRelationStr, ":");
                while(tokenRelationTok.hasMoreTokens()) {
                  token.addTokenRelation(Integer.decode(tokenRelationTok.nextToken()));
                }
              }
              catch(NumberFormatException nfe) {
                System.err.println(nfe.getMessage());
              }
            }
          }
          partialPlan.addToken(tokenId, token);
        }
      }
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
       ResultSet constraints = 
        queryDatabase("SELECT VConstraint.ConstraintId, VConstraint.ConstraintName, VConstraint.ConstraintType, ConstraintVarMap.VariableId FROM VConstraint LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=VConstraint.PartialPlanId && ConstraintVarMap.ConstraintId=VConstraint.ConstraintId WHERE VConstraint.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" ORDER BY VConstraint.ConstraintId"));
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
          predicate.addParameter(parameterId, parameters.getString("ParameterName"));
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
    try {
      ResultSet variables =
        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.EnumDomain, Variable.IntDomainType, Variable.IntDomainLowerBound, Variable.IntDomainUpperBound, Variable.TokenId, Variable.ParameterId, ConstraintVarMap.ConstraintId FROM Variable LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId && ConstraintVarMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getId().toString()));
      PwVariableImpl variable = null;
      while(variables.next()) {
        Integer variableId = new Integer(variables.getInt("Variable.VariableId"));
        if(variable == null || !variable.getId().equals(variableId)) {
          PwDomainImpl domain = null;
          String domainType = variables.getString("Variable.DomainType");
          if(domainType.equals("EnumeratedDomain")) {
            Blob blob = variables.getBlob("Variable.EnumDomain");
            domain = new PwEnumeratedDomainImpl(new String(blob.getBytes(1, (int) blob.length())));
          }
          else if(domainType.equals("IntervalDomain")) {
            domain = 
              new PwIntervalDomainImpl(variables.getString("Variable.IntDomainType"),
                                       variables.getString("Variable.IntDomainLowerBound"),
                                       variables.getString("Variable.IntDomainUpperBound"));
          }
          variable = new PwVariableImpl(variableId, variables.getString("Variable.VariableType"),
                                        domain, partialPlan);
          partialPlan.addVariable(variableId, variable);
          Integer parameterId = new Integer(variables.getInt("Variable.ParameterId"));
          if(!variables.wasNull() && parameterId.intValue() != -1) {
            variable.addParameter(parameterId);
          }
          Integer tokenId = new Integer(variables.getInt("Variable.TokenId"));
          variable.addToken(tokenId);
        }
        Integer constraintId = new Integer(variables.getInt("ConstraintVarMap.ConstraintId"));
        if(!variables.wasNull()) {
          variable.addConstraint(constraintId);
        }
      }
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

  synchronized public static List queryTransactionsForConstraint(Long sequenceId, 
                                                                 Integer constraintId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(constraintId.toString()).concat(" && TransactionType LIKE 'CONSTRAINT_%'ORDER BY TransactionId"));
      while(transactions.next()) {
        retval.add(Long.toString(transactions.getLong("PartialPlanId")).concat(Integer.toString(transactions.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTransactionsForToken(Long sequenceId, Integer tokenId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(tokenId.toString()).concat(" && TransactionType LIKE 'TOKEN_%' ORDER BY TransactionId"));
      while(transactions.next()) {
        retval.add(Long.toString(transactions.getLong("PartialPlanId")).concat(Integer.toString(transactions.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTransactionsForVariable(Long sequenceId, Integer varId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(varId.toString()).concat(" && TransactionType LIKE 'VARIABLE_%' ORDER BY TransactionId"));
      while(transactions.next()) {
        retval.add(Long.toString(transactions.getLong("PartialPlanId")).concat(Integer.toString(transactions.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithTokenTransaction(Long sequenceId, Integer tokenId,
                                                                 String type) {
    List retval = new UniqueSet();
    try {
      ResultSet translations =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(tokenId.toString()).concat(" && TransactionType LIKE '").concat(type).concat("'"));
      while(translations.next()) {
        retval.add(Long.toString(translations.getLong("PartialPlanId")).concat(Integer.toString(translations.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithVariableTransaction(Long sequenceId, Integer varId,
                                                                    String type) {
    List retval = new UniqueSet();
    try {
      ResultSet translations =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(varId.toString()).concat(" && TransactionType LIKE '").concat(type).concat("'"));
      while(translations.next()) {
        retval.add(Long.toString(translations.getLong("PartialPlanId")).concat(Integer.toString(translations.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithConstraintTransaction(Long sequenceId, 
                                                                      Integer constraintId,
                                                                      String type) {
    List retval = new UniqueSet();
    try {
      ResultSet translations =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(constraintId.toString()).concat(" && TransactionType LIKE '").concat(type).concat("'"));
      while(translations.next()) {
        retval.add(Long.toString(translations.getLong("PartialPlanId")).concat(Integer.toString(translations.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithTokenTransaction(Long sequenceId, String type) {
    List retval = new UniqueSet();
    try {
      ResultSet translations =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType LIKE '").concat(type).concat("'"));
      while(translations.next()) {
        retval.add(Long.toString(translations.getLong("PartialPlanId")).concat(Integer.toString(translations.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithVariableTransaction(Long sequenceId, String type) {
    List retval = new UniqueSet();
    try {
      ResultSet translations =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType LIKE '").concat(type).concat("'"));
      while(translations.next()) {
        retval.add(Long.toString(translations.getLong("PartialPlanId")).concat(Integer.toString(translations.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithConstraintTransaction(Long sequenceId, 
                                                                      String type) {
    List retval = new UniqueSet();
    try {
      ResultSet translations =
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType LIKE '").concat(type).concat("'"));
      while(translations.next()) {
        retval.add(Long.toString(translations.getLong("PartialPlanId")).concat(Integer.toString(translations.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithRestrictions(Long sequenceId) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions = queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType='").concat(DbConstants.VARIABLE_DOMAIN_RESTRICTED).concat("'"));
      while(transactions.next()) {
        retval.add(Long.toString(transactions.getLong("PartialPlanId")).concat(Integer.toString(transactions.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithRelaxations(Long sequenceId) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions = 
        queryDatabase("SELECT TransactionId, PartialPlanId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType='").concat(DbConstants.VARIABLE_DOMAIN_RELAXED).concat("'"));
      while(transactions.next()) {
        retval.add(Long.toString(transactions.getLong("PartialPlanId")).concat(Integer.toString(transactions.getInt("TransactionId"))));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithUnitVariableDecisions(PwPlanningSequenceImpl seq) {
    List retval = new ArrayList();
    try {
      ResultSet transactedSteps = queryDatabase("SELECT * FROM Transaction WHERE SequenceId=".concat(seq.getId().toString()).concat(" && TransactionType='").concat(DbConstants.VARIABLE_DOMAIN_SPECIFIED).concat("'"));
      while(transactedSteps.next()) {
	int stepNum = transactedSteps.getInt("StepNumber");
        int varId = transactedSteps.getInt("ObjectId");
        ResultSet previousSteps = queryDatabase("SELECT * FROM Transaction WHERE SequenceId=".concat(seq.getId().toString()).concat(" && ObjectId=").concat(Integer.toString(varId)).concat(" && StepNumber < ").concat(Integer.toString(stepNum)).concat(" ORDER BY StepNumber"));
        previousSteps.last();
        int lastStepNum = previousSteps.getInt("StepNumber");
        if(previousSteps.wasNull()) {
          continue;
        }
        if(varSpecDomainIsSingleton(transactedSteps.getBlob("TransactionInfo")) &&
           varDerivedDomainIsSingleton(previousSteps.getBlob("TransactionInfo"))) {
          retval.add(Long.toString(transactedSteps.getLong("PartialPlanId")).concat(Integer.toString(transactedSteps.getInt("TransactionId"))));
        }
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithNonUnitVariableDecisions(PwPlanningSequenceImpl seq) {
    List retval = new ArrayList();
    try {
      ResultSet transactedSteps = queryDatabase("SELECT * FROM Transaction WHERE SequenceId=".concat(seq.getId().toString()).concat(" && TransactionType='").concat(DbConstants.VARIABLE_DOMAIN_SPECIFIED).concat("'"));
      while(transactedSteps.next()) {
	int stepNum = transactedSteps.getInt("StepNumber");
        int varId = transactedSteps.getInt("ObjectId");
        if(!varDerivedDomainIsSingleton(transactedSteps.getBlob("TransactionInfo"))) {
          retval.add(Long.toString(transactedSteps.getLong("PartialPlanId")).concat(Integer.toString(transactedSteps.getInt("TransactionId"))));
        }
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  
  synchronized public static List queryFreeTokensAtStep( int stepNum, PwPlanningSequenceImpl seq) {
    // return list of lists of TokenId, PartialPlanId, StepNum, PredicateName
    // currently does all steps
    List retval = new ArrayList();
    try {
      Long partialPlanId = getPartialPlanIdByStepNum(seq.getId(), stepNum);
      String queryString = "SELECT Token.TokenId, Token.PredicateId, Token.PartialPlanId, Predicate.PredicateName FROM Token LEFT JOIN Predicate ON Predicate.PartialPlanId=Token.PartialPlanId && Predicate.PredicateId=Token.PredicateId WHERE Token.IsFreeToken=1 && Token.PartialPlanId=".concat(partialPlanId.toString());

      System.err.println( "queryString " + queryString);
      ResultSet queryResults = queryDatabase( queryString);
      while (queryResults.next()) {
        List retvalObject = new ArrayList();
        retvalObject.add( new Integer( queryResults.getInt( "Token.TokenId")));
        retvalObject.add(new Long( queryResults.getLong( "Token.PartialPlanId")));
        retvalObject.add(new Integer(stepNum));
        retvalObject.add( queryResults.getString( "Predicate.PredicateName"));
        System.err.println( "retvalObject " + retvalObject);
        retval.add( retvalObject);
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryUnboundVariablesAtStep( int stepNum,
                                                               PwPlanningSequenceImpl seq) {
    List retval = new ArrayList();
    try {
      Long partialPlanId = getPartialPlanIdByStepNum(seq.getId(), stepNum);
      ResultSet vars = 
        queryDatabase("SELECT VariableId, VariableType, DomainType, EnumDomain, IntDomainType, IntDomainUpperBound, IntDomainLowerBound FROM Variable WHERE PartialPlanId=".concat(partialPlanId.toString()).concat(" && VariableType IN('OBJECT_VAR', 'PARAMETER_VAR')"));
      boolean instantiateVar;
      while(vars.next()) {
        instantiateVar = false;
        if(vars.getString("DomainType").equals("IntervalDomain")) {
          if(vars.getString("IntDomainType").equals("INTEGER_SORT")) {
            instantiateVar = !(Long.parseLong(vars.getString("IntDomainLowerBound")) == 
                               Long.parseLong(vars.getString("IntDomainUpperBound")));
          }
          else {
            instantiateVar = !(Double.parseDouble(vars.getString("IntDomainLowerBound")) == 
                               Double.parseDouble(vars.getString("IntDomainUpperBound")));
          }
        }
        else {
          Blob domain = vars.getBlob("EnumDomain");
          instantiateVar = 
            !varDomainIsSingleton("E", new String(domain.getBytes(1, (int) domain.length())));
        }
        if(instantiateVar) {
          retval.add(new PwVariableQueryImpl(new Integer(vars.getInt("VariableId")),
                                             vars.getString("VariableType"), new Integer(stepNum),
                                             seq.getId(), partialPlanId, true));
        }
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }


  synchronized private static boolean varDerivedDomainIsSingleton(Blob info) {
    String temp;
    try {
      temp = new String(info.getBytes(1, (int) info.length()));
    }
    catch(SQLException sqle) {return false;}
    StringTokenizer strTok = new StringTokenizer(temp, ",");
    while(strTok.countTokens() != 4) {
      strTok.nextToken();
    }
    String type = strTok.nextToken();
    String domain = strTok.nextToken();
    return varDomainIsSingleton(type, domain);
  }

  synchronized private static boolean varSpecDomainIsSingleton(Blob info) {
    String temp;
    try {
      temp = new String(info.getBytes(1, (int) info.length()));
    }
    catch(SQLException sqle) {return false;}
    StringTokenizer strTok = new StringTokenizer(temp, ",");
    if (strTok.countTokens() != 7) {
      System.err.println( "MySQLDB.varSpecDomainIsSingleton temp '" + temp + "'");
      System.err.println( "   ... returning false");
      return false;
    }
    while(strTok.countTokens() != 2) {
      strTok.nextToken();
    }
    String type = strTok.nextToken();
    String domain = strTok.nextToken();
    return varDomainIsSingleton(type, domain);
  }

  synchronized private static boolean varDomainIsSingleton(String type, String domain) {
    StringTokenizer strTok = new StringTokenizer(domain);
    if(type.equals("E")) {
      return strTok.countTokens() == 1;
    }
    else if(type.equals("I")) {
      return strTok.countTokens() == 2 && 
        strTok.nextToken().trim().equals(strTok.nextToken().trim());
    }
    return false;
  }

  synchronized public static List queryPartialPlanSizes(Long sequenceId) {
    List retval = new ArrayList();
    int [] data;
    try {
      ResultSet steps = 
        queryDatabase("SELECT PartialPlanId, NumTokens, NumVariables, NumConstraints FROM PartialPlanStats WHERE SequenceId=".concat(sequenceId.toString()).concat(" ORDER BY StepNum"));
      while(steps.next()) {
        data = new int[3];
        data[0] = steps.getInt("NumTokens");
        data[1] = steps.getInt("NumVariables");
        data[2] = steps.getInt("NumConstraints");
        retval.add(data);
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static int [] queryPartialPlanSize(Long partialPlanId) {
    int [] retval = new int[3];
    try {
      ResultSet sizes = 
        queryDatabase("SELECT NumTokens, NumVariables, NumConstraints FROM PartialPlanStats WHERE PartialPlanId=".concat(partialPlanId.toString()));
      sizes.last();
      retval[0] = sizes.getInt("NumTokens");
      retval[1] = sizes.getInt("NumVaraibles");
      retval[2] = sizes.getInt("NumConstraints");
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static Long queryPartialPlanId(Long seqId, int stepNum) {
    Long retval = null;
    try {
      ResultSet ppId = queryDatabase("SELECT PartialPlanId FROM PartialPlanStats WHERE SequenceId=".concat(seqId.toString()).concat(" && StepNum=").concat(Integer.toString(stepNum)));
      ppId.last();
      retval = new Long(ppId.getLong("PartialPlanId"));
    }
    catch(SQLException sqle) {
    }
    return retval;
  }
  synchronized public static Long queryPartialPlanId(Long seqId, String stepName) {
    Long retval = null;
    try {
      ResultSet ppId = queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(seqId.toString()).concat(" && PlanName='").concat(stepName).concat("'"));
      ppId.last();
      retval = new Long(ppId.getLong("PartialPlanId"));
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static Map queryAllIdsForPartialPlan(Long ppId) {
    OneToManyMap retval = new OneToManyMap();
    try {
      ResultSet ids = 
        queryDatabase("SELECT ObjectId FROM Object WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_OBJECT, new Integer(ids.getInt("ObjectId")));
      }
      ids = queryDatabase("SELECT PredicateId FROM Predicate WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_PREDICATE, new Integer(ids.getInt("PredicateId")));
      }
      ids = queryDatabase("SELECT TokenId FROM Token WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_TOKEN, new Integer(ids.getInt("TokenId")));
      }
      ids = queryDatabase("SELECT TokenRelationId FROM TokenRelation WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_TOKENREL, new Integer(ids.getInt("TokenRelationId")));
      }
      ids = queryDatabase("SELECT VariableId FROM Variable WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_VARIABLE, new Integer(ids.getInt("VariableId")));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }
  
  synchronized public static List queryTimelineIdsForObject(Long ppId, Integer objId) {
    List retval = new ArrayList();
    try {
      ResultSet ids = 
        queryDatabase("SELECT DISTINCT TimelineId FROM Token WHERE PartialPlanId=".concat(ppId.toString()).concat(" && ObjectId=").concat(objId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("TimelineId")));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List querySlotIdsForTimeline(Long ppId, Integer objId, Integer tId) {
    List retval = new ArrayList();
    try {
      ResultSet ids =
        queryDatabase("SELECT DISTINCT SlotId FROM Token WHERE PartialPlanId=".concat(ppId.toString()).concat(" && ObjectId=").concat(objId.toString()).concat(" && TimelineId=").concat(tId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("SlotId")));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTokenRelationIdsForToken(Long ppId, Integer tId) {
    List retval = new ArrayList();
    try {
      ResultSet ids =
        //        queryDatabase("SELECT TokenRelationId FROM TokenRelation WHERE PartialPlanId=".concat(ppId.toString()).concat(" && (TokenAId=").concat(tId.toString()).concat(" || TokenBId=").concat(tId.toString()).concat(")"));
        queryDatabase("SELECT TokenRelationId FROM TokenRelation WHERE PartialPlanId=".concat(ppId.toString()).concat(" && TokenBId=").concat(tId.toString()));

      while(ids.next()) {
        retval.add(new Integer(ids.getInt("TokenRelationId")));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTransactionIdsForPartialPlan(Long seqId, Long ppId) {
    List retval = new ArrayList();
    try {
      ResultSet ids = queryDatabase("SELECT TransactionId FROM Transaction WHERE SequenceId=".concat(seqId.toString()).concat(" && PartialPlanId=").concat(ppId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("TransactionId")));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }
}

class ObjectIdComparator implements Comparator {
  public ObjectIdComparator() {
  }
  public boolean equals(Object o) {
    return false;
  }
  public int compare(Object o1, Object o2) {
    PwTransactionImpl t1 = (PwTransactionImpl) o1;
    PwTransactionImpl t2 = (PwTransactionImpl) o2;
    int cmp1 = t1.getPartialPlanId().compareTo(t2.getPartialPlanId());
    if(cmp1 == 0) {
      return t1.getObjectId().compareTo(t2.getObjectId());
    }
    return t1.getPartialPlanId().compareTo(t2.getPartialPlanId());
  }
}
