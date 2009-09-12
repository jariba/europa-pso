/*
 * NddlInterpreter.cpp
 *
 *  Created on: Jan 21, 2009
 *      Author: javier
 */

#include "NddlInterpreter.hh"

#include <sys/stat.h>

#include "NDDL3Lexer.h"
#include "NDDL3Parser.h"
#include "NDDL3Tree.h"
#include "antlr3exception.h"

#include "Debug.hh"
#include "Utils.hh"

namespace EUROPA {

NddlInterpreter::NddlInterpreter(EngineId& engine)
  : m_engine(engine)
{
}

NddlInterpreter::~NddlInterpreter()
{
}

pANTLR3_INPUT_STREAM getInputStream(std::istream& input, const std::string& source)
{
    if (source == "<eval>") {
        // TODO: this is kind of a hack, see if it can be made more robust & efficient
        std::istringstream* is = dynamic_cast<std::istringstream*>(&input);
        std::string strInput = is->str(); // This makes a copy of the original string that could be avoided

        return antlr3NewAsciiStringInPlaceStream((pANTLR3_UINT8)strInput.c_str(),(ANTLR3_UINT32)strInput.size(),(pANTLR3_UINT8)source.c_str());
    }
    else {
        return antlr3AsciiFileStreamNew((pANTLR3_UINT8)source.c_str());
    }
}

bool isFile(const std::string& filename)
{
    struct stat my_stat;
    return (stat(filename.c_str(), &my_stat) == 0);
}

bool NddlInterpreter::queryIncludeGuard(const std::string& f)
{
    for (unsigned int i = 0; i < m_filesread.size(); i++) {
      if (m_filesread[i] == f) { //Not the best. Fails if the paths differ in 'absoluteness'
	    return true;
        }
    }
    return false;
}

void NddlInterpreter::addInclude(const std::string &f)
{
    if (f != "<eval>")
        m_filesread.push_back(f);
}

void NddlInterpreter::addInputStream(pANTLR3_INPUT_STREAM in)
{
    m_inputstreams.push_back(in);
}

std::vector<std::string> NddlInterpreter::getIncludePath()
{
    // TODO: cache this
    std::vector<std::string> includePath;

    // Add overrides from config;
    const std::string& configPathStr = getEngine()->getConfig()->getProperty("nddl.includePath");
    if (configPathStr.size() > 0) {
        LabelStr configPath=configPathStr;
        unsigned int cnt = configPath.countElements(":");
        for (unsigned int i=0;i<cnt;i++)
            includePath.push_back(configPath.getElement(i,":").c_str());
    }

    // Look in current dir first
    includePath.push_back(".");

    // otherwise, look in include path, starting with $EUROPA_HOME, then $PLASMA_HOME
    const char* europaHome = std::getenv("EUROPA_HOME");
    if (europaHome != NULL)
        includePath.push_back(std::string(europaHome)+"/include");
    else // TODO: this should be at least INFO, possibly WARNING
        debugMsg("NddlInterpreter","$EUROPA_HOME is not defined, therefore not added to NddlInterpreter include path");

    const char* plasmaHome = std::getenv("PLASMA_HOME");
    if (plasmaHome != NULL) {
        includePath.push_back(std::string(plasmaHome)+"/src/PLASMA/NDDL/base");
        includePath.push_back(std::string(plasmaHome)+"/src/PLASMA/Resource/component/NDDL");
    }

    // TODO: dump includePath to log
    return includePath;
}


std::string NddlInterpreter::getFilename(const std::string& f)
{
    std::string fname = f.substr(1,f.size()-2); // remove quotes

    std::vector<std::string> includePath = getIncludePath();

    for (unsigned int i=0; i<includePath.size();i++) {
        // TODO: this may not be portable to all OSs
        std::string fullName = includePath[i]+"/"+fname;
        if (isFile(fullName)) {
            debugMsg("NddlInterpreter","Found:" << fullName);
            return fullName;
        }
        else
            debugMsg("NddlInterpreter",fullName << " doesn't exist");
    }

    return "";
}



std::string NddlInterpreter::interpret(std::istream& ins, const std::string& source)
{
    if (queryIncludeGuard(source))
    {
      debugMsg("NddlInterpreter:error", "Ignoring root file: " << source << ". Bug?");
        return "";
    }
    addInclude(source);

    pANTLR3_INPUT_STREAM input = getInputStream(ins,source);

    pNDDL3Lexer lexer = NDDL3LexerNew(input);
    lexer->parserObj = this;
    pANTLR3_COMMON_TOKEN_STREAM tstream = antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, TOKENSOURCE(lexer));
    pNDDL3Parser parser = NDDL3ParserNew(tstream);

    // Build he AST
    NDDL3Parser_nddl_return result = parser->nddl(parser);
    int errorCount = parser->pParser->rec->state->errorCount + lexer->pLexer->rec->state->errorCount;
    if (errorCount > 0) {
        // Since errors are no longer printed during parsing, print them here
        // to debugMsg
        std::vector<PSLanguageException> *lerrors = lexer->lexerErrors;
        std::vector<PSLanguageException> *perrors = parser->parserErrors;
        for (std::vector<PSLanguageException>::const_iterator it = lerrors->begin(); it != lerrors->end(); ++it) {
        	debugMsg("NddlInterpreter:interpret", it->asString());
        }
        for (std::vector<PSLanguageException>::const_iterator it = perrors->begin(); it != perrors->end(); ++it) {
        	debugMsg("NddlInterpreter:interpret", it->asString());
        }
        // Copy errors over
        std::vector<PSLanguageException> all(*lerrors);
        for (std::vector<PSLanguageException>::const_iterator it = perrors->begin(); it != perrors->end(); ++it)
        	all.push_back(*it);

        // Close everything nicely
        parser->free(parser);
        tstream->free(tstream);
        lexer->free(lexer);
        input->close(input);

        debugMsg("NddlInterpreter:interpret", "Interpreter returned errors");

        // Now throw the whole thing
        throw PSLanguageExceptionList(all);
    }
    else {
        debugMsg("NddlInterpreter:interpret","NDDL AST:\n" << result.tree->toStringTree(result.tree)->chars);
    }

    // Walk the AST to create nddl expr to evaluate
    pANTLR3_COMMON_TREE_NODE_STREAM nodeStream = antlr3CommonTreeNodeStreamNewTree(result.tree, ANTLR3_SIZE_HINT);
    pNDDL3Tree treeParser = NDDL3TreeNew(nodeStream);

    NddlSymbolTable symbolTable(m_engine);
    treeParser->SymbolTable = &symbolTable;

    try {
        treeParser->nddl(treeParser);
    }
    catch (const std::string& error) {
        debugMsg("NddlInterpreter:error","nddl parser halted on error:" << symbolTable.getErrors());
        return symbolTable.getErrors();
    }
    catch (const Error& internalError) {
        symbolTable.reportError(treeParser,internalError.getMsg());
        debugMsg("NddlInterpreter:error","nddl parser halted on error:" << symbolTable.getErrors());
        return symbolTable.getErrors();
    }

    // Free everything
    treeParser->free(treeParser);
    nodeStream->free(nodeStream);

    while(!m_inputstreams.empty()) {
      m_inputstreams[0]->close(m_inputstreams[0]);
      m_inputstreams.erase(m_inputstreams.begin());
    }

    parser->free(parser);
    tstream->free(tstream);
    lexer->free(lexer);
    input->close(input);
    return symbolTable.getErrors();
}


NddlSymbolTable::NddlSymbolTable(NddlSymbolTable* parent)
    : EvalContext(parent)
    , m_parentST(parent)
{
}

NddlSymbolTable::NddlSymbolTable(const EngineId& engine)
    : EvalContext(NULL)
    , m_parentST(NULL)
    , m_engine(engine)
{
}

NddlSymbolTable::~NddlSymbolTable()
{
}

NddlSymbolTable* NddlSymbolTable::getParentST() { return m_parentST; }

const EngineId& NddlSymbolTable::engine() const { return (m_parentST==NULL ? m_engine : m_parentST->engine()); }
std::vector<std::string>& NddlSymbolTable::errors() { return (m_parentST==NULL ? m_errors : m_parentST->errors()); }
const std::vector<std::string>& NddlSymbolTable::errors() const { return (m_parentST==NULL ? m_errors : m_parentST->errors()); }

void NddlSymbolTable::addError(const std::string& msg)
{
    errors().push_back(msg);
    debugMsg("NddlInterpreter:SymbolTable","SEMANTIC ERROR reported : " << msg);
}

std::string NddlSymbolTable::getErrors() const
{
    std::ostringstream os;

    for (unsigned int i=0; i<errors().size(); i++)
        os << errors()[i] << std::endl;

    return os.str();
}

void* NddlSymbolTable::getElement(const char* name) const
{
    EngineComponent* component = engine()->getComponent(name);

    if (component != NULL)
        return component;

    return EvalContext::getElement(name);
}

const PlanDatabaseId& NddlSymbolTable::getPlanDatabase() const
{
    return ((PlanDatabase*)getElement("PlanDatabase"))->getId();
}

DataTypeId NddlSymbolTable::getDataType(const char* name) const
{
    CESchemaId ces = ((CESchema*)getElement("CESchema"))->getId();

    if (ces->isDataType(name))
        return ces->getDataType(name);

    debugMsg("NddlInterpreter:SymbolTable","Unknown type " << name);
    return DataTypeId::noId();
}

ObjectTypeId NddlSymbolTable::getObjectType(const char* name) const
{
    if (m_parentST != NULL)
        return m_parentST->getObjectType(name);
    else {
        SchemaId s = ((Schema*)(engine()->getComponent("Schema")))->getId();

        if (s->isObjectType(name))
            return s->getObjectType(name);
    }

    return ObjectTypeId::noId();
}

TokenTypeId NddlSymbolTable::getTokenType(const char* name) const
{
    if (m_parentST != NULL)
        return m_parentST->getTokenType(name);
    else {
        SchemaId s = ((Schema*)(engine()->getComponent("Schema")))->getId();

        if (s->isPredicate(name))
            return s->getTokenType(name);
    }

    return TokenTypeId::noId();
}


void NddlSymbolTable::addLocalVar(const char* name,const DataTypeId& type)
{
    m_localVars[name]=type;
    debugMsg("NddlSymbolTable:addLocalVar","Added local var "+std::string(name));
}

void NddlSymbolTable::addLocalToken(const char* name,const TokenTypeId& type)
{
    m_localTokens[name]=type;
    debugMsg("NddlSymbolTable:addLocalToken","Added local token "+std::string(name));
}

DataTypeId NddlSymbolTable::getTypeForVar(const char* name)
{
    if (m_localVars.find(name) != m_localVars.end())
        return m_localVars[name];

    if (m_parentST != NULL)
        return m_parentST->getTypeForVar(name);
    else if (getPlanDatabase()->isGlobalVariable(name))
        return getPlanDatabase()->getGlobalVariable(name)->getDataType();

    return DataTypeId::noId();
}

DataTypeId NddlSymbolTable::getTypeForVar(const char* qualifiedName,std::string& errorMsg)
{
    std::string parentName;
    std::vector<std::string> vars;
    std::string fullName(qualifiedName);
    tokenize(fullName,vars,".");

    if (vars.size() > 1) {
      parentName = vars[0];
      fullName = fullName.substr(parentName.length()+1);
      vars.erase(vars.begin());
      debugMsg("NddlSymbolTable:getTypeForVar","Split " << qualifiedName << " into " << parentName << " and " << fullName);
    }
    else {
      parentName = "";
      debugMsg("NddlSymbolTable:getTypeForVar","Didn't split " << qualifiedName);
    }

    DataTypeId dt;

    if (parentName == "") {
        dt = getTypeForVar(qualifiedName);
        if (dt.isNoId()) {
            // We failed to find a variable, let's try to find a token
            TokenTypeId tt = getTypeForToken(qualifiedName);

            if (tt.isId())
                dt =  FloatDT::instance(); // TODO : return data type for state var?
            else
                errorMsg = fullName + " is not defined";
        }

        return dt;
    }
    else {
        unsigned int idx = 0;
        std::string curVarName=parentName;
        std::string curVarType;

        TokenTypeId tt = getTypeForToken(parentName.c_str());
        if (tt.isNoId()) {
            dt = getTypeForVar(parentName.c_str(),errorMsg);
            if (dt.isNoId())
                return dt;
        }
        else {
            dt = tt->getArgType(vars[idx].c_str());
            if (dt.isNoId()) {
                errorMsg = curVarName+"("+tt->getPredicateName().toString()+") doesn't have a parameter called "+vars[idx];
                return dt;
            }
            curVarName = vars[idx];
            idx++;
        }
        curVarType = dt->getName().toString();

        for (;idx<vars.size();idx++) {
            ObjectTypeId ot = getObjectType(curVarType.c_str());
            if (ot.isNoId()) {
                errorMsg = curVarName+"("+curVarType+") is not a reference to an object";
                return DataTypeId::noId();
            }

            dt = ot->getMemberType(vars[idx].c_str());
            if (dt.isNoId()) {
                errorMsg = curVarName+"("+curVarType+") doesn't have a member called "+vars[idx];
                return DataTypeId::noId();
            }

            curVarName = vars[idx];
            curVarType = dt->getName().toString();
        }
    }

    return dt;

}

TokenTypeId NddlSymbolTable::getTypeForToken(const char* name)
{
    if (m_localTokens.find(name) != m_localTokens.end())
        return m_localTokens[name];

    if (m_parentST != NULL)
        return m_parentST->getTypeForToken(name);
    else if (getPlanDatabase()->isGlobalToken(name)) {
        // TODO: modify Token to keep a handle on its Token Type
        TokenId t = getPlanDatabase()->getGlobalToken(name);
        return getTokenType(t->getPredicateName().c_str());
    }

    return TokenTypeId::noId();
}


TokenTypeId NddlSymbolTable::getTypeForToken(const char* qualifiedName,std::string& errorMsg)
{
    std::string parentName;
    std::string tokenType;
    std::vector<std::string> vars;
    std::string fullName(qualifiedName);
    tokenize(fullName,vars,".");

    if (vars.size() > 1) {
      parentName = vars[0];
      vars.erase(vars.begin());
      fullName = fullName.substr(parentName.length()+1);

      tokenType = vars.back();
      vars.erase(--vars.end());
      fullName = fullName.substr(0,fullName.length()-(tokenType.length()));

      debugMsg("NddlSymbolTable:getTypeForToken","Split " << qualifiedName
                                                           << " into " << parentName
                                                           << " , " << tokenType
                                                           << " and " << fullName);
    }
    else {
      parentName = "";
      debugMsg("NddlSymbolTable:getTypeForToken","Didn't split " << qualifiedName);
    }

    TokenTypeId tt;

    if (parentName == "") {
        tt = getTokenType(qualifiedName);
        if (tt.isNoId())
            errorMsg = fullName + " is not a predicate type";

        return tt;
    }
    else {
        DataTypeId dt;
        ObjectTypeId ot = getObjectType(parentName.c_str());
        if (ot.isId()) {
            if (vars.size() > 1) {
                errorMsg = std::string(qualifiedName)+" is not a predicate type";
                return tt;
            }
            return getTokenType(qualifiedName);
        }
        else {
            dt = getTypeForVar(parentName.c_str());
            if (dt.isNoId()) {
                errorMsg = parentName + " is not defined";
                return tt;
            }
            ot = getObjectType(dt->getName().c_str());
            if (ot.isNoId()) {
                errorMsg = parentName+"("+dt->getName().c_str()+") is not a reference to an object";
                return tt;
            }
        }

        std::string curVarName=parentName;
        std::string curVarType = dt->getName().toString();
        unsigned int idx = 0;
        for (;idx<vars.size();idx++) {
            dt = ot->getMemberType(vars[idx].c_str());
            if (dt.isNoId()) {
                errorMsg = curVarName+"("+curVarType+") doesn't have a member called "+vars[idx];
                return TokenTypeId::noId();
            }

            ot = getObjectType(dt->getName().c_str());
            if (ot.isNoId()) {
                errorMsg = curVarName+"("+curVarType+") is not a reference to an object";
                return TokenTypeId::noId();
            }

            curVarName = vars[idx];
            curVarType = dt->getName().toString();
        }

        tokenType = ot->getName().toString()+"."+tokenType;
        debugMsg("NddlSymbolTable:getTypeForToken","looking for tokenType " << tokenType);
        return getTokenType(tokenType.c_str());
    }

    return tt;
}


AbstractDomain* NddlSymbolTable::makeNumericDomainFromLiteral(const std::string& type,
                                                              const std::string& value)
{
    // TODO: only one copy should be kept for each literal, domains should be marked as constant
    CESchemaId ces = ((CESchema*)getElement("CESchema"))->getId();
    AbstractDomain* retval = ces->baseDomain(type.c_str()).copy();
    double v = getPlanDatabase()->getClient()->createValue(type.c_str(), value);
    retval->set(v);

    return retval;
}

ConstrainedVariableId NddlSymbolTable::getVar(const char* name)
{
    if (getPlanDatabase()->isGlobalVariable(name))
        return getPlanDatabase()->getGlobalVariable(name);
    else
        return EvalContext::getVar(name);
}

TokenId NddlSymbolTable::getToken(const char* name)
{
    if (getPlanDatabase()->isGlobalToken(name))
        return getPlanDatabase()->getGlobalToken(name);
    else
        return EvalContext::getToken(name);
}

const LabelStr& NddlSymbolTable::getEnumForValue(const char* value) const
{
    if (m_parentST == NULL)
        return getPlanDatabase()->getSchema()->getEnumForValue(LabelStr(value));

    return m_parentST->getEnumForValue(value);
}

bool NddlSymbolTable::isEnumValue(const char* value) const
{
    if (m_parentST == NULL)
        return getPlanDatabase()->getSchema()->isEnumValue(LabelStr(value));

    return m_parentST->isEnumValue(value);
}

Expr* NddlSymbolTable::makeEnumRef(const char* value) const
{
    const LabelStr& enumType = getEnumForValue(value);
    EnumeratedDomain* ad = dynamic_cast<EnumeratedDomain*>(
            getPlanDatabase()->getSchema()->getCESchema()->baseDomain(enumType.c_str()).copy());
    double v = LabelStr(value);
    ad->set(v);

    return new ExprConstant(enumType.c_str(),ad);
}

std::string getErrorLocation(pNDDL3Tree treeWalker);
void NddlSymbolTable::reportError(void* tw, const std::string& msg)
{
    pNDDL3Tree treeWalker = (pNDDL3Tree) tw;
    addError(getErrorLocation(treeWalker) + "\n" + msg);
}

void NddlSymbolTable::checkConstraint(const char* name,const std::vector<Expr*>& args)
{
    CESchemaId ceSchema = getPlanDatabase()->getSchema()->getCESchema();
    if (!ceSchema->isConstraintType(name))
        throw std::string("Unknown constraint type:")+name;

    ConstraintTypeId ctype = ceSchema->getConstraintType(name);
    std::vector<DataTypeId> argTypes;
    for (unsigned int i=0;i<args.size();i++)
        argTypes.push_back(args[i]->getDataType());

    ctype->checkArgTypes(argTypes);
}

void NddlSymbolTable::checkObjectFactory(const char* name, const std::vector<Expr*>& args)
{
    if (!getPlanDatabase()->getSchema()->isObjectType(name)) {
      throw std::string(name + std::string(" is not a valid object type."));
    }

    std::vector<const AbstractDomain*> argTypes;
    for (unsigned int i=0;i<args.size();i++)
      argTypes.push_back(&args[i]->getDataType()->baseDomain());

    ObjectFactoryId factory = getPlanDatabase()->getSchema()->getObjectFactory(name, argTypes, false);
    if (factory.isNoId()) {
      std::string argsig = "";
      for (unsigned int i=0;i<args.size();i++)
	argsig += std::string(std::string(":") + std::string(args[i]->getDataType()->getName().c_str()));
      throw std::string(std::string("Invalid object constructor: ") + name + argsig);
    }
}

NddlClassSymbolTable::NddlClassSymbolTable(NddlSymbolTable* parent, ObjectType* ot)
    : NddlSymbolTable(parent)
    , m_objectType(ot)
{
}

NddlClassSymbolTable::~NddlClassSymbolTable()
{
}

DataTypeId NddlClassSymbolTable::getDataType(const char* name) const
{
    if (m_objectType->getName().toString()==name)
        return m_objectType->getVarType();

    return NddlSymbolTable::getDataType(name);
}

ObjectTypeId NddlClassSymbolTable::getObjectType(const char* name) const
{
    if (m_objectType->getName().toString()==name)
        return m_objectType->getId();

    return NddlSymbolTable::getObjectType(name);
}

DataTypeId NddlClassSymbolTable::getTypeForVar(const char* varName)
{
    DataTypeId dt = m_objectType->getMemberType(varName);
    if (dt.isId())
        return dt;

    return NddlSymbolTable::getTypeForVar(varName);
}


NddlTokenSymbolTable::NddlTokenSymbolTable(NddlSymbolTable* parent,
                                           const TokenTypeId& tt,
                                           const ObjectTypeId& ot)
    : NddlSymbolTable(parent)
    , m_tokenType(tt)
    , m_objectType(ot)
{
}

NddlTokenSymbolTable::~NddlTokenSymbolTable()
{
}

DataTypeId NddlTokenSymbolTable::getTypeForVar(const char* varName)
{
    if (std::string(varName)=="object")
        return m_objectType->getVarType();

    DataTypeId dt = m_tokenType->getArgType(varName);
    if (dt.isId())
        return dt;

    return NddlSymbolTable::getTypeForVar(varName);
}

TokenTypeId NddlTokenSymbolTable::getTokenType(const char* name) const
{
    TokenTypeId tt= NddlSymbolTable::getTokenType(name);

    if (tt.isNoId()) {
        // Try implicit qualification
        std::string qualifiedName = m_objectType->getName().toString()+"."+name;
        tt= NddlSymbolTable::getTokenType(qualifiedName.c_str());
    }

    return tt;
}

TokenTypeId NddlTokenSymbolTable::getTypeForToken(const char* name)
{
    if (std::string(name)=="this")
        return m_tokenType;

    return NddlSymbolTable::getTypeForToken(name);
}



NddlToASTInterpreter::NddlToASTInterpreter(EngineId& engine)
    : NddlInterpreter(engine)
{
}

NddlToASTInterpreter::~NddlToASTInterpreter()
{
}

pANTLR3_STRING toVerboseString(pANTLR3_BASE_TREE tree);
pANTLR3_STRING toVerboseStringTree(pANTLR3_BASE_TREE tree);

std::string NddlToASTInterpreter::interpret(std::istream& ins, const std::string& source)
{
    pANTLR3_INPUT_STREAM input = getInputStream(ins,source);

    pNDDL3Lexer lexer = NDDL3LexerNew(input);
    lexer->parserObj = this;
    pANTLR3_COMMON_TOKEN_STREAM tstream = antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, TOKENSOURCE(lexer));
    pNDDL3Parser parser = NDDL3ParserNew(tstream);

    // Build he AST
    NDDL3Parser_nddl_return result = parser->nddl(parser);

    // The result
    std::ostringstream os;

    // Add errors, if any
    std::vector<PSLanguageException> *lerrors = lexer->lexerErrors;
    std::vector<PSLanguageException> *perrors = parser->parserErrors;

    for (unsigned int i=0; i<lerrors->size(); i++)
    	os << "L" << (*lerrors)[i] << "$\n";
    for (unsigned int i=0; i<perrors->size(); i++)
    	os << "P" << (*perrors)[i] << "$\n";
    // Warnings, if any, should go here

    // Calling static helper functions to get a verbose version of AST
    const char* ast = (char*)(toVerboseStringTree(result.tree)->chars);
    os << "AST " << ast;

    debugMsg("NddlToASTInterpreter:interpret",os.str());

    parser->free(parser);
    tstream->free(tstream);
    lexer->free(lexer);
    input->close(input);

    return os.str();
}

// Antlr functions
std::string getErrorLocation(pNDDL3Tree treeWalker)
{
    std::ostringstream os;

    // get location. see displayRecognitionError() in antlr3baserecognizer.c
    pANTLR3_BASE_RECOGNIZER rec = treeWalker->pTreeParser->rec;
    if (rec->state->exception == NULL) {
        antlr3RecognitionExceptionNew(rec);
        //rec->state->exception->type = ANTLR3_RECOGNITION_EXCEPTION;
        //rec->state->exception->message = (char*)msg.c_str();
    }
    //rec->reportError(rec);

    pANTLR3_EXCEPTION ex = rec->state->exception;
    if  (ex->streamName == NULL) {
        if  (((pANTLR3_COMMON_TOKEN)(ex->token))->type == ANTLR3_TOKEN_EOF)
            os << "-end of input-(";
        else
            os << "-unknown source-(";
    }
    else {
        pANTLR3_STRING ftext = ex->streamName->to8(ex->streamName);
        os << ftext->chars << "(";
    }

    // Next comes the line number
    os << "line:" << rec->state->exception->line << ")";

    pANTLR3_BASE_TREE theBaseTree = (pANTLR3_BASE_TREE)(rec->state->exception->token);
    pANTLR3_STRING ttext       = theBaseTree->toStringTree(theBaseTree);

    os << ", at offset " << theBaseTree->getCharPositionInLine(theBaseTree);
    os << ", near " <<  ttext->chars;

    return os.str();
}

/**
 *  Create a verbose string for a single tree node:
 *  "text":token-type:"file":line:offset-in-line
 */
pANTLR3_STRING toVerboseString(pANTLR3_BASE_TREE tree)
{
	if  (tree->isNilNode(tree) == ANTLR3_TRUE)
	{
		pANTLR3_STRING  nilNode;
		nilNode	= tree->strFactory->newPtr(tree->strFactory, (pANTLR3_UINT8)"nil", 3);
		return nilNode;
	}

	pANTLR3_COMMON_TOKEN ptoken = tree->getToken(tree);
	pANTLR3_INPUT_STREAM pstream = ptoken->input;
	pANTLR3_STRING  string = tree->strFactory->newRaw(tree->strFactory);

	// "text":token-type:"file":line:offset-in-line
	string->append8 (string, "\""); // "text"
	string->appendS	(string, ((pANTLR3_COMMON_TREE)(tree->super))->token->
			getText(((pANTLR3_COMMON_TREE)(tree->super))->token));
	string->append8	(string, "\"");
	string->append8 (string, ":");
	string->addi (string, tree->getType(tree)); // type

	// if no file (e.g., root NDDL node), last three items are dropped
	if (pstream) {
		string->append8 (string, ":");
		string->append8	(string, "\""); // "file", full path
		string->appendS(string, pstream->fileName);
		string->append8	(string, "\"");
		string->append8 (string, ":");
		string->addi (string, tree->getLine(tree)); // line
		string->append8 (string, ":");
		string->addi (string, ptoken->charPosition); // offset in line
	}

	return string;
}

/** Create a verbose string for the whole tree */
pANTLR3_STRING toVerboseStringTree(pANTLR3_BASE_TREE tree)
{
	pANTLR3_STRING  string;
	ANTLR3_UINT32   i;
	ANTLR3_UINT32   n;
	pANTLR3_BASE_TREE   t;

	if	(tree->children == NULL || tree->children->size(tree->children) == 0)
		return	toVerboseString(tree);

	/* Need a new string with nothing at all in it.
	*/
	string	= tree->strFactory->newRaw(tree->strFactory);

	if	(tree->isNilNode(tree) == ANTLR3_FALSE)
	{
		string->append8	(string, "(");
		string->appendS	(string, toVerboseString(tree));
		string->append8	(string, " ");
	}
	if	(tree->children != NULL)
	{
		n = tree->children->size(tree->children);

		for	(i = 0; i < n; i++)
		{
			t   = (pANTLR3_BASE_TREE) tree->children->get(tree->children, i);

			if  (i > 0)
			{
				string->append8(string, " ");
			}
			string->appendS(string, toVerboseStringTree(t));
		}
	}
	if	(tree->isNilNode(tree) == ANTLR3_FALSE)
	{
		string->append8(string,")");
	}

	return  string;
}

PSLanguageException::PSLanguageException(const char *fileName, int line,
		int offset, int length, const char *message) :
  m_line(line), m_offset(offset), m_length(length), m_message(message) {
  if (fileName) m_fileName = fileName;
  else fileName = "No_File";
}

ostream &operator<<(ostream &os, const PSLanguageException &ex)
{
	os << "\"" << ex.m_fileName << "\":" <<ex.m_line << ":" << ex.m_offset << ":"
		<< ex.m_length << " " << ex.m_message;

	return os;
}

std::string PSLanguageException::asString() const {
	std::ostringstream os;
	os << *this;
	return os.str();
}

PSLanguageExceptionList::PSLanguageExceptionList(const std::vector<PSLanguageException>& exceptions) :
  m_exceptions(exceptions) {}

ostream &operator<<(ostream &os, const PSLanguageExceptionList &ex) {
	os << "Got " << ex.m_exceptions.size() << " exceptions: " << std::endl;
	for (std::vector<EUROPA::PSLanguageException>::const_iterator it = ex.m_exceptions.begin();
		it != ex.m_exceptions.end(); ++it)
		os << *it << std::endl;
	return os;
}


void reportParserError(pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_UINT8 *tokenNames) {
	pANTLR3_EXCEPTION ex = recognizer->state->exception;

	const char *fileName = NULL;
	if (ex->streamName)
		fileName = (const char *)(ex->streamName->to8(ex->streamName)->chars);

	int line = recognizer->state->exception->line;
	const char* message = static_cast<const char *>(recognizer->state->exception->message);

	int offset = -1; // to signal something is wrong (like recognizer type)
	int length = 0; // in case there is no token
	if (recognizer->type == ANTLR3_TYPE_PARSER) {
		offset = recognizer->state->exception->charPositionInLine;

		// Look for a token
		pANTLR3_COMMON_TOKEN token = (pANTLR3_COMMON_TOKEN)(recognizer->state->exception->token);
		line = token->getLine(token);
		pANTLR3_STRING text = token->getText(token);
		if (text != NULL) {
			// It looks like when an extra token is present, message is
			// empty and the token points to the actual thing. When a token
			// is missing, the token text contains the message and the length
			// is irrelevant
			if (message == NULL || !message[0])
				length = text->len;
			message = (const char *)(text->chars);
		}
	}

	PSLanguageException exception(fileName, line, offset, length, message);

	pANTLR3_PARSER parser = (pANTLR3_PARSER) recognizer->super;
	pNDDL3Parser ctx = (pNDDL3Parser) parser->super;
	std::vector<PSLanguageException> *errors = ctx->parserErrors;
	errors->push_back(exception);

	// std::cout << errors->size() << "; " << (*errors)[errors->size()-1];
}

void reportLexerError(pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_UINT8 *tokenNames) {
    pANTLR3_LEXER lexer = (pANTLR3_LEXER)(recognizer->super);
    pANTLR3_EXCEPTION ex = lexer->rec->state->exception;

	const char *fileName = NULL;
	if (ex->streamName)
		fileName = (const char *)(ex->streamName->to8(ex->streamName)->chars);

	int line = recognizer->state->exception->line;
	const char* message = static_cast<const char *>(recognizer->state->exception->message);
	int offset = ex->charPositionInLine+1;

	PSLanguageException exception(fileName, line, offset, 1, message);

	pNDDL3Lexer ctx = (pNDDL3Lexer) lexer->ctx;
	std::vector<PSLanguageException> *errors = ctx->lexerErrors;
	errors->push_back(exception);

	// std::cout << errors->size() << "; " << (*errors)[errors->size()-1];
}
}
