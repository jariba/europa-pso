package org.space.oss.psim.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.space.oss.psim.GroundStation;

public class GroundStationViewer extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected GroundStation groundStation_;
	
	public GroundStationViewer(GroundStation gs)
	{
		groundStation_ = gs;
		
		JTabbedPane tp = new JTabbedPane();
		tp.add("Command Queue",makeCommandQueuePane());
		tp.add("Scheduled Ground Passes",makeGroundPassPane());
		
		setLayout(new BorderLayout());
		add(new JScrollPane(tp));
	}
	
	protected JPanel makeCommandQueuePane()
	{
		JPanel p = new JPanel(new BorderLayout());
		
		return p;
	}
	
	protected JPanel makeGroundPassPane()
	{
		JPanel p = new JPanel(new BorderLayout());
		
		return p;
	}	
}
