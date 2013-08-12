package org.space.oss.psim

object PSimServer 
{
  var psim: PSim = null
  
  def main(args: Array[String]) {
    val cfg = new ConfigByMap
    psim = new PSimImpl
    psim.init(cfg)
    println(psim.toString)
  }
}