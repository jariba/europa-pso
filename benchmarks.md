### Manufacturing ###

The manufacturing domain was proposed by `[`1`]`. It is a scheduling problem. Consider a factory is producing a set of same products. A product has to go over the assembly line, so that a fixed number of attributes are assembled to the raw products. Consider the assembly line as a graph, where each vertice is a machine that can add certain attribute to a product. Each machine can be occupied by at most one product at every time step. Products have to be arranged and transported on this graph, so that certain product deadlines have to be met.

### Currirulum ###

A constraint satisfaction problem. See [http://www.cs.st-andrews.ac.uk/~ianm/CSPLib/prob/prob030/spec.html](here.md) for its original description. It is used to model the performance of a student. A student has to take a fixed set of courses in a certain number of time steps. In each time step, there is a maximum and minimum bound on the total number of courses, and the total number of credits. Also, courses have prerequisite dependencies. For example, Math101 has to be taken in an earlier time step than Math201. A plan will always obey all the constraints, and prerequisites.

### Trucks ###
It is an IPC5 domain. The original description is [here](http://zeus.ing.unibs.it/ipc-5/domain-descriptions/trucks.txt). Trucks is a logistics domain. The idea is to deliver packages into destination locations. Packages have to be transported by some shared resources (trucks), and certain packages have delivery deadlines. On top of that, constrains have to be satisfied in order to load a package onto a truck.

### Openstack ###
It is an IPC5 domain. The original description is [here](http://zeus.ing.unibs.it/ipc-5/domain-descriptions/openstacks.txt). It is a scheduling problem. The idea is to ship orders. One order may contain certain types of products, and is ready for shipping when all products are produced. A certain type of product is only allowed to be produced when all the orders that contain the product is in an open stack, where the size of the open stack needs to be kept minimal, in order to save open time. All products are produced sequentially, through one shared machine. The idea is to schdule the production of products.

### References ###
`[`1`]` The Manufacturing Plant Domain. J. Benton, M. B. Do, and W. Ruml