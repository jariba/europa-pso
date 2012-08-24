package gov.nasa.arc.europa.utils;

abstract class Module(s: String) {
  val name = s;
  
  def initialize: Unit;
  def uninitialize: Unit;
  def initialize(engine: Engine): Unit;
  def uninitialize(engine: Engine): Unit;
}
