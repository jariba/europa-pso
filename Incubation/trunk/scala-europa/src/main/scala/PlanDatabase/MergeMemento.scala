package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Error._
import scala.collection.immutable.VectorBuilder

class MergeMemento(m_inactiveToken: Token, m_activeToken: Token) { 
  def undo(activeTokenDeleted: Boolean): Unit = { 
    m_undoing = true
    m_newConstraints.foreach(_.discard)
    m_deactivatedConstraints = List()
    if(!activeTokenDeleted) m_activeToken.getVariables.foreach(_.handleReset)
    m_inactiveToken.getVariables.foreach(_.undoDeactivation)
    m_inactiveToken.getVariables.foreach(_.handleReset)
    m_undoing = false
  }
  def handleAdditionOfInactiveConstraint(c: Constraint): Unit = migrateConstraint(c)
  def handleRemovalOfInactiveConstraint(c: Constraint): Unit = { 
    checkError(m_deactivatedConstraints.length + m_inactiveToken.getVariables.length >= m_newConstraints.length, "")
    checkError(!c.isActive, c)

    if(!m_undoing) { 
      val (deactivated, newConstraint) = m_deactivatedConstraints.zip(m_newConstraints).filterNot(_._1 == c).head
      m_deactivatedConstraints = m_deactivatedConstraints.filterNot(_ == deactivated)
      m_newConstraints = m_newConstraints.filterNot(_ == newConstraint)
      newConstraint.discard
    }
  }

  def migrateConstraint(c: Constraint): Unit = { 
    checkError(m_activeToken.isActive, m_activeToken)
    if(!m_inactiveToken.isStandardConstraint(c)) { 
      debugMsg("europa:merging:migrateConstraint", "Replacing scope for ", c);
      val variables = c.getScope
      val newScope = new VectorBuilder[ConstrainedVariable]
      for(v <- variables) { 
        newScope += (if(v.parent.isEmpty || v.parent.get != m_inactiveToken) v else m_activeToken.getVariables(v.getIndex.get))
      }
      val newConstraint = m_activeToken.getPlanDatabase.getConstraintEngine.createConstraint(c.name, newScope.result)
      newConstraint.setSource(c)
      m_newConstraints = m_newConstraints :+ newConstraint
    }
    m_deactivatedConstraints = m_deactivatedConstraints :+ c
  }

  def init: Unit = { 
    val inactiveVariables = m_inactiveToken.getVariables
    val activeVariables = m_activeToken.getVariables
    checkError(inactiveVariables.length == activeVariables.length,
               "Tried to merge two tokens with different sets of variables.")
    var varMap: Map[Int, ConstrainedVariable] = Map()
    var deactivatedConstraints: Set[Constraint] = Set()
    
    for((inactive, active) <- inactiveVariables.zip(activeVariables)) { 
      checkError(!varMap.contains(inactive.key), "")
      varMap = varMap + ((inactive.key, active))
      deactivatedConstraints = deactivatedConstraints ++ inactive.constraints.map(_._1)
      //activeVariables(i).handleBase(inactiveVariables(i).baseDomain)
      if(inactive.isSpecified)
        active.handleSpecified(inactive.getSingletonValue.get)
      inactive.deactivate
    }
    deactivatedConstraints.filterNot(m_inactiveToken.isStandardConstraint(_)).map(migrateConstraint(_))
  }
  var m_deactivatedConstraints = List[Constraint]()
  var m_newConstraints = List[Constraint]()
  var m_undoing: Boolean = false
  init
  
}
