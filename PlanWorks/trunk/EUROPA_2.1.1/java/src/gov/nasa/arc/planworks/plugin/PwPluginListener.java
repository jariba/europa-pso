// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PwPluginListener.java,v 1.2 2005-01-20 21:07:48 meboyce Exp $
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
public interface PwPluginListener extends EventListener
{
	public abstract void pluginRequest(PwPluginEvent evt);
}
