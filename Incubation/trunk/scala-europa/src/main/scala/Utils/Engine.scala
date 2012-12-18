package gov.nasa.arc.europa.utils;

import scala.collection.mutable.Map;
import scala.collection.mutable.HashMap;
import scala.io.Source

trait EngineComponent {
  protected var engine: Engine = null;
  def setEngine(e: Engine) = engine = e;
  def getEngine = engine
  def delete: Unit;
}

trait LanguageInterpreter extends EngineComponent { 
  def interpret(input: Source, source: String): String
  
}


class EngineConfig {
  protected val properties: Map[String, String] = new HashMap[String, String]
  def getProperty(name: String) = properties.get(name) match {
    case None => ""
    case Some(value: String) => value
  }
  def setProperty(name: String, value: String) = properties += (name -> value)
  /** TODO
   * def readFromXML(file: String, isFile: Boolean): Int
   * def writeFromXML(file: String)
   * def parseXML(parent: scala.xml.Node)
   */
}

abstract class Engine {
  def addComponent(name: String, comp: EngineComponent): Unit
  def removeComponent(name: String): EngineComponent
  def getComponent(name: String): EngineComponent
  def getComponents: scala.collection.immutable.Map[Int, EngineComponent];
  def getConfig: EngineConfig;
  def addLanguageInterpreter(language: String, interpreter: LanguageInterpreter): LanguageInterpreter = { addComponent(language, interpreter); interpreter}
  def getLanguageInterpreter(language: String) = getComponent(language).asInstanceOf[LanguageInterpreter]
  def removeLanguageInterpreter(language: String) = { 
    val retval = getLanguageInterpreter(language)
    removeComponent(language)
    retval
  }
}

class EngineBase extends Engine {
  protected val config = new EngineConfig;
  protected val components: Map[Int, EngineComponent] = new HashMap[Int, EngineComponent];
  protected var modules: List[Module] = List.empty
  private var started = false;
  
  def doStart: Unit = {
    if(!started) {
      initializeModules;
      initializeByModules;
      started = true;
    }
  }
  def doShutdown: Unit = {
    if(started) {
      Entity.purgeStart
      uninitializeByModules;
      uninitializeModules;
      Entity.purgeEnd
      started = false;
    }
  }

  def isStarted: Boolean = started
  
  def addModule(m: Module): Unit = {
    modules = (m :: modules).reverse
    if(started) {
      initializeModule(m)
      initializeByModule(m)
    }
  }
  //def loadModule(fileName: String) = {}
  def removeModule(m: Module) = {
    if(modules.exists(x => x == m)) {
      uninitializeByModule(m);
      uninitializeModule(m);
    }
    modules = modules.filterNot(x => x == m);
  }

  def getModule(moduleName: String) = {modules.filter(m => m.name == moduleName).head }

  override def addComponent(name: String, component: EngineComponent): Unit = {
    val key = LabelStr.getKey(name);
    components.get(key) match {
    case None => {components += (key -> component)}
    case Some(c: EngineComponent) => {
      c.delete
      components.update(key, component)
    }
    }
  }
  override def removeComponent(name: String): EngineComponent = {
    val retval = getComponent(name)
    val key = LabelStr.getKey(name)
    components.get(key) match {
    case Some(c: EngineComponent) => {components -= key; c.setEngine(null)}
    case None => {}
    }
    retval
  }
  override def getComponent(name: String) = components.getOrElse(LabelStr.getKey(name), null)
  override def getComponents: scala.collection.immutable.Map[Int, EngineComponent] = components.toMap

  override def getConfig = config

  protected def initializeModules = modules.foreach(m => m.initialize)
  protected def initializeModule(m: Module) = m.initialize
  protected def uninitializeModules = modules.foreach(m => m.uninitialize)
  protected def uninitializeModule(m: Module) = m.uninitialize

  protected def initializeByModules = modules.foreach(m => initializeByModule(m))
  protected def initializeByModule(m: Module) = m.initialize(this)
  protected def uninitializeByModules = modules.reverse.foreach(m => uninitializeByModule(m))
  protected def uninitializeByModule(m: Module) = m.uninitialize(this)
}
