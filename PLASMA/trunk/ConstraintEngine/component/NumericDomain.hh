#ifndef _H_NumericDomain
#define _H_NumericDomain

/**
 * @file NumericDomain.hh
 * @author Conor McGann
 * @brief Declares an enumerated domain of numeric values
 */

#include "EnumeratedDomain.hh"
#include "LabelStr.hh"

namespace EUROPA {

  /**
   * @class NumericDomain
   * @brief an enumerated domain of numbers
   */
  class NumericDomain : public EnumeratedDomain {
  public:

    /**
     * @brief Constructs an initially empty and open domain, with a default type name
     */
    NumericDomain();

    /**
     * @breif Construct with name, open and empty
     */
    NumericDomain(const char* typeName);

    /**
     * @brief Constructs an initial singleton domain with the given type name
     */
    NumericDomain(double value, const char* typeName = getDefaultTypeName().c_str());

    /**
     * @brief Constructor.
     * @param values The initial set of values to populate the domain.
     * @param typeName
     * @see AbstractDomain::isDynamic()
     */
    NumericDomain(const std::list<double>& values, 
                 const char* typeName = getDefaultTypeName().c_str());

    /**
     * @brief Copy constructor.
     * @param org The source domain.
     */
    NumericDomain(const AbstractDomain& org);

    /**
     * @brief Get the default name of the type of the domain.
     * @see AbstractDomain::getTypeName
     */
    static const LabelStr& getDefaultTypeName();

    /**
     * @brief Copy the concrete C++ object into new memory and return a pointer to it.
     */
    virtual NumericDomain *copy() const;

    /**
     * @brief Sets the domain
     */
    void set(const NumericDomain& dom);

  };

} // namespace EUROPA

#endif
