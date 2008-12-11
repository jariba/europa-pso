#include "EnumeratedTypeFactory.hh"
#include "EnumeratedDomain.hh"
#include "Variable.hh"

namespace EUROPA {
  
  EnumeratedTypeFactory::EnumeratedTypeFactory(const std::string& typeName, const std::string& elementName)
    : TypeFactory(typeName), m_elementName(elementName), m_baseDomain(false, typeName) {
  }

  EnumeratedTypeFactory::EnumeratedTypeFactory(const std::string& typeName, const std::string& elementName, const EnumeratedDomain& baseDomain)
    : TypeFactory(typeName), m_elementName(elementName), m_baseDomain(baseDomain) {
  }

  ConstrainedVariableId
  EnumeratedTypeFactory::createVariable(const ConstraintEngineId& constraintEngine, 
                                        const AbstractDomain& baseDomain,
                                        const bool internal,
                                        bool canBeSpecified,
                                        const std::string& name,
                                        const EntityId& parent,
                                        int index) const {
    const EnumeratedDomain * enumeratedDomain = dynamic_cast<const EnumeratedDomain*>(&baseDomain);
    check_error(enumeratedDomain != NULL, "tried to create a EnumeratedDomain variable with a different kind of base domain");
    Variable<EnumeratedDomain> * variable
      = new Variable<EnumeratedDomain>(constraintEngine, *enumeratedDomain, internal, canBeSpecified, name, parent, index);
    check_error(variable != NULL,
                "failed to create Variable for EnumeratedDomain with name '" + std::string(name) + "'");
    ConstrainedVariableId id = variable->getId();
    check_error(id.isValid());
    return(id);
  }

  const AbstractDomain &
  EnumeratedTypeFactory::baseDomain() const {
    return(m_baseDomain);
  }

  edouble EnumeratedTypeFactory::createValue(const std::string& value) const {
    if (m_baseDomain.isNumeric())
      return(atof(value.c_str()));
    return(LabelStr(value));
  }

} // namespace EUROPA
