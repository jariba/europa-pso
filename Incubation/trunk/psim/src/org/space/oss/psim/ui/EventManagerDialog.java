package org.space.oss.psim.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.space.oss.psim.PSimEvent;
import org.space.oss.psim.PSimEventManager;
import org.space.oss.psim.PSimObserver;

public class EventManagerDialog extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected PSimEventManager eventManager_;
	
	public EventManagerDialog(PSimEventManager m)
	{
		eventManager_ = m;
		setupLayout();
	}
	
	protected void setupLayout()
	{    	
    	JPanel btnPanel = new JPanel(new FlowLayout());  

    	JButton btn = new JButton("Play Next Event(s)");
    	btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
    		{
				eventManager_.playNextEvents();
    		} 
    	});
    	btnPanel.add(btn);
    	    	    	    	
    	setLayout(new BorderLayout());
    	add(BorderLayout.NORTH,btnPanel);  
    	add(BorderLayout.CENTER,new JScrollPane(new JTable(new PSimEventsTM(eventManager_))));
	}
		
	protected static class PSimEventsTM 
		extends AbstractTableModel
		implements PSimObserver
	{
		private static final long serialVersionUID = 1L;

		protected PSimEventManager eventManager_;

		public PSimEventsTM(PSimEventManager m)
		{
			eventManager_ = m;
			eventManager_.addObserver(this);
		}

		public void dispose()
		{
			eventManager_.removeObserver(this);
		}

		@Override
		public int getColumnCount() 
		{
			return 3;
		}

		@Override
		public int getRowCount() 
		{
			return eventManager_.getEvents().size();
		}

		@Override
		public Object getValueAt(int row, int col) 
		{
			if (row >= eventManager_.getEvents().size())
				return null;
			
			PSimEvent e = eventManager_.getEvents().get(row);

			if (col == 0)
				return e.getTime();
			if (col == 1)
				return e.getSource().getName();
			if (col == 2)
				return e.getEventData();

			throw new RuntimeException("Unexpected col number:"+col);
		}

		@Override
		public String getColumnName(int col)
		{
			if (col == 0)
				return "Time";
			if (col == 1)
				return "Source";
			if (col == 2)
				return "Data";

			throw new RuntimeException("Unexpected col number:"+col);			
		}

		@Override
		public void handleEvent(int type, Object arg) 
		{
			fireTableDataChanged();
		}		
	}	
}
