// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: FixedHeightPanel.java,v 1.1 2004-05-21 21:39:12 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 20may04
//

package gov.nasa.arc.planworks.viz.util;

import java.awt.Dimension;
import javax.swing.JPanel;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;


/**
 * <code>FixedHeightPanel</code> - 
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *              NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class FixedHeightPanel extends JPanel {

  private JGoView panelJGoView;
  private VizView vizView;

  /**
   * <code>FixedHeightPanel</code> - constructor 
   *
   * @param panelJGoView - <code>JGoView</code> - 
   * @param vizView - <code>VizView</code> - 
   */
  public FixedHeightPanel( final JGoView panelJGoView, final VizView vizView) {
    super();
    this.panelJGoView = panelJGoView;
    this.vizView = vizView;
  }

  /**
   *
   * <code>getMinimumSize</code>
   * @return - <code>Dimension</code> - 
   */
  public final Dimension getMinimumSize() {
    return new Dimension( (int) vizView.getSize().getWidth(),
                          (int) panelJGoView.getDocumentSize().getHeight() +
                          ViewConstants.TIMELINE_VIEW_Y_INIT);
  }

  /**
   * <code>getMaximumSize</code>
   *
   * @return - <code>Dimension</code> - 
   */
  public final Dimension getMaximumSize() {
    return new Dimension( (int) vizView.getSize().getWidth(),
                          (int) panelJGoView.getDocumentSize().getHeight() +
                          ViewConstants.TIMELINE_VIEW_Y_INIT);
  }

  /**
   * <code>getPreferredSize</code> - determine initial size
   *
   * @return - <code>Dimension</code> - 
   */
  public final Dimension getPreferredSize() {
    return new Dimension( (int) vizView.getSize().getWidth(),
                          (int) panelJGoView.getDocumentSize().getHeight() +
                          ViewConstants.TIMELINE_VIEW_Y_INIT);
  }
} // end class FixedHeightPanel
