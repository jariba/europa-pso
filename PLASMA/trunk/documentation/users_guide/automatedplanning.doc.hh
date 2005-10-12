/**
 * @page automatedplanning Automated Planning
 *
 * @section planning Planning and Scheduling
 *
 * Planning, for our purposes, can be thought of as determining all the small tasks that must be carried out in order to accomplish a goal.
 * Let's say your goal is to buy a gallon of milk. It may sound like a simple task, but if you break it down, there are many small tasks 
 * involved: obtain keys, obtain wallet, start car, drive to store, find and obtain milk, purchase milk, etc. Planning also takes into account
 * rules, called constraints, which control when certain tasks can or cannot happen.  Two of the many constraints in this example are, you must
 * obtain your keys and wallet before driving to the store and you must obtain the milk before purchasing it. 
 *
 * Here is what a simple plan for buying milk at the store might look like:
 * @image html timelinePurchaseMilk.jpg
 *
 * Scheduling can be thought of as determining whether adequate resources are available to carry out the plan. Two resources that scheduling would 
 * have to take into account for our example above are fuel and time. If it takes two gallons of gas to get to the store and back and your car only has 
 * one gallon, you must develop a plan that includes a stop at the gas station. If it takes 5 minutes to drive to the store, the store closes at 10:00, 
 * and it is currently 9:30, you must also take that time constraint into account when scheduling your task.
 *
 * @section automated Automated Planning
 *
 * The automated planning community provides techniques for representing the actions that an agent may take in the world and the goals 
 * it wants to achieve together. These are complemented with computer software for automatically generating a plan which is composed
 *  of actions that when executed will obtain the goals of the agent. 
 *
 * In our above example, we as users would describe each of the actions available to us together with the goal of having milk at home. 
 * The automated planning software would generate the timeline with the plan for going out a purchasing the milk.  
 *
 * The automated planning process may also involve the user in a mixed-initiative form of interaction where they user can specify portions 
 * of the plan that the automated planner should complete or the automated planner can ask the user for guidance on planning decisions such 
 * as which actions to use for a particular goal. 
 *
 * There is a wealth of literature on automated planning. The <a href="http://ic.arc.nasa.gov/tech/groups/index.php?gid=8&&ta=2">Planning and Scheduling Group</a> at NASA Ames 
 * NASA Ames' Planning and Scheduling group provides a good entry point for applications related to spacecraft. The annual 
 * <a href="http://www.icaps-conference.org">International Conference on Automated Planning and Scheduling</a> provides a excellent entry point into the broader research community.
 */
