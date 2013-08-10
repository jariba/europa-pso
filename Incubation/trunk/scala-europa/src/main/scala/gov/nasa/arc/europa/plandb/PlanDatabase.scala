package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.ConstraintEngine
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr
import scala.collection.immutable.VectorBuilder
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.{ Set => MSet}

class PlanDatabase(val constraintEngine: ConstraintEngine, val schema: Schema) { 
  object Event extends Enumeration {
    type Event = Value
    val TOKEN_ADDED,
        TOKEN_REMOVED,
        TOKEN_CLOSED,
        TOKEN_ACTIVATED,
        TOKEN_DEACTIVATED,
        TOKEN_MERGED,
        TOKEN_SPLIT,
        TOKEN_REJECTED,
        TOKEN_REINSTATED,
        OBJECT_ADDED,
        OBJECT_REMOVED = Value
  }
  import Event._

  object State extends Enumeration {
    type State = Value
    val OPEN,
    CLOSED,
    PURGED = Value
  }
  import State._

  def getConstraintEngine: ConstraintEngine = constraintEngine
  def getSchema: Schema = schema
  def getTemporalAdvisor: TemporalAdvisor = m_temporalAdvisor
  def setTemporalAdvisor(t: TemporalAdvisor): Unit = m_temporalAdvisor = t
  def getClient: DbClient = m_client
  def getEntityByKey(k: Int) = Entity.getEntity(k)
  def getObjects: Set[Object] = m_objects
  def getTokens: Set[Token] = m_tokens
  def getActiveTokens(predicate: LabelStr): Set[Token] = m_activeTokensByPredicate.getOrElse(predicate.key, Set())

  def registerGlobalVariable(v: ConstrainedVariable): Unit = { 
    checkError(!isGlobalVariable(v.name), v, " is not unique.")
    m_globalVariables = m_globalVariables + v
    m_globalVarsByName = m_globalVarsByName + ((v.name.key, v))
    checkError(isGlobalVariable(v.name), v, " didn't get registered.  This shouldn't be!")
    debugMsg("PlanDatabase:registerGlobalVariable", "Registered ", v);
  }

  def unregisterGlobalVariable(v: ConstrainedVariable): Unit = { 
    checkError(isGlobalVariable(v.name), v, " is not a global variable.");
    m_globalVariables = m_globalVariables - v
    m_globalVarsByName = m_globalVarsByName - v.name.key
    checkError(!isGlobalVariable(v.name), v, " failed to un-register.");
    debugMsg("PlanDatabase:unregisterGlobalVariable",
	     "Un-registered ", v);
  }

  def getGlobalVariables: Set[ConstrainedVariable] = m_globalVariables
  def getGlobalVariable(name: LabelStr): Option[ConstrainedVariable] = m_globalVarsByName.get(name.key)
  def isGlobalVariable(name: LabelStr): Boolean = m_globalVarsByName.contains(name.key)

  def registerGlobalToken(t: Token): Unit = { 
    checkError(!isGlobalToken(t.name), t.name, " is not unique. Can't register global token");
    m_globalTokens = m_globalTokens + t
    m_globalTokensByName = m_globalTokensByName + ((t.name.key, t))

    checkError(isGlobalToken(t.name), t.toLongString,
               " is not registered after all. This cannot be!.");
    debugMsg("PlanDatabase:registerGlobalToken", "Registered ", t.name);
  }

  def unregisterGlobalToken(t: Token): Unit = { 
    checkError(isGlobalToken(t.name), t.name, " is not a global token.");
    m_globalTokens = m_globalTokens - t
    m_globalTokensByName = m_globalTokensByName - t.name.key
    checkError(!isGlobalToken(t.name), t.name, " failed to un-register.");
    debugMsg("PlanDatabase:unregisterGlobalToken", "Un-registered ", t.name);
  }

  def getGlobalTokens: Set[Token] = m_globalTokens
  def getGlobalToken(name: LabelStr): Option[Token] = m_globalTokensByName.get(name.key)
  def isGlobalToken(name: LabelStr): Boolean = m_globalTokensByName.contains(name.key)
  def hasCompatibleTokens(t: Token): Boolean = countCompatibleTokens(t) > 0

  def getCompatibleTokens(t: Token): Vector[Token] = getCompatibleTokens(t, false)

  def getCompatibleTokens(t: Token, useExactTest: Boolean): Vector[Token] = { 
    val results = new VectorBuilder[Token]
    if(!constraintEngine.propagate)
      return results.result

    // Draw from list of active tokens of the same predicate
    val candidates = getActiveTokens(t.getPredicateName)

    condDebugMsg(candidates.isEmpty, "PlanDatabase:getCompatibleTokens",
                 "No candidates to evaluate for " , t.toString)

    val tVariables = t.getVariables
    val variableCount = tVariables.length

    val temporalAdvisor = getTemporalAdvisor

    for(candidate <- candidates) {
      debugMsg("PlanDatabase:getCompatibleTokens",
               "Evaluating candidate token (" , candidate.getKey , ") for token (" , t.getKey , ")")

      // Validate expectation about being active and predicate being the same
      checkError(schema.isA(candidate.getPredicateName, t.getPredicateName),
                 candidate.getPredicateName, " is not a ", t.getPredicateName)

      checkError(candidate.isActive, "Should not be trying to merge an active token.")

      val candidateTokenVariables = candidate.getVariables

      // Check assumption that the set of variables is the same
      checkError(candidateTokenVariables.length == variableCount,
	         "Candidate token (" , candidate.getKey , ") has " ,
	         candidateTokenVariables.length , " variables, while inactive token (" ,
	         t.getKey , ") has " , variableCount)

      // Iterate and ensure there is an intersection. This could possibly be optmized based on
      // the cost of comparing domains, or the likelihood of a variable excluding choice. Smaller domains
      // would seem to offer better options on both counts, in general. Don't yet know if this even needs
      // optimization
      var isCompatible = true

      checkError(tVariables(0) == t.getState,
                 "We expect the first var to be the state var, which we must skip.")

      import scala.util.control.Breaks._
      breakable { 
        for((varA, varB) <- tVariables.tail.zip(candidateTokenVariables.tail)) { 
          val (domA, domB) = (varA.lastDomain, varB.lastDomain)

            checkError(Domain.canBeCompared(domA, domB),
	               domA.toString , " cannot be compared to " , domB.toString , ".")

          if(domA.size.get == 0 && domB.size.get == 0)
            isCompatible = true
          else if(domA.isOpen && domB.isOpen)
            isCompatible = true
          else if(domA.size.get < domB.size.get)
            isCompatible = domA.intersects(domB)
          else
            isCompatible = domB.intersects(domA)

          if(!isCompatible) {
            debugMsg("PlanDatabase:getCompatibleTokens",
	             "EXCLUDING (" , candidate.getKey , ")" , "VAR=" , varB.name ,
	             "(" , varB.getKey , ") " , "Cannot intersect " , domA.toString , " with " , 
                     domB.toString)
            break
          }

          debugMsg("PlanDatabase:getCompatibleTokens",
	           "VAR=" , varB.name , "(" , varB.getKey , ") " ,
	           "Can intersect " , domA.toString , " with " , domB.toString)
        }
      }

      // If it is still compatible, we may wish to do a double check on the
      // Temporal Variables, since we could get more pruning from the TemporalNetwork based on
      // temporal distance. This is because temporal propagation is insufficient to ensure that if 2 timepoints
      // have an intersection that they can actually co-exist. For example, if a < b, then there may well
      // be an intersection but t would be immediately inconsistent of they were required to be concurrent.
      if (isCompatible &&
          (!useExactTest || temporalAdvisor.canBeConcurrent(t, candidate))){
            results += candidate

            debugMsg("PlanDatabase:getCompatibleTokens",
                     "EXACT=" , useExactTest , ". Adding " , candidate.getKey ,
                     " for token " , t.getKey)

          }
    }
    return results.result
  }

  def countCompatibleTokens(t: Token, useExactTest: Boolean = true): Int = 
    getCompatibleTokens(t, useExactTest).length

  //this function isn't actually implemented in the C++
//  def lastCompatibleTokenCount(t: Token): Int

  def getTokensToOrder: Map[Int, (Token, MSet[Object])] = m_tokensToOrder
  def getOrderingChoices(t: Token): Vector[(Object, (Token, Token))] = { 
    val retval = new VectorBuilder[(Object, (Token, Token))]

    if(!constraintEngine.propagate)
      return retval.result

    checkError(m_tokensToOrder.contains(t.getKey),
               "Should not be calling this method if it is not a token in need of ordering. ", t)

    val objects = m_tokensToOrder(t.getKey)._2

    checkError(!objects.isEmpty,
               "There should be at least one source of induced constraint on the token." ,
               t)

    for(obj <- objects) { 
      val choices = obj.getOrderingChoices(t)
      for(choice <- choices) { 
        retval += ((obj, choice))
      }
    }
    return retval.result
  }
  def countOrderingChoices(t: Token): Int = { 
    if(!constraintEngine.propagate)
      return 0
    else
      return ObjectDomain.makeObjectList(t.getObject.lastDomain.getValues).
    foldLeft(0)(_ + _.countOrderingChoices(t))
  }

  def lastOrderingChoiceCount(t: Token): Int = { 
    if(!constraintEngine.propagate)
      return 0
    else
      return ObjectDomain.makeObjectList(t.getObject.lastDomain.getValues).
    foldLeft(0)(_ + _.lastOrderingChoiceCount(t))
  }
  def hasOrderingChoice(t: Token): Boolean = countOrderingChoices(t) > 0

  def getObjectsByType[T <: Object](typeName: LabelStr): List[T] = { 
    checkError(schema.isObjectType(typeName), ",", typeName, "' is not an object type in the plan database.");
    m_objectsByType.getOrElse(typeName.key, Set[T]()).map(_.asInstanceOf[T]).toList
  }
  
  def hasObjectInstances(typeName: LabelStr): Boolean = m_objectsByType.contains(typeName.key)
  def getObject(name: LabelStr): Option[Object] = m_objectsByName.get(name.key)

  def close: Unit = { 
    checkError(!isClosed, "Tried to close a closed database")
    m_state = CLOSED
  }
  def close(objectType: LabelStr): Unit = { }

  def isClosed: Boolean = m_state == CLOSED
  def isClosed(objectType: LabelStr): Boolean = true
  def isOpen: Boolean = m_state == OPEN
  def getState: State = m_state
  
  def isPurged: Boolean = m_state == PURGED

  def purge: Unit = { 
    checkError(m_state != PURGED, "Can't purge twice")
    if(!Entity.isPurging) { 
      m_tokens.filterNot(_.master.isDefined).foreach(_.discard)
      m_objects.filterNot(_.getParent.isDefined).foreach(_.discard)
    }
    else { 
      m_tokens.foreach(_.discard)
      m_objects.foreach(_.discard)
    }
    m_globalVariables.foreach(_.discard)
  }

  def makeObjectVariableFromType(objectType: LabelStr): ConstrainedVariable = { 
    return new Variable[ObjectDomain](getConstraintEngine, 
                                      ObjectDomain(m_objectsByType.get(objectType.key).get.toSet, 
                                                   ObjectDT.INSTANCE),
                                      false, true, "object", None)
  }

  def createObject(typeName: LabelStr, name: LabelStr, args: Vector[Domain]): Option[Object] = { 
    None
    //TODO: update this
    // schema.getObjectType(typeName, args) match {
    //   case Some(f) => f.createInstance(this, typeName, name, args)
    //   case None => checkError(schema.isObjectType(typeName), typeName, " is not an object type."); None
    // }
  }
  def createToken(tokenType: LabelStr, tokenName: LabelStr, isRejectable: Boolean = false,
                  isFact: Boolean = false): Option[Token] = { 
    debugMsg("PlanDatabase:createToken", tokenType.toString() , " " , tokenName.toString())
    schema.getTokenType(tokenType) match { 
      case Some(factory) => { 
        factory.createInstance(this,
                               tokenType, // Hack!, this is supposed to be the instance name, InterpretedTokenFactory::createInstance relies on this hack
                               isRejectable,
                               isFact) match { 
          case Some(token) => { 
            token.setName(tokenName)
            if (!token.isClosed)
              token.close
            registerGlobalToken(token)
            debugMsg("PlanDatabase:createToken","Created Token:" , tokenName , "\n" , token.toLongString)
            Some(token)
          }
          case None => { checkError(false, "Failed to create token."); None}
        }
      }
      case None => { checkError(false, tokenType, " isn't a recognized token type."); None}
    }
  }

  def createSlaveToken(master: Token, tokenType: LabelStr, relation: LabelStr): Option[Token] = { 
    getSchema.getTokenType(tokenType) match {     
      case Some(factory) => { 
        factory.createInstance(master, tokenType, relation) match { 
          case Some(token) => {  
            if (!token.isClosed)
              token.close;
        
            debugMsg("PlanDatabase:createSlaveToken",
                     "Created Slave Token:" , "\n" , token.toLongString);
            Some(token)
          }
          case None => { checkError(false, "Failed to create the token."); None}
        }
      }
      case None => { checkError(false, tokenType, " isn't a known token type."); None}
    }
  }

  def hasTokenTypes: Boolean = schema.hasTokenTypes

  override def toString: String = PlanDatabaseWriter.toString(this)

  def publish(message: (PlanDatabaseListener) => Unit): Unit = { 
    m_listeners.foreach(message(_))
  }

  //private from C++
  def notifyAdded(o: Object): Unit = { 
    checkError(!Entity.isPurging, "Should not be in this method if in purgeMode.")
    checkError(!isClosed(o.getType),
                "Cannot add object ", o.getName, " if type ", o.getType, " is already closed.")
    checkError(!m_objects.contains(o), "Object ", o.getName.toString , " already added.")
    checkError(!m_objectsByName.contains(o.getName.key),
               "Object with the name ", o.getName, " already added.");
    m_objects = m_objects + o

    // Cache by name
    m_objectsByName = m_objectsByName + ((o.getName.key, o))

    // Now cache by type
    var typeName = o.getType
    m_objectsByType.addBinding(typeName.key, o)
    while(schema.hasParent(typeName)){
      typeName = schema.getParent(typeName)
      m_objectsByType.addBinding(typeName.key, o)
    }

    publish(_.notifyAdded(o))

    debugMsg("PlanDatabase:notifyAdded:Object",
             o.getType, "::", o.getName, " (", o.key, ")")
  }

  def notifyRemoved(o: Object): Unit = { 
    checkError(!Entity.isPurging, "Shouldn't receive a removed message while purging");
    checkError(m_objects.contains(o), o, " doesn't belong to this plan database.");
    checkError(m_objectsByName.contains(o.name.key), "No object named ", o.name, " in this plan database.");

    // Clean up cached values
    m_objects = m_objects - o
    m_objectsByName = m_objectsByName - o.name.key
    for((key, set) <- m_objectsByPredicate; if(set.contains(o))) { m_objectsByPredicate.removeBinding(key, o)}
    for((key, set) <- m_objectsByType; if(set.contains(o))) { m_objectsByPredicate.removeBinding(key, o)}

    publish(_.notifyRemoved(o))

    debugMsg("PlanDatabase:notifyRemoved:Object",
             o.getType, "::", o.getName, " (", o.getKey, ")")

  }

  def notifyAdded(t: Token): Unit = { 
    checkError(!m_tokens.contains(t), "Already added ", t)
    m_tokens = m_tokens + t
    publish(_.notifyAdded(t))

    debugMsg("PlanDatabase:notifyAdded:Token",  t);
  }

  def notifyRemoved(t: Token): Unit = { 
    checkError(!Entity.isPurging, "Shouldn't get a removed message while purging.");
    checkError(m_tokens.contains(t), "Token ", t, " doesn't belong to this plan database")

    if(isGlobalToken(t.getName))
      unregisterGlobalToken(t)

    m_tokens = m_tokens - t
    m_tokensToOrder = m_tokensToOrder - t.key
    publish(_.notifyRemoved(t));

    debugMsg("PlanDatabase:notifyRemoved:Token",
             t.getPredicateName, " (", t.getKey, ")");

  }

  def notifyAdded(o: Object, t: Token): Unit = { 
    publish(_.notifyAdded(o, t));

    debugMsg("PlanDatabase:notifyAdded:Object:Token",
             t, " added to ", o);
  }

  def notifyRemoved(o: Object, t: Token): Unit = { 
    publish(_.notifyRemoved(o,t));
    debugMsg("PlanDatabase:notifyRemoved:Object:Token",
             t, " removed from ", o)
  }

  def notifyActivated(t: Token): Unit = { 
    // Need to insert this token in the activeToken index
    checkError(t.isActive, "Received notification that an inactive token is active: ", t)

    insertActiveToken(t)

    debugMsg("PlanDatabase:notifyActivated",
             t.getPredicateName  , "(" , t.getKey , "}")
    publish(_.notifyActivated(t))

  }

  def notifyDeactivated(t: Token): Unit = { 
    checkError(!Entity.isPurging, "Shouldn't receive a deactivation message while purgin.")

    removeActiveToken(t)

    publish(_.notifyDeactivated(t))

    debugMsg("PlanDatabase:notifyDeactivated",
             t.getPredicateName  , "(" , t.getKey , "}")
  }

  def notifyMerged(t: Token): Unit = { 
    publish(_.notifyMerged(t));

    debugMsg("PlanDatabase:notifyMerged",
             t.getPredicateName, "(", t.getKey, ")",
             " merged with (", t.getActiveToken.get.getKey, ")")
  }

  def notifySplit(t: Token): Unit = { 
    checkError(!Entity.isPurging, "Shouldn't get a split messsage while purging")
    publish(_.notifySplit(t))

    debugMsg("PlanDatabase:notifySplit",
             t.getPredicateName, "(", t.getKey, ")")

  }

  def notifyRejected(token: Token): Unit = { 
    publish(_.notifyRejected(token))

    debugMsg("PlanDatabase:notifyRejected",
             token.getPredicateName, "(", token.getKey, ")")
  }

  def notifyReinstated(token: Token): Unit = { 
    checkError(!Entity.isPurging, "Shouldn't get a reinstation message while purging")
    publish(_.notifyReinstated(token))

    debugMsg("PlanDatabase:notifyReinstated",
             token.getPredicateName, "(", token.getKey, ")")
  }

  def notifyConstrained(obj: Object, predecessor: Token, successor: Token): Unit = { 
    publish(_.notifyConstrained(obj, predecessor, successor))

    debugMsg("PlanDatabase:notifyConstrained",
             "(", predecessor.getKey, ") On Object ", obj.getType, "::", obj.getName, " (", 
             obj.getKey, ") Constrained Before Token (", successor.getKey, ")")
  }

  def notifyFreed(obj: Object, predecessor: Token, successor: Token): Unit = { 
    checkError(!Entity.isPurging, "Shouldn't get a free message while purging")
    publish(_.notifyFreed(obj, predecessor, successor))

    debugMsg("PlanDatabase:notifyFreed",
             "(", predecessor.getKey, ") On Object ",
             obj.getType, "::", obj.getName, " (", obj.getKey, ") Freed from Before Token (", successor.getKey, ")");
  }

  def notifyAdded(listener: PlanDatabaseListener): Unit = { 
    checkError(!m_listeners.contains(listener), "Tried to add a listener twice: ", listener)
    m_listeners = m_listeners :+ listener
  }

  def notifyRemoved(listener: PlanDatabaseListener): Unit = { 
    if(!m_deleted) {
      debugMsg("PlanDatabase:notifyRemoved:Listener",
	       "Not in PlanDatabase destructor, so erasing ", listener)
      m_listeners = m_listeners.filterNot(_ == listener)
    }
    else {
      debugMsg("PlanDatabase:notifyRemoved:Listener",
	       "In PlanDatabase destructor, so not erasing ", listener)
    }
  }

  /**
   * @brief If an object induces an ordering constraint on a token, it notfifies the plan database.
   * @param object The object on which the token should be ordered.
   * @param token The token to order
   */
  def notifyOrderingRequired(obj: Object, token: Token): Unit = { 
    debugMsg("PlanDatabase:notifyOrderingRequired",
	     obj.getName, "(", obj.getKey, ") from ", token)

    checkError(token.isActive, "Token must be active to induce an ordering:", token)

    m_tokensToOrder.get(token.getKey) match { 
      case None => m_tokensToOrder = m_tokensToOrder + ((token.getKey, (token, MSet(obj))))
      case Some((t, dest)) => dest += obj
    }
  }

  /**
   * @brief If an object's state has changed such that a previously required ordering is no longer required then it should
   * notify the plan database.
   */
  def notifyOrderingNoLongerRequired(obj: Object, token: Token): Unit = { 
    debugMsg("PlanDatabase:notifyOrderingNoLongerRequired",
	     obj.getName, "(", obj.getKey, ") from ", token.toString);

    m_tokensToOrder.get(token.getKey) match { 
      case None =>  checkError(false,
	                       "Expect there to be a stored entry. Must be a bug in synchronization. Failed to send initial message.")
      case Some((t, set)) => { 
        set -= obj
        if(set.isEmpty)
          m_tokensToOrder = m_tokensToOrder - t.key
      }
    }
  }

  /* Useful internal accessors and indexes for accessing objects and tokens in the PlanDatabase. */

  /**
   * @brief Retrieve all objects that can be assigned tokens of a given predicate.
   * @param predicate The predicate name to search by. It must be defined according to the schema.
   * @param results A collection, initially empty, which is an output parameter for results.
   * @see Schema::isPredicateDefined, Schema::canBeAssigned
   */
  def getObjectsByPredicate(predicate: LabelStr): List[Object] = { 
    checkError(schema.isPredicate(predicate), predicate, " isn't a predicate.")

    // First try a cache hit.
    m_objectsByPredicate.get(predicate.key) match { 
      case Some(set) => set.toList
      case None => { 
        m_objects.filter((x) => schema.canBeAssigned(x.getType, predicate)).toList
      }
    }
  }

  //gets a list of all possible parents for a predicate.  
  def getPredicateRoots(objectType: LabelStr, predicate: LabelStr): List[LabelStr] = { 
    val sl_objectRoot = LabelStr("Object")
    val sl_timelineRoot = LabelStr("Timeline")
    if(!getSchema.isPredicate(predicate) || objectType == sl_objectRoot ||
       objectType == sl_timelineRoot)
      List()
    else
      (objectType.toString + "." + predicate.toString) :: 
    getPredicateRoots(getSchema.getParent(objectType), predicate)
  }
  /**
   * @brief Utility to index an active token.
   */
  def insertActiveToken(token: Token): Unit = { 
    var objectType = token.getObject.baseDomain.dataType.name
    val predicateSuffix = token.getUnqualifiedPredicateName

    debugMsg("PlanDatabase:insertActiveToken", token)
    getPredicateRoots(objectType, predicateSuffix).foreach((predicate: LabelStr) => { 
      m_activeTokensByPredicate = 
        m_activeTokensByPredicate + ((predicate.key, 
                                      m_activeTokensByPredicate.getOrElse(predicate.key, Set()) +
                                      token))
      debugMsg("PlanDatabase:insertActiveToken", token, " added for ", predicate)
    });
  }

  /**
   * @brief Utility to remove an active token
   */
  def removeActiveToken(token: Token): Unit = { 
    var objectType = token.getObject.baseDomain.dataType.name
    val predicateSuffix = token.getUnqualifiedPredicateName

    debugMsg("PlanDatabase:insertActiveToken", token)
    getPredicateRoots(objectType, predicateSuffix).foreach((predicate: LabelStr) => { 
      m_activeTokensByPredicate = 
        m_activeTokensByPredicate + ((predicate.key, 
                                      m_activeTokensByPredicate.getOrElse(predicate.key, Set()) -
                                      token))
      debugMsg("PlanDatabase:insertActiveToken", token, " added for ", predicate)
    });
  }
  

  var m_temporalAdvisor: TemporalAdvisor = null
  val m_client: DbClient = new DbClient(this)
  var m_state: State = OPEN
  var m_tokens: Set[Token] = Set()
  var m_objects: Set[Object] = Set()
  var m_globalTokens: Set[Token] = Set()
  var m_globalVariables: Set[ConstrainedVariable] = Set()
  var m_deleted: Boolean = false
  var m_listeners: Vector[PlanDatabaseListener] = Vector()
  var m_objectsByName: Map[Int, Object] = Map()
  val m_objectsByPredicate: MultiMap[Int, Object] = new HashMap[Int, MSet[Object]] with MultiMap[Int, Object]
  val m_objectsByType: MultiMap[Int, Object] = new HashMap[Int, MSet[Object]] with MultiMap[Int, Object]
  var m_globalVarsByName: Map[Int, ConstrainedVariable] = Map()
  var m_globalTokensByName: Map[Int, Token] = Map()
  var m_tokensToOrder: Map[Int, (Token, MSet[Object])] = Map()
  var m_activeTokensByPredicate: Map[Int, Set[Token]] = Map()
  
}
