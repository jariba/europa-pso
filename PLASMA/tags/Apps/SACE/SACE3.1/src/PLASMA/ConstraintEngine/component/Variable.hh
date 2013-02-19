#ifndef _H_AbstractVar
#define _H_AbstractVar

#include "Domain.hh"
#include "ConstrainedVariable.hh"
#include "ConstraintEngine.hh"
#include "Debug.hh"

/**
 * @file Variable.hh
 * @author Conor McGann
 * @date August, 2003
 * @brief Provides the Users (Planners) perspective for Variable Definition.
 *
 * This file introduces classes derived from ConstrainedVariable which provide an external view
 * of a variable and also implement the contract required by the ConstrainedVariable class. Handy for testing.
 * @see ConstrainedVariable
 */
namespace EUROPA {
  /**
   * @class Variable
   * @brief Template class to provide concrete variables of specific domain types.
   */
  template<class DomainType>
  class Variable : public ConstrainedVariable {
  public:

    /**
     * @brief Type specific constructor.
     * @param constraintEngine - the engine required by parent constructors.
     * @param baseDomain - the initial value for the domain and the maximum possible value.
     * @param parent owner if appropriate.
     * @param index position in parent collection.
     */
    Variable(const ConstraintEngineId& constraintEngine,
             const Domain& baseDomain,
             const bool internal = false,
             bool canBeSpecified = true,
             const LabelStr& name = ConstrainedVariable::NO_NAME(),
             const EntityId& parent = EntityId::noId(),
             int index = ConstrainedVariable::NO_INDEX);

    /**
     * Destructor.
     */
    virtual ~Variable();

    /**
     * @brief Return the domain first used in initialization.
     */
    const DomainType& getBaseDomain() const;

    /**
     * @brief Return the derived domain.
     * Return the domain resulting from constraint propagtion.
     * values.
     * @see getCurrentDomain()
     */
    const DomainType& getDerivedDomain();

    /**
     * @brief Return the current domain.
     * Return the last computed derived domain.
     * @see getCurrentDomain()
     */
    const DomainType& getLastDomain() const;


    /**
     * @brief Return the last computed derived domain.
     * @see ConstrainedVariable::lastDomain()
     */
    const Domain& lastDomain() const;

    /**
     * @brief Returns the derived domain.
     * @note Causes any needed propagation.
     * @see ConstrainedVariable::derivedDomain()
     */
    const Domain& derivedDomain();

    /**
     * @brief Retrieve the specified domain.
     */
    const Domain& baseDomain() const;

  protected:
    Domain& internal_baseDomain();
    virtual void handleRestrictBaseDomain(const Domain& baseDomain);

  private:
    Variable(const Variable&); // Prohibit compiler from generating copy constructor

    /**
     * @brief returns the current domain without checking for pending propagation first.
     * This method implements the required function for constraints to access the domain during
     * propagation.
     * @see lastDomain(), Constraint
     */
    Domain& getCurrentDomain();

  protected:
    DomainType* m_baseDomain; /**< The initial (and maximal, unless dynamic) set for the domain of this variable. */
    DomainType* m_derivedDomain; /**< The current domain of the variable based on user specifications and derived from
                                   constraint propagation. */
  };

  template<class DomainType>
  Variable<DomainType>::Variable(const ConstraintEngineId& constraintEngine,
                                 const Domain& baseDomain,
                                 const bool internal,
                                 bool canBeSpecified,
                                 const LabelStr& name,
                                 const EntityId& parent,
                                 int index)
    : ConstrainedVariable(constraintEngine, internal, canBeSpecified, name, parent, index),
    m_baseDomain(static_cast<DomainType*>(baseDomain.copy())),
    m_derivedDomain(static_cast<DomainType*>(baseDomain.copy())) {

	debugMsg("Variable:Variable", "Name " << name.toString());
    debugMsg("Variable:Variable", "Base Domain = " << baseDomain.toString());

    // Note that we permit the domain to be empty initially
    m_derivedDomain->setListener(m_listener);

    if(m_baseDomain->isSingleton()) {
    	debugMsg("Variable:Variable", "Base domain singleton; " << m_baseDomain->getSingletonValue());
    	if (m_baseDomain->isClosed() && !m_baseDomain->isEmpty())
    	    internalSpecify(m_baseDomain->getSingletonValue());
    }
  }

  template<class DomainType>
  Variable<DomainType>::~Variable() {
    debugMsg("Variable:~Variable", "Deleting " << getEntityName());
  	delete m_baseDomain;
  	delete m_derivedDomain;
  }

  template<class DomainType>
  const DomainType& Variable<DomainType>::getBaseDomain() const {
    return(*m_baseDomain);
  }

  template<class DomainType>
  const DomainType& Variable<DomainType>::getDerivedDomain() {
    if (!getConstraintEngine()->isPropagating() && pending())
      update();

    if (!provenInconsistent())
      return(*m_derivedDomain);

    static bool sl_initialized = false;
    static DomainType* sl_emptyDomain = 0;
    if (!sl_initialized) {
      sl_emptyDomain = static_cast<DomainType*>(m_derivedDomain->copy());
      if (sl_emptyDomain->isOpen())
        sl_emptyDomain->close();
      sl_emptyDomain->empty();
      sl_initialized = true;
    }
    return(*sl_emptyDomain);
  }

  template<class DomainType>
  Domain& Variable<DomainType>::getCurrentDomain() {
    check_error(validate());
    return(*m_derivedDomain);
  }

  template<class DomainType>
  const DomainType& Variable<DomainType>::getLastDomain() const {
    check_error(validate());
    return(*m_derivedDomain);
  }

  template<class DomainType>
  const Domain& Variable<DomainType>::lastDomain() const {
    check_error(validate());
    return(*m_derivedDomain);
  }
  template<class DomainType>
  const Domain& Variable<DomainType>::derivedDomain() {
    check_error(validate());
    return(getDerivedDomain());
  }

  template<class DomainType>
  const Domain& Variable<DomainType>::baseDomain() const {
    check_error(validate());
    return(*m_baseDomain);
  }

  template<class DomainType>
  void Variable<DomainType>::handleRestrictBaseDomain(const Domain& newBaseDomain) {
    check_error(validate());

    // For the case of the open domain, we will assign values. Also will assign closure. For the case
    // of a closed domain, just do intersection. In the event there is no restriction, we do nothing further.
    if(m_baseDomain->isOpen()){
      (*m_baseDomain) = newBaseDomain;
      if(newBaseDomain.isClosed())
    	  m_baseDomain->close();
    }
    else if(!m_baseDomain->intersect(newBaseDomain))
      return;

    // Apply restriction - force an event even if domain is unchanged
    m_derivedDomain->intersect(*m_baseDomain);

    // If a singleton, since it has changed, we have to set the value.
    if(m_baseDomain->isClosed() && m_baseDomain->isSingleton() && !isSpecified())
      internalSpecify(m_baseDomain->getSingletonValue());
  }

  template<class DomainType>
  Domain& Variable<DomainType>::internal_baseDomain() {
    return(*m_baseDomain);
  }
}
#endif
