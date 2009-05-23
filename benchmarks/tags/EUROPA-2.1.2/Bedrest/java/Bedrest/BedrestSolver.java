package Bedrest;

import psengine.*;
import java.util.List;
import java.util.Vector;

import org.ops.ui.util.SimpleTimer;

public class BedrestSolver
{
    protected PSEngine psengine_;
    protected PSSolver solver_;
    protected List<ActivityEntry> merged_;
    protected List<ActivityEntry> rejected_;
    
    public BedrestSolver(PSEngine pse,String solverConfig)
    {
        psengine_ = pse;
        solver_ = psengine_.createSolver(solverConfig);
        solver_.configure(0,(int)1e8);
        merged_ = new Vector<ActivityEntry>();        
        rejected_ = new Vector<ActivityEntry>();        
    }
    
    public List<ActivityEntry> getMerged() { return merged_; }
    public List<ActivityEntry> getRejected() { return rejected_; }
    
    public void solve(List<ActivityEntry> activities, int maxCnt)
    {
        SimpleTimer timer = new SimpleTimer();
        
        int cnt = activities.size();
        int i = 1;
        rejected_.clear();
        
        timer.start();
        for (ActivityEntry act : activities) {
            psengine_.executeScript("nddl",act.toNddl(),false/*isFile*/);
            PSToken newGoal = psengine_.getTokens().get(psengine_.getTokens().size()-1);
            solver_.solve(1000,1000); 
            if (solver_.isExhausted() || solver_.isTimedOut()) {
                psengine_.executeScript("nddl",act.name+".reject();",false/*isFile*/);
                rejected_.add(act);
                output("Rejected "+act.type+"("+newGoal.getKey()+","+act.name+")");
            }
            else if (hasState(newGoal,"MERGED")) {
                merged_.add(act);
                output("Merged "+act.type+"("+newGoal.getKey()+","+act.name+")");                
            }
            i++;
            
            StringBuffer buf = new StringBuffer();
            buf.append(i)
               .append(" - merged:").append(merged_.size())
               .append(" - rejected:").append(rejected_.size())
               .append(" - Elapsed:").append(timer.getElapsedString())
               .append(" - Avg:").append(timer.getElapsedPer(i))
            ;
            output(buf.toString());
            
            if (i>maxCnt)
                break;
        }
        timer.stop();
        
        StringBuffer buf = new StringBuffer();
        buf.append("BedrestSolver processed ").append(i).append(" activities in ").append(timer.getElapsedString()).append(".\n")
           .append("Avg time per activity is:").append(timer.getElapsedPer(i)); 
        ;
        output(buf.toString());
    }    
    
    protected boolean hasState(PSToken t,String state)
    {
        PSVariable stateVar = t.getParameter("state");
        if (stateVar.isSingleton() && stateVar.getSingletonValue().asString().equals(state))
            return true;
        
        return false;    
    }
    
    public void output(String msg)
    {
        System.out.println(msg);       
    }
}

