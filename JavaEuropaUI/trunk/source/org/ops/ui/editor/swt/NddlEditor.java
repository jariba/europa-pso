package org.ops.ui.editor.swt;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * NDDL editor with syntax highlighting.
 * 
 * @author Tatiana
 */
public class NddlEditor extends TextEditor {

	private ColorManager colorManager;
	// TODO private NddlOutlinePage outlinePage;
	// TODO private NddlDocumentModel documentModel;

	public NddlEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new NddlConfiguration(colorManager));
		setDocumentProvider(new NddlDocumentProvider());
		// setEditorContextMenuId("org.ops.ui.NddlEditorScope");
		// this.documentModel = new NddlDocumentModel(this);
	}
	
	/**
	 * Create the part control.
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
//		this.getDocumentProvider().getDocument(getEditorInput())
//				.addDocumentListener(this.documentModel);
//		this.documentModel.initializeModel();
//		this.documentModel.updateNow();
	}
	
	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	
	/**
	 * Used by platform to get the OutlinePage and ProjectionSupport adapter.
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 * /
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (this.outlinePage == null) {
				this.outlinePage = new NddlOutlinePage(this);
				this.documentModel.updateOutline();
			}
			return outlinePage;
		}
		return super.getAdapter(required);
	}

	public NddlOutlinePage getOutlinePage() {
		if (this.outlinePage == null) {
			this.outlinePage = new NddlOutlinePage(this);
			// need it? this.documentModel.updateOutline();
		}
		return outlinePage;
	}*/
	
	/**
	 * @return The source viewer of this editor
	 */
	public ISourceViewer getViewer() {
		return getSourceViewer();
	}
}
