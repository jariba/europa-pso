// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: SequenceStepsView.java,v 1.7 2003-10-16 21:40:42 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 24sep03
//

package gov.nasa.arc.planworks.viz.sequence.sequenceSteps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

// PlanWorks/java/lib/JGo/Classier.jar
import com.nwoods.jgo.examples.BasicNode;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwConstraint;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.ResourceNotFoundException;
import gov.nasa.arc.planworks.util.Utilities;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.sequence.SequenceView;
import gov.nasa.arc.planworks.viz.sequence.SequenceViewSet;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>SequenceStepsView</code> - render a histogram of plan data base size
 *                        over sequence steps
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class SequenceStepsView extends SequenceView {

  private PwPlanningSequence planSequence;
  private long startTimeMSecs;
  private ViewSet viewSet;
  private SequenceStepsJGoView jGoView;
  private JGoDocument document;
  private Graphics graphics;
  private FontMetrics fontMetrics;
  private Font font;
  private List tmpStepList; // of StepElement
  private List stepList; // of StepElement

  /**
   * <code>SequenceStepsView</code> - constructor 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param planSequence - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public SequenceStepsView( ViewableObject planSequence,  ViewSet viewSet) {
    super( (PwPlanningSequence) planSequence, (SequenceViewSet) viewSet);
    this.planSequence = (PwPlanningSequence) planSequence;
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (SequenceViewSet) viewSet;
    tmpStepList = new ArrayList();
    stepList = null;

    setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));

    jGoView = new SequenceStepsJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    add( jGoView, BorderLayout.NORTH);
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
    // wait for ConstraintNetworkView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "constraintNetworkView displayable " + this.isDisplayable());
    }
    graphics = this.getGraphics();
    font = new Font( ViewConstants.TIMELINE_VIEW_FONT_NAME,
                     ViewConstants.TIMELINE_VIEW_FONT_STYLE,
                     ViewConstants.TIMELINE_VIEW_FONT_SIZE);
    // does nothing
    // jGoView.setFont( font);
    fontMetrics = graphics.getFontMetrics( font);

    // graphics.dispose();

    document = jGoView.getDocument();

    renderHistogram();

    expandViewFrame( this.getClass().getName(),
                     (int) jGoView.getDocumentSize().getWidth(),
                     (int) jGoView.getDocumentSize().getHeight());

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  /**
   * <code>redraw</code>
   *
   */
  public void redraw() {
    new RedrawViewThread().start();
  }

  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      redrawView();
    } //end run

  } // end class RedrawViewThread

  private void redrawView() {
    jGoView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    document.deleteContents();
    renderHistogram();

    jGoView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end redrawView


  /**
   * <code>getJGoDocument</code>
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoDocument()  {
    return this.document;
  }

  /**
   * <code>getJGoView</code> - needed for PlanWorksTest
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoView()  {
    return jGoView;
  }

  /**
   * <code>getFontMetrics</code>
   *
   * @return - <code>FontMetrics</code> - 
   */
  public FontMetrics getFontMetrics()  {
    return fontMetrics;
  }


  private void renderHistogram() {
    // System.err.println( "stepCount " + planSequence.getStepCount());
    // System.err.println( "stepNumbers " + planSequence.getPartialPlanNamesList());
    
    int x = ViewConstants.STEP_VIEW_X_INIT, y = ViewConstants.STEP_VIEW_Y_INIT;
    Iterator stepItr = planSequence.getPartialPlanNamesList().iterator();
    while (stepItr.hasNext()) {
      String partialPlanName = (String) stepItr.next();
      int planDBSize =
        planSequence.getPlanDBSize( Utilities.getStepNumber( partialPlanName));

      StepElement stepElement = new StepElement( x, y, planDBSize, partialPlanName,
                                                 planSequence, this);
      document.addObjectAtTail( stepElement);
      x += ViewConstants.STEP_VIEW_STEP_WIDTH;
      tmpStepList.add( stepElement);
    }
    stepList = tmpStepList;
  } // end renderHistogram


  /**
   * <code>SequenceStepsJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class SequenceStepsJGoView extends JGoView {

    /**
     * <code>SequenceStepsJGoView</code> - constructor 
     *
     */
    public SequenceStepsJGoView() {
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

  } // end class SequenceStepsJGoView

  private void mouseRightPopupMenu( Point viewCoords) {
    JPopupMenu mouseRightPopup = new JPopupMenu();
    JMenuItem activeTokenItem = new JMenuItem( "Snap to Active Token");
    createActiveTokenItem( activeTokenItem);
    mouseRightPopup.add( activeTokenItem);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createActiveTokenItem( JMenuItem activeTokenItem) {
    activeTokenItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          // get activeToken from viewManager -- Michael will write accessor
//           PwToken activeToken =
//             ((SequenceViewSet) SequenceStepsView.this.getViewSet()).getActiveToken();
//           if (activeToken != null) {

//           }
        }
      });
  } // end createActiveTokenItem

} // end class SequenceStepsView
