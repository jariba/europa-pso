#include "TimetableProfile.hh"
#include "Instant.hh"
#include "Transaction.hh"
#include "ConstrainedVariable.hh"
#include "Debug.hh"

namespace EUROPA {
    TimetableProfile::TimetableProfile(const PlanDatabaseId db, const FVDetectorId flawDetector, const ExplicitProfileId limitProfile)
    	: Profile(db, flawDetector, limitProfile)
    	, m_lowerLevelMin(0)
    	, m_lowerLevelMax(0)
    	, m_upperLevelMin(0)
    	, m_upperLevelMax(0)
    	, m_minPrevConsumption(0)
    	, m_maxPrevConsumption(0)
    	, m_minPrevProduction(0)
    	, m_maxPrevProduction(0)
    {
    }

    void TimetableProfile::initRecompute(InstantId inst) {
      checkError(m_recomputeInterval.isValid(), "Attempted to initialize recomputation without a valid starting point!");
      m_lowerLevelMin = inst->getLowerLevel();
      m_lowerLevelMax = inst->getLowerLevelMax();
      m_upperLevelMin = inst->getUpperLevelMin();
      m_upperLevelMax = inst->getUpperLevel();
      m_minPrevConsumption = inst->getMinPrevConsumption();
      m_maxPrevConsumption = inst->getMaxPrevConsumption();
      m_minPrevProduction = inst->getMinPrevProduction();
      m_maxPrevProduction = inst->getMaxPrevProduction();
    }

    void TimetableProfile::initRecompute() {
		m_lowerLevelMin = getInitCapacityLb();
		m_lowerLevelMax = getInitCapacityLb();
		m_upperLevelMin = getInitCapacityUb();
		m_upperLevelMax = getInitCapacityUb();
		m_minPrevConsumption = 0;
		m_maxPrevConsumption = 0;
		m_minPrevProduction = 0;
		m_maxPrevProduction = 0;
    }

    void TimetableProfile::recomputeLevels( InstantId prev, InstantId inst) {
      check_error(inst.isValid());

      edouble maxInstantProduction(0), minInstantProduction(0), maxInstantConsumption(0), minInstantConsumption(0);
      edouble maxCumulativeProduction(m_maxPrevProduction), minCumulativeProduction(m_minPrevProduction);
      edouble maxCumulativeConsumption(m_maxPrevConsumption), minCumulativeConsumption(m_minPrevConsumption);

      const std::set<TransactionId>& transactions(inst->getTransactions());
      debugMsg("TimetableProfile:recomputeLevels", "Transactions at " << inst->getTime() << ":");
      for(std::set<TransactionId>::const_iterator it = transactions.begin(); it != transactions.end(); ++it) {
	TransactionId trans = *it;
	edouble lb, ub;
	trans->quantity()->lastDomain().getBounds(lb, ub);
	bool isConsumer = trans->isConsumer();
	debugMsg("TimetableProfile:recomputeLevels", "Time: [" << trans->time()->lastDomain().getLowerBound() << " " <<
		 trans->time()->lastDomain().getUpperBound() << "] Quantity: " << (isConsumer ? "-" : "+") <<
		 "[" << lb << " " << ub << "]");

	//the minInstant values are 0 unless there is a transaction that cannot happen before or after this instant, so we have to add those
	if(trans->time()->lastDomain().isSingleton()) {
	  if(isConsumer)
	    minInstantConsumption += lb;
	  else
	    minInstantProduction += lb;
	}

	//of course, in the upper case we consume and produce the most possible at this instant
	if(isConsumer)
	  maxInstantConsumption += ub;
	else
	  maxInstantProduction += ub;


	//if the transaction just started, add producer to upper bounds and consumer to lower bounds
	if(trans->time()->lastDomain().getLowerBound() == inst->getTime())
		handleTransactionStart(isConsumer, lb, ub);

	//if the transaction just ended, add producer to lower bounds and consumer to upper bounds
	if(trans->time()->lastDomain().getUpperBound() == inst->getTime()) {
		handleTransactionEnd(isConsumer, lb, ub);
		if(isConsumer)
			minCumulativeConsumption += lb;
		else
			minCumulativeProduction += lb;
	}

      }
      maxCumulativeConsumption += maxInstantConsumption;
      maxCumulativeProduction += maxInstantProduction;

      debugMsg("TimetableProfile:recompute", "Computed values for time " << inst->getTime() << ":" << std::endl <<
	       "    Lower level (min, max): (" << m_lowerLevelMin << ", " << m_lowerLevelMax << ")" << std::endl <<
	       "    Upper level (min, max): (" << m_upperLevelMin << ", " << m_upperLevelMax << ")" << std::endl <<
	       "    Instantaneous consumption (min, max): (" << minInstantConsumption << ", " << maxInstantConsumption << ")" << std::endl <<
	       "    Instantaneous production (min, max): (" << minInstantProduction << ", " << maxInstantProduction << ")" << std::endl <<
	       "    Cumulative consumption (min, max): (" << minCumulativeConsumption << ", " << maxCumulativeConsumption << ")" << std::endl <<
	       "    Cumulative production (min, max): (" << minCumulativeProduction << ", " << maxCumulativeProduction << ")" << std::endl);
      inst->update(m_lowerLevelMin, m_lowerLevelMax, m_upperLevelMin, m_upperLevelMax,
		   minInstantConsumption, maxInstantConsumption, minInstantProduction, maxInstantProduction,
		   minCumulativeConsumption, maxCumulativeConsumption, minCumulativeProduction, maxCumulativeProduction,
		   m_minPrevConsumption, m_maxPrevConsumption, m_minPrevProduction, m_maxPrevProduction);

      //update the values for production and consumption that must have happened by the next transaction
      for(std::set<TransactionId>::const_iterator it = inst->getEndingTransactions().begin(); it != inst->getEndingTransactions().end(); ++it) {
	TransactionId trans = *it;
	check_error(trans.isValid());
	edouble lb, ub;
	trans->quantity()->lastDomain().getBounds(lb, ub);
	if(trans->isConsumer()) {
	  m_maxPrevConsumption += ub;
	  m_minPrevConsumption += lb;
	}
	else {
	  m_maxPrevProduction += ub;
	  m_minPrevProduction += lb;
	}
      }
    }

    void TimetableProfile::getTransactionsToOrder(const InstantId& inst, std::vector<TransactionId>& results) {
      check_error(inst.isValid());
      check_error(results.empty());
      results.insert(results.end(), inst->getTransactions().begin(), inst->getTransactions().end());
    }


	void TimetableProfile::handleTransactionStart(bool isConsumer, const edouble & lb, const edouble & ub)
	{
		if(isConsumer) {
			m_lowerLevelMin -= ub;
			m_lowerLevelMax -= lb;
		}
		else {
			m_upperLevelMin += lb;
			m_upperLevelMax += ub;
		}
	}

	void TimetableProfile::handleTransactionEnd(bool isConsumer, const edouble & lb, const edouble & ub)
	{
		if(isConsumer) {
			m_upperLevelMax -= lb;
			m_upperLevelMin -= ub;
		}
		else {
			m_lowerLevelMin += lb;
			m_lowerLevelMax += ub;
		}
	}
}
