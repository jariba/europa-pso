package UBO;

import org.ops.ui.PSDesktop;
import org.ops.ui.util.LibraryLoader;
import org.ops.ui.util.SimpleTimer;

import psengine.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

class Main
{
	public static void main(String args[])
	{
	    if ("".equals(args[2]))
		    PSDesktop.run(args);
	    else 
	        runBatchTest(args);
	}
	
	public static void runBatchTest(String args[])
	{
	    try {
	        String debugMode = args[0];
	        String test = args[2];
	        Integer bound = new Integer(args[3]);
	        long timeoutMsecs = (new Integer(args[4])).intValue() * 1000; 
	        String solver = args[5];

	        LibraryLoader.loadLibrary("System_"+debugMode);        
	        PSEngine.initialize();	    
	        PSEngine engine = PSEngine.makeInstance();
	        engine.start();
	        String nddlModel = "UBO-gen-initial-state.nddl";
	        engine.executeScript("nddl",nddlModel,true/*isFile*/);

	        if ("BuiltIn".equals(solver))
	            runBuiltInSolver(engine,test,bound,timeoutMsecs);
	        else if ("IFIR".equals(solver))
	            runIFIRSolver(engine,test,bound,timeoutMsecs);
	        else
	            throw new RuntimeException("Unknown solver:"+solver);

	        //engine.shutdown();  TODO: this is causing problems      
	        PSEngine.terminate();
	    }
	    catch (Exception e) {
            throw new RuntimeException(e);	        
	    }
	}	
	
	public static void runBuiltInSolver(PSEngine engine,String test,Integer bound, long timeoutMsecs)
	{
        PSVariable v = engine.getVariableByName("maxDuration");
        v.specifyValue(PSVarValue.getInstance(bound));

        PSSolver solver = engine.createSolver("PlannerConfig.xml"/*config*/);
	    solver.configure(0/*horizonStart*/,1000/*horizonEnd*/);	   
	    
	    SimpleTimer timer = new SimpleTimer();
	    timer.start();
	    boolean timedOut = false;
	    while (solver.hasFlaws() && !solver.isExhausted()) {
	        solver.step();
	        if (timer.getElapsed() > timeoutMsecs) {
	            timedOut = true;
	            break;
	        }
	    }
        timer.stop();
        
        // Save results
        // test-name bound best-makespan time-in-msecs solution stepCount
        int makespan = ((timedOut || solver.isExhausted()) ? 0 : bound); // TODO: this could be lower, ground solution and find out
        StringBuffer buf = new StringBuffer();
        String separator="    ";
        buf.append(test).append(separator)
           .append(bound).append(separator)
           .append(makespan).append(separator)
           .append(timer.getElapsed()).append(separator)
           //.append(s.getSolutionAsString()) // TODO: extract solution
           .append(solver.getStepCount()).append(separator)
           .append("\n");
        
        writeToFile("Solver-BuiltIn.txt",buf.toString());  
	}

    public static void runIFIRSolver(PSEngine engine,String test,Integer bound, long timeoutMsecs)
    {     
        // TODO: since this is randomized, run several times an get avg
        IFlatIRelaxSolver s = new IFlatIRelaxSolver();
        boolean usePSResources = false; // TODO: eventually switch to true
        s.solve(engine,timeoutMsecs,bound,usePSResources);
        RCPSPUtil.ground(s.getActivities());
        
        // Save results
        // test-name bound best-makespan time-in-msecs solution
        int makespan = (s.getMakespan() != Integer.MAX_VALUE ? s.getMakespan() : 0);
        StringBuffer buf = new StringBuffer();
        String separator="    ";
        buf.append(test).append(separator)
           .append(bound).append(separator)
           .append(makespan).append(separator)
           .append(s.getElapsedMsecs()).append(separator)
           .append(s.getSolutionAsString())
           .append("\n");
        
        writeToFile("Solver-IFIR.txt",buf.toString());
    }	
    
    protected static void writeToFile(String filename,String str)
    {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename,true/*append*/));
            out.write(str);
            out.close();
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }                
    }
}
