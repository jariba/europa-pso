package org.ops.ui.europaplugin;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.ops.ui.filemanager.model.FileModel;
import org.osgi.framework.BundleContext;

import psengine.PSEngine;
import psengine.PSUtil;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Tatiana Kichkaylo
 */
public class Activator extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "EuropaPlugin";

	/** The shared instance */
	private static Activator plugin;

	/** Wrapper/access point for Europa engine */
	private PSEngine engine;

	/** Model that keeps track of loaded files */
	private FileModel fileModel;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		hookupEngine();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		releaseEngine();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/** Create and connect PSEngine */
	protected void hookupEngine() {
		String debugMode = "g";
		try {
			PSUtil.loadLibraries(debugMode);
		} catch (UnsatisfiedLinkError e) {
			logError("Cannot load Europa libraries. Please make the "
					+ "dynamic libraries are included in LD_LIBRARY_PATH "
					+ "(or PATH for Windows)\n" + e.getLocalizedMessage());
			throw e;
		}

		engine = PSEngine.makeInstance();
		engine.start();

		System.out.println("Engine started");
	}

	public void logError(String message) {
		this.getLog().log(new Status(Status.ERROR, PLUGIN_ID, message));
	}

	/** Shutdown and release engine. Save any state if necessary */
	protected void releaseEngine() {
		engine.shutdown();
	}

	/** Give access to the engine */
	public PSEngine getEngine() {
		return engine;
	}

	public FileModel getFileModel() {
		if (fileModel == null)
			fileModel = new FileModel(engine);
		return fileModel;
	}
}
