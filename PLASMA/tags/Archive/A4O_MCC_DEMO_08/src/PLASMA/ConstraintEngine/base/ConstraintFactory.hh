#ifndef _H_ConstraintFactory
#define _H_ConstraintFactory

#include "ConstraintEngineDefs.hh"
#include "AbstractDomain.hh"
#include "LabelStr.hh"
#include "Constraint.hh"

#include <string>
#include <vector>
#include <map>

/**
 * @file ConstraintFactory.hh
 */

namespace EUROPA {

  class ConstraintFactory;
  typedef Id<ConstraintFactory> ConstraintFactoryId;

  class ConstraintFactory {
  public:
    virtual ~ConstraintFactory() { }

    const LabelStr& getName() const {
      return m_name;
    }

    bool isSystemDefined() const {
      return m_systemDefined;
    }

    const ConstraintFactoryId& getId() const {return m_id;}

    virtual ConstraintId createConstraint(const ConstraintEngineId constraintEngine, 
					  const std::vector<ConstrainedVariableId>& scope) {
      check_error(ALWAYS_FAILS);
      return ConstraintId::noId();
    }

  protected:
    ConstraintFactory(const LabelStr& name, const LabelStr& propagatorName,
		      bool systemDefined = false)
      : m_id(this), m_name(name), m_propagatorName(propagatorName),
	m_systemDefined(systemDefined) { }

    ConstraintFactoryId m_id;
    const LabelStr m_name;
    const LabelStr m_propagatorName;
    const bool m_systemDefined;
  };

  /**********************************************************/

  template <class ConstraintType>
  class ConcreteConstraintFactory : public ConstraintFactory {
  public:
    ConcreteConstraintFactory(const LabelStr& name, const LabelStr& propagatorName, bool systemDefined = false)
      : ConstraintFactory(name, propagatorName, systemDefined) { }

    ConstraintId createConstraint(const ConstraintEngineId constraintEngine, 
				  const std::vector<ConstrainedVariableId>& scope) {
      check_error(constraintEngine.isValid());
      check_error(scope.size() >= 1);
      Constraint* constraint = new ConstraintType(m_name, m_propagatorName, constraintEngine, scope);
      check_error(constraint != 0);
      check_error(constraint->getId().isValid());
      return(constraint->getId());
    }
  };

  template <class ConstraintType>
  class RotatedNaryConstraintFactory : public ConstraintFactory {
  public:
    RotatedNaryConstraintFactory(const LabelStr& name, const LabelStr& propagatorName,
                                 const LabelStr& otherName, const int& rotateCount)
      : ConstraintFactory(name, propagatorName),
        m_otherName(otherName),
        m_rotateCount(rotateCount) {
      assertTrue(name != otherName);
    }

    ConstraintId createConstraint(const ConstraintEngineId constraintEngine, 
				  const std::vector<ConstrainedVariableId>& scope) {
      check_error(constraintEngine.isValid());
      check_error(scope.size() >= 1);
      Constraint* constraint = new ConstraintType(m_name, m_propagatorName, constraintEngine, scope,
                                                  m_otherName, m_rotateCount);
      check_error(constraint != 0);
      check_error(constraint->getId().isValid());
      return(constraint->getId());
    }

  private:
    const LabelStr m_otherName;
    const int m_rotateCount;
  };

  template <class ConstraintType>
  class SwapTwoVarsNaryConstraintFactory : public ConstraintFactory {
  public:
    SwapTwoVarsNaryConstraintFactory(const LabelStr& name, const LabelStr& propagatorName,
                                 const LabelStr& otherName, const int& first, const int& second)
      : ConstraintFactory(name, propagatorName),
        m_otherName(otherName), m_first(first), m_second(second) {
      assertTrue(name != otherName);
      assertTrue(first != second);
    }

    ConstraintId createConstraint(const ConstraintEngineId constraintEngine, 
				  const std::vector<ConstrainedVariableId>& scope) {
      check_error(constraintEngine.isValid());
      check_error(scope.size() >= 1);
      Constraint* constraint = new ConstraintType(m_name, m_propagatorName, constraintEngine,
                                                  scope, m_otherName, m_first, m_second);
      check_error(constraint != 0);
      check_error(constraint->getId().isValid());
      return(constraint->getId());
    }

  private:
    const LabelStr m_otherName;
    const int m_first, m_second;
  };

  /**
   * @def REGISTER_SYSTEM_CONSTRAINT
   * @brief Inform the constraint library about a particular non-unary constraint.
   * @param ConstraintType The constraint's implementation, a C++ class that
   * derives from class Constraint.
   * @param ConstraintName The constraint's name as used in a model, for example.
   * @param PropagatorName The constraint's propagator's name.
   */
#define REGISTER_SYSTEM_CONSTRAINT(ceSchema, ConstraintType, ConstraintName, PropagatorName) \
  (ceSchema->registerConstraintFactory(new ConcreteConstraintFactory<ConstraintType>(LabelStr(ConstraintName), LabelStr(PropagatorName), true)))

  /**
   * @def REGISTER_CONSTRAINT
   * @brief Inform the constraint library about a particular non-unary constraint.
   * @param ConstraintType The constraint's implementation, a C++ class that
   * derives from class Constraint.
   * @param ConstraintName The constraint's name as used in NDDL models.
   * @param PropagatorName The constraint's propagator's name.
   */
#define REGISTER_CONSTRAINT(ceSchema,ConstraintType, ConstraintName, PropagatorName) \
  (ceSchema->registerConstraintFactory(new ConcreteConstraintFactory<ConstraintType>(LabelStr(ConstraintName), LabelStr(PropagatorName))))

  /**
   * @def REGISTER_ROTATED_CONSTRAINT
   * @brief Inform the constraint library about a particular non-unary constraint
   * that can use an existing constraint's implementation after shifting the
   * variables in the scope to the right (positive) or left (negative).
   * @param ConstraintName The constraint's name as used in NDDL models.
   * @param PropagatorName The constraint's propagator's name.
   * @param RotateName The name of the constraint to be called after the
   * scope has been rotated based on RotateCount.
   * @param RotateCount The number of variables to be moved from the end of the scope
   * to the start of the scope.  E.g., if 1, move the last variable to the front;
   * if -1, move the first variable to the end.
   */
#define REGISTER_ROTATED_CONSTRAINT(ceSchema,ConstraintName, PropagatorName, RotateName, RotateCount) \
  (ceSchema->registerConstraintFactory(new RotatedNaryConstraintFactory<RotateScopeRightConstraint>(LabelStr(ConstraintName), LabelStr(PropagatorName),\
                                                                                                   LabelStr(RotateName), (RotateCount))))

  /**
   * @def REGISTER_SWAP_TWO_VARS_CONSTRAINT
   * @brief Inform the constraint library about a particular non-unary constraint
   * that can use an existing constraint's implementation after swapping two
   * variables in the scope.
   * @param ConstraintName The constraint's name as used in NDDL models.
   * @param PropagatorName The constraint's propagator's name.
   * @param RotateName The name of the constraint to be called after the
   * two variables have been swapped.
   * @param FirstVar The index of the first variable to swap.
   * @param SecondVar The index of the second variable to swap.
   * @note The indices start at 0 as for C++ STL's std::vector class.
   * @note An index of -1 indicates the last variable in the scope, -2 the one
   * before the last variable, etc.
   */
#define REGISTER_SWAP_TWO_VARS_CONSTRAINT(ceSchema,ConstraintName, PropagatorName, RotateName, FirstVar, SecondVar) \
  (ceSchema->registerConstraintFactory(new SwapTwoVarsNaryConstraintFactory<SwapTwoVarsConstraint>(LabelStr(ConstraintName), LabelStr(PropagatorName), \
                                                                                                  LabelStr(RotateName), (FirstVar), (SecondVar))))

}
#endif
