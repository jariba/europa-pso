#ifndef _H_DbLogger
#define _H_DbLogger

#include "PlanDatabaseListener.hh"
#include <iostream>

namespace Prototype{

  class DbLogger: public PlanDatabaseListener {
  public:
    DbLogger(ostream& os, const PlanDatabaseId& planDatabase);
    ~DbLogger();
    void notifyAdded(const ObjectId& object);
    void notifyRemoved(const ObjectId& object);
    void notifyAdded(const TokenId& token);
    void notifyRemoved(const TokenId& token);
    void notifyClosed(const TokenId& token);
    void notifyActivated(const TokenId& token);
    void notifyDeactivated(const TokenId& token);
    void notifyMerged(const TokenId& token);
    void notifySplit(const TokenId& token);
    void notifyRejected(const TokenId& token);
    void notifyReinstated(const TokenId& token);
    void notifyAdded(const ObjectId& object, const TokenId& token);
    void notifyRemoved(const ObjectId& object, const TokenId& token);
  private:
    ostream& m_os;
  };

}

#endif
