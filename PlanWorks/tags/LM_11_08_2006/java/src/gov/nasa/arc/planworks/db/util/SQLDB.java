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
import java.sql.ResultSetMetaData;
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
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.impl.PwConstraintImpl;
import gov.nasa.arc.planworks.db.impl.PwDecisionImpl;
import gov.nasa.arc.planworks.db.impl.PwDomainImpl;
//import gov.nasa.arc.planworks.db.impl.PwDBTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwEnumeratedDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwObjectImpl;
import gov.nasa.arc.planworks.db.impl.PwPartialPlanImpl;
import gov.nasa.arc.planworks.db.impl.PwPlanningSequenceImpl;
import gov.nasa.arc.planworks.db.impl.PwPredicateImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceInstantImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;
import gov.nasa.arc.planworks.db.impl.PwRuleInstanceImpl;
import gov.nasa.arc.planworks.db.impl.PwRuleImpl;
import gov.nasa.arc.planworks.db.impl.PwSlotImpl;
import gov.nasa.arc.planworks.db.impl.PwTimelineImpl;
import gov.nasa.arc.planworks.db.impl.PwTokenImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableImpl;
import gov.nasa.arc.planworks.db.impl.PwVariableQueryImpl;
import gov.nasa.arc.planworks.util.OneToManyMap;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.util.Utilities;

public abstract class SQLDB {
  private static SQLDB s_db;
  protected Connection conn;
  protected boolean dbIsStarted;
  protected boolean dbIsConnected;
  protected long queryTime;

  public SQLDB() {
    s_db = this;
    dbIsStarted = false;
    dbIsConnected = false;
    queryTime = 0;
  }

  synchronized public static void startDatabase() throws IllegalArgumentException, IOException, SecurityException {
    s_db._startDatabase();
  }
  abstract protected void _startDatabase() throws IllegalArgumentException, IOException, SecurityException;
   
  synchronized public static void stopDatabase() throws IllegalArgumentException, IOException, SecurityException {
    s_db._stopDatabase();
  }
  abstract protected void _stopDatabase() throws IllegalArgumentException, IOException, SecurityException;

  synchronized public static void registerDatabase() {s_db._registerDatabase();}
  abstract protected void _registerDatabase();

  synchronized public static void unregisterDatabase() {s_db._unregisterDatabase();}
  abstract protected void _unregisterDatabase();

  synchronized public static ResultSet queryDatabase(final String query) {
    //System.err.println("Executing query '" + query + "'");
    Statement stmt = null;
    ResultSet result = null;
    try {
      if(!s_db.dbIsConnected || s_db.conn.isClosed()) {
        registerDatabase();
      }
      long t1 = System.currentTimeMillis();
      stmt = s_db.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      result = stmt.executeQuery(query);
      //System.err.println("Query took " + (System.currentTimeMillis() - t1) + "ms");
      s_db.queryTime += System.currentTimeMillis() - t1;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
      System.exit(-1);
    }
    return result;
  }
  //abstract protected ResultSet _queryDatabase(final String query);

  synchronized public static int updateDatabase(final String update) {
    //System.err.println("Executing update '" + update + "'");
    Statement stmt = null;
    int result = -1;
    try {
      if(!s_db.dbIsConnected || s_db.conn.isClosed()) {
        s_db.registerDatabase();
      }
      long t1 = System.currentTimeMillis();
      stmt = s_db.conn.createStatement();
      result = stmt.executeUpdate(update);
      //System.err.println("Update took " + (System.currentTimeMillis() - t1) + "ms");
      s_db.queryTime += System.currentTimeMillis() - t1;
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
      return -1;
    }
    return result;
  }

  //abstract protected int _updateDatabase(final String update);

  synchronized public static void analyzeDatabase() {s_db._analyzeDatabase();}
  abstract protected void _analyzeDatabase();

  synchronized public static void cleanDatabase() {
    for(int i = 0; i < DbConstants.PW_DB_TABLES.length; i++)
      updateDatabase("DELETE FROM " + DbConstants.PW_DB_TABLES[i]);
  }

  //abstract protected void _cleanDatabase();

  synchronized public static void loadFile(final String file, final String tableName) {s_db._loadFile(file, tableName);}
  abstract protected void _loadFile(final String file, final String tableName);

  synchronized public static void loadFile(final String file, final String tableName, 
                                           final String columnSeparator, 
                                           final String entrySeparator) {s_db._loadFile(file, tableName, columnSeparator, entrySeparator);}
  abstract protected void _loadFile(final String file, final String tableName, 
                                        final String columnSeparator, 
                                        final String entrySeparator);
  
  synchronized public static String queryPartialPlanModelById(final Long partialPlanId){
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
  //abstract protected String _queryPartialPlanModelById(final Long partialPlanId);

  synchronized public static List getProjectNames() {
    ArrayList retval = new ArrayList();
    try {
      ResultSet names = queryDatabase("SELECT ProjectName FROM Project");
      while(names.next()) {
        retval.add(names.getString("ProjectName"));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _getProjectNames();

  synchronized public static boolean projectExists(final String name) {
    try {
      ResultSet rows = 
        queryDatabase("SELECT ProjectId FROM Project WHERE ProjectName='".concat(name).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return false;
  }
  //abstract protected boolean _projectExists(final String name);

  synchronized public static boolean sequenceExists(final String url) {
    try {
      ResultSet rows =
        queryDatabase("SELECT * FROM Sequence WHERE SequenceURL='".concat(url).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){
      sqle.printStackTrace();
    }
    return false;
  }
  //abstract protected boolean _sequenceExists(final String url);

  synchronized public static boolean sequenceExists(final Long id) { 
    try {
      ResultSet rows =
        queryDatabase("SELECT * FROM Sequence WHERE SequenceId=".concat(id.toString()));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){
      sqle.printStackTrace();
    }
    return false;
  }
  //abstract protected boolean _sequenceExists(final Long id);

  synchronized public static String getSequenceUrl(final Long id) {
    String retval = null;
    try {
      ResultSet rows = 
        queryDatabase("SELECT SequenceURL FROM Sequence WHERE SequenceId=".concat(id.toString()));
      rows.last();
      retval = rows.getString("SequenceURL");
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected String _getSequenceUrl(final Long id);

  synchronized public static Long getSequenceId(final String url) { 
		Long retval = null;
		try {
			ResultSet rows = queryDatabase("SELECT SequenceId FROM Sequence WHERE SequenceURL='".concat(url).concat("'"));
			rows.last();
			retval = new Long(rows.getLong("SequenceId"));
		}
		catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		return retval;
  }
  //abstract protected Long _getSequenceId(final String url);

  synchronized public static void setSequenceUrl(final Long id, final String url) { 
      updateDatabase("UPDATE Sequence SET SequenceURL='".concat(url).concat("' WHERE SequenceId=").concat(id.toString()));
  }
  //abstract protected void _setSequenceUrl(final Long id, final String url);

  synchronized public static boolean partialPlanExists(final Long sequenceId, final String name) { 
    try {
      ResultSet rows =
        queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()).concat(" AND PlanName='").concat(name).concat("'"));
      rows.last();
      return rows.getRow() != 0;
    }
    catch(SQLException sqle){
      sqle.printStackTrace();
    }
    return false;
  }
  //abstract protected boolean _partialPlanExists(final Long sequenceId, final String name);

  synchronized public static boolean transactionsInDatabase(final Long seqId) {
    try {
      ResultSet rows =
        queryDatabase("SELECT COUNT(DISTINCT TransactionId) as Result FROM Transaction WHERE SequenceId=".concat(seqId.toString()));
      rows.last();
      return rows.getInt("Result") > 0;
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return false;
  }
  //abstract protected boolean _transactionsInDatabase(final Long seqId);

  synchronized public static List queryPlanNamesInDatabase(final Long sequenceId) {
    List retval = new ArrayList();
    try {
      ResultSet rows = 
        queryDatabase("SELECT PlanName from PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      while(rows.next()) {
        retval.add(rows.getString("PlanName"));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryPlanNamesInDatabase(final Long sequenceId);

  synchronized public static List queryPlanIdsInDatabase(final Long seqId) {
    List retval = new ArrayList();
    try {
      ResultSet rows =
        queryDatabase("SELECT PartialPlanId from PartialPlan WHERE SequenceId=".concat(seqId.toString()));
      while(rows.next())
        retval.add(new Long(rows.getLong("PartialPlanId")));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryPlanIdsInDatabase(final Long seqId);

  synchronized public static void addProject(final String name) {
    updateDatabase("INSERT INTO Project (ProjectName) VALUES ('".concat(name).concat("')"));
  }
  //abstract protected void _addProject(final String name);

  synchronized public static Integer latestProjectId() {
    try {
      //LAST_INSERT_ID
      ResultSet newId = queryDatabase("SELECT MAX(ProjectId) AS ProjectId from Project");
      newId.last();
      return new Integer(newId.getInt("ProjectId"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return null;
  }
  //abstract protected Integer _latestProjectId();

  synchronized public static Long addSequence(final String url, final Integer projectId) {
    loadFile(url + System.getProperty("file.separator") + "sequence", "Sequence",
             DbConstants.SEQ_COL_SEP_HEX, DbConstants.SEQ_LINE_SEP_HEX);
    Long latestSequenceId = latestSequenceId();
    //System.err.println(projectId.toString());
    updateDatabase("UPDATE Sequence SET ProjectId=".concat(projectId.toString()).concat(" WHERE SequenceId=").concat(latestSequenceId.toString()));
    return latestSequenceId;
  }
  //abstract protected Long _addSequence(final String url, final Integer projectId);

  synchronized public static Long latestSequenceId() {
    try {
      ResultSet newId = 
        queryDatabase("SELECT SequenceId FROM Sequence ORDER BY SequenceOrdering");
      newId.next(); //added to make HSQL shut up
//       newId.last();
      //modified because not everybody is cool
      while(!newId.isLast())
        newId.next();
      //System.err.println("Got id " + newId.getLong("SequenceId"));
      return new Long(newId.getLong("SequenceId"));
    }
    catch(SQLException sqle) {
			sqle.printStackTrace();
    }
    return null;
  }
  //abstract protected Long _latestSequenceId();

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
      sqle.printStackTrace();
    }
    return null;
  }
  //abstract protected Integer _getProjectIdByName(final String name);

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
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Map _getSequences(final Integer projectId);

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
      sqle.printStackTrace();
    }
  }
  //abstract protected void _deleteProject(final Integer id) throws ResourceNotFoundException;

  synchronized public static void deletePlanningSequence(final Long sequenceId) throws ResourceNotFoundException{
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
        updateDatabase("DELETE FROM RuleInstance".concat(whereClause.toString()));
        updateDatabase("DELETE FROM RuleInstanceSlaveMap".concat(whereClause.toString()));
        updateDatabase("DELETE FROM ConstraintVarMap".concat(whereClause.toString()));
        updateDatabase("DELETE FROM ResourceInstants".concat(whereClause.toString()));
				updateDatabase("DELETE FROM Decision".concat(whereClause.toString()));
        updateDatabase("DELETE FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()));
      }
      updateDatabase("DELETE FROM PartialPlanStats WHERE SequenceId=".concat(sequenceId.toString()));
      updateDatabase("DELETE FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      analyzeDatabase();
    }
    catch(SQLException sqle){
      sqle.printStackTrace();
    }
  }
  //abstract protected void _deletePlanningSequence(final Long sequenceId) throws ResourceNotFoundException;

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
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _getPlanNamesInSequence(final Long sequenceId);

  synchronized public static Long getNewPartialPlanId(final Long sequenceId, final String name) {
    Long retval = null;
    try {
      ResultSet partialPlan = 
        queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(sequenceId.toString()).concat(" AND PlanName='").concat(name).concat("'"));
      //partialPlan.last();
      partialPlan.last();
      retval = new Long(partialPlan.getLong("PartialPlanId"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Long _getNewPartialPlanId(final Long sequenceId, final String name);

  synchronized public static Long getPartialPlanIdByName(final Long sequenceId, final String name) {
    Long retval = null;
    try {
      StringBuffer temp = 
        new StringBuffer("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=");
      temp.append(sequenceId.toString()).append(" AND PlanName='").append(name).append("'");
      ResultSet partialPlan =
        queryDatabase(temp.toString());
      boolean valid = partialPlan.last();
      if(!valid)
        return null;
      retval = new Long(partialPlan.getLong("PartialPlanId"));
      if(partialPlan.wasNull())
        return null;
    }
    catch(SQLException sqle) {
      //sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Long _getPartialPlanIdByName(final Long sequenceId, final String name);

  synchronized public static Long getPartialPlanIdByStepNum(final Long sequenceId, final int stepNum) {
    Long retval = null;
    try {
      StringBuffer temp =
        new StringBuffer("SELECT PartialPlanId FROM PartialPlanStats WHERE SequenceId=");
      temp.append(sequenceId.toString()).append(" AND StepNum=").append(stepNum);
      ResultSet ppId = queryDatabase(temp.toString());
      ppId.last();
      retval = new Long(ppId.getLong("PartialPlanId"));
      if(ppId.wasNull()) {
        return null;
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Long _getPartialPlanIdByStepNum(final Long sequenceId, final int stepNum);

  synchronized public static String getPartialPlanNameById(final Long seqId, final Long ppId) {
    String retval = null;
    try {
      ResultSet name = queryDatabase("SELECT StepNum FROM PartialPlanStats WHERE SequenceId=".concat(seqId.toString()).concat(" AND PartialPlanId=").concat(ppId.toString()));
      if(name.last())
        retval = "step" + name.getInt("StepNum");
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected String _getPartialPlanNameById(final Long seqId, final Long ppId);

  synchronized public static List queryPartialPlanNames(final Long sequenceId) {
    List retval = new ArrayList();
    try {
      ResultSet stepNums = queryDatabase("SELECT StepNum FROM PartialPlanStats WHERE SequenceId=".concat(sequenceId.toString()).concat(" ORDER BY PartialPlanId"));
      while(stepNums.next()) {
        retval.add("step" + stepNums.getInt("StepNum"));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryPartialPlanNames(final Long sequenceId);

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
      sqle.printStackTrace();
    }
  }
  //abstract protected void _createObjects(PwPartialPlanImpl partialPlan);

  synchronized public static int countTransactions(final Long sequenceId) {
    int retval = 0;
    try {
      ResultSet transactions = 
        queryDatabase("SELECT COUNT(*) FROM Transaction WHERE SequenceId=".concat(sequenceId.toString()));
      transactions.last();
      retval = transactions.getInt("COUNT(*)");
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected int _countTransactions(final Long sequenceId);

  //   synchronized public static Map queryTransactions(final Long sequenceId) {}
  synchronized public static void createSlotTokenNodesStructure(PwPartialPlanImpl partialPlan, final Long seqId) {
    boolean printTime = true;
    // boolean printTime = false;
    try {
      if (s_db.dbIsConnected) {
        unregisterDatabase();
      }
      registerDatabase();
      long t1 = 0L, t2 = 0L, t3 = 0L, t4 = 0L, t5 = 0L;
      if (printTime) {
        t1 = System.currentTimeMillis();
      }
      StringBuffer queryStr = new StringBuffer("SELECT Token.TokenId, Token.TokenType, Token.SlotId, Token.SlotIndex, Token.IsValueToken, Token.StartVarId, Token.EndVarId, Token.StateVarId, Token.DurationVarId, Token.ObjectVarId, Token.PredicateName, Token.ParamVarIds, Token.ExtraData, Token.ParentId, RuleInstanceSlaveMap.RuleInstanceId FROM Token LEFT JOIN RuleInstanceSlaveMap ON " );
      queryStr.append("RuleInstanceSlaveMap.PartialPlanId=Token.PartialPlanId");
      queryStr.append(" AND Token.TokenId  = RuleInstanceSlaveMap.SlaveTokenId");
      queryStr.append(" WHERE Token.PartialPlanId=").append(partialPlan.getId().toString());
      queryStr.append(" AND Token.IsFreeToken=0 ORDER BY Token.ParentId, Token.SlotIndex, Token.TokenId");
      ResultSet tokens = queryDatabase(queryStr.toString());
      if (printTime) {
        t2 = System.currentTimeMillis();
        System.err.println( "   ... queryDatabase slotted tokens elapsed time: " +
                            (t2 - t1) + " msecs.");
      }
      while(tokens.next()) {
        //Integer tokenId = new Integer(tokens.getInt("Token.TokenId"));
        Integer tokenId = new Integer(tokens.getInt("TokenId"));
        boolean isFreeToken = false;
        //boolean isValueToken = tokens.getBoolean("Token.IsValueToken");
        boolean isValueToken = tokens.getBoolean("IsValueToken");
        //Integer startVarId = new Integer(tokens.getInt("Token.StartVarId"));
        Integer startVarId = new Integer(tokens.getInt("StartVarId"));
        //Integer endVarId = new Integer(tokens.getInt("Token.EndVarId"));
        Integer endVarId = new Integer(tokens.getInt("EndVarId"));
        //Integer stateVarId = new Integer(tokens.getInt("Token.StateVarId"));
        Integer stateVarId = new Integer(tokens.getInt("StateVarId"));
        //Integer durationVarId = new Integer(tokens.getInt("Token.DurationVarId"));
        Integer durationVarId = new Integer(tokens.getInt("DurationVarId"));
        //Integer objectVarId = new Integer(tokens.getInt("Token.ObjectVarId"));
        Integer objectVarId = new Integer(tokens.getInt("ObjectVarId"));
        //Integer parentId = new Integer(tokens.getInt("Token.ParentId"));
        Integer parentId = new Integer(tokens.getInt("ParentId"));
        if(tokens.wasNull())
          parentId = DbConstants.NO_ID;
        //Integer ruleInstanceId = new Integer(tokens.getInt("RuleInstanceSlaveMap.RuleInstanceId"));
        Integer ruleInstanceId = new Integer(tokens.getInt("RuleInstanceId"));
        //String predName = tokens.getString("Token.PredicateName");
        String predName = tokens.getString("PredicateName");
        String paramVarIds = null;
        //Blob blob = tokens.getBlob("Token.ParamVarIds");
        Blob blob = tokens.getBlob("ParamVarIds");
        if(!tokens.wasNull()) {
          paramVarIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String extraInfo = null;
        //blob = tokens.getBlob("Token.ExtraData");
        blob = tokens.getBlob("ExtraData");
        if(!tokens.wasNull()) {
          extraInfo = new String(blob.getBytes(1, (int) blob.length()));
        }

        PwTokenImpl t = null;
        //if(tokens.getInt("Token.TokenType") == DbConstants.T_TRANSACTION) {
        if(tokens.getInt("TokenType") == DbConstants.T_TRANSACTION) {
          t = new PwResourceTransactionImpl(tokenId, isValueToken, predName, startVarId, endVarId,
                                            durationVarId, stateVarId, objectVarId, parentId, 
                                            ruleInstanceId, paramVarIds, extraInfo, partialPlan);
        }
        //else if(tokens.getInt("Token.TokenType") == DbConstants.T_INTERVAL) {
        else if(tokens.getInt("TokenType") == DbConstants.T_INTERVAL) {
          Integer slotId = new Integer(tokens.getInt("SlotId"));
          if(tokens.wasNull())
            slotId = DbConstants.NO_ID;
          t = new PwTokenImpl(tokenId, isValueToken, slotId, //new Integer(tokens.getInt("Token.SlotId")),
                              predName, startVarId, endVarId, durationVarId, stateVarId, 
                              objectVarId, parentId, ruleInstanceId, paramVarIds, extraInfo, 
                              partialPlan);
        }
      }
      if (printTime) {
        t3 = System.currentTimeMillis();
        System.err.println( "   ... create slotted tokens elapsed time: " + (t3 - t2) + " msecs.");
      }
      unregisterDatabase();
      registerDatabase();
      ResultSet freeTokens = queryDatabase("Select Token.TokenId, Token.TokenType, Token.IsValueToken, Token.ObjectVarId, Token.StartVarId, Token.EndVarId, Token.DurationVarId, Token.StateVarId, Token.PredicateName, Token.ParamVarIds, Token.ExtraData, RuleInstanceSlaveMap.RuleInstanceId FROM Token LEFT JOIN RuleInstanceSlaveMap ON RuleInstanceSlaveMap.PartialPlanId=Token.PartialPlanId AND Token.TokenId = RuleInstanceSlaveMap.SlaveTokenId WHERE Token.IsFreeToken=1 AND Token.PartialPlanId=".concat(partialPlan.getId().toString()));
      if (printTime) {
        t4 = System.currentTimeMillis();
        System.err.println( "   ... queryDatabase free tokens elapsed time: " +
                            (t4 - t3) + " msecs.");
      }
      while(freeTokens.next()) {
        //Integer tokenId = new Integer(freeTokens.getInt("Token.TokenId"));
        Integer tokenId = new Integer(freeTokens.getInt("TokenId"));
        boolean isFreeToken = true;
        //boolean isValueToken = freeTokens.getBoolean("Token.IsValueToken");
        boolean isValueToken = freeTokens.getBoolean("IsValueToken");
        //Integer startVarId = new Integer(freeTokens.getInt("Token.StartVarId"));
        Integer startVarId = new Integer(freeTokens.getInt("StartVarId"));
        //Integer endVarId = new Integer(freeTokens.getInt("Token.EndVarId"));
        Integer endVarId = new Integer(freeTokens.getInt("EndVarId"));
        //Integer stateVarId = new Integer(freeTokens.getInt("Token.StateVarId"));
        Integer stateVarId = new Integer(freeTokens.getInt("StateVarId"));
        //Integer durationVarId = new Integer(freeTokens.getInt("Token.DurationVarId"));
        Integer durationVarId = new Integer(freeTokens.getInt("DurationVarId"));
        //Integer objectVarId = new Integer(freeTokens.getInt("Token.ObjectVarId"));
        Integer objectVarId = new Integer(freeTokens.getInt("ObjectVarId"));
        //Integer ruleInstanceId = new Integer(freeTokens.getInt("RuleInstanceSlaveMap.RuleInstanceId"));
        Integer ruleInstanceId = new Integer(freeTokens.getInt("RuleInstanceId"));
        //String predName = freeTokens.getString("Token.PredicateName");
        String predName = freeTokens.getString("PredicateName");
        String paramVarIds = null;
        //Blob blob = freeTokens.getBlob("Token.ParamVarIds");
        Blob blob = freeTokens.getBlob("ParamVarIds");
        if(!freeTokens.wasNull()) {
          paramVarIds = new String(blob.getBytes(1, (int) blob.length()));
        }
        String extraInfo = null;
        //blob = freeTokens.getBlob("Token.ExtraData");
        blob = freeTokens.getBlob("ExtraData");
        if(!freeTokens.wasNull()) {
          extraInfo = new String(blob.getBytes(1, (int) blob.length()));
        }
        PwTokenImpl t = null;
        //if(freeTokens.getInt("Token.TokenType") == DbConstants.T_TRANSACTION) {
        if(freeTokens.getInt("TokenType") == DbConstants.T_TRANSACTION) {
          t = new PwResourceTransactionImpl(tokenId, isValueToken, predName, startVarId, endVarId,
                                            durationVarId, stateVarId, objectVarId,
                                            DbConstants.NO_ID, ruleInstanceId, paramVarIds, extraInfo,
                                            partialPlan);
        }
        //else if(freeTokens.getInt("Token.TokenType") == DbConstants.T_INTERVAL) {
        else if(freeTokens.getInt("TokenType") == DbConstants.T_INTERVAL) {
          t = new PwTokenImpl(tokenId, isValueToken, DbConstants.NO_ID, predName, startVarId, 
                              endVarId, durationVarId, stateVarId, objectVarId, DbConstants.NO_ID,
                              ruleInstanceId, paramVarIds, extraInfo, partialPlan);
        }
      }
      if (printTime) {
        t5 = System.currentTimeMillis();
        System.err.println( "   ... create free tokens elapsed time: " + (t5 - t4) + " msecs.");
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
    unregisterDatabase();
  }
  //abstract protected void _createSlotTokenNodesStructure(PwPartialPlanImpl partialPlan, final Long seqId);

  synchronized public static void queryConstraints(PwPartialPlanImpl partialPlan) {
    s_db._queryConstraints(partialPlan);
//     try {
//        ResultSet constraints = 
//         queryDatabase("SELECT VConstraint.ConstraintId, VConstraint.ConstraintName, VConstraint.ConstraintType, ConstraintVarMap.VariableId FROM VConstraint LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=VConstraint.PartialPlanId AND ConstraintVarMap.ConstraintId=VConstraint.ConstraintId WHERE VConstraint.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" ORDER BY VConstraint.ConstraintId"));
//       PwConstraintImpl constraint = null;
//       while(constraints.next()) {
//         Integer constraintId = new Integer(constraints.getInt("VConstraint.ConstraintId"));
        
//         if(constraint == null || !constraint.getId().equals(constraintId)) {
//           constraint = new PwConstraintImpl(constraints.getString("VConstraint.ConstraintName"),
//                                             constraintId,
//                                             constraints.getString("VConstraint.ConstraintType"),
//                                             partialPlan);
//           partialPlan.addConstraint(constraintId, constraint);
//         }
//         Integer variableId = new Integer(constraints.getInt("ConstraintVarMap.VariableId"));
//         if(!constraints.wasNull()) {
//           constraint.addVariable(variableId);
//         }
//       }
//     }
//     catch(SQLException sqle) {
//       System.err.println(sqle);
//       sqle.printStackTrace();
//     }
  }
  abstract protected void _queryConstraints(PwPartialPlanImpl partialPlan);

  synchronized public static void queryVariables(PwPartialPlanImpl partialPlan) {
    s_db._queryVariables(partialPlan);
//     try {
//       ResultSet variables =
//         queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.EnumDomain, Variable.IntDomainType, Variable.IntDomainLowerBound, Variable.IntDomainUpperBound, Variable.ParentId, Variable.ParameterName, ConstraintVarMap.ConstraintId FROM Variable LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId AND ConstraintVarMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getId().toString()));
//       PwVariableImpl variable = null;
//       while(variables.next()) {
//         Integer variableId = new Integer(variables.getInt("Variable.VariableId"));
//         if(variable == null || !variable.getId().equals(variableId)) {
//           PwDomainImpl domain = null;
//           String domainType = variables.getString("Variable.DomainType");
//           if(domainType.equals("EnumeratedDomain")) {
//             Blob blob = variables.getBlob("Variable.EnumDomain");
//             domain = new PwEnumeratedDomainImpl(new String(blob.getBytes(1, (int) blob.length())));
//           }
//           else if(domainType.equals("IntervalDomain")) {
//             domain = 
//               new PwIntervalDomainImpl(variables.getString("Variable.IntDomainType"),
//                                        variables.getString("Variable.IntDomainLowerBound"),
//                                        variables.getString("Variable.IntDomainUpperBound"));
//           }
//           variable = new PwVariableImpl(variableId, variables.getString("Variable.VariableType"),
//                                         new Integer(variables.getInt("Variable.ParentId")), domain,
//                                         partialPlan);
//           partialPlan.addVariable(variableId, variable);

//           String parameterName = variables.getString("Variable.ParameterName");
//           if(!variables.wasNull()) {
//             variable.addParameter(parameterName);
//           }
//         }
//         Integer constraintId = new Integer(variables.getInt("ConstraintVarMap.ConstraintId"));
//         if(!variables.wasNull()) {
//           variable.addConstraint(constraintId);
//         }
//       }
//     }
//     catch(SQLException sqle) {
//       System.err.println(sqle);
//       sqle.printStackTrace();
//     }
  }
  abstract protected void _queryVariables(PwPartialPlanImpl partialPlan);

  synchronized public static void queryRuleInstances(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet ruleInstances = 
        queryDatabase("SELECT RuleInstanceId, RuleId, MasterTokenId, SlaveTokenIds, RuleVarIds FROM RuleInstance WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(ruleInstances.next()) {
        Integer id = new Integer(ruleInstances.getInt("RuleInstanceId"));
        //System.err.println( "MySQLDB:RuleInstnceId " + id);
        partialPlan.addRuleInstance(id, 
                                     new PwRuleInstanceImpl(id,
                                                            new Integer(ruleInstances.getInt("RuleId")),
                                                            new Integer(ruleInstances.getInt("MasterTokenId")),
                                                            ruleInstances.getString("SlaveTokenIds"), 
                                                            ruleInstances.getString("RuleVarIds"), 
                                                            partialPlan));
      }
    }
    catch(SQLException sqle) {
      System.err.println(sqle);
      sqle.printStackTrace();
    }
  }
  //abstract protected void _queryRuleInstances(PwPartialPlanImpl partialPlan);

  synchronized public static void queryResourceInstants(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet resInsts = 
        queryDatabase("SELECT InstantId, ResourceId, TimePoint, LevelMin, LevelMax, Transactions FROM ResourceInstants WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
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
                                                       new Integer(resInsts.getInt("ResourceId")),
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
  //abstract protected void _queryResourceInstants(PwPartialPlanImpl partialPlan);

  synchronized public static Map queryRules(final Long sequenceId, final String modelRuleDelimiters) {
    Map retval = new HashMap();
    try {
      ResultSet ruleTextInDb = queryDatabase("SELECT RulesText FROM Sequence WHERE SequenceId=".concat(sequenceId.toString()));
      if(!ruleTextInDb.first())
        return retval;
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

      // char startDelim = System.getProperty("model.rule.delimiter").charAt(0);
      // char endDelim = System.getProperty("model.rule.delimiter").charAt(1);
      char startDelim = modelRuleDelimiters.charAt(0);
      char endDelim = modelRuleDelimiters.charAt(1);

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
        Integer id = new Integer(rules.getInt("RuleId"));
        if(ruleText == null || startIndex > ruleText.size()) {
          retval.put(id, new PwRuleImpl(new Long(rules.getLong("SequenceId")), id, ""));
          continue;
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
          if(Utilities.countOccurrences(startDelim, textArg) != 
             Utilities.countOccurrences(endDelim, textArg)) {
            throw new IllegalStateException("Close/end delims for rule don't match");
          }
          retval.put(id, new PwRuleImpl(new Long(rules.getLong("SequenceId")), id, textArg));
        }
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Map _queryRules(final Long sequenceId, final String modelRuleDelimiters);


  synchronized public static List queryOpenDecisionsForStep(final Long ppId, final PwPartialPlan partialPlan) {
    List retval = new ArrayList();
    try {
      ResultSet decs = 
        queryDatabase("SELECT Decision.DecisionId, Decision.DecisionType, Decision.EntityId, Decision.IsUnit, Decision.Choices FROM Decision WHERE Decision.PartialPlanId=".concat(ppId.toString()));
      while(decs.next()) {
        PwDecisionImpl impl = new PwDecisionImpl(new Integer(decs.getInt("DecisionId")),
                                                 decs.getInt("DecisionType"),
                                                 new Integer(decs.getInt("EntityId")),
                                                 decs.getBoolean("IsUnit"), partialPlan);
        Blob blob = decs.getBlob("Decision.Choices");
        if(!decs.wasNull()) {
          impl.makeChoices(new String(blob.getBytes(1, (int) blob.length())));
        }
        retval.add(impl);
        // System.err.println(impl.toOutputString());
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryOpenDecisionsForStep(final Long ppId, final PwPartialPlan partialPlan);

  synchronized public static Integer queryCurrentDecisionIdForStep(final Long ppId) {
    Integer retval = new Integer(-1);
    try {
      ResultSet transactions = queryDatabase("SELECT ObjectId from Transaction WHERE PartialPlanId=".concat(ppId.toString()).concat(" AND TransactionName REGEXP '^(ASSIGN|RETRACT).+DECISION_(SUCCEEDED|FAILED)$'"));
      if(transactions.last()) {
        retval = new Integer(transactions.getInt("ObjectId"));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Integer _queryCurrentDecisionIdForStep(final Long ppId);

  //   synchronized public static List queryTransactionsForStep(final Long seqId, final Long ppId) {}
  //   synchronized public static List queryTransactionsForConstraint(final Long sequenceId, 
  //   synchronized public static List queryTransactionsForToken(final Long sequenceId, 
  //   synchronized public static List queryTransactionsForVariable(final Long sequenceId, 
  //   synchronized public static List queryStepsWithTokenTransaction(final Long sequenceId, 
  //   synchronized public static List queryStepsWithVariableTransaction(final Long sequenceId, 
  //   synchronized public static List queryStepsWithConstraintTransaction(final Long sequenceId, 
  //   synchronized public static List queryStepsWithTokenTransaction(final Long sequenceId, 
  //   synchronized public static List queryStepsWithVariableTransaction(final Long sequenceId, 
  //   synchronized public static List queryStepsWithConstraintTransaction(final Long sequenceId, 
  //   synchronized public static List queryStepsWithRestrictions(final Long sequenceId) {}
  //   synchronized public static List queryStepsWithRelaxations(final Long sequenceId) {}
  //   synchronized public static List queryStepsWithUnitDecisions(final PwPlanningSequenceImpl seq) {}
  //   synchronized public static List queryStepsWithNonUnitDecisions(final PwPlanningSequenceImpl seq) {}
  synchronized public static List queryFreeTokensAtStep( final int stepNum, final PwPlanningSequenceImpl seq) {
    // return list of lists of TokenId, PartialPlanId, StepNum, PredicateName
    // currently does all steps
    List retval = new ArrayList();
    try {
      Long partialPlanId = getPartialPlanIdByStepNum(seq.getId(), stepNum);
      String queryString = "SELECT Token.TokenId, Token.PredicateName, Token.PartialPlanId FROM Token WHERE Token.IsFreeToken=1 AND Token.PartialPlanId=".concat(partialPlanId.toString());

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
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryFreeTokensAtStep( final int stepNum, final PwPlanningSequenceImpl seq);

  synchronized public static List queryUnboundVariablesAtStep( final int stepNum, final PwPlanningSequenceImpl seq) {
    List retval = new ArrayList();
    try {
      Long partialPlanId = getPartialPlanIdByStepNum(seq.getId(), stepNum);
      ResultSet vars = 
        queryDatabase("SELECT VariableId, VariableType, DomainType, EnumDomain, IntDomainType, IntDomainUpperBound, IntDomainLowerBound, ParentId FROM Variable WHERE PartialPlanId=".concat(partialPlanId.toString()).concat(" AND VariableType IN('OBJECT_VAR', 'PARAMETER_VAR')"));
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
                                             vars.getString("VariableType"),
                                             new Integer(vars.getInt("ParentId")),
                                             new Integer(stepNum),
                                             seq.getId(), partialPlanId, true));
        }
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    catch (Exception excp) {
      excp.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryUnboundVariablesAtStep( final int stepNum, final PwPlanningSequenceImpl seq);

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
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryPartialPlanSizes(Long sequenceId);

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
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected int [] _queryPartialPlanSize(final Long partialPlanId);

  synchronized public static Long queryPartialPlanId(final Long seqId, final int stepNum) {
    Long retval = null;
    try {
      ResultSet ppId = queryDatabase("SELECT PartialPlanId FROM PartialPlanStats WHERE SequenceId=".concat(seqId.toString()).concat(" AND StepNum=").concat(Integer.toString(stepNum)));
      ppId.last();
      retval = new Long(ppId.getLong("PartialPlanId"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Long _queryPartialPlanId(final Long seqId, final int stepNum);

  synchronized public static Long queryPartialPlanId(final Long seqId, final String stepName) {
    Long retval = null;
    try {
      ResultSet ppId = queryDatabase("SELECT PartialPlanId FROM PartialPlan WHERE SequenceId=".concat(seqId.toString()).concat(" AND PlanName='").concat(stepName).concat("'"));
      ppId.last();
      retval = new Long(ppId.getLong("PartialPlanId"));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Long _queryPartialPlanId(final Long seqId, final String stepName);

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
      ids = queryDatabase("SELECT RuleInstanceId FROM RuleInstance WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_RULE_INSTANCE, new Integer(ids.getInt("RuleInstanceId")));
      }
      ids = queryDatabase("SELECT VariableId FROM Variable WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_VARIABLE, new Integer(ids.getInt("VariableId")));
      }
      ids = queryDatabase("SELECT InstantId FROM ResourceInstants WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_INSTANTS, new Integer(ids.getInt("InstantId")));
      }
      ids = queryDatabase("SELECT ConstraintId FROM VConstraint WHERE PartialPlanId=".concat(ppId.toString()));
      while(ids.next()) {
        retval.put(DbConstants.TBL_CONSTRAINT, new Integer(ids.getInt("ConstraintId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Map _queryAllIdsForPartialPlan(final Long ppId);

  synchronized public static List queryRuleIdsForSequence(final Long seqId) {
    List retval = new ArrayList();
    try {
      ResultSet ids = 
        queryDatabase("SELECT RuleId FROM Rules WHERE SequenceId=".concat(seqId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("RuleId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryRuleIdsForSequence(final Long seqId);

  synchronized public static List queryResourceIdsForObject(final Long ppId, final Integer objId) {
    List retval = new ArrayList();
    Integer resourceObjectType = new Integer(DbConstants.O_RESOURCE);
    try {
      ResultSet ids = 
        queryDatabase("SELECT ObjectId FROM Object WHERE PartialPlanId=".concat(ppId.toString()).concat(" AND ParentId=").concat(objId.toString()).concat(" && ObjectType=").concat(resourceObjectType.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("ObjectId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryResourceIdsForObject(final Long ppId, final Integer objId);

  synchronized public static List queryTimelineIdsForObject(final Long ppId, final Integer objId) {
    List retval = new ArrayList();
    Integer timelineObjectType = new Integer(DbConstants.O_TIMELINE);
    try {
      ResultSet ids = 
        queryDatabase("SELECT ObjectId FROM Object WHERE PartialPlanId=".concat(ppId.toString()).concat(" AND ParentId=").concat(objId.toString()).concat(" && ObjectType=").concat(timelineObjectType.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("ObjectId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryTimelineIdsForObject(final Long ppId, final Integer objId);

  synchronized public static List querySlotIdsForTimeline(final Long ppId, final Integer objId, final Integer tId) {
    List retval = new ArrayList();
    try {
      ResultSet ids =
        queryDatabase("SELECT DISTINCT SlotId FROM Token WHERE PartialPlanId=".concat(ppId.toString()).concat(" AND ParentId=").concat(tId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("SlotId")));
      }
      ResultSet empties =
        queryDatabase("SELECT ExtraInfo FROM Object WHERE PartialPlanId=".concat(ppId.toString()).concat(" AND ObjectId=").concat(tId.toString()));
      empties.last();
      Blob emptySlots = empties.getBlob("ExtraInfo");
      if(!empties.wasNull()) {
        String slotIds = new String(emptySlots.getBytes(1, (int) emptySlots.length()));
        StringTokenizer strTok = new StringTokenizer(slotIds, ",");
        while(strTok.hasMoreTokens()) {
          //format is SlotId, SlotIndex 
          retval.add(Integer.valueOf(strTok.nextToken()));
          strTok.nextToken(); //skip slot index
        }
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  // abstract protected List _querySlotIdsForTimeline(final Long ppId, final Integer objId, 
//                                                        final Integer tId);

  synchronized public static Map queryAllChildRuleInstanceIds(PwPartialPlanImpl partialPlan) {
    OneToManyMap retval = new OneToManyMap();
    try {
      ResultSet criIds = 
        queryDatabase("SELECT RuleInstanceId, MasterTokenId FROM RuleInstance WHERE PartialPlanId=".concat(partialPlan.getId().toString()));
      while(criIds.next()) {
        //System.err.println( "MasterTokenId " + criIds.getInt("MasterTokenId"));
        //System.err.println( "RuleInstanceId " + criIds.getInt("RuleInstanceId"));
        retval.put(new Integer(criIds.getInt("MasterTokenId")), 
                   new Integer(criIds.getInt("RuleInstanceId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Map _queryAllChildRuleInstanceIds(PwPartialPlanImpl partialPlan);

  //  synchronized public static List queryTokenRelationIdsForToken(final Long ppId, 
  synchronized public static List queryTransactionIdsForPartialPlan(final Long seqId, final Long ppId) {
    List retval = new ArrayList();
    try {
      ResultSet ids = queryDatabase("SELECT TransactionId FROM Transaction WHERE SequenceId=".concat(seqId.toString()).concat(" AND PartialPlanId=").concat(ppId.toString()));
      while(ids.next()) {
        retval.add(new Integer(ids.getInt("TransactionId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryTransactionIdsForPartialPlan(final Long seqId, final Long ppId);

  synchronized public static List queryTransactionNameList() {
		List retval = new ArrayList();
		try {
			ResultSet names = queryDatabase("SELECT DISTINCT TransactionName FROM Transaction");
			while(names.next()) {
				retval.add(names.getString("TransactionName"));
			}
		}
		catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		return retval;
  }
  //abstract protected List _queryTransactionNameList();

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
  //abstract protected void _queryTransactionNames(PwPlanningSequence seq, Set constrTrans, Set tokTrans, Set varTrans);

  synchronized public static List queryPartialPlanIds(Long seqId) {
    List retval = new ArrayList();
    try {
      ResultSet ppIds = 
        queryDatabase("SELECT PartialPlanId FROM PartialPlanStats WHERE SequenceId=".concat(seqId.toString()));
      while(ppIds.next()) {
        retval.add(new Long(ppIds.getLong("PartialPlanId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryPartialPlanIds(Long seqId);

  synchronized public static List queryPartialPlanIds(Long seqId, String comparison) {
    List retval = new ArrayList();
    try {
      ResultSet ppIds =
        queryDatabase("SELECT PartialPlanId FROM PartialPlanStats WHERE SequenceId=".concat(seqId.toString()).concat(" AND ").concat(comparison));
      while(ppIds.next()) {
        retval.add(new Long(ppIds.getLong("PartialPlanId")));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected List _queryPartialPlanIds(Long seqId, String comparison);

  synchronized public static Map queryStepNumPartialPlanIds(Long seqId, String comparison) {
    Map retval = new HashMap();
    try {
      StringBuffer query = new StringBuffer("SELECT PartialPlanId, StepNum FROM PartialPlanStats WHERE SequenceId=");
      query.append(seqId.toString()).append(" AND ").append(comparison);
      //System.err.println(query);
      ResultSet ppIds = queryDatabase(query.toString());
      while(ppIds.next())
        retval.put(new Integer(ppIds.getInt("StepNum")), new Long(ppIds.getLong("PartialPlanId")));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected Map _queryStepNumPartialPlanIds(Long seqId, String comparison);

  synchronized public static boolean statsInDb(Long seqId) {
    boolean retval = false;
    try {
      ResultSet count = queryDatabase("SELECT COUNT(*) AS number FROM PartialPlanStats WHERE SequenceId=".concat(seqId.toString()));
      count.last();
      retval = count.getInt("number") != 0;
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected boolean _statsInDb(Long seqId);

  synchronized public static boolean rulesInDb(Long seqId) {
    boolean retval = false;
    try {
      ResultSet count = queryDatabase("SELECT COUNT(*) AS number FROM Rules WHERE SequenceId=".concat(seqId.toString()));
      count.last();
      retval = count.getInt("number") != 0;
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected boolean _rulesInDb(Long seqId);

  synchronized public static boolean transactionsInDatabaseForStep(Long partialPlanId) {
    boolean retval = false;
    try {
      ResultSet count = queryDatabase("SELECT COUNT(*) AS number FROM Transaction WHERE PartialPlanId=".concat(partialPlanId.toString()));
      count.last();
      retval = count.getInt("number") != 0;
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected boolean _transactionsInDatabaseForStep(Long partialPlanId);

  synchronized public static int maxStepForTransactionsInDb(Long seqId) {
    int retval = -1;
    try {
      ResultSet partialPlanIds = queryDatabase("SELECT DISTINCT PartialPlanId FROM Transaction WHERE SequenceId=".concat(seqId.toString()).concat( " ORDER BY PartialPlanId"));
      boolean rowsExist = partialPlanIds.last();
      if (rowsExist) {
        Long maxPartialPlanId = new Long(partialPlanIds.getLong("PartialPlanId"));
        String maxPartialPlanName = getPartialPlanNameById( seqId, maxPartialPlanId);
        retval = Integer.parseInt( maxPartialPlanName.substring( 4));
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //abstract protected int _maxStepForTransactionsInDb(Long seqId);

  synchronized protected static boolean varDerivedDomainIsSingleton(final Blob info) {
    String temp;
    try {
      temp = new String(info.getBytes(1, (int) info.length()));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
    StringTokenizer strTok = new StringTokenizer(temp, ",");
    while(strTok.countTokens() != 4) {
      strTok.nextToken();
    }
    String type = strTok.nextToken();
    String domain = strTok.nextToken();
    return varDomainIsSingleton(type, domain);
  }

  synchronized protected static boolean varSpecDomainIsSingleton(final Blob info) {
    String temp;
    try {
      temp = new String(info.getBytes(1, (int) info.length()));
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
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

  synchronized protected static boolean varDomainIsSingleton(final String type, String domain) {
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

  //added to make HSQL shut up.
  synchronized protected static boolean moveToLast(ResultSet results) {
    boolean retval = false;
    try {
      retval = results.next();
      while(!results.isLast()) {
        //System.err.println("Stepping result set.");
        retval = results.next();
      }
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
    }
    return retval;
  }
  //   public EntityIdComparator() {}
  //   public boolean equals(Object o) {}
  //   public int compare(Object o1, Object o2) {}  
}
