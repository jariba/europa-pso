package gov.nasa.arc.europa.utils;

import scala.collection.mutable.HashMap;
import scala.collection.mutable.Map;

object Entity {
  private var entities:  Map[Int, Entity] = new HashMap[Int, Entity];
  private var key: Int = 0;
  private var purging: Boolean = false;

  def getEntity(key: Int): Option[Entity] = {
    return entities.get(key);
  }

  protected def nextKey: Int = {val retval = key; key = key + 1; return retval;}
  protected def newEntity(e: Entity) {
    entities += (e.key -> e)
  }
  protected def remove(e: Entity) {
    entities -= e.key
  }

  def isPurging = purging
  def purgeStart = (purging = true)
  def purgeEnd = (purging = false)

    //implicit for Entity ordering?
}

trait Entity /*extends Ordered[Entity]*/ {
  private var m_externalEntity: Entity = null
  private var m_dependentEntity: Entity = null
  val key: Int = Entity.nextKey
  private var discarded: Boolean = false
  Entity.newEntity(this)

  def discard: Unit = {
    if(!discarded) { 
      discarded = true
      handleDiscard
    }
  }

  def isDiscarded: Boolean = discarded

  def handleDiscard: Unit = { 
    m_dependentEntity.notifyDiscarded(this)
    if(m_externalEntity != null)
      m_externalEntity.discard
    Entity.remove(this)
  }

  def notifyDiscarded(e: Entity): Unit = { }

  def compare(that: Entity) : Int = key - that.key
  def name: LabelStr = LabelStr("NO_NAME")

  def entityType: String = "Entity"
  
}

