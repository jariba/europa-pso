package gov.nasa.arc.planworks.dbg.testLang;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import net.n3.nanoxml.*;

import testLang.TestLangHelper;
import testLang.TestLangTokenTypes;
import testLang.TestLangRuntimeException;
import testLang.TestLangParseException;
import testLang.TestLangToXML;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;

public class TestLangInterpreter extends TestLangHelper implements TestLangTokenTypes {

  public static boolean runInternalTests() throws TestLangRuntimeException {
    return (new TestLangInterpreter()).internalTests();
    
  }

  public static void runTests(String projectName, String sequenceUrl, String testUrl) throws TestLangParseException, 
    TestLangRuntimeException {
    IXMLElement tests = commonInit(projectName, sequenceUrl, testUrl);
    Long seqId = MySQLDB.getSequenceId(sequenceUrl);
    (new TestLangInterpreter(tests, projectName, seqId)).runTest();
  }

  public static void runTests(String projectName, String sequenceUrl, String testUrl, String testName)
    throws TestLangParseException, TestLangRuntimeException {
    IXMLElement tests = commonInit(projectName, sequenceUrl, testUrl);
    Long seqId = MySQLDB.getSequenceId(sequenceUrl);
    IXMLElement test = findTest(tests, testName);
    if(test == null)
      throw new TestLangRuntimeException("Test '" + testName + "' doesn't exist in file '" + testUrl + "'");
    (new TestLangInterpreter(test, projectName, seqId)).runTest();
  }

  private static IXMLElement commonInit(String projectName, String sequenceUrl, String testUrl) 
    throws TestLangParseException, TestLangRuntimeException {
    if(!MySQLDB.projectExists(projectName))
      throw new TestLangRuntimeException("Project '" + projectName + "' doesn't exist.");
    if(!MySQLDB.sequenceExists(sequenceUrl) && !(new File(sequenceUrl)).exists())
      throw new TestLangRuntimeException("Sequence URL '" + sequenceUrl + "' isn't in the database or the filesystem.");
    File testFile = null;
    try {
      PwProject proj = proj = PwProject.getProject(projectName);
      if(!MySQLDB.sequenceExists(sequenceUrl))
        proj.addPlanningSequence(sequenceUrl);
      if(!testUrl.endsWith(".xml")) {
        TestLangToXML.convertFile(testUrl, testUrl + ".xml");
        testUrl = testUrl + ".xml";
      }
      testFile = new File(testUrl);
    }
    catch(Exception e) {
      throw new TestLangRuntimeException(e);
    }
    if(!testFile.exists())
      throw new TestLangRuntimeException("Test file '" + testUrl + "' doesn't exist.");
    if(!testFile.canRead())
      throw new TestLangRuntimeException("Can't read test file '" + testUrl + "'.");
    try {
      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      IXMLReader reader = StdXMLReader.fileReader(testUrl);
      parser.setReader(reader);
      return (IXMLElement) parser.parse();
    }
    catch(Exception e) {
      throw new TestLangParseException(e);
    }
  }

  private static IXMLElement findTest(IXMLElement root, String name) throws TestLangParseException, TestLangRuntimeException {
    if(root == null)
      return null;
    if(root.getName().equals("Test") && root.getAttribute("name", "").equals(name))
      return root;
    for(Iterator it = root.getChildren().iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      if(child.getName().equals("Test")) {
        IXMLElement retval;
        if((retval = findTest(child, name)) != null)
          return retval;
      }
    }
    return null;
  }

  //private static final String [] methodNames;
  private static final Map methodNames;
  private static final Class [] methodArgTypes = {IXMLElement.class};
  private static final Class [] twoArgMethodArgTypes = {IXMLElement.class, Object.class};
  private static final Class [] twoArgMethodArgStringTypes = {IXMLElement.class, String.class};

  static {
    methodNames = new HashMap();
    methodNames.put("EnumeratedDomain", "buildEnumDomain");
    methodNames.put("IntervalDomain", "buildIntDomain");
    methodNames.put("Value", "evalValue");
    methodNames.put("in", "in");
    methodNames.put("out", "out");
    methodNames.put("eq", "equalTo");
    methodNames.put("=", "equalTo");
    methodNames.put("==", "equalTo");
    methodNames.put("ne", "notEqual");
    methodNames.put("!=", "notEqual");
    methodNames.put("Tokens", "tokens");
    methodNames.put("Objects", "objects");
    methodNames.put("Transactions", "transactions");
    methodNames.put("Entity", "entity");
    methodNames.put("Count", "count");
    methodNames.put("lt", "lessThan");
    methodNames.put("<", "lessThan");
    methodNames.put("gt", "greaterThan");
    methodNames.put(">", "greaterThan");
    methodNames.put("ge", "greaterThanOrEqual");
    methodNames.put(">=", "greaterThanOrEqual");
    methodNames.put("le", "lessThanOrEqual");
    methodNames.put("<=", "lessThanOrEqual");
    methodNames.put("Test", "test");
    methodNames.put("intersects", "intersects");
    methodNames.put("At", "assertion");
  }

  private IXMLElement testTree;
  private Long seqId;
  private PwPlanningSequence seq;
  private String name;
  private String projName;
  private boolean tracePrint;
  private boolean boolPrint;

  public TestLangInterpreter(IXMLElement xml, String projName, Long seqId) throws TestLangRuntimeException {
    if(xml == null)
      throw new TestLangRuntimeException("Attempted to run unparsed test!");
    checkValidType(xml.getName(), "Test", "Attempted to instantiate a TestHelper with something other than a test!");
    this.seqId = seqId;
    testTree = xml;
    name = xml.getAttribute("name", "");
    this.projName = projName; 
    try {seq = PwProject.getProject(projName).getPlanningSequence(seqId);}
    catch(ResourceNotFoundException rnfe){throw new TestLangRuntimeException(rnfe);}
    tracePrint = false;
    boolPrint = true;
  }
  private TestLangInterpreter() {
    testTree = null;
    seqId = null;
    seq = null;
    name = "test";
    projName = null;
    tracePrint = true;
    boolPrint = true;
  }

  public Boolean runTest() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    for(Iterator it = testTree.getChildren().iterator(); it.hasNext();) {
      IXMLElement assertion = (IXMLElement) it.next();
      if(assertion.getName().equals("At") || assertion.getName().equals("Test")) {
        Boolean value = (Boolean) evaluate(assertion);
        System.err.println(value);
        if(value.equals(Boolean.FALSE)) {
          System.err.println("Test '" + testTree.getName() + "' FAILED at " +
                             assertion.getAttribute("file", "UnknownFile") + ", line " + 
                             assertion.getAttribute("lineNo", "UnknownLine"));
          return Boolean.FALSE;
        }
      }
      else
        throw new TestLangRuntimeException("Invalid assertion type '" +
                                           assertion.getName() + "'");
    }
    System.err.println("Test " + testTree.getName() + " PASSED.");
    return Boolean.TRUE;
  }

  private Object evaluate(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(xml == null)
      throw new TestLangRuntimeException("Attempted to evaluate a null element");
    checkValidType(xml.getName(), "Attempted to evaluate invalid type " + xml.getName());
    try {
      Method evalMethod = 
        this.getClass().getDeclaredMethod((String)methodNames.get(xml.getName()), methodArgTypes);
      Object [] args = {xml};
      return evalMethod.invoke(this, args);
    }
    catch(NoSuchMethodException nsme) {
      throw new TestLangRuntimeException(nsme);
    }
    catch(IllegalAccessException iae) {
      throw new TestLangRuntimeException(iae);
    }
    catch(InvocationTargetException ite) {
      throw new TestLangRuntimeException(ite);
    }
  }

  private Object evaluate(IXMLElement xml, Object arg) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(xml == null)
      throw new TestLangRuntimeException("Attempted to evaluate a null element.");
    checkValidType(xml.getName(), "Attempted to evaluate invalid type " + xml.getName());
    try {
      Method evalMethod =
        this.getClass().getDeclaredMethod((String)methodNames.get(xml.getName()),
                                          twoArgMethodArgTypes);
      Object [] args = {xml, arg};
      Object retval = evalMethod.invoke(this, args);
      if(retval instanceof Boolean && !((Boolean)retval).booleanValue())
        throw new TestLangRuntimeException("Assertion failed");
      return retval;
    }
    catch(NoSuchMethodException nsme) {
      throw new TestLangRuntimeException(nsme);
    }
    catch(IllegalAccessException iae) {
      throw new TestLangRuntimeException(iae);
    }
    catch(InvocationTargetException ite) {
      throw new TestLangRuntimeException(ite);
    }
  }

  private Object evaluate(IXMLElement xml, String arg) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(xml == null)
      throw new TestLangRuntimeException("Attempted to evaluate a null element.");
    checkValidType(xml.getName(), "Attempted to evaluate invalid type " + xml.getName());
    try {
      Method evalMethod =
        this.getClass().getDeclaredMethod((String)methodNames.get(xml.getName()),
                                          twoArgMethodArgStringTypes);
      Object [] args = {xml, arg};
      Object retval = evalMethod.invoke(this, args);
      if(retval instanceof Boolean && !((Boolean)retval).booleanValue())
        throw new TestLangRuntimeException("Assertion failed");
      return retval;
    }
    catch(NoSuchMethodException nsme) {
      throw new TestLangRuntimeException(nsme);
    }
    catch(IllegalAccessException iae) {
      throw new TestLangRuntimeException(iae);
    }
    catch(InvocationTargetException ite) {
      throw new TestLangRuntimeException(ite);
    }
  }

  private Boolean test(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    return (new TestLangInterpreter(xml, projName, seqId)).runTest();
  }

  private Boolean assertion(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "At", "Attempted to evalute assertion with invalid type.");
    Vector children = xml.getChildren();
    if(children.size() != 2)
      throw new TestLangRuntimeException("'At' element with incorrect number of children.");
    IXMLElement step = null;
    IXMLElement test = null;
    for(Iterator it = children.iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      if(child.getName().equals("step"))
        step = child;
      else
        test = child;
    }
    if(step == null)
      throw new TestLangRuntimeException("'At' element with no step.");
    if(test == null)
      throw new TestLangRuntimeException("'At' element with no test.");
    Map ppIds = getPartialPlanIds(step);
    String qualifier = step.getAttribute("qualifier", null);
    Boolean retval = null;
    if(qualifier.equals("first"))
      retval = (Boolean) evaluate(test, ppIds.get(CollectionUtils.findLeast(new ArrayList(ppIds.keySet()))).toString());
    else if(qualifier.equals("last"))
      retval = (Boolean) evaluate(test, ppIds.get(CollectionUtils.findGreatest(new ArrayList(ppIds.keySet()))).toString());
    else if(qualifier.equals("all"))
      retval = (Boolean) evaluate(test, CollectionUtils.join(", ", ppIds.values()));
    else {
      for(Iterator ppIt = ppIds.values().iterator(); ppIt.hasNext();) {
        retval = (Boolean)evaluate(test, ppIt.next().toString());
        if(qualifier.equals("each")) {
          if(retval.equals(Boolean.FALSE))
            break;
        }
        else if(qualifier.equals("any")) {
          if(retval.equals(Boolean.TRUE))
            break;
        }
      }
    }
    return retval;
  }

  private EnumeratedDomain tokens(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Tokens", 
                   "Attempted to evaluate 'Tokens' function with invalid type");
    IXMLElement start = null, end = null, status = null, predicate = null;
    List variables = new LinkedList();
    boolean foundStart = false;
    boolean foundEnd = false;
    boolean foundStatus = false;
    boolean foundPredicate = false;
    for(Iterator it = xml.getChildren().iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      if(!foundStart && child.getName().equals("start")) {
        start = child;
        foundStart = true;
      }
      else if(!foundEnd && child.getName().equals("end")) {
        end = child;
        foundEnd = true;
      }
      else if(!foundStatus && child.getName().equals("status")) {
        status = child;
        foundStatus = true;
      }
      else if(!foundPredicate && child.getName().equals("predicate")) {
        predicate = child;
        foundPredicate = true;
      }
      else if(child.getName().equals("variable"))
        variables.add(child);
      else
        throw new TestLangRuntimeException("Attempted to evaluate 'Tokens' function with invalid or extra " +
                                           "argument type " + child.getName());
    }

    StringBuffer tokenQuery = new StringBuffer("SELECT TokenId FROM Token WHERE PartialPlanId IN (");
    tokenQuery.append(ppIds).append(") ");
    if(predicate != null)
      tokenQuery.append(nameQueryStr(predicate, "Tokens"));
    
    EnumeratedDomain retval = new EnumeratedDomain();
    try {
      ResultSet tokIds = MySQLDB.queryDatabase(tokenQuery.toString());
      while(tokIds.next())
        retval.add(new Integer(tokIds.getInt("TokenId")));
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage() + "\nExecuting " + tokenQuery.toString());
    }

    trimTokensByVars(retval, ppIds, start, end, status, variables);
    return retval;
  }

  private EnumeratedDomain objects(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Objects", 
                   "Attempted to evaluate 'Objects function with invalid type " + 
                   xml.getName());

    List variables = new LinkedList();
    IXMLElement name = null;
    boolean foundName = false;
    for(Iterator it = xml.getChildren().iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      if(!foundName && child.getName().equals("name")) {
        name = child;
        foundName = true;
      }
      else if(child.getName().equals("variable"))
        variables.add(child);
      else
        throw new TestLangRuntimeException("Attempted to evaluate 'Objects' function with invalid or extra argument type " +
                                           child.getName());
    }

    StringBuffer objectQuery = new StringBuffer("SELECT ObjectId FROM Object WHERE PartialPlanId IN (");
    objectQuery.append(ppIds).append(") ");
    
    if(name != null)
      objectQuery.append(nameQueryStr(name, "Objects"));
    EnumeratedDomain retval = new EnumeratedDomain();
    try {
      ResultSet objIds = MySQLDB.queryDatabase(objectQuery.toString());
      while(objIds.next())
        retval.add(new Integer(objIds.getInt("ObjectId")));
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage() + "\nExecuting " + objectQuery.toString());
    }
    trimObjectsByVars(retval, ppIds, variables);
    return retval;
  }

  private EnumeratedDomain transactions(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Transactions", 
                   "Attempted to evaluate 'Transactions' function with invalid type " + 
                   xml.getName());
    IXMLElement name = null, type = null;
    boolean foundName = false;
    boolean foundType = false;
    for(Iterator it = xml.getChildren().iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      if(!foundName && child.getName().equals("name")) {
        name = child;
        foundName = true;
      }
      else if(!foundType && child.getName().equals("type")) {
        type = child;
        foundType = true;
      }
      else
        throw new TestLangRuntimeException("Attempted to evaluate 'Transactions' function with invalid or extra " +
                                           "argument type " + child.getName());
    }
    ensureTransactionsInDatabase();
    StringBuffer transQuery = new StringBuffer("SELECT TransactionId FROM Transaction WHERE ");
    transQuery.append(" SequenceId=").append(seqId.toString());
    transQuery.append(" && PartialPlanId IN (").append(ppIds).append(") ");
    if(name != null)
      transQuery.append(nameQueryStr(name, "Transactions"));
    if(type != null)
      transQuery.append(nameQueryStr(type, "Type"));

    EnumeratedDomain retval = new EnumeratedDomain();
    try {
      ResultSet transIds = MySQLDB.queryDatabase(transQuery.toString());
      while(transIds.next())
        retval.add(new Integer(transIds.getInt("TransactionId")));
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage() + "\nExecuting " + transQuery.toString());
    }
    return retval;
  }

  private Object entity(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "ENTITY", 
                   "Attempted to evaluate 'Entity' function with invalid type " +
                   xml.getName());
    IXMLElement indexElem = xml.getFirstChildNamed("Index");
    IXMLElement domElem = xml.getFirstChildNamed("Domain");

    int index = Integer.parseInt(indexElem.getFirstChildNamed("EnumeratedDomain").getContent());

    EnumeratedDomain dom = (EnumeratedDomain) evaluate(domElem.getChildAtIndex(0), ppIds);
    return dom.get(index);
  }

  private Object entity(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "ENTITY", 
                   "Attempted to evaluate 'Entity' function with invalid type " +
                   xml.getName());
    IXMLElement indexElem = xml.getFirstChildNamed("Index");
    IXMLElement domElem = xml.getFirstChildNamed("Domain");

    int index = Integer.parseInt(indexElem.getFirstChildNamed("EnumeratedDomain").getContent());

    EnumeratedDomain dom = (EnumeratedDomain) evaluate(domElem.getChildAtIndex(0));
    return dom.get(index);
  }

  private EnumeratedDomain count(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Count", "Attempted to evaluate 'Count' function with invalid type " +
                   xml.getName());
    Object dom = evaluate(xml.getChildAtIndex(0), ppIds);
    if(!(dom instanceof EnumeratedDomain))
      throw new TestLangRuntimeException("Attempted to evaluate 'Count' on non-enumerated domain.");
    EnumeratedDomain retval = count((EnumeratedDomain) dom);
    return retval;
  }

  private EnumeratedDomain count(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Count", "Attempted to evaluate 'Count' function with invalid type " +
                   xml.getName());
    Object dom = evaluate(xml.getChildAtIndex(0));
    if(!(dom instanceof EnumeratedDomain))
      throw new TestLangRuntimeException("Attempted to evaluate 'Count' on non-enumerated domain.");
    EnumeratedDomain retval = count((EnumeratedDomain) dom);
    return retval;
  }

  private EnumeratedDomain count(EnumeratedDomain dom) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain retval = new EnumeratedDomain();
    retval.add(new Integer(dom.size()));
    return retval;
  }


  private Comparable evalValue(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Value", "Attempted to evaluate a value with invalid type " +
                   xml.getName());
    Comparable retval = null;
    if(xml.getAttribute("type", "").equals("string"))
      retval = xml.getContent().trim();
    else if(xml.getAttribute("type", "").equals("integer"))
      retval = Integer.valueOf(xml.getContent().trim());
    else
      throw new TestLangRuntimeException("Attempted to evaluate a value with invalid data type " +
                                         xml.getAttribute("type", ""));
    return retval;
  }

  private Comparable evalValue(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "Value", "Attempted to evaluate a value with invalid type " +
                   xml.getName());
    Comparable retval = null;
    if(xml.getAttribute("type", "").equals("string"))
      retval = xml.getContent().trim();
    else if(xml.getAttribute("type", "").equals("integer"))
      retval = Integer.valueOf(xml.getContent().trim());
    else
      throw new TestLangRuntimeException("Attempted to evaluate a value with invalid data type " +
                                         xml.getAttribute("type", ""));
    return retval;
  }

  private EnumeratedDomain buildEnumDomain(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "EnumeratedDomain", "Attempted to evaluate an enumerated domain with " +
                   "invalid type " + xml.getName());
    EnumeratedDomain retval = new EnumeratedDomain();
    for(Iterator it = xml.getChildren().iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      checkValidType(child.getName(), "Value", "Attemped to evaluate an enumerated domain with non-value '" +
                     child.getName());
      retval.add((Comparable)evaluate(child, ppIds));
    }
    return retval;
  }

  private EnumeratedDomain buildEnumDomain(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "EnumeratedDomain", "Attempted to evaluate an enumerated domain with " +
                   "invalid type " + xml.getName());
    EnumeratedDomain retval = new EnumeratedDomain();
    for(Iterator it = xml.getChildren().iterator(); it.hasNext();) {
      IXMLElement child = (IXMLElement) it.next();
      checkValidType(child.getName(), "Value", "Attemped to evaluate an enumerated domain with non-value '" +
                     child.getName());
      retval.add((Comparable)evaluate(child));
    }
    return retval;
  }

  private IntervalDomain buildIntDomain(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "IntervalDomain", "Attempted to evaluate an interval domain with " +
                   "invalid type " + xml.getName());
    IXMLElement lbElem = xml.getFirstChildNamed("LowerBound");
    IXMLElement ubElem = xml.getFirstChildNamed("UpperBound");

    Comparable lb = evalValue(lbElem.getFirstChildNamed("Value"));
    Comparable ub = evalValue(ubElem.getFirstChildNamed("Value"));
    
    if(lb.compareTo(ub) > -1)
      throw new TestLangRuntimeException("Attempted to evaluate an improperly formatted interval domain.\n" +
                                         "Interval domains hav eformat '[lowerBound..upperBound]'");
    return new IntervalDomain(lb, ub);
  }

  private IntervalDomain buildIntDomain(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "IntervalDomain", "Attempted to evaluate an interval domain with " +
                   "invalid type " + xml.getName());
    IXMLElement lbElem = xml.getFirstChildNamed("LowerBound");
    IXMLElement ubElem = xml.getFirstChildNamed("UpperBound");

    Comparable lb = evalValue(lbElem.getFirstChildNamed("Value"));
    Comparable ub = evalValue(ubElem.getFirstChildNamed("Value"));
    
    if(lb.compareTo(ub) > -1)
      throw new TestLangRuntimeException("Attempted to evaluate an improperly formatted interval domain.\n" +
                                         "Interval domains hav eformat '[lowerBound..upperBound]'");
    return new IntervalDomain(lb, ub);
  }

  private Domain [] getBinaryChildren(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Domain [] retval = new Domain[2];
    IXMLElement child0 = xml.getChildAtIndex(0);
    IXMLElement child1 = xml.getChildAtIndex(1);
    retval[0] = (Domain) evaluate(child0, ppIds);
    retval[1] = (Domain) evaluate(child1, ppIds);
    return retval;
  }

  private Boolean lessThan(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "lt", "Attempted to evaluate '<' with invalid type " +
                   xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return lessThan(children[0], children[1]);
  }

  private Boolean lessThan(Domain first, Domain second) {
    Utilities.tracePrint(tracePrint);
    Boolean retval = new Boolean(getGreatest(first).compareTo(getLeast(second)) < 0);
    Utilities.debugPrint(boolPrint, "< " + first + " " + second + " " + retval);
    return retval;
  }

  private Boolean greaterThan(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "gt", "Attempted to evaluate '>' with invalid type " +
                   xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return greaterThan(children[0], children[1]);
  }

  private Boolean greaterThan(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Boolean retval = new Boolean(getLeast(first).compareTo(getGreatest(second)) > 0);
    Utilities.debugPrint(boolPrint, "> " + first + " " + second + " " + retval);
    return retval;
  }

  private Boolean lessThanOrEqual(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "le", "Attempted to evaluate '<=' with invalid type " +
                   xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return lessThanOrEqual(children[0], children[1]);
  }

  private Boolean lessThanOrEqual(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Boolean retval = Boolean.FALSE;
    if((first.isSingleton() && second.isSingleton()) ||
       (first instanceof IntervalDomain && second instanceof IntervalDomain) ||
       (first instanceof EnumeratedDomain && second instanceof EnumeratedDomain))
      retval = equalTo(first, second);
    retval = new Boolean(retval.booleanValue() || lessThan(first, second).booleanValue());
    Utilities.tracePrint(boolPrint, "<= " + first + " " + second + " " + retval);
    return retval;
  }

  private Boolean greaterThanOrEqual(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), ">=", "Attempted to evaluate '>=' with invalid type " + xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return greaterThanOrEqual(children[0], children[1]);
  }

  private Boolean greaterThanOrEqual(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Boolean retval = Boolean.FALSE;
    if((first.isSingleton() && second.isSingleton()) ||
       (first instanceof IntervalDomain && second instanceof IntervalDomain) ||
       (first instanceof EnumeratedDomain && second instanceof EnumeratedDomain))
      retval = equalTo(first, second);
    retval = new Boolean(retval.booleanValue() || greaterThan(first, second).booleanValue());
    Utilities.debugPrint(boolPrint, ">= " + first + " " + second + " " + retval);
    return retval;
  }

  private Boolean equalTo(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "eq", "Attempted to evaluate '=' with invalid type " + xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return equalTo(children[0], children[1]);
  }
  
  private Boolean equalTo(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(!(first.isSingleton() && second.isSingleton()) && 
       ((first instanceof IntervalDomain ^ second instanceof IntervalDomain) ||
        (first instanceof EnumeratedDomain ^ second instanceof EnumeratedDomain)))
      throw new TestLangRuntimeException("Arguments to '=' must be both singletons, enumerated domains, " +
                                         "or interval domains.");
    if(first.isSingleton() && second.isSingleton()) {
      Boolean retval = new Boolean(first.getFirst().compareTo(second.getFirst()) == 0);
      Utilities.debugPrint(boolPrint, "= " + first + " " + second + " " + retval);
      return retval;
    }
    if(first instanceof IntervalDomain) {
      Boolean retval = new Boolean(getLeast(first).compareTo(getLeast(second)) == 0 &&
                                   getGreatest(first).compareTo(getGreatest(second)) == 0);
      Utilities.debugPrint(boolPrint, "= " + first + " " + second + " " + retval);
      return retval;
    }
    boolean retval = false;
    List flist = CollectionUtils.tempSort((EnumeratedDomain)first);
    List slist = CollectionUtils.tempSort((EnumeratedDomain)second);
    retval = flist.size() == slist.size();
    for(Iterator fIt = flist.iterator(), sIt = slist.iterator(); 
        fIt.hasNext() && sIt.hasNext() && retval;) {
      retval = ((Comparable)fIt.next()).compareTo((Comparable)sIt.next()) == 0;
    }
    Utilities.debugPrint(boolPrint, "= " + first + " " + second + " " + retval);
    return new Boolean(retval);
  }

  private Boolean notEqual(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "ne", "Attempted to evaluate '!=' with invalid type " + xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return notEqual(children[0], children[1]);
  }
  
  private Boolean notEqual(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint); 
    Boolean retval = new Boolean(!(equalTo(first, second).booleanValue()));
    Utilities.debugPrint(boolPrint, "!= " + first + " " + second + " " + retval);
    return retval;
  }
  
  private Boolean in(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "in", "Attempted to evaluate 'in' with invalid type " + xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return in(children[0], children[1]);
  }

  private Boolean in(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(second.isSingleton() && !first.isSingleton())
      throw new TestLangRuntimeException("First argument to 'in' must be a singleton if the second is a singleton");
    if(first instanceof IntervalDomain && second instanceof EnumeratedDomain)
      throw new TestLangRuntimeException("First argument to 'in' must be an enumerated domain if the second is enumerated.");
    if(first.isSingleton() && second.isSingleton())
      return equalTo(first, second);
    if(first instanceof IntervalDomain) {
      Boolean retval = new Boolean(getLeast(first).compareTo(getLeast(second)) >= 0 && 
                                   getGreatest(first).compareTo(getGreatest(second)) <= 0);
      Utilities.debugPrint(boolPrint, "in " + first + " " + second + " " + retval);
      return retval;
    }
    if(second instanceof IntervalDomain) {
      boolean retval = true;
      for(Iterator fIt = ((EnumeratedDomain)first).iterator(); fIt.hasNext() && retval;) {
        Comparable comp = (Comparable) fIt.next();
        retval = retval && (comp.compareTo(getLeast(second)) >= 0) && 
          (comp.compareTo(getGreatest(second)) <= 0);
      }
      Utilities.debugPrint(boolPrint, "in " + first + " " + second + " " + retval);
      return new Boolean(retval);
    }
    boolean retval = true;
    for(Iterator fIt = ((EnumeratedDomain)first).iterator(); fIt.hasNext() && retval;) {
      boolean temp = false;
      Comparable comp = (Comparable) fIt.next();
      for(Iterator sIt = ((EnumeratedDomain)second).iterator(); sIt.hasNext() && !temp;) {
        temp = comp.compareTo((Comparable) sIt.next()) == 0;
      }
      retval = retval && temp;
    }
    Utilities.debugPrint(boolPrint, "in " + first + " " + second + " " + retval);
    return new Boolean(retval);
  }  

  private Boolean out(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "out", "Attempted to evaluate 'out' with invalid type " + xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return out(children[0], children[1]);
  }
  
  private Boolean out(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint); 
    Boolean retval = new Boolean(!(in(first, second).booleanValue()));
    Utilities.debugPrint(boolPrint, "out " + first + " " + second + " " + retval);
    return retval;
  }

  private Boolean intersects(IXMLElement xml, String ppIds) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(xml.getName(), "intersects", "Attempted to evaluate 'intersects' with invalid type " + xml.getName());
    Domain [] children = getBinaryChildren(xml, ppIds);
    Utilities.debugPrint(boolPrint, xml.getAttribute("file", "UnknownFile") + ", line " +
                         xml.getAttribute("lineNo", "UnknownLine") + " : " + xml.getAttribute("lineText", ""));
    return intersects(children[0], children[1]);
  }

  private Boolean intersects(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(first.isSingleton() && second.isSingleton()) {
      Boolean retval = equalTo(first, second);
      Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " " + retval);
      return retval;
    }
    if(first instanceof IntervalDomain) {
      if(second instanceof IntervalDomain) {
        Boolean retval = new Boolean((getLeast(first).compareTo(getLeast(second)) >= 0 && 
                                      getLeast(first).compareTo(getGreatest(second)) <= 0) ||
                                     (getGreatest(first).compareTo(getLeast(second)) >= 0 && 
                                      getGreatest(first).compareTo(getGreatest(second)) <= 0));
        Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " " + retval);
        return retval;
      }
      for(Iterator sIt = ((EnumeratedDomain)second).iterator(); sIt.hasNext();) {
        Comparable comp = (Comparable) sIt.next();
        if(comp.compareTo(getLeast(first)) >= 0 && comp.compareTo(getGreatest(first)) <= 0) {
          Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " true");
          return Boolean.TRUE;
        }
      }
      Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " false");      
      return Boolean.FALSE;
    }
    if(second instanceof IntervalDomain) {
      for(Iterator fIt = ((EnumeratedDomain) first).iterator(); fIt.hasNext();) {
        Comparable comp = (Comparable) fIt.next();
        if(comp.compareTo(getLeast(second)) >= 0 && comp.compareTo(getGreatest(second)) <= 0) {
          Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " true");      
          return Boolean.TRUE;
        }
      }
      Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " false");      
      return Boolean.FALSE;
    }
    for(Iterator fIt = ((EnumeratedDomain)first).iterator(); fIt.hasNext();) {
      Comparable comp = (Comparable) fIt.next();
      for(Iterator sIt = ((EnumeratedDomain)second).iterator(); sIt.hasNext();) {
        if(comp.compareTo((Comparable) sIt.next()) == 0) {
          Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " true");      
          return Boolean.TRUE;
        }
      }
    }
    Utilities.debugPrint(boolPrint, "intersects " + first + " " + second + " false");      
    return Boolean.FALSE;
  }

  private Comparable getGreatest(Domain d) {
    if(d instanceof EnumeratedDomain) {
      return (Comparable) CollectionUtils.findGreatest((EnumeratedDomain)d);
    }
    return ((IntervalDomain)d).getLast();
  }
  
  private Comparable getLeast(Domain d) {
    if(d instanceof EnumeratedDomain) {
      return (Comparable) CollectionUtils.findLeast((EnumeratedDomain)d);
    }
    return ((IntervalDomain)d).getFirst();
  }

  private Map getPartialPlanIds(IXMLElement stepElem) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(stepElem.getName(), "step", "Attempted to evaluate step assertion with invalid type " +
                   stepElem.getName());
    StringBuffer comparison = new StringBuffer("StepNum ");
    String op = stepElem.getAttribute("operator", "");
    IXMLElement rhs = null;
    Object val = null;

    rhs = stepElem.getChildAtIndex(0);
    val = evaluate(rhs);

    if(op.equals("in")) {
      if(!(val instanceof Domain))
        throw new TestLangRuntimeException("RHS of 'in' in step statement must be a domain.");
      if(val instanceof IntervalDomain) {
        IntervalDomain dom = (IntervalDomain) val;
        comparison.append(">= ").append(dom.getFirst().toString()).append(" && StepNum <= ");
        comparison.append(dom.getLast().toString());
      }
      else if(val instanceof EnumeratedDomain) {
        comparison.append(" IN (");
        for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
          comparison.append(it.next().toString());
          if(it.hasNext())
            comparison.append(", ");
        }
        comparison.append(")");
      }
      else 
        throw new TestLangRuntimeException("RHS of 'in' in step statement is of invalid domain type '" +
                                           val.getClass().getName() + "'");
    }
    else if(op.equals("out")) {
      if(!(val instanceof Domain))
        throw new TestLangRuntimeException("RHS of 'out' in step statement must be a domain.");
      if(val instanceof IntervalDomain) {
        IntervalDomain dom = (IntervalDomain) val;
        comparison.append("<= ").append(dom.getFirst().toString()).append(" || StepNum >= ");
        comparison.append(dom.getLast().toString());
      }
      else if(val instanceof EnumeratedDomain) {
        comparison = new StringBuffer("!(").append(comparison);
        comparison.append(" IN (");
        for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
          comparison.append(it.next().toString());
          if(it.hasNext())
            comparison.append(", ");
        }
        comparison.append("))");
      }
      else
        throw new TestLangRuntimeException("RHS of 'out' in step statement is of invalid domain type '" +
                                           val.getClass().getName() + "'");
    }
    else if(op.equals("=") || op.equals("==")) {
      if(val instanceof Integer) {
        Integer ival = (Integer) val;
        comparison.append("= ").append(ival.toString());
      }
      else if(val instanceof Domain && ((Domain)val).isSingleton())
        comparison.append("= ").append(((Domain)val).getFirst().toString());
      else if(val instanceof String)
        comparison.append("= ").append(val.toString());
      else
        throw new TestLangRuntimeException("RHS of '=' in step statement must be integer or related special value.");
    }
    else if(op.equals("!=")) {
      if(val instanceof Integer) {
        Integer ival = (Integer) val;
        comparison.append("!= ").append(ival.toString());
      }
      else if(val instanceof Domain && ((Domain) val).isSingleton())
        comparison.append("!= ").append(((Domain)val).getFirst().toString());
      else
        throw new TestLangRuntimeException("RHS of '!=' in step statement must be integer.");
    }
    else if(op.equals("<")) {
      if(val instanceof Integer)
        comparison.append("< ").append(val.toString());
      else if(val instanceof Domain)
        comparison.append("< ").append(getLeast((Domain) val).toString());
      else
        throw new TestLangRuntimeException("RHS of '<' in step statement is '" + val.getClass().getName() + "'." +
                                           "Must be Integer or a Domain.");
    }
    else if(op.equals(">")) {
      if(val instanceof Integer)
        comparison.append("> ").append(val.toString());
      else if(val instanceof Domain)
        comparison.append("> ").append(getGreatest((Domain) val).toString());
      else
        throw new TestLangRuntimeException("RHS of '>' in step statement is '" + val.getClass().getName() + "'." +
                                           "Must be Integer or a Domain.");
    }
    else if(op.equals(">=")) {
      if(val instanceof Integer)
        comparison.append(">= ").append(val.toString());
      else if(val instanceof IntervalDomain) {
        IntervalDomain dom = (IntervalDomain) val;
        comparison.append(">= ").append(dom.getFirst().toString());
      }
      else if(val instanceof EnumeratedDomain) {
        EnumeratedDomain dom = (EnumeratedDomain) val;
        comparison.append(" >= ").append(getGreatest(dom).toString()).append(" || ");
        comparison.append(" StepNum IN (");
        for(Iterator it = dom.iterator(); it.hasNext();) {
          comparison.append(it.next().toString());
          if(it.hasNext())
            comparison.append(", ");
        }
        comparison.append(")");
      }
      else
        throw new TestLangRuntimeException("RHS of '>=' in step statement is '" + val.getClass().getName() + "'." +
                                           "Must be Integer or a Domain.");
    }
    else if(op.equals("<=")) {
      if(val instanceof Integer)
        comparison.append("<= ").append(val.toString());
      else if(val instanceof Domain) {
        comparison.append("<= ").append(getGreatest((Domain)val).toString());
        if(val instanceof EnumeratedDomain) {
          comparison.append(" || StepNum IN (");
          for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
            comparison.append(it.next().toString());
            if(it.hasNext())
              comparison.append(", ");
          }
          comparison.append(")");
        }
      }
      else
        throw new TestLangRuntimeException("RHS of '<=' in step statement is '" + val.getClass().getName() + "'." +
                                           "Must be Integer or a Domain.");
    }
    else
      throw new TestLangRuntimeException("'" + op + "' is not a valid op in a step statement");

    Map retval = MySQLDB.queryStepNumPartialPlanIds(seqId, comparison.toString());
    if(retval.size() == 0)
      throw new TestLangRuntimeException("No steps found matching criteria.");
    ensurePartialPlansInDatabase(retval.values());
    return retval;
  }

  private String nameQueryStr(IXMLElement name, String type) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    StringBuffer retval = new StringBuffer(" && ");
    if(type.equals("Tokens"))
      retval.append("PredicateName ");
    else if(type.equals("Objects"))
      retval.append("ObjectName ");
    else if(type.equals("Transactions"))
      retval.append("TransactionName ");
    else if(type.equals("Type"))
      retval.append("TransactionType ");
    else
      throw new TestLangRuntimeException("Attempted to assemble name query for invald type " + type); 

    String op = name.getAttribute("operator", "");
    IXMLElement rhs = name.getChildAtIndex(0);
    Object val = evaluate(rhs);
    if(op.equals("=") || op.equals("==")) {
      if(val instanceof String)
        retval.append("= '").append(val.toString()).append("'");
      else if(val instanceof EnumeratedDomain) {
        EnumeratedDomain dom = (EnumeratedDomain) val;
        if(dom.getFirst() instanceof String)
          retval.append("= '").append(dom.getFirst().toString()).append("'");
        else
          throw new TestLangRuntimeException("RHS of '=' in predicate/name statement must be a string.");
      }
      else
        throw new TestLangRuntimeException("RHS of '=' in predicate/name statement must be a string.");
    }
    else if(op.equals("!=")) {
      if(val instanceof String)
        retval.append("!= '").append(val.toString()).append("'");
      else if(val instanceof EnumeratedDomain) {
        EnumeratedDomain dom = (EnumeratedDomain) val;
        if(dom.getFirst() instanceof String)
          retval.append("!= '").append(dom.getFirst().toString()).append("'");
        else
          throw new TestLangRuntimeException("RHS of '!=' in predicate/name statement must be a string.");
      }
      else
        throw new TestLangRuntimeException("RHS of '!=' in predicate/name statement must be a string\n");
    }
    else if(op.equals("in")) {
      if(val instanceof EnumeratedDomain) {
        retval.append(" IN (");
        for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
          retval.append("'").append(it.next().toString()).append("'");
          if(it.hasNext())
            retval.append(", ");
        }
        retval.append(")");
      }
      else
        throw new TestLangRuntimeException("RHS of 'in' in predicate/name statement must be an enumerated domain");
    }
    else if(op.equals("out")) {
      if(val instanceof EnumeratedDomain) {
        retval.append(" NOT IN (");
        for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
          retval.append("'").append(it.next().toString()).append("'");
          if(it.hasNext())
            retval.append(", ");
        }
        retval.append(")");
      }
      else
        throw new TestLangRuntimeException("RHS of 'out' in predicate/name statement must be an enumerated domain");
    }
    else
      throw new TestLangRuntimeException("'" + op + "' is not a valid operator in a predicate/name statement.");
    return retval.toString();
  }

  private void trimObjectsByVars(EnumeratedDomain objs, String ppIds, List params) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(params.isEmpty())
      return;

    String varQuery = assembleObjectVariableQuery(objs, ppIds);
    Class [] methodSignature = {Domain.class, Domain.class};
    if(varQuery == null)
      return;
    objs.clear();
    HashSet retval = new HashSet();
    try {
      ResultSet vars = MySQLDB.queryDatabase(varQuery);
      while(vars.next()) {
        Integer objectId = new Integer(vars.getInt("ParentId"));
        retval.add(objectId);
        Domain varDom = getVariableDomain(vars);
        for(Iterator it = params.iterator(); it.hasNext();) {
          EnumeratedDomain varName = new EnumeratedDomain();
          varName.add(vars.getString("ParameterName"));
          IXMLElement param = (IXMLElement) it.next();
          String nameOp = param.getFirstChildNamed("name").getAttribute("operator", "");
          Domain nameDom = null;
          Object tempVal = evaluate(param.getFirstChildNamed("name").getChildAtIndex(0));
          if(tempVal instanceof Domain)
            nameDom = (Domain) tempVal;
          else {
            nameDom = new EnumeratedDomain();
            ((EnumeratedDomain)nameDom).add(tempVal);
          }
          Method evalMethod = getClass().getDeclaredMethod((String)methodNames.get(nameOp), methodSignature);
          Object [] args = {varName, nameDom};
          if(((Boolean)evalMethod.invoke(this, args)).booleanValue()) {
            String op = param.getFirstChildNamed("value").getAttribute("operator", "");
            Object temp = evaluate(param.getFirstChildNamed("value").getChildAtIndex(0));
            Domain cmpDom = null;
            if(temp instanceof Domain)
              cmpDom = (Domain) temp;
            else {
              cmpDom = new EnumeratedDomain();
              ((EnumeratedDomain)cmpDom).add(temp);
            }
            evalMethod = getClass().getDeclaredMethod((String)methodNames.get(op), methodSignature);
            args[0] = varDom; args[1] = cmpDom;
            if(!((Boolean)evalMethod.invoke(this, args)).booleanValue())
              retval.remove(objectId);
          }
        }
      }
    }
    catch(Exception e){throw new TestLangRuntimeException(e);}
    objs.addAll(retval);
  }

  private void trimTokensByVars(EnumeratedDomain tokens, String ppIds, IXMLElement start, IXMLElement end, IXMLElement status,
                                List params) 
    throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(start == null && end == null && status == null && params.isEmpty())
      return;
    
    Domain startDom = getAttributeDomain(start);
    Domain endDom = getAttributeDomain(end);
    Domain statusDom = getAttributeDomain(status);

    String startOp = (start == null ? null : start.getAttribute("operator", ""));
    String endOp = (end == null ? null : end.getAttribute("operator", ""));
    String statusOp = (status == null ? null : status.getAttribute("operator", ""));

    Class [] methodSignature = {Domain.class, Domain.class};

    String varQuery = assembleTokenVariableQuery(tokens, ppIds, start, end, status, params);
    if(varQuery == null)
      return;
    tokens.clear();
    HashSet retval = new HashSet();
    try {
      ResultSet vars = MySQLDB.queryDatabase(varQuery);
      while(vars.next()) {
        Integer tokenId = new Integer(vars.getInt("ParentId"));
        retval.add(tokenId);
        Domain varDom = getVariableDomain(vars);
        Domain cmpDom = null;
        String op = null;
        if(startDom != null && vars.getString("VariableType").equals("START_VAR")) {
          cmpDom = startDom;
          op = startOp;
        }
        else if(endDom != null && vars.getString("VariableType").equals("END_VAR")) {
          cmpDom = endDom;
          op = endOp;
        }
        else if(statusDom != null && vars.getString("VariableType").equals("STATE_VAR")) {
          cmpDom = statusDom;
          op = statusOp;
        }
        else if(!params.isEmpty() && vars.getString("VariableType").equals("PARAMETER_VAR")) {
          boolean hasParam = false;
          for(Iterator pIt = params.iterator(); pIt.hasNext();) {
            EnumeratedDomain varName = new EnumeratedDomain();
            varName.add(vars.getString("ParameterName"));
            IXMLElement param = (IXMLElement) pIt.next();
            String nameOp = param.getFirstChildNamed("name").getAttribute("operator", "");
            Domain nameDom = null;
            Object tempVal = evaluate(param.getFirstChildNamed("name").getChildAtIndex(0));
            if(tempVal instanceof Domain)
              nameDom = (Domain) tempVal;
            else {
              nameDom = new EnumeratedDomain();
              ((EnumeratedDomain)nameDom).add(tempVal);
            }
            try {
              Method evalMethod = getClass().getDeclaredMethod((String)methodNames.get(nameOp), methodSignature);
              Object [] args = {varName, nameDom};
              if(((Boolean)evalMethod.invoke(this, args)).booleanValue()) {
                op = param.getFirstChildNamed("value").getAttribute("operator", "");
                Object temp = evaluate(param.getFirstChildNamed("value").getChildAtIndex(0));
                if(temp instanceof Domain)
                  cmpDom = (Domain) temp;
                else {
                  cmpDom = new EnumeratedDomain();
                  ((EnumeratedDomain)cmpDom).add(temp);
                }
                evalMethod = getClass().getDeclaredMethod((String)methodNames.get(op), methodSignature);
                args[0] = varDom; args[1] = cmpDom;
                if(!((Boolean)evalMethod.invoke(this, args)).booleanValue())
                  retval.remove(tokenId);
              }
            }
            catch(Exception e){throw new TestLangRuntimeException(e);}
          }
          continue;
        }
        else
          throw new TestLangRuntimeException("Attempted to trim token list with no assertions or attempted to trim with incorrect var type");
        try {
          Method evalMethod = getClass().getDeclaredMethod((String)methodNames.get(op), methodSignature);
          Object [] args = {varDom, cmpDom};
          if(!((Boolean)evalMethod.invoke(this, args)).booleanValue())
            retval.remove(tokenId);
        }
        catch(Exception e) {
          throw new TestLangRuntimeException(e);
        }
      }
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage());
    }
    tokens.addAll(retval);
  }

  private Domain getAttributeDomain(IXMLElement xml) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(xml == null)
      return null;
    Domain retval;
    //IXMLElement dom = xml.getFirstChildNamed("value").getChildAtIndex(0);
    IXMLElement dom = xml.getChildAtIndex(0);
    Object temp = evaluate(dom);
    if(temp instanceof Domain)
      retval = (Domain) temp;
    else {
      retval = new EnumeratedDomain();
      ((EnumeratedDomain)retval).add(temp);
    }
    return retval;
  }

  private String assembleObjectVariableQuery(EnumeratedDomain objects, String ppIds) {
    Utilities.tracePrint(tracePrint);
    if(objects.isEmpty())
      return null;
    StringBuffer varQuery = new StringBuffer("SELECT ParentId, DomainType, EnumDomain, IntDomainType, IntDomainLowerBound,");
    varQuery.append(" IntDomainUpperBound, VariableType, ParameterName FROM Variable WHERE PartialPlanId IN(");
    varQuery.append(ppIds);
    varQuery.append(") && ParentId IN (");
    for(Iterator it = objects.iterator(); it.hasNext();) {
      varQuery.append(it.next().toString());
      if(it.hasNext())
        varQuery.append(", ");
    }
    varQuery.append(") && VariableType='MEMBER_VAR' ORDER BY ParentId");
    return varQuery.toString();
  }

  private String assembleTokenVariableQuery(EnumeratedDomain tokens, String ppIds, IXMLElement start, IXMLElement end,
                                            IXMLElement status, List params) {
    Utilities.tracePrint(tracePrint);
    if(tokens.isEmpty())
      return null;
    StringBuffer varQuery = new StringBuffer("SELECT ParentId, DomainType, EnumDomain, IntDomainType, IntDomainLowerBound,");
    varQuery.append(" IntDomainUpperBound, VariableType, ParameterName FROM Variable WHERE PartialPlanId IN (");
    varQuery.append(ppIds);
    varQuery.append(") && ParentId IN (");
    for(Iterator it = tokens.iterator(); it.hasNext();) {
      varQuery.append(it.next().toString());
      if(it.hasNext())
        varQuery.append(", ");
    }
    varQuery.append(") && VariableType IN(");
    if(start != null)
      varQuery.append("'START_VAR'");
    if(end != null) {
      if(start != null)
        varQuery.append(", ");
      varQuery.append("'END_VAR'");
    }
    if(status != null) {
      if(start != null || end != null)
        varQuery.append(", ");
      varQuery.append("'STATE_VAR'");
    }
    if(!params.isEmpty()) {
      if(start != null || end != null || status != null)
        varQuery.append(", ");
      varQuery.append("'PARAMETER_VAR'");
    }
    varQuery.append(") ORDER BY ParentId");
    return varQuery.toString();
  }

  private Domain getVariableDomain(ResultSet var) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Domain retval = null;
    try {
      if(var.getString("DomainType").equals("IntervalDomain")) {
        Comparable lb = null, ub = null;
        if(var.getString("IntDomainType").equals("INTEGER_SORT")) {
          try {
            String strLb = var.getString("IntDomainLowerBound");
            String strUb = var.getString("IntDomainUpperBound");
            if(strLb.equals(DbConstants.PLUS_INFINITY))
              lb = new Integer(DbConstants.PLUS_INFINITY_INT);
            else if(strLb.equals(DbConstants.MINUS_INFINITY))
              lb = new Integer(DbConstants.MINUS_INFINITY_INT);
            else
              lb = Integer.valueOf(strLb);
            if(strUb.equals(DbConstants.PLUS_INFINITY))
              ub = new Integer(DbConstants.PLUS_INFINITY_INT);
            else if(strUb.equals(DbConstants.MINUS_INFINITY))
              ub = new Integer(DbConstants.MINUS_INFINITY_INT);
            else
              ub = Integer.valueOf(strUb);
          }
          catch(NumberFormatException nfe) {
            throw new TestLangRuntimeException(nfe);
          }
        }
        else if(var.getString("IntDomainType").equals("REAL_SORT")){
          try {
            //we don't have constants for +/- double infinity
            lb = Double.valueOf(var.getString("IntDomainLowerBound"));
            ub = Double.valueOf(var.getString("IntDomainUpperBound"));
          }
          catch(NumberFormatException nfe) {
            throw new TestLangRuntimeException(nfe);
          }
        }
        else
          throw new TestLangRuntimeException("Found an interval domain that is neither integer or real.");
        retval = new IntervalDomain(lb, ub);
      }
      else if(var.getString("DomainType").equals("EnumeratedDomain")) {
        retval = new EnumeratedDomain();
        Blob blob = var.getBlob("EnumDomain");
        StringTokenizer strTok = new StringTokenizer(new String(blob.getBytes(1, (int) blob.length())));
        while(strTok.hasMoreTokens()) {
          String str = strTok.nextToken();
          if(str.equals(DbConstants.PLUS_INFINITY))
            ((EnumeratedDomain)retval).add(new Integer(DbConstants.PLUS_INFINITY_INT));
          else if(str.equals(DbConstants.MINUS_INFINITY))
            ((EnumeratedDomain)retval).add(new Integer(DbConstants.MINUS_INFINITY_INT));
          else {
            if(!str.matches("^-?\\d+$"))
              ((EnumeratedDomain)retval).add(str);
              //((EnumeratedDomain)retval).add("'" + str + "'");
            else {
              try {((EnumeratedDomain)retval).add(Integer.valueOf(str));}
              catch(NumberFormatException nfe) {throw new TestLangRuntimeException(nfe);}
            }
          }
        }
      }
      else
        throw new TestLangRuntimeException("Found a domain that is neither interval nor enumerated.");
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage());
    }
    return retval;
  }

  private void ensurePartialPlansInDatabase(Collection ids) throws TestLangRuntimeException {
    List idsInDb = MySQLDB.queryPlanIdsInDatabase(seqId);
    List tempIds = new LinkedList(ids);
    tempIds.removeAll(idsInDb);
    for(Iterator it = tempIds.iterator(); it.hasNext();) {
      Long ppId = (Long) it.next();
      try{seq.getPartialPlan(ppId);}
      catch(ResourceNotFoundException rnfe){throw new TestLangRuntimeException(rnfe);}
      catch(CreatePartialPlanException cppe){throw new TestLangRuntimeException(cppe);}
    }
  } 

  private void ensureTransactionsInDatabase() throws TestLangRuntimeException {
    if(MySQLDB.transactionsInDatabase(seqId))
      return;
    MySQLDB.loadFile(seq.getUrl() + System.getProperty("file.separator") + DbConstants.SEQ_TRANSACTIONS,
                     DbConstants.TBL_TRANSACTION);
  }

  private boolean internalTests() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    return testCount() && testLessThan() && testGreaterThan() && testEqualTo() && 
      testIn() && testIntersects();
    
  }

  private boolean testCount() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain sizeOne = new EnumeratedDomain();
    sizeOne.add(new Integer(1));

    EnumeratedDomain sizeFour = new EnumeratedDomain();
    sizeFour.add(new Integer(1));
    sizeFour.add(new Integer(2));
    sizeFour.add(new Integer(3));
    sizeFour.add(new Integer(4));
    
    int randSize = ((int) Math.floor(Math.random() * 100)) + 1;
    EnumeratedDomain sizeRand = new EnumeratedDomain();
    for(int i = 0; i < randSize; i++)
      sizeRand.add(new Integer(i));

    EnumeratedDomain countOne = count(sizeOne);
    if(((Integer)countOne.getFirst()).intValue() != 1)
      return false;
    EnumeratedDomain countFour = count(sizeFour);
    if(((Integer)countFour.getFirst()).intValue() != 4)
      return false;
    EnumeratedDomain countRand = count(sizeRand);
    if(((Integer)countRand.getFirst()).intValue() != randSize)
      return false;
    return true;
  }

  private boolean testLessThan() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain three = new EnumeratedDomain();
    three.add(new Integer(3));
    EnumeratedDomain four = new EnumeratedDomain();
    four.add(new Integer(4));
    EnumeratedDomain four2 = new EnumeratedDomain();
    four2.add(new Integer(4));
    
    if(lessThan(three, four).equals(Boolean.FALSE))
      return false;
    if(lessThan(four, three).equals(Boolean.TRUE))
      return false;
    if(lessThan(four, four2).equals(Boolean.TRUE))
      return false;

    EnumeratedDomain enumeratedOne = new EnumeratedDomain();
    enumeratedOne.add(new Integer(5));
    enumeratedOne.add(new Integer(6));
    enumeratedOne.add(new Integer(7));
    
    EnumeratedDomain enumeratedTwo = new EnumeratedDomain();
    enumeratedTwo.add(new Integer(9));
    enumeratedTwo.add(new Integer(10));
    enumeratedTwo.add(new Integer(11));

    EnumeratedDomain enumeratedThree = new EnumeratedDomain();
    enumeratedThree.add(new Integer(7));
    enumeratedThree.add(new Integer(8));
    enumeratedThree.add(new Integer(9));

    if(lessThan(enumeratedOne, enumeratedTwo).equals(Boolean.FALSE))
      return false;
    if(lessThan(enumeratedTwo, enumeratedOne).equals(Boolean.TRUE))
      return false; 
    if(lessThan(enumeratedOne, enumeratedThree).equals(Boolean.TRUE))
      return false;
    if(lessThan(enumeratedThree, enumeratedTwo).equals(Boolean.TRUE))
      return false;

    IntervalDomain intervalOne = new IntervalDomain(new Integer(5), new Integer(7));
    IntervalDomain intervalTwo = new IntervalDomain(new Integer(9), new Integer(11));
    IntervalDomain intervalThree = new IntervalDomain(new Integer(7), new Integer(9));

    if(lessThan(intervalOne, intervalTwo).equals(Boolean.FALSE))
      return false;
    if(lessThan(intervalTwo, intervalOne).equals(Boolean.TRUE))
      return false;
    if(lessThan(intervalOne, intervalThree).equals(Boolean.TRUE))
      return false;
    if(lessThan(intervalThree, intervalTwo).equals(Boolean.TRUE))
      return false;

    if(lessThan(enumeratedOne, intervalTwo).equals(Boolean.FALSE))
      return false;
    if(lessThan(intervalTwo, enumeratedOne).equals(Boolean.TRUE))
      return false;
    if(lessThan(enumeratedOne, intervalThree).equals(Boolean.TRUE))
      return false;
    if(lessThan(intervalTwo, enumeratedThree).equals(Boolean.TRUE))
      return false;

    return true;
  }

  private boolean testGreaterThan() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain three = new EnumeratedDomain();
    three.add(new Integer(3));
    EnumeratedDomain four = new EnumeratedDomain();
    four.add(new Integer(4));
    EnumeratedDomain four2 = new EnumeratedDomain();
    four2.add(new Integer(4));
    
    if(greaterThan(three, four).equals(Boolean.TRUE))
      return false;
    if(greaterThan(four, three).equals(Boolean.FALSE))
      return false;
    if(greaterThan(four, four2).equals(Boolean.TRUE))
      return false;

    EnumeratedDomain enumeratedOne = new EnumeratedDomain();
    enumeratedOne.add(new Integer(5));
    enumeratedOne.add(new Integer(6));
    enumeratedOne.add(new Integer(7));
    
    EnumeratedDomain enumeratedTwo = new EnumeratedDomain();
    enumeratedTwo.add(new Integer(9));
    enumeratedTwo.add(new Integer(10));
    enumeratedTwo.add(new Integer(11));

    EnumeratedDomain enumeratedThree = new EnumeratedDomain();
    enumeratedThree.add(new Integer(7));
    enumeratedThree.add(new Integer(8));
    enumeratedThree.add(new Integer(9));

    if(greaterThan(enumeratedOne, enumeratedTwo).equals(Boolean.TRUE))
      return false;
    if(greaterThan(enumeratedTwo, enumeratedOne).equals(Boolean.FALSE))
      return false; 
    if(greaterThan(enumeratedOne, enumeratedThree).equals(Boolean.TRUE))
      return false;
    if(greaterThan(enumeratedThree, enumeratedTwo).equals(Boolean.TRUE))
      return false;

    IntervalDomain intervalOne = new IntervalDomain(new Integer(5), new Integer(7));
    IntervalDomain intervalTwo = new IntervalDomain(new Integer(9), new Integer(11));
    IntervalDomain intervalThree = new IntervalDomain(new Integer(7), new Integer(9));

    if(greaterThan(intervalOne, intervalTwo).equals(Boolean.TRUE))
      return false;
    if(greaterThan(intervalTwo, intervalOne).equals(Boolean.FALSE))
      return false;
    if(greaterThan(intervalOne, intervalThree).equals(Boolean.TRUE))
      return false;
    if(greaterThan(intervalThree, intervalTwo).equals(Boolean.TRUE))
      return false;

    if(greaterThan(enumeratedOne, intervalTwo).equals(Boolean.TRUE))
      return false;
    if(greaterThan(intervalTwo, enumeratedOne).equals(Boolean.FALSE))
      return false;
    if(greaterThan(enumeratedTwo, intervalOne).equals(Boolean.FALSE))
      return false;
    if(greaterThan(enumeratedOne, intervalThree).equals(Boolean.TRUE))
      return false;
    if(lessThan(intervalTwo, enumeratedThree).equals(Boolean.TRUE))
      return false;

    return true;
  }

  private boolean testEqualTo() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain one = new EnumeratedDomain();
    one.add(new Integer(1));
    EnumeratedDomain two = new EnumeratedDomain();
    two.add(new Integer(2));
    EnumeratedDomain two2 = new EnumeratedDomain();
    two2.add(new Integer(2));

    if(equalTo(one, two).equals(Boolean.TRUE))
      return false;
    if(equalTo(two, two2).equals(Boolean.FALSE))
      return false;

    EnumeratedDomain enumeratedOne = new EnumeratedDomain();
    EnumeratedDomain enumeratedTwo = new EnumeratedDomain();
    for(int i = 0; i < 4; i++) {
      enumeratedOne.add(new Integer(i));
      enumeratedTwo.add(new Integer(i));
    }
    EnumeratedDomain enumeratedThree = new EnumeratedDomain();
    for(int i = 3; i < 7; i++)
      enumeratedThree.add(new Integer(i));

    if(equalTo(enumeratedOne, enumeratedTwo).equals(Boolean.FALSE))
      return false;
    if(equalTo(enumeratedOne, enumeratedThree).equals(Boolean.TRUE))
      return false;

    IntervalDomain intervalOne = new IntervalDomain(new Integer(0), new Integer(3));
    IntervalDomain intervalTwo = new IntervalDomain(new Integer(0), new Integer(3));
    IntervalDomain intervalThree = new IntervalDomain(new Integer(3), new Integer(7));

    if(equalTo(intervalOne, intervalTwo).equals(Boolean.FALSE))
      return false;
    if(equalTo(intervalOne, intervalThree).equals(Boolean.TRUE))
      return false;

    return true;
  }

  private boolean testIn() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain three = new EnumeratedDomain();
    three.add(new Integer(3));

    EnumeratedDomain three2 = new EnumeratedDomain();
    three2.add(new Integer(3));

    EnumeratedDomain nine = new EnumeratedDomain();
    nine.add(new Integer(9));
    
    EnumeratedDomain enumeratedOne = new EnumeratedDomain();
    for(int i = 0; i < 5; i++)
      enumeratedOne.add(new Integer(i));
    
    EnumeratedDomain enumeratedTwo = new EnumeratedDomain();
    for(int i = 2; i < 4; i++)
      enumeratedTwo.add(new Integer(i));

    EnumeratedDomain enumeratedThree = new EnumeratedDomain();
    for(int i = 3; i < 6; i++)
      enumeratedThree.add(new Integer(i));

    if(in(three, enumeratedOne).equals(Boolean.FALSE))
      return false;
    if(in(three, three2).equals(Boolean.FALSE))
      return false;
    if(in(three, nine).equals(Boolean.TRUE))
      return false;
    if(in(enumeratedTwo, enumeratedOne).equals(Boolean.FALSE))
      return false;
    if(in(enumeratedThree, enumeratedOne).equals(Boolean.TRUE))
      return false;
    if(in(nine, enumeratedOne).equals(Boolean.TRUE))
      return false;

    IntervalDomain intervalOne = new IntervalDomain(new Integer(0), new Integer(4));
    IntervalDomain intervalTwo = new IntervalDomain(new Integer(2), new Integer(3));
    IntervalDomain intervalThree = new IntervalDomain(new Integer(3), new Integer(5));
    IntervalDomain intervalFour = new IntervalDomain(new Integer(2), new Integer(3));

    if(in(intervalTwo, intervalOne).equals(Boolean.FALSE))
      return false;
    if(in(intervalOne, intervalTwo).equals(Boolean.TRUE))
      return false;
    if(in(intervalThree, intervalOne).equals(Boolean.TRUE))
      return false;
    if(in(intervalTwo, intervalFour).equals(Boolean.FALSE))
      return false;

    if(in(enumeratedTwo, intervalOne).equals(Boolean.FALSE))
      return false;
      

    return true;
  }

  private boolean testIntersects() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain three = new EnumeratedDomain();
    three.add(new Integer(3));
    EnumeratedDomain three2 = new EnumeratedDomain();
    three2.add(new Integer(3));
    EnumeratedDomain nine = new EnumeratedDomain();
    nine.add(new Integer(9));

    EnumeratedDomain enumeratedOne = new EnumeratedDomain();
    for(int i = 0; i < 6; i++)
      enumeratedOne.add(new Integer(i));
    EnumeratedDomain enumeratedTwo = new EnumeratedDomain();
    for(int i = 3; i < 10; i++)
      enumeratedTwo.add(new Integer(i));
    EnumeratedDomain enumeratedThree = new EnumeratedDomain();
    for(int i = 99; i < 103; i++)
      enumeratedThree.add(new Integer(i));

    if(intersects(three, three2).equals(Boolean.FALSE))
      return false;
    if(intersects(three, nine).equals(Boolean.TRUE))
      return false;
    if(intersects(three, enumeratedOne).equals(Boolean.FALSE))
      return false;
    if(intersects(nine, enumeratedOne).equals(Boolean.TRUE))
      return false;
    if(intersects(enumeratedOne, enumeratedTwo).equals(Boolean.FALSE))
      return false;
    if(intersects(enumeratedTwo, enumeratedOne).equals(Boolean.FALSE))
      return false;
    if(intersects(enumeratedOne, enumeratedThree).equals(Boolean.TRUE))
      return false;
    if(intersects(enumeratedThree, enumeratedOne).equals(Boolean.TRUE))
      return false;

    IntervalDomain intervalOne = new IntervalDomain(new Integer(1), new Integer(9));
    IntervalDomain intervalTwo = new IntervalDomain(new Integer(5), new Integer(15));
    IntervalDomain intervalThree = new IntervalDomain(new Integer(99), new Integer(336));

    if(intersects(intervalOne, intervalTwo).equals(Boolean.FALSE))
      return false;
    if(intersects(intervalTwo, intervalOne).equals(Boolean.FALSE))
      return false;
    if(intersects(intervalOne, intervalThree).equals(Boolean.TRUE))
      return false;
    if(intersects(intervalThree, intervalOne).equals(Boolean.TRUE))
      return false;

    if(intersects(intervalOne, enumeratedOne).equals(Boolean.FALSE))
      return false;
    if(intersects(enumeratedOne, intervalOne).equals(Boolean.FALSE))
      return false;
    if(intersects(intervalThree, enumeratedOne).equals(Boolean.TRUE))
      return false;
    if(intersects(enumeratedOne, intervalThree).equals(Boolean.TRUE))
      return false;
    
    return true;
  }
}
