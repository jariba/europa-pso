package org.space.oss.psim.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import org.space.oss.psim.Config;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.PSimEventManager;
import org.space.oss.psim.Server;
import org.space.oss.psim.TelemetryService;
import org.space.oss.psim.TimeService;

import bsh.Interpreter;
import bsh.util.JConsole;

public class PSimDesktop 
{
	protected JDesktopPane desktop;
	protected int windowCnt=0;
    protected Interpreter bshInterpreter;
	protected JConsole bshConsole;
	protected Server psimServer;
	
	protected static String bshFile;

	public PSimDesktop(Server s,Config cfg)
	{
		psimServer = s;
        bshConsole = new JConsole();
        bshInterpreter = new Interpreter(bshConsole);
        bshFile = cfg.getValue("bshFile");
	}
	
	public void run()
	{
    	SwingUtilities.invokeLater(new UICreator());		
	}	
	
	private class UICreator
    	implements Runnable
	{
		public void run()
		{
			createAndShowGUI();
		}
	}

    private void createAndShowGUI()
    {
    	try {
    		//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    		JFrame.setDefaultLookAndFeelDecorated(true);

    		//Create and set up the window.
    		JFrame frame = new JFrame("PSim - Mission Planning Simulator");
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		frame.getContentPane().setLayout(new BorderLayout());
    		createDesktop();
    		frame.getContentPane().add(desktop,BorderLayout.CENTER);

    		//Display the window.
    		frame.pack();
    		frame.setSize(1200,600);
    		frame.setVisible(true);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		Runtime.getRuntime().exit(-1);
    	}
    }

    private void createDesktop()
    {
    	desktop = new JDesktopPane();

        // BeanShell scripting
        JInternalFrame consoleFrame = makeNewFrame("Console");
        consoleFrame.getContentPane().add(bshConsole);
        new Thread(bshInterpreter).start();

        registerBshVariables();

        if (bshFile != null) {
            try {
        	    bshInterpreter.eval("source(\""+bshFile+"\");");
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JInternalFrame makeNewFrame(String name)
    {
        JInternalFrame frame = new JInternalFrame(name);
        frame.getContentPane().setLayout(new BorderLayout());
	    desktop.add(frame);
	    int offset=windowCnt*15;
	    windowCnt++;
	    frame.setLocation(offset,offset);
	    frame.setSize(700,300);
	    frame.setResizable(true);
	    frame.setClosable(true);
	    frame.setMaximizable(true);
	    frame.setIconifiable(true);
        frame.setVisible(true);
        return frame;
    }

    public JInternalFrame makeNewFrame(String name, JComponent c)
    {
        JInternalFrame frame = makeNewFrame(name);
	    frame.add(c);
        return frame;
    }

    public void addBshVariable(String name,Object obj)
    {
        try {
            bshInterpreter.set(name,obj);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void registerBshVariables()
    {
        addBshVariable("desktop",this);
        addBshVariable("server",psimServer);
        addBshVariable("psim",Server.getPSim());
        addBshVariable("cmd",Server.getPSim().getCommandService());
        addBshVariable("tmt",Server.getPSim().getTelemetryService());
        addBshVariable("sc",Server.getPSim().getSpacecraftService());
        addBshVariable("ts",Server.getPSim().getTimeService());
    }    
    
    public JInternalFrame makeCommandingDialog()
    {
    	JInternalFrame f = makeNewFrame("Commanding", new CommandingDialog(Server.getPSim()));
    	
    	return f;
    }
    
    public JInternalFrame makeGroundStationViewer(GroundStation gs)
    {
    	JInternalFrame f = makeNewFrame("Ground Station "+gs.getID(), new GroundStationViewer(gs));
    	
    	return f;
    }
    
    public JInternalFrame makeTimeServiceDialog(TimeService ts)
    {
    	JInternalFrame f = makeNewFrame("Time Service ", new TimeServiceDialog(ts));
    	
    	return f;
    }    
    
    public JInternalFrame makeTelemetryViewer(TelemetryService ts)
    {
    	JInternalFrame f = makeNewFrame("Telemetry Service ", new TelemetryViewer(ts));
    	
    	return f;
    }        
    
    public JInternalFrame makeEventPlayer(PSimEventManager em)
    {
    	JInternalFrame f = makeNewFrame("PSim Event Player", new EventManagerDialog(em));
    	
    	return f;
    }            
}
