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
#include "SAVH_FlowProfile.hh"
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
    FlowProfileGraph::FlowProfileGraph( const SAVH::TransactionId& source, const SAVH::TransactionId& sink, bool lowerlevel ):
      m_lowerLevel( lowerlevel ),
      m_recalculate( false ),
      m_graph( 0 ),
      m_source( 0 ),
      m_sink( 0 )
    {
      m_graph = new SAVH::Graph();
      m_source = m_graph->createNode( source );
      m_sink = m_graph->createNode( sink );
    }

    FlowProfileGraph::~FlowProfileGraph()
    {
      delete m_graph;
      m_graph = 0;
      
      m_source = 0;
      m_sink = 0;
    }

    void FlowProfileGraph::enableAt( const SAVH::TransactionId& t1, const SAVH::TransactionId& t2 ) 
    {
      m_graph->createEdge( t1, t2, Edge::getMaxCapacity() );
      m_graph->createEdge( t2, t1, Edge::getMaxCapacity() );
    }

    void FlowProfileGraph::enableAtOrBefore( const SAVH::TransactionId& t1, const SAVH::TransactionId& t2 ) 
    {
      check_error( 0 != m_graph->getNode( t1 ) );
      check_error( 0 != m_graph->getNode( t2 ) );

      m_graph->createEdge( t1, t2, 0 );
      m_graph->createEdge( t2, t1, Edge::getMaxCapacity() );
    }

    void FlowProfileGraph::enableTransaction( const SAVH::TransactionId& t )
    {
      m_recalculate = true;

      m_graph->createNode( t, true );
      
      SAVH::TransactionId source = SAVH::TransactionId::noId();
      SAVH::TransactionId target = SAVH::TransactionId::noId();

      if( ( m_lowerLevel && t->isConsumer() )
	  ||
	  (!m_lowerLevel && !t->isConsumer() ) )
	{
	  // connect to the source of the graph
	  source = m_source->getIdentity();
	  target = t;
	}
      else
	{
	  // connect to the sink of the graph
	  source = t;
	  target = m_sink->getIdentity();
	}
      
      check_error( SAVH::TransactionId::noId() != source );
      check_error( SAVH::TransactionId::noId() != target );
      
      double edgeCapacity = t->quantity()->lastDomain().getUpperBound();

      m_graph->createEdge( source, target, edgeCapacity );
      m_graph->createEdge( target, source, 0 );
    }

    void FlowProfileGraph::removeTransaction( const SAVH::TransactionId& id )
    {
      checkError( 0 != m_graph->getNode( id ),
		  "Trying to remove transaction (" 
		  << id << ") which is not in the graph!");

      m_graph->removeNode( id );
    }

    void FlowProfileGraph::reset()
    {
      m_recalculate = true;

      m_graph->setDisabled();
      
      m_sink->setEnabled();
      m_source->setEnabled(); 
    }

    double FlowProfileGraph::getResidualFromSource()
    {
      double residual = 0.0;

      if( m_recalculate )
	{
	  MaximumFlowAlgorithm maxflow( m_graph, m_source, m_sink );
	
	  maxflow.execute();

	  EdgeOutIterator ite( *m_source );

	  for( ; ite.ok(); ++ite )
	    {
	      Edge* edge = *ite;
	    
	      residual += maxflow.getResidual( edge );
	    }

	  m_recalculate = false;
	}

      return residual;
    }


    //-------------------------------

    FlowProfile::FlowProfile( const PlanDatabaseId db, const FVDetectorId flawDetector, const double initLevelLb, const double initLevelUb): 
      Profile( db, flawDetector, initLevelLb, initLevelUb),
      m_lowerLevelGraph( 0 ),
      m_upperLevelGraph( 0 )
    {
      m_recomputeInterval = (new ProfileIterator(getId()))->getId();

      // every node in the maximum flow graph is identified by the id of the associated Transaction
      // we make a dummy transaction for the source nodes of the graphs
      m_dummySourceTransaction = ( new SAVH::Transaction( Variable<IntervalIntDomain>( db->getConstraintEngine(), IntervalIntDomain(0, 0) ).getId(),
							  Variable<IntervalIntDomain>( db->getConstraintEngine(), IntervalIntDomain(0, 0) ).getId(),
							  false ) )->getId();

      // every node in the maximum flow graph is identified by the id of the associated Transaction
      // we make a dummy transaction for the sink nodes of the graphs
      m_dummySinkTransaction = ( new SAVH::Transaction(  Variable<IntervalIntDomain>( db->getConstraintEngine(), IntervalIntDomain(0, 0) ).getId(),
							 Variable<IntervalIntDomain>( db->getConstraintEngine(), IntervalIntDomain(0, 0) ).getId(),
							 false ) )->getId();

      m_lowerLevelGraph = new FlowProfileGraph( m_dummySourceTransaction, m_dummySinkTransaction, true );
      m_upperLevelGraph = new FlowProfileGraph( m_dummySourceTransaction, m_dummySinkTransaction, false );
    }

    FlowProfile::~FlowProfile() 
    {

      delete m_lowerLevelGraph;
      m_lowerLevelGraph = 0;

      delete m_upperLevelGraph;
      m_upperLevelGraph = 0;

      delete (SAVH::Transaction*) m_dummySinkTransaction;
      delete (SAVH::Transaction*) m_dummySourceTransaction;
    }

    void FlowProfile::initRecompute( InstantId inst ) 
    {
      check_error(inst.isValid());

      debugMsg("FlowProfile:initRecompute","Instant (" << inst->getId() << ")");
    }

    void FlowProfile::initRecompute() 
    {
      checkError(m_recomputeInterval.isValid(), "Attempted to initialize recomputation without a valid starting point!");
      
      debugMsg("FlowProfile:initRecompute","");

      m_lowerLevelGraph->reset();
      m_upperLevelGraph->reset();
      
      // initial level
      m_lowerClosedLevel = m_initLevelLb;

      // initial level
      m_upperClosedLevel = m_initLevelUb;
    }

    void FlowProfile::recomputeLevels( InstantId prev, InstantId inst ) 
    {
      check_error( prev.isValid() || InstantId::noId() == prev );
      check_error( inst.isValid() );

      debugMsg("FlowProfile:recomputeLevels","Instant (" 
	       << inst->getId() << ") at time "
	       << inst->getTime() );

      const std::set<TransactionId>& transactions = inst->getTransactions();

      std::set<TransactionId>::const_iterator iter = transactions.begin();
      std::set<TransactionId>::const_iterator end = transactions.end();

      for( ; iter != end; ++iter )
	{
	  const TransactionId& transaction1 = (*iter);
	
	  // inst->getTransactions returns all transaction overlapping inst->getTime
	  // right inclusive
	  if( transaction1->time()->lastDomain().getUpperBound() == inst->getTime() )  
	    {
	      m_lowerLevelGraph->removeTransaction( transaction1 );
	      m_upperLevelGraph->removeTransaction( transaction1 );

	      // if upperbound equals the instant time the transaction enters the closed set
  	      if( transaction1->isConsumer() ) 
		{
		  m_upperClosedLevel -= transaction1->quantity()->lastDomain().getLowerBound();
		  m_lowerClosedLevel -= transaction1->quantity()->lastDomain().getUpperBound();
		}
	      else 
		{
		  m_upperClosedLevel += transaction1->quantity()->lastDomain().getUpperBound();
		  m_lowerClosedLevel += transaction1->quantity()->lastDomain().getLowerBound();
		}
	    }
	  else
	    {
	      if( transaction1->time()->lastDomain().getLowerBound() == inst->getTime() )  
		{
		  enableTransaction( transaction1 );
  
		  std::set<TransactionId>::const_iterator secondIter = transactions.begin();
		  
		  for( ; secondIter != end; ++secondIter ) 
		    {
		      const TransactionId& transaction2 = (*secondIter);
		      
		      if( transaction1 != transaction2 ) 
			{
			  if( transaction2->time()->lastDomain().getUpperBound() != inst->getTime() )
			    {
			      debugMsg("FlowProfile:recomputeLevels","Transaction (" 
				       << transaction1->getId() << ") "
				       << transaction1->time()->toString() << " and Transaction ("
				       << transaction2->getId() << ") "
				       << transaction2->time()->toString() );
			      
			      if( isConstrainedToAt( transaction1, transaction2 ) ) 
				{
				  handleOrderedAt( transaction1, transaction2 );
				}
			      else if( isConstrainedToBeforeOrAt( transaction1, transaction2 ) )  
				{
				  handleOrderedAtOrBefore( transaction1, transaction2 );
				}
			      else 
				{
				  debugMsg("FlowProfile::recomputeLevels","Transaction (" 
					   << transaction1->getId() << ") and Transaction ("
					   << transaction2->getId() << ") not constrained");
				}
			    }
			}
		    }
		}
	    }
	}


      debugMsg("FlowProfile::recomputeLevels","Computing lower level for instance at time "
	       << inst->getTime() << " closed level is "
	       << m_lowerClosedLevel );

      double lowerboundIncrement = m_lowerClosedLevel - m_lowerLevelGraph->getResidualFromSource();
      double upperboundIncrement = m_upperClosedLevel + m_upperLevelGraph->getResidualFromSource();

      debugMsg("FlowProfile::recomputeLevels","Computed levels for instance at time "
	       << inst->getTime() << "["
	       << lowerboundIncrement << "," 
	       << upperboundIncrement << "]");

      inst->update( lowerboundIncrement, lowerboundIncrement, upperboundIncrement, upperboundIncrement, 
		    0, 0, 0, 0, 
		    0, 0, 0, 0, 
		    0, 0, 0, 0 );
    }

    bool FlowProfile::isConstrainedToBeforeOrAt( const TransactionId t1, const TransactionId t2 ) 
    {
      check_error(t1.isValid());
      check_error(t2.isValid());

      const IntervalIntDomain distance = m_planDatabase->getTemporalAdvisor()->getTemporalDistanceDomain( t1->time(), t2->time(), true );
      
      return distance.getLowerBound() >= 0;
    }

    bool FlowProfile::isConstrainedToAt( const TransactionId t1, const TransactionId t2 ) 
    {

      const IntervalIntDomain distance = m_planDatabase->getTemporalAdvisor()->getTemporalDistanceDomain( t1->time(), t2->time(), true );

      return distance.getLowerBound() == 0 && distance.getUpperBound() == 0;
    }

    void FlowProfile::handleOrderedAt( const TransactionId t1, const TransactionId t2 ) 
    {
      check_error(t1.isValid());
      check_error(t2.isValid());
      
      debugMsg("FlowProfile:handleOrderedAt","TransactionId (" 
	       << t1->getId() << ") at TransactionId (" 
	       << t2->getId() << ")");

      m_lowerLevelGraph->enableAt( t1, t2 );
      m_upperLevelGraph->enableAt( t1, t2 );
    }

    void FlowProfile::handleOrderedAtOrBefore( const TransactionId t1, const TransactionId t2 ) 
    {
      check_error(t1.isValid());
      check_error(t2.isValid());
      
      debugMsg("FlowProfile:handleOrderedAtOrBefore","TransactionId (" 
	       << t1->getId() << ") at or before TransactionId (" 
	       << t2->getId() << ")");

      m_lowerLevelGraph->enableAtOrBefore( t1, t2 );
      m_upperLevelGraph->enableAtOrBefore( t1, t2 );
    }

    void FlowProfile::handleTransactionAdded(const TransactionId t) 
    {
      check_error(t.isValid());

      debugMsg("FlowProfile:handleTransactionAdded","TransactionId (" << t->getId() << ") " << t->time() );

      enableTransaction( t );

      if(m_recomputeInterval.isValid())
	delete (ProfileIterator*) m_recomputeInterval;
      
      m_recomputeInterval = (new ProfileIterator(getId()))->getId();
    }

    void FlowProfile::enableTransaction( const TransactionId t )
    {
      debugMsg("FlowProfile:enableTransaction","TransactionId (" << t->getId() << ")");
      
      m_lowerLevelGraph->enableTransaction( t );
      m_upperLevelGraph->enableTransaction( t );
    }
    
    void FlowProfile::resetEdgeWeights( const TransactionId t ) 
    {
      check_error(t.isValid());

      debugMsg("FlowProfile:resetEdgeWeights","TransactionId (" << t->getId() << ")");

      m_lowerLevelGraph->enableTransaction( t );
      m_upperLevelGraph->enableTransaction( t );
    }

    void FlowProfile::handleTransactionRemoved( const TransactionId t ) {
      check_error(t.isValid());

      debugMsg("FlowProfile:handleTransactionRemoved","TransactionId (" << t->getId() << ")");
      
      m_lowerLevelGraph->removeTransaction( t );
      m_upperLevelGraph->removeTransaction( t );
      
      if(m_recomputeInterval.isValid())
	delete (ProfileIterator*) m_recomputeInterval;

      // this needs to be 'smarter'
      m_recomputeInterval = (new ProfileIterator(getId()))->getId();
    }

    void FlowProfile::handleTransactionTimeChanged(const TransactionId t, const DomainListener::ChangeType& type)  
    {
      check_error(t.isValid());

      if(m_recomputeInterval.isValid())
	delete (ProfileIterator*) m_recomputeInterval;

      // this needs to be 'smarter'
      m_recomputeInterval = (new ProfileIterator(getId()))->getId();

      debugMsg("FlowProfile:handleTransactionTimeChanged","TransactionId (" << t->getId() << ") change " << type );
    }

    void FlowProfile::handleTransactionQuantityChanged(const TransactionId t, const DomainListener::ChangeType& type)
    {
      check_error(t.isValid());

      switch( type) {
      case DomainListener::UPPER_BOUND_DECREASED: 
      case DomainListener::RESET:
      case DomainListener::RELAXED: 
      case DomainListener::RESTRICT_TO_SINGLETON:
      case DomainListener::SET_TO_SINGLETON:
	{
	  resetEdgeWeights( t );
	}
      break;
      case DomainListener::LOWER_BOUND_INCREASED:
      case DomainListener::BOUNDS_RESTRICTED:
      default:
	break;
      };

      if(m_recomputeInterval.isValid())
	delete (ProfileIterator*) m_recomputeInterval;

      m_recomputeInterval = (new ProfileIterator(getId()))->getId();

      debugMsg("FlowProfile:handleTransactionQuantityChanged","TransactionId (" << t->getId() << ") change " << type );
    }

    void FlowProfile::handleTransactionsOrdered(const TransactionId t1, const TransactionId t2)
    {
      check_error(t1.isValid());
      check_error(t2.isValid());

      if(m_recomputeInterval.isValid())
	delete (ProfileIterator*) m_recomputeInterval;
      m_recomputeInterval = (new ProfileIterator(getId()))->getId();

      debugMsg("FlowProfile:handleTransactionsOrdered","TransactionId1 (" << t1->getId() << ") before TransactionId2 (" << t2->getId() << ")");

    }
  }
}
