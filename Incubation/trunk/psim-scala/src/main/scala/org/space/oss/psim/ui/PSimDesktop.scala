package org.space.oss.psim.ui

import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JDesktopPane
import javax.swing.JInternalFrame
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import bsh.Interpreter
import bsh.util.JConsole
import org.space.oss.psim.{Config,PSimServer}
import org.space.oss.psim.Spacecraft

class PSimDesktop(s:PSimServer, c:Config)
{
    var desktop:JDesktopPane = _
    var windowCnt:Int = 0
	val server:PSimServer  = s
	val bshConsole = new JConsole();
    val bshInterpreter = new Interpreter(bshConsole);
    val bshFile = c.getValue("bshFile").getOrElse("psim.bsh");
	
    def run() {
    	SwingUtilities.invokeLater(new UICreator());		
	}	
    
    class UICreator extends Runnable 
    {
    	def run() {
			createAndShowGUI();
		}      
    }
    
    def createAndShowGUI() {
    	try {
    		//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    		JFrame.setDefaultLookAndFeelDecorated(true);

    		//Create and set up the window.
    		val frame = new JFrame("PSim - Mission Planning Simulator");
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		frame.getContentPane().setLayout(new BorderLayout());
    		createDesktop();
    		frame.getContentPane().add(desktop,BorderLayout.CENTER);

    		//Display the window.
    		frame.pack();
    		frame.setSize(1200,600);
    		frame.setVisible(true);
    	}
    	catch {
    		case e:Exception =>
    			e.printStackTrace();
    			exit(-1);
    	}    
    }
    
    def createDesktop() {
    	desktop = new JDesktopPane();

        // BeanShell scripting
        val consoleFrame = makeNewFrame("Console");
        consoleFrame.getContentPane().add(bshConsole);
        new Thread(bshInterpreter).start();

        registerBshVariables();

        if (bshFile!=null && bshFile!="") {
            try {
        	    bshInterpreter.eval("source(\""+bshFile+"\");");
            }
            catch {
              case e:Exception =>
                throw new RuntimeException(e);
            }
        }
    }

    def makeNewFrame(name:String): JInternalFrame = {
        val frame = new JInternalFrame(name)
        frame.getContentPane().setLayout(new BorderLayout())
	    desktop.add(frame)
	    var offset:Int = windowCnt*15
	    windowCnt+=1
	    frame.setLocation(offset,offset)
	    frame.setSize(700,300)
	    frame.setResizable(true)
	    frame.setClosable(true)
	    frame.setMaximizable(true)
	    frame.setIconifiable(true)
        frame.setVisible(true)
        frame
    }

    def makeNewFrame(name:String, c:JComponent): JInternalFrame = {
        val frame = makeNewFrame(name)
	    frame.add(new JScrollPane(c))
        frame
    }

    def addBshVariable(name:String, obj:Any) {
        try {
            bshInterpreter.set(name,obj);
        }
        catch {
          case e:Exception =>
            throw new RuntimeException(e);
        }
    }
    
    def registerBshVariables() {
        addBshVariable("desktop",this);
        addBshVariable("server",server);
        addBshVariable("psim",server.psim);
    }        
    
    def makeSCViewer(sc:Spacecraft): JInternalFrame = {
      val scv = new SpacecraftViewer(sc)
      scv.init()
      makeNewFrame(sc.getID,scv)
    }
}
