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
  def getTypedEntity[T](key: Int): Option[T] = getEntity(key) match { 
    case Some(v) => Some(v.asInstanceOf[T])
    case None => None
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
    if(m_dependentEntity != null)
      m_dependentEntity.notifyDiscarded(this)
    if(m_externalEntity != null)
      m_externalEntity.discard
    Entity.remove(this)
  }

  def notifyDiscarded(e: Entity): Unit = { }

  def compare(that: Entity) : Int = key - that.key
  def name: LabelStr = LabelStr("NO_NAME")
  def getName: LabelStr = name
  def getKey: Int = key

  def entityType: String = "Entity"

  override def toString: String = (new StringBuilder) append name append "(" append key append ")" toString
  
  def externalEntity: Entity = m_externalEntity
  def getExternalEntity: Entity = m_externalEntity
}

