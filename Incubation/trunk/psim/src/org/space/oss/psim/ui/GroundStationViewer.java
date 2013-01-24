package org.space.oss.psim.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.table.AbstractTableModel;

import org.space.oss.psim.Command;
import org.space.oss.psim.GroundPass;
import org.space.oss.psim.GroundStation;
import org.space.oss.psim.GroundStation.GSEvent;
import org.space.oss.psim.GroundStationObserver;

public class GroundStationViewer extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected GroundStation groundStation_;
	protected JTextField retries_;
	protected JToggleButton discardOnFail_;
	protected JComboBox groundPassList_;
	private JPanel gpPane_;

	protected JTable gpCommandsTable_;
	
	public GroundStationViewer(GroundStation gs)
	{
		groundStation_ = gs;
		
		JTabbedPane tp = new JTabbedPane();
		tp.add("Command Queue",makeCommandQueuePane());
		tp.add("Scheduled Ground Passes",makeGroundPassPane());
		
		setLayout(new BorderLayout());
		add(tp);
		this.setPreferredSize(new Dimension(80,80));
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
		gpPane_ = new JPanel(new BorderLayout());
		
		groundPassList_ = new JComboBox(groundStation_.getGroundPasses().toArray());
    	groundPassList_.addItemListener(new ItemListener() {
    		public void itemStateChanged(ItemEvent e)  
    		{
    			if (e.getStateChange()==ItemEvent.SELECTED) {
    				GroundPass gp = (GroundPass)groundPassList_.getSelectedItem();
    				gpPane_.remove(gpCommandsTable_);
    				gpCommandsTable_ = new JTable(new GPCommandsTM(gp));
    				gpPane_.add(BorderLayout.CENTER,new JScrollPane(gpCommandsTable_));
    				gpPane_.revalidate();
    			}
    		} 
    	});
		
    	GroundPass gp = (GroundPass)groundPassList_.getSelectedItem();
    	gpCommandsTable_ = (gp != null ? new JTable(new GPCommandsTM(gp)) : new JTable());
		gpPane_.add(BorderLayout.CENTER,new JScrollPane(gpCommandsTable_));
				
		JPanel buttonPanel = new JPanel(new GridLayout(2,1));
		
		JPanel p1 = new JPanel(new FlowLayout());
		JButton btn = new JButton("Execute Ground Pass");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				((GroundPass)groundPassList_.getSelectedItem()).execute();
    		}
    	});		
		p1.add(btn);
		buttonPanel.add(groundPassList_);
		buttonPanel.add(p1);
		
		gpPane_.add(BorderLayout.NORTH,buttonPanel);
		
		return gpPane_;
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

		public void finalize()
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
		public void handleEvent(GSEvent type, Object o) 
		{
			fireTableDataChanged();
		}		
	}	
	
	
	protected static class GroundPassListTM 
		extends AbstractTableModel
		implements GroundStationObserver
	{
		private static final long serialVersionUID = 1L;

		protected GroundStation groundStation_;

		public GroundPassListTM(GroundStation gs)
		{
			groundStation_ = gs;
			groundStation_.addObserver(this);
		}

		public void finalize()
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
			return groundStation_.getGroundPasses().size();
		}

		@Override
		public Object getValueAt(int row, int col) 
		{
			GroundPass rowGP = null;
			int i=0;
			for(GroundPass gc : groundStation_.getGroundPasses()) {
				if (i==row) {
					rowGP=gc;
					break;
				}
				i++;
			}
			assert rowGP != null;
			if (col == 0)
				return rowGP;

			throw new RuntimeException("Unexpected col number:"+col);
		}

		@Override
		public String getColumnName(int col)
		{
			if (col == 0)
				return "Ground Pass Time";

			throw new RuntimeException("Unexpected col number:"+col);			
		}

		@Override
		public void handleEvent(GSEvent type, Object o) 
		{
			if (type==GSEvent.GROUND_PASS_ADDED || type==GSEvent.GROUND_PASS_REMOVED)
				fireTableDataChanged();
		}		
	}	

	protected static class GPCommandsTM 
	extends AbstractTableModel
	implements GroundStationObserver
	{
		private static final long serialVersionUID = 1L;

		protected GroundPass groundPass_;

		public GPCommandsTM(GroundPass gp)
		{
			groundPass_ = gp;
			groundPass_.getGroundStation().addObserver(this);
		}

		public void dispose()
		{
			groundPass_.getGroundStation().removeObserver(this);
		}

		@Override
		public int getColumnCount() 
		{
			return 1;
		}

		@Override
		public int getRowCount() 
		{
			return groundPass_.getCommands().size();
		}

		@Override
		public Object getValueAt(int row, int col) 
		{
			if (col == 0)
				return groundPass_.getCommands().get(row);

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
		public void handleEvent(GSEvent type, Object o) 
		{
			if (type==GSEvent.GP_COMMAND_QUEUED || type==GSEvent.GP_COMMAND_REMOVED)
				fireTableDataChanged();
		}		
	}		
}
