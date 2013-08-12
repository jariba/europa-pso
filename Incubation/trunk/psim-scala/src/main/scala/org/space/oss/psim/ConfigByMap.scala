package org.space.oss.psim

import scala.collection.mutable.HashMap

class ConfigByMap extends Config {
	var values = new HashMap[String,String]()
	
	override def getValue(name: String) = values.getOrElse(name,"")
}