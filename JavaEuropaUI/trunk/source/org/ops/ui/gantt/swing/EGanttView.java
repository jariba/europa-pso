package org.ops.ui.gantt.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.ops.ui.gantt.model.GanttActivity;
import org.ops.ui.gantt.model.GanttModel;
import org.ops.ui.main.swing.EuropaInternalFrame;
import org.ops.ui.solver.model.SolverAdapter;
import org.ops.ui.solver.model.SolverModel;

import com.egantt.model.drawing.ContextResources;
import com.egantt.model.drawing.DrawingContext;
import com.egantt.model.drawing.DrawingState;
import com.egantt.model.drawing.part.ListDrawingPart;
import com.egantt.model.drawing.state.BasicDrawingState;
import com.egantt.swing.cell.CellState;
import com.egantt.swing.component.ComponentResources;
import com.egantt.swing.component.context.BasicComponentContext;
import com.egantt.swing.component.tooltip.ToolTipState;
import com.egantt.swing.graphics.GraphicsResources;
import com.egantt.swing.table.list.BasicJTableList;

import ext.egantt.drawing.module.BasicPainterModule;
import ext.egantt.drawing.module.CalendarDrawingModule;
import ext.egantt.drawing.module.GradientColorModule;
import ext.egantt.drawing.painter.context.BasicPainterContext;
import ext.egantt.drawing.painter.context.compound.BasicCompoundContext;
import ext.egantt.swing.GanttDrawingPartHelper;
import ext.egantt.swing.GanttEntryHelper;
import ext.egantt.swing.GanttTable;
import ext.egantt.swing.GanttToolBar;

/**
 * Adaptation of the original PSUI Gantt chart based on EGantt library
 * 
 * @author Tatiana Kichkaylo
 */
public class EGanttView extends EuropaInternalFrame {

	private SolverModel solverModel;

	private GanttTable gantt;

	private final String TEXT_PAINTER = "MyTextPainter";

	private GanttContextProvider colorProvider = new GanttContextProvider() {
		public String getContext(GanttActivity activity) {
			if (activity.hasViolation())
				return GradientColorModule.RED_GRADIENT_CONTEXT;
			return GradientColorModule.GREEN_GRADIENT_CONTEXT;
		}
	};

	private BasicPainterContext textPainter = new BasicPainterContext();
	private BasicJTableList tableList = new BasicJTableList();

	private Calendar baseCalendar;

	public EGanttView(SolverModel solverModel) {
		super("Gantt chart");
		this.solverModel = solverModel;

		this.baseCalendar = Calendar.getInstance();
		baseCalendar.set(0, 0, 0, 0, 0, 0);

		textPainter.setPaint(Color.WHITE);
		textPainter.put(TEXT_PAINTER, new Font(null, Font.BOLD, 10));

		{
			BasicComponentContext componentContext = new BasicComponentContext();

			ToolTipState state = new ToolTipState() {
				public String getToolTipText(MouseEvent event,
						CellState cellState) {

					DrawingState drawing = cellState.getDrawing();
					Object key = drawing != null ? drawing.getValueAt(event
							.getPoint()) : null;
					if (key == null)
						return "";
					return key.toString();
				}
			};
			componentContext.put(ComponentResources.TOOLTIP_STATE, state);
			tableList.setRendererComponentContext(componentContext);
		}

		solverModel.addSolverListener(new SolverAdapter() {
			@Override
			public void afterStepping() {
				updateView();
			}
		});
	}

	protected void updateView() {
		System.out.println("Updating Gantt");
		if (gantt != null)
			this.removeAll();

		final String[][] columnNames = { { "Name" }, { "Timeline" } };
		GanttEntryHelper eHelper = new GanttEntryHelper();
		GanttDrawingPartHelper pHelper = new GanttDrawingPartHelper();

		GanttModel model = new GanttModel(solverModel);
		int offset = model.getStart();
		Object[][] data = new Object[model.getResourceCount()][2];
		for (int i = 0; i < model.getResourceCount(); i++) {
			BasicDrawingState activities = pHelper.createDrawingState();
			ListDrawingPart actList = pHelper.createDrawingPart(false);
			ListDrawingPart textLayer = pHelper.createDrawingPart(false);

			List<GanttActivity> resActivities = model.getActivities(i);
			for (GanttActivity act : resActivities) {
				String context = colorProvider.getContext(act);

				Date s = wrap(act.getStartMin() - offset);
				Date e = wrap(act.getEndMin() - offset);
				StringBuffer text = new StringBuffer(act.getText());
				pHelper.createActivityEntry(text, s, e, context, actList);
				pHelper.createActivityEntry(text, s, e,
						BasicPainterModule.BASIC_STRING_PAINTER, TEXT_PAINTER,
						textLayer);
			}

			activities.addDrawingPart(actList);
			activities.addDrawingPart(textLayer);

			data[i][0] = model.getResourceName(i);
			data[i][1] = activities;
		}

		this.gantt = new GanttTable(data, columnNames, tableList);
		this.gantt.getDrawingContext().put(TEXT_PAINTER,
				ContextResources.GRAPHICS_CONTEXT, textPainter);

		TableColumnModel columnModel = gantt.getColumnModel(1);
		TableColumn column = columnModel.getColumn(0);
		column.setHeaderValue(eHelper.createCalendar());
		this.add(gantt, BorderLayout.CENTER);
		this.add(new GanttToolBar(gantt.getViewManager(GanttTable.TIME_AXIS)),
				BorderLayout.SOUTH);

		this.gantt.setTimeRange(wrap(0), wrap(model.getEnd() - offset));
		{
			DrawingContext context = gantt.getDrawingContext();
			BasicCompoundContext gc = (BasicCompoundContext) context.get(
					CalendarDrawingModule.TIMELINE_TOP
							+ CalendarDrawingModule.TEXT_PAINTER,
					ContextResources.GRAPHICS_CONTEXT);
			cleanFormats(gc);
			gc = (BasicCompoundContext) context.get(
					CalendarDrawingModule.TIMELINE_BOTTOM
							+ CalendarDrawingModule.TEXT_PAINTER,
					ContextResources.GRAPHICS_CONTEXT);
			cleanFormats(gc);
		}
	}

	private void cleanFormats(BasicCompoundContext gc) {
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.MINUTE),
				new SimpleDateFormat(" m"));
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.HOUR),
				new SimpleDateFormat(" 'hour' H"));
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.DAY_OF_MONTH),
				new SimpleDateFormat(" day D")); // day of year
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.WEEK_OF_MONTH),
				new SimpleDateFormat(" day D"));
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.WEEK_OF_YEAR),
				new SimpleDateFormat(" day D"));
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.MONTH),
				new SimpleDateFormat(" day D"));
		gc.put(GraphicsResources.FORMAT, new Integer(Calendar.YEAR),
				new SimpleDateFormat(" day D"));
	}

	private Date wrap(int value) {
		Calendar cal = (Calendar) baseCalendar.clone();
		cal.add(Calendar.MINUTE, value);
		return cal.getTime();
	}

	@Override
	public Dimension getFavoriteSize() {
		return new Dimension(600, 400);
	}
}
