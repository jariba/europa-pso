/**
 * @file Schema.hh
 * @brief Introduces the interface for a Schema, which identifes the types and 
 * rules of a plan database,
 * @author Conor McGann, Andrew Bachmann
 */

#ifndef _H_Schema
#define _H_Schema

#include "PlanDatabaseDefs.hh"
#include "LabelStr.hh"
#include "AbstractDomain.hh"

#include <vector>

namespace EUROPA {

  /**
   * @class Schema
   * @brief Defines an interface for type checking information for the PlanDatabase.
   *
   * Schema is a singleton class since it inherits from DomainComparator, which is a singleton.
   * This singleton behavior is enforced in the constructor of the base class.
   *
   * @note Often, accessors which might appear to be immutable, are permitted to have non-const
   * interfaces to allow for the possibility of making incremental changes to cache for speed.
   * @see PlanDatabase
   */
  class Schema: public DomainComparator {
  public:
    /**
     * @brief Accessor for singleton instance, if present
     */
    static const SchemaId& instance(const LabelStr& name = LabelStr("Model"));

    /**
     * @brief Retrieve the delimiter for separating elements in schema element names.
     */
    static const char* getDelimiter();

    /**
     * @brief Creates a fully qualifiedName for a predicate
     * @see getDelimiter()
     */
    static const LabelStr makeQualifiedName(const LabelStr& objectType, 
					    const LabelStr& unqualifiedPredicateName);

    /**
     * @brief Accessor for the root object
     */
    static const LabelStr& rootObject();

    ~Schema();

    const SchemaId& getId() const;

    const LabelStr& getName() const;

    /**
     * @brief Implements the DomainComparator interface function
     * to over-ride the base type relationships to include the use
     * of classes and inheritance.
     */
    bool canCompare(const AbstractDomain& domx, const AbstractDomain& domy) const;

    /**
     * @brief Utility method to remove all schema details, excluding the model name
     */
    void reset();

    /**
     * @brief Tests if the given name is a defined objectType or predciate 
     */
    bool isType(const LabelStr& type) const;


    /**
     * @brief Tests if the given name is a recognized primitove type
     */
    bool isPrimitive(const LabelStr& str) const;

    /**
     * @brief Test if the given string is an enumerated type
     */
    bool isEnum(const LabelStr& str) const;

    /**
     * @brief Test if the given value is a member of the given enum
     * @param enumName The name of the enumeration
     * @param value The value to be tested.
     * @error !isEnum(enum)
     */
    bool isEnumValue(const LabelStr& enumName, double value) const;

    /**
     * @brief Determine of a given predicate is part of the schema
     */
    bool isPredicate(const LabelStr& type) const;

    /**
     * @brief Test if the given string is a class name
     */
    bool isObjectType(const LabelStr& type) const;

    /**
     * @brief Determine if a given predicate is compatible with a given object type.
     */
    bool canBeAssigned(const LabelStr& objectType, 
		       const LabelStr& predicate) const;

    /**
     * @brief Determine if a given composition of objects is allowed.
     * @param parentType the type of the composing object or predicate
     * @param memberType the type of the composed object
     * @param memberName the member name for the composed field.
     */
    bool canContain(const LabelStr& parentType, 
		    const LabelStr& memberType,
		    const LabelStr& memberName) const;

    /**
     * @brief Determines if the given member is contained in the parent
     * @param parentType the type of the composing object or predicate
     * @param memberName the member name for the composed field.
     */
    bool hasMember(const LabelStr& parentType, const LabelStr& memberName) const;

    /**
     * @brief Gets the type of a parents member.
     * @param parentType The parentType.
     * @param parameterName The parameter name.
     */
    const LabelStr getMemberType(const LabelStr& parentType,
					 const LabelStr& parameterName) const;

    /**
     * @brief Determine if one type is a sub type of another
     * @param descendant The candidate derived type. Must be a defined objectType.
     * @param ancestor The candidate ancestor type. Must be a defined objectType.
     * @see isObjectType
     */
    bool isA(const LabelStr& descendant, const LabelStr& ancestor) const;

    /**
     * @brief Tests if the given type has a parent.
     * @param objectType The objectType to test. Must be a valid type.
     * @return true if it does, otherwise false.
     * @see getParent, isA
     */
    bool hasParent(const LabelStr& objectType) const;

    /**
     * @brief Obtains the parent of a given type, if there is one. Will error out if it has no parent.
     * @param objectType the input type for which to obtain the parent
     * @see hasParent, isA
     */
    const LabelStr getParent(const LabelStr& objectType) const;

    /**
     * @brief Obtains all the Object Types in the Schema.  
     * @return a const ref to a set of LabelStr (each of which is the name of an  ObjectType)  
     */
    const std::set<LabelStr>& getAllObjectTypes() const;

    /**
     * @brief Obtains the set of all ObjectTypes that can be matched with the given object type.
     * @param objectType The objectType for which we want all matchable classes.
     * @return results All matchable classes in the schema. Includes objectType and all super classes.
     * @see hasParent, getParent
     */
    const std::vector<LabelStr>&  getAllObjectTypes(const LabelStr& objectType);

     /**
     * @brief Obtains the set of values for an enumeration.
     * @param enumName enumeration type name.  
     * @return a const ref to the set of values for enumName.  
     * @error !isEnum(enumName).
     */
    const std::set<double>& getEnumValues(const LabelStr& enumName) const;

    /**
     * @brief Obtain the set of predicates for a given object type.
     * @param objectType The ObjectType to use
     * @return a const ref to a set of LabelStr (each of which is the name of a Predicate)
     * @see addObjectType, isType
     * @error !isType(objecttype)
     */
    void getPredicates(const LabelStr& objectType, std::set<LabelStr>& results) const;

    /**
     * @brief Obtain the entire set of predicates.
     * @param results The set into which the predicates are put.
     */

    void getPredicates(std::set<LabelStr>& results) const;

    /**
     * @brief Test if an object type has any predicates
     */
    bool hasPredicates(const LabelStr& objectType);

    /**
     * @brief Helper method to compose a parent string
     * @param predicate The input predicate
     * @param predStr The result. Must be initially empty
     * @return true if it has a parent, othwerise false
     */
    bool makeParentPredicateString(const LabelStr& predicate, std::string& predStr) const;

    /**
     * @brief Obtain the object type for the given predicate.
     * @param predicate The predicate to use
     * @see addPredicate, isPredicateDefined
     * @error !isPredicateDefined(predicate)
     */
    const LabelStr getObjectType(const LabelStr& predicate) const;

    /**
     * @brief Gets the index of a named member in a types member list.
     * @param type the type to search
     * @param memberName the name of the member
     */
    unsigned int getIndexFromName(const LabelStr& type, const LabelStr& memberName) const;

    /**
     * @brief Gets the name of a member from a types member list.
     * @param type the type to search
     * @param index the index of the member
     */
    const LabelStr getNameFromIndex(const LabelStr& type, unsigned int index) const;

    /**
     * @brief Gets the type of the enumeration to which the given member belongs
     * @param member the member
     */
    const LabelStr getEnumFromMember(const LabelStr& member) const;

    /**
     * @brief Gets the number of parameters in a predicate
     * @param predicate the name of the predicate
     * @error !isPredicateDefined(predicate) 
     */
    unsigned int getParameterCount(const LabelStr& predicate) const;

    /**
     * @brief Gets the type of parameter at a particular index location in a predicate
     * @param predicate the name of the predicate
     * @param paramIndex the index of the parameter
     * @error !isPredicateDefined(predicate) 
     */
    const LabelStr getParameterType(const LabelStr& predicate, unsigned int paramIndex) const;


    /**
     * @brief Introduce a primitive type name to be used
     */
    void addPrimitive(const LabelStr& primitiveName);

    /**
     * @brief Declare an object type. The type can be referenced from now on.
     * I'ts the responsibility of the client to make sure that addObjectType will be called for this type eventually
     *  
     */
    void declareObjectType(const LabelStr& objectType);

    /**
     * @brief Add an object type. It must not be present already.
     */
    void addObjectType(const LabelStr& objectType);

    /**
     * @brief Add an object type as a derived type from the parent.
     */
    void addObjectType(const LabelStr& objectType,
                       const LabelStr& parent);

    /**
     * @brief Adds a predicate.
     * @param predicate The fully qualified name of the predicate. Must be of the form <prefix>.<suffix>
     * @error !isObjectType(prefix), isPredicateDefined(suffix)
     */
    void addPredicate(const LabelStr& predicate);

    /**
     * @brief Introduces a user defined enumeration type.
     * @param enumName The name of the enumeration.
     */
    void addEnum(const LabelStr& enumName);

    /**
     * @brief Indicates a composition of members
     * @param parentObjectType The type for the composing object
     * @param memberType The type for the composed member
     * @param memberName The name of the composed member
     * @return The indexed position of the parameter in the predicate list of parameters.
     */
    unsigned int addMember(const LabelStr& parentObjectType, 
				   const LabelStr& memberType,
				   const LabelStr& memberName);

    /**
     * @brief Add a member to a custom defined enumeration
     * @param enumName The name of the enumeration
     * @param enumValue The member to be added
     */
    void addValue(const LabelStr& enumName, double enumValue);

    /**
     * @brief Obtain a list of names of enumerations 
     * @param results a list of enumeration names
     */
    void getEnumerations(std::list<LabelStr>& results) const;

    /**
     * @brief Output contents to the given stream
     */
    void write (ostream& os) const;

  private:

    static const std::set<LabelStr>& getBuiltInVariableNames();

    /**
     * @brief Private constructor to avoid attempts to violate singleton pattern
     */
    Schema(const LabelStr& name);

    typedef std::set<double> ValueSet;
    typedef std::set<LabelStr> LabelStrSet;
    typedef std::pair<LabelStr, LabelStr> NameValuePair;
    typedef std::vector<NameValuePair> NameValueVector;
    typedef std::map<LabelStr, LabelStr> LabelStr_LabelStr_Map;
    typedef std::map<LabelStr, LabelStrSet > LabelStr_LabelStrSet_Map;
    typedef std::map<LabelStr, ValueSet > LabelStr_ValueSet_Map;
    typedef std::map<LabelStr,LabelStr_LabelStr_Map> LabelStr_LabelStrLabelStrMap_Map;

    SchemaId m_id;
    const LabelStr m_name;
    LabelStr_ValueSet_Map enumValues;
    LabelStrSet objectTypes;
    LabelStrSet predicates;
    LabelStrSet primitives;

    std::map<LabelStr, NameValueVector> membershipRelation; /*! All type compositions */
    std::map<LabelStr, LabelStr> childOfRelation; /*! Required to answer the getParent query */
    LabelStr_LabelStrSet_Map objectPredicates; /*! All predicates by object type */
    std::set<LabelStr> typesWithNoPredicates; /*! Cache for lookup efficiently */
    std::map<LabelStr, std::vector<LabelStr> > allObjectTypes; /*! Cache to retrieve allObjectTypes by sub-class */
    Schema(const Schema&); /**< NO IMPL */
  };

}
#endif
