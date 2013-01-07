package org.space.oss.psim.telemetry;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.TelemetryObserver;
import org.space.oss.psim.TelemetryRecord;
import org.space.oss.psim.TelemetryService;
import org.space.oss.psim.TelemetrySource;


public class TelemetryServiceImpl 
	implements TelemetryService, TelemetryObserver
{
	private static Logger LOG = Logger.getLogger(TelemetryServiceImpl.class);
	
	protected List<TelemetrySource> sources_;
	protected List<TelemetryObserver> observers_;
	protected List<TelemetryRecord> allTelemetry_; // TODO: store in database instead

	@Override
	public void init(PSim psim, Config cfg) 
	{
		sources_ = new ArrayList<TelemetrySource>();
		observers_ = new ArrayList<TelemetryObserver>();
		allTelemetry_ = new ArrayList<TelemetryRecord>();
		LOG.info("Initialized TelemetryService");
	}

	@Override
	public void shutdown() 
	{
		LOG.info("Shut down TelemetryService");
	}

	@Override
	public void addTelemetrySource(TelemetrySource ts) 
	{
		ts.addObserver(this);
		sources_.add(ts);
	}

	@Override
	public void removeTelemetrySource(TelemetrySource ts) 
	{
		ts.removeObserver(this);
		sources_.remove(ts);
	}

	@Override
	public void handleNewTelemetry(TelemetrySource source, long time,
			Object data) 
	{
		TelemetryRecord tr = new TelemetryRecordImpl(time,source,data);
		allTelemetry_.add(0,tr);
		notifyNewTelemetry(tr);
		LOG.debug("Received: {src="+source.getID()+" , time="+time+" , data="+data.toString());
	}

	protected void notifyNewTelemetry(TelemetryRecord tr) 
	{
		for (TelemetryObserver to : observers_)
			to.handleNewTelemetry(tr.getSource(), tr.getTime(), tr.getData());
	}

	@Override
	public List<TelemetryRecord> getAllTelemetry() 
	{
		return allTelemetry_;
	}

	@Override
	public void addObserver(TelemetryObserver to) 
	{
		observers_.add(to);
	}

	@Override
	public void removeObserver(TelemetryObserver to) 
	{
		observers_.remove(to);
	}
	
	protected static class TelemetryRecordImpl implements TelemetryRecord
	{
		protected long time;
		protected TelemetrySource source;
		protected Object data;
		
		public TelemetryRecordImpl(long t,TelemetrySource s,Object d)
		{
			time = t;
			source = s;
			data = d;
		}
		
		@Override
		public long getTime() { return time; }

		@Override
		public TelemetrySource getSource() { return source; }

		@Override
		public Object getData() { return data; }
	}
}
