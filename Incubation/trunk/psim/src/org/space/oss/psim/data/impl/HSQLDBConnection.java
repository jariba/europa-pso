package org.space.oss.psim.data.impl;


import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.space.oss.psim.Server;

public class HSQLDBConnection implements DatabaseConnectionStrategy {
	private static String configFile = "hibernate.hsqldb-cfg.xml";
	private static String driverClass = "org.hsqldb.jdbcDriver";
	private static String propertiesFile = "data/db/hsqldb-server.properties";
	private org.hsqldb.Server dbServer;

	public void connect() throws Exception {
        Class.forName( driverClass);	        
        dbServer = new org.hsqldb.Server();
        
        Properties props = new Properties();
        props.load(new FileInputStream( Server.makeAbsPath(propertiesFile)));
        String propName = "server.database.0";
        String v = props.getProperty(propName);
        StringTokenizer st = new StringTokenizer(v,":"); st.nextToken();
        String filename = st.nextToken();
        props.setProperty(propName, Server.makeAbsPath(filename));
        
        StringBuffer buf = new StringBuffer(); 
        int i=0;
        for (Entry<Object,Object> entry : props.entrySet()) {
        	if (i>0)
        		buf.append(";");
        	String key = entry.getKey().toString().substring(7);
        	buf.append(key).append("=").append(entry.getValue());
        	i++;
        }
        		        	
        dbServer.putPropertiesFromString(buf.toString());
        dbServer.start(); 
	}

	public void disconnect() throws Exception {
		try {
			dbServer.checkRunning(false);
		} catch (Exception e) {
			dbServer.shutdown();
		}

		int secsTimeout=5; // Give the DB server n secs to shutdown
		long tstamp = (new java.util.Date()).getTime();
		while (dbServer.getState() != org.hsqldb.ServerConstants.SERVER_STATE_SHUTDOWN) {
			long curTime = (new java.util.Date()).getTime();
			if ((curTime-tstamp) > (secsTimeout*1000)) {
				return;
			}
		}	
	}

	public String getConfigFile() {
		return configFile;
	}

	public String getServerName() {
		return "Embedded HSQLDB server"; 
	}
}
