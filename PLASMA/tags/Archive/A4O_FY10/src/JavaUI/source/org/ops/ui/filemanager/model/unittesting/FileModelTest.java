package org.ops.ui.filemanager.model.unittesting;

import java.net.URL;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.ops.ui.filemanager.model.AstNode;
import org.ops.ui.filemanager.model.AstNodeTypes;
import org.ops.ui.filemanager.model.ErrorRecord;
import org.ops.ui.filemanager.model.FileModel;

import psengine.PSUtil;

/**
 * Java side gets AST as a string. When parsing the AST, the Java code uses
 * constants in AstNodeTypes.java to figure out node types. These constants
 * should match the numbers generated by Antlr on the C++ side (NDDL3.tokens).
 * Often changes to the NDDL grammar cause these constants to shift, in which
 * case AstNodeTypes.java needs to be updated. For now this update is manual,
 * which is bad.
 * 
 * At the very least we need some unittests to verify that constants still
 * match.
 * 
 * @author Tatiana Kichkaylo
 */
public class FileModelTest extends TestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		String debugMode = "g";
		try {
			PSUtil.loadLibraries(debugMode);
		} catch (UnsatisfiedLinkError e) {
			fail("Cannot load Europa libraries. Please make the "
					+ "dynamic libraries are included in LD_LIBRARY_PATH "
					+ "(or PATH for Windows)\n" + e);
		}
	}

	/**
	 * Get a good model and verify that all constants in AstNodeTypes match
	 * whatever is actually produced by the C++ side
	 */
	public void testGetModel() {
		// Get absolute file name from this class's URL
		URL url = getClass().getResource("AstTest.nddl");
		assertEquals("The test expects test file in the file system", "file",
				url.getProtocol());
		String fileName = url.getPath();
		FileModel model = FileModel.getModel(fileName);
		assertNotNull(model);

		// Go over all node types that should be there
		AstNode root = model.getAST();
		assertEquals("NDDL node ", AstNodeTypes.NDDL, root.getType());

		// Ignore all nodes imported from standard NDDL includes
		int childIndex = 0;
		while (childIndex < root.getChildren().size()
				&& !fileName.equals(root.getSafe(childIndex).getFileName()))
			childIndex++;

		// Enum definition
		AstNode item = root.getSafe(childIndex);
		assertEquals("Enum ", AstNodeTypes.ENUM_KEYWORD, item.getType());

		// Class definition
		item = root.getSafe(++childIndex);
		assertEquals("Class definition node ", AstNodeTypes.CLASS_DEF, item
				.getType());
		assertEquals("# of children of SomeClass defintion ", 3, item
				.getChildren().size());
		// First child of that is the class name
		assertEquals("IDENT ", AstNodeTypes.IDENT, item.getSafe(0).getType());
		assertEquals("SomeClass", item.getSafe(0).getText());
		// 2nd child is 'extends', which we check by text. There is no constant
		assertEquals("extends", item.getSafe(1).getText());
		// 3rd child is the body. Reuse the variable
		item = item.getSafe(2);
		assertEquals("{ code in class definition ", AstNodeTypes.LBRACE, item
				.getType());
		// The body has only one child, which is a predicate
		assertEquals("# of elements in SomeClass body ", 1, item.getChildren()
				.size());
		item = item.getSafe(0);
		assertEquals("Predicate keyword in class definition ",
				AstNodeTypes.PREDICATE_KEYWORD, item.getType());

		// Second class definition: go straight to body
		item = root.getSafe(++childIndex).getSafe(2);
		// There is a variable, a constructor and a predicate
		assertEquals("# of children in 2nd class body ", 3, item.getChildren()
				.size());
		assertEquals("Variable ", AstNodeTypes.VARIABLE, item.getSafe(0)
				.getType());
		item = item.getSafe(1);
		assertEquals("Constructor ", AstNodeTypes.CONSTRUCTOR, item.getType());
		assertEquals("#of children for constructor ", 3, item.getChildren()
				.size());
		assertEquals(AstNodeTypes.IDENT, item.getSafe(0).getType());
		assertEquals("Constructor parameter ( ", AstNodeTypes.LPAREN, item
				.getSafe(1).getType());
		assertEquals("Constructor body { ", AstNodeTypes.LBRACE, item
				.getSafe(2).getType());

		// Next child is a predicate definition
		item = root.getSafe(++childIndex);
		assertEquals("Predicate definition ", AstNodeTypes.PREDICATE_DEF, item
				.getType());

		// Then 2 variable initializations
		item = root.getSafe(++childIndex);
		assertEquals("Variable initialization ", AstNodeTypes.VARIABLE, item
				.getType());
		childIndex++; // skip the second one

		// Next is a fact, which inside has a ( with a predicate instance
		item = root.getSafe(++childIndex);
		assertEquals("Fact ", AstNodeTypes.FACT_KEYWORD, item.getType());
		assertEquals(1, item.getChildren().size());
		item = item.getSafe(0);
		assertEquals(1, item.getChildren().size());
		assertEquals(AstNodeTypes.LPAREN, item.getType());
		item = item.getSafe(0);
		assertEquals("Predicate instance ", AstNodeTypes.PREDICATE_INSTANCE,
				item.getType());

		// Next have a constraint instance
		item = root.getSafe(++childIndex);
		assertEquals("Constraint instance ", AstNodeTypes.CONSTRAINT_INSTANCE,
				item.getType());

		// And a goal
		item = root.getSafe(++childIndex);
		assertEquals("Goal ", AstNodeTypes.GOAL_KEYWORD, item.getType());

		// There should be no errors
		assertTrue("Should be no errors", model.getErrors().isEmpty());
	}

	/**
	 * Test method for
	 * {@link org.ops.ui.filemanager.model.FileModel#getErrors()}.
	 */
	public void testGetErrors() {
		// Get absolute file name from this class's URL
		URL url = getClass().getResource("BrokenAstTest.nddl");
		assertEquals("The test expects test file in the file system", "file",
				url.getProtocol());
		String fileName = url.getPath();
		FileModel model = FileModel.getModel(fileName);
		assertNotNull(model);

		// There should be some error messages
		ArrayList<ErrorRecord> errors = model.getErrors();
		assertFalse("Error list empty", errors.isEmpty());
		assertEquals("File of the error ", fileName, errors.get(0)
				.getFileName());

		// There should also be an AST, even if broken
		AstNode root = model.getAST();
		assertNotNull("Broken file should still produce AST", root);
		assertEquals(AstNodeTypes.NDDL, root.getType());

		// One of the top-level children should be an error node
		boolean found = false;
		for (AstNode c : root.getChildren())
			if (c.getType() == AstNodeTypes.ERROR)
				found = true;
		assertTrue("Should find at least one error node", found);
	}
}
