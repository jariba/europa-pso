#ifndef INCREMENTAL_FLOW_PROFILE_HEADER__
#define INCREMENTAL_FLOW_PROFILE_HEADER__

/**
 * @file SAVH_IncrementalFlowProfile.hh
 * @author David Rijsman
 * @brief Defines the public interface for a maximum flow algorithm
 * @date April 2006
 * @ingroup Resource
 */

#include "SAVH_FlowProfile.hh"

namespace EUROPA 
{
  namespace SAVH 
  {
    class IncrementalFlowProfile:
      public FlowProfile
    {
    public:
      /**
       * @brief 
       */
      IncrementalFlowProfile( const PlanDatabaseId db, const FVDetectorId flawDetector, const double initLevelLb = 0, const double initLevelUb = 0 );
      /**
       * @brief 
       */
      virtual ~IncrementalFlowProfile();
      /**
       * @brief 
       */
      void initRecompute( InstantId inst );
      /**
       * @brief 
       */
      void initRecompute();
      /**
       * @brief 
       */
      void recomputeLevels( InstantId prev, InstantId inst );
      /**
       * @brief 
       */
      void enableOrderings(  const InstantId& inst  );
    private:
      void recomputeLevels( InstantId inst, double lowerLevel, double upperLevel );


    };
  }
}

#endif //INCREMENTAL_FLOW_PROFILE_HEADER__
