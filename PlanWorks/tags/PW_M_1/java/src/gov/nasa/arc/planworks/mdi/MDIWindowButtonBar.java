package gov.nasa.arc.planworks.mdi;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Dimension;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;


public class MDIWindowButtonBar extends JToolBar implements MDIWindowBar
{
    private ButtonGroup buttonGroup = null;
    private int minWidth, maxWidth;

    public MDIWindowButtonBar(int minWidth, int maxWidth)
    {
	buttonGroup = new ButtonGroup();
	setFloatable(false);
	this.minWidth = minWidth;
	this.maxWidth = maxWidth;
	addComponentListener(new WindowButtonBarListener(this));
    }
    public void add(JButton button)
    {
	buttonGroup.add(button);
	super.add(button);
	button.setSelected(true);
	resizeButtons();
    }
    public void add(AbstractButton button)
    {
	buttonGroup.add(button);
	super.add(button);
	button.setSelected(true);
	resizeButtons();
    }
    public void remove(AbstractButton button)
    {
	super.remove(button);
	buttonGroup.remove(button);
	resizeButtons();
	repaint();
    }
    public Enumeration getElements()
    {
	return buttonGroup.getElements();
    }
    //this only works with jdk > 1.2
    public int getButtonCount()
    {
	return buttonGroup.getButtonCount();
    }
    private void resizeButtons()
    {
	final float exactButtonWidth = getCurrentButtonWidth();
	SwingUtilities.invokeLater(new Runnable()
	    {
		public void run()
		{
		    //JToggleButton b = null;
		    JComponent b = null;
		    Enumeration e = getElements();
		    float currentButtonXLocation = 0.0f;
		    while(e.hasMoreElements())
			{
			    //b = (JToggleButton) e.nextElement();
			    b = (JComponent) e.nextElement();
			    int buttonWidth = 
				Math.round(currentButtonXLocation + 
					   exactButtonWidth) - 
				Math.round(currentButtonXLocation);
			    assignWidth(b, buttonWidth);
			    currentButtonXLocation += exactButtonWidth;
			}
		    revalidate();
		}
	    });
    }
    private float getCurrentButtonWidth()
    {
	int width = getWidth() - getInsets().left - getInsets().right;
	float buttonWidth = ((width <= 0) ? maxWidth : width);
	int numButtons = getButtonCount();
	if(numButtons > 0)
	    buttonWidth /= numButtons;
	if(buttonWidth < minWidth)
	    buttonWidth = minWidth;
	else if(buttonWidth > maxWidth)
	    buttonWidth = maxWidth;
	return buttonWidth;
    }
    //private void assignWidth(JToggleButton b, int buttonWidth)
    private void assignWidth(JComponent b, int buttonWidth)
    {
	b.setMinimumSize(new Dimension(buttonWidth - 2, 
				       b.getPreferredSize().height));
	b.setPreferredSize(new Dimension(buttonWidth,
					 b.getPreferredSize().height));
	Dimension newSize = b.getPreferredSize();
	b.setMaximumSize(newSize);
	b.setSize(newSize);
    }
    public void notifyDeleted(MDIFrame frame)
    {
	JButton button = frame.getButton();
	remove(button);
    }
    class WindowButtonBarListener implements ComponentListener
    {
	private MDIWindowButtonBar bar = null;

	public WindowButtonBarListener(MDIWindowButtonBar bar)
	{
	    this.bar = bar;
	}
	public void componentResized(ComponentEvent e)
	{
	    bar.resizeButtons();
	}
	public void componentShown(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentHidden(ComponentEvent e){}
    }
}
