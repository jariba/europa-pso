package gov.nasa.arc.planworks.mdi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

/**
 * A menubar with dynamic menus.  The menus are dependent on which 
 * MDIInternalFrame has focus.  The menubar also has constant menus, which 
 * appear after the MDIInternalFrame's associated menus.
 */

public class MDIDynamicMenuBar extends JMenuBar implements MDIMenu
{
    private ArrayList constantMenus = new ArrayList(3);

    /**
     * creates a new MDIDynamicMenuBar and registers itself with the MDIDesktop
     * @param desktop the MDIDesktop (the MDIDesktopFrame and, indirectly, the
     * JScroll
     */

    public MDIDynamicMenuBar()
    {
	super();
	for(int i = 0; i < getMenuCount(); i++)
	    constantMenus.add(getMenu(i));
	this.setVisible(true);
    }
    public MDIDynamicMenuBar(JMenu [] initialMenus, boolean constant)
    {
	super();
	for(int i = 0; i < initialMenus.length; i++)
	    {
		if(constant)
		    constantMenus.add(initialMenus[i]);
		add(initialMenus[i]);
	    }
	this.setVisible(true);
    }
    public MDIDynamicMenuBar(JMenu [] constantMenus,
			     JMenu [] initialMenus)
    {
	super();
	this.constantMenus = new ArrayList(constantMenus.length);
	for(int i = 0; i < constantMenus.length; i++)
	    {
		this.constantMenus.add(constantMenus[i]);
		this.add(constantMenus[i]);
	    }
	if(initialMenus != null)
	    for(int i = 0; i < initialMenus.length; i++)
		this.add(initialMenus[i]);
	this.setVisible(true);
    }
    public void notifyActivated(MDIFrame frame)
    {
	removeAll();
	if(constantMenus != null)
	    for(int i = 0; i < constantMenus.size(); i++)
		add((JMenu)constantMenus.get(i));
	JMenu [] temp = frame.getAssociatedMenus();
	if(temp != null)
	    for(int i = 0; i < temp.length; i++)
		add(temp[i]);
	//repaint(getVisibleRect());
	validate();
    }
    public void addConstantMenu(JMenu constantMenu)
    {
	if(constantMenus == null)
	    constantMenus = new ArrayList(3);
	constantMenus.add(constantMenu);
	this.add(constantMenu);
    }
    public void addConstantMenus(JMenu [] constantMenus)
    {
	for(int i = 0; i < constantMenus.length; i++)
	    addConstantMenu(constantMenus[i]);
    }
    public void addMenu(JMenu menu)
    {
	this.add(menu);
    }
    public void addMenus(JMenu [] menus)
    {
	for(int i = 0; i < menus.length; i++)
	    this.add(menus[i]);
    }
}
