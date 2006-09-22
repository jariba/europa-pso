package dsa;

import java.io.*;
import java.util.*;

public class JNI {
    public static native void load(String model);
    public static native void addPlan(String txSource);
    public static native String getComponents();
    public static native String getViolations(int actionKey);

    /** Solver API Calls **/
    public static native String solverConfigure(String configFile, int horizonStart, int horizonEnd);
    public static native String solverSolve(int maxSteps, int maxDepth);
    public static native String solverStep();
    public static native String solverReset();
    public static native String solverClear();

    /** Call-back handlers **/
    public static void handleCallBack(){
	System.out.println("Called from C++");
    }
}
