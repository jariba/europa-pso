#include "ANMLTranslator.hh"

#include "Debug.hh"

#include <sstream>

namespace ANML
{	
    ANMLTranslator::ANMLTranslator()
    {
    	m_context = createGlobalContext();
    	m_plannerConfig = new LHSPlannerConfig();
    }
    
    ANMLTranslator::~ANMLTranslator()
    {
    	delete m_context;
    	delete m_plannerConfig;
    }
    
    ANMLContext* ANMLTranslator::createGlobalContext()
    {
    	ANMLContext* context = new ANMLContext();
    	
    	context->addType(Type::BOOL);
    	context->addType(Type::INT);    	
    	context->addType(Type::FLOAT);
    	context->addType(Type::STRING);    	   	
    	context->addType(Type::OBJECT);
    	
    	// start and end of the planning horizon
    	context->addVariable(new Variable(*Type::INT,"start"));
    	context->addVariable(new Variable(*Type::INT,"end"));
    	
    	{
    		std::vector<Arg*> args;
    		args.push_back(new Arg("start_horizon",*Type::INT));
    		args.push_back(new Arg("end_horizon"  ,*Type::INT));    		
    	    context->addVariable(new Variable(*Type::BOOL,"PlanningHorizon",args));
    	}
    	
    	{
    		std::vector<Arg*> args;
    		args.push_back(new Arg("max_steps",*Type::INT));
    		args.push_back(new Arg("max_depth",*Type::INT));
    	    context->addVariable(new Variable(*Type::BOOL,"PlannerConfig",args));
    	}
    	
    	{
    		std::vector<Arg*> args;
    		args.push_back(new Arg("resource",*Type::INT));
    		args.push_back(new Arg("quantity",*Type::INT));
    	    context->addVariable(new Variable(*Type::BOOL,"uses",args));
    	}
    	
    	{
    		std::vector<const Type*> argTypes;
    		argTypes.push_back(Type::INT);
    		argTypes.push_back(Type::INT);
    	    context->addConstraint(new ConstraintDef("eq",argTypes));
    	}
    	
    	{
    		std::vector<const Type*> argTypes;
    		argTypes.push_back(Type::STRING);
    		argTypes.push_back(Type::STRING);
    	    context->addConstraint(new ConstraintDef("eq",argTypes));
    	}

    	{
    		std::vector<const Type*> argTypes;
    		argTypes.push_back(Type::INT);
    		argTypes.push_back(Type::INT);
    	    context->addConstraint(new ConstraintDef("neq",argTypes));
    	}
    	
    	{
    		std::vector<const Type*> argTypes;
    		argTypes.push_back(Type::STRING);
    		argTypes.push_back(Type::STRING);
    	    context->addConstraint(new ConstraintDef("neq",argTypes));
    	}
    	
    	return context;
    }
    
    void ANMLTranslator::pushContext(ANMLContext* context)
    {
    	context->setParentContext(m_context);
    	m_context = context;
    }
 
    void ANMLTranslator::popContext()
    {
    	m_context = (ANMLContext*)(m_context->getParentContext());
    	check_runtime_error(m_context != NULL,"ANMLTranslator context can't be NULL");
    }      
            
    void ANMLTranslator::toNDDL(std::ostream& os) const
    {
    	m_plannerConfig->toNDDL(os);
    	m_context->toNDDL(os);        
    }

    std::string ANMLTranslator::toString() const
    {
    	std::ostringstream os;    
    	toNDDL(os);            	
    	return os.str();
    }

    ANMLContext::ANMLContext(const ANMLContext* parent)
        : m_parentContext(parent)
	{
	}
	
    ANMLContext::~ANMLContext()
    {
    }

    void ANMLContext::addElement(ANMLElement* element)
    {
    	check_error(element != NULL, "Can't add a NULL element to an ANML context");
    	
   	    m_elements.push_back(element);
   	    debugMsg("ANMLContext", getContextDesc() << " Added element:" << element->getType() << " " << element->getName());    	    
    }

    ObjType* ANMLContext::addObjType(const std::string& name,const std::string& parentObjType)
    {
    	check_runtime_error(getType(name) == NULL,"data type "+name+" already defined");
    	    
    	ObjType* parent = getObjType(parentObjType);
    	check_runtime_error(parent != NULL,"parent class "+parentObjType+" has not been defined");
    	
    	ObjType* newType = new ObjType(name,parent);
    	addType(newType);
    	
    	return newType;
    }
	
    void ANMLContext::addType(Type* type)
    {
    	m_types[type->getName()] = type;
    	debugMsg("ANMLContext",getContextDesc() << " Added type:" << type->getName());
    }
	
	Type* ANMLContext::getType(const std::string& name,bool mustExist) const
	{
		std::map<std::string,Type*>::const_iterator it = m_types.find(name);
		
		if (it != m_types.end()) 
		    return it->second;
		
		if (m_parentContext != NULL)
		    return m_parentContext->getType(name,mustExist);
		       
		if (mustExist)
    		check_runtime_error(false, "Type "+name+" has not been defined");
		   
    	return NULL;
	}

	ObjType* ANMLContext::getObjType(const std::string& name) const
	{
		Type* type = getType(name);
		check_runtime_error(type != NULL, "Object type "+name+" has not been defined");
		check_runtime_error(!(type->isPrimitive()),name+" is a primitive type, not an Object type");
		   
		return (ObjType*)type;
	}
		
	void ANMLContext::addAction(Action* a)
	{
		// TODO: check name against functions as well?
		// TODO: warn if it hides an element in a parent context?
    	check_runtime_error(getAction(a->getName()) == NULL,"Action "+a->getName()+" already defined");
		m_actions[a->getName()] = a;
	}
	
	Action* ANMLContext::getAction(const std::string& name,bool mustExist) const
	{
		std::map<std::string,Action*>::const_iterator it = m_actions.find(name);
		
		if (it != m_actions.end()) 
		    return it->second;
		   
		if (m_parentContext != NULL)
		    return m_parentContext->getAction(name,mustExist);
		       
		if (mustExist)
    		check_runtime_error(false, "Action "+name+" has not been defined");
		   
    	return NULL;
	}
	
	void ANMLContext::addVariable(Variable* v)
	{
		// TODO: check name against actions as well?
		// TODO: warn if it hides an element in a parent context?
    	check_runtime_error(getVariable(v->getName()) == NULL,"Variable "+v->getName()+" already defined");
		m_variables[v->getName()] = v;
    	debugMsg("ANMLContext",getContextDesc() << " Added variable:" << v->getName());		
	}
	
	Variable* ANMLContext::getVariable(const std::string& name,bool mustExist) const
	{
		std::map<std::string,Variable*>::const_iterator it = m_variables.find(name);
		
		if (it != m_variables.end()) 
		    return it->second;
		   
		if (m_parentContext != NULL)
		    return m_parentContext->getVariable(name,mustExist);
		       
		if (mustExist)
    		check_runtime_error(false, "Variable "+name+" has not been defined");
		   
    	return NULL;
	}
		
	std::string makeKey(const std::string& name,const std::vector<const Type*>& argTypes)
	{
		std::ostringstream os;
		
		os << name;
		
		for (unsigned int i=0;i<argTypes.size();i++) 
		    os << ":" << argTypes[i]->getName();
		
		return os.str();
	}
	 	
    void ANMLContext::addConstraint(ConstraintDef* c)
    {
    	std::string key = makeKey(c->getName(),c->getArgTypes());
    	check_runtime_error(getConstraint(c->getName(),c->getArgTypes()) == NULL,"Constraint "+key+" already defined");
    	m_constraints[key] = c;
    	debugMsg("ANMLContext",getContextDesc() << " Added Constraint " << key);
    }
    
    ConstraintDef* ANMLContext::getConstraint(const std::string& name,const std::vector<const Type*>& argTypes,bool mustExist) const
    {
    	std::string key = makeKey(name,argTypes);
		std::map<std::string,ConstraintDef*>::const_iterator it = m_constraints.find(key);
		
		if (it != m_constraints.end()) 
		    return it->second;
        // TODO: check for generic matches, like numeric vs int,float		    
		   
		if (m_parentContext != NULL)
		    return m_parentContext->getConstraint(name,argTypes,mustExist);
				     
		if (mustExist)
    		check_runtime_error(false, "Constraint "+key+" has not been defined");
		   
    	return NULL;
    }
	
    void ANMLContext::toNDDL(std::ostream& os) const
    {
    	for (unsigned int i=0; i<m_elements.size(); i++) 
    	    m_elements[i]->toNDDL(os);
    }
	
    std::string ANMLContext::toString() const
    {
    	std::ostringstream os;
    	
    	for (unsigned int i=0; i<m_elements.size(); i++) {
    		debugMsg("ANMLContext", "toString:" << i << " " << m_elements[i]->getType()); 
    	    os << m_elements[i]->toString() << std::endl; 
    	}   	     
    	
    	return os.str();
    }

    void ANMLElement::toNDDL(std::ostream& os) const 
    { 
    	os << m_type << " " << m_name << std::endl; 
    }
    
    std::string ANMLElement::toString() const 
    { 
    	std::ostringstream os; 
    	toNDDL(os); 
    	return os.str(); 
    }
    
    Type* Type::VOID   = new Type("void");
    Type* Type::BOOL   = new Type("bool");
    Type* Type::INT    = new Type("int");
    Type* Type::FLOAT  = new Type("float");
    Type* Type::STRING = new Type("string");
    Type* Type::OBJECT = new ObjType("object",NULL);
    
	Type::Type(const std::string& name)
	    : ANMLElement("TYPE",name)
	{
	}
	
	Type::~Type()
	{
	}
	        
    std::string autoIdentifier(const char* base)
    {
    	static int cnt=0;
    	std::ostringstream os;
        
        os << base << "_" << cnt++;
        
        return os.str();    	
    }
    
	TypeAlias::TypeAlias(const std::string& name,const Type& t) 
	    : Type(name)
	    , m_wrappedType(t) 
	{
	}
	
	TypeAlias::~TypeAlias() 
	{
	}
	    	    
    void TypeAlias::toNDDL(std::ostream& os) const 
    { 
    	os << "typedef " << m_wrappedType.getName() << " " << m_name << ";" << std::endl; 
    }    
    
	Range::Range(const std::string& name,const Type& dataType,const std::string& lb,const std::string& ub)	
	    : Type(name!="" ? name : autoIdentifier("Range"))
	    , m_dataType(dataType)
	    , m_lb(lb)
	    , m_ub(ub)
	{
		// TODO: convert lb,ub to typed values
	}
	
	Range::~Range()
	{
	}
	        
    void Range::toNDDL(std::ostream& os) const
    {
        os << "typedef " << m_dataType.getName() << " [" << m_lb << " " << m_ub <<  "] " << getName() << ";"<< std::endl << std::endl;
    }
    
	Enumeration::Enumeration(const std::string& name,const Type& dataType,const std::vector<std::string>& values)
	    : Type(name!="" ? name : autoIdentifier("Enumeration"))
	    , m_dataType(dataType)
	    , m_values(values)
	{
		// TODO: convert lb,ub to typed values
	}
	
	Enumeration::~Enumeration()
	{
	}
	        
    void Enumeration::toNDDL(std::ostream& os) const
    {
        os << "enum " << getName() << " {";
        
        for (unsigned int i=0;i<m_values.size(); i++) {
            if (i>0)
                os << ",";
            os << m_values[i];
        }   
        os << "};" << std::endl << std::endl;                                
    }
    
    Variable::Variable(const Type& dataType, const std::string& name)
        : ANMLElement("VARIABLE",name)
        , m_dataType(dataType)
    {
    }

    Variable::Variable(const Type& dataType, const std::string& name, const std::vector<Arg*>& args)
        : ANMLElement("VARIABLE",name)
        , m_dataType(dataType)
        , m_args(args)
    {
    }

    Variable::~Variable()
    {
    }

    void Variable::toNDDL(std::ostream& os) const
    {
    	os << m_dataType.getName() << " " << m_name;
    }
    
    VarDeclaration::VarDeclaration(const Type& type, const std::vector<VarInit*>& init)
        : ANMLElement("VAR_DECLARATION")
        , m_dataType(type)
        , m_init(init)
    {
    }
    
    VarDeclaration::~VarDeclaration()
    {
    }

    void VarDeclaration::toNDDL(std::ostream& os) const
    {
    	os << m_dataType.getName() << " ";
    	for (unsigned int i=0; i < m_init.size(); i++) {
    		if (i>0)
    		    os << " , ";
    		os << m_init[i]->getName();
    		if (m_init[i]->getValue().length() > 0) {
    			os << "=" << m_init[i]->getValue();
    		}
    		else if (!m_dataType.isPrimitive()) {
    			os << " = new " << m_dataType.getName() << "()";
    		}
    	}
    	os << ";" << std::endl;
    }
    
    FreeVarDeclaration::FreeVarDeclaration(const std::vector<Variable*>& vars)
        : ANMLElement("FREE_VAR_DECLARATION")
        , m_vars(vars)
    {
    }
    
    FreeVarDeclaration::~FreeVarDeclaration()
    {
    }

    void FreeVarDeclaration::toNDDL(std::ostream& os) const
    {
    	for (unsigned int i=0; i < m_vars.size(); i++) 
    		os << m_vars[i]->getDataType().getName() << m_vars[i]->getName() << ";";
    }
    
    ObjType::ObjType(const std::string& name,ObjType* parentObjType)
        : Type(name)
        , m_parentObjType(parentObjType)
    {
    }
    
    ObjType::~ObjType()
    {
    }
    
    void ObjType::toNDDL(std::ostream& os) const
    {
        std::string parent = (
            (m_parentObjType != NULL && m_parentObjType->getName() != "object") 
                ? (std::string(" extends ") + m_parentObjType->getName()) 
                : ""
        );
        
        os << "class " << m_name << parent << std::endl 
           << "{" << std::endl;
                      
        for (unsigned int i=0; i<m_elements.size(); i++) {
    	    if (m_elements[i]->getType() == "ACTION") {
    	    	Action* a = (Action*) m_elements[i];
    	    	os << "    predicate " << a->getName() << " {";
    	    	
    	    	const std::vector<Variable*> params = a->getParams();
                for (unsigned int j=0; j<params.size(); j++) {
        	        params[j]->toNDDL(os);
       	            os << ";";
                }    	
    	    	
    	        os << "}" << std::endl;
    	    }
    	    else if (m_elements[i]->getType() == "VAR_DECLARATION") {
    	    	VarDeclaration* vd = (VarDeclaration*)m_elements[i];
    	    	for (unsigned j=0;j<vd->getInit().size();j++)
    	    	    os << "    " << vd->getDataType().getName() << " " << vd->getInit()[j]->getName() << ";" << std::endl;
    	    }
    	}

        // Generate constructor and initialize members that require contructors themselves
        os << std::endl << "    " << getName() << "()" << std::endl 
           << "    {" << std::endl;
        for (unsigned int i=0; i<m_elements.size(); i++) {
    	    if (m_elements[i]->getType() == "VAR_DECLARATION") {
    	    	VarDeclaration* vd = (VarDeclaration*)m_elements[i];
    	    	if (!vd->getDataType().isPrimitive()) {
        	        for (unsigned j=0;j<vd->getInit().size();j++) {
    	    	        os << "        " << vd->getInit()[j]->getName() 
    	    	           << " = new " << vd->getDataType().getName() << "();" << std::endl;
        	        }
    	    	}
    	    }
        }
        os << "    }" << std::endl;
           
        os << "}" << std::endl << std::endl;
        
        for (unsigned int i=0; i<m_elements.size(); i++) {
    	    if (m_elements[i]->getType() != "VAR_DECLARATION") 
    	        m_elements[i]->toNDDL(os);
        }    	     
    }
    
    Action::Action(ObjType& objType,const std::string& name, const std::vector<Variable*>& params)
        : ANMLElement("ACTION",name)
        , m_objType(objType)        
        , m_params(params)
    {
    }
    
    Action::~Action()
    {
    }
    
    void Action::setBody(const std::vector<ANMLElement*>& body)
    {
    	m_body = body;
    }
    
    void Action::toNDDL(std::ostream& os) const
    {
    	os << m_objType.getName() << "::" << m_name << "(";
        for (unsigned int i=0; i<m_params.size(); i++) {
        	if (i>0)
        	    os << ",";
        	m_params[i]->toNDDL(os);
        }    	
    	os << ")" << std::endl;

    	os << "{" << std::endl;
        for (unsigned int i=0; i<m_body.size(); i++) {
    		debugMsg("ANMLContext", "Action::toNDDL " << i << " " << m_body[i]->getType()); 
        	m_body[i]->toNDDL(os);
        }    	
    	os << "}" << std::endl << std::endl;
    }
    
    ActionDuration::ActionDuration(const std::vector<Expr*>& values)
        : ANMLElement("ACTION_DURATION")
        , m_values(values)
    {
    }
    
    ActionDuration::~ActionDuration()
    {    	
    }
    
    void ActionDuration::toNDDL(std::ostream& os) const
    {
    	os << "    eq(duration,";
    	
    	if (m_values.size() == 2) 
    		os << "[" << m_values[0]->toString() << " " << m_values[1]->toString() << "]";
    	else 
    	    os << m_values[0]->toString();
    	    
    	os << ");" << std::endl;
    }    
    
    
    TemporalQualifier::TemporalQualifier(const std::string& op,const std::vector<Expr*>& args) 
        : m_operator(op)
        , m_args(args)
        , m_argValues(args.size())
    {
    }
    
    TemporalQualifier::~TemporalQualifier() 
    {
    }
     
    void TemporalQualifier::toNDDL(std::ostream& os,Proposition::Context context) const 
    {
        // Evaluate all args, cache var names if necessary.
        for (unsigned int i=0;i<m_args.size();i++) {
            if (m_args[i]->needsVar()) {
            	std::string varName = autoIdentifier("_v");
            	m_argValues[i] = varName;
            	m_args[i]->toNDDL(os,context,varName);
            }
            else {
            	m_argValues[i] = m_args[i]->toString();
            }
        }                 
    }
     
    void TemporalQualifier::toNDDL(std::ostream& os, const std::string& ident,const std::string& fluentName) const 
    { 
    	if (m_operator == "at") {
    		const std::string& timePoint = m_argValues[0];
    		// TODO: determine context for timePoint, eval expr if necessary
    		os << ident << "leq(" << fluentName << ".start," << timePoint << ");" << std::endl;
    		os << ident << "leq(" << timePoint << "," << fluentName << ".end);"<< std::endl;    		
    	}
    	else if (m_operator == "over") {
    		const std::string& lb = m_argValues[0];
    		const std::string& ub = m_argValues[1];
    		// TODO: determine context for time points, eval expr if necessary
    		os << ident << "leq(" << fluentName << ".start," << lb << ");" << std::endl;
    		os << ident << "leq(" << ub << "," << fluentName << ".end);" << std::endl;    		                		
    	}
    	else if (m_operator == "in") {
    	}
    	else if (m_operator == "after") {
    	}
    	else if (m_operator == "before") {
    		const std::string& timePoint = m_argValues[0];
    		// TODO: determine context for timePoint, eval expr if necessary
    		os << ident << "leq(" << fluentName << ".end," << timePoint << ");" << std::endl;
    	}
    	else if (m_operator == "contains") {
    	}
    }
    
    RelationalFluent::RelationalFluent(LHSExpr* lhs,Expr* rhs) 
        : m_lhs(lhs)
        , m_rhs(rhs) 
    {
    	debugMsg("ANMLContext","Created RelationalExpr : " << lhs->toString() << " = " << (rhs != NULL ? rhs->toString() : "NULL"));
    }
    
    RelationalFluent::~RelationalFluent() 
    {
    }
        
    void RelationalFluent::toNDDL(std::ostream& os, TemporalQualifier* tq) const 
    {
    	std::string ident = (m_parentProp->getContext() == Proposition::GOAL || 
    	                     m_parentProp->getContext() == Proposition::FACT ? "" : "    ");
    	                     
   	    std::string varName = (m_lhs->needsVar() ? autoIdentifier("_v") : "");
   	    
    	m_lhs->toNDDL(os,m_parentProp->getContext(),varName);
    	if (m_lhs->needsVar()) 
   		    tq->toNDDL(os,ident,varName);
   		
    	if (m_rhs != NULL)
    	    m_rhs->toNDDL(os,m_parentProp->getContext(),varName);     
    }    
    
    Constraint::Constraint(const std::string& name,const std::vector<ANML::Expr*>& args) 
        : m_name(name)
        , m_args(args)
    {
    } 
       
    Constraint::~Constraint() 
    {
    }
    
    void Constraint::toNDDL(std::ostream& os, TemporalQualifier* tq) const
    {
    	// TODO: deal with the temporal qualifier??
    	os << m_name << "(";
    	
    	for (unsigned int i=0;i<m_args.size();i++) {
    		if (i>0)
    		    os << ",";
    		os << m_args[i]->toString();
    	}    	
    	
    	os << ");" << std::endl;
    }

    Condition::Condition(const std::vector<Proposition*>& propositions) 
        : ANMLElement("CONDITION")
        , m_propositions(propositions) 
    {
    	for (unsigned int i=0;i<m_propositions.size();i++) 
    		m_propositions[i]->setContext(Proposition::CONDITION);
    }
    
    Condition::~Condition() 
    {
    }
    
    void Condition::toNDDL(std::ostream& os) const 
    { 
    	for (unsigned int i=0; i<m_propositions.size(); i++) {
    		m_propositions[i]->toNDDL(os);
    	}
    }
    
    Effect::Effect(const std::vector<Proposition*>& propositions) 
        : ANMLElement("EFFECT")
        , m_propositions(propositions) 
    {
    	for (unsigned int i=0;i<m_propositions.size();i++) 
    		m_propositions[i]->setContext(Proposition::EFFECT);
    }
    
    Effect::~Effect() 
    {
    }
    
    void Effect::toNDDL(std::ostream& os) const 
    { 
    	for (unsigned int i=0; i<m_propositions.size(); i++) {
    		m_propositions[i]->toNDDL(os);
    	}
    }
    
    
    Goal::Goal(const std::vector<Proposition*>& propositions) 
        : ANMLElement("GOAL")
        , m_propositions(propositions) 
    {
    	for (unsigned int i=0;i<m_propositions.size();i++) 
    		m_propositions[i]->setContext(Proposition::GOAL);
    }
    
    Goal::~Goal() 
    {
    }
    
    void Goal::toNDDL(std::ostream& os) const 
    { 
    	for (unsigned int i=0; i<m_propositions.size(); i++) 
    		m_propositions[i]->toNDDL(os);
    }
    
    Fact::Fact(const std::vector<Proposition*>& propositions) 
        : ANMLElement("FACT")
        , m_propositions(propositions) 
    {
    	for (unsigned int i=0;i<m_propositions.size();i++) 
    		m_propositions[i]->setContext(Proposition::FACT);
    }
    
    Fact::~Fact() 
    {
    }
    
    void Fact::toNDDL(std::ostream& os) const 
    { 
    	for (unsigned int i=0; i<m_propositions.size(); i++) 
    		m_propositions[i]->toNDDL(os);
    }
    
    Proposition::Proposition(TemporalQualifier* tq,const std::vector<Fluent*>& fluents) 
       : ANMLElement("PROPOSITION")
       , m_temporalQualifier(tq)
       , m_fluents(fluents) 
    {
  	    debugMsg("ANMLContext","Creating  Proposition with " << fluents.size() << " fluents");
    	m_temporalQualifier->setProposition(this);
    	for (unsigned int i=0;i<m_fluents.size();i++) 
    	    m_fluents[i]->setProposition(this); 
    }
    
    Proposition::~Proposition() 
    {
    }
    
    void Proposition::toNDDL(std::ostream& os) const 
    {
    	// output any expressions needed for the parameters of the temporal qualifier
    	m_temporalQualifier->toNDDL(os,m_context);
    	 
    	for (unsigned int i=0;i<m_fluents.size();i++) 
    	    m_fluents[i]->toNDDL(os,m_temporalQualifier);
    }
    
    void LHSAction::toNDDL(std::ostream& os,Proposition::Context context,const std::string& varName) const 
    { 
    	switch (context) {
    		case Proposition::GOAL : 
    		    os << "goal(" << m_action->getName() << " " << varName << ");" << std::endl;
    		    break; 
    		case Proposition::CONDITION : 
    		    os << "    any(" << m_action->getName() << " " << varName << ");" << std::endl;
    		    break; 
    		case Proposition::FACT : 
    		    check_runtime_error(false,"ERROR! LHSAction not supported for FACTS");
    		    break; 
    		case Proposition::EFFECT : 
    		    check_runtime_error(false,"ERROR! LHSAction not supported for EFFECTS");
    		    break;
    		default:
    		    check_error(false,"Unexpected error");
    		    break;
    	}
    }          
    
    void LHSVariable::toNDDL(std::ostream& os,Proposition::Context context,const std::string& varName) const 
    { 
    	// TODO: implement this
    }
    
   void ExprArithOp::toNDDL(std::ostream& os,Proposition::Context context,const std::string& varName) const 
   {
       std::string op1,op2;
       
       if (m_op1->needsVar()) {
         op1 = autoIdentifier("_v");
         m_op1->toNDDL(os,context,op1);
       }
       else {
       	 op1 = m_op1->toString();
       }
       	
       if (m_op2->needsVar()) {
         op2 = autoIdentifier("_v");
         m_op2->toNDDL(os,context,op2);
       }
       else {
       	 op2 = m_op2->toString();
       }
       
       std::string ident="    ";
       if (m_operator == "-") {
       	   // TODO: need real type here!
       	   os << ident << "int " << varName << ";" << std::endl;
           // addEq(x,y,z) means x+y=z which implies x=z-y
           os << ident << "addEq(" << varName << "," << op2 << "," << op1 << ");" << std::endl << std::endl;
       }
   }    
   
   ExprVector::ExprVector(const std::vector<Expr*>& values)
       : m_values(values) 
   {
   	   m_dataType = new ObjType(autoIdentifier("VectorType_"),(ObjType*)Type::OBJECT);
       for (unsigned int i=0;i<m_values.size();i++)  {
       	   std::ostringstream name; name << "member_" << i;
           m_dataType->addVariable(new Variable(m_values[i]->getDataType(),name.str()));
       }
   }
   
   ExprVector::~ExprVector() 
   {
       delete m_dataType;
   }
  
   std::string ExprVector::toString() const
   {
       std::ostringstream os;
    
       os << "(";   
       for (unsigned int i=0;i<m_values.size();i++) {
           if (i>0)
             os << ",";
           os << m_values[i]->toString();
       }
       os << ")" << std::endl;
       
       return os.str();
   }
    
   void ExprVector::toNDDL(std::ostream& os,Proposition::Context context,const std::string& varName) const
   {
       // TODO: implement this correctly
       os << toString();
   }
   
    
    // Special expression to handle PlannerConfig
    LHSPlannerConfig::LHSPlannerConfig()
        : m_startHorizon(NULL)
        , m_endHorizon(NULL)
        , m_maxSteps(NULL)
        , m_maxDepth(NULL)
    {
    }
	
	LHSPlannerConfig::~LHSPlannerConfig()
	{
	}
	
    void LHSPlannerConfig::setArgs(const std::string& predicate,const std::vector<Expr*>& args)
    {
    	if (predicate == "PlannerConfig") {
    		m_maxSteps = args[0]; 
    		m_maxDepth = args[1]; 
    	}
    	else { // PlanningHorizon
    		m_startHorizon = args[0]; 
    		m_endHorizon = args[1]; 
    	}    	  
    }
    
    void LHSPlannerConfig::toNDDL(std::ostream& os) const
    {
    	std::string sh = (m_startHorizon != NULL ? m_startHorizon->toString() : "0");
    	std::string eh = (m_endHorizon != NULL ? m_endHorizon->toString() : "1");
    	std::string ms = (m_maxSteps != NULL ? m_maxSteps->toString() : "+inf");
    	std::string md = (m_maxDepth != NULL ? m_maxDepth->toString() : "+inf");
     	
   	    os << "int start=" << sh << ";" << std::endl;
   	    os << "int end="   << eh << ";" << std::endl;
   	    os << "int solver_maxSteps=" << ms << ";" << std::endl;
   	    os << "int solver_maxDepth=" << md << ";" << std::endl;
    	
    	os << "PlannerConfig plannerConfiguration = new PlannerConfig(start,end,solver_MaxSteps,solver_maxDepth);" 
    	   << std::endl << std::endl;    	
    }            
}
