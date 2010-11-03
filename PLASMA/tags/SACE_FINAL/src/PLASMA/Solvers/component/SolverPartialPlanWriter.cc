#ifdef __BEOS__
#include <Path.h>
#endif

#include "SolverPartialPlanWriter.hh"

#include "Constraint.hh"
#include "ConstraintEngine.hh"
#include "ConstraintEngineDefs.hh"
#include "ConstrainedVariable.hh"
#include "Domains.hh"
#include "LabelStr.hh"
#include "Variable.hh"

#include "PlanDatabase.hh"
#include "PlanDatabaseDefs.hh"
#include "Object.hh"
#include "Timeline.hh"
#include "Token.hh"
#include "TokenVariable.hh"

#include "Rule.hh"
#include "RulesEngine.hh"
#include "RulesEngineDefs.hh"
#include "RuleInstance.hh"

#include "Debug.hh"

#include "tinyxml.h"

#include <algorithm>
#include <exception>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <iterator>
#include <list>
#include <stdexcept>
#include <set>
#include <typeinfo>
#include <vector>

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>

#define FatalError(cond, msg...){Error(cond, msg, __FILE__, __LINE__).handleAssert();}
#define FatalErrno(){FatalError("Condition", strerror(errno))}
#define FatalErr(s) {std::cerr << (s) << std::endl; FatalErrno(); }

#define IN_NO_SECTION 0
#define IN_GENERAL_SECTION 1

#ifdef __MINGW32__
	static int mkdir(const char* path, mode_t mode) {
		int toRet = mkdir(path);
		chmod(path, mode);
		return toRet;
	}
#endif

namespace EUROPA {
  namespace SOLVERS {
    namespace PlanWriter {

      const char *envPPWConfigFile = "PPW_CONFIG";

      const std::string DURATION_VAR("DURATION_VAR");
      const std::string END_VAR("END_VAR");
      const std::string START_VAR("START_VAR");
      const std::string STATE_VAR("STATE_VAR");
      const std::string OBJECT_VAR("OBJECT_VAR");
      const std::string PARAMETER_VAR("PARAMETER_VAR");
      const std::string MEMBER_VAR("MEMBER_VAR");
      const std::string RULE_VAR("RULE_VAR");

      const std::string tokenVarTypes[8] =
	{STATE_VAR, OBJECT_VAR, DURATION_VAR, START_VAR, END_VAR, PARAMETER_VAR, MEMBER_VAR, RULE_VAR};

      enum varTypes {I_STATE = 0, I_OBJECT, I_DURATION, I_START, I_END, I_PARAMETER, I_MEMBER, I_RULE};
      enum objectTypes {O_OBJECT = 0, O_TIMELINE, O_RESOURCE};
      enum tokenTypes {T_INTERVAL = 0, T_TRANSACTION};
      enum decisionTypes {D_OBJECT = 0, D_TOKEN, D_VARIABLE, D_RESOURCE, D_ERROR};

#define SEQ_COL_SEP (unsigned char) 0x1e
#define SEQ_LINE_SEP (unsigned char) 0x1f
      const std::string TAB("\t");
      const std::string COLON(":");
      const std::string SNULL("\\N");
      const std::string CONSTRAINT_TOKEN("constraintToken");
      const std::string COMMA(",");
      const std::string SLASH("/");
      const std::string SPACE(" ");
      const std::string TEMPORAL("TEMPORAL");
      const std::string ATEMPORAL("ATEMPORAL");
      const std::string VAR_TEMP_CONSTR("variableTempConstr");
      const std::string UNARY_TEMP_CONSTR("unaryTempConstr");
      const std::string FIXED_TEMP_CONSTR("fixedTempConstr");
      const std::string UNARY_CONSTR("unaryConstr");
      const std::string EQ_CONSTR("equalityConstr");
      const std::string BUG_CONSTR("bugConstr");
      const std::string STRUE("true");
      const std::string SFALSE("false");
      const std::string PINFINITY("Infinity");
      const std::string NINFINITY("-Infinity");
      const std::string INTEGER_SORT("INTEGER_SORT");
      const std::string REAL_SORT("REAL_SORT");
      const std::string STEP("step");
      const std::string PARTIAL_PLAN_STATS("/partialPlanStats");
      const std::string SEQUENCE("/sequence");
      const std::string RULES("/rules");
      const std::string PARTIAL_PLAN(".partialPlan");
      const std::string OBJECTS(".objects");
      const std::string TOKENS(".tokens");
      const std::string RULE_INSTANCES(".ruleInstances");
      const std::string RULE_INSTANCE_SLAVE_MAP(".ruleInstanceSlaveMap");
      const std::string VARIABLES(".variables");
      const std::string CONSTRAINTS(".constraints");
      const std::string CONSTRAINT_VAR_MAP(".constraintVarMap");
      const std::string INSTANTS(".instants");
      const std::string DECISIONS(".decisions");
      const std::string E_DOMAIN("E");
      const std::string I_DOMAIN("I");
      const std::string CAUSAL("CAUSAL");
      const std::string ENUM_DOMAIN("EnumeratedDomain");
      const std::string INT_DOMAIN("IntervalDomain");
      const std::string ROOT_CONFIG("PartialPlanWriterConfig");
      const std::string GENERAL_CONFIG_SECTION("GeneralConfigSection");
      const std::string RULE_CONFIG_SECTION("RuleConfigSection");
      const std::string SOURCE_PATH("SourcePath");
      const std::string AUTO_WRITE("AutoWrite");
      const std::string STEPS_PER_WRITE("StepsPerWrite");
      const std::string WRITE_FINAL_STEP("WriteFinalStep");
      const std::string WRITE_DEST("WriteDest");
      const std::string MAX_CHOICES("MaxChoices");

      const std::string configSections[] = {GENERAL_CONFIG_SECTION, RULE_CONFIG_SECTION};

#ifdef __BEOS__
#define NBBY 8
      static char *realpath(const char *path, char *resolved_path) {
	BPath tempPath(path,NULL,true);
	if (tempPath.Path() == NULL) {
	  return NULL;
	}
	strcpy(resolved_path,tempPath.Path());
	return resolved_path;
      }
#endif

#ifdef __MINGW32__

#define NBBY 8

  static char *realpath(const char *path, char *resolved_path) {
    char* temp;
    if (GetFullPathNameA(path, MAXPATHLEN, resolved_path, &temp) == 0)
      return NULL;
    return resolved_path;
  }
#endif



      inline long long int timeval2Id(const struct timeval &currTime) {
	return (((long long int) currTime.tv_sec) * 1000) + (currTime.tv_usec / 1000);
      }

      /* These are static to make them visible in GDB out of their normal scope.
       */
      int PartialPlanWriter::noFullWrite(0);
      int PartialPlanWriter::writeStep(0);

      PartialPlanWriter::PartialPlanWriter(const PlanDatabaseId& planDb,
	  const ConstraintEngineId& ceId2,
	  const RulesEngineId& reId2,
	  SOLVERS::SolverId& solver) :
	PROPAGATION_COMMENCED("PROPAGATION_COMMENCED"),
	PROPAGATION_COMPLETED("PROPAGATION_COMPLETED"),
	PROPAGATION_PREEMPTED("PROPAGATION_PREEMPTED"),
	ERROR("ERROR"), STEP_SUCCEEDED("STEP_SUCCEEDED"),
	STEP_FAILED("STEP_FAILED"), RETRACT_SUCCEEDED("RETRACT_SUCCEEDED"),
	RETRACT_FAILED("RETRACT_FAILED"), PLAN_FOUND("PLAN_FOUND"),
	SEARCH_EXHAUSTED("SEARCH_EXHAUSTED"), TIMEOUT_REACHED("TIMEOUT_REACHED")
      {
	commonInit(planDb, ceId2, reId2);
	setSolver(solver);
      }

      PartialPlanWriter::PartialPlanWriter(const PlanDatabaseId &planDb,
	  const ConstraintEngineId &ceId2,
	  const RulesEngineId &reId2) :
	PROPAGATION_COMMENCED("PROPAGATION_COMMENCED"),
	PROPAGATION_COMPLETED("PROPAGATION_COMPLETED"),
	PROPAGATION_PREEMPTED("PROPAGATION_PREEMPTED"),
	ERROR("ERROR"), STEP_SUCCEEDED("STEP_SUCCEEDED"),
	STEP_FAILED("STEP_FAILED"), RETRACT_SUCCEEDED("RETRACT_SUCCEEDED"),
	RETRACT_FAILED("RETRACT_FAILED"), PLAN_FOUND("PLAN_FOUND"),
	SEARCH_EXHAUSTED("SEARCH_EXHAUSTED"), TIMEOUT_REACHED("TIMEOUT_REACHED")
      {
	commonInit(planDb, ceId2, reId2);
      }

      PartialPlanWriter::PartialPlanWriter(const PlanDatabaseId &planDb,
	  const ConstraintEngineId &ceId2) :
	PROPAGATION_COMMENCED("PROPAGATION_COMMENCED"),
	PROPAGATION_COMPLETED("PROPAGATION_COMPLETED"),
	PROPAGATION_PREEMPTED("PROPAGATION_PREEMPTED"),
	ERROR("ERROR"), STEP_SUCCEEDED("STEP_SUCCEEDED"),
	STEP_FAILED("STEP_FAILED"), RETRACT_SUCCEEDED("RETRACT_SUCCEEDED"),
	RETRACT_FAILED("RETRACT_FAILED"), PLAN_FOUND("PLAN_FOUND"),
	SEARCH_EXHAUSTED("SEARCH_EXHAUSTED"), TIMEOUT_REACHED("TIMEOUT_REACHED")
      {
	commonInit(planDb, ceId2, RulesEngineId::noId());
      }

      void PartialPlanWriter::allocateListeners() {
	cel = (new PPWConstraintEngineListener(ceId, this))->getId();
      }

      void PartialPlanWriter::commonInit(const PlanDatabaseId &planDb,
					 const ConstraintEngineId &ceId2,
					 const RulesEngineId& _reId) {
        reId = _reId;
	nstep = 0;
	destAlreadyInitialized = false;
	m_writing = false;
	struct timeval currTime;
	if(gettimeofday(&currTime, NULL)) {
	  FatalError("gettimeofday()", "Failed to get current time.");
	}
	seqId = timeval2Id(currTime);
	pdbId = planDb;
	ceId = ceId2;
	writeCounter = 0;
	stepsPerWrite = 0;
	dest = "./plans";
	noFullWrite = 1;
	writeStep = 0;

	//add default directories to search for model files
	sourcePaths.push_back(".");
	sourcePaths.push_back("..");
	std::string configPath;
	if(getenv(envPPWConfigFile) == NULL)
	  configPath = "PlanWorks.cfg";
	else
	  configPath = std::string(getenv(envPPWConfigFile));

	if (!std::ifstream(configPath.c_str()).good()) {
	  debugMsg("PartialPlanWriter",  "Warning: PPW_CONFIG not set or is empty.");
	  debugMsg("PartialPlanWriter",  "   PartialPlanWriter will not write.");
	  stepsPerWrite = 0;
	  noFullWrite = 1;
	  writeStep = 0;
	  return;
	}

	char *configBuf = new char[PATH_MAX + 100];
	if (configBuf == 0)
	  FatalErr("No memory for PPW_CONFIG");
	if (realpath(configPath.c_str(), configBuf) == NULL) {
	  std::cerr << "Failed to get config file " << configPath << std::endl;
	  FatalErrno();
	}

	std::string buf;

	parseConfigFile(configBuf);

	if (stepsPerWrite != 0) {
	  allocateListeners();
	}
	marksStep(PROPAGATION_COMPLETED);
	marksStep(PROPAGATION_PREEMPTED);
      }

      void PartialPlanWriter::setSolver(SOLVERS::SolverId& solver) {
	check_error(sl.isNoId(), "Already have a solver");
	sl = (new PPWSearchListener(solver, this))->getId();
	unmarksStep(PROPAGATION_PREEMPTED);
	unmarksStep(PROPAGATION_COMPLETED);
	marksStep(STEP_SUCCEEDED);
	marksStep(STEP_FAILED);
	marksStep(PLAN_FOUND);
	marksStep(SEARCH_EXHAUSTED);
	marksStep(TIMEOUT_REACHED);
	marksStep(RETRACT_SUCCEEDED);
      }

      void PartialPlanWriter::clearSolver() {
	check_error(sl.isValid(), "No solver.");
	delete (SOLVERS::SearchListener*) sl;
	sl = SOLVERS::SearchListenerId::noId();
	marksStep(PROPAGATION_PREEMPTED);
	marksStep(PROPAGATION_COMPLETED);
	unmarksStep(STEP_SUCCEEDED);
	unmarksStep(STEP_FAILED);
	unmarksStep(PLAN_FOUND);
	unmarksStep(SEARCH_EXHAUSTED);
	unmarksStep(TIMEOUT_REACHED);
	unmarksStep(RETRACT_SUCCEEDED);
      }

      void PartialPlanWriter::initOutputDestination() {
	debugMsg("PartialPlanWriter:initOutputDestination", "Running...");

	char *destBuf = new char[PATH_MAX];
	if(realpath(dest.c_str(), destBuf) == NULL && stepsPerWrite != 0) {
	  debugMsg("PartialPlanWriter:initOutputDestination", "Creating dest dir '" << destBuf << "'");
	  if(mkdir(destBuf, 0777) && errno != EEXIST) {
	    std::cerr << "Failed to make destination directory " << dest << std::endl;
	    FatalErrno();
	  }
	}
	realpath(dest.c_str(), destBuf);
	dest = destBuf;
	delete [] destBuf;

	char timestr[NBBY * sizeof(seqId) * 28/93 + 4];
	sprintf(timestr, "%lld", seqId);
	std::string modelName = pdbId->getSchema()->getName().toString();
	{
	  std::string::size_type tempIndex = modelName.rfind('/');
	  if(tempIndex > 0 && tempIndex < modelName.length()) {
	    modelName = modelName.substr(tempIndex);
	  }
	}

	std::string seqName = modelName;
	std::string::size_type extStart = seqName.find('.');
	seqName = seqName.substr(0, extStart);

	if(stepsPerWrite) {
	  if(mkdir(dest.c_str(), 0777) && errno != EEXIST) {
	    /*
	     * Failing with return code = -1, errno = 0 when dir already
	     * exists.  Should be returning errno = EEXIST.
	     * Check errno is non-zero before call to FatalErrno()
	     */
	    if (errno) {
	      std::cerr << "Failed to make directory " << dest << std::endl;
	      FatalErrno();
	    }
	  }
	  if(seqName[0] != '/')
	    dest += "/";
	  dest += seqName;
	  dest += timestr;

	  if(mkdir(dest.c_str(), 0777) && errno != EEXIST) {
	    std::cerr << "Failed to make directory " << dest << std::endl;
	    FatalErrno();
	  }
	  std::string ppStats(dest + PARTIAL_PLAN_STATS);
	  std::string seqRules(dest + RULES);
	  std::string seqStr(dest + SEQUENCE);
	  std::ofstream seqOut(seqStr.c_str());
	  if(!seqOut) {
	    std::cerr << "Failed to open " << seqStr << std::endl;
	    FatalErrno();
	  }
	  seqOut << dest << SEQ_COL_SEP << seqId << SEQ_COL_SEP;// << std::endl;

	  std::ofstream rulesOut(seqRules.c_str());
	  if(!rulesOut) {
	    std::cerr << "Failed to open " << seqRules << std::endl;
	    FatalErrno();
	  }

	  std::set<std::string> modelFiles;

	  const RuleSchemaId& rs = reId->getRuleSchema();

	  if (!rs.isNoId()) {
	      char realModelPaths[PATH_MAX];
	      bool foundModelPath = false;
	      const std::multimap<double, RuleId>& allRules = rs->getRules();
	      for(std::multimap<double, RuleId>::const_iterator it = allRules.begin(); it != allRules.end(); ++it) {
	          std::string ruleSrc = ((*it).second)->getSource().toString();
	          if(ruleSrc == "noSrc")
	              continue;
	          std::string modelFile = ruleSrc.substr(1, ruleSrc.rfind(",")-1);
	          std::string lineNumber = ruleSrc.substr(ruleSrc.rfind(","), ruleSrc.size()-1);
	          lineNumber.replace(lineNumber.rfind('"'), 1, "\0");
	          foundModelPath = false;
	          for(std::list<std::string>::iterator pathIt = sourcePaths.begin();
	          pathIt != sourcePaths.end(); ++pathIt) {
	              std::string modelPath = (*pathIt) + "/" + modelFile;
	              if(realpath(modelPath.c_str(), realModelPaths) == NULL) {
	                  continue;
	              }
	              modelPath = realModelPaths;
	              modelFiles.insert(modelPath);
	              foundModelPath = true;
	              rulesOut << seqId << TAB << (*it).second->getName().toString() << TAB << modelPath << lineNumber
	              << std::endl;
	              break;
	          }
	          if (foundModelPath == false) {
	              std::cerr << "Warning: PPW could not find path to model file for rule "
	              << (*it).second->getName().toString() << std::endl;
	              std::cerr << "         Check configuration of RuleConfigSection in PlanWorks.cfg " << std::endl;
	          }
	      }
	  }

	  {
	    std::ostream_iterator<unsigned char> out(seqOut);
	    for(std::set<std::string>::const_iterator it = modelFiles.begin();
		it != modelFiles.end(); ++it) {
	      seqOut << "--begin " << *it << std::endl;
	      std::ifstream modelIn((*it).c_str());
	      modelIn.unsetf(std::ios::skipws);
	      std::copy(std::istream_iterator<unsigned char>(modelIn),
			std::istream_iterator<unsigned char>(), out);
	    }
	  }
	  seqOut << SEQ_LINE_SEP;
	  seqOut.close();

	  statsOut = new std::ofstream(ppStats.c_str());
	  if(!(*statsOut)) {
	    FatalErrno();
	  }
	}
      }

      PartialPlanWriter::~PartialPlanWriter(void) {
	if (!cel.isNoId())  // not always allocated
	  delete (ConstraintEngineListener*) cel;
	if(!sl.isNoId())
	 delete (SOLVERS::SearchListener*) sl;

	if(stepsPerWrite) {
	  if(destAlreadyInitialized) {
	    statsOut->close();
	    delete statsOut;
	  }
	}
      }

      // accessor to get output destination full path
      std::string PartialPlanWriter::getDest(void) {
	return dest;
      }

      /*
       * accessor to set and init output destination path and files
       *
       * This should only be called before calling write() or
       * writeStatsAndTransactions()
       */
      void PartialPlanWriter::setDest(std::string destPath) {
	/*
	 * initialize write controls
	 * initialize the directories and files for the
	 * specified output destination.
	 */
	if(!destAlreadyInitialized) {
	  /*
	   *If stepsPerWrite is still 0 after commonInit has run
	   *then allocateListeners() has not been called yet
	   *Call now since Planner Control needs them
	   */
	  if (stepsPerWrite == 0) {
	    allocateListeners();
	  }
	  dest = destPath;
	  noFullWrite = 1;   // do not write every step
	  stepsPerWrite = 1; // enable write in one step increments
	  writeStep = 1;     // enable one step client control of write
	  initOutputDestination();
	  destAlreadyInitialized = true;
	} else {
	  std::cerr << "Destination directory already initialized to " << dest << std::endl;
	  std::cerr << "Failed to initialize destination directory to " << destPath << std::endl;
	  FatalErrno();
	}
      }

      void PartialPlanWriter::write(void) {

	/*
	 * init output destination files if this has not been done
	 * This is also called in WriteStatsAndTransactions() to cover
	 * cases where the first step is not written.
	 */

	// std::cerr << " PartialPlanWriter::write() called " << std::endl;
	check_error(!m_writing, "PartialPlanWriter attempted to write while writing.");
	m_writing = true;
	debugMsg("PartialPlanWriter:write", "Writing step " << nstep);

	if(!destAlreadyInitialized) {
	  initOutputDestination();
	  destAlreadyInitialized = true;
	}
	if(!statsOut)
	  return;
	ppId = 0LL;
	struct timeval currTime;
	if(gettimeofday(&currTime, NULL)) {
	  FatalError("gettimeofday()", "Failed to get current time.");
	}
	ppId = timeval2Id(currTime);

	numTokens = numVariables = numConstraints = 0;

	char stepstr[NBBY * sizeof(nstep) * 28/93 + 4];
	sprintf(stepstr, "%d", (int) nstep);

	std::string stepnum(STEP + stepstr);

	std::string ppDest = dest + SLASH + stepnum;
	if(mkdir(ppDest.c_str(), 0777) && errno != EEXIST) {
	  std::cerr << "Failed to create " << ppDest << std::endl;
	  FatalErrno();
	}

	std::string ppPartialPlan = ppDest + SLASH + stepnum + PARTIAL_PLAN;
	std::ofstream ppOut(ppPartialPlan.c_str());
	if(!ppOut) {
	  FatalErrno();
	}

	ppOut << stepnum << TAB << ppId << TAB << pdbId->getSchema()->getName().toString()
	      << TAB << seqId << std::endl;
	ppOut.close();

	std::string ppObj = ppDest + SLASH + stepnum + OBJECTS;
	std::ofstream objOut(ppObj.c_str());
	if(!objOut) {
	  FatalErrno();
	}

	std::string ppTok = ppDest + SLASH + stepnum + TOKENS;
	std::ofstream tokOut(ppTok.c_str());
	if(!tokOut) {
	  FatalErrno();
	}

	std::string ppRuleInstances = ppDest + SLASH + stepnum + RULE_INSTANCES;
	std::ofstream ruleInstanceOut(ppRuleInstances.c_str());
	if(!ruleInstanceOut) {
	  FatalErrno();
	}

	std::string ppRISM = ppDest + SLASH + stepnum + RULE_INSTANCE_SLAVE_MAP;
	std::ofstream rismOut(ppRISM.c_str());
	if(!rismOut) {
	  FatalErrno();
	}

	std::string ppVars = ppDest + SLASH + stepnum + VARIABLES;
	std::ofstream varOut(ppVars.c_str());
	if(!varOut) {
	  FatalErrno();
	}

	std::string ppConstrs = ppDest + SLASH + stepnum + CONSTRAINTS;
	std::ofstream constrOut(ppConstrs.c_str());
	if(!constrOut) {
	  FatalErrno();
	}

	std::string ppCVM = ppDest + SLASH + stepnum + CONSTRAINT_VAR_MAP;
	std::ofstream cvmOut(ppCVM.c_str());
	if(!cvmOut) {
	  FatalErrno();
	}

	std::string ppInsts = ppDest + SLASH + stepnum + INSTANTS;
	std::ofstream instsOut(ppInsts.c_str());
	if(!instsOut) {
	  FatalErrno();
	}

	std::string ppDecs = ppDest + SLASH + stepnum + DECISIONS;
	std::ofstream decsOut(ppDecs.c_str());
	if(!decsOut) {
	  FatalErrno();
	}

	const ConstraintSet &constraints = ceId->getConstraints();
	numConstraints = constraints.size();
	for(ConstraintSet::const_iterator it = constraints.begin(); it != constraints.end(); ++it) {
	  outputConstraint(*it, constrOut, cvmOut);
	}

	ObjectSet objects(pdbId->getObjects());
	TokenSet tokens(pdbId->getTokens());
	int slotId = 1000000;
	for(ObjectSet::iterator objectIterator = objects.begin();
	    objectIterator != objects.end(); ++objectIterator) {
	  const ObjectId &objId = *objectIterator;
	  if(TimelineId::convertable(objId)) {
	    outputObject(objId, O_TIMELINE, objOut, varOut);
	    TimelineId &tId = (TimelineId &) objId;
	    const std::list<TokenId>& orderedTokens = tId->getTokenSequence();
	    int slotIndex = 0;
	    int emptySlots = 0;
	    for(std::list<TokenId>::const_iterator tokenIterator = orderedTokens.begin();
		tokenIterator != orderedTokens.end(); ++tokenIterator) {
	      int slotOrder = 0;
	      const TokenId &token = *tokenIterator;
	      outputToken(token, T_INTERVAL, slotId, slotIndex, slotOrder, (ObjectId) tId, tokOut,
			  varOut);
	      tokens.erase(token);
	      TokenSet::const_iterator mergedTokenIterator =
		token->getMergedTokens().begin();
	      for(;mergedTokenIterator != token->getMergedTokens().end(); ++mergedTokenIterator) {
		slotOrder++;
		outputToken(*mergedTokenIterator, T_INTERVAL, slotId, slotIndex, slotOrder,
			    (ObjectId &) tId, tokOut, varOut);
		tokens.erase(*mergedTokenIterator);
	      }
	      slotId++;
	      slotIndex++;
	      ++tokenIterator;
	      /*ExtraData: empty slot info*/
	      if(tokenIterator != orderedTokens.end()) {
		const TokenId &nextToken = *tokenIterator;
		if(token->end()->lastDomain() != nextToken->start()->lastDomain()) {
		  objOut << slotId << COMMA << slotIndex << COLON;
		  emptySlots++;
		  slotId++;
		  slotIndex++;
		}
	      }
	      --tokenIterator;
	    }
	    if(!emptySlots)
	      objOut << SNULL;
	    objOut << std::endl;
	  }
#ifndef NO_RESOURCES
/* TODO JRB: move this to resource module
	  else if(ResourceId::convertable(objId)) {
	    outputObject(objId, O_RESOURCE, objOut, varOut);

	    ResourceId &rId = (ResourceId &) objId;

	    //ExtraData: resource info
	    objOut << MINUS_INFINITY << COMMA << PLUS_INFINITY << COMMA
		   << rId->getInitialCapacity() << COMMA << rId->getLimitMin() << COMMA
		   << rId->getLimitMax() << COMMA;

	    //const std::set<TransactionId>& resTrans = rId->getTransactions();
	    std::set<TransactionId> resTrans;
	    rId->getTransactions(resTrans, MINUS_INFINITY, PLUS_INFINITY, false);
	    for(std::set<TransactionId>::iterator transIt = resTrans.begin();
		transIt != resTrans.end(); ++transIt) {
	      TransactionId trans = *transIt;
	      outputToken(trans, T_TRANSACTION, 0, 1, 0, rId, tokOut, varOut);
	      tokens.erase(trans);
	    }

	    const std::map<int, InstantId>& insts = rId->getInstants();
	    for(std::map<int,InstantId>::const_iterator instIt = insts.begin();
		instIt != insts.end(); ++instIt) {
	      InstantId inst = (*instIt).second;
	      outputInstant(inst, rId->getKey(), instsOut);
	      objOut << inst->getKey() << COMMA;
	    }
	    objOut << std::endl;
	  }
*/
#endif
	  else {
	    outputObject(objId, O_OBJECT, objOut, varOut);
	    /*ExtraData: NULL*/
	    objOut << SNULL << std::endl;
	  }
	}
	for(TokenSet::iterator tokenIterator = tokens.begin();
	    tokenIterator != tokens.end(); ++tokenIterator) {
	  TokenId token = *tokenIterator;
	  check_error(token.isValid());
	  outputToken(token, T_INTERVAL, 0, 0, 0, ObjectId::noId(), tokOut, varOut);
	}



	std::set<RuleInstanceId> ruleInst = reId->getRuleInstances();
	for(std::set<RuleInstanceId>::const_iterator it = ruleInst.begin();
	    it != ruleInst.end(); ++it) {
	  RuleInstanceId ri = *it;
	  outputRuleInstance(ri, ruleInstanceOut, varOut, rismOut);
	}

	collectStats(); // this call will overwrite incremental counters for tokens, variables, and constraints
	(*statsOut) << seqId << TAB << ppId << TAB << nstep << TAB << numTokens << TAB << numVariables
		    << TAB << numConstraints << std::endl;
	statsOut->flush();

	objOut.close();
	tokOut.close();
	ruleInstanceOut.close();
	rismOut.close();
	varOut.close();
	constrOut.close();
	cvmOut.close();
	instsOut.close();
	decsOut.close();
	m_writing = false;
      }

      void PartialPlanWriter::writeStatistics(void) {
	writeStats();
      }
      /* writeStatsAndTransactions() is called at the end of each step
	 instead of write() when step data is written for only the
	 final step. this ensures that transaction and statistics info
	 is written for all steps.
      */
      void PartialPlanWriter::writeStats(void) {
	if(!destAlreadyInitialized) {
	  initOutputDestination();
	  destAlreadyInitialized = true;
	}

	ppId = 0LL;
	struct timeval currTime;
	if(gettimeofday(&currTime, NULL)) {
	  FatalError("gettimeofday()", "Failed to get current time.");
	}
	ppId = timeval2Id(currTime);

	collectStats();
	debugMsg("PartialPlanWriter:writeStats", "Writing statistics numTokens: " << numTokens
		 << " numVariables: " << numVariables << " numConstraints: " << numConstraints);
	(*statsOut) << seqId << TAB << ppId << TAB << nstep << TAB << numTokens << TAB << numVariables
		    << TAB << numConstraints << std::endl;
	statsOut->flush();

      }


      /* collects all but numTransactions which are
	 collected incrementally
      */
      void PartialPlanWriter::collectStats(void) {
	TokenSet tokens(pdbId->getTokens());
	numTokens = tokens.size();
	ConstrainedVariableSet variables = ceId->getVariables();
	numVariables = variables.size();
	const ConstraintSet &constraints = ceId->getConstraints();
	numConstraints = constraints.size();
      }

      void PartialPlanWriter::outputObject(const ObjectId &objId, const int type,
					   std::ofstream &objOut, std::ofstream &varOut) {
	int parentKey = -1;
	if(!objId->getParent().isNoId())
	  parentKey = objId->getParent()->getKey();
	objOut << objId->getKey() << TAB << type << TAB << parentKey << TAB
	       << ppId << TAB << objId->getName().toString() << TAB;
	/*ChildObjectIds*/
	if(objId->getComponents().empty()) {
	  objOut << SNULL << TAB;
	}
	else {
	  for(ObjectSet::const_iterator childIt = objId->getComponents().begin();
	      childIt != objId->getComponents().end(); ++childIt) {
	    ObjectId child = *childIt;
	    objOut << child->getKey() << COMMA;
	  }
	  objOut << TAB;
	}
	/*end ChildObjectIds*/
	/*VariableIds*/
	if(objId->getVariables().empty()) {
	  objOut << SNULL << TAB;
	}
	else {
	  for(std::vector<ConstrainedVariableId>::const_iterator varIt =
		objId->getVariables().begin(); varIt != objId->getVariables().end(); ++varIt) {
	    ConstrainedVariableId var = *varIt;
	    objOut << var->getKey() << COMMA;
	    outputConstrVar(var, objId->getKey(), I_MEMBER, varOut);
	  }
	  objOut << TAB;
	}
	/*end VariableIds*/
	/*TokenIds*/
	if(objId->tokens().empty()) {
	  objOut << SNULL << TAB;
	}
	else {
	  for(TokenSet::const_iterator tokIt = objId->tokens().begin();
	      tokIt != objId->tokens().end(); ++tokIt) {
	    TokenId token = *tokIt;
	    objOut << token->getKey() << COMMA;
	  }
	  objOut << TAB;
	}
	/*end TokenIds*/
      }

      void PartialPlanWriter::outputToken(const TokenId &token, const int type, const int slotId,
					  const int slotIndex, const int slotOrder,
					  const ObjectId &tId, std::ofstream &tokOut,
					  std::ofstream &varOut) {
	check_error(token.isValid());
	if(token->isIncomplete()) {
	  std::cerr << "Token " << token->getKey() << " is incomplete.  Skipping. " << std::endl;
	  return;
	}
	if(!tId.isNoId()) {
	  tokOut << token->getKey() << TAB << type << TAB << slotId << TAB << slotIndex << TAB
		 << ppId << TAB << 0 << TAB << 1 << TAB << token->start()->getKey() << TAB
		 << token->end()->getKey() << TAB << token->duration()->getKey() << TAB
		 << token->getState()->getKey() << TAB << token->getPredicateName().toString()
		 << TAB << tId->getKey() << TAB << tId->getName().toString() << TAB
		 << token->getObject()->getKey() << TAB;
	}
	else {
	  tokOut << token->getKey() << TAB << type << TAB << SNULL << TAB << SNULL << TAB << ppId
		 << TAB << 1 << TAB << 1 << TAB << token->start()->getKey() << TAB
		 << token->end()->getKey() << TAB << token->duration()->getKey() << TAB
		 << token->getState()->getKey() << TAB << token->getPredicateName().toString()
		 << TAB << SNULL << TAB << SNULL << TAB << token->getObject()->getKey() << TAB;
	}
	outputObjVar(token->getObject(), token->getKey(), I_OBJECT, varOut);
	outputIntIntVar(token->start(), token->getKey(), I_START, varOut);
	outputIntIntVar(token->end(), token->getKey(), I_END, varOut);
	outputIntIntVar(token->duration(), token->getKey(), I_DURATION, varOut);
	//outputEnumVar(token->getState(), token->getKey(), I_STATE, varOut);
	outputStateVar(token->getState(), token->getKey(), I_STATE, varOut);

	std::string paramVarIds;
	char paramIdStr[NBBY * sizeof(int) * 28/93 + 4];
	for(std::vector<ConstrainedVariableId>::const_iterator paramVarIterator =
	      token->parameters().begin();
	    paramVarIterator != token->parameters().end(); ++paramVarIterator) {
	  ConstrainedVariableId varId = *paramVarIterator;
	  check_error(varId.isValid());
	  outputConstrVar(varId, token->getKey(), I_PARAMETER, varOut);
	  memset(paramIdStr, '\0', NBBY * sizeof(int) * 28/93 + 4);
	  sprintf(paramIdStr, "%d", varId->getKey());
	  paramVarIds += std::string(paramIdStr) + COLON;
	}
	if(paramVarIds == "") {
	  tokOut << SNULL << TAB;
	}
	else {
	  tokOut << paramVarIds << TAB;
	}

	/*ExtraInfo: SlotOrder*/
	if(type != T_TRANSACTION) {
	  tokOut << slotOrder;
	}
#ifndef NO_RESOURCES
	/* TODO JRB: move this to Resource module
	//ExtraInfo: QuantityMin:QuantityMax
        else{
	  TransactionId trans = (TransactionId) token;
	  tokOut << trans->getMin() << COMMA << trans->getMax();
	}
	*/
#endif

	tokOut << std::endl;
      }

      void PartialPlanWriter::outputStateVar(const Id<TokenVariable<StateDomain> >& stateVar,
					     const int parentId, const int type,
					     std::ofstream &varOut) {

	varOut << stateVar->getKey() << TAB << ppId << TAB << parentId << TAB
	       << stateVar->getName().toString() << TAB;

	varOut << ENUM_DOMAIN << TAB;
	StateDomain &dom = (StateDomain &) stateVar->lastDomain();
	std::list<double> vals;
	dom.getValues(vals);
	for(std::list<double>::const_iterator it = vals.begin(); it != vals.end(); ++it){
	  LabelStr strVal(*it);
	  varOut << strVal.toString() << " ";
	}
	varOut << TAB;
	varOut << SNULL << TAB << SNULL << TAB << SNULL << TAB;

	varOut << tokenVarTypes[type] << std::endl;
      }

      void PartialPlanWriter::outputEnumVar(const Id<TokenVariable<EnumeratedDomain> >& enumVar,
					    const int parentId, const int type,
					    std::ofstream &varOut) {

	varOut << enumVar->getKey() << TAB << ppId << TAB << parentId << TAB
	       << enumVar->getName().toString() << TAB;

	varOut << ENUM_DOMAIN << TAB;

	varOut << getEnumerationStr((EnumeratedDomain &)enumVar->lastDomain()) << TAB;
	varOut << SNULL << TAB << SNULL << TAB << SNULL << TAB;

	varOut << tokenVarTypes[type] << std::endl;
      }

      void PartialPlanWriter::outputIntVar(const Id<TokenVariable<IntervalDomain> >& intVar,
					   const int parentId, const int type,
					   std::ofstream &varOut) {

	varOut << intVar->getKey() << TAB << ppId << TAB << parentId << TAB
	       << intVar->getName().toString() << TAB;

	varOut << INT_DOMAIN << TAB << SNULL << TAB << REAL_SORT << TAB
	       << getLowerBoundStr((IntervalDomain &)intVar->lastDomain()) << TAB
	       << getUpperBoundStr((IntervalDomain &)intVar->lastDomain()) << TAB;

	varOut << tokenVarTypes[type] << std::endl;
      }

      void PartialPlanWriter::outputIntIntVar(const Id<TokenVariable<IntervalIntDomain> >& intVar,
					      const int parentId, const int type,
					      std::ofstream &varOut) {


	varOut << intVar->getKey() << TAB << ppId << TAB << parentId << TAB
	       << intVar->getName().toString() << TAB;

	varOut << INT_DOMAIN << TAB << SNULL << TAB << INTEGER_SORT << TAB
	       << getLowerBoundStr((IntervalDomain &)intVar->lastDomain()) << TAB
	       << getUpperBoundStr((IntervalDomain &)intVar->lastDomain()) << TAB;

	varOut << tokenVarTypes[type] << std::endl;
      }

      void PartialPlanWriter::outputObjVar(const ObjectVarId& objVar,
					   const int parentId, const int type,
					   std::ofstream &varOut) {


	varOut << objVar->getKey() << TAB << ppId << TAB << parentId << TAB
	       << objVar->getName().toString() << TAB;

	varOut << ENUM_DOMAIN << TAB;

	std::list<double> objects;
	((ObjectDomain &)objVar->lastDomain()).getValues(objects);
	for(std::list<double>::iterator it = objects.begin(); it != objects.end(); ++it) {
	  varOut << ((ObjectId)(*it))->getName().toString() << " ";
	}

	varOut << TAB << SNULL << TAB << SNULL << TAB << SNULL << TAB;

	varOut << tokenVarTypes[type] << std::endl;
      }

      void PartialPlanWriter::outputConstrVar(const ConstrainedVariableId &otherVar,
					      const int parentId, const int type,
					      std::ofstream &varOut) {


	varOut << otherVar->getKey() << TAB << ppId << TAB << parentId << TAB
	       << otherVar->getName().toString() << TAB;

	if(otherVar->lastDomain().isEnumerated()) {
	  varOut << ENUM_DOMAIN << TAB << getEnumerationStr((EnumeratedDomain &)otherVar->lastDomain())
		 << TAB << SNULL << TAB << SNULL << TAB << SNULL << TAB;
	}
	else if(otherVar->lastDomain().isInterval()) {
	  varOut << INT_DOMAIN << TAB << SNULL << TAB << REAL_SORT << TAB
		 << getLowerBoundStr((IntervalDomain &)otherVar->lastDomain()) << TAB
		 << getUpperBoundStr((IntervalDomain &)otherVar->lastDomain()) << TAB;
	}
	else {
	  FatalError("otherVar->lastDomain()isEnumerated() || otherVar->lastDomain().isInterval()",
		     "I don't know what my domain is!");
	}
	varOut << tokenVarTypes[type] << std::endl;
      }

      void PartialPlanWriter::outputConstraint(const ConstraintId &constrId, std::ofstream &constrOut,
					       std::ofstream &cvmOut) {
	constrOut << constrId->getKey() << TAB << ppId << TAB << constrId->getName().toString()
		  << TAB << ATEMPORAL << std::endl;
	std::vector<ConstrainedVariableId>::const_iterator it =
	  constrId->getScope().begin();
	for(; it != constrId->getScope().end(); ++it) {
	  cvmOut << constrId->getKey() << TAB << (*it)->getKey() << TAB << ppId << std::endl;
	}
      }

      void PartialPlanWriter::outputRuleInstance(const RuleInstanceId &ruleId,
						 std::ofstream &ruleInstanceOut,
						 std::ofstream &varOut,
						 std::ofstream &rismOut) {

	ruleInstanceOut << ruleId->getKey() << TAB << ppId << TAB << seqId
			<< TAB << ruleId->getRule()->getName().toString()
			<< TAB << ruleId->getToken()->getKey() << TAB;

	/*SlaveTokenIds*/
	std::set<TokenId> slaves;
	std::set<ConstrainedVariableId> vars;
	buildSlaveAndVarSets(slaves, vars, ruleId);
	if(slaves.empty()) {
	  ruleInstanceOut << SNULL << TAB;
	}
	else {
	  for(std::set<TokenId>::const_iterator it = slaves.begin();
	      it != slaves.end(); ++it) {
	    TokenId slaveToken = *it;
	    if(slaveToken.isValid())
	      ruleInstanceOut << slaveToken->getKey() << COMMA;
	    rismOut << ruleId->getKey() <<TAB << slaveToken->getKey() << TAB << ppId << std::endl;
	  }
	  ruleInstanceOut << TAB;
	}

	/* gaurd and local variables */
	if(vars.empty()) {
	  ruleInstanceOut << SNULL;
	}
	else {
	  for(std::set<ConstrainedVariableId>::const_iterator it = vars.begin();
	      it != vars.end(); ++it) {
	    ConstrainedVariableId localVar = *it;
	    ruleInstanceOut << localVar->getKey() << COMMA;
	    outputConstrVar(localVar, ruleId->getKey(), I_RULE, varOut);
	  }
	}
	ruleInstanceOut << std::endl;
      }

      void PartialPlanWriter::buildSlaveAndVarSets(std::set<TokenId> &tokSet,
						   std::set<ConstrainedVariableId> &varSet,
						   const RuleInstanceId &ruleId) {
	std::vector<TokenId> tokVec = ruleId->getSlaves();
	for(std::vector<TokenId>::const_iterator tempIt = tokVec.begin(); tempIt != tokVec.end(); ++tempIt)
	  if(!((*tempIt).isNoId()))
	    tokSet.insert(*tempIt);

	for(std::vector<ConstrainedVariableId>::const_iterator varIt = ruleId->getVariables().begin();
	    varIt != ruleId->getVariables().end(); ++varIt) {
	  ConstrainedVariableId var = *varIt;
	  if(RuleInstanceId::convertable(var->parent())) {
	    varSet.insert(var);
	  }
	}
	for(std::vector<RuleInstanceId>::const_iterator ruleIt = ruleId->getChildRules().begin();
	    ruleIt != ruleId->getChildRules().end(); ++ruleIt) {
	  RuleInstanceId rid = *ruleIt;
	  buildSlaveAndVarSets(tokSet, varSet, rid);
	}
      }

#ifndef NO_RESOURCES
/* TODO JRB: move this to Resource module

      void PartialPlanWriter::outputInstant(const InstantId &instId, const int resId,
					    std::ofstream &instOut) {
	instOut << ppId << TAB << resId << TAB << instId->getKey() << TAB << instId->getTime()
		<< TAB << instId->getLevelMin() << TAB << instId->getLevelMax() << TAB;
	const TransactionSet &transactions = instId->getTransactions();
	for(TransactionSet::const_iterator transIt = transactions.begin();
	    transIt != transactions.end(); ++transIt) {
	  TransactionId trans = *transIt;
	  instOut << trans->getKey() << COMMA;
	}
	instOut << std::endl;
      }
*/
#endif

      const std::string PartialPlanWriter::getUpperBoundStr(IntervalDomain &dom) const {
	if(dom.isNumeric()) {
	  if((int) dom.getUpperBound() == PLUS_INFINITY)
	    return PINFINITY;
	  else if((int) dom.getUpperBound() == MINUS_INFINITY)
	    return NINFINITY;
	  else {
	    std::stringstream stream;
	    stream <<  dom.getUpperBound();
	    return std::string(stream.str());
	  }
	}
	else if(LabelStr::isString((int)dom.getUpperBound())) {
	  LabelStr label((int)dom.getUpperBound());
	  return label.toString();
	}
	else if(dom.isBool()) {
	  if (dom.getUpperBound() == 0)
	    return std::string("false");
	  return std::string("true");
	}
	else {
	  return ObjectId(dom.getUpperBound())->getName().toString();
	}
	return std::string("");
      }

      const std::string PartialPlanWriter::getLowerBoundStr(IntervalDomain &dom) const {

	if(dom.isNumeric()) {
	  if((int)dom.getLowerBound() == PLUS_INFINITY)
	    return PINFINITY;
	  else if((int) dom.getLowerBound() == MINUS_INFINITY)
	    return NINFINITY;
	  std::stringstream stream;
	  stream << dom.getLowerBound();
	  return std::string(stream.str());
	}
	else if(LabelStr::isString((int)dom.getLowerBound())) {
	  LabelStr label((int)dom.getLowerBound());
	  return label.toString();
	}
	else if(dom.isBool()) {
	  if (dom.getLowerBound() == 0)
	    return std::string("false");
	  return std::string("true");
	}
	else {
	  return ObjectId(dom.getLowerBound())->getName().toString();
	}
	return std::string("");
      }

      const std::string PartialPlanWriter::getEnumerationStr(EnumeratedDomain &edom) const {
	std::stringstream stream;
	std::list<double> enumeration;
	EnumeratedDomain dom(edom);
	if(dom.isOpen()) {
	  dom.close();
	}
	if(dom.isInfinite()) {
	  return "-Infinity +Infinity";
	}
	if(dom.isEmpty()) {
	  return "empty";
	}
	else {
	  dom.getValues(enumeration);
	}
	for(std::list<double>::iterator it = enumeration.begin(); it != enumeration.end(); ++it) {
	  if(dom.isNumeric()) {
	    if((int) (*it) == PLUS_INFINITY)
	      stream << PINFINITY;
	    else if((int) (*it) == MINUS_INFINITY)
	      stream << NINFINITY;
	    else
	      stream << (int)(*it) << " ";
	  }
	  else if(LabelStr::isString(*it)) {
	    LabelStr label(*it);
	    stream << label.toString() << " ";
	  }
	  else if(dom.isBool()) {
	    if ((*it) == 0)
	      stream << "false" << " ";
	    stream << "true" << " ";
	  }
	  else {
	    stream << ObjectId(*it)->getName().toString() << " ";
	  }
	}

	//BUG: WHAT IS THIS
	//if(streamIsEmpty(stream)) {
	//  return "bugStr";
	//}
	return std::string(stream.str());
      }

      const bool PartialPlanWriter::isCompatGuard(const ConstrainedVariableId &var) const {
	std::set<ConstraintId> constrs;
	var->constraints(constrs);
	for(std::set<ConstraintId>::const_iterator it = constrs.begin();
	    it != constrs.end(); ++it)
	  if((*it)->getName() == LabelStr("RuleVariableListener"))
	    return true;
	return false;
      }

      void PartialPlanWriter::condWrite(const LabelStr& trans) {
	debugMsg("PartialPlanWriter:condWrite", "Transaction " << trans.toString() << " isStep " << isStep(trans)
		 << " noFullWrite " << noFullWrite << " writeStep " << writeStep << " stepsPerWrite " << stepsPerWrite
		 << " writeCounter " << writeCounter);
	if(isStep(trans)) {
	  writeCounter++;
	  if(noFullWrite == 0) {
	    if(writeCounter >= stepsPerWrite) {
	      write();
	      nstep++;
	      writeCounter = 0;
	    }
	  }
	  else {
	    if(writeStep == 1) {
	      writeStats();
	      writeCounter = 0;
	      nstep++;
	    }
	  }
	}
      }
      void PartialPlanWriter::notifyPropagationCompleted(void) {
	condWrite(PROPAGATION_COMPLETED);
      }

      void PartialPlanWriter::notifyPropagationPreempted(void) {
	condWrite(PROPAGATION_PREEMPTED);
      }

      void PartialPlanWriter::notifyStepSucceeded() {
	condWrite(STEP_SUCCEEDED);
      }

      void PartialPlanWriter::notifyStepFailed() {
	condWrite(STEP_FAILED);
      }

      void PartialPlanWriter::notifyRetractSucceeded() {
	condWrite(RETRACT_SUCCEEDED);
      }

      void PartialPlanWriter::notifyRetractNotDone() {
	condWrite(RETRACT_FAILED);
      }


      void PartialPlanWriter::notifyCompleted() {
	condWrite(PLAN_FOUND);
      }

      void PartialPlanWriter::notifyExhausted() {
	condWrite(SEARCH_EXHAUSTED);
      }

      void PartialPlanWriter::notifyTimedOut() {
	condWrite(TIMEOUT_REACHED);
      }

      const std::string PartialPlanWriter::getVarInfo(const ConstrainedVariableId &varId) const {
	std::string type, paramName, predName, retval;
	if(varId->getIndex() >= I_STATE && varId->getIndex() <= I_END) {
	  type = tokenVarTypes[varId->getIndex()];
	}
	else {
	  type = PARAMETER_VAR;
	  paramName = varId->getName().toString();
	}
	if(TokenId::convertable(varId->parent())) {
	  predName = ((TokenId &)varId->parent())->getPredicateName().toString();
	}
	else if(ObjectId::convertable(varId->parent())) {
	  predName = ((ObjectId &)varId->parent())->getName().toString();
	}
	else {
	  predName = "UNKNOWN VARIABLE PARENT";
	}
	retval = type + COMMA + predName + COMMA + paramName + COMMA;
	return retval;
      }

      void PartialPlanWriter::parseConfigFile(const char * configFile) {
        TiXmlDocument doc(configFile);
        bool loadOkay = doc.LoadFile();
        if(!loadOkay) {
          std::cerr << "Failed to open config file " << configFile << std::endl;
          FatalErrno();
        }
        TiXmlElement* root = doc.FirstChildElement(ROOT_CONFIG);
        if(root == NULL) {
          std::cerr << "Config file missing root element " << ROOT_CONFIG << std::endl;
          FatalErrno();
        }
        parseGeneralConfigSection(root->FirstChildElement(GENERAL_CONFIG_SECTION));
        parseRuleConfigSection(root->FirstChildElement(RULE_CONFIG_SECTION));
      }

      void PartialPlanWriter::parseGeneralConfigSection(const TiXmlElement* config) {
        if(config == NULL)
          return;

        const TiXmlElement* child = config->FirstChildElement();
        while(child != NULL) {
          std::string name = std::string(child->Value());
          const char * value = child->Attribute("value");
	  if(name == AUTO_WRITE) {
	    std::cerr << " autoWrite " << value << std::endl;
	    noFullWrite = value[0] != '1';
	  }
	  else if(name == STEPS_PER_WRITE) {
	    stepsPerWrite = strtol(value, NULL, 10);
	    if(stepsPerWrite < 0)
	      FatalError("stepsPerWrite < 0", "StepsPerWrite must be a non-negative value");
	    if(stepsPerWrite == LONG_MAX || stepsPerWrite == LONG_MIN)
	      FatalErrno();
	  }
	  else if(name == WRITE_FINAL_STEP) {
	    writeStep = value[0] != '0';
	  }
	  else if(name == WRITE_DEST) {
	    dest = std::string(value);
	  }
	  else if(name == MAX_CHOICES) {
	    maxChoices = strtol(value, NULL, 10);
	    if(maxChoices < 0)
	      FatalError("maxChoices < 0", "MaxChoices must be a non-negative value");
	    if(maxChoices == LONG_MAX || maxChoices == LONG_MIN)
	      FatalErrno();
	  }
	  else {
	    FatalError("Config parse error: unexpected element:", name);
	  }
	  child = child->NextSiblingElement();
	}
      }

      void PartialPlanWriter::parseRuleConfigSection(const TiXmlElement* config) {
        if(config == NULL)
          return;

        const TiXmlElement* child = config->FirstChildElement();
        while(child != NULL) {
          std::string name = std::string(child->Value());
          const char * value = child->Attribute("value");
	  if(name == SOURCE_PATH) {
	    sourcePaths.push_back(std::string(value));
	  }
	  else {
	    FatalError("Config parse error: unexpected element: ", name);
	  }
	  child = child->NextSiblingElement();
	}
      }

      void PartialPlanWriter::marksStep(const LabelStr& trans) {
	stepTransactions.push_back(trans);
      }
      void PartialPlanWriter::unmarksStep(const LabelStr& trans) {
	std::vector<LabelStr>::iterator it = stepTransactions.begin();
	for(; it != stepTransactions.end() && (*it) != trans; ++it){}
	if(it != stepTransactions.end())
	  stepTransactions.erase(it);
      }

      bool PartialPlanWriter::isStep(const LabelStr& trans) {
	return std::find(stepTransactions.begin(), stepTransactions.end(), trans) != stepTransactions.end();
      }

      void PartialPlanWriter::incrementStep(){nstep++;}

      void PartialPlanWriter::addSourcePath(const char* path) {
	static const std::string EMPTY_STRING("");
	static const std::string COLON_DELIMITER(":");
	std::string spath(path);

	if(path == EMPTY_STRING || path == COLON_DELIMITER)
	  return;

	debugMsg("PartialPlanWriter:addSourcePath", "Adding path '" << spath << "'");

	sourcePaths.push_back(spath);
      }
    }
  }
}
