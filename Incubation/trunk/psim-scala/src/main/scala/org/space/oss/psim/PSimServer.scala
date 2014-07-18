package org.space.oss.psim

import org.space.oss.psim.ui.PSimDesktop

class PSimServer
{
  var psim: PSim = _
  var eventMgr:PSimEventManager = _

  // To make it accessible from BeanShell
  def getEventMgr = eventMgr
  
  def init(cfg:Config, numCraft: Int) {
    psim = new PSimImpl
    psim.init(cfg, numCraft)
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
    val numCraft = args(0).toInt
    val cfg = new ConfigByMap
    cfg.setBeanType("spacecraftFactory","org.space.oss.psim.spacecraft.DefaultSpacecraftFactory")
    instance = new PSimServer
    instance.init(cfg, numCraft)
    val desktop = new PSimDesktop(instance,cfg)
    desktop.run()
  }  
}