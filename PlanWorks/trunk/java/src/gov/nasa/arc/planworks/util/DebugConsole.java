package gov.nasa.arc.planworks.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import gov.nasa.arc.planworks.PlannerControlJNI;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;

public class DebugConsole extends JPanel {
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;
    private static final int DEBUG_WIDTH = 30;
    private static final int REFRESH_STEP = -1;

    private int refreshSleep;
    private JTextField timedRefreshField;
    private DebugMatchPanel debugMatchPanel;
    private JTextField debugInputField;
    private JTextArea textArea;
    private JPanel allPanel;
    private JPanel refreshPanel;
    private JPanel debugInputPanel;
    private JButton disableAll;

    public DebugConsole(final String debugPath) {
	refreshSleep = 300;

	setBackground(ViewConstants.VIEW_BACKGROUND_COLOR);
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(gridBag);
	c.weightx = 0;
	c.weighty = 0;
	c.gridx = 0;
	c.gridy = 0;

	refreshPanel = createRefreshPanel();
	gridBag.setConstraints(refreshPanel, c);
	add(refreshPanel);

	c.gridy++;
	textArea = new JTextArea(30,50);
	textArea.setEditable(false);
	JScrollPane textPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					       JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	gridBag.setConstraints(textPane, c);
	add(textPane);

	c.gridy++;
	debugInputPanel = createDebugInputPanel();
	gridBag.setConstraints(debugInputPanel, c);
	add(debugInputPanel);

	c.gridy++;
	debugMatchPanel = new DebugMatchPanel();
	gridBag.setConstraints(debugMatchPanel, c);
	add(debugMatchPanel);
	
	c.gridy++;
	disableAll = new JButton("Disable All");
	gridBag.setConstraints(disableAll, c);
	add(disableAll);
	
//  	addComponentListener(new ComponentAdapter() {
//  		public void componentResized(ComponentEvent ce) {
//  		    textArea.setSize(getWidth() - (getInsets().left + getInsets().right),
//  				     textArea.getHeight());
//  		}
//  	    });

	disableAll.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    debugMatchPanel.disableAll();
		}
	    });
	try {
	    Thread thread = new DebugWatcherThread(debugPath);
	    thread.setPriority(Thread.MIN_PRIORITY);
	    thread.start();
	}
	catch(FileNotFoundException fnfe) {
	    JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), 
					  "Error opening file '" + debugPath + "': " +fnfe.getMessage(),
					  "IOException", JOptionPane.ERROR_MESSAGE);
	}

    }

    private JPanel createRefreshPanel() {
	JPanel panel = new JPanel();
	GridBagConstraints c = new GridBagConstraints();
	GridBagLayout gridBag = new GridBagLayout();
	panel.setLayout(gridBag);
	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 0.5;
	c.weighty = 0.5;

	JLabel refreshLabel = new JLabel("Refresh every ");
	gridBag.setConstraints(refreshLabel, c);
	panel.add(refreshLabel);

	c.gridx++;
	timedRefreshField = new JTextField("300");
	gridBag.setConstraints(timedRefreshField, c);
	panel.add(timedRefreshField);

	c.gridx++;
	JLabel millis = new JLabel("ms");
	gridBag.setConstraints(millis, c);
	panel.add(millis);

	timedRefreshField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			refreshSleep = Integer.parseInt(timedRefreshField.getText());
			System.err.println("Set refresh time to " + refreshSleep);
		    }
		    catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), "Error: '" + timedRefreshField.getText() + "' is not an integer.",
							  "Integer parse error.", JOptionPane.ERROR_MESSAGE);
		    }
		}});

	return panel;
    }

    private JPanel createDebugInputPanel() {
	JPanel panel = new JPanel();
	GridBagConstraints c = new GridBagConstraints();
	GridBagLayout gridBag = new GridBagLayout();
	panel.setLayout(gridBag);
	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 0.5;
	c.weighty = 0.5;

	debugInputField = new JTextField(DEBUG_WIDTH);
	gridBag.setConstraints(debugInputField, c);
	panel.add(debugInputField);
	
	c.gridx++;
	JButton button = new JButton("+"); //add listener that adds stuff to the debugMatchPanel
	gridBag.setConstraints(button, c);
	panel.add(button);

	class AddActionListener implements ActionListener {
	    public AddActionListener() {}
	    public void actionPerformed(ActionEvent ae) {
		if(debugInputField.getText().trim().equals(""))
		    return;
		debugMatchPanel.addDebugString(debugInputField.getText().trim());
		debugInputField.setText("");
		((JInternalFrame)getRootPane().getParent()).pack();
		revalidate();
	    }
	}
	
	debugInputField.addActionListener(new AddActionListener());
	button.addActionListener(new AddActionListener());
	return panel;
    }

    class DebugMatchPanel extends JPanel {
	private List matches;

	public DebugMatchPanel() {
	    matches = new LinkedList();
	    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	public void addDebugString(String str) {
	    DebugMatch foo = new DebugMatch(str, this);
	    add(foo);
	    matches.add(foo);
	    ((JInternalFrame)getRootPane().getParent()).pack();
	    revalidate();
	}
	public void removeDebugString(DebugMatch match) {
	    if(match.isEnabled())
		disableDebugString(match.toString());
	    remove(match);
	    matches.remove(match);
	    ((JInternalFrame)getRootPane().getParent()).pack();
	    revalidate();
	}
	public void disableDebugString(String str) {
	    System.err.println("disableDebugString: " + firstPart(str) + " " + secondPart(str));
	    PlannerControlJNI.disableDebugMsg(firstPart(str), secondPart(str));
	}
	public void enableDebugString(String str) {
	    System.err.println("enableDebugString: " + firstPart(str) + " " + secondPart(str));
	    PlannerControlJNI.enableDebugMsg(firstPart(str), secondPart(str));
	}
	public void disableAll() {
	    System.err.println("Disabling all existing messages...");
	    for(Iterator it = matches.iterator(); it.hasNext();)
		((DebugMatch)it.next()).disableMsg();
	    System.err.println("Done with disableAll.");
	}
	private String firstPart(String str) {
	    return str.substring(0, str.indexOf(':'));
	}
	private String secondPart(String str) {
	    return str.substring(str.indexOf(':')+1);
	}
    }

    class DebugMatch extends JPanel {
	private String str;
	private DebugMatchPanel panel;
	private JTextField match;
	private DebugMatch me; //member variable so it can be used in anonymous class
	private JCheckBox enableBox;
	public DebugMatch(String _str, DebugMatchPanel _panel) {
	    str = _str;  panel = _panel;
	    me = this;
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	    enableBox = new JCheckBox();
	    add(enableBox);

	    match = new JTextField(DEBUG_WIDTH);
	    match.setText(str);
	    match.setEditable(false);
	    add(match);

	    JButton removeButton = new JButton("-");
	    add(removeButton);

	    enableBox.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent ie) {
			if(ie.getStateChange() == ItemEvent.SELECTED) {
			    match.setFont(match.getFont().deriveFont(Font.BOLD));
			    System.err.println("Enabling messages matching '" + str + "' because of enableBox state change...");
			    panel.enableDebugString(str);
			    revalidate();
			}
			else if(ie.getStateChange() == ItemEvent.DESELECTED) {
			    match.setFont(match.getFont().deriveFont(Font.PLAIN));
			    System.err.println("Disabling messages matching '" + str + "' because of enableBox state change...");
			    panel.disableDebugString(str);
			    revalidate();
			}
		    }
		});

	    removeButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			    panel.removeDebugString(me);
		    }
		});
	    enableBox.setSelected(true);
	}
	public String toString(){return str;}
	
	public boolean isEnabled(){return enableBox.isSelected();}

	public void disableMsg() {
	    if(enableBox.isSelected()) {
		System.err.println("Disabling messages matching '" + str + "'");
		enableBox.doClick();
	    }
	}
    }

    class DebugWatcherThread extends Thread {
	private File file;
	private BufferedReader debugFile;
	private long size;
	public DebugWatcherThread(String debugPath) throws FileNotFoundException {
	    file = new File(debugPath);
	    debugFile = new BufferedReader(new FileReader(file));
	    size = file.length();
	}
	public void run() {
	    while(true) {
		try{Thread.sleep(refreshSleep);}catch(Exception e){System.err.println("Warning<<sleep interrupted>>: " + e.getMessage());}
		try {
		    if(debugFile.ready()) {
			if(file.length() < size) //if there is a reduction in size, flush the current text
			    textArea.setText("");
			for(String line = debugFile.readLine(); line != null; line = debugFile.readLine()) {
			    textArea.append(line + "\n");
			    System.out.println(line);
			}
		    }
		}
		catch(IOException ioe) {
		    JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(),
						  "Debug I/O Error: " + ioe.getMessage() + "\n" +
						  "The debug console will no longer refresh for this planner run.",
						  "IOException", JOptionPane.ERROR_MESSAGE);
		    return;
		}
	    }
	}
    }
}