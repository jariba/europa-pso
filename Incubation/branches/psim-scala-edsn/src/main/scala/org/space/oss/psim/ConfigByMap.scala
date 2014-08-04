package org.space.oss.psim

import scala.collection.mutable.HashMap

class ConfigByMap extends Config {
	val values = new HashMap[String,String]()
	val beanTypes = new HashMap[String,String]()
	
	override def getValue(name: String): Option[String] = values.get(name)
	
	override def setBeanType(name:String, btype:String) { beanTypes += (name -> btype)}
	
	override def getBean(name:String): Any = {
	  beanTypes.get(name) match {
	    case Some(typeName) => Class.forName(typeName).newInstance
	    case None => null
	  }
	}
}