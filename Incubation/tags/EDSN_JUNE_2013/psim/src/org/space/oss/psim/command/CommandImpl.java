package org.space.oss.psim.command;

import java.lang.reflect.Type;
import java.util.Map;

import org.space.oss.psim.Command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CommandImpl 
	implements Command 
{
	private static int nextID=1;
	
	protected int id_;
	protected String type_;
	protected Map<String,Object> args_;
	protected String destinationField_="spacecraft";
	
	public CommandImpl(String type,Map<String,Object> args)
	{
		id_ = getNewID();
		type_ = type;
		setArgs(args);
	}
	
	public CommandImpl(String type,String args)
	{
		id_ = getNewID();
		type_ = type;
		setArgs(args);		
	}
	
	@Override
	public Integer getID() { return id_; }
	
	@Override
	public String getType() { return type_; }

	@Override
	public Map<String, Object> getArgs() { return args_; }
	
	@Override
	public void setArgs(Map<String, Object> args) 
	{
		args_ = args;
	}

	@Override
	public void setArgs(String args) 
	{
		Map<String,Object> newArgs = parseArgs(args);
		setArgs(newArgs);
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String,Object> parseArgs(String args)
	{
		Type type = new TypeToken<java.util.Map<String, Object>>(){}.getType();
		Gson gson = new Gson();
		Map<String,Object> retval = (Map<String,Object>)gson.fromJson(args, type);
		return retval;
	}
	
	protected static int getNewID()
	{
		return nextID++;
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		buf.append("CMD{").append(id_).append(",").append(type_).append(",").append(args_).append("}");
		return buf.toString();
	}

	@Override
	public String getDestination() 
	{
		// Default implementation
		return getArgs().get(destinationField_).toString();
	}

}
