/*
 * NddlInterpreter.h
 *
 *  Created on: Jan 21, 2009
 *      Author: javier
 */

#ifndef NDDLINTERPRETER_H_
#define NDDLINTERPRETER_H_

#include "Interpreter.hh"

namespace EUROPA {


class NddlInterpreter : public LanguageInterpreter
{
public:
    NddlInterpreter(EngineId& engine);
    virtual ~NddlInterpreter();
    virtual std::string interpret(std::istream& input, const std::string& source);

    std::string getFilename(const std::string& f);

protected:
    EngineId m_engine;

    std::vector<std::string> getIncludePath();
};


class NddlSymbolTable : public EvalContext
{
public:
    NddlSymbolTable(const EngineId& engine);
    virtual ~NddlSymbolTable();

    virtual void* getElement(const char* name) const;

    const PlanDatabaseId& getPlanDatabase() const;

    AbstractDomain* getVarType(const char* name) const;
    AbstractDomain* makeNumericDomainFromLiteral(const std::string& type,const std::string& value);

    void addError(const std::string& msg);
    std::string getErrors() const;

    // EvalContext methods
    virtual ConstrainedVariableId getVar(const char* name);

protected:
    EngineId m_engine;
    std::vector<std::string> m_errors;
};

}


#endif /* NDDLINTERPRETER_H_ */
