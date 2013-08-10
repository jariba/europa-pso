package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.CESchema
import gov.nasa.arc.europa.constraintengine.DataType
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.component.EnumeratedDomain
import gov.nasa.arc.europa.constraintengine.component.EnumeratedDomain
import gov.nasa.arc.europa.constraintengine.component.RestrictedDT
import gov.nasa.arc.europa.constraintengine.component.SymbolDT
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import gov.nasa.arc.europa.utils.LabelStr._

import scalaz.syntax.equal._

class Schema(val name: LabelStr, val ceSchema: CESchema) { 

  def getName: LabelStr = name

  def reset: Unit = {} //TODO: fill this in

  def isType(t: LabelStr): Boolean = isPrimitive(t) || isObjectType(t) || isEnum(t) || isPredicate(t)

  def isPrimitive(t: LabelStr): Boolean = m_primitives.contains(t.key)

  def isEnum(t: LabelStr): Boolean = m_enumValues.contains(t.key)

  def isEnumValue(t: LabelStr, v: Double): Boolean = m_enumValues.get(t.key) match { 
    case Some(s) => s.contains(v)
    case None => false
  }
  def isEnumValue(v: Double): Boolean = m_enumValues.values.foldLeft(true)(_ && _.contains(v))

  def isPredicate(predicateName: LabelStr): Boolean = m_tokenTypeMgr.getType(this, predicateName).isDefined

  def isObjectType(t: LabelStr): Boolean = m_objectTypeMgr.getObjectType(t).isDefined

  def canBeAssigned(objectType: LabelStr, predicate: LabelStr): Boolean = 
    isA(objectType, getObjectTypeForPredicate(predicate))

  //expect the assemblage of parent types to come from the ObjectType instance
  def canContain(parentType: LabelStr, memberType: LabelStr, memberName: LabelStr): Boolean = { 
    checkError(isType(parentType), parentType, " is not defined")
    checkError(isType(memberType), memberType, " is not defined")
    m_objectTypeMgr.getObjectType(parentType).get.getMemberType(memberName) match { 
      case None => false
      case Some(t) => t.name ≟ memberType
    }
  }

  def getMembers(objectType: LabelStr): List[(LabelStr, LabelStr)] = { 
    m_objectTypeMgr.getObjectType(objectType) match { 
      case None => { checkError(false, objectType, " is not defined"); List()}
      case Some(t) => t.getMembers.toList.map((x) => (LabelStr(x._1), x._2.name))
    }
  }

  def hasMember(parentType: LabelStr, memberName: LabelStr): Boolean = m_objectTypeMgr.getObjectType(parentType) match { 
    case None => { checkError(false, parentType, " is not defined"); false}
    case Some(t) => t.getMemberType(memberName).isDefined
  }

  def getMemberType(parentType: LabelStr, parameterName: LabelStr): LabelStr = m_objectTypeMgr.getObjectType(parentType) match { 
    case None => { checkError(false, parentType, " is not defined"); LabelStr("")}
    case Some(t) => t.getMemberType(parameterName) match { 
      case None => { checkError(false, parentType, " has no member named ", parameterName); LabelStr("")}
      case Some(p) => p.name
    }
  }

  def isA(descendant: LabelStr, ancestor: LabelStr): Boolean  = { 
    if(descendant ≟ ancestor) true
    else { 
      checkError(isType(descendant), descendant, " is not defined")
      checkError(isType(ancestor), "Ancestor ", ancestor, " of ", descendant, "is not defined")
      if(hasParent(descendant)) isA(getParent(descendant), ancestor)
      else isPrimitive(descendant) && isPrimitive(ancestor)
    }
  }

  def hasParent(t: LabelStr): Boolean = { 
    if(isPrimitive(t) || isEnum(t)) false
    else if(isObjectType(t)) m_objectTypeMgr.getObjectType(t) match { 
      case None => false
      case Some(o) => o.getParent.isDefined
    }
    else makeParentPredicateString(t) match { 
      case None => false
      case Some(p) => isPredicate(p) || hasParent(p)
    }
  }
  
  def getParent(t: LabelStr): LabelStr = { 
    checkError(hasParent(t), t, " does not have a parent.")
    if(isObjectType(t)) getObjectType(t).get.getParent.get.getName
    else makeParentPredicateString(t) match { 
      case None => {checkError(false, "Attempted to get a aprent predicate for ", t); LabelStr()}
      case Some(p) => p
    }
  }

  def getAllObjectTypes: Set[LabelStr] = m_objectTypeMgr.getAllObjectTypes.map(_.getName).toSet

  def getEnumValues(t: LabelStr): Set[LabelStr] = { 
    checkError(isEnum(t), t, " is not an enumeration")
    m_enumValues(t.key).map((x) => LabelStr(x.toInt))
  }

  def getEnumForValue(v: Double): LabelStr = LabelStr(v.toInt) //not sure why the Europa code doesn't just do this...

  def getPredicates(t: LabelStr): Set[LabelStr] = m_objectTypeMgr.getObjectType(t) match { 
    case None => { checkError(false, t, " is undefined"); Set()}
    case Some(o) => o.getTokenTypes.values.map(_.getPredicateName).toSet
  }
  
  def hasPredicates(t: LabelStr): Boolean = m_objectTypeMgr.getObjectType(t) match {
    case None => { checkError(false, t, " is undefined"); false}
    case Some(o) => !o.getTokenTypes.isEmpty
  }
  
  def makeParentPredicateString(predicate: LabelStr): Option[String] = { 
    import Schema._
    checkError(predicate.countElements(DELIMITER) == 2,
		"Invalid format for predicate ", predicate)

    val prefix = predicate.getElement(0, DELIMITER)

    // If not a defined class, or has no parent class, do no more and return false
    if(!isObjectType(prefix) || !hasParent(prefix))
      None
    else // Otherwise we are ready to compose with the parent
      Some(getParent(prefix).toString + DELIMITER + predicate.getElement(1, DELIMITER))
  }

  def getObjectTypeForPredicate(predicate: LabelStr): LabelStr = { 
    checkError(isPredicate(predicate),
	       "Predicate ", predicate,
	       " is not defined, but we expect all predicates to be defined. See 'isPredicate'")

    return predicate.getElement(0, Schema.DELIMITER)
  }

  //never used in the c++
  // def getIndexFromName(t: LabelStr, memberName: LabelStr): Int = { 
  // }
  
  //never used in the c++
  // def getNameFromIndex(t: LabelStr, index: Int): LabelStr

  //is this redundant with getEnumForValue?
  // def getEnumFromMember(member: LabelStr): LabelStr 

  def getParameterCount(predicate: LabelStr): Int = { 
    checkError(isPredicate(predicate), predicate, " isn't a predicate.")
    m_tokenTypeMgr.getType(this, predicate).get.getArgs.size
  }

  // def getParameterType(predicate: LabelStr, index: Int): LabelStr = { 
  // }

  def addPrimitive(name: LabelStr): Unit = m_primitives + name.key

  // def addObjectType(t: LabelStr): Unit = 
  // def addObjectType(t: LabelStr, parent: LabelStr): Unit
  // def addPredicate(p: LabelStr): Unit
  // def addMember(parentObjectType: LabelStr, memberType: LabelStr, memberName: LabelStr): Unit
  def addEnum(enumName: LabelStr): Unit = m_enumValues + ((enumName.key, Set()))
  def addValue(name: LabelStr, value: Double): Unit = 
    m_enumValues + ((name.key, m_enumValues.getOrElse(name.key, Set()) + value))
  def registerEnum(name: String, d: EnumeratedDomain): Unit = { 
    addEnum(name)
    d.getValues.foreach(addValue(name, _))
    getCESchema.registerDataType(new RestrictedDT(name, SymbolDT.INSTANCE, d))
  }

  def getEnumerations: List[LabelStr] = m_enumValues.keySet.map(LabelStr(_)).toList
  def getCESchema: CESchema = ceSchema

  private def createDefaultObjectFactory(t: ObjectType, canCreateObjects: Boolean): ObjectFactory = null

  def registerObjectType(t: ObjectType): Unit = { 
    if(t.getObjectFactories.size == 0)
      t.addObjectFactory(createDefaultObjectFactory(t, t.isNative))
    m_objectTypeMgr.registerObjectType(t)
    if(!getCESchema.isDataType(t.getName))
      getCESchema.registerDataType(new ObjectDT(t.getName))
    t.getTokenTypes.map((x) => registerTokenType(x._2))
  }

  def getObjectType(n: LabelStr): Option[ObjectType] = 
    m_objectTypeMgr.getObjectType(n)
  
  def registerTokenType(t: TokenType): Unit = m_tokenTypeMgr.registerType(t)
  def getTokenType(n: LabelStr): Option[TokenType] = m_tokenTypeMgr.getType(this, n)
  def getParentTokenType(t: LabelStr, parentObjectType: LabelStr): Option[TokenType] = { 
    val tokenName = t.getElement(1, Schema.DELIMITER)
    if(isPredicate(parentObjectType.toString + Schema.DELIMITER + tokenName))
      getTokenType(tokenName)
    else if(hasParent(parentObjectType))
      getParentTokenType(t, getParent(parentObjectType))
    else
      None
  }

  def hasTokenTypes: Boolean = m_tokenTypeMgr.hasType

  // def registerMethod(m: Method): Unit
  // def getMethod(n: LabelStr, targetType: DataType, argType: Vector[DataType]): Method
  // def getTypeSupporters(t: TokenType): Vector[TokenType]
  
  val m_objectTypeMgr: ObjectTypeManager = null//new ObjectTypeManager
  val m_tokenTypeMgr: TokenTypeManager = null//new TokenTypeManager
  var m_methods: Map[Int, Method] = Map()
  var m_primitives: Set[Int] = Set()
  var m_enumValues: Map[Int, Set[Double]] = Map()
}

object Schema { 
  val DELIMITER: String = "."

  def makeQualifiedName(objectType: LabelStr, unqualifiedPredicateName: LabelStr): LabelStr =
    objectType.toString + DELIMITER + unqualifiedPredicateName
  def rootObject: LabelStr = "Object"
}
