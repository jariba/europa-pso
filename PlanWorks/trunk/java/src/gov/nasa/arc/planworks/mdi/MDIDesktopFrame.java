package gov.nasa.arc.planworks.mdi;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class MDIDesktopFrame extends JFrame
{
    private MDIDynamicMenuBar menuBar = null;
    private MDIWindowButtonBar windowBar = null;
    private MDIDesktopPane desktopPane = null;
    private int unnamedwindows = 0;
    public MDIDesktopFrame(String name)
    {
	super(name);
	Container contentPane = getContentPane();
	menuBar = new MDIDynamicMenuBar();
	windowBar = new MDIWindowButtonBar(30, 80);
	desktopPane = new MDIDesktopPane();
	// contentPane.add(menuBar, BorderLayout.NORTH);
	contentPane.add(desktopPane, BorderLayout.CENTER);
	contentPane.add(windowBar, BorderLayout.SOUTH);
	setJMenuBar(menuBar);
	desktopPane.setVisible(true);
	menuBar.setVisible(true);
	windowBar.setVisible(true);
	this.setVisible(true);
    }
    public MDIDesktopFrame(String name, JMenu [] constantMenus)
    {
	super(name);
	Container contentPane = getContentPane();
	menuBar = new MDIDynamicMenuBar(constantMenus, true);
	windowBar = new MDIWindowButtonBar(30, 80);
	desktopPane = new MDIDesktopPane();
	// contentPane.add(menuBar, BorderLayout.NORTH);
	contentPane.add(desktopPane, BorderLayout.CENTER);
	contentPane.add(windowBar, BorderLayout.SOUTH);
	setJMenuBar(menuBar);
	desktopPane.setVisible(true);
	menuBar.setVisible(true);
	windowBar.setVisible(true);
	this.setVisible(true);
    }
    public MDIInternalFrame createFrame()
    {
	MDIInternalFrame newFrame = 
	    new MDIInternalFrame(unnamedwindows, menuBar, windowBar);
	desktopPane.add(newFrame);
	newFrame.setVisible(true);
	unnamedwindows++;
	return newFrame;
    }
    public MDIInternalFrame createFrame(String title)
    {
	MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar,
							 windowBar);
	desktopPane.add(newFrame);
	newFrame.setVisible(true);
	return newFrame;
    }
    public MDIInternalFrame createFrame(String title, boolean resizable)
    {
	MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar,
							 windowBar, resizable);
	desktopPane.add(newFrame);
	newFrame.setVisible(true);
	return newFrame;
    }
    public MDIInternalFrame createFrame(String title, boolean resizable, 
					boolean closable)
    {
	MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar,
							 windowBar, resizable,
							 closable);
	desktopPane.add(newFrame);
	newFrame.setVisible(true);
	return newFrame;
    }
    public MDIInternalFrame createFrame(String title, boolean resizable, 
					boolean closable, boolean maximizable)
    {
	MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar,
							 windowBar, resizable,
							 closable, 
							 maximizable);
	desktopPane.add(newFrame);
	newFrame.setVisible(true);
	return newFrame;
    }
    public MDIInternalFrame createFrame(String title, boolean resizable, 
					boolean closable, boolean maximizable,
					boolean iconifiable)
    {
	MDIInternalFrame newFrame = new MDIInternalFrame(title, menuBar,
							 windowBar, resizable,
							 closable, 
							 maximizable, 
							 iconifiable);
	desktopPane.add(newFrame);
	newFrame.setVisible(true);
	return newFrame;
    }
}
