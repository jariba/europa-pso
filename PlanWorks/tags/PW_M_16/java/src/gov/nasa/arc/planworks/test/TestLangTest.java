package gov.nasa.arc.planworks.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.*;

import antlr.RecognitionException;

import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.dbg.testLang.TestLangHelper;
import gov.nasa.arc.planworks.dbg.testLang.TestLangLexer;
import gov.nasa.arc.planworks.dbg.testLang.TestLangParser;
import gov.nasa.arc.planworks.dbg.testLang.TestLangParseException;
import gov.nasa.arc.planworks.dbg.testLang.TestLangRuntimeException;

public class TestLangTest extends TestCase implements IdSource {
  private int currId;
  public static void main(String [] args) {
    junit.textui.TestRunner.run(suite());
  }
  public static TestSuite suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new TestLangTest("testInternals"));
    suite.addTest(new TestLangTest("testQueries"));
    return suite;
  }
  public TestLangTest(String testType) {
    super(testType);
    currId = 0;
  }

  public void resetEntityIdInt() {currId = 0;}
  public int incEntityIdInt() {return ++currId;}

  public void testInternals() throws Exception {
    
    StringBuffer buf = new StringBuffer();
    buf.append("Test('testTest',\n"); //17
    buf.append("Count(Tokens(step=9 predicate='abcdefijkmnoprstuyghlqvwxz'))=39;\n"); //64
    buf.append("Count(Tokens(step=1 start < [2..10] end in {1 2 3 4} predicate='foobar'))=1;\n"); //76
    buf.append("Count(Tokens(step=2))<93;\n"); //25
    buf.append("Count(Objects(step=9 name='aorad.aoaer')) >= 2;\n"); //47
    buf.append("Count(Tokens(step=1 predicate='foo')) = Count(Tokens(step=2 predicate='foo'));\n"); //78
    buf.append("Count(Tokens(step in [1..12])) = 91;\n"); //36
    buf.append("Count(Tokens(step in {1, 3, 5, 7, 9})) = 91;\n"); //44
    buf.append("Tokens(step=73) = Tokens(step=98);\n"); //34
    buf.append("Test('subTest', \n"); //16
    buf.append("Count(Tokens(step in [1..12])) = 91;\n"); //33
    buf.append("Tokens(step=93) intersects Tokens(step=107);\n"); //45
    buf.append(");\n");
    buf.append("Tokens(step = 93 variable(name = 'foobar' value = [0..34])) in {0 1 2 3 4};\n");
    buf.append(");\n"); //4
    ByteArrayInputStream inputStream = new ByteArrayInputStream(buf.toString().getBytes());
    TestLangLexer lexer = new TestLangLexer(inputStream);
    TestLangParser parser = new TestLangParser(lexer);
    parser.test_set();
    assertTrue(TestLangHelper.runInternalTests());
  }


  public void testQueries() throws Exception {
    String dataDir = "TestLang";
    String testFileName = "TestFile";
    String projName = "TestLangTest";

    MySQLDB.updateDatabase("INSERT INTO Project (ProjectName) VALUES ('" + projName + "')");

    List seq = PWSetupHelper.buildTestData(1, 1, this, dataDir);
    
    StringBuffer testBuffer = new StringBuffer();
    testBuffer.append("Test('TestTest', \n");
    testBuffer.append("Count(Tokens(step = 0)) = 64;\n");
    testBuffer.append("Tokens(step = 0 start < [4000..9000] end > [0..2] status in {0, 1, 2, 3} ");
    testBuffer.append(" predicate = 'predicate282' variable(name = 'param1' value out {'foo', 'bar', 'baz', 'quux'})");
    testBuffer.append(") = {282};\n");
    testBuffer.append("Objects(step < 99 name = 'object606' variable(name = 'member0' value = 0) ");
    testBuffer.append("variable(name = 'member1' value >= 10) variable(name = 'member2' value in {3, 20, 7})) = {606};\n");
    testBuffer.append("Count(Transactions(step in [0..4] name = 'TOKEN_CREATED' type in {'CREATION', 'DELETION'})) = 40;\n");
    testBuffer.append(");\n");
   
    File testFile = new File(System.getProperty("planworks.test.data.dir") + 
                             System.getProperty("file.separator") + dataDir +
                             System.getProperty("file.separator") + testFileName);
    testFile.createNewFile();
    if(!testFile.exists() || !testFile.canWrite())
      throw new Exception("TestFile doesn't exist or can't be written.");
    System.err.println(testBuffer.toString());
    try {
      FileWriter writer = new FileWriter(testFile);
      writer.write(testBuffer.toString());
      writer.flush();
    }
    catch(IOException ioe) {
      System.err.println("Failed to write file: ");
      throw new Exception(ioe);
    }
    
    TestLangHelper.runTests(projName, (String) seq.get(0), testFile.getAbsolutePath());
    
    MySQLDB.deleteProject(MySQLDB.getProjectIdByName(projName));    
  }
}
