#include "SAVH_InstantTokens.hh"
#include "TokenVariable.hh"

namespace EUROPA {
  namespace SAVH {
    ReservoirToken::ReservoirToken(const PlanDatabaseId& planDatabase,
				   const LabelStr& predicateName,
				   const IntervalIntDomain& timeBaseDomain,
				   const IntervalDomain& quantityBaseDomain,
				   bool isConsumer,
				   bool closed) 
      : EventToken(planDatabase, predicateName,
		   false, timeBaseDomain,
		   Token::noObject(), false), m_isConsumer(isConsumer) {
      check_error(quantityBaseDomain.getLowerBound() >= 0 &&
		  quantityBaseDomain.getUpperBound() <= PLUS_INFINITY);
      commonInit(closed);
      m_quantity->restrictBaseDomain(quantityBaseDomain);
    }

    ReservoirToken::ReservoirToken(const PlanDatabaseId& planDatabase,
				   const LabelStr& predicateName,
				   bool rejectable,
				   const IntervalIntDomain& timeBaseDomain,
				   const LabelStr& objectName,
				   bool closed)
      : EventToken(planDatabase, predicateName,
		   false, timeBaseDomain, objectName, false) {
      commonInit(closed);
    }

    ReservoirToken::ReservoirToken(const TokenId& parent,
				   const LabelStr& relation,
				   const LabelStr& predicateName,
				   const IntervalIntDomain& timeBaseDomain,
				   const LabelStr& objectName,
				   bool closed)
      : EventToken(parent, relation, predicateName, timeBaseDomain, objectName, false) {
      commonInit(closed);
    }

    void ReservoirToken::commonInit(bool closed) {
      m_quantity = (new TokenVariable<IntervalDomain>(m_id, m_allVariables.size(),
						      m_planDatabase->getConstraintEngine(),
						      IntervalDomain(0, PLUS_INFINITY),
						      true, LabelStr("quantity")))->getId();
      m_allVariables.push_back(m_quantity);
      
      if(closed)
	close();
    }

    void ReservoirToken::close() {
      EventToken::close();
      activateInternal();
    }
    
    const ConstrainedVariableId& ReservoirToken::getQuantity() const {
      return m_quantity;
    }

    bool ReservoirToken::isConsumer() const {return m_isConsumer;}
  }
}
