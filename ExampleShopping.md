The example NDDL implementation provided in the two `*`.nddl files [here](http://code.google.com/p/europa-pso/source/browse/PLASMA/trunk/examples/Shopping) is lightly commented and should be self-explanatory.

The Shopping example requires an agent to start at home, buy milk, bananas, and a drill, and finally to finish back at home.

Run the example:

```
% cd $EUROPA_HOME/examples/Shopping
% ant
```
The following figure shows the resulting EUROPA plan (the table is produced by the code in [Shopping.bsh](http://code.google.com/p/europa-pso/source/browse/PLASMA/trunk/examples/Shopping/Shopping.bsh)):

![http://europa-pso.googlecode.com/svn/wiki/images/Examples-Shopping0.jpg](http://europa-pso.googlecode.com/svn/wiki/images/Examples-Shopping0.jpg)

Notice that while EUROPA specifies ranges of times for each event that will satisfy all constraints (for example, buying the drill could start anywhere between timepoint 2 and timepoint 77 inclusive), it has decided that the visit to the hardware store will precede the visit to the supermarket, and has correctly discovered that both milk and bananas can be purchased during the same visit to the supermarket.