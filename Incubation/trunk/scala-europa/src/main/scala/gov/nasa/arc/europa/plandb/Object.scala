package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Scope
import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.constraintengine.component.EnumeratedDomain
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.{ Set => MSet}
//import scala.collection.mutable.Set

import scalaz.syntax.equal._

sealed trait ObjectState
case object INCOMPLETE extends ObjectState
case object COMPLETE extends ObjectState

//TODO: make this not abstract
abstract class Object(val planDb: PlanDatabase, val objectType: LabelStr, override val name: LabelStr, open: Boolean) extends Entity { 
  def this(parent: Object, objectType: LabelStr, localName: LabelStr, open: Boolean) = { 
    this(parent.planDb, objectType, parent.name.toString + localName.toString, open)
    this.parent = Some(parent)
  }

  def constructor(args: Vector[Domain]): Unit = { }


  def addVariable(base: Domain, name: String): Option[ConstrainedVariable] = { 
    val varTypeName: LabelStr = planDb.getSchema.getMembers(objectType).filter(_._1 ≟ name).map(_._2).headOption.getOrElse(base.getTypeName)

    val typeDomain = planDb.getConstraintEngine.getSchema.baseDomain(varTypeName).get

    checkError(base.isSubsetOf(typeDomain), 
               "Variable ", name, " of type ", varTypeName, " can not be set to ", base)

    checkError(!isComplete,
	       "Cannot add variable ", name, " after completing object construction for ",
               this.name)

    checkError(planDb.getSchema.canContain(objectType, varTypeName, name),
	       "Cannot add a variable ", name, " of type ", base.getTypeName,
	       " to objects of type ", objectType)

    if(!base.isSubsetOf(typeDomain)) {
      return None
    }

    val fullVariableName = this.name + "." + name

    // TODO: Should this be considered internal, I think so?
    val id =
      planDb.getConstraintEngine.createVariableWithDomain(varTypeName,
				                          base,
				                          false, 
				                          true,
				                          fullVariableName,
				                          Some(this),
				                          variables.size)

    variables = variables :+ id.get
    return id
  }

  def getParent: Option[Object] = parent
  
  def getPlanDatabase: PlanDatabase = planDb
  
  def getThis: ConstrainedVariable = thisVar
  
  def getType: LabelStr = objectType

  def getRootType: LabelStr = { 
    def rootTypeRecurse(t: LabelStr) = 
      if(planDb.getSchema.hasParent(t)) planDb.getSchema.getParent(t) else t
    rootTypeRecurse(objectType)
  }

  //def getName: LabelStr = name

  def getComponents = components

  def getAncestors: List[Object] = parent match { 
    case Some(o) => o :: o.getAncestors
    case None => List()
  }

  def hasToken(t: Token): Boolean = tokens.contains(t) && t.getObject.lastDomain.isSingleton

  def getOrderingChoices(t: Token): Vector[(Token, Token)] = Vector((t, t))
  
  def countOrderingChoices(t: Token): Int = {lastOrderingChoiceCount = getOrderingChoices(t).size; lastOrderingChoiceCount}

  def lastOrderingChoiceCount(t: Token): Int = lastOrderingChoiceCount

  def getTokensToOrder: Vector[Token] = Vector()
  
  def hasTokensToOrder: Boolean = false
  
  def constrain(predecessor: Token, successor: Token, isExplicit: Boolean = true): Unit = { 
    checkError(predecessor.isActive, predecessor, ": ", predecessor.getState)
    checkError(successor.isActive, successor, ": ", successor.getState)

    checkError(!isConstrainedToPrecede(predecessor, successor),
	       "Attempted to constrain previously constrained tokens.")

    // Post constraints on object variable, predecessor only in event they are equal
    constrainToThisObjectAsNeeded(predecessor);

    debugMsg("Object:constrain",
             "Constraining ", predecessor, " to be before ", successor,
             if(isExplicit) " explicitly." else  " implicitly.")




    // If successor is not noId then add the precede constraint.
    if (!(predecessor ≟ successor)) {
      constrainToThisObjectAsNeeded(successor) // Also constrain successor if necessary
      
      // Create the precedence constraint
      val constraint = getPlanDatabase.getConstraintEngine.
      createConstraint("precedes",
		       Scope(predecessor.end, successor.start))

      // Store for bi-directional access by encoded key pair and constraint
      constraintsByKeyPair.addBinding((predecessor.key, successor.key), constraint)
      keyPairsByConstraintKey = keyPairsByConstraintKey + ((constraint.key, (predecessor.key, successor.key)))

      // Store for access by token
      constraintsByTokenKey.addBinding(predecessor.key, constraint)
      constraintsByTokenKey.addBinding(successor.key, constraint)

      if(isExplicit)
        explicitConstraints = explicitConstraints + constraint.key

    }
    else if(isExplicit) { 
        explicitConstraints = explicitConstraints + predecessor.key
    }

    planDb.notifyConstrained(this, predecessor, successor)
    checkError(isValid, this, " is no longer valid")

    if (getPlanDatabase.getConstraintEngine.getAutoPropagation)
    	getPlanDatabase.getConstraintEngine.propagate
  }

  def free(predecessor: Token, successor: Token, isExplicit: Boolean = true): Unit = { 
    checkError(!Entity.isPurging, "Can't free a token while purging")
    checkError(constraintsByTokenKey.contains(predecessor.key), 
               "No constraint found on predecessor.");
    checkError(constraintsByTokenKey.contains(successor.key),
	       "No constraint found on successor.");

    checkError(predecessor ≟ successor ||
               getPrecedenceConstraint(predecessor, successor).isDefined,
	       "A precedence constraint is required to free.");

    if(predecessor ≟ successor){
      if(isExplicit){
	checkError(explicitConstraints.contains(predecessor.key),
		   "May only explicit free and explicit constraint.")
	// Remove as an explicit constraint. No harm if it does not exist
	explicitConstraints = explicitConstraints - predecessor.key
      }
      // Clean up for token
      clean(predecessor)
    }
    else {
      // Now retrieve the constraint to be deleted
      val constraint = getPrecedenceConstraint(predecessor, successor).get

      removePrecedenceConstraint(constraint)

      // Now clean up tokens if there are no more precedence constraints left - will remove restriction
      // posted in this object
      clean(predecessor)
      clean(successor)
    }

    planDb.notifyFreed(this, predecessor, successor)
    checkError(isValid, this, " is no longer valid")

    if(getPlanDatabase.getConstraintEngine.getAutoPropagation)
      getPlanDatabase.getConstraintEngine.propagate
  }

  def getVariables: Vector[ConstrainedVariable] = variables

  def getVariable(n: LabelStr): Option[ConstrainedVariable] = variables.find(_.name == n)

  def close: Unit = { 
    state = COMPLETE
    planDb.notifyAdded(this)
  }

  def isComplete: Boolean = state == COMPLETE
  
  def isConstrainedToThisObject(t: Token): Boolean = constraintsByTokenKey.contains(t.key)

  def isConstrainedToPrecede(p: Token, t: Token): Boolean = false //TODO: Fill this in

  def canBeCompared(e: Entity): Boolean = e.isInstanceOf[Object]

  def notifyMerged(t: Token): Unit = { }
  
  def notifyRejected(t: Token): Unit = { }
  
  def notifyDeleted(t: Token): Unit = remove(t)

  def setParent(parent: Object): Unit = { 
    this.parent = Some(parent)
    parent.add(this)
  }

  def toLongString: String = (new StringBuilder(getType.toString)) append ":" append getName toString

  def getPrecedenceConstraint(pred: Token, succ: Token): Option[Constraint] = constraintsByKeyPair.getOrElse((pred.key, succ.key), Set()).headOption


  def getPrecedenceConstraints(t: Token): Vector[Constraint] = ((new VectorBuilder()) ++= constraintsByTokenKey(t.key)).result


  override def handleDiscard { 
    if(!Entity.isPurging) { 
      checkError(isValid, "Tried to discard an invalid object")
      thisVar.discard
      variables.map(_.discard)
      components.map(_.cascadeDelete)
      parent match { 
        case Some(p) => p.remove(this)
        case None => {}
      }
      planDb.notifyRemoved(this)
    }
    super.handleDiscard
  }

  def add(token: Token): Unit = { 
    checkError(isComplete, "Tried to add ", token, " to incomplete object ", this)
    debugMsg("Object:add:token ", "Adding token ", token, " to ", this)
    tokens = tokens + token
    planDb.notifyAdded(this, token)
  }
  
  def remove(token: Token): Unit = { 
    checkError(isValid, "Tried to remove ", token, " from invalid object ", this)
    checkError(!Entity.isPurging, "Tried to remove an object while purging")
    tokens = tokens - token
    //FINISH THIS
    val constraints = constraintsByTokenKey.getOrElse(token.key, MSet())
    constraintsByTokenKey -= token.key

    constraints.filter((x) => keyPairsByConstraintKey.contains(x.key)).foreach(removePrecedenceConstraint(_))
    
  }

  def add(component: Object): Unit = { 
    checkError(!components.contains(component), "Tried to add ", component, " to ", this, " twice")
    checkError(component.planDb == planDb, 
               "Tried to add ", component, " as child of ", this, 
               ", which is from a different database")
    checkError(component.parent.isDefined,
               "Tried to add ", component, " to ", this, " which isn't its parent")
    checkError(component.parent.get == this,
               "Tried to add ", component, " to ", this, " which isn't its parent (",
               component.parent.get, ")")
    components = components + component
  }

  def remove(component: Object): Unit = { 
    checkError(!Entity.isPurging, "Tried to remove an object while purging")
    checkError(components.contains(component),
               "Tried to remove ", component, " from ", this, ", but it isn't a component")
    components = components - component
  }

  
  def cascadeDelete: Unit = { 
    parent = None
    discard
  }

  def rootType: String = { 
    def rootTypeRecurse(t: LabelStr): LabelStr = { 
      if(planDb.schema.hasParent(t))
        rootTypeRecurse(planDb.schema.getParent(t))
      else
        t
    }
    rootTypeRecurse(objectType)
  }

  def hasExplicitConstraint(t: Token): Boolean = { 
    for(c <- getPrecedenceConstraints(t)){ if(explicitConstraints.contains(c.key)) return true}
    return explicitConstraints.contains(t.key)
  }
  
  def freeImplicitConstraints(t: Token): Unit = { 
    for(c <- getPrecedenceConstraints(t)) free(c.getScope(0).parent.get.asInstanceOf[Token],
                                               c.getScope(1).parent.get.asInstanceOf[Token],
                                               false)
  }

  def isValid: Boolean = { 
    // checkError(constraintsByKeyPair.size == keyPairsByConstraintKey.size,
    //            "Lookup tables should have identical sizes. Must be out of synch.")

    // // Validate tokens and constraints
    // for(k <- constraintsByTokenKey.keys) { 
    //   checkError(Entity.getEntity(k).isDefined, "")
    // }

    
    // for(std::multimap<int, ConstraintId>::const_iterator it = m_constraintsByKeyPair.begin();
    //     it != m_constraintsByKeyPair.end();
    //     ++it){
    //   checkError(it->second.isValid(), "Invalid constraint for key pair " << LabelStr(it->first).toString());
    //   checkError(m_keyPairsByConstraintKey.find(it->second->getKey())->second == it->first,
    //     	  "Lookup should be symmetric.");
    // }

    return true;

  }

  def notifyOrderingRequired(t: Token): Unit
  
  def notifyOrderingNoLongerRequired(t: Token): Unit

  def removePrecedenceConstraint(c: Constraint): Unit = { 
    val predecessor = c.getScope(0).parent.get.asInstanceOf[Token]
    val successor = c.getScope(1).parent.get.asInstanceOf[Token]

    constraintsByKeyPair.removeBinding((predecessor.key, successor.key), c)

    keyPairsByConstraintKey = keyPairsByConstraintKey - c.key
    explicitConstraints = explicitConstraints - c.key

    // Remove the entries in the m_constraintsByTokenKey list for predecessor and successor
    constraintsByTokenKey.removeBinding(predecessor.key, c)
    constraintsByTokenKey.removeBinding(successor.key, c)

    // Delete the actual constraint
    c.discard
  }

  private def clean(t: Token): Unit = {} //TODO: implement

  private def clean(c: Constraint, tokenKey: Int): Unit = {} //TODO: implement
  
  private def constrainToThisObjectAsNeeded(t: Token): Unit = {} //TODO: implement

  var state: ObjectState = INCOMPLETE
  var parent: Option[Object] = None
  var components: Set[Object] = Set()
  var variables: Vector[ConstrainedVariable] = Vector()
  var tokens: Set[Token] = Set()
  var explicitConstraints: Set[Int] = Set()
  var lastOrderingChoiceCount: Int = 0
  val constraintsByTokenKey: MultiMap[Int, Constraint] = new HashMap[Int, MSet[Constraint]] with MultiMap[Int, Constraint]
  val constraintsByKeyPair: MultiMap[(Int, Int), Constraint] = new HashMap[(Int, Int), MSet[Constraint]] with MultiMap[(Int, Int), Constraint]
  var keyPairsByConstraintKey: Map[Int, (Int, Int)] = Map()
  val thisVar: ConstrainedVariable = new Variable[ObjectDomain](planDb.constraintEngine, 
                                                                ObjectDomain(this, planDb.constraintEngine.getSchema.getDataType(objectType).get))
  if(!open)
    close
}

class ObjectDT(name: LabelStr) extends DataType(name, 1, null) { 
  override val isNumeric = false
  override val isBool = false
  override val isString = false
  override val isEntity = true
  override val isSymbolic = false
  
  override def createValue(value: String): Option[Double] = Some(LabelStr(value))
  override def toString(value: Double): String = { 
    if(Entity.isPurging) "Object data unavailable while purging (might no longer exist)"
    else Entity.getTypedEntity[Object](value.toInt).toString
  }
  override def emptyDomain: Domain = new ObjectDomain()
}

object ObjectDT { 
  val INSTANCE = new ObjectDT("object")
}

class ObjectDomain(dataType: DataType = ObjectDT.INSTANCE) extends EnumeratedDomain(dataType) { 
  checkError(dataType.isEntity, "Tried to construct an object domain with non-entity datatype ", dataType)

  def this(v: Set[Double], dataType: DataType = ObjectDT.INSTANCE) = { 
    this(dataType)
    values = values ++ v
  }
  // def this(v: Set[Object], dataType: DataType = ObjectDT.INSTANCE) = this(v.map(_.key.toDouble), dataType)
  def this(d: Domain) = { 
    this(d.getValues.toSet, d.dataType)
  }

  def getObject(key: Int): Option[Object] = if(isMember(key)) Entity.getTypedEntity[Object](key) else None
  
  def makeObjectList: List[Object] = ObjectDomain.makeObjectList(values)
  
  override def convertToMemberValue(strValue: String): Option[Double] = 
    try { 
      val value = java.lang.Integer.parseInt(strValue)
      if(Entity.getEntity(value).isDefined) Some(value.toDouble)
      else None
    }
    catch { 
      case _: Throwable => None
    }
  
  
  override def copy: Domain = new ObjectDomain(this)
  def isMember(v: Object): Boolean = isMember(v.key.toDouble)
  def remove(v: Object): Unit = remove(v.key.toDouble)
  
}

object ObjectDomain { 
  def apply(v: Set[Object], dataType: DataType): ObjectDomain = new ObjectDomain(v.map(_.key.toDouble), dataType)
def apply(v: List[Object], dataType: DataType): ObjectDomain = apply(v.toSet, dataType)
  def apply(v: Object, dataType: DataType): ObjectDomain = apply(Set(v), dataType)
  
  def makeObjectList(inputs: Iterable[Double]): List[Object] =  { 
    import scalaz._
    import Scalaz._
    inputs.map((x) => Entity.getTypedEntity[Object](x.toInt)).toList.sequence.get
  }
  
}

abstract class ObjectFactory { 
  def createInstance(db: PlanDatabase, objectType: LabelStr, objectName: LabelStr, args: Seq[Domain]): Option[Object]
}

abstract class ObjectType(val name: String, val parent: Option[ObjectType], val isNative: Boolean = false) { 
  def getVarType: DataType
  def getName: LabelStr = name
  def getParent: Option[ObjectType] = parent
  def addMember(t: DataType, name: String): Unit
  def getMembers: Map[String, DataType]
  def getMemberType(n: String): Option[DataType]
  def addObjectFactory(f: ObjectFactory): Unit
  def getObjectFactories: Map[Int, ObjectFactory]

  def addTokenType(t: TokenType): Unit
  def getTokenTypes: Map[Int, TokenType]
  def getTokenType(signature: LabelStr): TokenType
  def getParentType(t: TokenType): TokenType
  override def toString: String
}

abstract class ObjectTypeManager { 
  def registerObjectType(t: ObjectType)
  def getObjectType(n: LabelStr): Option[ObjectType]
  def getAllObjectTypes: Vector[ObjectType]
  def getFactory(schema: Schema, typeName: LabelStr, arguments: Vector[Domain], doCheckError: Boolean = true)
  def registerFactory(f: ObjectFactory): Unit
  def purgeAll: Unit
}

object ObjectTypeManager { 
  def makeFactoryName(objectType: LabelStr, arguments: Vector[Domain]): LabelStr = ""
  
}
