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

    // Now, rounding issues from mixed numeric types can lead to the following inconsistency, not yet caught.
    // We handle it here however, by emptying the domain if the invariant post-condition is not satisfied. The
    // motivating case for this: A:Int[0,10] + B:Int[0,10] == C:Real[0.5, 0.5].
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
    else if (superset.getType() == AbstractDomain::INT_INTERVAL)
      m_superSetDomain = new IntervalIntDomain((const IntervalIntDomain&) superset);
    else if (superset.getType() == AbstractDomain::REAL_INTERVAL)
      m_superSetDomain = new IntervalDomain((const IntervalDomain&) superset);
    else if (superset.getType() == AbstractDomain::BOOL)
      m_superSetDomain = new BoolDomain((const BoolDomain&) superset);
  }

  SubsetOfConstraint::~SubsetOfConstraint() {
    delete m_superSetDomain;
  }

  void SubsetOfConstraint::handleExecute() {
    // Why can't this be done in the constructor? --wedgingt 2004 Feb 24
    check_error(AbstractDomain::canBeCompared(m_currentDomain, *m_superSetDomain));

    // Shouldn't this do 'm_currentDomain = getCurrentDomain(variable);' first? --wedgingt 2004 Feb 24
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
}
