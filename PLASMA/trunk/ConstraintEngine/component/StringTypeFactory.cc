#include "StringTypeFactory.hh"
#include "StringDomain.hh"
#include "Variable.hh"

namespace Prototype {
  
  //
  // StringTypeFactory
  //

  StringTypeFactory::StringTypeFactory(const LabelStr& name)
   : ConcreteTypeFactory(name), m_baseDomain(name)
  {
  }

  ConstrainedVariableId
  StringTypeFactory::createVariable(const ConstraintEngineId& constraintEngine, 
                                    const AbstractDomain& baseDomain,
                                    bool canBeSpecified,
                                    const LabelStr& name,
                                    const EntityId& parent,
                                    int index) const
  {
    const StringDomain * stringDomain = dynamic_cast<const StringDomain*>(&baseDomain);
    check_error(stringDomain != NULL, "tried to create a StringDomain variable with a different kind of base domain");
    Variable<StringDomain> * variable
      = new Variable<StringDomain>(constraintEngine, *stringDomain, canBeSpecified, name, parent, index);
    check_error(variable != NULL,
                "failed to create Variable for StringDomain with name '" + name.toString() + "'");
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
