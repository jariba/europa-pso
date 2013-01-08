package org.space.oss.psim.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.table.AbstractTableModel;

import org.space.oss.psim.Command;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.GroundStation.GSEvent;
import org.space.oss.psim.GroundStationObserver;

public class GroundStationViewer extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected GroundStation groundStation_;
	protected JTextField retries_;
	protected JToggleButton discardOnFail_;
	
	public GroundStationViewer(GroundStation gs)
	{
		groundStation_ = gs;
		
		JTabbedPane tp = new JTabbedPane();
		tp.add("Command Queue",makeCommandQueuePane());
		tp.add("Scheduled Ground Passes",makeGroundPassPane());
		
		setLayout(new FlowLayout());
		add(new JScrollPane(tp));
	}
	
	protected JPanel makeCommandQueuePane()
	{
		JPanel p = new JPanel(new BorderLayout());
		
		p.add(BorderLayout.CENTER, new JScrollPane(new JTable(new CommandQueueTM(groundStation_))));
		
		JPanel buttonPanel = new JPanel(new GridLayout(2,1));
		
		JPanel p1 = new JPanel(new FlowLayout());
		JButton btn = new JButton("Send Next Command");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				groundStation_.sendQueuedCommand(getRetries(), getDiscardOnFail());
    		}

    	});		
		p1.add(btn);

		btn = new JButton("Send All Commands");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				groundStation_.sendAllQueuedCommands(getRetries(), getDiscardOnFail());
    		}

    	});		
		p1.add(btn);
		buttonPanel.add(p1);
		
		p1 = new JPanel(new FlowLayout());

		p1.add(new JLabel("Retries:"));
		retries_ = new JTextField("10");
		p1.add(retries_);
		
		discardOnFail_ = new JCheckBox("Discard On Fail");
		p1.add(discardOnFail_);

		buttonPanel.add(p1);
		
		p.add(BorderLayout.NORTH,buttonPanel);
		return p;
	}
	
	protected JPanel makeGroundPassPane()
	{
		JPanel p = new JPanel(new FlowLayout());
		
		return p;
	}	
	
	private boolean getDiscardOnFail() 
	{
		return discardOnFail_.isSelected();
	}

	private int getRetries() 
	{
		return Integer.parseInt(retries_.getText());
	}

	protected static class CommandQueueTM 
		extends AbstractTableModel
		implements GroundStationObserver
	{
		private static final long serialVersionUID = 1L;

		protected GroundStation groundStation_;

		public CommandQueueTM(GroundStation gs)
		{
			groundStation_ = gs;
			groundStation_.addObserver(this);
		}

		public void dispose()
		{
			groundStation_.removeObserver(this);
		}

		@Override
		public int getColumnCount() 
		{
			return 1;
		}

		@Override
		public int getRowCount() 
		{
			return groundStation_.getCommandQueue().size();
		}

		@Override
		public Object getValueAt(int row, int col) 
		{
			Command c = groundStation_.getCommandQueue().get(row);

			if (col == 0)
				return c.toString();

			throw new RuntimeException("Unexpected col number:"+col);
		}

		@Override
		public String getColumnName(int col)
		{
			if (col == 0)
				return "Command";

			throw new RuntimeException("Unexpected col number:"+col);			
		}

		@Override
		public void handleEvent(GSEvent type, Command c) 
		{
			fireTableDataChanged();
		}		
	}	
}
