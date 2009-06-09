package org.ops.ui.solver.swt;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.ops.ui.main.swt.EuropaPlugin;
import org.ops.ui.solver.model.SolverListener;
import org.ops.ui.solver.model.SolverModel;

/**
 * Statistics charts of the solver moved into a separate view.
 * 
 * @author Tatiana Kichkaylo
 */
public class StatisticsChartsView extends ViewPart implements SolverListener {
	public static final String VIEW_ID = "org.ops.ui.solver.swt.StatisticsView";

	/** Solver model, initialized in createPartControl */
	private SolverModel model;

	/* Labels for chart */
	private final String lblTimePerStep = "Time (secs) per Step ",
			lblAvgPerStep = "Avg time (sec) per Step",
			lblStepNumber = "Step Number", lblTimeSec = "Time (secs)",
			lblOpenDecCount = "Open Decision Count",
			lblDecsInPlan = "Decisions in Plan";

	protected XYSeries stepTimeSeries = new XYSeries(lblTimePerStep);
	protected XYSeries stepAvgTimeSeries = new XYSeries(lblAvgPerStep);
	protected XYSeries decisionCntSeries = new XYSeries(lblOpenDecCount);
	protected XYSeries solverDepthSeries = new XYSeries(lblDecsInPlan);

	/** Total running time, seconds */
	private double totalTimeSec = 0;

	@Override
	public void createPartControl(Composite parent) {
		model = EuropaPlugin.getDefault().getSolverModel();
		model.addSolverListener(this);

		FillLayout layout = new FillLayout();
		parent.setLayout(layout);

		XYSeriesCollection data = new XYSeriesCollection();
		data.addSeries(this.stepTimeSeries);
		data.addSeries(this.stepAvgTimeSeries);
		new SolverChartComposite(parent, SWT.BORDER, lblTimePerStep,
				lblStepNumber, lblTimeSec, data, false)
				.setLayoutData(new RowData(200, 200));
		data = new XYSeriesCollection();
		data.addSeries(this.decisionCntSeries);
		new SolverChartComposite(parent, SWT.BORDER, lblOpenDecCount,
				lblStepNumber, lblOpenDecCount, data, false);
		data = new XYSeriesCollection();
		data.addSeries(this.solverDepthSeries);
		new SolverChartComposite(parent, SWT.BORDER, lblDecsInPlan,
				lblStepNumber, lblDecsInPlan, data, false);
	}

	@Override
	public void dispose() {
		model.removeSolverListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		// Nothing
	}

	public void afterOneStep(long time) {
		int stepCnt = model.getStepCount();
		double secs = time / 1000.0;
		totalTimeSec += secs;
		ArrayList<String> flaws = model.getFlawsAtStep(stepCnt);
		int decs = flaws == null ? 0 : flaws.size();
		solverDepthSeries.add(stepCnt, model.getDepth());
		stepTimeSeries.add(stepCnt, secs);
		stepAvgTimeSeries.add(stepCnt, totalTimeSec / stepCnt);
		decisionCntSeries.add(stepCnt, decs);
	}

	public void afterStepping() {
		// Nothing
	}

	public void beforeStepping() {
		// Nothing
	}

	public void solverStarted() {
		totalTimeSec = 0;
	}

	public void solverStopped() {
		clearSeries();
	}

	private void clearSeries() {
		stepTimeSeries.clear();
		stepAvgTimeSeries.clear();
		decisionCntSeries.clear();
		solverDepthSeries.clear();
	}
}
