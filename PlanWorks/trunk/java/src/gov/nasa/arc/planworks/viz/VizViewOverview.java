// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VizViewOverview.java,v 1.1 2003-11-18 23:54:15 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17nov03
//

package gov.nasa.arc.planworks.viz;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.Overview;


/**
 * <code>VizViewOverview</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *               NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VizViewOverview extends Overview {

  private String overviewTitle; // key for viewSet hash map
  private VizView vizView;

  /**
   * <code>VizViewOverview</code> - constructor 
   *
   * @param vizView - <code>VizView</code> - 
   */
  public VizViewOverview( String overviewTitle, VizView vizView) {
    super();
    this.overviewTitle = overviewTitle;
    this.vizView = vizView;
  }

  /**
   * <code>getTitle</code> - key for viewSet hash map
   *
   * @return - <code>String</code> - 
   */
  public String getTitle() {
    return overviewTitle;
  }

  /**
   * <code>getVizView</code>
   *
   * @return - <code>VizView</code> - 
   */
  public VizView getVizView() {
    return vizView;
  }

  // when the Overview window is no longer needed, make sure the observed view doesn't keep
  // any references to this view
//   public void removeNotify()
//   {
//     removeListeners();
//     myObserved = null;
//     super.removeNotify();
//   }

  /**
   * I believe the problem may be related to focus changes in the
   * JDesktopPane.  When focus changes between different JInternalFrames on
   * the desktop it appears that the parent component of the JInternalFrame
   * changes, so momentarily it has no parent.  This causes
   * JGoOverview.removeNotify() to be called which sets the observed JGoView
   * to null:
   *
   * You should subclass JGoOverview and override this method to do nothing.
   * Then make sure these operations are performed when the JInternalFrame
   * holdiong the JGoOverview is closed.
   */
  public void removeNotify()
  {
    // System.err.println( "VizViewOverview.removeNotify");
    // System.err.println( "myObserved " + getObserved());
//     removeListeners();
//     myObserved = null;
//     super.removeNotify();
  }  

  /**
   * <code>removeNotifyFromViewSet</code>
   *
   */
  public void removeNotifyFromViewSet() {
    OverviewRectangle overviewRect = getOverviewRect();
    JGoView observed = getObserved();
    System.err.println( "removeNotifyFromViewSet");
    if (observed != null && overviewRect != null) {
      observed.getDocument().removeDocumentListener(this);
      observed.removeViewListener(overviewRect);
      observed.getCanvas().removeComponentListener(overviewRect);
    }
  }

} // end class VizViewOverview

