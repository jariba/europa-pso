#ifndef _H_SymbolTypeFactory
#define _H_SymbolTypeFactory

#include "TypeFactory.hh"
#include "SymbolDomain.hh"

namespace Prototype {

  class SymbolTypeFactory : public ConcreteTypeFactory {
  public:
    SymbolTypeFactory(const LabelStr& name = SymbolDomain::getDefaultTypeName());

    /**
     * @brief Create a variable
     */
    virtual ConstrainedVariableId createVariable(const ConstraintEngineId& constraintEngine, 
                                                 const AbstractDomain& baseDomain,
                                                 bool canBeSpecified = true,
                                                 const LabelStr& name = ConstrainedVariable::NO_NAME(),
                                                 const EntityId& parent = EntityId::noId(),
                                                 int index = ConstrainedVariable::NO_INDEX) const;

    /**
     * @brief Return the base domain
     */
    virtual const AbstractDomain & baseDomain() const;

    /**
     * @brief Create a value for a string
     */
    virtual double createValue(std::string value) const;

  private:
    SymbolDomain m_baseDomain;
  };

} // namespace Prototype

#endif // _H_SymbolTypeFactory
