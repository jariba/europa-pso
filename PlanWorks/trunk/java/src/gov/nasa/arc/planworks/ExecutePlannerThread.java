package gov.nasa.arc.planworks;

public class ExecutePlannerThread extends Thread {
  private String commandLine;
  public ExecutePlannerThread(String commandLine) {
    this.commandLine = commandLine;
  }
  public void run() {
    try {
      Thread.sleep(5000);
      System.err.println(commandLine);
    }
    catch(Exception e){}
  }
}
