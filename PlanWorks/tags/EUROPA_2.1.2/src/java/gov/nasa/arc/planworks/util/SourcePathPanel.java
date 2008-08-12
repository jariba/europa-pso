package gov.nasa.arc.planworks.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import gov.nasa.arc.planworks.ConfigureAndPlugins;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.util.DirectoryChooser;

public class SourcePathPanel extends JPanel {
    private List paths;
    private JPanel sourcePathPanel;

    public SourcePathPanel() {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	add(new JLabel("Source paths"));
	paths = new LinkedList();
	sourcePathPanel = new JPanel();
	sourcePathPanel.setBackground(ColorMap.getColor("System.text.background"));
	JScrollPane scrollPane = new JScrollPane(sourcePathPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	sourcePathPanel.setLayout(new BoxLayout(sourcePathPanel, BoxLayout.Y_AXIS));
	scrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
	add(scrollPane);

	add(new SourceInputPanel());
    }
    public SourcePathPanel(List paths) {
	this();
	for(Iterator it = paths.iterator(); it.hasNext();) {
	    try {
		addPath((String) it.next());
	    }
	    catch(FileNotFoundException fnfe) {
		displayFileNotFoundDialog(fnfe);
	    }
	}
    }
    private void addPath(String path) throws FileNotFoundException {
	if(path.equals(""))
	    return;
	File file = new File(path);
	if(!file.exists())
	    throw new FileNotFoundException("Source path '" + path + "' does not exist.");
	path = file.getAbsolutePath();
	sourcePathPanel.add(new SourcePath(path, sourcePathPanel));
	paths.add(path);
	revalidate();
    }

    private void removePath(String path) {
	System.err.println("Removing path '" + path + "'");
	System.err.println("BEFORE:");
	for(Iterator it = paths.iterator(); it.hasNext();) {
	    System.err.println((String)it.next());
	}
	paths.remove(path);
	System.err.println("AFTER:");
	for(Iterator it = paths.iterator(); it.hasNext();) {
	    System.err.println((String)it.next());
	}
    }
    public List getPaths() {
	return Collections.unmodifiableList(paths);
    }

    private void displayFileNotFoundDialog(FileNotFoundException fnfe) {
	JOptionPane.showMessageDialog(null, fnfe.getMessage(), "File not found", JOptionPane.ERROR_MESSAGE);
    }

    private class SourcePath extends JPanel {
	private JComponent parent;
	private String path;
	private SourcePath self;
	public SourcePath(String _path, JComponent _parent) {
	    parent = _parent;
	    path = _path;
	    self = this;
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(ColorMap.getColor( "System.text.background"));

	    JButton removeButton = new JButton("-");
			// a bit of overkill, but I like a consistant look (Matthew E. Boyce)
			if (PlanWorks.isMacOSX()) removeButton.setBackground(ColorMap.getColor( "System.text.background"));
	    removeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			removePath(path);
			parent.remove(self);
			parent.revalidate();
			parent.getParent().repaint();
		    }
		});

	    add(removeButton);

	    JLabel pathLabel = new JLabel(path);
	    add(pathLabel);

	    add(Box.createGlue());
	    setMaximumSize(getPreferredSize());
	}
    }

    private class SourceInputPanel extends JPanel {
	private JTextField input;
	public SourceInputPanel() {
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setBackground(ColorMap.getColor( "System.text.background"));

	    input = new JTextField(30);
	    add(input);
	    
	    JButton browseButton = new JButton("Browse");
	    add(browseButton);

	    JButton addButton = new JButton("+");
	    add(addButton);

	    browseButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			DirectoryChooser dirChooser = 
			    PlanWorks.getPlanWorks().
			    createDirectoryChooser(new File(ConfigureAndPlugins.
							    getProjectConfigValue(ConfigureAndPlugins.PROJECT_WORKING_DIR,
										  PlanWorks.getPlanWorks().getCurrentProjectName())));
			String currentSelectedDir = dirChooser.getValidSelectedDirectory();
			if(currentSelectedDir == null)
			    return;
			input.setText(currentSelectedDir);
		    }
		});

	    addButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			try {
			    addPath(input.getText());
			    input.setText("");
			}
			catch(FileNotFoundException fnfe) {
			    displayFileNotFoundDialog(fnfe);
			    //put up dialog
			}
		    }
		});
	}
    }
}
