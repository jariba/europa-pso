package gov.nasa.arc.planworks.mdi;

import javax.swing.JButton;
import javax.swing.JMenu;

/**
 *an interface-ala-conor to reveal only this method to the MDIDynamicMenuBar
 */

public interface MDIFrame
{
    public JMenu [] getAssociatedMenus();
    public JButton getButton();
}
