// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: TransactionView.java,v 1.4 2003-10-25 00:58:19 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 10oct03
//

package gov.nasa.arc.planworks.viz.partialPlan.transaction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.viz.TransactionContentView;
import gov.nasa.arc.planworks.viz.TransactionHeaderView;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;
import gov.nasa.arc.planworks.viz.util.TransactionIdComparator;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;


/**
 * <code>TransactionView</code> - render a planning sequence step's transactions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class TransactionView extends PartialPlanView {

  private PwPartialPlan partialPlan;
  private PwPlanningSequence planSequence;
  private List transactionList; // element PwTransaction
  private int stepNumber;

  private long startTimeMSecs;
  private ViewSet viewSet;
  private TransactionHeaderView headerJGoView;
  private TransactionHeaderPanel transactionHeaderPanel;
  private TransactionContentView contentJGoView;
  private JGoDocument jGoDocument;
  private List transactionJGoTextList; // element JGoText


  /**
   * <code>TransactionView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public TransactionView( ViewableObject partialPlan,  ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    this.partialPlan = (PwPartialPlan) partialPlan;
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;

    String partialPlanUrl = this.partialPlan.getUrl();
    String seqUrl =
      partialPlanUrl.substring( 0,
                                partialPlanUrl.lastIndexOf
                                ( System.getProperty( "file.separator")));
    try {
      planSequence = PlanWorks.planWorks.currentProject.getPlanningSequence( seqUrl);
    } catch (ResourceNotFoundException rnfExcep) {
      int index = rnfExcep.getMessage().indexOf( ":");
      JOptionPane.showMessageDialog
        (PlanWorks.planWorks, rnfExcep.getMessage().substring( index + 1),
         "Resource Not Found Exception", JOptionPane.ERROR_MESSAGE);
      System.err.println( rnfExcep);
      rnfExcep.printStackTrace();
    }

    transactionList = planSequence.getTransactionsList( this.partialPlan.getId());
    Collections.sort( transactionList, new TransactionIdComparator());

    stepNumber = this.partialPlan.getStepNumber();

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

    String query = null;
    headerJGoView = new TransactionHeaderView( transactionList, query, this);
    headerJGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    headerJGoView.validate();
    headerJGoView.setVisible( true);

    transactionHeaderPanel.add( headerJGoView, BorderLayout.NORTH);
    add( transactionHeaderPanel, BorderLayout.NORTH);

    contentJGoView = new TransactionContentView( transactionList, headerJGoView,
                                                 partialPlan, this);
    contentJGoView.getHorizontalScrollBar().addAdjustmentListener( new ScrollBarListener());
    add( contentJGoView, BorderLayout.NORTH);
    contentJGoView.validate();
    contentJGoView.setVisible( true);

    this.setVisible( true);

    expandViewFrame( this.getClass().getName(),
                     (int) headerJGoView.getDocumentSize().getWidth(),
                     (int) (headerJGoView.getDocumentSize().getHeight() +
                            contentJGoView.getDocumentSize().getHeight()));

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
   * <code>ScrollBarListener</code> - keep both headerJGoView & contentJGoView aligned,
   *                                  when user moves one scroll bar
   *
   */
  class ScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep both headerJGoView & contentJGoView aligned,
     *                                  when user moves one scroll bar
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
     *
     * <code>getMinimumSize</code>
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMinimumSize() {
      return new Dimension( (int) TransactionView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight() +
                            ViewConstants.TIMELINE_VIEW_Y_INIT);
    }

    /**
     * <code>getMaximumSize</code>
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) TransactionView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight() +
                            ViewConstants.TIMELINE_VIEW_Y_INIT);
    }

    /**
     * <code>getPreferredSize</code> - determine initial size
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getPreferredSize() {
      return new Dimension( (int) TransactionView.this.getSize().getWidth(),
                            (int) headerJGoView.getDocumentSize().getHeight() +
                            (int) headerJGoView.getHorizontalScrollBar().getSize().getHeight() +
                            ViewConstants.TIMELINE_VIEW_Y_INIT);
    }
  } // end class TransactionHeaderPanel



} // end class TransactionView

