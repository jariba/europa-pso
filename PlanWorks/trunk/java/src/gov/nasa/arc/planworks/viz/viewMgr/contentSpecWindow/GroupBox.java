package gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JPanel;

import javax.swing.JFrame;
import java.awt.Container;

import gov.nasa.arc.planworks.mdi.MDIInternalFrame;

public class GroupBox extends JPanel implements ContentSpecGroup
{
  protected ArrayList elements;
  protected MDIInternalFrame window;
  public GroupBox(MDIInternalFrame window)
    {
      this.window = window;
      elements = new ArrayList();
      GridBagLayout gridBag = new GridBagLayout();
      setLayout(gridBag);
    }
  public void add(ContentSpecElement element)
    {
      super.add((Component)element);
      elements.add(element);
      invalidate();
      validate();
      repaint();
      window.pack();
    }
  public void remove(SpecBox element)
    {
      super.remove(element);
      elements.remove(elements.indexOf(element));
      invalidate();
      validate();
      repaint();
      window.pack();
    }
  public List getValues() throws NullPointerException, IllegalArgumentException
    {
      if(elements.size() == 0)
        return null;
      ArrayList retval = new ArrayList();
      for(int i = 0; i < elements.size(); i++)
        {
          Collection c = ((ContentSpecElement)elements.get(i)).getValue();
          if(c == null)
            continue;
          retval.addAll(c);
        }
      if(retval.size() == 0)
        return null;
      return retval;
    }
}
