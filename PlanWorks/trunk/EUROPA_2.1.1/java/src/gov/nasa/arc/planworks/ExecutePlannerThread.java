// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ExecutePlannerThread.java,v 1.6 2003-12-15 18:40:21 taylor Exp $
//

package gov.nasa.arc.planworks;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class ExecutePlannerThread extends Thread {
  private String commandLine;
  private String [] env;
  private static final String ENV_DEST = "PPW_WRITE_DEST";
  private static final String ENV_NSTEPS = "PPW_WRITE_NSTEPS";
  public ExecutePlannerThread(String commandLine, String dest, String stepsPerWrite) {
    this.commandLine = commandLine;
    env = new String[2];
    env[0] = ENV_DEST + "=" + dest;
    env[1] = ENV_NSTEPS + "=" + stepsPerWrite;
  }
  public void run() {
    try {
      System.err.println("Executing command '" + commandLine + "'");
      long t1 = System.currentTimeMillis();
      Process planner = Runtime.getRuntime().exec(commandLine, env);
      
      InputStream is = planner.getInputStream();
      int character;
      while((character = is.read()) != -1) {
        System.err.write(character);
      }
      System.err.println("Done.  Returned value " + planner.exitValue() + ".\nExecution took " + (System.currentTimeMillis() - t1) + "ms");
    }
    catch(Exception e) {
      System.err.println( "ExecutePlannerThread failed: " + e);
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
