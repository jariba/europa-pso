package org.space.oss.psim.command;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.space.oss.psim.Command;

public class CommandImpl 
	implements Command 
{
	private static int nextID=1;
	
	protected int id_;
	protected String type_;
	protected Map<String,Object> args_;
	
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
	
	protected Map<String,Object> parseArgs(String args)
	{
		Map<String,Object> m = new TreeMap<String,Object>();
		
		StringTokenizer st = new StringTokenizer(args,",");
		
		while (st.hasMoreTokens()) {
			String arg = st.nextToken();
			StringTokenizer st1 = new StringTokenizer(arg,"=");
			assert st1.countTokens() == 2;
			m.put(st1.nextToken(),st1.nextToken());
		}
		
		return m;
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

}
