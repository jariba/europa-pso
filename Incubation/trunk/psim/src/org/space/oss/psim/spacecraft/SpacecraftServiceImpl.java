package org.space.oss.psim.spacecraft;

import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.space.oss.psim.Config;
import org.space.oss.psim.PSim;
import org.space.oss.psim.PSimServiceBase;
import org.space.oss.psim.Spacecraft;
import org.space.oss.psim.SpacecraftService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class SpacecraftServiceImpl extends PSimServiceBase implements SpacecraftService 
{
	private static Logger LOG = Logger.getLogger(SpacecraftServiceImpl.class);
	protected Map<String,Spacecraft> spacecraft_;
	
	@Autowired
	protected SpacecraftFactory spacecraftFactory;
	
	@Override
	public void init(PSim psim, Config cfg) 
	{
		super.init(psim, cfg);
		spacecraft_ = new TreeMap<String,Spacecraft>();
		List<Spacecraft> sc = spacecraftFactory.makeSpacecraft(psim_);
		for (Spacecraft s : sc)
			spacecraft_.put(s.getID(), s);
		
		LOG.info("Initialized SpacecraftService");
	}
	
	@Override
	public void shutdown() 
	{
		LOG.info("Shut down SpacecraftService");
	}

	@Override
	public Spacecraft getSpacecraftByID(String spacecraftID) 
	{
		return spacecraft_.get(spacecraftID);
	}

	@Override
	public Collection<Spacecraft> getAllSpacecraft() 
	{
		return spacecraft_.values();
	}
	
	@Override
	public void save(String dir) 
	{
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (Spacecraft s : this.spacecraft_.values()) {
			Gson gson = new Gson();
			buf.append(gson.toJson(s, s.getClass())).append(",");			
		}
		buf.append("]");
		
		try {
			FileWriter fw = new FileWriter(dir+"/Spacecraft.json");
			fw.write(buf.toString());
			fw.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}			
	}	
}
