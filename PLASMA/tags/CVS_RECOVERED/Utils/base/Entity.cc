#include "Entity.hh"
#include "Debug.hh"
#include <sstream>

namespace EUROPA {

  Entity::Entity():m_key(allocateKey()), m_refCount(1), m_discarded(false){
    entitiesByKey().insert(std::pair<int, int>(m_key, (int) this));
    check_error(!isPurging());
    debugMsg("Entity:Entity", "Allocating " << m_key);
  }

  Entity::~Entity(){
    discardedEntities().erase(this);
    discard(false);
  }

  void Entity::handleDiscard(){
    if(!Entity::isPurging()){

      // Notify dependents
      for(std::set<Entity*>::const_iterator it = m_dependents.begin(); it != m_dependents.end(); ++it){
	Entity* entity = *it;
	entity->notifyDiscarded(this);
      }

      m_dependents.clear();

      check_error(m_externalEntity.isNoId() || m_externalEntity.isValid());
      // If this entity has been integrated with an external entity, then delete the external
      // entity.
      if(!m_externalEntity.isNoId())
	delete (Entity*) m_externalEntity;

      debugMsg("Entity:discard", "Deallocating " << m_key);

      condDebugMsg(!canBeDeleted(), "Entity:warning", 
		   "(" << getKey() << ") being deleted with " << m_refCount << " outstanding references.");
    }
    entitiesByKey().erase(m_key);
  }

  const LabelStr& Entity::getName() const {
    static const LabelStr NO_NAME("NO_NAME");
    return NO_NAME;
  }

  std::string Entity::toString() const{
    std::stringstream sstr;
    sstr << getName().toString() << "(" << getKey() << ")";
    return sstr.str();
  }

  bool Entity::canBeCompared(const EntityId&) const{ return true;}

  EntityId Entity::getEntity(int key){
    EntityId entity;
    std::map<int, int>::const_iterator it = entitiesByKey().find(key);
    if(it != entitiesByKey().end())
      entity = (EntityId) it->second;
    return entity;
  }

  void Entity::setExternalEntity(const EntityId& externalEntity){
    check_error(m_externalEntity.isNoId());
    check_error(externalEntity.isValid());
    m_externalEntity = externalEntity;
  }

  void Entity::clearExternalEntity(){
    m_externalEntity = EntityId::noId();
  }

  const EntityId& Entity::getExternalEntity() const{
    check_error(m_externalEntity.isNoId() || m_externalEntity.isValid());
    return m_externalEntity;
  }

  std::map<int, int>& Entity::entitiesByKey(){
    static std::map<int, int> sl_entitiesByKey;
    return sl_entitiesByKey;
  }

  int Entity::allocateKey(){
    static int sl_key(0);
    sl_key++;
    return sl_key;
  }

  void Entity::purgeStarted(){
    check_error(!isPurging());
    getPurgeStatus() = true;
  }

  void Entity::purgeEnded(){
    check_error(isPurging());
    getPurgeStatus() = false;
  }

  bool Entity::isPurging(){
    return getPurgeStatus();
  }

  bool& Entity::getPurgeStatus(){
    static bool sl_isPurging(false);
    return sl_isPurging;
  }

  unsigned int Entity::refCount() const { return m_refCount; }

  void Entity::incRefCount() {m_refCount++;}

  bool Entity::decRefCount() {
    m_refCount--;
    if(m_refCount == 0){
      discard();
      return true;
    }
    
    return false;
  }

  bool Entity::canBeDeleted() const{ return m_refCount < 2;}

  void Entity::discard(bool pool){
    if(m_discarded)
      return;

    m_discarded = true;
    handleDiscard();
    if(pool)
      discardedEntities().insert(this);
  }

  bool Entity::isDiscarded() const {
    return m_discarded;
  }

  void Entity::addDependent(Entity* entity){
    m_dependents.insert(entity);
  }

  void Entity::removeDependent(Entity* entity){
    m_dependents.erase(entity);
  }

  void Entity::notifyDiscarded(const Entity* entity) {}

  bool Entity::isPooled(Entity* entity) {
    std::set<Entity*>& entities = discardedEntities();
    return entities.find(entity) != entities.end();
  }

  unsigned int Entity::garbageCollect(){
    std::set<Entity*>& entities = discardedEntities();
    unsigned int count(0);
    while(!entities.empty()){
      std::set<Entity*>::iterator it = entities.begin();
      Entity* entity = *it;
      entities.erase(entity);
      checkError(isPurging() || entity->canBeDeleted(), 
		 "Key:" << entity->getKey() << " RefCount:" << entity->refCount());
      debugMsg("Entity:garbageCollect", "Garbage collecting entity " << entity->getKey() << "(" << entity << ")");
      delete (Entity*) entity;
      count++;
    }
    return count;
  }

  std::set<Entity*>& Entity::discardedEntities(){
    static std::set<Entity*> sl_instance;
    return sl_instance;
  }
}
