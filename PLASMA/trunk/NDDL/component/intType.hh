#ifndef INT_TYPE_HH
#define INT_TYPE_HH

#include "IntervalIntDomain.hh"
#include "TypeFactory.hh"

namespace Prototype {

  /**
   * @class intDomain
   * @brief same as IntervalIntDomain, except with the NDDL-specific "int" type name.
   */
  class intDomain : public IntervalIntDomain {
  public:
    intDomain(const DomainListenerId& listener = DomainListenerId::noId());

    intDomain(int lb, int ub, 
    	      const DomainListenerId& listener = DomainListenerId::noId());

    intDomain(int value, 
    	      const DomainListenerId& listener = DomainListenerId::noId());

    intDomain(const intDomain& org);

    /**
     * @brief Get the name of the type of the domain.
     * @see AbstractDomain::getTypeName
     */
    virtual const LabelStr& getTypeName() const;
  };

  class intTypeFactory : public ConcreteTypeFactory {
  public:
    intTypeFactory();

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
     * @brief Create a domain
     */
    virtual AbstractDomain * createDomain() const;

    /**
     * @brief Create a value for a string
     */
    virtual double createValue(std::string value) const;

  };

} // namespace Prototype

#endif // INT_TYPE_HH
