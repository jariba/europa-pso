#include "IntervalIntDomain.hh"

namespace Prototype {

  IntervalIntDomain::IntervalIntDomain(const DomainListenerId& listener)
    :IntervalDomain(listener){}

  IntervalIntDomain::IntervalIntDomain(int lb, int ub, const DomainListenerId& listener)
    :IntervalDomain(lb, ub, listener){}

  IntervalIntDomain::IntervalIntDomain(int value, const DomainListenerId& listener)
    :IntervalDomain(value, listener){}

  IntervalIntDomain::IntervalIntDomain(const IntervalIntDomain& org)
    :IntervalDomain(org){}

  bool IntervalIntDomain::isFinite() const {
    check_error(!isDynamic());
    return (m_lb > -MAX_INT && m_ub < MAX_INT);
  }

  void IntervalIntDomain::testPrecision(const double& value) const {
    int intValue = (int) value;
    double dblValue =(double) intValue;
    check_error(dblValue == value) // confirms no loss in precision
  }

  double IntervalIntDomain::convert(const double& value) const{
    return (int) value;
  }

  const AbstractDomain::DomainType& IntervalIntDomain::getType() const{
    static const AbstractDomain::DomainType s_type = INT_INTERVAL;
    return s_type;
  }

  double IntervalIntDomain::minDelta() const {
    return 1;
  }

  double IntervalIntDomain::translateNumber(double number, bool asMin) const {
    double result = IntervalDomain::translateNumber(int(number), asMin);

    // If there has been no rounding, return result
    if(result == number)
      return result;
    else if (asMin && number > 0) // increment result for rounding up, instead of down
      result = result +1;

    return result;
  }
}
