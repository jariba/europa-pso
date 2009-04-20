package org.ops.ui.filemanager.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.ops.ui.europaplugin.Activator;
import org.ops.ui.filemanager.model.FileModel;
import org.ops.ui.filemanager.model.FileModelListener;

/**
 * This view maintains the list of files to be loaded into Europa DB. Add files
 * by dragging them from the navigator. Remove files by selecting and then
 * hitting delete (from the toolbar or context menu). Reload the DB using the
 * action from the toolbar/context menu
 * 
 * @author Tatiana Kichkaylo
 */

public class FileManager extends ViewPart implements FileModelListener {
	private FileModel model;

	private TableViewer viewer;
	private Action deleteAction, reloadAction, doubleClickAction;

	// Tags for reading/writing memento
	private static final String FILE_LIST_TAG = "EuropaFileModel";
	private static final String FILE_TAG = "EuropaFile";

	/** Content provider that simply exposes the list from the model */
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return model.toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FILE);
		}
	}

	/**
	 * Obtains the model from the Activator singleton and reads files from the
	 * Memento
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		// Parent class ignores memento, but can set some defaults
		super.init(site);
		model = Activator.getDefault().getFileModel();
		model.addListener(this);
		if (memento == null)
			return;
		IMemento m = memento.getChild(FILE_LIST_TAG);
		if (m == null)
			return;
		ArrayList<File> files = new ArrayList<File>();
		for (IMemento c : m.getChildren(FILE_TAG)) {
			File f = new File(c.getTextData());
			if (!f.exists()) {
				// TODO use the logger for error messages
				System.out.println("Got non-existing file? " + f);
			} else
				files.add(f);
		}
		model.addFiles(files);
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		IMemento mem = memento.createChild(FILE_LIST_TAG);
		for (File f : model) {
			mem.createChild(FILE_TAG).putTextData(f.getAbsolutePath());
		}
	}

	/** Create and initialize the viewer */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance(),
				TextTransfer.getInstance() };
		viewer
				.addDropSupport(ops, transfers, new FileDropAdapter(viewer,
						this));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				FileManager.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(deleteAction);
		manager.add(reloadAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(deleteAction);
		manager.add(reloadAction);
	}

	private void makeActions() {
		deleteAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (sel.size() == 0)
					return;
				ArrayList<File> files = new ArrayList<File>();
				for (Iterator<?> it = sel.iterator(); it.hasNext();) {
					Object f = it.next();
					if (f instanceof File)
						files.add((File) f);
				}
				if (!files.isEmpty())
					model.removeFiles(files);
			}
		};
		deleteAction.setText("Delete");
		deleteAction.setToolTipText("Delete selected files from the model");
		deleteAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_ELCL_REMOVE));

		reloadAction = new Action() {
			public void run() {
				model.reload();
			}
		};
		reloadAction.setText("Reload");
		reloadAction
				.setToolTipText("Reload the database using all files in the list");
		reloadAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_ELCL_SYNCED));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (sel.size() == 0)
					return;

				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();

				for (Iterator<?> it = sel.iterator(); it.hasNext();) {
					Object f = it.next();
					if (f instanceof File) {
						IFileStore fileStore = EFS.getLocalFileSystem()
								.getStore(((File) f).toURI());
						try {
							IDE.openEditorOnFileStore(page, fileStore);
						} catch (PartInitException e) {
							showMessage("Cannot open " + f);
						}

					} else {
						System.out.println("Selected thing not a file? " + f);
					}
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Europa File List", message);
	}

	/** Passing the focus request to the viewer's control. */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/** FileDropAdapter tells us new files have been added */
	public void addFiles(Collection<File> files) {
		model.addFiles(files);
	}

	/** The database was reloaded, need to update the view */
	public void databaseReloaded() {
		// This method may be called from init, when viewer does not yet exist
		if (viewer != null)
			viewer.refresh();
	}
}