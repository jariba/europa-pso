// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 
// $Id: ViewTransactionsFrame.java,v 1.2 2003-10-09 22:07:45 taylor Exp $
//
// PlanWorks
//
// Will Taylor -- started 08oct03
//

package gov.nasa.arc.planworks.viz.sequence;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwTransaction;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.Utilities;


/**
 * <code>ViewTransactionsFrame</code> -  standalone frame for displaying
 *                planning sequence step's transactions
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *           NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ViewTransactionsFrame extends JFrame {

  private static final int FRAME_WIDTH = 400;
  private static final int FRAME_HEIGHT = 200;

  private PwPartialPlan partialPlan;
  private List transactionList; // element PwTransaction
  private int stepNumber;
  private SequenceView sequenceView;

  private TransactionsJGoView jGoView;
  private JGoDocument jGoDocument;
  private Font font;
  private FontMetrics fontMetrics;
  private List transactionJGoTextList; // element JGoText


  public ViewTransactionsFrame( String title, PwPartialPlan partialPlan,
                                List transactionList, int stepNumber,
                                SequenceView sequenceView) {
    super( title);
    this.partialPlan = partialPlan;
    this.transactionList = transactionList;
    this.stepNumber = stepNumber;
    this.sequenceView = sequenceView;

    setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    setSize( FRAME_WIDTH, FRAME_HEIGHT);
    Utilities.setPopUpLocation( this, PlanWorks.planWorks);
    setVisible( true);

    jGoView = new TransactionsJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    getContentPane().add( jGoView, BorderLayout.NORTH);
    jGoView.validate();
    jGoView.setVisible( true);
    this.setVisible( true);

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
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for TimelineView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    Graphics graphics = ((JPanel) this.getContentPane()).getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    fontMetrics = graphics.getFontMetrics( font);
    graphics.dispose();

    // does not do anything
    // jGoView.setFont( font);

    jGoDocument = jGoView.getDocument();

    renderTransactions();

     jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  private void renderTransactions() {
    String headerText = "   ID   TYPE   SOURCE   OBJECT_ID   STEP_NUM";
    
    List transactionJGoTextList = new ArrayList();
    Iterator transItr = transactionList.iterator();
    while (transItr.hasNext()) {
      PwTransaction transaction = (PwTransaction) transItr.next();
      StringBuffer transText = new StringBuffer();
    }
  } // end renderTransactions


  /**
   * <code>TransactionsJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class TransactionsJGoView extends JGoView {

    /**
     * <code>TransactionsJGoView</code> - constructor 
     *
     */
    public TransactionsJGoView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        // mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick

  } // end class TransactionsJGoView


} // end class ViewTransactionsFrame

