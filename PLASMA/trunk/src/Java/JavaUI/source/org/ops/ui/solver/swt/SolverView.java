package org.ops.ui.solver.swt;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.ops.ui.main.swt.CommonImages;
import org.ops.ui.main.swt.EuropaPlugin;
import org.ops.ui.main.swt.NddlLauncher;
import org.ops.ui.solver.model.SolverListener;
import org.ops.ui.solver.model.SolverModel;
import org.ops.ui.solver.model.TimeFormatHelper;

/**
 * Solver view, Eclipse version.
 * 
 * @author Tatiana Kichkaylo
 */

public class SolverView extends ViewPart implements SolverListener,
		SolverModelView {
	public static final String VIEW_ID = "org.ops.ui.solver.swt.SolverView";
	public static final String MEMENTO_LAUNCH = "Europa.SolverView.LaunchName";

	/** Message strings. Should probably move this into plugin resources */
	private static final String TOOLTIP_START_ENGINE = "Start Europa engine";
	private static final String TOOLTIP_STOP_ENGINE = "Stop Europa engine";
	/** Attribute keys for storing time and step counts in models */
	private static final String TIME_LABEL = "Time searching";
	private static final String STEP_LABEL = "Step count";

	/** Minimum width of text fields */
	private static final int TEXT_WIDTH = 50;

	/** List of all NDDL launches */
	private Combo launchList;
	/** Models corresponding to strings to launchList */
	private ArrayList<SolverModelSWT> launches = new ArrayList<SolverModelSWT>();
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

	/** Solver model we are currently displaying */
	private SolverModelSWT model = null;

	/** Start of N step run. Used to get statistics labels */
	private long startOfRun;

	/** Remember the parent widget so we can force layout when labels change */
	private Composite widget;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		// Parent class ignores memento, but can set some defaults
		super.init(site);
		if (memento == null)
			return;
		IMemento m = memento.getChild(MEMENTO_LAUNCH);
		if (m == null)
			return;
		String name = m.getTextData();
		if (name == null)
			return;
		System.out.println("Will locate " + name);
		try {
			ILaunchConfiguration[] allcfg = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations();
			ILaunchConfiguration cfg = null;
			for (int i = 0; i < allcfg.length; i++)
				if (name.equals(allcfg[i].getName())) {
					cfg = allcfg[i];
					break;
				}
			if (cfg == null) {
				EuropaPlugin.getDefault().logError(
						"Cannot find old launch configuration " + name
								+ " when restarting Solver view");
				return;
			}
			// Temporary tell the launcher not to start the engine
			NddlLauncher.startModel = false;
			cfg.launch("run", null);
		} catch (CoreException e) {
			EuropaPlugin.getDefault().logError(
					"Cannot reload launch "
							+ "configuration when restarting Solver view", e);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		if (model == null)
			return; // nothing to do
		String name = model.getLaunch().getLaunchConfiguration().getName();
		if (name == null) {
			EuropaPlugin.getDefault().logError(
					"Cannot get launch name out of "
							+ "solver model on saving Solver view");
		}
		memento.createChild(MEMENTO_LAUNCH).putTextData(name);
	}

	/** Create and initialize the viewer */
	@Override
	public void createPartControl(Composite parent) {
		widget = parent;
		parent.setLayout(new GridLayout(4, false));

		new Label(parent, SWT.NONE).setText("Launches");
		launchList = new Combo(parent, SWT.READ_ONLY);
		launchList.addSelectionListener(new SelectionListener() {
			private void updateLaunchSelection() {
				int idx = launchList.getSelectionIndex();
				assert (idx < launches.size());
				SolverModelSWT md = (idx < 0) ? null : launches.get(idx);
				// Notify all relevant views, including this one
				SolverModelSWT.makeActive(md);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateLaunchSelection();
			}
		});
		GridData data = new GridData();
		data.minimumWidth = 200;
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		launchList.setLayoutData(data);
		updateLaunchList();

		new Label(parent, SWT.NONE).setText("File: ");
		modelFileLabel = new Label(parent, SWT.BOLD);
		data = new GridData();
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		modelFileLabel.setLayoutData(data);

		// Make bold font for labels
		FontData[] fontData = modelFileLabel.getFont().getFontData();
		for (FontData d : fontData)
			d.setStyle(SWT.BOLD);
		Font boldFont = new Font(parent.getDisplay(), fontData);

		modelFileLabel.setFont(boldFont);
		modelFileLabel.setText("Fixme");

		runEngineButton = new Button(parent, SWT.TOGGLE);
		runEngineButton.setImage(EuropaPlugin.getDefault().getImageRegistry()
				.get(CommonImages.IMAGE_EUROPA));
		runEngineButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				doEngineButtonPressed(evt);
			}
		});

		new Label(parent, SWT.NONE).setText("Horizon");
		startHorizonText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.minimumWidth = TEXT_WIDTH;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		startHorizonText.setLayoutData(data);
		endHorizonText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.minimumWidth = TEXT_WIDTH;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		endHorizonText.setLayoutData(data);
		resetHorizonButton = new Button(parent, SWT.PUSH);
		resetHorizonButton.setImage(EuropaPlugin.getDefault()
				.getImageRegistry().get(CommonImages.IMAGE_HORIZON));
		resetHorizonButton.setToolTipText("Reset horizon");
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

		new Label(parent, SWT.NONE).setText("Run for ");
		runForStepsText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		runForStepsText.setText("100");
		data = new GridData();
		data.minimumWidth = TEXT_WIDTH;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.FILL;
		runForStepsText.setLayoutData(data);
		runForStepsButton = new Button(parent, SWT.PUSH);
		runForStepsButton.setImage(EuropaPlugin.getDefault().getImageRegistry()
				.get(CommonImages.IMAGE_RUN));
		runForStepsButton.setToolTipText("Run for N steps");
		runForStepsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				doRunForNSteps();
			}
		});

		Composite row = new Composite(parent, SWT.NONE);
		row.setLayout(new RowLayout());
		new Label(row, SWT.NONE).setText("Step count: ");
		stepCountLabel = new Label(row, SWT.BOLD);
		stepCountLabel.setFont(boldFont);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.CENTER;
		row.setLayoutData(data);
		row = new Composite(parent, SWT.NONE);
		row.setLayout(new RowLayout());
		new Label(row, SWT.NONE).setText("Run time: ");
		timeSpentLabel = new Label(row, SWT.BOLD);
		timeSpentLabel.setFont(boldFont);
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.CENTER;
		row.setLayoutData(data);

		updateState();
	}

	/** Refresh the set of NDDL launches. Preserve selection, if possible */
	public void updateLaunchList() {
		// Remember selected launch
		int idx = launchList.getSelectionIndex();
		SolverModel old = (idx < 0) ? null : launches.get(idx);

		// Reset the list, get all launches
		this.launches.clear();
		ArrayList<String> labels = new ArrayList<String>();
		ILaunch[] all = DebugPlugin.getDefault().getLaunchManager()
				.getLaunches();
		for (int i = 0; i < all.length; i++) {
			if (all[i].getProcesses().length < 1)
				continue; // should not happen, but still
			IProcess pr = all[i].getProcesses()[0];
			if (pr instanceof SolverModelSWT) {
				launches.add((SolverModelSWT) pr);
				labels.add(all[i].getLaunchConfiguration().toString());
			}
		}
		launchList.setItems(labels.toArray(new String[labels.size()]));

		// Reapply the selection
		idx = this.launches.indexOf(old);
		if (idx >= 0)
			launchList.select(idx);
	}

	@Override
	public void setFocus() {
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(null, "Europa Solver", message);
	}

	@Override
	public void afterOneStep(long time) {
		String str = TimeFormatHelper.formatTime(System.currentTimeMillis()
				- startOfRun);
		model.setAttribute(TIME_LABEL, str);
		timeSpentLabel.setText(str);
		int stepCnt = model.getStepCount();
		str = Integer.toString(stepCnt);
		model.setAttribute(STEP_LABEL, str);
		stepCountLabel.setText(str);
	}

	@Override
	public void afterStepping() {
		runForStepsButton.setEnabled(true);
		widget.layout();
	}

	@Override
	public void beforeStepping() {
		runForStepsButton.setEnabled(false);
	}

	@Override
	public void solverStarted() {
		// Clean up statistics that might linger from previous run
		assert (model != null); // or it would not be started
		model.setAttribute(TIME_LABEL, null);
		model.setAttribute(STEP_LABEL, null);

		updateState();
		widget.layout();
	}

	@Override
	public void solverStopped() {
		updateState();
		widget.layout();
	}

	public File getModelFile() {
		if (model == null)
			return null;
		return model.getModelFile();
	}

	/** Update state of labels and buttons */
	private void updateState() {
		// Do we have a file
		boolean haveFile = (model != null);

		if (haveFile) {
			modelFileLabel.setText(model.getModelFile().getName());
			startHorizonText.setText(String.valueOf(model.getHorizon()[0]));
			endHorizonText.setText(String.valueOf(model.getHorizon()[1]));
			String str = model.getAttribute(TIME_LABEL);
			if (str != null)
				timeSpentLabel.setText(str);
			else
				timeSpentLabel.setText("");
			str = model.getAttribute(STEP_LABEL);
			if (str != null)
				stepCountLabel.setText(str);
			else
				stepCountLabel.setText("");
		} else {
			modelFileLabel.setText("No model selected. Use Run as NDDL");
			startHorizonText.setText("");
			endHorizonText.setText("");
			timeSpentLabel.setText("");
			stepCountLabel.setText("");
		}

		// Run engine button
		if (haveFile) {
			runEngineButton.setEnabled(true);
			if (!model.isTerminated()) {
				setEnabledFields(true);
			} else {
				setEnabledFields(false);
			}
			if (!model.isTerminated())
				runEngineButton.setToolTipText(TOOLTIP_STOP_ENGINE);
			else
				runEngineButton.setToolTipText(TOOLTIP_START_ENGINE);
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
	@Override
	public void setModel() {
		// Unsubscribe from the old model
		if (this.model != null)
			this.model.removeSolverListener(this);

		// Remember the new one
		this.model = SolverModelSWT.getCurrent();
		if (model != null) {
			model.addSolverListener(this);
		}
		updateState();

		int idx = this.launches.indexOf(model);
		if (idx >= 0)
			launchList.select(idx);
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
		// The button should be disabled when no model, so this method should
		// not be called. Still, out of paranoia...
		if (model == null)
			return;
		// The state of the button is changed, just read it
		if (runEngineButton.getSelection()) {
			// Need to run
			assert (model.isTerminated());
			model.start();
		} else {
			model.terminate();
		}
		updateState();
	}
}