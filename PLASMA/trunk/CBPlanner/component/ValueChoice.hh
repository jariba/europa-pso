#ifndef _H_ValueChoice
#define _H_ValueChoice

#include "CBPlannerDefs.hh"
#include "Choice.hh"

namespace Prototype {

  class ValueChoice : public Choice {
  public:
    virtual ~ValueChoice();
    
    const double getValue() const;
    const TokenId& getToken() const;

    bool operator==(const Choice& choice) const;

    void print(std::ostream& os) const;

  private:
    friend class Choice;

    ValueChoice(const DecisionPointId&, const double);
    ValueChoice(const DecisionPointId&, const double, const TokenId&);

    double m_value;		/**< One value of a domain. */
    TokenId m_token;
  };

}
#endif 
