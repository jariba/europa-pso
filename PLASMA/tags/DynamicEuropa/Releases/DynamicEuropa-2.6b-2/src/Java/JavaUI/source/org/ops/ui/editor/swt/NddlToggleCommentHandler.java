package org.ops.ui.editor.swt;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Trigger comment // for the current selection of the currently active editor.
 * 
 * @author Tatiana
 */
public class NddlToggleCommentHandler extends AbstractHandler {
	public static final String COMMENT_TEXT = "//";

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor == null || !(editor instanceof ITextEditor)) {
			// Nothing to do
			return null;
		}
		ITextEditor te = (ITextEditor) editor;
		ISelection sel = te.getSelectionProvider().getSelection();
		if (!(sel instanceof ITextSelection)) {
			IWorkbenchWindow window = HandlerUtil
					.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(window.getShell(), "Comment toggler",
					"NULL selection. Fix me!");
			return null;
		}

		ITextSelection ts = (ITextSelection) sel;
		IDocument doc = te.getDocumentProvider().getDocument(
				te.getEditorInput());
		int startOffset;
		try {
			startOffset = doc.getLineOffset(ts.getStartLine());
		} catch (BadLocationException e1) {
			throw new ExecutionException("Cannot get offset of the selection");
		}
		int endOffset = getNextLineOffset(doc, ts.getStartLine());

		boolean wasCommented;
		try {
			String line = doc.get(startOffset, endOffset - startOffset);
			line = line.trim();
			wasCommented = line.startsWith(COMMENT_TEXT);
		} catch (BadLocationException e) {
			e.printStackTrace();
			throw new ExecutionException("Cannot get text for selection");
		}

		if (wasCommented)
			removeComment(doc, ts.getStartLine(), ts.getEndLine());
		else
			addComment(doc, ts.getStartLine(), ts.getEndLine());

		return null;
	}

	private int getNextLineOffset(IDocument doc, int line) {
		try {
			return doc.getLineOffset(line + 1);
		} catch (Exception e) {
			return doc.getLength();
		}
	}

	private void removeComment(IDocument doc, int start, int end)
			throws ExecutionException {
		for (int l = start; l <= end; l++)
			try {
				int offset = doc.getLineOffset(l);
				int length = getNextLineOffset(doc, l) - offset;
				String text = doc.get(offset, length);
				if (!text.trim().startsWith(COMMENT_TEXT))
					continue;
				int idx = text.indexOf(COMMENT_TEXT);
				if (idx >= 0)
					doc.replace(offset + idx, COMMENT_TEXT.length(), "");
			} catch (BadLocationException e) {
				throw new ExecutionException("Could not put comment in line "
						+ l);
			}
	}

	private void addComment(IDocument doc, int start, int end)
			throws ExecutionException {
		for (int l = start; l <= end; l++)
			try {
				doc.replace(doc.getLineOffset(l), 0, COMMENT_TEXT);
			} catch (BadLocationException e) {
				throw new ExecutionException("Could not put comment in line "
						+ l);
			}
	}
}
