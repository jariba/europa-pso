#ifndef _H_DbClientTransactionLog
#define _H_DbClientTransactionLog

#include "DbClientListener.hh"
#include <list>
#include <vector>
#include <string>
#include <iostream>

class TiXmlElement;

/**
 * @file DbClientTransactionLog
 * @brief Main interface for logging transactions. Necessary for copy. replay, and possibly recovery.
 */

namespace PLASMA {

  class DbClientTransactionLog: public DbClientListener {
  public:
    DbClientTransactionLog(const DbClientId& client, bool chronologicalBacktracking = true);
    ~DbClientTransactionLog();

    /* Declare DbClient event handlers we will over-ride */
    void notifyObjectCreated(const ObjectId& object);
    void notifyObjectCreated(const ObjectId& object, const std::vector<ConstructorArgument>& arguments);
    void notifyClosed();
    void notifyClosed(const LabelStr& objectType);
    void notifyTokenCreated(const TokenId& token);
    void notifyConstrained(const ObjectId& object, const TokenId& token, const TokenId& successor);
    void notifyFreed(const ObjectId& object, const TokenId& token);
    void notifyActivated(const TokenId& token);
    void notifyMerged(const TokenId& token, const TokenId& activeToken);
    void notifyRejected(const TokenId& token);
    void notifyCancelled(const TokenId& token);
    void notifyConstraintCreated(const ConstraintId& constraint);
    void notifyVariableCreated(const ConstrainedVariableId& variable);
    void notifyVariableSpecified(const ConstrainedVariableId& variable);
    void notifyVariableReset(const ConstrainedVariableId& variable);

    /**
     * @brief Flush all buffered transactions to an output stream and clear the buffer. Handy for checkpointing.
     */
    void flush(std::ostream& os);

  private:
    friend class DbClientTransactionPlayer;
    const std::list<TiXmlElement*>& getBufferedTransactions() const;

    TiXmlElement * allocateXmlElement(const std::string&) const;
    void pushTransaction(TiXmlElement *);
    void popTransaction();

    std::list<TiXmlElement*> m_bufferedTransactions;
    bool m_chronologicalBacktracking;
    int m_tokensCreated;

  //! string output functions

    /** 
     * @brief create a string to describe a value, given its domain
     */
    std::string domainValueAsString(const AbstractDomain * domain, double value);

  //! XML output functions

    /** 
     * @brief create an xml element to represent a value, given its domain
     */
    TiXmlElement * domainValueAsXml(const AbstractDomain * domain, double value);

    /** 
     * @brief create an xml element to represent a domain
     */
    TiXmlElement * abstractDomainAsXml(const AbstractDomain * domain);

    /** 
     * @brief create an xml element to represent a token
     */
    TiXmlElement * tokenAsXml(const TokenId& token) const;

    /** 
     * @brief create an xml element to represent a variable
     */
    TiXmlElement * variableAsXml(const ConstrainedVariableId& variable) const;
  };
}
#endif
