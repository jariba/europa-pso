//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: MySQLDB.java,v 1.100 2004-05-11 22:45:46 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.impl.PwConstraintImpl;
import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwDBTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwPredicateImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceInstantImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwRuleImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenRelationImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableQueryImpl;
import gov.nasa.arc.planworks.util.OneToManyMap;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.util.Utilities;

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
      for(int triedConnections = 0; triedConnections <= 10 && !dbIsConnected; triedConnections++) {
        try {
          System.err.println("   ... connecting to database");
          conn = DriverManager.getConnection("jdbc:mysql://localhost/PlanWorks?user=root&autoReconnect=true");
          dbIsConnected = true;
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

  synchronized public static ResultSet queryDatabase(final String query) {
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
      System.exit(-1);
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

  synchronized public static int updateDatabase(final String update) {
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
  synchronized public static void analyzeDatabase() {
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      for(int i = 0; i < DbConstants.PW_DB_TABLES.length; i++) {
        stmt.execute("ANALYZE TABLE " + DbConstants.PW_DB_TABLES[i]);
      }
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

  synchronized public static void loadFile(final String file, final String tableName) {
    updateDatabase("LOAD DATA INFILE '".concat(file).concat("' IGNORE INTO TABLE ").concat(tableName));
  }

  synchronized public static void loadFile(final String file, final String tableName, 
                                           final String columnSeparator, 
                                           final String entrySeparator) {
    updateDatabase("LOAD DATA INFILE '".concat(file).concat("' IGNORE INTO TABLE ").concat(tableName).concat(" FIELDS TERMINATED BY ").concat(columnSeparator).concat(" LINES TERMINATED BY ").concat(entrySeparator));
  }

  /**
   * Get the model name of a partial plan by the partial plan's Id.
   *
   * @param partialPlanId
   * @return String - the name of the model.
   */

  synchronized public static String queryPartialPlanModelById(final Long partialPlanId) {
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

  synchronized public static boolean projectExists(final String name) {
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

  synchronized public static boolean sequenceExists(final String url) {
    try {
      ResultSet rows =
        queryDatabase("SELECT * FROM Sequence WHERE SequenceURL='".concat(url).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){}
    return false;
  }

  synchronized public static boolean sequenceExists(final Long id) {
    try {
      ResultSet rows =
        queryDatabase("SELECT * FROM Sequence WHERE SequenceId=".concat(id.toString()));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){}
    return false;
  }

  synchronized public static String getSequenceUrl(final Long id) {
    String retval = null;
    try {
      ResultSet rows = 
        queryDatabase("SELECT SequenceURL FROM Sequence WHERE SequenceId=".concat(id.toString()));
      rows.last();
      retval = rows.getString("SequenceURL");
    }
    catch(SQLException sqle) {}
    return retval;
  }

  synchronized public static void setSequenceUrl(final Long id, final String url) {
      updateDatabase("UPDATE Sequence SET SequenceURL='".concat(url).concat("' WHERE SequenceId=").concat(id.toString()));
  }
  
  /**
   * Determine the existence of a partial plan by its sequence Id and name.
   *
   * @param sequenceId
   * @param name
   * @return boolean
   */

  synchronized public static boolean partialPlanExists(final Long sequenceId, final String name) {
    try {
      ResultSet rows =
        queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()).concat(" && PlanName='").concat(name).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){}
    return false;
  }

  synchronized public static List queryPlanNamesInDatabase(final Long sequenceId) {
    List retval = new ArrayList();
    try {
      ResultSet rows = 
        queryDatabase("SELECT PlanName from PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      while(rows.next()) {
        retval.add(rows.getString("PlanName"));
      }
    }
    catch(SQLException sqle) {}
    return retval;
  }

  /**
   * Create a project entry in the database
   *
   * @param name The name of the project.
   */

  synchronized public static void addProject(final String name) {
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

  synchronized public static Long addSequence(final String url, final Integer projectId) {
    //loadFile(url + System.getProperty("file.separator") + "sequence", "Sequence");
    loadFile(url + System.getProperty("file.separator") + "sequence", "Sequence",
             DbConstants.SEQ_COL_SEP_HEX, DbConstants.SEQ_LINE_SEP_HEX);
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

  synchronized public static Integer getProjectIdByName(final String name) {
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

  synchronized public static Map getSequences(final Integer projectId) {
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

  synchronized public static void deleteProject(final Integer id) throws ResourceNotFoundException {
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

  synchronized public static void deletePlanningSequence(final Long sequenceId) 
    throws ResourceNotFoundException{
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
        updateDatabase("DELETE FROM ResourceInstants".concat(whereClause.toString()));
        updateDatabase("DELETE FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      }
      updateDatabase("DELETE FROM PartialPlanStats WHERE SequenceId=".concat(sequenceId.toString()));
      updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      updateDatabase("DELETE FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()));
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

  synchronized public static List getPlanNamesInSequence(final Long sequenceId) {
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

  synchronized public static Long getNewPartialPlanId(final Long sequenceId, final String name) {
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

  synchronized public static Long getPartialPlanIdByName(final Long sequenceId, final String name) {
    Long retval = null;
    try {
      StringBuffer temp = 
        new StringBuffer("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=");
      temp.append(sequenceId.toString()).append(" && PlanName='").append(name).append("'");
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

  synchronized public static Long getPartialPlanIdByStepNum(final Long sequenceId, 
                                                            final int stepNum) {
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

  synchronized public static List queryPartialPlanNames(final Long sequenceId) {
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
        queryDatabase("SELECT ObjectId, ObjectType, ParentId, ObjectName, ChildObjectIds, VariableIds, TokenIds, ExtraInfo FROM Object WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(objects.next()) {
        Integer objectId = new Integer(objects.getInt("ObjectId"));
        int type = objects.getInt("ObjectType");
        Integer parentId = new Integer(objects.getInt("ParentId"));
        String name = objects.getString("ObjectName");
        String childObjectIds = null;
        Blob blob = objects.getBlob("ChildObjectIds");
        if(!objects.wasNull()) {
          childObjectIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String variableIds = null;
        blob = objects.getBlob("VariableIds");
        if(!objects.wasNull()) {
          variableIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String tokenIds = null;
        blob = objects.getBlob("TokenIds");
        if(!objects.wasNull()) {
          tokenIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        if(type == DbConstants.O_TIMELINE) {
          String emptySlots = null;
          blob = objects.getBlob("ExtraInfo");
          if(!objects.wasNull()) {
            emptySlots = new String(blob.getBytes(1, (int) blob.length()));
          }
          partialPlan.addTimeline(objectId, 
                                  new PwTimelineImpl(objectId, type, parentId, name, 
                                                     childObjectIds, emptySlots, variableIds,
                                                     tokenIds, partialPlan));
        }
        else if(type == DbConstants.O_RESOURCE) {
          String resInfo = null;
          blob = objects.getBlob("ExtraInfo");
          if(!objects.wasNull()) {
            resInfo = new String(blob.getBytes(1, (int) blob.length()));
          }
          partialPlan.addResource(objectId, new PwResourceImpl(objectId, type, parentId, name,
                                                               childObjectIds, resInfo, 
                                                               variableIds, tokenIds, partialPlan));
        }
        else {
          partialPlan.addObject(objectId, 
                                new PwObjectImpl(objectId, type, parentId, name, childObjectIds,
                                                 variableIds, tokenIds, partialPlan));
        }
      }
    }
    catch(SQLException sqle) {
    }
  }

  synchronized public static int countTransactions(final Long sequenceId) {
    int retval = 0;
    try {
      ResultSet transactions = 
        queryDatabase("SELECT COUNT(*) FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()));
      transactions.last();
      retval = transactions.getInt("COUNT(*)");
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized private static PwDBTransactionImpl instantiateTransaction(ResultSet s) {
    PwDBTransactionImpl retval = null;
    try {
      String [] info = {"", "", ""};
      retval = new PwDBTransactionImpl(s.getString("TransactionName"),
                                       new Integer(s.getInt("TransactionId")),
                                       s.getString("Source"),
                                       new Integer(s.getInt("ObjectId")),
                                       new Integer(s.getInt("StepNumber")),
                                       new Long(s.getLong("SequenceId")),
                                       new Long(s.getLong("PartialPlanId")));
      Blob blob = s.getBlob("TransactionInfo");
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
      retval.setInfo(info);
    }
    catch(SQLException sqle){}
    return retval;
  }

  synchronized public static Map queryTransactions(final Long sequenceId) {
    Map retval = new HashMap();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, TransactionId, StepNumber, PartialPlanId, TransactionInfo, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" ORDER BY StepNumber, TransactionId"));
      while(transactions.next()) {
        PwDBTransactionImpl transaction = instantiateTransaction(transactions);
        retval.put(Long.toString(transactions.getLong("PartialPlanId"))
                   + Integer.toString(transactions.getInt("TransactionId")), transaction);
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

  synchronized public static void createSlotTokenNodesStructure(PwPartialPlanImpl partialPlan,
                                                                final Long seqId) {
    try {
      ResultSet tokens = 
        queryDatabase("SELECT Token.TokenId, Token.TokenType, Token.SlotId, Token.SlotIndex, Token.IsValueToken, Token.StartVarId, Token.EndVarId, Token.StateVarId, Token.DurationVarId, Token.ObjectVarId, Token.PredicateName, Token.ParamVarIds, Token.TokenRelationIds, Token.ExtraData, Token.ParentId, RuleTokenMap.RuleId FROM Token LEFT JOIN RuleTokenMap ON RuleTokenMap.SlaveId=Token.TokenId && RuleTokenMap.SequenceId=".concat(seqId.toString()).concat(" WHERE Token.PartialPlanId=").concat(partialPlan.getId().toString()).concat(" && Token.IsFreeToken=0 ORDER BY Token.ParentId, Token.SlotIndex, Token.TokenId"));
      while(tokens.next()) {
        Integer tokenId = new Integer(tokens.getInt("Token.TokenId"));
        boolean isFreeToken = false;
        boolean isValueToken = tokens.getBoolean("Token.IsValueToken");
        Integer startVarId = new Integer(tokens.getInt("Token.StartVarId"));
        Integer endVarId = new Integer(tokens.getInt("Token.EndVarId"));
        Integer stateVarId = new Integer(tokens.getInt("Token.StateVarId"));
        Integer durationVarId = new Integer(tokens.getInt("Token.DurationVarId"));
        Integer objectVarId = new Integer(tokens.getInt("Token.ObjectVarId"));
        Integer parentId = new Integer(tokens.getInt("Token.ParentId"));
        String predName = tokens.getString("Token.PredicateName");
        String paramVarIds = null;
        Blob blob = tokens.getBlob("Token.ParamVarIds");
        if(!tokens.wasNull()) {
          paramVarIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String tokenRelIds = null;
        blob = tokens.getBlob("Token.TokenRelationIds");
        if(!tokens.wasNull()) {
          tokenRelIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String extraInfo = null;
        blob = tokens.getBlob("Token.ExtraData");
        if(!tokens.wasNull()) {
          extraInfo = new String(blob.getBytes(1, (int) blob.length()));
        }

        PwTokenImpl t = null;
        if(tokens.getInt("Token.TokenType") == DbConstants.T_TRANSACTION) {
          t = new PwResourceTransactionImpl(tokenId, isValueToken, predName, startVarId, endVarId,
                                            durationVarId, stateVarId, objectVarId, parentId, 
                                            tokenRelIds, paramVarIds, extraInfo, partialPlan);
        }
        else if(tokens.getInt("Token.TokenType") == DbConstants.T_INTERVAL) {
          t = new PwTokenImpl(tokenId, isValueToken, new Integer(tokens.getInt("Token.SlotId")),
                              predName, startVarId, endVarId, durationVarId, stateVarId, 
                              objectVarId, parentId, tokenRelIds, paramVarIds, extraInfo, 
                              partialPlan);
        }
        int ruleKey = tokens.getInt("RuleTokenMap.RuleId");
        if(!tokens.wasNull()) {
          t.setRuleId(new Integer(ruleKey));
        }
      }
      ResultSet freeTokens = queryDatabase("Select Token.TokenId, Token.TokenType, Token.IsValueToken, Token.ObjectVarId, Token.StartVarId, Token.EndVarId, Token.DurationVarId, Token.StateVarId, Token.PredicateName, Token.ParamVarIds, Token.TokenRelationIds, Token.ExtraData, RuleTokenMap.RuleId FROM Token LEFT JOIN RuleTokenMap ON RuleTokenMap.SlaveId=Token.TokenId && RuleTokenMap.SequenceId=".concat(seqId.toString()).concat(" WHERE Token.IsFreeToken=1 && Token.PartialPlanId=").concat(partialPlan.getId().toString()));
      while(freeTokens.next()) {
        Integer tokenId = new Integer(freeTokens.getInt("Token.TokenId"));
        boolean isFreeToken = true;
        boolean isValueToken = freeTokens.getBoolean("Token.IsValueToken");
        Integer startVarId = new Integer(freeTokens.getInt("Token.StartVarId"));
        Integer endVarId = new Integer(freeTokens.getInt("Token.EndVarId"));
        Integer stateVarId = new Integer(freeTokens.getInt("Token.StateVarId"));
        Integer durationVarId = new Integer(freeTokens.getInt("Token.DurationVarId"));
        Integer objectVarId = new Integer(freeTokens.getInt("Token.ObjectVarId"));
        String predName = freeTokens.getString("Token.PredicateName");
        String paramVarIds = null;
        Blob blob = freeTokens.getBlob("Token.ParamVarIds");
        if(!freeTokens.wasNull()) {
          paramVarIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String tokenRelIds = null;
        blob = freeTokens.getBlob("Token.TokenRelationIds");
        if(!freeTokens.wasNull()) {
          tokenRelIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String extraInfo = null;
        blob = freeTokens.getBlob("Token.ExtraData");
        if(!freeTokens.wasNull()) {
          extraInfo = new String(blob.getBytes(1, (int) blob.length()));
        }
        PwTokenImpl t = null;
        if(freeTokens.getInt("Token.TokenType") == DbConstants.T_TRANSACTION) {
          t = new PwResourceTransactionImpl(tokenId, isValueToken, predName, startVarId, endVarId,
                                            durationVarId, stateVarId, objectVarId,
                                            DbConstants.NO_ID, tokenRelIds, paramVarIds, extraInfo,
                                            partialPlan);
        }
        else if(freeTokens.getInt("Token.TokenType") == DbConstants.T_INTERVAL) {
          t = new PwTokenImpl(tokenId, isValueToken, DbConstants.NO_ID, predName, startVarId, 
                              endVarId, durationVarId, stateVarId, objectVarId, DbConstants.NO_ID,
                              tokenRelIds, paramVarIds, extraInfo, partialPlan);
        }
        int ruleKey = freeTokens.getInt("RuleTokenMap.RuleId");
        if(!freeTokens.wasNull()) {
          t.setRuleId(new Integer(ruleKey));
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
   * Instantiate PwVariableImpl objects from data in the database
   *
   * @param partialPlan The partial plan to which the PwVariableImpls should be attached
   */
  synchronized public static void queryVariables(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet variables =
        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.EnumDomain, Variable.IntDomainType, Variable.IntDomainLowerBound, Variable.IntDomainUpperBound, Variable.ParentId, Variable.ParameterName, ConstraintVarMap.ConstraintId FROM Variable LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId && ConstraintVarMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getId().toString()));
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
                                        new Integer(variables.getInt("Variable.ParentId")), domain,
                                        partialPlan);
          partialPlan.addVariable(variableId, variable);

          String parameterName = variables.getString("Variable.ParameterName");
          if(!variables.wasNull()) {
            variable.addParameter(parameterName);
          }
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

  synchronized public static void queryResourceInstants(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet resInsts = 
        queryDatabase("SELECT InstantId, TimePoint, LevelMin, LevelMax, Transactions FROM ResourceInstants WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(resInsts.next()) {
        Integer instId = new Integer(resInsts.getInt("InstantId"));
        String transactionIds = null;
        Blob blob = resInsts.getBlob("Transactions");
        if(!resInsts.wasNull()) {
          transactionIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        partialPlan.
          addResourceInstant(instId, 
                             new PwResourceInstantImpl(instId, 
                                                       resInsts.getInt("TimePoint"),
                                                       resInsts.getDouble("LevelMin"),
                                                       resInsts.getDouble("LevelMax"),
                                                       transactionIds,
                                                       partialPlan));
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }

  synchronized public static Map queryRules(final Long sequenceId) {
    Map retval = new HashMap();
    try {
      ResultSet ruleTextInDb = queryDatabase("SELECT RulesText FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      ruleTextInDb.first();
      Blob blob = ruleTextInDb.getBlob("RulesText");
      if(ruleTextInDb.wasNull()) {
        return retval;
      }
      String [] text = (new String(blob.getBytes(1, (int) blob.length()))).split("\n");
      OneToManyMap rulesText = new OneToManyMap();
      String fileName = "";
      for(int i = 0; i < text.length; i++) {
        if(text[i].indexOf("--begin ") != -1) {
          String [] name = text[i].split("\\s", 2);
          fileName = name[1];
          continue;
        }
        rulesText.put(fileName, text[i]);
      }

      char startDelim = System.getProperty("model.rule.delimiter").charAt(0);
      char endDelim = System.getProperty("model.rule.delimiter").charAt(1);

      ResultSet rules = queryDatabase("SELECT SequenceId, RuleId, RuleSource FROM Rules WHERE SequenceId=".concat(sequenceId.toString()).concat(" ORDER BY RuleSource"));
      while(rules.next()) {
        Blob rblob = rules.getBlob("RuleSource");
        String source = new String(rblob.getBytes(1, (int) rblob.length()));
        String [] fileIndex = source.split(",", 2);
        int startIndex = (Integer.parseInt(fileIndex[1]) - 1);
        int endIndex = startIndex;
        List ruleText = rulesText.getList(fileIndex[0]);
        int delimCounter = 0;
        String textArg = "";
        if(startIndex > ruleText.size()) {
          throw new IllegalStateException("Start index OOB: " + startIndex + " " + ruleText.size());
        }
        if(Utilities.countOccurrences(startDelim, (String) ruleText.get(startIndex)) > 0) {
          for(; endIndex < ruleText.size(); endIndex++) {
            delimCounter += Utilities.countOccurrences(startDelim,
                                                       (String) ruleText.get(endIndex));
            delimCounter -= Utilities.countOccurrences(endDelim,
                                                       (String) ruleText.get(endIndex));
            textArg += (String) ruleText.get(endIndex) + "\n";
            if(delimCounter == 0) {
              break;
            }
          }
          Integer id = new Integer(rules.getInt("RuleId"));
          if(Utilities.countOccurrences(startDelim, textArg) != 
             Utilities.countOccurrences(endDelim, textArg)) {
            throw new IllegalStateException("Close/end delims for rule don't match");
          }
          retval.put(id, new PwRuleImpl(new Long(rules.getInt("SequenceId")), id, textArg));
        }
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }

  synchronized public static List queryTransactionsForStep(final Long seqId, final Long ppId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions = queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, TransactionId, StepNumber, TransactionInfo, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(seqId.toString()).concat(" && PartialPlanId=").concat(ppId.toString()).concat(" ORDER BY StepNumber, TransactionId"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle){}
    return retval;
  }

  synchronized public static List queryTransactionsForConstraint(final Long sequenceId, 
                                                                 final Integer constraintId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(constraintId.toString()).concat(" && TransactionName LIKE 'CONSTRAINT_%'ORDER BY TransactionId"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTransactionsForToken(final Long sequenceId, 
                                                            final Integer tokenId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(tokenId.toString()).concat(" && TransactionName LIKE 'TOKEN_%' ORDER BY TransactionId"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTransactionsForVariable(final Long sequenceId, 
                                                               final Integer varId) {
    List retval = new ArrayList();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(varId.toString()).concat(" && TransactionName LIKE 'VARIABLE_%' ORDER BY TransactionId"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryStepsWithTokenTransaction(final Long sequenceId, 
                                                                 final Integer tokenId,
                                                                 final String type) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(tokenId.toString()).concat(" && TransactionName LIKE '").concat(type).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithVariableTransaction(final Long sequenceId, 
                                                                    final Integer varId,
                                                                    final String type) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(varId.toString()).concat(" && TransactionName LIKE '").concat(type).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithConstraintTransaction(final Long sequenceId, 
                                                                      final Integer constraintId,
                                                                      final String type) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && ObjectId=").concat(constraintId.toString()).concat(" && TransactionName LIKE '").concat(type).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithTokenTransaction(final Long sequenceId, 
                                                                 final String type) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionName LIKE '").concat(type).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithVariableTransaction(final Long sequenceId, 
                                                                    final String type) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionName LIKE '").concat(type).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithConstraintTransaction(final Long sequenceId, 
                                                                      final String type) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions =
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionName LIKE '").concat(type).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithRestrictions(final Long sequenceId) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions = queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType='").concat(DbConstants.TT_RESTRICTION).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithRelaxations(final Long sequenceId) {
    List retval = new UniqueSet();
    try {
      ResultSet transactions = 
        queryDatabase("SELECT TransactionType, TransactionName, ObjectId, Source, StepNumber, TransactionInfo, TransactionId, PartialPlanId, SequenceId FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()).concat(" && TransactionType='").concat(DbConstants.TT_RELAXATION).concat("'"));
      while(transactions.next()) {
        retval.add(instantiateTransaction(transactions));
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  //NOTE: these should be changed to just take sequence Ids ~MJI
  synchronized public static List queryStepsWithUnitVariableDecisions(final PwPlanningSequenceImpl seq) {
    List retval = new UniqueSet();
    try {
      ResultSet transactedSteps = queryDatabase("SELECT * FROM Transaction WHERE SequenceId=".concat(seq.getId().toString()).concat(" && TransactionName='").concat(DbConstants.VARIABLE_DOMAIN_SPECIFIED).concat("'"));
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
            retval.add(instantiateTransaction(transactedSteps));
          }
      }
    }
    catch(SQLException sqle) {
    }
    // return retval;
    // Collections.sort will not handle UniqueSet -- convert to ArrayList
    return new ArrayList( retval);
  }

  synchronized public static List queryStepsWithNonUnitVariableDecisions(final PwPlanningSequenceImpl seq) {
    List retval = new ArrayList();
    try {
      ResultSet transactedSteps = queryDatabase("SELECT * FROM Transaction WHERE SequenceId=".concat(seq.getId().toString()).concat(" && TransactionName='").concat(DbConstants.VARIABLE_DOMAIN_SPECIFIED).concat("'"));
      while(transactedSteps.next()) {
	int stepNum = transactedSteps.getInt("StepNumber");
        int varId = transactedSteps.getInt("ObjectId");
        if(!varDerivedDomainIsSingleton(transactedSteps.getBlob("TransactionInfo"))) {
          retval.add(instantiateTransaction(transactedSteps));
        }
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  
  synchronized public static List queryFreeTokensAtStep( final int stepNum, 
                                                         final PwPlanningSequenceImpl seq) {
    // return list of lists of TokenId, PartialPlanId, StepNum, PredicateName
    // currently does all steps
    List retval = new ArrayList();
    try {
      Long partialPlanId = getPartialPlanIdByStepNum(seq.getId(), stepNum);
      String queryString = "SELECT Token.TokenId, Token.PredicateName, Token.PartialPlanId FROM Token WHERE Token.IsFreeToken=1 && Token.PartialPlanId=".concat(partialPlanId.toString());

      // System.err.println( "queryString " + queryString);
      ResultSet queryResults = queryDatabase( queryString);
      while (queryResults.next()) {
        List retvalObject = new ArrayList();
        retvalObject.add( new Integer( queryResults.getInt( "Token.TokenId")));
        retvalObject.add(new Long( queryResults.getLong( "Token.PartialPlanId")));
        retvalObject.add(new Integer(stepNum));
        retvalObject.add( queryResults.getString( "Token.PredicateName"));
        // System.err.println( "retvalObject " + retvalObject);
        retval.add( retvalObject);
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryUnboundVariablesAtStep( final int stepNum,
                                                               final PwPlanningSequenceImpl seq) {
    List retval = new ArrayList();
    try {
      Long partialPlanId = getPartialPlanIdByStepNum(seq.getId(), stepNum);
      ResultSet vars = 
        queryDatabase("SELECT VariableId, VariableType, DomainType, EnumDomain, IntDomainType, IntDomainUpperBound, IntDomainLowerBound FROM Variable WHERE PartialPlanId=".concat(partialPlanId.toString()).concat(" && VariableType IN('OBJECT_VAR', 'PARAMETER_VAR')"));
      boolean instantiateVar;
      while(vars.next()) {
        instantiateVar = false;
        if(vars.getString("DomainType").equals("IntervalDomain")) {
          instantiateVar =
            ! vars.getString("IntDomainLowerBound").equals
            ( vars.getString("IntDomainUpperBound"));
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
    catch (Exception excp) {
      excp.printStackTrace();
    }
    return retval;
  }


  synchronized private static boolean varDerivedDomainIsSingleton(final Blob info) {
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

  synchronized private static boolean varSpecDomainIsSingleton(final Blob info) {
    String temp;
    try {
      temp = new String(info.getBytes(1, (int) info.length()));
    }
    catch(SQLException sqle) {return false;}
    StringTokenizer strTok = new StringTokenizer(temp, ",");
    if (strTok.countTokens() != 7) {
//       System.err.println( "MySQLDB.varSpecDomainIsSingleton temp '" + temp + "'");
//       System.err.println( "   ... returning false");
      return false;
    }
    while(strTok.countTokens() != 2) {
      strTok.nextToken();
    }
    String type = strTok.nextToken();
    String domain = strTok.nextToken();
    return varDomainIsSingleton(type, domain);
  }

  synchronized private static boolean varDomainIsSingleton(final String type, String domain) {
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

  synchronized public static int [] queryPartialPlanSize(final Long partialPlanId) {
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

  synchronized public static Long queryPartialPlanId(final Long seqId, final int stepNum) {
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
  synchronized public static Long queryPartialPlanId(final Long seqId, final String stepName) {
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

  synchronized public static Map queryAllIdsForPartialPlan(final Long ppId) {
    OneToManyMap retval = new OneToManyMap();
    try {
      ResultSet ids = 
        queryDatabase("SELECT ObjectId FROM Object WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_OBJECT, new Integer(ids.getInt("ObjectId")));
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
  
  synchronized public static List queryTimelineIdsForObject(final Long ppId, final Integer objId) {
    List retval = new ArrayList();
    try {
      ResultSet ids = 
        queryDatabase("SELECT ChildObjectIds FROM Object WHERE PartialPlanId=".concat(ppId.toString()).concat(" && ObjectId=").concat(objId.toString()));
      ids.last();
      Blob blob = ids.getBlob("ComponentIds");
      String timelineIds = new String(blob.getBytes(1, (int) blob.length()));
      StringTokenizer strTok = new StringTokenizer(timelineIds, ",");
      while(strTok.hasMoreTokens()) {
        retval.add(Integer.valueOf(strTok.nextToken()));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List querySlotIdsForTimeline(final Long ppId, final Integer objId, 
                                                          final Integer tId) {
    List retval = new ArrayList();
    try {
      ResultSet ids =
        queryDatabase("SELECT DISTINCT SlotId FROM Token WHERE PartialPlanId=".concat(ppId.toString()).concat(" && ParentId=").concat(tId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("SlotId")));
      }
      ResultSet empties =
        queryDatabase("SELECT ExtraInfo FROM Object WHERE PartialPlanId=".concat(ppId.toString()).concat(" && ObjectId=").concat(tId.toString()));
      empties.last();
      Blob emptySlots = empties.getBlob("ExtraInfo");
      if(!empties.wasNull()) {
        String slotIds = new String(emptySlots.getBytes(1, (int) emptySlots.length()));
        StringTokenizer strTok = new StringTokenizer(slotIds, ",");
        while(strTok.hasMoreTokens()) {
          retval.add(Integer.valueOf(strTok.nextToken()));
        }
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTokenRelationIdsForToken(final Long ppId, 
                                                                final Integer tId) {
    List retval = new ArrayList();
    try {
      ResultSet ids =
        queryDatabase("SELECT TokenRelationId FROM TokenRelation WHERE PartialPlanId=".concat(ppId.toString()).concat(" && TokenBId=").concat(tId.toString()));

      while(ids.next()) {
        retval.add(new Integer(ids.getInt("TokenRelationId")));
      }
    }
    catch(SQLException sqle) {
    }
    return retval;
  }

  synchronized public static List queryTransactionIdsForPartialPlan(final Long seqId, 
                                                                    final Long ppId) {
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
  
//   synchronized public static List queryConstraintTransactionNames() {
//     return queryTransactionNames("'CONSTRAINT%'");
//   }

//   synchronized public static List queryTokenTransactionNames() {
//     return queryTransactionNames("'TOKEN%'");
//   }

//   synchronized public static List queryVariableTransactionNames() {
//     return queryTransactionNames("'VAR%'");
//   }

//   synchronized public static List queryTransactionNames(String type) {
//     List retval = new ArrayList();
//     try {
//       ResultSet names = 
//         queryDatabase("SELECT DISTINCT TransactionName FROM Transaction WHERE TransactionName LIKE " + type);
//       while(names.next()) {
//         retval.add(names.getString("TransactionName"));
//       }
//     }
//     catch(SQLException sqle) {
//     }
//     return retval;
//   }
	synchronized public static void queryTransactionNames(PwPlanningSequence seq, Set constrTrans, Set tokTrans, Set varTrans) {
		try {
			ResultSet names = queryDatabase("SELECT DISTINCT TransactionName FROM Transaction WHERE SequenceId=".concat(seq.getId().toString()));
			while(names.next()) {
				String name = names.getString("TransactionName");
				if(name.indexOf("CONSTRAINT_") != -1) {
					constrTrans.add(name);
				}
				else if(name.indexOf("TOKEN_") != -1) {
					tokTrans.add(name);
				}
				else if(name.indexOf("VARIABLE_") != -1) {
					varTrans.add(name);
				}
			}
		}
		catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		System.err.println("=====>File: " + seq.getUrl() + System.getProperty("file.separator") + "transactions");
		File transFile = new File(seq.getUrl() + System.getProperty("file.separator") + "transactions");
		if(!transFile.exists() || !transFile.canRead()) {
			return;
		}

		try {
			StreamTokenizer strTok = new StreamTokenizer(new BufferedReader(new FileReader(transFile)));
			strTok.wordChars('_', '_');
			while(strTok.nextToken() != StreamTokenizer.TT_EOF) {
				if(strTok.ttype == StreamTokenizer.TT_WORD && strTok.sval != null) {
					if(strTok.sval.indexOf("CONSTRAINT_") != -1) {
						constrTrans.add(strTok.sval);
					}
					else if(strTok.sval.indexOf("TOKEN_") != -1) {
						tokTrans.add(strTok.sval);
					}
					else if(strTok.sval.indexOf("VARIABLE_") != -1) {
						varTrans.add(strTok.sval);
					}
				}
			}
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

class ObjectIdComparator implements Comparator {
  public ObjectIdComparator() {
  }
  public boolean equals(Object o) {
    return false;
  }
  public int compare(Object o1, Object o2) {
    PwDBTransactionImpl t1 = (PwDBTransactionImpl) o1;
    PwDBTransactionImpl t2 = (PwDBTransactionImpl) o2;
    int cmp1 = t1.getPartialPlanId().compareTo(t2.getPartialPlanId());
    if(cmp1 == 0) {
      return t1.getObjectId().compareTo(t2.getObjectId());
    }
    return t1.getPartialPlanId().compareTo(t2.getPartialPlanId());
  }
}
