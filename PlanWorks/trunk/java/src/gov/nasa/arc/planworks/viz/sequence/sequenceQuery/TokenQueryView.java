// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TokenQueryView.java,v 1.6 2004-05-04 01:27:22 taylor Exp $
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
import gov.nasa.arc.planworks.viz.TokenQueryContentView;
import gov.nasa.arc.planworks.viz.TokenQueryHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;
import gov.nasa.arc.planworks.viz.util.TokenQueryComparatorAscending;


/**
 * <code>TokenQueryView</code> - render the free token results of a sequence query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TokenQueryView extends SequenceView {

  private PwPlanningSequence planSequence;
  private List freeTokenList; // element PwQueryToken
  private String query;
  private SequenceQueryWindow sequenceQueryWindow;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private TokenQueryHeaderView headerJGoView;
  private TokenQueryHeaderPanel freeTokenHeaderPanel;
  private TokenQueryContentView contentJGoView;
  private JGoDocument jGoDocument;


  /**
   * <code>TokenQueryView</code> - constructor 
   *
   * @param freeTokenList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param sequenceQueryWindow - <code>JPanel</code> - 
   * @param freeTokenQueryFrame - <code>MDIInternalFrame</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   */
  public TokenQueryView( List freeTokenList, String query, ViewableObject planSequence,
                         ViewSet viewSet, JPanel sequenceQueryWindow,
                         MDIInternalFrame freeTokenQueryFrame, long startTimeMSecs,
                         ViewListener viewListener) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.freeTokenList = freeTokenList;
    Collections.sort( freeTokenList,
                      new TokenQueryComparatorAscending
                      ( ViewConstants.QUERY_TOKEN_STEP_NUM_HEADER));
    this.query = query;
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;
    this.sequenceQueryWindow = (SequenceQueryWindow) sequenceQueryWindow;
    viewFrame = freeTokenQueryFrame;
    // for PWTestHelper.findComponentByName
    setName( freeTokenQueryFrame.getTitle());
    this.startTimeMSecs = startTimeMSecs;
    if (viewListener != null) {
      addViewListener( viewListener);
    }

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
    handleEvent( ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    freeTokenHeaderPanel = new TokenQueryHeaderPanel();
    freeTokenHeaderPanel.setLayout( new BoxLayout( freeTokenHeaderPanel, BoxLayout.Y_AXIS));
    headerJGoView = new TokenQueryHeaderView( freeTokenList, query, this);
    headerJGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    headerJGoView.validate();
    headerJGoView.setVisible( true);
    freeTokenHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( freeTokenHeaderPanel, BorderLayout.NORTH);

    contentJGoView = new TokenQueryContentView( freeTokenList, query, headerJGoView,
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
    viewFrame.setSize( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
                       maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
    int maxQueryFrameY =
      (int) (sequenceQueryWindow.getSequenceQueryFrame().getLocation().getY() +
             sequenceQueryWindow.getSequenceQueryFrame().getSize().getHeight());
    int delta = Math.min( ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4 *
                          sequenceQueryWindow.getQueryResultFrameCnt(),
                          (int) (PlanWorks.getPlanWorks().getSize().getHeight() -
                                 maxQueryFrameY -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    viewFrame.setLocation( ViewConstants.INTERNAL_FRAME_X_DELTA + delta,
                           maxQueryFrameY + delta);
    // prevent right edge from going outside the MDI frame
    expandViewFrame( viewFrame,
                     (int) headerJGoView.getDocumentSize().getWidth(),
                     (int) (headerJGoView.getDocumentSize().getHeight() +
                            contentJGoView.getDocumentSize().getHeight()));
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    handleEvent( ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>getTokenQueryContentView</code>
   *
   * @return - <code>TokenQueryContentView</code> - 
   */
  public TokenQueryContentView getTokenQueryContentView() {
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
   * <code>TokenQueryHeaderPanel</code> - require freeToken header view panel
   *                                       to be of fixed height
   *
   */
  class TokenQueryHeaderPanel extends JPanel {

    /**
     * <code>TokenQueryHeaderPanel</code> - constructor 
     *
     */
    public TokenQueryHeaderPanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code> - keep size during resizing
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMinimumSize() {
     return new Dimension( (int) TokenQueryView.this.getSize().getWidth(),
                           (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getMaximumSize</code> - keep size during resizing
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) TokenQueryView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getPreferredSize</code> - determine initial size
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getPreferredSize() {
      return new Dimension( (int) TokenQueryView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }
  } // end class TokenQueryHeaderPanel




} // end class TokenQueryView

