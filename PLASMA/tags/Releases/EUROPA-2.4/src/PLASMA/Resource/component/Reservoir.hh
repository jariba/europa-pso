#ifndef _H_Reservoir
#define _H_Reservoir

#include "ResourceDefs.hh"
#include "Resource.hh"
#include <map>
#include <vector>

namespace EUROPA {

    class Reservoir : public Resource {
    public:
      /**
       * @brief Constructor.  This is primarily a convenience constructor for resource interactions outside of a model.
       * @param planDatabase @see Object
       * @param type @see Object
       * @param name @see Object
       * @param initCapacityLb The lower bound of the Resource's initial level. (For example, a battery may be only half charged initially.)
       * @param initCapacityUb The upper bound of the Resource's initial level. (For example, a battery may be only half charged initially.)
       * @param lowerLimit The lower capacity limit. (For example, a battery can't have less than no energy stored, or it might be considered unsafe to allow
       * the capacity to get below 25%.)
       * @param upperLimit The upper capacity limit. (For example, a battery has a maximum charge, or it might be unsafe to fill a beaker more than three-quarters full.)
       * @param maxInstProduction The maximum amount of production possible at an instant. (For example, a power bus might only allow 2.5A to be drawn at one instant.)
       * @param maxInstConsumption The maximum amount of consumption possible at an instant. (For example, a power bus might only allow 2.5A to be drawn at one instant.)
       * @param maxProduction The maximum amount of production possible on this resource.
       * @param maxConsumption The maximum amount of consumption possible on this resource.
       */
      Reservoir(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name, const LabelStr& detectorName, const LabelStr& profileName,
	       edouble initCapacityLb = 0, edouble initCapacityUb = 0, edouble lowerLimit = MINUS_INFINITY,
	       edouble upperLimit = PLUS_INFINITY, edouble maxInstProduction = PLUS_INFINITY, edouble maxInstConsumption = PLUS_INFINITY,
	       edouble maxProduction = PLUS_INFINITY, edouble maxConsumption = PLUS_INFINITY);

      /**
       * @brief Constructor
       * @see Resource
       */
      Reservoir(const PlanDatabaseId& planDatabase, const LabelStr& type, const LabelStr& name, bool open);

      /**
       * @brief Constructor
       * @see Resource
       */
      Reservoir(const ObjectId& parent, const LabelStr& type, const LabelStr& localName, bool open);

      //~Reservoir();

      void getOrderingChoices(const TokenId& token,
			      std::vector<std::pair<TokenId, TokenId> >& results,
			      unsigned int limit = std::numeric_limits<unsigned int>::max());

//       void getTokensToOrder(std::vector<TokenId>& results);


    private:
      //void notifyViolated(const InstantId inst);
      //void notifyFlawed(const InstantId inst);
      //void notifyNoLongerFlawed(const InstantId inst);
      //void notifyDeleted(const InstantId inst);
      void addToProfile(const TokenId& tok);
      void removeFromProfile(const TokenId& tok);
      void createTransactions(const TokenId& tok);
      void removeTransactions(const TokenId& tok);

      std::map<TokenId, TransactionId> m_tokensToTransactions;
    };
}

#endif
