// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: VariableQueryView.java,v 1.3 2004-02-03 20:44:01 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 17dec03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceQuery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.viz.VariableQueryContentView;
import gov.nasa.arc.planworks.viz.VariableQueryHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;
import gov.nasa.arc.planworks.viz.util.VariableQueryComparatorAscending;


/**
 * <code>VariableQueryView</code> - render the unbound variable
 *                           results of a sequence query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class VariableQueryView extends SequenceView {

  private PwPlanningSequence planSequence;
  private List unboundVariableList; // element PwVariableQuery
  private String query;
  private SequenceQueryWindow sequenceQueryWindow;
  private MDIInternalFrame unboundVariableQueryFrame;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private VariableQueryHeaderView headerJGoView;
  private VariableQueryHeaderPanel unboundVariableHeaderPanel;
  private VariableQueryContentView contentJGoView;
  private JGoDocument jGoDocument;


  /**
   * <code>VariableQueryView</code> - constructor 
   *
   * @param unboundVariableList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param sequenceQueryWindow - <code>JPanel</code> - 
   * @param unboundVariableQueryFrame - <code>MDIInternalFrame</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public VariableQueryView( List unboundVariableList, String query,
                                   ViewableObject planSequence, ViewSet viewSet,
                                   JPanel sequenceQueryWindow,
                                   MDIInternalFrame unboundVariableQueryFrame,
                                   long startTimeMSecs) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.unboundVariableList = unboundVariableList;
    Collections.sort( unboundVariableList,
                      new VariableQueryComparatorAscending
                      ( ViewConstants.QUERY_VARIABLE_STEP_NUM_HEADER));
    this.query = query;
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;
    this.sequenceQueryWindow = (SequenceQueryWindow) sequenceQueryWindow;
    this.unboundVariableQueryFrame = unboundVariableQueryFrame;
    this.startTimeMSecs = startTimeMSecs;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    SwingUtilities.invokeLater( runInit);
  } // end constructor

 
  Runnable runInit = new Runnable() {
      public void run() {
        init();
      }
    };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoView exists and is part of a visible window".
   *    called by componentShown method on the JFrame
   *    JGoView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    unboundVariableHeaderPanel = new VariableQueryHeaderPanel();
    unboundVariableHeaderPanel.setLayout( new BoxLayout( unboundVariableHeaderPanel, BoxLayout.Y_AXIS));
    headerJGoView = new VariableQueryHeaderView( unboundVariableList, query, this);
    headerJGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    headerJGoView.validate();
    headerJGoView.setVisible( true);
    unboundVariableHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( unboundVariableHeaderPanel, BorderLayout.NORTH);

    contentJGoView = new VariableQueryContentView( unboundVariableList, query, headerJGoView,
                                                 planSequence, this);
    contentJGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    add( contentJGoView, BorderLayout.SOUTH);
    contentJGoView.validate();
    contentJGoView.setVisible( true);

    this.setVisible( true);

    int maxViewWidth = (int) headerJGoView.getDocumentSize().getWidth();
    int maxViewHeight = (int) ( headerJGoView.getDocumentSize().getHeight() +
                                // contentJGoView.getDocumentSize().getHeight());
                                // keep contentJGoView small
                                (ViewConstants.INTERNAL_FRAME_X_DELTA));
    unboundVariableQueryFrame.setSize
      ( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
        maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
    int maxQueryFrameY =
      (int) (sequenceQueryWindow.getSequenceQueryFrame().getLocation().getY() +
             sequenceQueryWindow.getSequenceQueryFrame().getSize().getHeight());
    int delta = Math.min( ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4 *
                          sequenceQueryWindow.getQueryResultFrameCnt(),
                          (int) (PlanWorks.getPlanWorks().getSize().getHeight() -
                                 maxQueryFrameY -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    unboundVariableQueryFrame.setLocation
      ( ViewConstants.INTERNAL_FRAME_X_DELTA + delta, maxQueryFrameY + delta);
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
  } // end init


  /**
   * <code>getVariableQueryContentView</code>
   *
   * @return - <code>VariableQueryContentView</code> - 
   */
  public VariableQueryContentView getVariableQueryContentView() {
    return contentJGoView;
  }

  /**
   * <code>ScrollBarListener</code> - keep both headerJGoView & contentJGoView aligned,
   *                                  when user moves one scroll bar
   *
   */
  class ScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep headerJGoView &
     *                                  contentJGoView aligned, when user moves one 
     *                                  scroll bar
     *
     * @param event - <code>AdjustmentEvent</code> - 
     */
    public void adjustmentValueChanged( AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
//         System.err.println( "adjustmentValueChanged " + source.getValue());
//         System.err.println( "headerJGoView " +
//                             headerJGoView.getHorizontalScrollBar().getValue());
//         System.err.println( "contentJGoView " +
//                             contentJGoView.getHorizontalScrollBar().getValue());
        int newPostion = source.getValue();
        if (newPostion != headerJGoView.getHorizontalScrollBar().getValue()) {
          headerJGoView.getHorizontalScrollBar().setValue( newPostion);
        } else if (newPostion != contentJGoView.getHorizontalScrollBar().getValue()) {
          contentJGoView.getHorizontalScrollBar().setValue( newPostion);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class ScrollBarListener 


  /**
   * <code>VariableQueryHeaderPanel</code> - require unboundVariable header view panel
   *                                       to be of fixed height
   *
   */
  class VariableQueryHeaderPanel extends JPanel {

    /**
     * <code>VariableQueryHeaderPanel</code> - constructor 
     *
     */
    public VariableQueryHeaderPanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code> - keep size during resizing
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMinimumSize() {
     return new Dimension( (int) VariableQueryView.this.getSize().getWidth(),
                           (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getMaximumSize</code> - keep size during resizing
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) VariableQueryView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getPreferredSize</code> - determine initial size
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getPreferredSize() {
      return new Dimension( (int) VariableQueryView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }
  } // end class VariableQueryHeaderPanel




} // end class VariableQueryView

