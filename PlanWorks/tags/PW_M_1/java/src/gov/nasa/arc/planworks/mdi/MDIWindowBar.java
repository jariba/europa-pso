package gov.nasa.arc.planworks.mdi;

import javax.swing.JButton;

public interface MDIWindowBar
{
    void notifyDeleted(MDIFrame frame);
    void add(JButton button);
}
