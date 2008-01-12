// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: DebugConsole.java,v 1.5 2005-11-08 21:30:46 miatauro Exp $
//
// PlanWorks -- 
//
// Michael Iatauro -- started 5nov05
//

package gov.nasa.arc.planworks.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
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
import javax.swing.ActionMap;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JInternalFrame;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

import gov.nasa.arc.planworks.PlannerControlJNI;
import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.sequence.sequenceSteps.SequenceStepsView;

/**
 * <code>DebugConsole</code> - Window to enable/disable and capture PLASMA debugging output
 *
 * @author <a href="mailto:miatauro@email.arc.nasa.gov">Michael Iatauro</a>
 * @version 0.1
 */

public class DebugConsole extends JPanel {
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;
    private static final int DEBUG_WIDTH = 30;
    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;

    private int refreshSleep;
    private JTextField timedRefreshField;
    private DebugMatchPanel debugMatchPanel;
    private JTextField debugInputField;
    private JScrollPane textPane;
    private JTextArea textArea;
    private JPanel allPanel;
    private RefreshPanel refreshPanel;
    private JPanel debugInputPanel;
    private JButton disableAll;
    private SearchStringManager searchString;
    private SearchStringListener searchUpdateListener;
    private boolean wrapSearch;
    private boolean writeToTerminal;

    public DebugConsole(final String debugPath) {
	refreshSleep = 300;
	searchString = new SearchStringManager();
	wrapSearch = false;
	writeToTerminal = false;

	setBackground(ViewConstants.VIEW_BACKGROUND_COLOR);
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	refreshPanel = new RefreshPanel();
	add(refreshPanel);
	searchString.addListener(refreshPanel);

	textArea = new JTextArea(30,50);
	//textArea.setEditable(false);
	textArea.setEditable(true);
	Caret caret = textArea.getCaret();
	caret.setVisible(true);
	textArea.setCaretColor(Color.BLACK);
	caret.setBlinkRate(0);
	textArea.setCaretPosition(0);
	textPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					       JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	add(textPane);

	debugInputPanel = createDebugInputPanel();
	add(debugInputPanel);

	debugMatchPanel = new DebugMatchPanel();
	add(debugMatchPanel);
	
	disableAll = new JButton("Disable All");
	add(disableAll);
	disableAll.setMaximumSize(disableAll.getPreferredSize());

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

	installBaseImap();	
    }

    private void installBaseImap() {
	//add an input map for when we're focused
	InputMap imap = new InputMap();
	imap.setParent(getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
	setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap);
	ActionMap amap = new ActionMap();
	amap.setParent(getActionMap());
	setActionMap(amap);
		      
	//C-s search foward forwardSearch
	String forwardSearch = "forwardSearch";
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, true), forwardSearch);
	amap.put(forwardSearch, new EnterSearchModeAction(this, FORWARD));

	//C-r search backward backwardSearch
	String backwardSearch = "backwardSearch";
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK, true), backwardSearch);
	amap.put(backwardSearch, new EnterSearchModeAction(this, BACKWARD));

	//M-,, home jump to beginning jumpBeginning
	String jumpBeginning = "jumpBeginning";
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.META_MASK | InputEvent.SHIFT_MASK, true), jumpBeginning);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0, true), jumpBeginning);
	amap.put(jumpBeginning, new JumpAction(this, Integer.MIN_VALUE));

	//M-., end jump to end jumpEnd
	String jumpEnd = "jumpEnd";
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.META_MASK | InputEvent.SHIFT_MASK, true), jumpEnd);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, true), jumpEnd);
	amap.put(jumpEnd, new JumpAction(this, Integer.MAX_VALUE));
    }

    private JPanel createDebugInputPanel() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

	panel.add(Box.createGlue());

	debugInputField = new JTextField(DEBUG_WIDTH);
	panel.add(debugInputField);
	debugInputField.setMaximumSize(debugInputField.getPreferredSize());
	
	JButton button = new JButton("+"); //add listener that adds stuff to the debugMatchPanel
	panel.add(button);
	
	panel.add(Box.createGlue());
	
	class AddActionListener implements ActionListener {
	    public AddActionListener() {}
	    public void actionPerformed(ActionEvent ae) {
		if(debugInputField.getText().trim().equals(""))
		    return;
		debugMatchPanel.addDebugString(debugInputField.getText().trim());
		debugInputField.setText("");
		//((JInternalFrame)getRootPane().getParent()).pack();
		revalidate();
	    }
	}
	
	debugInputField.addActionListener(new AddActionListener());
	button.addActionListener(new AddActionListener());
	return panel;
    }

    private void enterSearchMode(final int searchDirection) {
	refreshPanel.setSearchVisible(true);
	refreshPanel.setSearchDirection(searchDirection);
	refreshPanel.setSearchWrapped(false);

	InputMap imap = new InputMap();
	imap.setParent(getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
	setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap);
	ActionMap amap = new ActionMap();
	amap.setParent(getActionMap());
	setActionMap(amap);

	searchUpdateListener = new SearchStringListener() {
		public void stringChanged(String s) {
		    search(searchDirection, -1 - s.length()); 
		    //negative offset here will force it to skip in the correct direction so it appears
		    //as though we're searching from the original caret position
		}
	    };
	searchString.addListener(searchUpdateListener);
	//esc, up, down, left, right exit search mode exitSearchMode
	String exitSearch = "exitSearch";
	amap.put(exitSearch, new ExitSearchModeAction(this));
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), exitSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), exitSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), exitSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), exitSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), exitSearch);

	//have to add all of these by hand because Java doesn't provide an convenient way of listening for all typable characters
	String [] chars = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
			   "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
			   "`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "[", "]", "\\", ";", "'", ",", ".", "/",
			   "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "{", "}", "|", ":", "\"", "<", ">", "?"};
			   //};

	String addToSearch = "addToSearch";
	amap.put(addToSearch, new AddToSearchAction(this));
	for(int i = 0; i < chars.length; i++) {
	    imap.put(KeyStroke.getKeyStroke(chars[i]), addToSearch);
	    imap.put(KeyStroke.getKeyStroke("shift " + chars[i]), addToSearch);
	}
	
	//for some reason, this doesn't cover - _ = + [ { ] } \ | ; : ' " , < . > / ?
	//so we have to add those by hand.
	//also have to add _ + { } : " < > in two different ways because Java sucks.
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UNDERSCORE, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BRACELEFT, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BRACERIGHT, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COLON, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTEDBL, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LESS, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.SHIFT_MASK, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_GREATER, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, 0, true), addToSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, KeyEvent.SHIFT_MASK, true), addToSearch);

	//C-s search forward from point
	String continueSearchForward = "continueSearchForward";
	amap.put(continueSearchForward, new SearchAction(this, FORWARD));
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK, true), continueSearchForward);

	//C-r search backward from point
	String continueSearchBackward = "continueSearchBackward";
	amap.put(continueSearchBackward, new SearchAction(this, BACKWARD));
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK, true), continueSearchBackward);

	//C-w select word forward selectForward
	String addWordForwardToSearch = "addWordForwardToSearch";
	amap.put(addWordForwardToSearch, new AddWordAction(this));
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK, true), addWordForwardToSearch);

	//C-f select character forward selectCharForward
	String addCharForwardToSearch = "addCharForwardToSearch";
	amap.put(addCharForwardToSearch, new AddCharAction(this));
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK, true), addCharForwardToSearch);

	//C-b, delete, backspace select character back selectCharBackward
	String removeLastCharFromSearch = "removeLastCharFromSearch";
	amap.put(removeLastCharFromSearch, new RemoveCharAction(this));
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK, true), removeLastCharFromSearch);
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0, true), removeLastCharFromSearch);	
	imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, true), removeLastCharFromSearch);
    }

    private void leaveSearchMode() {
	searchString.removeListener(searchUpdateListener);
	refreshPanel.setSearchVisible(false);
	
	InputMap imap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap.getParent());
	imap.clear();

	ActionMap amap = getActionMap();
	setActionMap(amap.getParent());
	amap.clear();

	searchString.save();
	searchString.clear();
	searchUpdateListener = null;
	refreshPanel.setMatchFailed(false);
	refreshPanel.setSearchWrapped(false);
    }

    private void search(int dir, int offset) {
	if(dir == FORWARD)
	    searchForward(offset);
	else
	    searchBackward(offset);
    }

    private void searchForward(int offset) {
	refreshPanel.setSearchDirection(FORWARD);
	String searchStr = searchString.str();
	int currPos = textArea.getCaretPosition() + offset;
	
	//find index of search string from caret position + offset
	int nextIndex = textArea.getText().indexOf(searchStr, currPos);

	if(nextIndex == -1) {
	    refreshPanel.setMatchFailed(true);
	    wrapSearch = true;
	    return;
	}
	wrapSearch = false;
	//set caret at index
	textArea.setCaretPosition(nextIndex);
	//select to index + length
	textArea.moveCaretPosition(nextIndex + searchStr.length());
	refreshPanel.setMatchFailed(false);
    }

    private void searchBackward(int offset) {
	refreshPanel.setSearchDirection(BACKWARD);
	String searchStr = searchString.str();
	int currPos = textArea.getCaretPosition() - offset;

	int prevIndex = textArea.getText().lastIndexOf(searchStr, currPos);

	if(prevIndex == -1) {
	    refreshPanel.setMatchFailed(true);
	    wrapSearch = true;
	    return;
	}

	wrapSearch = false;
	//set caret at index + length
	textArea.setCaretPosition(prevIndex + searchStr.length());
	//select to index
	textArea.moveCaretPosition(prevIndex);
	refreshPanel.setMatchFailed(false);
    }

    private void addToNextNonWord() {
	refreshPanel.setSearchWrapped(false);
	StringBuffer addToSearch = new StringBuffer();
	int i = textArea.getCaretPosition();
	while(!Character.isLetterOrDigit(textArea.getText().charAt(i))) {
	    addToSearch.append(textArea.getText().charAt(i));
	    ++i;
	}
	while(Character.isLetterOrDigit(textArea.getText().charAt(i))) {
	    addToSearch.append(textArea.getText().charAt(i));
	    ++i;
	}
	searchString.add(addToSearch.toString());
    }

    private void addNextChar() {
	refreshPanel.setSearchWrapped(false);
	searchString.add(textArea.getText().charAt(textArea.getCaretPosition()));
    }

    private void removeLastChar() {
	refreshPanel.setSearchWrapped(false);
	searchString.backUp();
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
	    // ((JInternalFrame)getRootPane().getParent()).pack();
 	    revalidate();
	}
	public void removeDebugString(DebugMatch match) {
	    if(match.isEnabled())
		disableDebugString(match.toString());
	    remove(match);
	    matches.remove(match);
// 	    ((JInternalFrame)getRootPane().getParent()).pack();
 	    revalidate();
	}
	public void disableDebugString(String str) {
	    PlannerControlJNI.disableDebugMsg(firstPart(str), secondPart(str));
	}
	public void enableDebugString(String str) {
	    PlannerControlJNI.enableDebugMsg(firstPart(str), secondPart(str));
	}
	public void disableAll() {
	    for(Iterator it = matches.iterator(); it.hasNext();)
		((DebugMatch)it.next()).disableMsg();
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

	    add(Box.createGlue());

	    enableBox = new JCheckBox();
	    add(enableBox);

	    match = new JTextField(DEBUG_WIDTH);
	    match.setText(str);
	    match.setEditable(false);
	    match.setMaximumSize(match.getPreferredSize());
	    add(match);

	    JButton removeButton = new JButton("-");
	    add(removeButton);

	    add(Box.createGlue());
	    enableBox.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent ie) {
			if(ie.getStateChange() == ItemEvent.SELECTED) {
			    match.setFont(match.getFont().deriveFont(Font.BOLD));
			    panel.enableDebugString(str);
			    revalidate();
			}
			else if(ie.getStateChange() == ItemEvent.DESELECTED) {
			    match.setFont(match.getFont().deriveFont(Font.PLAIN));
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
			    if(writeToTerminal)
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

    class EnterSearchModeAction extends AbstractAction {
	private int dir;
	private DebugConsole console;
	public EnterSearchModeAction(DebugConsole console, int dir) {
	    this.console = console;
	    this.dir = dir;
	}
	public void actionPerformed(ActionEvent ae) {
	    console.enterSearchMode(dir);
	}
    }

    class JumpAction extends AbstractAction {
	private int amt;
	private DebugConsole console;
	public JumpAction(DebugConsole console, int amt) {
	    this.amt = amt;
	    this.console = console;
	}
	public void actionPerformed(ActionEvent ae) {
	    if(amt == Integer.MIN_VALUE)
		textArea.setCaretPosition(0);
	    else if(amt == Integer.MAX_VALUE)
		textArea.setCaretPosition(textArea.getText().length());
	}
    }

    class RefreshPanel extends JPanel implements SearchStringListener {
	private static final String forwardText = "I-search: ";
	private static final String backwardText = "Backward I-search: ";
	private static final String failingText = "Failing ";
	private static final String wrappedText = "Wrapped ";

	private JLabel searchLabel;
	String searchDir;
	String matchFailed;
	String wrapped;
	public RefreshPanel() {
	    searchDir = forwardText;
	    matchFailed = "";
	    wrapped = "";
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	    searchLabel = new JLabel(forwardText);
	    searchLabel.setVisible(false);
	    add(searchLabel);

	    add(Box.createGlue());

	    JLabel refreshLabel = new JLabel("Refresh every ");
	    add(refreshLabel);
	    refreshLabel.setMaximumSize(refreshLabel.getPreferredSize());

	    timedRefreshField = new JTextField("300");
	    add(timedRefreshField);
	    timedRefreshField.setMaximumSize(timedRefreshField.getPreferredSize());
	    
	    JLabel millis = new JLabel("ms");
	    add(millis);
	    millis.setMaximumSize(millis.getPreferredSize());

	    add(Box.createGlue());

	    JCheckBox writeToTerm = new JCheckBox("Copy output to terminal", false);
	    add(writeToTerm);
	    writeToTerm.setMaximumSize(writeToTerm.getPreferredSize());

	    timedRefreshField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			try {
			    refreshSleep = Integer.parseInt(timedRefreshField.getText());
			}
			catch(NumberFormatException nfe) {
			    JOptionPane.showMessageDialog(PlanWorks.getPlanWorks(), "Error: '" + timedRefreshField.getText() + "' is not an integer.",
							  "Integer parse error.", JOptionPane.ERROR_MESSAGE);
			}
		    }});
	    writeToTerm.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent ie) {
			writeToTerminal = ie.getStateChange() == ItemEvent.SELECTED;
		    }
		});

	}
	public void setSearchWrapped(boolean wrap) {
	    wrapped = (wrap ? wrappedText : "");
	}
	public void setMatchFailed(boolean failed) {
	    matchFailed = (failed ? failingText : "");
	    setText();
	}
	public void setSearchDirection(int dir) {
	    searchDir = (dir == FORWARD ? forwardText : backwardText);
	    setText();
	}
	public void setSearchVisible(boolean b) { 
	    searchLabel.setVisible(b); 
	    revalidate();
	}

	public void stringChanged(String s){setText();}

	private void setText() {
	    searchLabel.setText(matchFailed + wrapped + searchDir + searchString.str());
	}
    }

    class ExitSearchModeAction extends AbstractAction {
	private DebugConsole console;
	public ExitSearchModeAction(DebugConsole console) {
	    this.console = console;
	}
	public void actionPerformed(ActionEvent ae) {
	    console.leaveSearchMode();
	}
    }

    class AddToSearchAction extends AbstractAction {
	DebugConsole console;
	public AddToSearchAction(DebugConsole console) {
	    this.console = console;
	}
	public void actionPerformed(ActionEvent ae) {
	    console.refreshPanel.setSearchWrapped(false);
	    console.searchString.add(ae.getActionCommand());
	}
    }

    class SearchStringManager {
	private StringBuffer savedString;
	private StringBuffer string;
	private List listeners;
	public SearchStringManager() {
	    savedString = new StringBuffer();
	    string = new StringBuffer();
	    listeners = new LinkedList();
	}
	public void add(char c) {
	    string.append(c);
	    publishChanged();
	}
	public void add(String c) {
	    string.append(c);
	    publishChanged();
	}
	public void backUp() {
	    if(string.length() == 0)
		return;
	    string.deleteCharAt(string.length()-1);
	    publishChanged();
	}

	public void clear() {
	    string.delete(0, string.length());
	    publishChanged();
	}

	public String str(){return string.toString();}
	
	public void addListener(SearchStringListener l) {listeners.add(l);}
	public void removeListener(SearchStringListener l) {listeners.remove(l);}
	
	public void save() {
	    savedString.append(string);
	}
	
	public void restore() {
	    string.append(savedString);
	    savedString.delete(0, savedString.length());
	    publishChanged();
	}

	private void publishChanged() {
	    String val = str();
	    for(Iterator it = listeners.iterator(); it.hasNext();)
		((SearchStringListener)it.next()).stringChanged(val);
	}
    }

    interface SearchStringListener {
	public void stringChanged(String c);
    }

    class SearchAction extends AbstractAction {
	private DebugConsole console;
	private int dir;
	public SearchAction(DebugConsole console, int dir) {
	    this.console = console;
	    this.dir = dir;
	}

	public void actionPerformed(ActionEvent ae) {
	    int offset = 1;
	    if(searchString.str().equals("")) {
		console.searchString.clear();
		console.searchString.restore();
		offset = 0;
	    }
	    if(console.wrapSearch) {
		console.refreshPanel.setSearchWrapped(true);
		if(dir == FORWARD)
		    offset = 0 - console.textArea.getCaretPosition();
		else
		    offset = console.textArea.getCaretPosition() - console.textArea.getText().length();
		console.wrapSearch = false;
	    }
	    console.search(dir, offset);
	}
    }

    class AddWordAction extends AbstractAction {
	private DebugConsole console;
	public AddWordAction(DebugConsole console) {
	    this.console = console;
	}
	public void actionPerformed(ActionEvent ae) {
	    console.addToNextNonWord();
	}
    }

    //select forward to next character
    class AddCharAction extends AbstractAction {
	private DebugConsole console;
	public AddCharAction(DebugConsole console) {
	    this.console = console;
	}
	public void actionPerformed(ActionEvent ae) {
	    console.addNextChar();
	}
    }

    class RemoveCharAction extends AbstractAction {
	private DebugConsole console;
	public RemoveCharAction(DebugConsole console) {
	    this.console = console;
	}
	public void actionPerformed(ActionEvent ae) {
	    console.removeLastChar();
	}
    }
}