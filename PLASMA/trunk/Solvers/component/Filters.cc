#include "Filters.hh"
#include "Resource.hh"
#include "SolverUtils.hh"
#include "Debug.hh"
#include "PlanDatabase.hh"

namespace EUROPA {
  namespace SOLVERS {

    InfiniteDynamicFilter::InfiniteDynamicFilter(const TiXmlElement& configData)
      : VariableMatchingRule(configData) {
      debugMsg("InfiniteDynamicFilter:constructor", "Constructing an InfiniteDynamicFilter.");
    }

    bool InfiniteDynamicFilter::matches(const ConstrainedVariableId& var) const {
      debugMsg("InfiniteDynamicFilter:matches", "Evaluating " << var->toString() << " for dynamic/infinite filter.");
      debugMsg("InfiniteDynamicFilter:matches", var->lastDomain() << " isOpen : " << var->lastDomain().isOpen());
      debugMsg("InfiniteDynamicFilter:matches", var->lastDomain() << " isInfinite : " << var->lastDomain().isInfinite());
      return (var->lastDomain().isOpen() || var->lastDomain().isInfinite());
    }

    std::string InfiniteDynamicFilter::getExpression() const {
      return "infinite/dynamic";
    }

    SingletonFilter::SingletonFilter(const TiXmlElement& configData)
      : VariableMatchingRule(configData) {}

    bool SingletonFilter::matches(const ConstrainedVariableId& var) const {
      debugMsg("SingletonFilter:matches", "Evaluating " << var->toString() << " for singleton filter.");

      // Indicate a match if it is not a singleton
      return !var->lastDomain().isSingleton();
    }


    /** HORIZON FILTERING **/
    IntervalIntDomain& HorizonFilter::getHorizon() {
      static IntervalIntDomain sl_instance;
      return sl_instance;
    }

    HorizonFilter::HorizonFilter(const TiXmlElement& configData)
      : TokenMatchingRule(configData) {
      static const LabelStr sl_defaultPolicy("PartiallyContained");
      const char* argData = NULL;
      argData = configData.Attribute("policy");
      if(argData != NULL){
	checkError(policies().contains(argData), argData << " is not a valid policy. Choose one of " << policies().toString());
	m_policy = LabelStr(argData);
      }
      else
	m_policy = sl_defaultPolicy;
    }

    bool HorizonFilter::matches(const TokenId& token) const {
      static const LabelStr sl_possiblyContained("PossiblyContained");
      static const LabelStr sl_partiallyContained("PartiallyContained");
      static const LabelStr sl_totallyContained("TotallyContained");

      const IntervalIntDomain& horizon = getHorizon();
      checkError(horizon.isFinite(), "Infinite Horizon not permitted." << horizon.toString());
      const IntervalIntDomain& startTime = token->getStart()->lastDomain();
      const IntervalIntDomain& endTime = token->getEnd()->lastDomain();

      bool withinHorizon = false;

      debugMsg("HorizonFilter:matches",
	       "Evaluating: " << token->toString() << 
	       " Start=" << startTime.toString() << ", End=" << endTime.toString() <<
	       ", Policy='" << m_policy.toString() << "', Horizon =" << horizon.toString());

      if(m_policy == sl_possiblyContained)
	withinHorizon = startTime.intersects(horizon) && endTime.intersects(horizon);
      else if (m_policy == sl_partiallyContained)
	withinHorizon = (endTime.getLowerBound() > horizon.getLowerBound() &&
		  startTime.getUpperBound() < horizon.getUpperBound() &&
		  (startTime.intersects(horizon) || endTime.intersects(horizon)));
      else
	withinHorizon = horizon.isMember(startTime.getLowerBound()) && horizon.isMember(endTime.getUpperBound());

      debugMsg("HorizonFilter:matches", 
	       token->toString() << " is " << (withinHorizon ? " inside " : " outside ") << " the horizon.");

      return !withinHorizon;
    }


    std::string HorizonFilter::getExpression() const {
      const IntervalIntDomain& horizon = getHorizon();
      std::string expr = TokenMatchingRule::getExpression();
      expr = expr + " Policy='" + m_policy.toString() + "' Horizon=" + horizon.toString();
      return expr;
    }

    HorizonVariableFilter::HorizonVariableFilter(const TiXmlElement& configData)
      : VariableMatchingRule(configData), m_horizonFilter(configData){}

    bool HorizonVariableFilter::matches(const ConstrainedVariableId& var) const {
      debugMsg("HorizonVariableFilter:matches", "Evaluating " << var->toString() << " for horizon filter.");

      EntityId parent = var->getParent();

      if(parent.isNoId() || ObjectId::convertable(parent))
	return false;

      TokenId token;

      if(RuleInstanceId::convertable(parent))
	token = RuleInstanceId(parent)->getToken();
      else {
	checkError(TokenId::convertable(parent), 
		   "If we have covered our bases, it must be a token, but it ain't:" << var->toString());
	token = (TokenId) parent;
      }

      // Now simply delegate to the filter stored internally.
      return m_horizonFilter.matches(token);
    }

    std::string HorizonVariableFilter::getExpression() const {
      return m_horizonFilter.getExpression();
    }
  }
}
