package org.space.oss.psim.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.space.oss.psim.Spacecraft;

public class SpacecraftViewer extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected Spacecraft spacecraft_;
	
	public SpacecraftViewer(Spacecraft s)
	{
		spacecraft_ = s;
		
		setLayout(new BorderLayout());
	}	
}
