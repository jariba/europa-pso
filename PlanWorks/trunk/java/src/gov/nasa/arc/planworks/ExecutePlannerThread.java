package gov.nasa.arc.planworks;

public class ExecutePlannerThread extends Thread {
  private String commandLine;
  public ExecutePlannerThread(String commandLine) {
    this.commandLine = commandLine;
  }
  public void run() {
    try {
      System.err.println("Executing command '" + commandLine + "'");
      long t1 = System.currentTimeMillis();
      Runtime.getRuntime().exec(commandLine);
      System.err.println("Done.  Execution took " + (System.currentTimeMillis() - t1) + "ms");
    }
    catch(Exception e){}
  }
}
