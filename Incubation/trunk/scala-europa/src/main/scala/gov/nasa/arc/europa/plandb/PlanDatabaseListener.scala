package gov.nasa.arc.europa.plandb

trait PlanDatabaseListener { 
  def notifyAdded(o: Object): Unit
  def notifyRemoved(o: Object): Unit

  def notifyAdded(t: Token): Unit
  def notifyRemoved(t: Token): Unit

  def notifyActivated(t: Token): Unit
  def notifyDeactivated(t: Token): Unit
  def notifyMerged(t: Token): Unit
  def notifySplit(t: Token): Unit
  def notifyRejected(t: Token): Unit
  def notifyReinstated(t: Token): Unit

  def notifyConstrained(obj: Object, predecessor: Token, successor: Token): Unit
  def notifyFreed(obj: Object, predecessor: Token, successor: Token): Unit

  def notifyAdded(o:Object, t: Token): Unit
  def notifyRemoved(o:Object, t: Token): Unit

  val planDatabase: PlanDatabase
  planDatabase.notifyAdded(this)

}
