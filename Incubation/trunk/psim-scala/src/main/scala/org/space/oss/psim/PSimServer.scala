package org.space.oss.psim

import org.space.oss.psim.ui.PSimDesktop

class PSimServer
{
  var psim: PSim = _
  var eventMgr:PSimEventManager = _

  def init(cfg:Config) {
    psim = new PSimImpl
    psim.init(cfg)
    //println(psim.toString)    
    eventMgr = new PSimEventManager(psim)
    eventMgr.init()
    eventMgr.start()    
  }
  
  def runSimulation(maxTime:Int) {
    eventMgr.maxTime = maxTime
    eventMgr ! Start
  }
  
  def stopSimulation() {
    eventMgr ! Stop
  }
}

object PSimServer 
{
  var instance:PSimServer = _
  
  def main(args: Array[String]) {
    val cfg = new ConfigByMap
    cfg.setBeanType("spacecraftFactory","org.space.oss.psim.spacecraft.DefaultSpacecraftFactory")
    instance = new PSimServer
    instance.init(cfg)
    val desktop = new PSimDesktop(instance,cfg)
    desktop.run()
  }  
}