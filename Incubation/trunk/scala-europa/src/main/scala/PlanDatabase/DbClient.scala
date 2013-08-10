package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.CESchema
import gov.nasa.arc.europa.constraintengine.ConstrainedVariable
import gov.nasa.arc.europa.constraintengine.Constraint
import gov.nasa.arc.europa.constraintengine.Domain
import gov.nasa.arc.europa.constraintengine.Scope
import gov.nasa.arc.europa.utils.Debug._
import gov.nasa.arc.europa.utils.Entity
import gov.nasa.arc.europa.utils.Error._
import gov.nasa.arc.europa.utils.LabelStr

class DbClient(private val planDb: PlanDatabase) { 
  private def publish(message: (DbClientListener) => Unit): Unit = { 
    listeners.foreach(message(_))
  }
  /**
   * @brief Create a variable
   * @param type The type for the variable.
   * @param baseDomain The base domain of the new variable.
   * @param name The name for the variable. Must be unique.
   * @return The Id of the variable created. Will error out rather than return a noId.
   */
  def createVariable(typeName: String, baseDomain: Domain, name: String, 
                     isTmpVar: Boolean = false, canBeSpecified: Boolean = true): Option[ConstrainedVariable] = { 
    val optVariable = 
      planDb.getConstraintEngine.createVariableWithDomain(typeName, baseDomain, isTmpVar,
                                                          canBeSpecified, name)
    optVariable match { 
      case Some(v) =>
        //TODO: see if this is the right thing.  Seems like I got rid of makeObjectVariable FromType
        // if(planDb.schema.isObjectType(typeName) && v.isClosed)
        //   planDb.makeObjectVariableFromType(typeName, v)

      if (!isTmpVar)
        // Register as a global variable
        planDb.registerGlobalVariable(v)
      
      publish(_.notifyVariableCreated(v))
      case None =>
    }

    return optVariable;
  }

  /**
   * @brief Create a variable
   * @param type The type for the variable.
   * @param name The name for the variable. Must be unique.
   * @return The Id of the variable created. Will error out rather than return a noId.
   */
  def createVariable(typeName: String, name: String, isTmpVar: Boolean): Option[ConstrainedVariable] = { 
    val variable = 
      planDb.getConstraintEngine.createVariableByType(typeName, isTmpVar, true, name);
    variable match { 
      case Some(v) =>
        //TODO: see if this is the right thing.  Seems like I got rid of makeObjectVariable FromType
        // if (planDb.getSchema.isObjectType(typeName))
        //   planDb.makeObjectVariableFromType(typeName, v);

      // TODO: register TmpVariables so that they can be cleaned up easily
      if (!isTmpVar)
        // Register as a global variable
        planDb.registerGlobalVariable(v);
      
      publish(_.notifyVariableCreated(v));
      case None =>
    }
    return variable;
  }

  /**
   * @brief Delete a variable.  By way of symmetry with createVariable.
   */
  def deleteVariable(v: ConstrainedVariable): Unit = { 
    if(isGlobalVariable(v.name))
      planDb.unregisterGlobalVariable(v);
    publish(_.notifyVariableDeleted(v));
    v.discard
  }

  /**
   * @brief Create an object instance in the dabatase.
   * @param key The expected key value for the object. This is used as a check to ensure we are creating values
   * in the order we expect.
   * @param type The type of instance to create. Must match a name in the Schema. The daatabase must be open for
   * creation of instances of this type.
   * @param name The name for the instance. Must be unique.
   * @return The  of the object created. Will error out rather than return a no.
   */
  def createObject(typeName: String, name: String): Object = { 
    planDb.createObject(typeName, name, Vector()) match {
      case Some(obj) => {
        debugMsg("DbClient:createObject", obj.toLongString);
        publish(_.notifyObjectCreated(obj));
        return obj
      }
      case None => {
        checkError(ALWAYS_FAIL, "Failed to create object ", name, " of type, ", typeName);
        return null;
      }
    }
  }

  /**
   * @brief Create an object instance in the dabatase, with a call to a specialized constructor
   * @param key The expected key value for the object. This is used as a check to ensure we are creating values
   * in the order we expect.
   * @param type The type of instance to create. Must match a name in the Schema. The database must be open for
   * creation of instances of this type.
   * @param name The name for the instance. Must be unique.
   * @param arguments A vector of name/value pairs used to invoke a particular constructor.
   * @return The Id of the object created. Will error out rather than return a no.
   */
  def createObject(typeName: String, name: String, arguments: Vector[Domain]): Object = { 
    planDb.createObject(typeName, name, arguments) match {
      case Some(obj) => {
        debugMsg("DbClient:createObject", obj.toLongString);
        publish(_.notifyObjectCreated(obj, arguments));
        return obj;
      }
      case None => {
        checkError(ALWAYS_FAIL,
          "Failed to create ", name, " with ", typeName, "(", arguments, ")");
        return null;
      }
    }
  }

  /**
   * @brief Delete an object.  By way of symmetry with createObject.
   */
  def deleteObject(obj: Object): Unit = { 
    publish(_.notifyObjectDeleted(obj));
    obj.discard
  }

  /**
   * @brief Close the database. This will prohibit any further insertion of objects.
   * @see close(objectType: String)
   */
  def close: Unit = { 
    planDb.close
    debugMsg("DbClient:close", "CLOSE ALL TYPES");
    publish(_.notifyClosed);
  }

  /**
   * @brief Close the database for further creation of any objects of a given type. Not supported yet. There is no
   * implementation for this yet, since we are not really supporting incremental closure of objects at this time.
   */
  def close(objectType: String): Unit = { 
    debugMsg("DbClient:close", "Closing: ", objectType)
    planDb.close(objectType)
    debugMsg("DbClient:close", "Closed: ", objectType)
    publish(_.notifyClosed(objectType));
  }

  private def allocateToken(tokenType: String, tokenName: String, isRejectable: Boolean,
                          isFact: Boolean): Token = { 
    checkError(supportsAutomaticAllocation, "Cannot allocate tokens from the schema.");
    planDb.createToken(tokenType, tokenName, isRejectable, isFact) match { 
      case Some(token) => { 
        if (isTransactionLoggingEnabled) {
          debugMsg("DbClient:allocateToken",
	           "Saving token key ", token.key)
          keysOfTokensCreated = token.key :: keysOfTokensCreated
        }
        return token
      }
      case None => { 
        checkError(false, "Failed to allocate token for ", tokenType)
        return null
      }
    }
  }
  /**
   * @brief Constructs a Token instance.
   * @param predicateName The name of the predicate for which this token is an instance. Must match a name in the
   * schema.
   * @return The Id of the token created. Will error out rather than return a no.
   */
  def createToken(tokenType: String,
                  tokenName: String = "",
                  rejectable: Boolean = false,
                  isFact: Boolean = false): Token = { 
    val token = allocateToken(tokenType, tokenName, rejectable, isFact);
    debugMsg("DbClient:createToken", token.toString);
    publish(_.notifyTokenCreated(token));
    return(token);
  }

  /**
   * @brief Deletes a token instance.  By way of symmetry with createToken.
   */
  def deleteToken(token: Token, name: String = ""): Unit = { 
    checkError(token.isInactive || token.isFact,
	       "Attempted to delete active, non-fact token ", token.toLongString);
    if(planDb.isGlobalToken(token.getName))
      planDb.unregisterGlobalToken(token)
    publish(_.notifyTokenDeleted(token, name))

    //the keys are only recorded if logging is enabled
    //this may not be the right thing...
    if(!keysOfTokensCreated.isEmpty) {
      if(keysOfTokensCreated.head == token.getKey) {
	debugMsg("DbClient:deleteToken",
		 "Removing token key ", keysOfTokensCreated.head);
        keysOfTokensCreated = keysOfTokensCreated.tail
      }
      checkError(!keysOfTokensCreated.contains(token.key),
		 "Attempted to delete ", token, " out of order.");
    }
    token.discard
  }

  /**
   * @brief imposes a constraint such that token comes before successor, on the given object.
   * @param object
   * @param predecessor The token to be the predecessor
   * @param successor The token to be the successor. If 0, the Token is constrained to succeed all
   * other ordered tokens.
   * @return The resulting 'precedes' constraint
   */
  def constrain(obj: Object, predecessor: Token, successor: Token): Unit = { 
    obj.constrain(predecessor, successor);
    debugMsg("DbClient:constrain", predecessor, " before ", successor);
    publish(_.notifyConstrained(obj, predecessor, successor));
  }

  /**
   * @brief Frees any constraints imposed on a Token arising from calls to constrain.
   * @param object The object to which the token has been constrained.
   * @param predecessor The token that is the predecessor
   * @param successor The token that is the successor.
   * @param constraint The constraint to be removed.
   */
  def free(obj: Object, predecessor: Token, successor: Token): Unit = { 
    obj.free(predecessor, successor);
    debugMsg("DbClient:free", predecessor, " before ", successor);
    publish(_.notifyFreed(obj, predecessor, successor));
  }

  /**
   * @brief Activate the given token
   * @param token The token to be activated. It must be inactive.
   */
  def activate(token: Token): Unit = { 
    token.activate
    debugMsg("DbClient:activate", token);
    publish(_.notifyActivated(token));
  }

  /**
   * @brief Merge the given token
   * @param token The token to be merged. It must be inactive.
   * @param activeTokenKey The token to be merged onto.
   */
  def merge(token: Token, activeToken: Token): Unit = { 
    token.doMerge(activeToken);
    debugMsg("DbClient:merge", token, " onto ", activeToken);
    publish(_.notifyMerged(token, activeToken));
  }

  /**
   * @brief Reject the given token
   * @param token The token to be rejected. It must be inactive.
   */
  def reject(token: Token): Unit = { 
    token.reject
    debugMsg("DbClient:reject", token);
    publish(_.notifyRejected(token));
  }

  /**
   * @brief Cancel restriction to Token Variables state through activate, merge, or reject
   * @param token The target token
   */
  def cancel(token: Token): Unit = { 

    publish(_.notifyCancelled(token))

    if(token.isMerged) { 
      val activeToken = token.getActiveToken.get

      checkError(activeToken.isValid, activeToken);

      if(activeToken.refCount == 1){
        // Expect that the last key created and stored will be this key
        checkError(!isTransactionLoggingEnabled ||
		   keysOfTokensCreated.head == activeToken.getKey,
		   "If transaction logging enabled then we require chronological retraction. ",
                   activeToken);
        if(isTransactionLoggingEnabled) {
	  debugMsg("DbClient:cancel",
		     "Removing token key ", keysOfTokensCreated.head);
	  keysOfTokensCreated = keysOfTokensCreated.tail
        }
      }
    }
    token.cancel
    debugMsg("DbClient:cancel", token);
  }

  /**
   * @brief The initial state may include constraints, even if the planner does not express any decisions
   * as constraints. Must be at least a binary constraint.
   * @param name The name of the constraint to be created
   * @param scope The variables to provide the scope of the constraint.
   */
  def createConstraint(name: String,
                       scope: Vector[ConstrainedVariable],
		       violationExpl: String = null): Constraint = { 
    val constraint = planDb.getConstraintEngine.createConstraint(name,scope,violationExpl);
    debugMsg("DbClient:createConstraint", constraint);
    publish(_.notifyConstraintCreated(constraint));
    return constraint;
  }

  /**
   * @brief Construction of a unary constraint.
   * @param name The name of the constraint to be created
   * param var the target variable.
   * @param domain The domain to restrict against.
   * note: this method is unimplemented in C++
   */
  // def createConstraint(name: String,
  //       	       variable: ConstrainedVariable,
  //       	       domain: Domain,
  //                      violationExpl: String = ""): Constraint = { 
  //   val constraint = planDb.getConstraintEngine.createConstraint(name,Scope(variable),
  //                                                                violationExpl)
  //   debugMsg("DbClient:createConstraint", constraint)
  //   publish(_.notifyConstraintCreated(constraint))
  //   return constraint;
  // }

  /**
   * @brief Delete a constraint.  By way of symmetry with createConstraint.
   */
  def deleteConstraint(constr: Constraint): Unit = { 
    publish(_.notifyConstraintDeleted(constr))
    planDb.getConstraintEngine.deleteConstraint(constr)
  }

  /**
   * @brief Restricts the base domain of a variable
   * @param variable The variable to be restricted
   * @param value The new base domain of the variable.
   * @see getEntityByKey
   */
  // def restrict(variable: ConstrainedVariable, domain: Domain): Unit

  /**
   * @brief Binds the value of a variable
   * @param variable The variable to be bound
   * @param value The value of the variable. All variable values can be cast to a double. If the variable
   * is an object variable, care must be taken to translate the object key to the id and from there obtain the
   * value to specify.
   * @see getEntityByKey
   */
  def specify(variable: ConstrainedVariable, value: Double): Unit = { 
    debugMsg("DbClient:specify", "before:",  variable.toLongString, " to ",
             variable.toString(value));
    variable.specify(value);
    debugMsg("DbClient:specify", "after:", variable.toLongString);
    publish(_.notifyVariableSpecified(variable));
  }

  /**
   * @brief Close the domains of a dynamic variable.
   * @param variable The dynamic variable to be closed.=
   */
  // def close(variable: ConstrainedVariable): Unit

  /**
   * @brief resets the specified domain of the target variable to its base domain
   * @param variable The variable to be reset
   */
  def reset(variable: ConstrainedVariable): Unit = { 
    debugMsg("DbClient:reset","before:", variable.toLongString);
    variable.reset;
    debugMsg("DbClient:reset", "after:", variable.toLongString);
    publish(_.notifyVariableReset(variable));
  }

  /*!< Support for interaction with ConsistencyManagement */

  /**
   * @brief Force propagation of any pending changes in the system to check consistency.
   * @return true if not proven inconsistent and all data propagated.. Otherwise false.
   * @see ConstraintEngine::propagate
   */
  def propagate: Boolean = { 
    planDb.getConstraintEngine.propagate
    return planDb.getConstraintEngine.constraintConsistent
  }

  private def getOrError[T](o: Option[T], message: String): T = o match { 
    case Some(o) => o
    case None => checkError(false, message); null.asInstanceOf[T]
  }

  /**
   * @brief Lookup an object by name. It is an error if the object is not present.
   * @return The requested object
   */
  def getObject(name: String): Object = { 
    getOrError(planDb.getObject(name), "No object named '" + name + "'")
  }

  /**
   * @brief Lookup a global variable by name. It is an error if not present
   * @retrun The requested variable.
   */
  def getGlobalVariable(varName: LabelStr): ConstrainedVariable = { 
    planDb.getGlobalVariable(varName) match { 
      case Some(v) => v
      case None => checkError(false, "No object named ", varName); null
    }
  }

  /**
   * @brief Test if a global exists for a given name
   * @return true if present, otherwise false
   */
  def isGlobalVariable(varName: LabelStr) : Boolean = planDb.isGlobalVariable(varName)

  /**
   * @brief Lookup a global token by name. It is an error if not present
   * @retrun The requested token.
   */
  def getGlobalToken(name: LabelStr) : Token = { 
    getOrError(planDb.getGlobalToken(name), "No token named " + name)
  }

  /**
   * @brief Test if a global exists for a given name
   * @return true if present, otherwise false
   */
  def isGlobalToken(name: LabelStr) : Boolean = planDb.isGlobalToken(name)

  /**
   * @brief Retrieve token defined by a particular path from a root token. Transaction Logging must be enabled.
   * For example: [1265,2,3,1,0,9] will return (Token 1265).slave(2).slave(3).slave(1).slave(0).slave(9)
   * @param relativePath The relative path to find the target token where each vector position reflects the position in the ordered
   * set of slaves for the parent. The first location in the path is a token we expect to find directly by key.
   * @return The token given by the path. If no token found, return TokenId::no
   * @see Token::getChild(int slavePosition)
   * @see enableTransactionLogging
   */
  def getTokenByPath(relativePath: Vector[Int]) : Option[Token] = { 
    checkError(isTransactionLoggingEnabled, 
               "Can't get a token by it's path without logging transactions");
    checkError(!relativePath.isEmpty, "Can't get a token by an empty path");
    checkError(!keysOfTokensCreated.isEmpty, 
               "Can't get a token by a path without having created tokens");
    checkError(relativePath(0) >= 0, 
               "Can't have a path starting with a zero or negative initial key.");

    // Quick check for the root of the path
    // Cannot be a path for a token with this key set
    if(relativePath(0) >= keysOfTokensCreated.size)
      return None

    // Obtain the root token key using the first element in the path to index the tokenKeys.
    val rootTokenKey = keysOfTokensCreated(relativePath(0));

    // Now source the token as an enityt lookup by key. This works because we have a shared pool
    // of entities per process
    //figure out pattern matching on vectors
    // def recurseHelper(t: Token, path: Vector[Int]): Token = path match { 
    //   case i :: is => recurseHelper(t.getSlave(i), is)
    //   case _ => t
    // }

    Entity.getTypedEntity(rootTokenKey) match { 
      case None => None
      case Some(rootToken) => { 
        return None //recurseHelper(rootToken, relativePath.tail)
      }
    }
  }

  /**
   * @brief Retrieve the relative path for obtaining the target token from a given root token.
   * Transaction Logging must be enabled.
   * @param targetToken The token to find a path to from its root in the ancestor tree
   * @return The vector giving its relative path. The first element is the root token key. Subsequent elements represent slave positions.
   * @see getTokenByPath
   * @see enableTransactionLogging
   */
  def getPathByToken(targetToken: Token): Vector[Int] = { 
    checkError(isTransactionLoggingEnabled, 
               "Can't do anything with token paths with logging disabled.");

    def recurseHelper(t: Token, v: Vector[Int]): Vector[Int] = t.master match { 
      case Some(master) => { 
        recurseHelper(master, v :+ master.getSlavePosition(t))
      }
      case None => { 
        v :+ keysOfTokensCreated.indexOf(t.key)
      }
    }
    return recurseHelper(targetToken, Vector()).reverse
  }

  /**
   * @brief Retrieve the relative path for obtaining the target token from a given root token.
   * Transaction Logging must be enabled.
   * @param targetToken The token to find a path to from its root in the ancestor tree
   * @return A string of the vector giving its relative path. The first element is the root token key
   * @see getPathByToken
   * @see enableTransactionLogging
   */
  def getPathAsString(targetToken: Token): String = getPathByToken(targetToken).mkString(".")

  /**
   * @brief Retrieve a constrained variable of any type based on its 'index'
   */
  //possibly not implementing this
  // def getVariableByIndex(index: Int): ConstrainedVariable = planDb.getConstraintEngine.getVariable(index)

  /**
   * @brief Retrieve an index for a variable. Required for logging. Tricks will have to be done to make
   * this fast!
   */
  //possibly not implementing this
  // def getIndexByVariable(variable: ConstrainedVariable): Int

  // def getConstraintByIndex(index: Int): Constraint

  // def getIndexByConstraint(constr: Constraint): Int

  /**
   * @brief Adds a listener to operations invoked on the client
   */
  def notifyAdded(listener: DbClientListener): Unit = listeners = listener :: listeners

  /**
   * @brief Removes a listener
   */
  def notifyRemoved(listener: DbClientListener): Unit = listeners = listeners.filterNot(_ == listener)


  /**
   * @brief Check for consistency
   */
  def constraintConsistent : Boolean = planDb.getConstraintEngine.constraintConsistent


  /**
   * @brief Used to determine if system has the necessary components in place to support
   * automatic token allocation in the Plan Database.
   */
  def supportsAutomaticAllocation : Boolean = planDb.hasTokenTypes

  /**
   * @brief Enables transaction logging. This is necessary if you wisk to replay or log transactions.
   * Must be disabled.
   */
  def enableTransactionLogging : Unit = transactionLoggingEnabled = true

  /**
   * @brief Disables transaction logging. Must be enabled.
   */
  def disableTransactionLogging: Unit = transactionLoggingEnabled = false

  /**
   * @brief Test of Transaction logging is enabled.
   * @see enableTransactionLoggng
   */
  def isTransactionLoggingEnabled: Boolean = transactionLoggingEnabled

  /**
   * @brief Create a value for a string
   */
  def createValue(typeName: String, value: String): Option[Double] = planDb.getConstraintEngine.createValue(typeName, value)
  
  // Temporarily exposing these to remove singletons, need to review DbClient concept in general
  def getCESchema: CESchema = planDb.getConstraintEngine.getSchema
  def getSchema: Schema = planDb.getSchema

  var listeners: List[DbClientListener] = List()
  var keysOfTokensCreated: List[Int] = List()
  var transactionLoggingEnabled: Boolean = false
}
