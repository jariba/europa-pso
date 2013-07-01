package org.space.oss.psim.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.space.oss.psim.TelemetryObserver;
import org.space.oss.psim.TelemetryRecord;
import org.space.oss.psim.TelemetryService;
import org.space.oss.psim.TelemetrySource;

public class TelemetryViewer extends JPanel 
{
	private static final long serialVersionUID = 1L;

	protected TelemetryService telemetryService_;
	
	public TelemetryViewer(TelemetryService ts)
	{
		telemetryService_ = ts;
	
		setLayout(new BorderLayout());
		add(new JScrollPane(new JTable(new TelemetryDataTM(telemetryService_))));
	}
	
	protected static class TelemetryDataTM 
		extends AbstractTableModel
		implements TelemetryObserver
	{
		private static final long serialVersionUID = 1L;
		
		protected TelemetryService telemetryService_;
		
		public TelemetryDataTM(TelemetryService ts)
		{
			telemetryService_ = ts;
			telemetryService_.addObserver(this);
		}
		
		public void dispose()
		{
			telemetryService_.removeObserver(this);
		}
		
		@Override
		public int getColumnCount() 
		{
			return 3;
		}

		@Override
		public int getRowCount() 
		{
			return telemetryService_.getAllTelemetry().size();
		}

		@Override
		public Object getValueAt(int row, int col) 
		{
			TelemetryRecord tr = telemetryService_.getAllTelemetry().get(row);
			
			if (col == 0)
				return tr.getTime();
			if (col == 1)
				return tr.getSource().getID();
			if (col == 2)
				return tr.getData();
			
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
		public void handleNewTelemetry(TelemetrySource source, long time,
				Object data) 
		{
			fireTableDataChanged();
		}		
	}
}
