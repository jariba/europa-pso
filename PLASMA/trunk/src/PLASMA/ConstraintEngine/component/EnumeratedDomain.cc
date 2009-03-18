#include "EnumeratedDomain.hh"
#include "LabelStr.hh"
#include "Entity.hh"
#include "IntervalDomain.hh"
#include "BoolDomain.hh"

namespace EUROPA {

  bool isAscending(const std::set<double>& values) {
    double greatest = *(values.begin());
    for (std::set<double>::const_iterator it = values.begin(); it != values.end(); ++it) {
      double current = *it;
      if (current < greatest)
        return(false);
      else
        greatest = current;
    }
    return(true);
  }

  const LabelStr& EnumeratedDomain::getDefaultTypeName() {
    static const LabelStr sl_typeName("REAL_ENUMERATION");
    return(sl_typeName);
  }

  EnumeratedDomain::EnumeratedDomain(bool isNumeric,
                                     const char* typeName)
    : AbstractDomain(false, true, typeName), m_isNumeric(isNumeric), m_isString(false) {}

  EnumeratedDomain::EnumeratedDomain(const std::list<double>& values, bool isNumeric,
                                     const char* typeName)
    : AbstractDomain(false, true, typeName), m_isNumeric(isNumeric), m_isString(false) {
    for (std::list<double>::const_iterator it = values.begin(); it != values.end(); ++it)
      insert(*it);
    close();
  }

  EnumeratedDomain::EnumeratedDomain(double value,
                                     bool isNumeric,
                                     const char* typeName)
    : AbstractDomain(false, true, typeName), m_isNumeric(isNumeric), m_isString(false) {
    insert(value);
    close();
  }

  EnumeratedDomain::EnumeratedDomain(const AbstractDomain& org)
    : AbstractDomain(org) {
    check_error(org.isEnumerated(), 
                "Invalid source domain " + org.getTypeName().toString() + " for enumeration");
    const EnumeratedDomain& enumOrg = static_cast<const EnumeratedDomain&>(org);
    m_values = enumOrg.m_values;
    m_isNumeric = enumOrg.m_isNumeric;
    m_isString = enumOrg.m_isString;
  }

  bool EnumeratedDomain::isFinite() const {
    return(true); // Always finite, even if bounds are infinite, since there are always a finite number of values to select.
  }

  bool EnumeratedDomain::isNumeric() const {
    return(m_isNumeric);
  }

  bool EnumeratedDomain::isString() const {
      return m_isString;
  }


  bool EnumeratedDomain::isSingleton() const {
    return(m_values.size() == 1);
  }

  bool EnumeratedDomain::isEmpty() const {
    return(m_values.empty());
  }

  void EnumeratedDomain::empty() {
    m_values.clear();
    notifyChange(DomainListener::EMPTIED);
  }

  void EnumeratedDomain::close() {
    AbstractDomain::close();
    check_error(isEmpty() || isAscending(m_values));
  }

  unsigned int EnumeratedDomain::getSize() const {
    return(m_values.size());
  }

  void EnumeratedDomain::insert(double value) {
    check_error(check_value(value));
    checkError(isOpen(), "Cannot insert into a closed domain." << toString());
    std::set<double>::iterator it = m_values.begin();
    for ( ; it != m_values.end(); it++) {
      if (compareEqual(value, *it))
        return; // Already a member.
      if (value < *it) // Since members are sorted, value goes before *it.
        break;
    }
    m_values.insert(it, value);

    // CMG: Do not generate a relaxation for insertion into an open domain. The semantics of an open domain indicate that
    // the set of values is unbound, and we are now simply adding in another explicit member.
    // notifyChange(DomainListener::RELAXED);
  }

  void EnumeratedDomain::insert(const std::list<double>& values){
    for(std::list<double>::const_iterator it = values.begin(); it != values.end(); ++it)
      insert(*it);
  }

  void EnumeratedDomain::remove(double value) {
    check_error(check_value(value));
    std::set<double>::iterator it = m_values.begin();
    for ( ; it != m_values.end(); it++)
      if (compareEqual(value, *it))
        break;
    if (it == m_values.end())
      return; // not present: no-op
    m_values.erase(it);
    if (!isEmpty() || isOpen())
      notifyChange(DomainListener::VALUE_REMOVED);
    else
      notifyChange(DomainListener::EMPTIED);
  }

  void EnumeratedDomain::set(double value) {
    if(isOpen())
      close();

    if(isMember(value)){
      m_values.clear();
      m_values.insert(value);
      // Generate the notification, even if already a singleton. This is because setting a value to a singleton
      // is different from restricting it.
      notifyChange(DomainListener::SET_TO_SINGLETON);
    }
    else
      empty();
  }

  void EnumeratedDomain::reset(const AbstractDomain& dom) {
    if (*this != dom) {
      relax(dom);
      notifyChange(DomainListener::RESET);
    }
  }

  bool EnumeratedDomain::equate(AbstractDomain& dom) {
    safeComparison(*this, dom);

    // If both domains are closed enumerations we can use optimized method
    if(!dom.isInterval() && dom.isClosed() && isClosed())
      return equateClosedEnumerations(static_cast<EnumeratedDomain&>(dom));

    bool changed = dom.intersect(*this);

    if(changed && dom.isEmpty())
      return true;

    // Have to intersect again for the case of mixed types (enumeration and interval)
    if(intersect(dom) && !isEmpty())
      changed = dom.intersect(*this) || changed;

    return changed;
  }

  bool EnumeratedDomain::equateClosedEnumerations(EnumeratedDomain& dom){
    bool changed_a = false;
    bool changed_b = false;
    EnumeratedDomain& l_dom = static_cast<EnumeratedDomain&>(dom);

    std::set<double>::iterator it_a = m_values.begin();
    std::set<double>::iterator it_b = l_dom.m_values.begin();

    while (it_a != m_values.end() && it_b != l_dom.m_values.end()) {
      double val_a = *it_a;
      double val_b = *it_b;

      if (compareEqual(val_a, val_b)) {
	++it_a;
	++it_b;
      } else
	if (val_a < val_b) {
	  std::set<double>::iterator target = m_values.lower_bound(val_b);
	  m_values.erase(it_a, target);
	  it_a = target;
	  changed_a = true;
	  check_error(!isMember(val_a));
	} else {
	  std::set<double>::iterator target = l_dom.m_values.lower_bound(val_a);
	  l_dom.m_values.erase(it_b, target);
	  it_b = target;
	  changed_b = true;
	  check_error(!l_dom.isMember(val_b));
	}
    }

    if (it_a != m_values.end() && !l_dom.isEmpty()) {
      m_values.erase(it_a, m_values.end());
      changed_a = true;
      check_error(it_b == l_dom.m_values.end());
    } else
      if (it_b != l_dom.m_values.end() && !isEmpty()) {
	l_dom.m_values.erase(it_b, l_dom.m_values.end());
	changed_b = true;
	check_error(it_a == m_values.end());
      }

    if (changed_a) {
      if (isEmpty())
	notifyChange(DomainListener::EMPTIED);
      else
	if (isSingleton())
	  notifyChange(DomainListener::RESTRICT_TO_SINGLETON);
	else
	  notifyChange(DomainListener::VALUE_REMOVED);
    }

    if (changed_b) {
      if (l_dom.isEmpty())
	l_dom.notifyChange(DomainListener::EMPTIED);
      else
	if (isSingleton())
	  l_dom.notifyChange(DomainListener::RESTRICT_TO_SINGLETON);
	else
	  l_dom.notifyChange(DomainListener::VALUE_REMOVED);
    }

    check_error(!isEmpty() || ! dom.isEmpty());
    check_error(isEmpty() || dom.isEmpty() || (l_dom.m_values == m_values));
    return(changed_a || changed_b);
  }

  bool EnumeratedDomain::isMember(double value) const {
    if (m_values.empty())
      return false;
    std::set<double>::const_iterator it = m_values.lower_bound(value);
    // If we get a hit - the entry >= value
    if (it != m_values.end()) {
      double elem = *it;
      // Try fast compare first, then epsilon safe version
      if (value == elem || compareEqual(value, elem))
        return true;
			--it;
      // Before giving up, see if prior position is within epsilon
      return it != m_values.end() && compareEqual(value, *it);
    }
		return false;
  }


  bool EnumeratedDomain::convertToMemberValue(const std::string& strValue, double& dblValue) const {
    double value = dblValue;

    if(isNumeric())
      value = atof(strValue.c_str());
    else
      value = LabelStr(strValue);
    if(isMember(value)){
      dblValue = value;
      return true;
    }

    return false;
  }

  bool EnumeratedDomain::operator==(const AbstractDomain& dom) const {
    safeComparison(*this, dom);
    if (!dom.isEnumerated())
      return(dom.isFinite() &&
             getSize() == dom.getSize() &&
             isSubsetOf(dom));
    const EnumeratedDomain& l_dom = static_cast<const EnumeratedDomain&>(dom);
    if (!AbstractDomain::operator==(dom))
      return(false);
    // If any member of either is not a member of the other, they're not equal.
    // Since membership is not simple (due to minDelta()), this has to be done
    // via a scan of both memberships, one member at a time.
    std::set<double>::iterator it = m_values.begin();
    for ( ; it != m_values.end(); it++)
      if (!l_dom.isMember(*it))
        return(false);
    for (it = l_dom.m_values.begin(); it != l_dom.m_values.end(); it++)
      if (!isMember(*it))
        return(false);
    return(true);
  }

  bool EnumeratedDomain::operator!=(const AbstractDomain& dom) const {
    return(!operator==(dom));
  }

  void EnumeratedDomain::relax(const AbstractDomain& dom) {
    check_error(dom.isEnumerated());

    if(dom.isEmpty() && dom.isClosed())
      return;

    if (isEmpty() || this->isSubsetOf(dom)){
      const EnumeratedDomain& l_dom = static_cast<const EnumeratedDomain&>(dom);
      m_values = l_dom.m_values;
      // Open up if we are closed and need be be relaxed to an open domain
      if(dom.isOpen() && isClosed())
        open();

      notifyChange(DomainListener::RELAXED);
    }
  }

  void EnumeratedDomain::relax(double value) {
    checkError(isEmpty() || (isSingleton() && (getSingletonValue() == value)), toString());

    if (isEmpty()){
      m_values.insert(value);
      notifyChange(DomainListener::RELAXED);
    }
  }

  double EnumeratedDomain::getSingletonValue() const {
    checkError(isSingleton(), toString());
    return(*m_values.begin());
  }

  void EnumeratedDomain::getValues(std::list<double>& results) const {
    check_error(results.empty());
    check_error(isFinite());

    for (std::set<double>::iterator it = m_values.begin(); it != m_values.end(); ++it)
      results.push_back(*it);
  }

  const std::set<double>& EnumeratedDomain::getValues() const{
    return m_values;
  }

  double EnumeratedDomain::getUpperBound() const {
    double lb, ub;
    getBounds(lb, ub);
    return(ub);
  }

  double EnumeratedDomain::getLowerBound() const {
    double lb, ub;
    getBounds(lb, ub);
    return(lb);
  }

  bool EnumeratedDomain::getBounds(double& lb, double& ub) const {
    check_error(!isEmpty());
    lb = *m_values.begin();
    ub = *(--m_values.end());
    check_error(lb <= ub);
    return(!isNumeric() || lb == MINUS_INFINITY || ub == PLUS_INFINITY);
  }

  bool EnumeratedDomain::intersect(const AbstractDomain& dom) {
    safeComparison(*this, dom);

    // If this domain is open, and the new domain is closed, then assign all
    // values in the new domain to this domain.
    if(isOpen() && dom.isClosed()){
      checkError(!dom.isInterval(), "Cannot intersect a closed interval and and open enumeration.");
      close();
      const EnumeratedDomain& l_dom = static_cast<const EnumeratedDomain&>(dom);
      m_values = l_dom.m_values;
      return true;
    }


    bool changed = false;

    if (dom.isInterval()) {
      std::set<double>::iterator it = m_values.begin();
      while (it != m_values.end()) {
        double value = *it;
        if (!dom.isMember(value)) {
          changed = true;
          if (value > dom.getUpperBound()) {
            m_values.erase(it, m_values.end());
            break;
          } else
            m_values.erase(it++);
        } else {
          ++it;
        }
      }
    } else if (dom.isOpen())
      return false;
    else {
      const EnumeratedDomain& l_dom = static_cast<const EnumeratedDomain&>(dom);
      std::set<double>::iterator it_a = m_values.begin();
      std::set<double>::const_iterator it_b = l_dom.m_values.begin();

      while (it_a != m_values.end() && it_b != l_dom.m_values.end()) {
        double val_a = *it_a;
        double val_b = *it_b;

        if (compareEqual(val_a, val_b)) { // If they are equal, advance both
          ++it_a;
          ++it_b;
        } else
          if (val_a < val_b) { // A < B, so remove A and advance 
            m_values.erase(it_a++);
            changed = true;
            check_error(!isMember(val_a));
          } else
            ++it_b; // So just advance B
      }

      if (it_a != m_values.end()) {
        m_values.erase(it_a, m_values.end());
        changed = true;
      }
    }

    if (!changed)
      return(false);

    if (isEmpty())
      notifyChange(DomainListener::EMPTIED);
    else
      if (isSingleton())
        notifyChange(DomainListener::RESTRICT_TO_SINGLETON);
      else
        notifyChange(DomainListener::VALUE_REMOVED);

    return(true);
  }

  bool EnumeratedDomain::intersect(double lb, double ub){
    checkError(!isSymbolic(), "Cannot do bounds based intersection on symbolic domain " << toString());
    if(lb > ub){
      empty();
      return true;
    }

    // Allocate as an interval and delegate to existing method
    IntervalDomain intervalDomain(lb, ub, getTypeName().c_str());

    return intersect(intervalDomain);
  }

  bool EnumeratedDomain::difference(const AbstractDomain& dom) {
    safeComparison(*this, dom);

    // Trivial implementation, for all members of this domain that
    // are present in dom, remove them.
    bool value_removed = false;

    for (std::set<double>::iterator it = m_values.begin(); it != m_values.end();) {
      double value = *it;
      if (dom.isMember(value)) {
        m_values.erase(it++);
        value_removed = true;
      } else
        ++it;
    }

    if (m_values.empty())
      notifyChange(DomainListener::EMPTIED);
    else
      if (value_removed)
        notifyChange(DomainListener::VALUE_REMOVED);

    return(value_removed);
  }

  AbstractDomain& EnumeratedDomain::operator=(const AbstractDomain& dom) {
    safeComparison(*this, dom);
    check_error(m_listener.isNoId(), "Can only do direct assigment if not registered with a listener");
    const EnumeratedDomain& e_dom = static_cast<const EnumeratedDomain&>(dom);
    m_values = e_dom.m_values;
    return(*this);
  }

  bool EnumeratedDomain::isSubsetOf(const AbstractDomain& dom) const {
    safeComparison(*this, dom);

    // Always true if the given domain is open. Also never true if the given domain is closed
    // but this domain is open
    if(dom.isOpen())
      return true;
    else if(isOpen())
      return false;

    for (std::set<double>::const_iterator it = m_values.begin(); it != m_values.end(); ++it)
      if (!dom.isMember(*it))
        return(false);

    return(true);
  }

  bool EnumeratedDomain::intersects(const AbstractDomain& dom) const {
    if(dom.isOpen() || this->isOpen())
      return true;

    safeComparison(*this, dom);
    for (std::set<double>::const_iterator it = m_values.begin(); it != m_values.end(); ++it)
      if (dom.isMember(*it))
        return(true);
    return(false);
  }

  void EnumeratedDomain::operator>>(ostream&os) const {
    // Now commence output
    AbstractDomain::operator>>(os);
    os << "{";

    // First construct a lexicographic ordering for the set of values.
    std::set<std::string> orderedSet;

    std::string comma = "";
    for (std::set<double>::const_iterator it = m_values.begin(); it != m_values.end(); ++it) {
      double valueAsDouble = *it;
      std::string valueAsStr = toString(valueAsDouble);
      
      if (isNumeric()) {    	  
          os << comma << valueAsStr;
          comma = ", ";
      } 
      else  
          orderedSet.insert(valueAsStr);
    }

    for (std::set<std::string>::const_iterator it = orderedSet.begin(); it != orderedSet.end(); ++it) {
      check_error(!isNumeric());
      os << comma
         << *it;
      comma = ",";
    }

    os << "}";
  }
  
  std::string EnumeratedDomain::toString() const
  {
	  return AbstractDomain::toString();
  }

  std::string EnumeratedDomain::toString(double valueAsDouble) const
  {
      static const std::string sl_false("0");
	  static const std::string sl_true("1");

      if (isNumeric()) {
    	  std::ostringstream os; 
    	  os << valueAsDouble;
    	  return os.str(); 
      }
      else if (valueAsDouble == true)
          return sl_true;
      else if (valueAsDouble == false)
          return sl_false;
      else if (LabelStr::isString(valueAsDouble))
          return LabelStr(valueAsDouble).toString();
      else {
          EntityId entity(valueAsDouble);
          return entity->getName().toString();
      }	  
  }
  
  EnumeratedDomain *EnumeratedDomain::copy() const {
    EnumeratedDomain *ptr = new EnumeratedDomain(*this);
    check_error(ptr != 0);
    return(ptr);
  }

} /* namespace EUROPA */
