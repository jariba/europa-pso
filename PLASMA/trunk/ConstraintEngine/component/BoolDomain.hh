#ifndef _H_BoolDomain
#define _H_BoolDomain

/**
 * @file BoolDomain.hh
 * @author Conor McGann
 * @brief Declares a restriction to semantics of the IntervalDomain for integers only.
 */
#include "IntervalIntDomain.hh"

namespace EUROPA {

  /**
   * @class BoolDomain
   * @brief Imposes restrictions on the more generic super class.
   *
   * Restrictions are:
   * @li Always closed and so always finite.
   * @li All modification operations on the bounds must be checked to ensure they are integers.
   * @li Only values of 0 and 1 allowed.
   */
  class BoolDomain : public IntervalIntDomain {
  public:

    BoolDomain();

    BoolDomain(const char* typeName);

    BoolDomain(bool value);

    BoolDomain(bool value, const char* typeName);

    BoolDomain(const AbstractDomain& org);

    static const LabelStr& getDefaultTypeName();

    bool isFinite() const;

    bool isFalse() const;

    bool isTrue() const;

    bool isNumeric() const {
      return(false);
    }

    bool isBool() const {
      return(true);
    }

    /**
     * @brief Copy the concrete C++ object into new memory and return a pointer to it.
     */
    virtual BoolDomain *copy() const;

    /**
     * @brief COnvert to true or false as needed
     */
    LabelStr displayValue(double value) const;

    bool intersect(const AbstractDomain& dom);

    bool intersect(double lb, double ub);

  private:
    virtual void testPrecision(const double& value) const;
  };
}
#endif
