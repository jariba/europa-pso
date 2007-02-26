package org.ops.ui.nddl;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;

import nddl.ModelAccessor;
import nddl.NddlParser;
import nddl.NddlParserState;
import nddl.NddlTreeParser;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

import org.ops.ui.PSDesktop;
import org.ops.ui.ash.AshConsole;
import org.ops.ui.ash.AshInterpreter;
import org.ops.ui.ash.DocumentOutputStream;

public class NddlInterpreter extends AshInterpreter {
  NddlParserState persistantState = null;

  public NddlInterpreter() {
    super("Nddl");
    ModelAccessor.init();
    persistantState = new NddlParserState(null);
  }

  public void setConsole(AshConsole console) {
    PrintStream magic = new PrintStream(new BufferedOutputStream(new DocumentOutputStream(console.getDocument(), "!!")), true);
    persistantState.setErrStream(magic);
  }

  private boolean execute(NddlParser parser) {
    IXMLElement xml = new XMLElement("nddl");

    if(parser.getState().getErrorCount() == 0) {
      try {
        NddlTreeParser treeParser = new NddlTreeParser(parser.getState());
        treeParser.nddl(parser.getAST(),xml);
      }
      catch(Exception ex) {
        return false;
      }
    }
    else
      return false;

    StringWriter string = new StringWriter();
    try {
      new XMLWriter(new BufferedWriter(string)).write(xml);
    }
    catch(IOException ex) {
      return false;
    }

    PSDesktop.desktop.getPSEngine().executeTxns(string.toString(), false, true);
    persistantState = parser.getState();
    return true;
  }

  public void source(String filename) {
    try {
      File modelFile =  ModelAccessor.generateIncludeFileName("",filename);
      NddlParser parser = NddlParser.parse(persistantState, modelFile, null);
      execute(parser);
    }
    catch(Exception ex) {
      // later figure out how to send this to the "shell"
      ex.printStackTrace();
    }
  }

  // returns true if parse didn't encounter a premature end of string.
  public boolean eval(String toEval) {
    try {
      NddlParser parser = NddlParser.eval(persistantState, new StringReader(toEval));
      execute(parser);
    }
    catch(EOFException ex) {
      return false;
    }
    catch(Exception ex) {
      // later figure out how to send this to the "shell"
      ex.printStackTrace();
    }
    return true;
  }
}
