// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPluginEvent.java,v 1.2 2005-01-20 21:07:47 meboyce Exp $
//

package gov.nasa.arc.planworks.plugin;

import java.util.EventObject;

/**
 * <code>PluginEvent</code> - 
 *
 * @author <a href="mailto:mboyce@email.arc.nasa.gov">Matt Boyce</a>
 *                         NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PwPluginEvent extends EventObject
{
	// The highest GUI object that the event was fired from
	//protected Object source;
	// The data source which is represented by the source
	// in PlanWorks this is going to be some sort of PwEntity
	protected Object rootSource;
	// An integer defining the event type of this event
	// static ints will be defined below
	protected int id;
	public static int NOTHING = 0;
	// FLAGS
	public static int VIEW = 16;
	public static int MODEL = 32;
	public static int TRANSACTION = 64;
	public static int SELF = 128;
	// ACTION TYPES
	public static int OPEN = 1;
	public static int CLOSE = 2;
	// A String defining what to do, or some bit of information about
	// the above id... for instance, a view name.
	protected String command;
	// The event that caused this event to be fired...
	// if it were a right click this would be a MouseEvent.
	protected EventObject evtSource;
	
	public PwPluginEvent(Object source, Object rootSource, int id, String command, EventObject evtSource)
	{
		super(source);
		this.rootSource = rootSource;
		this.id = id;
		this.command = command;
		this.evtSource = evtSource;
	}
	public void setSource(Object source)
	{
		this.source = source;
	}
	public Object getSource() {return source;}
	public Object getGUISource() {return source;}
	public Object getDataSource() {return rootSource;}
	public EventObject getEventSource() {return evtSource;}
	public int getId() {return id;}
	public String getText() {return command;}
	public String getCommand() {return command;}
	public String toString()
	{
		return "Source: "+source+"\n"+
		       "Root Source: "+rootSource+"\n"+
					 "Event Source: "+evtSource+"\n"+
					 "(id/cmd): (" + id + "/\"" + command+"\")";
	}
}
