#ifndef _H_PlanDatabaseWriter
#define _H_PlanDatabaseWriter

#include "Timeline.hh"
#include "PlanDatabase.hh"
#include "Token.hh"
#include "ConstraintEngine.hh"
#include "IntervalIntDomain.hh"
#include "PlanDatabaseDefs.hh"
#include "TokenVariable.hh"
#include "ConstrainedVariable.hh"
#include "TestSupport.hh"

namespace Prototype {

  class PlanDatabaseWriter {

  public:
    static void write(PlanDatabaseId db, std::ostream& os) {
      check_error(!db->getConstraintEngine()->provenInconsistent());
      ObjectSet objs = db->getObjects();
      TokenSet alltokens = db->getTokens();
      for (ObjectSet::const_iterator oit = objs.begin(); oit != objs.end() ; ++oit) {
	if (TimelineId::convertable((*oit))) {
	  TimelineId timeline = (*oit);
	  os << timeline->getType().toString() << ":" << timeline->getName().toString() << "*************************" << std::endl;
	  std::list<TokenId> toks = timeline->getTokenSequence();
	  for(std::list<TokenId>::const_iterator tokit = toks.begin(); tokit != toks.end(); ++tokit) {
	    TokenId t = (*tokit);
	    alltokens.erase(t);
	    writeToken(t, os);
	  }
	  os << "End Timeline: " << timeline->getName().toString() << "*************************" << std::endl;
	}
	else { // Treat as any object
	  ObjectId object = *oit;
	  os << object->getType().toString() << ":" << object->getName().toString() << "*************************" << std::endl;
	  const TokenSet& tokens = object->getTokens();
	  for(TokenSet::const_iterator tokit = tokens.begin(); tokit != tokens.end(); ++tokit){
	    TokenId t = *tokit;
	    alltokens.erase(t);
	    writeToken(t, os);
	  }
	}
      }

      os << "Free Tokens: *************************" << std::endl;
      for(TokenSet::const_iterator tokit = alltokens.begin(); tokit != alltokens.end(); ++tokit){
	TokenId t = *tokit;	    
	writeToken(t, os);
      }

    }

  private:

    static void writeToken(const TokenId& t, ostream& os){
      TempVarId st = t->getStart();
      os << "[ " << st->derivedDomain() << " ]"<< std::endl;
      os << "\t" << t->getPredicateName().toString() << "(" ;
      std::vector<ConstrainedVariableId> vars = t->getParameters();
      for(std::vector<ConstrainedVariableId>::const_iterator varit = vars.begin(); varit != vars.end(); ++varit) {
	ConstrainedVariableId v = (*varit);
	os << v->getName().toString() << "=" << v->derivedDomain();
      }
      os << ")" <<std::endl;
      os << "\tKey=" << t->getKey() << std::endl;

      TokenSet mergedtoks = t->getMergedTokens();

      for(TokenSet::const_iterator mit = mergedtoks.begin(); mit != mergedtoks.end(); ++mit) 
	os << "\t\tMerged Key=" << (*mit)->getKey() << std::endl;

      os << "[ " << t->getEnd()->derivedDomain() << " ]"<< std::endl;
    }
  };
}
#endif
	    
	        
	  
