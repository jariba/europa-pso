#include "StringTypeFactory.hh"
#include "StringDomain.hh"
#include "Variable.hh"

namespace Prototype {
  
  //
  // StringTypeFactory
  //

  StringTypeFactory::StringTypeFactory(const char* name)
   : ConcreteTypeFactory(name), m_baseDomain(name) {}

  ConstrainedVariableId
  StringTypeFactory::createVariable(const ConstraintEngineId& constraintEngine, 
                                    const AbstractDomain& baseDomain,
                                    bool canBeSpecified,
                                    const char* name,
                                    const EntityId& parent,
                                    int index) const
  {
    Variable<StringDomain> * variable
      = new Variable<StringDomain>(constraintEngine, baseDomain, canBeSpecified, name, parent, index);
    check_error(variable != NULL,
                "failed to create Variable for StringDomain with name '" + std::string(name) + "'");
    ConstrainedVariableId id = variable->getId();
    check_error(id.isValid());
    return id;
  }

  const AbstractDomain &
  StringTypeFactory::baseDomain() const
  {
    return m_baseDomain;
  }

  double StringTypeFactory::createValue(std::string value) const
  {
    return LabelStr(value);
  }

} // namespace Prototype
