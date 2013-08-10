package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Entity._
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

import scalaz.Equal
import scalaz.syntax.equal._

abstract class StateDomain extends Domain { 
}

abstract class Token(val predicateName: LabelStr, val isRejectable: Boolean, val isFact: Boolean,
            val durationBaseDomain: IntervalIntDomain, var objectName: LabelStr,
            var closed: Boolean) extends Entity { 
  import plandb._

  type StateVar = Variable[StateDomain]

  def setName(name: LabelStr): Unit = { } //?

  def master: Option[Token] = m_master
  def getRelation: LabelStr = m_relation

  def getSlave(pos: Int): Option[Token] = m_slaves.drop(pos).headOption

  def getSlavePosition(t: Token): Int = {for(i <- 0 to m_slaves.size; s <- m_slaves) if(s ≟ t) return i; return -1;}
  def getPlanDatabase: PlanDatabase = m_planDb
  def getBaseObjectType: LabelStr = m_baseObjectType
  def getPredicateName: LabelStr = m_predicateName
  def getUnqualifiedPredicateName: LabelStr = m_unqualifiedPredicateName

  def getState: StateVar = m_state
  def getObject: ObjectVar = m_object
  def start: TimeVar
  def end: TimeVar
  def duration: TimeVar
  def parameters: Vector[ConstrainedVariable] = m_parameters
  def getVariables: Vector[ConstrainedVariable] = m_allVariables
  def getVariable(name: LabelStr,
                  checkGlobalContext: Boolean = true): Option[ConstrainedVariable] = { 
    getVariables.find(_.getName == name).orElse(if(checkGlobalContext) getPlanDatabase.getGlobalVariable(name) else None)
  }
  
  def slaves: Set[Token] = m_slaves
  def getMergedTokens: Set[Token] = m_mergedTokens
  def getActiveToken: Option[Token] = m_activeToken
  
  def addStandardConstraint(c: Constraint): Unit = m_standardConstraints = m_standardConstraints + c
  def isStandardConstraint(c: Constraint): Boolean = m_standardConstraints.contains(c)
  def getStandardConstraints: Set[Constraint] = m_standardConstraints
  
  def getViolation: Double = m_standardConstraints.foldLeft(0.0)(_ + _.getViolation)
  def getViolationExpl: String = m_standardConstraints.foldLeft("")(_ + _.getViolationExpl)
  
  def isIncomplete: Boolean = false //?

  def makeFact: Unit = m_isFact = true

  def isActive: Boolean = m_state.isSpecified && m_state.specifiedValue.get == Token.ACTIVE.key
  def isInactive: Boolean = !isIncomplete && !m_state.isSpecified
  def isMerged: Boolean = m_state.isSpecified && m_state.specifiedValue.get == Token.MERGED.key
  def isRejected: Boolean = m_state.isSpecified && m_state.specifiedValue.get == Token.MERGED.key

  def close: Unit = m_planDb.notifyAdded(this)
  def isClosed: Boolean = true

  def doMerge(active: Token): Unit = { 
    checkError(isValid, "Invalid")
    checkError(isInactive, "Tried to merge active token ", this);
    checkError(active.isActive, "Tried to merge ", this, " onto inactive token ", active);
    checkError(m_state.lastDomain.isMember(Token.MERGED),
               "Not permitted to merge ", this)
    checkError(getPlanDatabase.getSchema.isA(active.getPredicateName, m_predicateName),
	       "Cannot merge tokens with different predicates: ", m_predicateName, ", ",  
               active.getPredicateName)

    m_state.specify(Token.MERGED)
    m_unifyMemento = new UnifyMemento(this, active)
    m_activeToken = Some(active)
    active.addMergedToken(this)

    /* Send a message to all objects that it has been rejected */
    ObjectDomain.makeObjectList(getObject.baseDomain.getValues).foreach(_.notifyMerged(this))

    m_planDb.notifyMerged(this)

  }
  def activate: Unit
  def reject: Unit
  def cancel: Unit

  def handleAdditionOfInactiveConstraint(c: Constraint): Unit
  def handleRemovalOfInactiveConstraint(c: Constraint): Unit

  def isAssigned: Boolean
  def isDeleted: Boolean

  def addLocalVariable(v: ConstrainedVariable): Unit
  def removeLocalVariable(v: ConstrainedVariable): Unit
  def getLocalVariables: Set[ConstrainedVariable]
  
  def addParameter[D <: Domain](base: D, name: LabelStr): ConstrainedVariable
  
  def canBeCompared(e: Entity): Boolean
  def removeMaster(t: Token): Boolean
  
  def split: Unit
  def deactivate: Unit
  def reinstate: Unit
  def add(slave: Token): Unit
  def remove(slave: Token): Unit
  
  def activateInternal: Unit
  def addMergedToken(t: Token): Unit
  def removeMergedToken(t: Token): Boolean

  def toLongString: String

  def isValid: Boolean
  def refCount: Int

  var m_master: Option[Token]
  var m_relation: LabelStr
  var m_slaves: Set[Token]
  var m_planDb: PlanDatabase
  var m_baseObjectType: LabelStr
  var m_predicateName: LabelStr
  var m_unqualifiedPredicateName: LabelStr
  var m_state: StateVar
  var m_object: ObjectVar
  var m_parameters: Vector[ConstrainedVariable]
  var m_allVariables: Vector[ConstrainedVariable]
  var m_mergedTokens: Set[Token]
  var m_activeToken: Option[Token]
  var m_standardConstraints: Set[Constraint]
  var m_isFact: Boolean
  var m_unifyMemento: MergeMemento
}

object Token { 
  val INCOMPLETE = LabelStr("INCOMPLETE")
  val INACTIVE = LabelStr("INACTIVE")
  val ACTIVE = LabelStr("ACTIVE")
  val MERGED = LabelStr("MERGED") 
  val REJECTED = LabelStr("REJECTED")
  def isStateVariable(v: ConstrainedVariable): Boolean = false
  val noObject: LabelStr = LabelStr("NO_OBJECT_ASSIGNED")

  private var varKey: Int = -1;
  def makePseudoVarName: LabelStr = {varKey = varKey + 1; "PSEUDO_VARIABLE_" + varKey}
  import gov.nasa.arc.europa.utils.EqualImplicits.intEq
  implicit def TokenEqual: Equal[Token] = Equal.equalBy(_.key)
}

abstract class TokenType { 
  def createInstance(db: PlanDatabase, name: LabelStr, rejectable: Boolean, isFact: Boolean): Option[Token]
  def createInstance(master: Token, tokenType: LabelStr, relation: LabelStr): Option[Token]

  def addArg(t: DataType, name: LabelStr): Unit
  def getPredicateName: LabelStr
  override def toString: String
  def toLongString: String
  def getArgs: Map[LabelStr, DataType]
  def getArgType(n: LabelStr): Option[DataType]
  def getParentType: Option[TokenType]
  def getObjectType: ObjectType
  def getSignature: LabelStr

}

abstract class TokenTypeManager { 

  def purgeAll: Unit
  def hasType: Boolean
  def registerType(t: TokenType): Unit
  def getType(schema: Schema, name: LabelStr): Option[TokenType]
}
