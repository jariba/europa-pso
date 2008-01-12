// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ResourceView.java,v 1.21 2004-10-07 20:19:08 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 03march04
//

package gov.nasa.arc.planworks.viz.partialPlan;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoView;

import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.mdi.MDIInternalFrame;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.SwingWorker;
import gov.nasa.arc.planworks.viz.OverviewToolTip;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.ViewListener;
import gov.nasa.arc.planworks.viz.nodes.ResourceNameNode;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>ResourceView</code> - abstract class for subclassing
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public abstract class ResourceView extends PartialPlanView  {

  private static Object staticObject = new Object();

  /**
   * constant <code>LEVEL_SCALE_FONT_SIZE</code>
   *
   */
  public static final int LEVEL_SCALE_FONT_SIZE = 8;

  /**
   * constant <code>Y_MARGIN</code>
   *
   */
  public static final int Y_MARGIN = 4;

  /**
   * constant <code>ONE_HALF_MULTIPLIER</code>
   *
   */
  public static final double ONE_HALF_MULTIPLIER = 0.5;

  private static final int RESOURCE_NAME_Y_OFFSET = 2;
  private static final int SLEEP_FOR_50MS = 50;

  /**
   * variable <code>levelScaleFontMetrics</code>
   *
   */
  protected FontMetrics levelScaleFontMetrics;

  /**
   * variable <code>viewSet</code>
   *
   */
  protected ViewSet viewSet;

  /**
   * variable <code>jGoExtentView</code>
   *
   */
  protected ExtentView jGoExtentView;

  /**
   * variable <code>jGoRulerView</code>
   *
   */
  protected TimeScaleView jGoRulerView;

  /**
   * variable <code>timeScaleMark</code>
   *
   */
  protected JGoStroke timeScaleMark;

  /**
   * variable <code>startYLoc</code>
   *
   */
  protected int startYLoc;

  /**
   * variable <code>docCoords</code>
   *
   */
  protected static Point docCoords;

  /**
   * variable <code>currentYLoc</code>
   *
   */
  protected int currentYLoc;

  /**
   * variable <code>resourceNameNodeList</code> of ResourceNameNode
   *
   */
  protected List resourceNameNodeList;  

  protected double initialTimeScaleEnd;

  private long startTimeMSecs;
  private JGoView jGoLevelScaleView;
  private Component horizontalStrut;
  private int slotLabelMinLength;
  private int maxSlots;
  private int startXLoc;
  private float timeScale;
  private int levelScaleViewWidth;
  private int fillerWidth;
  private int maxYExtent;
  private JGoStroke maxExtentViewHeightPoint;
  private JGoStroke maxLevelViewHeightPoint;
  private Font levelScaleFont;
  private boolean isStepButtonView;
  private boolean isUnaryResource;

  /**
   * <code>ResourceView</code> - constructor 
   *                             Use SwingWorker to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param viewName - <code>String</code> - 
   */
  public ResourceView( final ViewableObject partialPlan, final ViewSet vSet,
                       final String viewName) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    this.viewName = viewName;
    resourceViewInit( vSet);
    isStepButtonView = false;

    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor


  /**
   * <code>ResourceView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param viewName - <code>String</code> - 
   * @param unaryResourceProfileFrame - <code>MDIInternalFrame</code> - 
   * @param resource - <code>PwResource</code> - 
   */
  public ResourceView( final ViewableObject partialPlan, final ViewSet vSet,
                       final String viewName,
                       final MDIInternalFrame unaryResourceProfileFrame,
                       final PwResource resource) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    this.viewName = viewName;
    resourceViewInit( vSet, unaryResourceProfileFrame);
    isStepButtonView = false;
    System.err.println( "Rendering Unary Resource Profile: " + resource.getName() + " ...");
    startTimeMSecs = System.currentTimeMillis();
      // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor


  /**
   * <code>ResourceView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   * @param viewName - <code>String</code> - 
   */
  public ResourceView( final ViewableObject partialPlan, final ViewSet vSet, 
                       final PartialPlanViewState state, final String viewName) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    this.viewName = viewName;
    resourceViewInit( vSet);
    isStepButtonView = true;
    setState( state);
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  }

  /**
   * <code>ResourceView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @param viewName - <code>String</code> - 
   */
  public ResourceView( final ViewableObject partialPlan, final ViewSet vSet,
                       final ViewListener viewListener, final String viewName) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    this.viewName = viewName;
    resourceViewInit( vSet);
    isStepButtonView = false;
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  } // end constructor

  /**
   * <code>ResourceView</code> - constructor 
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param vSet - <code>ViewSet</code> - 
   * @param state - <code>PartialPlanViewState</code> - 
   * @param viewListener - <code>ViewListener</code> - 
   * @param viewName - <code>String</code> - 
   */
  public ResourceView( final ViewableObject partialPlan, final ViewSet vSet, 
                       final PartialPlanViewState state, final ViewListener viewListener,
                       final String viewName) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) vSet);
    this.viewName = viewName;
    resourceViewInit( vSet);
    isStepButtonView = true;
    if (viewListener != null) {
      addViewListener( viewListener);
    }
    setState( state);
    // SwingUtilities.invokeLater( runInit);
    final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          init();
          return null;
        }
    };
    worker.start();  
  }

  private void resourceViewInit( final ViewSet vSet,
                                 final MDIInternalFrame unaryResourceProfileFrame) {
    isUnaryResource = true;
    viewFrame = unaryResourceProfileFrame;
    resourceViewInitCommon( vSet);
  }

  private void resourceViewInit( final ViewSet vSet) {
    isUnaryResource = false;
    ViewListener viewListener = null;
    viewFrame = vSet.openView( this.getClass().getName(), viewListener);
    resourceViewInitCommon( vSet);
  }

  private void resourceViewInitCommon( final ViewSet vSet) {
    this.viewSet = (PartialPlanViewSet) vSet;
    // startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startXLoc = 1;
    // startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    startYLoc = 2;
    timeScaleMark = null;
    maxYExtent = 0;
    maxExtentViewHeightPoint = null;
    maxLevelViewHeightPoint = null;
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    resourceNameNodeList = new ArrayList();
    // for PWTestHelper.findComponentByName
    this.setName( viewFrame.getTitle());
    // create panels/views after fontMetrics available
  } // end resourceViewInit

  private void createLevelScaleAndExtentPanel() {
    LevelScalePanel levelScalePanel = new LevelScalePanel();
    levelScalePanel.setLayout( new BoxLayout( levelScalePanel, BoxLayout.Y_AXIS));

    jGoLevelScaleView = new JGoView();
    jGoLevelScaleView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoLevelScaleView.getVerticalScrollBar().addAdjustmentListener
      ( new VerticalScrollBarListener());
    
    jGoLevelScaleView.validate();
    jGoLevelScaleView.setVisible( true);
    levelScalePanel.add( jGoLevelScaleView, BorderLayout.NORTH);                 
    add( levelScalePanel, "West");

    JPanel extentViewPanel = new JPanel();
    extentViewPanel.setLayout( new BoxLayout( extentViewPanel, BoxLayout.Y_AXIS));

    jGoExtentView = new ExtentView();
    jGoExtentView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoExtentView.getHorizontalScrollBar().addAdjustmentListener
                                     ( new HorizontalScrollBarListener());
    jGoExtentView.getVerticalScrollBar().addAdjustmentListener
                                     ( new VerticalScrollBarListener());
    jGoExtentView.validate();
    jGoExtentView.setVisible( true);
    extentViewPanel.add( jGoExtentView, BorderLayout.NORTH);
    add( extentViewPanel, "Center");
  } // end createLevelScaleAndExtentPanel

  private void createFillerAndRulerPanel() {
    FillerAndRulerPanel fillerAndRulerPanel = new FillerAndRulerPanel();
    fillerAndRulerPanel.setLayout( new BoxLayout( fillerAndRulerPanel, BoxLayout.X_AXIS));
    fillerAndRulerPanel.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);

    horizontalStrut = Box.createHorizontalStrut( fillerWidth);
    fillerAndRulerPanel.add( horizontalStrut, BorderLayout.WEST);

    JPanel rulerPanel = new JPanel();
    rulerPanel.setLayout( new BoxLayout( rulerPanel, BoxLayout.Y_AXIS));

    jGoRulerView = new TimeScaleView( startXLoc, startYLoc, partialPlan, this);
    jGoRulerView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getHorizontalScrollBar().addAdjustmentListener
                                     ( new HorizontalScrollBarListener());
    jGoRulerView.validate();
    jGoRulerView.setVisible( true);
    rulerPanel.add( jGoRulerView, BorderLayout.NORTH);
    fillerAndRulerPanel.add( rulerPanel, BorderLayout.EAST);
    add( fillerAndRulerPanel, "South");
  } // end createFillerAndRulerPanel

//   public Runnable runInit = new Runnable() {
//       public final void run() {
//         init();
//       }
//     };

  /**
   * <code>init</code> - wait for instance to become displayable, determine
   *                     appropriate font metrics, and render the JGo timeline,
   *                     and slot widgets
   *
   *    These functions are not done in the constructor to avoid:
   *    "Cannot measure text until a JGoExtentView exists and is part of a visible window".
   *     int extentScrollExtent = jGoExtentView.getHorizontalScrollBar().getSize().getWidth();
   *    called by componentShown method on the JFrame
   *    JGoExtentView.setVisible( true) must be completed -- use SwingWorker in constructor
   */
  public final void init() {
    handleEvent(ViewListener.EVT_INIT_BEGUN_DRAWING);
    // wait for ResourceView instance to become displayable
    if (! ViewGenerics.displayableWait( ResourceView.this)) {
      closeView( this);
      return;
    }

    this.computeFontMetrics( this);

    // resource names font
    levelScaleFont = new Font( ViewConstants.VIEW_FONT_NAME,
                               ViewConstants.VIEW_FONT_PLAIN_STYLE,
                               LEVEL_SCALE_FONT_SIZE);
    Graphics graphics = ((JPanel) this).getGraphics();
    levelScaleFontMetrics = graphics.getFontMetrics( levelScaleFont);
    graphics.dispose();


    levelScaleViewWidth = computeMaxResourceLabelWidth();
    fillerWidth = levelScaleViewWidth + ViewConstants.JGO_SCROLL_BAR_WIDTH;

    setLayout( new BorderLayout());

    createLevelScaleAndExtentPanel();

    createFillerAndRulerPanel();
   
    this.setVisible( true);

//     boolean doFreeTokens = false;
//     jGoRulerView.collectAndComputeTimeScaleMetrics( doFreeTokens, this);
//     jGoRulerView.createTimeScale();

    createTimeScaleView();

    boolean isRedraw = false, isScrollBarAdjustment = false;
    renderResourceExtent();

    if (! isStepButtonView) {
//       Rectangle documentBounds = jGoExtentView.getDocument().computeBounds();
//       jGoExtentView.getDocument().setDocumentSize( (int) documentBounds.getWidth(),
//                                                    (int) documentBounds.getHeight());
      expandViewFrame( viewFrame,
                       (int) (Math.max( jGoExtentView.getDocumentSize().getWidth(),
                                        jGoRulerView.getDocumentSize().getWidth()) +
                              jGoLevelScaleView.getDocumentSize().getWidth() +
                              jGoExtentView.getVerticalScrollBar().getSize().getWidth()),
                       (int) (jGoExtentView.getDocumentSize().getHeight() +
                              jGoRulerView.getDocumentSize().getHeight()));
    } else {
      // force re-display
      this.validate();
    }

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    int maxStepButtonY = addStepButtons( jGoExtentView);
    if (! isStepButtonView) {
      expandViewFrameForStepButtons( viewFrame, jGoExtentView);
    }

    // equalize ExtentView & ScaleView widths so horizontal scrollbars are equal
    // equalize ExtentView & LevelScaleView heights so vertical scrollbars are equal
    equalizeViewWidthsAndHeights( maxStepButtonY, isRedraw, isScrollBarAdjustment);

    long stopTimeMSecs = System.currentTimeMillis();
    long startTime = startTimeMSecs;
    if (! isUnaryResource) {
      startTime = PlanWorks.getPlanWorks().getViewRenderingStartTime( viewName);
    }
    System.err.println( "   ... " + viewName + " elapsed time: " +
                        (stopTimeMSecs - startTime) + " msecs.");
    startTimeMSecs = 0L;
    handleEvent(ViewListener.EVT_INIT_ENDED_DRAWING);
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public final void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority( Thread.MIN_PRIORITY);
    thread.start();
  }

  /**
   * <code>RedrawViewThread</code> - execute redraw in a new thread
   *
   */
  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public final void run() {
      synchronized( staticObject) {
        handleEvent(ViewListener.EVT_REDRAW_BEGUN_DRAWING);
        System.err.println( "Redrawing " + viewName + " ...");
        if (startTimeMSecs == 0L) {
          startTimeMSecs = System.currentTimeMillis();
        }
        try {
          ViewGenerics.setRedrawCursor( viewFrame);
          boolean isRedraw = true, isScrollBarAdjustment = false;
          renderResourceExtent();
          int maxStepButtonY = addStepButtons( jGoExtentView);
          // causes bottom view edge to creep off screen
          //       if (! isStepButtonView) {
          //         expandViewFrameForStepButtons( viewFrame, jGoExtentView);
          //       }
          // equalize ExtentView & ScaleView widths so horizontal scrollbars are equal
          // equalize ExtentView & LevelScaleView heights so vertical scrollbars are equal
          equalizeViewWidthsAndHeights( maxStepButtonY, isRedraw, isScrollBarAdjustment);
        } finally {
          ViewGenerics.resetRedrawCursor( viewFrame);
        }
        long stopTimeMSecs = System.currentTimeMillis();
        System.err.println( "   ... " + viewName + " elapsed time: " +
                            (stopTimeMSecs - startTimeMSecs) + " msecs.");
        startTimeMSecs = 0L;
        handleEvent(ViewListener.EVT_REDRAW_ENDED_DRAWING);
      }
    } // end run

  } // end class RedrawViewThread

  /**
   * <code>getJGoExtentView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public final JGoView getJGoExtentView()  {
    return this.jGoExtentView;
  }

  /**
   * <code>getJGoExtentDocument</code> - the resource extent view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoExtentDocument()  {
    return this.jGoExtentView.getDocument();
  }

  /**
   * <code>getJGoExtentViewSize</code>
   *
   * @return - <code>Dimension</code> - 
   */
  public final Dimension getJGoExtentViewSize() {
    return this.jGoExtentView.getExtentSize();
  }

  /**
   * <code>getJGoExtentViewHScrollBar</code>
   *
   * @return - <code>JScrollBar</code> - 
   */
  public final JScrollBar getJGoExtentViewHScrollBar() {
    return this.jGoExtentView.getHorizontalScrollBar();
  }

  /**
   * <code>getJGoExtentViewVScrollBar</code>
   *
   * @return - <code>JScrollBar</code> - 
   */
  public final JScrollBar getJGoExtentViewVScrollBar() {
    return this.jGoExtentView.getVerticalScrollBar();
  }

  /**
   * <code>getJGoExtentViewSelection</code>
   *
   * @return - <code>JGoSelection</code> - 
   */
  public final JGoSelection getJGoExtentViewSelection() {
    return this.jGoExtentView.getSelection();
  }

  /**
   * <code>getJGoLevelScaleViewSelection</code>
   *
   * @return - <code>JGoSelection</code> - 
   */
  public final JGoSelection getJGoLevelScaleViewSelection() {
    return this.jGoLevelScaleView.getSelection();
  }

  /**
   * <code>getJGoLevelScaleDocument</code> - the level scale view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public final JGoDocument getJGoLevelScaleDocument()  {
    return this.jGoLevelScaleView.getDocument();
  }

  /**
   * <code>getLevelScaleViewWidth</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getLevelScaleViewWidth() {
    return levelScaleViewWidth;
  }

  /**
   * <code>getTimeScale</code>
   *
   * @return - <code>double</code> - 
   */
  public final double getTimeScale() {
    return jGoRulerView.getTimeScaleNoZoom();
  }

  /**
   * <code>getTimeScaleStart</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getTimeScaleStart() {
    return jGoRulerView.getTimeScaleStart();
  }

  /**
   * <code>getTimeScaleEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getTimeScaleEnd() {
    return jGoRulerView.getTimeScaleEnd();
  }

  /**
   * <code>getStartYLoc</code>
   *
   * @return - <code>int</code> - 
   */
  public final int getStartYLoc() {
    return startYLoc;
  }

  /**
   * <code>getJGoRulerView</code>
   *
   * @return - <code>TimeScaleView</code> - 
   */
  public final TimeScaleView getJGoRulerView() {
    return jGoRulerView;
  }

  /**
   * <code>addResourceNameNode</code>
   *
   * @param node - <code>ResourceNameNode</code> - 
   */
  public final void addResourceNameNode( ResourceNameNode node) {
    resourceNameNodeList.add( node);
  }

  /**
   * <code>createTimeScaleView</code> - abstract
   *
   */
  protected abstract void createTimeScaleView();

  /**
   * <code>computeMaxResourceLabelWidth</code> - abstract
   *
   * @return - <code>int</code> - 
   */
  protected abstract int computeMaxResourceLabelWidth();

  /**
   * <code>renderResourceExtent</code> - abstract
   *
   */
  protected abstract void renderResourceExtent();

  /**
   * <code>findNearestResource</code> - abstract
   *
   * @param dCoords - <code>Point</code> - 
   * @return - <code>PwResource</code> - 
   */
  protected abstract PwResource findNearestResource( final Point dCoords);

  /**
   * <code>mouseRightPopupMenu</code>
   *
   * @param resource - <code>PwResource</code> - 
   * @param viewCoords - <code>Point</code> - 
   */
  protected abstract void mouseRightPopupMenu( final PwResource resource,
                                               final Point viewCoords);

  /**
   * <code>equalizeViewWidthsAndHeights</code>
   *
   *                    write a line at the max horizontal extent in each view, and
   *                    at max vertical extent in jGoExtentView & JGoLevelScaleView
   *                    is also called by PartialPlanView.ButtonAdjustmentListener
   *
   * @param maxStepButtonY - <code>int</code> - 
   * @param isRedraw - <code>boolean</code> - 
   * @param isScrollBarAdjustment - <code>boolean</code> - 
   */
  protected final void equalizeViewWidthsAndHeights( final int maxStepButtonY,
                                                  final boolean isRedraw, 
                                                  final boolean isScrollBarAdjustment) { 
    Dimension extentViewDocSize = jGoExtentView.getDocumentSize();
    Dimension rulerViewDocSize = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth B " + extentViewDocSize.getWidth() +
//                         " rulerViewDocumentWidth B " + rulerViewDocSize.getWidth());
    int xRulerMargin = ViewConstants.TIMELINE_VIEW_X_INIT;
    int jGoDocBorderWidth = ViewConstants.JGO_DOC_BORDER_WIDTH;
    if (isRedraw || isScrollBarAdjustment) {
      xRulerMargin = 0;
    }
    int maxWidth = Math.max( (int) extentViewDocSize.getWidth() - jGoDocBorderWidth,
                             (int) rulerViewDocSize.getWidth() + xRulerMargin -
                             jGoDocBorderWidth);  
//     System.err.println( "maxWidth " + maxWidth);  
    if (! isScrollBarAdjustment) {
      equalizeViewWidths( maxWidth, isRedraw);
    }

    equalizeViewHeights( maxWidth, maxStepButtonY);

//     extentViewDocSize = jGoExtentView.getDocumentSize();
//     rulerViewDocSize = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth A" + extentViewDocSize.getWidth() +
//                         " rulerViewDocumentWidth A" + rulerViewDocSize.getWidth());
  } // end equalizeViewWidthsAndHeights

  private void equalizeViewWidths( final int maxWidth, final boolean isRedraw) {
    JGoStroke maxViewWidthPoint = new JGoStroke();
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
//     System.err.println( "equalizeViewWidths maxWidth " + maxWidth);
    // make mark invisible
    maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1, 
                                          ViewConstants.VIEW_BACKGROUND_COLOR));
    // ColorMap.getColor( "black")));
    jGoExtentView.getDocument().addObjectAtTail( maxViewWidthPoint);

    if ( ! isRedraw) {
      maxViewWidthPoint = new JGoStroke();
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
      // make mark invisible
      maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                            ViewConstants.VIEW_BACKGROUND_COLOR));
      // ColorMap.getColor( "black")));
      jGoRulerView.getDocument().addObjectAtTail( maxViewWidthPoint);
    }
  } // end equalizeViewWidths

  private void equalizeViewHeights( final int maxWidth, final int maxStepButtonY) {
    // always put mark at max y location, so on redraw jGoRulerView does not expand
    int maxY = Math.max( currentYLoc, maxStepButtonY);
//     System.err.println( "equalizeViewWidths maxWidth " + maxWidth + " maxY " + maxY);
    if (maxY > maxYExtent) {
      maxYExtent = maxY;
      if (maxExtentViewHeightPoint != null) {
        jGoExtentView.getDocument().removeObject( maxExtentViewHeightPoint);
      } 
      maxExtentViewHeightPoint = new JGoStroke();
      maxExtentViewHeightPoint.addPoint( maxWidth, maxYExtent);
      maxExtentViewHeightPoint.addPoint( maxWidth - ViewConstants.TIMELINE_VIEW_X_INIT,
                                         maxYExtent);
//     Dimension extentViewDocSize = jGoExtentView.getDocumentSize();
//     Dimension rulerViewDocSize = jGoRulerView.getDocumentSize();
//     System.err.println( "equalizeViewHeights " + extentViewDocSize.getWidth() +
//                         "equalizeViewHeights " + rulerViewDocSize.getWidth());
//      System.err.println( "jGoExtentView height maxWidth " + maxWidth);
//      System.err.println( "jGoExtentView height maxWidth - " +
//                          (maxWidth - ViewConstants.TIMELINE_VIEW_X_INIT));
     // make mark invisible
      maxExtentViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                                   ViewConstants.VIEW_BACKGROUND_COLOR));
      // ColorMap.getColor( "black")));
      jGoExtentView.getDocument().addObjectAtTail( maxExtentViewHeightPoint);

      if (maxLevelViewHeightPoint != null) {
        jGoLevelScaleView.getDocument().removeObject( maxLevelViewHeightPoint);
      }
      maxLevelViewHeightPoint = new JGoStroke();
      maxLevelViewHeightPoint.addPoint( 0, maxYExtent);
      maxLevelViewHeightPoint.addPoint( levelScaleViewWidth -
                                        ViewConstants.RESOURCE_LEVEL_SCALE_WIDTH_OFFSET,
                                        maxYExtent);
      // make mark invisible
      maxLevelViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                                  ViewConstants.VIEW_BACKGROUND_COLOR));
      // ColorMap.getColor( "black")));
      jGoLevelScaleView.getDocument().addObjectAtTail( maxLevelViewHeightPoint);
    }
  } // end equalizeViewHeights


  /**
   * <code>LevelScalePanel</code> - require level scale view panel to be of fixed width
   *
   */
  class LevelScalePanel extends JPanel {

    /**
     * <code>LevelScalePanel</code> - constructor 
     *
     */
    public LevelScalePanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLayout
     */
    public final Dimension getMinimumSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getMaximumSize</code> - used by BoxLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public final Dimension getMaximumSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getPreferredSize</code> - used by BorderLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public final Dimension getPreferredSize() {
//       System.err.println( "jGoLevelScaleView " + jGoLevelScaleView);
//       System.err.println( "jGoRulerView " + jGoRulerView);
      if (jGoRulerView == null) {
        return new Dimension
          ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                   jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
            (int) ResourceView.this.getSize().getHeight());
      } else {
        return new Dimension
          ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                   jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
            (int) (ResourceView.this.getSize().getHeight() -
                   jGoRulerView.getDocumentSize().getHeight() -
                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
      }
    }

  } // end class LevelScalePanel


  /**
   * <code>FillerAndRulerPanel</code> - require ruler view panel to be of fixed height
   *
   */
  class FillerAndRulerPanel extends JPanel {

    /**
     * <code>FillerAndRulerPanel</code> - constructor 
     *
     */
    public FillerAndRulerPanel() {
      super();
    }

    /**
     * <code>getMinimumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLaout
     */
    public final Dimension getMinimumSize() {
      return new Dimension( (int) ResourceView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getMaximumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLaout
     */
    public final Dimension getMaximumSize() {
      return new Dimension( (int) ResourceView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getPreferredSize</code> - used by BorderLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public final Dimension getPreferredSize() {
      return new Dimension( (int) ResourceView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }
  } // end class FillerAndRulerPanel


  /**
   * <code>HorizontalScrollBarListener</code> - keep both jGoExtentView & jGoRulerView aligned,
   *                                  when user moves one scroll bar
   *
   */
  class HorizontalScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep both jGoExtentView & jGoRulerView
     *                                aligned, even when user moves one scroll bar
     *
     * @param event - <code>AdjustmentEvent</code> - 
     */
    public final void adjustmentValueChanged( final AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
        int newPostion = source.getValue();
        if (newPostion != jGoExtentView.getHorizontalScrollBar().getValue()) {
          jGoExtentView.getHorizontalScrollBar().setValue( newPostion);
        } else if (newPostion != jGoRulerView.getHorizontalScrollBar().getValue()) {
          jGoRulerView.getHorizontalScrollBar().setValue( newPostion);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class HorizontalScrollBarListener 


  /**
   * <code>VerticalScrollBarListener</code> - keep both jGoExtentView & jGoLevelScaleView
   *                                  aligned, when user moves one scroll bar
   *
   */
  class VerticalScrollBarListener implements AdjustmentListener {

    /**
     * <code>adjustmentValueChanged</code> - keep both jGoExtentView & jGoLevelScaleView
     *                                aligned, even when user moves one scroll bar
     *
     * @param event - <code>AdjustmentEvent</code> - 
     */
    public final void adjustmentValueChanged( final AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
        int newPostion = source.getValue();
        while ((jGoExtentView == null) ||
               (jGoExtentView.getVerticalScrollBar() == null) ||
               (jGoLevelScaleView.getVerticalScrollBar() == null)) {
          return;
        }
        if (newPostion != jGoExtentView.getVerticalScrollBar().getValue()) {
          jGoExtentView.getVerticalScrollBar().setValue( newPostion);
        } else if (newPostion != jGoLevelScaleView.getVerticalScrollBar().getValue()) {
          jGoLevelScaleView.getVerticalScrollBar().setValue( newPostion);
        }
        // }
    } // end adjustmentValueChanged 

  } // end class VerticalScrollBarListener 


  /**
   * <code>ExtentView</code> - subclass doBackgroundClick to handle drawing
   *                               vertical time marks on view
   *
   */
  class ExtentView extends JGoView {

    /**
     * <code>ExtentView</code> - constructor 
     *
     */
    public ExtentView() {
      super();
    }

    /**
     * <code>doBackgroundClick</code> - Mouse-Right pops up menu:
     *                             1) draws vertical line in extent view to
     *                                focus that time point across all resource profiles
     *
     * @param modifiers - <code>int</code> - 
     * @param dCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public final void doBackgroundClick( final int modifiers, final Point dCoords,
                                         final Point viewCoords) {
      PwResource resource = findNearestResource( dCoords);
      // System.err.println( "doBackgroundClick: resource " + resource.getName());
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        ResourceView.docCoords = dCoords;
        mouseRightPopupMenu( resource, viewCoords);
      }
    } // end doBackgroundClick


  } // end class ExtentView




  /**
   * <code>TimeScaleMark</code> - color the mark and provide its time value
   *                              as a tool tip.
   *
   */
  public class TimeScaleMark extends JGoStroke {

    private int xLoc;

    /**
     * <code>TimeScaleMark</code> - constructor 
     *
     * @param xLocation - <code>int</code> - 
     */
    public TimeScaleMark( final int xLocation) {
      super();
      this.xLoc = xLocation;
      setDraggable( false);
      setResizable( false);
      setSelectable( false);
      setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "red")));
    }

    /**
     * <code>getToolTipText</code>
     *
     * @return - <code>String</code> - 
     */
    public final String getToolTipText() {
      return String.valueOf( jGoRulerView.scaleXLocNoZoom( xLoc));
    }

  } // end class TimeScaleMark


  /**
   * <code>renderBordersUpper</code>
   *
   * @param resource - <code>PwResource</code> - 
   * @param xLeft - <code>int</code> - 
   * @param xRight - <code>int</code> - 
   * @param yLoc - <code>int</code> - 
   * @param jGoDocument - <code>JGoDocument</code> - 
   * @return - <code>int</code> - 
   */
  public final int renderBordersUpper( final PwResource resource, final int xLeft,
                                       final int xRight, final int yLoc,
                                       final JGoDocument jGoDocument) {
//     System.err.println( "renderBordersUpper: xLeft " + xLeft + " xRight " + xRight +
//                         " yLoc " + yLoc + " jGoDocument " + jGoDocument);
    JGoStroke divider = new JGoStroke();
    divider.addPoint( xLeft, yLoc);
    divider.addPoint( xRight, yLoc);
    divider.setDraggable( false); divider.setResizable( false);
    divider.setSelectable( false);
    divider.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "black")));
    jGoDocument.addObjectAtTail( divider);

    int extentYTop = yLoc + ViewConstants.RESOURCE_PROFILE_MAX_Y_OFFSET;
    JGoStroke extentTop = new ResourceLine( resource);
    extentTop.addPoint( xLeft, extentYTop);
    extentTop.addPoint( xRight, extentYTop);
    extentTop.setDraggable( false); extentTop.setResizable( false);
    extentTop.setSelectable( false);
    extentTop.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "green3")));
    jGoDocument.addObjectAtTail( extentTop);
    return extentYTop;
  } // end renderBordersUpper

  /**
   * <code>renderBordersLower</code>
   *
   * @param resource - <code>PwResource</code> - 
   * @param xLeft - <code>int</code> - 
   * @param xRight - <code>int</code> - 
   * @param yLoc - <code>int</code> - 
   * @param jGoDocument - <code>JGoDocument</code> - 
   */
  public final void renderBordersLower( final PwResource resource, final int xLeft,
                                        final int xRight, final int yLoc,
                                        final JGoDocument jGoDocument) {
    int extentYBottom = yLoc;
    JGoStroke extentBottom = new ResourceLine( resource);
    extentBottom.addPoint( xLeft, extentYBottom);
    extentBottom.addPoint( xRight, extentYBottom);
    extentBottom.setDraggable( false); extentBottom.setResizable( false);
    extentBottom.setSelectable( false);
    extentBottom.setPen( new JGoPen( JGoPen.SOLID, 2, ColorMap.getColor( "green3")));
    jGoDocument.addObjectAtTail( extentBottom);
  } // end renderBordersLower

  /**
   * <code>renderResourceName</code>
   *
   * @param yLoc - <code>int</code> - 
   * @param jGoDocument - <code>JGoDocument</code> - 
   */
  public final void renderResourceName( final PwResource resource, final int nameXOffset,
                                        final int yLoc, final JGoDocument jGoDocument,
                                        ResourceView resourceView) {
    int xTop = nameXOffset + (int) (ViewConstants.TIMELINE_VIEW_INSET_SIZE *
                                    ONE_HALF_MULTIPLIER);
    Point nameLoc = new Point( xTop, yLoc + RESOURCE_NAME_Y_OFFSET);
    ResourceNameNode nameNode = new ResourceNameNode( nameLoc, resource, resourceView);
    nameNode.setResizable( false); nameNode.setEditable( false);
    nameNode.setDraggable( false);
    // allow Mouse-Right handler
    // nameNode.setSelectable( false);
    nameNode.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoDocument.addObjectAtTail( nameNode);
    resourceView.addResourceNameNode( nameNode);
  } // end renderResourceName


  public class ResourceLine extends JGoStroke implements OverviewToolTip {

    private PwResource resource;

    public ResourceLine( final PwResource resource) {
      super();
      this.resource = resource;
    }

    public final String getToolTipText() {
      return null;
    } // end getToolTipText

    /**
     * <code>getToolTipText</code> - implements OverviewToolTip
     *
     * @param isOverview - <code>boolean</code> - 
     * @return - <code>String</code> - 
     */
    public final String getToolTipText( final boolean isOverview) {
    StringBuffer tip = new StringBuffer( "<html> ");
    tip.append( resource.getName());
    tip.append( "<br>key=");
    tip.append( resource.getId().toString());
    tip.append( "</html>");
    return tip.toString();
    } // end getToolTipText

  } // end class ResourceLine


} // end class ResourceView
 



