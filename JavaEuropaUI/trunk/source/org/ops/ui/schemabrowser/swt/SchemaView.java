package org.ops.ui.schemabrowser.swt;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.ops.ui.main.swt.EuropaPlugin;
import org.ops.ui.schemabrowser.model.SchemaSource;
import org.ops.ui.solver.model.SolverAdapter;
import org.ops.ui.solver.model.SolverModel;

/**
 * Europa schema browser - SWT version
 * 
 * @author Tatiana Kichkaylo
 */
public class SchemaView extends ViewPart {
	public static final String VIEW_ID = "org.ops.ui.schemabrowser.swt.SchemaView";
	private TreeViewer viewer;
	private SchemaSource model;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		// Parent class ignores memento, but can set some defaults
		super.init(site);
		SolverModel smodel = EuropaPlugin.getDefault().getSolverModel();
		model = new SchemaSource(smodel.getEngine());
		smodel.addSolverListener(new SolverAdapter() {
			@Override
			public void solverStarted() {
				reloadView();
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		SchemaContentProvider cProvider = new SchemaContentProvider(model);
		viewer.setContentProvider(cProvider);
		viewer.setLabelProvider(new SchemaLabelProvider());
		viewer.setInput(cProvider.getRootNode());
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void reloadView() {
		SolverModel smodel = EuropaPlugin.getDefault().getSolverModel();
		model.setEngine(smodel.getEngine());
		SchemaContentProvider cProvider = (SchemaContentProvider) viewer
				.getContentProvider();
		cProvider.initialize();
		viewer.refresh();
	}
}
