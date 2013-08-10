package gov.nasa.arc.europa.plandb

import gov.nasa.arc.europa.constraintengine.Variable
import gov.nasa.arc.europa.constraintengine.component.IntervalIntDomain

object plandb { 
  type TimeVar = Variable[IntervalIntDomain]
  type ObjectVar = Variable[ObjectDomain]
}
