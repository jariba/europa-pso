// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PluginListener.java,v 1.1 2004-07-27 21:58:06 taylor Exp $
//

package gov.nasa.arc.planworks.plugin;

import java.util.EventListener;

/**
 * Describe interface <code>PluginListener</code> here.
 *
 * @author <a href="mailto:meboyce@email.arc.nasa.gov">Matt Boyce</a>
 *                             NASA Ames Research Center - Code IC
 * @version 0.0
 */
public interface PluginListener extends EventListener
{
	public abstract void pluginRequest(PluginEvent evt);
}
