#ifndef _H_IntervalIntDomain
#define _H_IntervalIntDomain

/**
 * @file IntervalIntDomain.hh
 * @author Conor McGann
 * @brief Declares a restriction to semantics of the IntervalDomain for integers only.
 */
#include "IntervalDomain.hh"

namespace Prototype{

  /**
   * @class IntervalIntDomain
   * @brief Imposes restrictions on the more generic super class.
   *
   * Restrictions are:
   * @li If closed then it is finite.
   * @li All modification operations on the bounds must be checked to ensure they are integers
   */
  class IntervalIntDomain: public IntervalDomain {
  public:
    IntervalIntDomain(int lb = -MAX_INT, 
		      int ub = MAX_INT, 
		      const DomainListenerId& listener = DomainListenerId::noId());
    IntervalIntDomain(const DomainListenerId& listener);
    IntervalIntDomain(const IntervalIntDomain& org);

    bool isFinite() const;
    const DomainType& getType() const;

    /**
     * @brief Enforces integer semantics by restricting changes to units.
     */
    double minDelta() const;

  private:
    /**
     * @brief Enforces integer semantics. Will be compiled out for fast version.
     */
    void testPrecision(const double& value) const;

    /**
     * @brief Enforces integer semantics.
     */
    double convert(const double& value) const;
  };
}
#endif
