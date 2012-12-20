package org.space.oss.psim.data.impl;

public interface DatabaseConnectionStrategy {
	String getConfigFile();
	String getServerName();
	void connect() throws Exception;
	void disconnect() throws Exception;
}
