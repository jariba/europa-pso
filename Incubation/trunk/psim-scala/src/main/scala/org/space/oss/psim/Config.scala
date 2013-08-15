package org.space.oss.psim

trait Config 
{
	def getValue(name: String): Option[String]
}