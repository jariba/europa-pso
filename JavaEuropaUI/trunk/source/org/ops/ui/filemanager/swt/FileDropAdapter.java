package org.ops.ui.filemanager.swt;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * Drop adapter that accepts files for the file manager
 * 
 * @author Tatiana Kichkaylo
 */
public class FileDropAdapter extends ViewerDropAdapter {
	private FileManager manager;

	protected FileDropAdapter(Viewer viewer, FileManager fileManager) {
		super(viewer);
		this.manager = fileManager;
	}

	@Override
	public boolean performDrop(Object data) {
		if (data instanceof String[]) {
			String[] d = (String[]) data;
			ArrayList<File> files = new ArrayList<File>();
			for (String s : d) {
				files.add(new File(s));
			}
			manager.addFiles(files);
		} else
			// TODO use the logger for error messages
			System.out.println("In FileDropAdapter got data of unsupported type " + data);
		return false;
	}

	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		return FileTransfer.getInstance().isSupportedType(transferType);
	}
}
