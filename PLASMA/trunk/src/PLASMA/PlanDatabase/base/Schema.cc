#include "Schema.hh"
#include "Domains.hh"
#include "Debug.hh"
#include "Utils.hh"
#include "Object.hh"
#include "DataTypes.hh"


#ifdef _MANAGED
  using namespace System;

  void MarshalString ( String ^ s, std::string& os ) {
    using namespace Runtime::InteropServices;
    const char* chars = (const char*)(Marshal::StringToHGlobalAnsi(s)).ToPointer();
    os = chars;
    Marshal::FreeHGlobal(IntPtr((void*)chars));
  }
#endif


namespace EUROPA {

  const char* Schema::getDelimiter(){
    static const char* sl_delimiter = ".";
    return sl_delimiter;
  }

  const LabelStr& Schema::rootObject(){
    static const LabelStr sl_rootObject("Object");
    return sl_rootObject;
  }

  const std::set<LabelStr>& Schema::getBuiltInVariableNames(){
    static std::set<LabelStr> sl_instance;
    static bool sl_initialized(false);

    if(!sl_initialized){
      sl_instance.insert(LabelStr("start"));
      sl_instance.insert(LabelStr("end"));
      sl_instance.insert(LabelStr("duration"));
      sl_instance.insert(LabelStr("object"));
      sl_instance.insert(LabelStr("state"));
      sl_initialized = true;
    }

    return sl_instance;
  }

  const LabelStr Schema::makeQualifiedName(const LabelStr& objectType,
					   const LabelStr& unqualifiedPredicateName){
    std::string fullName = objectType.toString() + getDelimiter() + unqualifiedPredicateName.toString();
    return LabelStr(fullName.c_str());
  }

  Schema::Schema(const LabelStr& name, const CESchemaId& ces)
      : m_id(this)
      , m_name(name)
      , m_ceSchema(ces)
      , m_objectTypeMgr((new ObjectTypeMgr())->getId())
      , m_tokenTypeMgr((new TokenTypeMgr())->getId())
  {
      reset();
      debugMsg("Schema:constructor", "created Schema:" << name.toString());
  }

  Schema::~Schema()
  {
      delete (TokenTypeMgr*)m_tokenTypeMgr;
      delete (ObjectTypeMgr*)m_objectTypeMgr;

      std::map<edouble,MethodId>::iterator mit = m_methods.begin();
      for(;mit != m_methods.end();++mit)
          delete (Method*)mit->second;
      m_methods.clear();

      m_id.remove();
  }

  const SchemaId& Schema::getId() const {return m_id;}

  const LabelStr& Schema::getName() const {return m_name;}

  bool Schema::canCompare(const Domain& domx, const Domain& domy) const {

    // If either is an object type, both must be. All objects types are comaparable with
    // each other, even though the domain types may differ. ObjectId's are unambiguous.
    if(isObjectType(domx.getTypeName()) || isObjectType(domy.getTypeName()))
      return(isObjectType(domx.getTypeName()) && isObjectType(domy.getTypeName()));

    return DomainComparator::canCompare(domx, domy);
  }

  void Schema::reset(){
    primitives.clear();
    enumValues.clear();
    objectTypes.clear();
    predicates.clear();
    membershipRelation.clear();
    childOfRelation.clear();
    objectPredicates.clear();
    typesWithNoPredicates.clear();

    // Add System entities
	addPrimitive("int");
	addPrimitive("float");
	addPrimitive("bool");
	addPrimitive("string");
  }

  bool Schema::isType(const LabelStr& type) const{
    return(isPrimitive(type) || isObjectType(type) || isEnum(type) || isPredicate(type));
  }

  bool Schema::isPrimitive(const LabelStr& str) const {
    return (primitives.find(str) != primitives.end());
  }

  bool Schema::isPredicate(const LabelStr& predicateName) const {

    if(m_predTrueCache.find(predicateName) != m_predTrueCache.end())
      return true;

    if(m_predFalseCache.find(predicateName) != m_predFalseCache.end())
      return false;

    bool result = false;

    if(predicates.find(predicateName) != predicates.end()) // If a direct hit, then true
      result = true;
    else if(predicateName.countElements(getDelimiter()) != 2) // If not the correct format, return false
      result = false;
    else {
      // Call recursively if we have a parent
      std::string predStr;
      if(makeParentPredicateString(predicateName, predStr))
	result = isPredicate(predStr);
    }

    if(result)
      m_predTrueCache.insert(predicateName);
    else
      m_predFalseCache.insert(predicateName);

    return result;
  }

  bool Schema::isObjectType(const LabelStr& str) const {
    return objectTypes.find(str) != objectTypes.end();
  }

  bool Schema::isEnum(const LabelStr& str) const {
    return (enumValues.find(str) != enumValues.end());
  }

  bool Schema::isEnumValue(const LabelStr& enumName, edouble value) const {
    check_error(isEnum(enumName), enumName.toString() + " is not defined.");
    const std::set<edouble>& members = enumValues.find(enumName)->second;
    return(members.find(value) != members.end());
  }

  bool Schema::isEnumValue(edouble value) const {
    return(enumValuesToEnums.find(value) != enumValuesToEnums.end());
  }

  bool Schema::canBeAssigned(const LabelStr& objectType,
			     const LabelStr& predicate) const {
    check_error(isObjectType(objectType), objectType.toString() + " is not defined as an ObjectType");
    check_error(isPredicate(predicate), predicate.toString() + " is not defined as a Predicate");
    return isA(objectType, getObjectTypeForPredicate(predicate));
  }

  /**
   * @brief Also used internally in this class for testing if 2 domains are
   * comparable
   * @see canCompare
   */
  bool Schema::isA(const LabelStr& descendant,
		   const LabelStr& ancestor) const {
    debugMsg("Schema:isA", "Checking if " << descendant.toString() << " is a " << ancestor.toString());

    // Special case if the 2 are the same, in which case we suspend any requirement that
    // they be predefined types - class, predicate, enum, primitive.
    if(descendant == ancestor)
      return true;

    checkError(isType(descendant),
	       descendant.toString() << " is not defined.");
    checkError(isType(ancestor),
	       "Ancestor of '" << descendant.toString() << "' is '" << ancestor.toString() << "' which is not defined.");

    if(hasParent(descendant))
      return isA(getParent(descendant), ancestor);

    /** Temporary hack to allow primitives to be casted **/
    if(isPrimitive(descendant) && isPrimitive(ancestor))
      return true;

    return false;
  }

  bool Schema::canContain(const LabelStr& parentType,
			  const LabelStr& memberType,
			  const LabelStr& memberName) const {
    check_error(isType(parentType), parentType.toString() + " is not defined.");

    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(parentType);

    // If no hit, then try for the parent. There must be one since it is a valid
    // type but no hit yet
    if(membershipRelation_it == membershipRelation.end())
      return canContain(getParent(parentType), memberType, memberName);

    // Otherwise, we have a parentType with members defined, so search there
    const NameValueVector& members = membershipRelation_it->second;
    for(NameValueVector::const_iterator it = members.begin(); it != members.end();++it){
      const NameValuePair& pair = *it;
      if(pair.second == memberName && isA(memberType, pair.first))
          return true;
    }

    // Call recursively for inheritance relationships on parent and member types
    if(hasParent(parentType) && canContain(getParent(parentType), memberType, memberName))
      return true;

    // Allow for the possibility that it is declared as base type of the member type
    if(isObjectType(memberType) &&
       hasParent(memberType) &&
       canContain(parentType,getParent(memberType), memberName))
      return true;

    return false;
  }

  const Schema::NameValueVector& Schema::getMembers(const LabelStr& objectType) const
  {
    std::map<edouble, NameValueVector>::const_iterator it = membershipRelation.find(objectType);

    check_error(it != membershipRelation.end(), "Unable to find members for object type:" + objectType.toString() );
    return it->second;
  }

  // For now just return the member names, not their types:
  // TODO:  Is it better to use an iterator in this loop?
  PSList<std::string> Schema::getMembers(const std::string& objectType) const
  {
      PSList<std::string> retval;
      const NameValueVector& members = getMembers(LabelStr(objectType));
      for(std::vector< std::pair<LabelStr, LabelStr> >::const_iterator it = members.begin();
          it != members.end(); ++it)
      {
          retval.push_back((*it).second.toString());
      }
      return retval;
  }

  bool Schema::hasMember(const std::string& parentType, const std::string& memberName) const
  {
	  return hasMember(LabelStr(parentType), LabelStr(memberName));
  }

  bool Schema::hasMember(const LabelStr& parentType, const LabelStr& memberName) const{
    check_error(isType(parentType), parentType.toString() + " is undefined.");

    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(parentType);

    // If no hit, then try for the parent. There must be one since it is a valid
    // type but no hit yet
    if(membershipRelation_it == membershipRelation.end())
      return hasMember(getParent(parentType), memberName);

    const NameValueVector& members = membershipRelation_it->second;
    for(NameValueVector::const_iterator it = members.begin(); it != members.end(); ++it){
      const LabelStr& name = it->second;
      if(name == memberName) // Is the name equal to param
	return true;
    }

    // Recursive call if appropriate
    if(hasParent(parentType))
      return hasMember(getParent(parentType), memberName);

    // Otherwise, last act, see if it is built in
    return isPredicate(parentType) && getBuiltInVariableNames().find(memberName) != getBuiltInVariableNames().end();
  }

  const LabelStr Schema::getObjectTypeForPredicate(const LabelStr& predicate) const {
    check_error(isPredicate(predicate),
		"Predicate "+predicate.toString() +
		" is not defined, but we expect all predicates to be defined. See 'isPredicate'");
    return predicate.getElement(0, getDelimiter());
  }

  const std::vector<LabelStr>& Schema::getAllObjectTypes(const LabelStr& objectType) {
    std::map<edouble, std::vector<LabelStr> >::iterator it = allObjectTypes.find(objectType);
    if(it != allObjectTypes.end())
      return it->second;

    std::vector<LabelStr> results;
    results.clear();
    results.push_back(objectType);
    LabelStr lbl = objectType;
    while(hasParent(lbl)){
      lbl = getParent(lbl);
      results.push_back(lbl);
    }

    allObjectTypes.insert(std::pair<LabelStr, std::vector<LabelStr> >(objectType, results));

    return getAllObjectTypes(objectType);
  }

  bool Schema::hasParent(const LabelStr& type) const {

    if(m_hasParentCache.find(type) != m_hasParentCache.end())
      return true;

    bool result = false;

    if(isPrimitive(type) || isEnum(type)) // If it is a primitive, it has no parent
      result = false;
    else if(isObjectType(type)) // If an object type, then look it up in child relations
      result = childOfRelation.find(type) != childOfRelation.end();
    else {
      std::string predStr;
      if(makeParentPredicateString(type, predStr))
	result = predicates.find(LabelStr(predStr)) != predicates.end() || hasParent(predStr);
    }

    // If we get a true result, store it in the cache
    if(result)
      m_hasParentCache.insert(type);

    return result;
  }

  const LabelStr Schema::getParent(const LabelStr& type) const {
    check_error(hasParent(type), type.toString() + " does not have a parent.");

    // If it is an objectType. return child relation
    if(isObjectType(type))
      return childOfRelation.find(type)->second;

    // Otherwise it must be a predicate, so build the new fully qualified name
    std::string predStr;
    makeParentPredicateString(type, predStr);
    check_error(predStr != std::string(), "Attempted to get a parent predicate for " + type.toString() + " failed.");
    return predStr;
  }

  const LabelStrSet& Schema::getAllObjectTypes() const {
    return objectTypes;
  }


  const std::set<edouble>& Schema::getEnumValues(const LabelStr& enumName) const {
    check_error(isEnum(enumName), enumName.toString() + " is not a defined enumeration.");

    return enumValues.find(enumName)->second;

  }

  void Schema::getPredicates(const LabelStr& objectType, std::set<LabelStr>& results) const {
    check_error(isType(objectType), objectType.toString() + " is undefined");
    for(LabelStrSet::const_iterator pred = predicates.begin(); pred != predicates.end(); ++pred) {
      LabelStr predLbl(*pred);
      LabelStr object((predLbl).getElement(0, getDelimiter()));
      LabelStr predicate((predLbl).getElement(1, getDelimiter()));
      if ((object == objectType) || isA(objectType, object))
	results.insert(predicate);
    }
  }

  void Schema::getPredicates(std::set<LabelStr>& results) const {
    for(LabelStrSet::const_iterator it = predicates.begin(); it != predicates.end(); ++it)
      results.insert(*it);
  }

  bool Schema::hasPredicates(const LabelStr& objectType) {
    check_error(isType(objectType), objectType.toString() + " is undefined");

    // Try for a quick hit
    if(typesWithNoPredicates.find(objectType) != typesWithNoPredicates.end())
      return false;

    // Otherwise, it is not conclusive, so we try in detail
    for(LabelStrSet::const_iterator pred = predicates.begin(); pred != predicates.end(); ++pred) {
      LabelStr predLbl(*pred);
      LabelStr object((predLbl).getElement(0, getDelimiter()));
      if ((object == objectType) || isA(objectType, object))
	return true;
    }

    // It has no predicates, so cache and return result.
    typesWithNoPredicates.insert(objectType);
    return false;
  }

  const LabelStr Schema::getMemberType(const LabelStr& parentType, const LabelStr& memberName) const {
    check_error(hasMember(parentType, memberName),
		memberName.toString() + " is not a member of " + parentType.toString());

    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(parentType);

    // At this point we know if we do not have a hit, then try a parent
    if(membershipRelation_it == membershipRelation.end() && hasParent(parentType))
      return getMemberType(getParent(parentType), memberName);

    // Alternately, we have to have a hit
    const NameValueVector& members = membershipRelation_it->second;
    for(NameValueVector::const_iterator it = members.begin(); it != members.end(); ++it){
      const LabelStr& name = it->second;
      if(name == memberName) // Is the name equal to param
	return it->first;
    }

    // If we get to here, we should pursue the parent type (and it will have to have one).
    return getMemberType(getParent(parentType), memberName);
  }

  unsigned int Schema::getIndexFromName(const LabelStr& parentType, const LabelStr& memberName) const {
    check_error(hasMember(parentType, memberName),
		memberName.toString() + " is not a member of " + parentType.toString());

    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(parentType);

    // At this point we know if we do not have a hit, then try a parent
    if(membershipRelation_it == membershipRelation.end() && hasParent(parentType))
      return getIndexFromName(getParent(parentType), memberName);

    // Alternately, we have to have a hit
    const NameValueVector& members = membershipRelation_it->second;
    unsigned int index = 0;
    for(NameValueVector::const_iterator it = members.begin(); it != members.end(); ++it){
      const LabelStr& name = it->second;
      if(name == memberName) // Is the name equal to param
	return index;
      else
	index++;
    }

    // If we get to here, we should pursue the parent type (and it will have to have one).
    return getIndexFromName(getParent(parentType), memberName);
  }

  const LabelStr Schema::getNameFromIndex(const LabelStr& parentType, unsigned int index) const {
    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(parentType);

    // At this point we know if we do not have a hit, then try a parent
    if(membershipRelation_it == membershipRelation.end() && hasParent(parentType))
      return getNameFromIndex(getParent(parentType), index);

    // Alternately, we have to have a hit
    const NameValueVector& members = membershipRelation_it->second;
    unsigned int counter = 0;
    for(NameValueVector::const_iterator it = members.begin(); it != members.end(); ++it){
      if(counter == index) // Is the name equal to param
	return it->second;
      else
	counter++;
    }

    // If we get to here, we should pursue the parent type (and it will have to have one).
    checkError(hasParent(parentType),
	       parentType.toString() << " has no member with index " << index);

    return getNameFromIndex(getParent(parentType), index);
  }

  /**
   * @todo This may not be valid since a member name could in theory be duplicated
   * across enumerations. Look into this when we address enum scoping in a language
   */
  const LabelStr Schema::getEnumFromMember(const LabelStr& member) const {
    for(LabelStr_ValueSet_Map::const_iterator it = enumValues.begin();
        it != enumValues.end(); ++it)
      for(ValueSet::const_iterator memIt = it->second.begin(); memIt != it->second.end(); ++memIt)
        if((*memIt) == member)
          return it->first;

    check_error(ALWAYS_FAILS, member.toString() + " is not a member of any enumeration.");
    return LabelStr("error");
  }

  unsigned int Schema::getParameterCount(const LabelStr& predicate) const {
    check_error(isPredicate(predicate), predicate.toString() + " is not defined as a Predicate");
    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(predicate);

    check_error(membershipRelation_it != membershipRelation.end(), predicate.toString() + " not found in the membership relation");
    const NameValueVector& members = membershipRelation_it->second;

    return(members.size());
  }

  const LabelStr Schema::getParameterType(const LabelStr& predicate, unsigned int paramIndex) const {
    check_error(isPredicate(predicate), predicate.toString() + " is not defined as a Predicate");
#ifdef _MANAGED
    std::string tmp;
    System::String ^ tmpStr = paramIndex + " is not a valid index";
    MarshalString( tmpStr, tmp );
    check_error(paramIndex < getParameterCount(predicate), tmp);
#else
    checkError(paramIndex < getParameterCount(predicate), paramIndex << " is not a valid index");
#endif
    // First see if we get a hit for the parentType
    std::map<edouble, NameValueVector>::const_iterator membershipRelation_it =
      membershipRelation.find(predicate);

    check_error(membershipRelation_it != membershipRelation.end());
    const NameValueVector& members = membershipRelation_it->second;

    return(members[paramIndex].first);
  }

  void Schema::addPrimitive(const LabelStr& primitiveName){
    check_error(!isPrimitive(primitiveName), primitiveName.toString() + " is already defined.");
    debugMsg("Schema:addPrimitive", "[" << m_name.toString() << "] " << "Adding primitive type " << primitiveName.toString());
    primitives.insert(primitiveName);
  }

  void Schema::declareObjectType(const LabelStr& objectType) {
      if (!this->isObjectType(objectType)) {
          debugMsg("Schema:declareObjectType", "[" << m_name.toString() << "] " << "Declaring object type " << objectType.toString());
          objectTypes.insert(objectType);
          getCESchema()->registerDataType((new ObjectDT(objectType.c_str()))->getId());
      }
      else {
          debugMsg("Schema:declareObjectType", "[" << m_name.toString() << "] " << "Object type already declared, ignoring re-declaration for" << objectType.toString());
      }
  }

  void Schema::addObjectType(const LabelStr& objectType) {
    // Enforce assumption of a singly rooted class hierarchy
    addObjectType(objectType, rootObject());
  }

  void Schema::addObjectType(const LabelStr& objectType, const LabelStr& parent) {

    check_error(objectType.countElements(getDelimiter()) == 1,
                "ObjectType must not be delimited:" + objectType.toString());

    if (objectType != rootObject()) {
        checkError(isObjectType(parent), objectType.toString() + " has undefined parent class : " + parent.toString());
        checkError(childOfRelation.find(objectType) == childOfRelation.end(),objectType.toString() << " is already defined.");
        childOfRelation.insert(std::pair<LabelStr, LabelStr>(objectType, parent));
    }

    objectTypes.insert(objectType);
    membershipRelation.insert(std::pair<LabelStr, NameValueVector>(objectType, NameValueVector()));

    // Add type for constrained variables to be able to hold references to objects of the new type
    if (!getCESchema()->isDataType(objectType.c_str()))
        getCESchema()->registerDataType((new ObjectDT(objectType.c_str()))->getId());

    debugMsg("Schema:addObjectType",
	     "[" << m_name.toString() << "] " << "Added object type " << objectType.toString() << " that extends " <<
	     parent.toString());
  }

  void Schema::addPredicate(const LabelStr& predicate) {
    check_error(predicate.countElements(getDelimiter()) == 2,
		"Expect predicate names to be structured as <objectType>.<predicate>. Not found in "+
		predicate.toString());

    check_error(isObjectType(predicate.getElement(0, getDelimiter())),
		"Object Type not defined for " + predicate.toString() + ".");

    check_error(predicates.find(predicate) == predicates.end(), predicate.toString() + " already defined.");

    debugMsg("Schema:addPredicate",
	     "[" << m_name.toString() << "] " << "Added predicate " << predicate.toString());
    predicates.insert(predicate);
    membershipRelation.insert(std::pair<LabelStr, NameValueVector>(predicate, NameValueVector()));
  }

  /**
   * @todo memberType is not checked yet. It can be a class, enum, or primitive
   */
  unsigned int Schema::addMember(const LabelStr& parentType,
				 const LabelStr& memberType,
				 const LabelStr& memberName) {
    check_error(isType(parentType), parentType.toString() + " is undefined.");
    check_error(!canContain(parentType, memberType, memberName),
		parentType.toString() + " already contains " + memberName.toString());

    debugMsg("Schema:addMember",
	     "[" << m_name.toString() << "] " << "Added to " << parentType.toString() << ": " << memberType.toString() << " " <<
	     memberName.toString());
    // We can now assume the entry is present, so just add where appropriate
    NameValueVector& members = membershipRelation.find(parentType)->second;
    members.push_back(NameValuePair(memberType, memberName));
    return (members.size()-1);
  }

  void Schema::addEnum(const LabelStr& enumName) {
    check_error(!isEnum(enumName), enumName.toString() + " is already defined as an enumeration.");
    check_error(!isObjectType(enumName), enumName.toString() + " is already defined as an object type.");
    debugMsg("Schema:addEnum", "[" << m_name.toString() << "] " << "Added enumeration " << enumName.toString());
    enumValues.insert(std::pair<LabelStr, ValueSet>(enumName, ValueSet()));
  }

  void Schema::registerEnum(const char* enumName, const EnumeratedDomain& domain)
  {
      debugMsg("Schema:enumdef","Defining enum:" << enumName);

      addEnum(enumName);

      const std::set<edouble>& values = domain.getValues();
      for(std::set<edouble>::const_iterator it = values.begin();it != values.end();++it) {
          LabelStr newValue(*it);
          addValue(enumName, newValue);
      }

      getCESchema()->registerDataType(
          (new RestrictedDT(enumName,SymbolDT::instance(),domain))->getId()
      );

      debugMsg("Schema:enumdef"
              , "Created type factory " << enumName <<
              " with base domain " << domain.toString());
  }

  void Schema::addValue(const LabelStr& enumName, edouble enumValue) {
    check_error(isEnum(enumName), enumName.toString() + " is undefined.");
    check_error(enumValuesToEnums.find(enumValue) == enumValuesToEnums.end(),
            LabelStr(enumValue).toString() + " is already an enum value for " + (enumValuesToEnums[enumValue]).toString());

    debugMsg("Schema:addValue", "[" << m_name.toString() << "] " << "Added " <<
	     (LabelStr::isString(enumValue) ? LabelStr(enumValue).toString() : toString(enumValue)) << " to " <<
	     enumName.toString());
    ValueSet& members = enumValues.find(enumName)->second;
    members.insert(enumValue);
    enumValuesToEnums[enumValue] = enumName;
  }

  const LabelStr& Schema::getEnumForValue(edouble value) const
  {
    check_error(enumValuesToEnums.find(value) != enumValuesToEnums.end());
    return enumValuesToEnums.find(value)->second;
  }

  void Schema::write(ostream& os) const{
    os << "SCHEMA RULES:\n";
    for(LabelStr_LabelStrSet_Map::const_iterator it = objectPredicates.begin();
	it != objectPredicates.end(); ++it){
      LabelStr objectName = it->first;
      os << objectName.toString() << ":{";
      for (LabelStrSet::const_iterator pos = it->second.begin(); pos != it->second.end(); ++pos){
	LabelStr predicate = *pos;
	os << predicate.toString() << " ";
      }
      os << "}\n";
    }
  }

  bool Schema::makeParentPredicateString(const LabelStr& predicate, std::string& predStr) const{
    check_error(predicate.countElements(getDelimiter()) == 2,
		"Invalid format for predicate " + predicate.toString());

    LabelStr prefix = predicate.getElement(0, getDelimiter());

    // If not a defined class, or has no parent class, do no more and return false
    if(!isObjectType(prefix) || !hasParent(prefix))
      return false;

    // Otherwise we are ready to compose with the parent
    LabelStr suffix = predicate.getElement(1, getDelimiter());
    predStr = getParent(prefix).toString() + getDelimiter() + suffix.toString();
    return true;
  }

  void Schema::getEnumerations(std::list<LabelStr>& results) const {
    for(LabelStr_ValueSet_Map::const_iterator it = enumValues.begin();
        it != enumValues.end(); ++it)
      results.push_back(it->first);
  }

  const Id<ObjectFactory>& createDefaultObjectFactory(
          const ObjectTypeId& objType,
          bool canCreateObjects)
  {
      std::vector<std::string> constructorArgNames;
      std::vector<std::string> constructorArgTypes;
      std::vector<Expr*> constructorBody;
      ExprConstructorSuperCall* superCallExpr = NULL;

      // If it can't create objects, generate default super call
      if (!canCreateObjects)
          superCallExpr = new ExprConstructorSuperCall(objType->getParent()->getName(),std::vector<Expr*>());

      return (new InterpretedObjectFactory(
              objType,
              objType->getName(),
              constructorArgNames,
              constructorArgTypes,
              superCallExpr,
              constructorBody,
              canCreateObjects)
             )->getId();
  }

  void Schema::registerObjectType(const ObjectTypeId& objType)
  {
      const char* className = objType->getName().c_str();

      if (objType->getName() == Schema::rootObject())
          addObjectType(className);
      else
          addObjectType(className,objType->getParent()->getName().c_str());

      if (objType->getObjectFactories().size() == 0) {
          bool canCreateObjects = objType->isNative();
          objType->addObjectFactory(createDefaultObjectFactory(objType,canCreateObjects));
          debugMsg("Schema:registerObjectType","Generated default factory for object type:" << objType->getName().c_str());
      }

      m_objectTypeMgr->registerObjectType(objType);

      // Add type for constrained variables to be able to hold references to objects of the new type
      if (!getCESchema()->isDataType(className))
          getCESchema()->registerDataType((new ObjectDT(className))->getId());

      // TODO: all these need to go eventually (except for the registerTokenType call)
      {
          std::map<std::string,DataTypeId>::const_iterator it = objType->getMembers().begin();
          for(;it != objType->getMembers().end(); ++it)
              addMember(className, it->second->getName().toString() /*type*/, it->first/*name*/);
      }

      {
          std::map<edouble,TokenTypeId>::const_iterator it = objType->getTokenTypes().begin();
          for(;it != objType->getTokenTypes().end(); ++it) {
              const TokenTypeId& tokenType = it->second;
              LabelStr predName = tokenType->getSignature();

              addPredicate(predName.c_str());
              std::map<LabelStr,DataTypeId>::const_iterator paramIt = tokenType->getArgs().begin();
              for(;paramIt != tokenType->getArgs().end();++paramIt)
                  addMember(predName.c_str(), paramIt->second->getName() /*type*/, paramIt->first/*name*/);

              registerTokenType(it->second);
          }
      }

      debugMsg("Schema:registerObjectType","Registered object type:" << std::endl << objType->toString());
  }

  const ObjectTypeId& Schema::getObjectType(const LabelStr& objType)
  {
	  return m_objectTypeMgr->getObjectType(objType);
  }

  ObjectFactoryId Schema::getObjectFactory(const LabelStr& objectType, const std::vector<const Domain*>& arguments, const bool doCheckError)
  {
    return m_objectTypeMgr->getFactory(getId(),objectType,arguments,doCheckError);
  }

  void Schema::registerTokenType(const TokenTypeId& f)
  {
      m_tokenTypeMgr->registerType(f);
  }

  TokenTypeId Schema::getTokenType(const LabelStr& type)
  {
      return m_tokenTypeMgr->getType(getId(),type);
  }


  TokenTypeId Schema::getParentTokenType(const LabelStr& tokenType, const LabelStr& parentObjType)
  {
      LabelStr objType = parentObjType;
      std::string tokenName = tokenType.getElement(1, getDelimiter()).toString();

      for(;;) {
          std::string parentName = objType.toString()+getDelimiter()+tokenName;
          if (isPredicate(parentName))
              return getTokenType(parentName);
          if (hasParent(objType))
              objType = getParent(objType);
          else
              break;
      }

      return TokenTypeId::noId();
  }

  bool Schema::hasTokenTypes() const
  {
      return m_tokenTypeMgr->hasType();
  }


  void Schema::registerMethod(const MethodId& m)
  {
      // TODO: allow method overloading
      check_runtime_error(m_methods.find(m->getName()) == m_methods.end(), std::string("Method ")+m->getName().toString()+" already exists");
      m_methods[m->getName()] = m;
  }

  MethodId Schema::getMethod(const LabelStr& methodName, const DataTypeId& targetType, const std::vector<DataTypeId>& argTypes)
  {
      // TODO: use target type and arg types to resolve
      std::map<edouble,MethodId>::iterator it = m_methods.find(methodName);
      return (it != m_methods.end() ? it->second : MethodId::noId());
  }

  std::vector<TokenTypeId> Schema::getTypeSupporters( TokenTypeId type )
  {
    edouble key = type->getSignature().getKey();
    std::vector<TokenTypeId> retval;

    PSList<PSTokenType*> actionTypes = getPSTokenTypesByAttr( PSTokenType::ACTION );

    for( int i = 0; i < actionTypes.size(); i++){

      TokenType* tt = (TokenType*)actionTypes.get( i );
      PSList<PSTokenType*> effects = tt->getSubgoalsByAttr( PSTokenType::EFFECT);

      for ( int i = 0; i < effects.size(); i++ ) {
	TokenType* tt_effect = (TokenType*) effects.get(i);
	if( tt_effect->getSignature().getKey() == key)
	      retval.push_back( tt->getId() );
      }
    }

    return retval;

  }

  // PSSchema methods:
  PSList<std::string> Schema::getAllPredicates() const
   {
     PSList<std::string> retval;
     std::set<LabelStr> predicates;
     getPredicates(predicates);
     for(std::set<LabelStr>::const_iterator it = predicates.begin(); it != predicates.end(); ++it)
     {
    	 retval.push_back((*it).toString());
     }
     return retval;
   }

  PSList<PSObjectType*> Schema::getAllPSObjectTypes() const {
	PSList<PSObjectType*> retval;
	std::vector<ObjectTypeId> ots = m_objectTypeMgr->getAllObjectTypes();
    for(unsigned int i = 0;i<ots.size();i++)
    	retval.push_back(ots[i]);

    return retval;
  }

  /**
   * @brief Retrieve TokenTypes by searching in all ObjectTypes@
   */
  PSList<PSTokenType*>  Schema::getPSTokenTypesByAttr( int attrMask ) const
  {
    PSList<PSTokenType*> retval;
    std::vector<ObjectTypeId> ots = m_objectTypeMgr->getAllObjectTypes();
    for( unsigned i = 0; i < ots.size(); i++ ){
      PSList<PSTokenType*> tts = ots[i]->getPSTokenTypesByAttr( attrMask );
      for( int j = 0; j < tts.size(); j++ )
	retval.push_back( tts.get(j) );
    }

    return retval;

  }


} // namespace NDDL
