package org.space.oss.psim;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.space.oss.psim.data.impl.DatabaseConnectionStrategy;
import org.space.oss.psim.data.impl.HSQLDBConnection;
import org.space.oss.psim.ui.PSimDesktop;
import org.space.oss.psim.util.FileUtil;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;


/*
 * PSim server
 */
public class Server 
{
	private static Logger LOG = Logger.getLogger(Server.class);	
	
	protected GenericApplicationContext appContext = null;
	protected static String psimHome=null;
	protected static Server server;
	protected boolean running = false;
	
	// Storage Services
	protected DatabaseConnectionStrategy databaseConnection;
	
	// Server & Services
	protected PSim psim;
	
	// File locations determined by Server
	protected static String log4jConfig = "config/Server.log4j.properties";
	protected static String bshFile = "Server.bsh";
	protected static String appContextConfig = "config/Server.spring.xml";
	
	public static void main(String[] args) 
	{
		try {
	    	PropertyConfigurator.configure(makeAbsPath(log4jConfig));
	      	Config cfg = parseArgs(args);	
	      	
	    	server = new Server();
	    	server.init(cfg);
	    	Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	    	LOG.info("PSim server started");
		}
		catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
	}
	
	public Server()
	{
		// No initializations.
	}
	
	public static Server instance() { return server; }
	public static PSim getPSim() { return server.psim; }
	public ApplicationContext getAppContext() { return appContext;}
	public boolean isRunning() { return running; }

	public static Config parseArgs(String args[])
	{		
		ConfigByMap cfg = new ConfigByMap(args);

		String bshUI="Y";
		String db="hsqldb";
		for (int i=0;i<args.length;i++) {
			if (args[i].startsWith("-UseBshUI="))
				bshUI = args[i].substring("-UseBshUI=".length());
			if (args[i].startsWith("-bshFile="))
				bshFile = args[i].substring("-bshFile=".length());
			if (args[i].startsWith("-db="))
				db = args[i].substring("-db=".length());
		}				

		cfg.addValue("confrmHome", getDeployDirectory());
		cfg.addValue("bshUI", bshUI);
		cfg.addValue("bshFile", bshFile);
		cfg.addValue("db", db);
		return cfg;
	}
	
	public void init(Config cfg)
	{
		if (running) {
			LOG.info("PSim server already running. Server.init() had no effect");
		}
		else {
			//initDB(cfg);
			initAppContext();
			initServer(cfg);
			initUI(cfg);	
			running = true;
		}
	}
	
	public void shutdown()
	{
		if (running) {
			running = false;
			psim.shutdown();
			LOG.info("PSim server shutdown completed");
		}
		else {
			LOG.info("PSim server not running. Server.shutdown had no effect");
		}
	}
	
	protected void shutdownDB()
	{
		try {
			databaseConnection.disconnect();
			LOG.info( databaseConnection.getServerName()+" connection ended.");	
		}
		catch (Exception e) {
			LOG.error("Failed disconnecting from "+databaseConnection.getServerName(),e);			
		}
	}
	
	protected void initDB(Config cfg)
	{
		// initialize strategy
		if (cfg.getValue("db").equals("hsqldb")) {
			databaseConnection = new HSQLDBConnection();
		} else if (cfg.getValue("db").equals("mysql-dev")) {
			//databaseConnection = new MySQLDevConnection();
		} else if (cfg.getValue("db").equals("mysql")) {
			//databaseConnection = new MySQLConnection();
		} else {
			throw new RuntimeException("Database connection strategy cannot be determined.");
		}
		
	    try {
	    	databaseConnection.connect();
	        LOG.info( databaseConnection.getServerName()+" connection started.");	        
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
		
	protected void initAppContext()
	{	
		appContext = new GenericApplicationContext();
	    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
		xmlReader.loadBeanDefinitions(new FileSystemResource(makeAbsPath(appContextConfig)));
		
		String tempPath = null;
		try {
			File tempDir = FileUtil.getTempDir();
			if (tempDir!=null) {
				File confrmTempDir = new File(tempDir.getCanonicalPath()+"/"+"psim");
				if (confrmTempDir.exists()) {
					confrmTempDir.delete();
				}
				if (!FileUtil.createDirectory(confrmTempDir)) {
					LOG.error("Could not create confrm temp directory");
				} else {
					tempPath = confrmTempDir.getCanonicalPath();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		
		// Apply config parameter values
		Properties cfgParams = new Properties();
		cfgParams.put("confrm.home",psimHome);
		cfgParams.put("confrm.tempdir", tempPath);
		// TODO JRB:cfgParams.put("config.dbConfigFile", databaseConnection.getConfigFile());

		PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
		configurer.setProperties(cfgParams);
		appContext.addBeanFactoryPostProcessor(configurer);
		appContext.refresh();		
	}
	
	protected void initServer(Config cfg)
	{
		try {
			psim = (PSim)appContext.getBean("PSim");
			psim.init(cfg);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	protected boolean isLocal(Config cfg) {
		return cfg.getValue("db").equals("hsqldb");
	}
	
	protected void initUI(Config cfg)
	{
		// Bring up UI if requested
    	if("N".equals(cfg.getValue("bshUI"))) 
    		return;

    	PSimDesktop desktop = makeDesktop(cfg);
    	desktop.run();
    }
	
	protected PSimDesktop makeDesktop(Config cfg)
	{
		return new PSimDesktop(this,cfg);
	}
	
	public static String getDeployDirectory()
	{
		if (psimHome == null) {
		    psimHome = System.getenv("PSIM_HOME");
		    if (psimHome == null)
		    	psimHome = System.getProperty("PSIM_HOME");
		    if (psimHome==null) {
		    	psimHome=".";
		    	System.err.println("PSIM_HOME is not defined, internally setting PSIM_HOME to '"+psimHome+"'");
		    }
		    else
		    	System.out.println("PSIM_HOME is:"+psimHome);
		}
    	
        return psimHome;		
	}
	
	public static String makeAbsPath(String path)
	{
        return getDeployDirectory()+"/"+path;		
	}
	
    static protected class ShutdownHook extends Thread
    {
    	public ShutdownHook()
    	{
    		super("ShutdownHook");
    	}

    	public void run() 
    	{
    		if (server.isRunning())
    		    server.shutdown();
    	}
    }
    
}
