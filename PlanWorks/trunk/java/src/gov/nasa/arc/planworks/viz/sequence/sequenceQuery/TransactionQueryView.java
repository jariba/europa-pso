// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionQueryView.java,v 1.6 2004-01-12 19:46:35 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 16oct03
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
import gov.nasa.arc.planworks.viz.TransactionContentView;
import gov.nasa.arc.planworks.viz.TransactionHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants    ;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.contentSpecWindow.sequence.SequenceQueryWindow;
import gov.nasa.arc.planworks.viz.util.TransactionComparatorAscending;


/**
 * <code>TransactionQueryView</code> - render the transaction results of a 
 *                                     sequence query
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionQueryView extends SequenceView {

  private PwPlanningSequence planSequence;
  private List transactionList; // element PwTransaction
  private String query;
  private SequenceQueryWindow sequenceQueryWindow;
  private MDIInternalFrame transactionQueryFrame;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private TransactionHeaderView headerJGoView;
  private TransactionHeaderPanel transactionHeaderPanel;
  private TransactionContentView contentJGoView;
  private JGoDocument jGoDocument;


  /**
   * <code>TransactionQueryView</code> - constructor 
   *
   * @param transactionList - <code>List</code> - 
   * @param query - <code>String</code> - 
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   * @param sequenceQueryWindow - <code>JPanel</code> - 
   * @param transactionQueryFrame - <code>MDIInternalFrame</code> - 
   * @param startTimeMSecs - <code>long</code> - 
   */
  public TransactionQueryView( List transactionList, String query,
                               ViewableObject planSequence,  ViewSet viewSet,
                               JPanel sequenceQueryWindow,
                               MDIInternalFrame transactionQueryFrame,
                               long startTimeMSecs) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.transactionList = transactionList;
    Collections.sort( transactionList,
                      new TransactionComparatorAscending
                      ( ViewConstants.TRANSACTION_KEY_HEADER));
    this.query = query;
    this.planSequence = (PwPlanningSequence) planSequence;
    this.viewSet = (SequenceViewSet) viewSet;
    this.sequenceQueryWindow = (SequenceQueryWindow) sequenceQueryWindow;
    this.transactionQueryFrame = transactionQueryFrame;
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

    transactionHeaderPanel = new TransactionHeaderPanel();
    transactionHeaderPanel.setLayout( new BoxLayout( transactionHeaderPanel, BoxLayout.Y_AXIS));
    headerJGoView = new TransactionHeaderView( transactionList, query, this);
    headerJGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    headerJGoView.validate();
    headerJGoView.setVisible( true);
    transactionHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( transactionHeaderPanel, BorderLayout.NORTH);

    contentJGoView = new TransactionContentView( transactionList, headerJGoView,
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
    transactionQueryFrame.setSize
      ( maxViewWidth + ViewConstants.MDI_FRAME_DECORATION_WIDTH,
        maxViewHeight + ViewConstants.MDI_FRAME_DECORATION_HEIGHT);
    int maxQueryFrameY =
      (int) (sequenceQueryWindow.getSequenceQueryFrame().getLocation().getY() +
             sequenceQueryWindow.getSequenceQueryFrame().getSize().getHeight());
    int delta = Math.min( ViewConstants.INTERNAL_FRAME_X_DELTA_DIV_4 *
                          sequenceQueryWindow.getQueryResultFrameCnt(),
                          (int) (PlanWorks.planWorks.getSize().getHeight() -
                                 maxQueryFrameY -
                                 (ViewConstants.MDI_FRAME_DECORATION_HEIGHT * 2)));
    transactionQueryFrame.setLocation
      ( ViewConstants.INTERNAL_FRAME_X_DELTA + delta, maxQueryFrameY + delta);
    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
  } // end init


  /**
   * <code>getTransactionContentView</code>
   *
   * @return - <code>TransactionContentView</code> - 
   */
  public TransactionContentView getTransactionContentView() {
    return contentJGoView;
  }

  /**
   * <code>getQuery</code>
   *
   * @return - <code>String</code> - 
   */
  public String getQuery() {
    return query;
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
   * <code>TransactionHeaderPanel</code> - require transaction header view panel
   *                                       to be of fixed height
   *
   */
  class TransactionHeaderPanel extends JPanel {

    /**
     * <code>TransactionHeaderPanel</code> - constructor 
     *
     */
    public TransactionHeaderPanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code> - keep size during resizing
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMinimumSize() {
     return new Dimension( (int) TransactionQueryView.this.getSize().getWidth(),
                           (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getMaximumSize</code> - keep size during resizing
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) TransactionQueryView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }

    /**
     * <code>getPreferredSize</code> - determine initial size
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getPreferredSize() {
      return new Dimension( (int) TransactionQueryView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight());
    }
  } // end class TransactionHeaderPanel




} // end class TransactionQueryView

