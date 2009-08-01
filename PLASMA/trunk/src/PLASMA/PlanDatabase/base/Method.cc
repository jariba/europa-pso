/*
 * Method.cc
 *
 *  Created on: Jul 31, 2009
 *      Author: javier
 */

#include "Method.hh"

namespace EUROPA {

Method::Method(const char* name)
    : m_name(name)
{
}

Method::~Method()
{
}

const LabelStr& Method::getName() const
{
    return m_name;
}

std::string Method::toString() const
{
    std::ostringstream os;

    // TODO: dump signature and return type
    os << m_name.c_str();

    return os.str();
}

}
