#ifndef _H_SymbolDomain
#define _H_SymbolDomain

/**
 * @file SymbolDomain.hh
 * @author Andrew Bachmann
 * @brief Declares an enumerated domain of Symbols
 */
#include "EnumeratedDomain.hh"
#include "LabelStr.hh"

namespace Prototype {

  /**
   * @class SymbolDomain
   * @brief an enumerated domain of strings
   */
  class SymbolDomain : public EnumeratedDomain {
  public:

    /**
     * @brief Constructs an initially empty and open domain
     */
    SymbolDomain(const LabelStr& typeName = getDefaultTypeName());

    /**
     * @brief Constructor.
     * @param values The initial set of values to populate the domain.
     * @param closed Indicate if the set is initially closed.
     * @param isNumeric Indicate if the set is to be used to store numeric or symbolic values
     * @param listener Allows connection of a listener to change events on the domain. 
     * @see AbstractDomain::isDynamic()
     */
    SymbolDomain(const std::list<double>& values, 
                 bool closed = true,
                 const DomainListenerId& listener = DomainListenerId::noId(),
                 const LabelStr& typeName = getDefaultTypeName());

    /**
     * @brief Constructor.
     * @param value Constructs a singleton domain. Closed on construction.
     * @param isNumeric Indicate if the set is to be used to store numeric or symbolic values
     * @param listener Allows connection of a listener to change events on the domain. 
     */
    SymbolDomain(double value,
                 const DomainListenerId& listener = DomainListenerId::noId(),
                 const LabelStr& typeName = getDefaultTypeName());

    /**
     * @brief Copy constructor.
     * @param org The source domain.
     */
    SymbolDomain(const SymbolDomain& org);

    /**
     * @brief Get the type of the domain to aid in type checking.
     * @see AbstractDomain::DomainType
     */
    const DomainType& getType() const;

    /**
     * @brief Get the default name of the type of the domain.
     * @see AbstractDomain::getTypeName
     */
    static const LabelStr& getDefaultTypeName();

    /**
     * @brief Copy the concrete C++ object into new memory and return a pointer to it.
     */
    virtual SymbolDomain *copy() const;

  };

} // namespace Prototype

#endif // _H_SymbolDomain

