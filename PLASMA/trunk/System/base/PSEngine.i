%module PSEngineInterface
%include "std_string.i"
%{
#include "PSEngine.hh"
#include "Error.hh"
%}

%rename(PSException) Error;
%typemap(javabase) Error "java.lang.Exception";
%typemap(javacode) Error %{
  public String getMessage() {
    return getMsg();
  }
%}

%typemap(javabody) SWIGTYPE %{
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected $javaclassname(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr($javaclassname obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }
%}

// TODO: There's probably a better way to refer to both package and class name here.
%typemap(throws, throws="psengine.PSException") Error {
  jclass excepClass = jenv->FindClass("psengine/PSException");
  if (excepClass == NULL)
    return $null;

  jmethodID excepConstructor = jenv->GetMethodID(excepClass, "<init>", "(JZ)V");
  if(excepConstructor == NULL)
    return $null;

  jthrowable excep = static_cast<jthrowable> (jenv->NewObject(excepClass, excepConstructor, &$1, true));
  if(excep == NULL)
    return $null;
  else
    jenv->Throw(excep);

  return $null;
}

class Error {
public:
  std::string getMsg();
  std::string getFile();
  std::string getCondition();
  int getLine();
  std::string getType();
private:
  Error();
};

namespace EUROPA {

  typedef int TimePoint;
  typedef int PSEntityKey;

  class PSObject;
  class PSToken;
  class PSSolver;
  class PSVariable;
  class PSVarValue;

  template<class T>
  class PSList {
  public:
    int size() const;
    T& get(int idx);
  protected:
    PSList();
  };

  %template(PSObjectList) PSList<PSObject*>;
  %template(PSTokenList) PSList<PSToken*>;
  %template(PSVariableList) PSList<PSVariable*>;
  %template(PSValueList) PSList<PSVarValue>;

  //trying template instantiation to get the right results.

  %rename(PSStringList) PSList<std::string>;
  class PSList<std::string> {
  public:
    int size() const;
    std::string get(int idx);
  };

  %rename(PSTimePointList) PSList<int>;
  class PSList<int> {
  public:
    int size() const;
    int get(int idx);
  };

// If the output language is java, add a method to the PSEngine
// that can get at the NDDL implementation.
%typemap(javacode) PSEngine %{
  public void executeScript(String language, java.io.Reader reader) throws PSException {
    String txns = null;
    if(language.equalsIgnoreCase("nddl")) {
      try {
        Class nddlClass = ClassLoader.getSystemClassLoader().loadClass("nddl.Nddl");
        Class[] parameters = new Class[]{java.io.Reader.class, boolean.class};
        Object[] arguments = new Object[]{reader, new Boolean(false)};
        txns = (String) nddlClass.getMethod("nddlToXML", parameters).invoke(null, arguments);
      }
      catch(ClassNotFoundException ex) {
        System.err.println("Cannot execute NDDL source: failed to find NDDL implementation.");
        throw new RuntimeException(ex);
      }
      catch(NoSuchMethodException ex) {
        System.err.println("Cannot execute NDDL source: Unexpected NDDL implementation (nddlToXML not found).");
        throw new RuntimeException(ex);
      }
      catch(IllegalAccessException ex) {
        System.err.println("Cannot execute NDDL source: Unexpected NDDL implementation (access modifiers too restrictive on parse method).");
        throw new RuntimeException(ex);
      }
      catch(java.lang.reflect.InvocationTargetException ex) {
        System.err.println("Cannot execute NDDL source: exception during parsing: ");
        System.err.println(ex.getCause().getClass().getName() + ": "+ ex.getCause().getMessage());
        throw new RuntimeException(ex.getCause());
      }
      catch(RuntimeException ex) {
        System.err.println("Cannot execute NDDL source: exception during parsing: ");
        System.err.println(ex.getClass().getName() + ": "+ ex.getMessage());
        throw ex;
      }
    }

    if(txns != null) {
      executeTxns(txns, false , true);
    }
    else {
      throw new RuntimeException("Failed to create transactions from "+language+" source.");
    }
  }
%}

  class PSEngine {
  public:
    PSEngine();

    void start();
    void shutdown();

    void loadModel(const std::string& modelFileName);
    void executeTxns(const std::string& xmlTxnSource, bool isFile, bool useInterpreter) throw(Error);
    std::string executeScript(const std::string& language, const std::string& script) throw(Error);

    PSList<PSObject*> getObjectsByType(const std::string& objectType);
    PSObject* getObjectByKey(PSEntityKey id);

    PSList<PSVariable*> getGlobalVariables();

    PSList<PSToken*> getTokens();
    PSToken* getTokenByKey(PSEntityKey id);

    double getViolation();    

    PSSolver* createSolver(const std::string& configurationFile);   

    std::string planDatabaseToString();     
  };

  class PSEntity
  {
  public:
    PSEntityKey getKey() const;
    const std::string& getName() const;
    const std::string& getEntityType() const;
    
    std::string toString();
        
  protected:
    PSEntity(); //protected constructors prevent wrapper generation
  };


  class PSObject : public PSEntity
  {
  public:
    const PSList<PSVariable*>& getMemberVariables();
    PSVariable* getMemberVariable(const std::string& name);
    PSList<PSToken*> getTokens();
    ~PSObject();
  protected:
    PSObject();
  };

  class PSSolver
  {
  public:
    void step();
    void solve(int maxSteps,int maxDepth);
    void reset();
    void destroy();

    int getStepCount();
    int getDepth();

    bool isExhausted();
    bool isTimedOut();
    bool isConstraintConsistent();

    bool hasFlaws();

    int getOpenDecisionCnt();
    PSList<std::string> getFlaws();
    std::string getLastExecutedDecision();

    const std::string& getConfigFilename();
    int getHorizonStart();
    int getHorizonEnd();

    void configure(int horizonStart, int horizonEnd);
  protected:
    PSSolver();
  };

  class PSToken : public PSEntity
  {
  public:
    bool isFact();
    
    PSObject* getOwner();

    PSToken* getMaster();
    PSList<PSToken*> getSlaves();
    
    double getViolation();
    const std::string& getViolationExpl();

    const PSList<PSVariable*>& getParameters();
    PSVariable* getParameter(const std::string& name);
    
    std::string toString();
    
  protected:
    PSToken();
  };

  enum PSVarType {OBJECT,STRING,INTEGER,DOUBLE,BOOLEAN};

  class PSVariable : public PSEntity
  {
  public:

    bool isEnumerated();
    bool isInterval();

    PSVarType getType();

    bool isSingleton();

    PSVarValue getSingletonValue();    // Call to get value if isSingleton()==true

    PSList<PSVarValue> getValues();  // if isSingleton()==false && isEnumerated() == true

    double getLowerBound();  // if isSingleton()==false && isInterval() == true
    double getUpperBound();  // if isSingleton()==false && isInterval() == true

    void specifyValue(PSVarValue& v);

    double getViolation();
    
    std::string toString();
  protected:
    PSVariable();
  };

  class PSVarValue
  {
  public:
    PSVarValue          getInstance(std::string val);
    PSVarValue          getInstance(int val);
    PSVarValue          getInstance(double val);
    PSVarValue          getInstance(bool val);

    PSVarType getType() const;

    PSObject*           asObject();
    int                 asInt();
    double              asDouble();
    bool                asBoolean();
    const std::string&  asString();
    
    std::string toString();
    
  protected:
    PSVarValue();
  };

}
