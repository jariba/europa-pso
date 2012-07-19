#ifndef _H_Entity
#define _H_Entity

#include "LabelStr.hh"
#include "Id.hh"

#include <map>
#include <set>
#include <string>
#include <vector>

namespace EUROPA{

/**
 * @class Entity
 * @brief Basic entity in system
 * @author Conor McGann
 * @ingroup Utility
 */
  class Entity;
  typedef Id<Entity> EntityId;

  class Entity{
  public:
    inline int getKey() const {return m_key;}
    virtual ~Entity();
    virtual const LabelStr& getName() const;

    virtual std::string toString() const;

    virtual bool canBeCompared(const EntityId&) const;

    /**
     * @brief Set an external entity reference, indicating an external entity is shadowing this entity.
     */
    void setExternalEntity(const EntityId& externalEntity);

    /**
     * @brief Special case to reset the external entity to a noId.
     */
    void clearExternalEntity();

    /**
     * @brief Retrieve an external entity reference, if present.
     * @return Will return EntityId::noId() if not assigned.
     */
    const EntityId& getExternalEntity() const;

    /**
     * @brief Get the number of outsatnding references
     */
    unsigned int refCount() const;

    /**
     * @brief Increment the reference count. Use if you wish to delete the entity.
     */
    void incRefCount();

    /**
     * @brief Decrement the reference count. Return true if this self-destructs as a result
     */
    bool decRefCount();

    /**
     * @brief Test of the entity can be deleted. RefCount == 0
     */
    bool canBeDeleted() const;

    /**
     * @brief Discard the entity.
     * @param pool If true it will be pooled for garbage colection
     */
    void discard(bool pool = true);

    /**
     * @brief Test of the class has already been discarded.
     */
    bool isDiscarded() const;

    /**
     * @brief Add a dependent entity. It will be notified when this is discarded
     * @see notifyDiscarded
     */
    void addDependent(Entity* entity);

    /**
     * @brief Remove a dependent entity.
     */
    void removeDependent(Entity* entity);

    /**
     * @brief Retrieve an Entity by key.
     * @return The Id of the requested Entity if present, otherwise a noId;
     */
    static EntityId getEntity(int key);

    /**
     * @brief Retrieve a key
     */
    static int allocateKey();

    /**
     * @brief Indicates a system is being terminated
     */
    static void purgeStarted();

    /**
     * @brief Indicates a system is finished terminating
     */
    static void purgeEnded();

    /**
     * @brief Tests if purge in progress
     */
    static bool isPurging();


    /**
     * @brief Test of the entity by the given key is pooled for deallocation
     */
    static bool isPooled(Entity* entity);

    /**
     * @brief Garbage collect discarded entities
     * @return The number of entities deleted
     */
    static unsigned int garbageCollect();

  protected:
    Entity();

    /**
     * @brief Over-ride to custmize deallocation
     */
    virtual void handleDiscard();

    EntityId m_externalEntity; /*!< Helfpul to make synchronization with other data structures easy */

  private:

    /**
     * @brief Subclasses should over-ride this to handle special data structure management.
     */
    virtual void notifyDiscarded(const Entity* entity);

    const int m_key;
    unsigned int m_refCount;
    bool m_discarded;
    std::set<Entity*> m_dependents;
    static std::map<int, unsigned long int>& entitiesByKey();
    static std::set<Entity*>& discardedEntities();
    static bool& getPurgeStatus();
  };

  /**
   * @brief Key comparator class for ordering in stl containers
   */
  template <class T>
  class EntityComparator{
  public:
    bool operator() (const T& t1, const T& t2) const {
      checkError(t1.isValid(), t1);
      checkError(t2.isValid(), t2);
      return t1->getKey() < t2->getKey();
    }

    bool operator==(const EntityComparator& c){return true;}
  };
}
#endif
