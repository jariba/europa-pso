package org.ops.ui.main.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.ops.ui.filemanager.model.FileModel;
import org.ops.ui.schemabrowser.swing.SchemaView;

import psengine.PSEngine;
import psengine.PSUtil;

public class PSDesktop extends JFrame {

	private JDesktopPane desktop;
	private Logger log = Logger.getLogger(getClass().getName());
	private PSEngine engine;
	private FileModel fileModel;

	private SchemaView schemaBrowser;

	private String fileName;

	private PSDesktop() {
		this.desktop = new JDesktopPane();
		this.add(this.desktop);

		// Closing behavior. Add a question dialog?
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				askAndExit();
			}
		});

		// Hook up engine
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				if (engine != null)
					releaseEngine();
			}
		}));

		hookupEngine();

		// Build views
		this.schemaBrowser = new SchemaView(this.engine, this.fileModel);
		this.schemaBrowser.setVisible(true);
		this.desktop.add(this.schemaBrowser);

		// Finish up
		buildMenu();
		this.setSize(600, 700);
	}

	protected void askAndExit() {
		if (JOptionPane.showConfirmDialog(this, "Close this application?",
				"Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
			System.exit(0);
		}
	}

	private void buildMenu() {
		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);
		JMenu menu;

		menu = new JMenu("File");
		bar.add(menu);
		JMenuItem item = new JMenuItem("Load");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile(fileName);
			}
		});
		menu.addSeparator();
		item = new JMenuItem("Exit");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				askAndExit();
			}
		});

		menu = new JMenu("Windows");
		bar.add(menu);
		menu.add(this.schemaBrowser.getToggleMenuItem());
	}

	private void updateTitle() {
		final String prefix = "Europa desktop";
		// Add file name(s)
		StringBuffer b = new StringBuffer(prefix);
		int count = 0;
		for (File f : fileModel) {
			if (count++ == 0)
				b.append(": ");
			else
				b.append(", ");
			b.append(f.getName());
		}
		this.setTitle(b.toString());
	}

	/** Load the file into the model and update title */
	private void loadFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			JOptionPane.showMessageDialog(this, "File " + file
					+ " does not exist");
		} else {
			fileModel.removeFile(file);
			fileModel.addFile(file);
		}
		updateTitle();
	}

	/**
	 * Pops up a file chooser dialog to open .nddl file. @return full path to
	 * the chosen file, or null
	 */
	private String askForNddlFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"NDDL files", "nddl"));
		int res = chooser.showOpenDialog(this);
		if (res != JFileChooser.APPROVE_OPTION)
			return null;
		File file = chooser.getSelectedFile();
		return file.getAbsolutePath();
	}

	/** Create and connect PSEngine */
	protected void hookupEngine() {
		String debugMode = "g";
		try {
			PSUtil.loadLibraries(debugMode);
		} catch (UnsatisfiedLinkError e) {
			log
					.log(
							Level.SEVERE,
							"Cannot load Europa libraries. Please make the "
									+ "dynamic libraries are included in LD_LIBRARY_PATH "
									+ "(or PATH for Windows)", e);
			System.exit(1);
		}

		engine = PSEngine.makeInstance();
		engine.start();
		fileModel = new FileModel(engine);

		log.log(Level.INFO, "Engine started");
	}

	/** Shutdown and release engine. Save any state if necessary */
	protected void releaseEngine() {
		fileModel = null;
		if (engine != null) {
			engine.shutdown();
			engine.delete();
			engine = null;
			log.log(Level.INFO, "Engine released");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			System.out.println("Unable to load native look and feel");
		}

		PSDesktop me = new PSDesktop();

		// Load files or pop up dialog
		String fname;
		if (args.length == 0) {
			fname = me.askForNddlFile();
			if (fname == null) {
				JOptionPane.showMessageDialog(me, "No file chosen. Exiting");
				return;
			}
		} else
			fname = args[0];
		me.fileName = fname;
		me.loadFile(fname);

		me.setVisible(true);
	}

}
