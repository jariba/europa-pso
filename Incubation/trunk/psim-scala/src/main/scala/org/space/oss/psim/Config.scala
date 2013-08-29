package org.space.oss.psim

trait Config 
{
	def getValue(name: String): Option[String]
	
	def setBeanType(name:String, btype:String)
	def getBean(name: String): Any
}