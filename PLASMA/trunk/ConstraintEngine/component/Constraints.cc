#include "Constraints.hh"
#include "ConstraintEngine.hh"
#include "ConstrainedVariable.hh"
#include "IntervalIntDomain.hh"
#include "BoolDomain.hh"
#include "Domain.hh"
#include "Utils.hh"

namespace Prototype {

  /**
   * Utility class that might get promoted later.
   */
  class Infinity {
  public:
    static double plus(double n1, double n2, double defaultValue) {
      // Why cast to int and use abs()?  Why not just use fabs()? --wedgingt 2004 Feb 24
      if (abs((int)n1) == PLUS_INFINITY || abs((int)n2) == PLUS_INFINITY)
	return(defaultValue);
      return(n1 + n2);
    }

    static double minus(double n1, double n2, double defaultValue) {
      // Why cast to int and use abs()?  Why not just use fabs()? --wedgingt 2004 Feb 24
      if (abs((int)n1) == PLUS_INFINITY || abs((int)n2) == PLUS_INFINITY)
	return(defaultValue);
      return(n1 - n2);
    }
  };

  AddEqualConstraint::AddEqualConstraint(const LabelStr& name,
					 const LabelStr& propagatorName,
					 const ConstraintEngineId& constraintEngine,
					 const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables) {
    check_error(variables.size() == (unsigned int) ARG_COUNT);
    for (int i = 0; i < ARG_COUNT; i++)
      check_error(!getCurrentDomain(m_variables[i]).isEnumerated());
  }

  void AddEqualConstraint::handleExecute() {
    IntervalDomain& domx = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[X]));
    IntervalDomain& domy = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[Y]));
    IntervalDomain& domz = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[Z]));

    check_error(AbstractDomain::canBeCompared(domx, domy));
    check_error(AbstractDomain::canBeCompared(domx, domz));
    check_error(AbstractDomain::canBeCompared(domz, domy));

    // Test preconditions for continued execution.
    // Should this be part of canIgnore() instead? --wedgingt 2004 Feb 24
    if (domx.isDynamic() ||
        domy.isDynamic() ||
        domz.isDynamic())
      return;

    check_error(!domx.isEmpty() && !domy.isEmpty() && !domz.isEmpty());

    double xMin, xMax, yMin, yMax, zMin, zMax;
    domx.getBounds(xMin, xMax);
    domy.getBounds(yMin, yMax);
    domz.getBounds(zMin, zMax);

    // Process Z
    double xMax_plus_yMax = Infinity::plus(xMax, yMax, zMax);
    if (zMax > xMax_plus_yMax)
      zMax = domz.translateNumber(xMax_plus_yMax, false);

    double xMin_plus_yMin = Infinity::plus(xMin, yMin, zMin);
    if (zMin < xMin_plus_yMin)
      zMin = domz.translateNumber(xMin_plus_yMin, true);

    if (domz.intersect(zMin, zMax) && domz.isEmpty())
      return;

    // Process X
    double zMax_minus_yMin = Infinity::minus(zMax, yMin, xMax);
    if (xMax > zMax_minus_yMin)
      xMax = domx.translateNumber(zMax_minus_yMin, false);

    double zMin_minus_yMax = Infinity::minus(zMin, yMax, xMin);
    if (xMin < zMin_minus_yMax)
      xMin = domx.translateNumber(zMin_minus_yMax, true);

    if (domx.intersect(xMin, xMax) && domx.isEmpty())
      return;

    // Process Y
    double zMax_minus_xMin = Infinity::minus(zMax, xMin, yMax);
    if (yMax > zMax_minus_xMin)
      yMax = domy.translateNumber(zMax_minus_xMin, false);

    double zMin_minus_xMax = Infinity::minus(zMin, xMax, yMin);
    if (yMin < zMin_minus_xMax)
      yMin = domy.translateNumber(zMin_minus_xMax, true);

    if (domy.intersect(yMin,yMax) && domy.isEmpty())
      return;

    /* Now, rounding issues from mixed numeric types can lead to the
     * following inconsistency, not yet caught.  We handle it here
     * however, by emptying the domain if the invariant post-condition
     * is not satisfied. The motivating case for this: A:Int[-10,10] +
     * B:Int[-10,10] == C:Real[0.01, 0.99].
     */
    if (!domz.isMember(Infinity::plus(yMax, xMin, zMin)) ||
        !domz.isMember(Infinity::plus(yMin, xMax, zMin)))
      domz.empty();
  }

  EqualConstraint::EqualConstraint(const LabelStr& name,
				   const LabelStr& propagatorName,
				   const ConstraintEngineId& constraintEngine,
				   const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables) {
    check_error(variables.size() == (unsigned int) ARG_COUNT);

    // check the arguments - must both be enumerations or intervals.
    check_error((getCurrentDomain(m_variables[X]).isEnumerated()
                 == getCurrentDomain(m_variables[Y]).isEnumerated())
                || getCurrentDomain(m_variables[X]).isSingleton()
		|| getCurrentDomain(m_variables[Y]).isSingleton());
  }

  void EqualConstraint::handleExecute() {
    AbstractDomain& domx = getCurrentDomain(m_variables[X]);
    AbstractDomain& domy = getCurrentDomain(m_variables[Y]);

    // Why can't this be done in the constructor? --wedgingt 2004 Feb 23
    check_error(AbstractDomain::canBeCompared(domx, domy));

    // Discontinue if both domains are dynamic.
    if (domx.isDynamic() && domy.isDynamic())
      return;

    check_error(!domx.isEmpty() && !domy.isEmpty());
    domx.equate(domy);
    check_error(domx.isEmpty() || domy.isEmpty() || domx == domy);
  }

  AbstractDomain& EqualConstraint::getCurrentDomain(const ConstrainedVariableId& var) {
    return(Constraint::getCurrentDomain(var));
  }

  SubsetOfConstraint::SubsetOfConstraint(const LabelStr& name,
					 const LabelStr& propagatorName,
					 const ConstraintEngineId& constraintEngine,
					 const ConstrainedVariableId& variable,
					 const AbstractDomain& superset)
    : UnaryConstraint(name, propagatorName, constraintEngine, variable),
      m_isDirty(true),
      m_currentDomain(getCurrentDomain(variable)),
      m_executionCount(0) {
    check_error(superset.isDynamic() || !superset.isEmpty());

    // Why is this making the last call twice?
    // And why not use m_currentDomain, which has just been set?
    // --wedgingt 2004 Feb 24
    check_error(getCurrentDomain(variable).getType() == superset.getType() ||
		(getCurrentDomain(variable).isEnumerated() &&
                 getCurrentDomain(variable).isEnumerated()));

    if (m_currentDomain.isEnumerated())
      m_superSetDomain = new EnumeratedDomain((const EnumeratedDomain&) superset);
    else
      if (superset.getType() == AbstractDomain::INT_INTERVAL)
        m_superSetDomain = new IntervalIntDomain((const IntervalIntDomain&) superset);
      else
        if (superset.getType() == AbstractDomain::REAL_INTERVAL)
          m_superSetDomain = new IntervalDomain((const IntervalDomain&) superset);
        else
          if (superset.getType() == AbstractDomain::BOOL)
            m_superSetDomain = new BoolDomain((const BoolDomain&) superset);
          else
            assertTrue(false);
  }

  SubsetOfConstraint::~SubsetOfConstraint() {
    delete m_superSetDomain;
  }

  void SubsetOfConstraint::handleExecute() {
    // Why can't this be done in the constructor? --wedgingt 2004 Feb 24
    check_error(AbstractDomain::canBeCompared(m_currentDomain, *m_superSetDomain));

    if (m_currentDomain.isEnumerated())
      ((EnumeratedDomain&)m_currentDomain).intersect((const EnumeratedDomain&) *m_superSetDomain);
    else
      ((IntervalDomain&)m_currentDomain).intersect((const IntervalDomain&) *m_superSetDomain);
    m_isDirty = false;
    m_executionCount++;
  }

  bool SubsetOfConstraint::canIgnore(const ConstrainedVariableId& variable,
				     int argIndex,
				     const DomainListener::ChangeType& changeType) {
    check_error(argIndex == 0);
    return(changeType != DomainListener::RELAXED);
  }

  int SubsetOfConstraint::executionCount() const {
    return(m_executionCount);
  }

  const AbstractDomain& SubsetOfConstraint::getDomain() const {
    return(*m_superSetDomain);
  }

  LessThanEqualConstraint::LessThanEqualConstraint(const LabelStr& name,
						   const LabelStr& propagatorName,
						   const ConstraintEngineId& constraintEngine,
						   const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables) {
    check_error(variables.size() == (unsigned int) ARG_COUNT);
  }

  void LessThanEqualConstraint::handleExecute() {
    IntervalDomain& domx = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[X]));
    IntervalDomain& domy = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[Y]));

    check_error(AbstractDomain::canBeCompared(domx, domy));

    // Discontinue if either domain is dynamic.
    if (domx.isDynamic() || domy.isDynamic())
      return;

    check_error(!domx.isEmpty() && !domy.isEmpty());

    // Discontinue if any domain is enumerated but not a singleton.
    // Would not have to do this if enumerations were sorted. --wedgingt 2004 Feb 24
    if (domx.isEnumerated() && !domx.isSingleton())
      return;
    if (domy.isEnumerated() && !domy.isSingleton())
      return;

    // Restrict X to be no larger than Y's max
    if (domx.intersect(domx.getLowerBound(), domy.getUpperBound()) && domx.isEmpty())
      return;

    // Restrict Y to be at least X's min
    domy.intersect(domx.getLowerBound(), domy.getUpperBound());
  }

  bool LessThanEqualConstraint::canIgnore(const ConstrainedVariableId& variable,
					  int argIndex,
					  const DomainListener::ChangeType& changeType) {
    return((argIndex == X &&
	    (changeType == DomainListener::UPPER_BOUND_DECREASED)) ||
	   (argIndex == Y &&
	    (changeType == DomainListener::LOWER_BOUND_INCREASED)));
  }

  NotEqualConstraint::NotEqualConstraint(const LabelStr& name,
					 const LabelStr& propagatorName,
					 const ConstraintEngineId& constraintEngine,
					 const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables) {
    check_error(variables.size() == (unsigned int) ARG_COUNT);

    // Check the arguments - must both be enumerations, since we don't
    // implement splitting of intervals.
    // This is pointlessly restrictive; shouldn't the Domain class
    // decide whether it splits intervals or not?
    // --wedgingt@ptolemy.arc.nasa.gov 2004 Feb 12
    check_error(getCurrentDomain(m_variables[X]).isEnumerated() && getCurrentDomain(m_variables[Y]).isEnumerated());
  }

  void NotEqualConstraint::handleExecute() {
    AbstractDomain& domx = getCurrentDomain(m_variables[X]);
    AbstractDomain& domy = getCurrentDomain(m_variables[Y]);

    check_error(AbstractDomain::canBeCompared(domx, domy));

    // Discontinue if either domain is dynamic
    if (domx.isDynamic() || domy.isDynamic())
      return;

    check_error(!domx.isEmpty() && !domy.isEmpty());

    if (domx.isSingleton() && domy.isMember(domx.getSingletonValue()))
      domy.remove(domx.getSingletonValue());
    else if (domy.isSingleton() && domx.isMember(domy.getSingletonValue()))
      domx.remove(domy.getSingletonValue());
  }

  bool NotEqualConstraint::canIgnore(const ConstrainedVariableId& variable,
				     int argIndex,
				     const DomainListener::ChangeType& changeType) {
    // If it is a restriction, but not a singleton, then we can ignore it.
    // Can't anything except a restriction to singleton be ignored? --wedgingt 2004 Feb 24
    // And even that if the domains were disjoint previously?
    // But there is the odd case of a relax or reset of one var that includes
    // adding the other var's singleton value. --wedgingt 2004 Mar 4
    if (changeType != DomainListener::RESET && changeType != DomainListener::RELAXED)
      return(!getCurrentDomain(variable).isSingleton());
    return(false);
  }

  MultEqualConstraint::MultEqualConstraint(const LabelStr& name,
                                           const LabelStr& propagatorName,
                                           const ConstraintEngineId& constraintEngine,
                                           const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables) {
    check_error(variables.size() == (unsigned int) ARG_COUNT);
    for (int i = 0; i < ARG_COUNT; i++)
      check_error(!getCurrentDomain(m_variables[i]).isEnumerated());
  }

  /**
   * @brief Return larger argument.
   * @note Shouldn't be here, but in a generic "arithmetic" class or system library.
   * @note max() is a macro in some compiler implementations. --wedgingt 2004 Feb 26
   */
  double max(double a, double b) {
    return (a > b ? a : b);
  }

  /**
   * @brief Return smaller argument.
   * @note Shouldn't be here, but in a generic "arithmetic" class or system library.
   * @note min() is a macro in some compiler implementations. --wedgingt 2004 Feb 26
   */
  double min(double a, double b) {
    return(a < b ? a : b);
  }

  /**
   * @brief Helper method to compute new bounds for both X and Y in X*Y == Z.
   */
  bool updateMinAndMax(IntervalDomain& targetDomain,
		       double denomMin,
		       double denomMax,
		       double numMin,
		       double numMax) {
    double xMax = targetDomain.getUpperBound();
    double xMin = targetDomain.getLowerBound();
    double newMin = xMax;
    double newMax = xMin;

    // Shouldn't this also check for 0 being between denomMin and denomMax? --wedgingt 2004 Feb 24
    if (denomMin == 0 || denomMax == 0) {
      // If the denominators are 0, we know the results are infinite.
      if (numMax > 0)
        newMax = PLUS_INFINITY;
      if (numMin < 0)
	newMin = MINUS_INFINITY;
    } else {
      // Otherwise we must examine min and max of various permutations in order to handle signs correctly.
      if (denomMin != 0) {
	newMax = max(newMax, max(numMax / denomMin, numMin / denomMin));
	newMin = min(newMin, min(numMax / denomMin, numMin / denomMin));
      }
      if (denomMax != 0) {
	newMax = max(newMax, max(numMax / denomMax, numMin/ denomMax));
	newMin = min(newMin, min(numMax / denomMax, numMin/ denomMax));
      }
    }

    if (xMax > newMax)
      xMax = targetDomain.translateNumber(newMax, false);
    if (xMin < newMin)
      xMin = targetDomain.translateNumber(newMin, true);

    if (targetDomain.intersect(xMin, xMax) && targetDomain.isEmpty())
      return(false);
    return(true);
  }

  void MultEqualConstraint::handleExecute() {
    IntervalDomain& domx = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[X]));
    IntervalDomain& domy = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[Y]));
    IntervalDomain& domz = static_cast<IntervalDomain&>(getCurrentDomain(m_variables[Z]));

    check_error(AbstractDomain::canBeCompared(domx, domy));
    check_error(AbstractDomain::canBeCompared(domx, domz));
    check_error(AbstractDomain::canBeCompared(domz, domy));

    /* Test preconditions for continued execution */
    if (domx.isDynamic() ||
        domy.isDynamic() ||
        domz.isDynamic())
      return;

    check_error(!domx.isEmpty() && !domy.isEmpty() && !domz.isEmpty());

    double xMin;
    double xMax;
    double yMin;
    double yMax;
    double zMin;
    double zMax;

    domx.getBounds(xMin, xMax);
    domy.getBounds(yMin, yMax);
    domz.getBounds(zMin, zMax);

    // Process Z
    double max_z = max(max(xMax * yMax, xMin * yMin), max(xMin * yMax, xMax * yMin));
    if (zMax > max_z)
      zMax = domz.translateNumber(max_z, false);

    double min_z = min(min(xMax * yMax, xMin * yMin), min(xMin * yMax, xMax * yMin));
    if (zMin < min_z)
      zMin = domz.translateNumber(min_z, true);

    if (domz.intersect(zMin, zMax) && domz.isEmpty())
      return;

    // Process X
    if (!updateMinAndMax(domx, yMin, yMax, zMin, zMax))
      return;

    // Process Y
    updateMinAndMax(domy, xMin, xMax, zMin, zMax);
  }

  /**
   * @class AddMultEqualConstraint
   * @brief A + (B*C) == D
   */
  AddMultEqualConstraint::AddMultEqualConstraint(const LabelStr& name,
                                                 const LabelStr& propagatorName,
                                                 const ConstraintEngineId& constraintEngine,
                                                 const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables),
      m_interimVariable(constraintEngine, IntervalDomain(), false, LabelStr("InternalConstraintVariable"), getId()),
      m_multEqualConstraint(LabelStr("Internal::multEqual"), propagatorName, constraintEngine,
			    makeScope(m_variables[B], m_variables[C], m_interimVariable.getId())),
      m_addEqualConstraint(LabelStr("Internal:addEqual"), propagatorName, constraintEngine,
			   makeScope(m_interimVariable.getId(), m_variables[A], m_variables[D])) {
    check_error(m_variables.size() == (unsigned int) ARG_COUNT);
  }

  /**
   * @class EqSumConstraint
   * @brief A = B + C where B and C can each be sums.
   * Converted into an AddEqualConstraint and/or two EqSumConstraints with fewer variables.
   */
  EqualSumConstraint::EqualSumConstraint(const LabelStr& name,
                                         const LabelStr& propagatorName,
                                         const ConstraintEngineId& constraintEngine,
                                         const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables),
      ARG_COUNT(variables.size()),
      m_sum1(constraintEngine, IntervalDomain(), false, LabelStr("InternalEqSumVariable"), getId()),
      m_sum2(constraintEngine, IntervalDomain(), false, LabelStr("InternalEqSumVariable"), getId()),
      m_sum3(constraintEngine, IntervalDomain(), false, LabelStr("InternalEqSumVariable"), getId()),
      m_sum4(constraintEngine, IntervalDomain(), false, LabelStr("InternalEqSumVariable"), getId()) {
    check_error(ARG_COUNT > 2 && ARG_COUNT == (unsigned int)m_variables.size());
    std::vector<ConstrainedVariableId> scope;
    // B is always first and C is always second for the first set, so:
    scope.push_back(m_variables[1]); // B + ...
    scope.push_back(m_variables[2]); // ... C ...
    switch (ARG_COUNT) {
    case 3: // A = B + C
      scope.push_back(m_variables[0]); // ... = A
      m_eqSumC1 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
      break;
    case 4: // A = (B + C) + D
      scope.push_back(m_sum1.getId()); // ... = (B + C)
      m_eqSumC1 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
      scope.clear();
      scope.push_back(m_sum1.getId()); // (B + C) ...
      scope.push_back(m_variables[3]); // ... + D = ...
      scope.push_back(m_variables[0]); // ... A
      m_eqSumC2 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
      break;
    case 5: case 6: case 7:
      // 5: A = (B + C) + (D + E)
      // 6: A = (B + C) + (D + E + F)
      // 7: A = (B + C) + (D + E + F + G)
      // So, do (B + C) and (D + E ...) for all three:
      scope.push_back(m_sum1.getId()); // (B + C)
      m_eqSumC1 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
      scope.clear();
      scope.push_back(m_sum1.getId()); // (B + C) + ...
      scope.push_back(m_sum2.getId()); // (D + E ...) = ...
      scope.push_back(m_variables[0]); // A
      m_eqSumC2 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
      scope.clear();
      scope.push_back(m_variables[3]); // D + ...
      scope.push_back(m_variables[4]); // E ...
      switch (ARG_COUNT) {
      case 5:
        scope.push_back(m_sum2.getId()); // ... = (D + E)
        m_eqSumC3 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        break;
      case 6:
        scope.push_back(m_sum3.getId()); // ... = (D + E)
        m_eqSumC3 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        scope.clear();
        scope.push_back(m_sum3.getId()); // (D + E) + ...
        scope.push_back(m_variables[5]); // ... F = ...
        scope.push_back(m_sum2.getId()); // ... (D + E + F)
        m_eqSumC4 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        break;
      case 7:
        scope.push_back(m_sum3.getId()); // ... = (D + E)
        m_eqSumC3 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        scope.clear();
        scope.push_back(m_sum3.getId()); // (D + E) + ...
        scope.push_back(m_sum4.getId()); // ... (F + G) = ...
        scope.push_back(m_sum2.getId()); // (D + E + F + G)
        m_eqSumC4 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        scope.clear();
        scope.push_back(m_variables[5]); // F + ...
        scope.push_back(m_variables[6]); // ... G = ...
        scope.push_back(m_sum4.getId()); // ... (F + G)
        m_eqSumC5 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        break;
      default:
        check_error(ALWAYS_FAILS);
        break;
      } /* switch (ARGCOUNT) 5, 6, 7 */
      break;
    default:
      { // A = first_half + second_half, recursively
        check_error(ARG_COUNT > 7);
        scope.clear(); // Was B + C for first set: those that only call AddEqual
        scope.push_back(m_sum1.getId()); // first_half + ...
        scope.push_back(m_sum2.getId()); // ... second_half = ...
        scope.push_back(m_variables[0]); // ... A
        m_eqSumC1 = (new AddEqualConstraint(LabelStr("AddEqual"), propagatorName, constraintEngine, scope))->getId();
        scope.clear();
        scope.push_back(m_sum1.getId()); // first_half = ...
        unsigned int half = ARG_COUNT/2;
        unsigned int i = 1;
        for ( ; i <= half; i++)
          scope.push_back(m_variables[i]); // ... X + ...
        m_eqSumC2 = (new EqualSumConstraint(LabelStr("EqualSum"), propagatorName, constraintEngine, scope))->getId();
        scope.clear();
        scope.push_back(m_sum2.getId()); // second_half = ...
        for ( ; i < ARG_COUNT; i++)
          scope.push_back(m_variables[i]); // ... Y + ...
        m_eqSumC3 = (new EqualSumConstraint(LabelStr("EqualSum"), propagatorName, constraintEngine, scope))->getId();
        break;
      }
      break;
    }
  }

  EqualSumConstraint::~EqualSumConstraint() {
    // Have to remove these before the variables they refer to
    //   and there's no other way to force the compiler to do
    //   these first. --wedgingt 2004 Feb 27
    if (!m_eqSumC5.isNoId())
      delete (Constraint*) m_eqSumC5;
    if (!m_eqSumC4.isNoId())
      delete (Constraint*) m_eqSumC4;
    if (!m_eqSumC3.isNoId())
      delete (Constraint*) m_eqSumC3;
    if (!m_eqSumC2.isNoId())
      delete (Constraint*) m_eqSumC2;
    if (!m_eqSumC1.isNoId())
      delete (Constraint*) m_eqSumC1;
  }

  LessOrEqThanSumConstraint::LessOrEqThanSumConstraint(const LabelStr& name,
                                                       const LabelStr& propagatorName,
                                                       const ConstraintEngineId& constraintEngine,
                                                       const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables),
      m_interimVariable(constraintEngine, IntervalDomain(), false, LabelStr("InternalConstraintVariable"), getId()),
      m_lessThanEqualConstraint(LabelStr("Internal::lessThanEqual"), propagatorName, constraintEngine,
                                makeScope(m_variables[X], m_interimVariable.getId())),
      m_addEqualConstraint(LabelStr("Internal:addEqual"), propagatorName, constraintEngine,
			   makeScope(m_variables[Y], m_variables[Z], m_interimVariable.getId())) {
    check_error(m_variables.size() == (unsigned int) ARG_COUNT);
  }

  CondAllSameConstraint::CondAllSameConstraint(const LabelStr& name,
                                               const LabelStr& propagatorName,
                                               const ConstraintEngineId& constraintEngine,
                                               const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables),
      ARG_COUNT(variables.size()) {
    check_error(ARG_COUNT > 2);
    check_error(getCurrentDomain(m_variables[0]).getType() == AbstractDomain::BOOL);
    for (unsigned int i = 2; i < ARG_COUNT; i++) {
      check_error(AbstractDomain::canBeCompared(getCurrentDomain(m_variables[1]),
                                                getCurrentDomain(m_variables[i])));
      // If this second condition is not enforced, the value used for
      // minDelta() in handleExecute() depends on the order of the
      // variables within the scope.  It should, in fact, probably be
      // enforced by AbstractDomain::canBeCompared().  Another
      // possibility would be to change the '==' here to '<=' ...
      check_error(getCurrentDomain(m_variables[1]).minDelta() ==
                  getCurrentDomain(m_variables[i]).minDelta());
    }
  }

  void CondAllSameConstraint::handleExecute() {
    BoolDomain& boolDom = static_cast<BoolDomain&>(getCurrentDomain(m_variables[0]));
    check_error(!boolDom.isDynamic());
    AbstractDomain& dom1 = getCurrentDomain(m_variables[1]);

    if (!boolDom.isSingleton()) {
      // Condition is not singleton, so try to restrict it:
      // A. If all of the others are singleton and equal, the condition is true.
      // B. If the others have an empty intersection, the condition is false.
      // As with singleton false case, we can do nothing if any are dynamic.
      bool canProveTrue = true;
      AbstractDomain* common = 0;
      double single;
      for (unsigned int i = 1; !boolDom.isSingleton() && i < ARG_COUNT; i++) {
        AbstractDomain& current(getCurrentDomain(m_variables[i]));
        if (current.isDynamic()) {
          canProveTrue = false;
          continue;
        }
        if (!current.isSingleton()) {
          canProveTrue = false;
          if (common == 0) {
            // First one: copy it to intersect with later ones.
            common = current.copy();

            // Skipping this check merely results in less efficient propagation.
            // check_error(common != 0);

          } else {
            // Intersect common with this one; if now empty, the other
            // variables cannot all have the same value, so remove true
            // from the condition.
            if (common->intersect(current) && common->isEmpty())
              boolDom.remove(true);
          }
          continue;
        } /* if !current.isSingleton() */
        if (canProveTrue) {
          if (i == 1)
            single = current.getSingletonValue();
          else
            if (fabs(single - current.getSingletonValue()) > getCurrentDomain(m_variables[1]).minDelta()) {
              // Two singletons with different values: can't be all same, so:
              canProveTrue = false;
              boolDom.remove(true);
            }
        } /* if canProveTrue */
      } /* for i = 1; !boolDom.isSingleton && i < ARG_COUNT; i++ */
      if (canProveTrue)
        boolDom.remove(false);
      // Before it goes out of scope:
      if (common != 0)
        delete common;
    } /* if !boolDom.isSingleton */

    // Whether the condition was singleton on entry to this function
    // or became singleton just above, propagate the effects of that
    // to the other variables in the scope.
    if (boolDom.isSingleton()) {
      if (!boolDom.getSingletonValue()) {
        // Singleton false: ensure at least one other can be a value
        // different than another var.  Unlike the singleton true
        // case, if any are dynamic, we can do nothing.

        // If there's only one non-singleton variable and all of the
        // singletons have the same value, that singleton value could
        // be trimmed from the non-singleton's domain, but that is
        // relatively expensive to check for and can eliminate at most
        // one member from one variable's domain.

        AbstractDomain& common = getCurrentDomain(m_variables[1]);
        if (common.isDynamic() || !common.isSingleton())
          return; // Can ignore relax events until condition var is relaxed.
        double single = common.getSingletonValue();
        for (unsigned int i = 2; i < ARG_COUNT; i++)
          if (getCurrentDomain(m_variables[i]).isDynamic()
              || !getCurrentDomain(m_variables[i]).isSingleton()
              || getCurrentDomain(m_variables[i]).getSingletonValue() != single) {
            return; // Can ignore relax events until condition var is relaxed.
          }
        // All are singleton and identical to single, so provoke an inconsistency:
        boolDom.remove(false);
        return;
      } else {
        // Singleton true: force all other vars in scope to be equated if _any_ of them are not dynamic.
        unsigned int i = 1;
        for ( ; i < ARG_COUNT; i++)
          if (!getCurrentDomain(m_variables[i]).isDynamic())
            break;
        if (i == ARG_COUNT) // All of them are dynamic; can't reduce any.
          return; // Can ignore relax events until condition var is relaxed.
        for (bool changedOne = true; changedOne; ) {
          changedOne = false;
          for (i = 2; i < ARG_COUNT; i++) {
            changedOne = dom1.equate(getCurrentDomain(m_variables[i])) || changedOne;
            if (dom1.isEmpty())
              return; // inconsistent: can't all be the same.
          }
        }
      }
    } // else of if (boolDom.isSingleton())
  } // end of CondAllSameConstraint::handleExecute()

  CondAllDiffConstraint::CondAllDiffConstraint(const LabelStr& name,
                                               const LabelStr& propagatorName,
                                               const ConstraintEngineId& constraintEngine,
                                               const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables),
      ARG_COUNT(variables.size()) {
    check_error(ARG_COUNT > 2);
    check_error(getCurrentDomain(m_variables[0]).getType() == AbstractDomain::BOOL);
    for (unsigned int i = 2; i < ARG_COUNT; i++)
      check_error(AbstractDomain::canBeCompared(getCurrentDomain(m_variables[1]),
                                                getCurrentDomain(m_variables[i])));
  }

  /**
   * @brief Helper function: add domToAdd to unionOfDomains "usefully".
   * Adds all of the members of domToAdd and, if needed and useful,
   * more values to unionOfDomains.
   * @param unionOfDomains Pointer to new'd AbstractDomain, which may
   * be delete'd and new'd with a different concrete class by this
   * function.
   * @param domToAdd Set of values to add to unionOfDomains' concrete
   * (C++) domain object.
   * @note Can add too much without affecting CondAllDiffConstraint
   * other than by delaying propagation, so add a disjoint interval by
   * simply returning a single larger interval that 'covers' both
   * original intervals.
   */
  static void addToUnion(AbstractDomain **unionOfDomains,
                         const AbstractDomain& domToAdd) {
    assertTrue(unionOfDomains != 0 && *unionOfDomains != 0);
    assertTrue(!(*unionOfDomains)->isEmpty() && !(*unionOfDomains)->isDynamic());
    assertTrue(!domToAdd.isEmpty() && !domToAdd.isDynamic());
    AbstractDomain *newUnion;
    std::list<double> membersToAdd;
    std::list<double> newMembers;
    if (((*unionOfDomains)->isEnumerated() || (*unionOfDomains)->isSingleton())
        && (domToAdd.isEnumerated() || domToAdd.isSingleton())) {
      if (domToAdd.isEnumerated())
        domToAdd.getValues(membersToAdd);
      else
        membersToAdd.push_back(domToAdd.getSingletonValue());
      if ((*unionOfDomains)->isEnumerated())
        (*unionOfDomains)->getValues(newMembers);
      else
        newMembers.push_back((*unionOfDomains)->getSingletonValue());
      for (std::list<double>::const_iterator it = membersToAdd.begin();
           it != membersToAdd.end(); it++)
        if (find(newMembers.begin(), newMembers.end(), *it) == newMembers.end())
          newMembers.push_back(*it);
      newUnion = new EnumeratedDomain(newMembers, /* closed = */ true,
                                      (*unionOfDomains)->getListener(),
                                      (*unionOfDomains)->isNumeric());
      // Could just add to current unionOfDomains rather than failing here, but
      //   very messy to implement using current interface to *Domain classes.
      assertFalse(newUnion == 0);
      delete *unionOfDomains;
      *unionOfDomains = newUnion;
      return;
    }
    // At least one is a non-singleton interval, so the result will be
    //   also be one.
    double toAddMin, toAddMax, newMin, newMax;
    domToAdd.getBounds(toAddMin, toAddMax);
    (*unionOfDomains)->getBounds(newMin, newMax);
    bool changing = false;
    if (toAddMin < newMin) {
      newMin = toAddMin;
      changing = true;
    }
    if (newMax < toAddMax) {
      newMax = toAddMax;
      changing = true;
    }
    if (changing) {
      if (domToAdd.getType() == AbstractDomain::REAL_INTERVAL
          || (*unionOfDomains)->getType() == AbstractDomain::REAL_INTERVAL)
        newUnion = new IntervalDomain(newMin, newMax,
                                      (*unionOfDomains)->getListener());
      if (domToAdd.getType() == AbstractDomain::INT_INTERVAL
          || (*unionOfDomains)->getType() == AbstractDomain::INT_INTERVAL)
        newUnion = new IntervalIntDomain((int)newMin, (int)newMax,
                                         (*unionOfDomains)->getListener());
      /* BOOL should be not get to here since both are non-singleton
       *   but then unionOfDomains "covers" domToAdd and changing
       *   would be false.
       * USER_DEFINED and REAL_ENUMERATION should not get to here
       *   since enumerations are dealt with above.
       * As above, a memory failure here could be dealt with, but
       *   messy to implement, but note that this also checks the
       *   assumptions/logic earlier in this comment.
       */
      assertFalse(newUnion == 0);
      delete *unionOfDomains;
      *unionOfDomains = newUnion;
      return;
    }
  }

  void CondAllDiffConstraint::handleExecute() {
    BoolDomain& boolDom = static_cast<BoolDomain&>(getCurrentDomain(m_variables[0]));
    check_error(!boolDom.isDynamic());

    /* Whether the condition is singleton or not, try to restrict it:
     * A. If all pairs of the other's domains are disjoint, the
     *    condition is true.
     * B. If the union of any set of the others has cardinality less
     *    than their count, the condition is false.
     */
    bool canProveTrue = true;
    unsigned int firstNonDynamic = 0;
    unsigned int firstDynamic = 0;
    unsigned int i = 1;
    AbstractDomain* unionOfOthers = 0;
    for ( ; i < ARG_COUNT; i++) {
      AbstractDomain& current(getCurrentDomain(m_variables[i]));
      if (current.isDynamic()) {
        canProveTrue = false;
        if (firstDynamic == 0)
          firstDynamic = i;
        continue;
      }
      if (firstNonDynamic == 0)
        firstNonDynamic = i;
      if (unionOfOthers == 0) {
        // First (non-dynamic) one: copy it to union with later ones.
        unionOfOthers = current.copy();

        // Skipping this check merely results in less efficient propagation.
        // check_error(unionOfOthers != 0);

      } else {
        /* Intersect current with union of priors: if non-empty, the
           condition cannot be proven true since further restrictions
           could make two of the variables have the same singleton values.
        */
        if (canProveTrue && unionOfOthers->intersects(current))
          canProveTrue = false;
        // Add members of current to unionOfOthers "usefully".
        addToUnion(&unionOfOthers, current);
        if (unionOfOthers->isFinite() && (unsigned int)(unionOfOthers->getSize()) < i) {
          // At least two of the variables must have same value.
          boolDom.remove(true);
          canProveTrue = false;
          if (boolDom.isEmpty())
            break; // Would return except for delete of unionOfOthers.
        }
      }
    } // for ( ; i < ARG_COUNT; i++)
    // Don't need this below, so:
    if (unionOfOthers != 0)
      delete unionOfOthers;
    if (canProveTrue)
      boolDom.remove(false);

    /* Whether the condition was singleton on entry to this function
     * or became singleton just above, propagate the effects of that
     * to the other variables in the scope.  But nothing can be done
     * if there are no other non-dynamic domains.
     */
    if (!boolDom.isSingleton() || firstNonDynamic == 0)
      return; // Cannot restrict any of the other vars.

    if (!boolDom.getSingletonValue()) {
      /* Condition var is singleton false: at least two other vars
       * must have same value.  Any dynamic domain could potentially
       * be narrowed to any single value, so there's nothing further
       * to do if there are any dynamic domains.
       */
      if (firstDynamic > 0)
        return;
      for (i = 1; i < ARG_COUNT - 1; i++) {
        AbstractDomain& iDom = getCurrentDomain(m_variables[i]);
        for (unsigned int j = i; ++j < ARG_COUNT; ) {
          if (iDom.intersects(getCurrentDomain(m_variables[j])))
            // The two vars have overlapping domains, so constraint is satisfied.
            return;
        }
      }
      /* Could not find two vars that might have same value, so
       * the condition var must be true and this constraint is
       * violated.
       */
      boolDom.remove(false);
      return;
    }
    /* Then condition var is singleton true: force all other vars to be distinct.
     * If no other var is singleton, no (easy) way to restrict other
     *   any other var, so find singletons and remove them from the other
     *   (non-dynamic & finite) domains where the removal can be done
     *   without splitting intervals.
     * Harder: look for x vars that have at most y values where x >= y.
     *   Similar checks already made above, so effect here would be minimal.
     */
    for (bool changedOne = true; changedOne; ) {
      changedOne = false;
      // For each var ...
      for (i = firstNonDynamic; i < ARG_COUNT; i++) {
        // ... that has a singleton domain ...
        if (getCurrentDomain(m_variables[i]).isDynamic() ||
            !getCurrentDomain(m_variables[i]).isSingleton())
          continue;
        AbstractDomain& singletonDom = getCurrentDomain(m_variables[i]);
        // ... go thru the other vars ...
        for (unsigned int j = firstNonDynamic; j < ARG_COUNT; j++) {
          if (j == i)
            continue;
          AbstractDomain& jDom = getCurrentDomain(m_variables[j]);
          // ... that have non-dynamic and finite domains ...
          if (!jDom.isDynamic() && jDom.isFinite() &&
              // ... looking for one that contains singletonDom's value.
              // In an enumeration, any member can be removed:
              ((jDom.isEnumerated() && jDom.isMember(singletonDom.getSingletonValue()))
               // In an interval, can only remove an endpoint:
               || (!jDom.isEnumerated() && (singletonDom.isMember(jDom.getLowerBound())
                                            || singletonDom.isMember(jDom.getUpperBound()))))) {
            // Found one: remove singletonDom's value.
            jDom.remove(singletonDom.getSingletonValue());
            if (jDom.isEmpty())
              return;
            // No point in going thru again if we're just starting.
            changedOne = (i > firstNonDynamic);
          } // if !jDom.isDynamic ...
        } // for unsigned int j = firstNonDynamic; ...
      } // for i = firstNonDynamic; i < ARG_COUNT; i++
    } // for changedOne = true; changedOne;
  } // end of CondAllDiffConstraint::handleExecute()

} // end namespace Prototype
