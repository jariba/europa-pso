#include "BoolTypeFactory.hh"
#include "BoolDomain.hh"
#include "Variable.hh"

namespace Prototype {
  
  //
  // BoolTypeFactory
  //

  BoolTypeFactory::BoolTypeFactory() : ConcreteTypeFactory(BoolDomain().getTypeName()) {}

  ConstrainedVariableId
  BoolTypeFactory::createVariable(const ConstraintEngineId& constraintEngine, 
                                  const AbstractDomain& baseDomain,
                                  bool canBeSpecified = true,
                                  const LabelStr& name = ConstrainedVariable::NO_NAME(),
                                  const EntityId& parent = EntityId::noId(),
                                  int index = ConstrainedVariable::NO_INDEX) const
  {
    const BoolDomain * boolDomain = dynamic_cast<const BoolDomain*>(&baseDomain);
    check_error(boolDomain != NULL, "tried to create a BoolDomain variable with a different kind of base domain");
    Variable<BoolDomain> * variable
      = new Variable<BoolDomain>(constraintEngine, *boolDomain, canBeSpecified, name, parent, index);
    check_error(variable != NULL,
                "failed to create Variable for BoolDomain with name '" + name.toString() + "'");
    ConstrainedVariableId id = variable->getId();
    check_error(id.isValid());
    return id;
  }

  AbstractDomain *
  BoolTypeFactory::createDomain() const
  {
    BoolDomain * domain = new BoolDomain();
    check_error(domain != NULL, "failed to create BoolDomain");
    return domain;
  }

  double BoolTypeFactory::createValue(std::string value) const
  {
    if (value == "true") {
      return true;
    }
    if (value == "false") {
      return false;
    }
    check_error(ALWAYS_FAILS, "string value for boolean should be 'true' or 'false', not '" + value + "'");
    return -1;
  }

} // namespace Prototype
