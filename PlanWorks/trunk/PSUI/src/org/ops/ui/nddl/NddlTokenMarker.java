package org.ops.ui.nddl;

/*
 * NddlTokenMarker.java - Nddl Shell token marker
 */

import javax.swing.text.Segment;
import org.ops.ui.ash.KeywordMap;
import org.ops.ui.ash.TokenMarker;
import org.ops.ui.ash.Token;

/**
 * Nddl Shell token marker.
 *
 * @author Matthew E. Boyce
 * @version $Id: NddlTokenMarker.java,v 1.3 2007-02-28 00:36:31 meboyce Exp $
 */
public class NddlTokenMarker extends TokenMarker {
  public NddlTokenMarker() {
    this.keywords = getKeywords();
  }

  public byte markTokensImpl(byte token, Segment line, int lineIndex) {
    char[] array = line.array;
    int offset = line.offset;
    lastOffset = offset;
    lastKeyword = offset;
    int length = line.count + offset;
    boolean backslash = false;

loop:
    for(int i = offset; i < length; i++) {
      // test for prompt!
      if(lastOffset + 6 <= length) {
        String lineStart = new String(array, lastOffset, 6); 
        if(lineStart.equals("Nddl %")) {
          addToken(i - lastOffset,token);
          addToken(6, Token.LABEL);
          i+=6;
          lastOffset = lastKeyword = i;
          if(i >= length)
            continue;
        }
      }
      int i1 = (i+1);

      char c = array[i];
      if(c == '\\') {
        backslash = !backslash;
        continue;
      }

      switch(token) {
        case Token.NULL:
          switch(c) {
            case '"':
              doKeyword(line,i,c);
              if(backslash)
                backslash = false;
              else {
                addToken(i - lastOffset,token);
                token = Token.LITERAL1;
                lastOffset = lastKeyword = i;
              }
              break;
            case '\'':
              doKeyword(line,i,c);
              if(backslash)
                backslash = false;
              else {
                addToken(i - lastOffset,token);
                token = Token.LITERAL2;
                lastOffset = lastKeyword = i;
              }
              break;
            case '!':
              backslash = false;
              doKeyword(line,i,c);
              if(length - i > 1) {
                if(array[i1] == '!') {
                  addToken(i - lastOffset,token);
                  addToken(length - i,Token.ERROR);
                  lastOffset = lastKeyword = length;
                }
                else {
                  addToken(i - lastOffset,token);
                  addToken(length - i,Token.WARNING);
                  lastOffset = lastKeyword = length;
                }
              }
              break loop;
            case '/':
              backslash = false;
              doKeyword(line,i,c);
              if(length - i > 1) {
                switch(array[i1]) {
                  case '*':
                    addToken(i - lastOffset,token);
                    lastOffset = lastKeyword = i;
                    if(length - i > 2 && array[i+2] == '*')
                      token = Token.COMMENT2;
                    else
                      token = Token.COMMENT1;
                    break;
                  case '/':
                    addToken(i - lastOffset,token);
                    addToken(length - i,Token.COMMENT1);
                    lastOffset = lastKeyword = length;
                    break loop;
                }
              }
              break;
            default:
              backslash = false;
              if(Character.isDigit(c)
                  || (c == '.' && (length <= i || Character.isDigit(array[i1])))) {
                doKeyword(line,i,c);
                // check if we're on a word boundary!
                if(i == 0 || Character.isWhitespace(array[i-1])
								  || array[i-1] == '-' || array[i-1] == '+'
								  || array[i-1] == '=') {
                  addToken(i - lastOffset,token);
                  token = Token.LITERAL3;
                  lastOffset = lastKeyword = i;
                }
              }
              else if(!Character.isLetterOrDigit(c) && c != '_' && c != '#') {
                doKeyword(line,i,c);
              }
              break;
          }
          break;
        case Token.COMMENT1:
        case Token.COMMENT2:
          backslash = false;
          if(c == '*' && length - i > 1) {
            if(array[i1] == '/') {
              i++;
              addToken((i+1) - lastOffset,token);
              token = Token.NULL;
              lastOffset = lastKeyword = i+1;
            }
          }
          break;
        case Token.LITERAL1:
          if(backslash)
            backslash = false;
          else if(c == '"') {
            addToken(i1 - lastOffset,token);
            token = Token.NULL;
            lastOffset = lastKeyword = i1;
          }
          break;
        case Token.LITERAL2:
          if(backslash)
            backslash = false;
          else if(c == '\'') {
            addToken(i1 - lastOffset,Token.LITERAL1);
            token = Token.NULL;
            lastOffset = lastKeyword = i1;
          }
          break;
        case Token.LITERAL3:
          if(backslash)
            backslash = false;
          else {
            if(!Character.isLetterOrDigit(c) && c != '.' && c != '-' && c != '+') {
              addToken(i - lastOffset,Token.LITERAL1);
              token = Token.NULL;
              lastOffset = lastKeyword = i;
            }
          }
          break;
        default:
          throw new InternalError("Invalid state: "
              + token);
      }
    }

    if(token == Token.NULL)
      doKeyword(line,length,'\0');

    switch(token) {
      case Token.LITERAL1:
      case Token.LITERAL2:
        addToken(length - lastOffset,Token.INVALID);
        token = Token.NULL;
        break;
      case Token.KEYWORD2:
        addToken(length - lastOffset,token);
        if(!backslash)
          token = Token.NULL;
      default:
        addToken(length - lastOffset,token);
        break;
    }

    return token;
  }

  public static KeywordMap getKeywords() {
    if(nddlKeywords == null) {
      nddlKeywords = new KeywordMap(false);

      nddlKeywords.add("#include", Token.PREPROC);

      // functions (cyan)
      nddlKeywords.add("close", Token.KEYWORD3);
      nddlKeywords.add("free", Token.KEYWORD3);
      nddlKeywords.add("constrain", Token.KEYWORD3);
      nddlKeywords.add("merge", Token.KEYWORD3);
      nddlKeywords.add("reject", Token.KEYWORD3);
      nddlKeywords.add("activate", Token.KEYWORD3);
      nddlKeywords.add("cancel", Token.KEYWORD3);
      nddlKeywords.add("reset", Token.KEYWORD3);
      nddlKeywords.add("specify", Token.KEYWORD3);
      // conditionals (yellow)
      nddlKeywords.add("if", Token.KEYWORD1);
      nddlKeywords.add("else", Token.KEYWORD1);
      nddlKeywords.add("foreach", Token.KEYWORD1);
      nddlKeywords.add("filterOnly", Token.KEYWORD1);
      nddlKeywords.add("in", Token.KEYWORD1);
      // class related (yellow)
      nddlKeywords.add("new", Token.KEYWORD1);
      nddlKeywords.add("this", Token.KEYWORD1);
      nddlKeywords.add("super", Token.KEYWORD1);
      // types (green)
      nddlKeywords.add("bool", Token.KEYWORD2);
      nddlKeywords.add("string", Token.KEYWORD2);
      nddlKeywords.add("int", Token.KEYWORD2);
      nddlKeywords.add("float", Token.KEYWORD2);
      nddlKeywords.add("numeric", Token.KEYWORD2);
      // class (green)
      nddlKeywords.add("class", Token.KEYWORD2);
      nddlKeywords.add("enum", Token.KEYWORD2);
      nddlKeywords.add("typedef", Token.KEYWORD2);
      nddlKeywords.add("extends", Token.KEYWORD2);
      nddlKeywords.add("predicate", Token.KEYWORD2);
      // goals (cyan)
      nddlKeywords.add("goal", Token.KEYWORD3);
      nddlKeywords.add("rejectable", Token.KEYWORD3);
      // subgoal (cyan)

      nddlKeywords.add("contains", Token.KEYWORD3);
      nddlKeywords.add("any", Token.KEYWORD3);
      nddlKeywords.add("starts", Token.KEYWORD3);
      nddlKeywords.add("ends", Token.KEYWORD3);
      nddlKeywords.add("equals", Token.KEYWORD3);
      nddlKeywords.add("equal", Token.KEYWORD3);
      nddlKeywords.add("before", Token.KEYWORD3);
      nddlKeywords.add("after", Token.KEYWORD3);
      nddlKeywords.add("contained_by", Token.KEYWORD3);
      nddlKeywords.add("ends_before", Token.KEYWORD3);
      nddlKeywords.add("ends_after", Token.KEYWORD3);
      nddlKeywords.add("starts_before_end", Token.KEYWORD3);
      nddlKeywords.add("ends_after_start", Token.KEYWORD3);
      nddlKeywords.add("contains_start", Token.KEYWORD3);
      nddlKeywords.add("starts_during", Token.KEYWORD3);
      nddlKeywords.add("contains_end", Token.KEYWORD3);
      nddlKeywords.add("ends_during", Token.KEYWORD3);
      nddlKeywords.add("meets", Token.KEYWORD3);
      nddlKeywords.add("met_by", Token.KEYWORD3);
      nddlKeywords.add("parallels", Token.KEYWORD3);
      nddlKeywords.add("paralleled_by", Token.KEYWORD3);
      nddlKeywords.add("starts_before", Token.KEYWORD3);
      nddlKeywords.add("starts_after", Token.KEYWORD3);

      // literals
      // bools
      nddlKeywords.add("true", Token.LITERAL3);
      nddlKeywords.add("false", Token.LITERAL3);
      // infs
      nddlKeywords.add("+inf", Token.LITERAL3);
      nddlKeywords.add("-inf", Token.LITERAL3);
      nddlKeywords.add("+inff", Token.LITERAL3);
      nddlKeywords.add("-inff", Token.LITERAL3);
      // states
      nddlKeywords.add("INACTIVE", Token.LITERAL1);
      nddlKeywords.add("ACTIVE", Token.LITERAL1);
      nddlKeywords.add("MERGED", Token.LITERAL1);
      nddlKeywords.add("REJECTED", Token.LITERAL1);

      // common stuff
      // classes
      nddlKeywords.add("Object", Token.LITERAL1);
      nddlKeywords.add("TokenStates", Token.LITERAL1);
      nddlKeywords.add("Timeline", Token.LITERAL1);
      nddlKeywords.add("Resource", Token.LITERAL1);
      nddlKeywords.add("UnaryResource", Token.LITERAL1);
      nddlKeywords.add("StringData", Token.LITERAL1);
      nddlKeywords.add("PlannerConfig", Token.LITERAL1);
      // constraints
      nddlKeywords.add("eq", Token.OPERATOR);
      nddlKeywords.add("neq", Token.OPERATOR);
      nddlKeywords.add("leq", Token.OPERATOR);
      nddlKeywords.add("addEq", Token.OPERATOR);
      nddlKeywords.add("mulEq", Token.OPERATOR);
      nddlKeywords.add("multEq", Token.OPERATOR);
      nddlKeywords.add("allDiff", Token.OPERATOR);
    }
    return nddlKeywords;
  }

  // private members
  private static KeywordMap nddlKeywords;

  private boolean cpp = true;
  private KeywordMap keywords;
  private int lastOffset;
  private int lastKeyword;

  private boolean doKeyword(Segment line, int i, char c) {
    int i1 = i+1;

    int len = i - lastKeyword;
    byte id = keywords.lookup(line,lastKeyword,len);
    if(id != Token.NULL) {
      if(lastKeyword != lastOffset)
        addToken(lastKeyword - lastOffset,Token.NULL);
      addToken(len,id);
      lastOffset = i;
    }
    lastKeyword = i1;
    return false;
  }
}
