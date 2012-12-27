package org.space.oss.psim.spacecraft;

import org.space.oss.psim.Command;

public interface CommandHandler 
{
	public void execute(String sender, Command c);
}
