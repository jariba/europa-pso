package gov.nasa.arc.planworks.dbg.testLang;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwProject;
import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.dbg.testLang.TestLangParseException;
import gov.nasa.arc.planworks.dbg.testLang.TestLangRuntimeException;
import gov.nasa.arc.planworks.util.CollectionUtils;
import gov.nasa.arc.planworks.util.CreatePartialPlanException;
import gov.nasa.arc.planworks.util.DuplicateNameException;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;

public class TestLangHelper implements TestLangTokenTypes {
  public static boolean runInternalTests() throws TestLangRuntimeException {
    return (new TestLangHelper()).internalTests();
    
  }

  public static void runTests(String projectName, String sequenceUrl, String testUrl) throws TestLangParseException, 
    TestLangRuntimeException {
    CommonAST tests = commonInit(projectName, sequenceUrl, testUrl);
    Long seqId = MySQLDB.getSequenceId(sequenceUrl);
    (new TestLangHelper(tests, projectName, seqId)).runTest();
  }

  public static void runTests(String projectName, String sequenceUrl, String testUrl, String testName)
    throws TestLangParseException, TestLangRuntimeException {
    CommonAST tests = commonInit(projectName, sequenceUrl, testUrl);
    Long seqId = MySQLDB.getSequenceId(sequenceUrl);
    CommonAST test = findTest(tests, testName);
    if(test == null)
      throw new TestLangRuntimeException("Test '" + testName + "' doesn't exist in file '" + testUrl + "'");
    (new TestLangHelper(test, projectName, seqId)).runTest();
  }

  private static CommonAST commonInit(String projectName, String sequenceUrl, String testUrl) 
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
      TestLangParser parser = new TestLangParser(new TestLangLexer(new FileInputStream(testFile)));
      parser.test_set();
      return (CommonAST) parser.getAST();
    }
    catch(RecognitionException re) {
      throw new TestLangParseException(re);
    }
    catch(TokenStreamException tse) {
      throw new TestLangParseException(tse);
    }
    catch(FileNotFoundException fnfe) {
      throw new TestLangRuntimeException(fnfe);
    }
  }

  private static CommonAST findTest(CommonAST root, String name) throws TestLangParseException, TestLangRuntimeException {
    if(root.getType() != TEST)
      throw new TestLangRuntimeException("Looking for tests in all the wrong places.");
    CommonAST child = (CommonAST) root.getFirstChild();
    if(child.getType() != STRING)
      throw new TestLangParseException("First child of Test node not a string.");
    if(child.getText().equals("'" + name + "'"))
      return root;
    child = (CommonAST) child.getNextSibling();
    while(child != null) {
      if(child.getType() == TEST) {
        CommonAST retval = findTest(child, name);
        if(retval != null)
          return retval;
      }
      child = (CommonAST) child.getNextSibling();
    }
    return null;
  }

  private static final String [] methodNames;
  private static final Class [] methodArgTypes = {CommonAST.class};
  private static final Class [] twoArgMethodArgTypes = {CommonAST.class, Object.class};

  static {
    methodNames = new String [TestLangTokenTypes.DUMMY+1];
    methodNames[OBRACE] = "buildEnumDomain"; //done
    methodNames[OBRACKET] = "buildIntDomain"; //done
    methodNames[INTEGER] = "evaluateInteger"; //done
    methodNames[IN] = "in"; //done
    methodNames[OUT] = "out"; //done
    methodNames[EQ] = "equalTo"; //done
    methodNames[NE] = "notEqual"; //done
    methodNames[TOKENS] = "tokens"; //done
    methodNames[STRING] = "evaluateString"; //done
    methodNames[OBJECTS] = "objects"; //done
    methodNames[TRANSACTIONS] = "transactions"; //done
    methodNames[ENTITY] = "entity"; //done
    methodNames[COUNT] = "count"; //done
    methodNames[LT] = "lessThan"; //done
    methodNames[GT] = "greaterThan"; //done
    methodNames[GE] = "greaterThanOrEqual"; //done
    methodNames[LE] = "lessThanOrEqual"; //done
    methodNames[TEST] = "test"; //done
    methodNames[INTERSECTS] = "intersects"; //done
  }

  private CommonAST testTree;
  private Long seqId;
  private PwPlanningSequence seq;
  private String name;
  private String projName;
  private boolean tracePrint;

  public TestLangHelper(CommonAST ast, String projName, Long seqId) throws TestLangRuntimeException {
    if(ast == null)
      throw new TestLangRuntimeException("Attempted to run unparsed test!");
    checkValidType(ast.getType(), TEST, "Attempted to instantiate a TestHelper with something other than a test!");
    this.seqId = seqId;
    testTree = ast;
    CommonAST child = (CommonAST) ast.getFirstChild();
    name = (String) evaluateString(child, null);
    this.projName = projName; 
    try {seq = PwProject.getProject(projName).getPlanningSequence(seqId);}
    catch(ResourceNotFoundException rnfe){throw new TestLangRuntimeException(rnfe);}
    //tracePrint = true;
  }
  private TestLangHelper() {
    testTree = null;
    seqId = null;
    seq = null;
    name = "test";
    projName = null;
    tracePrint = true;
  }

  public Boolean runTest() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    CommonAST assertion = (CommonAST) testTree.getFirstChild().getNextSibling();
    while(assertion != null) {
      switch(assertion.getType()) {
      case IN:
      case OUT:
      case EQ:
      case NE:
      case LT:
      case GT:
      case GE:
      case LE:
      case TEST:
      case INTERSECTS:
        break;
      default:
        throw new TestLangRuntimeException("Invalid assertion type " + assertion.getType() +
                                           "\nAt " + assertion.toStringList());
      }
      if(((Boolean)evaluate(assertion)).equals(Boolean.FALSE)) {
        System.err.println("Test " + name + " FAILED at " + assertion.toStringList());
        return Boolean.FALSE;
      }
      assertion = (CommonAST) assertion.getNextSibling();
    }
    System.err.println("Test " + name + " PASSED");
    return Boolean.TRUE;
  }
  private Object evaluate(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(ast == null)
      throw new TestLangRuntimeException("Attempted to evaluate a null element.");
    checkValidType(ast.getType(), "Attempted to evalute invalid type " + ast.getType());
    try {
      Method evalMethod = this.getClass().getDeclaredMethod(methodNames[ast.getType()], methodArgTypes);
      Object [] args = {ast};
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

  private Object evaluate(CommonAST ast, Object arg) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(ast == null)
      throw new TestLangRuntimeException("Attempted to evaluate a null element.");
    checkValidType(ast.getType(), "Attempted to evaluate invalid type " + ast.getType());
    try {
      Method evalMethod = this.getClass().getDeclaredMethod(methodNames[ast.getType()], twoArgMethodArgTypes);
      Object [] args = {ast, arg};
      Object retval = evalMethod.invoke(this, args);
      if(retval instanceof Boolean && !((Boolean)retval).booleanValue())
        throw new TestLangRuntimeException("Assertion failed at " + ast.toStringList());
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

  private Boolean test(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    return (new TestLangHelper(ast, projName, seqId)).runTest();
  }

  private EnumeratedDomain tokens(CommonAST ast)  throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), TOKENS, "Attempted to evaluate 'Tokens' function with invalid type at " +
                   ast.toStringList());
    CommonAST step = null, start = null, end = null, status = null, predicate = null;
    List variables = new LinkedList();
    CommonAST child = (CommonAST) ast.getFirstChild();
    while(child != null) {
      switch(child.getType()) {
      case STEP:
        step = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case START:
        start = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case END:
        end = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case STATUS:
        status = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case PREDICATE:
        predicate = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case VARIABLE:
        variables.add(child);
        child = (CommonAST) child.getNextSibling();
        break;
      default:
        throw new TestLangRuntimeException("Attempted to evaluate 'Tokens' function with invalid argument type " + 
                                           child.getType() + "\nAt " + ast.toStringList());
      }
    }
    String partialPlans = (step != null ? getPartialPlanIdsStr(step) : getAllPartialPlanIdsStr());

    StringBuffer tokenQuery = new StringBuffer("SELECT TokenId FROM Token WHERE PartialPlanId IN (");
    tokenQuery.append(partialPlans).append(") ");
    
    if(predicate != null)
      tokenQuery.append(nameQueryStr(predicate, TOKENS));
    
    EnumeratedDomain retval = new EnumeratedDomain();
    try {
      ResultSet tokIds = MySQLDB.queryDatabase(tokenQuery.toString());
      while(tokIds.next())
        retval.add(new Integer(tokIds.getInt("TokenId")));
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage() + "\nExecuting " + tokenQuery.toString());
    }
    trimTokensByVars(retval, partialPlans, start, end, status, variables);

    return retval;
  }

  private EnumeratedDomain objects(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), OBJECTS, "Attempted to evalute 'Objects' function with invalid type at " +
                   ast.toStringList());
    List variables = new LinkedList();
    CommonAST step = null, name = null;
    CommonAST child = (CommonAST) ast.getFirstChild();
    while(child != null) {
      switch(child.getType()) {
      case STEP:
        step = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case NAME:
        name = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case VARIABLE:
        variables.add(child);
        child = (CommonAST) child.getNextSibling();
        break;
      default:
        throw new TestLangRuntimeException("Attempted to evaluate 'Objects' function with invalid argument type " +
                                           child.getType() + " at " + ast.toStringList());
      }
    }
    String partialPlans = (step != null ? getPartialPlanIdsStr(step) : getAllPartialPlanIdsStr());

    StringBuffer objectQuery = new StringBuffer("SELECT ObjectId FROM Object WHERE PartialPlanId IN (");
    objectQuery.append(partialPlans).append(") ");

    if(name != null)
      objectQuery.append(nameQueryStr(name, OBJECTS));

    EnumeratedDomain retval = new EnumeratedDomain();
    try {
      ResultSet objIds = MySQLDB.queryDatabase(objectQuery.toString());
      while(objIds.next())
        retval.add(new Integer(objIds.getInt("ObjectId")));
    }
    catch(SQLException sqle) {
      throw new TestLangRuntimeException(sqle.getMessage() + "\nExecuting " + objectQuery.toString());
    }
    trimObjectsByVars(retval, partialPlans, variables);
    return retval;
  }

  private EnumeratedDomain transactions(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), TRANSACTIONS, "Attempted to evaluate 'Transactions' function with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    CommonAST step = null, name = null, type = null;
    CommonAST child = (CommonAST) ast.getFirstChild();
    while(child != null) {
      switch(child.getType()) {
      case STEP:
        step = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case NAME:
        name = child;
        child = (CommonAST) child.getNextSibling();
        break;
      case TYPE:
        type = child;
        child = (CommonAST) child.getNextSibling();
        break;
      default:
        throw new TestLangRuntimeException("Attempted to evaluate 'Transactions' function with invalid argument type " +
                                           child.getType() + "\nAt " + ast.toStringList());
      }
    }

    ensureTransactionsInDatabase();
    StringBuffer transQuery = new StringBuffer("SELECT TransactionId FROM Transaction WHERE ");
    transQuery.append(" SequenceId=").append(seqId.toString());
    String partialPlans = "";
    if(step != null)
      transQuery.append(" && PartialPlanId IN (").append(getPartialPlanIdsStr(step)).append(") ");
    if(name != null)
      transQuery.append(nameQueryStr(name, TRANSACTIONS));
    if(type != null)
      transQuery.append(nameQueryStr(type, TYPE));
    
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

  private Object entity(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), ENTITY, "Attempted to evaluate 'Entity' function with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    CommonAST child = (CommonAST) ast.getFirstChild();
    checkValidType(ast.getType(), INTEGER, "Attempted to evaluate 'Entity' function with non-integer first arg." +
                   "\nAt " + ast.toStringList());
    Integer index = null;
    try{index = (Integer) evaluate(child);}
    catch(NumberFormatException nfe) {
      throw new TestLangRuntimeException("Attempted to evaluate 'Entity' function with improperly formatted " +
                                         "integer arg.\nAt " + ast.toStringList());
    }
    child = (CommonAST) child.getNextSibling();
    EnumeratedDomain dom = (EnumeratedDomain) evaluate(child);
    Utilities.debugPrint(tracePrint, "Entity( " + index + ") : " + dom.get(index.intValue()));
    return dom.get(index.intValue());
  }

  private EnumeratedDomain count(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), COUNT, "Attempted to evalute 'Count' function with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    Object dom = evaluate((CommonAST) ast.getFirstChild());
    if(!(dom instanceof EnumeratedDomain))
      throw new TestLangRuntimeException("Attempted to evaluate 'Count' on non-enumerated domain " +
                                         dom.getClass().getName() + "\nAt " + ast.toStringList());
    EnumeratedDomain retval = count((EnumeratedDomain)dom);
    Utilities.debugPrint(tracePrint, "Count : " + retval);
    return retval;
  }

  private EnumeratedDomain count(EnumeratedDomain dom) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    EnumeratedDomain retval = new EnumeratedDomain();
    retval.add(new Integer(((EnumeratedDomain)dom).size()));
    return retval;
  }

  private Object evaluateString(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint, "one-arg : " + ast.getText());
    checkValidType(ast.getType(), STRING, "Attempted to evaluate a singleton string with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    EnumeratedDomain retval = new EnumeratedDomain();
    retval.add(ast.getText());
    return retval;
  }

  private Object evaluateString(CommonAST ast, Object arg) throws TestLangRuntimeException { //if child of domain
    Utilities.tracePrint(tracePrint, "two-arg : " + ast.getText());
    checkValidType(ast.getType(), STRING, "Attempted to evaluate a string with invalid type " + 
                   ast.getType() + "\nAt " + ast.toStringList());
    return new String(ast.getText());
  }

  private Object evaluateInteger(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), INTEGER, "Attempted to evalute a singleton integer with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    EnumeratedDomain retval = new EnumeratedDomain();
    try{retval.add(Integer.valueOf(ast.getText()));}
    catch(NumberFormatException nfe) {
      throw new TestLangRuntimeException("Attempted to evaluate an improperly formatted singleton integer at " +
                                         ast.toStringList());
    }
    return retval;
  }

  private Object evaluateInteger(CommonAST ast, Object arg) throws TestLangRuntimeException { //if child of domain
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), INTEGER, "Attempted to evaluate an integer with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    Integer retval = null;
    try{retval = Integer.valueOf(ast.getText());}
    catch(NumberFormatException nfe) {
      throw new TestLangRuntimeException("Attempted to evaluate an improperly formatted integer at " +
                                         ast.toStringList());
    }
    return retval;
  }

  private EnumeratedDomain buildEnumDomain(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), OBRACE, "Attempted to evaluate an enumerated domain with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    CommonAST child = (CommonAST) ast.getFirstChild();
    EnumeratedDomain retval = new EnumeratedDomain();
    while(child != null) {
      retval.add((Comparable)evaluate(child, null));
      child = (CommonAST) child.getNextSibling();
    }
    return retval;
  }

  private IntervalDomain buildIntDomain(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), OBRACKET, "Attempted to evaluate an interval domain with invalid type " +
                   ast.getType() + "\nAt " + ast.toStringList());
    CommonAST child = (CommonAST) ast.getFirstChild();
    Comparable first = (Comparable) evaluate(child, null);
    child = (CommonAST) child.getNextSibling();
    Comparable last = (Comparable) evaluate(child, null);
    if(first.compareTo(last) > -1) {
      throw new TestLangRuntimeException("Attempted to evaluate an improperly formatted interval doman.\n" +
                                         "Interval domains have format '[lowerBound..upperBound]'");
    }
    return new IntervalDomain(first, last);
  }

  private Domain [] getBinaryChildren(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Domain [] retval = new Domain[2];
    CommonAST child = (CommonAST) ast.getFirstChild();
    retval[0] = (Domain) evaluate(child);
    child = (CommonAST) child.getNextSibling();
    retval[1] = (Domain) evaluate(child);
    return retval;
  }

  private Boolean lessThan(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), LT, "Attempted to evaluate '<' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return lessThan(children[0], children[1]);
  }

  private Boolean lessThan(Domain first, Domain second) {
    Utilities.tracePrint(tracePrint);
    Utilities.debugPrint(tracePrint, "< " + first + " " + second);
    Boolean retval = new Boolean(getGreatest(first).compareTo(getLeast(second)) < 0);
    Utilities.debugPrint(tracePrint, retval.toString());
    return retval;
    //return Boolean.TRUE;
  }

  private Boolean greaterThan(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), GT, "Attempted to evaluate '>' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return greaterThan(children[0], children[1]);
  }

  private Boolean greaterThan(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Utilities.debugPrint(tracePrint, "> " + first + " " + second);
    Boolean retval = new Boolean(getLeast(first).compareTo(getGreatest(second)) > 0);
    Utilities.debugPrint(tracePrint, retval.toString());
    return retval;
  }

  private Boolean lessThanOrEqual(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), LE, "Attempted to evaluate '<=' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return lessThanOrEqual(children[0], children[1]);
  }

  private Boolean lessThanOrEqual(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Boolean retval = Boolean.FALSE;
    if((first.isSingleton() && second.isSingleton()) ||
       (first instanceof IntervalDomain && second instanceof IntervalDomain) ||
       (first instanceof EnumeratedDomain && second instanceof EnumeratedDomain))
      retval = equalTo(first, second);
    return new Boolean(retval.booleanValue() || lessThan(first, second).booleanValue());
  }

  private Boolean greaterThanOrEqual(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), GE, "Attempted to evaluate '>=' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return greaterThanOrEqual(children[0], children[1]);
  }

  private Boolean greaterThanOrEqual(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Boolean retval = Boolean.FALSE;
    if((first.isSingleton() && second.isSingleton()) ||
       (first instanceof IntervalDomain && second instanceof IntervalDomain) ||
       (first instanceof EnumeratedDomain && second instanceof EnumeratedDomain))
      retval = equalTo(first, second);
    return new Boolean(retval.booleanValue() || greaterThan(first, second).booleanValue());
  }

  private Boolean equalTo(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), EQ, "Attempted to evaluate '=' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return equalTo(children[0], children[1]);
  }

  private Boolean equalTo(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Utilities.debugPrint(tracePrint, "= " + first + " " + second);
    if(!(first.isSingleton() && second.isSingleton()) && 
       ((first instanceof IntervalDomain ^ second instanceof IntervalDomain) ||
        (first instanceof EnumeratedDomain ^ second instanceof EnumeratedDomain)))
      throw new TestLangRuntimeException("Arguments to '=' must be both singletons, enumerated domains, " +
                                         "or interval domains.");

    if(first.isSingleton() && second.isSingleton()) {
      Boolean retval = new Boolean(first.getFirst().compareTo(second.getFirst()) == 0);
      Utilities.debugPrint(tracePrint, "= " + first + " " + second + " " + retval);
      return retval;
    }
    if(first instanceof IntervalDomain) {
      Boolean retval = new Boolean(getLeast(first).compareTo(getLeast(second)) == 0 &&
                                   getGreatest(first).compareTo(getGreatest(second)) == 0);
      Utilities.debugPrint(tracePrint, "= " + first + " " + second + " " + retval);
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
    Utilities.debugPrint(tracePrint, Boolean.toString(retval));
    return new Boolean(retval);
  }

  private Boolean notEqual(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), NE, "Attempted to evaluate '!=' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return notEqual(children[0], children[1]);
  }

  private Boolean notEqual(Domain first, Domain second) throws TestLangRuntimeException 
    {    Utilities.tracePrint(tracePrint); return new Boolean(!(equalTo(first, second).booleanValue()));}

  private Boolean in(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), IN, "Attempted to evaluate 'in' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return in(children[0], children[1]);
  }

  private Boolean in(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    Utilities.debugPrint(tracePrint, "in " + first + " " + second);
    if(second.isSingleton() && !first.isSingleton())
      throw new TestLangRuntimeException("First argument to 'in' must be a singleton if the second is a singleton");
    if(first instanceof IntervalDomain && second instanceof EnumeratedDomain)
      throw new TestLangRuntimeException("First argument to 'in' must be an enumerated domain if the second is enumerated.");
    if(first.isSingleton() && second.isSingleton())
      return equalTo(first, second);
    if(first instanceof IntervalDomain) {
      Boolean retval = new Boolean(getLeast(first).compareTo(getLeast(second)) >= 0 && 
                                   getGreatest(first).compareTo(getGreatest(second)) <= 0);
      Utilities.debugPrint(tracePrint, retval.toString());
      return retval;
    }
    if(second instanceof IntervalDomain) {
      boolean retval = true;
      for(Iterator fIt = ((EnumeratedDomain)first).iterator(); fIt.hasNext() && retval;) {
        Comparable comp = (Comparable) fIt.next();
        retval = retval && (comp.compareTo(getLeast(second)) >= 0) && 
          (comp.compareTo(getGreatest(second)) <= 0);
      }
      Utilities.debugPrint(tracePrint, Boolean.toString(retval));
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
    Utilities.debugPrint(tracePrint, Boolean.toString(retval));
    return new Boolean(retval);
  }

  private Boolean out(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), OUT, "Attempted to evaluate 'out' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return out(children[0], children[1]);
  }

  private Boolean out(Domain first, Domain second) throws TestLangRuntimeException 
    {    Utilities.tracePrint(tracePrint); return new Boolean(!(in(first, second).booleanValue()));}

  private Boolean intersects(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(ast.getType(), INTERSECTS, "Attempted to evaluate 'intersects' with invalid type " + ast.getType() +
                   "\nAt " + ast.toStringList());
    Domain [] children = getBinaryChildren(ast);
    return intersects(children[0], children[1]);
  }

  private Boolean intersects(Domain first, Domain second) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(first.isSingleton() && second.isSingleton())
      return equalTo(first, second);
    if(first instanceof IntervalDomain) {
      if(second instanceof IntervalDomain) {
        Boolean retval = new Boolean((getLeast(first).compareTo(getLeast(second)) >= 0 && 
                                      getLeast(first).compareTo(getGreatest(second)) <= 0) ||
                                     (getGreatest(first).compareTo(getLeast(second)) >= 0 && 
                                      getGreatest(first).compareTo(getGreatest(second)) <= 0));
        Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " " + retval);
        return retval;
      }
      for(Iterator sIt = ((EnumeratedDomain)second).iterator(); sIt.hasNext();) {
        Comparable comp = (Comparable) sIt.next();
        if(comp.compareTo(getLeast(first)) >= 0 && comp.compareTo(getGreatest(first)) <= 0) {
          Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " TRUE");
          return Boolean.TRUE;
        }
      }
      Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " FALSE");      
      return Boolean.FALSE;
    }
    if(second instanceof IntervalDomain) {
      for(Iterator fIt = ((EnumeratedDomain) first).iterator(); fIt.hasNext();) {
        Comparable comp = (Comparable) fIt.next();
        if(comp.compareTo(getLeast(second)) >= 0 && comp.compareTo(getGreatest(second)) <= 0) {
          Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " TRUE");      
          return Boolean.TRUE;
        }
      }
      Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " FALSE");      
      return Boolean.FALSE;
    }
    for(Iterator fIt = ((EnumeratedDomain)first).iterator(); fIt.hasNext();) {
      Comparable comp = (Comparable) fIt.next();
      for(Iterator sIt = ((EnumeratedDomain)second).iterator(); sIt.hasNext();) {
        if(comp.compareTo((Comparable) sIt.next()) == 0) {
          Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " TRUE");      
          return Boolean.TRUE;
        }
      }
    }
    Utilities.debugPrint(tracePrint, "intersects " + first + " " + second + " FALSE");      
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

  private String getPartialPlanIdsStr(CommonAST stepElem) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    checkValidType(stepElem.getType(), STEP, "Attempted to evaluate step assertion with invalid type " +
                   stepElem.getType() + "\nAt " + stepElem.toStringList());
    StringBuffer retval = new StringBuffer();
    StringBuffer comparison = new StringBuffer("StepNum ");
    CommonAST op = (CommonAST) stepElem.getFirstChild();
    CommonAST rhs = (CommonAST) op.getNextSibling();
    Object val = evaluate(rhs);
    switch(op.getType()) {
    case IN:
      if(!(val instanceof Domain))
        throw new TestLangRuntimeException("RHS of 'in' in step statement must be a domain.\n" +
                                           "At " + stepElem.toStringList());
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
                                           val.getClass().getName() + "'\n" +
                                           "At " + stepElem.toStringList());
      break;
    case OUT:
      if(!(val instanceof Domain))
        throw new TestLangRuntimeException("RHS of 'out' in step statement must be a domain.\n" +
                                           "At " + stepElem.toStringList());
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
                                           val.getClass().getName() + "'\n" +
                                           "At " + stepElem.toStringList());
      break;
    case EQ:
      if(val instanceof Integer) {
        Integer ival = (Integer) val;
        comparison.append("= ").append(ival.toString());
      }
      else if(val instanceof Domain && ((Domain)val).isSingleton())
        comparison.append("= ").append(((Domain)val).getFirst().toString());
      else
        throw new TestLangRuntimeException("RHS of '=' in step statement must be integer.\n" +
                                           "At " + stepElem.toStringList());
      break;
    case NE:
      if(val instanceof Integer) {
        Integer ival = (Integer) val;
        comparison.append("!= ").append(ival.toString());
      }
      else if(val instanceof Domain && ((Domain) val).isSingleton())
        comparison.append("!= ").append(((Domain)val).getFirst().toString());
      else
        throw new TestLangRuntimeException("RHS of '!=' in step statement must be integer.\n" +
                                           "At " + stepElem.toStringList());          
      break;
    case LT:
      if(val instanceof Integer)
        comparison.append("< ").append(val.toString());
      else if(val instanceof Domain)
        comparison.append("< ").append(getLeast((Domain) val).toString());
      else
        throw new TestLangRuntimeException("RHS of '<' in step statement is '" + val.getClass().getName() + "'." +
                                           "Must be Integer or a Domain.\nAt " + stepElem.toStringList());
      break;
    case GT:
      if(val instanceof Integer)
        comparison.append("> ").append(val.toString());
      else if(val instanceof Domain)
        comparison.append("> ").append(getGreatest((Domain) val).toString());
      else
        throw new TestLangRuntimeException("RHS of '>' in step statement is '" + val.getClass().getName() + "'." +
                                           "Must be Integer or a Domain.\nAt " + stepElem.toStringList());
      break;
    case GE:
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
                                           "Must be Integer or a Domain.\nAt " + stepElem.toStringList());
      break;
    case LE:
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
                                           "Must be Integer or a Domain.\nAt " + stepElem.toStringList());
      break;
    default:
      throw new TestLangRuntimeException("'" + op.getText() + "' is not a valid op in a step statement\n" +
                                         "At " + stepElem.toStringList());
    }

    List ids = MySQLDB.queryPartialPlanIds(seqId, comparison.toString());
    if(ids.size() == 0)
      throw new TestLangRuntimeException("No steps found matching criteria " + stepElem.toStringList());
    for(Iterator it = ids.iterator(); it.hasNext();) {
      retval.append(it.next().toString());
      if(it.hasNext())
        retval.append(", ");
    }
    ensurePartialPlansInDatabase(ids);
    return retval.toString();
  }

  private String getAllPartialPlanIdsStr() throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    StringBuffer retval = new StringBuffer();
    List ids = MySQLDB.queryPartialPlanIds(seqId);
    for(Iterator it = ids.iterator(); it.hasNext();) {
      retval.append(it.next().toString());
      if(it.hasNext())
        retval.append(", ");
    }
    ensurePartialPlansInDatabase(ids);
    return retval.toString();
  }

  private String nameQueryStr(CommonAST name, int type) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    StringBuffer retval = new StringBuffer(" && ");
    if(type == TOKENS)
      retval.append("PredicateName ");
    else if(type == OBJECTS)
      retval.append("ObjectName ");
    else if(type == TRANSACTIONS)
      retval.append("TransactionName ");
    else if(type == TYPE)
      retval.append("TransactionType ");
    else
      throw new TestLangRuntimeException("Attempted to assemble name query for invald type " + type + 
                                         "\nAt " + name.toStringList());
    CommonAST op = (CommonAST) name.getFirstChild();
    CommonAST rhs = (CommonAST) op.getNextSibling();
    Object val = evaluate(rhs);
    switch(op.getType()) {
    case EQ:
      if(val instanceof String)
        retval.append("= ").append(val.toString());
      else if(val instanceof EnumeratedDomain) {
        EnumeratedDomain dom = (EnumeratedDomain) val;
        if(dom.getFirst() instanceof String)
          retval.append("= ").append(dom.getFirst().toString());
        else
          throw new TestLangRuntimeException("RHS of '=' in predicate/name statement must be a string.\n" +
                                             "At " + name.toStringList());
      }
      else
        throw new TestLangRuntimeException("RHS of '=' in predicate/name statement must be a string.\n" +
                                           "At " + name.toStringList());
      break;
    case NE:
      if(val instanceof String)
        retval.append("!= '").append(val.toString()).append("'");
      else if(val instanceof EnumeratedDomain) {
        EnumeratedDomain dom = (EnumeratedDomain) val;
        if(dom.getFirst() instanceof String)
          retval.append("!= ").append(dom.getFirst().toString());
        else
          throw new TestLangRuntimeException("RHS of '!=' in predicate/name statement must be a string.\n" +
                                             "At " + name.toStringList());
      }
      else
        throw new TestLangRuntimeException("RHS of '!=' in predicate/name statement must be a string\n" +
                                           "At " + name.toStringList());
      break;
    case IN:
      if(val instanceof EnumeratedDomain) {
        retval.append(" IN (");
        for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
          retval.append(it.next().toString());
          if(it.hasNext())
            retval.append(", ");
        }
        retval.append(")");
      }
      else
        throw new TestLangRuntimeException("RHS of 'in' in predicate/name statement must be an enumerated domain\n" +
                                           "At " + name.toStringList());
      break;
    case OUT:
      if(val instanceof EnumeratedDomain) {
        retval.append(" NOT IN (");
        for(Iterator it = ((EnumeratedDomain)val).iterator(); it.hasNext();) {
          retval.append(it.next().toString());
          if(it.hasNext())
            retval.append(", ");
        }
        retval.append(")");
      }
      else
        throw new TestLangRuntimeException("RHS of 'out' in predicate/name statement must be an enumerated domain\n" +
                                           "At " + name.toStringList());
      break;
    default:
      throw new TestLangRuntimeException("'" + op.getText() + 
                                        "' is not a valid operator in a predicate/name statement.\n" +
                                        "At " + name.toStringList());
    }
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
          varName.add("'" + vars.getString("ParameterName") + "'");
          CommonAST param = (CommonAST) it.next();
          CommonAST nameOp = (CommonAST) param.getFirstChild().getFirstChild();
          Domain nameDom = null;
          Object tempVal = evaluate((CommonAST) nameOp.getNextSibling());
          if(tempVal instanceof Domain)
            nameDom = (Domain) tempVal;
          else {
            nameDom = new EnumeratedDomain();
            ((EnumeratedDomain)nameDom).add(tempVal);
          }
          Method evalMethod = getClass().getDeclaredMethod(methodNames[nameOp.getType()], methodSignature);
          Object [] args = {varName, nameDom};
          if(((Boolean)evalMethod.invoke(this, args)).booleanValue()) {
            CommonAST op = (CommonAST) param.getFirstChild().getNextSibling().getFirstChild();
            Object temp = evaluate((CommonAST) op.getNextSibling());
            Domain cmpDom = null;
            if(temp instanceof Domain)
              cmpDom = (Domain) temp;
            else {
              cmpDom = new EnumeratedDomain();
              ((EnumeratedDomain)cmpDom).add(temp);
            }
            evalMethod = getClass().getDeclaredMethod(methodNames[op.getType()], methodSignature);
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

  private void trimTokensByVars(EnumeratedDomain tokens, String ppIds, CommonAST start, CommonAST end, CommonAST status,
                                List params) 
    throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(start == null && end == null && status == null)
      return;
    
    Domain startDom = getAttributeDomain(start);
    Domain endDom = getAttributeDomain(end);
    Domain statusDom = getAttributeDomain(status);

    CommonAST startOp = (start == null ? null : (CommonAST) start.getFirstChild());
    CommonAST endOp = (end == null ? null : (CommonAST) end.getFirstChild());
    CommonAST statusOp = (status == null ? null : (CommonAST) status.getFirstChild());

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
        CommonAST op = null;
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
            varName.add("'" + vars.getString("ParameterName") + "'");
            CommonAST param = (CommonAST) pIt.next();
            CommonAST nameOp = (CommonAST) param.getFirstChild().getFirstChild();
            Domain nameDom = null;
            Object tempVal = evaluate((CommonAST) nameOp.getNextSibling());
            if(tempVal instanceof Domain)
              nameDom = (Domain) tempVal;
            else {
              nameDom = new EnumeratedDomain();
              ((EnumeratedDomain)nameDom).add(tempVal);
            }
            try {
              Method evalMethod = getClass().getDeclaredMethod(methodNames[nameOp.getType()], methodSignature);
              Object [] args = {varName, nameDom};
              if(((Boolean)evalMethod.invoke(this, args)).booleanValue()) {
                op = (CommonAST) param.getFirstChild().getNextSibling().getFirstChild();
                Object temp = evaluate((CommonAST)op.getNextSibling());
                if(temp instanceof Domain)
                  cmpDom = (Domain) temp;
                else {
                  cmpDom = new EnumeratedDomain();
                  ((EnumeratedDomain)cmpDom).add(temp);
                }
                evalMethod = getClass().getDeclaredMethod(methodNames[op.getType()], methodSignature);
                //args = {varDom, cmpDom};
                args[0] = varDom; args[1] = cmpDom;
                if(!((Boolean)evalMethod.invoke(this, args)).booleanValue())
                  retval.remove(tokenId);
              }
            }
            catch(Exception e){throw new TestLangRuntimeException(e);}
          }
        }
        else
          throw new TestLangRuntimeException("Attempted to trim token list with no assertions or attempted to trim with incorrect var type");
        try {
          Method evalMethod = getClass().getDeclaredMethod(methodNames[op.getType()], methodSignature);
          Object [] args = {varDom, cmpDom};
          if(!((Boolean)evalMethod.invoke(this, args)).booleanValue())
            retval.remove(tokenId);
            //tokens.remove(tokenId);
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

  private Domain getAttributeDomain(CommonAST ast) throws TestLangRuntimeException {
    Utilities.tracePrint(tracePrint);
    if(ast == null)
      return null;
    Domain retval;
    CommonAST dom = (CommonAST) ast.getFirstChild().getNextSibling();
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

  private String assembleTokenVariableQuery(EnumeratedDomain tokens, String ppIds, CommonAST start, CommonAST end,
                                            CommonAST status, List params) {
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
              ((EnumeratedDomain)retval).add("'" + str + "'");
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

  private void checkValidType(int type, String message) throws TestLangRuntimeException {
    switch(type) {
    case OBRACE:
    case OBRACKET:
    case INTEGER:
    case IN:
    case OUT:
    case EQ:
    case NE:
    case TOKENS:
    case STRING:
    case OBJECTS:
    case TRANSACTIONS:
    case ENTITY:
    case PROPERTY:
    case COUNT:
    case LT:
    case GT:
    case GE:
    case LE:
    case TEST:
    case INTERSECTS:
      break;
    default:
      throw new TestLangRuntimeException(message);
    }
  }

  private void checkValidType(int type, int check, String message) throws TestLangRuntimeException {
    if(type != check)
      throw new TestLangRuntimeException(message);
  }

  private void ensurePartialPlansInDatabase(List ids) throws TestLangRuntimeException {
    List idsInDb = MySQLDB.queryPlanIdsInDatabase(seqId);
    ids.removeAll(idsInDb);
    for(Iterator it = ids.iterator(); it.hasNext();) {
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
