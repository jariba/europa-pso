/**
 * @file Interpreter.cc
 * @brief Core classes to support an interpreter on top of the EUROPA runtime
 * @author Javier Barreiro
 * @date December, 2006
 */

#include "Interpreter.hh"

#include <string.h>
#include <stdio.h>

#include "Debug.hh"
#include "Error.hh"
#include "Utils.hh"

#include "ConstraintType.hh"
#include "DataTypes.hh"
#include "Domains.hh"

#include "DbClient.hh"
#include "DbClientTransactionPlayer.hh"
#include "Object.hh"
#include "ObjectType.hh"
#include "Schema.hh"
#include "TokenVariable.hh"

#include "NddlRules.hh"
#include "NddlUtils.hh"

namespace EUROPA {

  // TODO: keep using pdbClient?
  const DbClientId& getPDB(EvalContext& context)
  {
      // TODO: Add this behavior to EvalContext instead?
      DbClient* dbClient = (DbClient*)context.getElement("DbClient");
      if (dbClient != NULL)
          return dbClient->getId();

      PlanDatabase* pdb = (PlanDatabase*)context.getElement("PlanDatabase");
      check_error(pdb != NULL,"Could not find Plan Database in eval context");
      return pdb->getClient();
  }

  const SchemaId& getSchema(EvalContext& context)
  {
      /* TODO why doesn't this work???
      Schema* schema = (Schema*)context.getElement("Schema");
      check_error(schema != NULL,"Could not find Schema in eval context");
      return schema->getId();
      */
      return getPDB(context)->getSchema();
  }

  // TODO: move this to the eval contexts to make it cleaner
  void makeConstraint(EvalContext& context,
                      const LabelStr& name,
                      const std::vector<ConstrainedVariableId>& vars,
                      const char* violationExpl)
  {
      PlanDatabase* pdb = (PlanDatabase*)(context.getElement("PlanDatabase"));
      ConstraintId c = pdb->getClient()->createConstraint(name.c_str(), vars, violationExpl);
      debugMsg("Interpreter","Added Constraint : " << c->toString());

      InterpretedRuleInstance* rule = (InterpretedRuleInstance*)(context.getElement("RuleInstance"));
      if (rule != NULL) {
          rule->addConstraint(c);
          debugMsg("Interpreter:InterpretedRule","Added Constraint : " << c->toString());
          return;
      }

      Token* t = (Token*)(context.getElement("Token"));
      if (t != NULL) {
          t->addStandardConstraint(c);
          debugMsg("Interpreter:InterpretedToken","Added Constraint : " << c->toString());
          return;
      }
  }


  /*
   * ExprConstant
   */
  ExprConstant::ExprConstant(const char* type, const Domain* domain)
    : m_type(type)
    , m_domain(domain)
  {
  }

  ExprConstant::~ExprConstant()
  {
      delete m_domain;
  }

  const DataTypeId ExprConstant::getDataType() const
  {
      return m_domain->getDataType();
  }

  std::string getAutoName(const char* prefix)
  {
      static int cnt = 0;
      std::stringstream sstr;
      sstr << prefix << ++cnt;
      return sstr.str();
  }

  DataRef ExprConstant::eval(EvalContext& context) const
  {
    // TODO: need to create a new variable every time this is evaluated, since propagation
    // will affect the variable, should provide immutable variables so that a single var
    // can be created for a constant.
    ConstrainedVariableId var;

    bool canBeSpecified = false;
    std::string name = getAutoName("ExprConstant_PSEUDO_VARIABLE_");

    // TODO: this isn't pretty, have the different EvalContexts create the new var
    RuleInstanceEvalContext *riec = dynamic_cast<RuleInstanceEvalContext*>(&context);
    if (riec != NULL) {
        var = riec->getRuleInstance()->addLocalVariable(*m_domain,canBeSpecified,name);
    }
    else {
        DbClientId pdb = getPDB(context);
        // TODO: Who destroys this variable?
        var = pdb->createVariable(
                m_type.c_str(),
                *m_domain,
                name.c_str(),
                true, // isTmp
                canBeSpecified
        );
    }

    return DataRef(var);
  }

  std::string ExprConstant::toString() const
  {
      std::ostringstream os;

      os << "{CONSTANT " << m_type.c_str() << " " << m_domain->toString() << "}";
      return os.str();
  }

  std::string ExprConstant::getConstantValue() const
  {
      std::ostringstream os;
      std::string typeName(m_type.c_str());

      // TODO: this is a hack, where is this being used?
      if (m_domain->isSingleton()) {
    	  if (typeName == IntDT::NAME())
            os << eint(cast_int((m_domain->getSingletonValue())));
    	  else if (typeName == FloatDT::NAME())
    	      os << m_domain->getSingletonValue();
    	  else if (typeName == StringDT::NAME())
			  os << LabelStr(m_domain->getSingletonValue()).toString();
          else
        	  os << "CONST_" << typeName << " " << m_domain->toString();
      }
      else
    	  os << "CONST_" << typeName << " " << m_domain->toString();

      return os.str();
  }



  /*
   * ExprVarRef
   */
  ExprVarRef::ExprVarRef(const char* varName, const DataTypeId& type)
    : m_varName(varName)
    , m_varType(type)
  {
    tokenize(m_varName,m_vars,".");

    if (m_vars.size() > 1) {
      m_parentName = m_vars[0];
      m_varName = m_varName.substr(m_parentName.length()+1);
      m_vars.erase(m_vars.begin());
      debugMsg("Interpreter:ExprVarRef","Split " << varName << " into " << m_parentName << " and " << m_varName);
    }
    else {
      m_parentName = "";
      debugMsg("Interpreter:ExprVarRef","Didn't split " << varName);
    }
  }

  ExprVarRef::~ExprVarRef()
  {
  }

  const DataTypeId ExprVarRef::getDataType() const
  {
      return m_varType;
  }

  DataRef ExprVarRef::eval(EvalContext& context) const
  {
    ConstrainedVariableId var;

    if (m_parentName == "") {
        var = context.getVar(m_varName.c_str());
        if (var.isNoId()) {
            // If var evaluates to a token, return state var.
            TokenId tok = context.getToken(m_varName.c_str());
            if (tok.isNoId()) {
                check_runtime_error(!var.isNoId(),std::string("Couldn't find variable or token '" )+m_varName+"' in Evaluation Context");
                return DataRef::null;
            }
            var = tok->getState();
        }
    }
    else {
        TokenId tok = context.getToken(m_parentName.c_str());
        if (tok.isNoId()) {
            var = context.getVar(m_parentName.c_str());
            if (var.isNoId()) {
                check_runtime_error(ALWAYS_FAILS,std::string("Couldn't find variable or token '")+m_parentName+"' in Evaluation Context");
                return DataRef::null;
            }
        }

        // TODO: this isn't pretty, have the different EvalContexts perform the lookup
        // TODO: is this really still necessary?, code in "else" block should work in ruleInstance context as well
        RuleInstanceEvalContext *riec = dynamic_cast<RuleInstanceEvalContext*>(&context);
        if (riec != NULL) {
            if (tok.isId())
                var = riec->getRuleInstance()->varfromtok(tok,m_varName);
            else
                var = riec->getRuleInstance()->varFromObject(m_parentName,m_varName,false);
        }
        else {
            std::string varName=m_vars[0];
            unsigned int idx = 0;

            if (tok.isId()) {
                var = tok->getVariable(varName,false);
                idx++;
            }

            for (;idx<m_vars.size();idx++) {
              check_runtime_error(var->derivedDomain().isSingleton(),varName+" must be singleton to be able to get to "+m_vars[idx]);
              ObjectId object = Entity::getTypedEntity<Object>(var->derivedDomain().getSingletonValue());
              var = object->getVariable(object->getName().toString()+"."+m_vars[idx]);
              varName += "." + m_vars[idx];

              if (var.isNoId())
                  check_runtime_error(ALWAYS_FAILS,std::string("Couldn't find variable ")+m_vars[idx]+
                        " in object \""+object->getName().toString()+"\" of type "+object->getType().toString());
            }
        }
    }

    return DataRef(var);
  }

  std::string ExprVarRef::toString() const
  {
      if (m_parentName.size()==0)
          return m_varName;
      else
          return m_parentName + "." + m_varName;
  }


  /*
   * ExprNewObject
   */
  ExprNewObject::ExprNewObject(const LabelStr& objectType,
			       const LabelStr& objectName,
			       const std::vector<Expr*>& argExprs)
    : m_objectType(objectType)
    , m_objectName(objectName)
    , m_argExprs(argExprs)
  {
  }

  ExprNewObject::~ExprNewObject()
  {
      for (unsigned int i=0;i<m_argExprs.size();i++)
          delete m_argExprs[i];
      m_argExprs.clear();
  }

  DataRef ExprNewObject::eval(EvalContext& context) const
  {
    std::vector<const Domain*> arguments;
    for (unsigned int i=0; i < m_argExprs.size(); i++) {
      DataRef arg = m_argExprs[i]->eval(context);
      arguments.push_back(&(arg.getValue()->derivedDomain()));
    }

    // TODO: using ObjectEvalContext may be cleaner?
    ConstrainedVariableId thisVar = context.getVar("this");
    ObjectId thisObject = 
      (thisVar.isId() ? Entity::getTypedEntity<Object>(thisVar->derivedDomain().getSingletonValue()) : ObjectId::noId());
    std::string prefix = (thisObject.isId() ? thisObject->getName().toString() + "." : "");

    LabelStr name(prefix+m_objectName.toString());
    DbClientId pdb = getPDB(context);
    ObjectId newObject = pdb->createObject(
						  m_objectType.c_str(),
						  name.c_str(),
						  arguments
						  );

    if (thisObject.isId())
        newObject->setParent(thisObject);

    return DataRef(newObject->getThis());
  }

  std::string ExprNewObject::toString() const
  {
      std::ostringstream os;

      os << "{NEW_OBJECT " << m_objectName.toString() << "(" << m_objectType.toString() << ") ";

      for (unsigned int i =0; i < m_argExprs.size();i++) {
          if (i>0)
              os << " , ";
          os << m_argExprs[i]->toString();
      }
      os << " }";

      return os.str();
  }

  unsigned int CExpr::s_counter(1);

  std::string CExpr::createVariableName() const
  {
      char buff[15];
      SPRINTF_FUNC(buff, "%u", m_count);
      std::string variable = std::string("implicit_var_" + std::string(buff) + "_" + toString()).c_str();

      //Detect if the variable contains illegal characters and rewrite it if it does.
      for (unsigned int i = 0; i < variable.size(); i++) {
          if ((variable[i] > '9' || variable[i] < '0') && (variable[i] < 'A' || variable[i] > 'Z')
                  && (variable[i] < 'a' || variable[i] > 'z') && (variable[i] != '_')) {
              debugMsg("Interpreter","Bad var name generated : " << variable);
              debugMsg("NddlInterpreter","Bad var name generated : " << variable);
              variable = "implicit_var_" + std::string(buff) + "_generation_failure";
              break;
          }
      }
      return variable;
  }

  CExprValue::CExprValue(Expr* value)
      : m_value(value)
  {
  }

  void CExprValue::checkType()
  {
      // Nothing to check
  }

  DataRef CExprValue::eval(EvalContext& context) const
  {
      return m_value->eval(context);
  }

  CExprFunction::CExprFunction(const CFunctionId& func, const std::vector<CExpr*>& args)
    : m_func(func)
    , m_args(args)
  {
  }

  void CExprFunction::checkType()
  {
      // TODO: Have function type check arg types?, it's already done by the parser
  }

  DataRef CExprFunction::eval(EvalContext& context) const
  {
      //Create the variable name
      std::string variable = createVariableName();

      // TODO: CFunction should know how to eval itself
      ExprVarDeclaration* var = new ExprVarDeclaration(variable.c_str(), getDataType(), NULL, false);
      DataRef output = var->eval(context);

      Expr* varRef = new ExprVarRef(variable.c_str(), getDataType());
      std::vector<Expr*> args;
      args.push_back(varRef);
      for (unsigned int i = 0; i < m_args.size(); i++)
          args.push_back(m_args[i]);

      Expr* con = new ExprConstraint(m_func->getConstraint(), args, m_violationMsg.c_str());

      con->eval(context);
      return output;
  }

  std::string CExprFunction::toString() const
  {
      std::string args = "";
      for (unsigned int i = 0; i < m_args.size(); i++)
          args += m_args[i]->toString() + std::string("_");

      return m_func->getName().toString() + "__" + args + "_";
  }

  CExprBinary::CExprBinary(std::string op, CExpr* lhs, CExpr* rhs)
    : m_operator(op), m_lhs(lhs),  m_rhs(rhs)
  {
  }

  CExprBinary::~CExprBinary()
  {
	  delete m_lhs;
	  delete m_rhs;
  }

  bool CExprBinary::hasReturnValue() const {
    if ((m_operator == "==" || m_operator == "<=" || m_operator == ">=" || m_operator == "!=" || m_operator == ">" || m_operator == "<") && m_enforceContext) {
      return false;
    }
    return true;
  }

  const DataTypeId CExprBinary::getDataType() const {
    if (!hasReturnValue()) {
      return VoidDT::instance(); //In an optimizable enforce statement, there is a void return
    } else if (m_operator == "==" || m_operator == "<=" || m_operator == ">=" || m_operator == "!=" || m_operator == ">" || m_operator == "<" || m_operator == "||" || m_operator == "&&") {
      return BoolDT::instance(); //Boolean return from relationals and boolean ops.
    } else if (m_operator == "+" || m_operator == "*" || m_operator == "-") {
      check_error(m_lhs && m_rhs, "No arguments to arithmetic expression.");
      return m_lhs->getDataType();
    }
    return VoidDT::instance();
  }

  void CExprBinary::checkType()
  {
      m_lhs->checkType();
      m_rhs->checkType();

      if (m_operator == "||" || m_operator == "&&") {
          if (!m_lhs->getDataType()->isAssignableFrom(BoolDT::instance()) || !BoolDT::instance()->isAssignableFrom(m_lhs->getDataType())) {
              throw std::string("In a " + m_operator + " expression, both arguments must be of type boolean. In this case, "
                      + m_lhs->getDataType()->getName().c_str() + " from \"" + m_lhs->toString() + "\" is not a boolean.");
          }
          if (!m_rhs->getDataType()->isAssignableFrom(BoolDT::instance()) || !BoolDT::instance()->isAssignableFrom(m_rhs->getDataType())) {
              throw std::string("In a " + m_operator + " expression, both arguments must be of type boolean. In this case, "
                      + m_rhs->getDataType()->getName().c_str() + " from \"" + m_rhs->toString() + "\" is not a boolean.");
          }
      } else if (m_operator == "+" || m_operator == "-" || m_operator == "*" || m_operator == "=="
          || m_operator == "<=" || m_operator == ">=" || m_operator == "!=" || m_operator == ">" || m_operator == "<") {
          if (!m_rhs->getDataType()->isAssignableFrom(m_lhs->getDataType()) || !m_lhs->getDataType()->isAssignableFrom(m_rhs->getDataType())) {
              throw std::string("Cannot use types " + std::string(m_lhs->getDataType()->getName().c_str()) + " and "
                      + std::string(m_rhs->getDataType()->getName().c_str()) + "in expression: " + toString());
          }
      } else {
          check_runtime_error(ALWAYS_FAIL, "Warning no type check for expression: " + toString() + "\n");
      }
  }

  bool isTimepoint(DataRef var) {
    ConstrainedVariable *cvar = var.getValue();
    return dynamic_cast< TokenVariable<IntervalIntDomain>* >(cvar) != NULL
      && (cvar->getName().toString() == "end" || cvar->getName().toString() == "start");
  }

  bool CExprBinary::isSingleton() {
    return false;
  }

  bool CExprBinary::isSingletonOptimizable() {
    return (m_operator == "+" || m_operator == "-" || m_operator == "*");
  }

  DataRef CExprBinary::eval(EvalContext& context) const {
      //Figure out constraint type.
      std::string constraint = "", returnType = "";
      bool flipArguments = false;
      if (hasReturnValue()) {
          if (m_operator == "==") { constraint = "testEQ"; returnType = "bool"; }
          if (m_operator == "<=") { constraint = "testLEQ"; returnType = "bool"; }
          if (m_operator == ">=") { constraint = "testLEQ"; returnType = "bool"; flipArguments = true; }
          if (m_operator == "!=") { constraint = "testNEQ"; returnType = "bool"; }
          if (m_operator == ">") { constraint = "TestLessThan"; returnType = "bool"; flipArguments = true; }
          if (m_operator == "<") { constraint = "TestLessThan"; returnType = "bool"; }
          if (m_operator == "+") { constraint = "addEq"; }
          if (m_operator == "-") { constraint = "addEq"; }
          if (m_operator == "*") { constraint = "mulEq"; }
          if (m_operator == "||") { constraint = "testOr"; returnType = "bool"; }
          if (m_operator == "&&") { constraint = "testAnd"; returnType = "bool"; }
      } else { //Special for return type non-existant
          returnType = "VOID";
          if (m_operator == "==") { constraint = "eq"; }
          if (m_operator == "<=") { constraint = "leq"; }
          if (m_operator == ">=") { constraint = "leq"; flipArguments = true; }
          if (m_operator == "!=") { constraint = "neq"; }
          if (m_operator == ">") { constraint = "lt"; flipArguments = true; }
          if (m_operator == "<") { constraint = "lt"; }
      }
      check_runtime_error(constraint != "", "Illegal expression: " + m_operator);

      //If one side is a singleton and the other can be optimized, do a special case.
      if (returnType == "VOID" && m_operator == "==") {
          if (m_lhs->isSingleton() && m_rhs->isSingletonOptimizable()) {
              m_rhs->setReturnArgument(m_lhs);
              return m_rhs->eval(context);
          }
          if (m_rhs->isSingleton() && m_lhs->isSingletonOptimizable()) {
              m_lhs->setReturnArgument(m_rhs);
              return m_lhs->eval(context);
          }
      }

      DataRef left = m_lhs->eval(context), right = m_rhs->eval(context);

      std::vector<ConstrainedVariableId> args;
      DataRef output;

      if (hasReturnValue()) {
          ///First create the implicit return variable
          //Get the data type.
          DataTypeId data;
          if (returnType == "") {
              returnType = left.getValue()->getDataType()->getName().c_str();
              data = left.getValue()->getDataType();
              check_runtime_error(returnType == right.getValue()->getDataType()->getName().c_str(), "We don't support expressions with different types going in to one var (e.g. float + int)");
          } else if (returnType == "bool") {
              data = BoolDT::instance();
          } else {
              check_runtime_error(ALWAYS_FAIL, "Illegal data type: " + returnType);
          }

          if (m_returnArgument) {
              output = m_returnArgument->eval(context);
              //Make addEq a temporal constraint.
              if (isTimepoint(left) || isTimepoint(right) || isTimepoint(output)) {
                  if (constraint == "addEq") {
                      check_runtime_error(!isTimepoint(left) || !isTimepoint(right) || !isTimepoint(output), "You can't add two timepoints up to make a third time point.");

                      constraint = "temporalDistance";
                  }
              }
          } else {
              //Create the variable name
              std::string variable = this->createVariableName();

              //Declare the implicit return variable
              ExprVarDeclaration* var = new ExprVarDeclaration(variable.c_str(), data, NULL, false);
              output = var->eval(context);

              delete var;
          }

          //Make the constraint's arguments when return type present
          if (m_operator == "-") {
              args.push_back(output.getValue());
              args.push_back(right.getValue());
              args.push_back(left.getValue());
          } else if (m_operator == "+") {
              args.push_back(left.getValue());
              args.push_back(right.getValue());
              args.push_back(output.getValue());
          } else if (m_operator == "*") {
              args.push_back(left.getValue());
              args.push_back(right.getValue());
              args.push_back(output.getValue());
          } else { //relationals and booleans
              args.push_back(output.getValue());
              if (flipArguments) { //>= and >
                  args.push_back(right.getValue());
                  args.push_back(left.getValue());
              } else if (!flipArguments) { //all others
                  args.push_back(left.getValue());
                  args.push_back(right.getValue());
              }
          }
      } else { //No return value
          //Detect temporal variables
          if (isTimepoint(left) || isTimepoint(right)) {
              if (constraint == "eq") {
                  constraint = "concurrent";
              } else if (constraint == "leq") {
                  constraint = "precedes";
              }
          }
          if (flipArguments) {
              args.push_back(right.getValue());
              args.push_back(left.getValue());
          } else {
              args.push_back(left.getValue());
              args.push_back(right.getValue());
          }
      }

      makeConstraint(context, constraint.c_str(), args, m_violationMsg.c_str());

      return output;
  }

  std::string CExprBinary::toString() const {
    if (m_rhs && m_lhs) {
      std::string op = "BadOp";
      if (m_operator == "==") { op = "eq"; }
      if (m_operator == "<=") { op = "leq"; }
      if (m_operator == ">=") { op = "geq"; }
      if (m_operator == "!=") { op = "neq"; }
      if (m_operator == ">") { op = "gt"; }
      if (m_operator == "<") { op = "lt"; }
      if (m_operator == "+") { op = "plus"; }
      if (m_operator == "-") { op = "minus"; }
      if (m_operator == "*") { op = "times"; }
      if (m_operator == "||") { op = "or"; }
      if (m_operator == "&&") { op = "and"; }
      if (m_enforceContext) { op += "_enf"; }
      return m_lhs->toString() + "_" + op + "_" + m_rhs->toString();
    }

    return "BadExpression";
  }

  ExprConstraint::ExprConstraint(const char* name,const std::vector<Expr*>& args, const char* violationExpl)
    : m_name(name)
    , m_args(args)
    , m_violationExpl("")
  {
      if (violationExpl != NULL)
          m_violationExpl = violationExpl;
  }

  ExprConstraint::~ExprConstraint()
  {
      for (unsigned int i=0; i < m_args.size(); i++)
          delete m_args[i];
      m_args.clear();
  }

  std::string varsToString(const std::vector<ConstrainedVariableId>& vars)
  {
    std::ostringstream os;
    for (unsigned int i=0; i < vars.size(); i++) {
      if (i>0) os << ",";
      os << vars[i]->toString();
    }

    return os.str();
  }

  DataRef ExprConstraint::eval(EvalContext& context) const
  {
    std::vector<ConstrainedVariableId> vars;
    for (unsigned int i=0; i < m_args.size(); i++) {
      DataRef arg = m_args[i]->eval(context);
      vars.push_back(arg.getValue());
    }

    makeConstraint(context,m_name,vars,m_violationExpl.c_str());

    return DataRef::null;
  }

  std::string ExprConstraint::toString() const
  {
      std::stringstream os;

      os << "{CONSTRAINT:" << m_name.c_str() << " (";

      for (unsigned int i=0;i<m_args.size();i++)
             os << m_args[i]->toString() << " ";
      os << ") " << m_violationExpl << "}";

      return os.str();
  }

  bool isClass(EvalContext& ctx,const LabelStr& className)
  {
      return getSchema(ctx)->isObjectType(className);
  }

  // see ModelAccessor.isConstrained in Nddl compiler
  bool isConstrained(EvalContext& context, const LabelStr& predicateInstance)
  {
    unsigned int tokenCnt = predicateInstance.countElements(".");

    // If the predicate is not qualified that means it belongs to the object in scope
    if (tokenCnt == 1)
      return true;

    // If the prefix is a class, it means it can be any object instance, so it must not be constrained
    LabelStr prefix(predicateInstance.getElement(0,"."));
    if (!isClass(context,prefix))
      return true;

    return false;
  }

  LabelStr checkPredicateType(EvalContext& ctx,const LabelStr& type)
  {
    check_runtime_error(getSchema(ctx)->isPredicate(type),type.toString()+" is not a Type");
    return type;
  }

  LabelStr getObjectVarClass(EvalContext& ctx,const LabelStr& className,const LabelStr& var)
  {
    const SchemaId& schema = getSchema(ctx);
    check_runtime_error(schema->hasMember(className,var),className.toString()+" has no member called "+var.toString());
    return schema->getMemberType(className,var);
  }

  LabelStr getTokenVarClass(EvalContext& ctx,const LabelStr& className,const LabelStr& predName,const LabelStr& var)
  {
      if (strcmp(var.c_str(),"object") == 0) // is it the object variable?
          return className;
      else { // look through the parameters to the token
          const SchemaId& schema = getSchema(ctx);
          if (schema->hasMember(predName,var))
              return schema->getMemberType(predName,var);
      }

      // if everything else fails, see if it's an object member
      return getObjectVarClass(ctx,className,var);
  }

  /*
   * figures out the type of a predicate given an instance
   *
   */
  LabelStr predicateInstanceToType(
               EvalContext& ctx,
               const char* predicateName,
               const char* predicateInstance)
  {
      // see ModelAccessor.getSlaveObjectType() in NDDL compiler
      ConstrainedVariableId obj = ctx.getVar("object");
      check_error(obj.isId(),"Failed to find 'object' var in predicateInstanceToType()");
      const char* className = obj->baseDomain().getTypeName().c_str();
      LabelStr str(predicateInstance);

      unsigned int tokenCnt = str.countElements(".");

      if (tokenCnt == 1) {
          std::string retval = std::string(className)+"."+predicateInstance;
          return checkPredicateType(ctx,LabelStr(retval));
      }
      else if (tokenCnt == 2) {
          LabelStr prefix(str.getElement(0,"."));
          LabelStr suffix(str.getElement(1,"."));

          if (prefix.toString() == "object") {
              std::string retval = std::string(className)+"."+suffix.toString();
              return checkPredicateType(ctx,LabelStr(retval.c_str()));
          }
          else if (isClass(ctx,prefix)) {
              return checkPredicateType(ctx,LabelStr(predicateInstance));
          }
          else {
              ConstrainedVariableId var = ctx.getVar(prefix.c_str());
              if (var.isId()) {
                  std::string clazz = var->baseDomain().getTypeName().c_str();
                  return checkPredicateType(ctx,clazz+"."+suffix.toString());
              }
              else {
                  LabelStr clazz = getTokenVarClass(ctx,className,predicateName,prefix);
                  std::string retval = clazz.toString()+"."+suffix.toString();
                  return checkPredicateType(ctx,LabelStr(retval.c_str()));
              }
          }
      }
      else {
          LabelStr var = str.getElement(0,".");
          LabelStr clazz;
          ConstrainedVariableId v = ctx.getVar(var.c_str());
          if (v.isId())
              clazz = v->baseDomain().getTypeName().c_str();
          else
              clazz = getTokenVarClass(ctx,className,predicateName,var);

          for (unsigned int i=1;i<tokenCnt-1;i++) {
              LabelStr var = str.getElement(i,".");
              clazz = getObjectVarClass(ctx,clazz,var);
          }

          LabelStr predicate = str.getElement(tokenCnt-1,".");
          std::string retval = clazz.toString() + "." + predicate.toString();
          return checkPredicateType(ctx,LabelStr(retval));
      }
  }

  PredicateInstanceRef::PredicateInstanceRef(const char* predInstance, const char* predName)
      : m_predicateInstance(predInstance != NULL ? predInstance : "")
      , m_predicateName(predName != NULL ? predName : "")
  {
  }

  PredicateInstanceRef::~PredicateInstanceRef()
  {
  }

  // TODO: passing relation doesn't seem like a good idea, since a token may have more than one relation to its
  // master. However, MatchingEngine matches on that, so keeping it for now.
  TokenId PredicateInstanceRef::getToken(EvalContext& context, const char* relationName, bool isFact, bool isRejectable)
  {
      TokenId result;
      if (m_predicateInstance.length() == 0)
      {
          result = context.getToken(m_predicateName.c_str());
      }
      else
      {
	  InterpretedRuleInstance* rule = (InterpretedRuleInstance*)(context.getElement("RuleInstance"));
	  if (rule != NULL)
	     result = createSubgoal(context,rule,relationName);
	  else
	     result = createGlobalToken(context, isFact, isRejectable);
      }

      //TODO: In the future, this might be a hook into the NDDL error reporting system.
      checkError(result != TokenId() && result.isId(), "Error, no token " << m_predicateInstance << " "
		 << m_predicateName << " in relation: " << relationName << ".");
      return result;
  }

  TokenId PredicateInstanceRef::createGlobalToken(EvalContext& context, bool isFact, bool isRejectable)
  {
      debugMsg("Interpreter:createToken", "creating Token:" << m_predicateInstance << " " << m_predicateName);

      // The type may be qualified with an object name, in which case we should get the
      // object and specify it. We will also have to generate the appropriate type designation
      // by extracting the class from the object
      ObjectId object;
      const char* tokenType = DbClientTransactionPlayer::getObjectAndType(getSchema(context),getPDB(context),m_predicateInstance.c_str(),object);
      const char* tokenName = (m_predicateName.length()>0 ? m_predicateName.c_str() : NULL);

      TokenId token = getPDB(context)->createToken(tokenType,tokenName,isRejectable,isFact);

      if (object.isId()) {
          // We restrict the base domain permanently since the name is specifically mentioned on creation
          token->getObject()->restrictBaseDomain(object->getThis()->baseDomain());
      }

      debugMsg("Interpreter:createToken", "created Token:" << tokenName
                  << " of type " << tokenType
                  << " isFact:" << isFact
                  << " isRejectable:" << isRejectable
      );

      return token;
  }

  TokenId PredicateInstanceRef::createSubgoal(EvalContext& context, InterpretedRuleInstance* rule, const char* relationName)
  {
      // TODO: cache this? new parser is able to pass this in, do it when nddl-xml is gone.
      LabelStr predicateType = predicateInstanceToType(context,m_predicateName.c_str(),m_predicateInstance.c_str());
      debugMsg("Interpreter:InterpretedRule","Creating subgoal " << predicateType.c_str() << ":" << m_predicateName);

      LabelStr predicateName(m_predicateName); // TODO: auto-generate name if not provided?
      LabelStr predicateInstance(m_predicateInstance);
      bool constrained = isConstrained(context,predicateInstance);
      ConstrainedVariableId owner;
      if (constrained) {
        unsigned int tokenCnt = predicateInstance.countElements(".");
        if (tokenCnt == 1)
            owner = context.getVar("object");
        else
            owner = context.getVar(predicateInstance.getElement(0,".").c_str());
      }

      TokenId slave = rule->createSubgoal(
                                   predicateName,
                                   predicateType,
                                   predicateInstance,
                                   relationName,
                                   constrained,
                                   owner
                                   );

      context.addToken(predicateName.c_str(),slave);
      debugMsg("Interpreter:InterpretedRule","Created  subgoal " << predicateType.toString() << ":" << m_predicateName);

      return slave;
  }

  ExprRelation::ExprRelation(const char* relation,
                             PredicateInstanceRef* origin,
                             const std::vector<PredicateInstanceRef*>& targets)
    : m_relation(relation)
    , m_origin(origin)
    , m_targets(targets)
  {
      if (m_origin == NULL) {
          m_origin = new PredicateInstanceRef(NULL,"this");
      }
  }

  ExprRelation::~ExprRelation()
  {
      delete m_origin;
      for (unsigned int i =0;i<m_targets.size();i++)
          delete m_targets[i];
      m_targets.clear();
  }

#define makeRelation(relationname, origin, originvar, target, targetvar) {  \
    std::vector<ConstrainedVariableId> vars;\
    vars.push_back(origin->originvar());    \
    vars.push_back(target->targetvar());    \
    makeConstraint(context,LabelStr(#relationname), vars, NULL); \
  }


#define makeStrictPrecedenceRelation(origin, originvar, target, targetvar) { \
    PlanDatabase* db = (PlanDatabase*)(context.getElement("PlanDatabase"));\
    std::vector<ConstrainedVariableId> vars;				\
    ConstrainedVariableId var = (new Variable<IntervalIntDomain>(db->getConstraintEngine(), IntervalIntDomain(1, PLUS_INFINITY)))->getId(); \
    vars.push_back(origin->originvar());				\
    vars.push_back(var);						\
    vars.push_back(target->targetvar());				\
    makeConstraint(context, "temporalDistance", vars, NULL);			\
  }

  void createRelation(EvalContext& context,
                      const char* relationName,
                      TokenId origin,
                      TokenId target)
  {
      // Allen Relations according to EUROPA
      // See ConstraintLibraryReference on the wiki
      if (strcmp(relationName,"meets") == 0) {
        makeRelation(concurrent, origin, end, target, start);
      }
      else if (strcmp(relationName,"met_by") == 0) {
        makeRelation(concurrent, target, end, origin, start);
      }
      else if (strcmp(relationName,"contains") == 0) {
        makeRelation(precedes, origin, start, target, start);
        makeRelation(precedes, target, end, origin, end);
        makeRelation(leq, target, duration, origin, duration);
      }
      else if (strcmp(relationName,"contained_by") == 0) {
        makeRelation(precedes, target, start, origin, start);
        makeRelation(precedes, origin, end, target, end);
        makeRelation(leq, origin, duration, target, duration);
      }
      else if (strcmp(relationName,"before") == 0) {
        makeRelation(precedes, origin, end, target, start);
      }
      else if (strcmp(relationName,"after") == 0) {
        makeRelation(precedes, target, end, origin, start);
      }
      else if (strcmp(relationName,"starts") == 0) {
        makeRelation(concurrent, origin, start, target, start);
      }
      else if (strcmp(relationName,"ends") == 0) {
        makeRelation(concurrent, target, end, origin, end);
      }
      else if (strcmp(relationName, "parallels") == 0) {
        makeRelation(precedes, origin, start, target, start);
        makeRelation(precedes, origin, end, target, end);
      }
      else if (strcmp(relationName, "paralleled_by") == 0) {
        makeRelation(precedes, target, start, origin, start);
        makeRelation(precedes, target, end, origin, end);
      }
      else if (strcmp(relationName,"ends_after") == 0) {
        makeStrictPrecedenceRelation(target, end, origin, end);
      }
      else if (strcmp(relationName,"ends_before") == 0) {
        makeStrictPrecedenceRelation(origin, end, target, end);
      }
      else if (strcmp(relationName,"ends_after_start") == 0) {
        makeStrictPrecedenceRelation(target, start, origin, end);
      }
      else if (strcmp(relationName,"starts_before_end") == 0) {
        makeStrictPrecedenceRelation(origin, start, target, end);
      }
      else if (strcmp(relationName,"starts_during") == 0) {
        makeRelation(precedes, target, start, origin, start);
        makeStrictPrecedenceRelation(origin, start, target, end);
      }
      else if (strcmp(relationName,"ends_during") == 0) {
        makeStrictPrecedenceRelation(target, start, origin, end);
        makeRelation(precedes, origin, end, target, end);
      }
      else if (strcmp(relationName,"contains_start") == 0) {
        makeRelation(precedes, origin, start, target, start);
        makeStrictPrecedenceRelation(target, start, origin, end);
      }
      else if (strcmp(relationName,"contains_end") == 0) {
        makeStrictPrecedenceRelation(origin, start, target, end);
        makeRelation(precedes, origin, end, target, end);
      }
      else if (strcmp(relationName,"starts_after") == 0) {
        makeStrictPrecedenceRelation(target, start, origin, start);
      }
      else if (strcmp(relationName,"starts_before") == 0) {
        makeStrictPrecedenceRelation(origin, start, target, start);
      }
      else if (strcmp(relationName,"equals") == 0) {
        makeRelation(concurrent, origin, start, target, start);
        makeRelation(concurrent, target, end, origin, end);
        makeRelation(eq, origin, duration, target, duration);
      }
      else {
        check_runtime_error(strcmp(relationName,"any") == 0,std::string("Unrecognized relation:")+relationName);
      }
  }

  DataRef ExprRelation::eval(EvalContext& context) const
  {
    const char* relationName = m_relation.c_str();

    TokenId origin = m_origin->getToken(context,"any"); // This will create a subgoal if necessary
    for (unsigned int i=0;i<m_targets.size();i++) {
        TokenId target = m_targets[i]->getToken(context,relationName); // This will create a subgoal if necessary
        createRelation(context,relationName,origin,target);
    }

    debugMsg("Interpreter:InterpretedRule","Created relation " << relationName);
    return DataRef::null;
  }

  ExprIfGuard::ExprIfGuard(const char* op, Expr* lhs,Expr* rhs)
    : m_op(op)
    , m_lhs(lhs)
    , m_rhs(rhs)
  {
  }

  ExprIfGuard::~ExprIfGuard()
  {
      delete m_lhs;
      delete m_rhs;
  }

  const std::string& ExprIfGuard::getOperator() { return m_op; }
  Expr* ExprIfGuard::getLhs() { return m_lhs; }
  Expr* ExprIfGuard::getRhs() { return m_rhs; }

  DataRef ExprIfGuard::eval(EvalContext& context) const
  {
      // TODO: rework ExprIf implementation so that this can be evaluated
      check_runtime_error(ALWAYS_FAILS,"ExprIfGuard can't be evaluated");
      return DataRef::null;
  }

  std::string ExprIfGuard::toString() const
  {
      std::stringstream os;

      os << "{IfGuard:" << m_op << " LHS=" << m_lhs->toString() << " RHS=" << (m_rhs != NULL ? m_rhs->toString() : "NULL") << "}";

      return os.str();
  }



  ExprIf::ExprIf(ExprIfGuard* guard,const std::vector<Expr*>& ifBody, const std::vector<Expr*>& elseBody)
    : m_guard(guard)
    , m_ifBody(ifBody)
    , m_elseBody(elseBody)
  {
  }

  ExprIf::~ExprIf()
  {
      delete m_guard;

      for (unsigned int i=0;i<m_ifBody.size();i++)
          delete m_ifBody[i];
      m_ifBody.clear();

      for (unsigned int i=0;i<m_elseBody.size();i++)
          delete m_elseBody[i];
      m_elseBody.clear();
  }

  DataRef ExprIf::doEval(RuleInstanceEvalContext& context) const
  {
    bool isOpEquals = (m_guard->getOperator() == "equals" || m_guard->getOperator()=="==");

    DataRef lhs = m_guard->getLhs()->eval(context);

    // TODO: this assumes that the variable is always on the lhs and the value on the rhs
    // is this enforced by the parser? underlying Rule implementation should be made more generic

    if (m_guard->getRhs() != NULL) {
      DataRef rhs = m_guard->getRhs()->eval(context);
      context.getRuleInstance()->addChildRule(
					      new InterpretedRuleInstance(
									  context.getRuleInstance()->getId(),
									  lhs.getValue(),
									  rhs.getValue()->lastDomain(),
									  isOpEquals,
									  m_ifBody
									  )
					      );

      if (m_elseBody.size() > 0) {
          context.getRuleInstance()->addChildRule(
                              new InterpretedRuleInstance(
                                          context.getRuleInstance()->getId(),
                                          lhs.getValue(),
                                          rhs.getValue()->lastDomain(),
                                          !isOpEquals,
                                          m_elseBody
                                          )
                              );
      }

      debugMsg("Interpreter:InterpretedRule","Evaluated IF " << m_guard->toString());
    }
    else {
      context.getRuleInstance()->addChildRule(
					      new InterpretedRuleInstance(
									  context.getRuleInstance()->getId(),
									  makeScope(lhs.getValue()),
									  isOpEquals,
									  m_ifBody
									  )
					      );

      check_runtime_error(m_elseBody.size()==0, "Can't have else body for singleton guard");
      debugMsg("Interpreter:InterpretedRule","Evaluated IF " << m_guard->toString());
    }

    return DataRef::null;
  }

  std::string ExprIf::toString() const
  {
      std::stringstream os;

      os << "{If " << m_guard->toString() << " body(" << m_ifBody.size() << ") }";

      return os.str();
  }

  ExprLoop::ExprLoop(const char* varName, const char* varValue,const std::vector<Expr*>& loopBody)
    : m_varName(varName)
    , m_varValue(varValue)
    , m_loopBody(loopBody)
  {
  }

  ExprLoop::~ExprLoop()
  {
      for (unsigned int i=0;i<m_loopBody.size();i++)
          delete m_loopBody[i];
      m_loopBody.clear();
  }

  DataRef ExprLoop::doEval(RuleInstanceEvalContext& context) const
  {
    context.getRuleInstance()->executeLoop(context,m_varName,m_varValue,m_loopBody);
    debugMsg("Interpreter:InterpretedRule","Evaluated LOOP " << m_varName.toString() << "," << m_varValue.toString());
    return DataRef::null;
  }

    /*
     * InterpretedToken
     */
    InterpretedToken::InterpretedToken(
                     const PlanDatabaseId& planDatabase,
                     const LabelStr& predicateName,
                     const std::vector<Expr*>& body,
                     const bool& rejectable,
                     const bool& isFact,
                     const bool& close)
      : IntervalToken(planDatabase,
                        predicateName,
                        rejectable,
                        isFact,
                        IntervalIntDomain(),                  // start
                        IntervalIntDomain(),                  // end
                        IntervalIntDomain(1, PLUS_INFINITY),  // duration
                        Token::noObject(),                    // Object Name
                        false)
    {
    	commonInit(body, close);
    	debugMsg("Interpreter:InterpretedToken","Created token(" << getKey() << ") of type:" << predicateName.toString() << " objectVar=" << getVariable("object")->toString());
    }

  InterpretedToken::InterpretedToken(
                     const TokenId& master,
				     const LabelStr& predicateName,
				     const LabelStr& relation,
                     const std::vector<Expr*>& body,
				     const bool& close)
    : IntervalToken(master,
		    relation,
		    predicateName,
		    IntervalIntDomain(),                 // start
		    IntervalIntDomain(),                 // end
		    IntervalIntDomain(1, PLUS_INFINITY), // duration
		    Token::noObject(),                   // Object Name
		    false)
  {
    commonInit(body, close);
    debugMsg("Interpreter:InterpretedToken","Created slave token(" << getKey() << ") of type:" << predicateName.toString() << " objectVar=" << getVariable("object")->toString());
  }

  InterpretedToken::~InterpretedToken()
  {
  }

  void InterpretedToken::commonInit(
                    const std::vector<Expr*>& body,
				    const bool& autoClose)
  {
    // TODO: Pass in EvalContext to give access to class or global context
    TokenEvalContext context(NULL,getId());

    for (unsigned int i=0; i < body.size(); i++)
        body[i]->eval(context);

    if (autoClose)
      close();
  }

  /*
   * InterpretedTokenType
   */
    InterpretedTokenType::InterpretedTokenType(
            const ObjectTypeId& ot,
            const LabelStr& predicateName)
        : TokenType(ot,predicateName)
    {
    }

    void InterpretedTokenType::addBodyExpr(Expr* e)
    {
        m_body.push_back(e);
    }

    InterpretedTokenType::~InterpretedTokenType()
    {
        for (unsigned int i=0;i<m_body.size();i++)
            delete m_body[i];

        m_body.clear();
    }

    TokenTypeId InterpretedTokenType::getParentType(const PlanDatabaseId& planDb) const
    {
        // TODO: cache this?
        // TODO: drop planDb parameter, ObjectType must be able to answer this without reference to schema
        TokenTypeId parentType = planDb->getSchema()->getParentTokenType(getSignature(), m_objType->getParent()->getName());
        return parentType;
    }

	TokenId InterpretedTokenType::createInstance(const PlanDatabaseId& planDb, const LabelStr& name, bool rejectable, bool isFact) const
	{
	    TokenTypeId parentType = getParentType(planDb);

        TokenId token;
	    if (parentType.isNoId()) {
	        token = (new InterpretedToken(
	                planDb,
	                name,
	                m_body,
	                rejectable,
	                isFact,
	                false))->getId();
	    }
	    else {
	        token = parentType->createInstance(planDb,name,rejectable,isFact);
	        // TODO: Hack! this makes it impossible to extend native tokens
	        // class hierarchy needs to be fixed to avoid this cast
            InterpretedToken* it = dynamic_cast<InterpretedToken*>((Token*)token);
            it->commonInit(m_body,false);
	    }

	    return token;
	}

  TokenId InterpretedTokenType::createInstance(const TokenId& master, const LabelStr& name, const LabelStr& relation) const
  {
      TokenTypeId parentType = getParentType(master->getPlanDatabase());

      TokenId token;
      if (parentType.isNoId()) {
          token = (new InterpretedToken(
                  master,
                  name,
                  relation,
                  m_body,
                  false))->getId();
      }
      else {
          token = parentType->createInstance(master,name,relation);
          // TODO: Hack! this makes it impossible to extend native tokens
          // class hierarchy needs to be fixed to avoid this cast
          InterpretedToken* it = dynamic_cast<InterpretedToken*>((Token*)token);
          it->commonInit(m_body,false);
      }

      return token;
  }


  /*
   * RuleInstanceEvalContext
   * Puts RuleInstance variables like duration, start, end, in context
   */
  RuleInstanceEvalContext::RuleInstanceEvalContext(EvalContext* parent, const InterpretedRuleInstanceId& ruleInstance)
    : EvalContext(parent)
    , m_ruleInstance(ruleInstance)
  {
  }

  RuleInstanceEvalContext::~RuleInstanceEvalContext()
  {
  }

  void* RuleInstanceEvalContext::getElement(const char* name) const
  {
      std::string str(name);
      if (str == "RuleInstance")
          return (InterpretedRuleInstance*)m_ruleInstance;
      if (str == "PlanDatabase")
          return (PlanDatabase*)m_ruleInstance->getPlanDatabase();

      return EvalContext::getElement(name);
  }

  ConstrainedVariableId RuleInstanceEvalContext::getVar(const char* name)
  {
    ConstrainedVariableId var = m_ruleInstance->getVariable(LabelStr(name));

    if (!var.isNoId()) {
      debugMsg("Interpreter:EvalContext:RuleInstance","Found var in rule instance:" << name);
      return var;
    }
    else {
      debugMsg("Interpreter:EvalContext:RuleInstance","Didn't find var in rule instance:" << name);
      return EvalContext::getVar(name);
    }
  }

  TokenId RuleInstanceEvalContext::getToken(const char* name)
  {
	  LabelStr ls_name(name);
      TokenId tok = m_ruleInstance->getSlave(ls_name);
      if (!tok.isNoId()) {
          debugMsg("Interpreter:EvalContext:RuleInstance","Found token in rule instance:" << name);
          return tok;
      }
      else {
        debugMsg("Interpreter:EvalContext:RuleInstance","Didn't find token in rule instance:" << name);
        return EvalContext::getToken(name);
      }
  }

  bool RuleInstanceEvalContext::isClass(const LabelStr& className) const
  {
     return m_ruleInstance->getPlanDatabase()->getSchema()->isObjectType(className);
  }

  std::string RuleInstanceEvalContext::toString() const
  {
    std::ostringstream os;

    os << EvalContext::toString();

    os << "Token variables {";
    const std::vector<ConstrainedVariableId>& vars = m_ruleInstance->getToken()->getVariables();
    for(std::vector<ConstrainedVariableId>::const_iterator it = vars.begin(); it != vars.end(); ++it){
      ConstrainedVariableId var = *it;
      os << var->getName().toString() << "," ;
    }
    os << "}" << std::endl;

    return os.str();
  }

  /*
   * RuleInstanceEvalContext
   * Puts Token variables like duration, start, end, in context
   */
  TokenEvalContext::TokenEvalContext(EvalContext* parent, const TokenId& token)
    : EvalContext(parent)
    , m_token(token)
  {
  }

  TokenEvalContext::~TokenEvalContext()
  {
  }

  TokenId& TokenEvalContext::getToken() { return m_token; }

  ConstrainedVariableId TokenEvalContext::getVar(const char* name)
  {
    ConstrainedVariableId var = m_token->getVariable(LabelStr(name));

    if (!var.isNoId()) {
      debugMsg("Interpreter:EvalContext:Token","Found var in token :" << name);
      return var;
    }
    else
      return EvalContext::getVar(name);
  }

  void* TokenEvalContext::getElement(const char* name) const
  {
      std::string str(name);
      if (str == "Token")
          return (Token*)m_token;
      if (str == "PlanDatabase")
          return (PlanDatabase*)m_token->getPlanDatabase();

      return EvalContext::getElement(name);
  }


  bool TokenEvalContext::isClass(const LabelStr& className) const
  {
     return m_token->getPlanDatabase()->getSchema()->isObjectType(className);
  }

  /*
   * InterpretedRuleInstance
   */
  InterpretedRuleInstance::InterpretedRuleInstance(const RuleId& rule,
						   const TokenId& token,
						   const PlanDatabaseId& planDb,
						   const std::vector<Expr*>& body)
    : RuleInstance(rule, token, planDb)
    , m_body(body)
  {
  }

  InterpretedRuleInstance::InterpretedRuleInstance(
						   const RuleInstanceId& parent,
						   const ConstrainedVariableId& var,
						   const Domain& domain,
						   const bool positive,
						   const std::vector<Expr*>& body)
    : RuleInstance(parent,var,domain,positive)
    , m_body(body)
  {
  }

  InterpretedRuleInstance::InterpretedRuleInstance(
						   const RuleInstanceId& parent,
						   const std::vector<ConstrainedVariableId>& vars,
						   const bool positive,
						   const std::vector<Expr*>& body)
    : RuleInstance(parent,vars,positive)
    , m_body(body)
  {
  }

  InterpretedRuleInstance::~InterpretedRuleInstance()
  {
  }

  void InterpretedRuleInstance::handleExecute()
  {
    // Inheritance is handled by RulesEngine, see RuleSchema::getRules, where rules for the entire hierarchy are gathered

    // TODO: should pass in eval context from outside
    RuleInstanceEvalContext evalContext(NULL,getId());

    debugMsg("Interpreter:InterpretedRule","Executing interpreted rule:" << getRule()->getName().toString() << " token:" << m_token->toString());
    for (unsigned int i=0; i < m_body.size(); i++)
      m_body[i]->eval(evalContext);
    debugMsg("Interpreter:InterpretedRule","Executed  interpreted rule:" << getRule()->getName().toString() << " token:" << m_token->toString());
  }

  TokenId InterpretedRuleInstance::createSubgoal(
						 const LabelStr& name,
						 const LabelStr& predicateType,
						 const LabelStr& predicateInstance,
						 const LabelStr& relation,
						 bool isConstrained,
						 ConstrainedVariableId& owner)
  {
    TokenId slave;

    unsigned int tokenCnt = predicateInstance.countElements(".");
    bool isOnSameObject = (
        tokenCnt == 1 ||
        (tokenCnt==2 && (predicateInstance.getElement(0,".").toString() == "object"))
    );

    if (isOnSameObject) {
        // TODO: this is to support predicate inheritance
      	// currently doing the same as the compiler, it'll probably be surprising to the user that
      	// predicate inheritance will work only if the predicates are on the same object that the rule belongs to
      	LabelStr suffix = predicateInstance.getElement(tokenCnt-1,".");
        slave = NDDL::allocateOnSameObject(m_token,suffix,relation);
    }
    else {
        slave = m_token->getPlanDatabase()->createSlaveToken(m_token,predicateType,relation);
    }
    addSlave(slave,name);

    // For qualified names like "object.helloWorld" must add constraint to the object variable on the slave token
    // See RuleWriter.allocateSlave in Nddl compiler
    if (isConstrained) {
      std::vector<ConstrainedVariableId> vars;

      if (tokenCnt <= 2) {
          vars.push_back(owner);
      }
      else {  // equivalent of constrainObject() in NddlRules.hh
          // TODO: this can be done more efficiently
          int cnt = predicateInstance.countElements(".");
          std::string ownerName(predicateInstance.getElement(0,".").toString());
          std::string tokenName(predicateInstance.getElement(cnt-1,".").toString());
          std::string fullName = predicateInstance.toString();
          std::string objectPath = fullName.substr(
                  ownerName.size()+1,
                  fullName.size()-(ownerName.size()+tokenName.size()+2)
          );
          debugMsg("Interpreter:InterpretedRule","Subgoal slave object constraint. fullName=" << fullName << " owner=" << ownerName << " objPath=" << objectPath << " tokenName=" << tokenName);
          vars.push_back(varFromObject(owner,objectPath,fullName));
      }

      vars.push_back(slave->getObject());
      addConstraint(LabelStr("eq"),vars);
    }
    else {
      debugMsg("Interpreter:InterpretedRule",predicateInstance.toString() << " NotConstrained");
    }

    return slave;
  }

  ConstrainedVariableId InterpretedRuleInstance::addLocalVariable(
								  const Domain& baseDomain,
								  bool canBeSpecified,
								  const LabelStr& name)
  {
    return addVariable(baseDomain,canBeSpecified,name);
  }

  ConstrainedVariableId InterpretedRuleInstance::addObjectVariable(
								   const LabelStr& type,
								   const ObjectDomain& baseDomain,
								   bool canBeSpecified,
								   const LabelStr& name)
  {
    ConstrainedVariableId localVariable = addVariable(baseDomain,canBeSpecified,name);
    getPlanDatabase()->makeObjectVariableFromType(type,localVariable,canBeSpecified);

    return localVariable;
  }

  void InterpretedRuleInstance::executeLoop(EvalContext& evalContext,
					    const LabelStr& loopVarName,
					    const LabelStr& valueSet,
					    const std::vector<Expr*>& loopBody)
  {
    // Create a local domain based on the objects included in the valueSet
    ConstrainedVariableId setVar = evalContext.getVar(valueSet.c_str());
    check_error(!setVar.isNoId(),"Loop var can't be NULL");
    const Domain& setVarDomain = setVar->derivedDomain();
    debugMsg("Interpreter:InterpretedRule","set var for loop :" << setVar->toString());
    debugMsg("Interpreter:InterpretedRule","set var domain for loop:" << setVarDomain.toString());
    const ObjectDomain& loopObjectSet = dynamic_cast<const ObjectDomain&>(setVarDomain);

    if (loopObjectSet.isEmpty())
    	return; // we're done

    // The lock is an assert check really
    {
    	std::vector<ConstrainedVariableId> loop_vars;
    	loop_vars.push_back(setVar);
    	loop_vars.push_back(ruleVariable(loopObjectSet));
    	rule_constraint(filterLock, loop_vars);
    }
    std::list<edouble> loopObjectSet_values;
    loopObjectSet.getValues(loopObjectSet_values);

    // Translate into a set ordered by key to ensure reliable ordering across runs
    ObjectSet loopObjectSet_valuesByKey;
    for(std::list<edouble>::iterator it=loopObjectSet_values.begin();
        it!=loopObjectSet_values.end(); ++it) {
      ObjectId t = Entity::getTypedEntity<Object>(*it);
      loopObjectSet_valuesByKey.insert(t);
    }

    // iterate over loop collection
    for(ObjectSet::const_iterator it=loopObjectSet_valuesByKey.begin()
    		;it!=loopObjectSet_valuesByKey.end(); ++it) {
    	ObjectId loop_var = *it;
    	check_error(loop_var.isValid());

    	// Allocate a local variable for this singleton object
    	// see loopVar(Allocation, a);
    	{
    		ObjectDomain loopVarDomain(setVarDomain.getDataType());
    		loopVarDomain.insert(loop_var->getKey());
    		loopVarDomain.close();
    		// This will automatically put it in the evalContext, since all RuleInstance vars are reachable there
    		addVariable(loopVarDomain, false, loopVarName);
    	}

        // execute loop body
    	for (unsigned int i=0; i < loopBody.size(); i++)
    		loopBody[i]->eval(evalContext);

    	clearLoopVar(loopVarName);
    }
  }


  /*
   * InterpretedRuleFactory
   */
  InterpretedRuleFactory::InterpretedRuleFactory(const LabelStr& predicate,
						 const LabelStr& source,
						 const std::vector<Expr*>& body)
    : Rule(predicate,source)
    , m_body(body)
  {
  }

  InterpretedRuleFactory::~InterpretedRuleFactory()
  {
      for (unsigned int i=0;i<m_body.size();i++)
          delete m_body[i];
      m_body.clear();
  }

  RuleInstanceId InterpretedRuleFactory::createInstance(
							const TokenId& token,
							const PlanDatabaseId& planDb,
							const RulesEngineId &rulesEngine) const
  {
    InterpretedRuleInstance *foo = new InterpretedRuleInstance(m_id, token, planDb, m_body);
    foo->setRulesEngine(rulesEngine);
    return foo->getId();
  }

  ExprTypedef::ExprTypedef(const DataTypeId& baseType, const char* name, Domain* baseDomain)
      : m_baseType(baseType)
      , m_name(name)
      , m_baseDomain(baseDomain)
  {
  }

  ExprTypedef::~ExprTypedef()
  {
      delete m_baseDomain;
  }

  DataRef ExprTypedef::eval(EvalContext& context) const
  {
      const char* name = m_name.c_str();
      const Domain& domain = *m_baseDomain;

      debugMsg("Interpreter:typedef","Defining type:" << name);

      SchemaId schema = getSchema(context);

      // TODO!: this was inherited from the code generator, it's a hack, but schema needs it.
      // see for instance Schema::hasParent()
      schema->addEnum(name);

      schema->getCESchema()->registerDataType(
          (new RestrictedDT(name,m_baseType,domain))->getId()
      );

      debugMsg("Interpreter:typedef"
              , "Created type factory " << name <<
                " with base domain " << domain.toString());

      return DataRef::null;
  }

  std::string ExprTypedef::toString() const
  {
      std::ostringstream os;

      os << "TYPEDEF:" << m_baseType->getName().toString() << " -> " << m_name.toString();

      return os.str();
  }

  ExprEnumdef::ExprEnumdef(const char* name, const std::vector<std::string>& values)
      : m_name(name)
      , m_values(values)
  {
  }

  ExprEnumdef::~ExprEnumdef()
  {
  }

  DataRef ExprEnumdef::eval(EvalContext& context) const
  {
      const char* enumName = m_name.c_str();
      // TODO: hack! drop this after Core.nddl goes away
      if (strcmp(enumName,"TokenStates") == 0) {
          debugMsg("Interpreter:defineEnumeration","Ignoring redefinition for TokenStates enum");
          return DataRef::null;
      }

      debugMsg("Interpreter:enumdef","Defining enum:" << enumName);

      std::list<edouble> values;
      for(unsigned int i=0;i<m_values.size();i++) {
          LabelStr newValue(m_values[i]);
          values.push_back(newValue);
      }
      EnumeratedDomain domain(SymbolDT::instance(),values);

      SchemaId schema = getSchema(context);
      schema->registerEnum(enumName,domain);

      debugMsg("Interpreter:enumdef"
              , "Created type factory " << enumName <<
              " with base domain " << domain.toString());

      return DataRef::null;
  }

  std::string ExprEnumdef::toString() const
  {
      std::ostringstream os;

      os << "ENUMDEF:" << m_name.toString();

      return os.str();
  }

  ExprVarDeclaration::ExprVarDeclaration(const char* name, const DataTypeId& type, Expr* initValue, bool canBeSpecified)
      : m_name(name)
      , m_type(type)
      , m_initValue(initValue)
      , m_canBeSpecified(canBeSpecified)
  {
  }

  ExprVarDeclaration::~ExprVarDeclaration()
  {
      if (m_initValue != NULL)
          delete m_initValue;
  }

  const LabelStr& ExprVarDeclaration::getName() const { return m_name; }
  const DataTypeId ExprVarDeclaration::getDataType() const { return m_type; }
  const Expr* ExprVarDeclaration::getInitValue() const { return m_initValue; }
  void ExprVarDeclaration::setInitValue(Expr* iv) { m_initValue = iv; }

  DataRef ExprVarDeclaration::eval(EvalContext& context) const
  {
      ConstrainedVariableId v;

      // TODO: delegate to contexts instead
      TokenEvalContext* ctx = dynamic_cast<TokenEvalContext*>(&context);
      if (ctx != NULL)
          v = makeTokenVar(*ctx);
      else {
          RuleInstanceEvalContext* riec = dynamic_cast<RuleInstanceEvalContext*>(&context);
          if (riec != NULL)
              v = makeRuleVar(*riec);
          else
              v = makeGlobalVar(context);
      }

      debugMsg("Interpreter:varDeclaration","Declared variable:" << v->toLongString());
      return DataRef(v);
  }

  ConstrainedVariableId ExprVarDeclaration::makeGlobalVar(EvalContext& context) const
  {
      const LabelStr& name = getName();
      const LabelStr& type = getDataType()->getName();
      const Expr* initValue = getInitValue();
      const DbClientId& pdb = getPDB(context);

      ConstrainedVariableId v;

      if (initValue != NULL) {
          v = pdb->createVariable(
              type.c_str(),
              initValue->eval(context).getValue()->baseDomain(), // baseDomain
              name.c_str(),
              false, // isTmpVar
              m_canBeSpecified
          );
      }
      else {
          v = pdb->createVariable(
              type.c_str(),
              name.c_str()
          );
      }

      return v;
  }

  ConstrainedVariableId ExprVarDeclaration::makeTokenVar(TokenEvalContext& context) const
  {
      const LabelStr& parameterName = getName();
      const LabelStr& parameterType = getDataType()->getName();
      const Expr* initValue = getInitValue();
      TokenId token=context.getToken();

      check_runtime_error(token->getVariable(parameterName,false) == ConstrainedVariableId::noId(),
                          "Token parameter "+parameterName.toString()+ " already exists!");

      // This is a hack needed because TokenVariable is parametrized by the domain arg to addParameter
      ConstrainedVariableId parameter;
      const DataTypeId& parameterDataType = getDataType();

      // same as completeObjectParam in NddlRules.hh
      if(initValue != NULL) {
          Domain* bd = parameterDataType->baseDomain().copy();
          ConstrainedVariableId rhs = initValue->eval(context).getValue();
          bd->intersect(rhs->lastDomain());
          parameter = token->addParameter(
                  *bd,
                  parameterName
          );
          delete bd;
          if (context.isClass(parameterName))
              token->getPlanDatabase()->makeObjectVariableFromType(parameterType, parameter);
      }
      else {
          if (context.isClass(parameterType)) {
              parameter = token->addParameter(
                      ObjectDomain(parameterDataType),
                      parameterName
              );
              token->getPlanDatabase()->makeObjectVariableFromType(parameterType, parameter);
          }
          else {
              parameter = token->addParameter(
                      parameterDataType->baseDomain(),
                      parameterName
              );
          }
      }

      debugMsg("Interpreter:InterpretedToken","Token " << token->getPredicateName().toString() << " added Parameter "
              << parameter->toString() << " " << parameterName.toString());

      return parameter;
  }

  ConstrainedVariableId ExprVarDeclaration::makeRuleVar(RuleInstanceEvalContext& context) const
  {
	  const LabelStr typeName = getDataType()->getName();

	  ConstrainedVariableId localVar;
	  if (context.isClass(typeName)) {
		  const DataTypeId& dt = context.getRuleInstance()->getPlanDatabase()->getSchema()->getCESchema()->getDataType(typeName.c_str());
		  localVar = context.getRuleInstance()->addObjectVariable(
				  getDataType()->getName(),
				  ObjectDomain(dt),
				  m_canBeSpecified,
				  m_name
		  );
	  }
	  else {
		  // TODO: do we really need to pass the base domain?
				  const Domain& baseDomain = context.getRuleInstance()->getPlanDatabase()->getSchema()->getCESchema()->baseDomain(typeName.c_str());
				  localVar = context.getRuleInstance()->addLocalVariable(
						  baseDomain,
						  m_canBeSpecified,
						  m_name
				  );
	  }

	  if (m_initValue != NULL)
		  localVar->restrictBaseDomain(m_initValue->eval(context).getValue()->derivedDomain());

	  context.addVar(m_name.c_str(),localVar);
	  debugMsg("Interpreter:InterpretedRule","Added RuleInstance local var:" << localVar->toString());
	  return localVar;
  }

  std::string ExprVarDeclaration::toString() const
  {
      std::ostringstream os;

      os << m_type->getName().c_str() << " " << m_name.toString();
      if (m_initValue != NULL)
          os << " " << m_initValue->toString();

      return os.str();
  }

  ExprAssignment::ExprAssignment(Expr* lhs, Expr* rhs)
      : m_lhs(lhs)
      , m_rhs(rhs)
  {
  }

  ExprAssignment::~ExprAssignment()
  {
      delete m_lhs;
      delete m_rhs;
  }

  DataRef ExprAssignment::eval(EvalContext& context) const
  {
      DataRef lhs;

      ConstrainedVariableId thisVar = context.getVar("this");
      // TODO: modify interpreted object constructor to add vars upfront so that this if stmt isn't necessary
      if (thisVar.isId()) {
          ObjectId object = Entity::getTypedEntity<Object>(thisVar->derivedDomain().getSingletonValue());
          std::string varNameStr = m_lhs->toString();
          const char* varName = varNameStr.c_str(); // TODO: this is a hack!
          check_error(object->getVariable(varName) == ConstrainedVariableId::noId());
          ConstrainedVariableId rhsValue = m_rhs->eval(context).getValue();
          const Domain& domain = rhsValue->derivedDomain();
          ConstrainedVariableId v = object->addVariable(domain,varName);
          lhs = DataRef(v);
          debugMsg("Interpreter:InterpretedObject","Initialized variable:" << object->getName().toString() << "." << varName << " to " << rhsValue->derivedDomain().toString() << " in constructor");
      }
      else {
          lhs = m_lhs->eval(context);

          if (m_rhs != NULL) {
              DataRef rhs = m_rhs->eval(context);
              const DbClientId& pdb = getPDB(context);

              if (rhs.getValue()->lastDomain().isSingleton()) {
                  pdb->restrict(lhs.getValue(),rhs.getValue()->lastDomain());
              }
              else {
                  pdb->restrict(lhs.getValue(),rhs.getValue()->lastDomain());
                  // TODO: this behavior seems more reasonable, specially to support violation reporting
                  // lhs.getValue()->getCurrentDomain().equate(rhs.getValue()->getCurrentDomain());
              }
          }
      }

      return lhs;
  }

  std::string ExprAssignment::toString() const
  {
      std::ostringstream os;

      os << "{ " << m_lhs->toString() << " = " << (m_rhs != NULL ? m_rhs->toString() : "NULL") << "}";

      return os.str();
  }


  ExprObjectTypeDeclaration::ExprObjectTypeDeclaration(const LabelStr& name)
      : m_name(name)
  {
  }

  ExprObjectTypeDeclaration::~ExprObjectTypeDeclaration()
  {
  }

  DataRef ExprObjectTypeDeclaration::eval(EvalContext& context) const
  {
      SchemaId schema = getSchema(context);

      schema->declareObjectType(m_name);

      return DataRef::null;
  }

  std::string ExprObjectTypeDeclaration::toString() const
  {
      std::ostringstream os;

      os << "{class " << m_name.c_str() << "}";

      return os.str();
  }


  ExprObjectTypeDefinition::ExprObjectTypeDefinition(const ObjectTypeId& objType)
      : m_registered(false)
      , m_objType(objType)
  {
  }

  ExprObjectTypeDefinition::~ExprObjectTypeDefinition()
  {
      if (!m_registered) {
          m_objType->purgeAll();
          delete (ObjectType*)m_objType;
      }
  }

  DataRef ExprObjectTypeDefinition::eval(EvalContext& context) const
  {
      SchemaId schema = getSchema(context);

      ObjectType* objType = schema->getObjectType(m_objType->getName());

      if (objType != NULL) {
          // TODO: should always be displayed as INFO!
          // TODO: allow redefinition of non-native classes?
          std::string isNative = (objType->isNative() ? "native" : "");
          debugMsg("Interpreter","Ignoring re-definition for "<< isNative << " class : " << m_objType->getName().c_str());
      }
      else {
          schema->registerObjectType(m_objType);
          m_registered = true;
      }

      return DataRef::null;
  }

  std::string ExprObjectTypeDefinition::toString() const
  {
      std::ostringstream os;

      os << m_objType->toString();

      return os.str();
  }

  ExprRuleTypeDefinition::ExprRuleTypeDefinition(const RuleId& rf)
      : m_ruleFactory(rf)
  {
  }

  ExprRuleTypeDefinition::~ExprRuleTypeDefinition()
  {
  }

  DataRef ExprRuleTypeDefinition::eval(EvalContext& context) const
  {
      RuleSchema* rs = (RuleSchema*)context.getElement("RuleSchema");
      rs->registerRule(m_ruleFactory);
      return DataRef::null;
  }

  std::string ExprRuleTypeDefinition::toString() const
  {
      std::ostringstream os;

      os << m_ruleFactory->toString();

      return os.str();
  }

  void evalArgs(EvalContext& context, std::vector<ConstrainedVariableId>& args,const std::vector<Expr*>& argExprs)
  {
      for (unsigned int i=0;i<argExprs.size();i++)
          args.push_back(argExprs[i]->eval(context).getValue());
  }

  ExprMethodCall::ExprMethodCall(const MethodId& m, Expr* varExpr, const std::vector<Expr*>& argExprs)
      : m_method(m)
      , m_varExpr(varExpr)
      , m_argExprs(argExprs)
  {
  }

  ExprMethodCall::~ExprMethodCall()
  {
      if (m_varExpr != NULL)
          delete m_varExpr;

      for (unsigned int i=0;i<m_argExprs.size();i++)
          delete m_argExprs[i];
      m_argExprs.clear();
  }

  DataRef ExprMethodCall::eval(EvalContext& context) const
  {
      ConstrainedVariableId var;

      if (m_varExpr != NULL) {
          DataRef v = m_varExpr->eval(context);
          var = v.getValue();
      }

      // TODO: make sure any temp vars are disposed of correctly
      std::vector<ConstrainedVariableId> args;
      args.push_back(var);
      evalArgs(context,args,m_argExprs);
      return m_method->eval(context,args);
  }

  std::string ExprMethodCall::toString() const
  {
      std::ostringstream os;

      // TODO: implement this
      os << "METHOD CALL:" << m_method->getName().c_str();

      return os.str();
  }

  ExprVariableMethod::ExprVariableMethod(const char* name, Expr* varExpr, const std::vector<Expr*>& argExprs)
      : m_methodName(name)
      , m_varExpr(varExpr)
      , m_argExprs(argExprs)
  {
  }

  ExprVariableMethod::~ExprVariableMethod()
  {
      if (m_varExpr != NULL)
          delete m_varExpr;

      for (unsigned int i=0;i<m_argExprs.size();i++)
          delete m_argExprs[i];
      m_argExprs.clear();
  }

  DataRef ExprVariableMethod::eval(EvalContext& context) const
  {
      ConstrainedVariableId var;

      if (m_varExpr != NULL) {
          DataRef v = m_varExpr->eval(context);
          var = v.getValue();
      }

      // TODO: make sure any temp vars are disposed of correctly
      std::vector<ConstrainedVariableId> args;
      evalArgs(context,args,m_argExprs);
      return eval(context,var,args);
  }

  DataRef ExprVariableMethod::eval(EvalContext& context, ConstrainedVariableId& var, const std::vector<ConstrainedVariableId>& args) const
  {
      std::string method(m_methodName.toString());
      DbClientId pdb = getPDB(context); // TODO: keep using db client?

      if (method=="specify") {
          const Domain& ad = args[0]->lastDomain();
          if (ad.isSingleton())
              pdb->specify(var,ad.getSingletonValue());
          else
              pdb->restrict(var,ad);
      }
      else if (method=="reset")
          pdb->reset(var);
      else if (method=="close") {
          if (var.isId())
              pdb->close(var);
          else
              pdb->close();
      }
      else
          check_runtime_error(ALWAYS_FAILS,"Unknown variable method:" + method);

      return DataRef::null;
  }

  std::string ExprVariableMethod::toString() const
  {
      std::ostringstream os;

      // TODO: implement this
      os << "VAR_METHOD:" << m_methodName.c_str();

      return os.str();
  }

  ExprObjectMethod::ExprObjectMethod(const char* name, Expr* objExpr, const std::vector<Expr*>& argExprs)
      : m_methodName(name)
      , m_objExpr(objExpr)
      , m_argExprs(argExprs)
  {
  }

  ExprObjectMethod::~ExprObjectMethod()
  {
      for (unsigned int i=0;i<m_argExprs.size();i++)
          delete m_argExprs[i];
      m_argExprs.clear();
  }

  DataRef ExprObjectMethod::eval(EvalContext& context) const
  {
      ObjectId obj;

      if (m_objExpr != NULL) {
          DataRef v = m_objExpr->eval(context);
          obj = Entity::getTypedEntity<Object>(v.getValue()->derivedDomain().getSingletonValue());
      }

      // TODO: make sure any temp vars are disposed of correctly
      std::vector<ConstrainedVariableId> args;
      evalArgs(context,args,m_argExprs);
      return eval(context,obj,args);
  }

  DataRef ExprObjectMethod::eval(EvalContext& context, ObjectId& obj, const std::vector<ConstrainedVariableId>& args) const
  {
      std::string method(m_methodName.toString());
      DbClientId pdb = getPDB(context); // TODO: keep using db client?

      StateVarId stateVar = args[0];
      TokenId pred = stateVar->getParentToken();
      if (args.size()==2)
          stateVar = args[1];
      TokenId succ = stateVar->getParentToken();
      if (method=="constrain")
              pdb->constrain(obj,pred,succ);
      else if (method=="free")
              pdb->free(obj,pred,succ);
      else
          check_runtime_error(ALWAYS_FAILS,"Unknown variable method:" + method);

      return DataRef::null;
  }

  std::string ExprObjectMethod::toString() const
  {
      std::ostringstream os;

      // TODO: implement this
      os << "VAR_METHOD:" << m_methodName.c_str();

      return os.str();
  }

  ExprTokenMethod::ExprTokenMethod(const char* name, const char* tokenName, const std::vector<Expr*>& argExprs)
     : m_methodName(name)
     , m_tokenName(tokenName)
     , m_argExprs(argExprs)
  {
  }

  ExprTokenMethod::~ExprTokenMethod()
  {
      for (unsigned int i=0;i<m_argExprs.size();i++)
          delete m_argExprs[i];
      m_argExprs.clear();
  }

  DataRef ExprTokenMethod::eval(EvalContext& context) const
  {
      TokenId tok = context.getToken(m_tokenName.c_str());

      // TODO: make sure any temp vars are disposed of correctly
      std::vector<ConstrainedVariableId> args;
      evalArgs(context,args,m_argExprs);
      return eval(context,tok,args);
  }

  DataRef ExprTokenMethod::eval(EvalContext& context, TokenId& tok, const std::vector<ConstrainedVariableId>& args) const
  {
      checkError(tok.isId(),"Can't evaluate method on null token");
      std::string method(m_methodName.toString());
      DbClientId pdb = getPDB(context); // TODO: keep using db client?

      if (method=="activate") {
          if(!tok->isActive()) //Temporary.  Pull out when we scrub test input files. DbClientTransactionPlayer is doing the same
              pdb->activate(tok);
      }
      else if (method=="merge") {
          StateVarId stateVar = args[0];
          TokenId activeToken = stateVar->getParentToken();
          pdb->merge(tok,activeToken);
      }
      else if (method=="reject")
          pdb->reject(tok);
      else if (method=="cancel")
          pdb->cancel(tok);
      else
          check_runtime_error(ALWAYS_FAILS,"Unknown token method:" + method);

      debugMsg("Interpreter:ExprTokenMethod","Evaluated token method " << method << " on " << tok->toString());
      return DataRef::null;
  }

  std::string ExprTokenMethod::toString() const
  {
      std::ostringstream os;

      // TODO: implement this
      os << "TOKEN_METHOD:" << m_methodName.c_str();

      return os.str();
  }

  ExprProblemStmt::ExprProblemStmt(const char* name, const std::vector<PredicateInstanceRef*>& tokens)
      : m_name(name)
      , m_tokens(tokens)
  {
  }

  ExprProblemStmt::~ExprProblemStmt()
  {
      for (unsigned int i=0;i<m_tokens.size();i++)
          delete m_tokens[i];
      m_tokens.clear();
  }

  DataRef ExprProblemStmt::eval(EvalContext& context) const
  {
      std::string name(m_name.c_str());
      bool isFact=(name=="fact");
      bool isRejectable=(name=="rejectable");

      for (unsigned int i=0;i<m_tokens.size();i++) {
          TokenId tok = m_tokens[i]->getToken(context,"",isFact,isRejectable);
          debugMsg("Interpreter:ExprProblemStmt","Evaluated " << name << " on " << tok->toString());
      }

      return DataRef::null;
  }

  std::string ExprProblemStmt::toString() const
  {
      std::ostringstream os;

      // TODO: implement this
      os << "PROBLEM_STMT:" << m_name.c_str();

      return os.str();
  }

}

