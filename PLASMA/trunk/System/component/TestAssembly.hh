#ifndef _H_TestAssembly
#define _H_TestAssembly

/**
 * @file   TestAssembly.hh
 * @author Tania Bedrax-Weiss
 * @date   Mon Jan 10 16:46:40 2005
 * @brief  
 * @ingroup System
 */

#include "PlanDatabaseDefs.hh"
#include "RulesEngineDefs.hh"
//#include "CBPlanner.hh"
#include "Solver.hh"
#include "StandardAssembly.hh"
#include "DbClientTransactionLog.hh"

#ifndef TIXML_USE_STL
#define TIXML_USE_STL
#endif
#include "tinyxml.h"

namespace EUROPA {

#define PPW_WITH_PLANNER

  class TestAssembly : public StandardAssembly {
  public:
    TestAssembly(const SchemaId& schema);
    virtual ~TestAssembly();
    /**
     * @brief Sets up the necessary constraint factories. The assembly must be explicitly
     * initialized before use.  Mostly calls StandardAssembly, but needs
     * some special constraint factories for contraints needed in test
     * cases.
     */
    static void initialize();

    bool plan(const char* txSource, const char* config, const char* averFile = NULL);

    /**
     * @brief Invoke the planner. Calls playTransactions(txSource).
     * @param txSource The source from which we get the initial state
     * @param enableTransactionLogging When true, we log transactions so we
     * can replay them for test purposes only.
     * @return The result of planning
     * @see CBPlanner::Status
     */
    bool plan(const char* txSource, const TiXmlElement& config, const char* averFile = NULL);

    /** 
     * @brief Replays the transaction log and verifies that the outputs are
     * the same. Useful for testing.
     * 
     * @param txLog The transaction log.
     */    
    void replay(const DbClientTransactionLogId& txLog);

    const PlanDatabaseId& getPlanDatabase() const;

    const unsigned int getTotalNodesSearched() const;
    const unsigned int getDepthReached() const;

  private:
    unsigned int m_totalNodes;
    unsigned int m_finalDepth;
  };
}
#endif
