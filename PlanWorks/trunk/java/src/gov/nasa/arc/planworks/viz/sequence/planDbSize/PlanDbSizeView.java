// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: PlanDbSizeView.java,v 1.2 2003-09-30 19:18:57 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 24sep03
//

package gov.nasa.arc.planworks.viz.sequence.planDbSize;

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
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
import gov.nasa.arc.planworks.db.PwVariable;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.VizView;
import gov.nasa.arc.planworks.viz.nodes.HistogramElement;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.nodes.TokenNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;


/**
 * <code>PlanDbSizeView</code> - render a histogram of plan data base size
 *                        over sequence steps
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class PlanDbSizeView extends JFrame {

  private static final int X_LOCATION = 300;
  private static final int Y_LOCATION = 40;
  private static final int FRAME_WIDTH = 400;
  private static final int FRAME_HEIGHT = 200;

  PlanDbSizeJGoView jGoView;
  JGoDocument document;
  Graphics graphics;
  FontMetrics fontMetrics;
  Font font;

  /**
   * <code>PlanDbSizeView</code> - constructor 
   *
   */
  public PlanDbSizeView() {
    super( "Plan Data Base Size Histogram");

    // setLayout( new BoxLayout( this, BoxLayout.Y_AXIS));
    getContentPane().setLayout( new GridLayout( 1, 1)); // if JFrame
    setSize( FRAME_WIDTH, FRAME_HEIGHT);
    setLocation( X_LOCATION, Y_LOCATION);
    setVisible( true);

    jGoView = new PlanDbSizeJGoView();
    jGoView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    // add( jGoView, BorderLayout.NORTH);
    getContentPane().add( jGoView, BorderLayout.NORTH); // if JFrame
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
    int x = 10, y = 10, width = 10, height = 5;
    int lineWidth = 1;
    int lineType = JGoPen.SOLID;
    Color lineColor = ColorMap.getColor( "black");
    Color bgColor = ColorMap.getColor( "green3");
    while (x < 300) {
      HistogramElement element =
        new HistogramElement( x, y, width, height, lineWidth, lineType, lineColor, bgColor);
      document.addObjectAtTail( element);
      x += 10;
      height += 5;
    }

  } // end renderHistogram


  /**
   * <code>PlanDbSizeJGoView</code> - subclass JGoView to add doBackgroundClick
   *
   */
  class PlanDbSizeJGoView extends JGoView {

    /**
     * <code>PlanDbSizeJGoView</code> - constructor 
     *
     */
    public PlanDbSizeJGoView() {
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

  } // end class PlanDbSizeJGoView



} // end class PlanDbSizeView
