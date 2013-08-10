package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.DomainListener
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

class ObjectTokenRelation(name: LabelStr, propagatorName: LabelStr, engine: ConstraintEngine,
                          variables: Seq[ConstrainedVariable]) extends Constraint(name, propagatorName, engine, variables) { 
  import Constraint._

  override def handleExecute: Unit = { 
    checkError(!m_currentDomain.isEmpty, "Empty object domain!")
    if(m_token.isActive) { 
      if(m_notifiedObjects.isEmpty)
        notifyAdditions
      else
        notifyRemovals
    }
    checkError(isThisValid, "Invalidated ObjectTokenRelation")
  }

  override def handleExecute(v: ConstrainedVariable, argIndex: Int, 
                             changeType: DomainListener.ChangeType): Unit = handleExecute

  override def canIgnore(v: ConstrainedVariable, argIndex: Int, 
                    changeType: DomainListener.ChangeType): Boolean = { 
    debugMsg("ObjectTokenRelation:canIgnore", 
             m_token, " Received notification of change type ", changeType, " on variable ",
             v)

    if(changeType == DomainListener.RESET || changeType == DomainListener.RELAXED){
      debugMsg("ObjectTokenRelation:canIgnore", 
               "Evaluating relaxation event on ", v.toLongString)
      // Still active so must have been object variable relaxed. Notify Additions.
      if (m_token.isActive){ 
	// Otherwise, handle possible notifications for new values in object domain.
	debugMsg("ObjectTokenRelation:canIgnore", "Handling addition");
	notifyAdditions
      }
      else { // It is no longer active but we have outstanding objects to be notified of removal
      	debugMsg("ObjectTokenRelation:canIgnore", "Handling removal");
      	notifyRemovals
      }
      checkError(isThisValid, "Invalidated")
    }
    else if (m_token.isActive){ // It is a restriction so handle it straight away
      debugMsg("ObjectTokenRelation::canIgnore", 
               "Processing activation event on ", m_token)
      handleExecute
    }

    return true
  }

  override def handleDiscard: Unit = { 
    if(!Entity.isPurging)
      notifyRemovals
    super.handleDiscard
  }
  def notifyAdditions: Unit = { 
    for(i <- m_currentDomain.getValues; o <- Entity.getTypedEntity[Object](i.toInt)) { 
      if(!m_notifiedObjects.contains(o)) { 
        o.add(m_token)
        m_notifiedObjects = m_notifiedObjects + o
      }
    }
  }

  def notifyRemovals: Unit = { 
  }

  def isThisValid: Boolean = (!m_token.isActive && m_notifiedObjects.isEmpty) ||
                             (m_token.isActive && !m_notifiedObjects.isEmpty)
  
  val m_token: Token = variables(0).parent.get.asInstanceOf[Token]
  var m_notifiedObjects: Set[Object] = Set()
  val m_currentDomain: ObjectDomain = getCurrentDomain(variables(1)).asInstanceOf[ObjectDomain]
}
