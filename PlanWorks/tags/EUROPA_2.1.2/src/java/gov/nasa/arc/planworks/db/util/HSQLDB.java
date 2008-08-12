//
// * See the file "PlanWorks/disclaimers-and-notices.txt" for
// * information on usage and redistribution of this file,
// * and for a DISCLAIMER OF ALL WARRANTIES.
//

// $Id: HSQLDB.java,v 1.1 2006-10-03 16:14:16 miatauro Exp $
//
package gov.nasa.arc.planworks.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

public class HSQLDB extends SQLDB {

//   private static final String CREATE_CONSTRAINT_VAR_MAP = "CREATE CACHED TABLE ConstraintVarMap ( ConstraintId int (11) NOT NULL default '0', VariableId int(11) NOT NULL default '0', " +
//                                                           "PartialPlanId bigint(20) unsigned NOT NULL default '0', PRIMARY KEY  (PartialPlanId,ConstraintId,VariableId), " +
//                                                           "INDEX (ConstraintId), INDEX(VariableId), INDEX (PartialPlanId))";
  //have to, when loading data into the map, construct the primary key because it spans multiple Ids.
  //primary key is (PartialPlanId,ConstraintId,VariableId)
  private static final String CREATE_CONSTRAINT_VAR_MAP = "CREATE CACHED TABLE ConstraintVarMap ( ConstraintId INT DEFAULT 0, VariableId INT DEFAULT 0, " +
                                                          "PartialPlanId BIGINT DEFAULT 0, PRIMARY KEY (PartialPlanId, ConstraintId, VariableId) ) ; " +
                                                          "CREATE INDEX CVM_1 ON ConstraintVarMap (ConstraintId); " +
                                                          "CREATE INDEX CVM_2 ON ConstraintVarMap (VariableId); " +
                                                          "CREATE INDEX CVM_3 ON ConstraintVarMap (PartialPlanId);";

  //ChildObjectIds is a CSV, ObjectType is currently Object = 0, Timeline = 1, Resource = 2
  //if the object is a timeline, ExtraInfo format is "SlotId,SlotIndex:SlotId,SlotIndex"
  //if the object is a resource, ExtraInfo format is "HorizonStart,HorizonEnd,InitialCapacity,LimitMin,LimitMax, InstantIdList"
//   private static final String CREATE_OBJECT = "CREATE CACHED TABLE Object ( ObjectId int(11) NOT NULL default '0', ObjectType int(11) NOT NULL default '0', ParentId int(11) default '0', " + 
//                                               "PartialPlanId bigint(20) NOT NULL default '0', ObjectName varchar(255) NOT NULL default 'ErrorObject', ChildObjectIds blob default NULL, " +
//                                               "VariableIds blob default NULL, TokenIds blob default NULL, ExtraInfo blob default NULL, PRIMARY KEY  (PartialPlanId,ObjectId), " +
//                                               "INDEX (ObjectId))";
  //primary key is (PartialPlanId,ObjectId)
  private static final String CREATE_OBJECT = "CREATE CACHED TABLE Object ( ObjectId INT DEFAULT 0, ObjectType INT DEFAULT 0, ParentId INT DEFAULT 0, " + 
                                              "PartialPlanId BIGINT DEFAULT 0, ObjectName VARCHAR DEFAULT 'ErrorObject', ChildObjectIds VARCHAR DEFAULT NULL, " +
                                              "VariableIds VARCHAR DEFAULT NULL, TokenIds LONGVARCHAR DEFAULT NULL, ExtraInfo LONGVARCHAR DEFAULT NULL, PRIMARY KEY (PartialPlanId, ObjectId) ); " +
                                              "CREATE INDEX O_1 ON Object (ObjectId); " +
                                              "CREATE INDEX O_2 ON Object (PartialPlanId)";

  private static final String CREATE_PARTIAL_PLAN = "CREATE CACHED TABLE PartialPlan ( PlanName VARCHAR DEFAULT 'ErrorPlan', PartialPlanId BIGINT DEFAULT 0 PRIMARY KEY, " +
                                                    "Model VARCHAR DEFAULT NULL, SequenceId BIGINT DEFAULT 0 ); " +
                                                    "CREATE INDEX PP_1 ON PartialPlan (PlanName); " +
                                                    "CREATE INDEX PP_2 ON PartialPlan (SequenceId)";

  private static final String CREATE_PROJECT = "CREATE CACHED TABLE Project ( ProjectId INT IDENTITY , ProjectName VARCHAR DEFAULT 'ErrorProject')";

  private static final String CREATE_SEQUENCE = "CREATE CACHED TABLE Sequence ( SequenceURL VARCHAR DEFAULT 'ErrorURL', SequenceId BIGINT DEFAULT 0, " +
                                                "RulesText LONGVARCHAR DEFAULT NULL, ProjectId INT DEFAULT -1, SequenceOrdering INT IDENTITY); " +
                                                "CREATE INDEX S_1 ON Sequence (SequenceURL); CREATE INDEX S_2 ON Sequence (SequenceId)";

  //TokenType currently IntervalToken = 0, ResourceTransaction = 1
  //if TokenType == ResourceTransaction, ExtraData format: "QuantityMin:QuantityMax"
  //if TokenType == IntervalToken && !IsFreeToken, ExtraData format: SlotOrder
  private static final String CREATE_TOKEN = "CREATE CACHED TABLE Token ( TokenId INT DEFAULT 0, TokenType INT DEFAULT 0, SlotId INT DEFAULT NULL, " +
                                             "SlotIndex INT DEFAULT NULL, PartialPlanId BIGINT  DEFAULT 0, IsFreeToken BIT  DEFAULT 1, " + 
                                             "IsValueToken BIT  DEFAULT 1, StartVarId INT  DEFAULT 0, EndVarId INT  DEFAULT 0, " + 
                                             "DurationVarId INT DEFAULT NULL, StateVarId INT  DEFAULT 0, PredicateName VARCHAR DEFAULT 'ErrorPredicate', " + 
                                             "ParentId INT DEFAULT NULL, ParentName VARCHAR DEFAULT 'ErrorParentName', ObjectVarId INT DEFAULT NULL, " +
                                             "ParamVarIds LONGVARCHAR DEFAULT NULL, ExtraData LONGVARCHAR DEFAULT NULL, PRIMARY KEY  (PartialPlanId, TokenId)); " +
                                             "CREATE INDEX T_1 ON Token (TokenId); CREATE INDEX T_2 ON Token (SlotId); " +
                                             "CREATE INDEX T_3 ON Token (SlotIndex); CREATE INDEX T_4 ON Token (StartVarId); " +
                                             "CREATE INDEX T_5 ON Token (EndVarId); CREATE INDEX T_6 ON Token (DurationVarId); " +
                                             "CREATE INDEX T_7 ON Token (StateVarId); CREATE INDEX T_8 ON Token (PredicateName); " +
                                             "CREATE INDEX T_9 ON Token (ParentId); CREATE INDEX T_10 ON Token (ObjectVarId); " +
                                             "CREATE INDEX T_11 ON Token (IsFreeToken)";

  private static final String CREATE_CONSTRAINT = "CREATE CACHED TABLE VConstraint ( ConstraintId INT  DEFAULT 0, PartialPlanId BIGINT  DEFAULT 0, " + 
                                                  "ConstraintName VARCHAR  DEFAULT 'ErrorConstraint', ConstraintType VARCHAR  DEFAULT 'TEMPORAL', " +
                                                  "PRIMARY KEY  (PartialPlanId,ConstraintId), CONSTRAINT C_TYPE CHECK(ConstraintType = 'TEMPORAL' OR ConstraintType = 'ATEMPORAL')); " +
                                                  "CREATE INDEX VC_1 ON VConstraint(ConstraintId); " +
                                                  "CREATE INDEX VC_2 ON VConstraint (ConstraintType)";
 
  private static final String CREATE_VARIABLE = "CREATE CACHED TABLE Variable ( VariableId INT  DEFAULT 0, PartialPlanId BIGINT  DEFAULT 0, " +
                                                "ParentId INT  DEFAULT 0, ParameterName VARCHAR DEFAULT 'ErrorParam', " +
                                                "DomainType VARCHAR DEFAULT 'IntervalDomain', EnumDomain LONGVARCHAR, " +
                                                "IntDomainType VARCHAR, IntDomainLowerBound VARCHAR, IntDomainUpperBound VARCHAR, " +
                                                "VariableType VARCHAR , PRIMARY KEY  (PartialPlanId,VariableId,VariableType), CONSTRAINT D_TYPE CHECK(DomainType = 'EnumeratedDomain' OR DomainType = 'IntervalDomain') " +
                                                //", CONSTRAINT ID_TYPE CHECK(IntDomainType = 'INTEGER_SORT' OR IntDomainType = 'REAL_SORT' OR (IntDomainType = NULL AND DomainType = 'EnumeratedDomain')) " +
                                                ");" +
                                                "CREATE INDEX V_1 ON Variable (VariableId); CREATE INDEX V_2 ON Variable (VariableType); " + 
                                                "CREATE INDEX V_3 ON Variable (ParentId); CREATE INDEX V_4 ON Variable (DomainType); " + 
                                                "CREATE INDEX V_5 ON Variable (ParameterName); CREATE INDEX V_6 on Variable (PartialPlanId)";

  private static final String CREATE_PP_STATS = "CREATE CACHED TABLE PartialPlanStats ( SequenceId BIGINT  DEFAULT 0, PartialPlanId BIGINT  DEFAULT 0, " + 
                                                "StepNum INT  DEFAULT 0, NumTokens INT  DEFAULT 0, NumVariables INT  DEFAULT 0, " +
                                                "NumConstraints INT  DEFAULT 0, PRIMARY KEY (SequenceId, PartialPlanId)); " +
                                                "CREATE INDEX PS_1 ON PartialPlanStats (StepNum); CREATE INDEX PS_2 ON PartialPlanStats (NumTokens); " +
                                                "CREATE INDEX PS_3 ON PartialPlanStats (NumVariables); CREATE INDEX PS_4 ON PartialPlanStats (NumConstraints)";

  //Transactions blob is a CSV of Transaction Ids
  private static final String CREATE_RES_INSTANTS = "CREATE CACHED TABLE ResourceInstants ( PartialPlanId BIGINT  DEFAULT 0, ResourceId INT  DEFAULT 0, " +
                                                    "InstantId INT  DEFAULT 0, TimePoint INT  DEFAULT 0, LevelMin DECIMAL DEFAULT 0, " + 
                                                    "LevelMax DECIMAL DEFAULT 0, Transactions LONGVARCHAR DEFAULT NULL, PRIMARY KEY (PartialPlanId, ResourceId, InstantId)); " +
                                                    "CREATE INDEX RS_1 on ResourceInstants (TimePoint)";

  private static final String CREATE_RULES = "CREATE CACHED TABLE Rules ( SequenceId BIGINT  DEFAULT 0, RuleId INT  DEFAULT 0, RuleSource LONGVARCHAR , " +
                                             "PRIMARY KEY(SequenceId, RuleId))";

  private static final String CREATE_RULE_INSTANCE = "CREATE CACHED TABLE RuleInstance ( RuleInstanceId INT  DEFAULT 0, PartialPlanId BIGINT  DEFAULT 0, " +
                                                     "SequenceId BIGINT  DEFAULT 0, RuleId INT  DEFAULT 0, MasterTokenId INT  DEFAULT 0, " +
                                                     "SlaveTokenIds LONGVARCHAR DEFAULT NULL, RuleVarIds LONGVARCHAR DEFAULT NULL, PRIMARY KEY(RuleInstanceId, PartialPlanId)); " +
                                                     "CREATE INDEX RI_1 ON RuleInstance (SequenceId); CREATE INDEX RI_2 ON RuleInstance (PartialPlanId); " +
                                                     "CREATE INDEX RI_3 ON RuleInstance (RuleId); CREATE INDEX RI_4 ON RuleInstance (MasterTokenId)";
  
  private static final String CREATE_SLAVE_MAP = "CREATE CACHED TABLE RuleInstanceSlaveMap ( RuleInstanceId INT  DEFAULT 0, SlaveTokenId INT  DEFAULT 0, " +
                                                 "PartialPlanId BIGINT DEFAULT 0, PRIMARY KEY  (PartialPlanId,RuleInstanceId,SlaveTokenId))";

  private static final String CREATE_DECISION = "CREATE CACHED TABLE Decision ( PartialPlanId BIGINT  DEFAULT 0, DecisionId INT  DEFAULT 0, " + 
                                                "DecisionType INT  DEFAULT 0, EntityId INT  DEFAULT 0, IsUnit BIT  DEFAULT 0, " +
                                                "Choices LONGVARCHAR DEFAULT NULL, PRIMARY KEY(PartialPlanId, DecisionId))";

  private static final String[] TABLES = {CREATE_CONSTRAINT_VAR_MAP, CREATE_OBJECT, CREATE_PARTIAL_PLAN, CREATE_PROJECT, CREATE_SEQUENCE, CREATE_TOKEN, CREATE_CONSTRAINT, CREATE_VARIABLE,
                                          CREATE_PP_STATS, CREATE_RES_INSTANTS, CREATE_RULES, CREATE_RULE_INSTANCE, CREATE_SLAVE_MAP, CREATE_DECISION};

  private static final Map INSERT_COLUMNS;

  static {
    INSERT_COLUMNS = new HashMap();
    INSERT_COLUMNS.put("Sequence", new String[]{"SequenceUrl", "SequenceId", "RulesText"});
  }


  public HSQLDB() {
    super();
    dbIsStarted = false;
    dbIsConnected = false;
    queryTime = 0;
  }

  protected void _startDatabase() throws IllegalArgumentException, IOException, SecurityException {
    if(dbIsStarted) {
      System.err.println("Database already started.");
      return;
    }

    try {
      Class.forName("org.hsqldb.jdbcDriver");//load the driver
    }
    catch(ClassNotFoundException cnfe) {
      System.err.println(cnfe);
      cnfe.printStackTrace();
      System.exit(-1);
    }

    String dbFile = System.getProperty("hsqldb.db");
    if(dbFile == null) {
      System.err.println("No hsqldb.db property for the HSQLDB database file.");
      System.exit(-1);
    }
    
    
    System.err.println("Checking to see if database " + dbFile + ".data has been created...");
    File db = new File(dbFile + ".data");
    if(db.exists()) {
      System.err.println("It has.");
    }
    else {
      System.err.println("It hasn't.");
      try {
        Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:/" + dbFile + ";shutdown=true", "sa", "");
        for(int i = 0; i < TABLES.length; ++i) {
          //System.err.println("Executing " + TABLES[i]);
          Statement st = conn.createStatement();
          st.execute(TABLES[i]);
        }
        Statement st = conn.createStatement();
        st.execute("COMMIT");
      }
      catch(SQLException sqle) {
        System.err.println("Error trying to determine if database has been created: " + sqle);
        sqle.printStackTrace();
        System.exit(-1);
      }
    }
    dbIsStarted = true;
  }

  protected void _stopDatabase() throws IllegalArgumentException, IOException, SecurityException {
    try {
      Statement st = conn.createStatement();
      st.execute("SHUTDOWN");
      conn.close();
      conn = null;
      dbIsConnected = false;
    }
    catch(SQLException sqle) {
      System.err.println("Error executing SHUTDOWN: " + sqle);
      sqle.printStackTrace();
    }
  }

  protected void _registerDatabase() {
    if(dbIsConnected)
      return;

    try {
      if(!dbIsStarted)
        startDatabase();
    }
    catch (Exception e) {
      System.err.println(e);
      System.exit(-1);
    }

    try {
      String dbFile = System.getProperty("hsqldb.db");
      if(dbFile == null) {
        System.err.println("No hsqldb.db property for the HSQLDB database file.");
        System.exit(-1);
      }
      conn = DriverManager.getConnection("jdbc:hsqldb:file:/" + dbFile, "sa", "");
      dbIsConnected = true;
    }
    catch(Exception e) {
      System.err.println("Connection failed: " + e);
      e.printStackTrace();
      System.exit(-1);
    }
  }

  protected void _unregisterDatabase() {
    try {
      Statement st = conn.createStatement();
      st.execute("COMMIT");
      conn.close();
    }
    catch(SQLException sqle) {
      sqle.printStackTrace();
      System.exit(-1);
    }
    dbIsConnected =  false;
  }

//   protected ResultSet _queryDatabase(final String query) {
//     Statement stmt = null;
//     ResultSet result = null;
//     try {
//       if(!dbIsConnected || conn.isClosed()) {
//         registerDatabase();
//       }
//       long t1 = System.currentTimeMillis();
//       stmt = conn.createStatement();
//       result = stmt.executeQuery(query);
//       queryTime += System.currentTimeMillis() - t1;
//     }
//     catch(SQLException sqle) {
//       System.err.println(sqle);
//       sqle.printStackTrace();
//       System.exit(-1);
//     }
//     return result;
//   }

  //protected int _updateDatabase(final String update) {return 0;}
  protected void _analyzeDatabase() {}
  //protected void _cleanDatabase() {}

  protected void _loadFile(final String file, final String tableName) {
    _loadFile(file, tableName, "\t", "\n");
  }

  private String hexToChar(String s) {
    int x = Integer.parseInt(s.substring(2), 16);
    Character c = new Character((char)x);
    return c.toString();
  }

  private String readNextEntry(Reader reader, String lineSep) {
    final int bufSize = 100;
    char[] buf = new char[bufSize];
    StringBuffer outBuf = new StringBuffer();
    int index = 0;
    try {
      char read = (char) reader.read();
      while(read != (char) -1) {
        //if we've hit the line separator, we're done
        if(lineSep.charAt(0) == read) {
          outBuf.append(buf, 0, index);
          return outBuf.toString();
        }
        
        buf[index] = read;
        ++index;
        if(index == bufSize) {
        outBuf.append(buf);
        index = 0;
        }
        read = (char) reader.read();
      }
      outBuf.append(buf, 0, index);
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
    return outBuf.toString();
  }

  private String hsqlQuote(String str) {
    return str.replaceAll("'", "''");
  }

  protected void _loadFile(final String fileName, final String tableName, 
                           final String columnSeparator, 
                           final String entrySeparator) {
    String colSep = columnSeparator;
    String entSep = entrySeparator;
    if(columnSeparator.startsWith("0x"))
      colSep = hexToChar(colSep);
    if(entrySeparator.startsWith("0x"))
      entSep = hexToChar(entSep);
    
    File file = new File(fileName);
    if(!file.exists()) {
      System.err.println("File '" + fileName + "' doesn't exist.");
      return;
    }
    
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(file));
    }
    catch(FileNotFoundException fnfe) {
      System.err.println("File '" + fileName + "' doesn't exist.");
      return;
    }
    String entry = readNextEntry(reader, entSep);
    while(entry != null && entry.length() > 0) {
      String [] columns = entry.split(colSep); //possible optimization:  do the split as we read
      if(columns.length < 1) {
        System.err.println("Failed to read entry in " + fileName + " for " + tableName);
      }
      StringBuffer insert = new StringBuffer("INSERT INTO ");
      insert.append(tableName);
      if(INSERT_COLUMNS.containsKey(tableName)) {
        String[] colNames = (String []) INSERT_COLUMNS.get(tableName);
        if(colNames.length < 1) {
          System.err.println("Invalid entry for " + tableName + " in INSERT_COLUMNS.");
          System.exit(-1);
        }
        insert.append(" (").append(colNames[0]).append("");
        for(int i = 1; i < colNames.length && i < columns.length; ++i)
          insert.append(", ").append(colNames[i]);
        insert.append(")");
      }
      insert.append(" VALUES('").append(hsqlQuote(columns[0])).append("'");
      for(int i = 1; i < columns.length; ++i) {
        insert.append(", ");
        if(columns[i].equals("\\N"))
          insert.append("NULL");
        else
          insert.append("'").append(hsqlQuote(columns[i])).append("'");
      }
      insert.append(")");
      updateDatabase(insert.toString());
      entry = readNextEntry(reader, entSep);
    }
  }

  //protected String _queryPartialPlanModelById(final Long partialPlanId) {return null;}
  //protected List _getProjectNames() {return null;}
  //protected boolean _projectExists(final String name) {return false;}
  //protected boolean _sequenceExists(final String url) {return false;}
  //protected boolean _sequenceExists(final Long id) {return false;}
  //protected String _getSequenceUrl(final Long id) {return null;}
  //protected Long _getSequenceId(final String url) {return null;}
  //protected void _setSequenceUrl(final Long id, final String url) {}
  //protected boolean _partialPlanExists(final Long sequenceId, final String name) {return false;}
  //protected boolean _transactionsInDatabase(final Long seqId) {return false;}
  //protected List _queryPlanNamesInDatabase(final Long sequenceId) {return null;}
  //protected List _queryPlanIdsInDatabase(final Long seqId) {return null;}
  //protected void _addProject(final String name) {}
  //protected Integer _latestProjectId() {return null;}
  //protected Long _addSequence(final String url, final Integer projectId) {return null;}
  //protected Long _latestSequenceId() {return null;}
  //protected Integer _getProjectIdByName(final String name) {return null;}
  //protected Map _getSequences(final Integer projectId) {return null;}
  //protected void _deleteProject(final Integer id) throws ResourceNotFoundException {}
  //protected void _deletePlanningSequence(final Long sequenceId) {}
  //protected List _getPlanNamesInSequence(final Long sequenceId) {return null;}
  //protected Long _getNewPartialPlanId(final Long sequenceId, final String name) {return null;}
  //protected Long _getPartialPlanIdByName(final Long sequenceId, final String name) {return null;}
//   protected Long _getPartialPlanIdByStepNum(final Long sequenceId, 
//                                             final int stepNum) {return null;}
//   protected String _getPartialPlanNameById(final Long seqId,
//                                            final Long ppId) {return null;}
//  protected List _queryPartialPlanNames(final Long sequenceId) {return null;}
  //protected void _createObjects(PwPartialPlanImpl partialPlan) {}
  //protected int _countTransactions(final Long sequenceId) {return 0;}
  //   protected Map _queryTransactions(final Long sequenceId) {}
//   protected void _createSlotTokenNodesStructure(PwPartialPlanImpl partialPlan,
//                                                 final Long seqId) {}
  protected void _queryConstraints(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet constraints = 
        queryDatabase("SELECT VConstraint.ConstraintId, VConstraint.ConstraintName, VConstraint.ConstraintType, ConstraintVarMap.VariableId FROM VConstraint LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=VConstraint.PartialPlanId AND ConstraintVarMap.ConstraintId=VConstraint.ConstraintId WHERE VConstraint.PartialPlanId=".concat(partialPlan.getId().toString()).concat(" ORDER BY VConstraint.ConstraintId"));
      PwConstraintImpl constraint = null;
      while(constraints.next()) {
        //Integer constraintId = new Integer(constraints.getInt("VConstraint.ConstraintId"));
        Integer constraintId = new Integer(constraints.getInt("ConstraintId"));
        
        if(constraint == null || !constraint.getId().equals(constraintId)) {
          constraint = new PwConstraintImpl(constraints.getString("ConstraintName"),//constraints.getString("VConstraint.ConstraintName"),
                                            constraintId,
                                            constraints.getString("ConstraintType"),//constraints.getString("VConstraint.ConstraintType"),
                                            partialPlan);
          partialPlan.addConstraint(constraintId, constraint);
        }
        //Integer variableId = new Integer(constraints.getInt("ConstraintVarMap.VariableId"));
        Integer variableId = new Integer(constraints.getInt("VariableId"));
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

  protected void _queryVariables(PwPartialPlanImpl partialPlan) {
    try {
      ResultSet variables =
        queryDatabase("SELECT Variable.VariableId, Variable.VariableType, Variable.DomainType, Variable.EnumDomain, Variable.IntDomainType, Variable.IntDomainLowerBound, Variable.IntDomainUpperBound, Variable.ParentId, Variable.ParameterName, ConstraintVarMap.ConstraintId FROM Variable LEFT JOIN ConstraintVarMap ON ConstraintVarMap.PartialPlanId=Variable.PartialPlanId AND ConstraintVarMap.VariableId=Variable.VariableId WHERE Variable.PartialPlanId=".concat(partialPlan.getId().toString()));
      PwVariableImpl variable = null;
      while(variables.next()) {
        Integer variableId = new Integer(variables.getInt("VariableId"));
        if(variable == null || !variable.getId().equals(variableId)) {
          PwDomainImpl domain = null;
          String domainType = variables.getString("DomainType");
          if(domainType.equals("EnumeratedDomain")) {
            Blob blob = variables.getBlob("EnumDomain");
            domain = new PwEnumeratedDomainImpl(new String(blob.getBytes(1, (int) blob.length())));
          }
          else if(domainType.equals("IntervalDomain")) {
            domain = 
              new PwIntervalDomainImpl(variables.getString("IntDomainType"),
                                       variables.getString("IntDomainLowerBound"),
                                       variables.getString("IntDomainUpperBound"));
          }
          variable = new PwVariableImpl(variableId, variables.getString("VariableType"),
                                        new Integer(variables.getInt("ParentId")), domain,
                                        partialPlan);
          partialPlan.addVariable(variableId, variable);

          String parameterName = variables.getString("ParameterName");
          if(!variables.wasNull()) {
            variable.addParameter(parameterName);
          }
        }
        Integer constraintId = new Integer(variables.getInt("ConstraintId"));
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
  //protected void _queryRuleInstances(PwPartialPlanImpl partialPlan) {}
  //protected void _queryResourceInstants(PwPartialPlanImpl partialPlan) {}
//   protected Map _queryRules(final Long sequenceId,
//                             final String modelRuleDelimiters) {return null;}
  //protected List _queryOpenDecisionsForStep(final Long ppId,
  //                                          final PwPartialPlan partialPlan) {return null;}
  //protected Integer _queryCurrentDecisionIdForStep(final Long ppId) {return null;}
  //   protected List _queryTransactionsForStep(final Long seqId, final Long ppId) {}
  //   protected List _queryTransactionsForConstraint(final Long sequenceId, 
  //   protected List _queryTransactionsForToken(final Long sequenceId, 
  //   protected List _queryTransactionsForVariable(final Long sequenceId, 
  //   protected List _queryStepsWithTokenTransaction(final Long sequenceId, 
  //   protected List _queryStepsWithVariableTransaction(final Long sequenceId, 
  //   protected List _queryStepsWithConstraintTransaction(final Long sequenceId, 
  //   protected List _queryStepsWithTokenTransaction(final Long sequenceId, 
  //   protected List _queryStepsWithVariableTransaction(final Long sequenceId, 
  //   protected List _queryStepsWithConstraintTransaction(final Long sequenceId, 
  //   protected List _queryStepsWithRestrictions(final Long sequenceId) {}
  //   protected List _queryStepsWithRelaxations(final Long sequenceId) {}
  //   protected List _queryStepsWithUnitDecisions(final PwPlanningSequenceImpl seq) {}
  //   protected List _queryStepsWithNonUnitDecisions(final PwPlanningSequenceImpl seq) {}
//   protected List _queryFreeTokensAtStep( final int stepNum, 
//                                          final PwPlanningSequenceImpl seq) {return null;}
//   protected List _queryUnboundVariablesAtStep( final int stepNum,
//                                                final PwPlanningSequenceImpl seq) {return null;}
  //protected List _queryPartialPlanSizes(Long sequenceId) {return null;}
  //protected int [] _queryPartialPlanSize(final Long partialPlanId) {return new int[3];}
  //protected Long _queryPartialPlanId(final Long seqId, final int stepNum) {return null;}
  //protected Long _queryPartialPlanId(final Long seqId, final String stepName) {return null;}
  //protected Map _queryAllIdsForPartialPlan(final Long ppId) {return null;}
  //protected List _queryRuleIdsForSequence(final Long seqId) {return null;}
  //protected List _queryResourceIdsForObject(final Long ppId, final Integer objId) {return null;}
  //protected List _queryTimelineIdsForObject(final Long ppId, final Integer objId) {return null;}
//   protected List _querySlotIdsForTimeline(final Long ppId, final Integer objId, 
//                                           final Integer tId) {return null;}
  //protected Map _queryAllChildRuleInstanceIds(PwPartialPlanImpl partialPlan) {return null;}
  //  protected List _queryTokenRelationIdsForToken(final Long ppId, 
//   protected List _queryTransactionIdsForPartialPlan(final Long seqId, 
//                                                     final Long ppId) {return null;}
//  protected List _queryTransactionNameList() {return null;}
  //protected void _queryTransactionNames(PwPlanningSequence seq, Set constrTrans, Set tokTrans, Set varTrans) {}
  //protected List _queryPartialPlanIds(Long seqId) {return null;}
  //protected List _queryPartialPlanIds(Long seqId, String comparison) {return null;}
  //protected Map _queryStepNumPartialPlanIds(Long seqId, String comparison) {return null;}
  //protected boolean _statsInDb(Long seqId) {return false;}
  //protected boolean _rulesInDb(Long seqId) {return false;}
  //protected boolean _transactionsInDatabaseForStep(Long partialPlanId) {return false;}
  //protected int _maxStepForTransactionsInDb(Long seqId) {return 0;}


}
