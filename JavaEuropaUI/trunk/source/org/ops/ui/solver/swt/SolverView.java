package org.ops.ui.solver.swt;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.ops.ui.main.swt.EuropaPlugin;
import org.ops.ui.main.swt.NddlConfigurationFields;
import org.ops.ui.solver.model.SolverListener;
import org.ops.ui.solver.model.SolverModel;
import org.ops.ui.solver.model.TimeFormatHelper;

/**
 * Solver view, Eclipse version.
 * 
 * @author Tatiana Kichkaylo
 */

public class SolverView extends ViewPart implements SolverListener {
	public static final String VIEW_ID = "org.ops.ui.solver.swt.SolverView";
	public static final String MEMENTO_TAG = "EuropaRunNddlView";
	public static final String MEMENTO_FILE = "modelFile";
	public static final String MEMENTO_CONFIG = "configFile";
	public static final String MEMENTO_HOR_START = "horizonStart";
	public static final String MEMENTO_HOR_END = "horizonEnd";
	/** Minimum width of text fields */
	private static final int TEXT_WIDTH = 50;

	private SolverModel model;

	/** Display the name of the loaded model file */
	private Label modelFileLabel;
	/** Toggle button for running the engine */
	private Button runEngineButton;
	/** Push button for reseting the horizon bounds */
	private Button resetHorizonButton;
	/** Display/input fields for the horizon bounds */
	private Text startHorizonText, endHorizonText;
	/** Input field for running for N steps */
	private Text runForStepsText;
	/** Run for N steps push button */
	private Button runForStepsButton;
	/** Steps made and time spent display fields */
	private Label stepCountLabel, timeSpentLabel;

	/** File we are about to run, if any */
	private File modelFile = null;
	/** Configuration file */
	private File configFile = null;
	/** Used only during initialization to pass horizon from memento */
	private String horizonStart = String
			.valueOf(NddlConfigurationFields.DEF_HORIZON_START),
			horizonEnd = String
					.valueOf(NddlConfigurationFields.DEF_HORIZON_END);

	/** Start of N step run. Used to get statistics labels */
	private long startOfRun;

	/** Obtains the model from the Activator singleton */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		// Parent class ignores memento, but can set some defaults
		super.init(site);
		model = EuropaPlugin.getDefault().getSolverModel();
		model.addSolverListener(this);
		if (memento == null)
			return;
		IMemento m = memento.getChild(MEMENTO_TAG);
		if (m == null)
			return;
		// Using for loops, 'cause I am lazy to check if the array size == 0
		for (IMemento c : m.getChildren(MEMENTO_CONFIG))
			configFile = new File(c.getTextData());
		if (configFile == null || !configFile.exists())
			return; // do not even read further
		for (IMemento c : m.getChildren(MEMENTO_FILE)) {
			modelFile = new File(c.getTextData());
			// Paranoia
			if (!modelFile.exists())
				modelFile = null;
		}
		for (IMemento c : m.getChildren(MEMENTO_HOR_START))
			horizonStart = c.getTextData();
		for (IMemento c : m.getChildren(MEMENTO_HOR_END))
			horizonEnd = c.getTextData();
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		IMemento mem = memento.createChild(MEMENTO_TAG);
		if (modelFile != null)
			mem.createChild(MEMENTO_FILE).putTextData(
					modelFile.getAbsolutePath());
		if (configFile != null)
			mem.createChild(MEMENTO_CONFIG).putTextData(
					configFile.getAbsolutePath());
		mem.createChild(MEMENTO_HOR_START).putTextData(
				startHorizonText.getText());
		mem.createChild(MEMENTO_HOR_END).putTextData(endHorizonText.getText());
	}

	/** Create and initialize the viewer */
	@Override
	public void createPartControl(Composite parent) {
		RowLayout outerLayout = new RowLayout(SWT.VERTICAL);
		outerLayout.fill = true;
		parent.setLayout(outerLayout);

		Composite row = new Composite(parent, SWT.NONE);
		RowLayout innerLayout = new RowLayout();
		row.setLayout(innerLayout);
		new Label(row, SWT.NONE).setText("File: ");
		modelFileLabel = new Label(row, SWT.BOLD);

		// Make bold font for labels
		FontData[] fontData = modelFileLabel.getFont().getFontData();
		for (FontData d : fontData)
			d.setStyle(SWT.BOLD);
		Font boldFont = new Font(parent.getDisplay(), fontData);

		modelFileLabel.setFont(boldFont);
		modelFileLabel.setText("Fixme");
		runEngineButton = new Button(row, SWT.TOGGLE);
		runEngineButton.setText("Run");
		runEngineButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				doEngineButtonPressed(evt);
			}
		});

		row = new Composite(parent, SWT.NONE);
		innerLayout = new RowLayout();
		row.setLayout(innerLayout);
		new Label(row, SWT.NONE).setText("Horizon");
		startHorizonText = new Text(row, SWT.SINGLE | SWT.BORDER);
		startHorizonText.setText(horizonStart);
		startHorizonText.setLayoutData(new RowData(TEXT_WIDTH, SWT.DEFAULT));
		endHorizonText = new Text(row, SWT.SINGLE | SWT.BORDER);
		endHorizonText.setText(horizonEnd);
		endHorizonText.setLayoutData(new RowData(TEXT_WIDTH, SWT.DEFAULT));
		resetHorizonButton = new Button(row, SWT.PUSH);
		resetHorizonButton.setText("Reset");
		resetHorizonButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				try {
					int start = new Integer(startHorizonText.getText());
					int end = new Integer(endHorizonText.getText());
					model.setHorizon(start, end);
				} catch (NumberFormatException e) {
					showMessage(e.getLocalizedMessage());
				}
			}
		});

		row = new Composite(parent, SWT.NONE);
		innerLayout = new RowLayout();
		row.setLayout(innerLayout);
		new Label(row, SWT.NONE).setText("Run for ");
		runForStepsText = new Text(row, SWT.SINGLE | SWT.BORDER);
		runForStepsText.setText("100");
		runForStepsText.setLayoutData(new RowData(TEXT_WIDTH, SWT.DEFAULT));
		runForStepsButton = new Button(row, SWT.PUSH);
		runForStepsButton.setText("Run");
		runForStepsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				doRunForNSteps();
			}
		});

		row = new Composite(parent, SWT.NONE);
		innerLayout = new RowLayout();
		row.setLayout(innerLayout);
		new Label(row, SWT.NONE).setText("Step count: ");
		stepCountLabel = new Label(row, SWT.BOLD);
		stepCountLabel.setFont(boldFont);
		new Label(row, SWT.NONE).setText("Run time: ");
		timeSpentLabel = new Label(row, SWT.BOLD);
		timeSpentLabel.setFont(boldFont);

		updateState();
	}

	@Override
	public void setFocus() {
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(null, "Europa Solver", message);
	}

	public void afterOneStep(long time) {
		timeSpentLabel.setText(TimeFormatHelper.formatTime(System
				.currentTimeMillis()
				- startOfRun));
		int stepCnt = model.getStepCount();
		stepCountLabel.setText(Integer.toString(stepCnt));
	}

	public void afterStepping() {
		runForStepsButton.setEnabled(true);
	}

	public void beforeStepping() {
		runForStepsButton.setEnabled(false);
	}

	public void solverStarted() {
		updateState();
	}

	public void solverStopped() {
		updateState();
	}

	/** Update state of labels and buttons */
	private void updateState() {
		// Do we have a file
		boolean haveFile = (modelFile != null);

		// File name label
		if (haveFile)
			modelFileLabel.setText(modelFile.getName());
		else
			modelFileLabel.setText("No model selected. Use Run as NDDL");

		// Run engine button
		if (haveFile) {
			runEngineButton.setEnabled(true);
			if (model.isConfigured()) {
				setEnabledFields(true);
			} else {
				setEnabledFields(false);
			}
		} else {
			runEngineButton.setEnabled(false);
			// Cannot start without a model
			setEnabledFields(false);
		}
	}

	/**
	 * Set the toggle status of run button and enabled status of everything else
	 */
	private void setEnabledFields(boolean value) {
		runEngineButton.setSelection(value);
		if (value)
			runEngineButton.setToolTipText("Start Europa engine");
		else
			runEngineButton.setToolTipText("Stop Europa engine");
		startHorizonText.setEnabled(value);
		endHorizonText.setEnabled(value);
		resetHorizonButton.setEnabled(value);
		runForStepsText.setEnabled(value);
		runForStepsButton.setEnabled(value);

		if (!value) {
			stepCountLabel.setText("No info.");
			timeSpentLabel.setText("No info.");
		}
	}

	/** Launch configuration tells us field values */
	public void setFields(File nddl, File config, int horizonStart,
			int horizonEnd) {
		this.modelFile = nddl;
		this.configFile = config;
		this.startHorizonText.setText(String.valueOf(horizonStart));
		this.endHorizonText.setText(String.valueOf(horizonEnd));
	}

	/** Run for N steps button listener */
	protected void doRunForNSteps() {
		int count;
		try {
			startOfRun = System.currentTimeMillis();
			count = new Integer(runForStepsText.getText());
		} catch (NumberFormatException e) {
			showMessage(e.getMessage());
			return;
		}

		model.stepN(count, true);
	}

	/** Button to start/stop the engine is hit */
	protected void doEngineButtonPressed(SelectionEvent evt) {
		// The state of the button is changed, just read it
		if (runEngineButton.getSelection()) {
			// Need to run
			if (model.isConfigured())
				model.shutdown();
			int horizonStart, horizonEnd;
			try {
				horizonStart = new Integer(startHorizonText.getText());
			} catch (NumberFormatException e) {
				horizonStart = NddlConfigurationFields.DEF_HORIZON_START;
			}
			try {
				horizonEnd = new Integer(endHorizonText.getText());
			} catch (NumberFormatException e) {
				horizonEnd = NddlConfigurationFields.DEF_HORIZON_END;
			}
			model.configure(modelFile, configFile, horizonStart, horizonEnd);
		} else {
			model.shutdown();
		}
		updateState();
	}
}