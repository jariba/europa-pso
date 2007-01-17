#include <iostream>
#include <fstream>
#include "Error.hh"
#include "Debug.hh"

// Support for default setup
#include "ANMLParser.hpp"
#include "ANML2NDDL.hpp"

int max(int a, int b) {
  return (a<b)? b : a;
}

int outputAST2Dot(std::ostream& out, const antlr::RefAST ast, const int depth, const int parent, int& node) {
  if(ast == antlr::nullAST)
    return depth-1;
  int maxdepth = depth;

  // saved for iteration over children.
  int currParent = node;

  antlr::RefAST curr = ast;
  while(curr != antlr::nullAST) {
    out << "  node_" << node;
    out <<  " [label=\"" << curr->toString() << "\"];" << std::endl;
    if(curr != ast) {
      out << "  node_" << node-1 << " -> node_" << node << " [style=invis];" << std::endl;
    }
    if(parent != -1)
      out << "  node_" << parent << " -> node_" << node << ";" << std::endl;
    curr = curr->getNextSibling();
    ++node;
  }

  out << "  { rank = same; depth_" << depth;
  for(int i=currParent; i<node; ++i)
    out << ";"<< std::endl << "      node_" << i;
  out << "}" << std::endl;

  curr = ast;
  while(curr != antlr::nullAST) {
    maxdepth = max(maxdepth,outputAST2Dot(out, curr->getFirstChild(), depth+1, currParent, node));
    curr = curr->getNextSibling();
    ++currParent;
  }

  if(depth == 1) {
    out << "  {" << std::endl;
    for(int i=1; i<=maxdepth; ++i)
      out << "    depth_" << i << " [shape=plaintext, label=\"Tree Depth " << i << "\"];" << std::endl;
    out << "    depth_1";
    for(int i=2; i<=maxdepth; ++i)
      out << " -> depth_" << i;
    out << ";" << std::endl << "  }" << std::endl;
  }
  return maxdepth;
}

/**
 * Interface for testing ANML models for parsability.
 * Called with two filenames: [ANML model] [Output Digraph AST].
 */
int main(int argc, char** argv) {
  assertTrue(argc == 3, "Expected exactly two filename arguments: [ANML model] [Output Digraph AST]");

	debugMsg("ANMLParser:main", "Phase 1: parsing \"" << argv[1] << "\"");

  antlr::RefAST ast = ANMLParser::parse(".", argv[1]);

  assertTrue(ast != antlr::nullAST, "Parse failed to return an AST");

	debugMsg("ANMLParser:main", "Phase 1: dumping parse tree to \"" << argv[2] << "\"");

  std::ofstream digraph(argv[2]);

  int node = 0;
  digraph << "digraph ANMLAst {" << std::endl;
  outputAST2Dot(digraph, ast, 1, -1, node);
  digraph << "}" << std::endl;

	debugMsg("ANMLParser:main", "Phase 1 complete");

	debugMsg("ANMLParser:main", "Phase 2: Walking parse tree");

	ANML2NDDL* treeParser = new ANML2NDDL();
	
	for(int i=0; i <= FINAL_PASS; ++i) {
		treeParser->anml(ast, i);
	}

	debugMsg("ANMLParser:main", "Phase 2 complete");

  return 0;
}
