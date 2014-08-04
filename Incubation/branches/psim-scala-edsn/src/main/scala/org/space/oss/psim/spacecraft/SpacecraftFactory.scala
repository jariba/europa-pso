package org.space.oss.psim.spacecraft

import org.space.oss.psim.Spacecraft

trait SpacecraftFactory {
  def makeInstance(id:Int): Spacecraft
}

class DeafultSpacecraftFactory extends SpacecraftFactory {
  override def makeInstance(id:Int) = new SpacecraftBase("SC-"+id)
}