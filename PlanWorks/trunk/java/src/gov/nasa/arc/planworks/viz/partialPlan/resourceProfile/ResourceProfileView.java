// 
// * See the file "PlanWorks/disclaimers-and-notices.txt" for 
// * information on usage and redistribution of this file, 
// * and for a DISCLAIMER OF ALL WARRANTIES. 
// 

// $Id: ResourceProfileView.java,v 1.1 2004-02-03 20:43:56 taylor Exp $
//
// PlanWorks -- 
//
// Will Taylor -- started 26Jan04
//

package gov.nasa.arc.planworks.viz.partialPlan.resourceProfile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.Iterator;    
import java.util.List;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

// PlanWorks/java/lib/JGo/JGo.jar
import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoStroke;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;


import gov.nasa.arc.planworks.PlanWorks;
import gov.nasa.arc.planworks.db.DbConstants;
import gov.nasa.arc.planworks.db.PwDomain;
import gov.nasa.arc.planworks.db.PwObject;
import gov.nasa.arc.planworks.db.PwPartialPlan;
import gov.nasa.arc.planworks.db.PwPlanningSequence;
import gov.nasa.arc.planworks.db.PwResource;
import gov.nasa.arc.planworks.db.PwResourceInstant;
import gov.nasa.arc.planworks.db.PwResourceTransaction;
import gov.nasa.arc.planworks.db.PwSlot;
import gov.nasa.arc.planworks.db.PwTimeline;
import gov.nasa.arc.planworks.db.PwToken;
// testing
import gov.nasa.arc.planworks.db.PwIntervalDomain;
import gov.nasa.arc.planworks.db.impl.PwIntervalDomainImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceInstantImpl;
import gov.nasa.arc.planworks.db.impl.PwResourceTransactionImpl;

import gov.nasa.arc.planworks.util.Algorithms;
import gov.nasa.arc.planworks.util.ColorMap;
import gov.nasa.arc.planworks.util.MouseEventOSX;
import gov.nasa.arc.planworks.util.UniqueSet;
import gov.nasa.arc.planworks.viz.ViewConstants;
import gov.nasa.arc.planworks.viz.ViewGenerics;
import gov.nasa.arc.planworks.viz.VizViewOverview;
import gov.nasa.arc.planworks.viz.nodes.NodeGenerics;
import gov.nasa.arc.planworks.viz.partialPlan.AskNodeByKey;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanView;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewSet;
import gov.nasa.arc.planworks.viz.partialPlan.PartialPlanViewState;
import gov.nasa.arc.planworks.viz.viewMgr.ViewableObject;
import gov.nasa.arc.planworks.viz.viewMgr.ViewSet;

/**
 * <code>ResourceProfileView</code> - render the profiles of a
 *                partial plan's resources
 *
 * @author <a href="mailto:william.m.taylor@nasa.gov">Will Taylor</a>
 *                  NASA Ames Research Center - Code IC
 * @version 0.0
 */
public class ResourceProfileView extends PartialPlanView  {

  private long startTimeMSecs;
  private ViewSet viewSet;
  private ExtentView jGoExtentView;
  private JGoView jGoLevelScaleView;
  private JGoView jGoRulerView;
  private Component horizontalStrut;
  private List resourceProfileList; // element ResourceProfile
  private int slotLabelMinLength;
  private int timeScaleStart;
  private int timeScaleEnd;
  private int maxSlots;
  private int startXLoc;
  private int xOrigin;
  private int endXLoc;
  private int startYLoc;
  private int timeDelta;
  private int tickTime;
  private int maxCellRow;
  private float timeScale;
  private JGoStroke timeScaleMark;
  private int levelScaleViewWidth;
  private int fillerWidth;
  private int maxYExtent;
  private JGoStroke maxExtentViewHeightPoint;
  private JGoStroke maxLevelViewHeightPoint;

  private static Point docCoords;


  /**
   * <code>ResourceProfileView</code> - constructor 
   *                             Use SwingUtilities.invokeLater( runInit) to
   *                             properly render the JGo widgets
   *
   * @param partialPlan - <code>ViewableObject</code> - 
   * @param viewSet - <code>ViewSet</code> - 
   */
  public ResourceProfileView( ViewableObject partialPlan, ViewSet viewSet) {
    super( (PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    resourceProfileViewInit(viewSet);

    SwingUtilities.invokeLater( runInit);
  } // end constructor


  public ResourceProfileView( ViewableObject partialPlan, ViewSet viewSet, 
                              PartialPlanViewState s) {
    super((PwPartialPlan) partialPlan, (PartialPlanViewSet) viewSet);
    resourceProfileViewInit(viewSet);
    setState(s);
    SwingUtilities.invokeLater(runInit);
  }

  private void resourceProfileViewInit(ViewSet viewSet) {
    this.startTimeMSecs = System.currentTimeMillis();
    this.viewSet = (PartialPlanViewSet) viewSet;
    
    // startXLoc = ViewConstants.TIMELINE_VIEW_X_INIT * 2;
    startXLoc = 1;
    startYLoc = ViewConstants.TIMELINE_VIEW_Y_INIT;
    maxCellRow = 0;
    timeScaleMark = null;
    maxYExtent = 0;
    maxExtentViewHeightPoint = null;
    maxLevelViewHeightPoint = null;
    slotLabelMinLength = ViewConstants.TIMELINE_VIEW_EMPTY_NODE_LABEL_LEN;
    // create panels/views after fontMetrics available
   } // end resourceProfileViewInit

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

    jGoRulerView = new JGoView();
    jGoRulerView.setBackground( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getHorizontalScrollBar().addAdjustmentListener
                                     ( new HorizontalScrollBarListener());
    jGoRulerView.validate();
    jGoRulerView.setVisible( true);
    rulerPanel.add( jGoRulerView, BorderLayout.NORTH);
    fillerAndRulerPanel.add( rulerPanel, BorderLayout.EAST);
    add( fillerAndRulerPanel, "South");
  } // end createFillerAndRulerPanel

  public PartialPlanViewState getState() {
    return new ResourceProfileViewState(this);
  }

//   public boolean showLabels(){return isShowLabels;}
//   public int displayMode(){return temporalDisplayMode;}

  public void setState(PartialPlanViewState s) {
    super.setState(s);
    ResourceProfileViewState state = (ResourceProfileViewState)s;
//     isShowLabels = state.showingLabels();
//     temporalDisplayMode = state.displayMode();
  }

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
   *    "Cannot measure text until a JGoExtentView exists and is part of a visible window".
   *     int extentScrollExtent = jGoExtentView.getHorizontalScrollBar().getSize().getWidth();
   *    called by componentShown method on the JFrame
   *    JGoExtentView.setVisible( true) must be completed -- use runInit in constructor
   */
  public void init() {
    // jGoExtentView.setCursor( new Cursor( Cursor.WAIT_CURSOR));
    // wait for ResourceProfileView instance to become displayable
    while (! this.isDisplayable()) {
      try {
        Thread.currentThread().sleep(50);
      } catch (InterruptedException excp) {
      }
      // System.err.println( "timelineView displayable " + this.isDisplayable());
    }
    this.computeFontMetrics( this);

    levelScaleViewWidth = computeMaxResourceLabelWidth();
    fillerWidth = levelScaleViewWidth + ViewConstants.JGO_SCROLL_BAR_WIDTH;

    setLayout( new BorderLayout());

    createLevelScaleAndExtentPanel();

    createFillerAndRulerPanel();
   
    this.setVisible( true);
    
    collectAndComputeTimeScaleMetrics();
    createTimeScale();
    boolean isRedraw = false, isScrollBarAdjustment = false;
    renderResourceExtent();

    expandViewFrame( viewSet.openView( this.getClass().getName()),
                     (int) (Math.max( jGoExtentView.getDocumentSize().getWidth(),
                                      jGoRulerView.getDocumentSize().getWidth()) +
                            jGoLevelScaleView.getDocumentSize().getWidth() +
                            jGoExtentView.getVerticalScrollBar().getSize().getWidth()),
                     (int) (jGoExtentView.getDocumentSize().getHeight() +
                            jGoRulerView.getDocumentSize().getHeight()));

    // print out info for created nodes
    // iterateOverJGoDocument(); // slower - many more nodes to go thru
    // iterateOverNodes();

    long stopTimeMSecs = System.currentTimeMillis();
    System.err.println( "   ... elapsed time: " +
                        (stopTimeMSecs - startTimeMSecs) + " msecs.");
    
    int maxStepButtonY = addStepButtons( jGoExtentView);
    // equalize ExtentView & ScaleView widths so horizontal scrollbars are equal
    // equalize ExtentView & LevelScaleView heights so vertical scrollbars are equal
    equalizeViewWidthsAndHeights( maxStepButtonY, isRedraw, isScrollBarAdjustment);

    // jGoExtentView.setCursor( new Cursor( Cursor.DEFAULT_CURSOR));
  } // end init


  /**
   * <code>redraw</code> - called by Content Spec to apply user's content spec request.
   *                       setVisible(true | false)
   *                       according to the Content Spec enabled ids
   *
   */
  public void redraw() {
    Thread thread = new RedrawViewThread();
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  /**
   * <code>RedrawViewThread</code> - execute redraw in a new thread
   *
   */
  class RedrawViewThread extends Thread {

    public RedrawViewThread() {
    }  // end constructor

    public void run() {
      boolean isRedraw = true, isScrollBarAdjustment = false;
      renderResourceExtent();
      int maxStepButtonY = addStepButtons( jGoExtentView);
      // equalize ExtentView & ScaleView widths so horizontal scrollbars are equal
      // equalize ExtentView & LevelScaleView heights so vertical scrollbars are equal
      equalizeViewWidthsAndHeights( maxStepButtonY, isRedraw, isScrollBarAdjustment);
    } // end run

  } // end class RedrawViewThread

  private void renderResourceExtent() {
    jGoExtentView.getDocument().deleteContents();

    validTokenIds = viewSet.getValidIds();
    displayedTokenIds = new ArrayList();
    if(resourceProfileList != null) {
      resourceProfileList.clear();
    }
    resourceProfileList = new UniqueSet();

    createResourceProfiles();

    boolean showDialog = true;
    // isContentSpecRendered( PlanWorks.RESOURCE_PROFILE_VIEW, showDialog);

    layoutResourceProfiles();
  } // end createResourceProfileView

  /**
   * <code>getJGoExtentView</code> - 
   *
   * @return - <code>JGoView</code> - 
   */
  public JGoView getJGoExtentView()  {
    return this.jGoExtentView;
  }

  /**
   * <code>getJGoExtentDocument</code> - the resource extent view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoExtentDocument()  {
    return this.jGoExtentView.getDocument();
  }

  /**
   * <code>getJGoLevelScaleDocument</code> - the level scale view document
   *
   * @return - <code>JGoDocument</code> - 
   */
  public JGoDocument getJGoLevelScaleDocument()  {
    return this.jGoLevelScaleView.getDocument();
  }

  /**
   * <code>getLevelScaleViewWidth</code>
   *
   * @return - <code>int</code> - 
   */
  public int getLevelScaleViewWidth() {
    return levelScaleViewWidth;
  }

  /**
   * <code>getResourceProfileList</code>
   *
   * @return - <code>List</code> - of ResourceProfile
   */
  public List getResourceProfileList() {
    return resourceProfileList;
  }

  /**
   * <code>getTimeScale</code>
   *
   * @return - <code>float</code> - 
   */
  public float getTimeScale() {
    return timeScale;
  }

  /**
   * <code>getTimeScaleStart</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimeScaleStart() {
    return timeScaleStart;
  }

  /**
   * <code>getTimeScaleEnd</code>
   *
   * @return - <code>int</code> - 
   */
  public int getTimeScaleEnd() {
    return timeScaleEnd;
  }

  /**
   * <code>scaleTime</code> - convert time to view x location
   *
   * @param time - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int scaleTime( int time) {
    return xOrigin + (int) (timeScale * time);
  }

  /**
   * <code>scaleXLoc</code> - convert from view x location to time
   *
   * @param xLoc - <code>int</code> - 
   * @return - <code>int</code> - 
   */
  public int  scaleXLoc( int xLoc) {
    return (int) ((xLoc - xOrigin) / timeScale);
  }

  /**
   * <code>getStartYLoc</code>
   *
   * @return - <code>int</code> - 
   */
  public int getStartYLoc() {
    return startYLoc;
  }

  private void collectAndComputeTimeScaleMetrics() {
    List objectList = partialPlan.getObjectList();
    Iterator objectIterator = objectList.iterator();
    boolean alwaysReturnEnd = true;
    while (objectIterator.hasNext()) {
      PwObject object = (PwObject) objectIterator.next();
      String objectName = object.getName();
      List timelineList = object.getTimelineList();
      Iterator timelineIterator = timelineList.iterator();
      while (timelineIterator.hasNext()) {
        PwTimeline timeline = (PwTimeline) timelineIterator.next();
        List slotList = timeline.getSlotList();
        Iterator slotIterator = slotList.iterator();
        PwSlot previousSlot = null;
        int slotCnt = 0;
        while (slotIterator.hasNext()) {
          PwSlot slot = (PwSlot) slotIterator.next();
          boolean isLastSlot = (! slotIterator.hasNext());
          PwToken token = slot.getBaseToken();
          slotCnt++;
          PwDomain[] intervalArray =
            NodeGenerics.getStartEndIntervals( slot, previousSlot, isLastSlot,
                                               alwaysReturnEnd);
          collectTimeScaleMetrics( intervalArray[0], intervalArray[1], token);
          previousSlot = slot;
        }
        if (slotCnt > maxSlots) {
          maxSlots = slotCnt;
        }
      }
    }
    maxSlots = Math.max( maxSlots, ViewConstants.TEMPORAL_MIN_MAX_SLOTS);

    computeTimeScaleMetrics();

  } // collectAndComputeTimeScaleMetrics

  private void collectTimeScaleMetrics( PwDomain startTimeIntervalDomain,
                                        PwDomain endTimeIntervalDomain, PwToken token) {
    int leftMarginTime = 0;
    if (startTimeIntervalDomain != null) {
//       System.err.println( "collectTimeScaleMetrics earliest " +
//                           startTimeIntervalDomain.getLowerBound() + " latest " +
//                           startTimeIntervalDomain.getUpperBound());
      int earliestTime = startTimeIntervalDomain.getLowerBoundInt();
      leftMarginTime = earliestTime;
      if ((earliestTime != DbConstants.MINUS_INFINITY_INT) &&
          (earliestTime < timeScaleStart)) {
          timeScaleStart = earliestTime;
      }
      int latestTime = startTimeIntervalDomain.getUpperBoundInt();
      if (leftMarginTime == DbConstants.MINUS_INFINITY_INT) {
        leftMarginTime = latestTime;
      }
      if ((latestTime != DbConstants.PLUS_INFINITY_INT) &&
          (latestTime != DbConstants.MINUS_INFINITY_INT) &&
          (latestTime < timeScaleStart)) {
        timeScaleStart = latestTime;
      }
    }
    if (endTimeIntervalDomain != null) {
//       System.err.println( "collectTimeScaleMetrics latest " +
//                           endTimeIntervalDomain.getUpperBound() + " earliest " +
//                           endTimeIntervalDomain.getLowerBound());
      int latestTime = endTimeIntervalDomain.getUpperBoundInt();
      if (latestTime != DbConstants.PLUS_INFINITY_INT) {
        if (latestTime > timeScaleEnd) {
          timeScaleEnd = latestTime;
        }
      }
      int earliestTime = endTimeIntervalDomain.getLowerBoundInt();
      if ((earliestTime != DbConstants.MINUS_INFINITY_INT) &&
          (earliestTime != DbConstants.PLUS_INFINITY_INT) &&
          (earliestTime > timeScaleEnd)) {
        timeScaleEnd = earliestTime;
      }
    }
  } // end collectTimeScaleMetrics

  private void computeTimeScaleMetrics() {
    endXLoc = Math.max( startXLoc +
                        (maxSlots * slotLabelMinLength * fontMetrics.charWidth( 'A')),
                        ViewConstants.TEMPORAL_MIN_END_X_LOC);
    timeScale = ((float) (endXLoc - startXLoc)) / ((float) (timeScaleEnd - timeScaleStart));
    //System.err.println( "computeTimeScaleMetrics: startXLoc " + startXLoc +
    //                    " endXLoc " + endXLoc);
    //System.err.println( "Temporal Extent View time scale: " + timeScaleStart + " " +
    //                   timeScaleEnd + " maxSlots " + maxSlots + " timeScale " + timeScale);
    int timeScaleRange = timeScaleEnd - timeScaleStart;
    timeDelta = 1;
    int maxIterationCnt = 25, iterationCnt = 0;
    while ((timeDelta * maxSlots) < timeScaleRange) {
      if (timeDelta == 1) {
        timeDelta = 2;
      } else if (timeDelta == 2) {
        timeDelta = 5;
      } else {
        timeDelta *= 2;
      }
//       System.err.println( "range " + timeScaleRange + " maxSlots " +
//                           maxSlots + " timeDelta " + timeDelta);
      iterationCnt++;
      if (iterationCnt > maxIterationCnt) {
        String message = "Range (" + timeScaleRange + ") execeeds functionality";
        JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                       "Resource Profile View Exception",
                                       JOptionPane.ERROR_MESSAGE);
        System.err.println( message);
        System.exit( 1);
      }
    }
    tickTime = 0;
    xOrigin = startXLoc;
    int scaleStart = timeScaleStart;
    if ((scaleStart < 0) && ((scaleStart % timeDelta) == 0)) {
      scaleStart -= 1;
    }
    while (scaleStart < tickTime) {
      tickTime -= timeDelta;
      xOrigin += (int) (timeScale * timeDelta);
//       System.err.println( "scaleStart " + scaleStart + " tickTime " + tickTime +
//                           " xOrigin " + xOrigin);
    }
    // System.err.println( " xOrigin " + xOrigin);
  } // end computeTimeScaleMetrics

  private void createTimeScale() {
    int xLoc = (int) scaleTime( tickTime);
    // System.err.println( "createTimeScale: xLoc " + xLoc);
    int yRuler = startYLoc;
    int yLabelUpper = yRuler, yLabelLower = yRuler;
    if ((timeScaleEnd - timeScaleStart) > ViewConstants.TEMPORAL_LARGE_LABEL_RANGE) {
      yLabelLower = yRuler + ViewConstants.TIMELINE_VIEW_Y_INIT;
    }
    int yLabel = yLabelUpper;
    int scaleWidth = 2, tickHeight = ViewConstants.TIMELINE_VIEW_Y_INIT / 2;
    JGoStroke timeScaleRuler = new JGoStroke();
    timeScaleRuler.setPen( new JGoPen( JGoPen.SOLID, scaleWidth, ColorMap.getColor( "black")));
    timeScaleRuler.setDraggable( false);
    timeScaleRuler.setResizable( false);
    boolean isUpperLabel = true;
    while (tickTime < timeScaleEnd) {
      timeScaleRuler.addPoint( xLoc, yRuler);
      timeScaleRuler.addPoint( xLoc, yRuler + tickHeight);
      timeScaleRuler.addPoint( xLoc, yRuler);
      addTickLabel( tickTime, xLoc, yLabel + 4);
      tickTime += timeDelta;
      xLoc = (int) scaleTime( tickTime);
      isUpperLabel = (! isUpperLabel);
      if (isUpperLabel) {
        yLabel = yLabelUpper;
      } else {
        yLabel = yLabelLower;
      }
    }
    timeScaleRuler.addPoint( xLoc, yRuler);
    timeScaleRuler.addPoint( xLoc, yRuler + tickHeight);
    addTickLabel( tickTime, xLoc, yLabel + 4);

    jGoRulerView.getDocument().addObjectAtTail( timeScaleRuler);
  } // end createTimeScale


  private void addTickLabel( int tickTime, int x, int y) {
    String text = String.valueOf( tickTime);
    Point textLoc = new Point( x, y);
    JGoText textObject = new JGoText( textLoc, ViewConstants.TIMELINE_VIEW_FONT_SIZE,
                                      text, ViewConstants.TIMELINE_VIEW_FONT_NAME,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_BOLD,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_UNDERLINED,
                                      ViewConstants.TIMELINE_VIEW_IS_FONT_ITALIC,
                                      ViewConstants.TIMELINE_VIEW_TEXT_ALIGNMENT,
                                      ViewConstants.TIMELINE_VIEW_IS_TEXT_MULTILINE,
                                      ViewConstants.TIMELINE_VIEW_IS_TEXT_EDITABLE);
    textObject.setResizable( false);
    textObject.setEditable( false);
    textObject.setDraggable( false);
    textObject.setBkColor( ViewConstants.VIEW_BACKGROUND_COLOR);
    jGoRulerView.getDocument().addObjectAtTail( textObject);
  } // end addTickLabel


  private List createDummyData() {
    List resourceList = new ArrayList();
    List resourceInstantList = new ArrayList();
    PwIntervalDomain instantDomain =
      (PwIntervalDomain) new PwIntervalDomainImpl ( "type", "10", "10");
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99011), instantDomain, 4., 6.));
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99012),
                                   new PwIntervalDomainImpl( "type", "20", "20"), 6., 8.));
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99013),
                                   new PwIntervalDomainImpl( "type", "30", "30"), 8., 10.));
    resourceInstantList.add
      ( new PwResourceInstantImpl( new Integer( 99014),
                                   new PwIntervalDomainImpl( "type", "50", "50"), 2., 4.));
    PwResource dummyResource =
      new PwResourceImpl( new Integer( 9901), "Resource1", 4., 0., 12.,
                          timeScaleStart, timeScaleEnd,
                          new UniqueSet(), resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9902), "Resource2", 4., 0., 12., 
                          timeScaleStart, timeScaleEnd,
                          new UniqueSet(), resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9903), "ResourceThree", 4., 0., 12., 
                          timeScaleStart, timeScaleEnd,
                          new UniqueSet(), resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9904), "ResourceFour", 4., 0., 12., 
                          timeScaleStart, timeScaleEnd,
                          new UniqueSet(), resourceInstantList);
    resourceList.add( dummyResource);
    dummyResource =
      new PwResourceImpl( new Integer( 9905), "ResourceFive", 4., 0., 12., 
                          timeScaleStart, timeScaleEnd,
                          new UniqueSet(), resourceInstantList);
    resourceList.add( dummyResource);
    return resourceList;
  } // end createDummyData


  private void createResourceProfiles() {
    // resourceList will come from partialPlan
    List resourceList = createDummyData();
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      ResourceProfile resourceProfile =
        new ResourceProfile( resource, ColorMap.getColor( ViewConstants.FREE_TOKEN_BG_COLOR),
                             this);
      // System.err.println( "resourceProfile " + resourceProfile);
      jGoExtentView.getDocument().addObjectAtTail( resourceProfile);
      resourceProfileList.add( resourceProfile);
    }

  } // end createResourceProfiles

  private int computeMaxResourceLabelWidth() {
    int maxWidth = ViewConstants.JGO_SCROLL_BAR_WIDTH * 2;
    // resourceList will come from partialPlan
    List resourceList = createDummyData();
    Iterator resourceItr = resourceList.iterator();
    while (resourceItr.hasNext()) {
      PwResource resource = (PwResource) resourceItr.next();
      int width = ResourceProfile.getNodeLabelWidth( resource.getName(), this);
      if (width > maxWidth) {
        maxWidth = width;
      }
    }
    return maxWidth;
  } // end computeMaxResourceLabelWidth

  private void layoutResourceProfiles() {
    /*List extents = new ArrayList();
    Iterator resourceProfileIterator = resourceProfileList.iterator();
    while (resourceProfileIterator.hasNext()) {
      ResourceProfile resourceProfile = (ResourceProfile) resourceProfileIterator.next();
      extents.add( resourceProfile);
      }*/
    List extents = new ArrayList(resourceProfileList);
    // do the layout -- compute cellRow for each node
    List results = Algorithms.allocateRows( scaleTime( timeScaleStart),
                                            scaleTime( timeScaleEnd), extents);
    //List results = Algorithms.betterAllocateRows(scaleTime(timeScaleStart),
    //                                            scaleTime(timeScaleEnd), extents);
    if (resourceProfileList.size() != results.size()) {
      String message = String.valueOf( resourceProfileList.size() - results.size()) +
        " nodes not successfully allocated";
      JOptionPane.showMessageDialog( PlanWorks.getPlanWorks(), message,
                                     "Resource Profile View Layout Exception",
                                     JOptionPane.ERROR_MESSAGE);
      return;
    }
    for (Iterator it = extents.iterator(); it.hasNext();) {
      ResourceProfile resourceProfile = (ResourceProfile) it.next();
      // System.err.println( resourceProfile.getName() + " cellRow " + resourceProfile.getRow());
      if (resourceProfile.getRow() > maxCellRow) {
        maxCellRow = resourceProfile.getRow();
      }
      // render the profile
      resourceProfile.configure();
    }
  } // end layoutResourceProfiles


//   private void iterateOverNodes() {
//     int numResourceProfiles = resourceProfileList.size();
//     //System.err.println( "iterateOverNodes: numResourceProfiles " + numResourceProfiles);
//     Iterator resourceIterator = resourceProfileList.iterator();
//     while (resourceIterator.hasNext()) {
//       ResourceProfile resourceProfile = (ResourceProfile) resourceIterator.next();
//       System.err.println( "name '" + resourceProfile.getPredicateName() + "' location " +
//                           resourceProfile.getLocation());
//     }
//   } // end iterateOverNodes


//   private void iterateOverJGoDocument() {
//     JGoListPosition position = jGoExtentView.getDocument().getFirstObjectPos();
//     int cnt = 0;
//     while (position != null) {
//       JGoObject object = jGoExtentView.getDocument().getObjectAtPos( position);
//       position = jGoExtentView.getDocument().getNextObjectPosAtTop( position);
//       //System.err.println( "iterateOverJGoDoc: position " + position +
//       //                   " className " + object.getClass().getName());
//       if (object instanceof ResourceProfile) {
//         ResourceProfile resourceProfile = (ResourceProfile) object;

//       }
//       cnt += 1;
// //       if (cnt > 100) {
// //         break;
// //       }
//     }
//     //System.err.println( "iterateOverJGoDoc: cnt " + cnt);
//   } // end iterateOverJGoDocument


  /**
   * <code>equalizeViewWidthsAndHeights</code>
   *
   *                    write a line at the max horizontal extent in each view, and
   *                    at max vertical extent in jGoExtentView & JGoLevelScaleView
   *                    is also called by PartialPlanView.ButtonAdjustmentListener
   *
   * @param isRedraw - <code>boolean</code> - 
   * @param maxStepButtonY - <code>int</code> - 
   */
  public void equalizeViewWidthsAndHeights( int maxStepButtonY, boolean isRedraw, 
                                            boolean isScrollBarAdjustment) {
    Dimension extentViewDocSize = jGoExtentView.getDocumentSize();
    Dimension rulerViewDocSize = jGoRulerView.getDocumentSize();
//     System.err.println( "extentViewDocumentWidth B" + extentViewDocSize.getWidth() +
//                         " rulerViewDocumentWidth B" + rulerViewDocSize.getWidth());
    int xRulerMargin = ViewConstants.TIMELINE_VIEW_X_INIT;
    int jGoDocBorderWidth = ViewConstants.JGO_DOC_BORDER_WIDTH;
    if (isRedraw) {
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

  private void equalizeViewWidths( int maxWidth, boolean isRedraw) {
    JGoStroke maxViewWidthPoint = new JGoStroke();
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
    maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
    // make mark invisible
    maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1, 
                                          ViewConstants.VIEW_BACKGROUND_COLOR));
    jGoExtentView.getDocument().addObjectAtTail( maxViewWidthPoint);

    if ( ! isRedraw) {
      maxViewWidthPoint = new JGoStroke();
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT);
      maxViewWidthPoint.addPoint( maxWidth, ViewConstants.TIMELINE_VIEW_Y_INIT * 2);
      // make mark invisible
      maxViewWidthPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                            ViewConstants.VIEW_BACKGROUND_COLOR));
      jGoRulerView.getDocument().addObjectAtTail( maxViewWidthPoint);
    }
  } // end equalizeViewWidths

  private void equalizeViewHeights( int maxWidth, int maxStepButtonY) {
    // always put mark at max y location, so on redraw jGoRulerView does not expand
    int maxY = Math.max( startYLoc + ((maxCellRow + 1) *
                                      ViewConstants.RESOURCE_PROFILE_CELL_HEIGHT) + 2,
                         maxStepButtonY);
    if (maxY > maxYExtent) {
      maxYExtent = maxY;
      if (maxExtentViewHeightPoint != null) {
        jGoExtentView.getDocument().removeObject( maxExtentViewHeightPoint);
      }
      maxExtentViewHeightPoint = new JGoStroke();
      maxExtentViewHeightPoint.addPoint( maxWidth, maxYExtent);
      maxExtentViewHeightPoint.addPoint( maxWidth - ViewConstants.TIMELINE_VIEW_X_INIT,
                                         maxYExtent);
      // make mark invisible
      maxExtentViewHeightPoint.setPen( new JGoPen( JGoPen.SOLID, 1,
                                                   ViewConstants.VIEW_BACKGROUND_COLOR));
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
    public Dimension getMinimumSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceProfileView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getMaximumSize</code> - used by BoxLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getMaximumSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceProfileView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getPreferredSize</code> - used by BorderLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getPreferredSize() {
      return new Dimension
        ( (int) (jGoLevelScaleView.getDocumentSize().getWidth() +
                 jGoLevelScaleView.getVerticalScrollBar().getSize().getWidth()),
          (int) (ResourceProfileView.this.getSize().getHeight() -
                 jGoRulerView.getDocumentSize().getHeight() -
                 jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
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
    public Dimension getMinimumSize() {
      return new Dimension( (int) ResourceProfileView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getMaximumSize</code>
     *
     * @return - <code>Dimension</code> - used by BoxLaout
     */
    public Dimension getMaximumSize() {
      return new Dimension( (int) ResourceProfileView.this.getSize().getWidth(),
                            (int) (jGoRulerView.getDocumentSize().getHeight() +
                                   jGoRulerView.getHorizontalScrollBar().getSize().getHeight()));
    }

    /**
     * <code>getPreferredSize</code> - used by BorderLayout
     *
     * @return - <code>Dimension</code> - 
     */
    public Dimension getPreferredSize() {
      return new Dimension( (int) ResourceProfileView.this.getSize().getWidth(),
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
    public void adjustmentValueChanged( AdjustmentEvent event) {
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
    public void adjustmentValueChanged( AdjustmentEvent event) {
      JScrollBar source = (JScrollBar) event.getSource();
      // to get immediate incremental adjustment, rather than waiting for
      // final position, comment out next check
      // if (! source.getValueIsAdjusting()) {
        int newPostion = source.getValue();
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
     *                                focus that time point across all resource nodes
     *
     * @param modifiers - <code>int</code> - 
     * @param docCoords - <code>Point</code> - 
     * @param viewCoords - <code>Point</code> - 
     */
    public void doBackgroundClick( int modifiers, Point docCoords, Point viewCoords) {
      if (MouseEventOSX.isMouseLeftClick( modifiers, PlanWorks.isMacOSX())) {
        // do nothing
      } else if (MouseEventOSX.isMouseRightClick( modifiers, PlanWorks.isMacOSX())) {
        ResourceProfileView.docCoords = docCoords;
        mouseRightPopupMenu( viewCoords);
      }
    } // end doBackgroundClick


  } // end class ExtentView


  private void mouseRightPopupMenu( Point viewCoords) {
    String partialPlanName = partialPlan.getPartialPlanName();
    PwPlanningSequence planSequence = PlanWorks.getPlanWorks().getPlanSequence( partialPlan);
    JPopupMenu mouseRightPopup = new JPopupMenu();

//     JMenuItem nodeByKeyItem = new JMenuItem( "Find by Key");
//     createNodeByKeyItem( nodeByKeyItem);
//     mouseRightPopup.add( nodeByKeyItem);

    createOpenViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup,
                         PlanWorks.RESOURCE_PROFILE_VIEW);

    JMenuItem overviewWindowItem = new JMenuItem( "Overview Window");
    createOverviewWindowItem( overviewWindowItem, this, viewCoords);
    mouseRightPopup.add( overviewWindowItem);

    JMenuItem raiseContentSpecItem = new JMenuItem( "Raise Content Filter");
    createRaiseContentSpecItem( raiseContentSpecItem);
    mouseRightPopup.add( raiseContentSpecItem);
    
    JMenuItem timeMarkItem = new JMenuItem( "Set Time Scale Line");
    createTimeMarkItem( timeMarkItem);
    mouseRightPopup.add( timeMarkItem);

//     JMenuItem activeResourceItem = new JMenuItem( "Snap to Active Resource");
//     createActiveResourceItem( activeResourceItem);
//     mouseRightPopup.add( activeResourceItem);

    if (areThereNavigatorWindows()) {
      mouseRightPopup.addSeparator();
      JMenuItem closeWindowsItem = new JMenuItem( "Close Navigator Views");
      createCloseNavigatorWindowsItem( closeWindowsItem);
      mouseRightPopup.add( closeWindowsItem);
    }
    createAllViewItems( partialPlan, partialPlanName, planSequence, mouseRightPopup);

    NodeGenerics.showPopupMenu( mouseRightPopup, this, viewCoords);
  } // end mouseRightPopupMenu


  private void createActiveResourceItem( JMenuItem activeResourceItem) {
    activeResourceItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          PwResource activeResource =
            ((PartialPlanViewSet) ResourceProfileView.this.getViewSet()).getActiveResource();
          if (activeResource != null) {
            boolean isByKey = false;
            PwSlot slot = null;
            findAndSelectResource( activeResource, isByKey);
          }
        }
      });
  } // end createActiveResourceItem


  private void createNodeByKeyItem( JMenuItem nodeByKeyItem) {
    nodeByKeyItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          AskNodeByKey nodeByKeyDialog =
            new AskNodeByKey( "Find by Key", "key (int)", ResourceProfileView.this);
          Integer nodeKey = nodeByKeyDialog.getNodeKey();
          if (nodeKey != null) {
            // System.err.println( "createNodeByKeyItem: nodeKey " + nodeKey.toString());

            PwResource resourceToFind = null;
            // PwResource resourceToFind = partialPlan.getResource( nodeKey);
            
            boolean isByKey = true;
            findAndSelectResource( resourceToFind, isByKey);
          }
        }
      });
  } // end createNodeByKeyItem


  /**
   * <code>findAndSelectResource</code> - 
   *
   * @param resourceToFind - <code>PwResource</code> - 
   * @param isByKey - <code>boolean</code> - 
   */
  public void findAndSelectResource( PwResource resourceToFind, boolean isByKey) {
    boolean isResourceFound = false;
    boolean isHighlightNode = true;

  } // end findAndSelectResource


  private void createTimeMarkItem( JMenuItem timeMarkItem) {
    timeMarkItem.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent evt) {
          int xLoc = (int) ResourceProfileView.docCoords.getX();
          // System.err.println( "doMouseClick: xLoc " + xLoc + " time " + scaleXLoc( xLoc));
          if (timeScaleMark != null) {
            jGoExtentView.getDocument().removeObject( timeScaleMark);
            // jGoExtentView.validate();
          } 
          timeScaleMark = new TimeScaleMark( xLoc);
          timeScaleMark.addPoint( xLoc, startYLoc);
          timeScaleMark.addPoint( xLoc, startYLoc +
                                  ((maxCellRow + 1) *
                                   ViewConstants.RESOURCE_PROFILE_CELL_HEIGHT) + 2);
          jGoExtentView.getDocument().addObjectAtTail( timeScaleMark);
        }
      });
  } // end createTimeMarkItem


  private void createOverviewWindowItem( JMenuItem overviewWindowItem,
                                         final ResourceProfileView resourceProfileView,
                                         final Point viewCoords) {
    overviewWindowItem.addActionListener( new ActionListener() { 
        public void actionPerformed( ActionEvent evt) {
          VizViewOverview currentOverview =
            ViewGenerics.openOverviewFrame( PlanWorks.RESOURCE_PROFILE_VIEW, partialPlan,
                                            resourceProfileView, viewSet, jGoExtentView,
                                            viewCoords);
          if (currentOverview != null) {
            overview = currentOverview;
          }
        }
      });
  } // end createOverviewWindowItem

  /**
   * <code>TimeScaleMark</code> - color the mark and provide its time value
   *                              as a tool tip.
   *
   */
  class TimeScaleMark extends JGoStroke {

    private int xLoc;

    /**
     * <code>TimeScaleMark</code> - constructor 
     *
     * @param xLoc - <code>int</code> - 
     */
    public TimeScaleMark( int xLoc) {
      super();
      this.xLoc = xLoc;
      setDraggable( false);
      setResizable( false);
      setPen( new JGoPen( JGoPen.SOLID, 1,  ColorMap.getColor( "red")));
    }

    public String getToolTipText() {
      return String.valueOf( scaleXLoc( xLoc) + 1);
    }

  } // end class TimeScaleMark


    

} // end class ResourceProfileView
 



