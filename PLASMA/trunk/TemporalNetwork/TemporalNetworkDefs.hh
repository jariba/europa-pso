#ifndef _H_TemporalNetworkDefs
#define _H_TemporalNetworkDefs

#include "PlanDatabaseDefs.hh"

/*!< Type definitions to map for th eones we were using in Europa */
namespace Prototype {
  typedef bool Bool;
  typedef Bool Boolean;
  typedef int Int;
  typedef char Char;
  typedef short Short;
  typedef double Double;
  typedef float Float;
  typedef long Long;
  typedef unsigned Unsigned;
  typedef void Void;
  typedef int Index;
  typedef Long Time;        // Temporal distance value.

  class Tnode;
  typedef Id< Tnode > TimepointId;

  class Tspec;
  typedef Id<Tspec> TemporalConstraintId;

  class TemporalNetwork;
  typedef Id<TemporalNetwork> TemporalNetworkId; 

  class TemporalPropagator;
  typedef Id<TemporalPropagator> TemporalPropagatorId;

  class TemporalNetworkListener;
  typedef Id<TemporalNetworkListener> TemporalNetworkListenerId;

  class TimepointWrapper;
  typedef Id<TimepointWrapper> TimepointWrapperId;

  #define noIndex -1;

}
#endif
