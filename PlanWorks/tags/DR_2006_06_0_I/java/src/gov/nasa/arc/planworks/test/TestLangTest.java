package gov.nasa.arc.planworks.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.*;

import antlr.RecognitionException;
import antlr.debug.misc.ASTFrame;

import testLang.TestLangLexer;
import testLang.TestLangParser;
import testLang.TestLangParseException;
import testLang.TestLangRuntimeException;

import gov.nasa.arc.planworks.db.util.MySQLDB;
import gov.nasa.arc.planworks.dbg.testLang.TestLangInterpreter;

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
    System.err.println("====>testInternals");
    StringBuffer buf = new StringBuffer();
    buf.append("Test('testTest',\n"); //1
    buf.append("At step : Count(Tokens()) > 0;\n"); //2
    buf.append("At step=9 : Count(Tokens(predicate='abcdefijkmnoprstuyghlqvwxz'))=39;\n"); //3
    buf.append("At step=1 : Count(Tokens(start < [2..10] end in {1 2 3 4} predicate='foobar'))=1;\n"); //4
    buf.append("At step=2 : Count(Tokens())<93;\n"); //5
    buf.append("At step=9 : Count(Objects(name='aorad.aoaer')) >= 2;\n"); //6
    buf.append("At all step in [1..12] : Count(Tokens()) = 91;\n"); //7
    buf.append("At all step in {1, 3, 5, 7, 9} : Count(Tokens()) = 91;\n"); //8
    buf.append("Test('subTest', \n"); //9
    buf.append("At each step in [0..12] : Count(Transactions(type = 'RETRACTION')) = 0;\n");//10
    buf.append(");\n");//11
    buf.append("At step=93 : Tokens(variable(name = 'foobar' value = [0..34])) in {0 1 2 3 4};\n");//12
    buf.append(");\n"); //13
    ByteArrayInputStream inputStream = new ByteArrayInputStream(buf.toString().getBytes());
    TestLangLexer lexer = new TestLangLexer(inputStream);
    TestLangParser parser = new TestLangParser(lexer);
    parser.test_set();
    //(new ASTFrame("Foo", parser.getAST())).setVisible(true);
    assertTrue(TestLangInterpreter.runInternalTests());
    System.err.println("====>testInternals DONE");
  }


  public void testQueries() throws Exception {
    System.err.println("====>testQueries");
    String dataDir = "TestLang";
    String testFileName = "TestFile";
    String projName = "TestLangTest";

    MySQLDB.updateDatabase("INSERT INTO Project (ProjectName) VALUES ('" + projName + "')");

    List seq = PWSetupHelper.buildTestData(1, 1, this, dataDir);
    
    StringBuffer testBuffer = new StringBuffer();
    testBuffer.append("Test('TestTest', \n"); //1
    testBuffer.append("At FIRST step : Count(Tokens()) = 64;\n"); //2
    testBuffer.append("At LAST step : Tokens(start < [4000..9000] end > [0..2] status in {0, 1, 2, 3} "); //3
    testBuffer.append(" predicate = 'predicate282' variable(name = 'param1' value out {'foo', 'bar', 'baz', 'quux'})"); //3
    testBuffer.append(") = {282};\n"); //3
    testBuffer.append("At step < 99 : Objects(name = 'object622' variable(name = 'member0' value = 0) "); //4
    testBuffer.append("variable(name = 'member1' value >= 10) variable(name = 'member2' value in {3, 20, 7})) = {622};\n"); //4
    testBuffer.append("At ALL step in [0..4] : Count(Transactions(name = 'TOKEN_CREATED' type in {'CREATION', 'DELETION'})) = 64;\n"); //5
    testBuffer.append("At step = 0 : Count(Tokens()) = 64;\n");
    testBuffer.append("At ANY step in [0..4] : Count(Tokens()) = 64;\n");
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
    
    TestLangInterpreter.runTests(projName, (String) seq.get(0), testFile.getAbsolutePath());
    
    MySQLDB.deleteProject(MySQLDB.getProjectIdByName(projName));
    System.err.println("====>testQueries DONE");
  }
}
