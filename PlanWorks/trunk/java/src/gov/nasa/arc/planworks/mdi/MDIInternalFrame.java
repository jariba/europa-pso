package gov.nasa.arc.planworks.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public class MDIInternalFrame extends JInternalFrame implements MDIFrame
{
    private ArrayList menus = null;
    private ArrayList children = null;
    private JButton button = null;
    
    public MDIInternalFrame(int n, MDIMenu menuBar, MDIWindowBar windowBar)

    {
	super();
	button = new JButton("Window " + n);
	final MDIInternalFrame temp = this;
	button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    try
			{
			    if(temp.isIcon())
				temp.setIcon(false);
			    temp.setSelected(true);
			}
		    catch(PropertyVetoException pve){}
		}
	    });
	windowBar.add(button);
	addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
							      menuBar));
    }
    public MDIInternalFrame(String title, MDIMenu menuBar, MDIWindowBar windowBar)
    {
	super(title);
	button = new JButton(title);
	final MDIInternalFrame temp = this;
	button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    try
			{
			    if(temp.isIcon())
				temp.setIcon(false);
			    temp.setSelected(true);
			}
		    catch(PropertyVetoException pve){}
		}
	    });
	windowBar.add(button);
	addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
							      menuBar));
    }
    public MDIInternalFrame(String title, MDIMenu menuBar, MDIWindowBar windowBar,
			    boolean resizable)
    {
	super(title, resizable);
	button = new JButton(title);
	final MDIInternalFrame temp = this;
	button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    try
			{
			    if(temp.isIcon())
				temp.setIcon(false);
			    temp.setSelected(true);
			}
		    catch(PropertyVetoException pve){}
		}
	    });
	windowBar.add(button);
	addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
							      menuBar));
    }
    public MDIInternalFrame(String title, MDIMenu menuBar, MDIWindowBar windowBar,
			    boolean resizable, boolean closable)
    {
	super(title, resizable, closable);
	button = new JButton(title);
	final MDIInternalFrame temp = this;
	button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    try
			{
			    if(temp.isIcon())
				temp.setIcon(false);
			    temp.setSelected(true);
			}
		    catch(PropertyVetoException pve){}
		}
	    });
	windowBar.add(button);
	addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
							      menuBar));
    }
    public MDIInternalFrame(String title, MDIMenu menuBar,
			    MDIWindowBar windowBar,
			    boolean resizable, boolean closable, 
			    boolean maximizable)
    {
	super(title, resizable, closable, maximizable);
	button = new JButton(title);
	final MDIInternalFrame temp = this;
	button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    try
			{
			    if(temp.isIcon())
				temp.setIcon(false);
			    temp.setSelected(true);
			}
		    catch(PropertyVetoException pve){}
		}
	    });
	windowBar.add(button);
	addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
							      menuBar));
    }
    public MDIInternalFrame(String title, MDIMenu menuBar,
			    MDIWindowBar windowBar,
			    boolean resizable, boolean closable,
			    boolean maximizable, boolean iconifiable)
    {
	super(title, resizable, closable, maximizable, iconifiable);
	button = new JButton(title);
	final MDIInternalFrame temp = this;
	button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    try
			{
			    if(temp.isIcon())
				temp.setIcon(false);
			    temp.setSelected(true);
			}
		    catch(PropertyVetoException pve){}
		}
	    });
	windowBar.add(button);
	addInternalFrameListener(new MDIInternalFrameListener(this, windowBar,
							      menuBar));
    }
    public void associateMenu(JMenu menu)
    {
	if(this.menus == null)
	    this.menus = new ArrayList(3);
	menus.add(menu);
	//repaint();
	validate();
    }
    public void associateMenus(JMenu [] menus)
    {
	for(int i = 0; i < menus.length; i++)
	    associateMenu(menus[i]);
	//repaint();
	validate();
    }
    public JMenu [] getAssociatedMenus()
    {
	if(menus == null || menus.isEmpty())
	    return null;
	Object [] temp = menus.toArray();
	JMenu [] retval = new JMenu [temp.length];
	for(int i = 0; i < temp.length; i++)
	    retval[i] = (JMenu) temp[i];
	return retval;
    }
    public JButton getButton()
    {
	return button;
    }
    public void addChild(MDIInternalFrame child) 
	throws IllegalArgumentException
    {
	if(this.children == null)
	    this.children = new ArrayList(3);
	if(this.children.contains(child))
	    throw new IllegalArgumentException("Attempted to add a child twice");
	this.children.add(child);
    }
    public MDIInternalFrame [] getChildren()
    {
	if(this.children == null || this.children.isEmpty())
	    return null;
	return (MDIInternalFrame []) this.children.toArray();
    }
    class MDIInternalFrameListener implements InternalFrameListener
    {
	private MDIInternalFrame frame = null;
	private MDIWindowBar windowBar = null;
	private MDIMenu menu = null;
	
	public MDIInternalFrameListener(MDIInternalFrame frame, 
					MDIWindowBar windowBar, MDIMenu menu)
	{
	    this.frame = frame;
	    this.windowBar = windowBar;
	    this.menu = menu;
	}
	public void internalFrameClosing(InternalFrameEvent e)
	{
	    MDIInternalFrame [] children = frame.getChildren();
	    if(children == null)
		return;
	    for(int i = 0; i < children.length; i++)
		{
		    try
			{
			    children[i].setClosed(true);
			}
		    catch(java.beans.PropertyVetoException pve)
			{
			    //what on earth should be done here?
			}
		    //desktop.remove(children[i]); this may not be necessary
		}
	}
	public void internalFrameActivated(InternalFrameEvent e)
	{
            // System.out.println("Activated frame: " + frame.getTitle());
	    menu.notifyActivated(frame);
	    frame.validate();
	}
	public void internalFrameClosed(InternalFrameEvent e)
	{
	    windowBar.notifyDeleted(frame);
	}
	public void internalFrameDeactivated(InternalFrameEvent e)
	{
	}
	public void internalFrameDeiconified(InternalFrameEvent e)
	{
	}
	public void internalFrameIconified(InternalFrameEvent e)
	{
	}
	public void internalFrameOpened(InternalFrameEvent e)
	{
	}
    }
}


