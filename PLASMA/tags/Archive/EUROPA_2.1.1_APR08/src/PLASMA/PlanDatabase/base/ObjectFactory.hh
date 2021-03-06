#ifndef _H_ObjectFactory
#define _H_ObjectFactory

#include "PlanDatabaseDefs.hh"
#include "LabelStr.hh"
#include <map>
#include <vector>

/**
 * @file Factory class for allocation of objects.
 * @author Conor McGann, March, 2004
 * @see DbClient
 */
namespace EUROPA {

  class ConcreteObjectFactory;
  typedef Id<ConcreteObjectFactory> ConcreteObjectFactoryId;

  /**
   * @brief Singleton, abstract factory which provides main point for object allocation. It relies
   * on binding to concrete object factories for eacy distinct class.
   * @see ConcreteObjectFactory
   */
  class ObjectFactory {
  public:

    /**
     * @brief Helper method to compose full factory signature from type and arguments
     * @param objectType The type of the object defined in a model.
     * @param arguments The sequence of name/value pairs to be passed as arguments for construction of the object
     * @return A ':' deliimited string of <objectType>:<arg0.type>:..:<argn.type>
     */
    static LabelStr makeFactoryName(const LabelStr& objectType, const std::vector<const AbstractDomain*>& arguments);

    /**
     * @brief Create a root object instance
     * @see DbClient::createObject(const LabelStr& type, const LabelStr& name)
     */
    static ObjectId createInstance(const PlanDatabaseId& planDb, 
				   const LabelStr& objectType, 
				   const LabelStr& objectName,
				   const std::vector<const AbstractDomain*>& arguments);
		
	static ObjectId makeNewObject(
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& ancestorType, 
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments);
	                        
    static void evalConstructorBody(ObjectId& instance, 
                                  const LabelStr& objectType, 
                                  const std::vector<const AbstractDomain*>& arguments);				   

    /**
     * @brief Obtain the factory based on the type of object to create and the types of the arguments to the constructor
     */ 
    static ConcreteObjectFactoryId getFactory(const LabelStr& objectType, const std::vector<const AbstractDomain*>& arguments);

    /**
     * @brief Delete all factory instances stored. Should only be used to support testing, since
     * factories should remain for all instances of the plan database in the same process.
     */
    static void purgeAll();

  protected:

    virtual ~ObjectFactory();

  private:
    friend class ConcreteObjectFactory; /*!< Requires access to registerFactory */

    /**
     * @brief Add a factory to provide instantiation of particular concrete types based on a label.
     */
    static void registerFactory(const ConcreteObjectFactoryId& factory);    

    static ObjectFactory& getInstance();

    ObjectFactory();

    std::map<double, ConcreteObjectFactoryId> m_factories;
  };

  /**
   * @brief Each concrete class must provide an implementation for this.
   */
  class ConcreteObjectFactory{
  public:
    const ConcreteObjectFactoryId& getId() const;

    /**
     * @brief Return the type for which this factory is registered.
     */
    const LabelStr& getSignature() const;

    /**
     * @brief Retreive the type signature as a vector of types
     */
    const std::vector<LabelStr>& getSignatureTypes() const;

  protected:
    friend class ObjectFactory;

    ConcreteObjectFactory(const LabelStr& signature);

    /**
     * @brief Protected destructor ensures control so that only the object factory can
     * delete factories.
     */
    virtual ~ConcreteObjectFactory();

    /**
     * @brief Create a root object instance
     * @see DbClient::createObject(const LabelStr& type, const LabelStr& name)
     * for the interpreted version createInstance = makeObject + evalConstructorBody
     */
    virtual ObjectId createInstance(const PlanDatabaseId& planDb, 
				    const LabelStr& objectType, 
				    const LabelStr& objectName,
				    const std::vector<const AbstractDomain*>& arguments) const = 0;
	
				    
    /**
     * @brief makes an instance of a new object, this is purely construction, initialization happens in evalConstructorBody
     * TODO make pure virtual and fix code generation accordingly 
     */
   virtual ObjectId makeNewObject( 
	                        const PlanDatabaseId& planDb,
	                        const LabelStr& objectType, 
	                        const LabelStr& objectName,
	                        const std::vector<const AbstractDomain*>& arguments) const { return ObjectId::noId(); };
    /**
     * @brief The body of the constructor after the object is created
     * any operations done by createInstance to the object after it is created must be done by this method
     * so that calls to "super()" in subclasses can be supported correctly
     * TODO make pure virtual and fix code generation accordingly 
     */
    virtual void evalConstructorBody(ObjectId& instance, const std::vector<const AbstractDomain*>& arguments) const {};
				    

  private:
    ConcreteObjectFactoryId m_id;
    LabelStr m_signature;
    std::vector<LabelStr> m_signatureTypes;
  };
  
  /**
   * Macro for registering object factories with a signature for lookup
   */
#define REGISTER_OBJECT_FACTORY(className, signature) new className(LabelStr(#signature));

}

#endif
