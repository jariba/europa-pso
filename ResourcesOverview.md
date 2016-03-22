

# Resources #

For the purposes of using Europa, resources are quantities which change over
time.  A resource can be a fixed quantity which may need to be replenished or a
limitless asset which has a usage limitation.  Europa refers to the amount of a
resource using levels.  Production increase the level of a resource while consumption reduces the level of a
resource.  A change to the level of a resource is referred to as a transaction.  When a transaction occurs,
the moment within the timeline is referred to as an instant.

## Purpose of Resources ##

Resources are important to plans to illustrated the economic considerations which
can cause a plan to fail.

## Reservoir ##

Reservoirs are resources which have fixed quantities that require production to consume beyond their intial
value.

### Source ###

A source represents a reservoir which can only be consumed from.

### Sink ###

A sink represents a reservoir which can only receive production.

## Reusable ##

Reusables are resources which do not require production and generally limited only by usage during a single instant.

### Unary ###

Unary reusable may only be accessed once per instance.

### Multi-Capacity ###

Multi-capacity reusables may be accessed more than once per instant.

## Limits ##

Limits are used to identify a given amount of a resource at any time within a
flexible plan.  Since plans are designed to be flexible, Europa uses the upper and lower bounds
of the possible amounts.  Limits are represented as constants which change only during transactions.

### Lower Limit ###

The lower limit of a resource refers to the absolute minimum a resource can be at any time.  For lower limits, reservoirs are
assumed to consume early and produce later on.  If I have a tank of gas, the lower limit representing the level
should be highest at the beggining of the plan compared to the end of the plan.

### Upper Limit ###

The upper limit of a resource refers to the absolute maximum a resource can be at any time.  For upper limits,
resources are assumed to consume late and produce early.  The converse of the above limit example applies to upper
limits.

<a href='Hidden comment: 

Proprogation occurs when a resource object notifies the planner there is a problem.  The planner is notified to
fix the problem by searching through the available values for a solution.


'></a>