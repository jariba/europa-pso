#include "Constraints.hh"
#include "Constraint.hh"
#include "ConstraintEngine.hh"
#include "ConstrainedVariable.hh"
#include "IntervalIntDomain.hh"
#include "BoolDomain.hh"
#include "Domain.hh"
#include "Utils.hh"
#include "ResourceDefs.hh"
#include "Resource.hh"
#include "ResourceTransactionConstraint.hh"

namespace Prototype
{
  
  ResourceTransactionConstraint::ResourceTransactionConstraint(const LabelStr& name,
					 const LabelStr& propagatorName,
					 const ConstraintEngineId& constraintEngine,
					 const std::vector<ConstrainedVariableId>& variables)
    : Constraint(name, propagatorName, constraintEngine, variables){
    check_error(variables.size() == ARG_COUNT);
    //@todo add type checking of each variable in the constraint
  }

  void ResourceTransactionConstraint::handleExecute()
  {
    Domain<ResourceId>& domTO = static_cast<Domain<ResourceId>&> (getCurrentDomain(m_variables[TO]));
    IntervalIntDomain& domTH = static_cast<IntervalIntDomain&> (getCurrentDomain(m_variables[TH]));
    IntervalDomain& domTQ = static_cast<IntervalDomain&> (getCurrentDomain(m_variables[TQ]));


    std::list<ResourceId> objs;
    domTO.getValues(objs);
    int hMin = LATEST_TIME;
    int hMax = -LATEST_TIME;

    for(std::list<ResourceId>::const_iterator it = objs.begin(); it != objs.end(); ++it) {
      int oMin = (*it)->getHorizonStart();
      int oMax = (*it)->getHorizonEnd();
      IntervalIntDomain oHorizon(oMin, oMax);
      if (oHorizon.intersect(domTH.getLowerBound(), domTH.getUpperBound()) && oHorizon.isEmpty())
	domTO.remove((*it));
      else {
	if ( oMin < hMin)
	  hMin = oMin;
	if (hMax < oMax)
	  hMax = oMax;
	//std::cout << hMin << " : " << hMax << std::endl;
      }
    }
    //std::cout << hMin << " : " << hMax << std::endl;
    domTH.intersect(hMin, hMax);

    if (!domTO.isEmpty()) {      
      objs.clear();      
      domTO.getValues(objs);
      double qMin =  -LARGEST_VALUE;
      double qMax =  LARGEST_VALUE;
      for(std::list<ResourceId>::const_iterator it = objs.begin(); it != objs.end(); ++it) {
    	double oMin = (*it)->getConsumptionRateMax();
    	double oMax = (*it)->getProductionRateMax();
	std::cout << oMin << " : " << oMax << std::endl;
	std::cout << domTQ.getLowerBound() << " : " << domTQ.getUpperBound() << std::endl;
    	IntervalDomain oQty(oMin, oMax);
    	if (oQty.intersect(domTQ.getLowerBound(), domTQ.getUpperBound()) && oQty.isEmpty()) {
	  std::cout << "Throwing out an object " << (*it)  << std::endl;
    	  domTO.remove((*it));
	}
    	else {
    	  if ( oMin > qMin)
    	    qMin = oMin;
    	  if (qMax > oMax)
    	    qMax = oMax;
	  //  std::cout << qMin << " : " << qMax << std::endl;
    	}
      }
      //      std::cout << qMin << " : " << qMax << std::endl;
      domTQ.intersect(qMin, qMax);
    }
      
  }

  bool ResourceTransactionConstraint::canIgnore(const ConstrainedVariableId& variable, 
				     int argIndex, 
				     const DomainListener::ChangeType& changeType){
    return(false);
  }

}//namespace prototype
