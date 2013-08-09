package gov.nasa.arc.europa.constraintengine

private[constraintengine] class VariableChangeListener(v: ConstrainedVariable, e: ConstraintEngine) extends DomainListener { 
  def notifyChange(change: DomainListener.ChangeType): Unit = e.notify(v, change)
}
