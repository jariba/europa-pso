package gov.nasa.arc.europa.constraintengine
import gov.nasa.arc.europa.constraintengine.component.AbsoluteValueType
import gov.nasa.arc.europa.constraintengine.component.AddEqualType
import gov.nasa.arc.europa.constraintengine.component.BoolDT
import gov.nasa.arc.europa.constraintengine.component.DefaultPropagator
import gov.nasa.arc.europa.constraintengine.component.EqualConstraintType
import gov.nasa.arc.europa.constraintengine.component.FloatDT
import gov.nasa.arc.europa.constraintengine.component.IntDT
import gov.nasa.arc.europa.constraintengine.component.LessThanEqualConstraintType
import gov.nasa.arc.europa.constraintengine.component.NotEqualConstraintType
import gov.nasa.arc.europa.constraintengine.component.VoidDT
import gov.nasa.arc.europa.utils.Engine
import gov.nasa.arc.europa.utils.Module

class ModuleConstraintEngine extends Module("ConstraintEngine") { 
  override def initialize: Unit = { }
  override def uninitialize: Unit = { }
  override def initialize(engine: Engine): Unit = { 
    val ces = new CESchema
    ces.registerDataType(new VoidDT)
    ces.registerDataType(new BoolDT)
    ces.registerDataType(new IntDT)
    ces.registerDataType(new FloatDT)
    engine.addComponent("CESchema", ces)
    
    // ces.registerDataType(new StringDT)
    // ces.registerDataType(new SymbolDT)
    val ce = new ConstraintEngine(ces)
    new DefaultPropagator("Default", ce)
    engine.addComponent("ConstraintEngine", ce)
  }
  override def uninitialize(engine:Engine): Unit = {
    engine.removeComponent("ConstraintEngine").delete
    engine.removeComponent("CESchema").delete
  }

}

class ModuleConstraintLibrary extends Module("ConstraintLibrary") { 
  override def initialize: Unit = { }
  override def uninitialize: Unit = { }
  override def initialize(engine: Engine): Unit = { 
    val ces = engine.getComponent("CESchema").asInstanceOf[CESchema]
    ces.registerConstraintType(new AbsoluteValueType("absVal", "Default", true))
    ces.registerConstraintType(new AddEqualType("addEQ", "Default", true))
    ces.registerConstraintType(new EqualConstraintType("eq", "Default", true))
    ces.registerConstraintType(new EqualConstraintType("Equal", "Default", true))
    ces.registerConstraintType(new LessThanEqualConstraintType("leq", "Default", true))
    ces.registerConstraintType(new NotEqualConstraintType("neq", "Default", true))
  }
  override def uninitialize(engine:Engine): Unit = {
    engine.getComponent("CESchema").asInstanceOf[CESchema].purgeConstraintTypes
  }
}
