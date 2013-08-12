package org.space.oss.psim.spacecraft

import org.space.oss.psim.Spacecraft

class SpacecraftBase(id:String) extends Spacecraft 
{
	 def getID = id
	 override def toString(): String = id
}