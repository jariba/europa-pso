package org.ops.ui;

import java.awt.BorderLayout;
import java.util.Calendar;

import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;

import bsh.Interpreter;

import dsa.DSA;
import dsa.DSAManager;
import dsa.Component;
import dsa.Resource;
import dsa.Solver;
import dsa.SolverManager;

import org.ops.ui.chart.PSJFreeResourceChart;
import org.ops.ui.chart.PSResourceChart;
import org.ops.ui.chart.PSResourceChartDSAModel;
import org.ops.ui.gantt.PSEGantt;
import org.ops.ui.gantt.PSGantt;
import org.ops.ui.gantt.PSGanttDSAModel;
import org.ops.ui.solver.PSDSASolverDialog;
import org.ops.ui.mouse.ActionViolationsPanel;
import org.ops.ui.mouse.ActionDetailsPanel;

public class PSDesktopDSA
    extends PSDesktop
{
	public static void main(String[] args) 
	{
		if (args.length > 0)
			bshFile_ = args[0];
		
		PSDesktop desktop = new PSDesktop();
		desktop.runUI();
	}
   
    protected void registerPSEngine(Interpreter interp)
        throws Exception
    {
        interp.set("dsa",getDSA());
    }
    
    public static DSA getDSA()
    {
        return DSAManager.getInstance();
    }
    	
    public void makeSolverDialog()
    {
    	try {
    		Solver solver;
    		
    		try {
    			solver = SolverManager.instance();
    		}
    		catch (Exception e) {
    		     solver = null;	
    		}
    		
    		JInternalFrame frame = makeNewFrame("Solver");
    		frame.getContentPane().setLayout(new BorderLayout());
    		frame.getContentPane().add(new JScrollPane(new PSDSASolverDialog(this,solver)));
    		frame.setSize(675,375);
    	}
    	catch (Exception e)
    	{
    		throw new RuntimeException(e);
    	}
    }
    
    public void showActivities(Component c)
    {
        final String[] columns = {
                "Key",
                "Name",
                "EarliestStart",
                "LatestStart",
                "EarliestEnd",
                "LatestEnd",
                "DurationMin",
                "DurationMax",
         };
            
         makeTableFrame("Activities for "+c.getName(),c.getActions(),columns);        	
    }    
    
    public JInternalFrame makeResourceGanttFrame(
	        Calendar start,
	        Calendar end)
    {
        PSGantt gantt = new PSEGantt(new PSGanttDSAModel(getDSA(),start),start,end);

        JInternalFrame frame = makeNewFrame("Resource Schedule");
        frame.getContentPane().add(gantt);
		frame.setSize(frame.getSize()); // Force repaint
        
        return frame;    
    }    
    
    public PSResourceChart makeResourceChart(Resource r,Calendar start)
    {
    	return new PSJFreeResourceChart(
    			r.getName(),
    			new PSResourceChartDSAModel(r),
    			start);
    }    
    
    public JInternalFrame makeResourcesFrame(Calendar start)
    {
        JTabbedPane resourceTabs = new JTabbedPane();       
        for (Resource r : getDSA().getResources()) {
            resourceTabs.add(r.getName(),makeResourceChart(r,start));
        }
        
        JInternalFrame frame = makeNewFrame("Resources");
        frame.getContentPane().add(resourceTabs);
		frame.setSize(frame.getSize()); // Force repaint
        
        return frame;
    }    
    
    public JInternalFrame makeViolationsFrame()
    {
        ActionViolationsPanel vp = new ActionViolationsPanel(getDSA());
        JInternalFrame frame = makeNewFrame("Violations");
        frame.getContentPane().add(vp);
        frame.setLocation(500,180);
        frame.setSize(300,300); 
        
        return frame;       
    }

    public JInternalFrame makeDetailsFrame()
    {
        ActionDetailsPanel dp = new ActionDetailsPanel(getDSA());
        JInternalFrame frame = makeNewFrame("Details");
        frame.getContentPane().add(dp);
        frame.setLocation(800,180);
        frame.setSize(300,200);        
        
        return frame;
    }    
}

