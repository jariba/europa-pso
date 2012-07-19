#include "Resource.hh"
#include "Profile.hh"
#include "FVDetector.hh"
#include "Transaction.hh"
#include "Instant.hh"
#include "ResourceTokenRelation.hh"
#include "PlanDatabase.hh"
#include "Variable.hh"
#include "Domains.hh"
#include "Token.hh"
#include "TokenVariable.hh"
#include "TemporalAdvisor.hh"
#include "PlanDatabaseDefs.hh"

#include <cmath>
#ifdef _MSC_VER
#  include <functional>
   using std::select2nd;
#else
#  include <ext/functional>
   using __gnu_cxx::select2nd;
#endif //_MSC_VER

namespace EUROPA {

  Resource::Resource(const PlanDatabaseId& planDatabase,
                     const LabelStr& type, const LabelStr& name,
                     const LabelStr& detectorName,
                     const LabelStr& profileName,
                     edouble initCapacityLb, edouble initCapacityUb,
                     edouble lowerLimit, edouble upperLimit,
                     edouble maxInstProduction, edouble maxInstConsumption,
                     edouble maxProduction, edouble maxConsumption)
    : Object(planDatabase, type, name, false)
  {
    init(initCapacityLb, initCapacityUb,
         lowerLimit, upperLimit,
         maxInstProduction, maxInstConsumption,
         maxProduction, maxConsumption,
         detectorName,
         profileName
         );
  }

  Resource::Resource(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name, bool open)
    : Object(planDatabase, type, name, open) {}

  Resource::Resource(const ObjectId& parent, const LabelStr& type, const LabelStr& localName, bool open)
    : Object(parent, type, localName, open) {}

  Resource::~Resource()
  {
    for(std::map<TransactionId, TokenId>::const_iterator it = m_transactionsToTokens.begin(); it != m_transactionsToTokens.end();
        ++it) {
      if ((it->first->getOwner()).isNoId())
        delete (Transaction*) it->first;
    }
    delete (FVDetector*) m_detector;
    delete (Profile*) m_profile;
  }

  void Resource::init(const edouble initCapacityLb, const edouble initCapacityUb,
                      const edouble lowerLimit, const edouble upperLimit,
                      const edouble maxInstProduction, const edouble maxInstConsumption,
                      const edouble maxProduction, const edouble maxConsumption,
                      const LabelStr& detectorName,
                      const LabelStr& profileName) {
    debugMsg("Resource:init", "In base init function.");
    m_initCapacityLb = initCapacityLb;
    m_initCapacityUb = initCapacityUb;
    m_lowerLimit = lowerLimit;
    m_upperLimit = upperLimit;

    m_maxInstProduction = (maxInstProduction == PLUS_INFINITY ? maxProduction : maxInstProduction);
    m_maxProduction = maxProduction;
    m_maxInstConsumption = (maxInstConsumption == PLUS_INFINITY ? maxConsumption : maxInstConsumption);
    m_maxConsumption = maxConsumption;


    // TODO: make profile this  more robust?
    EngineId& engine = this->getPlanDatabase()->getEngine();

    FactoryMgr* fvdfm = (FactoryMgr*)engine->getComponent("FVDetectorFactoryMgr");
    m_detector = fvdfm->createInstance(detectorName, FVDetectorArgs(getId()));

    FactoryMgr* pfm = (FactoryMgr*)engine->getComponent("ProfileFactoryMgr");
    m_profile = pfm->createInstance(
                                    profileName,
                                    ProfileArgs(
                                                getPlanDatabase(),
                                                m_detector,
                                                m_initCapacityLb,
                                                m_initCapacityUb
                                                )
                                    );

    debugMsg("Resource:init", "Initialized Resource " << getName().toString() << "{"
             << "initCapacity=[" << m_initCapacityLb << "," << m_initCapacityUb << "],"
             << "usageLimits=[" << m_lowerLimit << "," << m_upperLimit << "],"
             << "productionLimits=[max=" << m_maxProduction << ",maxInst=" << m_maxInstProduction << "],"
             << "consumptionLimits=[max=" << m_maxConsumption << ",maxInst=" << m_maxInstConsumption << "],"
             << "}"
             );
  }

  void Resource::add(const TokenId& token) {
    if(tokens().find(token) != tokens().end())
      return;
    debugMsg("Resource:add", "Adding " << token->toString());
    Object::add(token);
    createTransactions(token);
  }

  //Due to the fact that add/remove and addToProfile/removeFromProfile are invoked by two different constraints and
  //that those constraints almost always execute in a single order, this needs to be done carefully
  void Resource::remove(const TokenId& token) {
    if(tokens().find(token) == tokens().end())
      return;
    debugMsg("Resource:remove", "Removing " << token->toString());
    Object::remove(token);
    removeFromProfile(token);
    removeTransactions(token);
  }

  void Resource::removeFromProfile(const TokenId& tok) {
    debugMsg("Resource:removeFromProfile", "Removing " << tok->toString());
    std::map<TokenId, std::set<InstantId> >::iterator it = m_flawedTokens.find(tok);
    if(it != m_flawedTokens.end()) {
      m_flawedTokens.erase(it);
      notifyOrderingNoLongerRequired(tok);
    }
  }


  bool Resource::hasTokensToOrder() const {
    return !m_flawedTokens.empty();
  }

  //can cache these results or perhaps compute the all-pairs incrementally in the instant
  void Resource::getOrderingChoices(const TokenId& token,
                                    std::vector<std::pair<TokenId, TokenId> >& results,
                                    unsigned int limit) {
    check_error(results.empty());
    check_error(token.isValid());
    check_error(limit > 0);
    debugMsg("Resource:getOrderingChoices", "Getting " << limit << " ordering choices for " << token->getPredicateName().toString() << "(" << token->getKey() << ")");

    if(!getPlanDatabase()->getConstraintEngine()->propagate()) {
      debugMsg("Resource:getOrderingChoices", "No ordering choices: the constraint network is inconsistent.");
      return;
    }

    std::multimap<TokenId, TokenId> pairs;
    std::map<eint, InstantId>::iterator first = m_flawedInstants.lower_bound(token->start()->lastDomain().getLowerBound());
    if(first == m_flawedInstants.end()) {
      debugMsg("Resource:getOrderingChoices", "No ordering choices:  no flawed instants after token start: " << token->start()->lastDomain().getLowerBound());
      return;
    }
    //       checkError(first != m_flawedInstants.end(),
    // 		 "No flawed instants within token " << token->getPredicateName().toString() << "(" << token->getKey() << ")");
    std::map<eint, InstantId>::iterator last = m_flawedInstants.lower_bound(token->end()->lastDomain().getUpperBound());

    debugMsg("Resource:getOrderingChoices", "Looking at flawed instants in interval [" << first->second->getTime() << " " <<
             (last == m_flawedInstants.end() ? PLUS_INFINITY : last->second->getTime()) << "]");
    if(last != m_flawedInstants.end() && last->second->getTime() == token->end()->lastDomain().getUpperBound())
      ++last;

    unsigned int count = 0;

    TemporalAdvisorId temporalAdvisor = getPlanDatabase()->getTemporalAdvisor();

    for(; first != last && count < limit; ++first) {
      debugMsg("Resource:getOrderingChoices", "Generating orderings for time " << first->second->getTime());

      const std::set<TransactionId>& transactions = first->second->getTransactions();
      for(std::set<TransactionId>::const_iterator it = transactions.begin(); it != transactions.end() && count < limit; ++it) {
        TransactionId predecessor = *it;
        check_error(predecessor.isValid());
        check_error(m_transactionsToTokens.find(predecessor) != m_transactionsToTokens.end());

        debugMsg("Resource:getOrderingChoices", "Looking at predecessor transaction <" << predecessor->time()->toString() << ", " <<
                 (predecessor->isConsumer() ? "(c)" : "(p)") << predecessor->quantity()->toString() << ">");
        TokenId predecessorToken = m_transactionsToTokens.find(predecessor)->second;
        check_error(predecessorToken.isValid());

        for(std::set<TransactionId>::const_iterator subIt = transactions.begin(); subIt != transactions.end() && count < limit; ++subIt) {
          if(subIt == it)
            continue;

          TransactionId successor = *subIt;
          check_error(successor.isValid());
          check_error(m_transactionsToTokens.find(successor) != m_transactionsToTokens.end());

          debugMsg("Resource:getOrderingChoices", "Looking at successor transaction <" << successor->time()->toString() << ", " <<
                   (successor->isConsumer() ? "(c)" : "(p)") << successor->quantity()->toString() << ">");

          TokenId successorToken = m_transactionsToTokens.find(successor)->second;
          check_error(successorToken.isValid());

          debugMsg("Resource:getOrderingChoices",
                   "Found predecessor token " << predecessorToken->getKey() << " and successorToken " << successorToken->getKey() <<
                   " " << (predecessorToken == successorToken));
          if(successorToken == predecessorToken || isConstrainedToPrecede(predecessorToken, successorToken) || !temporalAdvisor->canPrecede(predecessorToken, successorToken))
            continue;

          debugMsg("Resource:getOrderingChoices",
                   "Considering order <" << predecessorToken->getPredicateName().toString() << "(" << predecessorToken->getKey() << "), " <<
                   successorToken->getPredicateName().toString() << "(" << successorToken->getKey() << ")>");

          std::multimap<TokenId, TokenId>::iterator checkFirst = pairs.lower_bound(predecessorToken);
          //if we have an entry for the predecessor
          if(checkFirst != pairs.end() && checkFirst->first == predecessorToken) {
            bool foundPair = false;
            //look for the successor
            while(checkFirst != pairs.end() && checkFirst->first == predecessorToken) {
              if(checkFirst->second == successorToken) {
                debugMsg("Resource:getOrderingChoices", "Order already exists.");
                foundPair = true;
                break;
              }
              ++checkFirst;
            }
            //if we haven't found one
            if(!foundPair) {
              debugMsg("Resource:getOrderingChoices", "Adding order.");
              pairs.insert(std::pair<TokenId, TokenId>(predecessorToken, successorToken));
              ++count;
            }
          }
          else {
            debugMsg("Resource:getOrderingChoices", "Adding order.");
            pairs.insert(std::make_pair(predecessorToken, successorToken));
            ++count;
          }
        }
      }
    }
    for(std::multimap<TokenId, TokenId>::iterator it = pairs.begin(); it != pairs.end(); ++it)
      results.push_back(*it);
    debugMsg("Resource:getOrderingChoices", "Ultimately found " << results.size() << " orderings.");
  }

  void Resource::getTokensToOrder(std::vector<TokenId>& results) {
    check_error(results.empty());
    getPlanDatabase()->getConstraintEngine()->propagate();
    checkError(getPlanDatabase()->getConstraintEngine()->constraintConsistent(),
               "Should be consistent to continue here. Should have checked before you called the method in the first place.");
    for(ResourceFlaws::const_iterator it = m_flawedTokens.begin(); it != m_flawedTokens.end(); ++it)
      results.push_back(it->first);
  }

  TokenId Resource::getTokenForTransaction(TransactionId t)
  {
    std::map<TransactionId, TokenId>::iterator transIt = m_transactionsToTokens.find(t);
    check_error(transIt != m_transactionsToTokens.end());
    TokenId tok = transIt->second;
    check_error(tok.isValid());

    return tok;
  }

  ResourceTokenRelationId Resource::getRTRConstraint(TokenId tok)
  {
    ResourceTokenRelationId retval = ResourceTokenRelationId::noId();

    const std::set<ConstraintId>& constraints = tok->getStandardConstraints();
    std::set<ConstraintId>::const_iterator constIt = constraints.begin();
    for(;constIt != constraints.end();++constIt) {
      ConstraintId c = *constIt;
      if (c->getName() == ResourceTokenRelation::CONSTRAINT_NAME())
        return ResourceTokenRelationId(c);
    }

    check_error(ALWAYS_FAIL,"Could not find ResourceTokenRelation for Transaction");
    return retval;
  }

  bool isConsumptionProblem(Resource::ProblemType problem)
  {
    return
      problem == Resource::ConsumptionSumExceeded ||
      problem == Resource::ConsumptionRateExceeded ||
      problem == Resource::LevelTooLow;
  }

  bool isProductionProblem(Resource::ProblemType problem)
  {
    return
      problem == Resource::ProductionSumExceeded ||
      problem == Resource::ProductionRateExceeded ||
      problem == Resource::LevelTooHigh;
  }

  /*
   * reset violations from instant inst
   */
  void Resource::resetViolations(InstantId inst)
  {
    // TODO: for now reset all violations, see if this can be made more efficient
    resetViolations();
  }

  /*
   * reset all violations
   */
  void Resource::resetViolations()
  {
    if (getPlanDatabase()->getConstraintEngine()->getAllowViolations()) { // TODO: move this test to FVDetector?
      ProfileIterator it(m_profile);
      while(!it.done()) {
        notifyNoLongerViolated(it.getInstant());
        it.next();
      }
    }
  }

  void Resource::notifyViolated(const InstantId inst, ProblemType problem)
  {
    check_error(inst.isValid());
    check_error(inst->isViolated());
    check_error(!inst->getTransactions().empty());

    const std::set<TransactionId>& txns = inst->getTransactions();
    TransactionId txn = *(txns.begin());
    ConstraintEngineId ce = txn->quantity()->getConstraintEngine();

    debugMsg("Resource:notifyViolated", "Received notification of violation at time " << inst->getTime());

    if (ce->getAllowViolations()) { // TODO: move this test to the constraint?
      std::set<TransactionId>::const_iterator it = txns.begin();
      for(;it != txns.end(); ++it) {
        txn = *it;
        TokenId tok = getTokenForTransaction(txn);
        ResourceTokenRelationId c = getRTRConstraint(tok);

        if (isConsumptionProblem(problem) && txn->isConsumer()) {
          c->notifyViolated(problem,inst);
          debugMsg("Resource:notifyViolated", "Marked constraint as violated : Token(" << tok->getKey() << ") Constraint " << c->toString());
        }
        else if (isProductionProblem(problem) && !(txn->isConsumer())) {
          c->notifyViolated(problem,inst);
          debugMsg("Resource:notifyViolated", "Marked constraint as Violated : Token(" << tok->getKey() << ") Constraint " << c->toString());
        }
      }
    }
    else {
      const_cast<Domain&>(txn->quantity()->lastDomain()).empty();
    }
  }

  void Resource::notifyNoLongerViolated(const InstantId inst)
  {
    // remove all constraints associated with the instant from violated list
    const std::set<TransactionId>& txns = inst->getTransactions();
    std::set<TransactionId>::const_iterator it = txns.begin();
    for(;it != txns.end(); ++it) {
      TransactionId txn = *it;
      TokenId tok = getTokenForTransaction(txn);
      ResourceTokenRelationId c = getRTRConstraint(tok);
      if (c->getViolation() > 0) {
        c->notifyNoLongerViolated();
        debugMsg("Resource:notifyNoLongerViolated", "Marked constraint as NoLongerViolated : Token(" << tok->getKey() << ") Constraint " << c->toString());
      }
    }
  }

  //all ordering choices apply to all tokens in all flaws
  void Resource::notifyFlawed(const InstantId inst) {
    check_error(inst.isValid());
    check_error(inst->isFlawed());
    check_error(!inst->getTransactions().empty());

    debugMsg("Resource:notifyFlawed", toString() << " Received notification of flaw at time " << inst->getTime());

    std::vector<TransactionId> transactions;
    m_profile->getTransactionsToOrder(inst, transactions);

    //even if we've already recorded a flaw for this instant, there may be new transactions
    for(std::vector<TransactionId>::const_iterator it = transactions.begin();
        it != transactions.end(); ++it) {
      TransactionId trans = *it;
      check_error(trans.isValid());
      std::map<TransactionId, TokenId>::iterator transIt = m_transactionsToTokens.find(trans);
      check_error(transIt != m_transactionsToTokens.end());
      TokenId tok = transIt->second;

      check_error(tok.isValid());

      //if there are no recorded flaws for this token
      //  notify of the flaw
      //  record the flaw at the instant
      //else if this instant hasn't been recorded
      //  notify of the flaw
      // record the flaw at the instant

      ResourceFlaws::iterator flawIt = m_flawedTokens.find(tok);
      if(flawIt == m_flawedTokens.end()) {
        debugMsg("Resource:notifyFlawed", 
                 toString() << " Token " << tok->getPredicateName().toString() << "(" << tok->getKey() << ") is flawed at instant " << inst->getTime() <<
                 ".  Notifying that an ordering is required.");
        m_flawedTokens.insert(std::pair<TokenId, std::set<InstantId> >(tok, std::set<InstantId>()));
        flawIt = m_flawedTokens.find(tok);
        flawIt->second.insert(inst);
        notifyOrderingRequired(tok);
      }
      else {
        debugMsg("Resource:notifyFlawed",
                 toString() << " Received a redundant notification of a flaw for token " << tok->getPredicateName().toString() << "(" << tok->getKey() << ") at instant " <<
                 inst->getTime() << ".  Adding it to the set but not notifying.");
        flawIt->second.insert(inst);
      }
    }
    if(m_flawedInstants.find(inst->getTime()) == m_flawedInstants.end())
      m_flawedInstants.insert(std::make_pair(inst->getTime(), inst));
  }

  void Resource::notifyNoLongerFlawed(const InstantId inst) {
    check_error(inst.isValid());
    debugMsg("Resource:notifyNoLongerFlawed", toString() << " Received notification that instant " << inst->getTime() << " is no longer flawed.");

    if(m_flawedInstants.find(inst->getTime()) == m_flawedInstants.end()) {
      checkError(!inst->isFlawed(), "Instant at time " << inst->getTime() << " claims to be flawed, but resource didn't know it.");
      checkError(noFlawedTokensForInst(inst),
                 "Error: Instant " << inst->getTime() << " not in the list of flawed instants, but there are tokens that have a recorded flaw at that time.");
      debugMsg("Resource:notifyNoLongerFlawed", toString() << " Instant wasn't flawed in the first place.  Returning.");
      return;
    }

    //checkError(inst->isFlawed(), "Instant at time " << inst->getTime() << " isn't flawed, but is in the flawed instant list.");
    debugMsg("Resource:notifyNoLongerFlawed", toString() << " Removing instant " << inst->getTime() << " from the set of flawed instants.");
    m_flawedInstants.erase(inst->getTime());

    std::set<TokenId> flawlessTokens;

    for(ResourceFlaws::iterator tokIt = m_flawedTokens.begin(); tokIt != m_flawedTokens.end(); ++tokIt) {
      TokenId tok = tokIt->first;
      check_error(tok.isValid());
      check_error(!tokIt->second.empty());

      std::size_t size = tokIt->second.size();
      tokIt->second.erase(inst);
      condDebugMsg(size > tokIt->second.size(),
                   "Resource:notifyNoLongerFlawed", toString() << " Removed the flaw at time " << inst->getTime() << " for token " << tok->getPredicateName().toString() <<
                   "(" << tok->getKey() << ")");
      if(size > tokIt->second.size() && !tokIt->second.empty()) {
        std::stringstream str;
        for(std::set<InstantId>::const_iterator it = tokIt->second.begin(); it != tokIt->second.end(); ++it)
          str << (*it)->getTime() << " ";
        debugMsg("Resource:notifyNoLongerFlawed", toString() << " Remaining flaws for token " << tok->getPredicateName().toString() <<
                 "(" << tok->getKey() << "): " << str.str());
      }
      if(tokIt->second.empty())
        flawlessTokens.insert(tokIt->first);
    }

    for(std::set<TokenId>::const_iterator it = flawlessTokens.begin(); it != flawlessTokens.end(); ++it) {
      TokenId tok = *it;
      check_error(tok.isValid());
      debugMsg("Resource:notifyNoLongerFlawed", toString() << " Notifying that the token " << tok->getPredicateName().toString() << "(" <<
               tok->getKey() << ") no longer requires an ordering.");
      notifyOrderingNoLongerRequired(tok);
      m_flawedTokens.erase(tok);
    }
    debugMsg("Resource:notifyNoLongerFlawed", toString() << " Have " << m_flawedTokens.size() << " remaining flawed tokens.");
  }

  void Resource::notifyDeleted(const InstantId inst) {
    notifyNoLongerFlawed(inst);
    notifyNoLongerViolated(inst);
    debugMsg("Resource:notifyDeleted", "Removing (possibly) flawed instant at time " << inst->getTime());
    m_flawedInstants.erase(inst->getTime());
    for(ResourceFlaws::iterator it = m_flawedTokens.begin(); it != m_flawedTokens.end(); ++it)
      it->second.erase(inst);
  }

  bool Resource::noFlawedTokensForInst(const InstantId& inst) const {
    for(ResourceFlaws::const_iterator it = m_flawedTokens.begin(); it != m_flawedTokens.end(); ++it) {
      if(it->second.find(inst) != it->second.end()) {
        debugMsg("Resource:noFlawedTokensForInst",
                 "Found a flaw for instant " << inst->getTime() << " in token " << it->first->getPredicateName().toString() << "(" << it->first->getKey() << ")");
        return false;
      }
    }
    return true;
  }

  void Resource::getFlawedInstants(std::vector<InstantId>& results) {
    std::transform(m_flawedInstants.begin(), 
		   m_flawedInstants.end(), 
		   std::back_inserter(results), 
                   select2nd< std::map<eint, InstantId>::value_type >() );
    debugMsg("Resource:getFlawedInstants", "Have " << m_flawedInstants.size() << " flawed instants.  Returning " << results.size() << ".");
  }

  void Resource::getOrderingChoices(const InstantId& inst,
                                    std::vector<std::pair<TransactionId, TransactionId> >& results,
                                    unsigned int limit) {
      check_error(results.empty());
      check_error(inst.isValid());
      check_error(limit > 0);
      check_error(m_flawedInstants.find(inst->getTime()) != m_flawedInstants.end());
      check_error(m_flawedInstants.find(inst->getTime())->second == inst);

      debugMsg("Resource:getOrderingChoices", "Getting " << limit << " ordering choices for " << inst->getTime() << "(" << inst->getKey() << ") on " << toString());

      if(!getPlanDatabase()->getConstraintEngine()->propagate()) {
        debugMsg("Resource:getOrderingChoices", "No ordering choices: the constraint network is inconsistent.");
        return;
      }

      const std::set<TransactionId>& transactions = inst->getTransactions();
      unsigned int count = 0;
      TemporalAdvisorId temporalAdvisor = getPlanDatabase()->getTemporalAdvisor();
      std::set<std::pair<TransactionId, TransactionId> > uniquePairs;

      for(std::set<TransactionId>::const_iterator preIt = transactions.begin(); preIt != transactions.end() && count < limit; ++preIt) {
        TransactionId predecessor = *preIt;
        check_error(predecessor.isValid());
        //for(std::set<TransactionId>::const_iterator sucIt = transactions.begin(); sucIt != transactions.end() && count < limit; ++sucIt) {
        std::vector<ConstrainedVariableId> sucTimevars;
        for(std::map<TransactionId, TokenId>::const_iterator sucIt = m_transactionsToTokens.begin(); sucIt != m_transactionsToTokens.end() && count < limit; ++sucIt) {
          TransactionId successor = sucIt->first;
          check_error(successor.isValid());
          sucTimevars.push_back(TimeVarId(successor->time()));
        }
        std::vector<eint> presucLbs;
        std::vector<eint> presucUbs;
        temporalAdvisor->getTemporalDistanceSigns(TimeVarId(predecessor->time()),
                                                  sucTimevars, presucLbs, presucUbs);
        int i = 0;

        for(std::map<TransactionId, TokenId>::const_iterator sucIt = m_transactionsToTokens.begin(); sucIt != m_transactionsToTokens.end() && count < limit; ++sucIt) {
          TransactionId successor = sucIt->first;
          check_error(successor.isValid());

          bool canPrecede = (presucUbs[i] >= 0);//temporalAdvisor->canPrecede(TimeVarId(predecessor->time()), TimeVarId(successor->time()));
          bool mustPrecede = (presucLbs[i] >= 0);
          bool canFollow = (presucLbs[i] <= 0);
          bool mustFollow = (presucUbs[i] <= 0);
          i++;

          debugMsg("Resource:getOrderingChoices", "Considering pair <" << predecessor->toString() << ", " << successor->toString());
          if(predecessor == successor || !predecessor->time()->lastDomain().intersects(successor->time()->lastDomain())) {
            condDebugMsg(predecessor == successor, "Resource:getOrderingChoices", "Rejected pair because they are the same transaction.");
            condDebugMsg(!predecessor->time()->lastDomain().intersects(successor->time()->lastDomain()), "Resource:getOrderingChoices",
                         "Rejected pair because successor does not overlap predecessor.");
            continue;
          }
          //if(temporalAdvisor->canPrecede(TimeVarId(predecessor->time()), TimeVarId(successor->time())) &&
             //!transConstrainedToPrecede(predecessor, successor)) {
            // //results.push_back(std::make_pair(predecessor, successor));
          if (canPrecede && !mustPrecede) {
            bool added = uniquePairs.insert(std::make_pair(predecessor, successor)).second;
            if(added) {
              debugMsg("Resource:getOrderingChoices", "Added pair <" << predecessor->toString() << ", " << successor->toString());
              count++;
            }
            else {
              debugMsg("Resource:getOrderingChoices", "Pair is redundant.");
            }
          }
          else {
            //condDebugMsg(transConstrainedToPrecede(predecessor, successor), "Resource:getOrderingChoices", "Rejected pair because predecessor already constrained to precede successor.");
            //condDebugMsg(!temporalAdvisor->canPrecede(TimeVarId(predecessor->time()), TimeVarId(successor->time())), "Resource:getOrderingChoices",
                         //"Rejected pair because predecessor cannot precede successor.");
            condDebugMsg(mustPrecede, "Resource:getOrderingChoices", "Rejected pair because predecessor already constrained to precede successor.");
            condDebugMsg(!canPrecede, "Resource:getOrderingChoices",
                         "Rejected pair because predecessor cannot precede successor.");
          }
          debugMsg("Resource:getOrderingChoices", "Considering pair <" << successor->toString() << ", " << predecessor->toString());
          //if(temporalAdvisor->canPrecede(TimeVarId(successor->time()), TimeVarId(predecessor->time())) &&
             //!transConstrainedToPrecede(successor, predecessor)) {
            // //results.push_back(std::make_pair(successor, predecessor));
          if (canFollow && !mustFollow) {
            bool added = uniquePairs.insert(std::make_pair(successor, predecessor)).second;
            if(added) {
              debugMsg("Resource:getOrderingChoices", "Added pair <" << successor->toString() << ", " << predecessor->toString());
              count++;
            }
            else {
              debugMsg("Resource:getOrderingChoices", "Pair is redundant.");
            }
          }
          else {
            //condDebugMsg(transConstrainedToPrecede(successor, predecessor), "Resource:getOrderingChoices", "Rejected pair because predecessor already constrained to precede successor.");
            //condDebugMsg(!temporalAdvisor->canPrecede(TimeVarId(successor->time()), TimeVarId(predecessor->time())), "Resource:getOrderingChoices",
                         //"Rejected pair because predecessor cannot precede successor.");
            condDebugMsg(mustFollow, "Resource:getOrderingChoices", "Rejected pair because predecessor already constrained to precede successor.");
            condDebugMsg(!canFollow, "Resource:getOrderingChoices",
                         "Rejected pair because predecessor cannot precede successor.");
          }
        }
      }
      results.insert(results.end(), uniquePairs.begin(), uniquePairs.end());
      debugMsg("Resource:getOrderingChoices", "Ultimately found " << results.size() << " orderings.");
  }

  bool Resource::transConstrainedToPrecede(const TransactionId& predecessor, const TransactionId& successor) {
    IntervalIntDomain dom = getPlanDatabase()->getTemporalAdvisor()->getTemporalDistanceDomain(TimeVarId(predecessor->time()), TimeVarId(successor->time()), true);
    return dom.getLowerBound() >= 0;
    //       ConstrainedVariableId pre = predecessor->time();
    //       ConstrainedVariableId suc = successor->time();

    //       std::set<ConstraintId> preConstrs, sucConstrs, intersection;
    //       pre->constraints(preConstrs);
    //       suc->constraints(sucConstrs);

    //       std::set_intersection(preConstrs.begin(), preConstrs.end(),
    //                             sucConstrs.begin(), sucConstrs.end(),
    //                             inserter(intersection, intersection.begin()));

    //       for(std::set<ConstraintId>::const_iterator it = intersection.begin(); it != intersection.end(); ++it) {
    //         ConstraintId constr = *it;
    //         check_error(constr.isValid());
    //         const std::vector<ConstrainedVariableId>& scope = constr->getScope();
    //         if(scope.size() == 2 && scope[0] == pre) {
    //           std::string name = constr->getName().toString();
    //           if(name == "precedes" || name == "concurrent" || name == "eq" || name == "lt" || name == "leq")
    //             return true;
    //         }
    //         else if(scope.size() == 3 && ((scope[0] == pre && scope[2] == suc) || (scope[2] == pre && scope[0] == suc && scope[1]->lastDomain().getUpperBound() <= 0))) {
    //           std::string name = constr->getName().toString();
    //           if(name == "addeq" || name == "addEq" || name == "temporaldistance" || name == "temporalDistance")
    //             return true;
    //         }
    //       }
    //       return false;
  }

  // PS Methods:
  PSResourceProfile* Resource::getLimits() {
    return new PSResourceProfile(cast_double(getLowerLimit()), cast_double(getUpperLimit()));
  }

  PSResourceProfile* Resource::getLevels() {
    return new PSResourceProfile(getProfile());
  }

  PSList<PSEntityKey> Resource::getOrderingChoices(TimePoint t)
  {
    PSList<PSEntityKey> retval;

    InstantId instant;

    ProfileIterator it(getProfile());
    while(!it.done()) {
      TimePoint inst = (TimePoint) cast_basis(it.getTime());
      if (inst == t) {
        instant = it.getInstant();
        break;
      }
      it.next();
    }

    if (instant.isNoId()) {
      // TODO: log error
      return retval;
    }

    std::vector<std::pair<TransactionId, TransactionId> > results;
    getOrderingChoices(instant,results);
    for (unsigned int i = 0;i<results.size(); i++) {
      TransactionId predecessor = results[i].first;
      TransactionId successor = results[i].second;
      retval.push_back(cast_int(predecessor->time()->parent()->getKey()));
      retval.push_back(cast_int(successor->time()->parent()->getKey()));
    }

    return retval;
  }
}
