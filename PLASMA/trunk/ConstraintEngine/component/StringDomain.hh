#ifndef _H_StringDomain
#define _H_StringDomain

/**
 * @file StringDomain.hh
 * @author Andrew Bachmann
 * @brief Declares an enumerated domain of Strings
 */
#include "EnumeratedDomain.hh"
#include "LabelStr.hh"

namespace Prototype {

  /**
   * @class StringDomain
   * @brief an enumerated domain of strings
   */
  class StringDomain : public EnumeratedDomain {
  public:

    /**
     * @brief Constructs an initially empty and open domain
     */
    StringDomain(const LabelStr& typeName = getDefaultTypeName());

    /**
     * @brief Constructor.
     * @param values The initial set of values to populate the domain.
     * @param closed Indicate if the set is initially closed.
     * @param isNumeric Indicate if the set is to be used to store numeric or symbolic values
     * @param listener Allows connection of a listener to change events on the domain. 
     * @see AbstractDomain::isDynamic()
     */
    StringDomain(const std::list<double>& values, 
                 bool closed = true,
                 const DomainListenerId& listener = DomainListenerId::noId(),
                 const LabelStr& typeName = getDefaultTypeName());

    /**
     * @brief Constructor.
     * @param value Constructs a singleton domain. Closed on construction.
     * @param isNumeric Indicate if the set is to be used to store numeric or symbolic values
     * @param listener Allows connection of a listener to change events on the domain. 
     */
    StringDomain(double value,
                 const DomainListenerId& listener = DomainListenerId::noId(),
                 const LabelStr& typeName = getDefaultTypeName());

    /**
     * @brief Copy constructor.
     * @param org The source domain.
     */
    StringDomain(const StringDomain& org);

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
    virtual StringDomain *copy() const;

    void set(const AbstractDomain& dom) {
      std::list<double> values;
      dom.getValues(values);
      m_values.clear();
      std::list<double>::iterator it = values.begin();
      while (it != values.end()) {
        double value = *it++;
        check_error(LabelStr::isString(value));
        m_values.insert(value);
      }
      notifyChange(DomainListener::SET);
    }

    void set(double value) {
      check_error(LabelStr::isString(value));
      m_values.clear();
      m_values.insert(value);
      notifyChange(DomainListener::SET_TO_SINGLETON);
    }
  };

} // namespace Prototype

#endif // _H_StringDomain

