package org.space.oss.psim

import org.space.oss.psim.ui.PSimDesktop

class PSimServer
{
  var psim: PSim = null

  def init(cfg:Config) {
    psim = new PSimImpl
    psim.init(cfg)
    //println(psim.toString)    
  }
}

object PSimServer 
{
  var instance:PSimServer = _
  
  def main(args: Array[String]) {
    val cfg = new ConfigByMap
    instance = new PSimServer
    instance.init(cfg)
    val desktop = new PSimDesktop(instance,cfg)
    desktop.run()
    //runSimulation()
  }
  
  def runSimulation() {
    val mgr = new PSimEventManager(instance.psim)
    mgr.init()
    mgr ! Start
    mgr.start()    
  }
}