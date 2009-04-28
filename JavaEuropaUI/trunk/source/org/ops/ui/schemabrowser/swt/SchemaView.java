package org.ops.ui.schemabrowser.swt;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.ops.ui.europaplugin.Activator;
import org.ops.ui.filemanager.model.FileModelListener;
import org.ops.ui.schemabrowser.model.SchemaSource;

/**
 * Europa schema browser - SWT version
 * 
 * @author Tatiana Kichkaylo
 */
public class SchemaView extends ViewPart implements FileModelListener {
	private TreeViewer viewer;
	private SchemaSource model;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		// Parent class ignores memento, but can set some defaults
		super.init(site);
		Activator activator = Activator.getDefault();
		model = new SchemaSource(activator.getEngine());
		activator.getFileModel().addListener(this);
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

	public void databaseReloaded() {
		reloadView();
	}

	private void reloadView() {
		SchemaContentProvider cProvider = (SchemaContentProvider) viewer
				.getContentProvider();
		cProvider.initialize();
		viewer.refresh();
	}
}
