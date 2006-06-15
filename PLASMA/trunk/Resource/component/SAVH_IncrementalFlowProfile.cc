//  Copyright Notices

//  This software was developed for use by the U.S. Government as
//  represented by the Administrator of the National Aeronautics and
//  Space Administration. No copyright is claimed in the United States
//  under 17 U.S.C. 105.

//  This software may be used, copied, and provided to others only as
//  permitted under the terms of the contract or other agreement under
//  which it was acquired from the U.S. Government.  Neither title to nor
//  ownership of the software is hereby transferred.  This notice shall
//  remain on all copies of the software

#include "ConstrainedVariable.hh"
#include "ConstraintEngine.hh"
#include "Debug.hh"
#include "IntervalIntDomain.hh"
#include "SAVH_IncrementalFlowProfile.hh"
#include "SAVH_Graph.hh"
#include "SAVH_Instant.hh"
#include "SAVH_MaxFlow.hh"
#include "SAVH_Node.hh"
#include "SAVH_Transaction.hh"
#include "SAVH_Profile.hh"
#include "TemporalAdvisor.hh"
#include "Utils.hh"
#include "Variable.hh"

namespace EUROPA 
{
  namespace SAVH 
  {   
    IncrementalFlowProfile::IncrementalFlowProfile( const PlanDatabaseId db, const FVDetectorId flawDetector, const double initLevelLb, const double initLevelUb ):
      FlowProfile( db, flawDetector, initLevelLb, initLevelUb )
    {
      debugMsg("IncrementalFlowProfile:IncrementalFlowProfile","");
    }
     
    IncrementalFlowProfile::~IncrementalFlowProfile()
    {
    }
 
    void IncrementalFlowProfile::initRecompute() 
    {
      checkError(m_recomputeInterval.isValid(), "Attempted to initialize recomputation without a valid starting point!");
      
      debugMsg("FlowProfile:initRecompute","");

      if( m_recalculateLowerLevel )
	m_lowerLevelGraph->reset();

      if( m_recalculateUpperLevel )
	m_upperLevelGraph->reset();
      
      // initial level
      m_lowerClosedLevel = m_initLevelLb;

      // initial level
      m_upperClosedLevel = m_initLevelUb;
    }

    void IncrementalFlowProfile::enableOrderings( const InstantId& inst  )
    {
      debugMsg("IncrementalFlowProfile:enableOrderings","Instant Id (" << inst->getId() << ")");

      const std::set<TransactionId>& startingTransactions = inst->getStartingTransactions();
      
      std::set<TransactionId>::const_iterator ite = startingTransactions.begin();
      std::set<TransactionId>::const_iterator end = startingTransactions.end();

      for( ; ite != end; ++ite )
	{
	  const TransactionId& transaction1 = (*ite);

	  if( !transaction1->time()->lastDomain().isSingleton() )
	    {
	      const std::set<TransactionId>& transactions = inst->getTransactions();
	  
	      std::set<TransactionId>::const_iterator iter = transactions.begin();
	      std::set<TransactionId>::const_iterator end = transactions.end();
	  
	      for( ; iter != end; ++iter )
		{
		  const TransactionId& transaction2 = (*iter);
	      
		  if( m_recalculateLowerLevel )
		    {
		      if( transaction1 != transaction2 
			  && 
			  !transaction2->time()->lastDomain().isSingleton() 
			  &&
			  transaction2->time()->lastDomain().getUpperBound() != inst->getTime()
			  &&
			  (transaction2->time()->lastDomain().getLowerBound() == inst->getTime() || m_lowerLevelGraph->isEnabled( transaction2 ) ) )
			{
			  if( isConstrainedToAt( transaction1, transaction2 ) ) 
			    {
			      m_lowerLevelGraph->enableAt( transaction1, transaction2 );
			    }
			  else if( isConstrainedToBeforeOrAt( transaction1, transaction2 ) )  
			    {
			      m_lowerLevelGraph->enableAtOrBefore( transaction1, transaction2 );
			    }
			  else if( isConstrainedToBeforeOrAt( transaction2, transaction1 ) )  
			    {
			      m_lowerLevelGraph->enableAtOrBefore( transaction2, transaction1 );
			    }
			  else 
			    {
			      debugMsg("IncrementalFlowProfile:enableOrderings","Transaction (" 
				       << transaction1->getId() << ") and Transaction ("
				       << transaction2->getId() << ") not constrained");
			    }
			}
		    }
		  
		  if( m_recalculateUpperLevel )
		    {
		      if( transaction1 != transaction2 
			  && 
			  !transaction2->time()->lastDomain().isSingleton() 
			  &&
			  transaction2->time()->lastDomain().getUpperBound() != inst->getTime()
			  &&
			  ( transaction2->time()->lastDomain().getLowerBound() == inst->getTime() || m_upperLevelGraph->isEnabled( transaction2 ) ) )
			{
			  if( isConstrainedToAt( transaction1, transaction2 ) ) 
			    {
			      m_upperLevelGraph->enableAt( transaction1, transaction2 );
			    }
			  else if( isConstrainedToBeforeOrAt( transaction1, transaction2 ) )  
			    {
			      m_upperLevelGraph->enableAtOrBefore( transaction1, transaction2 );
			    }
			  else if( isConstrainedToBeforeOrAt( transaction2, transaction1 ) )  
			    {
			      m_upperLevelGraph->enableAtOrBefore( transaction2, transaction1 );
			    }
			  else 
			    {
			      debugMsg("IncrementalFlowProfile:enableOrderings","Transaction (" 
				       << transaction1->getId() << ") and Transaction ("
				       << transaction2->getId() << ") not constrained");
			    }
			}
		    }
		}
	    }
	}
    }
    
    void IncrementalFlowProfile::recomputeLevels( InstantId prev, InstantId inst ) 
    {
      check_error( prev.isValid() || InstantId::noId() == prev );
      check_error( inst.isValid() );

      double lowerLevel = prev == InstantId::noId() ? m_initLevelLb : prev->getLowerLevel();
      double upperLevel = prev == InstantId::noId() ? m_initLevelUb : prev->getUpperLevel();

      debugMsg("IncrementalFlowProfile::recomputeLevels","Instant (" 
	       << inst->getId() << ") at time "
	       << inst->getTime() << " start levels ["
	       << lowerLevel << "," 
	       << upperLevel << "]");

      const std::set<TransactionId>& startingTransactions = inst->getStartingTransactions();

      bool expansion = false;
      
      {
	std::set<TransactionId>::const_iterator ite = startingTransactions.begin();
	std::set<TransactionId>::const_iterator end = startingTransactions.end();
	
	for( ; ite != end; ++ite )
	  {
	    const TransactionId& started = (*ite);
	    
	    check_error( started->time()->lastDomain().getLowerBound() == inst->getTime() );

	    // if the transaction is a singleton it goes straight into the closed set
	    if( !started->time()->lastDomain().isSingleton() )
	      {
		debugMsg("IncrementalFlowProfile::recomputeLevels","Expanding graphs with transaction ("
			 << started->getId() << ")");

		expansion = true;
		
		enableTransaction( started );
	      }
	  }
	
	enableOrderings( inst );

	if( expansion )
	  {
	    if( m_recalculateLowerLevel )
	      {
		double delta = m_lowerLevelGraph->disableReachableResidualGraph();

		debugMsg("IncrementalFlowProfile::recomputeLevels","Expansion leads to delta lower level of "
			 << delta );

		lowerLevel += delta;
	      }
	    
	    if( m_recalculateUpperLevel )
	      {
		double delta = m_upperLevelGraph->disableReachableResidualGraph();

		debugMsg("IncrementalFlowProfile::recomputeLevels","Expansion leads to delta upper level of "
			 << delta );

		upperLevel += delta;
	      }
	  }
      }

      const std::set<TransactionId>& endingTransactions = inst->getEndingTransactions();

      bool contraction = false;

      {
	std::set<TransactionId>::const_iterator ite = endingTransactions.begin();
	std::set<TransactionId>::const_iterator end = endingTransactions.end();
	
	for( ; ite != end; ++ite )
	  {
	    const TransactionId& ended = (*ite);

	    if( m_recalculateLowerLevel )
	      {
		bool enteredClosedSet = false;

		// if it is still enabled it is not yet contributing to the level
		if( m_lowerLevelGraph->isEnabled( ended ) )
		  {
		    debugMsg("IncrementalFlowProfile::recomputeLevels","Contracting from lower graph transaction ("
			     << ended->getId() << ") "
			     << ended->time()->toString() << " "
			     << ended->quantity()->toString() );

		    enteredClosedSet = true;
		    contraction = true;
		    m_lowerLevelGraph->pushFlow( ended );
		    m_lowerLevelGraph->disable( ended );
		  }
		else if( ended->time()->lastDomain().isSingleton() )
		  {
		    debugMsg("IncrementalFlowProfile::recomputeLevels","Transaction ("
			     << ended->getId() << ") straight from open to closed set");

		    enteredClosedSet = true;
		  }

		if( enteredClosedSet )
		  {
		    if( ended->isConsumer() )
		      {
			lowerLevel -= ended->quantity()->lastDomain().getUpperBound();

			debugMsg("IncrementalFlowProfile::recomputeLevels","Transaction ("
				 << ended->getId() << ") decreases lower level by "
				 << ended->quantity()->lastDomain().getUpperBound() << " (new level "
				 << lowerLevel << ")");
		      }
		    else
		      {
			lowerLevel += ended->quantity()->lastDomain().getLowerBound();

			debugMsg("IncrementalFlowProfile::recomputeLevels","Transaction ("
				 << ended->getId() << ") increases lower level by "
				 << ended->quantity()->lastDomain().getLowerBound() << " (new level "
				 << lowerLevel << ")");
		      }		  
		  }
	      }

	    if( m_recalculateUpperLevel )
	      {
		bool enteredClosedSet = false;

		if( m_upperLevelGraph->isEnabled( ended ) )
		  {
		    debugMsg("IncrementalFlowProfile::recomputeLevels","Contracting from upper graph transaction ("
			     << ended->getId() << ") "
			     << ended->time()->toString() << " "
			     << ended->quantity()->toString() );

		    enteredClosedSet = true;
		    contraction = true;
		    m_upperLevelGraph->pushFlow( ended );
		    m_upperLevelGraph->disable( ended );
		  }
		else if( ended->time()->lastDomain().isSingleton() )
		  {
		    debugMsg("IncrementalFlowProfile::recomputeLevels","Transaction ("
			     << ended->getId() << ") straight from open to closed set");

		    enteredClosedSet = true;
		  }

		if( enteredClosedSet )
		  {
		    if( ended->isConsumer() )
		      {
			upperLevel -= ended->quantity()->lastDomain().getLowerBound();

			debugMsg("IncrementalFlowProfile::recomputeLevels","Transaction ("
				 << ended->getId() << ") decreases upper level by "
				 << ended->quantity()->lastDomain().getLowerBound() << " (new level "
				 << upperLevel << ")");
		      }
		    else
		      {
			upperLevel += ended->quantity()->lastDomain().getUpperBound();

			debugMsg("IncrementalFlowProfile::recomputeLevels","Transaction ("
				 << ended->getId() << ") increases upper level by "
				 << ended->quantity()->lastDomain().getUpperBound() << " (new level "
				 << upperLevel << ")");		      }
		  }
	      }
	  }

	if( contraction )
	  {
	    if( m_recalculateLowerLevel )
	      {
		m_lowerLevelGraph->restoreFlow();

		double delta = m_lowerLevelGraph->disableReachableResidualGraph();//getResidualFromSource(); 

		debugMsg("IncrementalFlowProfile::recomputeLevels","Contraction leads to delta lower level of "
			 << delta );

		lowerLevel += delta;
	      }

	    if( m_recalculateUpperLevel )
	      {
		m_upperLevelGraph->restoreFlow();

		double delta = m_upperLevelGraph->disableReachableResidualGraph();//getResidualFromSource();

		debugMsg("IncrementalFlowProfile::recomputeLevels","Contraction leads to delta upper level of "
			 << delta );

		upperLevel += delta;
	      }
	  }
      }
  

      debugMsg("IncrementalFlowProfile::recomputeLevels","Computed levels for instance at time "
	       << inst->getTime() << "["
	       << lowerLevel << "," 
	       << upperLevel << "]");

      inst->update( lowerLevel, lowerLevel, upperLevel, upperLevel, 
		    0, 0, 0, 0, 
		    0, 0, 0, 0, 
		    0, 0, 0, 0 );      
    }
  }
}
