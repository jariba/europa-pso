package org.ops.ui.schemabrowser.swt;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label and icon provider for the SWT version of schema browser
 * 
 * @author Tatiana Kichkaylo
 */
public class SchemaLabelProvider extends LabelProvider {
	@Override
	public String getText(Object obj) {
		return obj.toString();
	}

	// public Image getImage(Object obj) {
	// String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
	// return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	// }
}
